package edu.harvard.i2b2.ontology.delegate;

import java.util.Map;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.ontology.dao.CRCConceptUpdateDao;
import edu.harvard.i2b2.ontology.datavo.i2b2message.MessageHeaderType;
import edu.harvard.i2b2.ontology.datavo.pm.ProjectType;
import edu.harvard.i2b2.ontology.ejb.DBInfoType;

public class CRCConceptUpdateRunnable implements RequestRunnable  {
	
		public void runnable(Map parameterMap) throws I2B2Exception { 
			CRCConceptUpdateDao crcConceptUpdateDao = new CRCConceptUpdateDao();
			ProjectType projectInfo = (ProjectType) parameterMap
					.get("ProjectType");
			DBInfoType dbInfoType = (DBInfoType) parameterMap.get("DBInfoType");
			MessageHeaderType messageHeaderType = (MessageHeaderType) parameterMap
					.get("MessageHeaderType");
			int processId = (Integer) parameterMap.get("ProcessId");
			boolean synchronizeAllFlag = (Boolean) parameterMap
					.get("SynchronizeAllFlag");
			boolean hiddenConceptFlag = (Boolean) parameterMap
					.get("HiddenConceptFlag");
			crcConceptUpdateDao.addUpdateConcept(projectInfo, dbInfoType,
					processId, messageHeaderType, synchronizeAllFlag,hiddenConceptFlag);
		}

		
	

}
