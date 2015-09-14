package edu.harvard.i2b2.crc.dao.setfinder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.crc.dao.CRCDAO;
import edu.harvard.i2b2.crc.datavo.db.DataSourceLookup;
import edu.harvard.i2b2.crc.datavo.db.QtAnalysisPlugin;

public class AnalysisPluginSpringDao extends CRCDAO implements
		IAnalysisPluginDao {

	JdbcTemplate jdbcTemplate = null;

	QtAnalysisPluginRowMapper analysisPluginMapper = new QtAnalysisPluginRowMapper();

	private DataSourceLookup dataSourceLookup = null;

	public AnalysisPluginSpringDao(DataSource dataSource,
			DataSourceLookup dataSourceLookup) {
		setDataSource(dataSource);
		setDbSchemaName(dataSourceLookup.getFullSchema());
		jdbcTemplate = new JdbcTemplate(dataSource);
		this.dataSourceLookup = dataSourceLookup;

	}

	/**
	 * Return plugin metadata
	 * 
	 * @param analysisName
	 * @param projectId
	 * @return
	 */
	public List<QtAnalysisPlugin> getAnalysisPluginMetadata(
			String analysisName, String projectId) {
		List<QtAnalysisPlugin> analysisPluginList = null;

		// if the analysis name is ALL, then return all the plugin
		if (analysisName != null && analysisName.equalsIgnoreCase("ALL")) {
			String lookupSql = "select * from " + getDbSchemaName()
					+ "qt_analysis_plugin where group_id = ? or group_id = '@'";
			analysisPluginList = jdbcTemplate.query(lookupSql,
					new Object[] { projectId }, analysisPluginMapper);
		} else {
			// if not get analysisname and the project = projectId or
			// project='@'
			String lookupSql = "select * from "
					+ getDbSchemaName()
					+ "qt_analysis_plugin where plugin_name = ? and (group_id = ? or group_id = '@')";
			analysisPluginList = jdbcTemplate.query(lookupSql, new Object[] {
					analysisName, projectId }, analysisPluginMapper);
		}
		return analysisPluginList;
	}

	public QtAnalysisPlugin lookupAnalysisPluginByNameVersionProject(
			String analysisName, String version, String projectId)
			throws I2B2DAOException {
		List<QtAnalysisPlugin> analysisPluginList = null;
		boolean getMaxVersionFlag = false;
		String maxVersionCd = null, errorMsg = "";
		QtAnalysisPlugin analysisPlugin = null;

		if (version == null) {
			getMaxVersionFlag = true;
		}
		analysisPluginList = getAnalysisPluginByNameAndProject(analysisName,
				projectId);
		// if the list is empty, then try with group_id = @s
		if (analysisPluginList.size() < 1) {
			projectId = "@";
			analysisPluginList = getAnalysisPluginByNameAndProject(
					analysisName, projectId);
		}
		if (analysisPluginList.size() > 0) {
			if (getMaxVersionFlag) {
				maxVersionCd = getMaxAnalysisPluginVersion(analysisName,
						projectId);
				version = maxVersionCd;
			}
			if (version == null) {
				analysisPluginList = getAnalysisPluginByNameAndProject(
						analysisName, projectId);
			} else {
				analysisPluginList = getAnalysisPluginByNameVersionProject(
						analysisName, version, projectId);
			}
		}
		if (analysisPluginList.size() > 0) {
			analysisPlugin = analysisPluginList.get(0);
		} else {
			errorMsg = "Could not match plugin by name[" + analysisName
					+ "] + version [" + version + "] and group [" + projectId
					+ "]";
			throw new I2B2DAOException(errorMsg);
		}

		return analysisPlugin;

	}

	private List<QtAnalysisPlugin> getAnalysisPluginByNameAndProject(
			String analysisName, String projectId) {
		String lookupSql = "select * from " + getDbSchemaName()
				+ "qt_analysis_plugin where plugin_name = ? and  group_id = ?";
		List<QtAnalysisPlugin> analysisPluginList = jdbcTemplate.query(
				lookupSql, new Object[] { analysisName, projectId },
				analysisPluginMapper);
		return analysisPluginList;
	}

	private String getMaxAnalysisPluginVersion(String analysisName,
			String projectId) {
		String lookupSql = "select max(version_cd) as maxversion from "
				+ getDbSchemaName()
				+ "qt_analysis_plugin where plugin_name = ? and  group_id = ?";
		String maxVersion = (String) jdbcTemplate.queryForObject(lookupSql,
				new Object[] { analysisName, projectId }, String.class);
		return maxVersion;
	}

	private List<QtAnalysisPlugin> getAnalysisPluginByNameVersionProject(
			String analysisName, String version, String projectId) {
		String lookupSql = "select * from "
				+ getDbSchemaName()
				+ "qt_analysis_plugin where plugin_name = ? and version_cd = ? and  group_id = ?";
		List<QtAnalysisPlugin> analysisPluginList = jdbcTemplate.query(
				lookupSql, new Object[] { analysisName, version, projectId },
				analysisPluginMapper);
		return analysisPluginList;
	}

	public QtAnalysisPlugin getAnalysisPluginById(int analysisId)
			throws I2B2DAOException {
		String lookupSql = "select * from " + getDbSchemaName()
				+ "qt_analysis_plugin where plugin_id = ?";
		QtAnalysisPlugin analysisPlugin = (QtAnalysisPlugin) jdbcTemplate
				.queryForObject(lookupSql, new Object[] { analysisId },
						analysisPluginMapper);
		return analysisPlugin;
	}

	private static class QtAnalysisPluginRowMapper implements RowMapper {

		public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
			QtAnalysisPlugin analysisPlugin = new QtAnalysisPlugin();
			analysisPlugin.setPluginId(rs.getString("PLUGIN_ID"));
			analysisPlugin.setPluginName(rs.getString("PLUGIN_NAME"));
			analysisPlugin.setDescription(rs.getString("DESCRIPTION"));
			analysisPlugin.setVersion(rs.getString("VERSION_CD"));
			analysisPlugin.setCommandOptionCd(rs.getString("COMMANDOPTION_CD"));
			analysisPlugin.setParameterInfo(rs.getString("PARAMETER_INFO"));
			analysisPlugin.setParameterInfoXsd(rs
					.getString("PARAMETER_INFO_XSD"));
			analysisPlugin.setCommandLine(rs.getString("COMMAND_LINE"));
			analysisPlugin.setWorkingFolder(rs.getString("WORKING_FOLDER"));
			analysisPlugin.setPluginIcon(rs.getString("PLUGIN_ICON"));
			analysisPlugin.setStatusId(rs.getString("STATUS_CD"));
			analysisPlugin.setUserId(rs.getString("USER_ID"));
			analysisPlugin.setGroupId(rs.getString("GROUP_ID"));
			analysisPlugin.setCreateDate(rs.getTimestamp("CREATE_DATE"));
			analysisPlugin.setUpdateDate(rs.getTimestamp("UPDATE_DATE"));
			return analysisPlugin;
		}
	}

}
