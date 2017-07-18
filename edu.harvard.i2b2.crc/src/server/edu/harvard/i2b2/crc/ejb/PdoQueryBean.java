/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors: 
 *     Rajesh Kuttan
 */
package edu.harvard.i2b2.crc.ejb;
 
import java.io.StringWriter;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//import javax.ejb.CreateException;
//import javax.ejb.EJBException;
//import javax.ejb.SessionBean;
//import javax.ejb.SessionContext;
import javax.xml.bind.JAXBElement;
import javax.xml.stream.XMLStreamException;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.exception.StackTraceUtil;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtil;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.crc.dao.DAOFactoryHelper;
import edu.harvard.i2b2.crc.dao.IDAOFactory;
import edu.harvard.i2b2.crc.dao.PatientDataDAOFactory;
import edu.harvard.i2b2.crc.dao.pdo.DimensionTable;
import edu.harvard.i2b2.crc.dao.pdo.IMetadataDao;
import edu.harvard.i2b2.crc.dao.pdo.IObservationFactDao;
import edu.harvard.i2b2.crc.dao.pdo.MetaDataTypeMapper;
import edu.harvard.i2b2.crc.dao.pdo.MetadataDao;
import edu.harvard.i2b2.crc.dao.pdo.MetadataDao.TableMetaData;
import edu.harvard.i2b2.crc.dao.pdo.PdoQueryHandler;
import edu.harvard.i2b2.crc.dao.pdo.input.IInputOptionListHandler;
import edu.harvard.i2b2.crc.dao.pdo.input.OutputOptionFactRelatedHelper;
import edu.harvard.i2b2.crc.dao.pdo.input.PDOFactory;
import edu.harvard.i2b2.crc.dao.pdo.input.PagingHandler;
import edu.harvard.i2b2.crc.dao.setfinder.IQueryPdoMasterDao;
import edu.harvard.i2b2.crc.dao.setfinder.querybuilder.ItemMetaDataHandler;
import edu.harvard.i2b2.crc.datavo.CRCJAXBUtil;
import edu.harvard.i2b2.crc.datavo.db.DataSourceLookup;
import edu.harvard.i2b2.crc.datavo.db.QtQueryMaster;
import edu.harvard.i2b2.crc.datavo.i2b2message.PasswordType;
import edu.harvard.i2b2.crc.datavo.i2b2message.RequestMessageType;
import edu.harvard.i2b2.crc.datavo.i2b2message.SecurityType;
import edu.harvard.i2b2.crc.datavo.ontology.ConceptType;
import edu.harvard.i2b2.crc.datavo.ontology.ModifierType;
import edu.harvard.i2b2.crc.datavo.ontology.XmlValueType;
import edu.harvard.i2b2.crc.datavo.pdo.ParamType;
import edu.harvard.i2b2.crc.datavo.pdo.PatientDataType;
import edu.harvard.i2b2.crc.datavo.pdo.query.FilterListType;
import edu.harvard.i2b2.crc.datavo.pdo.query.GetObservationFactByPrimaryKeyRequestType;
import edu.harvard.i2b2.crc.datavo.pdo.query.GetPDOFromInputListRequestType;
import edu.harvard.i2b2.crc.datavo.pdo.query.InputOptionListType;
import edu.harvard.i2b2.crc.datavo.pdo.query.ItemType;
import edu.harvard.i2b2.crc.datavo.pdo.query.MetadataxmlValueType;
import edu.harvard.i2b2.crc.datavo.pdo.query.OutputOptionNameType;
import edu.harvard.i2b2.crc.datavo.pdo.query.PageByPatientType;
import edu.harvard.i2b2.crc.datavo.pdo.query.PageRangeType;
import edu.harvard.i2b2.crc.datavo.pdo.query.PageType;
import edu.harvard.i2b2.crc.datavo.pdo.query.PanelType;
import edu.harvard.i2b2.crc.datavo.pdo.query.PatientDataResponseType;
import edu.harvard.i2b2.crc.datavo.pdo.query.RangeType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryDefinitionType;
import edu.harvard.i2b2.crc.delegate.ontology.CallOntologyUtil;
import edu.harvard.i2b2.crc.role.AuthrizationHelper;
import edu.harvard.i2b2.crc.util.I2B2RequestMessageHelper;
import edu.harvard.i2b2.crc.util.PMServiceAccountUtil;
import edu.harvard.i2b2.crc.util.ParamUtil;
import edu.harvard.i2b2.crc.util.QueryProcessorUtil;

