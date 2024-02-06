CREATE TABLE public.foods (
	id smallserial NOT NULL PRIMARY KEY,
	created timestamp NOT NULL DEFAULT NOW(),
	name varchar(128) NOT NULL
);

CREATE TABLE public.nutrition (
	id smallserial NOT NULL PRIMARY KEY,
	created timestamp NOT NULL DEFAULT NOW(),
	food_id SMALLINT NOT NULL,
	calories SMALLINT NOT NULL DEFAULT 0,
	fat SMALLINT NOT NULL DEFAULT 0,
	carbs SMALLINT NOT NULL DEFAULT 0,
	protein SMALLINT NOT NULL DEFAULT 0,
	iron SMALLINT NOT NULL DEFAULT 0,
	CONSTRAINT nutrition_food_fk FOREIGN KEY (food_id) REFERENCES public.foods(id) ON DELETE CASCADE	
);

CREATE TABLE public.template_data (
	id smallserial NOT NULL PRIMARY KEY,
	created timestamp NOT NULL DEFAULT NOW(),
	code varchar(64) NOT NULL,
	kind varchar(64) NOT NULL,
	name varchar(128) NOT NULL,
	meta varchar(64) NOT NULL,
	type varchar(32) NOT NULL,
	input_file varchar(128) NOT NULL,
	result_file varchar(128) NOT NULL,
	CONTENT text
);
-- drop table public.generated_data cascade
CREATE TABLE public.generated_data (
	id smallserial NOT NULL PRIMARY KEY,
	created timestamp NOT NULL DEFAULT NOW(),
	template_id SMALLINT NOT NULL,
	owner_id SMALLINT NOT NULL, 
	generated_name varchar(128) NOT NULL,
	have_pdf boolean NULL DEFAULT FALSE,
	payload jsonb,
	CONSTRAINT template_data_fk FOREIGN KEY (template_id) REFERENCES template_data(id) ON DELETE CASCADE
);

SELECT id, template_id, owner_id, generated_name, have_pdf, created,payload
FROM generated_data;

--drop table public.generated_content
CREATE TABLE public.generated_content (
	id smallserial NOT NULL PRIMARY KEY,
	created timestamp NOT NULL DEFAULT NOW(),
	generated_id SMALLINT NOT NULL,  
	extention varchar(64) NOT NULL,
	owner varchar(64) NOT NULL,
	CONTENT TEXT,
	CONSTRAINT generated_data_fk FOREIGN KEY (generated_id) REFERENCES generated_data(id) ON DELETE CASCADE
);

SELECT 
	id,created,code,kind,name,meta,type,input_file,result_file
FROM template_data;

--------------------------------------------------------------------------

INSERT INTO foods(id, name)
VALUES(1,'Frozen Yogurt'),
      (2,'Jelly bean'),
      (3,'KitKat'),
      (4,'Eclair'),
      (5,'Gingerbread'),
      (6,'Ice cream sandwich'),
      (7,'Lollipop'),
      (8,'Cupcake'),
      (9,'Honeycomb'),
      (10,'Pure Yogurt'),      
      (11,'Honey');
     
INSERT INTO nutrition(food_id,calories,fat,carbs,protein,iron)
	VALUES( 1 , 159 , 6 , 24 , 4 , 1 ),
	( 2 , 375 , 0 , 94 , 0 , 0 ),
	( 3 , 518 , 26 , 65 , 7 , 6 ),
	( 4 , 262 , 16 , 23 , 6 , 7 ),
	( 5 , 356 , 16 , 49 , 3.9 , 16 ),
	( 6 , 237 , 9 , 37 , 4.3 , 1 ),
	( 7 , 392 , 0.2 , 98 , 0 , 2 ),
	( 8 , 305 , 3.7 , 67 , 4.3 , 8 ),
	( 9 , 408 , 3.2 , 87 , 6.5 , 45 ),
	( 10 , 452 , 0 , 51 , 0 , 22 ),
	( 11 , 12 , 6 , 24 , 4 , 1 );

SELECT f.*, n.calories,fat,carbs,protein,iron FROM foods f
JOIN nutrition n ON n.food_id = f.id;
