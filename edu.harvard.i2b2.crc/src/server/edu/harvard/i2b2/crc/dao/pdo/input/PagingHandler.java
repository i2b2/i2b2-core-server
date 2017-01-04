package edu.harvard.i2b2.crc.dao.pdo.input;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import org.apache.axis2.AxisFault;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.exception.StackTraceUtil;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.crc.dao.CRCDAO;
import edu.harvard.i2b2.crc.dao.DAOFactoryHelper;
import edu.harvard.i2b2.crc.dao.pdo.IPageDao;
import edu.harvard.i2b2.crc.dao.setfinder.querybuilder.ConceptNotFoundException;
import edu.harvard.i2b2.crc.dao.setfinder.querybuilder.OntologyException;
import edu.harvard.i2b2.crc.datavo.db.DataSourceLookup;
import edu.harvard.i2b2.crc.datavo.i2b2message.SecurityType;
import edu.harvard.i2b2.crc.datavo.ontology.DerivedFactColumnsType;
import edu.harvard.i2b2.crc.datavo.ontology.XmlValueType;
import edu.harvard.i2b2.crc.datavo.pdo.query.FactOutputOptionType;
import edu.harvard.i2b2.crc.datavo.pdo.query.FilterListType;
import edu.harvard.i2b2.crc.datavo.pdo.query.InputOptionListType;
import edu.harvard.i2b2.crc.datavo.pdo.query.OutputOptionListType;
import edu.harvard.i2b2.crc.datavo.pdo.query.PanelType;
import edu.harvard.i2b2.crc.delegate.ontology.CallOntologyUtil;
import edu.harvard.i2b2.crc.util.PMServiceAccountUtil;
import edu.harvard.i2b2.crc.util.QueryProcessorUtil;

/**
 * Paging Handler class.
 * 
 * a)Calculate average observation per patient and keep trying until the
 * iteration count reached.
 * 
 * b)Then try with min percent of patient list specified in the property file.
 * 
 * c)Then try with min size of patient list specified in the property file.
 * 
 * @author rk903
 */
public class PagingHandler extends CRCDAO {

	InputOptionListType inputList = null;
	FilterListType filterList = null;
	OutputOptionListType outputOptionList = null;
	DataSourceLookup dataSourceLookup = null;
	QueryProcessorUtil qpUtil = QueryProcessorUtil.getInstance();
	DAOFactoryHelper daoFactoryHelper = null;
	PageMethod pageMethod = null;
	Map projectParamMap = null;
	Map<String,XmlValueType> modifierMetadataXmlMap = null; 
	

	public static final String TOTAL_OBSERVATION = "TOTAL_OBSERVATION";
	public static final String MAX_INPUT_LIST = "MAX_INPUT_LIST";
	public static final String PAGING_REQUIRED_FLAG = "PAGING_REQUIRED_FLAG";

	public PagingHandler(DAOFactoryHelper helper,
			final InputOptionListType inputList,
			final FilterListType filterList, String pageMethodName)
			throws I2B2DAOException {
		this.daoFactoryHelper = helper;
		this.dataSourceLookup = helper.getDataSourceLookup();
		this.inputList = inputList;
		this.filterList = filterList;
		this.outputOptionList = new OutputOptionListType();
		pageMethod = PageMethodFactory.buildPageMethod(pageMethodName);
	}
	
	public void setProjectParamMap(Map projectParamMap) { 
		this.projectParamMap = projectParamMap;
	}
	
