package es.academia.campushub.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "actividades")
public record ActividadEntity(
        @Id String id,
        String titulo,
        String descripcion,
        List<String> tipos,
        int nmaxParticipantes,
        Long docenteId

){}
