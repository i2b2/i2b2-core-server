package edu.harvard.i2b2.crc.datavo.pdo.query;

import java.io.StringWriter;

import javax.xml.bind.JAXBElement;

import org.junit.BeforeClass;
import org.junit.Test;

import edu.harvard.i2b2.common.util.jaxb.JAXBUnWrapHelper;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtil;
import edu.harvard.i2b2.crc.axis2.PdoQueryTest;
import edu.harvard.i2b2.crc.datavo.CRCJAXBUtil;
import edu.harvard.i2b2.crc.datavo.i2b2message.BodyType;
import edu.harvard.i2b2.crc.datavo.i2b2message.RequestMessageType;
import edu.harvard.i2b2.crc.datavo.i2b2result.DataType;
import edu.harvard.i2b2.crc.datavo.i2b2result.ResultEnvelopeType;
import edu.harvard.i2b2.crc.datavo.i2b2result.ResultType;
import edu.harvard.i2b2.crc.datavo.pdo.ObservationSet;
import edu.harvard.i2b2.crc.datavo.pdo.ObservationType;
import edu.harvard.i2b2.crc.datavo.pdo.PatientDataType;
import edu.harvard.i2b2.crc.datavo.pdo.PatientIdType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.XmlValueType;

public class PDOResponseTypeTest {
	private static String testFileDir = null;

	@BeforeClass
	public static void init() throws Exception {
		testFileDir = System.getProperty("testfiledir");
		System.out.println("test file dir " + testFileDir);

		if (!((testFileDir != null) && (testFileDir.trim().length() > 0))) {
			throw new Exception(
					"please provide test file directory info -Dtestfiledir");
		}
	}

