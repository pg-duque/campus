package es.academia.campushub.repository;

import es.academia.campushub.entity.ActividadEtiquetaEntity;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ActividadEtiquetaRepository
    extends MongoRepository<ActividadEtiquetaEntity, String> {
  List<ActividadEtiquetaEntity> findByActividadId(String actividadId);

  boolean existsByActividadIdAndEtiquetaId(String actividadId, Long etiquetaId);
}
