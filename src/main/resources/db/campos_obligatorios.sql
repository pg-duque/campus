-- Ejecutar después del primer arranque, cuando existan las tablas.
-- Se puede repetir. No rellena ni elimina datos existentes.
-- Si faltan datos obligatorios, se detiene para que los reviséis.

BEGIN;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM public.estudiantes
        WHERE fecha_nacimiento IS NULL OR grupo_id IS NULL
    ) THEN
        RAISE EXCEPTION 'Hay estudiantes sin fecha de nacimiento o sin grupo. Revisarlos antes de aplicar las restricciones.';
    END IF;

    IF EXISTS (
        SELECT 1 FROM public.docentes WHERE departamento_id IS NULL
    ) THEN
        RAISE EXCEPTION 'Hay docentes sin departamento. Revisarlos antes de aplicar las restricciones.';
    END IF;
END;
$$;

ALTER TABLE public.estudiantes
    ALTER COLUMN fecha_nacimiento SET NOT NULL,
    ALTER COLUMN grupo_id SET NOT NULL;

ALTER TABLE public.docentes
    ALTER COLUMN departamento_id SET NOT NULL;

COMMIT;

SELECT table_name, column_name, is_nullable
FROM information_schema.columns
WHERE table_schema = 'public'
  AND (
      (table_name = 'estudiantes' AND column_name IN ('fecha_nacimiento', 'grupo_id'))
      OR (table_name = 'docentes' AND column_name = 'departamento_id')
  )
ORDER BY table_name, column_name;
