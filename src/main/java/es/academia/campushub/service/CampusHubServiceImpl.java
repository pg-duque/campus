package es.academia.campushub.service;

import es.academia.campushub.controller.dto.*;
import es.academia.campushub.entity.SeguimientoEntity;
import es.academia.campushub.repository.SeguimientoRepository;
import es.academia.campushub.util.exception.InvalidInputException;
import es.academia.campushub.util.exception.PersistenciaPendienteException;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Punto de entrada de la tarea. Sustituye cada stub por una implementación real. NO hay entidades,
 * repositorios ni documentos entregados.
 */
@Service
public class CampusHubServiceImpl implements CampusHubService {
  private final SeguimientoRepository seguimientoRepository;

  public CampusHubServiceImpl(SeguimientoRepository seguimientoRepository) {
    this.seguimientoRepository = seguimientoRepository;
  }

  @Override
  public GrupoResponse crearGrupo(CreateGrupoRequest request) {
    throw new PersistenciaPendienteException("crearGrupo");
  }

  @Override
  public DepartamentoResponse crearDepartamento(CreateDepartamentoRequest request) {
    throw new PersistenciaPendienteException("crearDepartamento");
  }

  @Override
  public EstudianteResponse crearEstudiante(CreateEstudianteRequest request) {
    throw new PersistenciaPendienteException("crearEstudiante");
  }

  @Override
  public EstudianteResponse obtenerEstudiante(Long id) {
    throw new PersistenciaPendienteException("obtenerEstudiante");
  }

  @Override
  public DocenteResponse crearDocente(CreateDocenteRequest request) {
    throw new PersistenciaPendienteException("crearDocente");
  }

  @Override
  public ActividadResponse crearActividad(CreateActividadRequest request) {
    throw new PersistenciaPendienteException("crearActividad");
  }

  @Override
  public ActividadResponse obtenerActividad(Long id) {
    throw new PersistenciaPendienteException("obtenerActividad");
  }

  @Override
  public InscripcionResponse inscribir(Long actividadId, CreateInscripcionRequest request) {
    throw new PersistenciaPendienteException("inscribir");
  }

  @Override
  public List<EstudianteResponse> participantes(Long actividadId) {
    throw new PersistenciaPendienteException("participantes");
  }

  @Override
  public SeguimientoResponse registrarSeguimiento(
      Long actividadId, CreateSeguimientoRequest request) {
    if (request.tipo() == null || request.tipo().isBlank()) {
      throw new InvalidInputException("El tipo de seguimiento es obligatorio");
    }
    if (request.datos() == null) {
      throw new InvalidInputException("Los datos del seguimiento son obligatorios");
    }

    SeguimientoEntity guardado =
        seguimientoRepository.save(
            new SeguimientoEntity(actividadId, request.tipo(), request.datos(), Instant.now()));
    return seguimientoResponse(guardado);
  }

  @Override
  public List<SeguimientoResponse> seguimientos(Long actividadId) {
    return seguimientoRepository.findByActividadIdOrderByCreadoEnAsc(actividadId).stream()
        .map(CampusHubServiceImpl::seguimientoResponse)
        .toList();
  }

  @Override
  public ActividadDetalleResponse detalle(Long id) {
    throw new PersistenciaPendienteException("detalle");
  }

  @Override
  public List<ActividadHistorialResponse> historial(Long estudianteId) {
    throw new PersistenciaPendienteException("historial");
  }

  @Override
  public ContactoResponse guardarContacto(Long estudianteId, UpsertContactoRequest request) {
    throw new PersistenciaPendienteException("guardarContacto");
  }

  @Override
  public ContactoResponse contacto(Long estudianteId) {
    throw new PersistenciaPendienteException("contacto");
  }

  @Override
  public EtiquetaResponse crearEtiqueta(CreateEtiquetaRequest request) {
    throw new PersistenciaPendienteException("crearEtiqueta");
  }

  @Override
  public void asociarEtiqueta(Long actividadId, Long etiquetaId) {
    throw new PersistenciaPendienteException("asociarEtiqueta");
  }

  @Override
  public List<ActividadResponse> actividadesPorEtiqueta(Long etiquetaId) {
    throw new PersistenciaPendienteException("actividadesPorEtiqueta");
  }

  @Override
  public ActividadDetalleResponse obtenerDetalleActividad(String id) {
    throw new PersistenciaPendienteException("obtenerDetalleActividad");
  }

  private static SeguimientoResponse seguimientoResponse(SeguimientoEntity seguimiento) {
    return new SeguimientoResponse(
        seguimiento.getId(),
        seguimiento.getActividadId(),
        seguimiento.getTipo(),
        seguimiento.getDatos(),
        seguimiento.getCreadoEn().toString());
  }
}
