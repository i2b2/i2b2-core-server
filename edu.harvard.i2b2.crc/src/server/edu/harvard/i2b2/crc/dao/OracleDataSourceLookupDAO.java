package edu.harvard.i2b2.crc.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.RowMapper;

import edu.harvard.i2b2.crc.datavo.db.DataSourceLookup;

public class OracleDataSourceLookupDAO extends DataSourceLookupDAO {

	/** log **/
	protected final Log log = LogFactory
			.getLog(OracleDataSourceLookupDAO.class);

	private String schemaName = null;

	public OracleDataSourceLookupDAO(DataSource dataSource, String schemaName) {
		setDataSource(dataSource);
		if (schemaName != null && schemaName.endsWith(".")) {
			this.schemaName = schemaName;
		} else {
			this.schemaName = schemaName + ".";
		}
	}

	public List<DataSourceLookup> getDbLookupByHive(String domainId) {
		String sql = "select * from "
				+ schemaName
				+ "crc_db_lookup where LOWER(c_domain_id) like ? ";
		log.debug("Executing SQL [" + sql + "]");
		List<DataSourceLookup> dataSourceLookupList = this.query(sql,
				new Object[] { domainId.toLowerCase() }, new mapper());
		return dataSourceLookupList;
	}

	
	public List<DataSourceLookup> getDbLookupByHiveOwner(String domainId,
			String ownerId) {
		String sql = "select * from "
				+ schemaName
				+ "crc_db_lookup where LOWER(c_domain_id) = ? and c_project_path = ? and (LOWER(c_owner_id) = ? or c_owner_id ='@') order by c_project_path";
		String projectId = "@";
		log.debug("Executing SQL [" + sql + "]");
		List<DataSourceLookup> dataSourceLookupList = this.query(sql,
				new Object[] { domainId.toLowerCase(), projectId, ownerId.toLowerCase() }, new mapper());
		return dataSourceLookupList;
	}

	@SuppressWarnings("unchecked")
	public List<DataSourceLookup> getDbLookupByHiveProjectOwner(
			String domainId, String projectId, String ownerId) {
		String sql = "select * from "
				+ schemaName
				+ "crc_db_lookup where LOWER(c_domain_id) = ? and c_project_path like  ? and (LOWER(c_owner_id) =? or c_owner_id = '@') order by c_project_path";
		List<DataSourceLookup> dataSourceLookupList = this.query(sql,
				new Object[] { domainId.toLowerCase(), projectId + "%", ownerId.toLowerCase() }, new int[] {
						Types.VARCHAR, Types.VARCHAR, Types.VARCHAR },
				new mapper());
		return dataSourceLookupList;
	}

	public static void main(String args[]) {
		OracleDataSourceLookupDAO dao = new OracleDataSourceLookupDAO(null,
				null);

	}

	public class mapper implements RowMapper {

		public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
			DataSourceLookup dataSourceLookup = new DataSourceLookup();
			dataSourceLookup.setDomainId(rs.getString("c_domain_id"));
			dataSourceLookup.setProjectPath(rs.getString("c_project_path"));
			dataSourceLookup.setOwnerId(rs.getString("c_owner_id"));
			dataSourceLookup.setFullSchema(rs.getString("c_db_fullschema"));
			dataSourceLookup.setDataSource(rs.getString("c_db_datasource"));
			dataSourceLookup.setServerType(rs.getString("c_db_servertype"));
			dataSourceLookup.setNiceName(rs.getString("c_db_nicename"));
			dataSourceLookup.setToolTip(rs.getString("c_db_tooltip"));
			dataSourceLookup.setComment(rs.getString("c_comment"));
			dataSourceLookup.setEntryDate(rs.getDate("c_entry_date"));
			dataSourceLookup.setChangeDate(rs.getDate("c_change_date"));
			dataSourceLookup.setStatusCd(rs.getString("c_status_cd"));
			return dataSourceLookup;
		}
	}
}
