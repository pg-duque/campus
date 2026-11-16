# Memoria de CampusHub

En este documento explicamos cómo hemos organizado los datos y por qué hemos
tomado estas decisiones. El diagrama y las tablas están en
[MODELO_DATOS.md](MODELO_DATOS.md). Los pasos para ejecutar el proyecto están en
[ARRANQUE.md](ARRANQUE.md). El [README](../README.md) incluye también cómo ejecutar
las pruebas y consultar los datos directamente en las bases.

## Qué guardamos en cada base de datos

| Base de datos | Qué guardamos | Por qué |
|---|---|---|
| PostgreSQL | Estudiantes, docentes, departamentos, grupos y etiquetas. | Tienen campos definidos y relaciones que podemos controlar con claves foráneas. |
| MongoDB | Actividades, inscripciones, seguimientos y etiquetas de cada actividad. | Nos permite trabajar con documentos. Es especialmente útil para los seguimientos, que pueden tener datos distintos. |
| Redis | La ficha de contacto y autorización del estudiante. | Podemos buscar la ficha usando directamente el identificador del estudiante. |

Podríamos haber guardado más información en PostgreSQL, pero el ejercicio nos
pide trabajar con distintos tipos de bases de datos. Hemos separado las
inscripciones de las actividades para poder buscar tanto los participantes de una
actividad como las actividades de un estudiante.

Cada seguimiento se guarda por separado. Así podemos añadir nuevas notas sin
modificar las anteriores. Su campo `datos` puede contener información diferente
según lo que necesitemos registrar.

La ficha de Redis no es una copia de otra base: es donde guardamos esos datos.
Por eso no le hemos puesto un tiempo de caducidad. Docker guarda sus archivos en
un volumen y Redis tiene activada la opción AOF, que registra los cambios para
poder recuperar los datos al arrancar. La caché de actividades del apartado
opcional no está hecha.

## Identificadores y relaciones

Usamos `Long` para los identificadores de estudiantes, docentes, grupos,
departamentos, etiquetas, actividades e inscripciones. PostgreSQL genera los
suyos automáticamente. Para actividades e inscripciones usamos
`SecuenciaMongoService`, que aumenta un contador de MongoDB cada vez que necesita
un número nuevo. Si falla un guardado, puede quedar algún número sin usar.

MongoDB genera el identificador de los seguimientos. Para relacionar una
actividad con una etiqueta, el servicio forma una cadena con sus dos
identificadores, por ejemplo `1:3`.

En PostgreSQL usamos claves foráneas: por ejemplo, el grupo de un estudiante debe
existir. No hemos puesto borrado en cascada, así que no se puede borrar un grupo
que todavía tenga estudiantes. La API actual no tiene operaciones para borrar.

Entre bases de datos distintas no podemos usar esas claves foráneas. Por eso el
servicio comprueba que el estudiante, docente o etiqueta existan antes de guardar
la relación.

## Comprobaciones e índices

El servicio revisa los datos recibidos. Además, las bases tienen sus propias
reglas: campos obligatorios, valores únicos, longitudes máximas y una fecha de
nacimiento anterior a hoy. Hemos añadido scripts para aplicar las reglas también
si las tablas ya estaban creadas.

En MongoDB se comprueban los campos y sus tipos. Por ejemplo, el número de plazas
debe ser mayor que cero. En los seguimientos, `datos` debe ser un objeto, pero sus
campos pueden cambiar. Para la fecha prevista, MongoDB comprueba el formato y
Java comprueba que la fecha exista.

Los índices ayudan a buscar los datos sin recorrerlos todos. Los hemos añadido
para las consultas del proyecto, como el historial de un estudiante o las
actividades de una etiqueta. Los índices únicos también impiden repetir una
inscripción o una relación entre actividad y etiqueta.

## Qué pasa con las plazas y qué limitaciones tenemos

Hemos usado `synchronized` en el método de inscripción. Si llegan dos peticiones
a la vez, una espera a que termine la otra antes de comprobar las plazas. Hemos
elegido esta opción porque es sencilla y podemos entenderla y explicarla.

Esto funciona mientras haya una sola aplicación ejecutándose. Si tuviéramos
varias copias de la aplicación, no se coordinarían entre ellas. Tampoco protege
frente a alguien que añada inscripciones directamente en MongoDB. Para esos
casos habría que cambiar la forma de controlar las plazas.

Las tres bases tampoco guardan todos los cambios como una única operación. Si
alguien cambia o borra datos directamente, pueden quedar relaciones incorrectas.
El servicio comprueba que los datos existan, pero no arregla automáticamente esos
problemas. Si Redis no funciona, no se podrá guardar ni consultar el contacto.

## Cambios que hemos ido haciendo

Hemos unificado los identificadores, quitado métodos repetidos y colocado la
generación de secuencias en el servicio. No usamos callbacks. También hemos
completado los métodos del servicio y añadido las reglas e índices de las bases.

Se valoró una solución con PostgreSQL para controlar las plazas desde varias
aplicaciones, pero hemos mantenido `synchronized` para que el proyecto sea más
sencillo. La limitación queda explicada arriba.
