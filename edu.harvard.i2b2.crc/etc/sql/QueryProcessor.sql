
//*****************************************************************
// TABLE CREATION
//*****************************************************************

CREATE TABLE QT_QUERY_MASTER (
       QUERY_MASTER_ID NUMBER(5,0) PRIMARY KEY
     , NAME VARCHAR2(50) NOT NULL
     , USER_ID VARCHAR2(50) NOT NULL
     , GROUP_ID VARCHAR2(50) NOT NULL
     , CREATE_DATE DATE NOT NULL
     , DELETE_DATE DATE 
     , REQUEST_XML CLOB
)
/

CREATE INDEX QT_IDX_QM_UGID ON QT_QUERY_MASTER(USER_ID,GROUP_ID,NAME)
/

CREATE TABLE QT_QUERY_RESULT_TYPE (
       RESULT_TYPE_ID NUMBER(3,0) PRIMARY KEY
     , NAME VARCHAR2(100)
     , DESCRIPTION VARCHAR2(200)
)
/


CREATE TABLE QT_QUERY_STATUS_TYPE (
       STATUS_TYPE_ID NUMBER(3,0) PRIMARY KEY
     , NAME VARCHAR2(100)
     , DESCRIPTION VARCHAR2(200)
)
/

CREATE TABLE QT_QUERY_INSTANCE (
       QUERY_INSTANCE_ID NUMBER(5,0) PRIMARY KEY
     , QUERY_MASTER_ID NUMBER(5,0) 
     , USER_ID VARCHAR2(50) NOT NULL
     , GROUP_ID VARCHAR2(50) NOT NULL
     , BATCH_MODE VARCHAR2(50)
     , START_DATE DATE NOT NULL
     , END_DATE DATE
     , STATUS_TYPE_ID NUMBER(5,0) 
     , CONSTRAINT QT_FK_QI_MID FOREIGN KEY (QUERY_MASTER_ID)
                  REFERENCES QT_QUERY_MASTER (QUERY_MASTER_ID)
     , CONSTRAINT QT_FK_QI_STID FOREIGN KEY (STATUS_TYPE_ID)
                  REFERENCES QT_QUERY_STATUS_TYPE (STATUS_TYPE_ID)
)
/
 

CREATE INDEX QT_IDX_QI_UGID ON QT_QUERY_INSTANCE(USER_ID,GROUP_ID)
/


CREATE INDEX QT_IDX_QI_MSTARTID ON QT_QUERY_INSTANCE(QUERY_MASTER_ID,START_DATE)
/


CREATE TABLE QT_QUERY_RESULT_INSTANCE (
       RESULT_INSTANCE_ID NUMBER(5,0) PRIMARY KEY
     , QUERY_INSTANCE_ID NUMBER(5,0) 
     , RESULT_TYPE_ID NUMBER(3,0) NOT NULL
     , SET_SIZE NUMBER(10,0)
     , START_DATE DATE NOT NULL
     , END_DATE   DATE
     , STATUS_TYPE_ID NUMBER(3,0) NOT NULL
     , CONSTRAINT QT_FK_QRI_RID FOREIGN KEY (QUERY_INSTANCE_ID)
                  REFERENCES QT_QUERY_INSTANCE (QUERY_INSTANCE_ID)
     , CONSTRAINT QT_FK_QRI_RTID FOREIGN KEY (RESULT_TYPE_ID)
                  REFERENCES QT_QUERY_RESULT_TYPE (RESULT_TYPE_ID)
     , CONSTRAINT QT_FK_QRI_STID FOREIGN KEY (STATUS_TYPE_ID)
                  REFERENCES QT_QUERY_STATUS_TYPE (STATUS_TYPE_ID)
)
/


CREATE TABLE QT_PATIENT_SET_COLLECTION ( 
	PATIENT_SET_COLL_ID NUMBER(10,0) PRIMARY KEY
	,RESULT_INSTANCE_ID NUMBER(5,0)
	,SET_INDEX NUMBER(10,0)
	,PATIENT_NUM NUMBER(10,0)
	,CONSTRAINT QT_FK_PSC_RI FOREIGN KEY (RESULT_INSTANCE_ID )
                  REFERENCES QT_QUERY_RESULT_INSTANCE (RESULT_INSTANCE_ID)
)
/

