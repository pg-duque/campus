package es.academia.campushub.repository;

import es.academia.campushub.entity.DepartamentosEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DepartamentosRepository extends JpaRepository<DepartamentosEntity, Long> {

    Optional<DepartamentosEntity> findByNombre(String nombre);

    boolean existsByNombre(String nombre);
}
