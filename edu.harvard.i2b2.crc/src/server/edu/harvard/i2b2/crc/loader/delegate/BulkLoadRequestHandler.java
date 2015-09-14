package edu.harvard.i2b2.crc.loader.delegate;

import java.io.StringWriter;

import javax.xml.bind.JAXBElement;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtil;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.crc.loader.datavo.CRCLoaderJAXBUtil;
import edu.harvard.i2b2.crc.datavo.i2b2message.BodyType;
import edu.harvard.i2b2.crc.datavo.i2b2message.MessageHeaderType;
import edu.harvard.i2b2.crc.datavo.i2b2message.RequestHeaderType;
import edu.harvard.i2b2.crc.datavo.i2b2message.RequestMessageType;
import edu.harvard.i2b2.crc.datavo.i2b2message.SecurityType;
import edu.harvard.i2b2.crc.loader.datavo.loader.query.BulkLoadRequestType;
import edu.harvard.i2b2.crc.loader.datavo.loader.query.LoadDataResponseType;
import edu.harvard.i2b2.crc.loader.datavo.loader.query.PublishDataRequestType;
import edu.harvard.i2b2.crc.loader.ejb.DataMartLoaderAsyncBean;
import edu.harvard.i2b2.crc.loader.ejb.DataMartLoaderAsyncBeanLocal;
import edu.harvard.i2b2.crc.loader.ejb.IDataMartLoaderBean;
import edu.harvard.i2b2.crc.loader.util.CRCLoaderUtil;

public class BulkLoadRequestHandler extends RequestHandler {

	BulkLoadRequestType bulkLoadRequestType = null;
	MessageHeaderType messageHeaderType = null;
	long timeout = 1;
	protected String irodsDefaultStorageResource = null;

	public BulkLoadRequestHandler(String requestXml) throws I2B2Exception {
		try {
			bulkLoadRequestType = (BulkLoadRequestType) this
					.getRequestType(
							requestXml,
							edu.harvard.i2b2.crc.loader.datavo.loader.query.BulkLoadRequestType.class);
			messageHeaderType = getMessageHeaderType(requestXml);
			timeout = getTimeout(requestXml);
			this.setDataSourceLookup(requestXml);
		} catch (JAXBUtilException jaxbUtilEx) {
			throw new I2B2Exception("Error ", jaxbUtilEx);
		}
	}

	@Override
	public BodyType execute() throws I2B2Exception {
		edu.harvard.i2b2.crc.loader.datavo.loader.query.ObjectFactory objectFactory = new edu.harvard.i2b2.crc.loader.datavo.loader.query.ObjectFactory();
		// call ejb and pass input object
		String responseString = null;
		BodyType bodyType = new BodyType();

		DataMartLoaderAsyncBean loader = new DataMartLoaderAsyncBean();
		JAXBUtil jaxbUtil = CRCLoaderJAXBUtil.getJAXBUtil();
		StringWriter strWriter = new StringWriter();
		try {
			jaxbUtil.marshaller(objectFactory
					.createBulkLoadRequest(bulkLoadRequestType), strWriter);
		} catch (JAXBUtilException jaxbEx) {
			throw new I2B2Exception("Error in marshalling bulk load request",
					jaxbEx);
		}
		SecurityType securityType = messageHeaderType.getSecurity();

		LoadDataResponseType response = loader.bulkLoad(this.getDataSourceLookup(),
				strWriter.toString(), securityType, timeout);

		bodyType.getAny().add(objectFactory.createLoadDataResponse(response));

		return bodyType;

	}


	public long getTimeout(String xmlRequest) {
		JAXBUtil jaxbUtil = CRCLoaderJAXBUtil.getJAXBUtil();
		JAXBElement jaxbElement = null;
		long timeOutValue = 1;
		try {
			jaxbElement = jaxbUtil.unMashallFromString(xmlRequest);

			RequestMessageType requestMessageType = (RequestMessageType) jaxbElement
					.getValue();

			RequestHeaderType requestHeader = requestMessageType
					.getRequestHeader();

			if (requestHeader != null
					&& requestHeader.getResultWaittimeMs() > -1) {
				timeOutValue = requestHeader.getResultWaittimeMs();
			}
		} catch (JAXBUtilException e) {
			log.error("Failed to read wait time from the request ", e);
		}
		return timeOutValue;
	}

}
