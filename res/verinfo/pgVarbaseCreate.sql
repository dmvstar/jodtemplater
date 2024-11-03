CREATE TYPE gl_varbase_kind_type AS ENUM('integer','string','boolean','double');
CREATE TYPE gl_dict_refs_type AS ENUM('table','dict','enum');

SELECT unnest(enum_range(NULL::gl_varbase_kind_type)) AS kind;

CREATE TABLE gl_varbase_desc (
	id bigserial NOT NULL UNIQUE,
	date timestamp NOT NULL DEFAULT NOW(),
	task VARCHAR(34) NOT NULL,
	code VARCHAR(64) NOT NULL,
	info VARCHAR(128) NOT NULL,
	kind gl_varbase_kind_type NOT NULL,
	dictId int, -- dictionary 
	status INT NOT NULL DEFAULT 1,
	UNIQUE (task, code)
	,CONSTRAINT fk_dict
      FOREIGN KEY(dictId)
	  REFERENCES gl_dictionary_desc(id)
);

CREATE TABLE gl_varbase_vals (
	id bigserial NOT NULL,
	date timestamp NOT NULL DEFAULT NOW(),
	descId  INT NOT NULL,
	ownerId INT NOT NULL, -- -1 - GLOBAL PARAMS
	value 	VARCHAR(128) NOT NULL,
	dateFrom timestamp,
	dateInto timestamp,
	status 	INT NOT NULL DEFAULT 1,
	UNIQUE (descId, ownerId)
    , CONSTRAINT fk_descript
      FOREIGN KEY(descId)
	  REFERENCES gl_varbase_desc(id)
);

CREATE TABLE gl_dictionary_desc (
	id bigserial NOT NULL UNIQUE,
	date timestamp NOT NULL DEFAULT NOW(),	
	code VARCHAR(64) NOT NULL,
	info VARCHAR(128) NOT NULL,
	kind gl_varbase_kind_type NOT NULL,
	refs gl_dict_refs_type NOT NULL,
	status INT NOT NULL DEFAULT 1,
	UNIQUE (code)
);

CREATE TABLE gl_dictionary_vals (
	id bigserial NOT NULL,
	date timestamp NOT NULL DEFAULT NOW(),
	descId  INT NOT NULL,	
	value 	VARCHAR(128) NOT NULL,
	desc 	VARCHAR(128) NOT NULL,
	info 	VARCHAR(256) NOT NULL,
	status 	INT NOT NULL DEFAULT 1,
	UNIQUE (descId, value),
    CONSTRAINT fk_descript
      FOREIGN KEY(descId)
	  REFERENCES gl_dictionary_desc(id)
);
-------------------------------------------------------------------
INSERT INTO gl_varbase_desc (task, code, info, kind) 
	VALUES ('clients','repo_code','report template code', 'string');
INSERT INTO gl_varbase_desc (task, code, info, kind) 
	VALUES ('clients','other_bank','have other banks', 'boolean');
INSERT INTO gl_varbase_desc (task, code, info, kind) 
	VALUES ('clients','e47_count','count of e47', 'integer');

INSERT INTO gl_varbase_desc (task, code, info, kind) 
	VALUES ('agreement','sap_code','agreement SAP code', 'string');
INSERT INTO gl_varbase_desc (task, code, info, kind) 
	VALUES ('agreement','rate_balance','x-rate balance', 'double');
INSERT INTO gl_varbase_desc (task, code, info, kind) 
	VALUES ('agreement','rate_ower','x-rate ower', 'double');
INSERT INTO gl_varbase_desc (task, code, info, kind) 
	VALUES ('agreement','rate_too','x-rate ower', 'double');
-------------------------------------------------------------------

INSERT INTO gl_varbase_vals(descId, ownerId, value) 
	VALUES (
		(SELECT id FROM gl_varbase_desc WHERE code = 'repo_code')
	, 123
	, 'clientFormFATF'
);
INSERT INTO gl_varbase_vals(descId, ownerId, value) 
	VALUES (
		(SELECT id FROM gl_varbase_desc WHERE code = 'other_bank')
	, 123
	, 'true'
);
INSERT INTO gl_varbase_vals(descId, ownerId, value) 
	VALUES (
		(SELECT id FROM gl_varbase_desc WHERE code = 'e47_count')
	, 123
	, '12'
);
-------------------------------------------------------------------
INSERT INTO gl_varbase_vals(descId, ownerId, value) 
	VALUES (
		(SELECT id FROM gl_varbase_desc WHERE code = 'sap_code')
	, 222
	, '004533322334'
);
INSERT INTO gl_varbase_vals(descId, ownerId, value, dateFrom) 
	VALUES (
		(SELECT id FROM gl_varbase_desc WHERE code = 'rate_ower')
	, 222
	, '3233.6544'
	, DATE()
);

SELECT * FROM gl_varbase_desc;

SELECT
	task
	, code
	, info
	, kind
	, ownerid
	, value
FROM
	GL_VARBASE_VALS GVV
JOIN GL_VARBASE_DESC GVD ON	GVD.ID = GVV.DESCID
WHERE 1=1
AND	GVV.OWNERID = 222;
-------------------------------------------------------------------