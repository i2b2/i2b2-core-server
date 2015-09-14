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

import java.io.IOException;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import edu.harvard.i2b2.common.util.db.JDBCUtil;
import edu.harvard.i2b2.common.util.jaxb.DTOFactory;
import edu.harvard.i2b2.crc.datavo.pdo.BlobType;
import edu.harvard.i2b2.crc.datavo.pdo.ConceptType;
import edu.harvard.i2b2.crc.datavo.pdo.EidType;
import edu.harvard.i2b2.crc.datavo.pdo.EventType;
import edu.harvard.i2b2.crc.datavo.pdo.ModifierType;
import edu.harvard.i2b2.crc.datavo.pdo.ObservationType;
import edu.harvard.i2b2.crc.datavo.pdo.ObserverType;
import edu.harvard.i2b2.crc.datavo.pdo.ParamType;
import edu.harvard.i2b2.crc.datavo.pdo.PatientIdType;
import edu.harvard.i2b2.crc.datavo.pdo.PatientType;
import edu.harvard.i2b2.crc.datavo.pdo.PidType;
import edu.harvard.i2b2.crc.datavo.pdo.PidType.PatientId;

/**
 * Class to build individual sections of table pdo xml like
 * patient,concept,observationfact from the given {@link java.sql.ResultSet}
 * $Id: RPDRPdoFactory.java,v 1.20 2009/11/14 16:41:26 rk903 Exp $
 * 
 * @author rkuttan
 */
public class RPDRPdoFactory {
	private static DTOFactory dtoFactory = new DTOFactory();

	/**
	 * Inner class to build observation fact in table PDO format
	 */
	public static class ObservationFactBuilder {
		boolean obsFactDetailFlag = false;
		boolean obsFactBlobFlag = false;
		boolean obsFactStatusFlag = false;

		public ObservationFactBuilder(boolean detailFlag, boolean blobFlag,
				boolean statusFlag) {
			this.obsFactDetailFlag = detailFlag;
			this.obsFactBlobFlag = blobFlag;
			this.obsFactStatusFlag = statusFlag;
		}

