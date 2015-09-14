package edu.harvard.i2b2.crc.dao.setfinder.querybuilder;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DirectQueryForSinglePanel {

	protected final Log log = LogFactory.getLog(DirectQueryForSinglePanel.class);
	
	
	
	
	
	public DirectQueryForSinglePanel() {
		
	}
	
	
	public String buildSqlWithOR(String tempTableSql) { 
		
		String[] individualSql = tempTableSql.split("<\\*>");
		StringBuffer  convertedSqlBuffer = new StringBuffer();
		String individualSqlLowerCase = "";
		for (int i =0 ; i<individualSql.length-1;i++) { 
			individualSqlLowerCase = individualSql[i].toLowerCase();
			if (individualSqlLowerCase.indexOf("select")>0) { 
				
				String constraint = "";
				
				Pattern p = Pattern.compile("\\bwhere\\b(.*?)(?:(\\bgroup by\\b|\\border by\\b|$))", Pattern.DOTALL|Pattern.CASE_INSENSITIVE);
				Matcher m = p.matcher(individualSqlLowerCase);
				if (m.find() && m.groupCount()>0)
					constraint = m.group(1).trim();
				
				convertedSqlBuffer.append( "( " + constraint + ")");
				//System.out.println("original split sql " + individualSql[i]);
				if (i+1<individualSql.length-1) { 
					convertedSqlBuffer.append("\n OR  \n");
				}
			}
		}
		return convertedSqlBuffer.toString();
		
	}
	
	public String buildSqlWithUnion(String tempTableSql) { 
		
		String[] individualSql = tempTableSql.split("<\\*>");
		StringBuffer  convertedSqlBuffer = new StringBuffer();
		String individualSqlLowerCase = "";
		for (int i =0 ; i<individualSql.length-1;i++) { 
			individualSqlLowerCase = individualSql[i].toLowerCase();
			if (individualSqlLowerCase.indexOf("select")>0) { 
				convertedSqlBuffer.append(individualSql[i].substring(individualSqlLowerCase.indexOf("select"),individualSqlLowerCase.length()));
				//System.out.println("original split sql " + individualSql[i]);
				if (i+1<individualSql.length-1) { 
					convertedSqlBuffer.append("\n UNION ALL  \n");
				}
			}
		}
		return convertedSqlBuffer.toString();
		
	}
	
}
