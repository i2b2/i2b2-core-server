package edu.harvard.i2b2.crc.dao.setfinder.querybuilder;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.exception.StackTraceUtil;
import edu.harvard.i2b2.common.util.jaxb.JAXBUnWrapHelper;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtil;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.crc.datavo.CRCJAXBUtil;
import edu.harvard.i2b2.crc.datavo.i2b2message.RequestMessageType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryDefinitionRequestType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryDefinitionType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.ResultOutputOptionListType;

public class QueryDefinitionUnWrapUtil {

	public QueryDefinitionType getQueryDefinitionType(String queryXML)
			throws I2B2DAOException {

		QueryDefinitionType queryDefType = null;
		try {
			JAXBUtil jaxbUtil = CRCJAXBUtil.getJAXBUtil();
			RequestMessageType reqMsgType = (RequestMessageType) jaxbUtil
					.unMashallFromString(queryXML).getValue();

			JAXBUnWrapHelper unWrapHelper = new JAXBUnWrapHelper();
			QueryDefinitionRequestType queryDefRequestType = (QueryDefinitionRequestType) unWrapHelper
					.getObjectByClass(
							reqMsgType.getMessageBody().getAny(),
							edu.harvard.i2b2.crc.datavo.setfinder.query.QueryDefinitionRequestType.class);
			queryDefType = queryDefRequestType.getQueryDefinition();
		} catch (JAXBUtilException e) {
			throw new I2B2DAOException(
					"Error while reading the querydefinition from request xml  "
							+ StackTraceUtil.getStackTrace(e));
		}
		return queryDefType;
	}

	public ResultOutputOptionListType getResultOuputOptionType(String queryXML)
			throws I2B2DAOException {
		ResultOutputOptionListType resultOutputListType = null;
		try {
			JAXBUtil jaxbUtil = CRCJAXBUtil.getJAXBUtil();
			RequestMessageType reqMsgType = (RequestMessageType) jaxbUtil
					.unMashallFromString(queryXML).getValue();

			JAXBUnWrapHelper unWrapHelper = new JAXBUnWrapHelper();
			QueryDefinitionRequestType queryDefRequestType = (QueryDefinitionRequestType) unWrapHelper
					.getObjectByClass(
							reqMsgType.getMessageBody().getAny(),
							edu.harvard.i2b2.crc.datavo.setfinder.query.QueryDefinitionRequestType.class);
			resultOutputListType = queryDefRequestType.getResultOutputList();
		} catch (JAXBUtilException e) {
			throw new I2B2DAOException(
					"Error while reading the querydefinition from request xml  "
							+ StackTraceUtil.getStackTrace(e));
		}
		return resultOutputListType;
	}
}