/**
 * This is the PDO Query ejb class. It passes pdo query request's to the
 * appropriate dao classes to get response in pdo format.
 * 
 * @ejb.bean description="Patient data Query bean"
 *           display-name="Patient data Query"
 *           jndi-name="ejb.querytool.PdoQuery"
 *           local-jndi-name="ejb.querytool.PdoQueryLocal"
 *           name="querytool.PdoQuery" type="Stateless" view-type="both"
 *           transaction-type="Container"
 * 
 * @ejb.interface remote-class="edu.harvard.i2b2.crc.ejb.PdoQueryRemote"
 * 
 * @author rkuttan
 */
public class PdoQueryBean { //implements SessionBean {
	// RunQuery
	/** log **/
	protected final Log log = LogFactory.getLog(getClass());

	/**
	 * Function to get plain pdo from the given pdo request
	 * 
	 * @ejb.interface-method view-type="both"
	 * @ejb.transaction type="Required"
	 */
	public PatientDataResponseType getPlainPatientData(
			DataSourceLookup dataSourceLookup,
			GetPDOFromInputListRequestType getPDOFromInputListReqType,
			String requestXml) throws I2B2Exception {
		PatientDataType patientDataType = null;

		DAOFactoryHelper helper = new DAOFactoryHelper(dataSourceLookup
				.getDomainId(), dataSourceLookup.getProjectPath(),
				dataSourceLookup.getOwnerId());
		IDAOFactory daoFactory = helper.getDAOFactory();
	
		
		IQueryPdoMasterDao queryPdoMasterDao = daoFactory
				.getSetFinderDAOFactory().getQueryPdoMasterDAO();

		savePdoQueryMaster(queryPdoMasterDao, requestXml);
		
		String version = getVersion(queryPdoMasterDao, requestXml);
		// check if the user have the blob permission
		boolean blobFlag = checkForBlob(getPDOFromInputListReqType);
		if (blobFlag) {
			//
			String domainId = dataSourceLookup.getDomainId();
			String projectId = dataSourceLookup.getProjectPath();
			String userId = dataSourceLookup.getOwnerId();
			daoFactory = helper.getDAOFactory();
			AuthrizationHelper authHelper = new AuthrizationHelper(domainId,
					projectId, userId, daoFactory);
			authHelper.checkRoleForProtectionLabel("PDO_WITH_BLOB");
		}
		
		// get unit cd conversion project param from cache 
				ParamUtil paramUtil = new ParamUtil();
				DataSourceLookup origDataSourceLookup = daoFactory.getPatientDataDAOFactory().getOriginalDataSourceLookup();
				String unitCdConversionParam = paramUtil.getParam(origDataSourceLookup.getProjectPath(), origDataSourceLookup.getOwnerId(), 
						origDataSourceLookup.getDomainId(), ParamUtil.CRC_ENABLE_UNITCD_CONVERSION);
				
				Map projectParamMap = null;
				if (unitCdConversionParam != null) { 
					projectParamMap = new HashMap();
					projectParamMap.put(ParamUtil.CRC_ENABLE_UNITCD_CONVERSION, unitCdConversionParam.trim());
					
				}

		// call ontology cell to get the item's metadata to build the query sql
		FilterListType filterList = getPDOFromInputListReqType.getFilterList();
		Map<String,XmlValueType> modifierMetadataXmlMap = new HashMap<String,XmlValueType>(); 
		if (filterList != null) { 
			try {
				//CallOntologyUtil ontologyUtil = new CallOntologyUtil(requestXml);

				
				JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil()
						.unMashallFromString(requestXml);
				RequestMessageType request = (RequestMessageType) responseJaxb
						.getValue();
				String projectId = request.getMessageHeader().getProjectId();
				SecurityType tempSecurityType = request.getMessageHeader()
						.getSecurity();
				SecurityType securityType = PMServiceAccountUtil
						.getServiceSecurityType(tempSecurityType.getDomain());
				// if regular concepts
				for (PanelType panel : filterList.getPanel()) {
					for (ItemType item : panel.getItem()) {
						//ConceptType conceptType = ontologyUtil.callOntology(item.getItemKey());
						ConceptType conceptType = CallOntologyUtil.callOntology(item.getItemKey(), securityType, projectId,  QueryProcessorUtil.getInstance().getOntologyUrl());
						
						log.debug("fetching the metadata information from ontology ["
										+ item.getItemKey() + "]");
						if (conceptType != null) {
							item.setDimDimcode(conceptType.getDimcode());
							item.setDimColumnname(conceptType.getColumnname());
							item.setDimOperator(conceptType.getOperator());
							item.setDimTablename(conceptType.getTablename());
							item.setFacttablecolumn(conceptType
									.getFacttablecolumn());
							item.setDimColumndatatype(conceptType
									.getColumndatatype());
							
							if (conceptType.getMetadataxml() != null && conceptType.getMetadataxml().getAny().get(0) != null) {
								MetadataxmlValueType metadataXmlType = new MetadataxmlValueType(); 
								metadataXmlType.getContent().add(conceptType.getMetadataxml().getAny().get(0));
								item.setMetadataxml(metadataXmlType);
							}
							
							log.debug("metadata from ontology received for ["
									+ item.getItemKey() + "]"
									+ conceptType.getTablename());
							
							//check for modifier constrain and get modifier metadata
							ItemType.ConstrainByModifier modifierConstrain = item.getConstrainByModifier();
							if (modifierConstrain != null) { 
								ItemMetaDataHandler itemMetaDataHandler = new ItemMetaDataHandler(requestXml);
								String modifierKey = modifierConstrain.getModifierKey();
								String modifierAppliedPath = modifierConstrain.getAppliedPath();
								ModifierType modifierType = itemMetaDataHandler.getModifierDataFromOntologyCell(modifierKey, modifierAppliedPath,   helper.getDataSourceLookup().getServerType());
								copyModifierToItem(item,modifierType);
								
								//cache the modifier metadat in the map
								if (projectParamMap != null) { 
									String unitConversionFlag = (String)projectParamMap.get(ParamUtil.CRC_ENABLE_UNITCD_CONVERSION);
									if ( unitConversionFlag != null && unitConversionFlag.equals("ON")) {
										if (modifierType.getMetadataxml() != null) {
											log.debug("Adding modifier metadata xml to lookup map for modifier key" + modifierType.getKey() + modifierType.getAppliedPath() );
											modifierMetadataXmlMap.put(modifierType.getKey()+modifierType.getAppliedPath(), modifierType.getMetadataxml());
										}
									}
								}
							}
							
						} else {
							log
									.debug("Unable to get item's metadata from ontology cell ["
											+ item.getItemKey() + "]");
						}
					}
				}
			} catch (JAXBUtilException jaxEx) {
				jaxEx.printStackTrace();
				throw new I2B2Exception(
						"Error in getting item's metadata from the ontology : "
								+ jaxEx.getMessage(), jaxEx);
			} catch (AxisFault e) {
				e.printStackTrace();
				throw new I2B2Exception(
						"Error in getting item's metadata from the ontology : "
								+ e.getMessage(), e);
			} catch (XMLStreamException e) {
				e.printStackTrace();
				throw new I2B2Exception(
						"Error in getting item's metadata from the ontology : "
								+ e.getMessage(), e);
			}
		}
		
		PatientDataDAOFactory pdoDaoFactory = daoFactory
				.getPatientDataDAOFactory();

		PatientDataResponseType patientDataResponseType = new PatientDataResponseType();
		try {
			OutputOptionNameType ot = getPDOFromInputListReqType
					.getOutputOption().getNames();

			QueryProcessorUtil qpUtil = QueryProcessorUtil.getInstance();
			String pageMethod = qpUtil.getPagingMethod();
			log.debug("PageMethod value [" + pageMethod + "]");

			InputOptionListType origInputList = getPDOFromInputListReqType
					.getInputList();
			InputOptionListType inputList = getPDOFromInputListReqType
					.getInputList();
			IInputOptionListHandler inputListHandler = PDOFactory
					.buildInputListHandler(inputList, helper
							.getDataSourceLookup());

			int requestedMinIndex = inputListHandler.getMinIndex();
			int requestedMaxIndex = inputListHandler.getMaxIndex();
			// if the request missing the max index, then default the maxindex
			// to size of the input list
			if (requestedMaxIndex < 1) {
				requestedMaxIndex = inputListHandler.getInputSize();
				RangeType rangeType = PDOFactory.getRangeType(inputList);
				rangeType.setMax(requestedMaxIndex);
			}

			// before doing the paging, check if the output option list has
			// observaion
			OutputOptionFactRelatedHelper opFactRelatedHelper = new OutputOptionFactRelatedHelper(
					getPDOFromInputListReqType.getOutputOption());
			boolean pagingNeeded = opFactRelatedHelper.isFactRelated();
			if (pagingNeeded) {
				PagingHandler ph = new PagingHandler(helper,
						getPDOFromInputListReqType.getInputList(),
						getPDOFromInputListReqType.getFilterList(), pageMethod);
				ph.setProjectParamMap(projectParamMap);
				ph.setModifierMetadataXmlMap(modifierMetadataXmlMap);
				HashMap calculatedValueMap = ph.calculateMaxPageInputList();

				// do paging if required
				if ((Boolean) calculatedValueMap
						.get(PagingHandler.PAGING_REQUIRED_FLAG)) {

					long totalSize = (Long) calculatedValueMap
							.get(PagingHandler.TOTAL_OBSERVATION);
					int maxInputSize = (Integer) calculatedValueMap
							.get(PagingHandler.MAX_INPUT_LIST);
					long pageSize = ph.getPageSize();

					// build page type
					PageType pageType = buildPageType(requestedMinIndex,
							requestedMaxIndex, maxInputSize, totalSize,
							pageSize);
					patientDataResponseType.setPage(pageType);

					RangeType rangeType = PDOFactory.getRangeType(inputList);
					rangeType.setMax(requestedMinIndex + maxInputSize - 1);

					// inputList.getPatientList().setMax(
					// requestedMinIndex + maxInputSize);
				}
			}
			boolean tablePDO = false;
			if (getPDOFromInputListReqType.getOutputOption().getNames() != null) {
				if (getPDOFromInputListReqType.getOutputOption().getNames().name().equalsIgnoreCase(OutputOptionNameType.ASATTRIBUTES.name())) { 
					tablePDO = true;
				}
			}
			log.debug("PDO reading metadat for patient_dimension table");
			//get patient dimension metadata
			List<ParamType> patientMetaDataList = getPDOTemplate("patient_dimension",
					dataSourceLookup, tablePDO);
			//get visit dimension metadata
			log.debug("PDO reading metadat for visit_dimension table");
			List<ParamType> visitMetaDataList = getPDOTemplate("visit_dimension",
					dataSourceLookup, tablePDO);

			if ((ot != null)
					&& ot.name().equalsIgnoreCase(
							OutputOptionNameType.ASATTRIBUTES.name())) {
				PdoQueryHandler pdoQueryHandler = new PdoQueryHandler(
						pdoDaoFactory, PdoQueryHandler.TABLE_PDO_TYPE,
						origInputList,
						inputList, // /getPDOFromInputListReqType.getInputList(),
						getPDOFromInputListReqType.getFilterList(),
						getPDOFromInputListReqType.getOutputOption());
				pdoQueryHandler.setProjectParamMap(projectParamMap);
				pdoQueryHandler.setModifierMetadataXmlMap(modifierMetadataXmlMap);
				pdoQueryHandler.setRequestVersion(version);
				pdoQueryHandler.setDimensionMetaDataParamList(patientMetaDataList, visitMetaDataList);
				pdoQueryHandler.processPDORequest();
				patientDataType = pdoQueryHandler.getTablePdo();
			} else {
				PdoQueryHandler pdoQueryHandler = new PdoQueryHandler(
						pdoDaoFactory, PdoQueryHandler.PLAIN_PDO_TYPE,
						origInputList,
						inputList, // getPDOFromInputListReqType.getInputList(),
						getPDOFromInputListReqType.getFilterList(),
						getPDOFromInputListReqType.getOutputOption());
				pdoQueryHandler.setProjectParamMap(projectParamMap);
				pdoQueryHandler.setModifierMetadataXmlMap(modifierMetadataXmlMap);
				pdoQueryHandler.setRequestVersion(version);
				pdoQueryHandler.setDimensionMetaDataParamList(patientMetaDataList, visitMetaDataList);
				pdoQueryHandler.processPDORequest();
				patientDataType = pdoQueryHandler.getPlainPdo();

			}
		} catch (Exception ex) {
			ex.printStackTrace();
			log.error(ex.getMessage(), ex);
			throw new I2B2Exception(ex.getMessage(), ex);
		}
		patientDataResponseType.setPatientData(patientDataType);

		return patientDataResponseType;
	}

