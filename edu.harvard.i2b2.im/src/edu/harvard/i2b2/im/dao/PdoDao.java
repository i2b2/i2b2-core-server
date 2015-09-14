/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors:
 * 		Lori Phillips
 */
package edu.harvard.i2b2.im.dao;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.w3c.dom.Element;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.jaxb.DTOFactory;
import edu.harvard.i2b2.common.util.jaxb.JAXBUnWrapHelper;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.common.util.xml.XMLUtil;
import edu.harvard.i2b2.im.datavo.i2b2message.BodyType;
import edu.harvard.i2b2.im.datavo.pdo.PatientDataType;
import edu.harvard.i2b2.im.datavo.pdo.PidSet;
import edu.harvard.i2b2.im.datavo.pdo.PidType;
import edu.harvard.i2b2.im.datavo.pdo.PidType.PatientId;
import edu.harvard.i2b2.im.datavo.pdo.PidType.PatientMapId;
import edu.harvard.i2b2.im.datavo.pdo.query.FilterListType;
import edu.harvard.i2b2.im.datavo.pdo.query.GetPDOFromInputListRequestType;
import edu.harvard.i2b2.im.datavo.pdo.query.OutputOptionListType;
import edu.harvard.i2b2.im.datavo.pdo.query.OutputOptionNameType;
import edu.harvard.i2b2.im.datavo.pdo.query.OutputOptionSelectType;
import edu.harvard.i2b2.im.datavo.pdo.query.OutputOptionType;
import edu.harvard.i2b2.im.datavo.pdo.query.PageByPatientType;
import edu.harvard.i2b2.im.datavo.pdo.query.PageRangeType;
import edu.harvard.i2b2.im.datavo.pdo.query.PageType;
import edu.harvard.i2b2.im.datavo.pdo.query.PatientDataResponseType;
import edu.harvard.i2b2.im.datavo.pdo.query.PdoQryHeaderType;
import edu.harvard.i2b2.im.datavo.pdo.query.PdoRequestTypeType;
import edu.harvard.i2b2.im.datavo.pdo.query.PidListType;
import edu.harvard.i2b2.im.datavo.pdo.query.PidListType.Pid;
import edu.harvard.i2b2.im.datavo.pdo.query.RequestType;
import edu.harvard.i2b2.im.datavo.pm.ProjectType;
import edu.harvard.i2b2.im.datavo.wdo.AuditType;
import edu.harvard.i2b2.im.datavo.wdo.AuditsType;
import edu.harvard.i2b2.im.datavo.wdo.GetAuditType;
import edu.harvard.i2b2.im.delegate.crc.CallCRCUtil;
import edu.harvard.i2b2.im.util.HighEncryption;
import edu.harvard.i2b2.im.ws.PDORequestMessage;
import edu.harvard.i2b2.im.util.IMUtil;
import edu.harvard.i2b2.im.ejb.DBInfoType;
import edu.harvard.i2b2.im.util.StringUtil;


public class PdoDao  extends JdbcDaoSupport {

	private static Log log = LogFactory.getLog(PdoDao.class);

	private GetPDOFromInputListRequestType getPDOFromInputListRequestType = null;

	private 				 DTOFactory dtoFactory = new DTOFactory();

	private SimpleJdbcTemplate jt;

	private void setDataSource(String dataSource) {
		DataSource ds = null;
		try {
			ds = IMUtil.getInstance().getDataSource(dataSource);
		} catch (I2B2Exception e2) {
			log.error(e2.getMessage());;
		} 
		this.jt = new SimpleJdbcTemplate(ds);
	}

	private String getMetadataSchema() throws I2B2Exception{

		return IMUtil.getInstance().getIMDataSchemaName();
	}


