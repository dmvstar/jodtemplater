CREATE TABLE gl_varbase (
	Id bigserial NOT NULL PRIMARY KEY,
	date timestamp NOT NULL DEFAULT NOW(),
	src  VARCHAR(32) NOT NULL, 
	fld  VARCHAR(32) NOT NULL, 
	val  VARCHAR(128) NOT NULL,
	val_int bigint,
	val_txt text
);
CREATE UNIQUE INDEX gl_varbase_src_fld ON gl_varbase USING btree (src, fld);

GRANT ALL ON gl_varbase TO nrlogger;
--SELECT src,fld,val FROM gl_varbase; 
