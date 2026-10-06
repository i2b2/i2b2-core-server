package edu.harvard.i2b2.crc.delegate.quartz;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.datatype.XMLGregorianCalendar;

import edu.harvard.i2b2.common.util.jaxb.DTOFactory;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.crc.dao.DAOFactoryHelper;
import edu.harvard.i2b2.crc.dao.SetFinderDAOFactory;
import edu.harvard.i2b2.crc.dao.setfinder.IQueryBreakdownTypeDao;
import edu.harvard.i2b2.crc.dao.setfinder.IQueryMasterDao;
import edu.harvard.i2b2.crc.dao.setfinder.IResultGenerator;
import edu.harvard.i2b2.crc.dao.setfinder.SetFinderConnection;
import edu.harvard.i2b2.crc.datavo.db.DataSourceLookup;
import edu.harvard.i2b2.crc.datavo.db.QtQueryBreakdownType;
import edu.harvard.i2b2.crc.datavo.db.QtQueryMaster;
import edu.harvard.i2b2.crc.datavo.i2b2message.SecurityType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.FindByChildType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.InstanceResultResponseType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.JobType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.JobsType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.MasterResponseType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.ParamType;
import edu.harvard.i2b2.crc.delegate.RequestHandlerDelegate;
import edu.harvard.i2b2.crc.ejb.QueryInfoBean;

import org.quartz.DateBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;

import static org.quartz.DateBuilder.futureDate;
import static org.quartz.JobBuilder.newJob;


@Service
public class SchedulerInfoBean {



	private static Log log = LogFactory.getLog(QueryInfoBean.class);
	protected static Log logesapi = LogFactory.getLog(QueryInfoBean.class);


