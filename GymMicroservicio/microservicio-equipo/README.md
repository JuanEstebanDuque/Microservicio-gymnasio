# microservicio-equipo

Bounded Context **Gestión de Equipos**: inventario de equipos (máquinas, implementos) del gimnasio.

- Puerto: **8081**
- Base de datos propia: `equipos_db` (PostgreSQL)
- Stack: Spring Boot, Java 17, Spring Data JPA, Lombok
- No depende de otros microservicios.

## Modelo

`Equipo`: `id`, `nombre`, `descripcion`, `cantidad`.

## Endpoints (`/api/equipo`)

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/api/equipo` | Registra un equipo |
| GET | `/api/equipo` | Lista los equipos |

## Ejecución

```bash
./mvnw spring-boot:run        # Windows: mvnw.cmd spring-boot:run
```

Con Docker:

```bash
docker build -t microservicio-equipo .
docker run -p 8081:8081 --env-file .env microservicio-equipo
```

Configuración en `src/main/resources/application.properties`; las credenciales pueden sobrescribirse con un `.env` local (no versionado).

## Ejemplo

```bash
curl -X POST http://localhost:8081/api/equipo -H "Content-Type: application/json" \
  -d '{"nombre":"Mancuernas","descripcion":"Par de 10 kg","cantidad":12}'
```

## Seguridad y documentación

Requiere JWT de Keycloak (realm `gimnasio`, ver README raíz). Roles: POST: ADMIN · GET: ADMIN, TRAINER, MEMBER.
Swagger UI: http://localhost:8081/swagger-ui.html
