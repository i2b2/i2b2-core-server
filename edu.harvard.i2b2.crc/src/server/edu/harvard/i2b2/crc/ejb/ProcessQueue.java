package edu.harvard.i2b2.crc.ejb;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.exception.StackTraceUtil;
import edu.harvard.i2b2.common.util.db.JDBCUtil;
import edu.harvard.i2b2.crc.dao.DAOFactoryHelper;
import edu.harvard.i2b2.crc.dao.DataSourceLookupDAO;
import edu.harvard.i2b2.crc.dao.DataSourceLookupDAOFactory;
import edu.harvard.i2b2.crc.dao.IDAOFactory;
import edu.harvard.i2b2.crc.dao.SetFinderDAOFactory;
import edu.harvard.i2b2.crc.dao.pdo.PdoTempTableUtil;
import edu.harvard.i2b2.crc.dao.setfinder.IQueryInstanceDao;
import edu.harvard.i2b2.crc.dao.setfinder.IQueryResultInstanceDao;
import edu.harvard.i2b2.crc.datavo.PSMFactory;
import edu.harvard.i2b2.crc.datavo.db.DataSourceLookup;
import edu.harvard.i2b2.crc.datavo.db.QtQueryInstance;
import edu.harvard.i2b2.crc.datavo.db.QtQueryResultInstance;
import edu.harvard.i2b2.crc.datavo.db.QtQueryResultType;
import edu.harvard.i2b2.crc.datavo.db.QtQueryStatusType;
import edu.harvard.i2b2.crc.datavo.db.StatusEnum;
import edu.harvard.i2b2.crc.datavo.setfinder.query.InstanceResultResponseType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryInstanceType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryResultInstanceType;
import edu.harvard.i2b2.crc.quartz.AnalysisQueue.QueueType;
import edu.harvard.i2b2.crc.util.QueryProcessorUtil;

public class ProcessQueue implements Runnable{
	private static Log log = LogFactory.getLog(ExecRunnable.class);

	public ProcessQueue(String lQueue) {

//		isActive = true;
		queue = lQueue;

	}

	private boolean jobCompleteFlag = false;

//	public boolean isActive() {
//		return isActive;
//	}

	private  String queue;
//	private boolean isActive = false;
	//	private boolean isRunning = false;
	public boolean isJobCompleteFlag() {
		return jobCompleteFlag;
	}
	
	private void initialize(){
	
	// initialize queue
	// If queued queries from a previous jboss session are in running state reset to queue state.
		QueryProcessorUtil qpUtil = QueryProcessorUtil.getInstance();
		DataSourceLookupDAO dsLookupDao = null;
		List<DataSourceLookup> dataSourceLookupList = null;
		try {
			dsLookupDao = DataSourceLookupDAOFactory.getDataSourceLookupDAO();
			 dataSourceLookupList = dsLookupDao
					.getDbLookupByHive("%");
		} catch (I2B2DAOException e1) {
			// TODO Auto-generated catch block
			log.error("Data source lookup failed: " + e1.getMessage());
		}

		
		Connection conn = null;
		PreparedStatement preparedStmt = null;
		for (DataSourceLookup dslookup: dataSourceLookupList)
		{		
			try {
				DataSource ds = qpUtil.getDataSource( dslookup.getDataSource());
				 conn = ds.getConnection();

				 String initializeSql = 
						 "update " + dslookup.getFullSchema() + ".qt_query_instance " +
								 " set batch_mode = '" + queue + "'" +
								 " where batch_mode = '" + queue + "_RUNNING' and end_date is null";
				 log.info("QUEUE START UP: " + initializeSql);
				 
				 preparedStmt = conn.prepareStatement(initializeSql);
				 preparedStmt.executeQuery();
					
			} catch (I2B2Exception e) {
				;
			} catch (SQLException e) {
				;
			} finally {
				try {
					if(preparedStmt != null)
						preparedStmt.close();
				} catch (SQLException e1) {
				}
				preparedStmt = null;
				
				try {
					if(conn != null)
						conn.close();
				} catch (SQLException e1) {
				}
				conn = null;
			}
		}
}

