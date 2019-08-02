/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
/*

 * 
 * Contributors:
 * 		Raj Kuttan
 * 		Lori Phillips
 */
package edu.harvard.i2b2.ontology.ws;

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.ontology.datavo.i2b2message.ResponseMessageType;
import edu.harvard.i2b2.ontology.delegate.AddChildHandler;
import edu.harvard.i2b2.ontology.delegate.AddModifierHandler;
import edu.harvard.i2b2.ontology.delegate.CRCConceptUpdateHandler;
import edu.harvard.i2b2.ontology.delegate.DeleteChildHandler;
import edu.harvard.i2b2.ontology.delegate.DeleteDblookupHandler;
import edu.harvard.i2b2.ontology.delegate.ExcludeModifierHandler;
import edu.harvard.i2b2.ontology.delegate.GetAllDblookupsHandler;
import edu.harvard.i2b2.ontology.delegate.GetCategoriesHandler;
import edu.harvard.i2b2.ontology.delegate.GetChildrenHandler;
import edu.harvard.i2b2.ontology.delegate.GetCodeInfoHandler;
import edu.harvard.i2b2.ontology.delegate.GetDblookupHandler;
import edu.harvard.i2b2.ontology.delegate.GetDerivedFactColumnsHandler;
import edu.harvard.i2b2.ontology.delegate.GetDirtyStateHandler;
import edu.harvard.i2b2.ontology.delegate.GetModifierChildrenHandler;
import edu.harvard.i2b2.ontology.delegate.GetModifierCodeInfoHandler;
import edu.harvard.i2b2.ontology.delegate.GetModifierInfoHandler;
import edu.harvard.i2b2.ontology.delegate.GetModifierNameInfoHandler;
import edu.harvard.i2b2.ontology.delegate.GetModifiersHandler;
import edu.harvard.i2b2.ontology.delegate.GetNameInfoHandler;
import edu.harvard.i2b2.ontology.delegate.GetOntProcessStatusHandler;
import edu.harvard.i2b2.ontology.delegate.GetSchemesHandler;
import edu.harvard.i2b2.ontology.delegate.GetTermInfoHandler;
import edu.harvard.i2b2.ontology.delegate.ModifyChildHandler;
import edu.harvard.i2b2.ontology.delegate.CheckMetadataTableHandler;
import edu.harvard.i2b2.ontology.delegate.LoadMetadataHandler;
import edu.harvard.i2b2.ontology.delegate.LoadSchemesHandler;
import edu.harvard.i2b2.ontology.delegate.LoadTableAccessHandler;
import edu.harvard.i2b2.ontology.delegate.RequestHandler;
import edu.harvard.i2b2.ontology.delegate.SetDblookupHandler;
import edu.harvard.i2b2.ontology.delegate.UpdateTotalNumHandler;

/**
 * This is webservice skeleton class. It parses incoming Ontology service
 * requests and generates responses in the Vocab Data Object XML format.
 * 
 */
public class OntologyService {
	private static Log log = LogFactory.getLog(OntologyService.class);

	/**
	 * This function is main webservice interface to get vocab data for a query.
	 * It uses AXIOM elements(OMElement) to conveniently parse xml messages.
	 * 
	 * It excepts incoming request in i2b2 message format, which wraps an
	 * Ontology query inside a vocab query request object. The response is also
	 * will be in i2b2 message format, which will wrap vocab data object. Vocab
	 * data object will have all the results returned by the query.
	 * 
	 * 
	 * @param getChildren
	 * @return OMElement in i2b2message format
	 * @throws Exception
	 */
	public OMElement getChildren(OMElement getChildrenElement)
			throws I2B2Exception {

		OMElement returnElement = null;
		String ontologyDataResponse = null;
		String unknownErrorMessage = "Error message delivered from the remote server \n"
				+ "You may wish to retry your last action";

		if (getChildrenElement == null) {
			log.error("Incoming Ontology request is null");
			ResponseMessageType responseMsgType = MessageFactory
					.doBuildErrorResponse(null, unknownErrorMessage);
			ontologyDataResponse = MessageFactory
					.convertToXMLString(responseMsgType);
			return MessageFactory
					.createResponseOMElementFromString(ontologyDataResponse);
		}
		String requestElementString = getChildrenElement.toString();
		GetChildrenDataMessage childrenDataMsg = new GetChildrenDataMessage(
				requestElementString);
		long waitTime = 0;
		if ((childrenDataMsg.getRequestMessageType() != null) && (childrenDataMsg.getRequestMessageType().getRequestHeader() != null)) {
			waitTime = childrenDataMsg.getRequestMessageType()
					.getRequestHeader().getResultWaittimeMs();
		}

		// do Ontology query processing inside thread, so that
		// service could sends back message with timeout error.
		// ExecutorRunnable er = new ExecutorRunnable();
		return execute(new GetChildrenHandler(childrenDataMsg), waitTime);

	}

	/**
	 * This function is main webservice interface to get vocab data for a query.
	 * It uses AXIOM elements(OMElement) to conveniently parse xml messages.
	 * 
	 * It excepts incoming request in i2b2 message format, which wraps an
	 * Ontology query inside a vocab query request object. The response is also
	 * will be in i2b2 message format, which will wrap vocab data object. Vocab
	 * data object will have all the results returned by the query.
	 * 
	 * 
	 * @param OMElement
	 *            getCategoriesElement
	 * @return OMElement in i2b2message format
	 * @throws Exception
	 */
	public OMElement getCategories(OMElement getCategoriesElement)
			throws I2B2Exception {

		OMElement returnElement = null;
		String ontologyDataResponse = null;
		String unknownErrorMessage = "Error message delivered from the remote server \n"
				+ "You may wish to retry your last action";

		if (getCategoriesElement == null) {
			log.error("Incoming Ontology request is null");
			ResponseMessageType responseMsgType = MessageFactory
					.doBuildErrorResponse(null, unknownErrorMessage);
			ontologyDataResponse = MessageFactory
					.convertToXMLString(responseMsgType);
			return MessageFactory
					.createResponseOMElementFromString(ontologyDataResponse);
		}

		String requestElementString = getCategoriesElement.toString();
		GetCategoriesDataMessage categoriesDataMsg = new GetCategoriesDataMessage(
				requestElementString);
		long waitTime = 0;
		if ((categoriesDataMsg.getRequestMessageType() != null) && (categoriesDataMsg.getRequestMessageType().getRequestHeader() != null)) {
			waitTime = categoriesDataMsg.getRequestMessageType()
					.getRequestHeader().getResultWaittimeMs();
		}

		// do Ontology query processing inside thread, so that
		// service could sends back message with timeout error.
		// ExecutorRunnable er = new ExecutorRunnable();
		return execute(new GetCategoriesHandler(categoriesDataMsg), waitTime);
	}

