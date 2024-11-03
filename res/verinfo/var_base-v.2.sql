/*
https://dbdiagram.io/d/varbase-65a94cf9ac844320ae37e647

2024-01-22 13:40:25
CREATE DATABASE commons
    WITH
    OWNER = postgres
    ENCODING = 'UTF8'
    CONNECTION LIMIT = -1;

COMMENT ON DATABASE commons
    IS 'Commons integration database';
*/
----------------------------------------------------------------  DDL 
CREATE TYPE gl_var_data_kind AS ENUM('int','double','string','text');
CREATE TYPE gl_var_type_kind AS ENUM('global','individual','external');
CREATE TYPE gl_group_kind AS ENUM('A','B','C','D');
CREATE TYPE gl_status_kind AS ENUM('0','1','2');
CREATE TYPE gl_files_ext_kind AS ENUM('pdf','docx','odt','txt');
SELECT unnest(enum_range(NULL::gl_var_data_kind)) AS gl_var_data_kind;
---------------------------------------------------------------- VARBASE
/*
DROP table IF EXISTS var_history;
SELECT * FROM VAR_values;
DROP table IF EXISTS var_values;
SELECT * FROM VAR_values;
DROP table IF EXISTS var_descript;
SELECT * FROM var_descript;
*/
CREATE TABLE var_descript (
	id bigserial NOT NULL UNIQUE,
	date timestamp NOT NULL DEFAULT NOW(),
	task VARCHAR(34) NOT NULL,
	code VARCHAR(64) NOT NULL,
	source VARCHAR(64) NOT NULL,
	info VARCHAR(128) NOT NULL,
	kind gl_var_data_kind NOT NULL,
	status INT NOT NULL DEFAULT 1,
	updated timestamp NOT NULL DEFAULT NOW(),
	PRIMARY KEY (id),
	UNIQUE (task, code)
); 

INSERT INTO var_descript(task, code, info, kind, source ) 
VALUES('EMA','CHECK_LAST_5MIN','Признак работы воркера (run|stop)','string','WORKER');
INSERT INTO var_descript(task, code, info, kind, source )
VALUES('EMA','CHECK_WORKERS','Статус запуску процесу перевірки клієнтів WebBank в EMA (run|stop)','string','WORKER');
INSERT INTO var_descript(task, code, info, kind, source )
VALUES('DIJA','LAST_STAT_STEPS_ID','Последний ИД шага обработки статистики','string','DASHBOARD');


/*
dev-nr-logic-pg.c7kidvtnejjg.eu-north-1.rds.amazonaws.com
nrlogger 
dbmanager 
ALTER TABLE var_descript ADD COLUMN source VARCHAR(64);
SQL Error [42501]: ERROR: must be owner of table var_descript

UPDATE var_descript SET source = '';
ALTER TABLE var_descript ALTER COLUMN source SET NOT NULL;
ALTER TABLE var_descript DROP COLUMN reffer_code;
ALTER TABLE var_descript DROP COLUMN owner_code;
ALTER TABLE var_descript ADD  COLUMN updated timestamp NOT NULL DEFAULT NOW();
*/
CREATE TABLE var_values (
	id bigserial NOT NULL,
	date timestamp NOT NULL DEFAULT NOW(),
	desc_id INT NOT NULL,
	source_id INT NOT NULL, -- -1 - GLOBAL	
	value_kind gl_var_type_kind NOT NULL DEFAULT 'global',
	date_from date DEFAULT NULL,
	date_into date DEFAULT NULL,
	value VARCHAR(256) NOT NULL,
	status INT NOT NULL DEFAULT 1,
	updated timestamp NOT NULL DEFAULT NOW(),
	PRIMARY KEY (id),
	UNIQUE (desc_id, source_id, date_from, date_into),
    CONSTRAINT fk_descript
      FOREIGN KEY(desc_id)
	  REFERENCES var_descript(id)	
);
--ALTER TABLE var_values DROP CONSTRAINT var_values_desc_id_owner_id_key;
--ALTER TABLE var_values ADD  CONSTRAINT var_values_desc_id_owner_id_key_dates UNIQUE (desc_id, owner_id, date_from, date_into);
--ALTER TABLE var_values DROP CONSTRAINT var_values_desc_id_owner_id_key_dates;
--ALTER TABLE var_values ADD  CONSTRAINT var_values_desc_id_owner_id_key_dates UNIQUE (desc_id, owner_id, owner_code, date_from, date_into);
--ALTER TABLE var_values ADD  COLUMN value_kind gl_var_type_kind NOT NULL DEFAULT 'global';
--ALTER TABLE var_values ALTER COLUMN value TYPE varchar(256);
--ALTER TABLE var_values ADD  COLUMN source_id INT NOT NULL DEFAULT -1;
--ALTER TABLE var_values DROP COLUMN reffer_id;

