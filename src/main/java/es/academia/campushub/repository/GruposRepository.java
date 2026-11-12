package es.academia.campushub.repository;

import es.academia.campushub.entity.GruposEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GruposRepository extends JpaRepository<GruposEntity, Long> {

    Optional<GruposEntity> findByNombre(String nombre);
    List<GruposEntity> findByNivel(String nivel);
    List<GruposEntity> findByDocenteTutor(String docenteTutor);
}
