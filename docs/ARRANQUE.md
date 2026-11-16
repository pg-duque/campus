# Arranque y comprobaciones de CampusHub

## 1. Elegir el entorno y preparar la configuración

Usamos Java 21, Maven y Docker Compose. Ejecutamos los comandos desde la carpeta
que contiene `compose.yaml`. Los bloques Bash de esta guía corresponden a WSL;
los de PowerShell se indican por separado.

Abrir WSL no convierte una pestaña de PowerShell en una terminal Linux. Para
usar sus comandos entramos en WSL o anteponemos `wsl --exec` desde PowerShell.
Ambos clientes pueden usar el mismo Docker, pero en clase encontramos que estaban
consultando entornos distintos.

En la terminal elegida comprobamos:

```bash
docker context show
docker compose ps
docker ps --no-trunc
```

Desde PowerShell, para consultar el Docker de WSL:

```powershell
wsl --exec docker context show
wsl --exec docker compose ps
wsl --exec docker ps --no-trunc
```

Comparamos los identificadores de contenedor: el nombre del contexto por sí solo
no garantiza que sea el mismo servidor. Mantenemos la misma distribución de WSL
y la misma carpeta del proyecto. Si elegimos el Docker de Windows, usamos
`docker` directamente en PowerShell durante todos los pasos.

La primera vez copiamos la plantilla:

```bash
cp .env.example .env
```

En PowerShell usamos `Copy-Item .env.example .env`. Si ya tenemos `.env`, lo
conservamos. Completamos `POSTGRES_DB`, `POSTGRES_USER` y `POSTGRES_PASSWORD`.
La plantilla incluye las conexiones locales de MongoDB y Redis. No subimos
`.env` ni credenciales reales al repositorio.

Cada línea tiene el formato `NOMBRE=valor`, sin comillas ni `export`. Evitamos
espacios, barras inversas y los símbolos `$` y `#` en estos valores. Spring Boot
carga el archivo desde su directorio de trabajo y Compose desde el proyecto.
Las variables de entorno y los argumentos de IntelliJ pueden sustituirlos.
Cambiar `.env` no modifica las credenciales de un volumen ya inicializado.

## 2. Arrancar las bases y Java

```bash
docker compose up -d
docker compose ps
docker compose port postgres 5432
docker compose port mongo 27017
docker compose port redis 6379
```

Las bases deben estar `healthy`. Los puertos publicados son `5433`, `27018` y
`6379`, respectivamente. El `27017` que muestra `mongosh` al entrar con
`docker compose exec` es el puerto interno; es correcto aunque Java use `27018`.

En IntelliJ ejecutamos `CampusHubApplication`, con la carpeta del proyecto como
directorio de trabajo. En **Run > Edit Configurations > Program arguments**:

```text
--spring.docker.compose.enabled=false
```

Es un argumento para Java, no un comando de PowerShell. Si necesitamos indicar
la conexión explícitamente, podemos añadir en ese mismo campo:

```text
--spring.mongodb.uri=mongodb://127.0.0.1:27018/campushub
--spring.mongodb.database=campushub
```

Son las propiedades de Spring Boot 4, usado en este proyecto. `application.yaml`
ya establece estos valores por defecto. Revisamos que no queden argumentos
antiguos apuntando a `27017` o a otra base.

Como alternativa, con Maven disponible en la terminal:

```bash
mvn spring-boot:run "-Dspring-boot.run.arguments=--spring.docker.compose.enabled=false"
```

Esperamos al arranque completo. Hibernate crea las tablas de PostgreSQL en este
primer arranque. Después paramos Java, manteniendo las bases encendidas.

## 3. Aplicar restricciones e índices

Ejecutamos los cuatro scripts en el mismo entorno Docker. En WSL:

```bash
docker compose exec -T postgres sh -c 'psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -v ON_ERROR_STOP=1' < src/main/resources/db/fecha_nacimiento.sql
docker compose exec -T postgres sh -c 'psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -v ON_ERROR_STOP=1' < src/main/resources/db/campos_obligatorios.sql
docker compose exec -T postgres sh -c 'psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -v ON_ERROR_STOP=1' < src/main/resources/db/indices_postgres.sql
docker compose exec -T mongo mongosh --quiet campushub --file /dev/stdin < src/main/resources/db/validacion_mongo.js
```

Desde PowerShell, para ese mismo Docker de WSL:

```powershell
$OutputEncoding = [System.Text.UTF8Encoding]::new()
Get-Content -Raw -Encoding UTF8 .\src\main\resources\db\fecha_nacimiento.sql |
    wsl --exec docker compose exec -T postgres sh -c 'psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -v ON_ERROR_STOP=1'
Get-Content -Raw -Encoding UTF8 .\src\main\resources\db\campos_obligatorios.sql |
    wsl --exec docker compose exec -T postgres sh -c 'psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -v ON_ERROR_STOP=1'
Get-Content -Raw -Encoding UTF8 .\src\main\resources\db\indices_postgres.sql |
    wsl --exec docker compose exec -T postgres sh -c 'psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -v ON_ERROR_STOP=1'
Get-Content -Raw -Encoding UTF8 .\src\main\resources\db\validacion_mongo.js |
    wsl --exec docker compose exec -T mongo mongosh --quiet campushub --file /dev/stdin
```

