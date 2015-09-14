package edu.harvard.i2b2.crc.dao.setfinder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.crc.dao.DAOFactoryHelper;
import edu.harvard.i2b2.crc.dao.IDAOFactory;
import edu.harvard.i2b2.crc.dao.SetFinderDAOFactory;
import edu.harvard.i2b2.crc.datavo.db.QtQueryInstance;
import edu.harvard.i2b2.crc.datavo.db.QtQueryMaster;

public class QueryInstanceDaoTest {

	static SetFinderDAOFactory sfDaoFactory = null;
	static String queryMasterId = null, queryInstanceId = null;

	@BeforeClass
	public static void setUp() throws Exception {
		String hiveId = "HIVE", projectId = "/Asthma/1/1/", ownerId = "@";
		DAOFactoryHelper daoFactoryHelper = new DAOFactoryHelper(hiveId,
				projectId, ownerId);
		IDAOFactory daoFactory = daoFactoryHelper.getDAOFactory();
		sfDaoFactory = daoFactory.getSetFinderDAOFactory();

		QtQueryMaster queryMaster = new QtQueryMaster();
		queryMaster.setUserId("test_user");
		queryMaster.setGroupId("test_group_id");
		queryMaster.setName("test_name");
		queryMaster.setCreateDate(new Date(System.currentTimeMillis()));
		queryMaster.setRequestXml("test_request_xml");
		queryMaster.setDeleteFlag("N");
		queryMasterId = sfDaoFactory.getQueryMasterDAO().createQueryMaster(
				queryMaster, "i2b2xml", null);
		System.out.println("Query master id " + queryMasterId);
		IQueryInstanceDao qiDAO = sfDaoFactory.getQueryInstanceDAO();
		queryInstanceId = qiDAO.createQueryInstance(queryMasterId,
				"test_instance_user", "test_instance_group", "batch_mode", 1);

	}

	@Test
	public void getQueryInstanceByInstanceId() {
		IQueryInstanceDao qiDAO = sfDaoFactory.getQueryInstanceDAO();
		QtQueryInstance queryInstance = qiDAO
				.getQueryInstanceByInstanceId(queryInstanceId);
		assertNotNull(queryInstance);
		assertEquals("test_instance_user", queryInstance.getUserId());

	}

	@Test
	public void getQueryInstanceByMasterId() {
		IQueryInstanceDao qiDAO = sfDaoFactory.getQueryInstanceDAO();
		List<QtQueryInstance> queryInstanceList = qiDAO
				.getQueryInstanceByMasterId(queryMasterId);
		assertTrue(queryInstanceList.size() > 0);

	}

	@Test
	public void update() throws I2B2DAOException {
		IQueryInstanceDao qiDAO = sfDaoFactory.getQueryInstanceDAO();
		QtQueryInstance queryInstance = qiDAO
				.getQueryInstanceByInstanceId(queryInstanceId);
		queryInstance.getQtQueryStatusType().setStatusTypeId(3);
		qiDAO.update(queryInstance, false);
	}

}
