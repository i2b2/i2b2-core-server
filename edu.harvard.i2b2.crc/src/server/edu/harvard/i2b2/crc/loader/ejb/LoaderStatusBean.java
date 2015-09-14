package edu.harvard.i2b2.crc.loader.ejb;

import java.util.List;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.jaxb.DTOFactory;
import edu.harvard.i2b2.crc.loader.dao.ILoaderDAOFactory;
import edu.harvard.i2b2.crc.loader.dao.IUploaderDAOFactory;
import edu.harvard.i2b2.crc.loader.dao.LoaderDAOFactoryHelper;
import edu.harvard.i2b2.crc.loader.dao.UploadStatusDAOI;
import edu.harvard.i2b2.crc.loader.datavo.loader.DataSourceLookup;
import edu.harvard.i2b2.crc.loader.datavo.loader.UploadSetStatus;
import edu.harvard.i2b2.crc.loader.datavo.loader.UploadStatus;
import edu.harvard.i2b2.crc.loader.datavo.loader.query.LoadDataListResponseType;
import edu.harvard.i2b2.crc.loader.datavo.loader.query.LoadDataResponseType;
import edu.harvard.i2b2.crc.loader.datavo.loader.query.SetStatusType;
import edu.harvard.i2b2.crc.loader.datavo.loader.query.LoadDataResponseType.DataFileLocationUri;

