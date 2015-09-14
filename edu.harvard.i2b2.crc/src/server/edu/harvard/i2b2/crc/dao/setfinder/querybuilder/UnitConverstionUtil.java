package edu.harvard.i2b2.crc.dao.setfinder.querybuilder;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.harvard.i2b2.common.exception.I2B2DAOException;

public class UnitConverstionUtil {

	
	public String buildUnitCdInClause(Element rootElement,String tableNamePrefix ) throws I2B2DAOException  { 
		if (rootElement == null) {
			return " " ;
		}
		
		NodeList normalUnitNodeList = rootElement.getElementsByTagName("NormalUnits");
		String normalUnitCd = "", equalUnitCd = ""; 
		if (normalUnitNodeList != null && normalUnitNodeList.getLength()>0 && normalUnitNodeList.item(0)!= null) { 
			normalUnitCd = normalUnitNodeList.item(0).getTextContent();
			if (normalUnitCd != null) {
				normalUnitCd = "'" + normalUnitCd.replaceAll("'", "''") + "'";
			}
		}
		
		NodeList equalUnitNodeList = rootElement.getElementsByTagName("EqualUnits");
		int nodeLength = equalUnitNodeList.getLength();
		int i = 0 ;
		Node unitNode = null;
		String unitNodeContent = "" ;
		while (i<nodeLength) {
			unitNode = equalUnitNodeList.item(i); 
			if (unitNode != null) { 
				unitNodeContent = unitNode.getTextContent();
				if (unitNodeContent != null && unitNodeContent.trim().length()>0) {
					equalUnitCd += "'" + unitNodeContent.replaceAll("'", "''") +  "'";
					if (i+1<nodeLength) {
						equalUnitCd += " , ";
					}
				}
				       
			}
			i++;
		}
		
		String unitNodeInClause = "", comma = "";
		
		if (equalUnitCd.length() > 0 || normalUnitCd.length() > 0) { 
			if (normalUnitCd != null && equalUnitCd != null) { 
				comma = " , ";
			}
			unitNodeInClause = "  " + "  ( "  + normalUnitCd + comma +  equalUnitCd +" )  " ;
				//case when 'a' in ('a','b') then 1 else 0 end  =1
		}
		
		return unitNodeInClause;
	}
	
	public String buildUnitCdSwitchClause(Element rootElement, boolean selectFlag, String tableNamePrefix) throws I2B2DAOException { 
		if (rootElement == null) {
			return " " ;
		}
		
		NodeList convertingUnitNodeList = rootElement.getElementsByTagName("ConvertingUnits");
		
		
		int nodeLength = convertingUnitNodeList.getLength();
		int i=0;
		NodeList childList = null;
		String singleUnitCd = "", singleMultiplyingFactor = "";
		String multiplyingFactor = "";
		while (i<nodeLength) {
			Node  node = convertingUnitNodeList.item(i);
			childList = node.getChildNodes();
			singleUnitCd = ""; singleMultiplyingFactor = "";
			for (int j=0;j<childList.getLength();j++) {
				Node node1 = childList.item(j);
				String firstNodeName = node1.getNodeName();
				if (firstNodeName.equalsIgnoreCase("Units")) { 
					singleUnitCd = node1.getTextContent(); 
				}
				if (firstNodeName.equalsIgnoreCase("MultiplyingFactor")) {
					//
					singleMultiplyingFactor = node1.getTextContent();
				}
			 
				
			}
			if (singleUnitCd !=null && singleMultiplyingFactor !=null && singleUnitCd.trim().length()>0 && singleMultiplyingFactor.trim().length()>0 ) {
				multiplyingFactor += "\n when  " + tableNamePrefix + "units_cd = '" + singleUnitCd  + "' then " + tableNamePrefix + "nval_num * " + singleMultiplyingFactor;
			}
			
			i++;
		}
		
		
		NodeList normalUnitNodeList = rootElement.getElementsByTagName("NormalUnits");
		String normalUnitCd = "", equalUnitCd = ""; 
		if (normalUnitNodeList != null && normalUnitNodeList.getLength()>0 && normalUnitNodeList.item(0)!= null) { 
			normalUnitCd = normalUnitNodeList.item(0).getTextContent();
			if (normalUnitCd != null && normalUnitCd.trim().length()>0) {
				multiplyingFactor += "\n when  " + tableNamePrefix + "units_cd = '" + normalUnitCd  + "' then " + tableNamePrefix + "nval_num  " ;
			}
		}
		
		NodeList equalUnitNodeList = rootElement.getElementsByTagName("EqualUnits");
		nodeLength = equalUnitNodeList.getLength();
		i = 0 ;
		Node unitNode = null;
		String unitNodeContent = "" ;
		while (i<nodeLength) {
			unitNode = equalUnitNodeList.item(i); 
			if (unitNode != null) { 
				unitNodeContent = unitNode.getTextContent();
				if (unitNodeContent != null && unitNodeContent.trim().length()>0) {
					multiplyingFactor += "\n when  " + tableNamePrefix + "units_cd = '" + unitNodeContent  + "' then " + tableNamePrefix + "nval_num  " ;
				}
			}
			i++;
		}
		
		if (multiplyingFactor.length()>0) {
			//" else " + tableNamePrefix + "nval_num end" removed else 
			multiplyingFactor =   " case  " + multiplyingFactor + " end "; 
			if ( selectFlag) { 
				multiplyingFactor += " " + tableNamePrefix + "nval_num " ;  
			}
		}
		
		
		return multiplyingFactor;
	}
	
	
}
