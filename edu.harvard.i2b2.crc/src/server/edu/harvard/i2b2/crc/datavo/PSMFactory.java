/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors: 
 *     Rajesh Kuttan
 */
package edu.harvard.i2b2.crc.datavo;

import edu.harvard.i2b2.common.util.jaxb.DTOFactory;
import edu.harvard.i2b2.crc.datavo.db.QtQueryInstance;
import edu.harvard.i2b2.crc.datavo.db.QtQueryMaster;
import edu.harvard.i2b2.crc.datavo.db.QtQueryResultInstance;
import edu.harvard.i2b2.crc.datavo.db.QtQueryResultType;
import edu.harvard.i2b2.crc.datavo.db.QtQueryStatusType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryInstanceType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryMasterType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryResultInstanceType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryResultTypeType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryStatusTypeType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.RequestXmlType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.XmlValueType;

/**
 * Class to convert database domain objects to response message objects
 * 
 * @author rkuttan
 */
public class PSMFactory {
	private static DTOFactory dtoFactory = new DTOFactory();

	/**
	 * Returns QueryMasterType for the given QtQueryMaster
	 * 
	 * @param queryMaster
	 * @return QueryMasterType
	 */
	public static QueryMasterType buildQueryMasterType(QtQueryMaster queryMaster) {
		QueryMasterType queryMasterType = new QueryMasterType();

		queryMasterType.setUserId(queryMaster.getUserId());
		queryMasterType.setQueryMasterId(queryMaster.getQueryMasterId());

		RequestXmlType requestXmlType = new RequestXmlType();
		requestXmlType.getContent().add(queryMaster.getRequestXml());
		queryMasterType.setRequestXml(requestXmlType);
		queryMasterType.setName(queryMaster.getName());
		queryMasterType.setGroupId(queryMaster.getGroupId());

		DTOFactory dtoFactory = new DTOFactory();

		if (queryMaster.getCreateDate() != null) {
			queryMasterType.setCreateDate(dtoFactory
					.getXMLGregorianCalendar(queryMaster.getCreateDate()
							.getTime()));
		}

		if (queryMaster.getDeleteDate() != null) {
			queryMasterType.setDeleteDate(dtoFactory
					.getXMLGregorianCalendar(queryMaster.getDeleteDate()
							.getTime()));
		}

		return queryMasterType;
	}

	/**
	 * Returns QueryInstanceType for the given QtQueryInstance
	 * 
	 * @param queryInstance
	 * @return QueryInstanceType
	 */
	public static QueryInstanceType buildQueryInstanceType(
			QtQueryInstance queryInstance) {
		QueryInstanceType queryInstanceType = new QueryInstanceType();
		queryInstanceType.setUserId(queryInstance.getUserId());
		queryInstanceType.setQueryMasterId(queryInstance.getQtQueryMaster()
				.getQueryMasterId());
		queryInstanceType.setGroupId(queryInstance.getGroupId());
		queryInstanceType
				.setQueryInstanceId(queryInstance.getQueryInstanceId());
		queryInstanceType.setBatchMode(queryInstance.getBatchMode());
		if (queryInstance.getStartDate() != null) {
			queryInstanceType.setStartDate(dtoFactory
					.getXMLGregorianCalendar(queryInstance.getStartDate()
							.getTime()));
		}

		if (queryInstance.getEndDate() != null) {
			queryInstanceType.setEndDate(dtoFactory
					.getXMLGregorianCalendar(queryInstance.getEndDate()
							.getTime()));
		}

		QueryStatusTypeType queryStatusType = new QueryStatusTypeType();
		queryStatusType.setDescription(queryInstance.getQtQueryStatusType()
				.getDescription());
		queryStatusType.setName(queryInstance.getQtQueryStatusType().getName());
		queryStatusType.setStatusTypeId(String.valueOf(queryInstance
				.getQtQueryStatusType().getStatusTypeId()));
		queryInstanceType.setQueryStatusType(queryStatusType);
		if (queryInstance.getMessage() != null) {
			XmlValueType xmlValueType = new XmlValueType();
			xmlValueType.getContent().add(queryInstance.getMessage());
			queryInstanceType.setMessage(xmlValueType);

		}

		return queryInstanceType;
	}

	/**
	 * Returns QueryInstanceType for the given QtQueryInstance
	 * 
	 * @param queryInstance
	 * @return QueryInstanceType
	 */
	public static QueryResultInstanceType buildQueryResultInstanceType(
			QtQueryResultInstance queryResultInstance) {
		QueryResultInstanceType resultInstanceType = new QueryResultInstanceType();
		resultInstanceType.setResultInstanceId(queryResultInstance
				.getResultInstanceId());
		resultInstanceType.setQueryInstanceId(queryResultInstance
				.getQtQueryInstance().getQueryInstanceId());
		resultInstanceType.setDescription(queryResultInstance.getDescription());
		resultInstanceType
				.setQueryResultType(buildQueryResultType(queryResultInstance
						.getQtQueryResultType()));
		resultInstanceType
				.setQueryStatusType(buildQueryStatusType(queryResultInstance
						.getQtQueryStatusType()));
		resultInstanceType.setStartDate(dtoFactory
				.getXMLGregorianCalendar(queryResultInstance.getStartDate()
						.getTime()));
		if (queryResultInstance.getEndDate() != null) {
			resultInstanceType.setEndDate(dtoFactory
					.getXMLGregorianCalendar(queryResultInstance.getEndDate()
							.getTime()));
		}
		if (queryResultInstance.getSetSize() != null) {
			resultInstanceType.setSetSize(queryResultInstance.getSetSize());
		}
		resultInstanceType.setObfuscateMethod(queryResultInstance
				.getObfuscateMethod());
		if (queryResultInstance.getMessage() != null) {
			XmlValueType xmlValueType = new XmlValueType();
			xmlValueType.getContent().add(resultInstanceType.getMessage());
			resultInstanceType.setMessage(xmlValueType);
		}
		return resultInstanceType;
	}

	public static QueryResultTypeType buildQueryResultType(
			QtQueryResultType queryResult) {
		QueryResultTypeType queryResultType = new QueryResultTypeType();
		queryResultType.setResultTypeId(String.valueOf(queryResult
				.getResultTypeId()));
		queryResultType.setName(queryResult.getName());
		queryResultType.setDescription(queryResult.getDescription());
		queryResultType.setDisplayType(queryResult.getDisplayType());
		queryResultType.setVisualAttributeType(queryResult
				.getVisualAttributeType());
		return queryResultType;
	}

	public static QueryStatusTypeType buildQueryStatusType(
			QtQueryStatusType queryStatus) {
		QueryStatusTypeType queryStatusType = new QueryStatusTypeType();
		queryStatusType.setDescription(queryStatus.getDescription());
		queryStatusType.setName(queryStatus.getName());
		queryStatusType.setStatusTypeId(String.valueOf(queryStatus
				.getStatusTypeId()));
		return queryStatusType;
	}
}
