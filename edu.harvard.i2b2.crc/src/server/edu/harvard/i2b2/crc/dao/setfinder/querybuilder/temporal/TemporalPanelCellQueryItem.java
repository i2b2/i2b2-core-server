/*
 * Copyright (c) 2006-2013 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors:
 * 		Christopher Herrick
 */
package edu.harvard.i2b2.crc.dao.setfinder.querybuilder.temporal;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBElement;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.exception.StackTraceUtil;
import edu.harvard.i2b2.common.util.axis2.ServiceClient;
import edu.harvard.i2b2.common.util.jaxb.JAXBUnWrapHelper;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtil;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.crc.datavo.CRCJAXBUtil;
import edu.harvard.i2b2.crc.datavo.i2b2message.FacilityType;
import edu.harvard.i2b2.crc.datavo.i2b2message.MessageHeaderType;
import edu.harvard.i2b2.crc.datavo.i2b2message.RequestHeaderType;
import edu.harvard.i2b2.crc.datavo.i2b2message.RequestMessageType;
import edu.harvard.i2b2.crc.datavo.i2b2message.ResponseMessageType;
import edu.harvard.i2b2.crc.datavo.i2b2message.SecurityType;
import edu.harvard.i2b2.crc.datavo.i2b2message.BodyType;
import edu.harvard.i2b2.crc.datavo.ontology.ConceptType;
import edu.harvard.i2b2.crc.datavo.ontology.XmlValueType;
import edu.harvard.i2b2.crc.datavo.pdo.PatientDataType;
import edu.harvard.i2b2.crc.datavo.pdo.PatientSet;
import edu.harvard.i2b2.crc.datavo.pdo.PatientType;
import edu.harvard.i2b2.crc.datavo.pdo.query.PatientDataResponseType;
import edu.harvard.i2b2.crc.datavo.pm.GetUserConfigurationType;
import edu.harvard.i2b2.crc.datavo.pm.ProjectType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.ItemType;
import edu.harvard.i2b2.crc.util.ItemKeyUtil;
import edu.harvard.i2b2.crc.util.QueryProcessorUtil;

/**
 * Temporal Panel Cell Query Item
 * 
 * <P>
 * Cell Query query object that wraps the item tag found in the query definition
 * xml for cell query items. It roughly corresponds to a panel in the query UI. Panel is responsible
 * for organizing the sql that comes back from individual panel items - sql from
 * items should be logically or'd together. It is also the container that holds
 * the panel constraint types - occurrence, dates, and exclude.
 * 
 * @author Christopher Herrick
 * 
 */
public class TemporalPanelCellQueryItem extends TemporalPanelItem {

	private JAXBUtil jaxbUtil = null; 
	private static Log log = LogFactory.getLog(TemporalPanelCellQueryItem.class);
	private XmlValueType requestXml = null;
	private String cellUrl = null;
	private String patientNums = null;
	
	public TemporalPanelCellQueryItem(TemporalPanel parent, ItemType item, ConceptType conceptType)
			throws I2B2Exception {
		super(parent, item, conceptType);
		parseCellItem();
	}

	protected void parseCellItem() throws I2B2Exception {
		super.parseItem();

		cellUrl = conceptType.getDimcode();
		if (cellUrl!=null&&cellUrl.toLowerCase().startsWith(ItemKeyUtil.ITEM_KEY_CELLID))
			cellUrl = cellUrl.substring(ItemKeyUtil.ITEM_KEY_CELLID.length());
		
		if (conceptType.getMetadataxml()!=null){
			requestXml = conceptType.getMetadataxml();
		}

		jaxbUtil = CRCJAXBUtil.getJAXBUtil();		
	}


	@Override
	protected String buildSql() throws I2B2DAOException {
		try {
			this.callCellUrlWithRequest();
			this.dimCode = patientNums;
		} catch (Exception e) {
			log.debug(e.getStackTrace());
			e.printStackTrace();
			return "";
		}		
		
		if (returnEncounterNum()||
				returnInstanceNum()||
				this.hasItemDateConstraint()||
				this.hasModiferConstraint()||
				this.hasPanelDateConstraint()||
				this.hasPanelOccurrenceConstraint()||
				this.hasValueConstraint()
				//||parent.isTimingQuery()
				){
			return super.buildSql();
		}
		else{
			return "select "
					+ this.factTableColumn + " from " + noLockSqlServer
					+ parent.getDatabaseSchema() + this.tableName
					+ "  " + " where " + this.columnName + " "
					+ this.operator + " " + this.dimCode
					+ "";
		}
	}

	@Override
	protected String getJoinTable() {
		if (returnInstanceNum()||
				hasItemDateConstraint()||
				hasPanelDateConstraint()||
				hasValueConstraint()||
				hasPanelOccurrenceConstraint()) {
			return "observation_fact";
		} else if (returnEncounterNum()
				//||parent.isTimingQuery()
				) {
			return "visit_dimension";
		} else {
			return "patient_dimension";
		}
	}
	
	
	public PatientSet getPatientSetFromResponseXML(String responseXML)
			throws Exception {
		JAXBUtil jaxbUtil = CRCJAXBUtil.getJAXBUtil();

		@SuppressWarnings("rawtypes")
		JAXBElement jaxbElement = jaxbUtil.unMashallFromString(responseXML);
		ResponseMessageType messageType = (ResponseMessageType) jaxbElement
				.getValue();
		BodyType bodyType = messageType.getMessageBody();
		PatientDataResponseType responseType = (PatientDataResponseType) new JAXBUnWrapHelper()
				.getObjectByClass(bodyType.getAny(),
						PatientDataResponseType.class);
		PatientDataType patientDataType = responseType.getPatientData();

		PatientSet patientSet = patientDataType.getPatientSet();

		return patientSet;
	}
	
