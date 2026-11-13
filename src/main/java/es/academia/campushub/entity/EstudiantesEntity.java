package es.academia.campushub.entity;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "estudiantes")
public class EstudiantesEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "apellidos", nullable = false, length = 100)
    private String apellidos;

    @Column(name = "email", nullable = false, length = 50, unique = true)
    private String email;

    @Column(name = "fecha_nacimiento", length = 50)
    private LocalDate fechaNacimiento;

    @ManyToOne
    @JoinColumn(name = "grupo_id")
    private GruposEntity grupo;

    public EstudiantesEntity() {
    }

    public EstudiantesEntity(Long id, String nombre, String apellido, String email, LocalDate fechaNacimiento) {
        this.id = id;
        this.nombre = nombre;
        this.apellidos = apellido;
        this.email = email;
        this.fechaNacimiento = fechaNacimiento;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellidos;
    }

    public void setApellido(String apellido) {
        this.apellidos = apellidos;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public String getApellidos() {
        return apellidos;
    }

    public Long getGrupoId() {
        return grupo == null ? null : grupo.getId();
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }
}
