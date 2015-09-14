package edu.harvard.i2b2.crc.dao.setfinder.querybuilder;

import java.io.StringWriter;

import javax.xml.datatype.XMLGregorianCalendar;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.util.jaxb.DTOFactory;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.crc.dao.DAOFactoryHelper;
import edu.harvard.i2b2.crc.dao.SetFinderDAOFactory;
import edu.harvard.i2b2.crc.dao.setfinder.IQueryInstanceDao;
import edu.harvard.i2b2.crc.datavo.CRCJAXBUtil;
import edu.harvard.i2b2.crc.datavo.db.DataSourceLookup;
import edu.harvard.i2b2.crc.datavo.setfinder.query.TimingStepType;
import edu.harvard.i2b2.crc.util.LogTimingUtil;

public class ProcessTimingReportUtil {
		
		public static final String INFO = "INFO";
		public static final String DEBUG = "DEBUG";
		public static final String NONE = "NONE";
		
		IQueryInstanceDao queryInstanceDao = null;

		public ProcessTimingReportUtil(DataSourceLookup dataSourceLookup) throws I2B2DAOException {
			SetFinderDAOFactory sfDaoFactory = this.getSetFinderDaoFactory(
					dataSourceLookup.getDomainId(), dataSourceLookup
							.getProjectPath(), dataSourceLookup.getOwnerId());
			queryInstanceDao  = sfDaoFactory.getQueryInstanceDAO();
		}
		
		public String buildProcessTiming(LogTimingUtil logTimingUtil,String name, String message) throws I2B2DAOException { 
			DTOFactory dtoFactory = new DTOFactory();
			TimingStepType timingStepType = new TimingStepType();
			timingStepType.setStartDate(dtoFactory.getXMLGregorianCalendar(logTimingUtil.getStartTime()) );
			timingStepType.setEndDate(dtoFactory.getXMLGregorianCalendar(logTimingUtil.getEndTime()) );
			timingStepType.setTotalTimeSecond(logTimingUtil.getDiffTime());
			timingStepType.setName(name);
			timingStepType.setMessage(message);
			edu.harvard.i2b2.crc.datavo.setfinder.query.ObjectFactory of = new edu.harvard.i2b2.crc.datavo.setfinder.query.ObjectFactory();
			StringWriter strWriter = new StringWriter(); 
			try {
				CRCJAXBUtil.getJAXBUtil().marshaller(of.createProcessStepTiming(timingStepType), strWriter) ;
			} catch (JAXBUtilException e) {
				throw new I2B2DAOException(e.getMessage());
			} 
			return "\n" +  strWriter.toString() + "\n";
		}
		
		public void logProcessTimingMessage(String queryInstanceId, String message) throws I2B2DAOException { 
			queryInstanceDao.updateMessage(queryInstanceId, message, true);
		}

		private SetFinderDAOFactory getSetFinderDaoFactory(String domainId,
				String projectPath, String ownerId) throws I2B2DAOException {
			DAOFactoryHelper helper = new DAOFactoryHelper(domainId, projectPath,
					ownerId);
			SetFinderDAOFactory sfDaoFactory = helper.getDAOFactory()
					.getSetFinderDAOFactory();
			return sfDaoFactory;
		}
}


