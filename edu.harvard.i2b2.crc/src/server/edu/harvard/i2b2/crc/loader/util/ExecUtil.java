package edu.harvard.i2b2.crc.loader.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.StringTokenizer;

import edu.harvard.i2b2.common.exception.I2B2Exception;

/**
 * Utility class to run OS command line calls.
 * 
 * @author rk903
 * 
 */
public class ExecUtil {

	/**
	 * function to execute system commands.
	 * 
	 * @param commandLine
	 * @param runDir
	 * @return Process
	 * @throws Exception
	 */
	public Process exec(String commandLine) throws I2B2Exception {
		Process process = null;

		try {
			if (commandLine == null) {
				throw new I2B2Exception(
						"Could not execute, commandLine is null");
			}

			// check if window OS and add cmd prefix
			String OS = System.getProperty("os.name").toLowerCase();
			if (OS.indexOf("win") > -1 || OS.indexOf("nt") > -1) {
				commandLine = "cmd.exe /C " + commandLine;
			}

			// convert command line to string tokens
			String[] commandTokenizedStr = parseCommandLineString(commandLine);

			// exec process
			process = Runtime.getRuntime().exec(commandTokenizedStr);
			// wait till the process finish
			process.waitFor();
		} catch (IOException e1) {
			e1.printStackTrace();
			throw new I2B2Exception("Error running the command "
					+ e1.getMessage());

		} catch (InterruptedException e) {
			e.printStackTrace();
			throw new I2B2Exception("Error running the command "
					+ e.getMessage());
		}
		return process;
	}

	/**
	 * Get process error message
	 * 
	 * @param process
	 * @return String
	 * @throws Exception
	 */
	public String getProcessError(Process process) throws I2B2Exception {
		return writeToString(process.getErrorStream());
	}

	/**
	 * Get Process output message
	 * 
	 * @param process
	 * @return String
	 * @throws Exception
	 */
	public String getProcessOutput(Process process) throws I2B2Exception {
		return writeToString(process.getInputStream());
	}

	private String writeToString(InputStream inputStream) throws I2B2Exception {
		StringBuffer msg = new StringBuffer();
		BufferedReader bufferReader = null;

		try {
			InputStreamReader inputReader = new InputStreamReader(inputStream);
			bufferReader = new BufferedReader(inputReader);

			String line = null;
			while ((line = bufferReader.readLine()) != null) {
				System.err.println(line);
				msg.append(line + '\n');
			}
		} catch (IOException e) {
			throw new I2B2Exception("Error reading " + e.getMessage());
		} finally {
			if (bufferReader != null) {
				try {
					bufferReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}
		return msg.toString();
	}

	private String[] parseCommandLineString(String commandString) {
		StringTokenizer st = new StringTokenizer(commandString, "\"", true);
		String currentToken = "";
		ArrayList<String> list = new ArrayList<String>();
		while (st.hasMoreTokens()) {
			currentToken = st.nextToken();
			if (currentToken.equals("\"")) {
				if (st.hasMoreTokens()) {
					list.add(st.nextToken());
				}
				if (st.hasMoreTokens()) {
					st.nextToken();
				}
			} else {
				StringTokenizer internalSt = new StringTokenizer(currentToken);
				while (internalSt.hasMoreTokens()) {
					list.add(internalSt.nextToken());
				}
			}
		}
		return (String[]) list.toArray(new String[] {});
	}

	public static void main(String[] args) {
		ExecUtil execUtil = new ExecUtil();
		Process process = null;
		try {
			process = execUtil.exec("c:\\temp\\simple.bat");
			String outputStr = execUtil.getProcessOutput(process);
			System.out.println(outputStr);
			System.out.println(execUtil.getProcessError(process));
			System.out.println(outputStr.substring(outputStr
					.indexOf("empikey=") + 9,
					outputStr.indexOf("empikey=") + 9 + 3));
			System.out.println(outputStr.substring(outputStr
					.indexOf("noteskey=") + 10,
					outputStr.indexOf("noteskey=") + 10 + 3));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
