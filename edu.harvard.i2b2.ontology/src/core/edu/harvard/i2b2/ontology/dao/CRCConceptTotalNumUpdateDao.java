/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
package edu.harvard.i2b2.ontology.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.Logger;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.exception.StackTraceUtil;
import edu.harvard.i2b2.common.util.db.JDBCUtil;
import edu.harvard.i2b2.ontology.datavo.crc.setfinder.query.MasterInstanceResultResponseType;
import edu.harvard.i2b2.ontology.datavo.crc.setfinder.query.MasterResponseType;
import edu.harvard.i2b2.ontology.datavo.crc.setfinder.query.QueryStatusTypeType;
import edu.harvard.i2b2.ontology.datavo.crc.setfinder.query.ResultResponseType;
import edu.harvard.i2b2.ontology.datavo.i2b2message.MessageHeaderType;
import edu.harvard.i2b2.ontology.datavo.i2b2message.PasswordType;
import edu.harvard.i2b2.ontology.datavo.i2b2message.SecurityType;
import edu.harvard.i2b2.ontology.datavo.pm.ProjectType;
import edu.harvard.i2b2.ontology.datavo.vdo.OntologyProcessStatusType;
import edu.harvard.i2b2.ontology.delegate.crc.CallCRCUtil;
import edu.harvard.i2b2.ontology.ejb.DBInfoType;
import edu.harvard.i2b2.ontology.ejb.TableAccessType;
import edu.harvard.i2b2.ontology.util.OntologyUtil;

public class CRCConceptTotalNumUpdateDao extends JdbcDaoSupport {

	private static Log log = LogFactory.getLog(CRCConceptTotalNumUpdateDao.class);
	protected static Logger logesapi = ESAPI.getLogger(CRCConceptTotalNumUpdateDao.class);

	private DataSource dataSource = null;

	OntologyUtil ontUtil = OntologyUtil.getInstance(); 
	