		/**
		 * Read one record from resultset and build observation fact
		 * 
		 * @param rowSet
		 * @param source
		 * @return ObservationSet.Observation
		 * @throws SQLException
		 * @throws IOException
		 */
		public ObservationType buildObservationSet(ResultSet rowSet,
				String source) throws SQLException, IOException {

			ObservationType observation = new ObservationType();
			PatientIdType pId = new PatientIdType();
			pId.setValue(rowSet.getString("obs_patient_num"));
			pId.setSource(source);
			observation.setPatientId(pId);

			ObservationType.EventId eventId = new ObservationType.EventId();
			eventId.setValue(rowSet.getString("obs_encounter_num"));
			eventId.setSource(source);
			observation.setEventId(eventId);

			ObservationType.ConceptCd conceptCd = new ObservationType.ConceptCd();
			conceptCd.setValue(rowSet.getString("obs_concept_cd"));
			conceptCd.setName(rowSet.getString("concept_name"));
			observation.setConceptCd(conceptCd);

			ObservationType.ModifierCd modifierCd = new ObservationType.ModifierCd();
			modifierCd.setValue(rowSet.getString("obs_modifier_cd"));
			modifierCd.setName(rowSet.getString("modifier_name"));
			observation.setModifierCd(modifierCd);

			ObservationType.InstanceNum instanceNum = new ObservationType.InstanceNum();
			instanceNum.setValue(rowSet.getString("obs_instance_num"));
			observation.setInstanceNum(instanceNum);

			Date startDate = rowSet.getTimestamp("obs_start_date");

			if (startDate != null) {
				observation.setStartDate(dtoFactory
						.getXMLGregorianCalendar(startDate.getTime()));
			}

			ObservationType.ObserverCd observerCd = new ObservationType.ObserverCd();
			observerCd.setValue(rowSet.getString("obs_provider_id"));
			String providerName = rowSet.getString("provider_name");
			observerCd.setName((providerName != null) ? providerName : "");
			observation.setObserverCd(observerCd);
			if (obsFactDetailFlag) {
				Date endDate = rowSet.getTimestamp("obs_end_date");

				if (endDate != null) {
					observation.setEndDate(dtoFactory
							.getXMLGregorianCalendar(endDate.getTime()));
				}

				observation.setValuetypeCd(rowSet.getString("obs_valtype_cd"));
				observation.setTvalChar(rowSet.getString("obs_tval_char"));

				ObservationType.NvalNum nvalNum = new ObservationType.NvalNum();
				nvalNum.setValue(rowSet.getBigDecimal("obs_nval_num"));
				observation.setNvalNum(nvalNum);

				ObservationType.ValueflagCd valueFlagCd = new ObservationType.ValueflagCd();
				valueFlagCd.setValue(rowSet.getString("obs_valueflag_cd"));
				observation.setValueflagCd(valueFlagCd);

				observation.setQuantityNum(rowSet
						.getBigDecimal("obs_quantity_num"));

				observation.setUnitsCd(rowSet.getString("obs_units_cd"));

				if (rowSet.getString("obs_location_cd") != null) {
					ObservationType.LocationCd locationCd = new ObservationType.LocationCd();
					locationCd.setValue(rowSet.getString("obs_location_cd"));
					String locationName = rowSet.getString("location_name");
					locationCd.setName((locationName != null) ? locationName
							: "");
					observation.setLocationCd(locationCd);
				}

				observation.setConfidenceNum(rowSet
						.getBigDecimal("obs_confidence_num"));

			}

			if (obsFactBlobFlag) {
				Clob observationClob = rowSet.getClob("obs_observation_blob");
				if (observationClob != null) {
					try {
						BlobType blobType = new BlobType();
						blobType.getContent().add(
								JDBCUtil.getClobStringWithLinebreak(observationClob));
						observation.setObservationBlob(blobType);
					} catch (IOException ioEx) {
						ioEx.printStackTrace();
						throw ioEx;
					}
				}
			}

			if (obsFactStatusFlag) {
				if (rowSet.getTimestamp("obs_update_date") != null) {
					observation.setUpdateDate(dtoFactory
							.getXMLGregorianCalendar(rowSet.getTimestamp(
									"obs_update_date").getTime()));
				}

				if (rowSet.getDate("obs_download_date") != null) {
					observation.setDownloadDate(dtoFactory
							.getXMLGregorianCalendar(rowSet.getTimestamp(
									"obs_download_date").getTime()));
				}

				if (rowSet.getDate("obs_import_date") != null) {
					observation.setImportDate(dtoFactory
							.getXMLGregorianCalendar(rowSet.getTimestamp(
									"obs_import_date").getTime()));
				}

				observation.setSourcesystemCd(rowSet
						.getString("obs_sourcesystem_cd"));
				observation.setUploadId(rowSet.getString("obs_upload_id"));
			}

			return observation;
		}
	}

	/*
	 * Inner class to build patient in table PDO format
	 */
	public static class PatientBuilder {
		boolean patientDetailFlag = false;
		boolean patientBlobFlag = false;
		boolean patientStatusFlag = false;

		public PatientBuilder(boolean detailFlag, boolean blobFlag,
				boolean statusFlag) {
			this.patientDetailFlag = detailFlag;
			this.patientBlobFlag = blobFlag;
			this.patientStatusFlag = statusFlag;
		}

