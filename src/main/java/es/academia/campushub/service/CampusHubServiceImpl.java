package es.academia.campushub.service;

import es.academia.campushub.controller.dto.*;
import es.academia.campushub.entity.ActividadEntity;
import es.academia.campushub.entity.ActividadEtiquetaEntity;
import es.academia.campushub.entity.DocenteEntity;
import es.academia.campushub.entity.EtiquetaEntity;
import es.academia.campushub.entity.EstudiantesEntity;
import es.academia.campushub.entity.InscripcionEntity;
import es.academia.campushub.entity.SeguimientoEntity;
import es.academia.campushub.repository.ActividadEtiquetaRepository;
import es.academia.campushub.repository.ActividadRepository;
import es.academia.campushub.repository.DocenteRepository;
import es.academia.campushub.repository.EtiquetaRepository;
import es.academia.campushub.repository.EstudiantesRepository;
import es.academia.campushub.repository.InscripcionRepository;
import es.academia.campushub.repository.SeguimientoRepository;
import es.academia.campushub.util.exception.InvalidInputException;
import es.academia.campushub.util.exception.PersistenciaPendienteException;
import es.academia.campushub.util.exception.ResourceNotFoundException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

/** Implementa los casos de uso del servicio de CampusHub. */
@Service
public class CampusHubServiceImpl implements CampusHubService {
  private final ActividadRepository actividadRepository;
  private final DocenteRepository docenteRepository;
  private final InscripcionRepository inscripcionRepository;
  private final EstudiantesRepository estudiantesRepository;
  private final SeguimientoRepository seguimientoRepository;
  private final ActividadEtiquetaRepository actividadEtiquetaRepository;
  private final EtiquetaRepository etiquetaRepository;

  public CampusHubServiceImpl(
      ActividadRepository actividadRepository,
      DocenteRepository docenteRepository,
      InscripcionRepository inscripcionRepository,
      EstudiantesRepository estudiantesRepository,
      SeguimientoRepository seguimientoRepository,
      ActividadEtiquetaRepository actividadEtiquetaRepository,
      EtiquetaRepository etiquetaRepository) {
    this.actividadRepository = actividadRepository;
    this.docenteRepository = docenteRepository;
    this.inscripcionRepository = inscripcionRepository;
    this.estudiantesRepository = estudiantesRepository;
    this.seguimientoRepository = seguimientoRepository;
    this.actividadEtiquetaRepository = actividadEtiquetaRepository;
    this.etiquetaRepository = etiquetaRepository;
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
    String actividadId = id.toString();
    ActividadEntity actividad =
        actividadRepository
            .findById(actividadId)
            .orElseThrow(() -> new ResourceNotFoundException("No existe la actividad indicada"));
    DocenteEntity docente =
        docenteRepository
            .findById(actividad.docenteId())
            .orElseThrow(
                () -> new ResourceNotFoundException("No existe el docente responsable"));

    List<InscripcionEntity> inscripciones =
        inscripcionRepository.findByActividadIdOrderByFechaInscripcionAsc(actividadId);
    List<EstudianteResponse> participantes = obtenerEstudiantes(inscripciones);
    int ocupadas = inscripciones.size();

    List<EtiquetaResponse> etiquetas = obtenerEtiquetas(actividadId);
    List<SeguimientoResponse> seguimientos = seguimientos(id);
    return new ActividadDetalleResponse(
        actividadResponse(actividad, id),
        docenteResponse(docente),
        ocupadas,
        Math.max(0, actividad.maxParticipantes() - ocupadas),
        participantes,
        etiquetas,
        seguimientos);
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
    if (request.nombre() == null || request.nombre().isBlank()) {
      throw new InvalidInputException("El nombre de la etiqueta es obligatorio");
    }
    EtiquetaEntity guardada = etiquetaRepository.saveAndFlush(new EtiquetaEntity(request.nombre()));
    return new EtiquetaResponse(guardada.getId(), guardada.getNombre());
  }

