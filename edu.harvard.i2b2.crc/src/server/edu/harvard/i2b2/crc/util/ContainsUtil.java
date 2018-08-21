package edu.harvard.i2b2.crc.util;

import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.crc.dao.DAOFactoryHelper;
import edu.harvard.i2b2.crc.sql.parser.contains.ParseResult;
import edu.harvard.i2b2.crc.sql.parser.contains.TokenizedStatement;
import edu.harvard.i2b2.crc.sql.parser.contains.rules.I2b2Grammar;
import edu.harvard.i2b2.crc.sql.parser.contains.translators.TranslatorFactory;
import edu.harvard.i2b2.crc.sql.parser.contains.translators.Translator;
import edu.harvard.i2b2.crc.sql.parser.contains.SQLParserException;

public class ContainsUtil {
	
	/** log **/
	protected final static Log log = LogFactory.getLog(ContainsUtil.class);			
	public static final String REMOVE_PUNCTUATION="[\\p{Punct}&&[^-()<>.\\*%/]]";
	public boolean isDEBUG = false;
	
	//  dbServerType can be one of DAOFactoryHelper.SQLSERVER, DAOFactoryHelper.ORACLE, DAOFactoryHelper.POSTGRESQL	
	public String formatValue(String containsValue, String dbServerType) throws SQLParserException
	{								
		if (containsValue == null)
			return null;
		//1: check if value is enclosed in []. If so, tis means we are using ADVANCED DB CONTAINS query (use the containsValue AS IS).
		if (containsValue.startsWith("[") && containsValue.endsWith("]")) 
			return  containsValue.substring(1,containsValue.length()-1).replaceAll("'","''");		 
		
		// We are not using advanced DB Search, we parse it using i2b2 grammar, and then translate the result to the correct syntax in accordance to DB flavor
		// tokenize and parse the contains value.
		I2b2Grammar grammar = new I2b2Grammar();
		TokenizedStatement ts = new TokenizedStatement(containsValue);
	
		ParseResult pr = grammar.parse( ts );
		if (pr.isSuccess()) 
		{
			// if parsing succeeds, translate it to the correct syntax. Translation require a TokanizedStatement that is parsed.
			Translator t = TranslatorFactory.getInstance().getTranslator( dbServerType );
			log.info("[tdw9] Translator type =" + t.getName() );
			String translatedContainsValue = t.translate( ts );
			log.info("[tdw9] ContainsUtil.formatValue(...): [" + t.getName() + "] translating '" + containsValue +"' -> '" + translatedContainsValue + "'");
			return translatedContainsValue;
		}
		else
		{
			// if parsing fails return error message. TODO: messages need more work: 1. better generation of messages, 2. messages should be relayed to user properly.
			throw pr.getException();
		}
				
		
		/*
		 * tdw9:
		 *   The old code that deals with Contains sits below.
		 *   It is  the code above means to replace. We keep the following for references only.
		 *
		 */
		
		/*
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
		*/
	}
	
	public static void main(String[] args) 
	{ 
		try
		{
			ContainsUtil conUtil = new ContainsUtil();
			String formattedVal = conUtil.formatValue("MRI Knee","SQLSERVER");
			System.out.println("formattedVal[" + formattedVal + "]");
		}
		catch (SQLParserException e)
		{
			e.printStackTrace();
		}
	}
	
}