	public void setModifierMetadataXmlMap(Map<String,XmlValueType> modifierMetadataXmlMap) { 
		this.modifierMetadataXmlMap = modifierMetadataXmlMap;
	}
	
	
	public long getTotal(int maxInputList) throws SQLException, I2B2Exception {

		IInputOptionListHandler inputOptionListHandler = PDOFactory
				.buildInputListHandler(inputList, dataSourceLookup);
		// inputList.getPatientList().setMax(maxInputList);
		int minIndex = inputOptionListHandler.getMinIndex();
		inputOptionListHandler.setMaxIndex(minIndex + maxInputList);

		// iterate the panel and call total
		IFactRelatedQueryHandler factRelatedHandler = null;
		String countSqlFrom = " ";
		String countClause = " COUNT(*) ";
		if (dataSourceLookup.getServerType().equalsIgnoreCase(
				DAOFactoryHelper.ORACLE) || dataSourceLookup.getServerType().equalsIgnoreCase(
						DAOFactoryHelper.POSTGRESQL)) {
			factRelatedHandler = new FactRelatedQueryHandler(dataSourceLookup,
					inputList, filterList, outputOptionList);
		} else if (dataSourceLookup.getServerType().equalsIgnoreCase(
				DAOFactoryHelper.SQLSERVER)) {
			countSqlFrom = "as";
			countClause = " COUNT_BIG(*) ";
			factRelatedHandler = new SQLServerFactRelatedQueryHandler(
					dataSourceLookup, inputList, filterList, outputOptionList);
		}
		factRelatedHandler.setProjectParamMap(this.projectParamMap);
		factRelatedHandler.setModifierMetadataXmlMap(this.modifierMetadataXmlMap);
		
		FactOutputOptionType factOutputOptionType = new FactOutputOptionType();
		factOutputOptionType.setOnlykeys(true);
		outputOptionList.setObservationSet(factOutputOptionType);
		// outputOptionList.getObservationSet().setOnlykeys(true);

		// build sql for all the panel
		// DAOFactoryHelper daoFactoryHelper = new DAOFactoryHelper(
		// dataSourceLookup);

		IPageDao pageTotalDao = daoFactoryHelper.getDAOFactory()
				.getPatientDataDAOFactory().getPageDAO();
		int panelCount = filterList.getPanel().size();
		List<String> panelSqlList = new ArrayList<String>(panelCount);
		List<Integer> sqlCountList = new ArrayList<Integer>(panelCount);
		long totalObservations = 0;
		for (PanelType singlePanel : filterList.getPanel()) {
			int sqlParamCount = singlePanel.getItem().size();
			if (singlePanel.getInvert() == 1) {
				sqlParamCount++;
			}

			sqlCountList.add(sqlParamCount);
			String totalSql = pageTotalDao.buildTotalSql(factRelatedHandler,
					singlePanel);
			if (totalSql.trim().length() == 0) { 
				continue;
			}
			if((singlePanel != null)&&(singlePanel.getItem().get(0).getFacttablecolumn()!=null)){
				boolean derivedFactTable = QueryProcessorUtil.getInstance().getDerivedFactTable();
				
				String defaultTableName = dataSourceLookup.getFullSchema() + ".observation_FACT" ;
				if(derivedFactTable == true){
					if(singlePanel.getItem().get(0).getFacttablecolumn().contains(".")){

						String baseItemFactColumn = singlePanel.getItem().get(0).getFacttablecolumn();
						int lastIndex = baseItemFactColumn.lastIndexOf(".");
						String factTable = dataSourceLookup.getFullSchema() + "."+(baseItemFactColumn.substring(0, lastIndex));

						DerivedFactColumnsType columns = getFactColumnsFromOntologyCell(singlePanel.getItem().get(0).getItemKey());
						if(columns != null){
							if(columns.getDerivedFactTableColumn().size() > 1) {
								// parse through solumns and build up replace string.

								Iterator<String> it = columns.getDerivedFactTableColumn().iterator();
								String column = it.next();
								
								lastIndex = column.lastIndexOf(".");
								String table = dataSourceLookup.getFullSchema() + "."+ (column.substring(0, lastIndex));

								
								factTable = "( select * from " + table;
								while(it.hasNext()){
									column = it.next();
									
									lastIndex = column.lastIndexOf(".");
									table = dataSourceLookup.getFullSchema() + "."+ (column.substring(0, lastIndex));

									factTable += "\n UNION ALL \n"
											+ " select * from " +  table;
								}
								factTable += " )";

							}
						}
						log.debug("Parse columns " + factTable);
						totalSql=totalSql.replaceAll(defaultTableName, factTable)	;
						
						
						
					}
				}
				
		
				
				
				
			}
			
			panelSqlList.add("SELECT "
					+ countClause
					+ " from ( "
					+ totalSql + " ) " + countSqlFrom + " totalsql");
		}

		totalObservations = pageTotalDao.getTotalForAllPanel(panelSqlList,
				sqlCountList, inputOptionListHandler);
		return totalObservations;
	}