	public ProjectType callCellUrlWithRequest() throws Exception {
		RequestMessageType requestMessageType = getI2B2RequestMessage(parent.getRequestorSecurityType(), parent.getProjectId());
		OMElement requestElement = null;
		ProjectType projectType = null;
		try {
			
			String requestXmlString = createRequestXmlString();			
			requestElement = buildOMElement(requestMessageType, requestXmlString);
			log.debug("call cell request xml " + requestElement);
			
			String response = ServiceClient.sendREST(cellUrl, requestElement);

			log.debug("Got Response");
			
			PatientSet pSet = getPatientSetFromResponseXML(response);
			
			if (pSet!=null){
				StringBuffer patientList = new StringBuffer();
				patientList.append("(");
				boolean first = true;
				for (PatientType patient : pSet.getPatient()){
					if (!first){
						patientList.append(", ");
					}
					else
						first = false;
					
					if (patient.getPatientId()!=null&&patient.getPatientId().getValue()!=null)
						patientList.append(patient.getPatientId().getValue());
				}
				patientList.append(")");
				
				patientNums = patientList.toString();
			}
		} catch (XMLStreamException e) {
			e.printStackTrace();
			throw new I2B2Exception("" + StackTraceUtil.getStackTrace(e));
		} catch (Exception e) {
			e.printStackTrace();
			throw new I2B2Exception("" + StackTraceUtil.getStackTrace(e));
		} 

		log.debug("Returning ProjectType");
		return projectType;
	}

	private OMElement buildOMElement(RequestMessageType requestMessageType, String requestXml)
			throws XMLStreamException, JAXBUtilException {
		StringWriter strWriter = new StringWriter();
		edu.harvard.i2b2.crc.datavo.i2b2message.ObjectFactory hiveof = new edu.harvard.i2b2.crc.datavo.i2b2message.ObjectFactory();
		jaxbUtil.marshaller(hiveof.createRequest(requestMessageType), strWriter);
		
		//insert request
		String msgBody = "<message_body/>";
		String xmlrequest = strWriter.toString();
		int index = xmlrequest.indexOf(msgBody);
		if (index>=0){
			xmlrequest = xmlrequest.substring(0, index) + "<message_body>" + requestXml + "</message_body>" + xmlrequest.substring(index + msgBody.length()+1);
		}

		StringReader strReader = new StringReader(xmlrequest);
		XMLInputFactory xif = XMLInputFactory.newInstance();
		XMLStreamReader reader = xif.createXMLStreamReader(strReader);
		StAXOMBuilder builder = new StAXOMBuilder(reader);
		OMElement request = builder.getDocumentElement();
		return request;
	}

	private RequestMessageType getI2B2RequestMessage(SecurityType securityType,  String projectId) throws Exception {
		QueryProcessorUtil queryUtil = QueryProcessorUtil.getInstance();
		MessageHeaderType messageHeaderType = (MessageHeaderType) queryUtil
				.getSpringBeanFactory().getBean("message_header");
		messageHeaderType.setSecurity(securityType);
		messageHeaderType.setProjectId(projectId);

		messageHeaderType.setReceivingApplication(messageHeaderType
				.getSendingApplication());
		FacilityType facilityType = new FacilityType();
		facilityType.setFacilityName("sample");
		messageHeaderType.setSendingFacility(facilityType);
		messageHeaderType.setReceivingFacility(facilityType);
		// build message body
		// GetUserInfoType getUserInfoType = null;
		GetUserConfigurationType userConfig = new GetUserConfigurationType();
		userConfig.getProject().add(projectId);

		RequestMessageType requestMessageType = new RequestMessageType();

		BodyType bodyType = new BodyType();
		requestMessageType.setMessageBody(bodyType);

		requestMessageType.setMessageHeader(messageHeaderType);

		RequestHeaderType requestHeader = new RequestHeaderType();
		requestHeader.setResultWaittimeMs(180000);
		requestMessageType.setRequestHeader(requestHeader);

		return requestMessageType;

	}
	
	private String createRequestXmlString(){
		String xmlRootString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		StringBuffer requestXmlString= new StringBuffer();
		if (requestXml!=null){
			if (requestXml.getAny().size()>0&&requestXml.getAny().get(0).getNodeName().toLowerCase().equals("cellrequest")){
				Element request = requestXml.getAny().get(0);
				for (int i = 0; i < request.getChildNodes().getLength(); i++){
					String nodeString = convertNodeToString(request.getChildNodes().item(i));
					if (nodeString.startsWith(xmlRootString)){
						nodeString = nodeString.substring(xmlRootString.length()).trim();
					}
					
					if (nodeString.length()>0) {
						requestXmlString.append(nodeString);
					}
				}
			}
		}
		return requestXmlString.toString();
	}
	
	
	private String convertNodeToString(Node request) {
		StringWriter writer = new StringWriter();
		Transformer transformer;
		try {
			transformer = TransformerFactory.newInstance().newTransformer();
			transformer.transform(new DOMSource(request), new StreamResult(writer));
		} catch (TransformerConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerFactoryConfigurationError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return writer.toString();
	}
	
}
