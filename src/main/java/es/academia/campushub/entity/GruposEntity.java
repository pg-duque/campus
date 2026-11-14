package es.academia.campushub.entity;


import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "grupos",
        indexes = @Index(name = "ix_grupos_tutor", columnList = "tutor_docente_id"))
public class GruposEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "nivel", nullable = false, length = 100)
    private String nivel;

    @ManyToOne(optional = false)
    @JoinColumn(name = "tutor_docente_id", nullable = false)
    private DocenteEntity docenteTutor;

    @OneToMany(mappedBy = "grupo")
    private List<EstudiantesEntity> estudiantes = new ArrayList<>();

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

    public String getNivel() {
        return nivel;
    }

    public void setNivel(String nivel) {
        this.nivel = nivel;
    }

    public DocenteEntity getDocenteTutor() {
        return docenteTutor;
    }

    public void setDocenteTutor(DocenteEntity docenteTutor) {
        this.docenteTutor = docenteTutor;
    }
}
