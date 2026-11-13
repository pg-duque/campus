package es.academia.campushub.entity;

import java.time.Instant;
import java.util.Map;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "seguimientos")
@CompoundIndex(name = "ix_seguimientos_actividad_fecha", def = "{'actividadId': 1, 'creadoEn': 1}")
public class SeguimientoEntity {
  @Id private String id;

  private Long actividadId;

  private String tipo;

  private Map<String, Object> datos;

  private Instant creadoEn;

  protected SeguimientoEntity() {}

  public SeguimientoEntity(
      Long actividadId, String tipo, Map<String, Object> datos, Instant creadoEn) {
    this.actividadId = actividadId;
    this.tipo = tipo;
    this.datos = datos;
    this.creadoEn = creadoEn;
  }

  public String getId() {
    return id;
  }

  public Long getActividadId() {
    return actividadId;
  }

  public String getTipo() {
    return tipo;
  }

  public Map<String, Object> getDatos() {
    return datos;
  }

  public Instant getCreadoEn() {
    return creadoEn;
  }
}
