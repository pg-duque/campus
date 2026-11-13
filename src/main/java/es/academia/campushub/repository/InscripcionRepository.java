package es.academia.campushub.repository;

import es.academia.campushub.entity.InscripcionEntity;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface InscripcionRepository extends MongoRepository<InscripcionEntity, Long> {
  List<InscripcionEntity> findByActividadIdOrderByFechaInscripcionAsc(Long actividadId);
}
