package edu.harvard.i2b2.ontology.delegate.crc;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBElement;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import edu.harvard.i2b2.common.util.axis2.ServiceClient;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.jaxb.JAXBUnWrapHelper;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.ontology.datavo.crc.setfinder.query.InstanceRequestType;
import edu.harvard.i2b2.ontology.datavo.crc.setfinder.query.ItemType;
import edu.harvard.i2b2.ontology.datavo.crc.setfinder.query.MasterDeleteRequestType;
import edu.harvard.i2b2.ontology.datavo.crc.setfinder.query.MasterInstanceResultResponseType;
import edu.harvard.i2b2.ontology.datavo.crc.setfinder.query.MasterResponseType;
import edu.harvard.i2b2.ontology.datavo.crc.setfinder.query.PanelType;
import edu.harvard.i2b2.ontology.datavo.crc.setfinder.query.PsmQryHeaderType;
import edu.harvard.i2b2.ontology.datavo.crc.setfinder.query.PsmRequestTypeType;
import edu.harvard.i2b2.ontology.datavo.crc.setfinder.query.QueryDefinitionRequestType;
import edu.harvard.i2b2.ontology.datavo.crc.setfinder.query.QueryDefinitionType;
import edu.harvard.i2b2.ontology.datavo.crc.setfinder.query.ResultOutputOptionListType;
import edu.harvard.i2b2.ontology.datavo.crc.setfinder.query.ResultOutputOptionType;
import edu.harvard.i2b2.ontology.datavo.crc.setfinder.query.ResultResponseType;
import edu.harvard.i2b2.ontology.datavo.crcloader.query.DataFormatType;
import edu.harvard.i2b2.ontology.datavo.crcloader.query.DataListType;
import edu.harvard.i2b2.ontology.datavo.crcloader.query.GetUploadInfoRequestType;
import edu.harvard.i2b2.ontology.datavo.crcloader.query.InputOptionListType;
import edu.harvard.i2b2.ontology.datavo.crcloader.query.LoadDataResponseType;
import edu.harvard.i2b2.ontology.datavo.crcloader.query.LoadOptionType;
import edu.harvard.i2b2.ontology.datavo.crcloader.query.LoadType;
import edu.harvard.i2b2.ontology.datavo.crcloader.query.OutputOptionListType;
import edu.harvard.i2b2.ontology.datavo.crcloader.query.PublishDataRequestType;
import edu.harvard.i2b2.ontology.datavo.i2b2message.BodyType;
import edu.harvard.i2b2.ontology.datavo.i2b2message.FacilityType;
import edu.harvard.i2b2.ontology.datavo.i2b2message.MessageHeaderType;
import edu.harvard.i2b2.ontology.datavo.i2b2message.RequestHeaderType;
import edu.harvard.i2b2.ontology.datavo.i2b2message.RequestMessageType;
import edu.harvard.i2b2.ontology.datavo.i2b2message.ResponseMessageType;
import edu.harvard.i2b2.ontology.datavo.i2b2message.ResultStatusType;
import edu.harvard.i2b2.ontology.datavo.i2b2message.SecurityType;
import edu.harvard.i2b2.ontology.util.OntologyJAXBUtil;
import edu.harvard.i2b2.ontology.util.OntologyUtil;

public class CallCRCUtil {

	//private SecurityType securityType = null;
	//private String projectId = null;
	private static String crcUrl = null;
	static OntologyUtil ontologyUtil = OntologyUtil.getInstance();

	private static Log log = LogFactory.getLog(CallCRCUtil.class);

	public static LoadDataResponseType callCRCUpload(String conceptFileName,
			String loadLabel, boolean synchronizeAllFlag,  SecurityType securityType,  String projectId) throws I2B2Exception {
		RequestMessageType requestMessageType = buildUploaderRequestMessage(
				conceptFileName, loadLabel, synchronizeAllFlag, securityType, projectId);
		LoadDataResponseType loadDataResponseType = null;
		try {
			OMElement requestElement = buildOMElement(requestMessageType);
			log.debug("CRC Ontology call's request xml "
					+ requestElement);
			String response = ServiceClient.sendREST(crcUrl + "/publishDataRequest", requestElement);
			log.debug("CRC Ontology call's reponse xml " + response);
			loadDataResponseType = getGetPublishDataResponseMessage(response
					);
		} catch (JAXBUtilException jaxbEx) {
			throw new I2B2Exception("Error in CRC upload ", jaxbEx);
		} catch (XMLStreamException e) {
			throw new I2B2Exception("Error in CRC upload ", e);

		} catch (Exception e) {

			throw new I2B2Exception("Error in CRC upload ", e);
		}
		return loadDataResponseType;
	}