	public void run() {
		initialize();
		
		while(!Thread.interrupted())
			try {
				runQueues();
			}
		catch(Exception e) {
			log.error("Failed in " + queue );
			e.printStackTrace();
		}
	}

	public void runQueues() {

	//	log.debug("In ProcessQueue");
		QueryProcessorUtil qpUtil = QueryProcessorUtil.getInstance();

		int	transactionTimeout = this
				.readTimeoutPropertyValue(queue);


		int count = 0;
		try {

	//		log.debug("My DBserver is: " + qpUtil.getCRCDBLookupServerType());
	//		log.debug("My source is: " + qpUtil.getCRCDBLookupDataSource());
	//		log.debug("My schemaName is: " + qpUtil.getCRCDBLookupSchemaName());


			DataSourceLookupDAO dsLookupDao =  DataSourceLookupDAOFactory.getDataSourceLookupDAO();
	//		log.debug("My dslookupis " + dsLookupDao);
	//		log.debug("My dslookupis ds conn info is " + dsLookupDao.getDataSource().getConnection().toString());
			List<DataSourceLookup> dataSourceLookupList = dsLookupDao
					.getDbLookupByHive("%");

	//		log.debug("I found this number of datasources: " +dataSourceLookupList.size() );


			while (true)
			{

				String finalSql = "";
				String message = "";
				int queryInstanceId = 0;
				String ownerId = "";
				String sqlString = "";
				String projectId = "";
				String xmlRequest= "";
				String pmXml = "";
				Connection conn = null;
				PreparedStatement preparedStmt = null;
				ResultSet resultSet = null;

				for (DataSourceLookup dslookup: dataSourceLookupList)
				{
					try {

						log.debug("Connecting to: " +  dslookup.getDataSource());
						DataSource ds = qpUtil.getDataSource( dslookup.getDataSource());
						conn = ds.getConnection();

						finalSql = 
								"select qm.*, qi.query_instance_id, Row_number() " +
										"over ( " +
										"   PARTITION BY qm.QUERY_MASTER_ID " +
										"  ORDER BY qm.QUERY_MASTER_ID desc) AS new_id " +
										" from  " +dslookup.getFullSchema() + ".QT_QUERY_MASTER qm, " +
										dslookup.getFullSchema() + ".qt_query_instance qi " +
										" where qm.QUERY_MASTER_ID = qi.QUERY_master_id " +
										"  and qi.batch_mode = '" + queue +  "' " +
										" and qi.end_date is null ";
						if (dslookup.equals("SQLSERVER"))
							finalSql +=	" and  start_date < dateadd(minute, -3, getdate()) ";

						finalSql += " order by qi.start_date ";
						
						log.debug("Execut SQL [" + finalSql + "] for " + dslookup.getDataSource());
						preparedStmt = conn.prepareStatement(finalSql);

						//conn = dataSource.getConnection();
						queryInstanceId = 0;
						resultSet = preparedStmt.executeQuery();
						if (resultSet.next()) {
							queryInstanceId = resultSet.getInt("query_instance_id");
							ownerId = resultSet.getString("user_id");
							sqlString = resultSet.getString("generated_sql");
							projectId = resultSet.getString("group_id");
							xmlRequest = resultSet.getString("i2b2_request_xml");
							pmXml = resultSet.getString("pm_xml");
				//			log.info("ProcessQueue found instance: " + queryInstanceId + " for " + dslookup.getDataSource());
						}
						

						try {
							if(resultSet != null)
								resultSet.close();
						} catch (SQLException e1) {
						}
						resultSet = null;
						
						try {
							if(preparedStmt != null)
								preparedStmt.close();
						} catch (SQLException e1) {
						}
						preparedStmt = null;
						
						try {
							if(conn != null)
								conn.close();
						} catch (SQLException e1) {
						}
						conn = null;
	

						if (queryInstanceId != 0 && readTimeoutPropertyValue(queue) > 1) { 

							log.debug("in ProcessQueue my pmXml is"+ pmXml);
							ExecRunnable exec = new ExecRunnable(sqlString, Integer.toString(queryInstanceId), null,
									xmlRequest, dslookup.getDomainId(), projectId, ownerId, pmXml, transactionTimeout);
							log.info("STARTING " + queue + " FOR " + queryInstanceId + " " + sqlString + " " + transactionTimeout);
							
							Thread t = new Thread(exec);


							synchronized (t) {
								t.start();

								// add 10 sec to waitTime to allow query to finish/time
								int waitTime = (readTimeoutPropertyValue(queue) + 10 )* 1000;
							//	int waitTime = (readTimeoutPropertyValue(queue))* 1000;
								log.info("Waittime for " + queue+ " is  " + waitTime);

								try {

									long startTime = System.currentTimeMillis(); 
									long deltaTime = -1; 
									while((exec.isJobCompleteFlag() == false)&& (exec.isJobErrorFlag() == false) && (deltaTime < waitTime)){ 

										if (t.isAlive() == false)
											log.debug("PQ32 - Job Exception: " + exec.getJobException().getMessage());
										if (t.isInterrupted() == true)
											log.debug("PQ42 - Job Exception: " + exec.getJobException().getMessage());
										
										if (exec.isJobErrorFlag() == true)
											log.debug("PQ12 - Job Exception: " + exec.getJobException().getMessage());

										if (exec.getJobException() != null)
											log.debug("PQ22 - Job Exception: " + exec.getJobException().getMessage());
										if (waitTime > 0) { 
									//		log.info("In ExecRunnable Thread QueryId: " +  queryInstanceId + "  Waiting: " + (waitTime - deltaTime));
											deltaTime = System.currentTimeMillis() - startTime; 
										} 
									} 
									log.debug("Timeout or finish of Thread of queryid " + queryInstanceId  + " in" + queue);

									log.debug("Start queue waiting: " + startTime);
									log.debug("End queue waiting: " +  System.currentTimeMillis() );
									log.debug("Delta queue time: " + deltaTime);


									if (exec.isJobCompleteFlag() == false) {
										String timeOuterror = "Result waittime millisecond <result_waittime_ms> :" +
												waitTime +
												" elapsed, setting to next queue after " + queue;
										log.info(timeOuterror);

										setToNextQueue( dslookup,  projectId,  ownerId, queryInstanceId);


									} 
								}
								catch (Exception e) {
									
									log.error("Error in thread ProcessQueue: " + queue + " " + e.getMessage());
									if (e.getMessage().startsWith("javax.naming.NameNotFoundException"))
										break;

									setToNextQueue( dslookup,  projectId,  ownerId, queryInstanceId);

									//e.printStackTrace();

								} finally {
								//	log.debug("Process Queue (runQueus-106-" + queue );
								log.debug("process queue finally queue : " + queue + " queryInstanceId = " + queryInstanceId);
									if (t != null) {
										
								//		log.info("Process Queue (runQueus-108-" + queue );

									//	t.interrupt();
										t = null;
								//		log.info("Thread successfully stopped.");
									}
								//	log.info("Process Queue (runQueus-107-" + queue );

									queryInstanceId = 0;
								}



							}
						} else if (queryInstanceId != 0) {
							setToNextQueue( dslookup,  projectId,  ownerId, queryInstanceId);
						}
						Thread.sleep(10000);
					} catch (Exception e) {
					//	log.debug("Process Queue (runQueus-105-" + queue );

						try {
							if (conn != null)
								conn.close();
						} catch (SQLException e1) {
							;
						}

						
					} finally {
						
					//	log.debug("Process Queue (runQueus-104-" + queue );
						
						try {
							if(resultSet != null)
								resultSet.close();
						} catch (SQLException e1) {
						}
						resultSet = null;
						
						try {
							if(preparedStmt != null)
								preparedStmt.close();
						} catch (SQLException e1) {
						}
						preparedStmt = null;
						
						try {
							if(conn != null)
								conn.close();
						} catch (SQLException e1) {
						}
						conn = null;
	
					}
				//	log.info(queue + " - Current count: " + count);
					count++;

				}
			}


		} catch (Exception e) {
		
			//e.printStackTrace();
			log.debug("Process Queue (runQueus-103-" + queue  + " error: " + e.getMessage());

			//	isRunning = false;
			//throw new I2B2DAOException(
			//		"Error while calculating query count by set size"
			//				+ StackTraceUtil.getStackTrace(e));
		} 
	}

