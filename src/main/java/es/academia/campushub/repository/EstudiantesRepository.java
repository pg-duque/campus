package es.academia.campushub.repository;

import es.academia.campushub.entity.EstudiantesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EstudiantesRepository extends JpaRepository<EstudiantesEntity, Long> {

    Optional<EstudiantesEntity> findByEmail(String email);
    boolean existsByEmail(String email);
    List<EstudiantesEntity> findByGrupoId(Long grupoId);
}
