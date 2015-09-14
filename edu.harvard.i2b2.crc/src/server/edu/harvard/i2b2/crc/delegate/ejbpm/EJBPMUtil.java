package edu.harvard.i2b2.crc.delegate.ejbpm;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;

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
import edu.harvard.i2b2.common.exception.StackTraceUtil;
import edu.harvard.i2b2.common.util.jaxb.JAXBUnWrapHelper;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.crc.datavo.CRCJAXBUtil;
import edu.harvard.i2b2.crc.datavo.i2b2message.BodyType;
import edu.harvard.i2b2.crc.datavo.i2b2message.FacilityType;
import edu.harvard.i2b2.crc.datavo.i2b2message.MessageHeaderType;
import edu.harvard.i2b2.crc.datavo.i2b2message.RequestHeaderType;
import edu.harvard.i2b2.crc.datavo.i2b2message.RequestMessageType;
import edu.harvard.i2b2.crc.datavo.i2b2message.ResponseHeaderType;
import edu.harvard.i2b2.crc.datavo.i2b2message.ResponseMessageType;
import edu.harvard.i2b2.crc.datavo.i2b2message.SecurityType;
import edu.harvard.i2b2.crc.datavo.i2b2message.StatusType;
import edu.harvard.i2b2.crc.datavo.pm.ConfigureType;
import edu.harvard.i2b2.crc.datavo.pm.GetUserConfigurationType;
import edu.harvard.i2b2.crc.datavo.pm.ObjectFactory;
import edu.harvard.i2b2.crc.datavo.pm.ParamType;
import edu.harvard.i2b2.crc.datavo.pm.ProjectType;
import edu.harvard.i2b2.crc.datavo.pm.RoleType;
import edu.harvard.i2b2.crc.datavo.pm.RolesType;
import edu.harvard.i2b2.crc.datavo.pm.UserType;
import edu.harvard.i2b2.crc.util.QueryProcessorUtil;

public class EJBPMUtil {

	public static String LOCKEDOUT = "LOCKEDOUT";

	private static Log log = LogFactory.getLog(EJBPMUtil.class);

	public static void setUserLockedParam(String userName,
			String paramName, String lockDate, SecurityType securityType, String projectId, String ontologyUrl ) throws I2B2Exception {

		ParamType paramType = new ParamType();
		paramType.setName(LOCKEDOUT);
		paramType.setValue(lockDate);
		paramType.setDatatype("T");

		ObjectFactory of = new ObjectFactory();
		BodyType bodyType = new BodyType();

		ProjectType projectType1 = of.createProjectType();
		projectType1.getParam().add(paramType);
		projectType1.setUserName(userName);
		projectType1.setId(projectId);
		bodyType.getAny().add(of.createSetProjectUserParam(projectType1));
		RequestMessageType requestMessageType = getI2B2RequestMessage(bodyType, securityType, projectId);
		OMElement requestElement = null;

		try {
			requestElement = buildOMElement(requestMessageType);
			log.debug("CRC PM call's request xml " + requestElement);
			//OMElement response = getServiceClient().sendReceive(requestElement);
			String response = ServiceClient.sendREST(ontologyUrl, requestElement);
			// :TODO check the status in the response

			// projectType = getUserProjectFromResponse(response.toString());
		} catch (XMLStreamException e) {
			e.printStackTrace();
			throw new I2B2Exception("" + StackTraceUtil.getStackTrace(e));
		} catch (JAXBUtilException e) {
			e.printStackTrace();
			throw new I2B2Exception("" + StackTraceUtil.getStackTrace(e));
		} catch (Exception  e) {
			e.printStackTrace();
			throw new I2B2Exception(
					"AxisFault error when setting lockedout param for user "
							+ StackTraceUtil.getStackTrace(e));
		} 
	}

	public static ProjectType callUserProject(SecurityType securityType, String projectId,  String ontologyUrl ) throws AxisFault, I2B2Exception {
		// build message body
		// GetUserInfoType getUserInfoType = null;
		GetUserConfigurationType userConfig = new GetUserConfigurationType();
		userConfig.getProject().add(projectId);

		ObjectFactory of = new ObjectFactory();
		BodyType bodyType = new BodyType();
		bodyType.getAny().add(of.createGetUserConfiguration(userConfig));
		RequestMessageType requestMessageType = getI2B2RequestMessage(bodyType, securityType, projectId);
		OMElement requestElement = null;
		ProjectType projectType = null;
		try {
			requestElement = buildOMElement(requestMessageType);
			log.debug("CRC PM call's request xml " + requestElement);
			//OMElement response = getServiceClient().sendReceive(requestElement);
			String response = ServiceClient.sendREST(ontologyUrl, requestElement );
			projectType = getUserProjectFromResponse(response, securityType, projectId);
		} catch (XMLStreamException e) {
			e.printStackTrace();
			throw new I2B2Exception("" + StackTraceUtil.getStackTrace(e));
		} catch (Exception e) {
			e.printStackTrace();
			throw new I2B2Exception("" + StackTraceUtil.getStackTrace(e));
		} 

		return projectType;
	}

