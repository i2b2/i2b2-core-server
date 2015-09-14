package edu.harvard.i2b2.crc.dao.pdo.input;

import static org.junit.Assert.assertNotNull;

import java.util.HashMap;

import javax.sql.DataSource;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import edu.harvard.i2b2.common.util.jaxb.JAXBUnWrapHelper;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtil;
import edu.harvard.i2b2.crc.axis2.CRCAxisAbstract;
import edu.harvard.i2b2.crc.dao.DAOFactoryHelper;
import edu.harvard.i2b2.crc.datavo.CRCJAXBUtil;
import edu.harvard.i2b2.crc.datavo.db.DataSourceLookup;
import edu.harvard.i2b2.crc.datavo.i2b2message.RequestMessageType;
import edu.harvard.i2b2.crc.datavo.pdo.query.GetPDOFromInputListRequestType;
import edu.harvard.i2b2.crc.util.QueryProcessorUtil;

public class PagingHandlerTest {
	private static String testFileDir = null;
	private static GetPDOFromInputListRequestType pdoRequestType = null;
	private static DataSourceLookup dataSourceLookup = null;
	private static String pageMethod = null;

	@BeforeClass
	public static void setUp() throws Exception {
		testFileDir = System.getProperty("testfiledir");
		System.out.println("test file dir " + testFileDir);

		if (!((testFileDir != null) && (testFileDir.trim().length() > 0))) {
			throw new Exception(
					"please provide test file directory info -Dtestfiledir");
		}

		QueryProcessorUtil qpUtil = QueryProcessorUtil.getInstance();
		dataSourceLookup = (DataSourceLookup) qpUtil.getSpringBeanFactory()
				.getBean("TestDataSourceLookup");

		String filename = testFileDir
				+ "/edu.harvard.i2b2.crc.dao.pdo/pdo_query.xml";

		// String filename = testFileDir + "/mikeincrement.xml";
		String xml = CRCAxisAbstract.getQueryString(filename);
		JAXBUtil jaxbUtil = CRCJAXBUtil.getJAXBUtil();
		RequestMessageType reqMsgType = (RequestMessageType) jaxbUtil
				.unMashallFromString(xml).getValue();
		System.out.println(reqMsgType.getMessageHeader().getMessageControlId());
		JAXBUnWrapHelper unWrapHelper = new JAXBUnWrapHelper();
		pdoRequestType = (GetPDOFromInputListRequestType) unWrapHelper
				.getObjectByClass(reqMsgType.getMessageBody().getAny(),
						GetPDOFromInputListRequestType.class);

		IFactRelatedQueryHandler pdoQueryHandler = null;
		if (dataSourceLookup.getServerType().equalsIgnoreCase(
				DAOFactoryHelper.ORACLE)) {
			pdoQueryHandler = new FactRelatedQueryHandler(dataSourceLookup,
					pdoRequestType.getInputList(), pdoRequestType
							.getFilterList(), pdoRequestType.getOutputOption());
		}
		pageMethod = qpUtil.getPagingMethod();

	}

	@Ignore
	@Test
	public void buildSql() throws Exception {
		DataSource dataSource = null;
		DAOFactoryHelper daoHelper = new DAOFactoryHelper(dataSourceLookup,
				dataSource);
		assertNotNull(dataSourceLookup);

		PagingHandler ph = new PagingHandler(daoHelper, pdoRequestType
				.getInputList(), pdoRequestType.getFilterList(), pageMethod);

		System.out.println("Total for the panel " + ph.getTotal(10));

	}

	@Ignore
	@Test
	public void buildMinSql() throws Exception {

		QueryProcessorUtil queryProcessorUtil = QueryProcessorUtil
				.getInstance();
		DataSource dataSource = queryProcessorUtil
				.getSpringDataSource("java:CRC_ASTHMADS_ORACLE");
		DAOFactoryHelper daoHelper = new DAOFactoryHelper(dataSourceLookup,
				dataSource);
		assertNotNull(dataSourceLookup);

		PagingHandler ph = new PagingHandler(daoHelper, pdoRequestType
				.getInputList(), pdoRequestType.getFilterList(), pageMethod);

		HashMap map = ph.getMinPatientIndexAndTheTotal(10);
		System.out.println("min index" + map.get("MIN_INDEX"));
		System.out.println("min index total " + map.get("MIN_INDEX_TOTAL"));
		// System.out.println("Min index " +
		// ph.getMinPatientIndexAndTheTotal(10));

	}

	@Test
	public void calculateMaxPageInputList() throws Exception {

		QueryProcessorUtil queryProcessorUtil = QueryProcessorUtil
				.getInstance();
		DataSource dataSource = queryProcessorUtil
				.getSpringDataSource("java:CRC_ASTHMADS_ORACLE");

		DAOFactoryHelper daoHelper = new DAOFactoryHelper(dataSourceLookup,
				dataSource);

		assertNotNull(dataSourceLookup);
		assertNotNull(dataSource);

		PagingHandler ph = new PagingHandler(daoHelper, pdoRequestType
				.getInputList(), pdoRequestType.getFilterList(), pageMethod);
		HashMap map = ph.calculateMaxPageInputList();
		System.out.println("Patients list size of  ["
				+ map.get(PagingHandler.MAX_INPUT_LIST) + "]  fits the page ");

	}
}