		/**
		 * Read one record from resultset and build patient set
		 * 
		 * @param rowSet
		 * @param source
		 * @return PatientSet.Patient
		 * @throws SQLException
		 * @throws IOException
		 */
		public PatientType buildPatientSet(ResultSet rowSet, String source,List<ParamType> metaDataParamList)
				throws SQLException, IOException {
			PatientType patientDimensionType = new PatientType();
			PatientIdType patientIdType = new PatientIdType();
			patientIdType.setSource(source);
			patientIdType.setValue(rowSet.getString("patient_patient_num"));
			patientDimensionType.setPatientId(patientIdType);

			if (patientDetailFlag) {

				

				

				ParamTypeValueBuilder paramValBuilder = new ParamTypeValueBuilder();
				for (Iterator<ParamType> metaParamIterator = metaDataParamList.iterator(); metaParamIterator.hasNext();) { 
					ParamType metaParamType = metaParamIterator.next();
					if (metaParamType.getType().equalsIgnoreCase("string")) { 
						patientDimensionType.getParam().add(paramValBuilder.buildParamType(metaParamType,"patient_","_name",rowSet));	
					} else { 
						patientDimensionType.getParam().add(paramValBuilder.buildParamType(metaParamType,"patient_",null,rowSet));
					}
					
					
				}
			}

			if (patientBlobFlag) {
				if (rowSet.getClob("patient_patient_blob") != null) {
					BlobType blobType = new BlobType();
					blobType.getContent().add(
							JDBCUtil.getClobStringWithLinebreak(rowSet
									.getClob("patient_patient_blob")));
				}
			}

			if (patientStatusFlag) {
				if (rowSet.getTimestamp("patient_update_date") != null) {
					patientDimensionType.setUpdateDate(dtoFactory
							.getXMLGregorianCalendar(rowSet.getTimestamp(
									"patient_update_date").getTime()));
				}

				if (rowSet.getTimestamp("patient_download_date") != null) {
					patientDimensionType.setDownloadDate(dtoFactory
							.getXMLGregorianCalendar(rowSet.getTimestamp(
									"patient_download_date").getTime()));
				}

				if (rowSet.getTimestamp("patient_import_date") != null) {
					patientDimensionType.setImportDate(dtoFactory
							.getXMLGregorianCalendar(rowSet.getTimestamp(
									"patient_import_date").getTime()));
				}

				patientDimensionType.setSourcesystemCd(rowSet
						.getString("patient_sourcesystem_cd"));
				patientDimensionType.setUploadId(rowSet
						.getString("patient_upload_id"));
			}

			return patientDimensionType;
		}
	}

	/*
	 * Inner class to build observer section in table PDO format
	 */
	public static class ProviderBuilder {
		boolean providerDetailFlag = false;
		boolean providerBlobFlag = false;
		boolean providerStatusFlag = false;

		public ProviderBuilder(boolean detailFlag, boolean blobFlag,
				boolean statusFlag) {
			this.providerDetailFlag = detailFlag;
			this.providerBlobFlag = blobFlag;
			this.providerStatusFlag = statusFlag;
		}

		/**
		 * Read one record from resultset and build observer set
		 * 
		 * @param rowSet
		 * @param source
		 * @return ObserverSet.Observer
		 * @throws SQLException
		 * @throws IOException
		 */
		public ObserverType buildObserverSet(ResultSet rowSet)
				throws IOException, SQLException {
			ObserverType providerDimensionType = new ObserverType();
			providerDimensionType.setObserverCd(rowSet
					.getString("provider_provider_id"));
			providerDimensionType.setObserverPath(rowSet
					.getString("provider_provider_path"));

			if (providerDetailFlag) {
				providerDimensionType.setNameChar(rowSet
						.getString("provider_name_char"));
			}

			if (providerBlobFlag) {
				Clob providerClob = rowSet.getClob("provider_provider_blob");

				if (providerClob != null) {
					BlobType blobType = new BlobType();
					blobType.getContent().add(
							JDBCUtil.getClobStringWithLinebreak(providerClob));
					providerDimensionType.setObserverBlob(blobType);
				}
			}

			if (providerStatusFlag) {
				if (rowSet.getTimestamp("provider_update_date") != null) {
					providerDimensionType.setUpdateDate(dtoFactory
							.getXMLGregorianCalendar(rowSet.getTimestamp(
									"provider_update_date").getTime()));
				}

				if (rowSet.getTimestamp("provider_download_date") != null) {
					providerDimensionType.setDownloadDate(dtoFactory
							.getXMLGregorianCalendar(rowSet.getTimestamp(
									"provider_download_date").getTime()));
				}

				if (rowSet.getTimestamp("provider_import_date") != null) {
					providerDimensionType.setImportDate(dtoFactory
							.getXMLGregorianCalendar(rowSet.getTimestamp(
									"provider_import_date").getTime()));
				}

				providerDimensionType.setSourcesystemCd(rowSet
						.getString("provider_sourcesystem_cd"));
				providerDimensionType.setUploadId(rowSet
						.getString("provider_upload_id"));

			}

			return providerDimensionType;
		}
	}