	public String getPDO(PdoQryHeaderType requestType,
			ProjectType projectInfo, PDORequestMessage getFoldersMsg) throws Exception {

		
		boolean protectedAccess = false;
		Iterator it = projectInfo.getRole().iterator();
		while (it.hasNext()){
			String role = (String) it.next();
			if(role.toLowerCase().equalsIgnoreCase("DATA_PROT")) {
				protectedAccess = true;
				break;
			}
		}

		if (!protectedAccess)
			throw new I2B2DAOException("Access Denied");

		
		//Update so sending is from IM cell
		getFoldersMsg.getMessageHeaderType().getSendingApplication().setApplicationName("Identity Management Cell");
		getFoldersMsg.getMessageHeaderType().getSendingApplication().setApplicationVersion("1.7");

		getPDOFromInputListRequestType = getFoldersMsg.getgetPDOFromInputListRequestType();

		//Clear the filter list if set
		FilterListType filterList = new FilterListType();
		getPDOFromInputListRequestType.setFilterList(filterList );

		//Set output options to only be pidset

		OutputOptionListType outputOptions = new OutputOptionListType();
		outputOptions.setNames(OutputOptionNameType.ASATTRIBUTES);

		OutputOptionType optionType = new OutputOptionType();
		optionType.setOnlykeys(false);
		optionType.setSelect(OutputOptionSelectType.USING_INPUT_LIST);
		outputOptions.setPidSet(optionType);
		getPDOFromInputListRequestType.setOutputOption(outputOptions);
		//Go through the patie
		//	String patientList = null;
		if (getPDOFromInputListRequestType.getInputList() != null & getPDOFromInputListRequestType.getInputList().getPidList() != null) 
		{
			List<Pid>  pids = getPDOFromInputListRequestType.getInputList().getPidList().getPid(); 
			//	else if (getPDOFromInputListRequestType.getInputList() != null & getPDOFromInputListRequestType.getInputList().getPatientList() != null)
			//		patientList =  getPDOFromInputListRequestType.getInputList().getPatientList().getPatientSetCollId();

			HighEncryption highEnc = new  HighEncryption(IMKey.getKey(projectInfo));
			if (highEnc == null)
				throw new I2B2Exception ("High Encrpytion not found.");
			for (Pid pid : pids)
			{
				if ((!pid.getSource().endsWith("_E")) && (!pid.getSource().equals("HIVE")))
				{
					pid.setValue( highEnc.mrn_encrypt(pid.getValue(), true, pid.getSource()));	
					pid.setSource(pid.getSource() + "_E");
				}
				//	pid.setValue( pid.getValue());	
				//	pid.setSource(pid.getSource());

			}
		}
		// Call the CRC with the new PDO
		return CallCRCUtil.callCRCPDORequest(getFoldersMsg);
	}


	public int addAudit( String projectInfo, PidSet pids, String userId, String comments, DBInfoType dbInfo) throws I2B2DAOException, I2B2Exception{
		String metadataSchema = dbInfo.getDb_fullSchema();
		setDataSource(dbInfo.getDb_dataSource());


		ParameterizedRowMapper<String> map = new ParameterizedRowMapper<String>() {
			public String mapRow(ResultSet rs, int rowNum) throws SQLException {
				String name = (rs.getString("c_table_name"));
				return name;
			}
		};



		int numRowsAdded = -1;
		try {
			String xml = null;
			if (comments != null) {
				String addSql = "insert into " + metadataSchema  + 
						"IM_AUDIT (lcl_site, lcl_id, user_id, project_id, comments) values (?,?,?,?,?)";

				for (PidType pidType : pids.getPid()) {
					numRowsAdded += jt.update(addSql, 
							pidType.getPatientId().getSource(), pidType.getPatientId().getValue(),
							userId, projectInfo, comments);
					for (PatientMapId pidPatient : pidType.getPatientMapId())
					{
						numRowsAdded += jt.update(addSql, 
								pidPatient.getSource(), pidPatient.getValue(),
								userId, projectInfo, comments);

					}
				}
			}		
			else {
				String addSql = "insert into " + metadataSchema  + 
						"IM_AUDIT (lcl_site, lcl_id, user_id, project_id) values (?,?,?,?)";

				for (PidType pidType : pids.getPid()) {
				//	numRowsAdded += jt.update(addSql, 
				//			pidType.getPatientId().getSource(), pidType.getPatientId().getValue(),
				//			userId, projectInfo);
					for (PatientMapId pidPatient : pidType.getPatientMapId())
					{
						numRowsAdded += jt.update(addSql, 
								pidPatient.getSource(), pidPatient.getValue(),
								userId, projectInfo);

					}
				}
			}
		} catch (DataAccessException e) {
			log.error("Dao addChild failed");
			log.error(e.getMessage());
			throw new I2B2DAOException("Data access error " , e);
		}

		//	log.info(addSql +  " " + numRowsAdded);
		log.debug("Number of rows added: " + numRowsAdded);

		return numRowsAdded;

	}