	public void setDataSourceObject(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	private void setDataSource(String dataSourceName) {
		DataSource ds = null;
		try {
			ds = OntologyUtil.getInstance().getDataSource(dataSourceName);
		} catch (I2B2Exception e2) {
			log.error(e2.getMessage());
			;
		}
		dataSource = ds;
	}

	public OntologyProcessStatusType updateConceptTotalNum(ProjectType projectInfo,
			DBInfoType dbInfo, int processId,
			MessageHeaderType messageHeaderType, boolean synchronizeAllFlag)
			throws I2B2Exception {
		OntologyProcessStatusType ontProcessStatusType = null;
		OntProcessStatusDao ontProcessStatusDao = null;

		
		Connection conn = null;
		boolean killedFlag = false;
		ResultSet resultSet = null;
		QueryStatusTypeType queryStatusType = null;
		MasterInstanceResultResponseType masterInstanceResultResponse = null;
		MasterResponseType masterResponseType = null;
		ResultResponseType resultResponse = null;
		TableAccessType tableAccessType = null;
		int totalRecordToUpdate = 0, totalProcessedRecord = 0 ;
		PreparedStatement pStmt = null, updatePStmt = null;
		try {
			String metadataSchema = dbInfo.getDb_fullSchema();
			TableAccessDao tableAccessDao = new TableAccessDao();
			if (this.dataSource == null) {
				setDataSource(dbInfo.getDb_dataSource());
			} else {
				tableAccessDao.setDataSourceObject(this.dataSource);
			}

			SecurityType securityType = getServiceAccountSecurityType(messageHeaderType);
			String projectId = getProjectId(messageHeaderType);

			// update the process status
			ontProcessStatusDao = new OntProcessStatusDao(dataSource,
					projectInfo, dbInfo);

			conn = this.dataSource.getConnection();
			
			//read the list of metadata
			//for each of metadata table select c_fullpath order by c_fullpath 
			List<TableAccessType> tableAccessTypeList = tableAccessDao.getAllTableAccess(projectInfo, dbInfo); 
			log.debug("Table access List size [ " + tableAccessTypeList.size() + " ]");
			List<String> tableNameList = new ArrayList<String>();
			
			
			for (Iterator<TableAccessType> tableAccess = tableAccessTypeList.iterator(); tableAccess.hasNext();) {
				tableAccessType = tableAccess.next();
				
				String updateStmtStr = " update " + metadataSchema + tableAccessType.getTableName().trim() + " set c_totalnum = null where c_fullname like ?  ";
				log.debug("Executing sql [" + updateStmtStr + "] c_fullname [" + tableAccessType.getFullName() + " ]");
				if (synchronizeAllFlag) {
					pStmt = conn.prepareStatement(updateStmtStr);
					pStmt.setString(1, tableAccessType.getFullName() + "%");
					pStmt.executeUpdate();
					pStmt.close();
				}
				
				
				
				String selectStmt = "select count(1) from " + metadataSchema + tableAccessType.getTableName().trim() + " where c_fullname like ? ";
				if (synchronizeAllFlag == false) {
					selectStmt += " and c_totalnum is null ";
				}
				
				pStmt = conn.prepareStatement(selectStmt);
				pStmt.setString(1, tableAccessType.getFullName() + "%");
				 resultSet = pStmt.executeQuery();
				 resultSet.next();
				 totalRecordToUpdate += resultSet.getInt(1);
				 resultSet.close();
				 pStmt.close();
				 log.debug("Executing sql [" + selectStmt + "] c_fullname [" + tableAccessType.getFullName() + " ] totalCount" + totalRecordToUpdate );
			}
			
			
			
			//update the step field (PROCESSED updatedrecord/totalrecordtoupdate format)
			ontProcessStatusDao.updateStatus(processId, new Date(System
					.currentTimeMillis()), "PROCESSED 0/"+totalRecordToUpdate, "PROCESSING");
			
			for (Iterator<TableAccessType> tableAccess = tableAccessTypeList.iterator(); tableAccess.hasNext();) { 
				tableAccessType = tableAccess.next();
				String selectStmt = "select * from " + metadataSchema + tableAccessType.getTableName().trim() + " where c_fullname like ? and c_visualattributes not like 'C%' and c_visualattributes not like 'O%' " +
						" and c_visualattributes not like 'D%' and c_visualattributes not like 'R%' ";
				if (synchronizeAllFlag == false) {
					selectStmt += " and c_totalnum is null ";
				}
				selectStmt += " order by c_fullname";
				pStmt = conn.prepareStatement(selectStmt);
				pStmt.setString(1, tableAccessType.getFullName() + "%");
				 resultSet = pStmt.executeQuery();
				 
				 updatePStmt = conn.prepareStatement("update "+ metadataSchema + tableAccessType.getTableName().trim() +" set c_totalnum = ? where c_fullname = ? ");
				//	CallCRCUtil crcUtil = new CallCRCUtil(
				//			securityType, projectId);
				String cFullName = "";
				boolean conceptSkipFlag = false;
				while (resultSet.next()) {
					
					cFullName = resultSet.getString("c_fullname");
					//check for status of ont_process_status giving the process id
					//if the status is  "aborted", then exit
					ontProcessStatusType = ontProcessStatusDao.findById(processId);
					if (ontProcessStatusType.getProcessStatusCd().equalsIgnoreCase("ABORT")) { 
						killedFlag = true;
						break;
					}
					//pass c_fullname to crc setfinder util class
					// call frc
					logesapi.debug(null,"Begin Setfinder query to CRC [" + cFullName + "]");
					conceptSkipFlag = false;
					try { 
						masterInstanceResultResponse = CallCRCUtil.callSetfinderQuery("\\\\" + tableAccessType.getTableCd().trim() + cFullName, securityType, projectId);
					} catch (Throwable  i2b2Ex) { 
						logesapi.info(null,"Patient count caught the exception " + i2b2Ex.getMessage());
						i2b2Ex.printStackTrace();
						conceptSkipFlag = true;
					}
					
					if (conceptSkipFlag == false) { 
						String queryInstanceId = masterInstanceResultResponse.getQueryInstance().getQueryInstanceId();
						queryStatusType = masterInstanceResultResponse.getQueryInstance().getQueryStatusType();
						logesapi.debug(null,"End Setfinder query to CRC[" + cFullName + "]");
						int totalNum = 0;
						if (queryStatusType.getName().equalsIgnoreCase("PROCESSING")) { 
							log.info("Setfinder request status is processing query instance id [ " + queryInstanceId + " ]");
							resultResponse = CallCRCUtil.pollQueryStatus(queryInstanceId, securityType, projectId); 
							totalNum = resultResponse.getQueryResultInstance().get(0).getSetSize();
						} else { 
							totalNum = masterInstanceResultResponse.getQueryResultInstance().get(0).getSetSize();
						}
						//update total_num column
						updatePStmt.setInt(1,totalNum);
						updatePStmt.setString(2,cFullName);
						updatePStmt.executeUpdate();
						
					}
				    
				    //delete the setfinder query
					log.debug("Delete query for master id [ " + masterInstanceResultResponse.getQueryMaster().getQueryMasterId() + " ]");
				    masterResponseType =CallCRCUtil.callDeleteMasterQuery(securityType.getUsername(), masterInstanceResultResponse.getQueryMaster().getQueryMasterId(), securityType, projectId);
				    log.debug("Deleted query for master id [ " + masterInstanceResultResponse.getQueryMaster().getQueryMasterId() + " ]");
					
					//update processed record count
					totalProcessedRecord++;
					if (totalProcessedRecord % 10 == 0) { 
						ontProcessStatusDao.updateStatus(processId, new Date(System
							.currentTimeMillis()), "PROCESSED "+ totalProcessedRecord +"/"+totalRecordToUpdate, "PROCESSING");
					}
					
				}
				if (killedFlag) {
					break;
				}
				resultSet.close();
				pStmt.close();
			}
			
			if (killedFlag == false) { 
				ontProcessStatusDao.updateStatus(processId, new Date(System
					.currentTimeMillis()), "PROCESSED "+ totalProcessedRecord +"/"+totalRecordToUpdate, "COMPLETED");
			} else { 
				ontProcessStatusDao.updateStatus(processId, new Date(System
						.currentTimeMillis()), "PROCESSED "+ totalProcessedRecord +"/"+totalRecordToUpdate, "KILLED");
			}
			return ontProcessStatusType;
		} catch (Throwable t) {
			t.printStackTrace();
			String stackTrace = StackTraceUtil.getStackTrace(t);
			if (ontProcessStatusDao != null && processId > 0) {
				ontProcessStatusDao.updateStatus(processId, new Date(System
						.currentTimeMillis()), "PROCESSED "+ totalProcessedRecord +"/"+totalRecordToUpdate, "ERROR");
				ontProcessStatusDao.updateStatusMessage(processId,
						stackTrace.substring(0, (stackTrace.length()>1900)?1900:stackTrace.length()));
			}
			throw new I2B2Exception(stackTrace);
		} finally {
			try {
				if (resultSet != null) { 
				
						resultSet.close();
					
				}
				if (conn != null) { 
					
						JDBCUtil.closeJdbcResource(null, pStmt, conn);
					
					JDBCUtil.closeJdbcResource(null, updatePStmt, null);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	private SecurityType getServiceAccountSecurityType(MessageHeaderType messageHeaderType) throws I2B2Exception {
		SecurityType securityType = new SecurityType();
		String serviceAccountUser = ontUtil.getServiceAccountUser(); 
		String serviceAccountPassword = ontUtil.getServiceAccountPassword();
		if (serviceAccountUser == null || serviceAccountPassword == null) { 
			throw new I2B2Exception("Service account user/password not set in ontology.properties file");
		}
		securityType.setUsername(serviceAccountUser);
		PasswordType password = new PasswordType(); 
		password.setValue(ontUtil.getServiceAccountPassword());
		securityType.setPassword(password);
		securityType.setDomain(messageHeaderType.getSecurity().getDomain());
		
		return securityType;
	}

	private String getProjectId(MessageHeaderType messageHeaderType) {
		return messageHeaderType.getProjectId();
	}

}
