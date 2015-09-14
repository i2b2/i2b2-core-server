	INSERT INTO [dbo].[TABLE_ACCESS]([c_table_cd], [c_table_name], [c_protected_access], [c_hlevel], [c_name], [c_fullname], [c_synonym_cd], [c_visualattributes], [c_tooltip], [c_total_num], [c_basecode], [c_comment], [c_metadataxml], [c_facttablecolumn], [c_dimtablename], [c_columnname], [c_columndatatype], [c_operator], [c_dimcode], [c_entry_date], [c_change_date], [c_status_cd])
VALUES('rpdr', 'rpdr', 'N', 0, 'RPDR', '\RPDR', 'N', 'CA', '\RPDR', NULL, NULL, NULL, NULL, 'concept_cd', 'concept_dimension', 'concept_path', 'T', 'LIKE', '\RPDR', '20080304 15:10:43', NULL, NULL)

	INSERT INTO [dbo].[ONT_DB_LOOKUP]([c_domain_id], [c_project_path], [c_owner_id], [c_db_fullschema], [c_db_datasource],[c_db_servertype], [c_db_nicename], [c_comment], [c_entry_date], [c_change_date], [c_status_cd])
VALUES('ra_test', 'Demo/', '@', 'ra_mart_test.dbo', 'java:OntologyDemoDS', 'SQLSERVER', 'RA Mart',  NULL,  
'20080304 15:10:43', NULL, NULL)

	INSERT INTO [dbo].[ONT_DB_LOOKUP]([c_domain_id], [c_project_path], [c_owner_id], [c_db_fullschema], [c_db_datasource],[c_db_servertype], [c_db_nicename], [c_comment], [c_entry_date], [c_change_date], [c_status_cd])
VALUES('ra_test', 'Demo/', 'demo', 'ra_mart_test.dbo', 'java:OntologyDemoDS', 'SQLSERVER', 'RA Mart',  NULL,  
'20080304 15:10:43', NULL, NULL)