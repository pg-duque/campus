package es.academia.campushub.entity;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "estudiantes")
public class EstudiantesEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 100)
    private String apellido;

    @Column(nullable = false, length = 50, unique = true)
    private String email;

    @Column(length = 50)
    private LocalDate fechaNacimiento;
}
