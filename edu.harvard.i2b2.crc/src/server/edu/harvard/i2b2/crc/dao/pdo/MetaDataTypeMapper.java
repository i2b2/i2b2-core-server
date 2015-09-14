package edu.harvard.i2b2.crc.dao.pdo;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MetaDataTypeMapper {
	
	/** log **/
	protected final Log log = LogFactory.getLog(getClass());
	
	String stringDataType[] = {"varchar","varchar2","char","nchar"};
	String dateDataType[] = {"date","datetime"};
	String intDataType[] = {"int","number","bigint"};
	String decimalDataType[] = {"number","decimal","float"};
	List<String> stringDataList,dateDataList,intDataList,decimalDataList;
	
	public MetaDataTypeMapper() { 
		stringDataList = Arrays.asList(stringDataType);
		dateDataList = Arrays.asList(dateDataType);
		intDataList = Arrays.asList(intDataType);
		decimalDataList = Arrays.asList(decimalDataType);
		
	}
	
	public void fillXmlDataType(MetadataDao.TableMetaData[] tableMetaDataList, boolean tablePDOFlag) {
		for (int i=0;i<tableMetaDataList.length;i++) { 
			tableMetaDataList[i].column_xml_type = getXmlDataType(tableMetaDataList[i].column_type, tablePDOFlag);
			log.debug("PDO mapping " + tableMetaDataList[i].column_type + "   " + tableMetaDataList[i].column_xml_type + " " + tableMetaDataList[i].column_name);
		}
	}
	

	public String getXmlDataType(String dbDataType, boolean tablePDOFlag  ) {
		String xmlDataType = "";
		if (stringDataList.contains(dbDataType)) { 
			xmlDataType = "string";
		} else if (dateDataList.contains(dbDataType)) { 
			System.out.println("dateTime");
			xmlDataType = "dateTime";
		} else if (intDataList.contains(dbDataType) ) { 
			xmlDataType = "int";
		} else if (decimalDataList.contains(dbDataType)) { 
			xmlDataType = "decimal";
		}  else { 
			log.debug("Data type not matching [" + dbDataType + "] default to string " );
			if (tablePDOFlag) { 
				xmlDataType = "defaulted:string";
			} else { 
				xmlDataType = "string";
			}
		}
		return xmlDataType;
	}
}
