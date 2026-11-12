/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
package edu.harvard.i2b2.crc.dao.pdo.input;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import edu.harvard.i2b2.crc.datavo.pdo.query.ConstrainOperatorType;
import edu.harvard.i2b2.crc.datavo.pdo.query.ConstrainValueType;
import edu.harvard.i2b2.crc.datavo.pdo.query.ItemType;

/**
 * Regression tests for
 * https://github.com/i2b2/i2b2-core-server/issues/156
 * ("Erroneous results on search by value with BETWEEN").
 *
 * A "search by value BETWEEN 4 and 6" used to generate
 * {@code ... nval_num BETWEEN 4 and 6 AND tval_char = 'E'}, silently dropping
 * numeric facts stored with inequality operators at the boundary values
 * (e.g. {@code >=4} stored as tval_char='GE', {@code <=6} as tval_char='LE').
 */
public class ValueConstrainsHandlerTest {

	private String buildBetweenSql(String betweenConstraint) throws Exception {
		ItemType.ConstrainByValue constrain = new ItemType.ConstrainByValue();
		constrain.setValueType(ConstrainValueType.NUMBER);
		constrain.setValueOperator(ConstrainOperatorType.BETWEEN);
		constrain.setValueConstraint(betweenConstraint);
		List<ItemType.ConstrainByValue> constrains = new ArrayList<ItemType.ConstrainByValue>();
		constrains.add(constrain);
		String[] sql = new ValueConstrainsHandler()
				.constructValueConstainClause(constrains, "POSTGRESQL", "", 0);
		// generated SQL pads identifiers with spaces; normalize before asserting
		return sql[0].replaceAll("\\s+", " ");
	}

	@Test
	public void testBetweenIncludesBoundaryInequalities() throws Exception {
		String sql = buildBetweenSql("4 and 6");
		// facts stored with exact values must still match
		assertTrue("BETWEEN must still match exact values, got: " + sql,
				sql.contains("nval_num BETWEEN 4 and 6")
						&& sql.contains("tval_char = 'E'"));
		// facts stored as >4 / >=4 at the low boundary must be included
		assertTrue(
				"BETWEEN must include low-boundary inequalities (G/GE), got: "
						+ sql,
				sql.contains("nval_num = 4 AND obs.tval_char IN ('G','GE')"));
		// facts stored as <6 / <=6 at the high boundary must be included
		assertTrue(
				"BETWEEN must include high-boundary inequalities (L/LE), got: "
						+ sql,
				sql.contains("nval_num = 6 AND obs.tval_char IN ('L','LE')"));
	}

	@Test
	public void testBetweenWithDecimalBoundaries() throws Exception {
		String sql = buildBetweenSql("4.5 and 6.5");
		assertTrue(
				"BETWEEN must include low-boundary inequalities (G/GE), got: "
						+ sql,
				sql.contains("nval_num = 4.5 AND obs.tval_char IN ('G','GE')"));
		assertTrue(
				"BETWEEN must include high-boundary inequalities (L/LE), got: "
						+ sql,
				sql.contains("nval_num = 6.5 AND obs.tval_char IN ('L','LE')"));
	}
}
