/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/

package edu.harvard.i2b2.crc.dao.redcap;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.crc.dao.redcap.xml.EnumValues;
import edu.harvard.i2b2.crc.dao.redcap.xml.Val;
import edu.harvard.i2b2.crc.dao.redcap.xml.ValueMetadata;
import edu.harvard.i2b2.crc.datavo.i2b2message.SecurityType;
import edu.harvard.i2b2.crc.datavo.ontology.DeleteChildType;
import edu.harvard.i2b2.crc.datavo.ontology.MetadataLoadType;
import edu.harvard.i2b2.crc.datavo.ontology.OntologyDataType;
import edu.harvard.i2b2.crc.datavo.ontology.XmlValueType;
import edu.harvard.i2b2.crc.datavo.pdo.BlobType;
import edu.harvard.i2b2.crc.datavo.pdo.ConceptType;
import edu.harvard.i2b2.crc.datavo.pdo.EidType;
import edu.harvard.i2b2.crc.datavo.pdo.ObservationType;
import edu.harvard.i2b2.crc.datavo.pdo.ObservationType.ConceptCd;
import edu.harvard.i2b2.crc.datavo.pdo.ObservationType.EventId;
import edu.harvard.i2b2.crc.datavo.pdo.ObservationType.InstanceNum;
import edu.harvard.i2b2.crc.datavo.pdo.ObservationType.ModifierCd;
import edu.harvard.i2b2.crc.datavo.pdo.ObservationType.NvalNum;
import edu.harvard.i2b2.crc.datavo.pdo.ObservationType.ObserverCd;
import edu.harvard.i2b2.crc.datavo.pdo.PatientIdType;
import edu.harvard.i2b2.crc.datavo.pdo.PidType;
import edu.harvard.i2b2.crc.datavo.pdo.PidType.PatientId;
import edu.harvard.i2b2.crc.datavo.pm.ParamType;
import edu.harvard.i2b2.crc.datavo.pm.ProjectType;
import edu.harvard.i2b2.crc.delegate.ontology.CallOntologyUtil;
import edu.harvard.i2b2.crc.delegate.pm.CallPMUtil;
import edu.harvard.i2b2.crc.loader.dao.ILoaderDAOFactory;
import edu.harvard.i2b2.crc.loader.dao.IUploaderDAOFactory;
import edu.harvard.i2b2.crc.loader.dao.LoaderDAOFactoryHelper;
import edu.harvard.i2b2.crc.loader.dao.ObservationFactInsertHandle;
import edu.harvard.i2b2.crc.loader.dao.TempConceptInsertHandler;
import edu.harvard.i2b2.crc.loader.dao.TempEidInsertHandler;
import edu.harvard.i2b2.crc.loader.dao.TempPidInsertHandler;
import edu.harvard.i2b2.crc.loader.datavo.loader.DataSourceLookup;
import edu.harvard.i2b2.crc.util.Base64;
import edu.harvard.i2b2.crc.util.PMServiceAccountUtil;
import edu.harvard.i2b2.crc.util.QueryProcessorUtil;

import org.apache.axis2.AxisFault;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Stream;

import javax.xml.bind.JAXBContext;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLStreamException;

import java.math.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths; 


public class RedcapPuller  {
	protected final Log logger = LogFactory.getLog(getClass());

	private final String FORMAT = "json";
	private final String CONTENT = "record";


	String rootOntology = "\\REDCAP\\";
	String setSourcesystemCd = "REDCAP";
	String protectedRole = "DATA_PROT";
	String refreshOntology = "Y";
	boolean isPHI = false;
	//String ontologyFormat = "tree"; //tree = For Tree format, popup = For popup with selection
	String api = null;
	String redcapApiUrl = null;
	String startDateName = null;

