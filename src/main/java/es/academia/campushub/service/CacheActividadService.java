package es.academia.campushub.service;

import es.academia.campushub.controller.dto.ActividadDetalleResponse;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

@Service
public class CacheActividadService {

    private static final Logger log =
            LoggerFactory.getLogger(CacheActividadService.class);

    private static final Duration DURACION = Duration.ofSeconds(60);

    private final StringRedisTemplate redis;
    private final JsonMapper json;

    public CacheActividadService(
            StringRedisTemplate redis,
            JsonMapper json) {

        this.redis = redis;
        this.json = json;
    }

    private String clave(Long actividadId) {
        return "cache:actividad:" + actividadId + ":detalle";
    }

    public ActividadDetalleResponse buscar(Long actividadId) {
        try {
            String contenido = redis.opsForValue().get(clave(actividadId));

            if (contenido == null) {
                return null;
            }

            ActividadDetalleResponse detalle =
                    json.readValue(contenido, ActividadDetalleResponse.class);

            log.info("Detalle de actividad {} leído de la caché", actividadId);

            return detalle;

        } catch (DataAccessException | JacksonException ex) {
            log.warn(
                    "No se pudo leer la caché de la actividad {}",
                    actividadId);

            return null;
        }
    }

    public void guardar(
            Long actividadId,
            ActividadDetalleResponse detalle) {

        try {
            String contenido = json.writeValueAsString(detalle);

            redis.opsForValue().set(
                    clave(actividadId),
                    contenido,
                    DURACION);

        } catch (DataAccessException | JacksonException ex) {
            log.warn(
                    "No se pudo guardar la caché de la actividad {}",
                    actividadId);
        }
    }

    public void borrar(Long actividadId) {
        try {
            redis.delete(clave(actividadId));

        } catch (DataAccessException ex) {
            log.warn(
                    "No se pudo borrar la caché de la actividad {}",
                    actividadId);
        }
    }
}