	/**
	 * Function to get plain pdo from the given pdo request
	 * 
	 * @ejb.interface-method view-type="both"
	 * @ejb.transaction type="Required"
	 */
	public List<ParamType> getPDOTemplate(String dimensionTableName,
			DataSourceLookup dataSourceLookup,boolean tablePDOFlag) throws I2B2Exception {
		
		List<ParamType> paramList = new ArrayList<ParamType>();
		
		DAOFactoryHelper helper = new DAOFactoryHelper(dataSourceLookup
				.getDomainId(), dataSourceLookup.getProjectPath(),
				dataSourceLookup.getOwnerId());
		IDAOFactory daoFactory = helper.getDAOFactory();
	
		//
		MetaDataTypeMapper metaDataTypeMapper = new MetaDataTypeMapper();
		IMetadataDao metadataDao = daoFactory.getPatientDataDAOFactory().getMetadataDAO();
		try { 
			Map optionalFieldMap = metadataDao.getMetadataForOptionalField(dimensionTableName);
			MetadataDao.TableMetaData[] tableMetaDataList = (TableMetaData[]) optionalFieldMap.values().toArray(new MetadataDao.TableMetaData[]{});
			metaDataTypeMapper.fillXmlDataType(tableMetaDataList,tablePDOFlag);
			for (int i=0;i<tableMetaDataList.length;i++) { 
				ParamType paramType = new ParamType(); 
				paramType.setColumn(tableMetaDataList[i].column_name); 
				paramType.setColumnDescriptor(tableMetaDataList[i].column_comment);
				paramType.setType(tableMetaDataList[i].column_xml_type);
				paramList.add(paramType); 
				
			}
	
			
		} catch (Exception i2b2DaoEx) { 
			i2b2DaoEx.printStackTrace();
			throw new I2B2Exception(i2b2DaoEx.getMessage(), i2b2DaoEx);
		}
		
		return paramList;
	}
	
	
	
