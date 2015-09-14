package edu.harvard.i2b2.crc.dao.setfinder;

import java.util.Map;

import org.junit.Test;
import org.springframework.beans.factory.BeanFactory;

import edu.harvard.i2b2.crc.util.QueryProcessorUtil;

public class SetfinderGeneratorConfigTest {

	@Test
	public void test() {
		QueryProcessorUtil qpUtil = QueryProcessorUtil.getInstance();
		BeanFactory bf = qpUtil.getSpringBeanFactory();
		Map sfMap = (Map)bf.getBean("setFinderResultGeneratorMap");
		String rg = (String)sfMap.get("PATIENTSET");
		try {
			Class generatorClass = Class.forName(rg, true, Thread.currentThread().getContextClassLoader());
			IResultGenerator gi = (IResultGenerator)generatorClass.newInstance();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
