package es.academia.campushub.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import es.academia.campushub.document.SecuenciaMongo;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

class SecuenciaMongoServiceTest {
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

}
