# microservicio-clase

Microservicio de **Gestión de Clases**, resultado de la descomposición del monolito
`monilito-gimnasio` en microservicios usando DDD. Sigue el mismo stack y estructura
que `microservicio-miembros` y `microservicio-equipo` (Spring Boot 4.1.1, Java 17,
Spring Data JPA + PostgreSQL, Lombok).

## Bounded Context

**Gestión de Clases**: programar clases, consultarlas y garantizar que cada clase
tenga asignado un entrenador válido.

### Aggregate root: `Clase`
- `id`
- `nombre`
- `horario`
- `capacidadMaxima`
- `entrenadorId` — referencia externa al microservicio de Entrenador.

En el monolito, `Clase` tenía una relación `@ManyToOne` hacia la entidad `Entrenador`
mapeada por JPA (mismo esquema/base de datos). Al independizar Entrenador en su
propio microservicio con su propia base de datos, esa relación ya no puede seguir
siendo un JOIN de base de datos: **cada microservicio es dueño exclusivo de sus
propios datos**. Por eso `Clase` solo guarda el `entrenadorId`, y la validez de esa
referencia (que el entrenador exista) se resuelve en tiempo de
ejecución con una llamada REST al microservicio de Entrenador — no con una FK.

## Comunicación entre microservicios

`EntrenadorClient` reutiliza el endpoint de consulta por id que expone
`microservicio-entrenadores` (ese servicio no tiene un endpoint de validación
dedicado, es un CRUD estándar) para validar que el entrenador exista antes de
programar una clase:

```
GET {entrenador.service.url}/api/entrenador/{id}
200 OK       -> { "id": 1, "nombre": "Carlos Rodríguez", "especialidad": "Yoga" }
404 Not Found -> el entrenador no existe
```

`entrenador.service.url` se configura en `application.properties` (por defecto
`http://localhost:8083`).

**Puertos usados en el taller:** miembros=8080, equipo=8081, clase=8082,
entrenador=8083. `microservicio-entrenadores` no trae `server.port` configurado
(por defecto usaría 8080, chocando con miembros) — agregar
`server.port=8083` a su `application.properties` antes de la demo.

### Manejo de errores al validar el entrenador
- Entrenador no existe (`404`) → `400 Bad Request`.
- Microservicio de Entrenador no responde (caído / timeout) → `503 Service Unavailable`.

Ver `exception/GlobalExceptionHandler.java`. (`microservicio-entrenadores` no
modela disponibilidad, solo existencia, así que esa es la única validación posible
desde Clase con el contrato actual.)

## Endpoints

| Método | Ruta               | Descripción                                                        |
|--------|--------------------|----------------------------------------------------------------------|
| POST   | `/api/clase`        | Programa una clase (valida el entrenador contra su microservicio).  |
| GET    | `/api/clase`        | Lista todas las clases.                                              |
| GET    | `/api/clase/{id}`   | Obtiene una clase por id.                                             |

Equivalen a `POST/GET /api/gimnasio/clases` del monolito.

## Configuración

`src/main/resources/application.properties`:
- `server.port=8082` (miembros usa 8080, equipo 8081, clase 8082; dejar 8083 libre para entrenador).
- `spring.datasource.*`: base de datos propia `clases_db` (independencia de datos por microservicio).
- `entrenador.service.url`: URL base del microservicio de Entrenador.

## Ejecución local

```bash
./mvnw spring-boot:run
```

O con Docker:

```bash
docker build -t microservicio-clase .
docker run -p 8082:8082 --env-file .env microservicio-clase
```

## Ejemplo de uso

```bash
curl -X POST http://localhost:8082/api/clase \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Yoga Matutino",
    "horario": "2026-09-01T08:00:00",
    "capacidadMaxima": 20,
    "entrenadorId": 1
  }'

curl http://localhost:8082/api/clase
```

## Seguridad y documentación

Requiere JWT de Keycloak (realm `gimnasio`, ver README raíz). Roles: POST: ADMIN, TRAINER · GET: ADMIN, TRAINER, MEMBER.
Swagger UI: http://localhost:8082/swagger-ui.html
