/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
package edu.harvard.i2b2.crc.loader.util;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.crc.datavo.i2b2message.PasswordType;
/*
import edu.sdsc.grid.io.GeneralFile;
import edu.sdsc.grid.io.irods.IRODSAccount;
import edu.sdsc.grid.io.irods.IRODSFile;
import edu.sdsc.grid.io.irods.IRODSFileSystem;
import edu.sdsc.grid.io.local.LocalFile;
*/

public class FileTransferUtil {
	private final static String STORAGE_RESOURCE = "demoResc";

	public String getFile(String uriLoc, PasswordType password,String defaultStorageResource,
			String localDestinationFolder) throws I2B2Exception {

		String username = uriLoc.substring(uriLoc.indexOf('/') + 2, uriLoc
				.indexOf('.'));
		String mdas = uriLoc.substring(uriLoc.indexOf('.') + 1, uriLoc
				.indexOf('@'));
		String host = uriLoc.substring(uriLoc.indexOf('@') + 1, uriLoc.indexOf(
				':', 5));
		int port = Integer.parseInt(uriLoc.substring(
				uriLoc.indexOf(':', 5) + 1, uriLoc.indexOf('/', 7)));
		String file = uriLoc.substring(uriLoc.indexOf('/', 7));
		String localTransferedFile = null;

		/*
		IRODSAccount irodsAccount = new IRODSAccount(host, port, username,
				password.getValue(), "/", mdas, defaultStorageResource);

		GeneralFile source = null;
		IRODSFileSystem irodsFileSystem = null;
		
		try {
			irodsFileSystem = new IRODSFileSystem(irodsAccount);

			// URI uri = new URI( uriLoc );
			// source = FileFactory.newFile( uri, password);
			source = new IRODSFile(irodsFileSystem, file);
			if (localDestinationFolder == null) { 
				source.copyTo(new LocalFile(source.getName()), true);
				localTransferedFile = source.getName();
			}
			else { 
				source.copyTo(new LocalFile(localDestinationFolder
						+ java.io.File.separator + source.getName()), true);
				localTransferedFile = localDestinationFolder
				+ java.io.File.separator + source.getName();
			}
			
		} catch (IOException ioe) {
			throw new I2B2Exception("FileTranferUtil.getFile failed  "
							+ ioe.getMessage(),ioe);

		} finally { 
			if (irodsFileSystem != null) { 
				try {
					irodsFileSystem.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		}
		*/
		return localTransferedFile;
	}

}