	/**
	 * This function is main webservice interface to get vocab data for a query.
	 * It uses AXIOM elements(OMElement) to conveniently parse xml messages.
	 * 
	 * It excepts incoming request in i2b2 message format, which wraps an
	 * Ontology query inside a vocab query request object. The response is also
	 * will be in i2b2 message format, which will wrap vocab data object. Vocab
	 * data object will have all the results returned by the query.
	 * 
	 * 
	 * @param OMElement
	 *            geSchemesElement
	 * @return OMElement in i2b2message format
	 * @throws Exception
	 */
	public OMElement getSchemes(OMElement getSchemesElement)
			throws I2B2Exception {
		OMElement returnElement = null;
		String ontologyDataResponse = null;
		String unknownErrorMessage = "Error message delivered from the remote server \n"
				+ "You may wish to retry your last action";

		if (getSchemesElement == null) {
			log.error("Incoming Ontology request is null");
			ResponseMessageType responseMsgType = MessageFactory
					.doBuildErrorResponse(null, unknownErrorMessage);
			ontologyDataResponse = MessageFactory
					.convertToXMLString(responseMsgType);
			return MessageFactory
					.createResponseOMElementFromString(ontologyDataResponse);
		}

		String requestElementString = getSchemesElement.toString();
		GetSchemesDataMessage schemesDataMsg = new GetSchemesDataMessage(
				requestElementString);

		long waitTime = 0;
		if ((schemesDataMsg.getRequestMessageType() != null) && (schemesDataMsg.getRequestMessageType().getRequestHeader() != null)) {
			waitTime = schemesDataMsg.getRequestMessageType()
					.getRequestHeader().getResultWaittimeMs();
		}

		// do Ontology query processing inside thread, so that
		// service could sends back message with timeout error.
		// ExecutorRunnable er = new ExecutorRunnable();
		return execute(new GetSchemesHandler(schemesDataMsg), waitTime);
	}

	/**
	 * This function is main webservice interface to get vocab data for a query.
	 * It uses AXIOM elements(OMElement) to conveniently parse xml messages.
	 * 
	 * It excepts incoming request in i2b2 message format, which wraps an
	 * Ontology query inside a vocab query request object. The response is also
	 * will be in i2b2 message format, which will wrap vocab data object. Vocab
	 * data object will have all the results returned by the query.
	 * 
	 * 
	 * @param OMElement
	 *            getCodeInfoElement
	 * @return OMElement in i2b2message format
	 * @throws Exception
	 */
	public OMElement getCodeInfo(OMElement getCodeInfoElement)
			throws I2B2Exception {
		OMElement returnElement = null;
		String ontologyDataResponse = null;
		String unknownErrorMessage = "Error message delivered from the remote server \n"
				+ "You may wish to retry your last action";

		if (getCodeInfoElement == null) {
			log.error("Incoming Ontology request is null");
			ResponseMessageType responseMsgType = MessageFactory
					.doBuildErrorResponse(null, unknownErrorMessage);
			ontologyDataResponse = MessageFactory
					.convertToXMLString(responseMsgType);
			return MessageFactory
					.createResponseOMElementFromString(ontologyDataResponse);
		}

		String requestElementString = getCodeInfoElement.toString();
		GetCodeInfoDataMessage codeInfoDataMsg = new GetCodeInfoDataMessage(
				requestElementString);

		long waitTime = 0;
		if ((codeInfoDataMsg.getRequestMessageType() != null) && (codeInfoDataMsg.getRequestMessageType().getRequestHeader() != null)) {
			waitTime = codeInfoDataMsg.getRequestMessageType()
					.getRequestHeader().getResultWaittimeMs();
		}

		// do Ontology query processing inside thread, so that
		// service could sends back message with timeout error.

		// ExecutorRunnable er = new ExecutorRunnable();
		return execute(new GetCodeInfoHandler(codeInfoDataMsg), waitTime);
	}

	/**
	 * This function is main webservice interface to get vocab data for a query.
	 * It uses AXIOM elements(OMElement) to conveniently parse xml messages.
	 * 
	 * It excepts incoming request in i2b2 message format, which wraps an
	 * Ontology query inside a vocab query request object. The response is also
	 * will be in i2b2 message format, which will wrap vocab data object. Vocab
	 * data object will have all the results returned by the query.
	 * 
	 * 
	 * @param OMElement
	 *            getCodeInfoElement
	 * @return OMElement in i2b2message format
	 * @throws Exception
	 */
	public OMElement getNameInfo(OMElement getNameInfoElement)
			throws I2B2Exception {
		OMElement returnElement = null;
		String ontologyDataResponse = null;
		String unknownErrorMessage = "Error message delivered from the remote server \n"
				+ "You may wish to retry your last action";

		if (getNameInfoElement == null) {
			log.error("Incoming Ontology request is null");
			ResponseMessageType responseMsgType = MessageFactory
					.doBuildErrorResponse(null, unknownErrorMessage);
			ontologyDataResponse = MessageFactory
					.convertToXMLString(responseMsgType);
			return MessageFactory
					.createResponseOMElementFromString(ontologyDataResponse);
		}

		String requestElementString = getNameInfoElement.toString();
		GetNameInfoDataMessage nameInfoDataMsg = new GetNameInfoDataMessage(
				requestElementString);

		long waitTime = 0;
		if ((nameInfoDataMsg.getRequestMessageType() != null) && (nameInfoDataMsg.getRequestMessageType().getRequestHeader() != null)) {
			waitTime = nameInfoDataMsg.getRequestMessageType()
					.getRequestHeader().getResultWaittimeMs();
		}

		// do Ontology query processing inside thread, so that
		// service could sends back message with timeout error.

		// ExecutorRunnable er = new ExecutorRunnable();
		return execute(new GetNameInfoHandler(nameInfoDataMsg), waitTime);
	}

