# CampusHub

Proyecto de Spring Boot para gestionar estudiantes, docentes, grupos y actividades.
Guardamos los datos con relaciones en PostgreSQL, las actividades y sus registros
en MongoDB, y las fichas de contacto en Redis. También usamos Redis como caché
del detalle de una actividad durante 60 segundos.

El documento inicial del profesor se conserva en
[README_PROFESOR.md](README_PROFESOR.md). Las instrucciones de nuestra versión son
las de este README y la [guía de arranque](docs/ARRANQUE.md).

## Requisitos

- Java 21.
- Maven 3.9 o posterior, instalado o disponible desde IntelliJ.
- Docker con Docker Compose. En Windows usamos Docker Desktop.
- Si ejecutamos Docker desde WSL, integración con la distribución correspondiente.

Ejecutamos los comandos desde la carpeta del proyecto. Podemos usar PowerShell o
WSL, manteniendo el mismo entorno Docker para el arranque, las consultas y los
scripts. En clase encontramos que ambos clientes apuntaban a bases distintas.
La guía explica cómo comprobarlo.

## Configuración y arranque

La primera vez copiamos la plantilla. En PowerShell:

```powershell
Copy-Item .env.example .env
```

En WSL usamos `cp .env.example .env`. Si ya existe `.env`, conservamos sus datos.
Completamos sus valores locales. Compose y Spring Boot lo leen al arrancar desde
la carpeta del proyecto. Usamos `NOMBRE=valor`, sin comillas ni `export`, y valores
sin espacios, barras inversas ni los símbolos `$` y `#` para que ambos lo lean igual.
El archivo queda excluido de Git. Cambiar las credenciales en `.env` no modifica
las de una base ya creada.

En el entorno Docker elegido:

```bash
docker compose up -d
docker compose ps
```

Cuando estén `healthy`, ejecutamos `CampusHubApplication` en IntelliJ, con la
carpeta del proyecto como directorio de trabajo y este **argumento del programa**:

```text
--spring.docker.compose.enabled=false
```

También podemos arrancar con Maven:

```bash
mvn spring-boot:run "-Dspring-boot.run.arguments=--spring.docker.compose.enabled=false"
```

| Servicio | Dirección desde el ordenador | Puerto dentro del contenedor |
|---|---|---|
| PostgreSQL | `localhost:5433` | `5432` |
| MongoDB | `localhost:27018`, base `campushub` | `27017` |
| Redis | `localhost:6379` | `6379` |

Después del primer arranque de Java aplicamos los cuatro scripts de
[preparación de las bases](docs/ARRANQUE.md#3-aplicar-restricciones-e-índices).
Hibernate necesita crear las tablas antes de ejecutar los scripts SQL.

Probamos la API desde [Swagger](http://localhost:8080/swagger-ui.html).
La guía contiene ejemplos y consultas para comprobar dónde queda cada dato.

## Pruebas

```bash
mvn test
mvn verify
```

`mvn test` ejecuta las pruebas unitarias. `mvn verify` incluye las de integración
y la revisión del formato. Testcontainers necesita acceso a Docker y crea
contenedores separados de los datos del proyecto.

## Consultas directas

Estos comandos usan el Docker de la terminal actual. Si utilizamos el de WSL
desde PowerShell, ponemos `wsl --exec` delante de `docker`.

### PostgreSQL

```bash
docker compose exec postgres sh -c 'psql -U "$POSTGRES_USER" -d "$POSTGRES_DB"'
```

Dentro de `psql`:

```sql
\conninfo
\dt public.*
SELECT id, nombre, apellidos, email, grupo_id FROM public.estudiantes;
SELECT * FROM public.docentes;
```

Salimos con `\q`.

### MongoDB

```bash
docker compose exec mongo mongosh campushub
```

Dentro de `mongosh`:

```javascript
db.getName()
show collections
db.actividades.find().toArray()
db.actividades.findOne({ _id: NumberLong("1") })
db.inscripciones.find({ actividadId: NumberLong("1") }).toArray()
db.seguimientos.find({ actividadId: NumberLong("1") }).sort({ creadoEn: 1 }).toArray()
```

Sustituimos los identificadores por los de los registros creados. Salimos con `exit`.

### Redis

```bash
docker compose exec redis redis-cli
```

Dentro de `redis-cli`, para el estudiante y la actividad de ejemplo:

```text
HGETALL contactos:1
GET cache:actividad:1:detalle
TTL cache:actividad:1:detalle
```

La ficha es un hash sin caducidad. El detalle es un texto JSON que aparece al
consultar `GET /api/actividades/1/detalle` y caduca a los 60 segundos.
Salimos con `exit`.

## Detener y reiniciar

Paramos Java y detenemos las bases con `docker compose stop`. Para volver a
arrancarlas usamos `docker compose up -d` y ejecutamos Java otra vez.
También podemos reiniciar las bases directamente con `docker compose restart`.

Los datos se guardan en volúmenes. `docker compose down -v` elimina esos volúmenes,
por lo que no lo usamos para comprobar la persistencia. La caché puede caducar;
los datos originales de la actividad siguen en PostgreSQL y MongoDB.

## Documentación

- [Modelo de datos](docs/MODELO_DATOS.md): diagrama, colecciones, índices y restricciones.
- [Memoria](docs/MEMORIA.md): decisiones, alternativas y limitaciones.
- [Arranque y comprobaciones](docs/ARRANQUE.md): preparación, scripts, Swagger y consultas.
- [Contrato de la API](docs/api-contract.md): rutas y respuestas.

El control de plazas y la coordinación de la caché con `synchronized` están
pensados para una sola instancia de la aplicación.