CREATE INDEX QT_IDX_QPSC_RIID ON QT_PATIENT_SET_COLLECTION(RESULT_INSTANCE_ID)
/


CREATE TABLE QT_PATIENT_ENC_COLLECTION (
	 PATIENT_ENC_COLL_ID NUMBER(10,0) PRIMARY KEY
	, RESULT_INSTANCE_ID NUMBER(5,0)
	, SET_INDEX NUMBER(10,0)
	, PATIENT_NUM NUMBER(10,0)
	, ENCOUNTER_NUM NUMBER(10,0)
	, CONSTRAINT QT_FK_PESC_RI FOREIGN KEY (RESULT_INSTANCE_ID)
                  REFERENCES QT_QUERY_RESULT_INSTANCE(RESULT_INSTANCE_ID)
)
/

CREATE TABLE QT_XML_RESULT (
       XML_RESULT_ID NUMBER(5,0) PRIMARY KEY
     , RESULT_INSTANCE_ID NUMBER(5,0)
     , XML_VALUE VARCHAR2(4000)
     , CONSTRAINT QT_FK_XMLR_RIID FOREIGN KEY (RESULT_INSTANCE_ID)
                  REFERENCES QT_QUERY_RESULT_INSTANCE (RESULT_INSTANCE_ID)
)
/


//*****************************************************************
//SEQUENCE CREATION
//*****************************************************************
//QUERY MASTER SEQUENE
create sequence QT_SQ_QM_QMID start with 1
/
//QUERY RESULT 
create sequence QT_SQ_QR_QRID start with 1
/
create sequence QT_SQ_QS_QSID start with 1
/
//QUERY INSTANCE SEQUENE
create sequence QT_SQ_QI_QIID start with 1
/
//QUERY RESULT INSTANCE ID
create sequence QT_SQ_QRI_QRIID start with 1
/
//QUERY PATIENT SET RESULT INSTANCE ID
/

//QUERY PATIENT SET RESULT COLLECTION ID
create sequence QT_SQ_QPR_PCID start with 1
/

//QUERY PATTIENT ENCOUNTER SET RESULT INSTANCE ID

//QUERY PATTIENT ENCOUNTER SET RESULT COLLECTION ID
create sequence QT_SQ_QPER_PECID start with 1
/


//QUERY XML RESULT INSTANCE ID
create sequence QT_SQ_QXR_XRID start with 1
/

//*****************************************************************
//INIT WITH SEED DATA
//*****************************************************************
insert into QT_QUERY_STATUS_TYPE(STATUS_TYPE_ID,NAME,DESCRIPTION) values(1,'QUEUED',' WAITING IN QUEUE TO START PROCESS')
/
insert into QT_QUERY_STATUS_TYPE(STATUS_TYPE_ID,NAME,DESCRIPTION) values(2,'PROCESSING','PROCESSING')
/
insert into QT_QUERY_STATUS_TYPE(STATUS_TYPE_ID,NAME,DESCRIPTION) values(3,'FINISHED','FINISHED')
/
insert into QT_QUERY_STATUS_TYPE(STATUS_TYPE_ID,NAME,DESCRIPTION) values(4,'ERROR','ERROR')
/
insert into QT_QUERY_STATUS_TYPE(STATUS_TYPE_ID,NAME,DESCRIPTION) values(5,'INCOMPLETE','INCOMPLETE')
/
insert into QT_QUERY_STATUS_TYPE(STATUS_TYPE_ID,NAME,DESCRIPTION) values(6,'COMPLETED','COMPLETED')
/


