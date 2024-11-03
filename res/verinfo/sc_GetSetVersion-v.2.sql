/*
sp_helptext sc_GetVersion
sp_helptext sc_SetVersion
PG SQL Version Restored 2024-11-02 09:24:53
*/

-- TEST 
IF OBJECT_ID('dbo.sc_GetVersion') IS NOT NULL
    EXEC dbo.sc_GetVersion
    	@TaskCode = 'UGB_listLastCardTransactions';
GO

IF OBJECT_ID('dbo.sc_GetVersionHistory') IS NOT NULL
    EXEC dbo.sc_GetVersionHistory
    	@TaskCode = 'UGB_listLastCardTransactions';
GO

IF OBJECT_ID('dbo.sc_SetVersion') IS NOT NULL
    EXEC dbo.sc_SetVersion
         @TaskCode = 'UGB_listLastCardTransactions',
         @Build = 1,
         @Version = '0.0.11 from 23.11.2020',
         @TaskName = 'Get list of last 50 cart transactions';
GO
-- TEST


-- PROCEDURES
-- sc_GetVersion
-- DROP PROCEDURE sc_GetVersion;
CREATE OR REPLACE PROCEDURE sc_GetVersion(
	  TaskCode varchar(255)
	, Version INOUT varchar(255) DEFAULT NULL  
	, Build INOUT int DEFAULT NULL 
	, TaskName INOUT varchar(255) DEFAULT NULL
	--, Mode int DEFAULT 0
) 
LANGUAGE plpgsql 
AS $$ 
DECLARE 
    -- Deklaracje zmiennych (opcjonalne)
	Info varchar(255);	
 	total_rows integer;
BEGIN 
    -- Treść procedury (instrukcje SQL)
	SELECT SPLIT_PART( TaskCode, '.', 1 ) INTO TaskCode;
	Version := '1.0.0';
	SELECT dob.Version 
    , dob.Build  
    , dob.Info
	INTO Version 
    , Build  
    , TaskName
	FROM dbObjects dob
	WHERE dob.Name = TaskCode;
	--GET DIAGNOSTICS Build := ROW_COUNT;
END; 
$$;

CALL sc_GetVersion('sc_GetVersion');

CREATE PROCEDURE sc_GetVersion
	  @TaskCode varchar(255)
	, @Version varchar(255) = NULL OUT 
	, @Build int = NULL OUT
	, @TaskName varchar(255) = NULL OUT
	, @Mode int = 0
/*
 0 выдать результат SELECT'ом
 1 выдать результат через OUT параметры
*/
	
AS
SET NOCOUNT  ON
SET ROWCOUNT 0

SELECT @TaskCode = parsename( @TaskCode, 1 )

SELECT @Version = Version 
    , @Build  = Build  
    , @TaskName = Info
FROM dbObjects
WHERE Name = @TaskCode

IF @@ERROR <> 0
    OR @@ROWCOUNT <> 1
    GOTO ERR_FIND

IF @Mode = 0
    SELECT cast( @Version as varchar(80))  as Version ,
        @Build as Build ,
        cast( @TaskName as varchar(240)) as TaskName

EXITSTAT:
    RETURN 1

ERR_FIND:
    RAISERROR( 'Отсутствует информация о версии объекта [%s]', 16, -1, @TaskCode )¶
    GOTO ERR_TRAN

ERR_TRAN:
    IF @SysError IS NULL
    OR @SysError = 0
        SELECT @SysError = 50000
RETURN 0;
-- sc_GetVersion

-- sc_SetVersion
CREATE PROCEDURE dbo.sc_SetVersion
      @TaskCode varchar(255)   -- название объекта
    , @Version varchar(255) = NULL  -- версия объекта
    , @Build  int  = NULL   -- билд объекта
    , @TaskName varchar(255) = NULL   -- "читаемое" название объекта
    , @FileName varchar(255) = NULL   -- имя файла, в котором лежит объект
    , @Hash  varbinary(255) = NULL  
    , @SysError int  = 0 OUT 
    , @Label  varchar(255) = ''  
    , @Note  varchar(255) = ''
AS

SET NOCOUNT  ON

DECLARE
    @OldName varchar(255) ,
    @Error  int  ,
    @Count  int  ,
    @Issue  int

-- Делать нечего!
IF @Version IS NULL
AND @Build  IS NULL
AND @TaskName IS NULL
AND @FileName IS NULL BEGIN

EXEC dbo.sc_GetVersion
     @TaskCode = @TaskCode;

GOTO EXITSTAT;
END;

-- смена имени
IF @TaskCode <> parsename( @TaskCode, 1 )
 SELECT @OldName = @TaskCode
ELSE SELECT @OldName = NULL

SELECT @TaskCode = parsename( @TaskCode, 1 )

INSERT INTO
    dbHistory (
        ObjectId
        , Version
        , Build
        , Info
        , Path
        , Hash
        , Changer
        , Changed
        , Creator
        , Created
        , Label
        , Note
    )
    SELECT
        Id
        , Version
        , Build
        , Info
        , Path
        , Hash
        , Creator
        , Created
        , system_user        
        , NOW()
        , Label
        , Note
    FROM
        dbObjects
    WHERE
        Name  = @TaskCode

