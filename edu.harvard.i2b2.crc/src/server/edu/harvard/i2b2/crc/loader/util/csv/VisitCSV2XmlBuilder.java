package edu.harvard.i2b2.crc.loader.util.csv;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.Hashtable;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.jaxb.DTOFactory;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtil;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.crc.loader.datavo.CRCLoaderJAXBUtil;
import edu.harvard.i2b2.crc.datavo.pdo.EventSet;
import edu.harvard.i2b2.crc.datavo.pdo.EventType;
import edu.harvard.i2b2.crc.datavo.pdo.PatientIdType;

/**
 * Build's patient data xml with visit information, from input csv file.
 * 
 * CSV Header information: "Record_Id," "Report_Id," "EMPI," "MRN_Type," "MRN,"
 * "LMRNote_Date_Time," "Status," "Author," "COD," "Institution," "Author_MRN,"
 * "Subject," "Classification," "Timestamp," "Software_Version"
 * 
 * @author rk903
 * 
 */
public class VisitCSV2XmlBuilder {

	private String inputFileName = null;
	private String outputXmlFileName = null;

	private static Hashtable csvHeaderMap = null;
	private Hashtable csvHeaderColumnPosition = null;
	private DTOFactory dtoFactory = new DTOFactory();
	private JAXBUtil jaxbUtil = null;

	static {
		csvHeaderMap = new Hashtable();
		csvHeaderMap.put("encounter_ide", new String[] { "encounter_id" });
		csvHeaderMap.put("patient_ide", new String[] { "patient_id" });
		// csvHeaderMap.put("encounter_ide_source",new
		// String[]{"MRN_TYPE","MRN_Type"});
		csvHeaderMap.put("encounter_ide_source",
				new String[] { "sourcesystem_cd" });
		csvHeaderMap.put("start_date", new String[] { "start_date" });
		csvHeaderMap.put("sourcesystem_cd", new String[] { "sourcesystem_cd" });
		csvHeaderMap.put("update_date", new String[] { "Timestamp",
				"Extraction_Timestamp" });
		csvHeaderMap.put("end_date", new String[] { "Dis_Date_Time" });
	}

	/**
	 * Constructor
	 * 
	 * @param inputFileName
	 *            input csv file name
	 * @param outputXmlFileName
	 *            output xml file name
	 */
	public VisitCSV2XmlBuilder(String inputFileName, String outputXmlFileName) {
		this.inputFileName = inputFileName;
		this.outputXmlFileName = outputXmlFileName;

		jaxbUtil = CRCLoaderJAXBUtil.getJAXBUtil();
	}

	public EventType getVisitDimension(String col[]) {
		EventType event = new EventType();
		EventType.EventId eventId = new EventType.EventId();
		eventId.setValue(getColumnValue(col, "encounter_ide"));
		eventId.setSource(getColumnValue(col, "encounter_ide_source"));
		event.setEventId(eventId);
		PatientIdType patientId = new PatientIdType();
		patientId.setSource("EMPI");
		patientId.setValue(getColumnValue(col, "patient_ide"));
		event.setPatientId(patientId);

		// event.setPatientId(patientIdType);
		Date date = PatientDataXmlBuilder.getDate(getColumnValue(col,
				"start_date"));
		event.setStartDate(dtoFactory.getXMLGregorianCalendar(date.getTime()));
		date = PatientDataXmlBuilder.getDate(getEndDate(col));
		event.setEndDate((date != null) ? dtoFactory
				.getXMLGregorianCalendar(date.getTime()) : null);
		event.setSourcesystemCd(getColumnValue(col, "sourcesystem_cd"));
		// date =
		// PatientDataXmlBuilder.formatIntDate(getColumnValue(col,"update_date"
		// );
		event.setUpdateDate(null);
		return event;
	}

	private void writeHeader(BufferedWriter visitWriter) throws IOException {
		visitWriter.write(PatientDataXmlBuilder.getDocumentHeader());
	}

	private void writeEndDocument(BufferedWriter visitWriter)
			throws IOException {
		visitWriter.write(PatientDataXmlBuilder.getEndDocument());
	}

	/**
	 * Parse csv file and generate xml file.
	 * 
	 * @throws I2B2Exception
	 */
	@SuppressWarnings("unchecked")
	public void buildXml() throws I2B2Exception {

		BufferedReader inputReader = null;
		BufferedWriter visitWriter = null;

		try {
			// read file and return buffered reader
			visitWriter = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(outputXmlFileName)));
			writeHeader(visitWriter);
			visitWriter.write("<ns2:event_set>");
			CSVFileReader csvReader = new CSVFileReader(inputFileName, ',',
					'\"');
			// read header and map header column position with element name
			java.util.Vector<String> headerFields = csvReader.readFields();
			int headerFieldCount = headerFields.size();
			csvHeaderColumnPosition = PatientDataXmlBuilder
					.getCsvHeaderColumnPosition(csvHeaderMap, headerFields);

			java.util.Vector<String> fields = null;
			int i = 0;
			EventSet eventSet = new EventSet();
			while ((fields = csvReader.readFields()) != null) {
				// skip line which have less columns, compared to header column
				if (headerFieldCount > fields.size()) {
					continue;
				}
				i++;

				String col[] = (String[]) fields.toArray(new String[] {});
				eventSet.getEvent().add(getVisitDimension(col));
				if (i % 1000 == 0) {
					i = 0;
					StringWriter strWriter = new StringWriter();
					try {
						jaxbUtil.marshaller(new JAXBElement(new QName("",
								"eventset"), EventSet.class, eventSet),
								strWriter);
					} catch (JAXBUtilException e) {
						e.printStackTrace();
					}
					String xml = strWriter.toString();
					String observationStr = xml.substring(xml.indexOf('>', xml
							.indexOf("eventset")) + 1, xml
							.indexOf("</eventset"));
					visitWriter.write(observationStr);
					eventSet = new EventSet();

				}
			}
			if (i > 0) {
				StringWriter strWriter = new StringWriter();
				try {
					jaxbUtil.marshaller(new JAXBElement(new QName("",
							"eventset"), EventSet.class, eventSet), strWriter);
				} catch (JAXBUtilException e) {
					e.printStackTrace();
				}
				String xml = strWriter.toString();
				String observationStr = xml.substring(xml.indexOf('>', xml
						.indexOf("eventset")) + 1, xml.indexOf("</eventset"));
				visitWriter.write(observationStr);
				visitWriter.write("</ns2:event_set>");
			}

			writeEndDocument(visitWriter);
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new I2B2Exception("Error building Visit xml", ex);
		} finally {
			try {
				if (inputReader != null) {
					inputReader.close();
				}
				if (visitWriter != null) {
					visitWriter.close();
				}

			} catch (IOException closeEx) {
				closeEx.printStackTrace();
			}
		}

	}

	private String getColumnValue(String col[], String elementName) {
		String elementValue = PatientDataXmlBuilder.getColumnValue(
				csvHeaderColumnPosition, col, elementName);
		return elementValue;
	}

	private String getEndDate(String col[]) {
		String endDate = getColumnValue(col, "end_date");
		if (endDate == null) {
			endDate = "";
		}
		return endDate;
	}

}
