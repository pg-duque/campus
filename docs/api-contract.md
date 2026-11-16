# Contrato HTTP — CampusHub

Todos los cuerpos son JSON con `Content-Type: application/json`.
Campos `fechaNacimiento`, `fechaPrevista` y `fechaAutorizacion` son strings ISO `YYYY-MM-DD`.
`creadoEn` y `fechaInscripcion` de las respuestas son strings ISO-8601 de fecha/hora.

| Método | Ruta | DTO de entrada | DTO de salida | Éxito |
|---|---|---|---|---|
| POST | `/api/departamentos` | CreateDepartamentoRequest | DepartamentoResponse | 201 |
| POST | `/api/docentes` | CreateDocenteRequest | DocenteResponse | 201 |
| POST | `/api/grupos` | CreateGrupoRequest | GrupoResponse | 201 |
| POST | `/api/estudiantes` | CreateEstudianteRequest | EstudianteResponse | 201 |
| GET | `/api/estudiantes/{id}` | — | EstudianteResponse | 200 |
| POST | `/api/actividades` | CreateActividadRequest | ActividadResponse | 201 |
| GET | `/api/actividades/{id}` | — | ActividadResponse | 200 |
| POST | `/api/actividades/{id}/inscripciones` | CreateInscripcionRequest | InscripcionResponse | 201 |
| GET | `/api/actividades/{id}/inscripciones` | — | EstudianteResponse[] | 200 |
| POST | `/api/actividades/{id}/seguimientos` | CreateSeguimientoRequest | SeguimientoResponse | 201 |
| GET | `/api/actividades/{id}/seguimientos` | — | SeguimientoResponse[] | 200 |
| GET | `/api/actividades/{id}/detalle` | — | ActividadDetalleResponse | 200 |
| GET | `/api/estudiantes/{id}/actividades` | — | ActividadHistorialResponse[] | 200 |
| PUT | `/api/estudiantes/{id}/contacto` | UpsertContactoRequest | ContactoResponse | 200 |
| GET | `/api/estudiantes/{id}/contacto` | — | ContactoResponse | 200 |
| POST | `/api/etiquetas` | CreateEtiquetaRequest | EtiquetaResponse | 201 |
| PUT | `/api/actividades/{id}/etiquetas/{etiquetaId}` | — | — | 204 |
| GET | `/api/etiquetas/{id}/actividades` | — | ActividadResponse[] | 200 |

## DTO y persistencia

Los DTO definen los datos que recibe y devuelve la API. Las entidades organizan
cómo los guardamos en cada base de datos. Los identificadores
visibles en la API son `Long` salvo los registros de seguimiento, cuyo ID es `String`.

La propiedad `datos` de `CreateSeguimientoRequest` es un objeto JSON de claves libres, para
permitir distintos seguimientos. Conservamos su estructura al guardar y consultar.
Los seguimientos se guardan como documentos independientes en MongoDB: `tipo` es texto libre y
`datos` se conserva como un mapa BSON, sin convertirlo a clases específicas por tipo de actividad.
Cada alta crea un documento nuevo con su propio instante de creación; así se pueden añadir tipos
y estructuras sin sobrescribir ni migrar registros anteriores. El índice por actividad y fecha
permite recuperar el historial cronológicamente.

## Errores

- 400: datos malformados o inválidos que no tienen sentido como conflicto con datos existentes.
- 404: estudiante, docente, actividad o etiqueta requeridos inexistentes.
- 409: inscripción duplicada, falta de plazas o violación de unicidad de recursos existentes.

En el detalle, una actividad sin inscripciones, etiquetas o seguimientos devuelve listas
vacías, no `null`. Las plazas disponibles se calculan como capacidad - ocupadas.
El detalle agrega actividad y seguimientos desde MongoDB, docente y estudiantes desde PostgreSQL,
y las etiquetas vinculando sus IDs almacenados en MongoDB con sus datos en PostgreSQL. Las plazas
ocupadas se calculan a partir de las inscripciones recuperadas para que coincidan con los
estudiantes incluidos en la misma respuesta.

## Comprobación de datos

Los DTO no contienen todas las anotaciones de validación. Comprobamos los campos
y las reglas de negocio en el servicio y aplicamos restricciones en las bases.
El parser JSON puede rechazar entradas estructuralmente mal formadas, pero eso no sustituye
la protección del almacenamiento frente a inserciones inválidas.

## Caché del detalle

`GET /api/actividades/{id}/detalle` consulta primero Redis. Si hay una copia,
devuelve su contenido; si no, calcula la respuesta con PostgreSQL y MongoDB y la
guarda durante 60 segundos. El cuerpo y el código HTTP son los mismos en ambos casos.

Las inscripciones, los seguimientos y las asociaciones de etiquetas borran la
copia de la actividad afectada. Si Redis falla, el servicio intenta calcular el
detalle con las otras bases. Las rutas de contacto siguen dependiendo de Redis.
