package es.academia.campushub.repository;

import es.academia.campushub.entity.SeguimientoEntity;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SeguimientoRepository extends MongoRepository<SeguimientoEntity, String> {
  List<SeguimientoEntity> findByActividadIdOrderByCreadoEnAsc(Long actividadId);
}
