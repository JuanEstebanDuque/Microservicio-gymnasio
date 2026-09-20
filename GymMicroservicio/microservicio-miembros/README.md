# microservicio-miembros

Bounded Context **Gestión de Miembros**: registro y consulta de los miembros del gimnasio.

- Puerto: **8080**
- Base de datos propia: `miembros_db` (PostgreSQL)
- Stack: Spring Boot, Java 17, Spring Data JPA, Lombok
- No depende de otros microservicios.

## Modelo

`Miembro`: `id`, `nombre`, `email`, `fechaInscripcion` (`LocalDate`).

## Endpoints (`/api/miembro`)

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/api/miembro` | Registra un miembro |
| GET | `/api/miembro` | Lista los miembros |

## Ejecución

```bash
./mvnw spring-boot:run        # Windows: mvnw.cmd spring-boot:run
```

Con Docker:

```bash
docker build -t microservicio-miembros .
docker run -p 8080:8080 --env-file .env microservicio-miembros
```

Configuración en `src/main/resources/application.properties`; las credenciales pueden sobrescribirse con un `.env` local (no versionado).

## Ejemplo

```bash
curl -X POST http://localhost:8080/api/miembro -H "Content-Type: application/json" \
  -d '{"nombre":"Ana Pérez","email":"ana@mail.com","fechaInscripcion":"2026-09-01"}'
```

## Seguridad y documentación

Requiere JWT de Keycloak (realm `gimnasio`, ver README raíz). Roles: POST: ADMIN · GET: ADMIN, TRAINER.
Swagger UI: http://localhost:8080/swagger-ui.html