SELECT  @SysError = @@ERROR 
        , @Count  = @@ROWCOUNT

IF @SysError <> 0
    GOTO ERR_TRAN

-- Объект не зарегистрирован?
IF @Count = 0 BEGIN

 -- А то не вставится
SELECT   @Version = ISNULL( @Version ,'' )
       , @Build = ISNULL( @Build ,0 )
       , @TaskName = ISNULL( @TaskName ,'' )
       , @FileName = ISNULL( @FileName ,'' )
       , @Label = ISNULL( @Label ,'' )
       , @Note = ISNULL( @Note ,'' )

INSERT INTO dbObjects ( 
         Name 
        , Version 
        , Build 
        , Info 
        , Path 
        , Hash 
        , Creator 
        , Created 
        , Label 
        , Note ) 
VALUES ( 
        @TaskCode 
        , @Version 
        , @Build 
        , @TaskName 
        , @FileName 
        , @Hash 
        , system_user 
        , getdate() 
        , @Label 
        , @Note )

SET @SysError = @@ERROR

IF @SysError <> 0
  GOTO ERR_TRAN

END ELSE BEGIN

UPDATE dbObjects
SET    Version  = ISNULL( @Version , Version )
       , Build  = ISNULL( @Build , Build  )
       , Info  = ISNULL( @TaskName , Info  )
       , Path  = ISNULL( @FileName , Path  )
       , Hash  = ISNULL( @Hash  , Hash  )
       , Creator  = system_user  
       , Created  = getdate()  
       , Label  = ISNULL( @Label , ''  )
       , Note  = ISNULL( @Note  , ''  )
WHERE Name  = @TaskCode;

SET @SysError = @@ERROR

IF @SysError <> 0
  GOTO ERR_TRAN
END
-- если указано "старое" имя, то подправим историю и уберем старую точку входа¶
IF @OldName IS NOT NULL
AND EXISTS ( 
    SELECT 1
    FROM dbObjects
    WHERE Name = @OldName
)
BEGIN
    UPDATE dbHistory
    SET ObjectId =
    ( 
        SELECT Id
        FROM dbObjects
        WHERE Name  = @TaskCode
    )
WHERE ObjectId =
    ( 
        SELECT Id
        FROM dbObjects
        WHERE Name  = @OldName
    )

SET @SysError = @@ERROR

IF @SysError <> 0
    GOTO ERR_TRAN

DELETE
    FROM dbObjects
    WHERE Name  = @OldName

SET @SysError = @@ERROR

IF @SysError <> 0
    GOTO ERR_TRAN
END

EXITSTAT:
    RETURN 1

ERR_EXEC:
    SELECT @SysError = @Error
    GOTO ERR_TRAN

ERR_TRAN:
    IF @SysError IS NULL
    OR @SysError = 0
    SELECT @SysError = 50000

RETURN 0;
-- sc_SetVersion 
-- PROCEDURES

 
-- dbo.dbObjects definition
-- Drop table
-- DROP TABLE dbo.dbObjects;
CREATE TABLE
    dbObjects (        
        Id bigserial NOT NULL
        , Name varchar(255) NOT NULL
        , Label varchar(255) DEFAULT '' NOT NULL
        , Version varchar(255) DEFAULT '' NOT NULL
        , Build int DEFAULT 0 NOT NULL
        , Info varchar(255) DEFAULT '' NOT NULL
        , Path varchar(255) DEFAULT '' NOT NULL
        , Hash bytea NULL
        , Creator varchar(255) DEFAULT current_user NOT NULL
        , Created timestamp NOT NULL DEFAULT NOW()
        , Stamp timestamp NOT NULL DEFAULT NOW()
        , Note varchar(255) DEFAULT '' NOT NULL
        , CONSTRAINT IdbObjectsId PRIMARY KEY (Id)
        , CONSTRAINT IdbObjectsName UNIQUE (Name)
);

-- dbo.dbHistory definition
-- Drop table
-- DROP TABLE dbo.dbHistory;
CREATE TABLE
    dbHistory (
        Id bigserial NOT NULL
        , ObjectId int NOT NULL
        , Label varchar(255) NOT NULL
        , Version varchar(255) NOT NULL
        , Build int NOT NULL
        , Info varchar(255) NOT NULL
        , Path varchar(255) NOT NULL
        , Hash bytea NULL
        , Changer varchar(255) NOT NULL
        , Changed timestamp NOT NULL
        , Creator varchar(255) DEFAULT current_user NOT NULL
        , Created timestamp NOT NULL DEFAULT NOW()
        , Stamp timestamp NOT NULL DEFAULT NOW()
        , Note varchar(255) DEFAULT '' NOT NULL
        , CONSTRAINT IdbHistoryId PRIMARY KEY (Id)
);
-- dbo.dbHistory foreign keys
ALTER TABLE dbo.dbHistory ADD CONSTRAINT RdbHistoryObjectId FOREIGN KEY (ObjectId) REFERENCES Scrooge.dbo.dbObjects(Id) ON DELETE CASCADE;

SELECT * FROM dbHistory;
SELECT * FROM dbObjects;





