package es.academia.campushub.service;

import es.academia.campushub.document.SecuenciaMongo;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

@Service
public class SecuenciaMongoService {
  private final MongoOperations mongo;

  public SecuenciaMongoService(MongoOperations mongo) {
    this.mongo = mongo;
  }

  public Long siguiente(String coleccion) {
    SecuenciaMongo secuencia = mongo.findAndModify(
        Query.query(Criteria.where("_id").is(coleccion)),
        new Update().inc("valor", 1L),
        FindAndModifyOptions.options().upsert(true).returnNew(true),
        SecuenciaMongo.class);
    if (secuencia == null) {
      throw new IllegalStateException("No se pudo generar el identificador de " + coleccion);
    }
    return secuencia.valor();
  }
}
