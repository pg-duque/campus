package es.academia.campushub.repository;

import es.academia.campushub.entity.ActividadEntity;
import es.academia.campushub.entity.enums.TipoActividad;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;


public interface ActividadRepository extends MongoRepository<ActividadEntity, String> {

    List<ActividadEntity> findByTipo(TipoActividad tipo);
    List<ActividadEntity> findByDocenteId(Long docenteId);
}
