package edu.harvard.i2b2.crc.dao.role;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.crc.dao.CRCDAO;
import edu.harvard.i2b2.crc.datavo.db.DataSourceLookup;
import edu.harvard.i2b2.crc.datavo.db.QtPriviledge;
 
public class PriviledgeSpringDao extends CRCDAO implements IPriviledgeDao {

	JdbcTemplate jdbcTemplate = null;

	QtPriviledgeRowMapper priviledgeMapper = new QtPriviledgeRowMapper();

	private DataSourceLookup dataSourceLookup = null;

	public PriviledgeSpringDao(DataSource dataSource,
			DataSourceLookup dataSourceLookup) {
		setDataSource(dataSource);
		setDbSchemaName(dataSourceLookup.getFullSchema());
		jdbcTemplate = new JdbcTemplate(dataSource);
		this.dataSourceLookup = dataSourceLookup;

	}

	public QtPriviledge getPriviledgeByProtectionLabel(String protectionLabel)
			throws I2B2DAOException {
		String lookupSql = "select * from "
				+ getDbSchemaName()
				+ "qt_privilege where protection_label_cd = ? and plugin_id is NULL";
		QtPriviledge priviledgeRow = (QtPriviledge) jdbcTemplate
				.queryForObject(lookupSql, new Object[] { protectionLabel },
						priviledgeMapper);
		return priviledgeRow;

	}

	public List<QtPriviledge> getPriviledgeByPluginId(String pluginId)
			throws I2B2DAOException {
		String lookupSql = "select * from "
				+ getDbSchemaName()
				+ "qt_privilege where plugin_id = ? and protection_label_cd is NULL";
		List<QtPriviledge> priviledgeRow = jdbcTemplate.query(lookupSql,
				new Object[] { pluginId }, priviledgeMapper);
		return priviledgeRow;
	}

	private static class QtPriviledgeRowMapper implements RowMapper {

		public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
			QtPriviledge priviledge = new QtPriviledge();
			priviledge
					.setProtectionLabelCd(rs.getString("PROTECTION_LABEL_CD"));
			priviledge.setDataProtCd(rs.getString("DATAPROT_CD"));
			priviledge.setHivemgmtCd(rs.getString("HIVEMGMT_CD"));
			priviledge.setPluginId(rs.getString("PLUGIN_ID"));
			return priviledge;
		}
	}

}
