# microservicio-entrenadores

Bounded Context **Gestión de Entrenadores**: CRUD de los entrenadores del gimnasio.
Es consultado por `microservicio-clases` para validar que un entrenador existe.

- Puerto: **8083**
- Base de datos propia: `entrenador_bd` (PostgreSQL)
- Stack: Spring Boot, Java 17, Spring Data JPA, Lombok, spring-dotenv
- No depende de otros microservicios.

## Modelo

`Entrenador`: `id`, `nombre`, `especialidad`.

## Endpoints (`/api/entrenador`)

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/api/entrenador` | Crea un entrenador |
| GET | `/api/entrenador` | Lista los entrenadores |
| GET | `/api/entrenador/{id}` | Obtiene uno por id (usado por clases) |
| PUT | `/api/entrenador/{id}` | Actualiza un entrenador |
| DELETE | `/api/entrenador/{id}` | Elimina un entrenador |

## Configuración

`spring-dotenv` lee un `.env` local (no versionado) con:

```
DB_URL=jdbc:postgresql://localhost:5432/gimnasio_test
DB_USER=postgres
DB_PASSWORD=postgres
```

`docker-compose.yml` levanta un PostgreSQL 16 local (`gimnasio_test`, puerto 5432):

```bash
docker compose up -d
```

## Ejecución

```bash
./mvnw spring-boot:run        # Windows: mvnw.cmd spring-boot:run
```

Con Docker:

```bash
docker build -t microservicio-entrenadores .
docker run -p 8083:8083 --env-file .env microservicio-entrenadores
```

`test-entrenador.sh` contiene peticiones de prueba contra el servicio.

## Ejemplo

```bash
curl -X POST http://localhost:8083/api/entrenador -H "Content-Type: application/json" \
  -d '{"nombre":"Carlos Rodríguez","especialidad":"Yoga"}'
```