insert into QT_QUERY_RESULT_TYPE(RESULT_TYPE_ID,NAME,DESCRIPTION) values(1,'PATIENTSET','PATIENT SET')
/
insert into QT_QUERY_RESULT_TYPE(RESULT_TYPE_ID,NAME,DESCRIPTION) values(2,'PATIENT_ENCOUNTER_SET','PATIENT WITH ENCOUNTER SET')
/
insert into QT_QUERY_RESULT_TYPE(RESULT_TYPE_ID,NAME,DESCRIPTION) values(3,'XML','XML RESULT')
/
insert into QT_QUERY_RESULT_TYPE(RESULT_TYPE_ID,NAME,DESCRIPTION) values(4,'PATIENT_COUNT_XML','PATIENT COUNT XML')
/
insert into QT_QUERY_RESULT_TYPE(RESULT_TYPE_ID,NAME,DESCRIPTION) values(5,'PATIENT_GENDER_COUNT_XML','PATIENT GENDER COUNT XML ')
/
insert into QT_QUERY_RESULT_TYPE(RESULT_TYPE_ID,NAME,DESCRIPTION) values(6,'PATIENT_MORTALITY_COUNT_XML','PATIENT GENDER COUNT XML ')
/ 
  
 
 


//*****************************************************************
// CLEAN QUERY TOOL SCHEMA
//*****************************************************************
//DROP TABLE QT_PATIENT_ENC_COLLECTION
/
//DROP TABLE QT_PATIENT_ENCOUNTER_SET_RESULT
///
//DROP TABLE QT_PATIENT_SET_COLLECTION
/
//DROP TABLE QT_PATIENT_SET_RESULT
///
//DROP TABLE QT_XML_RESULT 
/
//DROP TABLE QT_QUERY_RESULT_INSTANCE 
/
//DROP TABLE QT_QUERY_INSTANCE 
/
//DROP TABLE QT_QUERY_STATUS 
/
//DROP TABLE QT_QUERY_STATUS_TYPE 
/
//DROP TABLE QT_QUERY_RESULT_TYPE 
/
//DROP TABLE QT_QUERY_MASTER
/


//DROP INDEX
//DROP INDEX QT_IDX_QM_UGID
/
//DROP INDEX QT_IDX_QI_UGID
/

//************* Creating Global Temp Table for Query Processing *******
//	Glboal temp table to delete rows on commit
//*********************************************************************
CREATE GLOBAL TEMPORARY TABLE DX  (
 ENCOUNTER_NUM NUMBER(22,0),
 PATIENT_NUM NUMBER(22,0)
 ) on COMMIT PRESERVE ROWS 
 /
 CREATE GLOBAL TEMPORARY TABLE QUERY_GLOBAL_TEMP   ( 
 ENCOUNTER_NUM NUMBER(22,0),
 PATIENT_NUM NUMBER(22,0),
 PANEL_COUNT number(5,0),
 fact_count number(22,0),
 fact_panels number(5,0)
 ) on COMMIT PRESERVE ROWS --on commit delete rows
/

 
//************* Release 1.0 change **************
// Adding Delete flag for tables
//***********************************************
alter table qt_query_master modify name varchar2(250)
/

alter table qt_query_master add delete_flag varchar2(3)
/
alter table qt_query_instance add delete_flag varchar2(3)
/
alter table qt_query_result_instance add delete_flag varchar2(3)
/
//Set Delete flag to 'N' for all the records
update QT_QUERY_MASTER set delete_flag='N'
/
update QT_QUERY_INSTANCE set delete_flag='N'
/
update QT_QUERY_RESULT_INSTANCE set delete_flag='N'
/

//*****************************************************************
// ARRAY TYPE FOR PDO QUERY
//*****************************************************************
CREATE OR REPLACE TYPE QT_PDO_QRY_INT_ARRAY AS varray(100000) of  NUMBER(20) 
/
CREATE OR REPLACE TYPE QT_PDO_QRY_STRING_ARRAY AS varray(100000) of  VARCHAR2(150) 
/


//************* Release 1.0 change **************
// Adding Delete flag for tables
//***********************************************
alter table qt_query_master add  generated_sql clob
/


//***********************************************
// Add lookup tables for tablepdo and setfinder
//***********************************************
//RUN, CREATE LOOKUP TABLE SQL FROM INFRA1(KRISTEL)


