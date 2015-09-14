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
