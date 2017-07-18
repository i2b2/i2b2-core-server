/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors: 
 *     Rajesh Kuttan
 */
package edu.harvard.i2b2.crc.dao.pdo;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.Iterator;

import javax.sql.DataSource;
import javax.xml.stream.XMLStreamException;

import org.apache.axis2.AxisFault;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.exception.StackTraceUtil;
import edu.harvard.i2b2.common.util.db.JDBCUtil;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.crc.dao.CRCDAO;
import edu.harvard.i2b2.crc.dao.DAOFactoryHelper;
import edu.harvard.i2b2.crc.dao.pdo.output.ObservationFactFactRelated;
import edu.harvard.i2b2.crc.dao.setfinder.querybuilder.ConceptNotFoundException;
import edu.harvard.i2b2.crc.dao.setfinder.querybuilder.OntologyException;
import edu.harvard.i2b2.crc.datavo.db.DataSourceLookup;
import edu.harvard.i2b2.crc.datavo.i2b2message.SecurityType;
import edu.harvard.i2b2.crc.datavo.ontology.DerivedFactColumnsType;
import edu.harvard.i2b2.crc.datavo.pdo.ObservationSet;
import edu.harvard.i2b2.crc.datavo.pdo.ObservationType;
import edu.harvard.i2b2.crc.datavo.pdo.PatientDataType;
import edu.harvard.i2b2.crc.datavo.pdo.query.FactPrimaryKeyType;
import edu.harvard.i2b2.crc.datavo.pdo.query.OutputOptionType;
import edu.harvard.i2b2.crc.delegate.ontology.CallOntologyUtil;
import edu.harvard.i2b2.crc.util.PMServiceAccountUtil;
import edu.harvard.i2b2.crc.util.QueryProcessorUtil;

/**
 * DAO class for observation fact $Id: ObservationFactDao.java,v 1.13 2008/07/21
 * 19:53:40 rk903 Exp $
 * 
 * @author rkuttan
 * @see FactPrimaryKeyType
 * @see OutputOptionType
 */
public class ObservationFactDao extends CRCDAO implements IObservationFactDao {

	private DataSourceLookup dataSourceLookup = null;

	public ObservationFactDao(DataSourceLookup dataSourceLookup,
			DataSource dataSource) {
		setDataSource(dataSource);
		setDbSchemaName(dataSourceLookup.getFullSchema());
		this.dataSourceLookup = dataSourceLookup;
	}