--ALTER TABLE var_values ADD  COLUMN owner_code VARCHAR(64);
--ALTER TABLE var_values DROP COLUMN owner_code;
--ALTER TABLE var_values DROP COLUMN owner_id;
--ALTER TABLE var_values ADD  COLUMN reffer_id INT;
--UPDATE var_values SET reffer_id = -1;
--ALTER TABLE var_values ALTER  COLUMN reffer_id SET NOT NULL;
-- +++
--ALTER TABLE var_values ADD  COLUMN updated timestamp NOT NULL DEFAULT NOW();

CREATE TABLE var_history (
	id bigserial NOT NULL,
	date timestamp NOT NULL DEFAULT NOW(),
	value VARCHAR(128) NOT NULL,
	value_id INT NOT NULL,
	updated timestamp NOT NULL DEFAULT NOW(),
	CONSTRAINT fk_value
      FOREIGN KEY(value_id)
	  REFERENCES var_values(id)
	  ON DELETE CASCADE
);	
--ALTER TABLE var_history ADD  COLUMN updated timestamp NOT NULL DEFAULT NOW();
--ALTER TABLE var_history DROP CONSTRAINT fk_value;
--ALTER TABLE var_history ADD CONSTRAINT fk_value
--   FOREIGN KEY (value_id)
--   REFERENCES var_values(id)
--   ON DELETE CASCADE;

----------------------------------------------------------------
COMMENT ON TABLE  var_descript IS 'Опис реквізитів';
COMMENT ON TABLE  var_values IS 'Значення реквізитів';
COMMENT ON TABLE  var_history  IS 'Історія змін реквізитів';

COMMENT ON COLUMN var_descript.id IS 'Ідентифікатор';
COMMENT ON COLUMN var_descript.date IS 'Створено';
COMMENT ON COLUMN var_descript.task IS 'Задача';
COMMENT ON COLUMN var_descript.code IS 'Код реквізиту';
COMMENT ON COLUMN var_descript.info IS 'Опис реквізиту';
COMMENT ON COLUMN var_descript.kind IS 'Тип реквізиту';
COMMENT ON COLUMN var_descript.status IS 'Статус';
COMMENT ON COLUMN var_descript.source IS 'Зовнішній Код власника (назва таблиці або джерело)';
COMMENT ON COLUMN var_descript.updated IS 'Оновлено';

COMMENT ON COLUMN var_values.id IS 'Ідентифікатор';
COMMENT ON COLUMN var_values.date IS 'Створено';
COMMENT ON COLUMN var_values.desc_id IS 'Ідентифікатор опису';
COMMENT ON COLUMN var_values.source_id IS 'Ідентифікатор власника';
COMMENT ON COLUMN var_values.date_from IS 'Діє з';
COMMENT ON COLUMN var_values.date_into IS 'Діє до';
COMMENT ON COLUMN var_values.value IS 'Значення';
COMMENT ON COLUMN var_values.status IS 'Статус';
COMMENT ON COLUMN var_values.value_kind IS 'Тип зв’язку реквізита';
COMMENT ON COLUMN var_values.updated IS 'Оновлено';

COMMENT ON COLUMN var_history.id IS 'Ідентифікатор';
COMMENT ON COLUMN var_history.date IS 'Створено';
COMMENT ON COLUMN var_history.value IS 'Значення';
COMMENT ON COLUMN var_history.value_id IS 'Ідентифікатор значення';
----------------------------------------------------------------  DDL 
----------------------------------------------------------------  FUNCTIONS

-- +++
CREATE OR REPLACE FUNCTION trigger_set_timestamp()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated = NOW();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS set_timestamp ON var_values;
CREATE TRIGGER set_timestamp
BEFORE UPDATE ON var_values
FOR EACH ROW
EXECUTE PROCEDURE trigger_set_timestamp();

DROP TRIGGER IF EXISTS set_timestamp ON var_history;
CREATE TRIGGER set_timestamp
BEFORE UPDATE ON var_history
FOR EACH ROW
EXECUTE PROCEDURE trigger_set_timestamp();

CREATE OR REPLACE FUNCTION trigger_set_var_history()
RETURNS TRIGGER AS $$
BEGIN
	IF(OLD.value <> NEW.value) 
	THEN
		INSERT INTO var_history (value, value_id) 
		VALUES(OLD.value, OLD.id);
	END;
	RETURN new;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trigger_set_var_history ON var_values;
CREATE TRIGGER trigger_set_var_history 
AFTER UPDATE
ON var_values
FOR EACH ROW
EXECUTE PROCEDURE trigger_set_var_history();

