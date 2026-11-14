package es.academia.campushub.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "docentes",
        indexes = @Index(name = "ix_docentes_departamento", columnList = "departamento_id"))
public class DocenteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long docenteId;

    @Column(nullable = false, length = 250)
    private String docenteNombre;

    @Column(nullable = false, length = 250)
    private String docenteApellido;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El formato del correo no es válido")
    @Column(name = "docente_correo_electronico", nullable = false, unique = true, length = 250)
    private String docenteCorreoElectronico;

    @ManyToOne(optional = false)
    @JoinColumn(name = "departamento_id", nullable = false)
    private DepartamentosEntity departamento;

    public Long getDocenteId() {
        return docenteId;
    }

    public void setDocenteId(Long docenteId) {
        this.docenteId = docenteId;
    }

    public String getDocenteNombre() {
        return docenteNombre;
    }

    public void setDocenteNombre(String docenteNombre) {
        this.docenteNombre = docenteNombre;
    }

    public String getDocenteApellido() {
        return docenteApellido;
    }

    public void setDocenteApellido(String docenteApellido) {
        this.docenteApellido = docenteApellido;
    }

    public String getDocenteCorreoElectronico() {
        return docenteCorreoElectronico;
    }

    public void setDocenteCorreoElectronico(String docenteCorreoElectronico) {
        this.docenteCorreoElectronico = docenteCorreoElectronico;
    }

    public DepartamentosEntity getDepartamento() {
        return departamento;
    }

    public void setDepartamento(DepartamentosEntity departamento) {
        this.departamento = departamento;
    }
}
