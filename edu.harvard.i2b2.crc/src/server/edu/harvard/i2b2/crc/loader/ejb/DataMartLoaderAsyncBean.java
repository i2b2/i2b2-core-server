package edu.harvard.i2b2.crc.loader.ejb;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.xml.bind.JAXBElement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.jaxb.DTOFactory;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtil;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.crc.loader.dao.ILoaderDAOFactory;
import edu.harvard.i2b2.crc.loader.dao.IUploaderDAOFactory;
import edu.harvard.i2b2.crc.loader.dao.LoaderDAOFactoryHelper;
import edu.harvard.i2b2.crc.loader.dao.UniqueKeyException;
import edu.harvard.i2b2.crc.loader.dao.UploadStatusDAOI;
import edu.harvard.i2b2.crc.datavo.i2b2message.SecurityType;
import edu.harvard.i2b2.crc.loader.datavo.loader.DataSourceLookup;
import edu.harvard.i2b2.crc.loader.datavo.loader.UploadSetStatus;
import edu.harvard.i2b2.crc.loader.datavo.loader.UploadStatus;
import edu.harvard.i2b2.crc.loader.datavo.loader.query.BulkLoadRequestType;
import edu.harvard.i2b2.crc.loader.datavo.loader.query.InputOptionListType;
import edu.harvard.i2b2.crc.loader.datavo.loader.query.LoadDataResponseType;
import edu.harvard.i2b2.crc.loader.datavo.loader.query.PublishDataRequestType;
import edu.harvard.i2b2.crc.loader.datavo.loader.query.SetStatusType;
import edu.harvard.i2b2.crc.loader.datavo.loader.query.StatusType;
import edu.harvard.i2b2.crc.loader.datavo.loader.query.LoadDataResponseType.DataFileLocationUri;

