/**
 * 
 */
package edu.harvard.i2b2.crc.loader.datavo.loader;

import java.util.regex.*;
import java.io.*;


//import sun.security.krb5.internal.s;
/**
 * @author mem61
 *
 */
public class ProcessPulmonary {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Pulmonary p = new Pulmonary();

		String dataIn = "";
		if ( args.length > 0) {
	        try {
	           FileInputStream fin =  new FileInputStream(args[0]);
	           // JDK1.1+
	           BufferedReader myInput = new BufferedReader
	               (new InputStreamReader(fin));
	           while ((dataIn = myInput.readLine()) != null) {
	        	   if (dataIn.toUpperCase().indexOf("HEIGHT") > -1)
	        	   {
	        		   // Found a Height, find the next ":" and than
	        		   // the number will be the height
	        		   int heightLocBegin = dataIn.indexOf(":", dataIn.toUpperCase().indexOf("HEIGHT")) + 2;
	        		   int heightLocEnd = dataIn.indexOf(" ", heightLocBegin+1);
	        		   p.setHeight(Double.parseDouble(dataIn.substring(heightLocBegin,heightLocEnd).trim()));
	        	   }
	        	   if (dataIn.toUpperCase().indexOf("WEIGHT") > -1)
	        	   {
	        		   // Found a Height, find the next ":" and than
	        		   // the number will be the height
	        		   int heightLocBegin = dataIn.indexOf(":", dataIn.toUpperCase().indexOf("WEIGHT")) + 2;
	        		   int heightLocEnd = dataIn.indexOf(" ", heightLocBegin+1);
	        		   p.setHeight(Double.parseDouble(dataIn.substring(
	        				   heightLocBegin,(heightLocEnd > -1 ? 
	        						   heightLocEnd : dataIn.length())).trim()));
	        	   }
	        	   if (dataIn.toUpperCase().indexOf("FIRST SEC VC (L)") > -1)
	        	   {
		        	   Bronchodilator bronchodilator = new Bronchodilator();

	        		   int locBegin = dataIn.toUpperCase().indexOf("FIRST SEC VC (L)") + 17;
	        		   java.util.StringTokenizer st = new java.util.StringTokenizer (dataIn.substring(locBegin));
	        		   String[] array = new String[st.countTokens()];
	        		   for (int i =0; i < st.countTokens(); i++) {
	        		     array[i] = st.nextToken ();
	        		   } 
	        		   bronchodilator.setNoObserved(Double.parseDouble(array[0]));
	        		   bronchodilator.setNoPredicted(Double.parseDouble(array[2]));
	        		   p.setFirstSecVC(bronchodilator);
	        	   }
	        	   if (dataIn.toUpperCase().indexOf("VITAL CAPACITY (L)") > -1)
	        	   {
		        	   Bronchodilator bronchodilator = new Bronchodilator();

	        		   int locBegin = dataIn.toUpperCase().indexOf("VITAL CAPACITY (L)") + 19;
	        		   java.util.StringTokenizer st = new java.util.StringTokenizer (dataIn.substring(locBegin));
	        		   String[] array = new String[st.countTokens()];
	        		   for (int i =0; i < st.countTokens(); i++) {
	        		     array[i] = st.nextToken ();
	        		   } 
	        		   bronchodilator.setNoObserved(Double.parseDouble(array[0]));
	        		   bronchodilator.setNoPredicted(Double.parseDouble(array[2]));
	        		   p.setVitalCapacity(bronchodilator);
	        	   }

	        	   
	        	   if (dataIn.toUpperCase().indexOf("HEIGHT") > -1)
	        	   {
	        		   // Found a Height, find the next ":" and than
	        		   // the number will be the height
	        		   int heightLocBegin = dataIn.indexOf(":", dataIn.toUpperCase().indexOf("HEIGHT")) + 2;
	        		   int heightLocEnd = dataIn.indexOf(" ", heightLocBegin+1);
	        		   p.setHeight(Double.parseDouble(dataIn.substring(heightLocBegin,heightLocEnd).trim()));
	        	   }
	              }
	           }
	        catch (Exception e) {
	          e.printStackTrace();
	          }
	    }
		

	}

}