	/*
	 * Inner class to build concept section in table PDO format
	 */
	public static class ConceptBuilder {
		boolean conceptDetailFlag = false;
		boolean conceptBlobFlag = false;
		boolean conceptStatusFlag = false;

		public ConceptBuilder(boolean detailFlag, boolean blobFlag,
				boolean statusFlag) {
			this.conceptDetailFlag = detailFlag;
			this.conceptBlobFlag = blobFlag;
			this.conceptStatusFlag = statusFlag;
		}

		/**
		 * Read one record from resultset and build concept set
		 * 
		 * @param rowSet
		 * @param source
		 * @return ConceptSet.Concept
		 * @throws SQLException
		 * @throws IOException
		 */
		public ConceptType buildConceptSet(ResultSet rowSet)
				throws SQLException, IOException {
			ConceptType conceptDimensionType = new ConceptType();

			conceptDimensionType.setConceptCd(rowSet
					.getString("concept_concept_cd"));

			if (conceptDetailFlag) {
				conceptDimensionType.setConceptPath(rowSet
						.getString("concept_concept_path"));
				conceptDimensionType.setNameChar(rowSet
						.getString("concept_name_char"));
			}

			if (conceptBlobFlag) {
				Clob conceptClob = rowSet.getClob("concept_concept_blob");

				if (conceptClob != null) {
					BlobType blobType = new BlobType();
					blobType.getContent().add(
							JDBCUtil.getClobStringWithLinebreak(conceptClob));
					conceptDimensionType.setConceptBlob(blobType);
				}
			}

			if (conceptStatusFlag) {
				if (rowSet.getTimestamp("concept_update_date") != null) {
					conceptDimensionType.setUpdateDate(dtoFactory
							.getXMLGregorianCalendar(rowSet.getTimestamp(
									"concept_update_date").getTime()));
				}

				if (rowSet.getTimestamp("concept_download_date") != null) {
					conceptDimensionType.setDownloadDate(dtoFactory
							.getXMLGregorianCalendar(rowSet.getTimestamp(
									"concept_download_date").getTime()));
				}

				if (rowSet.getTimestamp("concept_import_date") != null) {
					conceptDimensionType.setImportDate(dtoFactory
							.getXMLGregorianCalendar(rowSet.getTimestamp(
									"concept_import_date").getTime()));
				}

				conceptDimensionType.setSourcesystemCd(rowSet
						.getString("concept_sourcesystem_cd"));

				conceptDimensionType.setUploadId(rowSet
						.getString("concept_upload_id"));
			}

			return conceptDimensionType;
		}
	}

	/*
	 * Inner class to build concept section in table PDO format
	 */
	public static class ModifierBuilder {
		boolean modifierDetailFlag = false;
		boolean modifierBlobFlag = false;
		boolean modifierStatusFlag = false;

