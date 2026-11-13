package es.academia.campushub.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import es.academia.campushub.controller.dto.ActividadDetalleResponse;
import es.academia.campushub.entity.ActividadEntity;
import es.academia.campushub.entity.ActividadEtiquetaEntity;
import es.academia.campushub.entity.DepartamentosEntity;
import es.academia.campushub.entity.DocenteEntity;
import es.academia.campushub.entity.EtiquetaEntity;
import es.academia.campushub.entity.EstudiantesEntity;
import es.academia.campushub.entity.InscripcionEntity;
import es.academia.campushub.entity.SeguimientoEntity;
import es.academia.campushub.entity.enums.TipoActividad;
import es.academia.campushub.repository.ActividadEtiquetaRepository;
import es.academia.campushub.repository.ActividadRepository;
import es.academia.campushub.repository.DocenteRepository;
import es.academia.campushub.repository.EtiquetaRepository;
import es.academia.campushub.repository.EstudiantesRepository;
import es.academia.campushub.repository.InscripcionRepository;
import es.academia.campushub.repository.SeguimientoRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
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

  @InjectMocks private CampusHubServiceImpl service;

  @Test
  void detalleCombinaActividadDocenteInscripcionesEtiquetasYSeguimientos() {
    DocenteEntity docente = docente();
    EstudiantesEntity estudiante =
        new EstudiantesEntity(
            23L, "Alicia", "Ruiz", "alicia@example.test", LocalDate.of(2005, 2, 3));
    ActividadEntity actividad =
        new ActividadEntity(
            "7", "Taller", "Práctica", TipoActividad.TALLERER_PRECTICOS, "2027-05-01", 4, 12L);
    SeguimientoEntity seguimiento =
        new SeguimientoEntity(7L, "TALLER", java.util.Map.of("lenguaje", "Java"), Instant.EPOCH);
    EtiquetaEntity etiqueta = new EtiquetaEntity(31L, "programación");

    when(actividadRepository.findById("7")).thenReturn(Optional.of(actividad));
    when(docenteRepository.findById(12L)).thenReturn(Optional.of(docente));
    when(inscripcionRepository.findByActividadIdOrderByFechaInscripcionAsc("7"))
        .thenReturn(List.of(new InscripcionEntity("7", 23L)));
    when(estudiantesRepository.findAllById(List.of(23L))).thenReturn(List.of(estudiante));
    when(actividadEtiquetaRepository.findByActividadId("7"))
        .thenReturn(List.of(new ActividadEtiquetaEntity("7", 31L)));
    when(etiquetaRepository.findAllById(List.of(31L))).thenReturn(List.of(etiqueta));
    when(seguimientoRepository.findByActividadIdOrderByCreadoEnAsc(7L))
        .thenReturn(List.of(seguimiento));

    ActividadDetalleResponse detalle = service.detalle(7L);

    assertEquals("Taller", detalle.actividad().titulo());
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
    when(actividadRepository.findById("8"))
        .thenReturn(
            Optional.of(
                new ActividadEntity(
                    "8", "Charla", null, TipoActividad.CHARLAS, "2027-06-01", 10, 12L)));
    when(docenteRepository.findById(12L)).thenReturn(Optional.of(docente()));
    when(inscripcionRepository.findByActividadIdOrderByFechaInscripcionAsc("8"))
        .thenReturn(List.of());
    when(estudiantesRepository.findAllById(List.of())).thenReturn(List.of());
    when(actividadEtiquetaRepository.findByActividadId("8")).thenReturn(List.of());
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
