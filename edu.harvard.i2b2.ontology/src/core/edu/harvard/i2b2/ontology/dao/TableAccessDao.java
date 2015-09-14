package edu.harvard.i2b2.ontology.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.ontology.datavo.pm.ProjectType;
import edu.harvard.i2b2.ontology.ejb.DBInfoType;
import edu.harvard.i2b2.ontology.ejb.TableAccessType;
import edu.harvard.i2b2.ontology.util.OntologyUtil;

/**
 * Class to access table_access table.
 * 
 * @author rkuttan
 * 
 */
public class TableAccessDao extends JdbcDaoSupport {

	private static Log log = LogFactory.getLog(TableAccessDao.class);

	private SimpleJdbcTemplate jt = null;

	private void setDataSource(String dataSource) {
		DataSource ds = null;
		try {
			ds = OntologyUtil.getInstance().getDataSource(dataSource);
		} catch (I2B2Exception e2) {
			log.error(e2.getMessage());
			;
		}
		this.jt = new SimpleJdbcTemplate(ds);
	}

	public void setDataSourceObject(DataSource dataSource) {
		this.jt = new SimpleJdbcTemplate(dataSource);
	}

	public List<String> getEditorTableName(ProjectType projectInfo,
			DBInfoType dbInfo, boolean synchronizeAllFlag) throws I2B2Exception {
		String metadataSchema = dbInfo.getDb_fullSchema();
		if (jt == null) {
			setDataSource(dbInfo.getDb_dataSource());
		}
		boolean protectedAccess = isProtectedAccess(projectInfo);
		String sql = "select distinct(c_table_name) from " + metadataSchema
				+ "table_access ";
		if (synchronizeAllFlag == false) {
			sql += " where c_visualattributes like '%E' ";
			if(!protectedAccess)
				sql += " and c_protected_access = ? ";
		}
		else if (!protectedAccess) {
			sql += " where c_protected_access = ? ";
		}
		ParameterizedRowMapper<String> map = new ParameterizedRowMapper<String>() {
			public String mapRow(ResultSet rs, int rowNum) throws SQLException {
				String name = (rs.getString("c_table_name"));
				return name;
			}
		};

		List<String> tableNameList = null;
		log.debug("Executing sql [" + sql + "]");
		if (!protectedAccess) {
			tableNameList = jt.query(sql, map, "N");
		} else {
			tableNameList = jt.query(sql, map);
		}
		return tableNameList;
	}

	public List<String> getAllTableName(ProjectType projectInfo,
			DBInfoType dbInfo) throws I2B2Exception {
		String metadataSchema = dbInfo.getDb_fullSchema();
		if (jt == null) {
			setDataSource(dbInfo.getDb_dataSource());
		}
		boolean protectedAccess = isProtectedAccess(projectInfo);
		String sql = "select distinct(c_table_name) from " + metadataSchema
				+ "table_access ";
		if (!protectedAccess) {
			sql += " where c_protected_access = ? ";
		}
		ParameterizedRowMapper<String> map = new ParameterizedRowMapper<String>() {
			public String mapRow(ResultSet rs, int rowNum) throws SQLException {
				String name = (rs.getString("c_table_name"));
				return name;
			}
		};

		List<String> tableNameList = null;
		log.debug("Executing sql [" + sql + "]");
		if (!protectedAccess) {
			tableNameList = jt.query(sql, map, "N");
		} else {
			tableNameList = jt.query(sql, map);
		}
		return tableNameList;
	}

	
	public List<TableAccessType> getAllTableAccess(ProjectType projectInfo,
			DBInfoType dbInfo) throws I2B2Exception {
		String metadataSchema = dbInfo.getDb_fullSchema();
		if (jt == null) {
			setDataSource(dbInfo.getDb_dataSource());
		}
		boolean protectedAccess = isProtectedAccess(projectInfo);
		String sql = "select * from " + metadataSchema
				+ "table_access ";
		if (!protectedAccess) {
			sql += " where c_protected_access = ? ";
		}
		ParameterizedRowMapper<TableAccessType> map = new ParameterizedRowMapper<TableAccessType>() {
			
			public TableAccessType mapRow(ResultSet rs, int rowNum) throws SQLException {
				TableAccessType tableAccessType = new TableAccessType();
				tableAccessType.setTableName(rs.getString("c_table_name"));
				tableAccessType.setTableCd(rs.getString("c_table_cd"));
				tableAccessType.setFullName(rs.getString("c_fullname"));
				tableAccessType.setSynonymCd(rs.getString("c_synonym_cd"));
				tableAccessType.setVisualAttributes(rs.getString("c_visualattributes"));
				tableAccessType.setDimCode(rs.getString("c_dimcode"));
				return tableAccessType;
			}
		};

		List<TableAccessType> tableAccessList = null;
		log.debug("Executing sql [" + sql + "]");
		if (!protectedAccess) {
			tableAccessList = jt.query(sql, map, "N");
		} else {
			tableAccessList = jt.query(sql, map);
		}
		return tableAccessList;
	}
	private boolean isProtectedAccess(ProjectType projectInfo)
			throws I2B2Exception {
		boolean protectedAccess = false;
		if (projectInfo.getRole().size() == 0) {
			log.error("no role found for this user in project: "
					+ projectInfo.getName());
			I2B2Exception e = new I2B2Exception("No role found for user");
			throw e;
		}

		Iterator it = projectInfo.getRole().iterator();
		while (it.hasNext()) {
			String role = (String) it.next();
			if (role.toUpperCase().equals("DATA_PROT")) {
				protectedAccess = true;
				break;
			}
		}
		return protectedAccess;
	}
}