		public ModifierBuilder(boolean detailFlag, boolean blobFlag,
				boolean statusFlag) {
			this.modifierDetailFlag = detailFlag;
			this.modifierBlobFlag = blobFlag;
			this.modifierStatusFlag = statusFlag;
		}

		/**
		 * Read one record from resultset and build concept set
		 * 
		 * @param rowSet
		 * @param source
		 * @return ConceptSet.Concept
		 * @throws SQLException
		 * @throws IOException
		 */
		public ModifierType buildModifierSet(ResultSet rowSet)
				throws SQLException, IOException {
			ModifierType modifierDimensionType = new ModifierType();

			modifierDimensionType.setModifierCd(rowSet
					.getString("modifier_modifier_cd"));

			if (modifierDetailFlag) {
				modifierDimensionType.setModifierPath(rowSet
						.getString("modifier_modifier_path"));
				modifierDimensionType.setNameChar(rowSet
						.getString("modifier_name_char"));
			}

			if (modifierBlobFlag) {
				Clob modifierClob = rowSet.getClob("modifier_modifier_blob");

				if (modifierClob != null) {
					BlobType blobType = new BlobType();
					blobType.getContent().add(
							JDBCUtil.getClobStringWithLinebreak(modifierClob));
					modifierDimensionType.setModifierBlob(blobType);
				}
			}

			if (modifierStatusFlag) {
				if (rowSet.getTimestamp("modifier_update_date") != null) {
					modifierDimensionType.setUpdateDate(dtoFactory
							.getXMLGregorianCalendar(rowSet.getTimestamp(
									"modifier_update_date").getTime()));
				}

				if (rowSet.getTimestamp("modifier_download_date") != null) {
					modifierDimensionType.setDownloadDate(dtoFactory
							.getXMLGregorianCalendar(rowSet.getTimestamp(
									"modifier_download_date").getTime()));
				}

				if (rowSet.getTimestamp("modifier_import_date") != null) {
					modifierDimensionType.setImportDate(dtoFactory
							.getXMLGregorianCalendar(rowSet.getTimestamp(
									"modifier_import_date").getTime()));
				}

				modifierDimensionType.setSourcesystemCd(rowSet
						.getString("modifier_sourcesystem_cd"));

				modifierDimensionType.setUploadId(rowSet
						.getString("modifier_upload_id"));
			}

			return modifierDimensionType;
		}
	}
	/*
	 * Inner class to build event section in table PDO format
	 */
	public static class EventBuilder {
		boolean eventDetailFlag = false;
		boolean eventBlobFlag = false;
		boolean eventStatusFlag = false;

		public EventBuilder(boolean detailFlag, boolean blobFlag,
				boolean statusFlag) {
			this.eventDetailFlag = detailFlag;
			this.eventBlobFlag = blobFlag;
			this.eventStatusFlag = statusFlag;
		}

