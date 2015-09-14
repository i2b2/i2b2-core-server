package edu.harvard.i2b2.ontology.dao;

import java.io.File;
import java.util.Date;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.exception.StackTraceUtil;
import edu.harvard.i2b2.ontology.datavo.crcloader.query.LoadDataResponseType;
import edu.harvard.i2b2.ontology.datavo.i2b2message.MessageHeaderType;
import edu.harvard.i2b2.ontology.datavo.i2b2message.SecurityType;
import edu.harvard.i2b2.ontology.datavo.pm.ProjectType;
import edu.harvard.i2b2.ontology.datavo.vdo.OntologyProcessStatusType;
import edu.harvard.i2b2.ontology.delegate.crc.CallCRCUtil;
import edu.harvard.i2b2.ontology.delegate.fr.CallFileRepositoryUtil;
import edu.harvard.i2b2.ontology.ejb.DBInfoType;
import edu.harvard.i2b2.ontology.util.OntologyUtil;

public class CRCConceptUpdateDao extends JdbcDaoSupport {

	private static Log log = LogFactory.getLog(CRCConceptUpdateDao.class);

	private DataSource dataSource = null;

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

	public OntologyProcessStatusType addUpdateConcept(ProjectType projectInfo,
			DBInfoType dbInfo, int processId,
			MessageHeaderType messageHeaderType, boolean synchronizeAllFlag,
			boolean hiddenConceptFlag)
			throws I2B2Exception {
		OntologyProcessStatusType ontProcessStatusType = null;
		OntProcessStatusDao ontProcessStatusDao = null;

		String pdoFileName = "";
		try {
			String metadataSchema = dbInfo.getDb_fullSchema();
			TableAccessDao tableAccessDao = new TableAccessDao();
			if (this.dataSource == null) {
				setDataSource(dbInfo.getDb_dataSource());
			} else {
				tableAccessDao.setDataSourceObject(this.dataSource);
			}

			SecurityType securityType = getSecurityType(messageHeaderType);
			String projectId = getProjectId(messageHeaderType);

			// update the process status
			ontProcessStatusDao = new OntProcessStatusDao(dataSource,
					projectInfo, dbInfo);

			pdoFileName = OntologyUtil.getInstance()
					.getFileRepositoryTempSpace()
					+ System.getProperty("file.separator")
					+ System.currentTimeMillis() + "_PDO_Concepts.xml";

			CreateConceptXmlDao createConceptXmlDao = new CreateConceptXmlDao();
			createConceptXmlDao.buildConceptUpdateXml(projectInfo, dbInfo,
					pdoFileName, synchronizeAllFlag,hiddenConceptFlag);
			ontProcessStatusDao.updateStatus(processId, null, "ONT_BUILD_PDO",
					"PROCESSING");
			// call frc
			log.debug("Begin file transfer to FR");
			//CallFileRepositoryUtil frUtil = new CallFileRepositoryUtil(
			//		securityType, projectId);
			CallFileRepositoryUtil.callFileRepository(pdoFileName, securityType, projectId);
			log.debug("File transfered to FR");
			ontProcessStatusDao.updateStatus(processId, null, "ONT_SENTTO_FRC",
					"PROCESSING");

			// call crc loader
			//CallCRCUtil crcUtil = new CallCRCUtil(securityType, projectId);
			LoadDataResponseType loadDataResponseType = CallCRCUtil.callCRCUpload(
					pdoFileName, "ONT_SYNC_" + processId, synchronizeAllFlag,securityType, projectId);
			// update upload_id in the process table
			ontProcessStatusDao.updateCRCUploadId(processId,
					loadDataResponseType.getUploadId());
			// poll upload status
			loadDataResponseType = CallCRCUtil
					.pollUploadStatus(loadDataResponseType.getUploadId());

			ontProcessStatusDao.updateStatus(processId, new Date(System
					.currentTimeMillis()), "ONT_SENTTO_CRCLOADER", "COMPLETED");
			return ontProcessStatusType;
		} catch (Throwable t) {
			t.printStackTrace();
			if (ontProcessStatusDao != null && processId > 0) {
				ontProcessStatusDao.updateStatus(processId, new Date(System
						.currentTimeMillis()), "ERROR", "ERROR");
				ontProcessStatusDao.updateStatusMessage(processId,
						StackTraceUtil.getStackTrace(t).substring(0, 1900));
			}
			throw new I2B2Exception(StackTraceUtil.getStackTrace(t).substring(
					0, 1900));
		} finally {

			// try to delete the temp pdo file
			try {
				if (pdoFileName != null) {
					File deleteFile = new File(pdoFileName);
					if (deleteFile.exists()) {
						deleteFile.delete();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private SecurityType getSecurityType(MessageHeaderType messageHeaderType) {
		SecurityType securityType = messageHeaderType.getSecurity();
		return securityType;
	}

	private String getProjectId(MessageHeaderType messageHeaderType) {
		return messageHeaderType.getProjectId();
	}

}
