package edu.harvard.i2b2.crc.loader.util.csv;


import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import java.util.List;


/**
 * Class to build full patient data xml. 
 * @author rk903
 *
 */
public class PatientDataXmlBuilder {

	private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static String SOURCE_SYSTEM_CD = "SOURCE_SYSTEM_CD";
	
	/**
	 * build concept dimension part
	 *
	 */
	public void buildConceptDimension() { 
		//TODO
	}
	
	/**
	 * build provider dimension part
	 * 
	 */
	public void builProviderDimension() { 
		//TODO
	}
	
	/**
	 * build patient dimension part
	 *
	 */
	public void patientDimension() { 
		//TODO
		
		
	}
	
	/**
	 * build observation fact part.
	 *
	 */
	public void buildObservationFact() { 
		//TODO 
	}
	
	/**
	 * build visit dimension part
	 *
	 */
	public void buildVisitDimension() {
		//TODO 
	}
	
	/**
	 * Create Date from timestamp value in String format.
	 * @param timeStamp
	 * @return
	 */
	public static String formatIntDate(String timeStamp) {
		timeStamp = timeStamp.replace("\"","");
		//System.out.println(timeStamp);
		String year = timeStamp.substring(0,4);
		String month = timeStamp.substring(4,6);
		String day = timeStamp.substring(6,8);
		String hour = timeStamp.substring(8,10);
		String minute = timeStamp.substring(10,12);
		String secound = timeStamp.substring(12,14);
		java.util.GregorianCalendar calendar = new java.util.GregorianCalendar(Integer.parseInt(year), Integer.parseInt(month)-1, Integer.parseInt(day), Integer.parseInt(hour), Integer.parseInt(minute), Integer.parseInt(secound));  
		return dateFormat.format(calendar.getTime());
		//return new Date(Long.parseLong(timeStamp)).toString();
	}
	
	/**
	 * Function to get patient data xml header
	 * @return String
	 */
	public static String getDocumentHeader() { 
		return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
		"<ns2:patient_data   xmlns:ns2=\"http://www.i2b2.org/xsd/hive/pdo/1.1/\">";
		
		
		   
	}
	
	/**
	 * Function to get patient data xml end document.
	 * @return String
	 */
	public static String getEndDocument()  { 
		return "</ns2:patient_data>";
	}
	
	public static Hashtable getCsvHeaderColumnPosition(Hashtable csvHeaderMap,Vector headerFields) {
		Hashtable csvHeaderColumnPosition = new Hashtable();
		//iterate cvsHeaderMap and locate column position in header
		for (Iterator iterator = csvHeaderMap.keySet().iterator(); iterator.hasNext();) {
			String elementName = (String)iterator.next();
			String[] columnHeading = (String[])csvHeaderMap.get(elementName);
			int columnPosition = -1;
			int columnIndex = 0;
			for(Iterator fileHeaderIterator = headerFields.iterator();fileHeaderIterator.hasNext();) {
				String singleHeaderField = (String)fileHeaderIterator.next();
				List possibleColumnList = Arrays.asList(columnHeading);
				boolean containFlag =  possibleColumnList.contains(singleHeaderField);
				if (containFlag) {
					csvHeaderColumnPosition.put(elementName,columnIndex);
					break;
				}
				columnIndex++;
			}
			
		}
		return csvHeaderColumnPosition;
	}
	
	public static String getColumnValue(Map csvHeaderColumnPosition, String col[],String elementName) {
		 String elementValue = null;
		 try {
			 Integer elementColumnPosition = (Integer)csvHeaderColumnPosition.get(elementName);
			 if (elementColumnPosition == null) { 
				 return elementValue;
			 }
			 elementValue = col[elementColumnPosition.intValue()];
		 } catch (java.lang.ArrayIndexOutOfBoundsException ex) { 
			System.out.println("ArrayIndexOutOfBoundsException:  For debug first column" + col[0]);
		 }
		 return elementValue;
	 }
	
