# Taller: API Gateway para los microservicios del gimnasio

En este documento explico qué hice para el taller del patrón API Gateway y qué cambió frente al
proyecto base, que eran los 4 microservicios del gimnasio (miembros, equipo, clases y entrenadores)
con Keycloak, RabbitMQ y Kafka ya funcionando.

## Qué tenía antes y qué problema resuelve el gateway

Antes, para usar el sistema había que conocer el puerto de cada microservicio: miembros en el 8080,
equipo en el 8081, clases en el 8082 y entrenadores en el 8083. Cada uno validaba el token por su
cuenta y, si quería armar una pantalla con datos de varios servicios, tenía que hacer varias
peticiones desde el cliente.

Con el gateway todo entra por un solo lugar, `http://localhost:8090`. El gateway decide a qué
servicio mandar cada petición, valida el token antes de dejarla pasar y además puede juntar
respuestas de varios servicios en una sola.

## Resumen de cambios frente al proyecto base

| Qué | Cambio |
|-----|--------|
| `api-gateway/` | Proyecto nuevo (Spring Cloud Gateway reactivo), puerto 8090 |
| `eureka-server/` | Proyecto nuevo (Eureka Server), puerto 8761 |
| Los 4 microservicios | Solo `pom.xml` y `application.properties`, para registrarse en Eureka. No toqué su lógica |
| `postman/` | Carpeta nueva "API Gateway (puerto 8090)" y variables `gateway_url` y `eureka_url` |
| `README.md` | Sección del gateway y nuevo orden de ejecución |
| `.gitignore` | Permitir las dos carpetas nuevas |

## Por qué son dos proyectos nuevos

Al principio no entendía por qué había que crear proyectos aparte en vez de agregar dependencias a
lo que ya tenía. La razón es que el gateway no es una funcionalidad de ningún microservicio: es
otro servicio, que corre en su propio proceso. Si lo metiera dentro de miembros, por ejemplo,
miembros sería a la vez negocio y enrutador de los demás, y si se cae se cae todo.

Además hay una razón técnica: Spring Cloud Gateway (la versión reactiva) corre sobre WebFlux y
Netty, mientras que mis microservicios usan Spring MVC y Tomcat, y no se pueden mezclar en la misma
aplicación. Por lo mismo Eureka Server tampoco puede vivir dentro del gateway, porque corre sobre
Servlet. Por eso quedaron dos proyectos: uno para el registro y otro para el gateway.

Con Eureka hay que distinguir dos cosas. El **cliente** sí es solo una dependencia, y se la puse al
gateway y a los 4 microservicios. El **servidor** es el registro en sí, y alguien tiene que
alojarlo; ese es el proyecto `eureka-server`.

## Paso 1: crear el proyecto

Lo generé en Spring Initializr con Spring Boot 4.1.1 y Java 17, igual que mis otros servicios.
Un detalle: la dependencia ya no aparece como "Spring Cloud Gateway". Ahora hay dos, "Gateway"
(sobre Servlet) y "Reactive Gateway". Usé **Reactive Gateway**, que es el Spring Cloud Gateway de
siempre y el que tiene `RouteLocator` y los filtros que pide el taller.

Dependencias del gateway: Reactive Gateway, Actuator, Security, OAuth2 Resource Server,
Eureka Discovery Client y Cloud LoadBalancer.

## Paso 2: enrutamiento

El taller pide rutas en `application.yml` y también una clase de configuración, así que hice las dos:

- En [application.yml](api-gateway/src/main/resources/application.yml) están las rutas de miembros,
  equipo y clases.
- En [RutasConfig.java](api-gateway/src/main/java/com/GymMicroservicio/api_gateway/config/RutasConfig.java)
  está la ruta de entrenadores y la del filtro de agregación, que necesita código.

Una ruta tiene tres partes: un `id`, un predicado (la condición, en mi caso que el path empiece por
algo) y la `uri` a donde se reenvía. Las rutas del YAML y las de Java se suman.

