package es.academia.campushub.service;

import es.academia.campushub.controller.dto.*;
import es.academia.campushub.entity.ActividadEntity;
import es.academia.campushub.entity.ActividadEtiquetaEntity;
import es.academia.campushub.entity.ContactoEntity;
import es.academia.campushub.entity.DepartamentosEntity;
import es.academia.campushub.entity.DocenteEntity;
import es.academia.campushub.entity.EtiquetaEntity;
import es.academia.campushub.entity.EstudiantesEntity;
import es.academia.campushub.entity.GruposEntity;
import es.academia.campushub.entity.InscripcionEntity;
import es.academia.campushub.entity.SeguimientoEntity;
import es.academia.campushub.entity.enums.TipoActividad;
import es.academia.campushub.repository.ActividadEtiquetaRepository;
import es.academia.campushub.repository.ActividadRepository;
import es.academia.campushub.repository.ContactoRepository;
import es.academia.campushub.repository.DepartamentosRepository;
import es.academia.campushub.repository.DocenteRepository;
import es.academia.campushub.repository.EtiquetaRepository;
import es.academia.campushub.repository.EstudiantesRepository;
import es.academia.campushub.repository.GruposRepository;
import es.academia.campushub.repository.InscripcionRepository;
import es.academia.campushub.repository.SeguimientoRepository;
import es.academia.campushub.util.exception.InvalidInputException;
import es.academia.campushub.util.exception.BusinessConflictException;
import es.academia.campushub.util.exception.ResourceNotFoundException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
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
  private final ContactoRepository contactoRepository;
  private final DepartamentosRepository departamentosRepository;
  private final GruposRepository gruposRepository;
  private final SecuenciaMongoService secuencias;

  public CampusHubServiceImpl(
      ActividadRepository actividadRepository,
      DocenteRepository docenteRepository,
      InscripcionRepository inscripcionRepository,
      EstudiantesRepository estudiantesRepository,
      SeguimientoRepository seguimientoRepository,
      ActividadEtiquetaRepository actividadEtiquetaRepository,
      EtiquetaRepository etiquetaRepository,
      ContactoRepository contactoRepository,
      DepartamentosRepository departamentosRepository,
      GruposRepository gruposRepository,
      SecuenciaMongoService secuencias) {
    this.actividadRepository = actividadRepository;
    this.docenteRepository = docenteRepository;
    this.inscripcionRepository = inscripcionRepository;
    this.estudiantesRepository = estudiantesRepository;
    this.seguimientoRepository = seguimientoRepository;
    this.actividadEtiquetaRepository = actividadEtiquetaRepository;
    this.etiquetaRepository = etiquetaRepository;
    this.contactoRepository = contactoRepository;
    this.departamentosRepository = departamentosRepository;
    this.gruposRepository = gruposRepository;
    this.secuencias = secuencias;
  }

  @Override
  public GrupoResponse crearGrupo(CreateGrupoRequest request) {
    String nombre = textoObligatorio(request.nombre(), "El nombre del grupo", 100);
    String nivel = textoObligatorio(request.nivel(), "El nivel del grupo", 100);
    DocenteEntity tutor = buscarDocente(request.tutorDocenteId());
    GruposEntity grupo = new GruposEntity();
    grupo.setNombre(nombre);
    grupo.setNivel(nivel);
    grupo.setDocenteTutor(tutor);
    GruposEntity guardado = gruposRepository.saveAndFlush(grupo);
    return new GrupoResponse(guardado.getId(), guardado.getNombre(), guardado.getNivel(),
        guardado.getDocenteTutor().getDocenteId());
  }

  @Override
  public DepartamentoResponse crearDepartamento(CreateDepartamentoRequest request) {
    String nombre = textoObligatorio(request.nombre(), "El nombre del departamento", 100);
    if (departamentosRepository.existsByNombre(nombre)) {
      throw new BusinessConflictException("Ya existe un departamento con ese nombre");
    }
    DepartamentosEntity departamento = new DepartamentosEntity();
    departamento.setNombre(nombre);
    DepartamentosEntity guardado = departamentosRepository.saveAndFlush(departamento);
    return new DepartamentoResponse(guardado.getId(), guardado.getNombre());
  }

  @Override
  public EstudianteResponse crearEstudiante(CreateEstudianteRequest request) {
    String nombre = textoObligatorio(request.nombre(), "El nombre del estudiante", 100);
    String apellidos = textoObligatorio(request.apellidos(), "Los apellidos del estudiante", 100);
    String email = validarEmail(request.email(), 50);
    LocalDate fechaNacimiento = validarFecha(request.fechaNacimiento(), "La fecha de nacimiento");
    if (!fechaNacimiento.isBefore(LocalDate.now())) {
      throw new InvalidInputException("La fecha de nacimiento debe ser anterior a hoy");
    }
    validarId(request.grupoId());
    if (estudiantesRepository.existsByEmail(email)) {
      throw new BusinessConflictException("Ya existe un estudiante con ese email");
    }
    EstudiantesEntity estudiante = new EstudiantesEntity(null, nombre, apellidos, email, fechaNacimiento);
    GruposEntity grupo = gruposRepository.findById(request.grupoId())
        .orElseThrow(() -> new ResourceNotFoundException("No existe el grupo indicado"));
    estudiante.setGrupo(grupo);
    return estudianteResponse(estudiantesRepository.saveAndFlush(estudiante));
  }

  @Override
  public EstudianteResponse obtenerEstudiante(Long id) {
    validarId(id);
    EstudiantesEntity estudiante = estudiantesRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("No existe el estudiante indicado"));
    return estudianteResponse(estudiante);
  }

  @Override
  public DocenteResponse crearDocente(CreateDocenteRequest request) {
    String nombre = textoObligatorio(request.nombre(), "El nombre del docente", 250);
    String apellidos = textoObligatorio(request.apellidos(), "Los apellidos del docente", 250);
    String email = validarEmail(request.email(), 250);
    validarId(request.departamentoId());
    if (docenteRepository.existsByDocenteCorreoElectronico(email)) {
      throw new BusinessConflictException("Ya existe un docente con ese email");
    }
    DocenteEntity docente = new DocenteEntity();
    docente.setDocenteNombre(nombre);
    docente.setDocenteApellido(apellidos);
    docente.setDocenteCorreoElectronico(email);
    DepartamentosEntity departamento = departamentosRepository.findById(request.departamentoId())
        .orElseThrow(() -> new ResourceNotFoundException("No existe el departamento indicado"));
    docente.setDepartamento(departamento);
    return docenteResponse(docenteRepository.saveAndFlush(docente));
  }

  @Override
  public ActividadResponse crearActividad(CreateActividadRequest request) {
    String titulo = textoObligatorio(request.titulo(), "El título", 250);
    if (request.descripcion() == null || request.descripcion().isBlank()) {
      throw new InvalidInputException("La descripción es obligatoria");
    }
    TipoActividad tipo;
    try {
      tipo = TipoActividad.valueOf(textoObligatorio(request.tipo(), "El tipo de actividad", 100));
    } catch (IllegalArgumentException ex) {
      throw new InvalidInputException("El tipo de actividad no es válido");
    }
    validarFecha(request.fechaPrevista(), "La fecha prevista");
    if (request.maxParticipantes() == null || request.maxParticipantes() < 1) {
      throw new InvalidInputException("El número máximo de participantes debe ser mayor que cero");
    }
    buscarDocente(request.docenteId());
    Long id = secuencias.siguiente("actividades");
    ActividadEntity actividad = new ActividadEntity(id, titulo, request.descripcion(), tipo,
        request.fechaPrevista(), request.maxParticipantes(), request.docenteId());
    return actividadResponse(actividadRepository.save(actividad));
  }

  @Override
  public ActividadResponse obtenerActividad(Long id) {
    return actividadResponse(buscarActividad(id));
  }

  @Override
  public synchronized InscripcionResponse inscribir(Long actividadId, CreateInscripcionRequest request) {
    // Dentro de esta aplicación, ninguna otra inscripción comprueba las plazas hasta terminar esta.
    ActividadEntity actividad = buscarActividad(actividadId);
    comprobarEstudiante(request.estudianteId());
    if (inscripcionRepository.existsByActividadIdAndEstudianteId(actividadId, request.estudianteId())) {
      throw new BusinessConflictException("El estudiante ya está inscrito en esta actividad");
    }
    if (inscripcionRepository.countByActividadId(actividadId) >= actividad.maxParticipantes()) {
      throw new BusinessConflictException("No quedan plazas disponibles en la actividad");
    }
    InscripcionEntity inscripcion = new InscripcionEntity(actividadId, request.estudianteId());
    inscripcion.setId(secuencias.siguiente("inscripciones"));
    InscripcionEntity guardada = inscripcionRepository.save(inscripcion);
    return new InscripcionResponse(guardada.getId(), guardada.getActividadId(),
        guardada.getEstudianteId(), guardada.getFechaInscripcion().toString());
  }

  @Override
  public List<EstudianteResponse> participantes(Long actividadId) {
    buscarActividad(actividadId);
    return obtenerEstudiantes(inscripcionRepository.findByActividadIdOrderByFechaInscripcionAsc(actividadId));
  }

  @Override
  public SeguimientoResponse registrarSeguimiento(
      Long actividadId, CreateSeguimientoRequest request) {
    validarId(actividadId);
    if (request.tipo() == null || request.tipo().isBlank()) {
      throw new InvalidInputException("El tipo de seguimiento es obligatorio");
    }
    if (request.datos() == null) {
      throw new InvalidInputException("Los datos del seguimiento son obligatorios");
    }
    if (!actividadRepository.existsById(actividadId)) {
      throw new ResourceNotFoundException("No existe la actividad indicada");
    }

    SeguimientoEntity guardado =
        seguimientoRepository.save(
            new SeguimientoEntity(actividadId, request.tipo(), request.datos(), Instant.now()));
    return seguimientoResponse(guardado);
  }

  @Override
  public List<SeguimientoResponse> seguimientos(Long actividadId) {
    validarId(actividadId);
    if (!actividadRepository.existsById(actividadId)) {
      throw new ResourceNotFoundException("No existe la actividad indicada");
    }
    return obtenerSeguimientos(actividadId);
  }

  private List<SeguimientoResponse> obtenerSeguimientos(Long actividadId) {
    return seguimientoRepository.findByActividadIdOrderByCreadoEnAsc(actividadId).stream()
        .map(CampusHubServiceImpl::seguimientoResponse)
        .toList();
  }

  @Override
  public ActividadDetalleResponse detalle(Long id) {
    Long actividadId = id;
    ActividadEntity actividad = buscarActividad(actividadId);
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
    List<SeguimientoResponse> seguimientos = obtenerSeguimientos(id);
    return new ActividadDetalleResponse(
        actividadResponse(actividad),
        docenteResponse(docente),
        ocupadas,
        Math.max(0, actividad.maxParticipantes() - ocupadas),
        participantes,
        etiquetas,
        seguimientos);
  }

  @Override
  public List<ActividadHistorialResponse> historial(Long estudianteId) {
    comprobarEstudiante(estudianteId);
    List<ActividadHistorialResponse> historial = new ArrayList<>();
    for (InscripcionEntity inscripcion :
        inscripcionRepository.findByEstudianteIdOrderByFechaInscripcionAsc(estudianteId)) {
      ActividadEntity actividad = buscarActividad(inscripcion.getActividadId());
      DocenteEntity docente = buscarDocente(actividad.docenteId());
      historial.add(new ActividadHistorialResponse(actividad.actividadId(), actividad.titulo(),
          actividad.tipo().name(), actividad.fechaPrevista(),
          inscripcion.getFechaInscripcion().toString(), docenteResponse(docente)));
    }
    return historial;
  }

  @Override
  public ContactoResponse guardarContacto(Long estudianteId, UpsertContactoRequest request) {
    comprobarEstudiante(estudianteId);
    if (request.fechaAutorizacion() != null) {
      try {
        LocalDate.parse(request.fechaAutorizacion());
      } catch (DateTimeParseException ex) {
        throw new InvalidInputException("La fecha de autorización debe ser válida y tener formato YYYY-MM-DD");
      }
    }
    ContactoEntity guardado = contactoRepository.save(new ContactoEntity(
        estudianteId, request.tutorLegal(), request.telefono(), request.email(),
        request.autorizado(), request.fechaAutorizacion()));
    return contactoResponse(guardado);
  }

  @Override
  public ContactoResponse contacto(Long estudianteId) {
    comprobarEstudiante(estudianteId);
    ContactoEntity contacto = contactoRepository.findById(estudianteId)
        .orElseThrow(() -> new ResourceNotFoundException("El estudiante no tiene ficha de contacto"));
    return contactoResponse(contacto);
  }

  private void comprobarEstudiante(Long estudianteId) {
    if (estudianteId == null || estudianteId <= 0) {
      throw new InvalidInputException("El identificador del estudiante debe ser un número positivo");
    }
    if (!estudiantesRepository.existsById(estudianteId)) {
      throw new ResourceNotFoundException("No existe el estudiante indicado");
    }
  }

  private static ContactoResponse contactoResponse(ContactoEntity contacto) {
    return new ContactoResponse(
        contacto.getEstudianteId(), contacto.getTutorLegal(), contacto.getTelefono(),
        contacto.getEmail(), contacto.getAutorizado(), contacto.getFechaAutorizacion());
  }

  @Override
  public EtiquetaResponse crearEtiqueta(CreateEtiquetaRequest request) {
    String nombre = textoObligatorio(request.nombre(), "El nombre de la etiqueta", 100);
    EtiquetaEntity guardada = etiquetaRepository.saveAndFlush(new EtiquetaEntity(nombre));
    return new EtiquetaResponse(guardada.getId(), guardada.getNombre());
  }

  @Override
  public void asociarEtiqueta(Long actividadId, Long etiquetaId) {
    validarId(actividadId);
    validarId(etiquetaId);
    if (!actividadRepository.existsById(actividadId)) {
      throw new ResourceNotFoundException("No existe la actividad indicada");
    }
    if (!etiquetaRepository.existsById(etiquetaId)) {
      throw new ResourceNotFoundException("No existe la etiqueta indicada");
    }
    // El ID compuesto hace que repetir la misma asociación guarde la misma relación.
    if (!actividadEtiquetaRepository.existsByActividadIdAndEtiquetaId(actividadId, etiquetaId)) {
      actividadEtiquetaRepository.save(new ActividadEtiquetaEntity(
          actividadId + ":" + etiquetaId, actividadId, etiquetaId));
    }
  }

  @Override
  public List<ActividadResponse> actividadesPorEtiqueta(Long etiquetaId) {
    validarId(etiquetaId);
    if (!etiquetaRepository.existsById(etiquetaId)) {
      throw new ResourceNotFoundException("No existe la etiqueta indicada");
    }
    List<ActividadResponse> actividades = new ArrayList<>();
    for (ActividadEtiquetaEntity relacion : actividadEtiquetaRepository.findByEtiquetaId(etiquetaId)) {
      actividades.add(actividadResponse(buscarActividad(relacion.actividadId())));
    }
    return actividades;
  }

  private ActividadEntity buscarActividad(Long id) {
    validarId(id);
    return actividadRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("No existe la actividad indicada"));
  }

  private DocenteEntity buscarDocente(Long id) {
    validarId(id);
    return docenteRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("No existe el docente indicado"));
  }

  private static void validarId(Long id) {
    if (id == null || id <= 0) {
      throw new InvalidInputException("El identificador debe ser un número positivo");
    }
  }

  private static String textoObligatorio(String valor, String campo, int longitudMaxima) {
    if (valor == null || valor.isBlank()) {
      throw new InvalidInputException(campo + " no puede estar vacío");
    }
    String texto = valor.strip();
    if (texto.length() > longitudMaxima) {
      throw new InvalidInputException(campo + " no puede superar " + longitudMaxima + " caracteres");
    }
    return texto;
  }

  private static String validarEmail(String valor, int longitudMaxima) {
    String email = textoObligatorio(valor, "El email", longitudMaxima);
    if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
      throw new InvalidInputException("El formato del email no es válido");
    }
    return email;
  }

  private static LocalDate validarFecha(String valor, String campo) {
    if (valor == null || valor.isBlank()) {
      throw new InvalidInputException(campo + " es obligatoria");
    }
    try {
      return LocalDate.parse(valor);
    } catch (DateTimeParseException ex) {
      throw new InvalidInputException(campo + " debe ser válida y tener formato YYYY-MM-DD");
    }
  }

  @Override
  public ActividadDetalleResponse obtenerDetalleActividad(Long id) {
    return detalle(id);
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

  private List<EtiquetaResponse> obtenerEtiquetas(Long actividadId) {
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

  private static ActividadResponse actividadResponse(ActividadEntity actividad) {
    return new ActividadResponse(
        actividad.actividadId(),
        actividad.titulo(),
        actividad.descripcion(),
        actividad.tipo().name(),
        actividad.fechaPrevista(),
        actividad.maxParticipantes(),
        actividad.docenteId());
  }
}
