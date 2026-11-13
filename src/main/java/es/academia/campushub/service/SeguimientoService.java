package es.academia.campushub.service;

import es.academia.campushub.controller.dto.CreateSeguimientoRequest;
import es.academia.campushub.controller.dto.SeguimientoResponse;
import es.academia.campushub.entity.SeguimientoEntity;
import es.academia.campushub.repository.SeguimientoRepository;
import es.academia.campushub.util.exception.InvalidInputException;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class SeguimientoService {
  private final SeguimientoRepository repository;

  public SeguimientoService(SeguimientoRepository repository) {
    this.repository = repository;
  }

  public SeguimientoResponse registrar(Long actividadId, CreateSeguimientoRequest request) {
    if (request.tipo() == null || request.tipo().isBlank()) {
      throw new InvalidInputException("El tipo de seguimiento es obligatorio");
    }
    if (request.datos() == null) {
      throw new InvalidInputException("Los datos del seguimiento son obligatorios");
    }

    SeguimientoEntity guardado =
        repository.save(
            new SeguimientoEntity(actividadId, request.tipo(), request.datos(), Instant.now()));
    return toResponse(guardado);
  }

  public List<SeguimientoResponse> deActividad(Long actividadId) {
    return repository.findByActividadIdOrderByCreadoEnAsc(actividadId).stream()
        .map(SeguimientoService::toResponse)
        .toList();
  }

  private static SeguimientoResponse toResponse(SeguimientoEntity seguimiento) {
    return new SeguimientoResponse(
        seguimiento.getId(),
        seguimiento.getActividadId(),
        seguimiento.getTipo(),
        seguimiento.getDatos(),
        seguimiento.getCreadoEn().toString());
  }
}