| Path | Servicio |
|---|---|
| `/api/miembro/**`, `/api/pago/**`, `/api/entrenamiento/**` | miembros |
| `/api/equipo/**` | equipo |
| `/api/clase/**`, `/api/ocupacion/**` | clases |
| `/api/entrenador/**` | entrenadores |

Miembros tiene tres paths porque ese microservicio tiene tres controladores, y clases tiene dos.
No tuve que reescribir paths porque mis controladores ya usaban `/api/...`.

Algo que cambió frente a la guía: en esta versión de Spring Cloud la propiedad de las rutas es
`spring.cloud.gateway.server.webflux.routes`, no `spring.cloud.gateway.routes`.

## Paso 3: seguridad con Keycloak

No tuve que crear nada en Keycloak porque el realm `gimnasio` ya existía con sus roles, clientes y
usuarios. Lo que hice fue convertir el gateway en *resource server* de ese mismo realm:

- En el `application.yml` puse el `issuer-uri` del realm. Con eso el gateway descarga las claves
  públicas de Keycloak y verifica la firma, el emisor y el vencimiento de cada token.
- En [SecurityConfig.java](api-gateway/src/main/java/com/GymMicroservicio/api_gateway/config/SecurityConfig.java)
  definí las reglas:
  - `/actuator/health` es público.
  - `DELETE /api/**` exige `ROLE_ADMIN`.
  - Todo lo demás exige un token válido.

La clase se parece a la de mis microservicios, pero usa las versiones reactivas
(`ServerHttpSecurity`, `SecurityWebFilterChain`). Reutilicé el mismo método que lee los roles de
`realm_access.roles`, porque Spring no los lee de ahí por defecto.

Quedaron **dos capas de autorización**: el gateway filtra lo general y cada microservicio sigue
decidiendo el detalle con sus `@PreAuthorize`.

Sobre el reenvío del token: en las rutas normales no hay que programar nada, porque el gateway es un
proxy y pasa el header `Authorization` tal cual. La prueba es que los microservicios siguen
aplicando sus reglas por rol. Donde sí lo reenvío a mano es en el filtro de agregación, porque ahí
el gateway hace llamadas nuevas.

## Paso 4: Eureka y balanceo de carga

**Eureka Server.** Solo necesitó la anotación `@EnableEurekaServer`, el puerto 8761 y dos
propiedades para que no intente registrarse en sí mismo.

**Microservicios.** A cada uno le agregué en el `pom.xml` la versión de Spring Cloud (`2025.1.3`),
la dependencia del cliente Eureka y el bloque `dependencyManagement`. En el `application.properties`
agregué la URL de Eureka y `eureka.instance.hostname=localhost`. Se registran con el
`spring.application.name` que ya tenían: `miembros`, `equipo`, `clase` y `entrenador`.

**Gateway.** Cambié las URIs de las rutas de `http://localhost:8080` a `lb://miembros` (y así con
las demás). `lb://` significa que el gateway le pregunta a Eureka dónde está el servicio y balancea
entre sus instancias en round-robin. El gateway ya no conoce puertos.

Para demostrar el balanceo levanto una segunda instancia de entrenadores en otro puerto:

```bash
cd GymMicroservicio/microservicio-entrenadores
./mvnw spring-boot:run -Dspring-boot.run.arguments=--server.port=8093
```

En Eureka aparecen 2 instancias de `ENTRENADOR`, y al llamar varias veces a `/api/entrenador` por el
gateway las consultas SQL se alternan entre las dos consolas.

## Paso 5: filtro de agregación

Pensé en la pantalla de inicio de la app de un miembro. Para pintarla se necesitan cuatro cosas que
están en tres servicios distintos, así que hice un filtro que las junta en una sola respuesta:
`GET /api/resumen/miembro/{id}`.

