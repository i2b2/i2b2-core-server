package edu.harvard.i2b2.crc.dao.pdo;

import org.junit.Test;

import edu.harvard.i2b2.crc.dao.DataSourceLookupHelper;
import edu.harvard.i2b2.crc.datavo.db.DataSourceLookup;

public class DataSourceLookupTest {

	@Test
	public void testLookupHelper() throws Exception  { 
		DataSourceLookupHelper helper = new DataSourceLookupHelper();
		DataSourceLookup ds = helper.matchDataSource("Demo", "/Asthma2/1/1/4", "raj");
		System.out.println(ds.getDataSource());
	}
	
	public static void  main(String args[]) { 
		String projectId = "/Asthma/a/";
		String[] individualProjects = projectId.split("/");
		int i =0;
		while (i<individualProjects.length) { 
			System.out.println("individual token" + individualProjects[i++]);
		}
		projectId = "/Asthma/1/2/3/4/";
		System.out.println(projectId.lastIndexOf('/', 5) + projectId.substring(0,6));
		
		int projectLevel = projectId.length();
		boolean flag = false;
		while ((projectLevel=projectId.lastIndexOf('/', projectLevel))>0) {
			
			System.out.println("project id :" + projectId.substring(0, projectLevel+1));
			
			projectLevel = projectId.lastIndexOf('/', projectLevel-1);
		}
	}
	
	
	
	
}