	/**
	 * Function returns Observation fact from the primary key.
	 * <p>
	 * Required fields : <b>patient_num, concept_cd, encounter_num</b>
	 * <p>
	 * Optional field : <b>provider_id,start_date</b>
	 * 
	 * @param factPrimaryKey
	 * @param factOutputOption
	 * @return PatientDataType
	 * @throws I2B2Exception
	 */
	public PatientDataType getObservationFactByPrimaryKey(
			FactPrimaryKeyType factPrimaryKey, OutputOptionType factOutputOption)
			throws I2B2DAOException {
		PatientDataType patientDataType = new PatientDataType();

		ObservationFactFactRelated factRelated = new ObservationFactFactRelated(
				factOutputOption);

		String defaultTableName =  getDbSchemaName() + "observation_fact";
		
		String sql = " SELECT " + factRelated.getSelectClause() + " \n "
				+ " FROM " + defaultTableName + " obs \n"
				+ " WHERE obs.encounter_num = ? AND \n "
				+ " obs.patient_num  = ? AND \n" + " obs.concept_cd = ?  \n";

		if (factPrimaryKey.getObserverId() != null) {
			sql += " AND obs.provider_id = ? \n";
		}

		// make given start date to 'mm-dd-yyyy hh24:mi' format
		if (factPrimaryKey.getStartDate() != null) {
			GregorianCalendar gc = factPrimaryKey.getStartDate()
					.toGregorianCalendar();
			String sqlFormatedStartDate = "";
			if (dataSourceLookup.getServerType().equalsIgnoreCase(
					DAOFactoryHelper.ORACLE)) {
				SimpleDateFormat dateFormat = new SimpleDateFormat(
						"dd-MMM-yyyy HH:mm:ss");
				sqlFormatedStartDate = dateFormat.format(gc.getTime());
				sql += (" AND obs.start_date = to_date('"
						+ sqlFormatedStartDate + " ', 'DD-MON-YYYY HH24:MI:SS') ");
			} else if (dataSourceLookup.getServerType().equalsIgnoreCase(
					DAOFactoryHelper.SQLSERVER) || dataSourceLookup.getServerType().equalsIgnoreCase(
							DAOFactoryHelper.POSTGRESQL)) {
				SimpleDateFormat dateFormat = new SimpleDateFormat(
						"yyyy-MM-dd'T'HH:mm:ss");
				sqlFormatedStartDate = dateFormat.format(gc.getTime());
				sql += (" AND obs.start_date = '" + sqlFormatedStartDate + "'");
			}
		}

		if (factPrimaryKey.getModifierCd() != null) {
			sql += " AND obs.modifier_cd = ? ";
		}
		if (factPrimaryKey.getInstanceNum() != null) {
			sql += " AND obs.instance_num = ? ";
		}

//		log.info("Pre Sql from ObservationFactDAO[" + sql + "]");
		
		
		int numDerivedTables = 1;
		boolean derivedFactTable = QueryProcessorUtil.getInstance().getDerivedFactTable();
		String finalSql = sql;
		if(derivedFactTable == true){
			// call ONT to get concept info 
			DerivedFactColumnsType columns = getFactColumnsFromOntologyCellByCode(factPrimaryKey.getConceptCd());
			
			if(columns != null){
				numDerivedTables = columns.getDerivedFactTableColumn().size();
//				log.info("found " +numDerivedTables + " derived tables");
				// parse through solumns and build up replace string.

				Iterator<String> it = columns.getDerivedFactTableColumn().iterator();
				String column = it.next();
//				log.info(column);
				if(column.contains(".")) {
					int lastIndex = column.lastIndexOf(".");
					String tableName = this.getDbSchemaName() + (column.substring(0, lastIndex));
					log.info(tableName);
					finalSql = finalSql.replace(defaultTableName, tableName);
				}
				while(it.hasNext()){
					column = it.next();
					if(column.contains(".")) {
						int lastIndex = column.lastIndexOf(".");
						String table = this.getDbSchemaName() + (column.substring(0, lastIndex));
						if(table != null)
							finalSql += "\n union all \n";
							finalSql  += sql.replace(defaultTableName, table);
					}
				
				}
			}
		}
			
//		log.info("Generated Sql from ObservationFactDAO[" + finalSql + "]");

		Connection conn = null;
		PreparedStatement stmt = null;

		try {
			// get db connection
			// conn = getConnection();
			conn = getDataSource().getConnection();

			// create prepared statement
			stmt = conn.prepareStatement(finalSql);
			int index = 1;
			for (int i=0; i<= numDerivedTables -1; i++){
				stmt.setInt(i+index, Integer.parseInt(factPrimaryKey.getEventId()));
			//	log.info(i+index + " " + factPrimaryKey.getEventId());
				index++;
				stmt.setInt(i+index , Integer.parseInt(factPrimaryKey.getPatientId()));
			//	log.info(i+index +" " + factPrimaryKey.getPatientId());
				index++;
				stmt.setString(i+index, factPrimaryKey.getConceptCd());
		//		log.info(i+index + " "  + factPrimaryKey.getConceptCd());
				index++;
				// if provider id is not null add it to sql parameter
				if (factPrimaryKey.getObserverId() != null) {
					stmt.setString(i+index, factPrimaryKey.getObserverId());
				//	log.info(i+index + " " + factPrimaryKey.getObserverId());
					index++;
				}
				// if modifier cd is not null add it to sql parameter
				if (factPrimaryKey.getModifierCd() != null) {
					stmt.setString(i+index, factPrimaryKey.getModifierCd());
		//			log.info(i + index +" " + factPrimaryKey.getModifierCd());
					index++;
				}
				if (factPrimaryKey.getInstanceNum() != null) {
					int instanceNum = Integer.parseInt(factPrimaryKey
							.getInstanceNum());
					stmt.setInt(i+index, instanceNum);
		//			log.info(i + index + " " + factPrimaryKey.getInstanceNum());
					index++;
				}

			} 

			// original (pre-OMOP) code
			/*			stmt.setInt(1, Integer.parseInt(factPrimaryKey.getEventId())); 
			stmt.setInt(2, Integer.parseInt(factPrimaryKey.getPatientId()));
			stmt.setString(3, factPrimaryKey.getConceptCd());

			int i = 4;
			String providerId = factPrimaryKey.getObserverId();

			// if provider id is not null add it to sql parameter
			if (providerId != null) {
				stmt.setString(i, providerId);
				i++;
			}

			// if modifier cd is not null add it to sql parameter
			if (factPrimaryKey.getModifierCd() != null) {
				stmt.setString(i, factPrimaryKey.getModifierCd());
				i++;
			}

			if (factPrimaryKey.getInstanceNum() != null) {
				int instanceNum = Integer.parseInt(factPrimaryKey
						.getInstanceNum());
				i++;
				stmt.setInt(i, instanceNum);
			}
			 */

			ResultSet resultSet = stmt.executeQuery();
			ObservationSet obsFactSet = new ObservationSet();

			I2B2PdoFactory.ObservationFactBuilder observationFactBuilder = new I2B2PdoFactory().new ObservationFactBuilder(
					factRelated.isSelectDetail(), factRelated.isSelectBlob(),
					factRelated.isSelectStatus(), dataSourceLookup.getServerType());
			while (resultSet.next()) {
				ObservationType observationFactType = observationFactBuilder
						.buildObservationSet(resultSet);
				obsFactSet.getObservation().add(observationFactType);
			}

			patientDataType.getObservationSet().add(obsFactSet);
		} catch (SQLException sqlEx) {
			log.error(sqlEx);
			throw new I2B2DAOException("", sqlEx);
		} catch (IOException ioEx) {
			log.error(ioEx);
			throw new I2B2DAOException("", ioEx);
		} finally {
			try {
				JDBCUtil.closeJdbcResource(null, stmt, conn);
			} catch (SQLException sqlEx) {
				sqlEx.printStackTrace();
			}
		}

		return patientDataType;
	}
	

	
	protected DerivedFactColumnsType getFactColumnsFromOntologyCellByCode(String conceptCd)
			throws ConceptNotFoundException, OntologyException {
		DerivedFactColumnsType factColumns = new DerivedFactColumnsType();
		try {
			
			QueryProcessorUtil qpUtil = QueryProcessorUtil.getInstance();
			String ontologyUrl = qpUtil
					.getCRCPropertyValue(QueryProcessorUtil.ONTOLOGYCELL_ROOT_WS_URL_PROPERTIES);

			SecurityType securityType = PMServiceAccountUtil
					.getServiceSecurityType(dataSourceLookup.getDomainId());
			
			factColumns = CallOntologyUtil.callGetFactColumnsByConceptCd(conceptCd,
					securityType, dataSourceLookup.getProjectPath(),
					ontologyUrl +"/getCodeInfo");
		} catch (JAXBUtilException e) {

			log.error("Error while fetching metadata [" + conceptCd
					+ "] from ontology ", e);
			throw new OntologyException("Error while fetching metadata ["
					+ conceptCd + "] from ontology "
					+ StackTraceUtil.getStackTrace(e));
		} catch (I2B2Exception e) {
			log.error("Error while fetching metadata from ontology ", e);
			throw new OntologyException("Error while fetching metadata ["
					+ conceptCd + "] from ontology "
					+ StackTraceUtil.getStackTrace(e));
		} catch (AxisFault e) {
			log.error("Error while fetching metadata from ontology ", e);
			throw new OntologyException("Error while fetching metadata ["
					+ conceptCd + "] from ontology "
					+ StackTraceUtil.getStackTrace(e));
		} catch (XMLStreamException e) {
			log.error("Error while fetching metadata from ontology ", e);
			throw new OntologyException("Error while fetching metadata ["
					+ conceptCd + "] from ontology "
					+ StackTraceUtil.getStackTrace(e));
		}

		return factColumns;
	}
	

}
