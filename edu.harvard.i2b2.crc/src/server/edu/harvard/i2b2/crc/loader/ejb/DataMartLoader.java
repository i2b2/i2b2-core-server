package edu.harvard.i2b2.crc.loader.ejb;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

import javax.xml.bind.JAXBElement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtil;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.crc.loader.dao.IUploaderDAOFactory;
import edu.harvard.i2b2.crc.loader.dao.UploadStatusDAO;
import edu.harvard.i2b2.crc.loader.dao.UploadStatusDAOI;
import edu.harvard.i2b2.crc.loader.datavo.loader.UploadSetStatus;
import edu.harvard.i2b2.crc.loader.datavo.loader.query.PublishDataRequestType;

/**
 * Main class for datamart load operation. The Load function delegates load
 * operation to appropriate loader class.
 * 
 * @author rk903
 * 
 */
public class DataMartLoader implements IDataMartLoaderHelper {

	// if the format is xml, then input file is in PatientData schema.
	public final static String INPUT_LOAD_FORMAT_XML = "XML";

	public final static String INPUT_LOAD_FORMAT_CSV = "CSV";

	// this is temp holder, later its read thru property file
	public static String loaderWorkingDirectory = "/";


	// @EJB(mappedName = "service/local")
	// private Service service;

	// log
	private static Log log = LogFactory.getLog(DataMartLoader.class);

	/*
	 * public void load(int uploadId, String inputLoadFile, String
	 * inputLoadFileFormat, String sourceSystemCd, boolean visitLoadFlag,
	 * boolean observationFactLoadFlag,boolean pidLoadFlag, boolean
	 * patientLoadFlag, String encounterSource, String conceptCodePrefix,
	 * boolean appendFlag, String userId, boolean tempCleanUpFlag) throws
	 * I2B2Exception {
	 */