| Sección de la respuesta | Servicio | Endpoint que consulta |
|-------------------------|-----------|----------------------|
| `entrenamientoSemanal` | miembros | `/api/entrenamiento/resumen/{id}` |
| `clases` | clases | `/api/clase` |
| `ocupacionEnVivo` | clases | `/api/ocupacion` |
| `entrenadores` | entrenadores | `/api/entrenador` |

El filtro es [ResumenMiembroFilter.java](api-gateway/src/main/java/com/GymMicroservicio/api_gateway/filter/ResumenMiembroFilter.java).
Las decisiones que tomé:

- **Consultas en paralelo.** Uso `Mono.zip`, así que el panel tarda lo que tarde el servicio más
  lento y no la suma de los cuatro.
- **Reenvío del token.** Copio el header `Authorization` del usuario en cada llamada, para que cada
  microservicio aplique sus permisos.
- **Nombres de servicio.** Las URLs son `http://miembros/...`, no puertos. Funcionan porque el
  `WebClient` está marcado con `@LoadBalanced`, o sea que también pasa por Eureka.
- **Degradación parcial.** Si un servicio falla o está caído, su sección queda en `null` y el resto
  del panel responde igual. Me pareció mejor que el miembro vea parte de su pantalla a que vea un
  error completo.
- **El filtro responde él mismo.** Un filtro normal modifica la petición y la deja seguir. Este no
  la enruta a ningún servicio, por eso la ruta usa la URI `no://op`.
- **Solo ids numéricos.** La ruta es `/api/resumen/miembro/{miembroId:[0-9]+}`; con otra cosa
  responde `404` y nunca llega al filtro.

## Cómo lo pruebo

Orden para levantar todo: Docker (`docker compose up -d`), `eureka-server`, los 4 microservicios y
al final `api-gateway`.

En Postman agregué la carpeta **API Gateway (puerto 8090)** a la colección que ya tenía:

| Petición | Qué demuestra | Esperado |
|----------|---------------|----------|
| Enrutamiento (entrenadores, clases, equipo, miembros) | Las 4 rutas funcionan | `200` |
| Sin token a `/noexiste` | El rechazo es del gateway, porque ninguna ruta coincide | `401` |
| Token inválido | El gateway verifica la firma | `401` |
| DELETE con trainer o member | La regla de ADMIN del gateway | `403` |
| Health sin token | El único endpoint público | `200` |
| Eureka: servicios registrados | Los 4 servicios y el gateway están en el registro | `200` |
| Panel del miembro | El filtro de agregación | `200` con las 4 secciones |
| Panel con id no numérico | La validación de la ruta | `404` |

Para mostrar la degradación parcial detengo `microservicio-clases` y repito la petición del panel:
`clases` y `ocupacionEnVivo` salen en `null` y las otras dos secciones siguen con datos.

## Problemas que tuve

- **Maven no descargaba nada.** VS Code marcaba en rojo todos los imports de Spring. No era el
  código: mi `settings.xml` global de Maven había quedado de otro taller (el de Nexus) y redirigía
  todas las descargas a un servidor que no estaba configurado. Lo renombré y forcé la descarga con
  `./mvnw -U clean compile`.
- **Seguridad antes de configurarla.** Como el proyecto trae Spring Security, sin configuración el
  gateway bloqueaba todo. Para probar el enrutamiento usé primero una configuración provisional que
  dejaba pasar todo, y en el paso 3 la reemplacé por la de Keycloak.
- **Servicios sin poder conectarse.** En una prueba los logs se llenaron de errores de RabbitMQ y
  Kafka. Eran los contenedores de Docker, que estaban apagados.
- **`503` al arrancar.** Si levanto el gateway antes de que los servicios se registren, responde
  `503` unos 30 segundos, hasta que descarga el registro de Eureka.

## Qué mejoraría

- El panel hereda una limitación del microservicio de miembros: un usuario con rol MEMBER puede
  consultar el resumen de cualquier `miembroId`, porque no se valida que el id sea el del usuario
  del token.
- Clases sigue llamando a entrenadores con una URL fija (`entrenador.service.url`). Lo siguiente
  sería que también lo resuelva por Eureka.
