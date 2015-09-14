package edu.harvard.i2b2.crc.dao.setfinder;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.Types;
import java.text.SimpleDateFormat;

import javax.sql.DataSource;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import edu.harvard.i2b2.crc.dao.DAOFactoryHelper;

import edu.harvard.i2b2.crc.util.QueryProcessorUtil;

public class SQLServerTest {
	private static DataSource dataSource = null;

	@BeforeClass
	public static void setUp() throws Exception { 
		QueryProcessorUtil crcUtil = QueryProcessorUtil.getInstance();
		dataSource = crcUtil.getSpringDataSource("TestDataSource");
	}
	
	@Test
	public void testSimpleDateFormat() throws Exception  { 
		
		SimpleDateFormat oracleDateFormat = new SimpleDateFormat(
	            "dd-MMM-yyyy HH:mm:ss");
	      
	    SimpleDateFormat sqlServerDateFormat =  new SimpleDateFormat(
	        			"yyyy-MM-dd'T'HH:mm:ss");
	    
	    //oracleDateFormat.
	    String theDateFrom = "1979-11-01Z";
	    
	    DatatypeFactory dataTypeFactory = DatatypeFactory.newInstance();
    	XMLGregorianCalendar cal = dataTypeFactory.newXMLGregorianCalendar(theDateFrom);
    	String theOracleDateFrom = oracleDateFormat.format(cal.toGregorianCalendar().getTime()); 
    	String theSqlServerDateFrom = sqlServerDateFormat.format(cal.toGregorianCalendar().getTime());
	    System.out.println("Oracle"+ theOracleDateFrom);
	    System.out.println("SqlServer"+ theSqlServerDateFrom);
	    
	}

	@Ignore
	@Test
	public void generateQueryMasterSequence() throws Exception { 
		CallableStatement cstmt = dataSource.getConnection().prepareCall("{? = call dbo.p_get_next_sequence(?)}");
    	cstmt.registerOutParameter(1, Types.INTEGER);
        cstmt.setString(2, "TEST_SEQUENCE_ID");
        cstmt.execute();
        int testSeq = cstmt.getInt(1);
        System.out.println("Sequence Id " + testSeq);
	}
	
	@Ignore
	@Test
	public void tempTableTest() throws Exception { 
		 String createSql = "CREATE  TABLE #QUERY_GLOBAL_TEMP   ( " + 
		 " ENCOUNTER_NUM int, "  + 
		 " PATIENT_NUM int, " +
		 " PANEL_COUNT int, " + 
		 " fact_count int, " + 
		 " fact_panels int " + 
		 " )"; 
		 Connection conn = dataSource.getConnection();
		 Statement stmt = conn.createStatement();
		stmt.executeUpdate(createSql);
		createSql = " CREATE  TABLE #DX  ( " + 
				 " ENCOUNTER_NUM int, " + 
				 " PATIENT_NUM int " + 
				 " ) " ;
		stmt.executeUpdate(createSql);
		
		stmt.executeUpdate("insert into #query_global_temp(encounter_num) values(1)");
		stmt.close();
		conn.close();
	}
	
}
