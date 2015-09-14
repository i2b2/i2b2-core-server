package edu.harvard.i2b2.crc.loader.ejb;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.Hashtable;
import java.util.logging.Logger;

import javax.xml.bind.JAXBElement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.ServiceLocatorException;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtil;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.crc.loader.dao.IUploaderDAOFactory;
import edu.harvard.i2b2.crc.loader.dao.LoaderDAOFactoryHelper;
import edu.harvard.i2b2.crc.loader.dao.UniqueKeyException;
import edu.harvard.i2b2.crc.loader.dao.UploadStatusDAOI;
import edu.harvard.i2b2.crc.datavo.i2b2message.PasswordType;
import edu.harvard.i2b2.crc.datavo.i2b2message.SecurityType;
import edu.harvard.i2b2.crc.loader.datavo.loader.UploadStatus;
import edu.harvard.i2b2.crc.loader.datavo.loader.query.DataListType;
import edu.harvard.i2b2.crc.loader.datavo.loader.query.PublishDataRequestType;
import edu.harvard.i2b2.crc.loader.ejb.fr.FRBean;
import edu.harvard.i2b2.crc.loader.util.CRCLoaderUtil;

/**
 * The MessageBean class is a message-driven bean. It implements the
 * javax.jms.MessageListener interface. It is defined as public (but not final
 * or abstract).
 */

public class DataMartLoaderBeanMDB { //mm removed EJB implements MessageListener {

	public final static String UPLOAD_ID = "UPLOAD_ID";
	public final static String DS_LOOKUP_DOMAIN_ID = "DS_LOOKUP_DOMAIN_ID";
	public final static String DS_LOOKUP_PROJECT_ID = "DS_LOOKUP_PROJECT_ID";
	public final static String DS_LOOKUP_OWNER_ID = "DS_LOOKUP_OWNER_ID";
	public final static String I2B2_USER_ID = "I2B2_USER_ID";
	public final static String I2B2_REQUEST_MSG = "I2B2_REQUEST_MSG";
	public final static String I2B2_PASSWORD = "I2B2_PASSWORD";
	public final static String I2B2_PASSWORD_ISTOKEN = "false";
	public final static String IROD_FILESYSTEM_STORAGE_RESOURCE = "IROD_FILESYSTEM_STORAGE_RESOURCE";

	IDataMartLoaderHelper dmLoader = new DataMartLoader();

	// @ActivationConfigProperty(propertyName = "transactionTimeout",
	// propertyValue = "0"),

	/*
	 * @ActivationConfigProperty(propertyName="useDLQ", propertyValue="false"),
	 * 
	 * @ActivationConfigProperty(propertyName="DLQMaxResent",
	 * propertyValue="1"),
	 * 
	 * @ActivationConfigProperty(propertyName = "subscriptionDurability",
	 * propertyValue = "Durable"),
	 */
	private static Log log = LogFactory.getLog(DataMartLoaderBeanMDB.class);

	// get this
	public static final String LOG_REFERENCE_PREFIX = "";

	static final Logger logger = Logger.getLogger("DataMartLoaderBeanMDB");

	/**
	 * Constructor, which is public and takes no arguments.
	 */
	public DataMartLoaderBeanMDB() {
	}

	/**
	 * onMessage method, declared as public (but not final or static), with a
	 * return type of void, and with one argument of type javax.jms.Message.
	 * 
	 * Casts the incoming Message to a TextMessage and displays the text.
	 * 
	 * @param inMessage
	 *            the incoming message
	 */
	//mm removed EJB
	
