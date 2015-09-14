package edu.harvard.i2b2.crc.quartz;

import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.exception.StackTraceUtil;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.crc.dao.DAOFactoryHelper;
import edu.harvard.i2b2.crc.dao.DataSourceLookupDAOFactory;
import edu.harvard.i2b2.crc.dao.ICRCQueueDAO;
import edu.harvard.i2b2.crc.dao.IDAOFactory;
import edu.harvard.i2b2.crc.dao.SetFinderDAOFactory;
import edu.harvard.i2b2.crc.dao.setfinder.IAnalysisPluginDao;
import edu.harvard.i2b2.crc.dao.setfinder.IQueryInstanceDao;
import edu.harvard.i2b2.crc.dao.setfinder.IQueryMasterDao;
import edu.harvard.i2b2.crc.datavo.db.AnalysisJob;
import edu.harvard.i2b2.crc.datavo.db.QtAnalysisPlugin;
import edu.harvard.i2b2.crc.datavo.db.QtQueryInstance;
import edu.harvard.i2b2.crc.datavo.db.QtQueryMaster;
import edu.harvard.i2b2.crc.datavo.db.StatusEnum;
import edu.harvard.i2b2.crc.datavo.setfinder.query.AnalysisDefinitionType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.ResultOutputOptionListType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.UserType;
import edu.harvard.i2b2.crc.ejb.analysis.QueryInstance;
import edu.harvard.i2b2.crc.ejb.analysis.QueryMaster;
import edu.harvard.i2b2.crc.exec.ExecException;
import edu.harvard.i2b2.crc.exec.ExecUtil;
import edu.harvard.i2b2.crc.quartz.AnalysisQueue.QueueType;
import edu.harvard.i2b2.crc.util.I2B2RequestMessageHelper;

public class AnalysisJobStarter {
	protected static final Log log = LogFactory
			.getLog(AnalysisJobStarter.class);
	IDAOFactory daoFactory;
	String projectId;
	String userId;
	String domainId;

	public AnalysisJobStarter(String domainId, String projectId, String userId)
			throws I2B2DAOException {
		System.out.println("**************" + domainId + projectId + userId);
		this.projectId = projectId;
		this.userId = userId;
		this.domainId = domainId;
		DAOFactoryHelper factoryHelper = new DAOFactoryHelper(domainId,
				projectId, userId);
		this.daoFactory = factoryHelper.getDAOFactory();
	}

	public AnalysisJobStarter(IDAOFactory daoFactory) {
		this.daoFactory = daoFactory;
	}