	private PageType buildPageType(int requestedMinIndex,
			int requestedMaxIndex, int maxInputIndex, long totalSize,
			long pageSize) {
		PageType pageType = new PageType();
		PageByPatientType pageByPatientType = new PageByPatientType();
		pageByPatientType.setPageSize(pageSize);
		pageByPatientType.setFactsTotal(totalSize);

		PageRangeType requestedPageRangeType = new PageRangeType();
		requestedPageRangeType.setFirstIndex(requestedMinIndex);
		requestedPageRangeType.setLastIndex(requestedMaxIndex);
		pageByPatientType.setPatientsRequested(requestedPageRangeType);

		PageRangeType returnedPageRangeType = new PageRangeType();
		returnedPageRangeType.setFirstIndex(requestedMinIndex);
		int maxReturnedIndex = requestedMinIndex + maxInputIndex - 1;
		returnedPageRangeType.setLastIndex(maxReturnedIndex);
		pageByPatientType.setPatientsReturned(returnedPageRangeType);

		PageRangeType skippedPageRangeType = new PageRangeType();
		skippedPageRangeType.setFirstIndex(0);
		skippedPageRangeType.setLastIndex(requestedMinIndex);
		pageByPatientType.setPatientsSkipped(skippedPageRangeType);

		pageType.setPagingByPatients(pageByPatientType);
		return pageType;
	}

