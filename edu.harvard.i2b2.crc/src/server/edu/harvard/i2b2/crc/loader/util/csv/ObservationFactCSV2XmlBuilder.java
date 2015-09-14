package edu.harvard.i2b2.crc.loader.util.csv;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.Hashtable;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.jaxb.DTOFactory;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtil;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.crc.loader.datavo.CRCLoaderJAXBUtil;
import edu.harvard.i2b2.crc.datavo.pdo.ObservationSet;
import edu.harvard.i2b2.crc.datavo.pdo.ObservationType;
import edu.harvard.i2b2.crc.datavo.pdo.PatientIdType;

/**
 * This program creates ObservationFact portion of PatientData XML file, from
 * given input csv file.
 * 
 * Sample column heading : "Record_Id," "Report_Id," 1 "EMPI," 2 "MRN_Type," 3
 * "MRN," "LMRNote_Date_Time," 5 "Status," "Author," "COD," "Institution," 9
 * "Author_MRN," 10 "Subject," 11 "Classification," "Timestamp," 13
 * "Software_Version"
 * 
 * The above numbered columns are currently used.
 * 
 * @author rk903
 * 
 */
public class ObservationFactCSV2XmlBuilder {

	private String inputFileName = null;

	private String outputXmlFileName = null;

	private static Log log = LogFactory
			.getLog(ObservationFactCSV2XmlBuilder.class);

	private static Hashtable csvHeaderMap = null;

	private Hashtable csvHeaderColumnPosition = null;

	private JAXBUtil jaxbUtil = null;

	private DTOFactory dtoFactory = new DTOFactory();

	private edu.harvard.i2b2.crc.datavo.pdo.ObjectFactory pdoObjectFactory = new edu.harvard.i2b2.crc.datavo.pdo.ObjectFactory();
	static {
		csvHeaderMap = new Hashtable();
		csvHeaderMap.put("encounter_ide", new String[] { "encounter_id" });
		csvHeaderMap.put("patient_ide", new String[] { "patient_id" });
		// csvHeaderMap.put("encounter_ide_source",new
		// String[]{"MRN_TYPE","MRN_Type"});
		csvHeaderMap.put("encounter_ide_source",
				new String[] { "sourcesytem_cd" });
		csvHeaderMap.put("start_date", new String[] { "start_date" });
		csvHeaderMap.put("sourcesystem_cd", new String[] { "sourcesystem_cd" });
		csvHeaderMap.put("provider_id", new String[] { "provider_id" });
		csvHeaderMap.put("concept_cd", new String[] { "concept_cd" });
		csvHeaderMap.put("update_date", new String[] { "Timestamp",
				"Extraction_Timestamp" });
		csvHeaderMap.put("Negation", new String[] { "Negation" });
		csvHeaderMap.put("end_date", new String[] { "end_date" });
	}

	/**
	 * constructor
	 * 
	 * @param inputFileName
	 * @param outputXmlFileName
	 */
	public ObservationFactCSV2XmlBuilder(String inputFileName,
			String outputXmlFileName) {
		this.inputFileName = inputFileName;
		this.outputXmlFileName = outputXmlFileName;

		jaxbUtil = CRCLoaderJAXBUtil.getJAXBUtil();
	}

	/**
	 * Read csv file and create ObservationFact xml file.
	 * 
	 * @throws I2B2Exception
	 */
	public void buildXml() throws I2B2Exception {

		BufferedReader inputReader = null;
		BufferedWriter observationFactWriter = null;
		log.info("Before building ObservationFact xml: " + outputXmlFileName
				+ " for " + inputFileName);
		try {
			// read file and return buffered reader
			inputReader = new BufferedReader(new InputStreamReader(
					new FileInputStream(inputFileName)));
			observationFactWriter = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(outputXmlFileName)));
			writeHeader(observationFactWriter);
			observationFactWriter.write("<ns2:observation_set>\n");
			CSVFileReader csvReader = new CSVFileReader(inputFileName, ',',
					'\"');

			// read header and map header column position with element name
			java.util.Vector<String> headerFields = csvReader.readFields();
			int headerFieldCount = headerFields.size();
			csvHeaderColumnPosition = PatientDataXmlBuilder
					.getCsvHeaderColumnPosition(csvHeaderMap, headerFields);

