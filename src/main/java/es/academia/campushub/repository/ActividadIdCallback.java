package es.academia.campushub.repository;

import es.academia.campushub.entity.ActividadEntity;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertCallback;
import org.springframework.stereotype.Component;

@Component
public class ActividadIdCallback implements BeforeConvertCallback<ActividadEntity> {
  private final SecuenciaMongoService secuencias;

  public ActividadIdCallback(SecuenciaMongoService secuencias) {
    this.secuencias = secuencias;
  }

  @Override
  public ActividadEntity onBeforeConvert(ActividadEntity actividad, String collection) {
    if (actividad.actividadId() != null) {
      return actividad;
    }
    // Los records son inmutables: devolvemos una copia con el identificador asignado.
    return new ActividadEntity(
        secuencias.siguiente(collection), actividad.titulo(), actividad.descripcion(),
        actividad.tipo(), actividad.fechaPrevista(), actividad.maxParticipantes(),
        actividad.docenteId());
  }
}