	public void start(String instanceId, long timeout) throws I2B2Exception {

		// get query definition
		SetFinderDAOFactory setfinderFactory = daoFactory
				.getSetFinderDAOFactory();
		IQueryInstanceDao queryInstanceDao = setfinderFactory
				.getQueryInstanceDAO();
		QtQueryInstance queryInstance = queryInstanceDao
				.getQueryInstanceByInstanceId(instanceId);
		String masterId = queryInstance.getQtQueryMaster().getQueryMasterId();
		IQueryMasterDao queryMasterDao = setfinderFactory.getQueryMasterDAO();
		IAnalysisPluginDao analysisPluginDao = setfinderFactory
				.getAnalysisPluginDao();
		QtQueryMaster queryMaster = queryMasterDao.getQueryDefinition(masterId);
		ICRCQueueDAO crcQueueDao = null;
		List<AnalysisJob> analysisJobList = null;
		boolean timeoutFlag = false, errorFlag = false;
		String statusMsg = null;
		Throwable individualEx = null;
		// get command line
		try {
			crcQueueDao = DataSourceLookupDAOFactory.getCRCQueueDAO();
			analysisJobList = crcQueueDao.getJob(instanceId, projectId);

			AnalysisDefinitionType analysisDefType = I2B2RequestMessageHelper
					.getAnalysisDefinitionFromXml(queryMaster.getRequestXml());
			String analysisName = analysisDefType.getAnalysisPluginName();
			String version = analysisDefType.getVersion();
			log.debug("Looking up Analysis plugin by name[" + analysisName
					+ "] version [" + version + "] and project [" + projectId
					+ "]");
			QtAnalysisPlugin analysisPlugin = analysisPluginDao
					.lookupAnalysisPluginByNameVersionProject(analysisName,
							version, projectId);
			String commandLine = analysisPlugin.getCommandLine();
			String workingFolder = analysisPlugin.getWorkingFolder();

			// add domain, project and user id tocommand line
			commandLine += " -project_id=" + projectId + " -domain_id="
					+ domainId + " -user_id=" + userId + " -instance_id="
					+ instanceId;
			// call exec util with command line and timeout
			ExecUtil execUtil = new ExecUtil();
			execUtil.execute(workingFolder, commandLine, timeout);

		} catch (JAXBUtilException e) {
			errorFlag = true;
			statusMsg = e.getMessage();
			individualEx = e;
			e.printStackTrace();
		} catch (I2B2DAOException e) {
			errorFlag = true;
			statusMsg = e.getMessage();
			individualEx = e;
		} catch (ExecException e) {
			errorFlag = true;
			statusMsg = e.getMessage();
			individualEx = e;
			if (e.getExitStatus().equals(ExecException.TIMEOUT_STATUS)) {
				timeoutFlag = true;
			}
		} catch (Throwable throwable) {
			errorFlag = true;
			statusMsg = throwable.getMessage();
			individualEx = throwable;
		} finally {
			String statusType = "";
			String stacktrace = "";
			if (errorFlag) {
				statusType = StatusEnum.ERROR.toString();
				stacktrace = StackTraceUtil.getStackTrace(individualEx);
				if (stacktrace != null) {
					if (stacktrace.length() > 2000) {
						stacktrace = stacktrace.substring(0, 1998);
					} else {
						stacktrace = stacktrace.substring(0, stacktrace
								.length());
					}
				}
			} else {
				statusType = StatusEnum.COMPLETED.toString();
			}
			try {

				// update status to query instance
				QueryInstance queryInstanceHelper = new QueryInstance(
						daoFactory.getSetFinderDAOFactory());

				queryInstanceHelper.updateInstanceStatus(instanceId,
						statusType, stacktrace);
				// update result instance
				queryInstanceHelper.updateResultInstanceStatusByInstanceId(
						instanceId, statusType, 0, stacktrace);

				// update status to analysis job table in the hive if the queue
				// name is medium or large

				crcQueueDao = DataSourceLookupDAOFactory.getCRCQueueDAO();
				crcQueueDao.updateStatus(instanceId, projectId, statusType);

				if (timeoutFlag) {
					AnalysisJob analysisJob = null;
					AnalysisQueue.QueueType jobQueue = AnalysisQueue.QueueType.FILLER;
					if (analysisJobList.size() > 0) {
						analysisJob = analysisJobList.get(0);
						jobQueue = AnalysisQueue.QueueType.valueOf(analysisJob
								.getQueueName());

					}
					// if not large queue, then
					// create query instance and query result instance and
					// create to entry to crc queue
					if (jobQueue.compareTo(QueueType.LARGE_QUEUE) < 0) {
						AnalysisQueue.QueueType newQueue = AnalysisQueue
								.getNextQueue(jobQueue.toString());

						UserType userType = new UserType();
						userType.setGroup(projectId);
						userType.setLogin(userId);

						// update status to query instance
						QueryMaster queryMasterHelper = new QueryMaster(
								daoFactory.getSetFinderDAOFactory());
						AnalysisDefinitionType analysisDefType;
						String newQueryInstanceId = null;
						try {
							analysisDefType = queryMasterHelper
									.getAnalysisDefinitionByMasterId(masterId);
							ResultOutputOptionListType resultList = I2B2RequestMessageHelper
									.buildResultOptionListFromAnalysisResultList(analysisDefType
											.getCrcAnalysisResultList());
							newQueryInstanceId = queryInstanceHelper
									.saveInstanceAndResultInstance(masterId,
											userType, newQueue.toString(),
											resultList);
						} catch (JAXBUtilException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						analysisJob = new AnalysisJob();
						analysisJob.setJobId(newQueryInstanceId);
						analysisJob.setDomainId(domainId);
						analysisJob.setUserId(userId);
						analysisJob.setProjectId(projectId);
						analysisJob.setCreateDate(new Date(System
								.currentTimeMillis()));
						analysisJob
								.setStatusTypeId(StatusEnum.QUEUED.ordinal());
						analysisJob.setQueueName(newQueue.toString());
						crcQueueDao.addJob(analysisJob);

					}

				}

			} catch (I2B2DAOException e) {
				throw new I2B2Exception("Error : [" + statusMsg + "]"
						+ "unable to update instance table");
			}
			// if status is timeout, then update the
		}

	}
}