	/**
	 * Function to get observation fact by its primary key
	 * 
	 * @ejb.interface-method view-type="both"
	 * @ejb.transaction type="Required"
	 */
	public PatientDataType getObservationFactByPrimaryKey(
			DataSourceLookup dataSourceLookup,
			GetObservationFactByPrimaryKeyRequestType getObservationFactByPrimaryKeyRequestType)
			throws I2B2Exception {
		PatientDataType patientDataType = null;

		try {

			PatientDataDAOFactory pdoDaoFactory = getPatientDataDaoFactory(
					dataSourceLookup.getDomainId(), dataSourceLookup
							.getProjectPath(), dataSourceLookup.getOwnerId());

			IObservationFactDao observationFactDao = pdoDaoFactory
					.getObservationFactDAO();
			patientDataType = observationFactDao
					.getObservationFactByPrimaryKey(
							getObservationFactByPrimaryKeyRequestType
									.getFactPrimaryKey(),
							getObservationFactByPrimaryKeyRequestType
									.getFactOutputOption());
		} catch (I2B2DAOException ex) {
			ex.printStackTrace();
			log.error(ex.getMessage(), ex);
			throw new I2B2Exception(ex.getMessage(), ex);
		}

		return patientDataType;
	}

	private PatientDataDAOFactory getPatientDataDaoFactory(String domainId,
			String projectPath, String ownerId) throws I2B2DAOException {
		DAOFactoryHelper helper = new DAOFactoryHelper(domainId, projectPath,
				ownerId);
		PatientDataDAOFactory pdoDaoFactory = helper.getDAOFactory()
				.getPatientDataDAOFactory();
		return pdoDaoFactory;
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

	private void savePdoQueryMaster(IQueryPdoMasterDao queryPdoMasterDao,
			String requestXml) throws I2B2Exception {
		QtQueryMaster queryMaster = new QtQueryMaster();

		I2B2RequestMessageHelper requestMessageHelper = new I2B2RequestMessageHelper(
				requestXml);

		SecurityType securityType = requestMessageHelper.getSecurityType();
		String userId = securityType.getUsername();
		String groupId = requestMessageHelper.getProjectId();

		QueryDefinitionType queryDefType = null;
		try {
			queryDefType = requestMessageHelper.getQueryDefinition();
		} catch (JAXBUtilException e) {
			throw new I2B2Exception("Error reading the query definition ["
					+ StackTraceUtil.getStackTrace(e) + "]");
		}
		edu.harvard.i2b2.crc.datavo.setfinder.query.ObjectFactory of = new edu.harvard.i2b2.crc.datavo.setfinder.query.ObjectFactory();

		queryMaster.setUserId(userId);
		StringWriter queryDefWriter = new StringWriter();
		try {
			CRCJAXBUtil.getQueryDefJAXBUtil().marshaller(
					of.createQueryDefinition(queryDefType), queryDefWriter);
		} catch (JAXBUtilException e) {
			throw new I2B2Exception("Error marshalling the query definition ["
					+ StackTraceUtil.getStackTrace(e) + "]");
		}

		queryMaster.setRequestXml(queryDefWriter.toString());
		queryMaster.setGroupId(groupId);
		queryMaster.setCreateDate(new Date(System.currentTimeMillis()));
		queryMaster.setI2b2RequestXml(requestXml);
		queryMaster.setRequestXml(requestXml);

		//remove user password from the request
		RequestMessageType i2b2RequestMsgType = requestMessageHelper.getI2B2RequestMessageType();
		PasswordType passType = i2b2RequestMsgType.getMessageHeader().getSecurity().getPassword();
		passType.setValue("password not stored"); 
		passType.setIsToken(false);
		
		JAXBUtil util = CRCJAXBUtil.getJAXBUtil();
		StringWriter strWriter = new StringWriter();
		edu.harvard.i2b2.crc.datavo.i2b2message.ObjectFactory i2b2ObjFactory = new edu.harvard.i2b2.crc.datavo.i2b2message.ObjectFactory();
		try {
				util.marshaller(i2b2ObjFactory.createRequest(i2b2RequestMsgType), strWriter);
		} catch (JAXBUtilException e) {
			throw new I2B2Exception("Error marshalling the request after resetting the password  ["
					+ StackTraceUtil.getStackTrace(e) + "]");
		}
		
		queryPdoMasterDao.createPdoQueryMaster(queryMaster, strWriter.toString());
	}
	
	private String getVersion(IQueryPdoMasterDao queryPdoMasterDao,
			String requestXml) throws I2B2Exception {
		QtQueryMaster queryMaster = new QtQueryMaster();

		I2B2RequestMessageHelper requestMessageHelper = new I2B2RequestMessageHelper(
				requestXml);
		return requestMessageHelper.getVersion();
	}
	
	private void copyModifierToItem(ItemType itemType, ModifierType modifierType) { 
		ItemType.ConstrainByModifier modifierConstrain = itemType.getConstrainByModifier();
		modifierConstrain.setAppliedPath(modifierType.getAppliedPath());
		modifierConstrain.setDimColumndatatype(modifierType.getColumndatatype()); 
		modifierConstrain.setDimColumnname(modifierType.getColumnname()); 
		modifierConstrain.setDimDimcode(modifierType.getDimcode()); 
		modifierConstrain.setDimOperator(modifierType.getOperator()); 
		modifierConstrain.setDimTablename(modifierType.getTablename()); 
		modifierConstrain.setFacttablecolumn(modifierType.getFacttablecolumn()); 
		modifierConstrain.setModifierKey(modifierType.getKey()); 
		modifierConstrain.setModifierName(modifierType.getName()); 
	}


}