	public PidSet findPidsByProject(final PidSet returnType, final String userId, final ProjectType projectInfo, final DBInfoType dbInfo) throws Exception{

		PidSet pidSet = new PidSet();


		String tempTable = "im_temp_site";

		String metadataSchema = dbInfo.getDb_fullSchema();

		setDataSource(dbInfo.getDb_dataSource());



		//		 First step is to call PM to see what roles user belongs to.

		if (projectInfo.getRole().size() == 0)
		{
			log.error("no role found for this user in project: " + projectInfo.getName());
			I2B2Exception e = new I2B2Exception("No role found for user");
			throw e;
		}

		boolean protectedAccess = false;
		Iterator it = projectInfo.getRole().iterator();
		while (it.hasNext()){
			String role = (String) it.next();
			if(role.toLowerCase().equalsIgnoreCase("DATA_PROT")) {
				protectedAccess = true;
				break;
			}
		}

		if (!protectedAccess)
			throw new I2B2DAOException("Access Denied");



		ParameterizedRowMapper<PatientMapId> map = new ParameterizedRowMapper<PatientMapId>() {
			public PatientMapId mapRow(ResultSet rs, int rowNum) throws SQLException {
				PatientMapId pid = new PatientMapId();
				pid.setValue(rs.getString("lcl_id"));
				pid.setSource(rs.getString("lcl_site"));
				return pid;
			}
		};

		HighEncryption highEnc = new  HighEncryption(IMKey.getKey(projectInfo));
		if (highEnc == null)
			throw new I2B2Exception ("High Encrpytion not found.");




		//If sqlserver add # in front of it
		if (dbInfo.getDb_serverType().toUpperCase().equals("SQLSERVER"))
		{
			tempTable = "#"+tempTable;
			tempTable = metadataSchema  + tempTable;
			String sql = "CREATE TABLE " + tempTable +"  (  "+
					"LCL_SITE         VARCHAR(50) NULL, "+
					"LCL_ID           VARCHAR(200) NULL, "+
					"PROJECT_ID       VARCHAR(50) NULL "+
					")";
			jt.update(sql); 
		} else 	if (dbInfo.getDb_serverType().toUpperCase().equals("POSTGRESQL"))
		{

			// Drop if already exists
				String sql = "DROP TABLE IF EXISTS "+  tempTable;
				jt.update(sql);
			
			
			 sql = "CREATE TEMP TABLE " +  tempTable +"  (  "+
					"LCL_SITE         VARCHAR(50) NULL, "+
					"LCL_ID           VARCHAR(200) NULL, "+
					"PROJECT_ID       VARCHAR(50) NULL "+
					")";
			jt.update(sql); 
		} else
		{
			tempTable = metadataSchema  + tempTable;
		}

		//Save entries in temp table
		String addSql = "insert into " +  tempTable +
				" (lcl_site, lcl_id, project_id) values (?,?,?)";

		for (PidType pidType: returnType.getPid()) {
			int numRowsAdded = 0;

			PidType pidt = new PidType();

			pidt.setPatientId(pidType.getPatientId());
			for (PatientMapId pid : pidType.getPatientMapId()) {
				//If enecrpyted than decyppy

				if (pid.getSource().endsWith("_E"))
				{
					pid.setValue(highEnc.mrn_decrypt(pid.getValue(), true));
					pid.setSource(pid.getSource().substring(0,pid.getSource().length()-2));

				}
				//	pid.setValue( highEnc.mrn_encrypt(pid.getValue(), true, pid.getSource()));	
				//	pid.setSource(pid.getSource() + "_E");


				numRowsAdded += jt.update(addSql, pid.getSource(), pid.getValue(), projectInfo.getId()); 
			}





			List<PidType.PatientMapId> queryResult = null;		
			String tablesSql = 

					"SELECT distinct m1.lcl_id, m1.lcl_site " +
							"FROM   (SELECT global_id,  " +
							"               lcl_site,  " +
							"               lcl_id,  " +
							"				lcl_status, " +
							"              Row_number()  " +
							"                 over (  " +
							"                   PARTITION BY lcl_site, lcl_id  " +
							"                   ORDER BY update_date) AS new_id  " +
							"        FROM    " +  metadataSchema +  "im_mpi_mapping) m1,  " +
							metadataSchema +  "im_mpi_demographics d,  " +
							metadataSchema +  "im_project_patients pp,  " +
							metadataSchema +  "im_project_sites ps,  " +
							tempTable + " ts  " +
							"WHERE  m1.new_id = 1  " +
							"       AND d.global_id = m1.global_id  " +
							"       AND d.global_status = 'A'  " +
							"		AND m1.lcl_status = 'A' " +
							"       AND pp.global_id = d.global_id  " +
							"       AND ps.project_id = pp.project_id  " +
							"       AND m1.lcl_site = ts.lcl_site  " +
							"       AND m1.lcl_id = ts.lcl_id  " ;


			try {
				queryResult = jt.query(tablesSql, map);	    

				//				queryResult = jt.query(tablesSql, mapper, "N", projectInfo.getId().toLowerCase());
			} catch (DataAccessException e) {
				log.error(e.getMessage());
				throw new I2B2DAOException("Database error: " + e.getMessage());
			}

			log.debug("result size = " + queryResult.size());

			//PatientMap
			pidt.getPatientMapId().addAll(queryResult);
			pidSet.getPid().add(pidt);

			if (dbInfo.getDb_serverType().toUpperCase().equals("SQLSERVER") )	{
				String sql = "DROP TABLE " + tempTable;
				jt.update(sql); 
			} else if (dbInfo.getDb_serverType().toUpperCase().equals("POSTGRESQL"))
			{
				String sql = "DISCARD TEMP ";
				jt.update(sql); 				
			} else {
				String sql = "DELETE FROM " + tempTable;
				jt.update(sql);
				
			}
		}
		return pidSet;
	}



