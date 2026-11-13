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

Los DTO fijan el contrato del cliente, **no** el diseño relacional o documental. El alumno
puede elegir entidades, colecciones, tablas intermedias y tipos internos. Los identificadores
visibles en la API son `Long` salvo los registros de seguimiento, cuyo ID es `String`.

La propiedad `datos` de `CreateSeguimientoRequest` es un objeto JSON de claves libres, para
permitir distintos seguimientos. Se debe conservar su estructura y permitir recuperarla.
Los seguimientos se guardan como documentos independientes en MongoDB: `tipo` es texto libre y
`datos` se conserva como un mapa BSON, sin convertirlo a clases específicas por tipo de actividad.
Cada alta crea un documento nuevo con su propio instante de creación; así se pueden añadir tipos
y estructuras sin sobrescribir ni migrar registros anteriores. El índice por actividad y fecha
permite recuperar el historial cronológicamente.

## Errores

- 400: datos malformados o inválidos que no tienen sentido como conflicto con datos existentes.
- 404: estudiante, docente, actividad o etiqueta requeridos inexistentes.
- 409: inscripción duplicada, falta de plazas o violación de unicidad de recursos existentes.
- 501: stub todavía no implementado (solo durante el desarrollo).

En el detalle: una actividad sin inscripciones, etiquetas o seguimientos debe devolver listas
vacías, no `null`. Las plazas disponibles se calculan como capacidad - ocupadas.
El detalle agrega actividad y seguimientos desde MongoDB, docente y estudiantes desde PostgreSQL,
y las etiquetas vinculando sus IDs almacenados en MongoDB con sus datos en PostgreSQL. Las plazas
ocupadas se calculan a partir de las inscripciones recuperadas para que coincidan con los
estudiantes incluidos en la misma respuesta.

## Decisión deliberada: DTO sin Bean Validation exhaustiva

No se han añadido `@NotNull`, `@NotBlank`, `@Size`, `@Email` ni `@Min` para que el
alumno tenga que implementar tanto reglas de negocio como restricciones de persistencia.
El parser JSON puede rechazar entradas estructuralmente mal formadas, pero eso no sustituye
la protección del almacenamiento frente a inserciones inválidas.
