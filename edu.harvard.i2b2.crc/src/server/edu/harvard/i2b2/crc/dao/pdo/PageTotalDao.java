package edu.harvard.i2b2.crc.dao.pdo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;

import javax.sql.DataSource;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.util.db.JDBCUtil;
import edu.harvard.i2b2.crc.dao.CRCDAO;
import edu.harvard.i2b2.crc.dao.DAOFactoryHelper;
import edu.harvard.i2b2.crc.dao.pdo.input.IFactRelatedQueryHandler;
import edu.harvard.i2b2.crc.dao.pdo.input.IInputOptionListHandler;
import edu.harvard.i2b2.crc.dao.pdo.input.SQLServerFactRelatedQueryHandler;
import edu.harvard.i2b2.crc.datavo.db.DataSourceLookup;
import edu.harvard.i2b2.crc.datavo.pdo.query.PanelType;

public class PageTotalDao extends CRCDAO implements IPageDao {

	private DataSourceLookup dataSourceLookup = null;

	public PageTotalDao(DataSourceLookup dataSourceLookup, DataSource dataSource) {
		setDataSource(dataSource);
		setDbSchemaName(dataSourceLookup.getFullSchema());
		this.dataSourceLookup = dataSourceLookup;
	}

	public long getTotalForAllPanel(List<String> panelSqlList,
			List<Integer> sqlParamCountList,
			IInputOptionListHandler inputOptionListHandler)
			throws I2B2DAOException {

		long totalAcrossPanel = 0;
		int i = 0, sqlParamCount = 0;
		ResultSet resultSet = null;
		Connection conn = null;
		try {
			// get connection
			conn = this.getDataSource().getConnection();

			if (dataSourceLookup.getServerType().equalsIgnoreCase(
					DAOFactoryHelper.SQLSERVER)
					&& inputOptionListHandler.isEnumerationSet()) {
				inputOptionListHandler.uploadEnumerationValueToTempTable(conn);
				// sqlserverLoadTempTable(conn, inputOptionListHandler);
			} else if (dataSourceLookup.getServerType().equalsIgnoreCase(
					DAOFactoryHelper.ORACLE)
					&& inputOptionListHandler.isEnumerationSet()) {
				inputOptionListHandler.uploadEnumerationValueToTempTable(conn);
			} else 		if (dataSourceLookup.getServerType().equalsIgnoreCase(
					DAOFactoryHelper.POSTGRESQL)
					&& inputOptionListHandler.isEnumerationSet()) {
				inputOptionListHandler.uploadEnumerationValueToTempTable(conn);
				// sqlserverLoadTempTable(conn, inputOptionListHandler);
			}
			long startTime = System.currentTimeMillis();

			// iterate sql
			for (String singlePanelSql : panelSqlList) {
				sqlParamCount = sqlParamCountList.get(i++);
				resultSet = executeTotalSql(singlePanelSql, conn,
						sqlParamCount, inputOptionListHandler);
				resultSet.next();
				totalAcrossPanel += resultSet.getLong(1);
			}
			long endTime = System.currentTimeMillis();
			long totalTime = endTime - startTime;
			log.debug("********* Time for the  Total Sql ************ "
							+ totalTime);
		} catch (SQLException sqlEx) {
			throw new I2B2DAOException("", sqlEx);
		} finally {

			try {
				inputOptionListHandler.deleteTempTable(conn);
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			// close connection
			try {
				JDBCUtil.closeJdbcResource(null, null, conn);
			} catch (SQLException e) {
				log.error("Error trying to close connection", e);
			}
		}

		return totalAcrossPanel;
	}

	public HashMap getMinIndexAndCountAllPanel(List<String> panelSqlList,
			List<Integer> sqlParamCountList,
			IInputOptionListHandler inputOptionListHandler)
			throws I2B2DAOException {

		int i = 0, sqlParamCount = 0;
		ResultSet resultSet = null;
		Connection conn = null;
		HashMap minAndTotalMap = new HashMap();
		int minIndex = 0, tempMinIndex = 0;
		long minIndexTotal = 0, tempMinIndexTotal = 0;

		try {
			// get connection
			conn = this.getDataSource().getConnection();

			if (dataSourceLookup.getServerType().equalsIgnoreCase(
					DAOFactoryHelper.SQLSERVER)
					&& inputOptionListHandler.isEnumerationSet()) {
				// upLoadTempTableForMin(conn, inputOptionListHandler);
				// sqlserverLoadTempTable(conn, inputOptionListHandler);
				inputOptionListHandler.uploadEnumerationValueToTempTable(conn);
			} else if (dataSourceLookup.getServerType().equalsIgnoreCase(
					DAOFactoryHelper.ORACLE)
					&& inputOptionListHandler.isEnumerationSet()) {
				// oracleLoadTempTable(conn, inputOptionListHandler);
				inputOptionListHandler.uploadEnumerationValueToTempTable(conn);
			} else 		if (dataSourceLookup.getServerType().equalsIgnoreCase(
					DAOFactoryHelper.POSTGRESQL)
					&& inputOptionListHandler.isEnumerationSet()) {
				// upLoadTempTableForMin(conn, inputOptionListHandler);
				// sqlserverLoadTempTable(conn, inputOptionListHandler);
				inputOptionListHandler.uploadEnumerationValueToTempTable(conn);
			}

			boolean firstTimeFlag = true;
			// iterate sql
			for (String singlePanelSql : panelSqlList) {
				sqlParamCount = sqlParamCountList.get(i++);
				resultSet = executeTotalSql(singlePanelSql, conn,
						sqlParamCount, inputOptionListHandler);
				resultSet.next();
				tempMinIndex = resultSet.getInt(1);
				tempMinIndexTotal = resultSet.getLong(2);
				if (firstTimeFlag) {
					minIndex = tempMinIndex;
					minIndexTotal = tempMinIndexTotal;
					firstTimeFlag = false;
				} else if (minIndexTotal < tempMinIndexTotal) {
					minIndex = tempMinIndex;
					minIndexTotal = tempMinIndexTotal;
				}
			}
		} catch (SQLException sqlEx) {
			throw new I2B2DAOException("", sqlEx);
		} finally {
			if (dataSourceLookup.getServerType().equalsIgnoreCase(
					DAOFactoryHelper.SQLSERVER)) {
				deleteTempTable(conn);
				if (inputOptionListHandler.isEnumerationSet()) {
					deleteTemp1Table(conn);
				}
			}
			// close connection
			try {
				JDBCUtil.closeJdbcResource(null, null, conn);
			} catch (SQLException e) {
				log.error("Error trying to close connection", e);
			}
		}

		minAndTotalMap.put("MIN_INDEX", minIndex);
		minAndTotalMap.put("MIN_INDEX_TOTAL", minIndexTotal);

		return minAndTotalMap;
	}

	private ResultSet executeTotalSql(String totalSql, Connection conn,
			int sqlParamCount, IInputOptionListHandler inputOptionListHandler)
			throws SQLException {

		PreparedStatement stmt = conn.prepareStatement(totalSql);
		ResultSet resultSet = null;

		System.out.println(totalSql + " [ " + sqlParamCount + " ]");
		if (inputOptionListHandler.isCollectionId()) {
			for (int i = 1; i <= sqlParamCount; i++) {
				stmt.setInt(i, Integer.parseInt(inputOptionListHandler
						.getCollectionId()));
			}
		}

		resultSet = stmt.executeQuery();

		return resultSet;
	}

	public void sqlserverLoadTempTable(Connection conn,
			IInputOptionListHandler inputOptionListHandler) throws SQLException {
		// sqlserver
		upLoadTempTable(conn, inputOptionListHandler);

	}

	public String buildTotalSql(IFactRelatedQueryHandler factHandler,
			PanelType panel) throws I2B2DAOException {
		// call factrelatedhandler to build sql
		return factHandler.buildTotalQuery(panel,
				PdoQueryHandler.PLAIN_PDO_TYPE);
	}

	public void getSelect() {
	}

	public void getFrom() {
	}

	public void getWhere() {
	}

	private void upLoadTempTable(Connection conn,
			IInputOptionListHandler inputOptionListHandler) throws SQLException {
		List<String> enumList = inputOptionListHandler.getEnumerationList();
		// create temp table
		java.sql.Statement tempStmt = conn.createStatement();
		String createTempInputListTable = "create table "
				+ SQLServerFactRelatedQueryHandler.TEMP_PDO_INPUTLIST_TABLE
				+ " ( char_param1 varchar(100) )";
		tempStmt.executeUpdate(createTempInputListTable);
		// load to temp table
		// TempInputListInsert inputListInserter = new
		// TempInputListInsert(dataSource,TEMP_PDO_INPUTLIST_TABLE);
		// inputListInserter.setBatchSize(100);
		int i = 0;
		for (String singleValue : enumList) {
			tempStmt.addBatch("insert into "
					+ SQLServerFactRelatedQueryHandler.TEMP_PDO_INPUTLIST_TABLE
					+ " values ('" + singleValue + "' )");
			i++;
			if (i % 100 == 0) {
				tempStmt.executeBatch();

			}
		}
		tempStmt.executeBatch();
	}

	private void upLoadTempTableForMin(Connection conn,
			IInputOptionListHandler inputOptionListHandler) throws SQLException {
		List<String> enumList = inputOptionListHandler.getEnumerationList();
		deleteTempTable(conn);
		// create temp table
		java.sql.Statement tempStmt = conn.createStatement();
		String createTempInputListTable = "create table "
				+ SQLServerFactRelatedQueryHandler.TEMP_PDO_INPUTLIST_TABLE
				+ " (set_index int, char_param1 varchar(100) )";
		tempStmt.executeUpdate(createTempInputListTable);
		// load to temp table
		// TempInputListInsert inputListInserter = new
		// TempInputListInsert(dataSource,TEMP_PDO_INPUTLIST_TABLE);
		// inputListInserter.setBatchSize(100);
		int i = 0, j = 1;
		for (String singleValue : enumList) {
			tempStmt.addBatch("insert into "
					+ SQLServerFactRelatedQueryHandler.TEMP_PDO_INPUTLIST_TABLE
					+ "(set_index,char_param1)  values (" + j++ + ",'"
					+ singleValue + "' )");
			i++;
			if (i % 100 == 0) {
				tempStmt.executeBatch();

			}
		}
		tempStmt.executeBatch();
	}

	private void deleteTempTable(Connection conn) {

		Statement deleteStmt = null;
		try {
			deleteStmt = conn.createStatement();

			//conn
			//		.createStatement()
			deleteStmt.executeUpdate(
							"drop table "
									+ SQLServerFactRelatedQueryHandler.TEMP_PDO_INPUTLIST_TABLE);
		} catch (SQLException sqle) {
			;
		} finally {
			try {
				if(deleteStmt != null)
					deleteStmt.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	private void deleteTemp1Table(Connection conn) {

		Statement deleteStmt = null;
		try {
			deleteStmt = conn.createStatement();
		//	conn
		//			.createStatement()
			deleteStmt.executeUpdate(
							"drop table "
									+ SQLServerFactRelatedQueryHandler.TEMP_PDO_INPUTLIST_TABLE);
		} catch (SQLException sqle) {
			;
		} finally {
			try {
				if(deleteStmt != null)
					deleteStmt.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

}
