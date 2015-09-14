/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors: 
 *     Rajesh Kuttan
 */
package edu.harvard.i2b2.crc.dao.pdo.output;

import edu.harvard.i2b2.crc.datavo.pdo.query.OutputOptionType;


/**
 * Class to support operation of {@link OutputOptionType}
 * in pdo request
 * $Id: FactRelated.java,v 1.3 2007/08/31 14:43:33 rk903 Exp $
 * @author rkuttan
 */
public abstract class FactRelated {
    private OutputOptionType outputOptionType = null;

    public FactRelated(OutputOptionType outputOptionType) {
        this.outputOptionType = outputOptionType;
    }

    public boolean isSelected() {
        if (outputOptionType != null && outputOptionType.getSelect() !=null) {
            return true;
        }
        else {
            return false;
        }
    }

    public boolean isFactRelated() {
    	if (outputOptionType != null && outputOptionType.getSelect() !=null) {
    		if (outputOptionType.getSelect().equals(outputOptionType.getSelect().USING_FILTER_LIST)) {
    			return true;
    		}
    		else { 
    			return false;
    		}
    	}
    	else { 
    		return false;
    	}
    }

    public boolean isSelectDetail() {
        if (outputOptionType != null) {
            if (outputOptionType.isOnlykeys()) {
                return false;
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    public boolean isSelectStatus() {
        if (outputOptionType != null) {
            return outputOptionType.isTechdata();
        } else {
            return false;
        }
    }

    public boolean isSelectBlob() {
        if (outputOptionType != null) {
            return outputOptionType.isBlob();
        } else {
            return false;
        }
    }
}
