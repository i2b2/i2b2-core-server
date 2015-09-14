package edu.harvard.i2b2.common.datavo;

import edu.harvard.i2b2.common.util.jaxb.DTOFactory;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtil;
import edu.harvard.i2b2.core.datavo.pdo.EncounterIdeType;
import edu.harvard.i2b2.core.datavo.pdo.ObservationFactType;
import edu.harvard.i2b2.core.datavo.pdo.PatientDataType;
import edu.harvard.i2b2.core.datavo.pdo.PatientIdeType;
import edu.harvard.i2b2.core.datavo.pdo.VisitDimensionType;

import junit.framework.TestCase;

import java.io.StringWriter;

import java.util.Date;

import javax.xml.bind.JAXBElement;


public class PatientDataMessageTest extends TestCase {
    public VisitDimensionType getVisitDimensionType() {
        VisitDimensionType visitType = new VisitDimensionType();
        visitType.setPatientIde(getPatientIde());
        visitType.setEncounterIde(getEncounterIde());
        visitType.setSourcesystemCd("i2b2");

        return visitType;
    }

    private PatientIdeType getPatientIde() {
        PatientIdeType patientIde = new PatientIdeType();
        patientIde.setSource("i2b2");
        patientIde.setValue("10000");

        return patientIde;
    }

    private EncounterIdeType getEncounterIde() {
        EncounterIdeType encounterIde = new EncounterIdeType();
        encounterIde.setSource("i2b2");
        encounterIde.setValue("1000001");

        return encounterIde;
    }

    // Function to build observation fact
    public ObservationFactType getObservationFactType() {
        Date currentDate = new Date();
        ObservationFactType obType = new ObservationFactType();
        DTOFactory dtoFactory = new DTOFactory();
        obType.setEndDate(dtoFactory.getXMLGregorianCalendar(
                currentDate.getTime()));
        obType.setConceptCd("i2b2:60004");
        obType.setConfidenceNum(1d);
        obType.setEncounterIde(getEncounterIde());
        obType.setPatientIde(getPatientIde());
        obType.setSourcesystemCd("i2b2");
        obType.setTvalChar("tval");
        obType.setNvalNum(100.0);
        obType.setObservationBlob("BLOB");

        return obType;
    }

    public PatientDataType createPatientDataType() {
        PatientDataType patientData = new PatientDataType();
        PatientDataType.VisitDimensionSet vdSet = new PatientDataType.VisitDimensionSet();
        vdSet.getVisitDimension().add(getVisitDimensionType());
        patientData.setVisitDimensionSet(vdSet);

        PatientDataType.ObservationFactSet obsSet = new PatientDataType.ObservationFactSet();
        obsSet.getObservationFact().add(getObservationFactType());
        patientData.getObservationFactSet().add(obsSet);

        return patientData;
    }

    private String getXMLString(JAXBElement<?> jaxbElement)
        throws Exception {
        JAXBUtil jaxbUtil = new JAXBUtil(new String[] {
                    "edu.harvard.i2b2.core.datavo.pdo"
                });
        StringWriter strWriter = new StringWriter();

        jaxbUtil.marshaller(jaxbElement, strWriter);

        return strWriter.toString();
    }

    public void testPatientDataMessage() throws Exception {
        edu.harvard.i2b2.core.datavo.pdo.ObjectFactory of = new edu.harvard.i2b2.core.datavo.pdo.ObjectFactory();
        JAXBElement<?> jaxbElement = of.createPatientData(createPatientDataType());
        String xmlMessage = getXMLString(jaxbElement);
        System.out.println("Patient Data Message");
        System.out.println(xmlMessage);
    }
}