	public static MasterInstanceResultResponseType callSetfinderQuery(String conceptKey,  SecurityType securityType,  String projectId) throws I2B2Exception {
		RequestMessageType requestMessageType = buildSetfinderQueryRequestMessage(conceptKey, securityType, projectId);
		MasterInstanceResultResponseType masterInstanceResultResponseType = null;
		try {
			OMElement requestElement = buildOMElement(requestMessageType);
			log.debug("CRC Ontology call's request xml "
					+ requestElement);
			String response = ServiceClient.sendREST(crcUrl + "/request", requestElement);
			log.debug("CRC Ontology call's reponse xml " + response);
			masterInstanceResultResponseType = getMasterInstanceResultResponseMessage(response
					);
		} catch (JAXBUtilException jaxbEx) {
			throw new I2B2Exception("Error in CRC setfinder ", jaxbEx);
		} catch (XMLStreamException e) {
			throw new I2B2Exception("Error in CRC setfinder ", e);

		} catch (Exception e) {

			throw new I2B2Exception("Error in CRC setfinder ", e);
		}	
		return masterInstanceResultResponseType;
	}

	public static MasterResponseType callDeleteMasterQuery(String userId, String queryMasterId,  SecurityType securityType,  String projectId)
			throws I2B2Exception {
		RequestMessageType requestMessageType = buildDeleteSetfinderStatusRequestMessage(userId, queryMasterId, securityType, projectId);
		MasterResponseType masterResponseType = null;
		try {
			OMElement requestElement = buildOMElement(requestMessageType);
			log.debug("CRC setfinder query delete call's request xml "
					+ requestElement);
			String response = ServiceClient.sendREST(crcUrl + "/request", requestElement);
			log.debug("CRC setfinder query delete call's request xml " + response);
			masterResponseType = getMasterResponseMessage(response
					);

		} catch (JAXBUtilException jaxbEx) {
			throw new I2B2Exception("Error in CRC upload ", jaxbEx);
		} catch (XMLStreamException e) {
			throw new I2B2Exception("Error in CRC upload ", e);

		} catch (Exception e) {

			throw new I2B2Exception("Error in CRC upload ", e);
		} 
		return masterResponseType;
	}

	public static ResultResponseType callCRCQueryStatus(String queryInstanceId,  SecurityType securityType,  String projectId)
			throws I2B2Exception {
		RequestMessageType requestMessageType = buildSetfinderStatusRequestMessage(queryInstanceId, securityType, projectId);
		ResultResponseType resultResponseType = null;
		try {
			OMElement requestElement = buildOMElement(requestMessageType);
			log.debug("CRC setfinder query status call's request xml "
					+ requestElement);
			String response = ServiceClient.sendREST(crcUrl + "/request", requestElement);
			log.debug("CRC setfinder query status call's request xml " + response);
			resultResponseType = getResultResponseMessage(response
					.toString());

		} catch (JAXBUtilException jaxbEx) {
			throw new I2B2Exception("Error in CRC upload ", jaxbEx);
		} catch (XMLStreamException e) {
			throw new I2B2Exception("Error in CRC upload ", e);

		} catch (Exception e) {

			throw new I2B2Exception("Error in CRC upload ", e);
		} 
		return resultResponseType;
	}

