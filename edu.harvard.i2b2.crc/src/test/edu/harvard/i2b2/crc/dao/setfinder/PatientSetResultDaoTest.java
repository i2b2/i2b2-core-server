package edu.harvard.i2b2.crc.dao.setfinder;

import static org.junit.Assert.assertNotNull;

import java.util.Date;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import edu.harvard.i2b2.crc.dao.DAOFactoryHelper;
import edu.harvard.i2b2.crc.dao.IDAOFactory;
import edu.harvard.i2b2.crc.dao.SetFinderDAOFactory;
import edu.harvard.i2b2.crc.datavo.db.QtQueryMaster;
import edu.harvard.i2b2.crc.datavo.db.QtQueryResultInstance;

public class PatientSetResultDaoTest {

	static SetFinderDAOFactory sfDaoFactory = null;
	static String queryMasterId = null, queryInstanceId = null,
			patientSetId = null;

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
		IQueryResultInstanceDao psDAO = sfDaoFactory.getPatientSetResultDAO();
		patientSetId = psDAO.createPatientSet(queryInstanceId, "PATIETNSET");

	}

	@Test
	public void getResultInstanceList() {
		IQueryResultInstanceDao psDAO = sfDaoFactory.getPatientSetResultDAO();
		List<QtQueryResultInstance> resultList = psDAO
				.getResultInstanceList(queryInstanceId);
		System.out.println("size " + resultList.size() + " "
				+ resultList.get(0).getResultInstanceId() + " patient set id"
				+ patientSetId);
		assertNotNull(resultList);
	}

	@Test
	public void createPatientSetCollection() {
		System.out.println(" Result instance id " + patientSetId);
		IPatientSetCollectionDao psCollDAO = sfDaoFactory
				.getPatientSetCollectionDAO();
		psCollDAO.createPatientSetCollection(patientSetId);
		for (int i = 0; i < 10; i++) {
			psCollDAO.addPatient(i);
		}
		psCollDAO.flush();
	}

	@Test
	public void updatePatientSet() {
		IQueryResultInstanceDao psDAO = sfDaoFactory.getPatientSetResultDAO();
		psDAO.updatePatientSet(patientSetId, 3, 10);
	}

}
