/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors: 
 *     Rajesh Kuttan
 */

package edu.harvard.i2b2.crc.datavo.db;


import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

/**
 * This class allows strings to be stored in an escaped form, so that
they will never be
 * automatically converted to NULL values by the database, should they
be empty.
 * Note that this class will not allow you to use NULL value strings
when they are not allowed by
 * Hibernate (such as in Maps).
 *
 * Version for Hibernate 3 that does not add quotes to non-empty strings
but escapes by a keyword.
 * This seems more economic in cases where empty strings are rare.
 * 
 * @author rkuttan
 */
public class IntegerStringUserType { //implements UserType {
    private static final int[] SQL_TYPES = { Types.INTEGER };

    public int[] sqlTypes() {
        return SQL_TYPES;
    }

    public Class returnedClass() {
        return String.class;
    }
/*
    public boolean equals(Object x, Object y) throws HibernateException {
        if (x == y) {
            return true;
        } else if ((x == null) || (y == null)) {
            return false;
        } else {
            return x.equals(y);
        }
    }

    public Object nullSafeGet(ResultSet resultSet, String[] names, Object owner)
        throws HibernateException, SQLException {
        String result = null;
        int idInt = resultSet.getInt(names[0]);

        if (!resultSet.wasNull()) {
            result = (idInt == 0) ? null : String.valueOf(idInt);
        }

        return result;
    }

    public void nullSafeSet(PreparedStatement statement, Object value, int index)
        throws HibernateException, SQLException {
        if (value == null) {
            statement.setInt(index, 0);
        } else {
            Integer dateAsInteger = Integer.parseInt((String) value);
            statement.setInt(index, dateAsInteger);
        }
    }

    public Object deepCopy(Object value) throws HibernateException {
        return value;
    }

    public boolean isMutable() {
        return false;
    }

	public Object assemble(Serializable arg0, Object arg1) throws HibernateException {
		return deepCopy(arg0);
	}

	public Serializable disassemble(Object arg0) throws HibernateException {
		return (Serializable)deepCopy(arg0);
	}

	public int hashCode(Object arg0) throws HibernateException {

		return arg0.hashCode();
	}

	public Object replace(Object arg0, Object arg1, Object arg2) throws HibernateException {
		return deepCopy(arg0);
	}
	*/
}
