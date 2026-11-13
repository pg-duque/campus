package es.academia.campushub.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "actividades_etiquetas")
@CompoundIndex(
    name = "uk_actividad_etiqueta",
    def = "{'actividadId': 1, 'etiquetaId': 1}",
    unique = true)
public record ActividadEtiquetaEntity(
    @Id String id, String actividadId, Long etiquetaId) {

  public ActividadEtiquetaEntity(String actividadId, Long etiquetaId) {
    this(null, actividadId, etiquetaId);
  }
}