	/**
	 * This function is main webservice interface to get vocab data for a query.
	 * It uses AXIOM elements(OMElement) to conveniently parse xml messages.
	 * 
	 * It excepts incoming request in i2b2 message format, which wraps an
	 * Ontology query inside a vocab query request object. The response is also
	 * will be in i2b2 message format, which will wrap vocab data object. Vocab
	 * data object will have all the results returned by the query.
	 * 
	 * 
	 * @param getTermInfo
	 * @return OMElement in i2b2message format
	 * @throws Exception
	 */
	public OMElement getTermInfo(OMElement getTermInfoElement)
			throws I2B2Exception {

		OMElement returnElement = null;
		String ontologyDataResponse = null;
		String unknownErrorMessage = "Error message delivered from the remote server \n"
				+ "You may wish to retry your last action";

		if (getTermInfoElement == null) {
			log.error("Incoming Ontology request is null");
			ResponseMessageType responseMsgType = MessageFactory
					.doBuildErrorResponse(null, unknownErrorMessage);
			ontologyDataResponse = MessageFactory
					.convertToXMLString(responseMsgType);
			return MessageFactory
					.createResponseOMElementFromString(ontologyDataResponse);

		}

		String requestElementString = getTermInfoElement.toString();
		GetTermInfoDataMessage termInfoDataMsg = new GetTermInfoDataMessage(
				requestElementString);

		long waitTime = 0;
		if ((termInfoDataMsg.getRequestMessageType() != null) && (termInfoDataMsg.getRequestMessageType().getRequestHeader() != null)) {
			waitTime = termInfoDataMsg.getRequestMessageType()
					.getRequestHeader().getResultWaittimeMs();
		}

		// do Ontology query processing inside thread, so that
		// service could sends back message with timeout error.

		// ExecutorRunnable er = new ExecutorRunnable();
		return execute(new GetTermInfoHandler(termInfoDataMsg), waitTime);
	}

	/**
	 * This function is main webservice interface to get vocab data for a query.
	 * It uses AXIOM elements(OMElement) to conveniently parse xml messages.
	 * 
	 * It excepts incoming request in i2b2 message format, which wraps an
	 * Ontology query inside a vocab query request object. The response is also
	 * will be in i2b2 message format, which will wrap vocab data object. Vocab
	 * data object will have all the results returned by the query.
	 * 
	 * 
	 * @param getDerivedFactColumns
	 * @return OMElement in i2b2message format
	 * @throws Exception
	 */
	public OMElement getDerivedFactColumns(OMElement getDerivedFactColumnsElement)
			throws I2B2Exception {

		OMElement returnElement = null;
		String ontologyDataResponse = null;
		String unknownErrorMessage = "Error message delivered from the remote server \n"
				+ "You may wish to retry your last action";

		if (getDerivedFactColumnsElement == null) {
			log.error("Incoming Ontology request is null");
			ResponseMessageType responseMsgType = MessageFactory
					.doBuildErrorResponse(null, unknownErrorMessage);
			ontologyDataResponse = MessageFactory
					.convertToXMLString(responseMsgType);
			return MessageFactory
					.createResponseOMElementFromString(ontologyDataResponse);

		}

		String requestElementString = getDerivedFactColumnsElement.toString();
		log.debug("ONT derivedFactCol request " + requestElementString);
		GetTermInfoDataMessage dataMsg = new GetTermInfoDataMessage(
				requestElementString);

		long waitTime = 0;
		if ((dataMsg.getRequestMessageType() != null) && (dataMsg.getRequestMessageType().getRequestHeader() != null)) {
			waitTime = dataMsg.getRequestMessageType()
					.getRequestHeader().getResultWaittimeMs();
		}

		// do Ontology query processing inside thread, so that
		// service could sends back message with timeout error.

		// ExecutorRunnable er = new ExecutorRunnable();
		return execute(new GetDerivedFactColumnsHandler(dataMsg), waitTime);
	}



	private OMElement execute(RequestHandler handler, long waitTime)
			throws I2B2Exception {
		// do Ontology query processing inside thread, so that
		// service could sends back message with timeout error.
		OMElement returnElement = null;

		String unknownErrorMessage = "Error message delivered from the remote server \n"
				+ "You may wish to retry your last action";

		ExecutorRunnable er = new ExecutorRunnable();

		er.setRequestHandler(handler);

		Thread t = new Thread(er);
		String ontologyDataResponse = null;

		synchronized (t) {
			t.start();

			// try {
			// if (waitTime > 0) {
			// t.wait(waitTime);
			// } else {
			// t.wait();
			// }

			try {
				long startTime = System.currentTimeMillis();
				long deltaTime = -1;
				while ((er.isJobCompleteFlag() == false)
						&& (deltaTime < waitTime)) {
					if (waitTime > 0) {
						t.wait(waitTime - deltaTime);
						deltaTime = System.currentTimeMillis() - startTime;
					} else {
						t.wait();
					}
				}

				ontologyDataResponse = er.getOutputString();

				if (ontologyDataResponse == null) {
					if (er.getJobException() != null) {
						log.error("er.jobException is "
								+ er.getJobException().getMessage());

						log.info("waitTime is " + waitTime);

						ResponseMessageType responseMsgType = MessageFactory
								.doBuildErrorResponse(null, unknownErrorMessage);
						ontologyDataResponse = MessageFactory
								.convertToXMLString(responseMsgType);

					} else if (er.isJobCompleteFlag() == false) {
						// <result_waittime_ms>5000</result_waittime_ms>
						String timeOuterror = "Remote server timed out \n"
								+ "Result waittime = " + waitTime
								+ " ms elapsed,\nPlease try again";
						log.error(timeOuterror);

						log.debug("ontology waited " + deltaTime + "ms for "
								+ er.getRequestHandler().getClass().getName());

						ResponseMessageType responseMsgType = MessageFactory
								.doBuildErrorResponse(null, timeOuterror);
						ontologyDataResponse = MessageFactory
								.convertToXMLString(responseMsgType);

					} else {
						log.error("ontology data response is null");
						log.info("waitTime is " + waitTime);
						log.debug("ontology waited " + deltaTime + "ms for "
								+ er.getRequestHandler().getClass().getName());
						ResponseMessageType responseMsgType = MessageFactory
								.doBuildErrorResponse(null, unknownErrorMessage);
						ontologyDataResponse = MessageFactory
								.convertToXMLString(responseMsgType);
					}
				}
			} catch (InterruptedException e) {
				log.error(e.getMessage());
				throw new I2B2Exception(
						"Thread error while running Ontology job ");
			} finally {
				t.interrupt();
				er = null;
				t = null;
			}
		}
		returnElement = MessageFactory
				.createResponseOMElementFromString(ontologyDataResponse);

		return returnElement;
	}

