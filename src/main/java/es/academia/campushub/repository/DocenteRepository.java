package es.academia.campushub.repository;

import es.academia.campushub.entity.DocenteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocenteRepository extends JpaRepository<DocenteEntity, Long> {

    Optional<DocenteEntity> findByDocenteCorreoElectronico(String docenteCorreoElectronico);
    boolean existsByDocenteCorreoElectronico(String docenteCorreoElectronico);
    List<DocenteEntity> findByDepartamentoId(Long departamentoId);
}
