package edu.harvard.i2b2.im.util;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.axis2.ServiceClient;
import edu.harvard.i2b2.im.datavo.pdo.ParamType;
import edu.harvard.i2b2.im.datavo.pdo.PatientType;
import edu.harvard.i2b2.im.datavo.pdo.PidType;


public  class EMPIOpenEMPI  implements EMPI {
	private static Log log = LogFactory.getLog(EMPIOpenEMPI.class.getName());

	String authenticate = null;
	String person = null;
	public String findPerson(String username,
			String source, String value) throws Exception {

		if (authenticate == null)
			Authenticate();


		person = getPersonById(source, value);

		return person;
	}

	private String getPersonById(String source, String value) throws Exception {
		// TODO Auto-generated method stub
		try {
			String getRequestString = "";


			// First step is to get PM endpoint reference from properties file.
			String imEPR = "";
			try {
				imEPR = IMUtil.getInstance().getOpenEMPIWebService() + "/person-query-resource/findPersonById";
				getRequestString = 
						"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
								"<personIdentifier>" +
								"<identifier>"+ value + "</identifier>" +
								"<identifierDomain>" +
								"<namespaceIdentifier>" + source + "</namespaceIdentifier>" +
								"<universalIdentifier>" + source + "</universalIdentifier>" +
								"<universalIdentifierTypeCode>" + source + "</universalIdentifierTypeCode>" +
								"</identifierDomain>" +
								"</personIdentifier>";

			} catch (I2B2Exception e1) {
				log.error(e1.getMessage());
				throw e1;
			}

			URL url = new URL(imEPR);
			HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
			httpCon.setDoOutput(true);
			httpCon.setRequestMethod("POST");
			httpCon.setRequestProperty(
					"Content-Type", "application/xml" );
			httpCon.addRequestProperty("OPENEMPI_SESSION_KEY", authenticate);
			OutputStreamWriter out = new OutputStreamWriter(
					httpCon.getOutputStream());
			out.write(getRequestString);
			out.close();
			return IOUtils.toString(httpCon.getInputStream());
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		} 

	}

	public void parse(PatientType ptype) throws SAXException, IOException, ParserConfigurationException {

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

		DocumentBuilder builder = factory.newDocumentBuilder();

		Document document =
				builder.parse((new InputSource(new StringReader(person))));
		List<ParamType> paramList = new ArrayList<ParamType>();

		NodeList nodeList = document.getDocumentElement().getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);

			if (node instanceof Element) {


				String content = node.getLastChild().
						getTextContent().trim();
				if (!node.getNodeName().equals("personIdentifiers")) {
					ParamType param = new ParamType();
					param.setName(node.getNodeName());
					param.setValue(content);
					param.setType("T");
					ptype.getParam().add(param);
				}
			}

		}
		//return paramList;

	}

	private void Authenticate() throws Exception {
		// TODO Auto-generated method stub
		try {
			String getRequestString = "";


			// First step is to get PM endpoint reference from properties file.
			String imEPR = "";
			try {
				imEPR = IMUtil.getInstance().getOpenEMPIWebService() + "/security-resource/authenticate";
				getRequestString = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><authenticationRequest><password>"+ IMUtil.getInstance().getOpenEMPIPassword() +"</password><username>" + IMUtil.getInstance().getOpenEMPIUsername() + "</username></authenticationRequest>";
			} catch (I2B2Exception e1) {
				log.error(e1.getMessage());
				throw e1;
			}

			URL url = new URL(imEPR);
			HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
			httpCon.setDoOutput(true);
			httpCon.setRequestMethod("PUT");
			httpCon.setRequestProperty(
					"Content-Type", "application/xml" );
			OutputStreamWriter out = new OutputStreamWriter(
					httpCon.getOutputStream());
			out.write(getRequestString);
			out.close();
			authenticate =IOUtils.toString(httpCon.getInputStream());
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		} 

	}

	public void getIds(PidType newPidType) throws SAXException, IOException, ParserConfigurationException {


		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

		DocumentBuilder builder = factory.newDocumentBuilder();

		Document document =
				builder.parse((new InputSource(new StringReader(person))));
		List<ParamType> paramList = new ArrayList<ParamType>();

		NodeList nodeList = document.getDocumentElement().getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);

			if (node instanceof Element) {


				String value = null;
				if (node.getNodeName().equals("personIdentifiers")) {
					for(Node childNode=node.getFirstChild(); childNode!=null; childNode=childNode.getNextSibling()){
						if (childNode.getNodeName().equals("identifier"))
						{
							value = childNode.getLastChild().getTextContent().trim();
						}
						if (childNode.getNodeName().equals("identifierDomain"))
						{
							for(Node childNode2=childNode.getFirstChild(); childNode2!=null; childNode2=childNode2.getNextSibling()){
								if (childNode2.getNodeName().equals("namespaceIdentifier"))
								{
									PidType.PatientMapId patientMapId = new PidType.PatientMapId();
									patientMapId.setSource(childNode2.getLastChild().getTextContent().trim());
									patientMapId.setValue(value);
									newPidType.getPatientMapId().add(patientMapId);
								}
							}
						}
					}

					//			pidType.
				}
			}

		}

	}



}