	public OMElement addChild(OMElement addChildElement) throws I2B2Exception {

		OMElement returnElement = null;
		String ontologyDataResponse = null;
		String unknownErrorMessage = "Error message delivered from the remote server \n"
				+ "You may wish to retry your last action";

		if (addChildElement == null) {
			log.error("Incoming Ontology request is null");
			ResponseMessageType responseMsgType = MessageFactory
					.doBuildErrorResponse(null, unknownErrorMessage);
			ontologyDataResponse = MessageFactory
					.convertToXMLString(responseMsgType);
			return MessageFactory
					.createResponseOMElementFromString(ontologyDataResponse);
		}
		String requestElementString = addChildElement.toString();
		AddChildDataMessage childDataMsg = new AddChildDataMessage(
				requestElementString);
		long waitTime = 0;
		if ((childDataMsg.getRequestMessageType() != null) && (childDataMsg.getRequestMessageType().getRequestHeader() != null)) {
			waitTime = childDataMsg.getRequestMessageType()
					.getRequestHeader().getResultWaittimeMs();
		}

		// do Ontology query processing inside thread, so that
		// service could sends back message with timeout error.
		// ExecutorRunnable er = new ExecutorRunnable();
		return execute(new AddChildHandler(childDataMsg), waitTime);

	}

	public OMElement modifyChild(OMElement modifyChildElement) throws I2B2Exception {

		OMElement returnElement = null;
		String ontologyDataResponse = null;
		String unknownErrorMessage = "Error message delivered from the remote server \n"
				+ "You may wish to retry your last action";

		if (modifyChildElement == null) {
			log.error("Incoming Ontology request is null");
			ResponseMessageType responseMsgType = MessageFactory
					.doBuildErrorResponse(null, unknownErrorMessage);
			ontologyDataResponse = MessageFactory
					.convertToXMLString(responseMsgType);
			return MessageFactory
					.createResponseOMElementFromString(ontologyDataResponse);
		}
		String requestElementString = modifyChildElement.toString();
		log.debug(requestElementString);
		ModifyChildDataMessage childDataMsg = new ModifyChildDataMessage(
				requestElementString);
		long waitTime = 0;
		if ((childDataMsg.getRequestMessageType() != null) && (childDataMsg.getRequestMessageType().getRequestHeader() != null)) {
			waitTime = childDataMsg.getRequestMessageType()
					.getRequestHeader().getResultWaittimeMs();
		}

		// do Ontology query processing inside thread, so that
		// service could sends back message with timeout error.
		// ExecutorRunnable er = new ExecutorRunnable();
		return execute(new ModifyChildHandler(childDataMsg), waitTime);

	}

	public OMElement deleteChild(OMElement deleteNodeElement) throws Exception {
		OMElement returnElement = null;
		String ontologyDataResponse = null;
		String unknownErrorMessage = "Error message delivered from the remote server \n"
				+ "You may wish to retry your last action";

		if (deleteNodeElement == null) {
			log.error("Incoming Ontology request is null");

			ResponseMessageType responseMsgType = MessageFactory
					.doBuildErrorResponse(null, unknownErrorMessage);
			ontologyDataResponse = MessageFactory
					.convertToXMLString(responseMsgType);
			return MessageFactory
					.createResponseOMElementFromString(ontologyDataResponse);
		}

		String requestElementString = deleteNodeElement.toString();
		DeleteChildDataMessage deleteDataMsg = new DeleteChildDataMessage(
				requestElementString);
		// log.info(requestElementString);
		// deleteDataMsg.setRequestMessageType(requestElementString);

		long waitTime = 0;
		if ((deleteDataMsg.getRequestMessageType() != null) && (deleteDataMsg.getRequestMessageType().getRequestHeader() != null)) {
			waitTime = deleteDataMsg.getRequestMessageType()
					.getRequestHeader().getResultWaittimeMs();
		}

		// do Workplace query processing inside thread, so that
		// service could send back message with timeout error.
		// ExecutorRunnable er = new ExecutorRunnable();
		return execute(new DeleteChildHandler(deleteDataMsg), waitTime);

	}

	public OMElement updateCRCConcept(OMElement updateCRCConceptElement)
			throws I2B2Exception {

		OMElement returnElement = null;
		String ontologyDataResponse = null;
		String unknownErrorMessage = "Error message delivered from the remote server \n"
				+ "You may wish to retry your last action";

		if (updateCRCConceptElement == null) {
			log.error("Incoming Ontology request is null");
			ResponseMessageType responseMsgType = MessageFactory
					.doBuildErrorResponse(null, unknownErrorMessage);
			ontologyDataResponse = MessageFactory
					.convertToXMLString(responseMsgType);
			return MessageFactory
					.createResponseOMElementFromString(ontologyDataResponse);
		}
		String requestElementString = updateCRCConceptElement.toString();
		CRCUpdateConceptMessage childDataMsg = new CRCUpdateConceptMessage(
				requestElementString);
		long waitTime = 0;
		if ((childDataMsg.getRequestMessageType() != null) && (childDataMsg.getRequestMessageType().getRequestHeader() != null)) {
			waitTime = childDataMsg.getRequestMessageType()
					.getRequestHeader().getResultWaittimeMs();
		}

		// do Ontology query processing inside thread, so that
		// service could sends back message with timeout error.
		// ExecutorRunnable er = new ExecutorRunnable();
		return execute(new CRCConceptUpdateHandler(childDataMsg), waitTime);

	}

	public OMElement updateConceptTotalNum(OMElement updateConceptTotalNumElement)
			throws I2B2Exception {

		OMElement returnElement = null;
		String ontologyDataResponse = null;
		String unknownErrorMessage = "Error message delivered from the remote server \n"
				+ "You may wish to retry your last action";

		if (updateConceptTotalNumElement == null) {
			log.error("Incoming Ontology request is null");
			ResponseMessageType responseMsgType = MessageFactory
					.doBuildErrorResponse(null, unknownErrorMessage);
			ontologyDataResponse = MessageFactory
					.convertToXMLString(responseMsgType);
			return MessageFactory
					.createResponseOMElementFromString(ontologyDataResponse);
		}
		String requestElementString = updateConceptTotalNumElement.toString();
		UpdateTotalNumMessage childDataMsg = new UpdateTotalNumMessage(
				requestElementString);
		long waitTime = 0;
		if ((childDataMsg.getRequestMessageType() != null) && (childDataMsg.getRequestMessageType().getRequestHeader() != null)) {
			waitTime = childDataMsg.getRequestMessageType()
					.getRequestHeader().getResultWaittimeMs();
		}

		// do Ontology query processing inside thread, so that
		// service could sends back message with timeout error.
		// ExecutorRunnable er = new ExecutorRunnable();
		return execute(new UpdateTotalNumHandler(childDataMsg), waitTime);

	}