		/**
		 * Read one record from resultset and build event set
		 * 
		 * @param rowSet
		 * @param source
		 * @return EventSet.Event
		 * @throws SQLException
		 * @throws IOException
		 */
		public EventType buildEventSet(ResultSet rowSet, String source,List<ParamType> metaDataParamList)
				throws SQLException, IOException {
			EventType visitDimensionType = new EventType();

			PatientIdType patientIdType = new PatientIdType();
			patientIdType.setValue(rowSet.getString("visit_patient_num"));
			patientIdType.setSource(source);
			visitDimensionType.setPatientId(patientIdType);

			EventType.EventId eventId = new EventType.EventId();
			eventId.setValue(rowSet.getString("visit_encounter_num"));
			eventId.setSource(source);
			visitDimensionType.setEventId(eventId);
			ParamTypeValueBuilder paramValBuilder = new ParamTypeValueBuilder();
			
			if (eventDetailFlag) {
				for (Iterator<ParamType> metaParamIterator = metaDataParamList.iterator(); metaParamIterator.hasNext();) { 
					ParamType metaParamType = metaParamIterator.next();
					
					if (metaParamType.getType().equalsIgnoreCase("string")) {
						visitDimensionType.getParam().add(paramValBuilder.buildParamType(metaParamType,"visit_","_name",rowSet));
					} else { 
						visitDimensionType.getParam().add(paramValBuilder.buildParamType(metaParamType,"visit_",null,rowSet));
					}
				}

				Date startDate = rowSet.getTimestamp("visit_start_date");

				if (startDate != null) {
					visitDimensionType.setStartDate(dtoFactory
							.getXMLGregorianCalendar(startDate.getTime()));
				}

				Date endDate = rowSet.getTimestamp("visit_end_date");

				if (endDate != null) {
					visitDimensionType.setEndDate(dtoFactory
							.getXMLGregorianCalendar(endDate.getTime()));
				}
			}

			if (eventBlobFlag) {
				Clob visitClob = rowSet.getClob("visit_visit_blob");

				if (visitClob != null) {
					BlobType blobType = new BlobType();
					blobType.getContent()
							.add(JDBCUtil.getClobStringWithLinebreak(visitClob));
					visitDimensionType.setEventBlob(blobType);
				}
			}

			if (eventStatusFlag) {
				if (rowSet.getTimestamp("visit_update_date") != null) {
					visitDimensionType.setUpdateDate(dtoFactory
							.getXMLGregorianCalendar(rowSet.getTimestamp(
									"visit_update_date").getTime()));
				}

				if (rowSet.getTimestamp("visit_download_date") != null) {
					visitDimensionType.setDownloadDate(dtoFactory
							.getXMLGregorianCalendar(rowSet.getTimestamp(
									"visit_download_date").getTime()));
				}

				if (rowSet.getTimestamp("visit_import_date") != null) {
					visitDimensionType.setImportDate(dtoFactory
							.getXMLGregorianCalendar(rowSet.getTimestamp(
									"visit_import_date").getTime()));
				}

				visitDimensionType.setSourcesystemCd(rowSet
						.getString("visit_sourcesystem_cd"));

				visitDimensionType.setUploadId(rowSet
						.getString("visit_upload_id"));
			}

			return visitDimensionType;
		}
	}

	/*
	 * Inner class to build pid section in table PDO format
	 */
	public static class PidBuilder {
		boolean pmDetailFlag = false;
		boolean pmBlobFlag = false;
		boolean pmStatusFlag = false;

		public PidBuilder(boolean detailFlag, boolean blobFlag,
				boolean statusFlag) {
			this.pmDetailFlag = detailFlag;
			this.pmBlobFlag = blobFlag;
			this.pmStatusFlag = statusFlag;
		}

		/**
		 * Read one record from resultset and build concept set
		 * 
		 * @param rowSet
		 * @param source
		 * @return ConceptSet.Concept
		 * @throws SQLException
		 * @throws IOException
		 */
		public PidType buildPidSet(ResultSet rowSet) throws SQLException,
				IOException {
			PidType.PatientMapId patientMapType = new PidType.PatientMapId();
			patientMapType.setValue(rowSet.getString("pm_patient_ide"));
			patientMapType.setSource(rowSet.getString("pm_patient_ide_source"));

			PatientId patientId = new PatientId();
			patientId.setValue(rowSet.getString("pm_patient_num"));

			PidType pidType = new PidType();
			pidType.setPatientId(patientId);
			pidType.getPatientMapId().add(patientMapType);
			// patientMapType.setValue(rowSet.getString("pm_patient_num"));

			if (pmDetailFlag) {
				patientMapType.setStatus(rowSet
						.getString("pm_patient_ide_status"));
			}

			if (pmBlobFlag) {
				; // no blob field in the mapping table
			}

			if (pmStatusFlag) {
				if (rowSet.getTimestamp("pm_update_date") != null) {
					patientMapType.setUpdateDate(dtoFactory
							.getXMLGregorianCalendar(rowSet.getTimestamp(
									"pm_update_date").getTime()));
				}

				if (rowSet.getTimestamp("pm_download_date") != null) {
					patientMapType.setDownloadDate(dtoFactory
							.getXMLGregorianCalendar(rowSet.getTimestamp(
									"pm_download_date").getTime()));
				}

				if (rowSet.getTimestamp("pm_import_date") != null) {
					patientMapType.setImportDate(dtoFactory
							.getXMLGregorianCalendar(rowSet.getTimestamp(
									"pm_import_date").getTime()));
				}

				patientMapType.setSourcesystemCd(rowSet
						.getString("pm_sourcesystem_cd"));
				patientMapType.setUploadId(rowSet.getString("pm_upload_id"));

			}

			return pidType;
		}
	}

