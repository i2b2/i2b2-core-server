/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors: 
 *     Rajesh Kuttan
 */
package edu.harvard.i2b2.crc.loader.ejb.fr;

import java.rmi.RemoteException;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.crc.datavo.i2b2message.SecurityType;
import edu.harvard.i2b2.crc.loader.datavo.loader.query.DataListType;
import edu.harvard.i2b2.crc.loader.delegate.fr.CallFileRepositoryUtil;
import edu.harvard.i2b2.crc.loader.delegate.fr.FileRepositoryServiceClient;
import edu.harvard.i2b2.crc.loader.util.FileTransferUtil;

/**
 * This is the PDO Query ejb class. It passes pdo query request's to the
 * appropriate dao classes to get response in pdo format.
 * 
 * @ejb.bean description="FR bean" display-name="FR Bean"
 *           jndi-name="ejb.crc.loader.FRBean"
 *           local-jndi-name="ejb.crc.loader.FRBeanLocal"
 *           name="ejb.crc.loader.frbean" type="Stateless" view-type="both"
 *           transaction-type="Bean"
 * 
 * @ejb.interface remote-class="edu.harvard.i2b2.crc.loader.ejb.fr.FRBeanRemote"
 * 
 * @author rkuttan
 */
public class FRBean  {
	// RunQuery
	/** log **/
	protected final Log log = LogFactory.getLog(getClass());

	/**
	 * Function to get plain pdo from the given pdo request
	 * @throws AxisFault 
	 * @throws JAXBUtilException 
	 * 
	 * @ejb.interface-method view-type="both"
	 * @ejb.transaction type="Required"
	 */
	public String createClient(int uploadId, DataListType dataListType,
			SecurityType securityType, String projectId,
			String fileSystemDefaultStorageResource) throws I2B2Exception, AxisFault, JAXBUtilException {
		FileRepositoryServiceClient.getServiceClient();
		return createLocalFile(uploadId, dataListType, securityType, projectId,
				fileSystemDefaultStorageResource);
	}

	private String createLocalFile(int uploadId, DataListType dataListType,
			SecurityType securityType, String projectId,
			String fileSystemDefaultStorageResource) throws I2B2Exception, AxisFault, JAXBUtilException {
		String localUploadFile = null;
		String uploadRemoteFileProtocol = dataListType.getLocationUri()
				.getProtocolName();
		String uploadRemoteFileLocation = dataListType.getLocationUri()
				.getValue();
		if (uploadRemoteFileProtocol != null
				&& uploadRemoteFileProtocol.equalsIgnoreCase("irod")) {
			FileTransferUtil ftUtil = new FileTransferUtil();
			String localDestinationFolder = "/tmp";

			System.out.println("uploadRemoteFileLocation"
					+ uploadRemoteFileLocation);
			System.out.println("fileSystemDefaultStorageResource"
					+ fileSystemDefaultStorageResource);
			localUploadFile = ftUtil.getFile(uploadRemoteFileLocation,
					securityType.getPassword(),
					fileSystemDefaultStorageResource, localDestinationFolder);
		} else if (uploadRemoteFileProtocol != null
				&& uploadRemoteFileProtocol.equalsIgnoreCase("srb")) {
			FileTransferUtil ftUtil = new FileTransferUtil();
			String localDestinationFolder = "/tmp";
			System.out.println("uploadRemoteFileLocation"
					+ uploadRemoteFileLocation);
			System.out.println("fileSystemDefaultStorageResource"
					+ fileSystemDefaultStorageResource);
			localUploadFile = ftUtil.getFile(uploadRemoteFileLocation,
					securityType.getPassword(),
					fileSystemDefaultStorageResource, localDestinationFolder);

		} else if (uploadRemoteFileProtocol != null
				&& uploadRemoteFileProtocol.equalsIgnoreCase("fr")) {
			// call fr util
			CallFileRepositoryUtil fileRepUtil = new CallFileRepositoryUtil(
					securityType, projectId);

			localUploadFile = fileRepUtil.callFileRepository(uploadId,
					uploadRemoteFileLocation);

		} else {
			localUploadFile = uploadRemoteFileLocation;
		}
		return localUploadFile;
	}


}