	public OMElement getProcessStatus(OMElement getOntProcessStatus)
			throws I2B2Exception {

		OMElement returnElement = null;
		String ontologyDataResponse = null;
		String unknownErrorMessage = "Error message delivered from the remote server \n"
				+ "You may wish to retry your last action";

		if (getOntProcessStatus == null) {
			log.error("Incoming Ontology request is null");
			ResponseMessageType responseMsgType = MessageFactory
					.doBuildErrorResponse(null, unknownErrorMessage);
			ontologyDataResponse = MessageFactory
					.convertToXMLString(responseMsgType);
			return MessageFactory
					.createResponseOMElementFromString(ontologyDataResponse);
		}
		String requestElementString = getOntProcessStatus.toString();
		GetOntProcessStatusMessage childDataMsg = new GetOntProcessStatusMessage(
				requestElementString);
		long waitTime = 0;
		if ((childDataMsg.getRequestMessageType() != null) && (childDataMsg.getRequestMessageType().getRequestHeader() != null)) {
			waitTime = childDataMsg.getRequestMessageType()
					.getRequestHeader().getResultWaittimeMs();
		}

		// do Ontology query processing inside thread, so that
		// service could sends back message with timeout error.
		// ExecutorRunnable er = new ExecutorRunnable();
		return execute(new GetOntProcessStatusHandler(childDataMsg), waitTime);

	}

	/**
	 * This function is main webservice interface to get vocab data for a query.
	 * It uses AXIOM elements(OMElement) to conveniently parse xml messages.
	 * 
	 * It excepts incoming request in i2b2 message format, which wraps an
	 * Ontology query inside a vocab query request object. The response is also
	 * will be in i2b2 message format, which will wrap vocab data object. Vocab
	 * data object will have all the results returned by the query.
	 * 
	 * 
	 * @param OMElement
	 *            getDirtyStateElement
	 * @return OMElement in i2b2message format
	 * @throws Exception
	 */
	public OMElement getDirtyState(OMElement getDirtyStateElement)
			throws I2B2Exception {
		OMElement returnElement = null;
		String ontologyDataResponse = null;
		String unknownErrorMessage = "Error message delivered from the remote server \n"
				+ "You may wish to retry your last action";

		if (getDirtyStateElement == null) {
			log.error("Incoming Ontology request is null");
			ResponseMessageType responseMsgType = MessageFactory
					.doBuildErrorResponse(null, unknownErrorMessage);
			ontologyDataResponse = MessageFactory
					.convertToXMLString(responseMsgType);
			return MessageFactory
					.createResponseOMElementFromString(ontologyDataResponse);
		}

		String requestElementString = getDirtyStateElement.toString();
		GetDirtyStateDataMessage dirtyStateDataMsg = new GetDirtyStateDataMessage(
				requestElementString);

		long waitTime = 0;
		if ((dirtyStateDataMsg.getRequestMessageType() != null) && (dirtyStateDataMsg.getRequestMessageType().getRequestHeader() != null)) {
			waitTime = dirtyStateDataMsg.getRequestMessageType()
					.getRequestHeader().getResultWaittimeMs();
		}

		// do Ontology query processing inside thread, so that
		// service could sends back message with timeout error.
		// ExecutorRunnable er = new ExecutorRunnable();
		return execute(new GetDirtyStateHandler(dirtyStateDataMsg), waitTime);
	}

	/**
	 * This function is main webservice interface to get modifier data for a query.
	 * It uses AXIOM elements(OMElement) to conveniently parse xml messages.
	 * 
	 * It excepts incoming request in i2b2 message format, which wraps an
	 * Ontology query inside a modifier query request object. The response is also
	 * will be in i2b2 message format, which will wrap modifier data object. Modifier
	 * data object will have all the results returned by the query.
	 * 
	 * 
	 * @param getModifiers
	 * @return OMElement in i2b2message format
	 * @throws Exception
	 */
	public OMElement getModifiers(OMElement getModifiersElement)
			throws I2B2Exception {

		OMElement returnElement = null;
		String ontologyDataResponse = null;
		String unknownErrorMessage = "Error message delivered from the remote server \n"
				+ "You may wish to retry your last action";

		if (getModifiersElement == null) {
			log.error("Incoming Ontology request is null");
			ResponseMessageType responseMsgType = MessageFactory
					.doBuildErrorResponse(null, unknownErrorMessage);
			ontologyDataResponse = MessageFactory
					.convertToXMLString(responseMsgType);
			return MessageFactory
					.createResponseOMElementFromString(ontologyDataResponse);
		}
		String requestElementString = getModifiersElement.toString();
		GetModifiersDataMessage modifiersDataMsg = new GetModifiersDataMessage(
				requestElementString);
		long waitTime = 0;
		if ((modifiersDataMsg.getRequestMessageType() != null) && (modifiersDataMsg.getRequestMessageType().getRequestHeader() != null)) {
			waitTime = modifiersDataMsg.getRequestMessageType()
					.getRequestHeader().getResultWaittimeMs();
		}

		// do Ontology query processing inside thread, so that
		// service could sends back message with timeout error.
		// ExecutorRunnable er = new ExecutorRunnable();
		return execute(new GetModifiersHandler(modifiersDataMsg), waitTime);

	}
	/**
	 * This function is main webservice interface to get modifier data for a query.
	 * It uses AXIOM elements(OMElement) to conveniently parse xml messages.
	 * 
	 * It excepts incoming request in i2b2 message format, which wraps an
	 * Ontology query inside a modifier query request object. The response is also
	 * will be in i2b2 message format, which will wrap modifier data object. Modifier
	 * data object will have all the results returned by the query.
	 * 
	 * 
	 * @param getModifierInfo
	 * @return OMElement in i2b2message format
	 * @throws Exception
	 */
	public OMElement getModifierInfo(OMElement getModifierInfoElement)
			throws I2B2Exception {

		OMElement returnElement = null;
		String ontologyDataResponse = null;
		String unknownErrorMessage = "Error message delivered from the remote server \n"
				+ "You may wish to retry your last action";

		if (getModifierInfoElement == null) {
			log.error("Incoming Ontology request is null");
			ResponseMessageType responseMsgType = MessageFactory
					.doBuildErrorResponse(null, unknownErrorMessage);
			ontologyDataResponse = MessageFactory
					.convertToXMLString(responseMsgType);
			return MessageFactory
					.createResponseOMElementFromString(ontologyDataResponse);
		}
		String requestElementString = getModifierInfoElement.toString();
		GetModifierInfoDataMessage modifierInfoDataMsg = new GetModifierInfoDataMessage(
				requestElementString);
		long waitTime = 0;
		if ((modifierInfoDataMsg.getRequestMessageType() != null) && (modifierInfoDataMsg.getRequestMessageType().getRequestHeader() != null)) {
			waitTime = modifierInfoDataMsg.getRequestMessageType()
					.getRequestHeader().getResultWaittimeMs();
		}

		// do Ontology query processing inside thread, so that
		// service could sends back message with timeout error.
		// ExecutorRunnable er = new ExecutorRunnable();
		return execute(new GetModifierInfoHandler(modifierInfoDataMsg), waitTime);

	}