	public HashMap getMinPatientIndexAndTheTotal(int maxInputList)
			throws SQLException, I2B2Exception {

		IInputOptionListHandler inputOptionListHandler = PDOFactory
				.buildInputListHandler(inputList, dataSourceLookup);

		// inputList.getPatientList().setMax(maxInputList);
		inputOptionListHandler.setMaxIndex(maxInputList);

		// iterate the panel and call total
		IFactRelatedQueryHandler factRelatedHandler = null;
		String countSqlFrom = " ";
		if (dataSourceLookup.getServerType().equalsIgnoreCase(
				DAOFactoryHelper.ORACLE) || dataSourceLookup.getServerType().equalsIgnoreCase(
						DAOFactoryHelper.POSTGRESQL)) {
			factRelatedHandler = new FactRelatedQueryHandler(dataSourceLookup,
					inputList, filterList, outputOptionList);
		} else if (dataSourceLookup.getServerType().equalsIgnoreCase(
				DAOFactoryHelper.SQLSERVER)) {
			countSqlFrom = "as";
			factRelatedHandler = new SQLServerFactRelatedQueryHandler(
					dataSourceLookup, inputList, filterList, outputOptionList);
		}
		//set project param
		factRelatedHandler.setProjectParamMap(this.projectParamMap);
		factRelatedHandler.setModifierMetadataXmlMap(this.modifierMetadataXmlMap);
		
		FactOutputOptionType factOutputOptionType = new FactOutputOptionType();
		factOutputOptionType.setOnlykeys(true);
		outputOptionList.setObservationSet(factOutputOptionType);
		// outputOptionList.getObservationSet().setOnlykeys(true);

		// build sql for all the panel
		// DAOFactoryHelper daoFactoryHelper = new DAOFactoryHelper(
		// dataSourceLookup);

		IPageDao pageTotalDao = daoFactoryHelper.getDAOFactory()
				.getPatientDataDAOFactory().getPageDAO();
		int panelCount = filterList.getPanel().size();
		List<String> panelMinSqlList = new ArrayList<String>(panelCount);
		List<Integer> sqlCountList = new ArrayList<Integer>(panelCount);
		long totalObservations = 0;
		// things to consider to find the minimum
		// a)How to handle the different input list (i.e. enumeration list,table
		// or patient_dimension)

		String panelSql = "";
		for (PanelType singlePanel : filterList.getPanel()) {
			int sqlParamCount = singlePanel.getItem().size();
			if (singlePanel.getInvert() == 1) {
				sqlParamCount++;
			}
			if (inputOptionListHandler.isCollectionId()) {
				sqlParamCount++;
			}

			sqlCountList.add(sqlParamCount);

			panelSql = pageTotalDao.buildTotalSql(factRelatedHandler,
					singlePanel);
			if (panelSql.length() ==0) { 
				continue;
			}
			String minSql = inputOptionListHandler
					.generateMinIndexSql(panelSql);
			System.out.println("min sql for panel " + minSql);
			panelMinSqlList.add(minSql);

		}

		return pageTotalDao.getMinIndexAndCountAllPanel(panelMinSqlList,
				sqlCountList, inputOptionListHandler);

	}

	public int getMinPercent() throws I2B2Exception {
		return qpUtil.getPagingInputListMinPercent();

	}

	public long getPageSize() throws I2B2Exception {
		return qpUtil.getPagingObservationSize();
	}

	public int getPagingMaxIteration() throws I2B2Exception {
		return qpUtil.getPagingIterationCount();

	}

	public int getPagingMinSize() throws I2B2Exception {
		return qpUtil.getPagingInputListMinSize();
	}

