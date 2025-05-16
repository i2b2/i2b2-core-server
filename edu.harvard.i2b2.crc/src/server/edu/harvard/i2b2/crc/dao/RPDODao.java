/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
/*

 * 
 * Contributors:
 * 		Wayne Chan
 */
package edu.harvard.i2b2.crc.dao;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.ServiceLocator;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.crc.datavo.i2b2message.MessageHeaderType;
import edu.harvard.i2b2.crc.datavo.i2b2message.SecurityType;
import edu.harvard.i2b2.crc.datavo.pdo.query.ConceptTableType;
import edu.harvard.i2b2.crc.datavo.pdo.query.DblookupType;
import edu.harvard.i2b2.crc.datavo.pdo.query.DeleteDblookupType;
import edu.harvard.i2b2.crc.datavo.pdo.query.RpdoType;
import edu.harvard.i2b2.crc.datavo.pdo.query.RpdosType;
import edu.harvard.i2b2.crc.datavo.pdo.query.SetDblookupType;
import edu.harvard.i2b2.crc.datavo.pm.ProjectType;
import edu.harvard.i2b2.crc.delegate.pm.PMServiceDriver;
import edu.harvard.i2b2.crc.loader.ejb.DataMartLoaderAsyncBean;
import edu.harvard.i2b2.crc.util.QueryProcessorUtil;
import edu.harvard.i2b2.crc.dao.pdo.RpdoTable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import javax.sql.DataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class RPDODao extends JdbcDaoSupport {
	protected static Log logesapi = LogFactory.getLog(RPDODao.class);

	private static Log log = LogFactory.getLog(RPDODao.class);
	private static DataSource ds = null;
	private static JdbcTemplate jt;
	private static String dbluTable;
	private static String breakdownTable;
	private static String resultTypeTable;	
	private static String getTable = " (LOWER(group_id)=LOWER(?) OR group_id='@')  AND table_instance_id=? order by set_index ";
	private static String keyOrderRegular = " (LOWER(group_id)=LOWER(?) OR group_id='@') AND (LOWER(user_id)=LOWER(?) OR SHARED='Y') "; //AND (LOWER(user_id)=LOWER(?) OR user_id='@') "; //ORDER BY TABLE_INSTANCE_NAME ";
	private static String keyOrderPriv = " (LOWER(group_id)=LOWER(?) OR group_id='@') "; //AND (LOWER(user_id)=LOWER(?) OR user_id='@') "; //ORDER BY TABLE_INSTANCE_NAME ";
	private String domainId = null;
	private String userId = null;
	private String projectPath = null;
	private String dataSchema = "";
	private String serverType = "";
	//private String projectId = null;
	private boolean isManager = false;
	private boolean isAdmin = false;

	public RPDODao() {		
		initDblookupDao();
	} 


	public RPDODao(String domainID, String userID, String projectPath) throws I2B2Exception, JAXBUtilException {
		this.domainId = domainID;
		this.userId = userID;
		this.projectPath = projectPath;

		initDblookupDao();

	}

	public RPDODao(MessageHeaderType reqMsgHdr) throws I2B2Exception, JAXBUtilException {
		domainId = reqMsgHdr.getSecurity().getDomain();
		userId = reqMsgHdr.getSecurity().getUsername();
		projectPath = reqMsgHdr.getProjectId();

		initDblookupDao();

		try {
			PMServiceDriver pmServiceDriver = new PMServiceDriver();
			ProjectType projectType = pmServiceDriver.checkValidUser(reqMsgHdr.getSecurity(),
					projectPath);

			if (projectType != null && projectType.getRole().size() > 0) {
				for(String role: projectType.getRole()) {
					if ((role.equals("MANAGER"))) {
						isManager = true;
					}
					if ((role.equals("ADMIN"))) {
						isAdmin = true;
					}
				}
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}		
	}


	private void initDblookupDao() {		


		try {


			DAOFactoryHelper daoFactoryHelper = new DAOFactoryHelper(
					domainId, projectPath, userId);

			IDAOFactory daoFactory = daoFactoryHelper.getDAOFactory();
			ds = daoFactory.getSetFinderDAOFactory().getDataSource();

			dataSchema = daoFactory.getSetFinderDAOFactory().getDataSourceLookup().getFullSchema();
			serverType = daoFactory.getSetFinderDAOFactory().getDataSourceLookup().getServerType();
			//ds = QueryProcessorUtil.getInstance().getDataSource("java:/CRCBootStrapDS");
			//ServiceLocator.getInstance()
			//.getAppServerDataSource(dataSourceName);
		} catch (I2B2Exception e2) {
			log.error(e2.getMessage());;
		} 
		jt = new JdbcTemplate(ds);
		if (dataSchema.equals(""))
			try {

				Connection conn = ds.getConnection();

				dataSchema = conn.getSchema();
				conn.close();
			} catch (SQLException e1) {
				log.error(e1.getMessage());
			} 
		if (dataSchema.endsWith(".")) {
			dbluTable = dataSchema + "RPDO_TABLE_REQUEST ";
			breakdownTable = dataSchema + "QT_BREAKDOWN_PATH ";
			resultTypeTable = dataSchema + "QT_QUERY_RESULT_TYPE ";

		} else {
			dbluTable = dataSchema + ".RPDO_TABLE_REQUEST ";
			breakdownTable = dataSchema + ".QT_BREAKDOWN_PATH ";
			resultTypeTable = dataSchema + ".QT_QUERY_RESULT_TYPE ";
		}
		log.info("RPDO_TABLE_REQUEST = " + dbluTable);
	} 

	public String slashSandwich(String s) {
		StringBuffer sb = new StringBuffer();
		if (!s.startsWith("/")) {
			sb.append('/');
		}
		sb.append(s);
		if (!s.endsWith("/")) {
			sb.append('/');
		}
		log.info(sb.toString());
		return sb.toString();
	}


	public List<RpdoType> findRPDOs() throws DataAccessException, I2B2DAOException{	
		//String sql = "SELECT * FROM " +  dbluTable + " WHERE" + keyOrder;		
		String sql = "select distinct table_instance_id, table_instance_name, max(set_index) as set_index, user_id, shared, create_date, update_date "
				+ "  FROM " +  dbluTable 
				+ " WHERE " + keyOrderRegular
				+ "  group by  table_instance_id, table_instance_name, user_id, shared, create_date, update_date";

		/*	if (isAdmin || isManager)
			sql = "select distinct table_instance_id, table_instance_name, max(set_index) as set_index, user_id, shared, create_date "
					+ "  FROM " +  dbluTable 
					+ " WHERE delete_flag = 'N' and " + keyOrderPriv
					+ "  group by  table_instance_id, table_instance_name, user_id, shared, create_date";

		log.info("my sql"+ sql);
		log.info("isAdmin" + isAdmin);
		log.info("isManager" + isManager);
		 */
		List<RpdoType> queryResult = null;
		try {
			/*
			if (isAdmin || isManager)
				queryResult = jt.query(sql, new getMapperRPDOs(), projectPath);
			else
			 */
			queryResult = jt.query(sql, new getMapperRPDOs(), projectPath, userId);

		} catch (DataAccessException e) {
			log.error(e.getMessage());
			throw e;
		}
		log.info("result size = " + queryResult.size());		
		return queryResult;
	}

	public List<RpdoTable> getRPDO( int tableID) throws DataAccessException, I2B2Exception {
		String sql = "SELECT table_instance_id, table_instance_name, column_name, user_id, set_index, json_data, required, shared, create_date, update_date, delete_flag FROM " +  dbluTable + " WHERE " + getTable;		
		List<RpdoTable> queryResult = null;
		try {
			queryResult = jt.query(sql, new getMapperRPDO(), projectPath,tableID);
		} catch (DataAccessException e) {
			log.error(e.getMessage());
			e.printStackTrace();
			throw new I2B2DAOException("Database error");
		}
		return queryResult;	
	}


	public int setRPDO(final RpdoType rpdoType) throws DataAccessException, I2B2Exception {
		//verify ismanager and can set userId to '@'
		if (isAdmin == false && rpdoType.getCreatorId().equals("@"))
			throw new I2B2Exception("Access Denied");	
		if (isAdmin && rpdoType.getCreatorId().equals("@"))
		{
			projectPath = "@";
			userId = "@";
		}

		RpdoTable origRpdo = null;
		int naxtTableInstanceID = 0;
		int numRowsAdded = 0;
		String sql = "SELECT MAX(TABLE_INSTANCE_ID) from " + dbluTable;

		naxtTableInstanceID = jt.queryForObject(sql, Integer.class) + 1;

		java.sql.Timestamp now = new java.sql.Timestamp(System.currentTimeMillis());
		java.sql.Timestamp create = now;
		if (rpdoType.getId() != null)
		{

			origRpdo = getRPDO(rpdoType.getId()).get(0);
			sql = "DELETE FROM  " + dbluTable +
					" WHERE TABLE_INSTANCE_ID = ?";

			jt.update(sql, 
					rpdoType.getId());

			naxtTableInstanceID = origRpdo.getId();
			create = new java.sql.Timestamp(origRpdo.getCreateDate().getTime());
			userId = origRpdo.getCreatorId();
		} 

		for(int i=0; i < rpdoType.getConcept().size(); i++)
		{
			//
			//	}RpdoType rpdoType: rpdosType.getRPDO()) {
			//RpdoType rpdoType = rpdosType.getRpdo().get(i);
			String json = rpdoType.getConcept().get(i).getData();

			//Gson gson = new Gson();
			//json = gson.toJson(json);

			if (json != null && !json.trim().equals("")) {
				String c_fullpath = " ";
				String c_columnname  = " ";
				String c_tablename  = " ";
				String c_operator  = " ";
				String c_dimcode  = " ";
				String aggType = " ";
				
				String valueType = null;
				String valueOperator = null;
				String value = null;
				String valueUnit = null;
				String dateTo = null;
				String dateFrom = null;

				try {
					json = json.trim();
					JsonElement jelement = JsonParser.parseString(json);
					//JsonElement jelement = new JsonParser().parse(json);

					JsonArray  jarray = jelement.getAsJsonArray();
					JsonObject jobject = jarray.get(0).getAsJsonObject();



					aggType = jobject.get("dataOption").getAsString();

					JsonObject jObj= jobject.get("sdxData").getAsJsonObject();
					JsonElement jObj2 = jObj.get("origData");
					jobject =jObj2.getAsJsonObject();
					c_fullpath = jobject.get("key").getAsString(); 
					c_columnname  = jobject.get("column_name").getAsString(); 
					c_tablename  = jobject.get("table_name").getAsString(); 
					c_operator  = jobject.get("operator").getAsString(); 
					c_dimcode  = jobject.get("dim_code").getAsString(); 
					
					//Get lab values
					JsonElement jObj3 = jObj.get("LabValues");
					if (jObj3 != null)
					{
						jobject =jObj3.getAsJsonObject();

						valueType = jobject.get("ValueType").getAsString(); 
						valueOperator = jobject.get("ValueOperator").getAsString(); 
						value = jobject.get("Value").getAsString(); 
						valueUnit = jobject.get("ValueUnit").getAsString(); 
						
					}
					
					//Get date range
					JsonElement jObj4 = jObj.get("dateRange");
					if (jObj4 != null)
					{
						jobject =jObj4.getAsJsonObject();

						dateFrom = jobject.get("start").getAsString(); 
						if (dateFrom.equals(""))
							dateFrom = null;
						dateTo = jobject.get("end").getAsString(); 
						if (dateTo.equals(""))
							dateTo = null;

					}
					
				} catch (Exception e) {}

				if (rpdoType.isVisible() == null) 
					rpdoType.setVisible(true);
				if (rpdoType.isShared() == null) 
					rpdoType.setShared(false);

				if (rpdoType.getTitle().length() > 199)
					rpdoType.setTitle(rpdoType.getTitle().substring(0, 199));

				sql = "INSERT INTO " + dbluTable +
						"(TABLE_INSTANCE_ID, TABLE_INSTANCE_NAME, COLUMN_NAME, C_FACTTABLECOLUMN, C_FULLPATH, AGG_TYPE,C_COLUMNNAME,C_TABLENAME,C_OPERATOR,C_DIMCODE,CONSTRAIN_BY_DATE_TO, CONSTRAIN_BY_DATE_FROM, CONSTRAIN_BY_VALUE_OPERATOR, CONSTRAIN_BY_VALUE_CONSTRAINT, CONSTRAIN_BY_VALUE_UNIT_OF_MEASURE, CONSTRAIN_BY_VALUE_TYPE,USER_ID ,GROUP_ID ,SET_INDEX,JSON_DATA,CREATE_DATE,UPDATE_DATE,DELETE_FLAG,USE_AS_COHORT,REQUIRED,SHARED) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,'N',?,?)";		

				numRowsAdded = jt.update(sql, 
						naxtTableInstanceID,
						rpdoType.getTitle(),
						rpdoType.getConcept().get(i).getName(),
						"concept_cd",
						c_fullpath,
						aggType,
						c_columnname,
						c_tablename,
						c_operator,
						c_dimcode,
						
						dateTo,
						dateFrom,
						valueOperator,
						value, 
						valueUnit,
						valueType,
						
						userId,
						projectPath,
						i,
						json,
						create,
						now,
						(rpdoType.getConcept().get(i).isDisplay() == false?"Y":"N"),
						(rpdoType.getConcept().get(i).isRequired()== true?"Y":"N"),
						(rpdoType.isShared() == true?"Y":"N")
						);

				log.info("setRPDO - Number of rows added: " + numRowsAdded);

			}
		}

		//Update required
		if (serverType.equalsIgnoreCase(DAOFactoryHelper.ORACLE))
			sql = "MERGE INTO " + dbluTable + "  tgt "
					+ "USING ( "
					+ "    SELECT  C_FACTTABLECOLUMN, "
					+ "            C_TABLENAME, "
					+ "            COLUMN_NAME, "
					+ "            C_FULLPATH, "
					+ "            C_COLUMNNAME, "
					+ "            C_COLUMNDATATYPE, "
					+ "            C_OPERATOR, "
					+ "            C_DIMCODE, "
					+ "            GENERATED_SQL "
					+ "    FROM    " + dbluTable 
					+ "    WHERE   TABLE_INSTANCE_ID = -1 "
					+ ") src "
					+ "ON (   src.COLUMN_NAME       = tgt.COLUMN_NAME "
					+ "   AND tgt.TABLE_INSTANCE_ID = ? "
					+ "   AND tgt.REQUIRED          = 'Y' ) "
					+ "WHEN MATCHED THEN "
					+ "  UPDATE SET "
					+ "        tgt.C_FACTTABLECOLUMN = src.C_FACTTABLECOLUMN, "
					+ "        tgt.C_TABLENAME       = src.C_TABLENAME, "
					+ "        tgt.C_FULLPATH        = src.C_FULLPATH, "
					+ "        tgt.C_COLUMNNAME      = src.C_COLUMNNAME, "
					+ "        tgt.C_COLUMNDATATYPE  = src.C_COLUMNDATATYPE, "
					+ "        tgt.C_OPERATOR        = src.C_OPERATOR, "
					+ "        tgt.C_DIMCODE         = src.C_DIMCODE, "
					+ "        tgt.GENERATED_SQL     = src.GENERATED_SQL ";
		else
			sql = "  update " + dbluTable 
			+ "  set  "
			+ "       C_FACTTABLECOLUMN = rpdo.C_FACTTABLECOLUMN "
			+ "      ,C_TABLENAME = rpdo.C_TABLENAME "
			+ "      ,C_FULLPATH = rpdo.C_FULLPATH "
			+ "      ,C_COLUMNNAME = rpdo.C_COLUMNNAME "
			+ "      ,C_COLUMNDATATYPE = rpdo.C_COLUMNDATATYPE "
			+ "      ,C_OPERATOR = rpdo.C_OPERATOR "
			+ "      ,C_DIMCODE = rpdo.C_DIMCODE "
			+ "	   ,GENERATED_SQL = rpdo.GENERATED_SQL "
			+ "from ( "
			+ " SELECT C_FACTTABLECOLUMN "
			+ "      ,C_TABLENAME "
			+ "      ,COLUMN_NAME "
			+ "      ,C_FULLPATH "
			+ "      ,C_COLUMNNAME "
			+ "      ,C_COLUMNDATATYPE "
			+ "      ,C_OPERATOR "
			+ "      ,C_DIMCODE "
			+ "	  ,GENERATED_SQL "
			+ "    FROM " + dbluTable 
			+ "	where  table_instance_id = -1) AS rpdo "
			+ "where  "
			+ "rpdo.COLUMN_NAME = RPDO_TABLE_REQUEST.COLUMN_NAME "
			+ "and RPDO_TABLE_REQUEST.TABLE_INSTANCE_ID = ? "
			+ "and RPDO_TABLE_REQUEST.REQUIRED = 'Y' ";

		log.info(sql);
		try {
		numRowsAdded = jt.update(sql, 
				naxtTableInstanceID);

		
		//Remove existing
			sql = "DELETE FROM  " + breakdownTable +
					" WHERE NAME = ?";

			jt.update(sql, 
					"RPDO_" + naxtTableInstanceID);
			//(rpdoType.isShared() == true?"@":userId));

			//Remove existing
			sql = "DELETE FROM  " + resultTypeTable +
					" WHERE NAME = ? and CLASSNAME = 'edu.harvard.i2b2.crc.dao.setfinder.QueryResultUserCreated' ";

			jt.update(sql, 
					"RPDO_" + naxtTableInstanceID);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		//Insert

		if (!userId.equals("@")) {
			sql = "INSERT INTO " + breakdownTable +
					"(NAME, VALUE, CREATE_DATE, UPDATE_DATE, USER_ID, GROUP_ID) VALUES (?,?,?,?,?,?) ";

			String value = "RPDO()";
			if (serverType.equalsIgnoreCase(DAOFactoryHelper.SQLSERVER))
			{
				value = "EXEC " + dataSchema + ".usp_rpdo2 @TABLE_INSTANCE_ID=" + naxtTableInstanceID
						+ ", @RESULT_INSTANCE_ID={{{RESULT_INSTANCE_ID}}}" 
						+ ", @MIN_ROW=0"
						+ ", @MAX_ROW=10000";

			} else if (serverType.equalsIgnoreCase(DAOFactoryHelper.ORACLE))
			{
				value = "{ call " + dataSchema + ".usp_rpdo2 ('" + naxtTableInstanceID
						+ "','{{{RESULT_INSTANCE_ID}}}" 
						+ "','0"
						+ "','10000',?)}";

			} else if (serverType.equalsIgnoreCase(DAOFactoryHelper.POSTGRESQL))
			{
				value = "SELECT " + dataSchema + ".usp_rpdo2 ('" + naxtTableInstanceID
						+ "','{{{RESULT_INSTANCE_ID}}}" 
						+ "','0"
						+ "','10000')";

			}


			jt.update(sql, 
					"RPDO_" + naxtTableInstanceID,
					value,
					now,
					now,
					(rpdoType.isShared() == true?"@":userId),
					projectPath);


			sql = "SELECT MAX(RESULT_TYPE_ID) from " + resultTypeTable;
			int resultTypeId = jt.queryForObject(sql, Integer.class) + 1;

			String VisualAttr = "LU";
			if (rpdoType.isShared())
				VisualAttr = "LP";

			//Insert
			sql = "INSERT INTO " + resultTypeTable +
					"(RESULT_TYPE_ID, NAME, DESCRIPTION, DISPLAY_TYPE_ID, VISUAL_ATTRIBUTE_TYPE_ID, USER_ROLE_CD, CLASSNAME) VALUES (?,?,?,?,?,?,?) ";
			jt.update(sql, 
					resultTypeId,
					"RPDO_" + naxtTableInstanceID,
					rpdoType.getTitle(),
					"CATNUM",
					VisualAttr,
					null,
					"edu.harvard.i2b2.crc.dao.setfinder.QueryResultUserCreated");
		}
		//}
		return naxtTableInstanceID;
	}


	public int getRPDORename(Integer id, String title) {
		int numRowsAdded = 0;

		String sql = "UPDATE " + dbluTable +
				" SET  TABLE_INSTANCE_NAME = ? WHERE TABLE_INSTANCE_ID = ? and GROUP_ID = ?";		
		numRowsAdded = jt.update(sql, 
				title,
				id,
				projectPath
				);

		if (numRowsAdded > 0) {
			sql = "UPDATE " + resultTypeTable +
					" SET  DESCRIPTION = ? WHERE NAME = ?";		
			numRowsAdded = jt.update(sql, 
					title,
					"RPDO_" + id
					);
		}


		return numRowsAdded;
	}

	public int getRPDODelete(Integer id) {
		int numRowsAdded = 0;
		String sql = null;

		if (isManager || isAdmin)
		{
			sql = "DELETE FROM " + dbluTable +
					" WHERE TABLE_INSTANCE_ID = ?";		
			numRowsAdded = jt.update(sql, 
					id
					);

		} else {
			sql = "DELETE FROM " + dbluTable +
					" WHERE TABLE_INSTANCE_ID = ? and GROUP_ID = ? and USER_ID != '@'";		
			numRowsAdded = jt.update(sql, 
					id,
					projectPath
					);
		}

		if (numRowsAdded > 0) {
			sql = "DELETE FROM  " + breakdownTable +
					" WHERE NAME = ?";

			jt.update(sql, 
					"RPDO_" + id);

			//Remove existing
			sql = "DELETE FROM  " + resultTypeTable +
					" WHERE NAME = ? and CLASSNAME = 'edu.harvard.i2b2.crc.dao.setfinder.QueryResultUserCreated' ";

			jt.update(sql, 
					"RPDO_" + id);
		}

		return numRowsAdded;
	}

}



class getMapperRPDOs implements RowMapper<RpdoType> {
	@Override
	public RpdoType mapRow(ResultSet rs, int rowNum) throws SQLException {
		RpdoType dblu = new RpdoType();
		//dblu.setDomainId(rs.getString("c_domain_id"));
		//String sql = "select distinct table_instance_id, table_instance_name, max(set_index) as set_index, user_id, create_date "

		dblu.setId(rs.getInt("table_instance_id"));
		dblu.setTitle(rs.getString("table_instance_name"));
		dblu.setCreatorId(rs.getString("user_id"));
		dblu.setColumnCount((rs.getInt("set_index")+1) + "");
		dblu.setCreateDate(rs.getTimestamp("create_date"));
		if ((rs.getTimestamp("update_date") != null))
			dblu.setUpdateDate(rs.getTimestamp("update_date"));
		if ((rs.getString("shared") != null && rs.getString("shared").equals("Y")))
			dblu.setShared(true);
		else
			dblu.setShared(false);
		/*
		if (rs.getString("user_id").equals("@"))
			dblu.setShared(true);
		else
			dblu.setShared(false);
		 */

		return dblu;
	}
}

class getMapperRPDO implements RowMapper<RpdoTable> {
	@Override
	public RpdoTable mapRow(ResultSet rs, int rowNum) throws SQLException {
		RpdoTable dblu = new RpdoTable();
		//dblu.setDomainId(rs.getString("c_domain_id"));
		dblu.setId(rs.getInt("table_instance_id"));
		dblu.setTitle(rs.getString("table_instance_name"));
		dblu.setColumnName(rs.getString("column_name"));
		dblu.setCreatorId(rs.getString("user_id"));
		//	dblu.setGroupId(rs.getString("group_id"));
		dblu.setColumnCount(rs.getString("set_index"));
		dblu.setConcept(rs.getString("json_data"));
		if (dblu.getId() == -1 ||  (rs.getString("required") != null && rs.getString("required").equals("Y")))
			dblu.setRequired(true);
		else
			dblu.setRequired(false);

		if ((rs.getString("delete_flag") != null && rs.getString("delete_flag").equals("Y")))
			dblu.setVisible(false);
		else
			dblu.setVisible(true);

		if ((rs.getString("shared") != null && rs.getString("shared").equals("Y")))
			dblu.setShared(true);
		else
			dblu.setShared(false);
		dblu.setCreateDate(rs.getTimestamp("create_date"));
		dblu.setUpdateDate(rs.getTimestamp("update_date"));
		return dblu;
	}
}