	/**
	 * This function is main webservice interface to get modifier data for a query.
	 * It uses AXIOM elements(OMElement) to conveniently parse xml messages.
	 * 
	 * It excepts incoming request in i2b2 message format, which wraps an
	 * Ontology query inside a modifier query request object. The response is also
	 * will be in i2b2 message format, which will wrap modifier data object. Modifier
	 * data object will have all the results returned by the query.
	 * 
	 * 
	 * @param getModifierInfo
	 * @return OMElement in i2b2message format
	 * @throws Exception
	 */
	public OMElement getModifierChildren(OMElement getModifierChildrenElement)
			throws I2B2Exception {

		OMElement returnElement = null;
		String ontologyDataResponse = null;
		String unknownErrorMessage = "Error message delivered from the remote server \n"
				+ "You may wish to retry your last action";

		if (getModifierChildrenElement == null) {
			log.error("Incoming Ontology request is null");
			ResponseMessageType responseMsgType = MessageFactory
					.doBuildErrorResponse(null, unknownErrorMessage);
			ontologyDataResponse = MessageFactory
					.convertToXMLString(responseMsgType);
			return MessageFactory
					.createResponseOMElementFromString(ontologyDataResponse);
		}
		String requestElementString = getModifierChildrenElement.toString();
		GetModifierChildrenDataMessage modifierChildrenDataMsg = new GetModifierChildrenDataMessage(
				requestElementString);
		long waitTime = 0;
		if ((modifierChildrenDataMsg.getRequestMessageType() != null) && (modifierChildrenDataMsg.getRequestMessageType().getRequestHeader() != null)) {
			waitTime = modifierChildrenDataMsg.getRequestMessageType()
					.getRequestHeader().getResultWaittimeMs();
		}

		// do Ontology query processing inside thread, so that
		// service could sends back message with timeout error.
		// ExecutorRunnable er = new ExecutorRunnable();
		return execute(new GetModifierChildrenHandler(modifierChildrenDataMsg), waitTime);

	}

	public OMElement addModifier(OMElement addChildElement) throws I2B2Exception {

		OMElement returnElement = null;
		String ontologyDataResponse = null;
		String unknownErrorMessage = "Error message delivered from the remote server \n"
				+ "You may wish to retry your last action";

		if (addChildElement == null) {
			log.error("Incoming Ontology request is null");
			ResponseMessageType responseMsgType = MessageFactory
					.doBuildErrorResponse(null, unknownErrorMessage);
			ontologyDataResponse = MessageFactory
					.convertToXMLString(responseMsgType);
			return MessageFactory
					.createResponseOMElementFromString(ontologyDataResponse);
		}
		String requestElementString = addChildElement.toString();
		AddChildDataMessage childDataMsg = new AddChildDataMessage(
				requestElementString);
		long waitTime = 0;
		if ((childDataMsg.getRequestMessageType() != null) && (childDataMsg.getRequestMessageType().getRequestHeader() != null)) {
			waitTime = childDataMsg.getRequestMessageType()
					.getRequestHeader().getResultWaittimeMs();
		}

		// do Ontology query processing inside thread, so that
		// service could sends back message with timeout error.
		// ExecutorRunnable er = new ExecutorRunnable();
		return execute(new AddModifierHandler(childDataMsg), waitTime);

	}

	public OMElement excludeModifier(OMElement addChildElement) throws I2B2Exception {

		OMElement returnElement = null;
		String ontologyDataResponse = null;
		String unknownErrorMessage = "Error message delivered from the remote server \n"
				+ "You may wish to retry your last action";

		if (addChildElement == null) {
			log.error("Incoming Ontology request is null");
			ResponseMessageType responseMsgType = MessageFactory
					.doBuildErrorResponse(null, unknownErrorMessage);
			ontologyDataResponse = MessageFactory
					.convertToXMLString(responseMsgType);
			return MessageFactory
					.createResponseOMElementFromString(ontologyDataResponse);
		}
		String requestElementString = addChildElement.toString();
		AddChildDataMessage childDataMsg = new AddChildDataMessage(
				requestElementString);
		long waitTime = 0;
		if ((childDataMsg.getRequestMessageType() != null) && (childDataMsg.getRequestMessageType().getRequestHeader() != null)) {
			waitTime = childDataMsg.getRequestMessageType()
					.getRequestHeader().getResultWaittimeMs();
		}

		// do Ontology query processing inside thread, so that
		// service could sends back message with timeout error.
		// ExecutorRunnable er = new ExecutorRunnable();
		return execute(new ExcludeModifierHandler(childDataMsg), waitTime);

	}

	/**
	 * This function is main webservice interface to get vocab data for a query.
	 * It uses AXIOM elements(OMElement) to conveniently parse xml messages.
	 * 
	 * It excepts incoming request in i2b2 message format, which wraps an
	 * Ontology query inside a vocab query request object. The response is also
	 * will be in i2b2 message format, which will wrap vocab data object. Vocab
	 * data object will have all the results returned by the query.
	 * 
	 * 
	 * @param OMElement
	 *            getModifierNameInfoElement
	 * @return OMElement in i2b2message format
	 * @throws Exception
	 */
	public OMElement getModifierNameInfo(OMElement getNameInfoElement)
			throws I2B2Exception {
		OMElement returnElement = null;
		String ontologyDataResponse = null;
		String unknownErrorMessage = "Error message delivered from the remote server \n"
				+ "You may wish to retry your last action";

		if (getNameInfoElement == null) {
			log.error("Incoming Ontology request is null");
			ResponseMessageType responseMsgType = MessageFactory
					.doBuildErrorResponse(null, unknownErrorMessage);
			ontologyDataResponse = MessageFactory
					.convertToXMLString(responseMsgType);
			return MessageFactory
					.createResponseOMElementFromString(ontologyDataResponse);
		}

		String requestElementString = getNameInfoElement.toString();
		GetNameInfoDataMessage nameInfoDataMsg = new GetNameInfoDataMessage(
				requestElementString);

		long waitTime = 0;
		if ((nameInfoDataMsg.getRequestMessageType() != null) && (nameInfoDataMsg.getRequestMessageType().getRequestHeader() != null)) {
			waitTime = nameInfoDataMsg.getRequestMessageType()
					.getRequestHeader().getResultWaittimeMs();
		}

		// do Ontology query processing inside thread, so that
		// service could sends back message with timeout error.

		// ExecutorRunnable er = new ExecutorRunnable();
		return execute(new GetModifierNameInfoHandler(nameInfoDataMsg), waitTime);
	}

