/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
/* 
 * Copyright (c) 2011 - University of Texas Health Science Center at Houston.
 * 7000 Fannin St, Suite 600, Houston, Texas 77030
 * All rights reserved.   This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v2.1 
 * which accompanies this distribution. 
 */

package edu.harvard.i2b2.pm.util;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.pm.dao.PMDbDao;
import edu.harvard.i2b2.pm.datavo.pm.PasswordType;
import edu.harvard.i2b2.pm.datavo.pm.UserType;

/*
 * Basic authentication for i2b2 
 *
 * @param username String 
 * @param password String
 * @param params Hashtable object that holds user parameters for BASIC configuration
 * 
 * The parameters are listed below with their possible values in ():
 *
 */


public class SecurityAuthenticationBASIC implements SecurityAuthentication {

	@Override
	public boolean validateUser(String username, String password,
			Hashtable params) throws Exception {

		PMDbDao pmDb = new PMDbDao();
		
		List response = null;

		try {
			response = pmDb.getUser(username, null, null, true); //PMUtil.getInstance().getHashedPassword(password));
		} catch (I2B2DAOException e1) {
			throw new Exception ( "Database error in getting user data");
		} catch (I2B2Exception e1) {
			throw new Exception ("Database error in getting user data");
		}

		Iterator it = response.iterator();
		UserType user = null;
		
		while (it.hasNext())
		{
			user = (UserType)it.next();

			// Check if MD5 and if so tahn convert to SHA256
			if (user.getPassword().getValue().equals(PMUtil.getInstance().getHashedPassword("MD5", password)))
			{
				pmDb.setPassword(user.getPassword().getValue(), password, username);
				PasswordType pass = user.getPassword();
				pass.setValue(PMUtil.getInstance().getHashedPassword("SHA-256", password));
				user.setPassword(pass );
			}
			
			//Check the password
			if (user.getPassword().getValue().startsWith("@"))
			{
				if	(!(user.getPassword().getValue().substring(1)).equals(password))
				{
					pmDb.setLoginAttempt(username, "BADPASSWORD" );
					throw new Exception ("Unable to sign in");
				}
			}				
			else if (!user.getPassword().getValue().equals(PMUtil.getInstance().getHashedPassword("SHA-256", password)))
			{
				String haspass = PMUtil.getInstance().getHashedPassword("SHA-256", password);
				pmDb.setLoginAttempt(username, "BADPASSWORD" );
				throw new Exception ("Unable to sign in");

			}
			

		}

		return true;
	}

}
