package edu.harvard.i2b2.crc.dao.setfinder.querybuilder;

import edu.harvard.i2b2.crc.datavo.ontology.ConceptType;
import edu.harvard.i2b2.crc.util.ItemKeyUtil;

public class ConceptTypeHandler {

	private String queryXML = null;
	StringBuffer ignoredItemMessageBuffer = new StringBuffer();

	public ConceptTypeHandler(String queryXML) {
		this.queryXML = queryXML;
	}

	public ConceptType getConceptType(String itemKey, String dbType)
			throws ConceptNotFoundException, OntologyException {

		ConceptType conceptType = null;

		// if patient list
		if (itemKey.toLowerCase().startsWith(ItemKeyUtil.ITEM_KEY_PATIENT_SET)) {
			String[] patientSetId = itemKey.split(":");
			if (patientSetId[1] != null) {
				conceptType = new ConceptType();
				conceptType.setColumnname(" result_instance_id ");
				conceptType.setOperator(" = ");
				conceptType.setFacttablecolumn(" patient_num ");
				conceptType.setTablename("qt_patient_set_collection ");
				conceptType.setDimcode(patientSetId[1]);
			}
			
		} else if (itemKey.toLowerCase().startsWith(ItemKeyUtil.ITEM_KEY_PATIENT_ENCOUNTER_SET)) { // if event
			String[] encounterSetId = itemKey.split(":");
			if (encounterSetId[1] != null) {
				conceptType = new ConceptType();
				conceptType.setColumnname(" result_instance_id ");
				conceptType.setOperator(" = ");
				conceptType.setFacttablecolumn(" encounter_num ");
				conceptType.setTablename("qt_patient_enc_collection  ");
				conceptType.setDimcode(encounterSetId[1]);
			}
		} else if (itemKey.toLowerCase().startsWith(ItemKeyUtil.ITEM_KEY_MASTERID)) {
			String[] masterId = itemKey.split(":");
			conceptType = new ConceptType();
			conceptType.setDimcode("'" + masterId[1] + "'");
			
		} else {
			ItemMetaDataHandler metadataHandler = new ItemMetaDataHandler(
					queryXML);
			conceptType = metadataHandler.getMetaDataFromOntologyCell(itemKey, dbType);

		}
		return conceptType;
	}

	public void handleConceptNotFoundException(ConceptNotFoundException e,
			int panelCount) {
		ignoredItemMessageBuffer
				.append(e.getMessage() + " panel#" + panelCount);
	}

	public String getIgnoredItem() {
		return ignoredItemMessageBuffer.toString();
	}
}