public class LoaderStatusBean implements LoaderStatusBeanLocal,
		LoaderStatusBeanRemote {
	private DTOFactory dtoFactory = new DTOFactory();

	public LoadDataListResponseType getLoadDataResponseByUserId(
			DataSourceLookup dataSourceLookup, String userId)
			throws I2B2Exception {
		LoadDataListResponseType responseList = new LoadDataListResponseType();
		LoaderDAOFactoryHelper daoHelper = new LoaderDAOFactoryHelper(
				dataSourceLookup.getDomainId(), dataSourceLookup
						.getProjectPath(), dataSourceLookup.getOwnerId());
		ILoaderDAOFactory loaderDaoFactory = daoHelper.getDAOFactory();
		IUploaderDAOFactory uploaderDaoFactory = loaderDaoFactory
				.getUpLoaderDAOFactory();
		UploadStatusDAOI statusDao = uploaderDaoFactory.getUploadStatusDAO();
		List<UploadStatus> uploadStatusList = statusDao
				.getUpoadStatusByUser(userId);
		for (UploadStatus uploadStatus : uploadStatusList) {
			LoadDataResponseType loadDataResponse = new LoadDataResponseType();
			DataFileLocationUri fileLoc = new DataFileLocationUri();
			fileLoc.setValue(uploadStatus.getInputFileName());

			loadDataResponse.setDataFileLocationUri(fileLoc);
			loadDataResponse.setLoadStatus(uploadStatus.getLoadStatus());
			loadDataResponse.setUploadId(String.valueOf(uploadStatus
					.getUploadId()));
			loadDataResponse.setUserId(uploadStatus.getUserId());
			loadDataResponse
					.setTransformerName(uploadStatus.getTransformName());
			loadDataResponse.setStartDate(dtoFactory
					.getXMLGregorianCalendar(uploadStatus.getLoadDate()
							.getTime()));
			if (uploadStatus.getEndDate() != null) {
				loadDataResponse.setEndDate(dtoFactory
						.getXMLGregorianCalendar(uploadStatus.getEndDate()
								.getTime()));
			}

			loadDataResponse.setMessage(uploadStatus.getMessage());
			List<UploadSetStatus> setStatusList = statusDao
					.getUploadSetStatusByLoadId(uploadStatus.getUploadId());
			for (UploadSetStatus setStatus : setStatusList) {
				SetStatusType responseSetStatusType = new SetStatusType();
				responseSetStatusType.setIgnoredRecord(setStatus
						.getNoOfRecord()
						- setStatus.getLoadedRecord());
				responseSetStatusType.setInsertedRecord(setStatus
						.getLoadedRecord());
				responseSetStatusType.setTotalRecord(setStatus.getNoOfRecord());
				responseSetStatusType.setMessage(setStatus.getMessage());
				if (setStatus.getSetTypeId() == 1) {
					loadDataResponse.setEventSet(responseSetStatusType);
				} else if (setStatus.getSetTypeId() == 2) {
					loadDataResponse.setPatientSet(responseSetStatusType);
				} else if (setStatus.getSetTypeId() == 3) {
					loadDataResponse.setConceptSet(responseSetStatusType);
				} else if (setStatus.getSetTypeId() == 4) {
					loadDataResponse.setObserverSet(responseSetStatusType);
				} else if (setStatus.getSetTypeId() == 5) {
					loadDataResponse.setObservationSet(responseSetStatusType);
				} else if (setStatus.getSetTypeId() == 6) {
					loadDataResponse.setPidSet(responseSetStatusType);
				} else if (setStatus.getSetTypeId() == 7) {
					loadDataResponse.setEventidSet(responseSetStatusType);
				} else if (setStatus.getSetTypeId() == 8) {
					loadDataResponse.setModifierSet(responseSetStatusType);
				}
			}
			responseList.getLoadDataResponse().add(loadDataResponse);
		}
		return responseList;
	}

	public LoadDataResponseType getLoadDataResponseByUploadId(
			DataSourceLookup dataSourceLookup, int uploadId)
			throws I2B2Exception {
		LoadDataResponseType loadDataResponse = new LoadDataResponseType();
		LoaderDAOFactoryHelper daoHelper = new LoaderDAOFactoryHelper(
				dataSourceLookup.getDomainId(), dataSourceLookup
						.getProjectPath(), dataSourceLookup.getOwnerId());
		ILoaderDAOFactory loaderDaoFactory = daoHelper.getDAOFactory();
		IUploaderDAOFactory uploaderDaoFactory = loaderDaoFactory
				.getUpLoaderDAOFactory();

		UploadStatusDAOI statusDao = uploaderDaoFactory.getUploadStatusDAO();

		UploadStatus uploadStatus = statusDao.findById(uploadId);
		if (uploadStatus != null) {
			DataFileLocationUri fileLoc = new DataFileLocationUri();
			fileLoc.setValue(uploadStatus.getInputFileName());

			loadDataResponse.setDataFileLocationUri(fileLoc);
			loadDataResponse.setLoadStatus(uploadStatus.getLoadStatus());
			loadDataResponse.setUploadId(String.valueOf(uploadStatus
					.getUploadId()));
			loadDataResponse.setUserId(uploadStatus.getUserId());
			loadDataResponse
					.setTransformerName(uploadStatus.getTransformName());
			loadDataResponse.setStartDate(dtoFactory
					.getXMLGregorianCalendar(uploadStatus.getLoadDate()
							.getTime()));
			if (uploadStatus.getEndDate() != null) {
				loadDataResponse.setEndDate(dtoFactory
						.getXMLGregorianCalendar(uploadStatus.getEndDate()
								.getTime()));
			}
			loadDataResponse.setMessage(uploadStatus.getMessage());

			List<UploadSetStatus> setStatusList = statusDao
					.getUploadSetStatusByLoadId(uploadStatus.getUploadId());
			for (UploadSetStatus setStatus : setStatusList) {
				SetStatusType responseSetStatusType = new SetStatusType();
				responseSetStatusType.setIgnoredRecord(setStatus
						.getNoOfRecord()
						- setStatus.getLoadedRecord());
				responseSetStatusType.setInsertedRecord(setStatus
						.getLoadedRecord());
				responseSetStatusType.setMessage(setStatus.getMessage());
				responseSetStatusType.setTotalRecord(setStatus.getNoOfRecord());
				if (setStatus.getSetTypeId() == 1) {
					loadDataResponse.setEventSet(responseSetStatusType);
				} else if (setStatus.getSetTypeId() == 2) {
					loadDataResponse.setPatientSet(responseSetStatusType);
				} else if (setStatus.getSetTypeId() == 3) {
					loadDataResponse.setConceptSet(responseSetStatusType);
				} else if (setStatus.getSetTypeId() == 4) {
					loadDataResponse.setObserverSet(responseSetStatusType);
				} else if (setStatus.getSetTypeId() == 5) {
					loadDataResponse.setObservationSet(responseSetStatusType);
				} else if (setStatus.getSetTypeId() == 6) {
					loadDataResponse.setPidSet(responseSetStatusType);
				} else if (setStatus.getSetTypeId() == 7) {
					loadDataResponse.setEventidSet(responseSetStatusType);
				} else if (setStatus.getSetTypeId() == 8) {
					loadDataResponse.setModifierSet(responseSetStatusType);
				}
			}
		}
		return loadDataResponse;

	}
}