	public static ResultResponseType pollQueryStatus(String instanceId,   SecurityType securityType,  String projectId)
			throws I2B2Exception {
		SetfinderQueryStatusRunner setfinderQueryStatusRunner = new SetfinderQueryStatusRunner();
		//setfinderQueryStatusRunner.setCRCUtil(this);
		setfinderQueryStatusRunner.setQueryInstanceId(instanceId);
		setfinderQueryStatusRunner.setSecurityType(securityType);
		setfinderQueryStatusRunner.setProjectId(projectId);
		Thread t = new Thread(setfinderQueryStatusRunner);
		t.start();
		while (setfinderQueryStatusRunner.isNotDone()) {
			try {
				Thread.sleep(60000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		String exceptionMsg = setfinderQueryStatusRunner.getExceptionMsg();
		if (exceptionMsg != null) {
			throw new I2B2Exception("Error while getting status of upload ["
					+ exceptionMsg + "]");
		}
		ResultResponseType resultResponse = setfinderQueryStatusRunner.getQueryInstanceStatusResponseType();


		return resultResponse;
	}


	public static LoadDataResponseType callCRCUploadStatus(String uploadId,  SecurityType securityType,  String projectId)
			throws I2B2Exception {
		RequestMessageType requestMessageType = buildUploaderRequestMessage(uploadId, securityType, projectId);
		LoadDataResponseType loadDataResponseType = null;
		try {
			OMElement requestElement = buildOMElement(requestMessageType);
			log.debug("CRC Ontology call's request xml "
					+ requestElement);
			String response = ServiceClient.sendREST(crcUrl + "/getLoadDataStatusRequest", requestElement);
			log.debug("CRC Ontology call's request xml " + response);
			loadDataResponseType = getGetPublishDataResponseMessage(response);

		} catch (JAXBUtilException jaxbEx) {
			throw new I2B2Exception("Error in CRC upload ", jaxbEx);
		} catch (XMLStreamException e) {
			throw new I2B2Exception("Error in CRC upload ", e);

		} catch (Exception e) {

			throw new I2B2Exception("Error in CRC upload ", e);
		}
		return loadDataResponseType;
	}

	public static  LoadDataResponseType pollUploadStatus(String uploadId)
			throws I2B2Exception {
		UploadStatusRunner uploadStatusRunner = new UploadStatusRunner();
		//uploadStatusRunner.setCRCUtil(this);
		uploadStatusRunner.setUploadId(uploadId);
		Thread t = new Thread(uploadStatusRunner);
		t.start();
		while (uploadStatusRunner.isNotDone()) {
			try {
				Thread.sleep(60000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		String exceptionMsg = uploadStatusRunner.getExceptionMsg();
		if (exceptionMsg != null) {
			throw new I2B2Exception("Error while getting status of upload ["
					+ exceptionMsg + "]");
		}
		LoadDataResponseType loadDataResponse = uploadStatusRunner
				.getLodDataResponseType();

		return loadDataResponse;
	}

	public static  RequestMessageType buildUploaderRequestMessage(
			String conceptFileName, String loadLabel, boolean synchronizeAllFlag, SecurityType securityType,  String projectId) {
		PublishDataRequestType publishDataRequest = new PublishDataRequestType();
		DataListType dataListType = new DataListType();
		DataListType.LocationUri locationUri = new DataListType.LocationUri();
		locationUri.setValue(conceptFileName);
		locationUri.setProtocolName("FR");
		dataListType.setLocationUri(locationUri);

		dataListType.setDataFormatType(DataFormatType.PDO);
		InputOptionListType inputListType = new InputOptionListType();
		inputListType.setDataFile(dataListType);
		dataListType.setLoadLabel(loadLabel);
		dataListType.setSourceSystemCd("edu.harvard.i2b2.ontology.sync");
		publishDataRequest.setInputList(inputListType);

		LoadType loadType = new LoadType();
		loadType.setClearTempLoadTables(true);
		LoadOptionType loadOption = new LoadOptionType();
		loadOption.setIgnoreBadData(true);
		if (synchronizeAllFlag) {
			loadOption.setDeleteExistingData(true);
		}
		loadType.setLoadConceptSet(loadOption);
		loadType.setLoadObserverSet(loadOption);
		loadType.setLoadModifierSet(loadOption);
		publishDataRequest.setLoadList(loadType);
		OutputOptionListType outOption = new OutputOptionListType();
		publishDataRequest.setOutputList(outOption);

		MessageHeaderType messageHeaderType = (MessageHeaderType) ontologyUtil
				.getSpringBeanFactory().getBean("message_header");
		messageHeaderType.setSecurity(securityType);
		messageHeaderType.setProjectId(projectId);

		messageHeaderType.setReceivingApplication(messageHeaderType
				.getSendingApplication());
		FacilityType facilityType = new FacilityType();
		facilityType.setFacilityName("sample");
		messageHeaderType.setSendingFacility(facilityType);
		messageHeaderType.setReceivingFacility(facilityType);

		RequestMessageType requestMessageType = new RequestMessageType();
		edu.harvard.i2b2.ontology.datavo.crcloader.query.ObjectFactory of = new edu.harvard.i2b2.ontology.datavo.crcloader.query.ObjectFactory();
		BodyType bodyType = new BodyType();
		bodyType.getAny().add(of.createPublishDataRequest(publishDataRequest));
		requestMessageType.setMessageBody(bodyType);

		requestMessageType.setMessageHeader(messageHeaderType);

		RequestHeaderType requestHeader = new RequestHeaderType();
		requestHeader.setResultWaittimeMs(180000); //(3000);
		requestMessageType.setRequestHeader(requestHeader);
		return requestMessageType;
	}

	public static  RequestMessageType buildUploaderRequestMessage(String uploadId,  SecurityType securityType,  String projectId) {
		GetUploadInfoRequestType getUploadInfo = new GetUploadInfoRequestType();
		getUploadInfo.setLoadId(uploadId);
		getUploadInfo.setUserId(securityType.getUsername());

		MessageHeaderType messageHeaderType = (MessageHeaderType) ontologyUtil
				.getSpringBeanFactory().getBean("message_header");
		messageHeaderType.setSecurity(securityType);
		messageHeaderType.setProjectId(projectId);

		messageHeaderType.setReceivingApplication(messageHeaderType
				.getSendingApplication());
		FacilityType facilityType = new FacilityType();
		facilityType.setFacilityName("sample");
		messageHeaderType.setSendingFacility(facilityType);
		messageHeaderType.setReceivingFacility(facilityType);

		RequestMessageType requestMessageType = new RequestMessageType();
		edu.harvard.i2b2.ontology.datavo.crcloader.query.ObjectFactory of = new edu.harvard.i2b2.ontology.datavo.crcloader.query.ObjectFactory();
		BodyType bodyType = new BodyType();
		bodyType.getAny().add(of.createGetUploadInfoRequest(getUploadInfo));
		requestMessageType.setMessageBody(bodyType);

		requestMessageType.setMessageHeader(messageHeaderType);

		RequestHeaderType requestHeader = new RequestHeaderType();
		requestHeader.setResultWaittimeMs(180000); //3000);
		requestMessageType.setRequestHeader(requestHeader);
		return requestMessageType;
	}

	public static RequestMessageType buildSetfinderStatusRequestMessage(String queryInstanceId,  SecurityType securityType,  String projectId) {
		InstanceRequestType instanceRequestType = new InstanceRequestType();
		instanceRequestType.setQueryInstanceId(queryInstanceId);

		MessageHeaderType messageHeaderType = (MessageHeaderType) ontologyUtil
				.getSpringBeanFactory().getBean("message_header");
		messageHeaderType.setSecurity(securityType);
		messageHeaderType.setProjectId(projectId);

		messageHeaderType.setReceivingApplication(messageHeaderType
				.getSendingApplication());
		FacilityType facilityType = new FacilityType();
		facilityType.setFacilityName("sample");
		messageHeaderType.setSendingFacility(facilityType);
		messageHeaderType.setReceivingFacility(facilityType);

		RequestMessageType requestMessageType = new RequestMessageType();

		edu.harvard.i2b2.ontology.datavo.crc.setfinder.query.ObjectFactory of = new edu.harvard.i2b2.ontology.datavo.crc.setfinder.query.ObjectFactory();
		BodyType bodyType = new BodyType();
		PsmQryHeaderType psm = new PsmQryHeaderType();
		psm.setRequestType(PsmRequestTypeType.CRC_QRY_GET_QUERY_RESULT_INSTANCE_LIST_FROM_QUERY_INSTANCE_ID);
		bodyType.getAny().add(of.createPsmheader(psm));

		bodyType.getAny().add(of.createRequest(instanceRequestType));
		requestMessageType.setMessageBody(bodyType);

		requestMessageType.setMessageHeader(messageHeaderType);

		RequestHeaderType requestHeader = new RequestHeaderType();
		requestHeader.setResultWaittimeMs(220000); //3000);
		requestMessageType.setRequestHeader(requestHeader);
		return requestMessageType;
	}

	public static RequestMessageType buildDeleteSetfinderStatusRequestMessage(String userId, String queryMasterId,  SecurityType securityType,  String projectId) {
		MasterDeleteRequestType masterDeleteReqType = new MasterDeleteRequestType();
		masterDeleteReqType.setQueryMasterId(queryMasterId);
		masterDeleteReqType.setUserId(userId); 

		MessageHeaderType messageHeaderType = (MessageHeaderType) ontologyUtil
				.getSpringBeanFactory().getBean("message_header");
		messageHeaderType.setSecurity(securityType);
		messageHeaderType.setProjectId(projectId);

		messageHeaderType.setReceivingApplication(messageHeaderType
				.getSendingApplication());
		FacilityType facilityType = new FacilityType();
		facilityType.setFacilityName("sample");
		messageHeaderType.setSendingFacility(facilityType);
		messageHeaderType.setReceivingFacility(facilityType);

		RequestMessageType requestMessageType = new RequestMessageType();

		edu.harvard.i2b2.ontology.datavo.crc.setfinder.query.ObjectFactory of = new edu.harvard.i2b2.ontology.datavo.crc.setfinder.query.ObjectFactory();
		BodyType bodyType = new BodyType();
		PsmQryHeaderType psm = new PsmQryHeaderType();
		psm.setRequestType(PsmRequestTypeType.CRC_QRY_DELETE_QUERY_MASTER);
		bodyType.getAny().add(of.createPsmheader(psm));

		bodyType.getAny().add(of.createRequest(masterDeleteReqType));
		requestMessageType.setMessageBody(bodyType);

		requestMessageType.setMessageHeader(messageHeaderType);

		RequestHeaderType requestHeader = new RequestHeaderType();
		requestHeader.setResultWaittimeMs(180000); //(3000);
		requestMessageType.setRequestHeader(requestHeader);
		return requestMessageType;
	}

	public static RequestMessageType buildSetfinderQueryRequestMessage(String itemKey,  SecurityType securityType,  String projectId) {
		QueryDefinitionType queryDef = new QueryDefinitionType();
		PanelType panelType = new PanelType();
		ItemType itemType = new ItemType();
		itemType.setItemKey(itemKey);
		panelType.getItem().add(itemType);
		queryDef.getPanel().add(panelType);

		queryDef.setQueryName(itemKey.substring(0,(itemKey.length()>10)?9:itemKey.length()) + System.currentTimeMillis());
		QueryDefinitionRequestType queryDefinitionRequestType = new QueryDefinitionRequestType();
		ResultOutputOptionListType resultOutputOptionListType = new ResultOutputOptionListType();
		ResultOutputOptionType resultOutputOptionType = new ResultOutputOptionType();
		resultOutputOptionType.setName("PATIENT_COUNT_XML");
		resultOutputOptionListType.getResultOutput().add(resultOutputOptionType);
		queryDefinitionRequestType.setQueryDefinition(queryDef);
		queryDefinitionRequestType.setResultOutputList(resultOutputOptionListType);

		MessageHeaderType messageHeaderType = (MessageHeaderType) ontologyUtil
				.getSpringBeanFactory().getBean("message_header");
		messageHeaderType.setSecurity(securityType);
		messageHeaderType.setProjectId(projectId);

		messageHeaderType.setReceivingApplication(messageHeaderType
				.getSendingApplication());
		FacilityType facilityType = new FacilityType();
		facilityType.setFacilityName("sample");
		messageHeaderType.setSendingFacility(facilityType);
		messageHeaderType.setReceivingFacility(facilityType);

		RequestMessageType requestMessageType = new RequestMessageType();
		edu.harvard.i2b2.ontology.datavo.crc.setfinder.query.ObjectFactory of = new edu.harvard.i2b2.ontology.datavo.crc.setfinder.query.ObjectFactory();
		BodyType bodyType = new BodyType();
		PsmQryHeaderType psm = new PsmQryHeaderType();
		psm.setRequestType(PsmRequestTypeType.CRC_QRY_RUN_QUERY_INSTANCE_FROM_QUERY_DEFINITION);
		bodyType.getAny().add(of.createPsmheader(psm));
		bodyType.getAny().add(of.createRequest(queryDefinitionRequestType));
		requestMessageType.setMessageBody(bodyType);

		requestMessageType.setMessageHeader(messageHeaderType);

		RequestHeaderType requestHeader = new RequestHeaderType();
		requestHeader.setResultWaittimeMs(180000); //3000);
		requestMessageType.setRequestHeader(requestHeader);
		return requestMessageType;
	}

	private static  LoadDataResponseType getGetPublishDataResponseMessage(
			String responseXml) throws JAXBUtilException, I2B2Exception {
		JAXBElement responseJaxb = OntologyJAXBUtil.getJAXBUtil()
				.unMashallFromString(responseXml);
		ResponseMessageType r = (ResponseMessageType) responseJaxb.getValue();
		log.debug("CRC's ontology call response xml" + responseXml);

		JAXBUnWrapHelper helper = new JAXBUnWrapHelper();
		ResultStatusType rt = r.getResponseHeader().getResultStatus();
		if (rt.getStatus().getType().equals("ERROR")) {
			throw new I2B2Exception(rt.getStatus().getValue());
		}
		LoadDataResponseType loadDataResponseType = (LoadDataResponseType) helper
				.getObjectByClass(r.getMessageBody().getAny(),
						LoadDataResponseType.class);

		return loadDataResponseType;
	}

	private static MasterInstanceResultResponseType getMasterInstanceResultResponseMessage(
			String responseXml) throws JAXBUtilException, I2B2Exception {
		JAXBElement responseJaxb = OntologyJAXBUtil.getJAXBUtil()
				.unMashallFromString(responseXml);
		ResponseMessageType r = (ResponseMessageType) responseJaxb.getValue();
		log.debug("CRC's ontology call response xml" + responseXml);

		JAXBUnWrapHelper helper = new JAXBUnWrapHelper();
		ResultStatusType rt = r.getResponseHeader().getResultStatus();
		if (rt.getStatus().getType().equals("ERROR")) {
			throw new I2B2Exception(rt.getStatus().getValue());
		}
		MasterInstanceResultResponseType masterInstanceResultResponseType = (MasterInstanceResultResponseType) helper
				.getObjectByClass(r.getMessageBody().getAny(),
						MasterInstanceResultResponseType.class);

		return masterInstanceResultResponseType;
	}

	private static MasterResponseType getMasterResponseMessage(
			String responseXml) throws JAXBUtilException, I2B2Exception {
		JAXBElement responseJaxb = OntologyJAXBUtil.getJAXBUtil()
				.unMashallFromString(responseXml);
		ResponseMessageType r = (ResponseMessageType) responseJaxb.getValue();
		log.debug("CRC's ontology call response xml" + responseXml);

		JAXBUnWrapHelper helper = new JAXBUnWrapHelper();
		ResultStatusType rt = r.getResponseHeader().getResultStatus();
		if (rt.getStatus().getType().equals("ERROR")) {
			throw new I2B2Exception(rt.getStatus().getValue());
		}
		MasterResponseType masterResponseType = (MasterResponseType) helper
				.getObjectByClass(r.getMessageBody().getAny(),
						MasterResponseType.class);

		return masterResponseType;
	}

	private static ResultResponseType getResultResponseMessage(
			String responseXml) throws JAXBUtilException, I2B2Exception {
		JAXBElement responseJaxb = OntologyJAXBUtil.getJAXBUtil()
				.unMashallFromString(responseXml);
		ResponseMessageType r = (ResponseMessageType) responseJaxb.getValue();
		log.debug("CRC's ontology call response xml" + responseXml);

		JAXBUnWrapHelper helper = new JAXBUnWrapHelper();
		ResultStatusType rt = r.getResponseHeader().getResultStatus();
		if (rt.getStatus().getType().equals("ERROR")) {
			throw new I2B2Exception(rt.getStatus().getValue());
		}
		ResultResponseType instanceResultResponseType = (ResultResponseType) helper
				.getObjectByClass(r.getMessageBody().getAny(),
						ResultResponseType.class);

		return instanceResultResponseType;
	}

	private static OMElement buildOMElement(RequestMessageType requestMessageType)
			throws XMLStreamException, JAXBUtilException {
		StringWriter strWriter = new StringWriter();
		edu.harvard.i2b2.ontology.datavo.i2b2message.ObjectFactory hiveof = new edu.harvard.i2b2.ontology.datavo.i2b2message.ObjectFactory();
		OntologyJAXBUtil.getJAXBUtil().marshaller(
				hiveof.createRequest(requestMessageType), strWriter);
		// getOMElement from message
		OMFactory fac = OMAbstractFactory.getOMFactory();

		StringReader strReader = new StringReader(strWriter.toString());
		XMLInputFactory xif = XMLInputFactory.newInstance();
		XMLStreamReader reader = xif.createXMLStreamReader(strReader);
		StAXOMBuilder builder = new StAXOMBuilder(reader);
		OMElement request = builder.getDocumentElement();
		return request;
	}


}
