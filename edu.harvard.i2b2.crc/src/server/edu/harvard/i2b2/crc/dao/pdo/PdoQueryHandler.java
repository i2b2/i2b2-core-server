/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors: 
 *     Rajesh Kuttan
 */
package edu.harvard.i2b2.crc.dao.pdo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.crc.dao.DAOFactoryHelper;
import edu.harvard.i2b2.crc.dao.PatientDataDAOFactory;
import edu.harvard.i2b2.crc.dao.pdo.input.FactRelatedQueryHandler;
import edu.harvard.i2b2.crc.dao.pdo.input.IFactRelatedQueryHandler;
import edu.harvard.i2b2.crc.dao.pdo.input.IInputOptionListHandler;
import edu.harvard.i2b2.crc.dao.pdo.input.PDOFactory;
import edu.harvard.i2b2.crc.dao.pdo.input.SQLServerFactRelatedQueryHandler;
import edu.harvard.i2b2.crc.dao.pdo.output.ConceptFactRelated;
import edu.harvard.i2b2.crc.dao.pdo.output.EidFactRelated;
import edu.harvard.i2b2.crc.dao.pdo.output.ModifierFactRelated;
import edu.harvard.i2b2.crc.dao.pdo.output.ObservationFactFactRelated;
import edu.harvard.i2b2.crc.dao.pdo.output.PatientFactRelated;
import edu.harvard.i2b2.crc.dao.pdo.output.PidFactRelated;
import edu.harvard.i2b2.crc.dao.pdo.output.ProviderFactRelated;
import edu.harvard.i2b2.crc.dao.pdo.output.VisitFactRelated;
import edu.harvard.i2b2.crc.datavo.db.DataSourceLookup;
import edu.harvard.i2b2.crc.datavo.i2b2message.SecurityType;
import edu.harvard.i2b2.crc.datavo.ontology.XmlValueType;
import edu.harvard.i2b2.crc.datavo.pdo.ConceptSet;
import edu.harvard.i2b2.crc.datavo.pdo.EidSet;
import edu.harvard.i2b2.crc.datavo.pdo.EventSet;
import edu.harvard.i2b2.crc.datavo.pdo.ModifierSet;
import edu.harvard.i2b2.crc.datavo.pdo.ModifierType;
import edu.harvard.i2b2.crc.datavo.pdo.ObservationSet;
import edu.harvard.i2b2.crc.datavo.pdo.ObserverSet;
import edu.harvard.i2b2.crc.datavo.pdo.ParamType;
import edu.harvard.i2b2.crc.datavo.pdo.PatientDataType;
import edu.harvard.i2b2.crc.datavo.pdo.PatientSet;
import edu.harvard.i2b2.crc.datavo.pdo.PidSet;
import edu.harvard.i2b2.crc.datavo.pdo.query.EidListType;
import edu.harvard.i2b2.crc.datavo.pdo.query.EventListType;
import edu.harvard.i2b2.crc.datavo.pdo.query.FilterListType;
import edu.harvard.i2b2.crc.datavo.pdo.query.InputOptionListType;
import edu.harvard.i2b2.crc.datavo.pdo.query.OutputOptionListType;
import edu.harvard.i2b2.crc.datavo.pdo.query.PanelType;
import edu.harvard.i2b2.crc.datavo.pdo.query.PatientListType;
import edu.harvard.i2b2.crc.datavo.pdo.query.PidListType;
import edu.harvard.i2b2.crc.datavo.pdo.query.PidListType.Pid;
import edu.harvard.i2b2.crc.datavo.pm.ProjectType;

/**
 * <b>Main class for PDO queries.<b>
 * 
 * <p>
 * The is the main class to handle pdo query's. It reads input,filter and output
 * option list from pdo request and delegate to following class to build
 * individual PDO sections.
 * 
 * Observation Fact = {@link FactRelatedQueryHandler} PatientSet = {@list
 * PatientSection} ObservationSet ={@list ObservationSection} ObserverSet =
 * {@list ObserverSection} ConceptSet = {@list ConceptSection}
 * 
 * <p>
 * Sample PDO request sections:
 * <p>
 * Input list: <b><input_list> <patient_list max="10" min="0">
 * <patient_set_coll_id>184</patient_set_coll_id> </patient_list> </input_list>
 * </b>
 * 
 * <p>
 * Filter list: <b><filter_list> <concept_list> <concept_path
 * filter_name="Bakers'asthma">\i2b2\Diagnoses\</concept_path> </concept_list>
 * </filter_list> </b>
 * 
 *<p>
 * Output Option List: <b> <ouput_option> <observation_set blob="false"
 * onlykeys="false"/> <patient_set select="using_input_list" onlykeys="false"/>
 * </ouput_option> </b> $Id: PdoQueryHandler.java,v 1.12 2008/07/21 19:53:40
 * rk903 Exp $
 * 
 * @author rkuttan
 * @see FactRelatedQueryHandler
 * @see PatientFactRelated
 * @see ProviderFactRelated
 * @see VisitFactRelated
 * @see ObservationFactFactRelated
 */