	/**
	 * Main function to start upload process.
	 * 
	 * @param inputLoadFile
	 * @param inputLoadFileFormat
	 * @param visitLoadFlag
	 * @param observationFactLoadFlag
	 * @param userId
	 * @param tempCleanUpFlag
	 * @throws I2B2Exception
	 */
	public void load(IUploaderDAOFactory uploaderDaoFactory, String userId,
			int uploadId, String localUploadFile, String publishMessage)
			throws I2B2Exception {

		try {

			JAXBUtil jaxbUtil = edu.harvard.i2b2.crc.loader.datavo.CRCLoaderJAXBUtil
					.getJAXBUtil();
			PublishDataRequestType publishType = null;
			try {
				JAXBElement<?> jaxbElement = jaxbUtil
						.unMashallFromString(publishMessage);
				publishType = (PublishDataRequestType) jaxbElement.getValue();
			} catch (JAXBUtilException jaxbEx) {
				throw new I2B2Exception("Error proacessing request message "
						+ jaxbEx.getMessage(), jaxbEx);
			}

			String uploadFileName = localUploadFile;

			String inputLoadFileFormat = publishType.getInputList()
					.getDataFile().getDataFormatType().toString();
			String sourceSystemCd = publishType.getInputList().getDataFile()
					.getSourceSystemCd();
			boolean visitLoadFlag = (publishType.getLoadList()
					.getLoadEventSet() != null) ? true : false;
			boolean observationFactLoadFlag = (publishType.getLoadList()
					.getLoadObservationSet() != null) ? true : false;
			boolean appendFlag = false;
			if (observationFactLoadFlag) {
				appendFlag = publishType.getLoadList().getLoadObservationSet()
						.isAppendFlag();
			}
			boolean pidLoadFlag = (publishType.getLoadList().getLoadPidSet() != null) ? true
					: false;
			boolean eidLoadFlag = (publishType.getLoadList().getLoadEidSet() != null) ? true
					: false;
			boolean patientLoadFlag = (publishType.getLoadList()
					.getLoadPatientSet() != null) ? true : false;
			boolean conceptLoadFlag = (publishType.getLoadList()
					.getLoadConceptSet() != null) ? true : false;
			boolean conceptDeleteExistingDataFlag = (publishType.getLoadList()
					.getLoadConceptSet() != null) ? publishType.getLoadList()
					.getLoadConceptSet().isDeleteExistingData() : false;
			boolean modifierLoadFlag = (publishType.getLoadList()
					.getLoadModifierSet() != null) ? true : false;
			boolean modifierDeleteExistingDataFlag = (publishType.getLoadList()
					.getLoadModifierSet() != null) ? publishType.getLoadList()
					.getLoadModifierSet().isDeleteExistingData() : false;
			boolean observerLoadFlag = (publishType.getLoadList()
					.getLoadObserverSet() != null) ? true : false;
			boolean observerDeleteExistingDataFlag = (publishType.getLoadList()
					.getLoadObserverSet() != null) ? publishType.getLoadList()
					.getLoadObserverSet().isDeleteExistingData() : false;
			boolean stagingCleanUpFlag = publishType.getLoadList()
					.isClearTempLoadTables();

			String encounterSource = "";
			String conceptCodePrefix = "";

			doNullCheck(uploadFileName, inputLoadFileFormat, sourceSystemCd,
					userId);

			log.info("Starting to process loadfile :" + uploadFileName
					+ "format " + inputLoadFileFormat + " userid " + userId);

			// create Upload Status
			// uploadId = createUploadStatus(inputLoadFile, "STARTED",
			// sourceSystemCd, userId);
			// log.info("Created Upload Status: uploadId=" + uploadId);

			// check if pid set need to be loaded
			UploadStatusDAOI uploadStatusDAO = uploaderDaoFactory
					.getUploadStatusDAO();
			if (pidLoadFlag) {
				PidLoader pidLoader = new PidLoader(uploaderDaoFactory,
						uploadFileName, inputLoadFileFormat, encounterSource,
						sourceSystemCd, uploadId);
				performLoad(pidLoader, UploadStatusDAO.PID_SET);
				log.info("Pid load complete " + uploadFileName + " uploadId"
						+ uploadId);
				if (stagingCleanUpFlag) {
					log.info("Droping temp table ["
							+ pidLoader.getStagingTableName() + "]");
					uploadStatusDAO.dropTempTable(pidLoader
							.getStagingTableName());

				}
			}

			// check if eid set need to be loaded
			if (eidLoadFlag) {
				EidLoader eidLoader = new EidLoader(uploaderDaoFactory,
						uploadFileName, inputLoadFileFormat, encounterSource,
						sourceSystemCd, uploadId);
				performLoad(eidLoader, UploadStatusDAO.EID_SET);
				log.info("Eid load complete " + uploadFileName + " uploadId"
						+ uploadId);
				if (stagingCleanUpFlag) {
					log.info("Droping temp table ["
							+ eidLoader.getStagingTableName() + "]");
					uploadStatusDAO.dropTempTable(eidLoader
							.getStagingTableName());

				}
			}

			// check if patient set need to be loaded
			if (patientLoadFlag) {
				PatientLoader patientLoader = new PatientLoader(
						uploaderDaoFactory, uploadFileName,
						inputLoadFileFormat, encounterSource, sourceSystemCd,
						uploadId);
				performLoad(patientLoader, UploadStatusDAO.PATIENT_SET);
				log.info("Patient load complete " + uploadFileName
						+ " uploadId" + uploadId);
				if (stagingCleanUpFlag) {
					log.info("Droping temp table ["
							+ patientLoader.getStagingTableName() + "]");
					uploadStatusDAO.dropTempTable(patientLoader
							.getStagingTableName());

				}
			}

			// check if visit need to be added
			if (visitLoadFlag) {
				VisitLoader visitLoader = new VisitLoader(uploaderDaoFactory,
						uploadFileName, inputLoadFileFormat, encounterSource,
						sourceSystemCd, uploadId);
				performLoad(visitLoader, UploadStatusDAO.EVENT_SET);
				log.info("Visit load complete " + uploadFileName + " uploadId"
						+ uploadId);
				if (stagingCleanUpFlag) {
					log.info("Droping temp table ["
							+ visitLoader.getStagingTableName() + "]");
					uploadStatusDAO.dropTempTable(visitLoader
							.getStagingTableName());

				}
			}

			// check if concept set need to be loaded
			if (conceptLoadFlag) {
				log.info("Concept load started " + uploadFileName + " uploadId"
						+ uploadId);

				ConceptLoader conceptLoader = new ConceptLoader(
						uploaderDaoFactory, uploadFileName,
						inputLoadFileFormat, encounterSource, sourceSystemCd,
						conceptDeleteExistingDataFlag, uploadId);
				performLoad(conceptLoader, UploadStatusDAO.CONCEPT_SET);
				log.info("Concept load complete " + uploadFileName
						+ " uploadId" + uploadId);
				if (stagingCleanUpFlag) {
					log.info("Droping temp table ["
							+ conceptLoader.getStagingTableName() + "]");
					uploadStatusDAO.dropTempTable(conceptLoader
							.getStagingTableName());

				}
			}
			
			// check if modifier set need to be loaded
			if (modifierLoadFlag) {
				log.info("Modifier load started " + uploadFileName + " uploadId"
						+ uploadId);

				ModifierLoader modifierLoader = new ModifierLoader(
						uploaderDaoFactory, uploadFileName,
						inputLoadFileFormat, encounterSource, sourceSystemCd,
						modifierDeleteExistingDataFlag, uploadId);
				performLoad(modifierLoader, UploadStatusDAO.MODIFIER_SET);
				log.info("Modifier load complete " + uploadFileName
						+ " uploadId" + uploadId);
				if (stagingCleanUpFlag) {
					log.info("Droping temp table ["
							+ modifierLoader.getStagingTableName() + "]");
					uploadStatusDAO.dropTempTable(modifierLoader
							.getStagingTableName());

				}
			}

			// check if observer set need to be loaded
			if (observerLoadFlag) {
				log.info("Provider load started " + uploadFileName
						+ " uploadId" + uploadId);

				ProviderLoader providerLoader = new ProviderLoader(
						uploaderDaoFactory, uploadFileName,
						inputLoadFileFormat, encounterSource, sourceSystemCd,
						observerDeleteExistingDataFlag, uploadId);
				performLoad(providerLoader, UploadStatusDAO.OBSERVER_SET);
				log.info("Provider load complete " + uploadFileName
						+ " uploadId" + uploadId);
				if (stagingCleanUpFlag) {
					log.info("Droping temp table ["
							+ providerLoader.getStagingTableName() + "]");
					uploadStatusDAO.dropTempTable(providerLoader
							.getStagingTableName());

				}
			}

			// check if observation fact need to be added
			if (observationFactLoadFlag) {
				ObservationFactLoader obsFactLoader = new ObservationFactLoader(
						uploaderDaoFactory, uploadFileName,
						inputLoadFileFormat, encounterSource,
						conceptCodePrefix, sourceSystemCd, appendFlag, uploadId);
				performLoad(obsFactLoader, UploadStatusDAO.OBSERVATION_SET);
				log.info("ObservationFact load complete " + uploadFileName
						+ " uploadId" + uploadId);
				if (stagingCleanUpFlag) {

					log.info("Droping temp table ["
							+ obsFactLoader.getStagingTableName() + "]");
					uploadStatusDAO.dropTempTable(obsFactLoader
							.getStagingTableName());

				}

			}

			if (stagingCleanUpFlag) {
				try { // remove local input file File
					File deleteLocalFile = new File(localUploadFile);
					deleteLocalFile.delete();
				} catch (Throwable t) {
					log.error("Unable to delete the temp file ["
							+ localUploadFile + " ]");
				}
			}

		} catch (Throwable ex) {
			ex.printStackTrace();
			log.error("Error in Upload Process ", ex);
			/*
			try {
				if (utx.getStatus() == Status.STATUS_ACTIVE) {
					utx.rollback();
				}
			} catch (Exception e) {
				log.error("unable to rollback transaction [" + e.getMessage()
						+ "]");
			}
			 */
			// rethrow exception
			throw new I2B2Exception("Error in Upload Process ["
					+ ex.getMessage() + "]");
		}
	}