	public String onMessage(Hashtable msg) {
	//	TextMessage msg = null;
		String sessionId = null, publishMessage = null;
		int uploadId = 0;
		IUploaderDAOFactory uploaderDaoFactory = null;
		Throwable throwable = null;
		try {
			//if (inMessage instanceof TextMessage) {
			//	msg = (TextMessage) inMessage;
				//publishMessage = msg.getText();
			

				String userId = (String) msg.get(I2B2_USER_ID);
//				String password = (String) msg.get(I2B2_PASSWORD);
//				boolean password_istoken = (Boolean) msg.get(I2B2_PASSWORD_ISTOKEN);
				uploadId =  (Integer) msg.get(UPLOAD_ID);

//				String fileSystemDefaultStorageResource = (String) msg
//						.get(IROD_FILESYSTEM_STORAGE_RESOURCE);

				String dsLookupDomainId = (String) msg
						.get(DS_LOOKUP_DOMAIN_ID);
				String dsLookupProjectId = (String) msg
						.get(DS_LOOKUP_PROJECT_ID);
				String dsLookupOwnerId = (String) msg
						.get(DS_LOOKUP_OWNER_ID);
				LoaderDAOFactoryHelper daoFactoryHelper = new LoaderDAOFactoryHelper(
						dsLookupDomainId, dsLookupProjectId, dsLookupOwnerId);
				uploaderDaoFactory = daoFactoryHelper.getDAOFactory()
						.getUpLoaderDAOFactory();
				
				publishMessage = (String)msg.get(I2B2_REQUEST_MSG);
	

				if(publishMessage == null)
				log.debug("MDB publishMessage is NULL");
		
			
				PublishDataRequestType publishDataReqType = getPublishDataRequestType(publishMessage);
				DataListType dataListType = publishDataReqType.getInputList()
						.getDataFile();
				// use the file in the FRC dir.
				String localUploadFile = dataListType.getLocationUri().getValue();
				
				if(localUploadFile == null)
					log.debug("MDB localUploadFile is NULL");
			
				
				updateUploadStatus(uploaderDaoFactory, uploadId, "PROCESSING",
				"");
				processUploadMessage(uploaderDaoFactory, userId, uploadId,
						localUploadFile, publishMessage);
			//	utx.commit();

				// write completed status to upload status
				log.info("Update Upload Status with Completed Status");
			//	utx.begin();
				updateUploadStatus(uploaderDaoFactory, uploadId, "COMPLETED",
						"");
			//	utx.commit();

		//	} else {
		//		logger.warning("Message of wrong type: "
		//				+ inMessage.getClass().getName());
		//	}

		} catch (Exception e) {
			logger.severe("MessageBean.onMessage: JMSException: "
					+ e.toString());
			e.printStackTrace();
			/*
			try {
				if (utx.getStatus() == Status.STATUS_ACTIVE) {
					utx.rollback();
				}
			} catch (IllegalStateException e1) {
				e1.printStackTrace();
			} catch (SecurityException e1) {
				e1.printStackTrace();
			} catch (SystemException e1) {
				e1.printStackTrace();
			}*/

		} catch (Throwable te) {
			throwable = te;
			logger.severe("MessageBean.onMessage: Exception: " + te.toString());
			te.printStackTrace();
			/*
			try {
				if (utx.getStatus() == Status.STATUS_ACTIVE) {
					utx.rollback();
				}
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (SystemException e) {
				e.printStackTrace();
			}
			*/

		} finally {
			if (throwable != null) {
				// try to write error status to upload status
				log.info("Update Upload Status with Completed Status");
				StringWriter stringWriter = new StringWriter();
				throwable.printStackTrace(new PrintWriter(stringWriter));
				try {
				//	utx.begin();
					updateUploadStatus(uploaderDaoFactory, uploadId, "ERROR",
							stringWriter.toString());
				//	utx.commit();
				} catch (Exception e) {
					log.error(
							"Failed to update 'error' status to upload status",
							e);
				}
			}
			try {
				sendResponse(uploadId, sessionId, publishMessage);
			} catch (Throwable t) {
				t.printStackTrace();
				log
						.error("Error sending response ack [" + t.getMessage()
								+ "]");
			}
		}
		return publishMessage;
		
	}

	private String createLocalFile(int uploadId, String publishMessage,
			SecurityType i2b2SecurityType, String projectId,
			String fileSystemDefaultStorageResource) throws I2B2Exception {
		String localUploadFile = null;
		try {
			PublishDataRequestType pubishDataReqType = getPublishDataRequestType(publishMessage);
			DataListType dataListType = pubishDataReqType.getInputList()
					.getDataFile();
			//TODO removed EJB
			
			//FRLocal testLocal = CRCLoaderUtil.getInstance()
			//		.getFRBeanLocalHome().create();
			FRBean testLocal = new FRBean();

			localUploadFile = testLocal.createClient(uploadId, dataListType,
					i2b2SecurityType, projectId,
					fileSystemDefaultStorageResource);
			System.out.println("localUploadFile  :" + localUploadFile);

		} catch (Exception e) {
			e.printStackTrace();
			throw new I2B2Exception("Failed to create CRCFR-EJB :["
					+ e.getMessage() + "]");
		}
		return localUploadFile;
	}

	/*
	 * @throws Throwable
	 */
	private void sendResponse(int uploadId, String sessionId,
			String publishMessage) throws Throwable {
		//mm removed EJB
		/*
		Session session = null;
		MessageProducer publisher = null;
		TextMessage message = null;
		try {

			connection = connectionFactory.createConnection();
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

			publisher = session.createProducer(responseQueue);
			message = session.createTextMessage();
			message.setJMSCorrelationID(sessionId);
			message.setText(publishMessage);
			message.setIntProperty(UPLOAD_ID, uploadId);
			publisher.send(message);

		} catch (Throwable t) {
			throw t;
		} finally {
			if (session != null) {
				try {
					session.close();
				} catch (JMSException e) {
				}

			}
			if (connection != null) {
				try {
					connection.close();
				} catch (JMSException e) {

				}
			}
		}
	*/
	}

	/**
	 * 
	 */
	public int processUploadMessage(IUploaderDAOFactory uploaderDaoFactory,
			String userId, int uploadId, String localUploadFile,
			String publishMessage) throws I2B2Exception {

		dmLoader.load(uploaderDaoFactory, userId, uploadId, localUploadFile,
				publishMessage);
		return uploadId;

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

	private PublishDataRequestType getPublishDataRequestType(
			String publishMessage) throws I2B2Exception {
		JAXBUtil jaxbUtil = edu.harvard.i2b2.crc.loader.datavo.CRCLoaderJAXBUtil
				.getJAXBUtil();
		PublishDataRequestType publishType = null;
		try {
			JAXBElement<?> jaxbElement = jaxbUtil
					.unMashallFromString(publishMessage);
			publishType = (PublishDataRequestType) jaxbElement.getValue();
		} catch (JAXBUtilException jaxbEx) {
			throw new I2B2Exception("Error processing request message "
					+ jaxbEx.getMessage(), jaxbEx);
		}
		return publishType;
	}

}
