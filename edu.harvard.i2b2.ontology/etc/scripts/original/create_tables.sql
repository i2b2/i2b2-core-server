CREATE TABLE I2B2METADATA.ONT_DB_LOOKUP ( 
	c_domain_id    	VARCHAR2(255) NOT NULL,
	c_project_path 	VARCHAR2(255) NOT NULL,
	c_owner_id     	VARCHAR2(255) NOT NULL,
	c_db_fullschema	VARCHAR2(255) NOT NULL,
	c_db_datasource	VARCHAR2(255) NOT NULL,
	c_db_servertype	VARCHAR2(255) NOT NULL,
	c_db_nicename  	VARCHAR2(255) NOT NULL,
	c_db_tooltip   	VARCHAR2(255) NULL,
	c_comment      	CLOB NULL,
	c_entry_date   	DATE NULL,
	c_change_date  	DATE NULL,
	c_status_cd    	CHAR(1) NULL 
	)

	
	CREATE TABLE I2B2METADATA.TABLE_ACCESS ( 
	c_table_cd    	VARCHAR2(255) NOT NULL,
	c_table_name      VARCHAR2(255) NOT NULL,
	c_protected_access     	CHAR(1) NULL,
	c_hlevel         INT,
	c_name            VARCHAR2(255) NOT NULL,
	c_fullname         VARCHAR2(255) NOT NULL,
	c_synonym_cd  	CHAR(1) NOT NULL,
	c_visualattributes   	VARCHAR2(255) NOT NULL,
    c_tooltip           VARCHAR2(255) NULL,
    c_totalnum        INT,
    c_basecode           VARCHAR2(255) NULL,
	c_comment      	CLOB NULL,
	c_metadataxml      	CLOB NULL,
	c_facttablecolumn          VARCHAR2(255) NOT NULL,
	c_dimtablename            VARCHAR2(255) NOT NULL,
	c_columnname         VARCHAR2(255) NOT NULL,
	c_columndatatype            VARCHAR2(255) NOT NULL,
	c_operator         VARCHAR2(255) NOT NULL,
	c_dimcode         VARCHAR2(255) NOT NULL,
	c_entry_date   	DATE NULL,
	c_change_date  	DATE NULL,
	c_status_cd    	CHAR(1) NULL 
	)
	
	


	
	