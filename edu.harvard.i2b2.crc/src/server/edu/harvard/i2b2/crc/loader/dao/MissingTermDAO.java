package edu.harvard.i2b2.crc.loader.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.crc.loader.datavo.loader.DataSourceLookup;
import edu.harvard.i2b2.crc.loader.datavo.loader.query.MissingCodesConceptSetType;
import edu.harvard.i2b2.crc.loader.datavo.loader.query.MissingCodesModifierSetType;
import edu.harvard.i2b2.crc.loader.datavo.loader.query.MissingCodesObserverSetType;
import edu.harvard.i2b2.crc.loader.datavo.loader.query.MissingTermSetReportType;

/**
 * Patient Dimension data access object.
 * 
 * @author rk903
 */
public class MissingTermDAO extends CRCLoaderDAO implements IMissingTermDAO {

	private DataSourceLookup dataSourceLookup = null;

	private static Log log = LogFactory.getLog(MissingTermDAO.class);

	public MissingTermDAO(DataSourceLookup dataSourceLookup,
			DataSource dataSource) {
		setDataSource(dataSource);
		setDbSchemaName(dataSourceLookup.getFullSchema());
		this.dataSourceLookup = dataSourceLookup;

	}

	public MissingTermSetReportType getMissingTermReport(int uploadId,
			String setName) throws I2B2DAOException {

		String uploadConstrainSql = "", uploadConstrainMappedSql = "" ;
		Connection conn = null;
		MissingTermSetReportType missingTermReport = new MissingTermSetReportType();
		try {

			if (uploadId != 0) {
				uploadConstrainSql = " and obs.upload_id =  " + uploadId;
				uploadConstrainMappedSql = " where obs.upload_id =  " + uploadId;
			}
			conn = getDataSource().getConnection();
			String unmappedSql = "", mappedSql = "";

			if (setName.equals("observer_set")) {
				unmappedSql = " select count(distinct provider_id) from "
						+ this.getDbSchemaName() + "observation_fact "
						+ " where " 
						+ " provider_id not in (select provider_id  from "
						+ this.getDbSchemaName() + "provider_dimension) " + uploadConstrainSql;
				mappedSql = " select count(distinct provider_id) from "
						+ this.getDbSchemaName() + "observation_fact obs" + uploadConstrainMappedSql;

			} else if (setName.equals("concept_set")) {

				unmappedSql = " select count(distinct concept_cd) from "
						+ this.getDbSchemaName() + "observation_fact "
						+ " where " + uploadConstrainSql
						+ " concept_cd not in (select concept_cd  from "
						+ this.getDbSchemaName() + "concept_dimension) " + uploadConstrainSql;
				mappedSql = " select count(distinct concept_cd) from "
						+ this.getDbSchemaName() + "observation_fact obs " + uploadConstrainMappedSql;
			} else if (setName.equals("modifier_set")) {
				unmappedSql = " select count(distinct modifier_cd) from "
						+ this.getDbSchemaName() + "observation_fact "
						+ " where " + uploadConstrainSql
						+ " modifier_cd not in (select modifier_cd  from "
						+ this.getDbSchemaName() + "modifier_dimension) " + uploadConstrainSql;
				mappedSql = " select count(distinct modifier_cd) from "
						+ this.getDbSchemaName() + "observation_fact obs" + uploadConstrainMappedSql;

			}

			Statement stmt = conn.createStatement();
			log.debug("Executing sql [" + unmappedSql + "]");
			ResultSet resultSet = stmt.executeQuery(unmappedSql);
			resultSet.next();
			int unmappedCount = resultSet.getInt(1);
			log.debug("Executing sql [" + mappedSql + "]");
			stmt = conn.createStatement();
			resultSet = stmt.executeQuery(mappedSql);
			resultSet.next();
			int mappedCount = resultSet.getInt(1);
			resultSet.close();
			stmt.close();

			missingTermReport.setUnmapped(unmappedCount);
			missingTermReport.setMapped(mappedCount - unmappedCount);

		} catch (SQLException sqlEx) {
			sqlEx.printStackTrace();
			throw new I2B2DAOException("SQLException occured"
					+ sqlEx.getMessage(), sqlEx);
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new I2B2DAOException("Exception occured" + ex.getMessage(),
					ex);
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException sqlEx) {
					sqlEx.printStackTrace();
					log.error("Error while closing connection", sqlEx);
				}
			}
		}
		return missingTermReport;
	}

	public MissingCodesConceptSetType getMissingConceptSet(int uploadId,
			int startPos, int endPos, boolean detailFlag)
			throws I2B2DAOException {
		Connection conn = null;
		String sql = "", uploadConstrainSql = "";
		MissingCodesConceptSetType missConceptSetType = new MissingCodesConceptSetType();
		try {

			if (uploadId != 0) {
				uploadConstrainSql = " and obs.upload_id = " + uploadId;
			}
			conn = getDataSource().getConnection();
			if (dataSourceLookup.getServerType().equalsIgnoreCase(
					DataSourceLookupDAOFactory.SQLSERVER)) {
				sql = " select tot,concept_cd, rnum  from ( "
						+ " select count(*) tot, obs1.concept_cd, row_number() over (order by obs1.concept_cd asc)  rnum from "
						+ this.getDbSchemaName() + "observation_fact obs1 where concept_cd not in ( "
						+ " select concept_cd from " + this.getDbSchemaName()
						+ "concept_dimension)  "
						+ uploadConstrainSql + " group by obs1.concept_cd) f where rnum between " + startPos + " and "  + endPos ;
			} else if (dataSourceLookup.getServerType().equalsIgnoreCase(
					DataSourceLookupDAOFactory.ORACLE)) {
				sql = " select tot, concept_cd, rnum from (select count(*) tot, obs1.concept_cd, rownum  rnum from "
						+ this.getDbSchemaName()
						+ "observation_fact obs1 where  "
						+ " concept_cd not in ( "
						+ " select concept_cd from "
						+ this.getDbSchemaName()
						+ "concept_dimension) "
		   			    + uploadConstrainSql
						+ " group by obs1.concept_cd) f	where rnum between "
						+ startPos + " and " + endPos;

			}
			Statement stmt = conn.createStatement();
			log.debug("Executing sql ["+ sql + "]");
			ResultSet resultSet = stmt.executeQuery(sql);

			MissingCodesConceptSetType.Concept concept = new MissingCodesConceptSetType.Concept();

			while (resultSet.next()) {
				concept = new MissingCodesConceptSetType.Concept();
				concept.setConceptCd(resultSet.getString("concept_cd"));
				concept.setMissingTotal(resultSet.getInt("tot"));
				missConceptSetType.getConcept().add(concept);
			}
			resultSet.close();

		} catch (SQLException sqlEx) {
			sqlEx.printStackTrace();
			throw new I2B2DAOException("SQLException occured"
					+ sqlEx.getMessage(), sqlEx);
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new I2B2DAOException("Exception occured" + ex.getMessage(),
					ex);
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException sqlEx) {
					sqlEx.printStackTrace();
					log.error("Error while closing connection", sqlEx);
				}
			}
		}

		return missConceptSetType;
	}

	public MissingCodesModifierSetType getMissingModifierSet(int uploadId,
			int startPos, int endPos, boolean detailFlag)
			throws I2B2DAOException {
		Connection conn = null;
		String sql = "", uploadConstrainSql = "";
		MissingCodesModifierSetType missModifierSetType = new MissingCodesModifierSetType();
		try {

			if (uploadId != 0) {
				uploadConstrainSql = " and obs.upload_id = " + uploadId;
			}
			conn = getDataSource().getConnection();
			if (dataSourceLookup.getServerType().equalsIgnoreCase(
					DataSourceLookupDAOFactory.SQLSERVER)) {
				sql = " select tot,modifier_cd, rnum  from ( "
					+ " select count(*) tot, obs1.modifier_cd, row_number() over (order by obs1.modifier_cd asc)  rnum from "
					+ this.getDbSchemaName() + "observation_fact obs1 where modifier_cd not in ( "
					+ " select modifier_cd from " + this.getDbSchemaName()
					+ "modifier_dimension)  "
					+ uploadConstrainSql + " group by obs1.modifier_cd) f where rnum between " + startPos + " and "  + endPos ;
			} else if (dataSourceLookup.getServerType().equalsIgnoreCase(
					DataSourceLookupDAOFactory.ORACLE)) {
				sql = " select tot, modifier_cd, rnum from (select count(*) tot, obs1.modifier_cd, rownum  rnum from "
					+ this.getDbSchemaName()
					+ "observation_fact obs1 where  "
					+ " modifier_cd not in ( "
					+ " select modifier_cd from "
					+ this.getDbSchemaName()
					+ "modifier_dimension) "
	   			    + uploadConstrainSql
					+ " group by obs1.modifier_cd) f	where rnum between "
					+ startPos + " and " + endPos;

			}
			Statement stmt = conn.createStatement();
			log.debug("Executing sql ["+ sql + "]");
			ResultSet resultSet = stmt.executeQuery(sql);

			MissingCodesModifierSetType.Modifier modifier = new MissingCodesModifierSetType.Modifier();

			while (resultSet.next()) {
				modifier = new MissingCodesModifierSetType.Modifier();
				modifier.setModifierCd(resultSet.getString("modifier_cd"));
				modifier.setMissingTotal(resultSet.getInt("tot"));
				missModifierSetType.getModifier().add(modifier);
			}
			resultSet.close();

		} catch (SQLException sqlEx) {
			sqlEx.printStackTrace();
			throw new I2B2DAOException("SQLException occured"
					+ sqlEx.getMessage(), sqlEx);
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new I2B2DAOException("Exception occured" + ex.getMessage(),
					ex);
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException sqlEx) {
					sqlEx.printStackTrace();
					log.error("Error while closing connection", sqlEx);
				}
			}
		}

		return missModifierSetType;
	}

	public MissingCodesObserverSetType getMissingObserverSet(int uploadId,
			int startPos, int endPos, boolean detailFlag)
			throws I2B2DAOException {
		Connection conn = null;
		String sql = "", uploadConstrainSql = "";
		MissingCodesObserverSetType missObserverSetType = new MissingCodesObserverSetType();
		try {

			if (uploadId != 0) {
				uploadConstrainSql = " and obs.upload_id = " + uploadId;
			}
			conn = getDataSource().getConnection();
			if (dataSourceLookup.getServerType().equalsIgnoreCase(
					DataSourceLookupDAOFactory.SQLSERVER)) {
				sql = " select tot,provider_id, rnum  from ( "
					+ " select count(*) tot, obs1.provider_id, row_number() over (order by obs1.provider_id asc)  rnum from "
					+ this.getDbSchemaName() + "observation_fact obs1 where provider_id not in ( "
					+ " select provider_id from " + this.getDbSchemaName()
					+ "provider_dimension)  "
					+ uploadConstrainSql + " group by obs1.provider_id) f where rnum between " + startPos + " and "  + endPos ;
			} else if (dataSourceLookup.getServerType().equalsIgnoreCase(
					DataSourceLookupDAOFactory.ORACLE)) {
				sql = " select tot,provider_id, rnum from (select count(*) tot, obs1.provider_id, rownum  rnum from "
					+ this.getDbSchemaName()
					+ "observation_fact obs1 where  "
					+ " provider_id not in ( "
					+ " select provider_id from "
					+ this.getDbSchemaName()
					+ "provider_dimension) "
	   			    + uploadConstrainSql
					+ " group by obs1.provider_id) f	where rnum between "
					+ startPos + " and " + endPos;

			}
			Statement stmt = conn.createStatement();
			log.debug("Executing sql ["+ sql + "]");
			ResultSet resultSet = stmt.executeQuery(sql);

			MissingCodesObserverSetType.Observer observer = new MissingCodesObserverSetType.Observer();

			while (resultSet.next()) {
				observer = new MissingCodesObserverSetType.Observer();
				observer.setObserverCd(resultSet.getString("provider_id"));
				observer.setMissingTotal(resultSet.getInt("tot"));
				missObserverSetType.getObserver().add(observer);
			}
			resultSet.close();

		} catch (SQLException sqlEx) {
			sqlEx.printStackTrace();
			throw new I2B2DAOException("SQLException occured"
					+ sqlEx.getMessage(), sqlEx);
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new I2B2DAOException("Exception occured" + ex.getMessage(),
					ex);
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException sqlEx) {
					sqlEx.printStackTrace();
					log.error("Error while closing connection", sqlEx);
				}
			}
		}

		return missObserverSetType;
	}

}
