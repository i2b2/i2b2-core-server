/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
package edu.harvard.i2b2.crc.dao.setfinder;

public class QueryStatusTypeId {
	public final static int STATUSTYPE_ID_QUEUED = 1;
	public final static int STATUSTYPE_ID_PROCESSING = 2;
	public final static int  STATUSTYPE_ID_FINISHED = 3;
	public final static int STATUSTYPE_ID_ERROR = 4;
	public final static int STATUSTYPE_ID_INCOMPLETE = 5;
	public final static int STATUSTYPE_ID_COMPLETED = 6;
}
