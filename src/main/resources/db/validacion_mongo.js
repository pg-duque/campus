// Ejecutar con mongosh sobre la base campushub. Se puede repetir.
// Crea las colecciones que falten o actualiza sus validadores.
// No modifica documentos. Antes de aplicar cambios comprueba los datos existentes.
(function () {
  const campusDb = db.getSiblingDB("campushub");

  // Spring guarda los Long como enteros BSON y las fechas de inscripción/seguimiento como Date.
  const idNumerico = { bsonType: ["int", "long"], minimum: 1 };
  const idDocumento = { bsonType: ["objectId", "string"] };

  const colecciones = [
    {
      nombre: "actividades",
      esquema: {
        bsonType: "object",
        required: ["_id", "titulo", "tipo", "maxParticipantes", "docenteId"],
        properties: {
          _id: idNumerico,
          titulo: { bsonType: "string", minLength: 1, maxLength: 250, pattern: "\\S" },
          // Siguen siendo opcionales, como en el servicio actual.
          descripcion: { bsonType: ["string", "null"] },
          fechaPrevista: { bsonType: ["string", "null"], pattern: "^[0-9]{4}-[0-9]{2}-[0-9]{2}$" },
          tipo: { bsonType: "string", enum: ["TALLER", "CHARLA", "VISITA", "JORNADAS_TECNOLOGICAS"] },
          maxParticipantes: { bsonType: "int", minimum: 1 },
          docenteId: idNumerico
        }
      },
      indices: []
    },
    {
      nombre: "inscripciones",
      esquema: {
        bsonType: "object",
        required: ["_id", "actividadId", "estudianteId", "fechaInscripcion"],
        properties: {
          _id: idNumerico,
          actividadId: idNumerico,
          estudianteId: idNumerico,
          fechaInscripcion: { bsonType: "date" }
        }
      },
      indices: [
        {
          campos: { actividadId: 1, estudianteId: 1 },
          opciones: { name: "uk_inscripcion_actividad_estudiante", unique: true }
        }
      ]
    },
    {
      nombre: "seguimientos",
      esquema: {
        bsonType: "object",
        required: ["_id", "actividadId", "tipo", "datos", "creadoEn"],
        properties: {
          _id: idDocumento,
          actividadId: idNumerico,
          tipo: { bsonType: "string", minLength: 1, pattern: "\\S" },
          // Solo se exige un objeto. Sus campos internos, listas y objetos anidados son libres.
          datos: { bsonType: "object" },
          creadoEn: { bsonType: "date" }
        }
      },
      indices: [
        {
          campos: { actividadId: 1, creadoEn: 1 },
          opciones: { name: "ix_seguimientos_actividad_fecha" }
        }
      ]
    },
    {
      nombre: "actividades_etiquetas",
      esquema: {
        bsonType: "object",
        required: ["_id", "actividadId", "etiquetaId"],
        properties: {
          _id: idDocumento,
          actividadId: idNumerico,
          etiquetaId: idNumerico
        }
      },
      indices: [
        {
          campos: { actividadId: 1, etiquetaId: 1 },
          opciones: { name: "uk_actividad_etiqueta", unique: true }
        }
      ]
    }
  ];

  // Primera pasada: comprobar todos los documentos y las parejas únicas antes de cambiar reglas.
  for (const configuracion of colecciones) {
    const coleccion = campusDb.getCollection(configuracion.nombre);
    const invalidos = coleccion.countDocuments({
      $nor: [{ $jsonSchema: configuracion.esquema }]
    });
    if (invalidos > 0) {
      throw new Error(configuracion.nombre + ": hay " + invalidos
        + " documentos incompatibles. Revisarlos antes de aplicar las reglas.");
    }
    for (const indice of configuracion.indices) {
      if (indice.opciones.unique) {
        const pareja = {};
        for (const campo of Object.keys(indice.campos)) {
          pareja[campo] = "$" + campo;
        }
        const duplicados = coleccion.aggregate([
          { $group: { _id: pareja, cantidad: { $sum: 1 } } },
          { $match: { cantidad: { $gt: 1 } } },
          { $limit: 1 }
        ]).toArray();
        if (duplicados.length > 0) {
          throw new Error(configuracion.nombre + ": hay relaciones duplicadas. Revisarlas antes de crear el índice único.");
        }
      }
    }
  }

  for (const configuracion of colecciones) {
    const opciones = {
      validator: { $jsonSchema: configuracion.esquema },
      validationLevel: "strict",
      validationAction: "error"
    };
    const existe = campusDb.getCollectionInfos({ name: configuracion.nombre }).length > 0;
    const comando = existe
      ? Object.assign({ collMod: configuracion.nombre }, opciones)
      : Object.assign({ create: configuracion.nombre }, opciones);
    const resultado = campusDb.runCommand(comando);
    if (resultado.ok !== 1) {
      throw new Error("No se pudo configurar " + configuracion.nombre + ": " + resultado.errmsg);
    }
    for (const indice of configuracion.indices) {
      campusDb.getCollection(configuracion.nombre).createIndex(indice.campos, indice.opciones);
    }
    print("OK: " + configuracion.nombre + " — validación strict/error e índices aplicados");
  }

  print("La estructura interna de datos en seguimientos sigue siendo libre.");
  print("Las referencias a PostgreSQL se comprueban desde el servicio; este script no crea claves foráneas entre motores.");
})();
