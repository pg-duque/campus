package es.academia.campushub.repository;

import es.academia.campushub.entity.ActividadEntity;
import org.springframework.data.mongodb.repository.MongoRepository;


public interface ActividadRepository extends MongoRepository<ActividadEntity, Long> {
}
