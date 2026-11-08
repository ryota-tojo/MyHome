ALTER TABLE operation_log ADD COLUMN IF NOT EXISTS action varchar;
ALTER TABLE operation_log ADD COLUMN IF NOT EXISTS target_id varchar;
ALTER TABLE operation_log ADD COLUMN IF NOT EXISTS detail varchar;
ALTER TABLE operation_log ADD COLUMN IF NOT EXISTS request_id varchar;
ALTER TABLE operation_log ADD COLUMN IF NOT EXISTS ip_address varchar;

DO $$
DECLARE r record;
BEGIN
  FOR r IN SELECT c.oid,c.conname,c.conrelid::regclass AS rel,pg_get_constraintdef(c.oid) AS def
           FROM pg_constraint c
           WHERE c.contype='f'
             AND c.confrelid IN ('"user"'::regclass,'"group"'::regclass)
             AND c.confupdtype<>'c'
  LOOP
    EXECUTE format('ALTER TABLE %s DROP CONSTRAINT %I',r.rel,r.conname);
    EXECUTE format('ALTER TABLE %s ADD CONSTRAINT %I %s ON UPDATE CASCADE',r.rel,r.conname,r.def);
  END LOOP;
END $$;