	public  JobsType getAllJobsScheduler(Scheduler scheduler,
			DataSourceLookup dataSourceLookup, SecurityType userRequestType)
					throws I2B2Exception {
		//String groupId = userRequestType.getGroupId();
		//int fetchSize = userRequestType.getFetchSize();

		DAOFactoryHelper helper = new DAOFactoryHelper(dataSourceLookup.getDomainId(), dataSourceLookup.getProjectPath(),
				dataSourceLookup.getProjectPath());
		SetFinderDAOFactory sfDAOFactory = helper.getDAOFactory()
				.getSetFinderDAOFactory();

		 JobsType jobs =  new  JobsType();

		try {

			for (String groupName : scheduler.getJobGroupNames()) {

				for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {
					JobType job = new JobType();
					job.setName(jobKey.getName());
					job.setGroup(jobKey.getGroup());
					List<Trigger> triggers = (List<Trigger>) scheduler.getTriggersOfJob(jobKey);
					Date nextFireTime = triggers.get(0).getNextFireTime();

					DTOFactory factory = new DTOFactory();
					job.setStartTime(factory.getXMLGregorianCalendar(nextFireTime.getTime()));

					jobs.getJob().add(job);

					String jobName = jobKey.getName();
					String jobGroup = jobKey.getGroup();
					//get job's trigger
					System.out.println("[jobName] : " + jobName + " [groupName] : " + jobGroup + " - " + nextFireTime);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		} 

		//return null;
		/*
		IQueryMasterDao queryMasterDao = sfDAOFactory.getQueryMasterDAO();
		List<QtQueryMaster> masterList = queryMasterDao
				.getQueryMasterByNameInfo(userRequestType, jobsType);
		MasterResponseType masterResponseType = buildMasterResponseType(masterList);
		 */

		return jobs;

	}


	public MasterResponseType setScheduler(Scheduler scheduler,
			DataSourceLookup dataSourceLookup, SecurityType userRequestType, JobType setJobRequestType)
					throws I2B2Exception {
		//String groupId = userRequestType.getGroupId();
		//int fetchSize = userRequestType.getFetchSize();
		try {
			DAOFactoryHelper helper = new DAOFactoryHelper(dataSourceLookup.getDomainId(), dataSourceLookup.getProjectPath(),
					dataSourceLookup.getProjectPath());
			SetFinderDAOFactory sfDAOFactory = helper.getDAOFactory()
					.getSetFinderDAOFactory();



			//Map param = new HashMap();

			JobDataMap jobData = new JobDataMap();

			log.debug("Creatiung hash map");
			jobData.put("SetFinderDAOFactory", sfDAOFactory);
			SetFinderConnection sfConn = new SetFinderConnection(sfDAOFactory.getDataSource().getConnection());
			jobData.put("SetFinderConnection", sfConn);
			jobData.put("SecurityType", userRequestType);

			for (ParamType param: setJobRequestType.getParam())
			{
				jobData.put(param.getName(), param.getValue());
			}

			/*
			SetFinderConnection sfConn = new SetFinderConnection(manualConnection);
			param.put("SetFinderConnection", sfConn);
			param.put("PatientSetId", patientSetId);
			param.put("QueryInstanceId", queryInstanceId);
			param.put("TEMP_DX_TABLE", TEMP_DX_TABLE);
			param.put("setFinderResultOntologyKeyMap", ontologyKeyMap);
			param.put("ServerType", this.dataSourceLookup.getServerType());
			//		param.put("CallOntologyUtil", callOntologyUtil);
			param.put("projectId", projectId);
			param.put("ontologyGetChildrenUrl", ontologyGetChildrenUrl);		
			param.put("securityType", securityType);
			param.put("OriginalDataSourceLookup", this.originalDataSourceLookup);
			param.put("Roles", roles);
			param.put("TransactionTimeout", transactionTimeout);
			param.put("ProcessTimingFlag", processTimingFlag);
			param.put("ObfuscatedRecordCount", obfuscatedRecordCount);
			param.put("RecordCount", recordCount);
			param.put("ObfuscatedRoleFlag", dataObfuscFlag);
			param.put("panelList", panelList);
			param.put("queryDef", queryDef);
			 */
			//
			//runGenerator(resultName, param, queryBreakdownType.getClassname());




			IQueryBreakdownTypeDao queryBreakdownTypeDao = sfDAOFactory
					.getQueryBreakdownTypeDao();
			QtQueryBreakdownType queryBreakdownType = queryBreakdownTypeDao
					.getBreakdownTypeByName(setJobRequestType.getName());


			LocalDateTime dateObj = LocalDateTime.now();
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
			String date = "_" + dateObj.format(formatter);


			IResultGenerator resultGenerator;
			Class	generatorClass = Class.forName(queryBreakdownType.getClassname(), true, Thread
					.currentThread().getContextClassLoader());
			//JobDetail job2 = JobBuilder.newJob((Class<? extends Job>) SimpleJob.class);
			JobDetail job = newJob( generatorClass)
					.usingJobData(jobData)
					.withIdentity(setJobRequestType.getName()+date, setJobRequestType.getGroup()) 
					.build();

			Date runTime = setJobRequestType.getStartTime().toGregorianCalendar().getTime();



			// Trigger the job to run now, and then every 40 seconds
			Trigger trigger = TriggerBuilder.newTrigger()
					.withIdentity(setJobRequestType.getName()+date, setJobRequestType.getGroup())
					.startAt(runTime)
					//.withSchedule(simpleSchedule()
					//        .withIntervalInMinutes(5))
					//.startNow()
					.build();
			//.withSchedule(simpleSchedule()
			//	          .withIntervalInHours(3)
			//NOTE: check to see if can have multiple withinterval

			scheduler.scheduleJob(job, trigger);
			scheduler.start();

		} catch (Exception e) {
			e.printStackTrace();
		} 


		return null;
		/*
		IQueryMasterDao queryMasterDao = sfDaoFactory.getQueryMasterDAO();
		List<QtQueryMaster> masterList = queryMasterDao
				.getQueryMasterByNameInfo(userRequestType, findChildType);
		MasterResponseType masterResponseType = buildMasterResponseType(masterList);
		return masterResponseType;
		 */
	}




}