public class DataMartLoaderAsyncBean implements DataMartLoaderAsyncBeanRemote,
		DataMartLoaderAsyncBeanLocal {

	DTOFactory dtoFactory = new DTOFactory();

	// get this
	public static final String LOG_REFERENCE_PREFIX = "";

	static final Log log = LogFactory.getLog("DataMartLoaderAsyncBean");

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.harvard.i2b2.crc.loader.ejb.IDataMartLoaderBean#load(java.lang.String
	 * )
	 */
	public LoadDataResponseType load(DataSourceLookup dataSourceLookup,
			String publishMessage, SecurityType i2b2SecurityType, long timeout,
			String fileSystemDefaultStorageResource) throws I2B2Exception {
		LoadDataResponseType response = null;
		String userId = null, password = null;
		int uploadId = 0;
		Exception exception = null;

		// //String hiveId, String projectId, String ownerId
		LoaderDAOFactoryHelper daoHelper = new LoaderDAOFactoryHelper(
				dataSourceLookup.getDomainId(), dataSourceLookup
						.getProjectPath(), dataSourceLookup.getOwnerId());
		ILoaderDAOFactory loaderDaoFactory = daoHelper.getDAOFactory();
		IUploaderDAOFactory uploaderDaoFactory = loaderDaoFactory
				.getUpLoaderDAOFactory();
		log.debug("In Load-1");
		try {
			if (i2b2SecurityType == null) {
				String errorMsg = "DataMartLoaderAsyncBean.load: input security type is null";
				log.error(errorMsg);
				throw new I2B2Exception(errorMsg);
			} else {
				userId = i2b2SecurityType.getUsername();
				password = i2b2SecurityType.getPassword().getValue();

			}
			PublishDataRequestType publishType = null;

			try {
				log.debug("In Load-2");

				JAXBUtil jaxbUtil = edu.harvard.i2b2.crc.loader.datavo.CRCLoaderJAXBUtil
						.getJAXBUtil();

				JAXBElement<?> jaxbElement = jaxbUtil
						.unMashallFromString(publishMessage);
				publishType = (PublishDataRequestType) jaxbElement.getValue();

			} catch (JAXBUtilException jaxbEx) {

				throw new I2B2Exception("Error processing request message "
						+ jaxbEx.getMessage(), jaxbEx);
			}

	//		utx.begin();
			log.debug("In Load-3");

			uploadId = createUploadStatus(uploaderDaoFactory, publishType,
					userId);
			log.info("Created Upload Status: uploadId=" + uploadId);
	//		utx.commit();

	//		utx.begin();
			
			if ((publishType.getInputList().getDataFile().getTransformName() == null) ||
					(publishType.getInputList().getDataFile().getTransformName().equals("PDO_IMPORT"))){
			
					response = sendAndGetQueueResponse(uploaderDaoFactory, uploadId,
							i2b2SecurityType, publishMessage, timeout,
							fileSystemDefaultStorageResource);
			}
			
			// Its something other than a PDO import so dont kick off queueing..
			else{
				if(publishType.getInputList().getDataFile().getTransformName().equals("GENOMIC_IMPORT")){	
					if(publishType.getInputList().getDataFile().getLoadLabel().equals("BULK_LOAD_OBS_FACT")){	
						createSetUploadStatus(uploaderDaoFactory, publishType,uploadId, 5);
					}
				}
				response = buildResponse(uploaderDaoFactory, uploadId);
			}
	//		utx.commit();
			// return processUploadMessage(publishType);
		} catch (I2B2Exception e) {
			exception = e;
		} catch (SecurityException e) {
			exception = e;
		} catch (IllegalStateException e) {
			exception = e;
		} finally {
			if (exception != null) {
				//try {
				//	if (utx.getStatus() == Status.STATUS_ACTIVE) {
				//		utx.rollback();
				//	}
				//} catch (Exception e) {
				//}

				try {
	//				utx.begin();
					// update status

					StringWriter stringWriter = new StringWriter();
					exception.printStackTrace(new PrintWriter(stringWriter));
					updateUploadStatus(uploaderDaoFactory, uploadId,
							"INCOMPLETE", stringWriter.toString());
					// build response
					response = buildResponse(uploaderDaoFactory, uploadId);
		//			utx.commit();
				} catch (Exception e) {
		//			try {
		//				utx.rollback();
		//			} catch (Exception e1) {
		//			}
				}

				String errorMsg = "LoadDataResponseType.load:Error "
						+ exception.getMessage();
				log.error(errorMsg);
				throw new I2B2Exception(errorMsg, exception);

			}

		}

		return response;

	}

	public LoadDataResponseType bulkLoad(DataSourceLookup dataSourceLookup,
			String bulkLoadMessage, SecurityType i2b2SecurityType, long timeout) throws I2B2Exception {
		LoadDataResponseType response = null;
		String userId = null, password = null;
		int uploadId = 0;
		Exception exception = null;

		// //String hiveId, String projectId, String ownerId
		LoaderDAOFactoryHelper daoHelper = new LoaderDAOFactoryHelper(
				dataSourceLookup.getDomainId(), dataSourceLookup
						.getProjectPath(), dataSourceLookup.getOwnerId());
		ILoaderDAOFactory loaderDaoFactory = daoHelper.getDAOFactory();
		IUploaderDAOFactory uploaderDaoFactory = loaderDaoFactory
				.getUpLoaderDAOFactory();
	//	log.debug("In BulkLoad-1");
		try {
			if (i2b2SecurityType == null) {
				String errorMsg = "DataMartLoaderAsyncBean.bulkLoad: input security type is null";
				log.error(errorMsg);
				throw new I2B2Exception(errorMsg);
			} else {
				userId = i2b2SecurityType.getUsername();
				password = i2b2SecurityType.getPassword().getValue();

			}
			BulkLoadRequestType bulkLoadType = null;

			try {
		//		log.debug("In BulkLoad-2");

				JAXBUtil jaxbUtil = edu.harvard.i2b2.crc.loader.datavo.CRCLoaderJAXBUtil
						.getJAXBUtil();

				JAXBElement<?> jaxbElement = jaxbUtil
						.unMashallFromString(bulkLoadMessage);
				bulkLoadType = (BulkLoadRequestType) jaxbElement.getValue();

			} catch (JAXBUtilException jaxbEx) {

				throw new I2B2Exception("Error processing request message "
						+ jaxbEx.getMessage(), jaxbEx);
			}

	//		utx.begin();
	//		log.debug("In BulkLoad-3");

			uploadId = createUploadStatus(uploaderDaoFactory, bulkLoadType,
					userId);
	//		log.info("Created Upload Status: uploadId=" + uploadId);
	//		utx.commit();

	//		utx.begin();
			response = buildBulkLoadResponse(uploaderDaoFactory, uploadId);
	//		utx.commit();
			// return processUploadMessage(publishType);
		} catch (I2B2Exception e) {
			exception = e;
		} catch (SecurityException e) {
			exception = e;
		} catch (IllegalStateException e) {
			exception = e;
		} finally {
			if (exception != null) {
				//try {
				//	if (utx.getStatus() == Status.STATUS_ACTIVE) {
				//		utx.rollback();
				//	}
				//} catch (Exception e) {
				//}

				try {
	//				utx.begin();
					// update status

					StringWriter stringWriter = new StringWriter();
					exception.printStackTrace(new PrintWriter(stringWriter));
					updateUploadStatus(uploaderDaoFactory, uploadId,
							"INCOMPLETE", stringWriter.toString());
					// build response
					response = buildBulkLoadResponse(uploaderDaoFactory, uploadId);
		//			utx.commit();
				} catch (Exception e) {
		//			try {
		//				utx.rollback();
		//			} catch (Exception e1) {
		//			}
				}

				String errorMsg = "LoadDataResponseType.bulkLoad:Error "
						+ exception.getMessage();
				log.error(errorMsg);
				throw new I2B2Exception(errorMsg, exception);

			}

		}

		return response;

	}

	
	
	
	
	private int createUploadStatus(IUploaderDAOFactory uploaderDaoFactory,
			PublishDataRequestType publishType, String userId)
			throws I2B2Exception {
		InputOptionListType inputOptionType = publishType.getInputList();
		String loadFileName = inputOptionType.getDataFile().getLocationUri()
				.getValue();
		String sourceSystemCd = inputOptionType.getDataFile()
				.getSourceSystemCd();

		String status = "QUEUED";
		String uploadLabel = inputOptionType.getDataFile().getLoadLabel();
		UploadStatus uploadStatus = new UploadStatus();
		uploadStatus.setInputFileName(loadFileName);
		uploadStatus.setUploadLabel(uploadLabel);
		uploadStatus.setSourceCd(sourceSystemCd);
		uploadStatus.setLoadStatus(status);
		uploadStatus.setUserId(userId);
		uploadStatus.setLoadDate(new Date(System.currentTimeMillis()));
		uploadStatus.setTransformName(inputOptionType.getDataFile().getTransformName());

		UploadStatusDAOI uploadStatusDAO = uploaderDaoFactory
				.getUploadStatusDAO();
		return uploadStatusDAO.insertUploadStatus(uploadStatus);

	}
	
   /* 
    * This method is a way for the non-PDO loads to write the initial entry
    * to the set_upload_status table
    */
	
	private void createSetUploadStatus(IUploaderDAOFactory uploaderDaoFactory,
			PublishDataRequestType publishType, int uploadId, int setId)
			throws I2B2Exception {
		InputOptionListType inputOptionType = publishType.getInputList();
		String loadFileName = inputOptionType.getDataFile().getLocationUri().getValue();
			
		//set_upload_status has no load label, so store this info in source system code.
		String sourceSystemCd = inputOptionType.getDataFile().getLoadLabel();
			
		String status = "QUEUED";
		UploadSetStatus uploadSetStatus = new UploadSetStatus();
		uploadSetStatus.setUploadId(uploadId);
		uploadSetStatus.setInputFileName(loadFileName);
		uploadSetStatus.setSetTypeId(setId);
		uploadSetStatus.setSourceCd(sourceSystemCd);
		uploadSetStatus.setLoadStatus(status);		
		uploadSetStatus.setLoadDate(new Date(System.currentTimeMillis()));
		uploadSetStatus.setTransformName(inputOptionType.getDataFile().getTransformName());

		UploadStatusDAOI uploadStatusDAO = uploaderDaoFactory
				.getUploadStatusDAO();
		uploadStatusDAO.insertUploadSetStatus(uploadSetStatus);

	}
	
	
	private int createUploadStatus(IUploaderDAOFactory uploaderDaoFactory,
			BulkLoadRequestType bulkLoadType, String userId)
			throws I2B2Exception {
		String loadFileName = bulkLoadType.getFileName();
		String sourceSystemCd = bulkLoadType.getSourceSystemCd();

		String status = "READY";
		String uploadLabel = bulkLoadType.getFileType();
		UploadStatus uploadStatus = new UploadStatus();
		uploadStatus.setInputFileName(loadFileName);
		uploadStatus.setUploadLabel(uploadLabel);
		uploadStatus.setSourceCd(sourceSystemCd);
		uploadStatus.setLoadStatus(status);
		uploadStatus.setUserId(userId);
		uploadStatus.setLoadDate(new Date(System.currentTimeMillis()));

		UploadStatusDAOI uploadStatusDAO = uploaderDaoFactory
				.getUploadStatusDAO();
		return uploadStatusDAO.insertUploadStatus(uploadStatus);

	}

	private void updateUploadStatus(IUploaderDAOFactory uploaderDaoFactory,
			int uploadId, String status, String message) throws I2B2Exception {
		UploadStatusDAOI uploadStatusDAO = uploaderDaoFactory
				.getUploadStatusDAO();
		// try {
		UploadStatus uploadStatus;
		try {
			uploadStatus = uploadStatusDAO.findById(uploadId);
			uploadStatus.setLoadStatus(status);
			if (message != null) {
				int length = (message.length() > 3995) ? 3995 : message
						.length();
				uploadStatus.setMessage(message.substring(0, length));
			}
			uploadStatus.setEndDate(new Date(System.currentTimeMillis()));
			uploadStatusDAO.updateUploadStatus(uploadStatus);
			// uploadStatusDAO.calculateUploadStatus(uploadId);

		} catch (UniqueKeyException unqEx) {
			unqEx.printStackTrace();
			log.error("Error while upload status update", unqEx);
		}
	}

	private LoadDataResponseType sendAndGetQueueResponse(
			IUploaderDAOFactory uploaderDaoFactory, int uploadId,
			SecurityType securityType, String publishMessage, long timeout,
			String fileSystemDefaultStorageResource) {
		/*//mm removed EJB
		Session session = null;
		MessageProducer producer = null;
		MessageConsumer receiver = null;
		TextMessage message = null;
		*/
		LoadDataResponseType response = null;
		try {
			/*//mm removed EJB
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			producer = session.createProducer(queue);
			message = session.createTextMessage();

			message.setJMSCorrelationID(String.valueOf(uploadId));
			message.setIntProperty(DataMartLoaderBeanMDB.UPLOAD_ID, uploadId);
			message.setStringProperty(DataMartLoaderBeanMDB.I2B2_USER_ID,
					securityType.getUsername());
			message.setStringProperty(DataMartLoaderBeanMDB.I2B2_PASSWORD,
					securityType.getPassword().getValue());
			message.setBooleanProperty(DataMartLoaderBeanMDB.I2B2_PASSWORD_ISTOKEN,
					securityType.getPassword().isIsToken());

			message.setStringProperty(
					DataMartLoaderBeanMDB.DS_LOOKUP_DOMAIN_ID,
					uploaderDaoFactory.getDataSourceLookup().getDomainId());
			message.setStringProperty(DataMartLoaderBeanMDB.DS_LOOKUP_OWNER_ID,
					uploaderDaoFactory.getDataSourceLookup().getOwnerId());
			message.setStringProperty(
					DataMartLoaderBeanMDB.DS_LOOKUP_PROJECT_ID,
					uploaderDaoFactory.getDataSourceLookup().getProjectPath());
			message.setStringProperty(
					DataMartLoaderBeanMDB.IROD_FILESYSTEM_STORAGE_RESOURCE,
					fileSystemDefaultStorageResource);

			message.setText(publishMessage);
			log.info("DataMartLoaderSync: Sending " + "message text to: "
					+ message.getText());
			producer.send(message);

			//
			String selector = "JMSCorrelationID='" + uploadId + "'";
			receiver = session.createConsumer(responseQueue, selector);

			connection.start();

			TextMessage inMessage = (TextMessage) receiver.receive(timeout);
			if (inMessage != null) {
				System.out.println("Received text message from response queue"
						+ inMessage.getText());

			}
			*/
			
			
			Hashtable message = new Hashtable();
			message.put(DataMartLoaderBeanMDB.UPLOAD_ID, uploadId);
			message.put(DataMartLoaderBeanMDB.I2B2_USER_ID,
					securityType.getUsername());
			message.put(DataMartLoaderBeanMDB.I2B2_PASSWORD,
					securityType.getPassword().getValue());
			message.put(DataMartLoaderBeanMDB.I2B2_PASSWORD_ISTOKEN,
					securityType.getPassword().isIsToken());
			message.put(
					DataMartLoaderBeanMDB.I2B2_REQUEST_MSG,
					publishMessage);
			message.put(
					DataMartLoaderBeanMDB.DS_LOOKUP_DOMAIN_ID,
					uploaderDaoFactory.getDataSourceLookup().getDomainId());
			message.put(DataMartLoaderBeanMDB.DS_LOOKUP_OWNER_ID,
					uploaderDaoFactory.getDataSourceLookup().getOwnerId());
			message.put(
					DataMartLoaderBeanMDB.DS_LOOKUP_PROJECT_ID,
					uploaderDaoFactory.getDataSourceLookup().getProjectPath());
			
			// no longer needed?
	//		message.put(
	//				DataMartLoaderBeanMDB.IROD_FILESYSTEM_STORAGE_RESOURCE,
	//				fileSystemDefaultStorageResource);
			

			log.debug("In DatamartLoaderAsync about to run MDB.onMessage");
			
			
			DataMartLoaderBeanMDB run = new DataMartLoaderBeanMDB();
			String results = run.onMessage(message);
			
			log.debug("In DatamartLoaderAsync updateUploadStatus :" + results);
			
			
			
			response = buildResponse(uploaderDaoFactory, uploadId);
		} catch (Throwable t) {
			// JMSException could be thrown
			log.error("DataMartLoaderAsync.sendAndGetQueueResponse: "
					+ "Exception: " + t.toString());
			
			

			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			t.printStackTrace(pw);
			
			try {
				buildResponse(uploaderDaoFactory, uploadId);
			} catch (I2B2Exception e) {
				StatusType statusType = new StatusType();
				StatusType.Condition condition = new StatusType.Condition();
				condition.setType("ERROR");
				condition.setValue(sw.toString());
				statusType.getCondition().add(condition);
				response.setStatus(statusType);
			}
		} finally {
			//mm removed EJB
			/*
			if (session != null) {
				try {
					session.close();
				} catch (JMSException e) {
				}
			}
			*/

		}
		return response;

	}

	private LoadDataResponseType buildResponse(
			IUploaderDAOFactory uploaderDaoFactory, int uploadId)
			throws I2B2Exception {
		LoadDataResponseType response = new LoadDataResponseType();
		try {
			UploadStatusDAOI statusDao = uploaderDaoFactory
					.getUploadStatusDAO();

			UploadStatus uploadStatus = statusDao.findById(uploadId);
			// build response
			DataFileLocationUri fileLoc = new DataFileLocationUri();
			fileLoc.setValue(uploadStatus.getInputFileName());
			response.setDataFileLocationUri(fileLoc);
			response.setLoadStatus(uploadStatus.getLoadStatus());
			response.setUploadId(String.valueOf(uploadStatus.getUploadId()));
			response.setUserId(uploadStatus.getUserId());
			response.setMessage(uploadStatus.getMessage());
			response.setTransformerName(uploadStatus.getTransformName());
			response.setStartDate(dtoFactory
					.getXMLGregorianCalendar(uploadStatus.getLoadDate()
							.getTime()));
			if (uploadStatus.getEndDate() != null) {
				response.setEndDate(dtoFactory
						.getXMLGregorianCalendar(uploadStatus.getEndDate()
								.getTime()));
			}

			List<UploadSetStatus> setStatusList = statusDao
					.getUploadSetStatusByLoadId(uploadId);
			for (UploadSetStatus setStatus : setStatusList) {
				SetStatusType responseSetStatusType = new SetStatusType();
				responseSetStatusType.setIgnoredRecord(setStatus
						.getNoOfRecord()
						- setStatus.getLoadedRecord());
				responseSetStatusType.setInsertedRecord(setStatus
						.getLoadedRecord());
				responseSetStatusType.setMessage(setStatus.getMessage());
				responseSetStatusType.setTotalRecord(setStatus.getNoOfRecord());
				if (setStatus.getSetTypeId() == 1) {
					response.setEventSet(responseSetStatusType);
				} else if (setStatus.getSetTypeId() == 2) {
					response.setPatientSet(responseSetStatusType);
				} else if (setStatus.getSetTypeId() == 3) {
					response.setConceptSet(responseSetStatusType);
				} else if (setStatus.getSetTypeId() == 4) {
					response.setObserverSet(responseSetStatusType);
				} else if (setStatus.getSetTypeId() == 5) {
					response.setObservationSet(responseSetStatusType);
				} else if (setStatus.getSetTypeId() == 6) {
					response.setPidSet(responseSetStatusType);
				} else if (setStatus.getSetTypeId() == 7) {
					response.setEventidSet(responseSetStatusType);
				} else if (setStatus.getSetTypeId() == 8) {
					response.setModifierSet(responseSetStatusType);
				}

			}
		} catch (I2B2Exception i2b2Ex) {
			throw new I2B2Exception(
					"DataMartLoaderAsync.buildResponse:Exception"
							+ i2b2Ex.getMessage(), i2b2Ex);
		}
		return response;
	}

	private LoadDataResponseType buildBulkLoadResponse(
			IUploaderDAOFactory uploaderDaoFactory, int uploadId)
			throws I2B2Exception {
		LoadDataResponseType response = new LoadDataResponseType();
		try {
			UploadStatusDAOI statusDao = uploaderDaoFactory
					.getUploadStatusDAO();

			UploadStatus uploadStatus = statusDao.findById(uploadId);
			// build response
			DataFileLocationUri fileLoc = new DataFileLocationUri();
			fileLoc.setValue(uploadStatus.getInputFileName());
			response.setDataFileLocationUri(fileLoc);
			response.setLoadStatus(uploadStatus.getLoadStatus());
			response.setUploadId(String.valueOf(uploadStatus.getUploadId()));
			response.setUserId(uploadStatus.getUserId());
			response.setMessage(uploadStatus.getMessage());
			response.setTransformerName(uploadStatus.getTransformName());
			response.setStartDate(dtoFactory
					.getXMLGregorianCalendar(uploadStatus.getLoadDate()
							.getTime()));
			if (uploadStatus.getEndDate() != null) {
				response.setEndDate(dtoFactory
						.getXMLGregorianCalendar(uploadStatus.getEndDate()
								.getTime()));
			
			}
		} catch (I2B2Exception i2b2Ex) {
			throw new I2B2Exception(
					"DataMartLoaderAsync.buildBulkLoadResponse:Exception"
							+ i2b2Ex.getMessage(), i2b2Ex);
		}
		return response;
	}
	
	
	
	/**
	 * Creates the connection.
	 */
	@PostConstruct
	public void makeConnection() {
		try {
			//mm removed EJB
		//	connection = connectionFactory.createConnection();
		} catch (Throwable t) {
			// JMSException could be thrown
			log.error("DataMartLoaderAsync.makeConnection:" + "Exception: "
					+ t.toString());
		}
	}

	/**
	 * Closes the connection.
	 */
	@PreDestroy
	public void endConnection() throws RuntimeException {
		//mm removed EJB
		/*
		if (connection != null) {
			try {
				connection.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		*/
	}

}