  @Override
  public void asociarEtiqueta(Long actividadId, Long etiquetaId) {
    String id = actividadId.toString();
    if (!actividadRepository.existsById(id)) {
      throw new ResourceNotFoundException("No existe la actividad indicada");
    }
    if (!etiquetaRepository.existsById(etiquetaId)) {
      throw new ResourceNotFoundException("No existe la etiqueta indicada");
    }
    if (!actividadEtiquetaRepository.existsByActividadIdAndEtiquetaId(id, etiquetaId)) {
      actividadEtiquetaRepository.save(new ActividadEtiquetaEntity(id, etiquetaId));
    }
  }

  @Override
  public List<ActividadResponse> actividadesPorEtiqueta(Long etiquetaId) {
    throw new PersistenciaPendienteException("actividadesPorEtiqueta");
  }

  @Override
  public ActividadDetalleResponse obtenerDetalleActividad(String id) {
    try {
      return detalle(Long.valueOf(id));
    } catch (NumberFormatException ex) {
      throw new InvalidInputException("El identificador de actividad debe ser numérico");
    }
  }

  private static SeguimientoResponse seguimientoResponse(SeguimientoEntity seguimiento) {
    return new SeguimientoResponse(
        seguimiento.getId(),
        seguimiento.getActividadId(),
        seguimiento.getTipo(),
        seguimiento.getDatos(),
        seguimiento.getCreadoEn().toString());
  }

  private List<EstudianteResponse> obtenerEstudiantes(List<InscripcionEntity> inscripciones) {
    List<Long> ids = inscripciones.stream().map(InscripcionEntity::getEstudianteId).distinct().toList();
    Map<Long, EstudiantesEntity> estudiantes = new HashMap<>();
    estudiantesRepository.findAllById(ids).forEach(estudiante -> estudiantes.put(estudiante.getId(), estudiante));

    List<EstudianteResponse> respuestas = new ArrayList<>(inscripciones.size());
    for (InscripcionEntity inscripcion : inscripciones) {
      EstudiantesEntity estudiante = estudiantes.get(inscripcion.getEstudianteId());
      if (estudiante == null) {
        throw new IllegalStateException(
            "La inscripción referencia un estudiante que no existe: "
                + inscripcion.getEstudianteId());
      }
      respuestas.add(estudianteResponse(estudiante));
    }
    return List.copyOf(respuestas);
  }

  private List<EtiquetaResponse> obtenerEtiquetas(String actividadId) {
    List<ActividadEtiquetaEntity> relaciones =
        actividadEtiquetaRepository.findByActividadId(actividadId);
    List<Long> ids = relaciones.stream().map(ActividadEtiquetaEntity::etiquetaId).distinct().toList();
    Map<Long, EtiquetaEntity> etiquetas = new HashMap<>();
    etiquetaRepository.findAllById(ids).forEach(etiqueta -> etiquetas.put(etiqueta.getId(), etiqueta));

    List<EtiquetaResponse> respuestas = new ArrayList<>(relaciones.size());
    for (ActividadEtiquetaEntity relacion : relaciones) {
      EtiquetaEntity etiqueta = etiquetas.get(relacion.etiquetaId());
      if (etiqueta == null) {
        throw new IllegalStateException(
            "La relación referencia una etiqueta que no existe: " + relacion.etiquetaId());
      }
      respuestas.add(new EtiquetaResponse(etiqueta.getId(), etiqueta.getNombre()));
    }
    return List.copyOf(respuestas);
  }

  private static EstudianteResponse estudianteResponse(EstudiantesEntity estudiante) {
    return new EstudianteResponse(
        estudiante.getId(),
        estudiante.getNombre(),
        estudiante.getApellidos(),
        estudiante.getEmail(),
        estudiante.getFechaNacimiento() == null ? null : estudiante.getFechaNacimiento().toString(),
        estudiante.getGrupoId());
  }

  private static DocenteResponse docenteResponse(DocenteEntity docente) {
    return new DocenteResponse(
        docente.getDocenteId(),
        docente.getDocenteNombre(),
        docente.getDocenteApellido(),
        docente.getDocenteCorreoElectronico(),
        docente.getDepartamento() == null ? null : docente.getDepartamento().getId());
  }

  private static ActividadResponse actividadResponse(ActividadEntity actividad, Long id) {
    return new ActividadResponse(
        id,
        actividad.titulo(),
        actividad.descripcion(),
        actividad.tipo().name(),
        actividad.fechaPrevista(),
        actividad.maxParticipantes(),
        actividad.docenteId());
  }
}
