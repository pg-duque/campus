# Cómo arrancar y usar CampusHub

Estos pasos son para ejecutar nuestra versión del proyecto. Los comandos se
ejecutan desde la carpeta del proyecto. Hemos dejado el README del profesor
como estaba.

## Qué necesitamos

- Java 21.
- IntelliJ con Maven configurado, o Maven 3.9 o posterior si usamos la terminal.
- Docker Desktop abierto.
- Si usamos WSL, activar su integración en Docker Desktop.

## 1. Arrancar las bases de datos

En PowerShell:

```powershell
docker compose up -d
docker compose ps
```

Deben aparecer `postgres`, `mongo` y `redis` con estado `healthy`.
Este Compose arranca las bases de datos. La aplicación Java la arrancamos aparte.

| Base de datos | Dirección | Datos para conectar |
|---|---|---|
| PostgreSQL | `localhost:5433` | Base, usuario y contraseña: `campushub`. |
| MongoDB | `localhost:27017` | Base: `campushub`. Sin usuario ni contraseña en esta práctica. |
| Redis | `localhost:6379` | Base 0. Sin contraseña en esta práctica. |

PostgreSQL usa el puerto **5433 en nuestro ordenador** y el 5432 dentro del
contenedor. Lo cambiamos para que no chocara con otra instalación de PostgreSQL.
No hace falta crear la base con pgAdmin: Docker la crea la primera vez.

## 2. Arrancar Java por primera vez

En IntelliJ abrimos el proyecto como Maven, seleccionamos Java 21 y ejecutamos
`es.academia.campushub.CampusHubApplication`.

Como ya hemos arrancado las bases con Docker, añadimos esto en los argumentos
del programa de la configuración de ejecución:

```text
--spring.docker.compose.enabled=false
```

También se puede arrancar desde la terminal, si tenemos Maven instalado:

```powershell
mvn spring-boot:run "-Dspring-boot.run.arguments=--spring.docker.compose.enabled=false"
```

Esperamos a que la aplicación termine de arrancar. Hibernate crea las tablas
en este primer arranque. Por eso los scripts SQL se ejecutan después.

## 3. Ejecutar los scripts

Paramos Java, pero dejamos Docker y las bases encendidas. Ejecutamos estos cuatro
comandos en PowerShell.

### Comprobar la fecha de nacimiento

```powershell
Get-Content -Raw .\src\main\resources\db\fecha_nacimiento.sql |
    docker compose exec -T postgres psql -U campushub -d campushub -v ON_ERROR_STOP=1
```

Al final debe aparecer `ck_estudiantes_fecha_nacimiento_pasada` con
`validada = t`. Esto significa que la regla de fecha de nacimiento está aplicada.

### Marcar los campos obligatorios

```powershell
Get-Content -Raw .\src\main\resources\db\campos_obligatorios.sql |
    docker compose exec -T postgres psql -U campushub -d campushub -v ON_ERROR_STOP=1
```

Deben aparecer `fecha_nacimiento`, `grupo_id` y `departamento_id` con
`is_nullable = NO`. Así no se pueden guardar sin valor.

### Añadir los índices de PostgreSQL

```powershell
Get-Content -Raw .\src\main\resources\db\indices_postgres.sql |
    docker compose exec -T postgres psql -U campushub -d campushub -v ON_ERROR_STOP=1
```

Deben aparecer los tres índices: `ix_estudiantes_grupo`,
`ix_docentes_departamento` e `ix_grupos_tutor`.

### Aplicar las reglas e índices de MongoDB

```powershell
Get-Content -Raw .\src\main\resources\db\validacion_mongo.js |
    docker compose exec -T mongo mongosh --quiet campushub --file /dev/stdin
```

Debe aparecer un `OK` para cada colección: `actividades`, `inscripciones`,
`seguimientos` y `actividades_etiquetas`.

Los scripts se pueden volver a ejecutar. Si hay datos que no cumplen las reglas,
avisan y se detienen. Hay que revisar esos datos; los scripts no los arreglan
ni los borran.

## 4. Probar la aplicación