	@Test
	public void testPDOMarshall() throws Exception {
		PatientDataType patientData = new PatientDataType();
		ObservationType observation = new ObservationType();
		// observation.setPatientId("patientid");
		// observation.setPatientIdSource("source");
		PatientIdType patientIdType = new PatientIdType();
		// patientIdType.setSource("soruce");
		patientIdType.setValue("patient_id");
		observation.setPatientId(patientIdType);
		ObservationSet observationSet = new ObservationSet();
		observationSet.getObservation().add(observation);
		patientData.getObservationSet().add(observationSet);

		try {
			JAXBUtil jaxbUtil = CRCJAXBUtil.getJAXBUtil();
			edu.harvard.i2b2.crc.datavo.pdo.ObjectFactory of = new edu.harvard.i2b2.crc.datavo.pdo.ObjectFactory();

			StringWriter strWriter = new StringWriter();
			jaxbUtil.marshaller(of.createPatientData(patientData), strWriter);
			System.out.print(strWriter.toString());
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	@Test
	public void testResult() throws Exception {
		DataType dataType = new DataType();
		dataType.setValue("100");
		dataType.setColumn("count");
		dataType.setType("int");

		ResultType resultType = new ResultType();
		resultType.setName("PATIENT_DEMOGRAPHICS_COUNT");
		resultType.getData().add(dataType);
		edu.harvard.i2b2.crc.datavo.i2b2result.ObjectFactory of = new edu.harvard.i2b2.crc.datavo.i2b2result.ObjectFactory();

		edu.harvard.i2b2.crc.datavo.i2b2result.BodyType bodyType = new edu.harvard.i2b2.crc.datavo.i2b2result.BodyType();
		bodyType.getAny().add(of.createResult(resultType));
		ResultEnvelopeType resultEnvelopeType = new ResultEnvelopeType();
		resultEnvelopeType.setBody(bodyType);

		StringWriter strWriter = new StringWriter();
		JAXBUtil jaxbUtil = CRCJAXBUtil.getJAXBUtil();
		jaxbUtil.marshaller(of.createI2B2ResultEnvelope(resultEnvelopeType),
				strWriter);

		System.out.println("Results marshalled" + strWriter.toString());
		JAXBElement jaxbElement = jaxbUtil.unMashallFromString(strWriter
				.toString());
		ResultEnvelopeType resultEnvelopeType1 = (ResultEnvelopeType) jaxbElement
				.getValue();
		JAXBUnWrapHelper helper = new JAXBUnWrapHelper();
		ResultType umResultType = (ResultType) helper.getObjectByClass(
				resultEnvelopeType1.getBody().getAny(), ResultType.class);

		XmlValueType xmlValueType = new XmlValueType();
		xmlValueType.getContent().add(resultEnvelopeType);

		ResultEnvelopeType resultEnvelopeType2 = (ResultEnvelopeType) helper
				.getObjectByClass(xmlValueType.getContent(),
						ResultEnvelopeType.class);

		if (resultEnvelopeType2 == null) {
			System.out.println("null");
		}

	}

	@Test
	public void pdoRequestUnMarshallTest() throws Exception {
		String filename = testFileDir + "/pdo_query1.xml";
		// read file as string
		String requestString = PdoQueryTest.getQueryString(filename);
		// unmarshall
		JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil()
				.unMashallFromString(requestString);
		RequestMessageType r = (RequestMessageType) responseJaxb.getValue();
		BodyType bodyType = r.getMessageBody();
		// get body and search for analysis definition
		JAXBUnWrapHelper unWraphHelper = new JAXBUnWrapHelper();
		GetPDOFromInputListRequestType pdoReqType = (GetPDOFromInputListRequestType) unWraphHelper
				.getObjectByClass(bodyType.getAny(),
						GetPDOFromInputListRequestType.class);
		System.out.println("boolean flag " + checkForBlob(pdoReqType));

	}

	private boolean checkForBlob(
			GetPDOFromInputListRequestType getPDOFromInputListReqType) {
		boolean booleanConcept = true, booleanEid = true, booleanEvent = true, booleanObservation = true, booleanObserver = true, booleanPatient = true;
		boolean booleanPid = true;

		if (getPDOFromInputListReqType.getOutputOption()
				.getConceptSetUsingFilterList() != null) {
			booleanConcept = getPDOFromInputListReqType.getOutputOption()
					.getConceptSetUsingFilterList().isBlob();
			if (booleanConcept) {
				booleanConcept = false;
			} else {
				booleanConcept = true;
			}
		}
		if (getPDOFromInputListReqType.getOutputOption().getEidSet() != null) {
			booleanEid = getPDOFromInputListReqType.getOutputOption()
					.getEidSet().isBlob();
			if (booleanEid) {
				booleanEid = false;
			} else {
				booleanEid = true;
			}
		}
		if (getPDOFromInputListReqType.getOutputOption().getEventSet() != null) {
			booleanEvent = getPDOFromInputListReqType.getOutputOption()
					.getEventSet().isBlob();
			if (booleanEvent) {
				booleanEvent = false;
			} else {
				booleanEvent = true;
			}
		}
		if (getPDOFromInputListReqType.getOutputOption().getObservationSet() != null) {
			booleanObservation = getPDOFromInputListReqType.getOutputOption()
					.getObservationSet().isBlob();
			if (booleanObservation) {
				booleanObservation = false;
			} else {
				booleanObservation = true;
			}
		}
		if (getPDOFromInputListReqType.getOutputOption()
				.getObserverSetUsingFilterList() != null) {
			booleanObserver = getPDOFromInputListReqType.getOutputOption()
					.getObserverSetUsingFilterList().isBlob();
			if (booleanObserver) {
				booleanObserver = false;
			} else {
				booleanObserver = true;
			}
		}

		if (getPDOFromInputListReqType.getOutputOption().getPatientSet() != null) {
			booleanPatient = getPDOFromInputListReqType.getOutputOption()
					.getPatientSet().isBlob();
			if (booleanPatient) {
				booleanPatient = false;
			} else {
				booleanPatient = true;
			}
		}

		if (getPDOFromInputListReqType.getOutputOption().getPidSet() != null) {
			booleanPid = getPDOFromInputListReqType.getOutputOption()
					.getPidSet().isBlob();
			if (booleanPid) {
				booleanPid = false;
			} else {
				booleanPid = true;
			}
		}
		if (booleanConcept && booleanEid && booleanEvent && booleanObservation
				&& booleanObserver && booleanPatient && booleanPid) {
			return false;
		} else {
			return true;
		}

	}
}