----------------------------------------------------------------  FUNCTIONS
----------------------------------------------------------------  SQLS
/*
INSERT INTO var_descript (task, code, info, kind)
VALUES('var','last_task','last tasc info','string');

INSERT INTO var_values(desc_id, owner_id, value)
VALUES(1, -1,'test 1');


UPDATE var_values
SET value = 'test 2'
WHERE id = 1;
*/
----------------------------------------------------------------
-- V1
SELECT
	c.table_schema	,
    c.table_name	,
    c.column_name	,
    data_type ,
    pgd.description
FROM
	pg_catalog.pg_statio_all_tables AS st
INNER JOIN pg_catalog.pg_description pgd ON
	(    pgd.objoid = st.relid)
INNER JOIN information_schema.columns c ON
	(    pgd.objsubid = c.ordinal_position
		AND    c.table_schema = st.schemaname
		AND    c.table_name = st.relname
	);
----------------------------------------------------------------
SELECT
	t.table_name
	, pg_catalog.obj_description(pgc.oid
	, 'pg_class')
FROM
	information_schema.tables t
INNER JOIN pg_catalog.pg_class pgc
ON
	t.table_name = pgc.relname
WHERE
	t.table_type = 'BASE TABLE'
	AND t.table_schema = 'public';
----------------------------------------------------------------
----------------------------------------------------------------
----------------------------------------------------------------
SELECT obj_description(oid), pgc.relname, pgc.relnamespace
FROM pg_class pgc
WHERE pgc.relkind = 'r'
----------------------------------------------------------------  SQLS
SELECT 
'var_descript' 	AS table_name
, COUNT(*) 		AS record_count 
FROM var_descript
WHERE 1=1;

SELECT unnest(enum_range(NULL::gl_var_data_kind)) AS gl_var_data_kind;

-- V2
SELECT
	c.table_schema	,
    c.table_name	,
    c.column_name	,
    c.data_type ,
    c.udt_name ,
    t.typcategory,
    pgd.description ,
    '|\' AS separ
FROM
	pg_catalog.pg_statio_all_tables AS st
INNER JOIN pg_catalog.pg_description pgd ON
	(    pgd.objoid = st.relid)
INNER JOIN information_schema.columns c ON
	(    pgd.objsubid = c.ordinal_position
		AND    c.table_schema = st.schemaname
		AND    c.table_name = st.relname
	)
LEFT JOIN pg_type t ON t.typname = c.udt_name
WHERE 1=1
--AND c.table_name = 'var_descript';

SELECT format_type(t.oid, NULL) AS name
  FROM pg_type AS t 
  LEFT JOIN pg_enum AS e ON e.enumtypid = t.oid
WHERE t.typname = 'gl_var_data_kind'
LIMIT 1

SELECT format_type(t.oid, NULL) AS name,
       array_agg(e.enumlabel ORDER BY e.enumsortorder) AS elements
  FROM pg_type AS t 
  LEFT JOIN pg_enum AS e ON e.enumtypid = t.oid
 WHERE t.typname = 'gl_var_data_kind'
 GROUP BY t.oid;
----------------------------------------------------------------  SQLS
--https://passwordsgenerator.net/
CREATE ROLE dbmanager WITH LOGIN PASSWORD 'Qt43+ua9GWdxX^nJ(wf%<P';
ALTER  USER dbmanager CREATEDB;

GRANT ALL PRIVILEGES ON DATABASE commons TO dbmanager;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO dbmanager;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO dbmanager;



CREATE ROLE dbreader WITH LOGIN PASSWORD 'RN@h`eZ>sC7Xdu:8~+yVtn';
GRANT CONNECT ON DATABASE commons TO dbreader;
GRANT SELECT ON ALL TABLES    IN SCHEMA public TO dbreader;
GRANT SELECT ON ALL SEQUENCES IN SCHEMA public TO dbreader;

SELECT * FROM pg_catalog.pg_database
WHERE datname NOT LIKE '%postgres%';

SELECT grantee, privilege_type,table_name from information_schema.role_table_grants WHERE grantee LIKE 'db%'
ORDER BY grantee;

DO
$$
BEGIN
    EXECUTE (
        SELECT 
            'GRANT CONNECT ON DATABASE '
            || string_agg (format('%I', datname), ', ')
            || ' to dbreader'
        FROM pg_catalog.pg_database
        WHERE datname NOT LIKE '%postg%'
    );
END
$$;

DO
$$
BEGIN
    EXECUTE (
        SELECT 
            'GRANT ALL PRIVILEGES ON DATABASE '
            || string_agg (format('%I', datname), ', ')
            || ' to dbmanager'
        FROM pg_catalog.pg_database
    );
END
$$;



----------------------------------------------------------------  SQLS


GRANT ALL PRIVILEGES ON DATABASE commons TO dbmanager;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO dbmanager;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO dbmanager;


GRANT ALL PRIVILEGES ON DATABASE commons TO nrlogger;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO nrlogger;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO nrlogger;

