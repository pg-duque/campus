package es.academia.campushub;

import static org.junit.jupiter.api.Assertions.assertTrue;

import es.academia.campushub.support.ApiIntegrationSupport;
import org.junit.jupiter.api.Test;

/** Contrato visible: fallará con HTTP 501 hasta implementar los casos de uso. */
class ContratoApiIT extends ApiIntegrationSupport {
  @Test
  void registroDeDepartamentoYDocente() throws Exception {
    long departamento = id(post("/api/departamentos", "{\"nombre\":\"Informática\"}"));
    var docente =
        post(
            "/api/docentes",
            "{\"nombre\":\"Ana\",\"apellidos\":\"García\",\"email\":\"ana@campus.test\",\"departamentoId\":"
                + departamento
                + "}");
    status(201, docente);
    assertTrue(docente.body().contains("ana@campus.test"));
  }

  @Test
  void estudianteSeRecupera() throws Exception {
    long dept = id(post("/api/departamentos", "{\"nombre\":\"Ciencias\"}"));
    long docente =
        id(
            post(
                "/api/docentes",
                "{\"nombre\":\"Pedro\",\"apellidos\":\"Díaz\",\"email\":\"pedro@campus.test\",\"departamentoId\":"
                    + dept
                    + "}"));
    long grupo =
        id(
            post(
                "/api/grupos",
                "{\"nombre\":\"DAM2\",\"nivel\":\"2 DAM\",\"tutorDocenteId\":" + docente + "}"));
    var creado =
        post(
            "/api/estudiantes",
            "{\"nombre\":\"Laura\",\"apellidos\":\"Ruiz\",\"email\":\"laura@campus.test\",\"fechaNacimiento\":\"2004-04-01\",\"grupoId\":"
                + grupo
                + "}");
    status(201, creado);
    status(200, get("/api/estudiantes/" + id(creado)));
  }

  @Test
  void unaActividadAdmiteSeguimientosVariables() throws Exception {
    long dept = id(post("/api/departamentos", "{\"nombre\":\"Tecnología\"}"));
    long docente =
        id(
            post(
                "/api/docentes",
                "{\"nombre\":\"Iván\",\"apellidos\":\"López\",\"email\":\"ivan@campus.test\",\"departamentoId\":"
                    + dept
                    + "}"));
    long actividad =
        id(
            post(
                "/api/actividades",
                "{\"titulo\":\"Visita\",\"descripcion\":\"Visita técnica\",\"tipo\":\"VISITA\",\"fechaPrevista\":\"2027-05-01\",\"maxParticipantes\":20,\"docenteId\":"
                    + docente
                    + "}"));
    var seguimiento =
        post(
            "/api/actividades/" + actividad + "/seguimientos",
            "{\"tipo\":\"NUEVO_TIPO\",\"datos\":{\"empresa\":\"Acme\",\"observaciones\":[\"aprendizaje\",\"visita\"],\"detalle\":{\"preguntas\":[\"¿Cómo?\"],\"confirmado\":true}}}");
    status(201, seguimiento);
    assertTrue(seguimiento.body().contains("\"tipo\":\"NUEVO_TIPO\""), seguimiento.body());
    assertTrue(seguimiento.body().contains("\"creadoEn\""), seguimiento.body());
    assertTrue(seguimiento.body().contains("\"confirmado\":true"), seguimiento.body());
    status(
        201,
        post(
            "/api/actividades/" + actividad + "/seguimientos",
            "{\"tipo\":\"VISITA\",\"datos\":{\"empresa\":\"Otra empresa\",\"incidencias\":[]}}"));
    var historial = get("/api/actividades/" + actividad + "/seguimientos");
    status(200, historial);
    assertTrue(historial.body().contains("\"empresa\":\"Acme\""), historial.body());
    assertTrue(historial.body().contains("\"preguntas\":[\"¿Cómo?\"]"), historial.body());
    assertTrue(historial.body().contains("\"incidencias\":[]"), historial.body());
    var detalle = get("/api/actividades/" + actividad + "/detalle");
    status(200, detalle);
    assertTrue(detalle.body().contains("Acme"));
  }
}
