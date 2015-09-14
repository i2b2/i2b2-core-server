/*
 * Copyright (c) 2006-2013 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors:
 * 		Christopher Herrick
 */
package edu.harvard.i2b2.crc.dao.setfinder.querybuilder.temporal;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class TemporalQuerySimpleSqlParser {
	
	protected final Log log = LogFactory.getLog(TemporalQuerySimpleSqlParser.class);
	private String fullSql = null;
	
	private String selectClause = null;
	private String fromClause = null;
	private String whereClause = null;
	private String groupByClause = null;
	private String havingClause = null;
	private String orderByClause = null;
	
	public TemporalQuerySimpleSqlParser(String simpleSqlStatement){
		fullSql = simpleSqlStatement;
		selectClause = parseSelectClause(fullSql);
		fromClause = parseFromClause(fullSql);
		whereClause = parseWhereClause(fullSql);
		groupByClause = parseGroupByClause(fullSql);
		havingClause = parseHavingClause(fullSql);
		orderByClause = parseOrderByClause(fullSql);
	}
	
	private String parseFromClause(String sql){
		if (sql==null||sql.trim().length()==0)
			return null;
		
		return runRegExExpression(sql, "\\bfrom\\b(.*?)(?:(\\bwhere\\b|\\bgroup by\\b|\\border by\\b|$))");
	}
	
	private String parseSelectClause(String sql){
		if (sql==null||sql.trim().length()==0)
			return null;
		
		return runRegExExpression(sql, "\\bselect\\b(.*?)(?:(\\bfrom\\b|$))");
	}
	
	private String parseWhereClause(String sql){
		if (sql==null||sql.trim().length()==0)
			return null;
		
		return runRegExExpression(sql, "\\bwhere\\b(.*?)(?:(\\bgroup by\\b|\\border by\\b|$))");
	}
	
	private String parseGroupByClause(String sql){
		if (sql==null||sql.trim().length()==0)
			return null;
		
		return runRegExExpression(sql, "\\bgroup by\\b(.*?)(?:(\\bhaving\\b|\\border by\\b|$))");
	}
	
	private String parseHavingClause(String sql){
		if (sql==null||sql.trim().length()==0)
			return null;
		
		return runRegExExpression(sql, "\\bhaving\\b(.*?)(?:(\\border by\\b|$))");
	}
	
	private String parseOrderByClause(String sql){
		if (sql==null||sql.trim().length()==0)
			return null;
		
		return runRegExExpression(sql, "\\border by\\b(.*)");
	}
	
	private String runRegExExpression(String sqlString, String regEx){
		Pattern p = Pattern.compile(regEx, Pattern.DOTALL|Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(sqlString);
		if (m.find() && m.groupCount()>0)
			return m.group(1).trim();
		else
			return null;		
	}

	public String getSelectClause() {
		return selectClause;
	}

	public String getFromClause() {
		return fromClause;
	}

	public String getWhereClause() {
		return whereClause;
	}

	public String getGroupByClause() {
		return groupByClause;
	}

	public String getHavingClause() {
		return havingClause;
	}

	public String getOrderByClause() {
		return orderByClause;
	}

}