	/**
	 * This function is main webservice interface to get vocab data for a query.
	 * It uses AXIOM elements(OMElement) to conveniently parse xml messages.
	 * 
	 * It excepts incoming request in i2b2 message format, which wraps an
	 * Ontology query inside a vocab query request object. The response is also
	 * will be in i2b2 message format, which will wrap vocab data object. Vocab
	 * data object will have all the results returned by the query.
	 * 
	 * 
	 * @param OMElement
	 *            getModifierCodeInfoElement
	 * @return OMElement in i2b2message format
	 * @throws Exception
	 */
	public OMElement getModifierCodeInfo(OMElement getCodeInfoElement)
			throws I2B2Exception {
		OMElement returnElement = null;
		String ontologyDataResponse = null;
		String unknownErrorMessage = "Error message delivered from the remote server \n"
				+ "You may wish to retry your last action";

		if (getCodeInfoElement == null) {
			log.error("Incoming Ontology request is null");
			ResponseMessageType responseMsgType = MessageFactory
					.doBuildErrorResponse(null, unknownErrorMessage);
			ontologyDataResponse = MessageFactory
					.convertToXMLString(responseMsgType);
			return MessageFactory
					.createResponseOMElementFromString(ontologyDataResponse);
		}

		String requestElementString = getCodeInfoElement.toString();
		GetCodeInfoDataMessage codeInfoDataMsg = new GetCodeInfoDataMessage(
				requestElementString);

		long waitTime = 0;
		if ((codeInfoDataMsg.getRequestMessageType() != null) && (codeInfoDataMsg.getRequestMessageType().getRequestHeader() != null)) {
			waitTime = codeInfoDataMsg.getRequestMessageType()
					.getRequestHeader().getResultWaittimeMs();
		}

		// do Ontology query processing inside thread, so that
		// service could sends back message with timeout error.

		// ExecutorRunnable er = new ExecutorRunnable();
		return execute(new GetModifierCodeInfoHandler(codeInfoDataMsg), waitTime);
	}



	public OMElement loadMetadata(OMElement loadElement) throws I2B2Exception {

		OMElement returnElement = null;
		String ontologyDataResponse = null;
		String unknownErrorMessage = "Error message delivered from the remote server \n"
				+ "You may wish to retry your last action";

		if (loadElement == null) {
			log.error("Incoming Ontology request is null");
			ResponseMessageType responseMsgType = MessageFactory
					.doBuildErrorResponse(null, unknownErrorMessage);
			ontologyDataResponse = MessageFactory
					.convertToXMLString(responseMsgType);
			return MessageFactory
					.createResponseOMElementFromString(ontologyDataResponse);
		}
		String requestElementString = loadElement.toString();

		LoadDataMessage loadDataMsg = new LoadDataMessage(
				requestElementString);

		log.info(loadDataMsg.getMetadataLoad().getTableName());
		long waitTime = 0;
		if ((loadDataMsg.getRequestMessageType() != null) && (loadDataMsg.getRequestMessageType().getRequestHeader() != null)) {
			waitTime = loadDataMsg.getRequestMessageType()
					.getRequestHeader().getResultWaittimeMs();
		}

		// do Ontology query processing inside thread, so that
		// service could sends back message with timeout error.
		// ExecutorRunnable er = new ExecutorRunnable();
		if(loadDataMsg.getMetadataLoad().getTableName().equalsIgnoreCase("SCHEMES")) {
			log.info(requestElementString);
			return execute(new LoadSchemesHandler(loadDataMsg), waitTime);
		}
		else if(loadDataMsg.getMetadataLoad().getTableName().equalsIgnoreCase("TABLE_ACCESS")) {
			log.info(requestElementString);
			return execute(new LoadTableAccessHandler(loadDataMsg), waitTime);
		}
		else
			return execute(new LoadMetadataHandler(loadDataMsg), waitTime);

	}


	public OMElement checkForTableExistence(OMElement loadElement) throws I2B2Exception {

		OMElement returnElement = null;
		String ontologyDataResponse = null;
		String unknownErrorMessage = "Error message delivered from the remote server \n"
				+ "You may wish to retry your last action";

		if (loadElement == null) {
			log.error("Incoming Ontology request is null");
			ResponseMessageType responseMsgType = MessageFactory
					.doBuildErrorResponse(null, unknownErrorMessage);
			ontologyDataResponse = MessageFactory
					.convertToXMLString(responseMsgType);
			return MessageFactory
					.createResponseOMElementFromString(ontologyDataResponse);
		}
		String requestElementString = loadElement.toString();

		LoadDataMessage loadDataMsg = new LoadDataMessage(
				requestElementString);

		log.info(loadDataMsg.getMetadataLoad().getTableName());
		long waitTime = 0;
		if ((loadDataMsg.getRequestMessageType() != null) && (loadDataMsg.getRequestMessageType().getRequestHeader() != null)) {
			waitTime = loadDataMsg.getRequestMessageType()
					.getRequestHeader().getResultWaittimeMs();
		}

		// do Ontology query processing inside thread, so that
		// service could sends back message with timeout error.
		// ExecutorRunnable er = new ExecutorRunnable();


		return execute(new CheckMetadataTableHandler(loadDataMsg), waitTime);

	}

