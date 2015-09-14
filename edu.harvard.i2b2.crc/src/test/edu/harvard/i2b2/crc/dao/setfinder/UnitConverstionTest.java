package edu.harvard.i2b2.crc.dao.setfinder;

import static org.junit.Assert.assertNotNull;

import java.util.Date;
import java.util.List;

import org.w3c.dom.Element;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.util.xml.XMLUtil;
import edu.harvard.i2b2.crc.axis2.PdoQueryTest;
import edu.harvard.i2b2.crc.dao.DAOFactoryHelper;
import edu.harvard.i2b2.crc.dao.IDAOFactory;
import edu.harvard.i2b2.crc.dao.SetFinderDAOFactory;
import edu.harvard.i2b2.crc.dao.setfinder.querybuilder.UnitConverstionUtil;
import edu.harvard.i2b2.crc.datavo.db.QtQueryMaster;
import edu.harvard.i2b2.crc.datavo.db.QtQueryResultInstance;
import edu.harvard.i2b2.crc.datavo.setfinder.query.ItemType;

public class UnitConverstionTest {
	static SetFinderDAOFactory sfDaoFactory = null;
	static String queryMasterId = null, queryInstanceId = null,
			patientSetId = null;
	private static String testFileDir = null;
	

	@BeforeClass
	public static void setUp() throws Exception {
		testFileDir = System.getProperty("testfiledir");
		System.out.println("test file dir " + testFileDir);

		if (!((testFileDir != null) && (testFileDir.trim().length() > 0))) {
			throw new Exception(
					"please provide test file directory info -Dtestfiledir");
		}

	}

	@Test
	public void getResultInstanceList() throws Exception {
		//read the xml
		String filename = testFileDir + "/metadata_value.xml";
		
		//String filename = testFileDir + "/setfinder_query_textconstraint_IN.xml";
		//String filename = testFileDir + "/setfinder_infra3_enc.xml";
		
		String requestString = PdoQueryTest.getQueryString(filename);
		System.out.println("metadata xml " + requestString);
		
		Document doc = XMLUtil.convertStringToDOM(requestString);
		org.w3c.dom.Element element = doc.getDocumentElement();
		//call the helper function to  convert the xml to switch statement
		if (element != null) { 
			//NodeList enumUnitNodeList = element.getElementsByTagName("EnumValues");
			//System.out.println(" enumlist first value " + enumUnitNodeList.item(0).getFirstChild().getTextContent());
			NodeList normalUnitNodeList = element.getElementsByTagName("NormalUnits");
			NodeList equalUnitNodeList = element.getElementsByTagName("EqualUnits");
			NodeList convertingUnitNodeList = element.getElementsByTagName("ConvertingUnits");
			System.out.println(" normal units " + normalUnitNodeList.item(0).getNodeName());
		}
		
		UnitConverstionUtil unitConverstionUtil = new UnitConverstionUtil();
		String unitCdSwitch = unitConverstionUtil.buildUnitCdSwitchClause(element,false,""); 
		String unitInClause = unitConverstionUtil.buildUnitCdInClause(element,"");
		System.out.println(" sql :  " + unitCdSwitch + unitInClause);
		
		//String unitCdSql = this.callUnitCdConversion( element);
		
		//assertNotNull(resultList);
		
	}
	

	
}
