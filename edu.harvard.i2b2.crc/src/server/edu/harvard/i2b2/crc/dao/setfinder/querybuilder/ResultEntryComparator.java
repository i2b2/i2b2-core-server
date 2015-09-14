/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors: 
 * 	   Chris
 *     Rajesh Kuttan
 */
package edu.harvard.i2b2.crc.dao.setfinder.querybuilder;

import java.util.Comparator;


/**
 * Implement comparator for ResultEntry
 * $Id: ResultEntryComparator.java,v 1.3 2007/08/31 14:46:23 rk903 Exp $
 * @author chris
 */
public class ResultEntryComparator implements Comparator {
    private String PATIENT_TABLE = null;
    private String ENCOUNTER_TABLE = null;
    private String THIRD_TABLE = null;

    public ResultEntryComparator(String patient_table, String encounter_table,
        String third_table) {
        PATIENT_TABLE = patient_table;
        ENCOUNTER_TABLE = encounter_table;
        THIRD_TABLE = third_table;
    }

    public int compare(Object element1, Object element2) {
        if ((element1.getClass().equals(ResultEntry.class)) &&
                (element2.getClass().equals((ResultEntry.class)))) {
            ResultEntry r1 = (ResultEntry) element1;
            ResultEntry r2 = (ResultEntry) element2;
            int compare = new Integer(r1.Panel).compareTo(new Integer(r2.Panel));

            if (compare == 0) {
                compare = new Integer(r1.SqlX).compareTo(new Integer(r2.SqlX)) * -1;

                if (compare == 0) {
                    if (r1.TableName.equals(r2.TableName)) {
                        compare = new Long(r1.EstItemSize).compareTo(new Long(
                                    r2.EstItemSize)) * -1;
                    } else {
                        int r1TblValue = -1;
                        int r2TblValue = -1;

                        if (r1.TableName.equals(PATIENT_TABLE)) {
                            r1TblValue = 0;
                        } else if (r1.TableName.equals(ENCOUNTER_TABLE)) {
                            r1TblValue = 1;
                        } else if (r1.TableName.equals(THIRD_TABLE)) {
                            r1TblValue = 2;
                        } else {
                            r1TblValue = 3;
                        }

                        if (r2.TableName.equals(PATIENT_TABLE)) {
                            r2TblValue = 0;
                        } else if (r2.TableName.equals(ENCOUNTER_TABLE)) {
                            r2TblValue = 1;
                        } else if (r2.TableName.equals(THIRD_TABLE)) {
                            r2TblValue = 2;
                        } else {
                            r2TblValue = 3;
                        }

                        compare = new Integer(r1TblValue).compareTo(new Integer(
                                    r2TblValue));
                    }
                }
            }

            return compare;
        } else {
            return element1.toString().compareTo(element2.toString());
        }
    }
}