	/** swc20160515
	 * This function is main webservice interface to get the I2B2HIVE.ONT_DB_LOOKUP data.
	 * It uses AXIOM elements(OMElement) to conveniently parse xml messages.
	 * 
	 * It accepts incoming request, and returns a response, both in i2b2 message format. 
	 * 
	 * @param  OMElement
	 *            getAllDblookupsElement
	 * @return OMElement in i2b2message format
	 * @throws Exception
	 */
	public OMElement getAllDblookups(OMElement getAllDblookupsElement) throws I2B2Exception {
		String ontologyDataResponse = null;
		String unknownErrMsg = null;
		if (null == getAllDblookupsElement) {
			log.error("Incoming Ontology request is null");
			unknownErrMsg = "Error message delivered from the remote server.\nYou may wish to retry your last action";
			ResponseMessageType responseMsgType = MessageFactory.doBuildErrorResponse(null, unknownErrMsg);
			ontologyDataResponse = MessageFactory.convertToXMLString(responseMsgType);
			return MessageFactory.createResponseOMElementFromString(ontologyDataResponse);
		}
		String requestElementString = getAllDblookupsElement.toString();
		GetAllDblookupsDataMessage dblookupsDataMsg = new GetAllDblookupsDataMessage(requestElementString);
		long waitTime = 0;
		if ((null != dblookupsDataMsg.getRequestMessageType()) && (null != dblookupsDataMsg.getRequestMessageType().getRequestHeader())) {
			waitTime = dblookupsDataMsg.getRequestMessageType().getRequestHeader().getResultWaittimeMs();
		}
		// do processing inside thread, so that service could send back message with timeout error.
		return execute(new GetAllDblookupsHandler(dblookupsDataMsg), waitTime);
	}

	/** swc20160517
	 * This function is main webservice interface to get specific I2B2HIVE.ONT_DB_LOOKUP data.
	 * It uses AXIOM elements(OMElement) to conveniently parse xml messages.
	 * 
	 * It accepts incoming request, and returns a response, both in i2b2 message format. 
	 * 
	 * @param  OMElement
	 *            getDblookupElement
	 * @return OMElement in i2b2message format
	 * @throws Exception
	 */
	public OMElement getDblookup(OMElement getDblookupElement) throws I2B2Exception {
		String ontologyDataResponse = null;
		String unknownErrMsg = null;
		if (null == getDblookupElement) {
			log.error("Incoming Ontology request is null");
			unknownErrMsg = "Error message delivered from the remote server.\nYou may wish to retry your last action";
			ResponseMessageType responseMsgType = MessageFactory.doBuildErrorResponse(null, unknownErrMsg);
			ontologyDataResponse = MessageFactory.convertToXMLString(responseMsgType);
			return MessageFactory.createResponseOMElementFromString(ontologyDataResponse);
		}
		String requestElementString = getDblookupElement.toString();
		GetDblookupDataMessage dblookupDataMsg = new GetDblookupDataMessage(requestElementString);
		long waitTime = 0;
		if ((null != dblookupDataMsg.getRequestMessageType()) && (null != dblookupDataMsg.getRequestMessageType().getRequestHeader())) {
			waitTime = dblookupDataMsg.getRequestMessageType().getRequestHeader().getResultWaittimeMs();
		}
		// do processing inside thread, so that service could send back message with timeout error.
		return execute(new GetDblookupHandler(dblookupDataMsg), waitTime);
	}

	/** swc20160516
	 * This function is main webservice interface to add a new entry to the I2B2HIVE.ONT_DB_LOOKUP data.
	 * It uses AXIOM elements(OMElement) to conveniently parse xml messages.
	 * 
	 * It accepts incoming request, and returns a response, both in i2b2 message format. 
	 * 
	 * @param  OMElement
	 *            getAllDblookupsElement
	 * @return OMElement in i2b2message format
	 * @throws Exception
	 */
	public OMElement setDblookup(OMElement setDblookupElement) throws I2B2Exception {
		//		OMElement returnElement = null;
		String ontologyDataResponse = null;
		String unknownErrMsg = null;
		if (null == setDblookupElement) {
			log.error("Incoming Ontology request is null");
			unknownErrMsg = "Error message delivered from the remote server.\nYou may wish to retry your last action";
			ResponseMessageType responseMsgType = MessageFactory.doBuildErrorResponse(null, unknownErrMsg);
			ontologyDataResponse = MessageFactory.convertToXMLString(responseMsgType);
			return MessageFactory.createResponseOMElementFromString(ontologyDataResponse);
		}
		String requestElementString = setDblookupElement.toString();
		SetDblookupDataMessage dblookupDataMsg = new SetDblookupDataMessage(requestElementString);
		long waitTime = 0;
		if ((null != dblookupDataMsg.getRequestMessageType()) && (null != dblookupDataMsg.getRequestMessageType().getRequestHeader())) {
			waitTime = dblookupDataMsg.getRequestMessageType().getRequestHeader().getResultWaittimeMs();
		}
		// do processing inside thread, so that service could send back message with timeout error.
		return execute(new SetDblookupHandler(dblookupDataMsg), waitTime);
	}

	/** swc20160518
	 * This function is main webservice interface to delete specific I2B2HIVE.ONT_DB_LOOKUP data.
	 * It uses AXIOM elements(OMElement) to conveniently parse xml messages.
	 * 
	 * It accepts incoming request, and returns a response, both in i2b2 message format. 
	 * 
	 * @param  OMElement
	 *            deleteDblookupElement
	 * @return OMElement in i2b2message format
	 * @throws Exception
	 */
	public OMElement deleteDblookup(OMElement deleteDblookupElement) throws I2B2Exception {
		String ontologyDataResponse = null;
		String unknownErrMsg = null;
		if (null == deleteDblookupElement) {
			log.error("Incoming Ontology request is null");
			unknownErrMsg = "Error message delivered from the remote server.\nYou may wish to retry your last action";
			ResponseMessageType responseMsgType = MessageFactory.doBuildErrorResponse(null, unknownErrMsg);
			ontologyDataResponse = MessageFactory.convertToXMLString(responseMsgType);
			return MessageFactory.createResponseOMElementFromString(ontologyDataResponse);
		}
		String requestElementString = deleteDblookupElement.toString();
		DeleteDblookupDataMessage dblookupDataMsg = new DeleteDblookupDataMessage(requestElementString);
		long waitTime = 0;
		if ((null != dblookupDataMsg.getRequestMessageType()) && (null != dblookupDataMsg.getRequestMessageType().getRequestHeader())) {
			waitTime = dblookupDataMsg.getRequestMessageType().getRequestHeader().getResultWaittimeMs();
		}
		// do processing inside thread, so that service could send back message with timeout error.
		return execute(new DeleteDblookupHandler(dblookupDataMsg), waitTime);
	}

}
