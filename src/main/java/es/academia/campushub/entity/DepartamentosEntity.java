package es.academia.campushub.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "departamentos")
public class DepartamentosEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", unique = true, nullable = false, length = 100)
    private String nombre;




}
