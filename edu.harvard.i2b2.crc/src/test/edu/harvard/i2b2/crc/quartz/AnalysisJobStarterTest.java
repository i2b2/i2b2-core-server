package edu.harvard.i2b2.crc.quartz;

import javax.sql.DataSource;

import org.junit.BeforeClass;
import org.junit.Test;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.crc.dao.DAOFactoryHelper;
import edu.harvard.i2b2.crc.datavo.db.DataSourceLookup;
import edu.harvard.i2b2.crc.util.QueryProcessorUtil;

public class AnalysisJobStarterTest {

	static AnalysisJobStarter jobStarter = null;

	@BeforeClass
	public static void init() throws I2B2Exception {
		QueryProcessorUtil qpUtil = QueryProcessorUtil.getInstance();
		DataSourceLookup dataSourceLookup = (DataSourceLookup) qpUtil
				.getSpringBeanFactory().getBean("TestDataSourceLookup");
		// instanciate datasource
		DataSource dataSource = QueryProcessorUtil.getInstance()
				.getSpringDataSource(dataSourceLookup.getDataSource());

		DAOFactoryHelper daoHelper = new DAOFactoryHelper(dataSourceLookup,
				dataSource);

		jobStarter = new AnalysisJobStarter(daoHelper.getDAOFactory());
	}

	@Test
	public void testStart() throws I2B2Exception {
		String instanceId = "10172";
		long timeout = 60L * 1000L;
		jobStarter.start(instanceId, timeout);
	}

}
