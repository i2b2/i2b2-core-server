package edu.harvard.i2b2.crc.util;

import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.crc.dao.DAOFactoryHelper;
import edu.harvard.i2b2.crc.sql.parser.contains.ParseResult;
import edu.harvard.i2b2.crc.sql.parser.contains.TokenizedStatement;
import edu.harvard.i2b2.crc.sql.parser.contains.sqlserver.rules.Contains;

public class ContainsUtil {
	
	/** log **/
	protected final static Log log = LogFactory
			.getLog(ContainsUtil.class);
			
	public static final String REMOVE_PUNCTUATION="[\\p{Punct}&&[^-()<>.\\*%/]]";
	
	public String formatValue(String containsValue, String dbServerType) {
		
		log.info("[tdw9] We got in ContainsUtil.formatValue(...) containsValue = " + containsValue );
							
		if (containsValue == null) { 
			return null;
		}
		//1: check if value is enclosed in []
		if (containsValue.startsWith("[") && containsValue.endsWith("]")) { 
			return  containsValue.substring(1,containsValue.length()-1).replaceAll("'","''");
			 
		}
		
		// bugbug: refactor the following after postgresql is done
		if ( dbServerType.equalsIgnoreCase(DAOFactoryHelper.SQLSERVER) ) // tdw9: using new syntax checker and formatter for SQLSERVER
		{
			log.info("[tdw9] ContainsUtil.formatValue(...): SQLSERVER parsing '" + containsValue +"'");
			Contains c = new Contains();
			ParseResult pr = c.parse( new TokenizedStatement(containsValue) );
			if (pr.isSuccess()) return containsValue;
			else return pr.getErrorMsg();
		}
		else if ( dbServerType.equalsIgnoreCase(DAOFactoryHelper.ORACLE) ) // for ORACLE
		{
			log.info("[tdw9] ContainsUtil.formatValue(...): ORACLE parsing '" + containsValue +"'");
			Contains c = new Contains();
			TokenizedStatement ts = new TokenizedStatement(containsValue);
			ParseResult pr = c.parse( ts );
			if (pr.isSuccess()) 
				return OracleContainsTranslator.getInstance().formatValue(containsValue, ts);
			else 
				return pr.getErrorMsg();
		}
		// let postgresql fall through to the old code for the time being.
		
		
		
		//2: check if value is enclosed in ""
		if (containsValue.startsWith("\"") && containsValue.endsWith("\"")) {
			if (dbServerType.equalsIgnoreCase(DAOFactoryHelper.ORACLE) == false) { 
				return  "\"" + containsValue.substring(1,containsValue.length()-1).replaceAll(REMOVE_PUNCTUATION,"") + "\"";
			} else { 
				return   containsValue.substring(1,containsValue.length()-1).replaceAll(REMOVE_PUNCTUATION,"");
			}
		}
		
		boolean textWithoutOperator = true;
		if (containsValue.indexOf("-") > 0 ||
				containsValue.indexOf("AND") > 0 || 
				containsValue.indexOf("OR") > 0 ||
				containsValue.indexOf("*") > 0) { 
			textWithoutOperator = false;
		}
				
		
		//3: remove punctuation 
		String punctuationStr = containsValue.replaceAll(REMOVE_PUNCTUATION,"");
		
		
		
		//4 word start with "-", then add NOT
		StringTokenizer strTokenizer = new StringTokenizer(punctuationStr);
		String singleToken = null;
		String notStr = "";
		
		boolean noOperator = true;
		while(strTokenizer.hasMoreTokens()) {
			singleToken = strTokenizer.nextToken();
			if (singleToken.startsWith("-")) { 
				notStr += singleToken.replaceAll("(-)"," NOT ");
				noOperator = false;
			} else { 
				notStr += singleToken;
				if (strTokenizer.hasMoreTokens()) { 
					notStr += " ";
				}
			}
		}
		
		 
		
		//5 replace CAPS AND with accum (only for oracle)
		String accumStr = "";
		if (dbServerType.equalsIgnoreCase(DAOFactoryHelper.ORACLE)) { 
			accumStr  = notStr.replaceAll("\\s(AND)\\s", " ACCUM ");
		} else { 
			accumStr  = notStr.replaceAll("\\s(AND)\\s", "  ");
		}
		
		
		//6: replace "*" with  %, or *(only for oracle)
		String starStr = accumStr;
		if (dbServerType.equalsIgnoreCase(DAOFactoryHelper.ORACLE)) {
			starStr = accumStr.replaceAll("(\\*)", "%");	
		} 
		System.out.println("start value [" + starStr + "]"); 
		
		//7: replace DB_AND with AND
		String andStr = starStr.replaceAll("\\s(DB_AND)\\s", " AND ");
		
		//8: replace OR with  minus(only for oracle)
		String orStr = andStr;
		if (dbServerType.equalsIgnoreCase(DAOFactoryHelper.ORACLE)) {
			orStr = andStr.replaceAll("\\s(OR)\\s", " MINUS ");
		}
		
		//9: single
		String finalStr = orStr;
		if (textWithoutOperator) { 
			//split the words
			String defaultAccumStr = "";
			StringTokenizer accumTokenizer = new StringTokenizer(finalStr);
			while(accumTokenizer.hasMoreTokens()) {
				singleToken = accumTokenizer.nextToken();
				if (dbServerType.equalsIgnoreCase(DAOFactoryHelper.ORACLE) || dbServerType.equalsIgnoreCase(DAOFactoryHelper.POSTGRESQL)) {
					defaultAccumStr += singleToken;
				} else if (dbServerType.equalsIgnoreCase(DAOFactoryHelper.SQLSERVER)) { 
					defaultAccumStr += "\"" + singleToken + "\"";
				}
				
				if (accumTokenizer.hasMoreTokens()) { 
					if (dbServerType.equalsIgnoreCase(DAOFactoryHelper.ORACLE)) { 
						defaultAccumStr += " ACCUM ";
					} else if (dbServerType.equalsIgnoreCase(DAOFactoryHelper.SQLSERVER)) { 
						defaultAccumStr += " OR ";
					} else if  (dbServerType.equalsIgnoreCase(DAOFactoryHelper.POSTGRESQL)) {
						defaultAccumStr += " | ";
					}
				}
			}
			return defaultAccumStr;
		} else { 
			return finalStr;
		}
		
		
	}
	
	public static void main(String[] args) { 
		ContainsUtil conUtil = new ContainsUtil();
	String formattedVal = conUtil.formatValue("MRI Knee","SQLSERVER");
		System.out.println("formattedVal[" + formattedVal + "]");
	}
	
}
