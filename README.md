# Microservicio Gimnasio

Reestructuración de un monolito de gestión de gimnasio en **microservicios**, aplicando DDD:
cada Bounded Context es un servicio independiente con su propia base de datos.

El diagrama de componentes está en [`DiagramaComponentes.pdf`](DiagramaComponentes.pdf).

## Estructura del repositorio

```
.
├── DiagramaComponentes.pdf
├── docker-compose.yml                # Keycloak
├── keycloak/                         # Realm gimnasio exportado
├── postman/                          # Colección y entorno de Postman
└── GymMicroservicio/
    ├── microservicio-miembros/       # Gestión de miembros       (puerto 8080)
    ├── microservicio-equipo/         # Gestión de equipos        (puerto 8081)
    ├── microservicio-clases/         # Gestión de clases         (puerto 8082)
    └── microservicio-entrenadores/   # Gestión de entrenadores   (puerto 8083)
```

Todos los servicios son proyectos Maven independientes (Spring Boot, Java 17, Spring Data JPA,
PostgreSQL, Lombok). Cada uno tiene su propio README con detalles.

| Servicio | Puerto | Base de datos | Ruta base | Depende de |
|---|---|---|---|---|
| [microservicio-miembros](GymMicroservicio/microservicio-miembros) | 8080 | `miembros_db` | `/api/miembro` | — |
| [microservicio-equipo](GymMicroservicio/microservicio-equipo) | 8081 | `equipos_db` | `/api/equipo` | — |
| [microservicio-clases](GymMicroservicio/microservicio-clases) | 8082 | `clases_db` | `/api/clase` | entrenadores (REST) |
| [microservicio-entrenadores](GymMicroservicio/microservicio-entrenadores) | 8083 | `entrenador_bd` | `/api/entrenador` | — |

## Conexiones entre servicios

Cada microservicio es dueño exclusivo de sus datos: **no hay claves foráneas ni JOINs entre servicios**.

La única comunicación síncrona actual es **clases → entrenadores**:

```
Cliente ──POST /api/clase──▶ microservicio-clases ──GET /api/entrenador/{id}──▶ microservicio-entrenadores
```

- `Clase` guarda solo `entrenadorId`; antes de crear una clase, `EntrenadorClient` verifica por REST que el entrenador existe.
- Entrenador no existe (404) → clases responde `400 Bad Request`.
- Entrenadores caído o sin respuesta → clases responde `503 Service Unavailable`.
- La URL se configura en `microservicio-clases/src/main/resources/application.properties` (`entrenador.service.url=http://localhost:8083`).

Miembros y equipo no se comunican con otros servicios.

## Requisitos

- JDK 17
- Maven o el wrapper `mvnw` incluido en cada servicio
- Una base de datos PostgreSQL por servicio (Neon en la nube o local con Docker)
- Docker (opcional)

## Configuración de base de datos

Cada servicio lee la conexión de las variables `DB_URL`, `DB_USER` y `DB_PASSWORD`, definidas en un
archivo `.env` dentro de la carpeta del servicio (**no se versiona**). Crearlo con los datos de tu base de datos:

```
DB_URL=jdbc:postgresql://<host>/<nombre_bd>?sslmode=require
DB_USER=<usuario>
DB_PASSWORD=<contraseña>
```

Para usar una base local, `microservicio-entrenadores/docker-compose.yml` levanta un PostgreSQL 16:

```bash
cd GymMicroservicio/microservicio-entrenadores
docker compose up -d      # postgres en localhost:5432, db gimnasio_test
```

`spring.jpa.hibernate.ddl-auto=update` crea las tablas automáticamente al arrancar.

## Ejecución

Levantar cada servicio en su propia terminal (**entrenadores debe estar arriba para poder crear clases**):

```bash
cd GymMicroservicio/microservicio-miembros       && ./mvnw spring-boot:run   # 8080
cd GymMicroservicio/microservicio-equipo         && ./mvnw spring-boot:run   # 8081
cd GymMicroservicio/microservicio-entrenadores   && ./mvnw spring-boot:run   # 8083
cd GymMicroservicio/microservicio-clases         && ./mvnw spring-boot:run   # 8082
```

En Windows usar `mvnw.cmd` en lugar de `./mvnw`.

### Con Docker

Cada servicio tiene su `Dockerfile` (build multi-etapa, Temurin 17):

```bash
cd GymMicroservicio/microservicio-miembros
docker build -t microservicio-miembros .
docker run -p 8080:8080 --env-file .env microservicio-miembros
```

Si clases y entrenadores corren en contenedores, `entrenador.service.url` no puede ser
`localhost`; debe apuntar al host/nombre del contenedor de entrenadores.

## Seguridad (Keycloak + JWT)

Todos los endpoints exigen un token JWT emitido por **Keycloak** (realm `gimnasio`). Los servicios son *resource servers*
de Spring Security: validan firma, emisor y expiración, y autorizan por rol con `@PreAuthorize`.

Levantar Keycloak (importa el realm `keycloak/realm-gimnasio.json` automáticamente):

