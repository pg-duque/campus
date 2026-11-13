package es.academia.campushub.entity;

import java.time.LocalDateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "inscripciones")
@CompoundIndex(
    name = "uk_inscripcion_actividad_estudiante",
    def = "{'actividadId': 1, 'estudianteId': 1}",
    unique = true)
public class InscripcionEntity {
  @Id private Long id;

  @Field("actividadId")
  private Long actividadId;

  @Field("estudianteId")
  private Long estudianteId;

  @Field("fechaInscripcion")
  private LocalDateTime fechaInscripcion;

  protected InscripcionEntity() {}

  public InscripcionEntity(Long actividadId, Long estudianteId) {
    this.actividadId = actividadId;
    this.estudianteId = estudianteId;
    this.fechaInscripcion = LocalDateTime.now();
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getActividadId() {
    return actividadId;
  }

  public Long getEstudianteId() {
    return estudianteId;
  }

  public LocalDateTime getFechaInscripcion() {
    return fechaInscripcion;
  }
}
