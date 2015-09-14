package edu.harvard.i2b2.crc.ejb.analysis;

import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanFactory;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.crc.dao.SetFinderDAOFactory;
import edu.harvard.i2b2.crc.dao.setfinder.IQueryInstanceDao;
import edu.harvard.i2b2.crc.dao.setfinder.IQueryResultInstanceDao;
import edu.harvard.i2b2.crc.datavo.db.QtQueryInstance;
import edu.harvard.i2b2.crc.datavo.db.QtQueryResultInstance;
import edu.harvard.i2b2.crc.datavo.db.QtQueryStatusType;
import edu.harvard.i2b2.crc.datavo.db.StatusEnum;
import edu.harvard.i2b2.crc.datavo.setfinder.query.ResultOutputOptionListType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.ResultOutputOptionType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.UserType;
import edu.harvard.i2b2.crc.util.QueryProcessorUtil;

public class QueryInstance {

	private static Log log = LogFactory.getLog(QueryInstance.class);

	SetFinderDAOFactory sfDAOFactory = null;

	public QueryInstance(SetFinderDAOFactory sfDAOFactory) {
		this.sfDAOFactory = sfDAOFactory;
	}

	public String saveInstanceAndResultInstance(String queryMasterId,
			UserType userType, String queueName,
			ResultOutputOptionListType resultOptionList)
			throws I2B2DAOException {
		// create query instance
		IQueryInstanceDao queryInstanceDao = sfDAOFactory.getQueryInstanceDAO();
		// UserType userType = getUserTypeFromSetfinderHeader(xmlRequest);
		String userId = userType.getLogin();
		String groupId = userType.getGroup();
		String queryInstanceId = queryInstanceDao.createQueryInstance(
				queryMasterId, userId, groupId, queueName, StatusEnum.QUEUED
						.ordinal());
		log.debug("New Query instance id " + queryInstanceId);

		IQueryResultInstanceDao patientSetResultDao = sfDAOFactory
				.getPatientSetResultDAO();
		String patientSetId = null;
		if (resultOptionList != null
				&& resultOptionList.getResultOutput() != null
				&& resultOptionList.getResultOutput().size() > 0) {

			for (ResultOutputOptionType resultOption : resultOptionList
					.getResultOutput()) {
				log.debug("Patient result ID [" + resultOption.getName()
						+ "] for query instance= " + queryInstanceId);
				patientSetId = patientSetResultDao.createPatientSet(
						queryInstanceId, resultOption.getName());
				log.debug("Patient Set ID [" + patientSetId
						+ "] for query instance= " + queryInstanceId);
			}
		} else {
			QueryProcessorUtil qp = QueryProcessorUtil.getInstance();
			BeanFactory bf = qp.getSpringBeanFactory();
			String defaultResultType = (String) bf
					.getBean(QueryProcessorUtil.DEFAULT_SETFINDER_RESULT_BEANNAME);
			patientSetId = patientSetResultDao.createPatientSet(
					queryInstanceId, defaultResultType);
			log.debug("Patient Set ID [" + patientSetId
					+ "] for query instance= " + queryInstanceId);
		}
		return queryInstanceId;
	}

	public void updateInstanceStatus(String queryInstanceId, String statusType,
			String message) throws I2B2DAOException {
		// get query instance
		// update the status
		IQueryInstanceDao queryInstanceDao = sfDAOFactory.getQueryInstanceDAO();
		QtQueryInstance qtQueryInstance = queryInstanceDao
				.getQueryInstanceByInstanceId(queryInstanceId);
		qtQueryInstance.setEndDate(new Date(System.currentTimeMillis()));
		qtQueryInstance.setMessage(message);
		QtQueryStatusType qtQueryStatusType = new QtQueryStatusType();
		qtQueryStatusType.setStatusTypeId(StatusEnum.valueOf(statusType)
				.ordinal());
		qtQueryInstance.setQtQueryStatusType(qtQueryStatusType);
		queryInstanceDao.update(qtQueryInstance, true);

	}

	public void updateResultInstanceStatusByInstanceId(String queryInstanceId,
			String statusType, int setSize, String statusMsg) {
		if (statusMsg == null) {
			statusMsg = "";
		}
		IQueryResultInstanceDao patientSetResultDao = sfDAOFactory
				.getPatientSetResultDAO();
		List<QtQueryResultInstance> resultInstanceList = patientSetResultDao
				.getResultInstanceList(queryInstanceId);

		// iterate the list and update the status
		for (QtQueryResultInstance singleResultInstance : resultInstanceList) {
			updateResultInstanceStatus(singleResultInstance
					.getResultInstanceId(), StatusEnum.valueOf(statusType)
					.ordinal(), statusMsg, setSize);
		}
	}

	public void updateResultInstanceStatus(String resultInstanceId,
			int statusTypeId, String statusMsg, int setSize) {
		// get query result instance
		// update result instance status
		IQueryResultInstanceDao patientSetResultDao = sfDAOFactory
				.getPatientSetResultDAO();
		patientSetResultDao.updatePatientSet(resultInstanceId, statusTypeId,
				statusMsg, setSize, 0, "");

	}

}
