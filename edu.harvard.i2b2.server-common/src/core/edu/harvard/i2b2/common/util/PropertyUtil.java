/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
package edu.harvard.i2b2.common.util;

import org.springframework.beans.factory.BeanFactory;


/**
 * This class encapsulates Spring's BeanFactory.
 * @author Rajesh Kuttan
 */
public class PropertyUtil {
    private BeanFactory beanFactory = null;

    public PropertyUtil() {
    }

    public PropertyUtil(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    public BeanFactory getBeanFactory() {
        return beanFactory;
    }

    /**
     * Call's BeanFactory's getBean
     * @param beanName
     * @return
     */
    public Object getProperty(String beanName) {
        return beanFactory.getBean(beanName);
    }
}