	/*
	 * Inner class to build pid section in table PDO format
	 */
	public static class EidBuilder {
		boolean pmDetailFlag = false;
		boolean pmBlobFlag = false;
		boolean pmStatusFlag = false;

		public EidBuilder(boolean detailFlag, boolean blobFlag,
				boolean statusFlag) {
			this.pmDetailFlag = detailFlag;
			this.pmBlobFlag = blobFlag;
			this.pmStatusFlag = statusFlag;
		}

		/**
		 * Read one record from resultset and build concept set
		 * 
		 * @param rowSet
		 * @param source
		 * @return ConceptSet.Concept
		 * @throws SQLException
		 * @throws IOException
		 */
		public EidType buildEidSet(ResultSet rowSet) throws SQLException,
				IOException {
			EidType.EventMapId eventMapType = new EidType.EventMapId();
			eventMapType.setValue(rowSet.getString("em_encounter_ide"));
			eventMapType.setSource(rowSet.getString("em_encounter_ide_source"));
			eventMapType.setPatientId(rowSet.getString("em_patient_ide"));
			eventMapType.setPatientIdSource(rowSet
					.getString("em_patient_ide_source"));

			EidType.EventId eventId = new EidType.EventId();
			eventId.setValue(rowSet.getString("em_encounter_num"));

			EidType eidType = new EidType();
			eidType.setEventId(eventId);
			eidType.getEventMapId().add(eventMapType);
			// patientMapType.setValue(rowSet.getString("pm_patient_num"));

			if (pmDetailFlag) {
				eventMapType.setStatus(rowSet
						.getString("em_encounter_ide_status"));
				eventId.setStatus(eventMapType.getStatus());
			}

			if (pmBlobFlag) {
				; // no blob field in the mapping table
			}

			if (pmStatusFlag) {
				if (rowSet.getTimestamp("em_update_date") != null) {
					eventMapType.setUpdateDate(dtoFactory
							.getXMLGregorianCalendar(rowSet.getTimestamp(
									"em_update_date").getTime()));
					eventId.setUpdateDate(eventMapType.getUpdateDate());
				}

				if (rowSet.getTimestamp("em_download_date") != null) {
					eventMapType.setDownloadDate(dtoFactory
							.getXMLGregorianCalendar(rowSet.getTimestamp(
									"em_download_date").getTime()));
					eventId.setDownloadDate(eventMapType.getDownloadDate());
				}

				if (rowSet.getTimestamp("em_import_date") != null) {
					eventMapType.setImportDate(dtoFactory
							.getXMLGregorianCalendar(rowSet.getTimestamp(
									"em_import_date").getTime()));
					eventId.setImportDate(eventMapType.getImportDate());
				}

				eventMapType.setSourcesystemCd(rowSet
						.getString("em_sourcesystem_cd"));
				eventId.setSourcesystemCd(eventMapType.getSourcesystemCd());

				eventMapType.setUploadId(rowSet.getString("em_upload_id"));
				eventId.setUploadId(eventMapType.getUploadId());
			}

			// 

			return eidType;
		}
	}
}
