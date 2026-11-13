package es.academia.campushub.repository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import es.academia.campushub.document.SecuenciaMongo;
import es.academia.campushub.entity.ActividadEntity;
import es.academia.campushub.entity.InscripcionEntity;
import es.academia.campushub.entity.enums.TipoActividad;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

class IdentificadoresMongoTest {
  @Test
  void secuenciaIncrementaEnMongoYDevuelveElNuevoValor() {
    MongoOperations mongo = mock(MongoOperations.class);
    when(mongo.findAndModify(any(Query.class), any(Update.class),
        any(FindAndModifyOptions.class), eq(SecuenciaMongo.class)))
        .thenReturn(new SecuenciaMongo("actividades", 1L),
            new SecuenciaMongo("actividades", 2L));
    SecuenciaMongoService servicio = new SecuenciaMongoService(mongo);

    assertEquals(1L, servicio.siguiente("actividades"));
    assertEquals(2L, servicio.siguiente("actividades"));

    var query = ArgumentCaptor.forClass(Query.class);
    var update = ArgumentCaptor.forClass(Update.class);
    var opciones = ArgumentCaptor.forClass(FindAndModifyOptions.class);
    verify(mongo, times(2)).findAndModify(
        query.capture(), update.capture(), opciones.capture(), eq(SecuenciaMongo.class));
    assertEquals("actividades", query.getValue().getQueryObject().get("_id"));
    assertEquals(new org.bson.Document("valor", 1L),
        update.getValue().getUpdateObject().get("$inc"));
    assertTrue(opciones.getValue().isUpsert());
    assertTrue(opciones.getValue().isReturnNew());
  }

  @Test
  void actividadNuevaRecibeIdYConservaLosDatos() {
    SecuenciaMongoService secuencias = mock(SecuenciaMongoService.class);
    when(secuencias.siguiente("actividades")).thenReturn(7L);
    ActividadEntity original = new ActividadEntity(
        null, "Charla", "Descripción", TipoActividad.CHARLAS, "2027-01-01", 10, 12L);
    ActividadIdCallback callback = new ActividadIdCallback(secuencias);

    ActividadEntity guardada = callback.onBeforeConvert(original, "actividades");

    assertEquals(new ActividadEntity(
        7L, "Charla", "Descripción", TipoActividad.CHARLAS, "2027-01-01", 10, 12L), guardada);
    assertSame(guardada, callback.onBeforeConvert(guardada, "actividades"));
    verify(secuencias, times(1)).siguiente("actividades");
  }

  @Test
  void inscripcionRecibeIdSoloLaPrimeraVez() {
    SecuenciaMongoService secuencias = mock(SecuenciaMongoService.class);
    when(secuencias.siguiente("inscripciones")).thenReturn(9L);
    InscripcionEntity inscripcion = new InscripcionEntity(7L, 23L);
    var fecha = inscripcion.getFechaInscripcion();
    InscripcionIdCallback callback = new InscripcionIdCallback(secuencias);

    assertSame(inscripcion, callback.onBeforeConvert(inscripcion, "inscripciones"));
    assertEquals(9L, inscripcion.getId());
    assertEquals(7L, inscripcion.getActividadId());
    assertEquals(23L, inscripcion.getEstudianteId());
    assertEquals(fecha, inscripcion.getFechaInscripcion());
    callback.onBeforeConvert(inscripcion, "inscripciones");
    verify(secuencias, times(1)).siguiente("inscripciones");
  }
}