	public static RolesType callGetRole(String userId, SecurityType securityType,  String projectId, String ontologyUrl  )
			throws AxisFault, I2B2Exception {
		RolesType rolesType = new RolesType();
		String pmBypassRole = null, pmBypassProject = null;
		boolean pmBypass = false;
		try {
			pmBypass = QueryProcessorUtil.getInstance()
					.getProjectManagementByPassFlag();
			pmBypassRole = QueryProcessorUtil.getInstance()
					.getProjectManagementByPassRole();

			log.debug("Project Management bypass flag  from property file :["
					+ pmBypass + "] bypass role [" + pmBypassRole
					+ "] project [" + pmBypassProject + "]");
		} catch (I2B2Exception e1) {
			e1.printStackTrace();
			log
					.info("Could not read Project Management bypass setting, trying PM without bypass option");
		}

		if (pmBypass == true) {
			log.info("Using Project Management by pass option ");
			log
					.info("Using project Management bypass flag  from property file :["
							+ pmBypass
							+ "] bypass role ["
							+ pmBypassRole
							+ "] project [" + pmBypassProject + "]");

			if (pmBypassRole != null) {
				String[] roles = pmBypassRole.split(",");
				RoleType role = null;
				for (int i = 0; i < roles.length; i++) {
					role = new RoleType();
					role.setRole(roles[i]);
					rolesType.getRole().add(role);
				}
			}

		} else {
			RoleType roleType = new RoleType();
			roleType.setUserName(userId);
			roleType.setProjectId(projectId);
			ObjectFactory of = new ObjectFactory();
			BodyType bodyType = new BodyType();
			bodyType.getAny().add(of.createGetAllRole(roleType));
			RequestMessageType requestMessageType = getI2B2RequestMessage(bodyType, securityType, projectId);
			OMElement requestElement = null;

			try {
				requestElement = buildOMElement(requestMessageType);
				log.debug("CRC PM call's request xml " + requestElement);
				//OMElement response = getServiceClient().sendReceive(
				//		requestElement);
				String response = ServiceClient.sendREST(ontologyUrl,requestElement);

				rolesType = getUserRolesFromResponse(response);
			} catch (XMLStreamException e) {
				e.printStackTrace();
				throw new I2B2Exception("" + StackTraceUtil.getStackTrace(e));
			} catch (Exception e) {
				e.printStackTrace();
				throw new I2B2Exception("" + StackTraceUtil.getStackTrace(e));
			}
		}

		return rolesType;
	}

	private static ProjectType getUserProjectFromResponse(String responseXml, SecurityType securityType,  String projectId )
			throws JAXBUtilException, I2B2Exception {
		JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil()
				.unMashallFromString(responseXml);
		ResponseMessageType pmRespMessageType = (ResponseMessageType) responseJaxb
				.getValue();
		log.debug("CRC's PM call response xml" + responseXml);

		ResponseHeaderType responseHeader = pmRespMessageType
				.getResponseHeader();
		StatusType status = responseHeader.getResultStatus().getStatus();
		String procStatus = status.getType();
		String procMessage = status.getValue();

		if (procStatus.equals("ERROR")) {
			log.info("PM Error reported by CRC web Service " + procMessage);
			throw new I2B2Exception("PM Error reported by CRC web Service "
					+ procMessage);
		} else if (procStatus.equals("WARNING")) {
			log.info("PM Warning reported by CRC web Service" + procMessage);
			throw new I2B2Exception("PM Warning reported by CRC web Service"
					+ procMessage);
		}

		JAXBUnWrapHelper helper = new JAXBUnWrapHelper();
		ConfigureType configureType = (ConfigureType) helper.getObjectByClass(
				pmRespMessageType.getMessageBody().getAny(),
				ConfigureType.class);
		UserType userType = configureType.getUser();
		List<ProjectType> projectTypeList = userType.getProject();

		ProjectType projectType = null;
		if (projectTypeList != null && projectTypeList.size() > 0) {
			for (ProjectType pType : projectTypeList) {
				if (pType.getId().equalsIgnoreCase(projectId)) {
					projectType = pType;

					break;
				}
			}
			if (projectType == null) {
				throw new I2B2Exception("User not registered to the project["
						+ projectId + "]");
			}
		}

		return projectType;
	}

	private static RolesType getUserRolesFromResponse(String responseXml)
			throws JAXBUtilException, I2B2Exception {
		JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil()
				.unMashallFromString(responseXml);
		ResponseMessageType pmRespMessageType = (ResponseMessageType) responseJaxb
				.getValue();
		log.debug("CRC's PM call response xml" + responseXml);

		ResponseHeaderType responseHeader = pmRespMessageType
				.getResponseHeader();
		StatusType status = responseHeader.getResultStatus().getStatus();
		String procStatus = status.getType();
		String procMessage = status.getValue();

		if (procStatus.equals("ERROR")) {
			log.info("PM Error reported by CRC web Service " + procMessage);
			throw new I2B2Exception("PM Error reported by CRC web Service "
					+ procMessage);
		} else if (procStatus.equals("WARNING")) {
			log.info("PM Warning reported by CRC web Service" + procMessage);
			throw new I2B2Exception("PM Warning reported by CRC web Service"
					+ procMessage);
		}

		JAXBUnWrapHelper helper = new JAXBUnWrapHelper();
		RolesType rolesType = (RolesType) helper.getObjectByClass(
				pmRespMessageType.getMessageBody().getAny(), RolesType.class);
		return rolesType;

	}

	private static OMElement buildOMElement(RequestMessageType requestMessageType)
			throws XMLStreamException, JAXBUtilException {
		StringWriter strWriter = new StringWriter();
		edu.harvard.i2b2.crc.datavo.i2b2message.ObjectFactory hiveof = new edu.harvard.i2b2.crc.datavo.i2b2message.ObjectFactory();
		CRCJAXBUtil.getJAXBUtil().marshaller(
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

	private static RequestMessageType getI2B2RequestMessage(BodyType bodyType, SecurityType securityType,  String projectId ) {
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
		RequestMessageType requestMessageType = new RequestMessageType();
		requestMessageType.setMessageBody(bodyType);

		requestMessageType.setMessageHeader(messageHeaderType);

		RequestHeaderType requestHeader = new RequestHeaderType();
		requestHeader.setResultWaittimeMs(180000);
		requestMessageType.setRequestHeader(requestHeader);

		return requestMessageType;

	}

}