Si trabajamos con el Docker de Windows, quitamos `wsl --exec` en estos comandos.
La asignación de `OutputEncoding` conserva las tildes al enviar los archivos por
una tubería en PowerShell 5.1.

| Script | Resultado que comprobamos |
|---|---|
| `fecha_nacimiento.sql` | `ck_estudiantes_fecha_nacimiento_pasada` con `validada = t`. |
| `campos_obligatorios.sql` | `fecha_nacimiento`, `grupo_id` y `departamento_id` con `is_nullable = NO`. |
| `indices_postgres.sql` | `ix_estudiantes_grupo`, `ix_docentes_departamento` e `ix_grupos_tutor`. |
| `validacion_mongo.js` | Un `OK` para actividades, inscripciones, seguimientos y actividades_etiquetas. |

Podemos repetir los scripts. Si hay datos que incumplen las reglas, se detienen;
no los corrigen ni los borran. Las reglas solo se aplican en el servidor y la
base donde los ejecutamos. Después volvemos a arrancar Java.

## 4. Crear datos desde Swagger

Abrimos [Swagger](http://localhost:8080/swagger-ui.html). Creamos los registros
en este orden, usando los identificadores reales de cada respuesta. Los `1` son
ejemplos; si ya hay datos usamos nombres y correos nuevos para evitar duplicados.

| Registro | Petición | Cuerpo de ejemplo |
|---|---|---|
| Departamento | `POST /api/departamentos` | `{"nombre":"Informática"}` |
| Docente | `POST /api/docentes` | `{"nombre":"Ana","apellidos":"García","email":"ana@example.test","departamentoId":1}` |
| Grupo | `POST /api/grupos` | `{"nombre":"2DAM","nivel":"Segundo","tutorDocenteId":1}` |
| Estudiante | `POST /api/estudiantes` | `{"nombre":"Julia","apellidos":"Prueba","email":"julia@example.test","fechaNacimiento":"2000-01-01","grupoId":1}` |
| Actividad | `POST /api/actividades` | `{"titulo":"Prueba de persistencia","descripcion":"Taller Java","tipo":"TALLER","fechaPrevista":"2027-01-20","maxParticipantes":30,"docenteId":1}` |
| Inscripción | `POST /api/actividades/1/inscripciones` | `{"estudianteId":1}` |
| Seguimiento | `POST /api/actividades/1/seguimientos` | `{"tipo":"NOTA","datos":{"texto":"Material preparado","aulaLista":true}}` |
| Etiqueta | `POST /api/etiquetas` | `{"nombre":"Programación"}` |
| Asociación | `PUT /api/actividades/1/etiquetas/1` | Sin cuerpo. |
| Contacto | `PUT /api/estudiantes/1/contacto` | `{"tutorLegal":"Tutor de prueba","telefono":"600000000","email":"tutor@example.test","autorizado":true,"fechaAutorizacion":"2026-11-16"}` |

Las altas devuelven `201`, la asociación `204` y el contacto `200`. Consultamos
`GET /api/actividades/1/detalle`, `GET /api/estudiantes/1/actividades` y
`GET /api/estudiantes/1/contacto`.

## 5. Localizar los datos y las reglas

### PostgreSQL

En WSL:

```bash
docker compose exec postgres sh -c 'psql -U "$POSTGRES_USER" -d "$POSTGRES_DB"'
```

Dentro de `psql`:

```sql
\conninfo
\dt public.*
SELECT id, nombre, apellidos, email FROM public.estudiantes ORDER BY id;
SELECT e.id, e.nombre, g.nombre AS grupo
FROM public.estudiantes e JOIN public.grupos g ON e.grupo_id = g.id;
SELECT * FROM public.docentes;
\d public.estudiantes
\d public.docentes
\d public.grupos
```

Las últimas órdenes muestran campos, restricciones e índices. Salimos con `\q`.
Desde PowerShell, para el Docker de WSL, anteponemos `wsl --exec` al comando de
entrada, no a las consultas SQL.

### MongoDB

```bash
docker compose exec mongo mongosh campushub
```

Dentro de `mongosh`, ejecutamos cada orden por separado:

```javascript
db.getName()
db.serverStatus().host
show collections
db.actividades.find({}, { _id: 1, titulo: 1 }).toArray()
db.actividades.findOne({ titulo: "Prueba de persistencia" })
db.inscripciones.find({ actividadId: NumberLong("1") }).toArray()
db.seguimientos.find({ actividadId: NumberLong("1") }).sort({ creadoEn: 1 }).toArray()
db.actividades_etiquetas.find({ actividadId: NumberLong("1") }).toArray()
db.inscripciones.getIndexes()
db.seguimientos.getIndexes()
db.actividades_etiquetas.getIndexes()
db.getCollectionInfos({ name: "actividades" })[0].options
```

Después del script deben aparecer `validator`, `validationLevel: 'strict'` y
`validationAction: 'error'`. Si sale `{}`, las reglas no están aplicadas en esa
colección. Si la colección no existe, revisamos primero la base y el servidor.

Para cambiar de base usamos `use campushub`; escribir solo `campushub` no cambia
de base. `show collections` lleva un espacio. `secuencias` guarda contadores,
no actividades; no hace falta modificarla para consultar datos. Salimos con `exit`.

### Redis

```bash
docker compose exec redis redis-cli
```

Dentro de `redis-cli`:

```text
SCAN 0 MATCH contactos:* COUNT 100
HGETALL contactos:1
TTL contactos:1
GET cache:actividad:1:detalle
TTL cache:actividad:1:detalle
```

La ficha tiene `TTL = -1`: no caduca. `TTL = -2` significa que la clave no existe.
Un valor positivo indica los segundos restantes. `SCAN` puede necesitar más
llamadas usando el cursor devuelto hasta que vuelva a `0`.

## 6. Comprobar la caché de actividades

Usamos una actividad existente y sustituimos su identificador en estos pasos:

1. Consultamos `GET /api/actividades/1/detalle`. En Redis, `GET cache:actividad:1:detalle` debe devolver el JSON y `TTL` un valor entre 0 y 60.
2. Repetimos el GET antes de que caduque. En el registro de Java debe aparecer que se ha leído de la caché. La lectura no reinicia los 60 segundos.
3. Añadimos un seguimiento desde Swagger. Antes de consultar otra vez el detalle comprobamos que `TTL cache:actividad:1:detalle` devuelve `-2`.
4. Consultamos el detalle: debe incluir el nuevo seguimiento y volver a crear la clave. Las inscripciones y las asociaciones de etiquetas también borran la copia.
5. Dejamos pasar 60 segundos sin nuevas consultas. La clave debe desaparecer; el siguiente GET vuelve a calcular el detalle.

Para probar un fallo de la caché salimos de `redis-cli`, ejecutamos
`docker compose stop redis` y consultamos el detalle. El código está preparado
para consultar PostgreSQL y MongoDB aunque falle Redis. Las fichas de contacto
sí dependen de Redis y no estarán disponibles durante esa prueba. Al terminar
ejecutamos `docker compose up -d redis`.

## 7. Comprobar la persistencia y terminar

Anotamos los identificadores creados, paramos Java y reiniciamos las bases:

```bash
docker compose restart
docker compose ps
```

Cuando estén listas, arrancamos Java y repetimos las consultas de los mismos
registros. Los datos deben seguir en PostgreSQL, MongoDB y en las fichas de Redis.
La caché puede haber caducado y se vuelve a generar al pedir el detalle.

Para terminar paramos Java y usamos `docker compose stop`. No usamos
`docker compose down -v`: borra los volúmenes y sus datos. Esta guía describe las
comprobaciones que podemos repetir; no sustituye a revisar sus resultados.

## Problemas de conexión que encontramos

| Síntoma | Comprobación |
|---|---|
| Swagger crea registros pero la consola no los encuentra. | Comparar los contenedores de Windows y WSL, los puertos y la base usada por Java. |
| MongoDB no muestra colecciones en `campushub`. | Revisar si los datos antiguos quedaron en `test` con `use test` y `show collections`. Cambiar la configuración no mueve documentos. |
| No existe `public.estudiantes`. | Revisar base, servidor y primer arranque de Java. Si Swagger ya tiene estudiantes pero no hay ninguna tabla, comprobar primero si consultamos otro entorno. |
| PostgreSQL rechaza el usuario o la contraseña. | Revisar las credenciales del volumen existente y que Java use el puerto publicado `5433`. |
| Hibernate no puede determinar el dialecto. | Revisar el error de conexión anterior, el puerto y el estado de PostgreSQL. |
| `mongosh` no se reconoce. | Usar `docker compose exec mongo mongosh campushub`; el cliente está dentro del contenedor. |
| Las reglas de MongoDB salen vacías. | Aplicar `validacion_mongo.js` en el servidor correcto y consultar las opciones otra vez. |

`docker compose ps -q mongo` y `docker compose ps -q postgres` muestran los
identificadores de los contenedores, que cambian al recrearlos. Si necesitamos
contrastar la conexión con Java, podemos inspeccionar en el depurador
`mongoTemplate.getDb().getName()` y el campo `host` del resultado de
`mongoTemplate.getDb().runCommand(new org.bson.Document("serverStatus", 1))`.
La base y el servidor deben coincidir con los de la consola.

Las variables para cambiar conexiones son `JDBC_URL`, `JDBC_USER`, `JDBC_PASSWORD`,
`MONGODB_URI`, `REDIS_HOST`, `REDIS_PORT` y `PORT`. Las direcciones locales sirven
cuando Java se ejecuta en el ordenador. Si Java se ejecuta en otro contenedor,
`localhost` apunta a ese contenedor y hay que configurar las conexiones.