public class PdoQueryHandler {
	/** logger **/
	protected final Log log = LogFactory.getLog(getClass());

	/** Table pdo type value used internally **/
	public static final String TABLE_PDO_TYPE = "TABLE_PDO_TYPE";
	/** Plain pdo type value used internally **/
	public static final String PLAIN_PDO_TYPE = "PLAIN_PDO_TYPE";
	/** PDO request input list **/
	private InputOptionListType inputList = null;
	/** PDO request input list before paging **/
	private InputOptionListType origInputList = null;
	/** PDO request filter list **/
	private FilterListType filterList = null;
	/** PDO output option list **/
	private OutputOptionListType outputOptionList = null;
	/** Observation helper to build observation section in pdo **/
	private VisitFactRelated visitFactRelated = null;
	/** Observer helper class to build Observer section in pdo **/
	private ProviderFactRelated providerFactRelated = null;
	/** Patient helper class to build patient section in pdo **/
	private PatientFactRelated patientFactRelated = null;
	/** Concept helper class to build concept section in pdo **/
	private ConceptFactRelated conceptFactRelated = null;
	/** Modifier helper class to build concept section in pdo **/
	private ModifierFactRelated modifierFactRelated = null;
	/** Observation fact helper class to build observationfact **/
	private ObservationFactFactRelated obsFactFactRelated = null;
	private PidFactRelated pidFactRelated = null;
	private EidFactRelated eidFactRelated = null;
	/** instance variable for pdo type **/
	private String pdoType = null;
	/** instance variable to hold plain pdo **/
	private PatientDataType plainPdoType = null;
	/** instance variable to hold table pdo **/
	private PatientDataType tablePdoType = null;

	private PatientDataDAOFactory pdoDaoFactory = null;
	
	private Map projectParamMap = null;
	private Map<String,XmlValueType> modifierMetadataXmlMap = null;
	private String requestVersion = "";
	private List<ParamType> patientMetaDataParamType = null, visitMetaDataParamType = null;


	
	/**
	 * Parameter constructor to initialize helper classes
	 * 
	 * @param pdoType
	 * @param inputList
	 * @param filterList
	 * @param outputOptionList
	 * @throws I2B2Exception
	 */
	public PdoQueryHandler(PatientDataDAOFactory pdoDaoFactory, String pdoType,
			InputOptionListType origInputList, InputOptionListType inputList,
			FilterListType filterList, OutputOptionListType outputOptionList)
			throws I2B2DAOException {
		if (pdoType == null) {
			throw new I2B2DAOException("Input pdoType should not be null");
		}

		if (!(pdoType.equalsIgnoreCase(PLAIN_PDO_TYPE) || pdoType
				.equalsIgnoreCase(TABLE_PDO_TYPE))) {
			throw new I2B2DAOException("Invalid pdoType : " + pdoType);
		}

		if (outputOptionList == null) {
			throw new I2B2DAOException(
					"Input output option list should not be null");
		}

		
		this.pdoDaoFactory = pdoDaoFactory;
		this.pdoType = pdoType;
		this.origInputList = origInputList;
		this.inputList = inputList;
		this.filterList = filterList;
		this.outputOptionList = outputOptionList;

		visitFactRelated = new VisitFactRelated(outputOptionList.getEventSet());
		providerFactRelated = new ProviderFactRelated(outputOptionList
				.getObserverSetUsingFilterList());
		patientFactRelated = new PatientFactRelated(outputOptionList
				.getPatientSet());
		conceptFactRelated = new ConceptFactRelated(outputOptionList
				.getConceptSetUsingFilterList());
		modifierFactRelated = new ModifierFactRelated(outputOptionList
				.getModifierSetUsingFilterList());
		obsFactFactRelated = new ObservationFactFactRelated(outputOptionList
				.getObservationSet());
		pidFactRelated = new PidFactRelated(outputOptionList.getPidSet());
		eidFactRelated = new EidFactRelated(outputOptionList.getEidSet());

	}
	
	public void setProjectParamMap(Map projectParamMap) { 
		this.projectParamMap =  projectParamMap;
	}
	
	public void setModifierMetadataXmlMap(Map<String,XmlValueType> modifierMetadataXmlMap) { 
		this.modifierMetadataXmlMap = modifierMetadataXmlMap;
	}
	
	public void setRequestVersion(String requestVersion) { 
		this.requestVersion = requestVersion;
	}
	
