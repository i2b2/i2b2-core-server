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

		isActive = true;
		queue = lQueue;
		//setDaemon(true) ;   

	}

	private boolean jobCompleteFlag = false;

	public boolean isActive() {
		return isActive;
	}

	private  String queue;
	private boolean isActive = false;
	//	private boolean isRunning = false;
	public boolean isJobCompleteFlag() {
		return jobCompleteFlag;
	}

	public void run() {
		  log.info(queue + " Going into  while look in ProcessQueue");

		
		  while(!Thread.interrupted())
		      try {
		    	  runQueues();
		      }
		      catch(Exception e) {
		    	  log.error("Failed in " + queue );
		          e.printStackTrace();
		      }
		  log.info(queue + " Out of while look in ProcessQueue");
	}

	public void runQueues() {

		log.debug("In ProcessQueue");
		QueryProcessorUtil qpUtil = QueryProcessorUtil.getInstance();

		int				 transactionTimeout = this
				.readTimeoutPropertyValue(queue);


		int count = 0;
		try {

			log.debug("My DBserver is: " + qpUtil.getCRCDBLookupServerType());
			log.debug("My source is: " + qpUtil.getCRCDBLookupDataSource());
			log.debug("My schemaName is: " + qpUtil.getCRCDBLookupSchemaName());


			DataSourceLookupDAO dsLookupDao =  DataSourceLookupDAOFactory.getDataSourceLookupDAO();
			log.debug("My dslookupis " + dsLookupDao);
			log.debug("My dslookupis ds conn info is " + dsLookupDao.getDataSource().getConnection().toString());
			List<DataSourceLookup> dataSourceLookupList = dsLookupDao
					.getDbLookupByHive("%");

			log.debug("I found this number of datasources: " +dataSourceLookupList.size() );


			while (true)
			{
				//get list of available process to run


				//				log.debug("Running is " + isRunning);

				//				if (!isRunning) {
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
				//getDataSourceLookupDAO();

				//conn = qpUtil.getConnection();

				for (DataSourceLookup dslookup: dataSourceLookupList)
				{
					
					
					try {

						log.debug("Connecting to: " +  dslookup.getDataSource());
						DataSource ds = qpUtil.getDataSource( dslookup.getDataSource());
						conn = ds.getConnection();
						
						//set any running queus back to in queue
						finalSql = "update " + dslookup.getFullSchema() + ".qt_query_instance  set batch_mode = '" + queue +  "' " +
						" where  batch_mode = '" + queue +  "_RUNNING' and end_date is null ";
						//conn.
						

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
						//else
						//	finalSql += " and "

						finalSql += " order by qi.start_date ";



						log.debug("Execut SQL [" + finalSql + "]");
						preparedStmt = conn.prepareStatement(finalSql);

						//conn = dataSource.getConnection();
						queryInstanceId = 0;
						ResultSet resultSet = preparedStmt.executeQuery();
						if (resultSet.next()) {
							queryInstanceId = resultSet.getInt("query_instance_id");
							ownerId = resultSet.getString("user_id");
							sqlString = resultSet.getString("generated_sql");
							projectId = resultSet.getString("group_id");
							xmlRequest = resultSet.getString("i2b2_request_xml");
							pmXml = resultSet.getString("pm_xml");
						}

						conn.close();
						conn = null;

						if (queryInstanceId != 0 && readTimeoutPropertyValue(queue) > 1) { // && (!isRunning)) {
							//						isRunning = true;
							//						log.debug("Running is " + isRunning);
							log.info("Working on in " + queryInstanceId + " in " + queue);


							log.debug("in ProcessQueue my pmXml is"+ pmXml);
							ExecRunnable exec = new ExecRunnable(sqlString, Integer.toString(queryInstanceId), null,
									xmlRequest, dslookup.getDomainId(), projectId, ownerId, pmXml);

							Thread t = new Thread(exec);


							synchronized (t) {
								t.start();

								int waitTime = readTimeoutPropertyValue(queue) * 1000;

								log.info("Waittime for " + queue+ " is  " + waitTime);



								try {

									long startTime = System.currentTimeMillis(); 
									long deltaTime = -1; 
									while((exec.isJobCompleteFlag() == false)&& (exec.isJobErrorFlag() == false) && (deltaTime < waitTime)){ 

										if (t.isAlive() == false)
											log.debug("32 - Job Exception: " + exec.getJobException().getMessage());
										if (t.isInterrupted() == true)
											log.debug("42 - Job Exception: " + exec.getJobException().getMessage());
										
										if (exec.isJobErrorFlag() == true)
											log.debug("12 - Job Exception: " + exec.getJobException().getMessage());

										if (exec.getJobException() != null)
											log.debug("22 - Job Exception: " + exec.getJobException().getMessage());
										if (waitTime > 0) { 
											//log.info("In ExecRunnable Thread QueryId: " +  queryInstanceId + "  Waiting: " + (waitTime - deltaTime));
											//t.wait(waitTime - deltaTime); 
											deltaTime = System.currentTimeMillis() - startTime; 
										} //else { 
										//t.wait(); 
										//} 
									} 
									log.info("Finished Thread of queryid " + queryInstanceId  + " in" + queue);

									log.debug("Start waiting: " + startTime);
									log.debug("End waiting: " +  System.currentTimeMillis() );
									log.debug("Delta time: " + deltaTime);


									if (exec.isJobCompleteFlag() == false) {
										String timeOuterror = "Result waittime millisecond <result_waittime_ms> :" +
												waitTime +
												" elapsed, setting to next queue";
										log.debug(timeOuterror);

										//DataSourceLookup dsLookup = sfDAOFactory.getDataSourceLookup();



										log.debug("Set to Next Queue after " + queue);

										setToNextQueue( dslookup,  projectId,  ownerId, queryInstanceId);


									} 
								}
								catch (Exception e) {
									//				isRunning = false;
									log.debug("Error in thread ProcessQueue: " + e.getMessage());
									if (e.getMessage().startsWith("javax.naming.NameNotFoundException"))
										break;

									log.error("Error in thread ProcessQueue: " + e.getMessage());
									setToNextQueue( dslookup,  projectId,  ownerId, queryInstanceId);

									e.printStackTrace();

								} finally {
									log.debug("Process Queue (runQueus-106-" + queue );

									if (t != null) {
										//exec.terminate();
										//t.join();
										log.debug("Process Queue (runQueus-108-" + queue );

										t.interrupt();
										log.debug("Thread successfully stopped.");
									}
									log.debug("Process Queue (runQueus-107-" + queue );

									//t.interrupt();
									//exec = null;
									//t = null;
									//								isRunning = false;
									queryInstanceId = 0;
								}



							}
						} else if (queryInstanceId != 0) {
							setToNextQueue( dslookup,  projectId,  ownerId, queryInstanceId);
						}
						Thread.sleep(10000);
					} catch (Exception e) {
						log.debug("Process Queue (runQueus-105-" + queue );

						try {
							//							isRunning = false;
							if (conn != null)
								conn.close();
						} catch (SQLException e1) {
							// TODO Auto-generated catch block
							//							isRunning = false;
							e1.printStackTrace();
						}

							// TODO Auto-generated catch block
					} finally {
						//						isRunning = false;
						log.debug("Process Queue (runQueus-104-" + queue );

						if (conn != null)
							conn.close();
	
					}

					log.debug(queue + " - Current count: " + count);
					count++;

				}
			}


		} catch (Exception e) {
			//e.printStackTrace();
			log.info("Process Queue (runQueus-103-" + queue  + " error: " + e.getMessage());

			//	isRunning = false;
			//throw new I2B2DAOException(
			//		"Error while calculating query count by set size"
			//				+ StackTraceUtil.getStackTrace(e));
		} 
	}

	private void setToNextQueue(DataSourceLookup dslookup, String projectId, String ownerId, int queryInstanceId) throws I2B2DAOException
	{
		
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
		}
		else if (queue.equals(QueryManagerBeanUtil.LARGE_QUEUE))
		{
			
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
