/*
https://dbdiagram.io/d/varbase-65a94cf9ac844320ae37e647

2024-05-22 13:40:25
CREATE DATABASE commons
    WITH
    OWNER = postgres
    ENCODING = 'UTF8'
    CONNECTION LIMIT = -1;

COMMENT ON DATABASE commons
    IS 'Commons integration database';
    
SERVER DEV dev-nr-logic-pg.c7kidvtnejjg.eu-north-1.rds.amazonaws.com/commons
SERVER PRO prod-nr-logic-pg.c7kidvtnejjg.eu-north-1.rds.amazonaws.com/commons
*/
----------------------------------------------------------------  DDL 
CREATE TYPE gl_var_data_kind AS ENUM('int','double','string','text');
CREATE TYPE gl_var_type_kind AS ENUM('global','individual','external');
CREATE TYPE gl_group_kind AS ENUM('A','B','C','D');
CREATE TYPE gl_status_kind AS ENUM('0','1','2');
CREATE TYPE gl_files_ext_kind AS ENUM('pdf','docx','odt','txt');
SELECT unnest(enum_range(NULL::gl_var_data_kind)) AS gl_var_data_kind;
---------------------------------------------------------------- VARBASE
DROP table IF EXISTS var_history;
SELECT * FROM VAR_values;
DROP table IF EXISTS var_values;
SELECT * FROM VAR_values;
DROP table IF EXISTS var_descript;
SELECT * FROM var_descript;
---------------------------------------------------------------- VARBASE
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
----------------------------------------------------------------  FUNCTIONS
CREATE OR REPLACE FUNCTION trigger_set_var_history()
RETURNS TRIGGER AS $$
BEGIN
	IF(OLD.value <> NEW.value) 
	THEN
		INSERT INTO var_history (value, value_id) 
		VALUES(OLD.value, OLD.id);
	END IF;
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
GRANT ALL PRIVILEGES ON DATABASE commons TO dbmanager;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO dbmanager;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO dbmanager;

GRANT ALL PRIVILEGES ON DATABASE commons TO nrlogger;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO nrlogger;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO nrlogger;
----------------------------------------------------------------  SQLS
INSERT INTO var_descript(task, code, info, kind, source ) 
VALUES('EMA','CHECK_LAST_5MIN','Признак работы воркера (run|stop)','string','WORKER');
INSERT INTO var_descript(task, code, info, kind, source )
VALUES('EMA','CHECK_WORKERS','Статус запуску процесу перевірки клієнтів WebBank в EMA (run|stop)','string','WORKER');
INSERT INTO var_descript(task, code, info, kind, source )
VALUES('DIJA','LAST_STAT_STEPS_ID','Последний ИД шага обработки статистики','string','DASHBOARD');
----------------------------------------------------------------  SQLS

