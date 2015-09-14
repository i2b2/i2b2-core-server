package edu.harvard.i2b2.crc.ejb;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

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
import edu.harvard.i2b2.crc.datavo.db.DataSourceLookup;
import edu.harvard.i2b2.crc.datavo.db.QtQueryInstance;
import edu.harvard.i2b2.crc.quartz.AnalysisQueue.QueueType;
import edu.harvard.i2b2.crc.util.QueryProcessorUtil;

public class ProcessQueue extends Thread{
	private static Log log = LogFactory.getLog(ExecRunnable.class);

	public ProcessQueue(String lQueue) {

		isActive = true;
		queue = lQueue;
		setDaemon(true) ;   

	}

	private boolean jobCompleteFlag = false;

	public boolean isActive() {
		return isActive;
	}

	private  String queue;
	private boolean isActive = false;
	private boolean isRunning = false;
	public boolean isJobCompleteFlag() {
		return jobCompleteFlag;
	}


	public void run() {

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


				log.debug("Running is " + isRunning);

				if (!isRunning) {
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

							if (queryInstanceId != 0 && (!isRunning)) {
								isRunning = true;
								log.debug("Running is " + isRunning);
								log.info("Working on in " + queryInstanceId + " in " + QueryManagerBeanUtil.MEDIUM_QUEUE);




								//	ExecRunnable exec = new ExecRunnable(transactionTimeout,
								//			QueryManagerBeanUtil.MEDIUM_QUEUE, null, Integer.toString(sessionId));

								//	String sqlString, String queryInstanceId, String patientSetId ,
								//	String xmlRequest, String dsLookupDomainId, String dsLookupProjectId ,
								//	String dsLookupOwnerId

								log.debug("in ProcessQueue my pmXml is"+ pmXml);
								ExecRunnable exec = new ExecRunnable(sqlString, Integer.toString(queryInstanceId), null,
										xmlRequest, dslookup.getDomainId(), projectId, ownerId, pmXml);

								Thread t = new Thread(exec);

								int waitTime = readTimeoutPropertyValue(queue) * 1000;

								log.info("Waittime for " + queue+ " is  " + waitTime);

								synchronized (t) {
									t.start();

									try {
										//if (waitTime > 0) {
										//	t.wait(waitTime);
										//} else {
										//	t.wait();
										//}

										long startTime = System.currentTimeMillis(); 
										long deltaTime = -1; 
										while((exec.isJobCompleteFlag() == false)&& (deltaTime < waitTime)){ 
											if (waitTime > 0) { 
												log.debug("Waiting for: " + (waitTime - deltaTime));
												t.wait(waitTime - deltaTime); 
												deltaTime = System.currentTimeMillis() - startTime; 
											} else { 
												t.wait(); 
											} 
										} 
										log.info("Finished on in " + queryInstanceId );

										log.debug("Start waiting: " + startTime);
										log.debug("End waiting: " +  System.currentTimeMillis() );
										log.debug("Delta time: " + deltaTime);


										if (exec.isJobCompleteFlag() == false) {
											String timeOuterror = "Result waittime millisecond <result_waittime_ms> :" +
													waitTime +
													" elapsed, setting to next queue";
											log.debug(timeOuterror);

											//DataSourceLookup dsLookup = sfDAOFactory.getDataSourceLookup();



											log.debug("Set to LARGE Queue");
											//throw new Exception("Timed Out, setting to LARGE Queue");

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

											if (queue.equals(QueryManagerBeanUtil.MEDIUM_QUEUE))
											{
												queryInstance.setBatchMode(QueryManagerBeanUtil.LARGE_QUEUE);
											}
											else if (queue.equals(QueryManagerBeanUtil.LARGE_QUEUE))
											{
												queryInstance.setBatchMode("NO_MORE_QUEUE");

												queryInstance.setEndDate(new Date(System
														.currentTimeMillis()));
											}
											queryInstanceDao.update(queryInstance, false);										

										} 
									}
									catch (InterruptedException e) {
										isRunning = false;
										log.error("Error in thread ProcessQueue: " + e.getMessage());
										if (e.getMessage().startsWith("javax.naming.NameNotFoundException"))
											break;
						
										log.error("Error in thread ProcessQueue: " + e.getMessage());
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

										if (queue.equals(QueryManagerBeanUtil.MEDIUM_QUEUE))
										{
											queryInstance.setBatchMode(QueryManagerBeanUtil.LARGE_QUEUE);
										}
										else if (queue.equals(QueryManagerBeanUtil.LARGE_QUEUE))
										{
											queryInstance.setBatchMode("NO_MORE_QUEUE");

											queryInstance.setEndDate(new Date(System
													.currentTimeMillis()));
										}
										queryInstanceDao.update(queryInstance, false);
										e.printStackTrace();

									} finally {
										//t.interrupt();
										//exec = null;
										t = null;
										isRunning = false;
									}
								}
							}
							Thread.sleep(10000);
						} catch (Exception e) {
							try {
								isRunning = false;
								if (conn != null)
									conn.close();
							} catch (SQLException e1) {
								// TODO Auto-generated catch block
								isRunning = false;
								e1.printStackTrace();
							}

							// TODO Auto-generated catch block
							e.printStackTrace();
						} finally {
							isRunning = false;
							if (conn != null)
								conn.close();
						}

						log.debug(queue + " - Current count: " + count);
						count++;

					}
				}

				//				DataSource ds = qpUtil.getDataSource( qpUtil.getCRCDBLookupDataSource());
				//	conn = dsLookupDao.getDataSource().getConnection();
				//ds.getConnection();

				//Get all projects

				//conn = dataSource.getConnection();

				//	log.debug("getting list of datasources");
				//	ResultSet resultSet = preparedStmt.executeQuery();
				//	if (resultSet.next()) {
				//		log.debug("found datasource: " + resultSet.getString("c_db_datasource"));
				//	}

			}


			//			qpUtil.
		} catch (SQLException e) {
			e.printStackTrace();
			isRunning = false;
			//throw new I2B2DAOException(
			//		"Error while calculating query count by set size"
			//				+ StackTraceUtil.getStackTrace(e));
		} catch (I2B2Exception e) {
			isRunning = false;
			e.printStackTrace();
		} 
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
