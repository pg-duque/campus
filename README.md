# CampusHub

Proyecto de Spring Boot para gestionar estudiantes, docentes, grupos y actividades.
Usamos PostgreSQL para los datos con relaciones, MongoDB para las actividades y
sus registros, y Redis para la ficha de contacto del estudiante.

El README inicial del profesor se conserva sin cambios en
[README_PROFESOR.md](README_PROFESOR.md).

## Requisitos

- Java 21.
- Maven 3.9 o posterior, instalado o configurado en IntelliJ.
- Docker Desktop abierto, con Docker Compose.
- Si usamos WSL, activar su integración en Docker Desktop.

Todos los comandos se ejecutan desde la carpeta del proyecto. Los ejemplos de
terminal son para PowerShell.

## Preparar la configuración

La primera vez, copiar la plantilla:

```powershell
Copy-Item .env.example .env
```

Abrir `.env` y completar `POSTGRES_DB`, `POSTGRES_USER` y `POSTGRES_PASSWORD`.
Los valores de la plantilla son ejemplos que hay que sustituir. Escribirlos como
`NOMBRE=valor`, sin comillas ni `export`. Para evitar diferencias entre cómo lo
leen Java y Docker, usar valores sin espacios, barras inversas ni los símbolos
`$` y `#`.

**Si ya tenéis una base creada, usad sus datos actuales.** Cambiar este archivo
no cambia el usuario ni la contraseña que ya tiene PostgreSQL.

Docker Compose lee `.env` y Spring Boot también lo carga al arrancar desde la
carpeta del proyecto. El archivo es local: está excluido de Git y del contexto
de construcción de Docker. La plantilla `.env.example` sí se sube al repositorio.

## Arrancar

Arrancar las bases y comprobar su estado:

```powershell
docker compose up -d
docker compose ps
```

Cuando aparezcan como `healthy`, arrancar Java:

```powershell
mvn spring-boot:run "-Dspring-boot.run.arguments=--spring.docker.compose.enabled=false"
```

En IntelliJ también se puede ejecutar `CampusHubApplication` con el argumento
`--spring.docker.compose.enabled=false` y la carpeta del proyecto como directorio
de trabajo.

PostgreSQL se publica en `localhost:5433`, MongoDB en `localhost:27017` y Redis
en `localhost:6379`.

**En la primera preparación hay que aplicar los scripts SQL y de MongoDB después
del primer arranque de Java**, cuando ya existan las tablas. Los comandos y sus
resultados esperados están en [ARRANQUE.md](docs/ARRANQUE.md#3-ejecutar-los-scripts).

La API se puede probar desde [Swagger](http://localhost:8080/swagger-ui.html).
Hay ejemplos de peticiones en [ARRANQUE.md](docs/ARRANQUE.md).

## Ejecutar las pruebas

Pruebas unitarias:

```powershell
mvn test
```

Pruebas de integración y comprobación del formato:

```powershell
mvn verify
```

Para las de integración, Docker Desktop debe estar abierto. Testcontainers crea
sus propios contenedores de prueba. `mvn verify` también revisa el formato del
código; si falla, hay que mirar qué comprobación ha fallado.

Aunque las pruebas no se evalúen en clase, dejamos los comandos para poder
ejecutar las que contiene el proyecto.

## Consultar directamente PostgreSQL

Entrar con el usuario y la base que tiene configurados el contenedor:

```powershell
docker compose exec postgres sh -c 'psql -U "$POSTGRES_USER" -d "$POSTGRES_DB"'
```

Dentro de `psql`:

```sql
-- Ver las tablas
\dt

-- Consultar estudiantes
SELECT id, nombre, apellidos, email, grupo_id FROM estudiantes;

-- Consultar estudiantes con su grupo
SELECT e.id, e.nombre, e.apellidos, g.nombre AS grupo
FROM estudiantes e
JOIN grupos g ON e.grupo_id = g.id;

-- Consultar docentes y departamentos
SELECT * FROM docentes;
SELECT * FROM departamentos;
```

Para buscar un registro concreto, se puede añadir `WHERE id = 1` a la consulta de
estudiantes, cambiando el número por uno que exista. Para salir: `\q`.

## Consultar directamente MongoDB

```powershell
docker compose exec mongo mongosh campushub
```

Dentro de `mongosh`:

```javascript
// Ver las colecciones
show collections

// Consultar actividades
db.actividades.find()

// Buscar una actividad concreta: sustituir 1 por su identificador
db.actividades.findOne({ _id: NumberLong("1") })

// Ver sus inscripciones por fecha
db.inscripciones.find({
  actividadId: NumberLong("1")
}).sort({ fechaInscripcion: 1 })

// Ver sus seguimientos
db.seguimientos.find({
  actividadId: NumberLong("1")
}).sort({ creadoEn: 1 })

// Ver los indices
db.inscripciones.getIndexes()
```

Para salir: `exit`.

## Consultar Redis

```powershell
docker compose exec redis redis-cli
```

Para ver la ficha del estudiante 1, cambiando el número por uno que tenga ficha:

```text
HGETALL contactos:1
```

Para salir: `exit`.

## Detener o reiniciar

Parar Java desde IntelliJ o con `Ctrl+C`. Después, parar las bases:

```powershell
docker compose stop
```

Para volver a arrancar las bases:

```powershell
docker compose up -d
```

También se pueden reiniciar directamente con `docker compose restart`.
Después hay que arrancar Java otra vez si estaba parado.

Los datos se guardan en volúmenes de Docker. **No usar `docker compose down -v`
si queremos conservarlos**, porque elimina esos volúmenes.

## Documentación de la entrega

- [Modelo de datos](docs/MODELO_DATOS.md): diagrama, colecciones, campos, índices y reglas.
- [Memoria](docs/MEMORIA.md): decisiones, alternativas y limitaciones.
- [Guía de arranque](docs/ARRANQUE.md): preparación, scripts y ejemplos.
- [Contrato de la API](docs/api-contract.md): rutas y respuestas.

Para demostrar que los datos están guardados, crear registros con Swagger y
localizarlos después con `psql`, `mongosh` y `redis-cli`. Tras reiniciar el
entorno, consultar los mismos identificadores.

La caché opcional del detalle de actividades no está implementada. El control de
plazas con `synchronized` está pensado para una sola aplicación ejecutándose.