	public void setDimensionMetaDataParamList(List<ParamType> patientMetaDataParamType, List<ParamType> visitMetaDataParamType) { 
		this.patientMetaDataParamType = patientMetaDataParamType;
		this.visitMetaDataParamType = visitMetaDataParamType;
		
	}
	
	/**
	 * Method to find if input list is pid set
	 * 
	 * @return boolean
	 */
	public boolean isGetPDOFromPIDSet() {
		if (inputList.getPidList() != null) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Method to find if input list is patient set
	 * 
	 * @return boolean
	 */
	public boolean isGetPDOFromPatientSet() {
		if (inputList.getPatientList() != null) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Method to find if input list is visit set
	 * 
	 * @return boolean
	 */
	public boolean isGetPDOFromVisitSet() {
		if (inputList.getEventList() != null) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Method to find if input list is pid set
	 * 
	 * @return boolean
	 */
	public boolean isGetPDOFromPidSet() {
		if (inputList.getPidList() != null) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Method to find if input list is eid set
	 * 
	 * @return boolean
	 */
	public boolean isGetPDOFromEidSet() {
		if (inputList.getEidList() != null) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Returns TablePDO
	 * 
	 * @return TablePatientDataType
	 */
	public PatientDataType getTablePdo() {
		return tablePdoType;
	}

	/**
	 * Returns PlainPDO
	 * 
	 * @return PatientDataType
	 */
	public PatientDataType getPlainPdo() {
		return plainPdoType;
	}


	/**
	 * 
	 * @throws Exception
	 */
	public void processPDORequest() throws Exception {
		tablePdoType = new PatientDataType();
		plainPdoType = new PatientDataType();

		// check if obsrvation_fact tag present
		boolean obsFactSelected = obsFactFactRelated.isSelected();

		// check if provider or concept present
		boolean providerSelected = providerFactRelated.isSelected();
		boolean conceptSelected = conceptFactRelated.isSelected();
		boolean modifierSelected = modifierFactRelated.isSelected();

		// check if patient present
		boolean patientSelected = patientFactRelated.isSelected();

		// check if visit present
		boolean visitSelected = visitFactRelated.isSelected();
		boolean pidSelected = pidFactRelated.isSelected();
		boolean eidSelected = eidFactRelated.isSelected();

		boolean patientFromFact = patientFactRelated.isFactRelated();
		boolean visitFromFact = visitFactRelated.isFactRelated();
		boolean pidFromFact = pidFactRelated.isFactRelated();
		boolean eidFromFact = eidFactRelated.isFactRelated();

		IFactRelatedQueryHandler factRelatedQry = null;

		// PatientDataType patientDataType = new PatientDataType();

		// check if this is a fact related query
		if (obsFactSelected || providerSelected || conceptSelected
				|| patientFromFact || visitFromFact || pidFromFact
				|| eidFromFact || modifierSelected) {

			DataSourceLookup dataSourceLookup = pdoDaoFactory
					.getDataSourceLookup();
			if (dataSourceLookup.getServerType().equalsIgnoreCase(
					DAOFactoryHelper.ORACLE) ||
					dataSourceLookup.getServerType().equalsIgnoreCase(
					DAOFactoryHelper.POSTGRESQL)) {
				factRelatedQry = new FactRelatedQueryHandler(pdoDaoFactory
						.getDataSourceLookup(), inputList, filterList,
						outputOptionList);
			} else if (dataSourceLookup.getServerType().equalsIgnoreCase(
					DAOFactoryHelper.SQLSERVER)) {
				factRelatedQry = new SQLServerFactRelatedQueryHandler(
						pdoDaoFactory.getDataSourceLookup(), inputList,
						filterList, outputOptionList);
			}
			//set project param map
			factRelatedQry.setProjectParamMap(this.projectParamMap);
			factRelatedQry.setModifierMetadataXmlMap(modifierMetadataXmlMap);
			factRelatedQry.setRequestVersion(this.requestVersion);
			
			// execute query
			if (pdoType.equalsIgnoreCase(TABLE_PDO_TYPE)) {
				List<ObservationSet> tableObservationSet = factRelatedQry
						.getTablePdoObservationFact();
				if (obsFactSelected) {
					tablePdoType.getObservationSet()
							.addAll(tableObservationSet);
				}
			} else {
				List<ObservationSet> plainPdoObservationSet = factRelatedQry
						.getPdoObservationFact();

				if (obsFactSelected) {
					plainPdoType.getObservationSet().addAll(
							plainPdoObservationSet);
				}
			}
		}

		// check if observer section is specified in outputoption
		if (providerSelected) {
			ProviderSection providerSection = new ProviderSection(pdoType,
					factRelatedQry);
			providerSection.generateSet();

			if (pdoType.equalsIgnoreCase(TABLE_PDO_TYPE)) {
				tablePdoType.setObserverSet(providerSection
						.getTableProviderSet());
			} else {
				plainPdoType.setObserverSet(providerSection
						.getPlainProviderSet());
			}
		}

		// check if concept section is specified in outputoption
		if (conceptSelected) {
			ConceptSection cs = new ConceptSection(pdoType, factRelatedQry);
			cs.generateSet();

			if (pdoType.equalsIgnoreCase(TABLE_PDO_TYPE)) {
				tablePdoType.setConceptSet(cs.getTableConceptSet());
			} else {
				plainPdoType.setConceptSet(cs.getPlainConceptSet());
			}
		}
		
		// check if modifier section is specified in outputoption
		if (modifierSelected) {
			ModifierSection ms = new ModifierSection(pdoType, factRelatedQry);
			ms.generateSet();

			if (pdoType.equalsIgnoreCase(TABLE_PDO_TYPE)) {
				tablePdoType.setModifierSet(ms.getTableModifierSet());
			} else {
				plainPdoType.setModifierSet(ms.getPlainModifierSet());
			}
		}

		// check if patient section is specified in outputoption
		if (patientSelected) {
			PatientSection ps = new PatientSection(pdoType, factRelatedQry,
					patientFromFact, isGetPDOFromVisitSet(),
					isGetPDOFromPatientSet(), isGetPDOFromPIDSet(), patientMetaDataParamType);
			ps.generateSet();

			if (pdoType.equalsIgnoreCase(TABLE_PDO_TYPE)) {
				tablePdoType.setPatientSet(ps.getTablePatientSet());
			} else {
				plainPdoType.setPatientSet(ps.getPlainPatientSet());
			}
		}

		// check if observation section is specified in outputoption
		if (visitSelected) {
			VisitSection vs = new VisitSection(pdoType, factRelatedQry,
					visitFromFact, isGetPDOFromVisitSet(),
					isGetPDOFromPatientSet(),visitMetaDataParamType);
			vs.generateSet();

			if (pdoType.equalsIgnoreCase(TABLE_PDO_TYPE)) {
				tablePdoType.setEventSet(vs.getTableEventSet());
			} else {
				plainPdoType.setEventSet(vs.getPlainVisitSet());
			}
		}

		// check if pid section is specified in outputoption
		if (pidSelected) {
			PidSection pids = new PidSection(pdoType, factRelatedQry,
					pidFromFact, isGetPDOFromPatientSet(), isGetPDOFromPidSet());
			pids.generateSet();
			if (pdoType.equalsIgnoreCase(TABLE_PDO_TYPE)) {
				tablePdoType.setPidSet(pids.getPidSet());
			} else {
				plainPdoType.setPidSet(pids.getPidSet());
			}
		}

		// check if eid section is specified in outputoption
		if (eidSelected) {
			EidSection eids = new EidSection(pdoType, factRelatedQry,
					eidFromFact, isGetPDOFromPatientSet(), isGetPDOFromEidSet());
			eids.generateSet();
			if (pdoType.equalsIgnoreCase(TABLE_PDO_TYPE)) {
				tablePdoType.setEidSet(eids.getEidSet());
			} else {
				plainPdoType.setEidSet(eids.getEidSet());

			}
		}

	}

	private class EidSection {
		IFactRelatedQueryHandler factRelatedQry = null;
		EidSet eidSet = null;
		String pType = null;
		boolean pidFromFact = false;
		boolean fromPatientSet = false;
		boolean fromEidSet = false;

		// TODO , boolean fromPidSet
		public EidSection(String pType,
				IFactRelatedQueryHandler factRelatedQry, boolean pidFromFact,
				boolean fromPatientSet, boolean fromEidSet) {
			this.factRelatedQry = factRelatedQry;
			this.pType = pType;
			this.pidFromFact = pidFromFact;
			this.fromPatientSet = fromPatientSet;
			this.fromEidSet = fromEidSet;
		}

		/**
		 * 
		 * @throws I2B2Exception
		 */
		public void generateSet() throws I2B2Exception {
			// check if provider selected
			IPdoQueryEidDao eidDao = pdoDaoFactory.getPdoQueryEidDAO();

			boolean detailFlag = pidFactRelated.isSelectDetail();
			boolean blobFlag = pidFactRelated.isSelectBlob();
			boolean statusFlag = pidFactRelated.isSelectStatus();
			if (pidFromFact) {
				List<String> encounterNumFactList = factRelatedQry
						.getVisitFactList();
				List<String> panelSqlList = factRelatedQry.getPanelSqlList();

				String panelSql = null;

				List<Integer> sqlParamCountList = new ArrayList<Integer>();
				for (PanelType panel : filterList.getPanel()) {

					int sqlParamCount = panel.getItem().size();
					if (panel.getInvert() == 1) {
						sqlParamCount++;
					}
					sqlParamCountList.add(sqlParamCount);

				}
				IInputOptionListHandler inputOptionListHandler = PDOFactory
						.buildInputListHandler(inputList, pdoDaoFactory
								.getDataSourceLookup());

				eidSet = eidDao.getEidByFact(panelSqlList, sqlParamCountList,
						inputOptionListHandler, detailFlag, blobFlag,
						statusFlag);

			} else if (fromPatientSet) {
				PatientListType patientListType = inputList.getPatientList();
				eidSet = eidDao.getEidFromPatientSet(patientListType,
						detailFlag, blobFlag, statusFlag);
			} else if (fromEidSet) {
				EidListType eidListType = inputList.getEidList();
				eidSet = eidDao.getEidByEidList(eidListType, detailFlag,
						blobFlag, statusFlag);
			}

		}

		public EidSet getEidSet() {
			return eidSet;
		}

	}

	private class PidSection {
		IFactRelatedQueryHandler factRelatedQry = null;
		PidSet pidSet = null;
		String pType = null;
		boolean pidFromFact = false;
		boolean fromPatientSet = false;
		boolean fromPidSet = false;

		// TODO , boolean fromPidSet
		public PidSection(String pType,
				IFactRelatedQueryHandler factRelatedQry, boolean pidFromFact,
				boolean fromPatientSet, boolean fromPidSet) {
			this.factRelatedQry = factRelatedQry;
			this.pType = pType;
			this.pidFromFact = pidFromFact;
			this.fromPatientSet = fromPatientSet;
			this.fromPidSet = fromPidSet;
		}

		/**
		 * 
		 * @throws I2B2Exception
		 */
		public void generateSet() throws I2B2Exception {
			// check if provider selected
			IPdoQueryPidDao pidDao = pdoDaoFactory.getPdoQueryPidDAO();

			boolean detailFlag = pidFactRelated.isSelectDetail();
			boolean blobFlag = pidFactRelated.isSelectBlob();
			boolean statusFlag = pidFactRelated.isSelectStatus();
			if (pidFromFact) {
				List<String> patientNumFactList = factRelatedQry
						.getPatientFactList();
				System.out.println("Patient fact list size"
						+ patientNumFactList.size());
				List<String> panelSqlList = factRelatedQry.getPanelSqlList();

				String panelSql = null;

				List<Integer> sqlParamCountList = new ArrayList<Integer>();
				for (PanelType panel : filterList.getPanel()) {

					int sqlParamCount = panel.getItem().size();
					if (panel.getInvert() == 1) {
						sqlParamCount++;
					}
					sqlParamCountList.add(sqlParamCount);

				}
				IInputOptionListHandler inputOptionListHandler = PDOFactory
						.buildInputListHandler(inputList, pdoDaoFactory
								.getDataSourceLookup());

				pidSet = pidDao.getPidByFact(panelSqlList, sqlParamCountList,
						inputOptionListHandler, detailFlag, blobFlag,
						statusFlag);
			} else if (fromPatientSet) {
				PatientListType patientListType = inputList.getPatientList();
				pidSet = pidDao.getPidFromPatientSet(patientListType,
						detailFlag, blobFlag, statusFlag);
			} else if (fromPidSet) {
				PidListType pidListType = inputList.getPidList();
				pidSet = pidDao.getPidByPidList(pidListType, detailFlag,
						blobFlag, statusFlag);
			}

		}

		public PidSet getPidSet() {
			return pidSet;
		}

	}

	/**
     *   
     */
	private class ProviderSection {
		IFactRelatedQueryHandler factRelatedQry = null;
		ObserverSet providerDimensionSet = null;
		ObserverSet observerSet = null;
		String pType = null;

		public ProviderSection(String pType,
				IFactRelatedQueryHandler factRelatedQry) {
			this.factRelatedQry = factRelatedQry;
			this.pType = pType;
		}

		/**
		 * 
		 * @throws I2B2Exception
		 */
		public void generateSet() throws I2B2Exception {
			// check if provider selected
			IPdoQueryProviderDao providerDao = pdoDaoFactory
					.getPdoQueryProviderDAO();
			ITablePdoQueryProviderDao tableProviderDao = pdoDaoFactory
					.getTablePdoQueryProviderDAO();

			boolean detailFlag = providerFactRelated.isSelectDetail();
			boolean blobFlag = providerFactRelated.isSelectBlob();
			boolean statusFlag = providerFactRelated.isSelectStatus();

			List<String> panelSqlList = factRelatedQry.getPanelSqlList();

			String panelSql = null;

			List<Integer> sqlParamCountList = new ArrayList<Integer>();
			for (PanelType panel : filterList.getPanel()) {

				int sqlParamCount = panel.getItem().size();
				if (panel.getInvert() == 1) {
					sqlParamCount++;
				}
				sqlParamCountList.add(sqlParamCount);

			}
			IInputOptionListHandler inputOptionListHandler = PDOFactory
					.buildInputListHandler(origInputList, pdoDaoFactory
							.getDataSourceLookup());

			if (pType.equalsIgnoreCase(TABLE_PDO_TYPE)) {

				observerSet = tableProviderDao.getProviderByFact(panelSqlList,
						sqlParamCountList, inputOptionListHandler, detailFlag,
						blobFlag, statusFlag);

			} else {
				providerDimensionSet = providerDao.getProviderByFact(
						panelSqlList, sqlParamCountList,
						inputOptionListHandler, detailFlag, blobFlag,
						statusFlag);

			}

		}

		public ObserverSet getTableProviderSet() {
			return observerSet;
		}

		public ObserverSet getPlainProviderSet() {
			return providerDimensionSet;
		}
	}

	private class ConceptSection {
		IFactRelatedQueryHandler factRelatedQry = null;
		String pType = null;
		ConceptSet conceptSet = null;
		ConceptSet conceptDimensionSet = null;

		public ConceptSection(String pType,
				IFactRelatedQueryHandler factRelatedQry) {
			this.factRelatedQry = factRelatedQry;
			this.pType = pType;
		}

		public void generateSet() throws I2B2Exception {
			// check if concept selected
			List<String> conceptFactList = factRelatedQry.getConceptFactList();
			IPdoQueryConceptDao conceptDao = pdoDaoFactory
					.getPdoQueryConceptDAO();
			ITablePdoQueryConceptDao tableConceptDao = pdoDaoFactory
					.getTablePdoQueryConceptDAO();
			boolean detailFlag = conceptFactRelated.isSelectDetail();
			boolean blobFlag = conceptFactRelated.isSelectBlob();
			boolean statusFlag = conceptFactRelated.isSelectStatus();

			List<String> panelSqlList = factRelatedQry.getPanelSqlList();

			String panelSql = null;

			List<Integer> sqlParamCountList = new ArrayList<Integer>();
			for (PanelType panel : filterList.getPanel()) {

				int sqlParamCount = panel.getItem().size();
				if (panel.getInvert() == 1) {
					sqlParamCount++;
				}
				sqlParamCountList.add(sqlParamCount);

			}
			IInputOptionListHandler inputOptionListHandler = PDOFactory
					.buildInputListHandler(origInputList, pdoDaoFactory
							.getDataSourceLookup());

			if (pType.equalsIgnoreCase(TABLE_PDO_TYPE)) {

				conceptSet = tableConceptDao.getConceptByFact(panelSqlList,
						sqlParamCountList, inputOptionListHandler, detailFlag,
						blobFlag, statusFlag);

			} else {
				conceptDimensionSet = conceptDao.getConceptByFact(panelSqlList,
						sqlParamCountList, inputOptionListHandler, detailFlag,
						blobFlag, statusFlag);
			}
		}

		public ConceptSet getTableConceptSet() {
			return conceptSet;
		}

		public ConceptSet getPlainConceptSet() {
			return conceptDimensionSet;
		}
	}
	
	private class ModifierSection {
		IFactRelatedQueryHandler factRelatedQry = null;
		String pType = null;
		ModifierSet modifierSet = null;
		ModifierSet modifierDimensionSet = null;

		public ModifierSection(String pType,
				IFactRelatedQueryHandler factRelatedQry) {
			this.factRelatedQry = factRelatedQry;
			this.pType = pType;
		}

		public void generateSet() throws I2B2Exception {
			// check if modifier selected
			List<String> modifierFactList = factRelatedQry.getModifierFactList();
			IPdoQueryModifierDao modifierDao = pdoDaoFactory
					.getPdoQueryModifierDAO();
			ITablePdoQueryModifierDao tableModifierDao = pdoDaoFactory
					.getTablePdoQueryModifierDAO();
			boolean detailFlag = conceptFactRelated.isSelectDetail();
			boolean blobFlag = conceptFactRelated.isSelectBlob();
			boolean statusFlag = conceptFactRelated.isSelectStatus();

			List<String> panelSqlList = factRelatedQry.getPanelSqlList();

			String panelSql = null;

			List<Integer> sqlParamCountList = new ArrayList<Integer>();
			for (PanelType panel : filterList.getPanel()) {

				int sqlParamCount = panel.getItem().size();
				if (panel.getInvert() == 1) {
					sqlParamCount++;
				}
				sqlParamCountList.add(sqlParamCount);

			}
			IInputOptionListHandler inputOptionListHandler = PDOFactory
					.buildInputListHandler(origInputList, pdoDaoFactory
							.getDataSourceLookup());

			if (pType.equalsIgnoreCase(TABLE_PDO_TYPE)) {

				modifierSet = tableModifierDao.getModifierByFact(panelSqlList,
						sqlParamCountList, inputOptionListHandler, detailFlag,
						blobFlag, statusFlag);

			} else {
				modifierDimensionSet = modifierDao.getModifierByFact(panelSqlList,
						sqlParamCountList, inputOptionListHandler, detailFlag,
						blobFlag, statusFlag);
			}
		}

		public ModifierSet getTableModifierSet() {
			return modifierSet;
		}

		public ModifierSet getPlainModifierSet() {
			return modifierDimensionSet;
		}
	}

	private class PatientSection {
		IFactRelatedQueryHandler factRelatedQry = null;
		boolean patientFromFact = false;
		boolean fromVisitSet = false;
		boolean fromPatientSet = false;
		boolean fromPIDSet = false;
		PatientSet patientDimensionSet = null;
		PatientSet patientSet = null;
		String pType = null;
		List<ParamType> patientMetaDataType = null;

		public PatientSection(String pType,
				IFactRelatedQueryHandler factRelatedQry,
				boolean patientFromFact, boolean fromVisitSet,
				boolean fromPatientSet, boolean fromPIDSet, List<ParamType> patientMetaDataType) {
			this.factRelatedQry = factRelatedQry;
			this.patientFromFact = patientFromFact;
			this.fromVisitSet = fromVisitSet;
			this.fromPatientSet = fromPatientSet;
			this.fromPIDSet = fromPIDSet;
			this.pType = pType;
			this.patientMetaDataType  = patientMetaDataType;
		}

		public void generateSet() throws Exception {
			IPdoQueryPatientDao pdoQueryPatientDao = pdoDaoFactory
					.getPdoQueryPatientDAO();
			pdoQueryPatientDao.setMetaDataParamList(this.patientMetaDataType); 
			ITablePdoQueryPatientDao tablePdoQueryPatientDao = pdoDaoFactory
					.getTablePdoQueryPatientDAO();
			tablePdoQueryPatientDao.setMetaDataParamList(this.patientMetaDataType);

			// check if patient dimension is in output option
			boolean detailFlag = patientFactRelated.isSelectDetail();

			// check if patient dimension is in output option
			boolean blobFlag = patientFactRelated.isSelectBlob();

			// check if patient dimension is in output option
			boolean statusFlag = patientFactRelated.isSelectStatus();

			if (patientFromFact) {
				List<String> patientFactList = factRelatedQry
						.getPatientFactList();
				log.debug("Patient fact list size"
						+ patientFactList.size());

				List<String> panelSqlList = factRelatedQry.getPanelSqlList();

				String panelSql = null;

				List<Integer> sqlParamCountList = new ArrayList<Integer>();
				for (PanelType panel : filterList.getPanel()) {

					int sqlParamCount = panel.getItem().size();
					if (panel.getInvert() == 1) {
						sqlParamCount++;
					}
					sqlParamCountList.add(sqlParamCount);

				}
				IInputOptionListHandler inputOptionListHandler = PDOFactory
						.buildInputListHandler(inputList, pdoDaoFactory
								.getDataSourceLookup());

				if (pType.equalsIgnoreCase(TABLE_PDO_TYPE)) {

					patientSet = tablePdoQueryPatientDao.getPatientByFact(
							panelSqlList, sqlParamCountList,
							inputOptionListHandler, detailFlag, blobFlag,
							statusFlag);

				} else {
					patientDimensionSet = pdoQueryPatientDao.getPatientByFact(
							panelSqlList, sqlParamCountList,
							inputOptionListHandler, detailFlag, blobFlag,
							statusFlag);
				}

			} else {
				// if visit list get patient list from the visit list and pass
				// it to patient dimention
				// if patient list do direct query from patient dimension
				if (fromPatientSet) {
					PatientListType patientListType = inputList
							.getPatientList();

					if (pType.equalsIgnoreCase(TABLE_PDO_TYPE)) {
						patientSet = tablePdoQueryPatientDao
								.getPatientFromPatientSet(patientListType,
										detailFlag, blobFlag, statusFlag);
					} else {
						patientDimensionSet = pdoQueryPatientDao
								.getPatientFromPatientSet(patientListType,
										detailFlag, blobFlag, statusFlag);
					}
				} else if (fromPIDSet) {
					PidListType  pidListType = inputList.getPidList();
					List<String> patientList = new ArrayList<String>();
					
					for (Pid pids : pidListType.getPid())
					{
						patientList.add(pids.getValue());
						
						
					}
					
					
					if (pType.equalsIgnoreCase(TABLE_PDO_TYPE)) {
						patientSet = tablePdoQueryPatientDao
								.getPatientByPatientNum(patientList,
										detailFlag, blobFlag, statusFlag);
					} else {
						patientDimensionSet = pdoQueryPatientDao
								.getPatientByPatientNum(patientList,
										detailFlag, blobFlag, statusFlag);
					}
				} else if (fromVisitSet) {
					EventListType visitListType = inputList.getEventList();

					if (pType.equalsIgnoreCase(TABLE_PDO_TYPE)) {
						patientSet = tablePdoQueryPatientDao
								.getPatientFromVisitSet(visitListType,
										detailFlag, blobFlag, statusFlag);
					} else {
						patientDimensionSet = pdoQueryPatientDao
								.getPatientFromVisitSet(visitListType,
										detailFlag, blobFlag, statusFlag);
					}
				}
			}
		}

		public PatientSet getTablePatientSet() {
			return patientSet;
		}

		public PatientSet getPlainPatientSet() {
			return patientDimensionSet;
		}
	}

	private class VisitSection {
		boolean visitFromFact = false;
		boolean fromVisitSet = false;
		boolean fromPatientSet = false;
		IFactRelatedQueryHandler factRelatedQry = null;
		EventSet visitDimensionSet = null;
		EventSet eventSet = null;
		String pType = null;
		List<ParamType> visitMetaDataParamList = null;

		public VisitSection(String pType,
				IFactRelatedQueryHandler factRelatedQry, boolean visitFromFact,
				boolean fromVisitSet, boolean fromPatientSet,List<ParamType> visitMetaDataParamList) {
			this.factRelatedQry = factRelatedQry;
			this.visitFromFact = visitFromFact;
			this.fromVisitSet = fromVisitSet;
			this.fromPatientSet = fromPatientSet;
			this.pType = pType;
			this.visitMetaDataParamList = visitMetaDataParamList;
		}

		public void generateSet() throws Exception {
			IPdoQueryVisitDao pdoQueryVisitDao = pdoDaoFactory
					.getPdoQueryVisitDAO();
			pdoQueryVisitDao.setMetaDataParamList(visitMetaDataParamList);
			
			ITablePdoQueryVisitDao tablePdoQueryVisitDao = pdoDaoFactory
					.getTablePdoQueryVisitDAO();
			tablePdoQueryVisitDao.setMetaDataParamList(this.visitMetaDataParamList);

			// check if visit is in output option
			boolean detailFlag = visitFactRelated.isSelectDetail();

			// check if visit is in output option
			boolean blobFlag = visitFactRelated.isSelectBlob();

			// check if visit is in output option
			boolean statusFlag = visitFactRelated.isSelectStatus();

			if (visitFromFact) {
				List<String> visitFactList = factRelatedQry.getVisitFactList();

				List<String> panelSqlList = factRelatedQry.getPanelSqlList();

				String panelSql = null;

				List<Integer> sqlParamCountList = new ArrayList<Integer>();
				for (PanelType panel : filterList.getPanel()) {

					int sqlParamCount = panel.getItem().size();
					if (panel.getInvert() == 1) {
						sqlParamCount++;
					}
					sqlParamCountList.add(sqlParamCount);

				}
				IInputOptionListHandler inputOptionListHandler = PDOFactory
						.buildInputListHandler(inputList, pdoDaoFactory
								.getDataSourceLookup());

				if (pType.equalsIgnoreCase(TABLE_PDO_TYPE)) {

					eventSet = tablePdoQueryVisitDao.getVisitByFact(
							panelSqlList, sqlParamCountList,
							inputOptionListHandler, detailFlag, blobFlag,
							statusFlag);

				} else {
					visitDimensionSet = pdoQueryVisitDao.getVisitByFact(
							panelSqlList, sqlParamCountList,
							inputOptionListHandler, detailFlag, blobFlag,
							statusFlag);
				}

			} else {
				// check if input is visit or patient list
				if (fromVisitSet) {
					EventListType visitListType = inputList.getEventList();

					if (pType.equalsIgnoreCase(TABLE_PDO_TYPE)) {
						eventSet = tablePdoQueryVisitDao
								.getVisitDimensionSetFromVisitList(
										visitListType, detailFlag, blobFlag,
										statusFlag);
					} else {
						visitDimensionSet = pdoQueryVisitDao
								.getVisitDimensionSetFromVisitList(
										visitListType, detailFlag, blobFlag,
										statusFlag);
					}
				} else if (fromPatientSet) {
					PatientListType patientListType = inputList
							.getPatientList();

					if (pType.equalsIgnoreCase(TABLE_PDO_TYPE)) {
						eventSet = tablePdoQueryVisitDao
								.getVisitDimensionSetFromPatientList(
										patientListType, detailFlag, blobFlag,
										statusFlag);
					} else {
						visitDimensionSet = pdoQueryVisitDao
								.getVisitDimensionSetFromPatientList(
										patientListType, detailFlag, blobFlag,
										statusFlag);
					}
				}
			}
		}

		public EventSet getTableEventSet() {
			return eventSet;
		}

		public EventSet getPlainVisitSet() {
			return visitDimensionSet;
		}
	}
}
