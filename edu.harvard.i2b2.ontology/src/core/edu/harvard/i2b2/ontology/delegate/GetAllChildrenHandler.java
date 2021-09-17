package edu.harvard.i2b2.ontology.delegate;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.ontology.dao.ConceptDao;
import edu.harvard.i2b2.ontology.datavo.i2b2message.MessageHeaderType;
import edu.harvard.i2b2.ontology.datavo.i2b2message.ResponseMessageType;
import edu.harvard.i2b2.ontology.datavo.pm.ProjectType;
import edu.harvard.i2b2.ontology.datavo.vdo.ConceptType;
import edu.harvard.i2b2.ontology.datavo.vdo.ConceptsType;
import edu.harvard.i2b2.ontology.datavo.vdo.GetAllChildrenType;
import edu.harvard.i2b2.ontology.ws.GetAllChildrenDataMessage;
import edu.harvard.i2b2.ontology.ws.MessageFactory;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * Sep 14, 2021 11:06:06 AM
 *
 * @author Kevin V. Bui (kvb2univpitt@gmail.com)
 */
public class GetAllChildrenHandler extends RequestHandler {

    private static Log log = LogFactory.getLog(GetChildrenHandler.class);
    private GetAllChildrenDataMessage getChildrenMsg = null;
    private GetAllChildrenType getChildrenType = null;
    private ProjectType project = null;

    public GetAllChildrenHandler(GetAllChildrenDataMessage requestMsg) throws I2B2Exception {
        try {
            getChildrenMsg = requestMsg;
            getChildrenType = requestMsg.getAllChildrenType();

            setDbInfo(requestMsg.getMessageHeaderType());

            // test case for bad user
            project = getRoleInfo(getChildrenMsg.getMessageHeaderType());
        } catch (JAXBUtilException e) {
            log.error("error setting up getChildrenHandler");
            throw new I2B2Exception("GetChildrenHandler not configured");
        }
    }

    @Override
    public String execute() throws I2B2Exception {
        // call ejb and pass input object
        ConceptDao conceptDao = new ConceptDao();
        ConceptsType concepts = new ConceptsType();
        ResponseMessageType responseMessageType = null;

        // if project == null, user was not validated or PM service problem
        if (project == null) {
            String response = null;
            responseMessageType = MessageFactory.doBuildErrorResponse(getChildrenMsg.getMessageHeaderType(), "User was not validated");
            response = MessageFactory.convertToXMLString(responseMessageType);
            log.debug("USER_INVALID or PM_SERVICE_PROBLEM");
            return response;
        }

        List response = null;
        try {
            response = conceptDao.findAllChildrenByParent(getChildrenMsg, project, this.getDbInfo());
        } catch (I2B2DAOException e1) {
            responseMessageType = MessageFactory.doBuildErrorResponse(getChildrenMsg.getMessageHeaderType(), "Database error");
        } catch (I2B2Exception e1) {
            responseMessageType = MessageFactory.doBuildErrorResponse(getChildrenMsg.getMessageHeaderType(), "Database error");
        } catch (JAXBUtilException e1) {
            responseMessageType = MessageFactory.doBuildErrorResponse(getChildrenMsg.getMessageHeaderType(), "Incoming request message error");
        }

        // no errors found
        if (responseMessageType == null) {
            // no db error but response is empty
            if (response == null) {
                log.debug("query results are empty");
                responseMessageType = MessageFactory.doBuildErrorResponse(getChildrenMsg.getMessageHeaderType(), "Query results are empty");
            } //			 No errors, non-empty response received
            // If max is specified, check that response is not > max
            else if (getChildrenType.getMax() != null) {
                // if max exceeded send error message
                if (response.size() > getChildrenType.getMax()) {
                    log.debug("Max request size of " + getChildrenType.getMax() + " exceeded ");
                    responseMessageType = MessageFactory.doBuildErrorResponse(getChildrenMsg.getMessageHeaderType(), "MAX_EXCEEDED");
                } // otherwise send results
                else {
                    Iterator it = response.iterator();
                    while (it.hasNext()) {
                        ConceptType node = (ConceptType) it.next();
                        concepts.getConcept().add(node);
                    }
                    // create ResponseMessageHeader using information from request message header.
                    MessageHeaderType messageHeader = MessageFactory.createResponseMessageHeader(getChildrenMsg.getMessageHeaderType());
                    responseMessageType = MessageFactory.createBuildResponse(messageHeader, concepts);
                }
            } // max not specified so send results
            else {
                Iterator it = response.iterator();
                while (it.hasNext()) {
                    ConceptType node = (ConceptType) it.next();
                    concepts.getConcept().add(node);
                }
                MessageHeaderType messageHeader = MessageFactory.createResponseMessageHeader(getChildrenMsg.getMessageHeaderType());
                responseMessageType = MessageFactory.createBuildResponse(messageHeader, concepts);
            }
        }

        String responseVdo = null;
        responseVdo = MessageFactory.convertToXMLString(responseMessageType);

        return responseVdo;
    }

}