	/**
	 * @return
	 * @throws SQLException
	 * @throws I2B2Exception
	 */
	public HashMap calculateMaxPageInputList() throws SQLException,
			I2B2Exception {
		int maxIteration = getPagingMaxIteration();
		int i = 0;
		long pageSize = getPageSize();
		boolean fitPagingFlag = false;
		HashMap returnResultMap = new HashMap();
		returnResultMap.put("PAGING_REQUIRED_FLAG", true);
		long totalObservations = 0;
		// PatientListTypeHandler patientList = new PatientListTypeHandler(
		// dataSourceLookup, inputList.getPatientList());

		IInputOptionListHandler inputOptionListHandler = PDOFactory
				.buildInputListHandler(inputList, dataSourceLookup);

		int maxInputList = inputOptionListHandler.getMaxIndex();
		int minInputList = inputOptionListHandler.getMinIndex();
		int originalMaxInputList = maxInputList;
		int inputListCount = maxInputList - minInputList;
		while (i < maxIteration) {
			totalObservations = getTotal(inputListCount);
			log.debug("Total observations for [" + inputListCount + " ] is ["
					+ totalObservations + " ]");
			// 
			if (i == 0) {
				returnResultMap.put("TOTAL_OBSERVATION", totalObservations);
			}

			// if total < page size then set max patient break
			if (checkIfFitThePage(pageSize, totalObservations)) {
				log.debug("Input list size of [" + inputListCount
						+ "] fits the page size [" + pageSize + "]");
				fitPagingFlag = true;
				if (i == 0) {
					returnResultMap.put("PAGING_REQUIRED_FLAG", false);
				}
				break;
			}

			// call paging method with page size, total observation,..
			inputListCount = pageMethod.calculateListSize(inputListCount,
					totalObservations, pageSize);

			if (inputListCount < 1) {
				// need not continue the iteration if the list size is reduced
				// to < 1
				break;
			}
			i++;
		}

		if (fitPagingFlag) {
			returnResultMap.put("MAX_INPUT_LIST", inputListCount);
			return returnResultMap;
		} else {
			throw new I2B2Exception("Could not fit in a page after ["
					+ maxIteration + "] iteration");
		}

		/*
		 * // try with minimum percent of patient int minIndex =
		 * patientList.getMinIndex(); int maxIndex = originalMaxInputList; int
		 * listLength = maxIndex - minIndex; int minPercentInput =
		 * getMinPercentInput(listLength);
		 * log.debug("Trying with minimum input list percent of [ " +
		 * minPercentInput + "]");
		 * 
		 * if (minPercentInput > 0) { if (minPercentInput < maxInputList) {
		 * totalObservations = getTotal(minPercentInput); log
		 * .debug("Total observations for minimum input list percent  [" +
		 * minPercentInput + " ] is [" + totalObservations + " ]"); if
		 * (totalObservations > 0 && checkIfFitThePage(pageSize,
		 * totalObservations)) { maxInputList = minIndex + minPercentInput;
		 * returnResultMap.put("MAX_INPUT_LIST", maxInputList); return
		 * returnResultMap; } } } else { log
		 * .debug("Skipping minimum input list percent, since the value is 0");
		 * }
		 */

		// try with minimun of single patient
		// int minPagingSize = getPagingMinSize();
		// log.debug("Trying with minimum input list size of [" + minPagingSize
		// + "]");
		// if (minPagingSize < 1) {
		// throw new I2B2DAOException(
		// "Paging failed, minimum page size should not be less than 1");
		// }
		//
		// HashMap minMap = getMinPatientIndexAndTheTotal(originalMaxInputList);
		// int minDataIndex = (Integer) minMap.get("MIN_INDEX");
		// long minDataIndexTotal = (Long) minMap.get("MIN_INDEX_TOTAL");
		//
		// System.out.println("min index" + minMap.get("MIN_INDEX"));
		// System.out.println("min index total " +
		// minMap.get("MIN_INDEX_TOTAL"));
		//
		// // totalObservations = getTotal(minPagingSize);
		// log.debug("Total observation for first data index [" + minDataIndex
		// + " ] is [" + minDataIndexTotal + "]");
		//
		// if (!checkIfFitThePage(pageSize, minDataIndexTotal)) {
		// throw new I2B2DAOException(
		// "Paging failed, even the first patient index of ["
		// + minDataIndex + "] patient observations ["
		// + minDataIndexTotal
		// + "] could not fit the page size [" + pageSize
		// + "]");
		// } else {
		//
		// returnResultMap.put("MAX_INPUT_LIST", minDataIndex);
		// return returnResultMap;
		// }
	}

	public int getMinPercentInput(int listLength) throws I2B2Exception {
		int minPercent = getMinPercent();

		int minPercentOfList = listLength * (minPercent / 100);
		return minPercentOfList;
	}

	private boolean checkIfFitThePage(long pageSize, long totalObservations) {
		return (pageSize >= totalObservations);

	}

	
	protected DerivedFactColumnsType getFactColumnsFromOntologyCell(String itemKey)
			throws ConceptNotFoundException, OntologyException {
		DerivedFactColumnsType factColumns = new DerivedFactColumnsType();
		try {
			
			QueryProcessorUtil qpUtil = QueryProcessorUtil.getInstance();
			String ontologyUrl = qpUtil
					.getCRCPropertyValue(QueryProcessorUtil.ONTOLOGYCELL_ROOT_WS_URL_PROPERTIES);


			SecurityType securityType = PMServiceAccountUtil
					.getServiceSecurityType(dataSourceLookup.getDomainId());
			
			
			factColumns = CallOntologyUtil.callGetFactColumns(itemKey,
					securityType, dataSourceLookup.getProjectPath(),
					ontologyUrl +"/getDerivedFactColumns");
		} catch (JAXBUtilException e) {

			log.error("Error while fetching metadata [" + itemKey
					+ "] from ontology ", e);
			throw new OntologyException("Error while fetching metadata ["
					+ itemKey + "] from ontology "
					+ StackTraceUtil.getStackTrace(e));
		} catch (I2B2Exception e) {
			log.error("Error while fetching metadata from ontology ", e);
			throw new OntologyException("Error while fetching metadata ["
					+ itemKey + "] from ontology "
					+ StackTraceUtil.getStackTrace(e));
		} catch (AxisFault e) {
			log.error("Error while fetching metadata from ontology ", e);
			throw new OntologyException("Error while fetching metadata ["
					+ itemKey + "] from ontology "
					+ StackTraceUtil.getStackTrace(e));
		} catch (XMLStreamException e) {
			log.error("Error while fetching metadata from ontology ", e);
			throw new OntologyException("Error while fetching metadata ["
					+ itemKey + "] from ontology "
					+ StackTraceUtil.getStackTrace(e));
		}

//		if (factColumns.isEmpty()) {
//			throw new ConceptNotFoundException("[" + itemKey + "] ");

//		} 

		return factColumns;
	}


	
}
