package edu.harvard.i2b2.crc.dao.pdo;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.JdbcUtils;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.util.db.JDBCUtil;
import edu.harvard.i2b2.crc.dao.CRCDAO;
import edu.harvard.i2b2.crc.dao.DAOFactoryHelper;
import edu.harvard.i2b2.crc.datavo.db.DataSourceLookup;

public class MetadataDao extends CRCDAO implements IMetadataDao {

	/** log * */
	protected final Log log = LogFactory.getLog(getClass());
	
	

	private ArrayList<String> PATIENT_DIMENSION_REQUIRED_FIELDS = new ArrayList<String>();
	private ArrayList<String> VISIT_DIMENSION_REQUIRED_FIELDS = new ArrayList<String>();
	private ArrayList<String> VISIT_DIMENSION_REQUIRED_DETAIL_FIELDS = new ArrayList<String>();
	private ArrayList<String> PATIENT_DIMENSION_REQUIRED_DETAIL_FIELDS = new ArrayList<String>();

	private String[] patient_dim_required_field = new String[] {
			"patient_num", "vital_status_cd", "birth_date", "death_date", "patient_blob", "update_date",
			"download_date", "import_date", "sourcesystem_cd", "upload_id" };
	
	private String[] patient_dim_detail_field = new String[] { 
			"vital_status_cd","birth_date"};
	
	private String[] visit_dim_detail_field = new String[] {"active_status_cd"};
	
	private String[] visit_dim_required_field = new String[] { "encounter_num",
			"patient_num", "start_date", "end_date", "active_status_cd", "visit_blob",
			"update_date", "download_date", "import_date", "sourcesystem_cd",
			"upload_id" };
	
	public final static String CRC_COLUMN_DESCRIPTOR = "crc_column_descriptor";
	
	
	

	private DataSourceLookup dataSourceLookup = null;

	public MetadataDao(DataSourceLookup dataSourceLookup, DataSource dataSource) {
		this.dataSourceLookup = dataSourceLookup;
		setDataSource(dataSource);
		setDbSchemaName(dataSourceLookup.getFullSchema());
		PATIENT_DIMENSION_REQUIRED_FIELDS.addAll(Arrays
				.asList(patient_dim_required_field));
		VISIT_DIMENSION_REQUIRED_FIELDS.addAll(Arrays
				.asList(visit_dim_required_field));
		PATIENT_DIMENSION_REQUIRED_DETAIL_FIELDS.addAll(Arrays
				.asList(patient_dim_detail_field));
		VISIT_DIMENSION_REQUIRED_DETAIL_FIELDS.addAll(Arrays
				.asList(visit_dim_detail_field));
	}

	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.harvard.i2b2.crc.dao.pdo.IMetadataDao#blah(java.lang.String)
	 */
	public Map getMetadataForOptionalField(String tableName)
			throws I2B2DAOException {

		ArrayList<String> requiredFieldLookupList = null;
		ArrayList<String> requiredDetailFieldLookupList = null;
		
		if (tableName.equalsIgnoreCase(DimensionTable.PATIENT_DIMENSION)) {
			requiredFieldLookupList = PATIENT_DIMENSION_REQUIRED_FIELDS;
			requiredDetailFieldLookupList = PATIENT_DIMENSION_REQUIRED_DETAIL_FIELDS;
		} else if (tableName.equalsIgnoreCase(DimensionTable.VISIT_DIMENSION)) {
			requiredFieldLookupList = VISIT_DIMENSION_REQUIRED_FIELDS;
			requiredDetailFieldLookupList = VISIT_DIMENSION_REQUIRED_DETAIL_FIELDS;
		}

		Map<String, TableMetaData> columnMap = new HashMap<String, TableMetaData>();
		Connection conn = null;
		Statement stmt = null;
		try {
			conn = getDataSource().getConnection();
			
			Map<String,String> columnDescriptorMap =  getColumnDescription(conn, tableName) ; 
			
			stmt = conn.createStatement();
			// Execute the query
			ResultSet rs = stmt.executeQuery("SELECT * FROM "
					+ getDbSchemaName() + tableName + " WHERE 1 = 2");

			// Get the metadata
			ResultSetMetaData md = rs.getMetaData();

			String columnName = "", columnType = ""; 
			int columnSize = 0;
			// Print the column labels
			for (int i = 1; i <= md.getColumnCount(); i++) {
				TableMetaData tableMetaData = new TableMetaData();

				boolean requiredDetailFieldFlag = false;
				if (requiredDetailFieldLookupList.contains(md.getColumnLabel(i)
						.toLowerCase())) {
					requiredDetailFieldFlag = true;
				}
				if (requiredDetailFieldFlag || (!requiredFieldLookupList.contains(md.getColumnLabel(i)
						.toLowerCase()))) {
					columnName = md.getColumnLabel(i).toLowerCase();
					columnType = md.getColumnTypeName(i).toLowerCase();
					columnSize = md.getColumnDisplaySize(i);
					
					log.debug("column name "  
							+ columnName);
					log.debug(" column type name"
							+ columnType );
					log.debug(" column display type size "
							+ columnSize);
					tableMetaData.column_name = columnName;
					tableMetaData.column_type = columnType;
					tableMetaData.column_comment = columnName;
					tableMetaData.column_type_size = String.valueOf(columnSize);
					tableMetaData.column_comment = columnDescriptorMap.get(columnName);
					columnMap.put(columnName, tableMetaData);
				}
			}
		} catch (SQLException sqlEx) {
			sqlEx.printStackTrace();
			log.error("", sqlEx);
			throw new I2B2DAOException("", sqlEx);
		} finally {
			
			try {
				JDBCUtil.closeJdbcResource(null, stmt, conn);
			} catch (SQLException sqlEx) {
				sqlEx.printStackTrace();
			}
		}
		System.out.println();
		return columnMap;
	}
	
	private Map<String,String> getColumnDescription(Connection conn, String tableName) throws SQLException {
		Map<String,String> columnDescriptorMap = new HashMap<String,String>();
		PreparedStatement pStmt = conn.prepareStatement("select * from " +  getDbSchemaName() + "code_lookup where lower(table_cd) = ?   and lower(code_cd) = ?");
		pStmt.setString(1, tableName.toLowerCase());
		pStmt.setString(2, CRC_COLUMN_DESCRIPTOR);
		ResultSet resultSet = pStmt.executeQuery();
		String columnCd = null, columnDescriptor = null;
		while (resultSet.next()) { 
			columnCd = resultSet.getString("column_cd"); 
			columnDescriptor = resultSet.getString("name_char");
			if (columnCd != null && columnDescriptor != null) { 
				columnDescriptorMap.put(columnCd.trim().toLowerCase() , columnDescriptor);
			}
			
		}
		return columnDescriptorMap;
	}

	public class TableMetaData {
		public String column_name;
		public String column_type;
		public String column_type_size;
		public String column_comment;
		public String column_xml_type;
	}
}
