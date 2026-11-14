package es.academia.campushub.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "actividades_etiquetas")
@CompoundIndexes({
    @CompoundIndex(
    name = "uk_actividad_etiqueta",
    def = "{'actividadId': 1, 'etiquetaId': 1}",
    unique = true),
    @CompoundIndex(name = "ix_actividades_etiquetas_etiqueta",
        def = "{'etiquetaId': 1}")
})
public record ActividadEtiquetaEntity(
    @Id String id, Long actividadId, Long etiquetaId) {

  public ActividadEtiquetaEntity(Long actividadId, Long etiquetaId) {
    this(null, actividadId, etiquetaId);
  }
}