	private void setToNextQueue(DataSourceLookup dslookup, String projectId, String ownerId, int queryInstanceId) throws I2B2DAOException
	{
	//	log.info("In set to next queue");
		
		DAOFactoryHelper daoFactoryHelper = new DAOFactoryHelper(
				dslookup.getDomainId(), projectId, ownerId);

		IDAOFactory daoFactory = daoFactoryHelper.getDAOFactory();

		SetFinderDAOFactory sfDAOFactory = daoFactory
				.getSetFinderDAOFactory();

		// check if the status is cancelled
		IQueryInstanceDao queryInstanceDao = sfDAOFactory
				.getQueryInstanceDAO();
		QtQueryInstance queryInstance = queryInstanceDao
				.getQueryInstanceByInstanceId(Integer.toString(queryInstanceId));
		if (queryInstance.getBatchMode().equals(QueryManagerBeanUtil.ERROR))
		{
			return;
		}
		else if (queue.equals(QueryManagerBeanUtil.MEDIUM_QUEUE))
		{
			queryInstance.setBatchMode(QueryManagerBeanUtil.LARGE_QUEUE);
			log.debug("MEDIUM QUEUE timedout ... setting to LARGE_QUEUE");
		}
		else if (queue.equals(QueryManagerBeanUtil.LARGE_QUEUE))
		{
			log.debug("LARGE QUEUE timedout ... setting to NEVER_FINISHED");
			sfDAOFactory.getQueryResultTypeDao();
			queryInstance.setBatchMode("NEVER_FINISHED");
			QtQueryStatusType queryStatusType = queryInstance.getQtQueryStatusType();
			queryStatusType.setStatusTypeId(10);
			
			InstanceResultResponseType instanceResultResponseType = new InstanceResultResponseType();

			IQueryResultInstanceDao queryResultInstanceDao = sfDAOFactory
					.getPatientSetResultDAO();			
			List<QtQueryResultInstance> resultInstanceList = queryResultInstanceDao
					.getResultInstanceList(Integer.toString(queryInstanceId));
			
			// update cancelled status to all the result instance
			String resultInstanceId = "";
			int statusTypeId = 0;
			for (QtQueryResultInstance resultInstance : resultInstanceList) {
				resultInstanceId = resultInstance.getResultInstanceId();
				queryResultInstanceDao.updatePatientSet(resultInstanceId, 10, 
						 resultInstance.getMessage(), 0, 0, resultInstance.getObfuscateMethod());
						
			}
			queryInstance.setQtQueryStatusType(queryStatusType);
			
			queryInstance.setEndDate(new Date(System
					.currentTimeMillis()));
		}
		queryInstanceDao.update(queryInstance, false);

	}

	private int readTimeoutPropertyValue(String queueType) {
		QueryProcessorUtil qpUtil = QueryProcessorUtil.getInstance();
		String timeoutStr = "";
		int timeoutVal = 0;
		try {
			if (queueType.equals(QueryManagerBeanUtil.SMALL_QUEUE)) {
				timeoutStr = qpUtil
						.getCRCPropertyValue("edu.harvard.i2b2.crc.jms.small.timeoutsec");
			} else if (queueType.equals(QueryManagerBeanUtil.MEDIUM_QUEUE)) {
				timeoutStr = qpUtil
						.getCRCPropertyValue("edu.harvard.i2b2.crc.jms.medium.timeoutsec");
			} else if (queueType.equals(QueryManagerBeanUtil.LARGE_QUEUE)) {
				timeoutStr = qpUtil
						.getCRCPropertyValue("edu.harvard.i2b2.crc.jms.large.timeoutsec");
			}
			timeoutVal = Integer.parseInt(timeoutStr);

		} catch (I2B2Exception ex) {
			ex.printStackTrace();
		}
		return timeoutVal;

	}

}
