-- Ejecutar después del primer arranque de la aplicación, cuando exista la tabla.
-- Sirve para bases ya creadas con ddl-auto=update, que puede no añadir los CHECK.
-- No elimina ni corrige datos. Si hay fechas inválidas, la operación falla.
-- El CHECK comprueba el valor; campos_obligatorios.sql añade el NOT NULL para impedir fechas ausentes.

BEGIN;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conrelid = 'public.estudiantes'::regclass
          AND conname = 'ck_estudiantes_fecha_nacimiento_pasada'
    ) THEN
        ALTER TABLE public.estudiantes
            ADD CONSTRAINT ck_estudiantes_fecha_nacimiento_pasada
            CHECK (fecha_nacimiento < CURRENT_DATE);
    END IF;
END;
$$;

ALTER TABLE public.estudiantes
    VALIDATE CONSTRAINT ck_estudiantes_fecha_nacimiento_pasada;

COMMIT;

-- Debe aparecer con validada=true. Esto confirma que existe también en la base.
SELECT conname, convalidated AS validada, pg_get_constraintdef(oid) AS definicion
FROM pg_constraint
WHERE conrelid = 'public.estudiantes'::regclass
  AND conname = 'ck_estudiantes_fecha_nacimiento_pasada';
