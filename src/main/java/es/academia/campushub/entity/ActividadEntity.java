package es.academia.campushub.entity;

import es.academia.campushub.entity.enums.TipoActividad;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "actividades")
public record ActividadEntity(
        @Id Long actividadId,

        @NotBlank(message = "El título es obligatorio")
        String titulo,

        @NotBlank(message = "La descripción es obligatoria")
        String descripcion,

        @NotNull(message = "El tipo de actividad es obligatorio")
        TipoActividad tipo,

        @NotBlank(message = "La fecha prevista es obligatoria")
        String fechaPrevista,

        @Min(value = 1, message = "El número máximo de participantes debe ser mayor que cero")
        int maxParticipantes,

        @NotNull(message = "El docente responsable es obligatorio")
        Long docenteId

){}
