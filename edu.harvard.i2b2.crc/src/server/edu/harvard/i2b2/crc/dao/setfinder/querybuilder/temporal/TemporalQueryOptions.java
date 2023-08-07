/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
/*

 * 
 * Contributors:
 * 		Christopher Herrick
 */
package edu.harvard.i2b2.crc.dao.setfinder.querybuilder.temporal;

import edu.harvard.i2b2.crc.util.QueryProcessorUtil;

public class TemporalQueryOptions {

	
	public enum QueryConstraintStrategy{
		TEMP_TABLES,
		WITH_STATEMENT,
		DERIVED_TABLES		
	}
	
	public enum TemporalConstraintStrategy{
		TEMP_TABLE_UPDATE,
		WITH_CONSTRAINT
	}
	
	
	private boolean usePanelLevelOccurrence = false;
	private QueryConstraintStrategy queryConstraintLogic = QueryConstraintStrategy.WITH_STATEMENT;
	private TemporalConstraintStrategy temporalConstraintLogic = TemporalConstraintStrategy.TEMP_TABLE_UPDATE;
	private boolean useItemGroupByStatement = true;
	private boolean useSqlHints = true;
	private boolean derivedFactTable = QueryProcessorUtil.getInstance().getDerivedFactTable();
	
	/**
	 * @return the usePanelLevelOccurrence
	 */
	public boolean usePanelLevelOccurrence() {
		return usePanelLevelOccurrence;
	}
	
	public enum InvertedConstraintStrategy{
		NOT_EXISTS,
		MINUS_CLAUSE
	}
	private InvertedConstraintStrategy invertedConstraintLogic = InvertedConstraintStrategy.NOT_EXISTS;

	/**
	 * @param usePanelLevelOccurrence the usePanelLevelOccurrence to set
	 */
	public void setUsePanelLevelOccurrence(boolean usePanelLevelOccurrence) {
		this.usePanelLevelOccurrence = usePanelLevelOccurrence;
	}

	/**
	 * @return the queryConstraintLogic
	 */
	public QueryConstraintStrategy getQueryConstraintLogic() {
		return queryConstraintLogic;
	}

	/**
	 * @param queryConstraintLogic the queryConstraintLogic to set
	 */
	public void setQueryConstraintLogic(QueryConstraintStrategy queryConstraintLogic) {
		this.queryConstraintLogic = queryConstraintLogic;
	}

	/**
	 * @return the temporalConstraintType
	 */
	public TemporalConstraintStrategy getTemporalConstraintStrategy() {
		return temporalConstraintLogic;
	}

	/**
	 * @param temporalConstraintType the temporalConstraintType to set
	 */
	public void setTemporalConstraintStrategy(TemporalConstraintStrategy temporalConstraintStrategy) {
		this.temporalConstraintLogic = temporalConstraintStrategy;
	}

	/**
	 * @return the useItemGroupByStatement
	 */
	public boolean useItemGroupByStatement() {
		return useItemGroupByStatement;
	}

	/**
	 * @param useItemGroupByStatement the useItemGroupByStatement to set
	 */
	public void setUseItemGroupByStatement(boolean useItemGroupByStatement) {
		this.useItemGroupByStatement = useItemGroupByStatement;
	}

	/**
	 * @return the invertedConstraintLogic
	 */
	public InvertedConstraintStrategy getInvertedConstraintLogic() {
		return invertedConstraintLogic;
	}

	/**
	 * @param invertedConstraintLogic the invertedConstraintLogic to set
	 */
	public void setInvertedConstraintLogic(InvertedConstraintStrategy invertedConstraintLogic) {
		this.invertedConstraintLogic = invertedConstraintLogic;
	}

	/**
	 * @return the useSqlHints
	 */
	public boolean useSqlHints() {
		return useSqlHints;
	}

	/**
	 * @param useSqlHints the useSqlHints to set
	 */
	public void setUseSqlHints(boolean useSqlHints) {
		this.useSqlHints = useSqlHints;
	}

	public boolean useDerivedFactTable() {
		return derivedFactTable;
	}

	public void setUseDerivedFactTable(boolean derivedFactTable) {
		this.derivedFactTable = derivedFactTable;
	}
	
}
