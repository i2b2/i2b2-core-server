package edu.harvard.i2b2.crc.loader.delegate;

import java.io.StringWriter;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtil;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.crc.loader.datavo.CRCLoaderJAXBUtil;
import edu.harvard.i2b2.crc.datavo.i2b2message.BodyType;
import edu.harvard.i2b2.crc.datavo.i2b2message.MessageHeaderType;
import edu.harvard.i2b2.crc.loader.datavo.loader.query.GetUploadInfoRequestType;
import edu.harvard.i2b2.crc.loader.datavo.loader.query.LoadDataListResponseType;
import edu.harvard.i2b2.crc.loader.datavo.loader.query.LoadDataResponseType;
import edu.harvard.i2b2.crc.loader.ejb.LoaderStatusBean;
import edu.harvard.i2b2.crc.loader.ejb.LoaderStatusBeanLocal;
import edu.harvard.i2b2.crc.loader.util.CRCLoaderUtil;

public class GetLoadStatusRequestHandler extends RequestHandler {

	GetUploadInfoRequestType getUploadInfoRequest = null;
	MessageHeaderType messageHeaderType = null;

	public GetLoadStatusRequestHandler(String requestXml) throws I2B2Exception {
		try {
			getUploadInfoRequest = (GetUploadInfoRequestType) this
					.getRequestType(
							requestXml,
							edu.harvard.i2b2.crc.loader.datavo.loader.query.GetUploadInfoRequestType.class);
			messageHeaderType = getMessageHeaderType(requestXml);
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

		//LoaderStatusBeanLocal loaderStatusBean = CRCLoaderUtil.getInstance()
		//		.getLoaderStatusBean();
		
		LoaderStatusBean loaderStatusBean =  new LoaderStatusBean();
		
		JAXBUtil jaxbUtil = CRCLoaderJAXBUtil.getJAXBUtil();
		StringWriter strWriter = new StringWriter();
		try {
			jaxbUtil.marshaller(objectFactory
					.createGetUploadInfoRequest(getUploadInfoRequest),
					strWriter);
		} catch (JAXBUtilException jaxbEx) {
			throw new I2B2Exception("Error in marshalling publishdata request",
					jaxbEx);
		}
		String uploadId = getUploadInfoRequest.getLoadId();
		String userId = getUploadInfoRequest.getUserId();
		if (uploadId != null) {
			LoadDataResponseType response = loaderStatusBean
					.getLoadDataResponseByUploadId(this.getDataSourceLookup(),
							Integer.parseInt(uploadId));
			bodyType.getAny().add(
					objectFactory.createLoadDataResponse(response));
		} else if (userId != null) {
			LoadDataListResponseType response = loaderStatusBean
					.getLoadDataResponseByUserId(this.getDataSourceLookup(),
							userId);

			bodyType.getAny().add(
					objectFactory.createLoadDataListResponse(response));

		}

		return bodyType;

	}

}
