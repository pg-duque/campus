package es.academia.campushub.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import es.academia.campushub.controller.dto.ActividadDetalleResponse;
import es.academia.campushub.controller.dto.CreateSeguimientoRequest;
import es.academia.campushub.controller.dto.UpsertContactoRequest;
import es.academia.campushub.entity.ActividadEntity;
import es.academia.campushub.entity.ActividadEtiquetaEntity;
import es.academia.campushub.entity.ContactoEntity;
import es.academia.campushub.entity.DepartamentosEntity;
import es.academia.campushub.entity.DocenteEntity;
import es.academia.campushub.entity.EtiquetaEntity;
import es.academia.campushub.entity.EstudiantesEntity;
import es.academia.campushub.entity.InscripcionEntity;
import es.academia.campushub.entity.SeguimientoEntity;
import es.academia.campushub.entity.enums.TipoActividad;
import es.academia.campushub.repository.ActividadEtiquetaRepository;
import es.academia.campushub.repository.ActividadRepository;
import es.academia.campushub.repository.ContactoRepository;
import es.academia.campushub.repository.DocenteRepository;
import es.academia.campushub.repository.EtiquetaRepository;
import es.academia.campushub.repository.EstudiantesRepository;
import es.academia.campushub.repository.InscripcionRepository;
import es.academia.campushub.repository.SeguimientoRepository;
import es.academia.campushub.util.exception.ResourceNotFoundException;
import es.academia.campushub.util.exception.InvalidInputException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CampusHubServiceImplTest {
  @Mock private ActividadRepository actividadRepository;
  @Mock private DocenteRepository docenteRepository;
  @Mock private InscripcionRepository inscripcionRepository;
  @Mock private EstudiantesRepository estudiantesRepository;
  @Mock private SeguimientoRepository seguimientoRepository;
  @Mock private ActividadEtiquetaRepository actividadEtiquetaRepository;
  @Mock private EtiquetaRepository etiquetaRepository;
  @Mock private ContactoRepository contactoRepository;

  @InjectMocks private CampusHubServiceImpl service;

  @Test
  void guardarContactoUsaElIdDelEstudianteYConservaTodosLosCampos() {
    when(estudiantesRepository.existsById(23L)).thenReturn(true);
    when(contactoRepository.save(any(ContactoEntity.class)))
        .thenAnswer(invocacion -> invocacion.getArgument(0));

    var respuesta = service.guardarContacto(23L, new UpsertContactoRequest(
        "Julia Ortiz", "+34 600000000", "julia@test.invalid", true, "2026-10-01"));

    assertEquals(23L, respuesta.estudianteId());
    assertEquals("Julia Ortiz", respuesta.tutorLegal());
    assertEquals("+34 600000000", respuesta.telefono());
    assertEquals("julia@test.invalid", respuesta.email());
    assertEquals(true, respuesta.autorizado());
    assertEquals("2026-10-01", respuesta.fechaAutorizacion());
  }

  @Test
  void contactoRecuperaLaFichaDelEstudiante() {
    when(estudiantesRepository.existsById(23L)).thenReturn(true);
    when(contactoRepository.findById(23L)).thenReturn(Optional.of(new ContactoEntity(
        23L, "Julia Ortiz", "600000000", "julia@test.invalid", true, "2026-10-01")));

    var respuesta = service.contacto(23L);

    assertEquals(23L, respuesta.estudianteId());
    assertEquals("Julia Ortiz", respuesta.tutorLegal());
    assertEquals("600000000", respuesta.telefono());
    assertEquals("julia@test.invalid", respuesta.email());
    assertEquals(true, respuesta.autorizado());
    assertEquals("2026-10-01", respuesta.fechaAutorizacion());
  }

  @Test
  void contactoDeEstudianteInexistenteNoAccedeARedis() {
    when(estudiantesRepository.existsById(999L)).thenReturn(false);
    var request = new UpsertContactoRequest("Julia", "600000000", null, false, null);

    assertThrows(ResourceNotFoundException.class, () -> service.guardarContacto(999L, request));
    assertThrows(ResourceNotFoundException.class, () -> service.contacto(999L));

    verifyNoInteractions(contactoRepository);
  }

  @Test
  void contactoSinFichaDevuelveNoEncontrado() {
    when(estudiantesRepository.existsById(23L)).thenReturn(true);
    when(contactoRepository.findById(23L)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> service.contacto(23L));
  }

  @Test
  void guardarContactoRechazaFechasImposiblesOConFormatoIncorrecto() {
    when(estudiantesRepository.existsById(23L)).thenReturn(true);
    for (String fecha : List.of("2026-02-30", "13/11/2026", "")) {
      var request = new UpsertContactoRequest("Julia", "600000000", null, true, fecha);
      assertThrows(InvalidInputException.class, () -> service.guardarContacto(23L, request));
    }
    verifyNoInteractions(contactoRepository);
  }

  @Test
  void guardarContactoPermiteFechaAusenteYAutorizacionFalse() {
    when(estudiantesRepository.existsById(23L)).thenReturn(true);
    when(contactoRepository.save(any(ContactoEntity.class)))
        .thenAnswer(invocacion -> invocacion.getArgument(0));

    var respuesta = service.guardarContacto(23L,
        new UpsertContactoRequest("Julia", "600000000", null, false, null));

    assertEquals(false, respuesta.autorizado());
    assertEquals(null, respuesta.fechaAutorizacion());
    assertEquals(null, respuesta.email());
  }

  @Test
  void contactoRechazaIdsNulosOCeroONegativos() {
    Long[] ids = {null, 0L, -1L};
    var request = new UpsertContactoRequest("Julia", "600000000", null, false, null);
    for (Long id : ids) {
      assertThrows(InvalidInputException.class, () -> service.guardarContacto(id, request));
      assertThrows(InvalidInputException.class, () -> service.contacto(id));
    }
    verifyNoInteractions(estudiantesRepository, contactoRepository);
  }

  @Test
  void seguimientoNoSeGuardaCuandoLaActividadNoExiste() {
    when(actividadRepository.existsById(999L)).thenReturn(false);

    assertThrows(ResourceNotFoundException.class,
        () -> service.registrarSeguimiento(999L,
            new CreateSeguimientoRequest("VISITA", Map.of("empresa", "Acme"))));

    verifyNoInteractions(seguimientoRepository);
  }

  @Test
  void seguimientoConservaTiposLibresYDatosAnidadosParaUnaActividadExistente() {
    when(actividadRepository.existsById(7L)).thenReturn(true);
    when(seguimientoRepository.save(any(SeguimientoEntity.class)))
        .thenAnswer(invocacion -> invocacion.getArgument(0));
    Map<String, Object> datos = Map.of(
        "empresa", "Acme", "detalle", Map.of("confirmado", true),
        "observaciones", List.of("aprendizaje", "visita"));

    var respuesta = service.registrarSeguimiento(7L,
        new CreateSeguimientoRequest("NUEVO_TIPO", datos));

    assertEquals(7L, respuesta.actividadId());
    assertEquals("NUEVO_TIPO", respuesta.tipo());
    assertEquals(datos, respuesta.datos());
    assertNotNull(Instant.parse(respuesta.creadoEn()));
  }

  @Test
  void detalleCombinaActividadDocenteInscripcionesEtiquetasYSeguimientos() {
    DocenteEntity docente = docente();
    EstudiantesEntity estudiante =
        new EstudiantesEntity(
            23L, "Alicia", "Ruiz", "alicia@example.test", LocalDate.of(2005, 2, 3));
    ActividadEntity actividad =
        new ActividadEntity(
            7L, "Taller", "Práctica", TipoActividad.TALLER, "2027-05-01", 4, 12L);
    SeguimientoEntity seguimiento =
        new SeguimientoEntity(7L, "TALLER", java.util.Map.of("lenguaje", "Java"), Instant.EPOCH);
    EtiquetaEntity etiqueta = new EtiquetaEntity(31L, "programación");

    when(actividadRepository.findById(7L)).thenReturn(Optional.of(actividad));
    when(docenteRepository.findById(12L)).thenReturn(Optional.of(docente));
    when(inscripcionRepository.findByActividadIdOrderByFechaInscripcionAsc(7L))
        .thenReturn(List.of(new InscripcionEntity(7L, 23L)));
    when(estudiantesRepository.findAllById(List.of(23L))).thenReturn(List.of(estudiante));
    when(actividadEtiquetaRepository.findByActividadId(7L))
        .thenReturn(List.of(new ActividadEtiquetaEntity(7L, 31L)));
    when(etiquetaRepository.findAllById(List.of(31L))).thenReturn(List.of(etiqueta));
    when(seguimientoRepository.findByActividadIdOrderByCreadoEnAsc(7L))
        .thenReturn(List.of(seguimiento));

    ActividadDetalleResponse detalle = service.detalle(7L);

    assertEquals("Taller", detalle.actividad().titulo());
    assertEquals("TALLER", detalle.actividad().tipo());
    assertEquals("2027-05-01", detalle.actividad().fechaPrevista());
    assertEquals(12L, detalle.responsable().id());
    assertEquals(1, detalle.plazasOcupadas());
    assertEquals(3, detalle.plazasDisponibles());
    assertEquals("Alicia", detalle.estudiantes().getFirst().nombre());
    assertEquals("programación", detalle.etiquetas().getFirst().nombre());
    assertEquals("Java", detalle.seguimientos().getFirst().datos().get("lenguaje"));
  }

  @Test
  void detalleDevuelveColeccionesVaciasCuandoNoHayDatosRelacionados() {
    when(actividadRepository.findById(8L))
        .thenReturn(
            Optional.of(
                new ActividadEntity(
                    8L, "Charla", null, TipoActividad.CHARLA, "2027-06-01", 10, 12L)));
    when(docenteRepository.findById(12L)).thenReturn(Optional.of(docente()));
    when(inscripcionRepository.findByActividadIdOrderByFechaInscripcionAsc(8L))
        .thenReturn(List.of());
    when(estudiantesRepository.findAllById(List.of())).thenReturn(List.of());
    when(actividadEtiquetaRepository.findByActividadId(8L)).thenReturn(List.of());
    when(etiquetaRepository.findAllById(List.of())).thenReturn(List.of());
    when(seguimientoRepository.findByActividadIdOrderByCreadoEnAsc(8L)).thenReturn(List.of());

    ActividadDetalleResponse detalle = service.detalle(8L);

    assertNotNull(detalle.estudiantes());
    assertNotNull(detalle.etiquetas());
    assertNotNull(detalle.seguimientos());
    assertEquals(List.of(), detalle.estudiantes());
    assertEquals(List.of(), detalle.etiquetas());
    assertEquals(List.of(), detalle.seguimientos());
  }

  private static DocenteEntity docente() {
    DepartamentosEntity departamento = new DepartamentosEntity();
    departamento.setId(5L);
    DocenteEntity docente = new DocenteEntity();
    docente.setDocenteId(12L);
    docente.setDocenteNombre("Rosa");
    docente.setDocenteApellido("Gómez");
    docente.setDocenteCorreoElectronico("rosa@example.test");
    docente.setDepartamento(departamento);
    return docente;
  }
}