	public AuditsType getAudit(final GetAuditType auditType, final String userId, final ProjectType projectInfo, final DBInfoType dbInfo) throws Exception{


		String metadataSchema = dbInfo.getDb_fullSchema();
		setDataSource(dbInfo.getDb_dataSource());



		//		 First step is to call PM to see what roles user belongs to.

		if (projectInfo.getRole().size() == 0)
		{
			log.error("no role found for this user in project: " + projectInfo.getName());
			I2B2Exception e = new I2B2Exception("No role found for user");
			throw e;
		}

		boolean protectedAccess = false;
		Iterator it = projectInfo.getRole().iterator();
		while (it.hasNext()){
			String role = (String) it.next();
			if(role.toLowerCase().equalsIgnoreCase("ADMIN") || (role.toLowerCase().equalsIgnoreCase("MANAGER"))) {
				protectedAccess = true;
				break;
			}
		}

		if (!protectedAccess)
			throw new I2B2DAOException("Access Denied");

		ParameterizedRowMapper<AuditType> map = new ParameterizedRowMapper<AuditType>() {
			public AuditType mapRow(ResultSet rs, int rowNum) throws SQLException {
				AuditType pid = new AuditType();
				pid.setPid(rs.getString("lcl_id"));
				pid.setComment(rs.getString("comments"));
				pid.setProjectId(rs.getString("project_id"));
				pid.setSource(rs.getString("lcl_site"));
				pid.setImportDate(dtoFactory
						.getXMLGregorianCalendar(rs.getTimestamp(
								"query_date").getTime()));
				pid.setUserId(rs.getString("user_id"));
				return pid;
			}
		};
		List<AuditType> queryResult = null;		
		String tablesSql = "";
		int min = 1;
		int max = 1000;

		if (dbInfo.getDb_serverType().equals("ORACLE")) {
			tablesSql =	"SELECT * FROM ( " +
					"    SELECT a.*, rownum r__ " +
					"    FROM " +
					"    ( " +
					"    	SELECT * FROM " +  metadataSchema +  "im_audit WHERE ";

			if (auditType != null)
			{
				if (auditType.getMin() != null)
					min = auditType.getMin();
				if (auditType.getMax() != null)
					max = auditType.getMax();
				if ((auditType.getPid() != null)  &&  (auditType.getPid().length() > 0))
					tablesSql += "lcl_site = '"+auditType.getSource()+"' and lcl_id = '"+auditType.getPid() +"' and ";
				if ((auditType.getUserId() != null)  &&  (auditType.getUserId().length() > 0))
					tablesSql += "user_id = '"+ auditType.getUserId() +"' and ";
				if ((auditType.getProjectId() != null) &&  (auditType.getProjectId().length() > 0))
					tablesSql += " project_id =  '"+auditType.getProjectId()+"' and ";

				if (auditType.getComment() != null)
					tablesSql += " comment =  '"+auditType.getComment()+"' and ";


			}


			tablesSql += " 1 = 1 " +
					"    	ORDER BY query_date DESC " +
					"    ) a " +
					"    WHERE rownum < (("+ min +" * "+max + ") + 1 ) " +
					" ) " +
					" WHERE r__ >= ((("+ min +"-1) * "+max + ") + 1) ";




		} else 	if (dbInfo.getDb_serverType().equals("SQLSERVER") || (dbInfo.getDb_serverType().equals("POSTGRESQL"))) {
			tablesSql =	"SELECT * FROM ( " +
					"    SELECT ROW_NUMBER() OVER ( ORDER BY query_date ) AS RowNum, * " +
					"       FROM " +  metadataSchema +  "im_audit WHERE ";

			if (auditType != null)
			{
				if (auditType.getMin() != null)
					min = auditType.getMin();
				if (auditType.getMax() != null)
					max = auditType.getMax();
				if (auditType.getPid() != null)
					tablesSql += "lcl_site = '"+auditType.getSource()+"' and lcl_id = '"+auditType.getPid() +"' and ";
				if ((auditType.getUserId() != null) &&  (auditType.getUserId().length() > 0))
					tablesSql += "user_id = '"+ auditType.getUserId() +"' and ";
				if ((auditType.getProjectId() != null) &&  (auditType.getProjectId().length() > 0))
					tablesSql += " project_id =  '"+auditType.getProjectId()+"' and ";

				if (auditType.getComment() != null)
					tablesSql += " comment =  '"+auditType.getComment()+"' and ";


			}


			tablesSql += " 1=1 " +
					"    ) as  RowConstrainedResult " +
					"    WHERE RowNum >= "+ min +" and RowNum < "+max +
					" ORDER BY RowNum ";
		}

		try {
			queryResult = jt.query(tablesSql, map);	    

			//				queryResult = jt.query(tablesSql, mapper, "N", projectInfo.getId().toLowerCase());
		} catch (DataAccessException e) {
			log.error(e.getMessage());
			throw new I2B2DAOException("Database error: " + e.getMessage());
		}

		log.debug("result size = " + queryResult.size());


		AuditsType auditTypes = new AuditsType();
		auditTypes.getAudit().addAll( queryResult);

		return auditTypes; //patientdataResonse;
	}

}
