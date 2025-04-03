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
import edu.harvard.i2b2.crc.datavo.pdo.query.ConceptTableType;
import edu.harvard.i2b2.crc.datavo.pdo.query.DblookupType;
import edu.harvard.i2b2.crc.datavo.pdo.query.DeleteDblookupType;
import edu.harvard.i2b2.crc.datavo.pdo.query.RpdoType;
import edu.harvard.i2b2.crc.datavo.pdo.query.RpdosType;
import edu.harvard.i2b2.crc.datavo.pdo.query.SetDblookupType;
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
	private static String keyOrder = " LOWER(group_id)=LOWER(?) "; //AND (LOWER(user_id)=LOWER(?) OR user_id='@') "; //ORDER BY TABLE_INSTANCE_NAME ";
	private String domainId = null;
	private String userId = null;
	private String projectPath = null;
	private String dataSchema = "";
	private String serverType = "";
	
	public RPDODao() {		
		initDblookupDao();
	} 

	public RPDODao(MessageHeaderType reqMsgHdr) throws I2B2Exception, JAXBUtilException {
		domainId = reqMsgHdr.getSecurity().getDomain();
		userId = reqMsgHdr.getSecurity().getUsername();
		projectPath = reqMsgHdr.getProjectId();
		initDblookupDao();
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
		String sql = "select distinct table_instance_id, table_instance_name, max(set_index) as set_index, user_id, create_date "
				+ "  FROM " +  dbluTable 
				+ " WHERE delete_flag = 'N' and " + keyOrder
				+ "  group by  table_instance_id, table_instance_name, user_id, create_date";
		List<RpdoType> queryResult = null;
		try {
			queryResult = jt.query(sql, new getMapperRPDOs(), projectPath);
		} catch (DataAccessException e) {
			log.error(e.getMessage());
			throw e;
		}
		log.info("result size = " + queryResult.size());		
		return queryResult;
	}

	public List<RpdoTable> getRPDO( int tableID) throws DataAccessException, I2B2Exception {
		String sql = "SELECT table_instance_id, table_instance_name, column_name, user_id, set_index, json_data, required, create_date, update_date FROM " +  dbluTable + " WHERE " + getTable;		
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

	public List<RpdoTable> getDblookup(String column, String value) throws DataAccessException, I2B2Exception {
		String sql = "SELECT * FROM " +  dbluTable + " WHERE ";	
		String v = value, s = column.toLowerCase();
		List<RpdoTable> queryResult = null;
		try {
			if (s.equalsIgnoreCase("domain_id")) {
				sql += keyOrder;
				queryResult = jt.query(sql, new getMapperRPDO(), value, userId);
			} else if (s.equalsIgnoreCase("user_id")) {
				sql += keyOrder;
				queryResult = jt.query(sql, new getMapperRPDO(), domainId, value);
			} else {
				sql += "c_" + column + "=? AND " + keyOrder;
				if (s.equalsIgnoreCase("group_id")) {
					v = slashSandwich(value);
				} else {
				}
				queryResult = jt.query(sql, new getMapperRPDO(), v, domainId, userId);
			}
		} catch (DataAccessException e) {
			log.error(e.getMessage());
			e.printStackTrace();
			throw new I2B2DAOException("Database error");
		}
		return queryResult;	
	}

	public int setRPDO(final RpdoType rpdoType) throws DataAccessException, I2B2Exception {
		int naxtTableInstanceID = 0;
		int numRowsAdded = 0;
		String sql = "SELECT MAX(TABLE_INSTANCE_ID) from " + dbluTable;
		naxtTableInstanceID = jt.queryForObject(sql, Integer.class) + 1;

		java.sql.Timestamp now = new java.sql.Timestamp(System.currentTimeMillis());
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
				} catch (Exception e) {}

				if (rpdoType.isVisible() == null) 
					rpdoType.setVisible(false);
				if (rpdoType.isShared() == null) 
					rpdoType.setShared(false);
				

				sql = "INSERT INTO " + dbluTable +
						"(TABLE_INSTANCE_ID, TABLE_INSTANCE_NAME, COLUMN_NAME, C_FACTTABLECOLUMN, C_FULLPATH, AGG_TYPE,C_COLUMNNAME,C_TABLENAME,C_OPERATOR,C_DIMCODE,USER_ID ,GROUP_ID ,SET_INDEX,JSON_DATA,CREATE_DATE,UPDATE_DATE,DELETE_FLAG,USE_AS_COHORT,REQUIRED) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,'N',?)";		
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
						(rpdoType.isShared() == true?"@":userId),
						projectPath,
						i,
						json,
						now,
						now,
						(rpdoType.isVisible() == true?"Y":"N"),
						(rpdoType.getConcept().get(i).isRequired()== true?"Y":"N")
						);

				log.info("setRPDO - Number of rows added: " + numRowsAdded);
			}
		}

		// Always save regardless of isVisible is set
		//if (rpdoType.isVisible())
		//{
			//Remove existing
		try {
			sql = "DELETE FROM  " + breakdownTable +
					" WHERE NAME = ? and USER_ID = ? ";
	
			jt.update(sql, 
					"RPDO_" + naxtTableInstanceID,
					(rpdoType.isShared() == true?"@":userId));
		
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
				value = "EXEC " + dataSchema + ".usp_rpdo2 ('" + naxtTableInstanceID
						+ "','{{{RESULT_INSTANCE_ID}}}" 
						+ "','0"
						+ "','10000')";
				
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


			//Insert
			sql = "INSERT INTO " + resultTypeTable +
					"(RESULT_TYPE_ID, NAME, DESCRIPTION, DISPLAY_TYPE_ID, VISUAL_ATTRIBUTE_TYPE_ID, USER_ROLE_CD, CLASSNAME) VALUES (?,?,?,?,?,?,?) ";
			jt.update(sql, 
					resultTypeId,
					"RPDO_" + naxtTableInstanceID,
					rpdoType.getTitle(),
					"CATNUM",
					"LU",
					null,
					"edu.harvard.i2b2.crc.dao.setfinder.QueryResultUserCreated");

		//}
		return numRowsAdded;
	}


	//rpdoType.getcFacttablecolumn(),rpdoType.getcTablename(),rpdoType.getColumnName(),rpdoType.getcFullpath(), rpdoType.getcColumnname(),rpdoType.getcColumndatatype(), rpdoType.getcOperator(), rpdoType.getcDimcode(), rpdoType.getAggType(), rpdoType.getConstrainByDateTo(), rpdoType.getConstrainByDateFrom(), rpdoType.getConstrainByValueOperator(),
	//																																																															rpdoType.getConstrainByValueConstraint(), rpdoType.getConstrainByValueUnitOfMeasure(), rpdoType.getConstrainByValueType(),

	//rpdoType.getCreateDate(), rpdoType.getUpdateDate()
	//, rpdoType.getDeleteFlag()
	//, rpdoType.getConstrainByIndexdateColumnname(), rpdoType.getConstrainByIndexdateFromDays(), rpdoType.getConstrainByIndexdateToDays(), rpdoType.getUseAsCohort()

	public int getRPDORename(Integer id, String title) {
		int numRowsAdded = 0;

		String sql = "UPDATE " + dbluTable +
				" SET  TABLE_INSTANCE_NAME = ? WHERE TABLE_INSTANCE_ID = ? and GROUP_ID = ?";		
		numRowsAdded = jt.update(sql, 
				title,
				id,
				projectPath
				);
		return numRowsAdded;
	}

	public int getRPDODelete(Integer id) {
		int numRowsAdded = 0;

		
		String sql = "DELETE FROM  " + breakdownTable +
				" WHERE NAME = ?";

		jt.update(sql, 
				"RPDO_" + id);
	
		//Remove existing
		sql = "DELETE FROM  " + resultTypeTable +
				" WHERE NAME = ? and CLASSNAME = 'edu.harvard.i2b2.crc.dao.setfinder.QueryResultUserCreated' ";

		jt.update(sql, 
				"RPDO_" + id);
		
		 sql = "UPDATE " + dbluTable +
				" SET  DELETE_FLAG = 'Y' WHERE TABLE_INSTANCE_ID = ? and GROUP_ID = ?";		
		numRowsAdded = jt.update(sql, 
				id,
				projectPath
				);
		return numRowsAdded;
	}

	/*
	public int setRPDO(final SetRpdoType dblookupType) throws DataAccessException, I2B2Exception {
		List<RpdoType> queryResult = getDblookup(dblookupType);
		if (null == queryResult || (0 == queryResult.size())) {
			return insertRPDO(dblookupType);
		} else {
			return updateDblookup(dblookupType);
		}
	}

	public int insertRPDO(final SetRpdoType rpdoType) throws DataAccessException, I2B2Exception {
		int numRowsAdded = 0;
		String sql = "INSERT INTO " + dbluTable +
				     "(TABLE_INSTANCE_ID ,TABLE_INSTANCE_NAME ,USER_ID ,GROUP_ID ,SET_INDEX ,C_FACTTABLECOLUMN ,C_TABLENAME ,COLUMN_NAME ,C_FULLPATH ,C_COLUMNNAME ,C_COLUMNDATATYPE ,C_OPERATOR ,C_DIMCODE ,AGG_TYPE ,CONSTRAIN_BY_DATE_TO ,CONSTRAIN_BY_DATE_FROM ,CONSTRAIN_BY_VALUE_OPERATOR ,CONSTRAIN_BY_VALUE_CONSTRAINT ,CONSTRAIN_BY_VALUE_UNIT_OF_MEASURE ,CONSTRAIN_BY_VALUE_TYPE ,CREATE_DATE ,DELETE_DATE ,DELETE_FLAG ,GENERATED_SQL ,CONSTRAIN_BY_INDEXDATE_COLUMNNAME ,CONSTRAIN_BY_INDEXDATE_FROM_DAYS ,CONSTRAIN_BY_INDEXDATE_TO_DAYS ,USE_AS_COHORT) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";		
		numRowsAdded = jt.update(sql, 
								 dblookupType.getDomainId(),  
								 slashSandwich(dblookupType.getProjectPath()),
								 dblookupType.getOwnerId(),
								 dblookupType.getDbFullschema(),
								 dblookupType.getDbDatasource(),
								 dblookupType.getDbServertype(),
								 dblookupType.getDbNicename(),
								 dblookupType.getDbTooltip(),
								 dblookupType.getComment(),
								 Calendar.getInstance().getTime(),
								 Calendar.getInstance().getTime(),
								 dblookupType.getStatusCd()
								);
		log.info("insertDblookup - Number of rows added: " + numRowsAdded);
		return numRowsAdded;
	}

	public int updateDblookup(final SetDblookupType dblookupType) throws DataAccessException, I2B2Exception {
		int numRowsSet = 0;
		String sql = "UPDATE " + dbluTable +
					 "SET c_db_fullschema=?, c_db_datasource=?, c_db_servertype=?, c_db_nicename=?, c_db_tooltip=?, c_comment=?, c_change_date=?, c_status_cd=? WHERE c_project_path=? AND " + 
					 key;		
		numRowsSet = jt.update(sql, 
							   dblookupType.getDbFullschema(),
							   dblookupType.getDbDatasource(),
							   dblookupType.getDbServertype(),
							   dblookupType.getDbNicename(),
							   dblookupType.getDbTooltip(),
							   dblookupType.getComment(),
							   Calendar.getInstance().getTime(),
							   dblookupType.getStatusCd(),
							   slashSandwich(dblookupType.getProjectPath()),
							   dblookupType.getDomainId(),  
							   dblookupType.getOwnerId()
							  );
		log.info("updateDblookup - Number of rows updated: " + numRowsSet);
		return numRowsSet;
	}

	public int deleteDblookup(final DeleteDblookupType dblookupType) throws DataAccessException, I2B2Exception {
		int numRowsDeleted = 0;
		String sql = "DELETE FROM " + dbluTable + " WHERE c_project_path=? AND " + key;		
		try {
			numRowsDeleted = jt.update(sql, slashSandwich(dblookupType.getProjectPath()), dblookupType.getDomainId(), dblookupType.getOwnerId());
		} catch (DataAccessException e) {
			log.error(e.getMessage());
			e.printStackTrace();
			throw new I2B2DAOException("Database error");
		}
		return numRowsDeleted;	
	}
	 */
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
		if (rs.getString("user_id").equals("@"))
			dblu.setShared(true);
		else
			dblu.setShared(false);

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
		
			//	dblu.setJsonData(rs.getString("json_data"));
		/*
		dblu.setcFacttablecolumn(rs.getString("c_facttablecolumn"));
		dblu.setcTablename(rs.getString("c_tablename"));
		dblu.setColumnName(rs.getString("column_name"));
		dblu.setcFullpath(rs.getString("c_fullpath"));
		dblu.setcColumnname(rs.getString("c_columnname"));
		dblu.setcColumndatatype(rs.getString("c_columndatatype"));
		dblu.setcOperator(rs.getString("c_operator"));
		dblu.setcDimcode(rs.getString("c_dimcode"));
		dblu.setAggType(rs.getString("agg_type"));
		dblu.setConstrainByDateTo(rs.getTimestamp("constrain_by_date_to"));
		dblu.setConstrainByDateFrom(rs.getTimestamp("constrain_by_date_from"));
		dblu.setConstrainByValueOperator(rs.getString("constrain_by_value_operator"));
		dblu.setConstrainByValueConstraint(rs.getString("constrain_by_value_constraint"));
		dblu.setConstrainByValueUnitOfMeasure(rs.getString("constrain_by_value_unit_of_measure"));
		dblu.setConstrainByValueType(rs.getString("constrain_by_value_type"));
		 */
		if (rs.getString("user_id").equals("@"))
			dblu.setShared(true);
		else
			dblu.setShared(false);
		//dblu.setShared(true);
		dblu.setCreateDate(rs.getTimestamp("create_date"));
		dblu.setUpdateDate(rs.getTimestamp("update_date"));
		//		dblu.setDeleteFlag(rs.getString("delete_flag"));
		//	dblu.setGeneratedSql(rs.getString("generated_sql"));
		//		dblu.setConstrainByIndexdateColumnname(rs.getString("constrain_by_indexdate_columnname"));
		//		dblu.setConstrainByIndexdateFromDays(rs.getInt("constrain_by_indexdate_from_days"));
		//		dblu.setConstrainByIndexdateToDays(rs.getInt("constrain_by_indexdate_to_days"));
		//		dblu.setUseAsCohort(rs.getString("use_as_cohort"));
		return dblu;
	}
}


