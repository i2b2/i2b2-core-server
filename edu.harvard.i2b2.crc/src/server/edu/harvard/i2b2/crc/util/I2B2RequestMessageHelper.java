package edu.harvard.i2b2.crc.util;

import java.io.StringWriter;

import javax.xml.bind.JAXBElement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.jaxb.JAXBUnWrapHelper;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtil;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.crc.datavo.CRCJAXBUtil;
import edu.harvard.i2b2.crc.datavo.i2b2message.BodyType;
import edu.harvard.i2b2.crc.datavo.i2b2message.RequestHeaderType;
import edu.harvard.i2b2.crc.datavo.i2b2message.RequestMessageType;
import edu.harvard.i2b2.crc.datavo.i2b2message.SecurityType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.AnalysisDefinitionRequestType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.AnalysisDefinitionType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.AnalysisResultOptionListType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.AnalysisResultOptionType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryDefinitionRequestType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryDefinitionType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.ResultOutputOptionListType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.ResultOutputOptionType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.UserType;

public class I2B2RequestMessageHelper {

	private static Log log = LogFactory.getLog(I2B2RequestMessageHelper.class);

	String requestXml = null;
	JAXBUnWrapHelper unWrapHelper = null;
	JAXBUtil jaxbUtil = CRCJAXBUtil.getJAXBUtil();
	JAXBElement jaxbElement = null;
	RequestMessageType requestMessageType = null;
	
	public I2B2RequestMessageHelper(String requestXml) throws I2B2Exception {
		this.requestXml = requestXml;
		unWrapHelper = new JAXBUnWrapHelper();
		try {
			jaxbElement = jaxbUtil.unMashallFromString(requestXml);

			if (jaxbElement == null) {
				throw new I2B2Exception(
						"null value in after unmarshalling request string ");
			}
			requestMessageType = (RequestMessageType) jaxbElement
			.getValue();
		} catch (JAXBUtilException jaxbUtilEx) {
			log.error("Error processing request xml [" + requestXml + "]",
					jaxbUtilEx);
			throw new I2B2Exception(jaxbUtilEx.getMessage());
		}
	}

	public RequestMessageType getI2B2RequestMessageType() { 
		return this.requestMessageType;
	}
	
	private BodyType getBodyType() {
		return requestMessageType.getMessageBody();
	}

	public QueryDefinitionType getQueryDefinition() throws JAXBUtilException {
		BodyType bodyType = getBodyType();

		QueryDefinitionRequestType queryDefReqType = (QueryDefinitionRequestType) unWrapHelper
				.getObjectByClass(bodyType.getAny(),
						QueryDefinitionRequestType.class);

		QueryDefinitionType queryDef = null;
		if (queryDefReqType != null) {
			queryDef = queryDefReqType.getQueryDefinition();
		}
		return queryDef;
	}

	public AnalysisDefinitionType getAnalysisDefinition()
			throws JAXBUtilException {
		BodyType bodyType = getBodyType();

		AnalysisDefinitionRequestType analysisDefReqType = (AnalysisDefinitionRequestType) unWrapHelper
				.getObjectByClass(bodyType.getAny(),
						AnalysisDefinitionRequestType.class);
		AnalysisDefinitionType analysisDef = null;
		if (analysisDefReqType != null) {
			analysisDef = analysisDefReqType.getAnalysisDefinition();
		}
		return analysisDef;

	}

	public UserType getUserType() {

		RequestMessageType requestMessageType = (RequestMessageType) jaxbElement
				.getValue();

		UserType userType = new UserType();
		userType.setLogin(requestMessageType.getMessageHeader().getSecurity()
				.getUsername());
		userType.setGroup(requestMessageType.getMessageHeader().getSecurity()
				.getDomain());

		return userType;
	}

	public SecurityType getSecurityType() {
		RequestMessageType requestMessageType = (RequestMessageType) jaxbElement
				.getValue();
		return requestMessageType.getMessageHeader().getSecurity();
	}

	public String getProjectId() {
		String projectId = null;
		RequestMessageType requestMessageType = (RequestMessageType) jaxbElement
				.getValue();
		projectId = requestMessageType.getMessageHeader().getProjectId();
		return projectId;
	}

	public long getTimeout() {
		RequestMessageType requestMessageType = (RequestMessageType) jaxbElement
				.getValue();

		RequestHeaderType requestHeader = requestMessageType.getRequestHeader();
		long timeOut = 1;
		if (requestHeader != null && requestHeader.getResultWaittimeMs() > -1) {
			timeOut = requestHeader.getResultWaittimeMs();
		}
		return timeOut;

	}
	
	public String getVersion() { 
		RequestMessageType requestMessageType = (RequestMessageType) jaxbElement
				.getValue();
		String version = "";
		if (requestMessageType.getMessageHeader().getSendingApplication() != null) {
			version = requestMessageType.getMessageHeader().getSendingApplication().getApplicationVersion(); 
			if (version == null) { 
				version = "";
			} else  {
				version = version.trim();
			}
		}
		
		return version;
	}

	public static String getAnalysisDefinitionXml(
			AnalysisDefinitionType analysisDefinition) throws JAXBUtilException {
		StringWriter queryDefWriter = new StringWriter();
		edu.harvard.i2b2.crc.datavo.setfinder.query.ObjectFactory of = new edu.harvard.i2b2.crc.datavo.setfinder.query.ObjectFactory();
		JAXBUtil jaxbUtil = CRCJAXBUtil.getAnalysisDefJAXBUtil();
		jaxbUtil.marshaller(of.createAnalysisDefinition(analysisDefinition),
				queryDefWriter);

		return queryDefWriter.toString();
	}

	public static AnalysisDefinitionType getAnalysisDefinitionFromXml(
			String defXml) throws JAXBUtilException {
		JAXBUtil jaxbUtil = CRCJAXBUtil.getJAXBUtil();
		JAXBElement analysisDef = jaxbUtil.unMashallFromString(defXml);
		return (AnalysisDefinitionType) analysisDef.getValue();
	}

	public static ResultOutputOptionListType buildResultOptionListFromAnalysisResultList(
			AnalysisResultOptionListType analysisResultListType) {
		ResultOutputOptionListType resultOutputOptionList = new ResultOutputOptionListType();
		if (analysisResultListType == null) {
			return resultOutputOptionList;
		}
		for (AnalysisResultOptionType analysisResultType : analysisResultListType
				.getResultOutput()) {
			ResultOutputOptionType resultOuputOptionType = new ResultOutputOptionType();
			resultOuputOptionType.setName(analysisResultType.getName());
			resultOuputOptionType.setFullName(analysisResultType.getFullName());
			resultOuputOptionType.setDisplayType(analysisResultType
					.getDisplayType());
			resultOuputOptionType.setPriorityIndex(analysisResultType
					.getPriorityIndex());
			resultOutputOptionList.getResultOutput().add(resultOuputOptionType);
		}
		return resultOutputOptionList;
	}
}
