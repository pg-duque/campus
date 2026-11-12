package es.academia.campushub.entity;

import jakarta.persistence.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "actividades")
public record ActividadEntity(
        @Id String id,
        String titulo,
        String descripcion,
        List<String> tipos,
        int nmaxParticipantes,
        String docenteResponsable

){}