	public void pullRecordRequest(String redcapUrl, String record, String recordId, String surveyForm, String pid) throws I2B2Exception {
		if (record == null || record.isEmpty()) {
			throw new I2B2Exception("record is null");
		}
		if (redcapUrl == null || redcapUrl.isEmpty()) {
			throw new I2B2Exception("url is null");
		}

		APISurveyResponse redcapResult = new APISurveyResponse();

		try {
			redcapApiUrl = redcapUrl + "api/";

			String responseStr =  null;
			CloseableHttpResponse response = null;

			SecurityType securityType = PMServiceAccountUtil
					.getServiceSecurityType("");

			ProjectType projectParams = CallPMUtil.callUserProjectRedcap(securityType,pid);// surveyForm);

			DataCollectionInstrumentMetadata[] metadatas = null;
			try {


				for (ParamType projectParam: projectParams.getParam())
				{
					if (projectParam.getName().equalsIgnoreCase("REDCAP_TOKEN_PID_" + pid))  // Located in the PM_USER_PARAM
						api = projectParam.getValue();
					else if  (projectParam.getName().equalsIgnoreCase("REDCAP_ROOT_ONTOLOGY"))
						rootOntology =  projectParam.getValue();
					else if  (projectParam.getName().equalsIgnoreCase("REDCAP_SOURCESYSTEM"))
						setSourcesystemCd =  projectParam.getValue();	
					//		else if  (projectParam.getName().equalsIgnoreCase("REDCAP_ONTOLOGY_FORMAT"))
					//			ontologyFormat =  projectParam.getValue();	
					else if  (projectParam.getName().equalsIgnoreCase("REDCAP_PROTECTED_ROLE"))
						protectedRole =  projectParam.getValue();	
					else if  (projectParam.getName().equalsIgnoreCase("REDCAP_ONTOLOGY_REFRESH"))
						refreshOntology =  projectParam.getValue();
					else if  (projectParam.getName().equalsIgnoreCase("REDCAP_STARTDATE_NAME"))
						startDateName =  projectParam.getValue();

				}

				if (api == null)
					throw new I2B2Exception("Project " + surveyForm + " does not have a param called REDCAP_TOKEN_PID_" + pid + " with the token number.");

				if (redcapApiUrl.startsWith("file:")) {
					responseStr = readLineByLineJava8( redcapApiUrl.substring(7) ) ;
					metadatas = new Gson().fromJson(responseStr, DataCollectionInstrumentMetadata[].class);
					if (metadatas != null) {
						redcapResult.getMetadata().addAll(Arrays.asList(metadatas));
					}
				} else {
					List <NameValuePair> apiCall = new ArrayList <NameValuePair>();
					apiCall.add(new BasicNameValuePair("token",  api));
					apiCall.add(new BasicNameValuePair("content", "metadata"));
					apiCall.add(new BasicNameValuePair("format", FORMAT));
					HttpPost httpPost = new HttpPost(redcapApiUrl);

					httpPost.setEntity(new UrlEncodedFormEntity(apiCall, HTTP.UTF_8));
					httpPost.addHeader("Content-Type", "application/x-www-form-urlencoded");

					CloseableHttpClient client = HttpClientBuilder.create().build();
					response = client.execute(httpPost);
					if (response.getStatusLine().getStatusCode() >= 300) {
						throw new  I2B2Exception(String.format("failure - received a %d for %s.", 
								response.getStatusLine().getStatusCode(), httpPost.getURI().toString()));
					}

					HttpEntity entity = response.getEntity();
					responseStr = EntityUtils.toString(entity, "UTF-8");

					if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
						metadatas = new Gson().fromJson(responseStr, DataCollectionInstrumentMetadata[].class);
						if (metadatas != null) {
							redcapResult.getMetadata().addAll(Arrays.asList(metadatas));
						}
					}
				}

			} catch (Exception e) {
				throw new Exception(e);
			}

			try {	

				List <NameValuePair>  apiCall = new ArrayList <NameValuePair>();
				apiCall.add(new BasicNameValuePair("token",  api));
				apiCall.add(new BasicNameValuePair("content", CONTENT));
				apiCall.add(new BasicNameValuePair("format", FORMAT));
				apiCall.add(new BasicNameValuePair("type", record));
				apiCall.add(new BasicNameValuePair("exportSurveyFields", "true"));
				apiCall.add(new BasicNameValuePair("rawOrLabel", "raw"));
				apiCall.add(new BasicNameValuePair("exportCheckboxLabel", "true"));
				if (surveyForm != null && !surveyForm.isEmpty()) {
					apiCall.add(new BasicNameValuePair("forms", surveyForm));
				}
				if (recordId != null && !recordId.isEmpty()) {
					apiCall.add(new BasicNameValuePair("records", recordId));
				}
				//if (eventName != null && !eventName.isEmpty()) {
				//		apiCall.add(new BasicNameValuePair("events", eventName));
				//		}

				HttpPost httpPost = new HttpPost(redcapApiUrl);

				httpPost.setEntity(new UrlEncodedFormEntity(apiCall, HTTP.UTF_8));
				httpPost.addHeader("Content-Type", "application/x-www-form-urlencoded");

				CloseableHttpClient client = HttpClientBuilder.create().build();
				response = client.execute(httpPost);
				if (response.getStatusLine().getStatusCode() >= 300) {
					throw new  I2B2Exception(String.format("failure - received a %d for %s.", 
							response.getStatusLine().getStatusCode(), httpPost.getURI().toString()));
				}


				HttpEntity entity = response.getEntity();
				responseStr = EntityUtils.toString(entity, "UTF-8");



			} catch (Exception e) {
				throw new Exception(e);
			}
			// Create Table_access

			logger.debug("Response body: " + responseStr);

			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				SurveyRecord[] records = new Gson().fromJson(responseStr, SurveyRecord[].class);
				insertI2B2(records, redcapResult, surveyForm, projectParams, securityType, metadatas);

			} 

		} catch (Exception ie) {
			logger.error("Unexpected IO exception", ie);

		}
	}

	private static String readLineByLineJava8(String filePath)
	{
		StringBuilder contentBuilder = new StringBuilder();

		try (Stream<String> stream = Files.lines( Paths.get(filePath), StandardCharsets.UTF_8))
		{
			stream.forEach(s -> contentBuilder.append(s).append("\n"));
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		return contentBuilder.toString();
	}

	private void insertI2B2(SurveyRecord[] records, APISurveyResponse redcapResult, String surveyForm, ProjectType projectParams
			, SecurityType securityType, DataCollectionInstrumentMetadata[] metadatas ) throws I2B2Exception, DatatypeConfigurationException, AxisFault, XMLStreamException, JAXBUtilException {
		if (records != null) {
			QueryProcessorUtil qpUtil = QueryProcessorUtil.getInstance();
			String ontologyUrl = qpUtil
					.getCRCPropertyValue(QueryProcessorUtil.ONTOLOGYCELL_ROOT_WS_URL_PROPERTIES);


			List<OntologyDataType> c = new ArrayList();


			redcapResult.getRecords().addAll(Arrays.asList(records));

			redcapResult.process();

			DataSourceLookup dataSourceLookup = new DataSourceLookup();
			dataSourceLookup.setProjectPath(projectParams.getId());
			dataSourceLookup.setDomainId(securityType.getDomain());
			dataSourceLookup.setOwnerId(securityType.getUsername());

			//DataMartLoaderAsyncBeanLocal loader = CRCLoaderUtil.getInstance().getDataMartLoaderBean();


			LoaderDAOFactoryHelper daoHelper = new LoaderDAOFactoryHelper(
					dataSourceLookup.getDomainId(), dataSourceLookup
					.getProjectPath(), dataSourceLookup.getOwnerId());
			ILoaderDAOFactory loaderDaoFactory = daoHelper.getDAOFactory();
			IUploaderDAOFactory uploaderDaoFactory = loaderDaoFactory
					.getUpLoaderDAOFactory();


			GregorianCalendar gcal = (GregorianCalendar) GregorianCalendar.getInstance();
			XMLGregorianCalendar startDate = DatatypeFactory.newInstance()
					.newXMLGregorianCalendar(gcal);






			ObserverCd observer = new ObserverCd();
			observer.setName("@");
			observer.setValue("@");

			InstanceNum instance = new InstanceNum();
			instance.setValue("1");

			int uploadId = -1;
			String tempObsTableName = "TEMP_OBS_MAP_" + gcal.getTime().getDay()+gcal.getTime().getMonth()+gcal.getTime().getYear()
					+gcal.getTime().getHours()+gcal.getTime().getMinutes();
			String tempPidTableName = "TEMP_PID_MAP_" + gcal.getTime().getDay()+gcal.getTime().getMonth()+gcal.getTime().getYear()
					+gcal.getTime().getHours()+gcal.getTime().getMinutes();

			String tempEidTableName = "TEMP_EID_MAP_" + gcal.getTime().getDay()+gcal.getTime().getMonth()+gcal.getTime().getYear()
					+gcal.getTime().getHours()+gcal.getTime().getMinutes();

			String tempConceptTableName = "TEMP_CONCEPT_MAP_" + gcal.getTime().getDay()+gcal.getTime().getMonth()+gcal.getTime().getYear()
					+gcal.getTime().getHours()+gcal.getTime().getMinutes();

			uploaderDaoFactory.getPidDAO().createTempTable(tempPidTableName);
			uploaderDaoFactory.getEidDAO().createTempTable(tempEidTableName);
			uploaderDaoFactory.getObservationDAO().createTempTable(tempObsTableName);
			uploaderDaoFactory.getConceptDAO().createTempTable(tempConceptTableName);

			ObservationFactInsertHandle observationFactInsertHandle = uploaderDaoFactory.getObservationDAO()
					.createObservationFactInserter(tempObsTableName);

			TempPidInsertHandler pidInsertHandle = uploaderDaoFactory.getPidDAO().createTempPidInsert(tempPidTableName);

			TempEidInsertHandler eidInsertHandle = uploaderDaoFactory.getEidDAO().createTempEidInsert(tempEidTableName);

			TempConceptInsertHandler conceptInsertHandle = uploaderDaoFactory.getConceptDAO().createTempConceptInsert(tempConceptTableName);
			//observationFactInsertHandle.c

			if (records.length > 0) {
				PidType pidType = new PidType();
				PatientId patientId = new PatientId();
				patientId.setSource("REDCAP");
				patientId.setValue(records[0].getRecordId());
				patientId.setStatus("A");
				patientId.setSourcesystemCd(setSourcesystemCd);
				patientId.setDownloadDate(startDate);
				patientId.setUpdateDate(startDate);
				patientId.setImportDate(startDate);
				pidType.setPatientId(patientId);

				pidInsertHandle.insertPid(pidType);


				EidType eidType = new EidType();
				edu.harvard.i2b2.crc.datavo.pdo.EidType.EventId eventId = new edu.harvard.i2b2.crc.datavo.pdo.EidType.EventId();

				eventId.setPatientId(records[0].getRecordId());
				eventId.setPatientIdSource("REDCAP");
				eventId.setSourcesystemCd(setSourcesystemCd);
				eventId.setStatus("A");
				eventId.setSource("REDCAP");
				eventId.setValue("0");
				eidType.setEventId(eventId);
				eidInsertHandle.insertEid(eidType);
				uploaderDaoFactory.getPidDAO().createPidFromTempTable(tempPidTableName, uploadId);
				uploaderDaoFactory.getEidDAO().createEidFromTempTable(tempEidTableName, uploadId);
				uploaderDaoFactory.getObservationDAO().removeTempTable(tempPidTableName);
				uploaderDaoFactory.getObservationDAO().removeTempTable(tempEidTableName);
			}

			//Create root node in metadata in table_access
			OntologyDataType ontologyData = new OntologyDataType();
			ontologyData.setLevel(0);
			ontologyData.setFullname(rootOntology);
			ontologyData.setName(surveyForm);
			ontologyData.setSynonymCd("N");
			ontologyData.setVisualattributes("FAE");
			ontologyData.setFacttablecolumn("concept_cd");
			ontologyData.setDimtablename("concept_dimension");
			ontologyData.setColumnname("concept_path");
			ontologyData.setColumndatatype("T");
			ontologyData.setOperator("LIKE");
			ontologyData.setTooltip(rootOntology);
			ontologyData.setDimcode(rootOntology );							
			ontologyData.setAppliedPath("@");
			ontologyData.setDownloadDate(startDate);
			ontologyData.setUpdateDate(startDate);
			ontologyData.setImportDate(startDate);
			c.add(ontologyData);

			for (DataCollectionInstrumentMetadata metadata: metadatas) {
				if (surveyForm.equalsIgnoreCase(metadata.getFormName())) {
					//Check to see if PHI, if so set gloval variable
					if (metadata.isPHI())
						isPHI = true;


					// Insert Concept Dimension
					//	if ((metadata.isHasEnumOptions() || metadata.isTextOptions())) {
					ConceptType conceptType =  new ConceptType();
					conceptType.setConceptCd("REDCAP:" + metadata.getFieldName());
					conceptType.setNameChar("REDCAP:" + metadata.getFieldLabel());
					conceptType.setConceptPath(rootOntology + metadata.getFieldName()+ "\\");

					conceptType.setSourcesystemCd(setSourcesystemCd);
					conceptType.setUploadId(String.valueOf(uploadId));
					conceptInsertHandle.insertConcept(conceptType);

					// Insert meta data
					ontologyData = new OntologyDataType();
					ontologyData.setLevel(1);
					ontologyData.setFullname(rootOntology + metadata.getFieldName()+ "\\");
					ontologyData.setName(metadata.getFieldLabel());
					ontologyData.setSynonymCd("N");
					//if ((metadata.isTextOptions() == true) || (metadata.isHasMultpleOptions() ==  true))


					//if (ontologyFormat.equalsIgnoreCase("popup") 
					//|| metadata.isHasEnumOptions() == true))
					// (metadata.isHasCheckBoxOptions() == true) 
					//if ((ontologyFormat.equalsIgnoreCase("tree") && ((metadata.isHasEnumOptions() == true)  || (metadata.isHasCheckBoxOptions() == true))))
					if ((metadata.isHasEnumOptions() == true)  || (metadata.isHasCheckBoxOptions() == true) )
						ontologyData.setVisualattributes("FAE");
					//					else if ((ontologyFormat.equalsIgnoreCase("popup") && (metadata.isHasCheckBoxOptions() == true) ))
					//						ontologyData.setVisualattributes("FAE");
					else
						ontologyData.setVisualattributes("LAE");

					ontologyData.setBasecode(metadata.getFieldName());
					ontologyData.setFacttablecolumn("concept_cd");
					ontologyData.setDimtablename("concept_dimension");
					ontologyData.setColumnname("concept_path");
					ontologyData.setColumndatatype("T");
					ontologyData.setOperator("LIKE");
					ontologyData.setDimcode(rootOntology + metadata.getFieldName()+ "\\");							
					ontologyData.setTooltip(metadata.getFieldName());
					ontologyData.setAppliedPath("@");
					ontologyData.setDownloadDate(startDate);
					ontologyData.setUpdateDate(startDate);
					ontologyData.setImportDate(startDate);

					if ((metadata.isSliderOptions()) || (metadata.isNoteOptions()) || (metadata.isTextOptions() == true) )

					{
						ValueMetadata metadataxml = new ValueMetadata();

						metadataxml.setVersion("3.2");
						metadataxml.setCreationDateTime(startDate.toString());
						metadataxml.setTestID(metadata.getFieldName());
						metadataxml.setTestName(metadata.getFieldLabel());
						if (metadata.isTextOptions()) {
							metadataxml.setDataType("String");

						}else if (metadata.isNoteOptions()) {
							metadataxml.setDataType("largestring");
						}else if (metadata.isSliderOptions()) {
							metadataxml.setDataType("PosInteger");
							metadataxml.setLowofLowValue("1");
							metadataxml.setLowofHighValue("100");
							metadataxml.setHighofHighValue("100");
							metadataxml.setHighofLowValue("1");
						}else {
							metadataxml.setDataType("Enum");
							EnumValues enumValues = new EnumValues();


							Val[] vals = new Val[metadata.getChoices().length];

							//for (String enums: metadata.getChoices()) 
							for (int i=0; i <  metadata.getChoices().length; i++)
							{
								Val val = new Val();
								val.setDescription(metadata.getChoice(i)[1].trim());
								val.setValue(Integer.toString(i+1));
								vals[i] = val;

							}

							enumValues.setVal(vals);
							metadataxml.setEnumValues(enumValues);

						}
						Element rootElement = null;
						String xmlString = "";
						XmlValueType xml = null;
						try {
							DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
							//dbf.setNamespaceAware(true);
							Document doc = dbf.newDocumentBuilder().newDocument(); 

							JAXBContext context = JAXBContext.newInstance(metadataxml.getClass());
							context.createMarshaller().marshal(metadataxml, doc);
							//     Element element = metadataxml;
							rootElement = doc.getDocumentElement();
						} catch (Exception e1) {
							e1.printStackTrace();
						}
						if (rootElement != null) {
							xml = new XmlValueType();
							xml.getAny().add(rootElement);

							//if (ontologyFormat.equalsIgnoreCase("popup") || metadata.isHasEnumOptions() == true)
							ontologyData.setMetadataxml(xml);
						}
					}

					//check what way to save metadata
					for (int i=0; i < metadata.getChoices().length; i++)
					{
						//if ((ontologyFormat.equalsIgnoreCase("tree")) )//|| metadata.isHasCheckBoxOptions() == true)
						//{
						ConceptType leafType =  new ConceptType();
						//String endnode = Integer.toString(i);

						leafType.setConceptCd("REDCAP:" + metadata.getFieldName() + ":" + metadata.getChoice(i)[0].trim());
						try {
							leafType.setNameChar("REDCAP:" + metadata.getChoice(i)[1].trim());
						} catch (Exception meta)
						{
							logger.error("Metadata Issue: " + metadata.getFieldName());

						}
						leafType.setConceptPath(rootOntology + metadata.getFieldName() + "\\" + i+ "\\");

						leafType.setSourcesystemCd(setSourcesystemCd);
						leafType.setUploadId(String.valueOf(uploadId));
						conceptInsertHandle.insertConcept(leafType);

						// Insert meta data
						OntologyDataType leafData = new OntologyDataType();
						//ontologyData.setColumnname(surveyForm);
						//ontologyData.setTableCd(surveyForm);
						//ontologyData.setTableName(surveyForm);



						leafData.setLevel(2);
						leafData.setFullname(rootOntology + metadata.getFieldName()+ "\\" + i+ "\\");
						leafData.setName(metadata.getChoice(i)[1].trim());
						leafData.setSynonymCd("N");
						leafData.setVisualattributes("LAE");
						leafData.setBasecode(leafType.getConceptCd());
						leafData.setFacttablecolumn("concept_cd");
						leafData.setDimtablename("concept_dimension");
						leafData.setColumnname("concept_path");
						leafData.setColumndatatype("T");
						leafData.setOperator("LIKE");
						leafData.setDimcode(rootOntology + metadata.getFieldName()+ "\\" + i+ "\\");							
						leafData.setTooltip(metadata.getFieldName());
						leafData.setAppliedPath("@");
						leafData.setDownloadDate(startDate);
						leafData.setUpdateDate(startDate);
						leafData.setImportDate(startDate);

						c.add(leafData);

						///}
					}


					c.add(ontologyData);

				}
			}


			// Save Records to Observation Fact Table
			for (SurveyRecord record: records) {

				// get Metadata for record
				DataCollectionInstrumentMetadata metadata = redcapResult.getMetadata(record.getFieldName());

				if (metadata != null) {
					String blob = "";
					ObservationType observationType = new ObservationType();
					PatientIdType patid = new PatientIdType();
					patid.setSource("REDCAP");
					patid.setValue(record.getRecordId());
					observationType.setPatientId(patid);
					ConceptCd conceptCd = new ConceptCd();
					if  ( (metadata.getFieldType().equals("yesno") || metadata.getFieldType().equals("truefalse")  || metadata.getFieldType().equals("radio")
							|| metadata.getFieldType().equals("checkbox") || metadata.getFieldType().equals("dropdown")))
					{
						conceptCd.setName("REDCAP:" + record.getFieldName() + ":" + record.getValue());
						conceptCd.setValue("REDCAP:" + record.getFieldName() + ":" + record.getValue());
					} else {
						conceptCd.setName("REDCAP:" + record.getFieldName());
						conceptCd.setValue("REDCAP:" + record.getFieldName());
					}
					observationType.setConceptCd(conceptCd);

					observationType.setUploadId(String.valueOf(uploadId));

					String modifierCd = "@";
					//if (metadata.getFieldType().equals("checkbox")) {

					//	modifierCd = record.getValue();
					//}
					ModifierCd modifer = new ModifierCd();
					modifer.setName(modifierCd);
					modifer.setValue(modifierCd);
					observationType.setModifierCd(modifer);

					observationType.setInstanceNum(instance);

					observationType.setObserverCd(observer);
					observationType.setSourcesystemCd(setSourcesystemCd);
					EventId eventId = new EventId();
					eventId.setSource("REDCAP");
					eventId.setValue("0");//record.getRecordId());
					observationType.setEventId(eventId);

					if (modifierCd.equals("@"))
						if ( (metadata.getFieldType().equals("notes") || metadata.getFieldType().equals("descriptive")))
						{
							observationType.setValuetypeCd("B");
							blob += record.getValue();
						} else if  (metadata.getFieldType().equals("slider") ) {
							observationType.setValuetypeCd("N");
							NvalNum value = new NvalNum();
							value.setValue(new BigDecimal(record.getValue()));
							observationType.setNvalNum(value); //(record.getValue());
							observationType.setTvalChar("E");
						} else if  (metadata.getFieldType().equals("file") ) {

							observationType.setValuetypeCd("B");
							blob += getFile( metadata.getFieldName(), record.record);
						}
						else if  (metadata.getFieldType().equals("text") || metadata.getFieldType().equals("sql"))
						{
							observationType.setValuetypeCd("T");
							observationType.setTvalChar(record.getValue());
						}


					if (metadata.getFieldType().equals("date")) {
						try {

							String FORMATER = "yyyy-MM-dd";

							if (metadata.getFieldContent().equals("date_ymd"))
								FORMATER = "yyyy-MM-dd";
							else if (metadata.getFieldContent().equals("date_mdy"))
								FORMATER = "yyyy-MM-dd";
							else if (metadata.getFieldContent().equals("date_dmy"))
								FORMATER = "yyyy-MM-dd";
							else if (metadata.getFieldContent().equals("datetime_ymd"))
								FORMATER = "yyyy-MM-dd kk:mm";
							else if (metadata.getFieldContent().equals("datetime_mdy"))
								FORMATER = "yyyy-MM-dd kk:mm";
							else if (metadata.getFieldContent().equals("datetime_dmy"))
								FORMATER = "yyyy-MM-dd kk:mm";
							else if (metadata.getFieldContent().equals("datetime_seconds_ymd"))
								FORMATER = "yyyy-MM-dd kk:mm:ss";
							else if (metadata.getFieldContent().equals("datetime_seconds_mdy"))
								FORMATER = "yyyy-MM-dd kk:mm:ss";
							else if (metadata.getFieldContent().equals("datetime_seconds_dmy"))
								FORMATER = "yyyy-MM-dd kk:mm:ss";							

							DateFormat format = new SimpleDateFormat(FORMATER);
							Date date = format.parse(record.getValue());

							GregorianCalendar gregorianCalendar = 
									(GregorianCalendar)GregorianCalendar.getInstance();
							gregorianCalendar.setTime(date);
							XMLGregorianCalendar gDateFormatted =
									DatatypeFactory.newInstance().newXMLGregorianCalendar(gregorianCalendar);

							observationType.setStartDate(gDateFormatted);		
						} catch (Exception e3)								
						{
							// Error savind or converting date, switching to Text
							blob += "\nError converting Date: " + e3.getMessage();
							observationType.setValuetypeCd("T");
							e3.printStackTrace();
							observationType.setStartDate(startDate);
						}
					} else {
						observationType.setStartDate(startDate);
					}

					if (blob.length() > 0) {
						BlobType blobType = new BlobType();
						blobType.getContent().add(blob);
						observationType.setObservationBlob(blobType);
					}
					observationFactInsertHandle
					.insertObservationFact(observationType);

				}

			}
			uploaderDaoFactory.getConceptDAO().createConceptFromTempTable(tempConceptTableName, uploadId);

			uploaderDaoFactory.getObservationDAO().doTempTableMerge(tempObsTableName, uploadId, false);

			// Add to tabel access
			List<OntologyDataType> cTableAccess = new ArrayList();
			OntologyDataType e = new OntologyDataType();
			e.setColumnname(surveyForm);
			e.setTableCd(surveyForm);
			e.setTableName(surveyForm);
			e.setLevel(0);
			if (isPHI)
			{
				e.setProtectedAccess("Y");
				e.setOntologyProtection(protectedRole);

			}
			else
			{
				e.setProtectedAccess("N");
			}
			e.setFullname(rootOntology);
			e.setName(surveyForm);
			e.setSynonymCd("N");
			e.setVisualattributes("CA");
			e.setFacttablecolumn("concept_cd");
			e.setDimtablename("concept_dimension");
			e.setColumnname("concept_path");
			e.setColumndatatype("T");
			e.setOperator("LIKE");
			e.setDimcode(rootOntology);
			//e.setTooltip("FIX ME");
			e.setDimcode(rootOntology);
			cTableAccess.add(e);


			if (refreshOntology.equalsIgnoreCase("Y")) {

				// Delete old Ontolgy
				DeleteChildType deleteChild = new DeleteChildType();
				deleteChild.setName(rootOntology);
				deleteChild.setKey("\\\\" + surveyForm + rootOntology );
				deleteChild.setIncludeChildren(true);
				CallOntologyUtil.callDeleteChild(deleteChild,
						securityType, projectParams.getPath(),
						ontologyUrl +"/deleteChild");

				MetadataLoadType factColumns = CallOntologyUtil.callLoadData("table_access", cTableAccess,
						securityType, projectParams.getPath(),
						ontologyUrl +"/loadMetadata");

				// Add children to ontology

				factColumns = CallOntologyUtil.callLoadData(surveyForm, c,
						securityType, projectParams.getPath(),
						ontologyUrl +"/loadMetadata");
			}


			uploaderDaoFactory.getObservationDAO().removeTempTable(tempConceptTableName);
			uploaderDaoFactory.getObservationDAO().removeTempTable(tempObsTableName);
		}		
	}


	private String getFile( String fieldType, String  recordId) {
		CloseableHttpResponse response = null;
		String responseStr = null;
		Base64.Encoder encoder = Base64.getEncoder();

		try {	

			List <NameValuePair>  apiCall = new ArrayList <NameValuePair>();
			apiCall.add(new BasicNameValuePair("token",  api));
			apiCall.add(new BasicNameValuePair("content", "file"));
			apiCall.add(new BasicNameValuePair("action", "export"));
			apiCall.add(new BasicNameValuePair("record", recordId));
			apiCall.add(new BasicNameValuePair("field", fieldType));
			apiCall.add(new BasicNameValuePair("event", ""));
			apiCall.add(new BasicNameValuePair("returnFormat", "json"));

			HttpPost httpPost = new HttpPost(redcapApiUrl);

			httpPost.setEntity(new UrlEncodedFormEntity(apiCall, HTTP.UTF_8));
			httpPost.addHeader("Content-Type", "application/x-www-form-urlencoded");

			CloseableHttpClient client = HttpClientBuilder.create().build();
			response = client.execute(httpPost);
			if (response.getStatusLine().getStatusCode() >= 300) {
				throw new  I2B2Exception(String.format("failure - received a %d for %s.", 
						response.getStatusLine().getStatusCode(), httpPost.getURI().toString()));
			}


			HttpEntity entity = response.getEntity();
			InputStream is = entity.getContent();
			byte[] isByte = IOUtils.toByteArray(is);
			responseStr = encoder.encodeToString(isByte);
			//			responseStr = EntityUtils.toString(entity, "UTF-8");



		} catch (Exception e) {
			//throw new Exception(e);
		}
		// Create Table_access


		return responseStr;
	}




}