	public static String getElementPrefix(Vector headerFields, String elementName, String sourceSystemCd) {
		String prefix = null;
		for(Object columnHeading:headerFields) {
			String strColumnHeading = (String)columnHeading; 
			int prefixColumn = strColumnHeading.lastIndexOf(elementName + "_prefix");
			String skipLengthString  = elementName + "_prefix"; 
			
			if (prefixColumn>-1) { 
				 String prefixValue = strColumnHeading.substring(prefixColumn+skipLengthString.length()+1,strColumnHeading.length());
				 if (prefixValue != null && prefixValue.equals(SOURCE_SYSTEM_CD)) { 
					 prefix = sourceSystemCd;
				 }
				 else { 
					 prefix = prefixValue;
				 }
			}
		}
		return prefix;	
	}
	
	public static Date getDate(String origdate)
	{
		//	getValidDate(date, "MM/dd/yyyy");
		//	if (correctDate != null)
		//		return correctDate;
		String date = origdate.replace(':', '-');
		date = date.replace('/', '-');
		date = date.replace('.', '-');

		Date correctDate = null;
		correctDate = getValidDate(date, "dd-MMM-yyyy hh-mm a");
		if (correctDate != null)
			return correctDate;
		correctDate = getValidDate(date, "dd-MMM-yyyy HH-mm");
		if (correctDate != null)
			return correctDate;
		correctDate =  getValidDate(date, "dd-MMM-yy hh-mm a");
		if (correctDate != null)
			return correctDate;
		correctDate = getValidDate(date, "dd-MMM-yy HH-mm");
		if (correctDate != null)
			return correctDate;
		correctDate = getValidDate(date, "dd-MMM-yy");
		if (correctDate != null)
			return correctDate;		
		correctDate = getValidDate(date, "MM-dd-yyyy hh-mm a");
		if (correctDate != null)
			return correctDate;
		correctDate = getValidDate(date, "MM-dd-yyyy HH-mm");
		if (correctDate != null)
			return correctDate;
		correctDate = getValidDate(date, "MM-dd-yy hh-mm a");
		if (correctDate != null)
			return correctDate;
		getValidDate(date, "MM-dd-yy HH-mm");
		if (correctDate != null)
			return correctDate;


		correctDate = getValidDate(origdate, DateFormat.SHORT);
		if (correctDate != null)
			return correctDate;
		correctDate = getValidDate(origdate, DateFormat.FULL);
		if (correctDate != null)
			return correctDate;
		correctDate = getValidDate(origdate, DateFormat.LONG);
		if (correctDate != null)
			return correctDate;
		correctDate = getValidDate(origdate, DateFormat.DEFAULT);
		if (correctDate != null)
			return correctDate;
		correctDate = getValidDate(origdate, DateFormat.MEDIUM);
		if (correctDate != null)
			return correctDate;



		return correctDate;
	}

	private static Date getValidDate(String date, String format)
	{
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		//Date testDate = null;
		Date correctDate = null;
		// we will now try to parse the string into date form
		try
		{
			correctDate = sdf.parse(date);
		}

		// if the format of the string provided doesn't match the format we
		// declared in SimpleDateFormat() we will get an exception

		catch (ParseException e)
		{
			correctDate = null;
		
		}
		return correctDate;
		//return testDate;
		//correctDate = correctFormat.format(testDate);

	} // end isValidDate

	private static Date getValidDate(String date, int format)
	{
		DateFormat testFormat =	DateFormat.getDateInstance(format);

		// declare and initialize testDate variable, this is what will hold
		// our converted string

		//Date testDate = null;
		Date correctDate = null;
		// we will now try to parse the string into date form
		try
		{
			correctDate = testFormat.parse(date);
		}

		// if the format of the string provided doesn't match the format we
		// declared in SimpleDateFormat() we will get an exception

		catch (ParseException e)
		{
			correctDate = null;
			
		}
		return correctDate;

		//correctDate = correctFormat.format(testDate);
	} // end isValidDate
	
}