Volvemos a arrancar Java y abrimos
[Swagger](http://localhost:8080/swagger-ui.html). Desde ahí podemos enviar las
peticiones. La lista de operaciones también está en
[OpenAPI](http://localhost:8080/v3/api-docs).

Primero creamos el departamento, después el docente, el grupo, el estudiante y
la actividad. En cada paso usamos los identificadores que devuelve la aplicación.
En los ejemplos hemos puesto `1`, pero en otra base pueden ser distintos.

| Qué creamos | Petición | Ejemplo del cuerpo |
|---|---|---|
| Departamento | `POST /api/departamentos` | `{"nombre":"Informática"}` |
| Docente | `POST /api/docentes` | `{"nombre":"Ana","apellidos":"García","email":"ana@example.test","departamentoId":1}` |
| Grupo | `POST /api/grupos` | `{"nombre":"2DAM","nivel":"Segundo","tutorDocenteId":1}` |
| Estudiante | `POST /api/estudiantes` | `{"nombre":"Pablo","apellidos":"Prueba","email":"pablo@example.test","fechaNacimiento":"2000-01-01","grupoId":1}` |
| Actividad | `POST /api/actividades` | `{"titulo":"Taller Java","descripcion":"Actividad de programación","tipo":"TALLER","fechaPrevista":"2027-01-20","maxParticipantes":1,"docenteId":1}` |
| Inscripción | `POST /api/actividades/1/inscripciones` | `{"estudianteId":1}` |
| Etiqueta | `POST /api/etiquetas` | `{"nombre":"Programación"}` |
| Asociación con la etiqueta | `PUT /api/actividades/1/etiquetas/1` | Sin cuerpo. |
| Seguimiento | `POST /api/actividades/1/seguimientos` | `{"tipo":"NOTA","datos":{"texto":"Material preparado","aulaLista":true}}` |
| Contacto | `PUT /api/estudiantes/1/contacto` | `{"tutorLegal":"Tutor de prueba","telefono":"600000000","email":"tutor@example.test","autorizado":true,"fechaAutorizacion":"2026-11-14"}` |

Después podemos consultar los resultados con estas peticiones GET:

| Qué consultamos | Ruta |
|---|---|
| Estudiante | `/api/estudiantes/1` |
| Actividad | `/api/actividades/1` |
| Participantes | `/api/actividades/1/inscripciones` |
| Historial del estudiante | `/api/estudiantes/1/actividades` |
| Seguimientos | `/api/actividades/1/seguimientos` |
| Actividades de una etiqueta | `/api/etiquetas/1/actividades` |
| Contacto | `/api/estudiantes/1/contacto` |
| Detalle completo | `/api/actividades/1/detalle` |

## 5. Parar y volver a arrancar

Paramos Java desde IntelliJ o con `Ctrl+C` si lo arrancamos en la terminal.
Para parar las bases:

```powershell
docker compose stop
```

Para arrancarlas otra vez:

```powershell
docker compose up -d
```

Después arrancamos Java y consultamos los mismos identificadores. Los datos
deberían seguir ahí porque se guardan en los volúmenes de Docker.

**No usar `docker compose down -v` para esta prueba:** ese comando elimina los
volúmenes y los datos guardados.

## Si algo falla

| Problema | Qué revisar |
|---|---|
| WSL no encuentra `docker` | Que Docker Desktop esté abierto y tenga activada la integración con WSL. |
| Hibernate dice que no puede determinar el dialecto | Mirar el error de conexión que aparece antes. Comprobar que las bases estén arrancadas, el puerto 5433 y las credenciales. |
| No existe el usuario o rol `campushub` | Que Java esté conectando al PostgreSQL de Docker en 5433 y no a otro PostgreSQL del ordenador. |
| No existe la tabla `public.estudiantes` | Arrancar Java primero para que cree las tablas. |
| Un script rechaza datos | Revisar qué registros incumplen la regla y corregirlos antes de repetir el comando. |

Las conexiones están en `application.yaml`. Se pueden cambiar con las variables
`JDBC_URL`, `JDBC_USER`, `JDBC_PASSWORD`, `MONGODB_URI`, `REDIS_HOST`,
`REDIS_PORT` y `PORT`. Si la base de PostgreSQL ya estaba creada, cambiar el
usuario o contraseña en Compose no cambia los que estaban guardados.
