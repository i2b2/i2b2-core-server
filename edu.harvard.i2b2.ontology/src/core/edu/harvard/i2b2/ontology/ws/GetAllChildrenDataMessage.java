package edu.harvard.i2b2.ontology.ws;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.jaxb.JAXBUnWrapHelper;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.ontology.datavo.i2b2message.BodyType;
import edu.harvard.i2b2.ontology.datavo.vdo.GetAllChildrenType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The GetAllChildrenDataMessage class is a helper class to build Ontology
 * messages in the i2b2 format.
 *
 * Sep 1, 2021 5:23:33 PM
 *
 * @author Kevin V. Bui (kvb2univpitt@gmail.com)
 */
public class GetAllChildrenDataMessage extends RequestDataMessage {

    private static final Log LOGGER = LogFactory.getLog(GetAllChildrenDataMessage.class);

    public GetAllChildrenDataMessage(String requestVdo) throws I2B2Exception {
        super(requestVdo);
    }

    public GetAllChildrenType getAllChildrenType() throws JAXBUtilException {
        BodyType bodyType = reqMessageType.getMessageBody();
        JAXBUnWrapHelper helper = new JAXBUnWrapHelper();
        GetAllChildrenType allChildrenType = (GetAllChildrenType) helper.getObjectByClass(bodyType.getAny(),
                GetAllChildrenType.class);

        return allChildrenType;
    }

}
