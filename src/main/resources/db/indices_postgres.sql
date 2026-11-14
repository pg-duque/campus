-- Ejecutar despues del primer arranque, cuando existan las tablas.
-- Se puede repetir. No cambia los datos ni las restricciones existentes.
-- Las claves primarias y los campos UNIQUE ya tienen sus propios indices.
BEGIN;

CREATE INDEX IF NOT EXISTS ix_estudiantes_grupo
    ON public.estudiantes (grupo_id);

CREATE INDEX IF NOT EXISTS ix_docentes_departamento
    ON public.docentes (departamento_id);

CREATE INDEX IF NOT EXISTS ix_grupos_tutor
    ON public.grupos (tutor_docente_id);

COMMIT;

SELECT tablename, indexname, indexdef
FROM pg_indexes
WHERE schemaname = 'public'
  AND indexname IN (
      'ix_estudiantes_grupo',
      'ix_docentes_departamento',
      'ix_grupos_tutor'
  )
ORDER BY tablename, indexname;
