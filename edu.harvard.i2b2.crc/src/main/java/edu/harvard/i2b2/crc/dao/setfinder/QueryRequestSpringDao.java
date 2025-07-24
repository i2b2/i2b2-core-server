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
 *     Rajesh Kuttan
 */
package edu.harvard.i2b2.crc.dao.setfinder;
 
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.db.JDBCUtil;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.crc.dao.CRCDAO;
import edu.harvard.i2b2.crc.dao.setfinder.querybuilder.QueryToolUtil;
import edu.harvard.i2b2.crc.dao.setfinder.querybuilder.RecursiveBuild;
import edu.harvard.i2b2.crc.dao.setfinder.querybuilder.temporal.TemporalQueryBuilder;
import edu.harvard.i2b2.crc.datavo.db.DataSourceLookup;
import edu.harvard.i2b2.crc.util.ParamUtil;
import edu.harvard.i2b2.crc.util.QueryProcessorUtil;

/**
 * Helper class for setfinder operation. Builds sql from query definition,
 * executes the generated sql and create query results instance $Id:
 * QueryRequestSpringDao.java,v 1.5 2008/07/10 20:11:21 rk903 Exp $
 * 
 * @author rkuttan
 */
public class QueryRequestSpringDao extends CRCDAO implements IQueryRequestDao {
	/** Global temp table to store intermediate setfinder results **/
	private String TEMP_TABLE = "QUERY_GLOBAL_TEMP";

	/** Global temp table to store intermediate patient list **/
	private String TEMP_DX_TABLE = "DX";

	JdbcTemplate jdbcTemplate = null;
	DataSourceLookup dataSourceLookup = null;
	String processTimingFlag = "NONE";
	Map projectParamMap = null;
	boolean allowLargeTextValueConstrainFlag = true;
	boolean queryWithoutTempTableFlag = false;
	//boolean allowProtectedQueryFlag = false;
	List<String> userRoles = null;
	
	public QueryRequestSpringDao(DataSource dataSource,
			DataSourceLookup dataSourceLookup) {
		setDataSource(dataSource);
		jdbcTemplate = new JdbcTemplate(dataSource);
		this.dataSourceLookup = dataSourceLookup;

	}

	
	
	
	@Override
	public void setProjectParam(Map projectParamMap) {
		this.projectParamMap = projectParamMap;
		if (projectParamMap != null && projectParamMap.get(ParamUtil.PM_ENABLE_PROCESS_TIMING) != null) {
			this.processTimingFlag = (String)projectParamMap.get(ParamUtil.PM_ENABLE_PROCESS_TIMING);
		}
		
	}
	
	@Override
	public void setAllowLargeTextValueConstrainFlag(boolean allowLargeTextValueConstrainFlag)  { 
		this.allowLargeTextValueConstrainFlag = allowLargeTextValueConstrainFlag;
	}

	//@Override
//	public void setAllowProtectedQueryFlag(boolean allowProtectedQueryFlag)  { 
//		this.allowProtectedQueryFlag = allowProtectedQueryFlag;
//	}
	
	/**
	 * Function to build sql from given query definition This function uses
	 * QueryToolUtil class to build sql
	 * 
	 * @param queryRequestXml
	 * @return sql string
	 * @throws I2B2Exception 
	 * @throws JAXBUtilException 
	 */
	@Override
	public String[] buildSql(String queryRequestXml, boolean encounterSetFlag) throws I2B2Exception, JAXBUtilException {
		String sql = null, ignoredItemMessage = null, processTimingMessage = null;
		Connection conn = null;

		String queryType = null;
		try {
			// conn = getConnection();
			conn = dataSource.getConnection();

			// check to switch between the old and new setfinder query
			// generator.
			String queryGeneratorVersion = "1.7";
			try {
				QueryProcessorUtil qpUtil = QueryProcessorUtil.getInstance();
				queryGeneratorVersion = qpUtil
						.getCRCPropertyValue("edu.harvard.i2b2.crc.setfinder.querygenerator.version");
			} catch (I2B2Exception e) {
				// ignore this default will be 1.6
			}
			if (queryGeneratorVersion.equals("1.7")) {
				TemporalQueryBuilder temporalBuild = new TemporalQueryBuilder(dataSourceLookup,queryRequestXml);
				temporalBuild.setProjectParamMap(this.projectParamMap);
				temporalBuild.setAllowLargeTextValueConstrainFlag(allowLargeTextValueConstrainFlag);
				temporalBuild.setQueryWithoutTempTableFlag(queryWithoutTempTableFlag);
				temporalBuild.setUserRoles(userRoles);
				
				temporalBuild.startSqlBuild();
				sql = temporalBuild.getSql();
				ignoredItemMessage = temporalBuild.getIgnoredItemMessage();
				processTimingMessage = temporalBuild.getProcessTimingMessage();
				
				//if (temporalBuild.isProtectedQuery() && allowProtectedQueryFlag==false)
				//	throw new I2B2DAOException("This query contains protected.");
				//if (temporalBuild.isProtectedQuery() && allowProtectedQueryFlag==false)
			//		throw new I2B2DAOException("This query contains protected.");
				if (temporalBuild.isTemporalQuery())
						queryType = "TEMPORAL";
				if (temporalBuild.isProtectedQuery())
					queryType = "PROT"; 
				if (temporalBuild.isProtectedQuery()&&temporalBuild.isTemporalQuery())
					queryType = "PROT_TEMPORAL";
			}
			else if (queryGeneratorVersion.equals("1.6")) {
				RecursiveBuild recursiveBuild = new RecursiveBuild(dataSourceLookup,queryRequestXml,encounterSetFlag);
				recursiveBuild.setProjectParamMap(this.projectParamMap);
				recursiveBuild.setAllowLargeTextValueConstrainFlag(allowLargeTextValueConstrainFlag);
				
					recursiveBuild.startSqlBuild();
				sql = recursiveBuild.getSql();
				ignoredItemMessage = recursiveBuild.getIgnoredItemMessage();
				processTimingMessage = recursiveBuild.getProcessTimingMessage();
				//QueryToolUtilNew queryUtil = new QueryToolUtilNew(
				//		dataSourceLookup, queryRequestXml, encounterSetFlag);
				//sql = queryUtil.getSetfinderSqlForQueryDefinition();
				//ignoredItemMessage = queryUtil.getIgnoredItemMessage();
			} 
			/*else {
				log
						.warn("*** USING THE OLD QUERY GENERATOR *** QueryToolUtil.java");
				QueryToolUtil queryUtil = new QueryToolUtil(dataSourceLookup);
				sql = queryUtil.generateSQL(conn, queryRequestXml,
						encounterSetFlag);
				ignoredItemMessage = queryUtil.getIgnoredItemMessage();
			} */

		} catch (SQLException ex) {
			log.error("Error while building sql", ex);
			//throw new Exception("Error while building sql ", ex);
		} catch (I2B2Exception e) {
			log.error("QuieryRequestSptingDAO: Error while building sql I2b2 Error ", e);
			// TODO Auto-generated catch block
			throw e;
		//	e.printStackTrace();
		} catch (JAXBUtilException e) {
			throw e;
		} finally {
			try {
				JDBCUtil.closeJdbcResource(null, null, conn);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		return new String[] { sql, ignoredItemMessage, processTimingMessage, queryType};
	}




	@Override
	public void setQueryWithoutTempTableFlag(boolean queryWithoutTempTableFlag) {
		this.queryWithoutTempTableFlag = queryWithoutTempTableFlag;
	}




	@Override
	public void setUserRoles(List<String> userRoles) {
		this.userRoles = userRoles;
		
	}

	
}
