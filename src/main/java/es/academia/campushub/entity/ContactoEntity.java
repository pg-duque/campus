package es.academia.campushub.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@RedisHash("contactos")
public class ContactoEntity {
  @Id
  private Long estudianteId;
  private String tutorLegal;
  private String telefono;
  private String email;
  private Boolean autorizado;
  private String fechaAutorizacion;

  public ContactoEntity() {}

  public ContactoEntity(Long estudianteId, String tutorLegal, String telefono, String email,
      Boolean autorizado, String fechaAutorizacion) {
    this.estudianteId = estudianteId;
    this.tutorLegal = tutorLegal;
    this.telefono = telefono;
    this.email = email;
    this.autorizado = autorizado;
    this.fechaAutorizacion = fechaAutorizacion;
  }

  public Long getEstudianteId() {
    return estudianteId;
  }

  public void setEstudianteId(Long estudianteId) {
    this.estudianteId = estudianteId;
  }

  public String getTutorLegal() {
    return tutorLegal;
  }

  public void setTutorLegal(String tutorLegal) {
    this.tutorLegal = tutorLegal;
  }

  public String getTelefono() {
    return telefono;
  }

  public void setTelefono(String telefono) {
    this.telefono = telefono;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public Boolean getAutorizado() {
    return autorizado;
  }

  public void setAutorizado(Boolean autorizado) {
    this.autorizado = autorizado;
  }

  public String getFechaAutorizacion() {
    return fechaAutorizacion;
  }

  public void setFechaAutorizacion(String fechaAutorizacion) {
    this.fechaAutorizacion = fechaAutorizacion;
  }
}
