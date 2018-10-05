/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
package edu.harvard.i2b2.crc.exec;

import java.io.File;
import java.io.IOException;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.common.exception.StackTraceUtil;

public class ExecUtil {

	private static Log log = LogFactory.getLog(ExecUtil.class);

	public void execute(String workingFolder, String commandLineStr,
			long timeOut) throws ExecException {
		log.info("Working folder :[" + workingFolder + "]");
		log.info("Command line :[ " + commandLineStr + "]");
		CommandLine commandLine = CommandLine.parse(commandLineStr);
		ExecuteWatchdog watchdog = new ExecuteWatchdog(timeOut);
		DefaultExecutor executor = new DefaultExecutor();
		executor.setWorkingDirectory(new File(workingFolder));
		executor.setWatchdog(watchdog);
		try {
			log.info("Executing command line [" + commandLine + "]");
			// executor.setExitValue(0);
			long startTime = System.currentTimeMillis();
			int exitValue = executor.execute(commandLine);
			long endTime = System.currentTimeMillis();
			long diffTime = endTime - startTime;
			log.debug(commandLine + " ran for " + diffTime + " mills");
		} catch (ExecuteException e) {
			throw new ExecException(e.getExitValue(), StackTraceUtil
					.getStackTrace(e));
		} catch (IOException e) {
			e.printStackTrace();
			throw new ExecException(0, StackTraceUtil.getStackTrace(e));
		}

	}

}
