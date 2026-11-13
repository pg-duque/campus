package es.academia.campushub.repository;

import es.academia.campushub.entity.InscripcionEntity;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertCallback;
import org.springframework.stereotype.Component;

@Component
public class InscripcionIdCallback implements BeforeConvertCallback<InscripcionEntity> {
  private final SecuenciaMongoService secuencias;

  public InscripcionIdCallback(SecuenciaMongoService secuencias) {
    this.secuencias = secuencias;
  }

  @Override
  public InscripcionEntity onBeforeConvert(InscripcionEntity inscripcion, String collection) {
    if (inscripcion.getId() == null) {
      inscripcion.setId(secuencias.siguiente(collection));
    }
    return inscripcion;
  }
}