```bash
docker compose up -d keycloak      # consola: http://localhost:8180  (admin / admin)
```

**Realm `gimnasio`** — clientes: `miembros-client`, `equipo-client`, `clases-client`, `entrenadores-client`.
Usuarios de prueba (solo para desarrollo):

| Usuario | Contraseña | Rol |
|---|---|---|
| `admin` | `admin123` | `ROLE_ADMIN` |
| `trainer` | `trainer123` | `ROLE_TRAINER` |
| `member` | `member123` | `ROLE_MEMBER` |

**Autorización por endpoint**

| Servicio | GET | POST / PUT / DELETE |
|---|---|---|
| miembros | ADMIN, TRAINER | ADMIN |
| equipo | ADMIN, TRAINER, MEMBER | ADMIN |
| entrenadores | ADMIN, TRAINER, MEMBER | ADMIN |
| clases | ADMIN, TRAINER, MEMBER | ADMIN, TRAINER |

Sin token, con token inválido o vencido → `401`. Con un rol sin permiso → `403`.
Cuando clases valida al entrenador, reenvía el JWT del usuario a `microservicio-entrenadores`.

Obtener un token:

```bash
curl -s -X POST http://localhost:8180/realms/gimnasio/protocol/openid-connect/token   -d "client_id=entrenadores-client&grant_type=password&username=admin&password=admin123"
# usar el campo access_token:  -H "Authorization: Bearer <token>"
```

## Documentación de la API (Swagger / OpenAPI)

Cada servicio publica su documentación (endpoints, parámetros y respuestas 200/201/400/401/403/404/503):

| Servicio | Swagger UI | OpenAPI JSON |
|---|---|---|
| miembros | http://localhost:8080/swagger-ui.html | `/v3/api-docs` |
| equipo | http://localhost:8081/swagger-ui.html | `/v3/api-docs` |
| clases | http://localhost:8082/swagger-ui.html | `/v3/api-docs` |
| entrenadores | http://localhost:8083/swagger-ui.html | `/v3/api-docs` |

En Swagger UI, usar el botón **Authorize** y pegar el `access_token`.

## Pruebas con Postman

En [`postman/`](postman) hay una colección con todos los endpoints de los 4 servicios y un entorno local:

1. En Postman: **Import** → seleccionar `postman/Gimnasio-Microservicios.postman_collection.json` y `postman/Gimnasio-Local.postman_environment.json`.
2. Elegir el entorno **Gimnasio - Local** (arriba a la derecha).
3. Con Keycloak y los servicios levantados, ejecutar primero la carpeta **Autenticación** (guarda el token del rol elegido en `token`) y luego las demás. La carpeta **Seguridad** prueba los casos 401 y 403.
4. Ejecutar las peticiones. El entorno define las URLs (`miembro_url`, `equipo_url`, `clase_url`, `entrenador_url`, puertos 8080-8083) y las variables `entrenador_id` y `clase_id`, que hay que rellenar con el id devuelto al crear cada recurso.

La colección incluye el caso de error de clases con un entrenador inexistente (responde `400`).

## Prueba rápida

```bash
curl -X POST http://localhost:8083/api/entrenador -H "Content-Type: application/json" \
  -d '{"nombre":"Carlos Rodríguez","especialidad":"Yoga"}'

curl -X POST http://localhost:8082/api/clase -H "Content-Type: application/json" \
  -d '{"nombre":"Yoga Matutino","horario":"2026-09-01T08:00:00","capacidadMaxima":20,"entrenadorId":1}'
```

## Secretos

Los archivos `.env` están excluidos del repositorio. No subir credenciales reales de base de datos.

## Comunicación asincrónica con RabbitMQ

Broker en el `docker-compose.yml` raíz (`docker compose up -d rabbitmq`): AMQP `5672`, consola `http://localhost:15672` (guest / guest).

| Función | Publica | Consume | Elementos |
|---|---|---|---|
| Notificación de inscripción | miembros (`POST /api/miembro`) | clases | exchange `inscripciones-exchange` (fanout) → `inscripciones-clases-queue` |
| Cambio de horario (pub/sub) | clases (`PUT /api/clase/{id}/horario`) | miembros y entrenadores | exchange `horarios-exchange` (fanout) → `horarios-miembros-queue` y `horarios-entrenadores-queue` |
| Pagos con DLQ | miembros (`POST /api/pago`, ADMIN/MEMBER) | miembros (`PagoProcessor`) | `pagos-queue` (TTL 30 s) → `pagos-dlq` |

El consumidor de pagos reintenta 3 veces (`spring.rabbitmq.listener.simple.retry`) y, si sigue fallando, lanza `AmqpRejectAndDontRequeueException`: RabbitMQ envía el mensaje a `pagos-dlq`. Un pago es inválido si el monto es ≤ 0 o mayor a 5.000.000.

```bash
# Pago inválido: termina en pagos-dlq (verlo en la consola de RabbitMQ)
curl -X POST http://localhost:8080/api/pago -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" -d '{"miembroId":1,"monto":-5}'
```
