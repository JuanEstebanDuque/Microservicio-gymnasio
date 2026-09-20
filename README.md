# Microservicio Gimnasio

Reestructuración de un monolito de gestión de gimnasio en **microservicios**, aplicando DDD:
cada Bounded Context es un servicio independiente con su propia base de datos.

El diagrama de componentes está en [`DiagramaComponentes.pdf`](DiagramaComponentes.pdf).

## Estructura del repositorio

```
.
├── DiagramaComponentes.pdf
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

## Pruebas con Postman

En [`postman/`](postman) hay una colección con todos los endpoints de los 4 servicios y un entorno local:

1. En Postman: **Import** → seleccionar `postman/Gimnasio-Microservicios.postman_collection.json` y `postman/Gimnasio-Local.postman_environment.json`.
2. Elegir el entorno **Gimnasio - Local** (arriba a la derecha).
3. Con los servicios levantados, ejecutar las peticiones. El entorno define las URLs (`miembro_url`, `equipo_url`, `clase_url`, `entrenador_url`, puertos 8080-8083) y las variables `entrenador_id` y `clase_id`, que hay que rellenar con el id devuelto al crear cada recurso.

La colección incluye el caso de error de clases con un entrenador inexistente (responde `400`).

## Prueba rápida

```bash
curl -X POST http://localhost:8083/api/entrenador -H "Content-Type: application/json" \
  -d '{"nombre":"Carlos Rodríguez","especialidad":"Yoga"}'

curl -X POST http://localhost:8082/api/clase -H "Content-Type: application/json" \
  -d '{"nombre":"Yoga Matutino","horario":"2026-09-01T08:00:00","capacidadMaxima":20,"entrenadorId":1}'
```

## Seguridad

Los archivos `.env` están excluidos del repositorio. No subir credenciales reales de base de datos.