	public String getLoaderWorkingDirectory() {
		return loaderWorkingDirectory;
	}

	public void setLoaderWorkingDirectory(String loaderWorkingDirectory) {
		DataMartLoader.loaderWorkingDirectory = loaderWorkingDirectory;
	}

	private void performLoad(AbstractDimensionLoader dimensionLoader,
			int setTypeId) throws I2B2Exception {

		Throwable t = null;
		// setstatus started
		IUploaderDAOFactory uploadDaoFactory = dimensionLoader
				.getUploaderDaoFactory();

		try {

//			utx.begin();
			UploadStatusDAOI uploadStatusDao = uploadDaoFactory
					.getUploadStatusDAO();
			UploadSetStatus setStatus = new UploadSetStatus();
			setStatus.setUploadId(dimensionLoader.getUploadId());
			setStatus.setLoadStatus("PROCESSING");
			setStatus.setSetTypeId(setTypeId);
			setStatus.setInputFileName(dimensionLoader.getInputLoadFile());
			setStatus.setSourceCd(dimensionLoader.getSourceSystemCd());
			setStatus.setLoadDate(new Date(System.currentTimeMillis()));
			uploadStatusDao.insertUploadSetStatus(setStatus);
	//		utx.commit();

		//	utx.begin();
			dimensionLoader.createTempTable();
	//		utx.commit();

	//		utx.begin();
			int totalRecords = dimensionLoader.loadTempTable();
	//		utx.commit();

			// if (deleteExistingDataFlag) {
			// utx.begin();
			// dimensionLoader.backupAndClearTable();
			// utx.commit();
			// }
	//		utx.begin();
			int loadedRecords = dimensionLoader.mergeTempTable();
	//		utx.commit();

	//		utx.begin();
			setStatus = uploadStatusDao.getUploadSetStatus(dimensionLoader
					.getUploadId(), setTypeId);
			if (loadedRecords == totalRecords) {
				setStatus.setLoadStatus("FINISHED");
			} else {
				setStatus.setLoadStatus("WARNING");
			}

			setStatus.setLoadedRecord(loadedRecords);
			setStatus.setEndDate(new Date(System.currentTimeMillis()));
			setStatus.setNoOfRecord(totalRecords);
			uploadStatusDao.updateUploadSetStatus(setStatus);
	//		utx.commit();
		} catch (Throwable e) {
			e.printStackTrace();
			t = e;
		} finally {

			if (t != null) {

				try { // check if exception thrown after the begin, if so
					// rollback
			//		if (utx.getStatus() == Status.STATUS_ACTIVE) {
			//			utx.rollback();
			//		} // update set status with error status
			//		utx.begin();
					UploadStatusDAOI uploadStatusDao = uploadDaoFactory
							.getUploadStatusDAO();
					UploadSetStatus setStatus = uploadStatusDao
							.getUploadSetStatus(dimensionLoader.getUploadId(),
									setTypeId);
					setStatus.setLoadStatus("ERROR");
					StringWriter stringWriter = new StringWriter();
					t.printStackTrace(new PrintWriter(stringWriter));
					String message = stringWriter.toString();
					if (message != null) {
						setStatus.setMessage(message.substring(0, (message
								.length() > 3995) ? 3995 : message.length()));
					}
					setStatus.setEndDate(new Date(System.currentTimeMillis()));
					uploadStatusDao.updateUploadSetStatus(setStatus);
		//			utx.commit();
	//			} catch (NotSupportedException e) {
	//				e.printStackTrace();
	//			} catch (SystemException e) {
		//			e.printStackTrace();
				} catch (SecurityException e) {
					e.printStackTrace();
				} catch (IllegalStateException e) {
					e.printStackTrace();
	//			} catch (RollbackException e) {
	//				e.printStackTrace();
	//			} catch (HeuristicMixedException e) {
	//				e.printStackTrace();
		//		} catch (HeuristicRollbackException e) {
		//			e.printStackTrace();
				}
				throw new I2B2Exception("Error while loading data "
						+ t.getMessage());
			}

		}

		// setstatus fininshed
	}

	public void deleteUploadData(IUploaderDAOFactory uploaderDaoFactory,
			int uploadId) throws I2B2Exception {
		UploadStatusDAOI uploadStatusDAO = uploaderDaoFactory
				.getUploadStatusDAO();
		// :TODO throw i2b2exception to client.
		try {
			uploadStatusDAO.deleteUploadData(uploadId);
		} catch (I2B2Exception i2b2Ex) {
			i2b2Ex.printStackTrace();
		}
	}

	private void doNullCheck(String inputLoadFile, String inputLoadFileFormat,
			String sourceSystemCd, String userId) throws I2B2Exception {
		if (inputLoadFile == null) {
			throw new I2B2Exception("Input load file is null");
		}
		if (inputLoadFileFormat == null) {
			throw new I2B2Exception("Input load file format is null");
		}
		if (sourceSystemCd == null) {
			throw new I2B2Exception("Input source system code is null");
		}

		if (userId == null) {
			throw new I2B2Exception("User id is null");
		}
	}

}