			java.util.Vector<String> fields = null;
			int i = 0;
			ObservationSet observationSet = new ObservationSet();
			while ((fields = csvReader.readFields()) != null) {
				// skip line which have less columns, compared to header column
				if (headerFieldCount > fields.size()) {
					continue;
				}
				String col[] = (String[]) fields.toArray(new String[] {});

				observationSet.getObservation().add(getObservationFact(col));
				i++;
				if (i % 100 == 0) {
					i = 0;
					StringWriter strWriter = new StringWriter();
					try {
						jaxbUtil.marshaller(new JAXBElement(new QName("",
								"observation_set"), ObservationSet.class,
								observationSet), strWriter);
					} catch (JAXBUtilException e) {
						e.printStackTrace();
					}
					String xml = strWriter.toString();
					String observationStr = xml.substring(xml.indexOf('>', xml
							.indexOf("observation_set")) + 1, xml
							.indexOf("</observation_set"));
					observationFactWriter.write(observationStr);
					observationSet = new ObservationSet();
				}
			}
			if (i > 0) {
				StringWriter strWriter = new StringWriter();
				try {
					jaxbUtil.marshaller(new JAXBElement(new QName("",
							"observation_set"), ObservationSet.class,
							observationSet), strWriter);
				} catch (JAXBUtilException e) {
					e.printStackTrace();
				}
				String xml = strWriter.toString();
				String observationStr = xml.substring(xml.indexOf('>', xml
						.indexOf("observation_set")) + 1, xml
						.indexOf("</observation_set"));
				observationFactWriter.write(observationStr);

			}
			observationFactWriter.write("</ns2:observation_set>\n");
			writeEndDocument(observationFactWriter);
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new I2B2Exception("Build XML failed ", ex);
		} finally {
			try {
				if (inputReader != null) {
					inputReader.close();
				}

				if (observationFactWriter != null) {
					observationFactWriter.close();
				}
			} catch (IOException closeEx) {
				closeEx.printStackTrace();
			}
		}
		log.info("Finished building ObservationFact xml: " + outputXmlFileName
				+ " for " + inputFileName);
	}

	private String getColumnValue(String col[], String elementName) {
		String elementValue = PatientDataXmlBuilder.getColumnValue(
				csvHeaderColumnPosition, col, elementName);
		return elementValue;
	}

	private String getNegationPrefix(String col[]) {
		// check if negation present in this file.
		if (csvHeaderColumnPosition.get("Negation") == null) {
			return "";
		}

		String negation = getColumnValue(col, "Negation");
		if (negation != null) {
			if (negation.equals("negated")) {
				return "-NEG";
			} else {
				return "";
			}
		}
		return "";
	}

	private String getProviderId(String col[]) {
		if (csvHeaderColumnPosition.get("provider_id") == null) {
			return "@";
		} else {
			return getColumnValue(col, "provider_id");
		}
	}

	private String getEndDate(String col[]) {
		String endDate = getColumnValue(col, "end_date");
		if (endDate == null) {
			endDate = "";
		}
		return endDate;
	}

	private ObservationType getObservationFact(String col[]) {
		ObservationType observation = new ObservationType();
		ObservationType.EventId eventId = new ObservationType.EventId();
		eventId.setSource(getColumnValue(col, "sourcesystem_cd"));
		eventId.setValue(getColumnValue(col, "encounter_ide"));
		observation.setEventId(eventId);
		PatientIdType patientId = new PatientIdType();
		patientId.setSource("EMPI");
		patientId.setValue(getColumnValue(col, "patient_ide"));
		observation.setPatientId(patientId);
		ObservationType.ConceptCd conceptCd = new ObservationType.ConceptCd();
		conceptCd.setValue(getColumnValue(col, "concept_cd").toLowerCase());
		observation.setConceptCd(conceptCd);
		ObservationType.ObserverCd observerCd = new ObservationType.ObserverCd();
		observerCd.setValue(getProviderId(col));
		observation.setObserverCd(observerCd);
		Date date = PatientDataXmlBuilder.getDate(getColumnValue(col,
				"start_date"));
		observation.setStartDate(dtoFactory.getXMLGregorianCalendar(date
				.getTime()));
		date = PatientDataXmlBuilder.getDate(getEndDate(col));
		observation.setEndDate((date != null) ? dtoFactory
				.getXMLGregorianCalendar(date.getTime()) : null);
		return observation;

	}

	private void writeHeader(BufferedWriter observationFactWriter)
			throws IOException {
		observationFactWriter.write(PatientDataXmlBuilder.getDocumentHeader());
	}

	private void writeEndDocument(BufferedWriter observationFactWriter)
			throws IOException {
		observationFactWriter.write(PatientDataXmlBuilder.getEndDocument());
	}

}
