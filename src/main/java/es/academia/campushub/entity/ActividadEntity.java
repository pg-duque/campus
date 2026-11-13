package es.academia.campushub.entity;

import es.academia.campushub.entity.enums.TipoActividad;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "actividades")
public record ActividadEntity(
        @Id String actividadId,

        @NotBlank(message = "El título es obligatorio")
        String titulo,

        String descripcion,

        @NotNull(message = "El tipo de actividad es obligatorio")
        TipoActividad tipo,

        @Min(value = 1, message = "El número máximo de participantes debe ser mayor que cero")
        Integer nmaxParticipantes,

        @NotNull(message = "El docente responsable es obligatorio")
        Long docenteId

){}
