package edu.harvard.i2b2.crc.dao.setfinder;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.crc.datavo.db.QtQueryResultType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.ItemType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.PanelType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.PanelType.TotalItemOccurrences;
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryDefinitionRequestType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.ResultOutputOptionListType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.ResultOutputOptionType;
import edu.harvard.i2b2.crc.util.QueryProcessorUtil;

public class CheckSkipTempTable {
	
	private static Log log = LogFactory.getLog(CheckSkipTempTable.class);
	
	public CheckSkipTempTable() {
		
	}
	
	public boolean getSkipTempTable(QueryDefinitionRequestType queryDefRequestType, ResultOutputOptionListType resultOutputList )  {
		boolean  patientCountResultTypeFlag = false;
		boolean notMorethanOneResultType = false, samePanelTiming = true, itemWithStandardConcept = true;
		boolean totOccurencesMoreThanOne = false, accuracyScaleFlag = false;
		boolean totConceptCount = true, panelInvert = false;
		QueryProcessorUtil qpUtil = QueryProcessorUtil.getInstance();
		int maxConceptCount = 40;
		
		try { 
			String maxConceptCountStr = qpUtil.getCRCPropertyValue(QueryProcessorUtil.SINGLEPANEL_SKIPTEMPTABLE_MAXCONCEPT_PROPERTIES);
			if (maxConceptCountStr != null) { 
				maxConceptCount = Integer.parseInt(maxConceptCountStr); 
			} else { 
				log.info("Setfinder query without temp table defaulting [" + QueryProcessorUtil.SINGLEPANEL_SKIPTEMPTABLE_MAXCONCEPT_PROPERTIES + "] to 40 ");
			}
		} catch (I2B2Exception i2b2Ex) { 
			log.info("Setfinder query without temp table defaulting [" + QueryProcessorUtil.SINGLEPANEL_SKIPTEMPTABLE_MAXCONCEPT_PROPERTIES + "] to 40 ");
		}
		
		//skip table disabled in version 1.7
		try {
			String queryGeneratorVersion = qpUtil
					.getCRCPropertyValue("edu.harvard.i2b2.crc.setfinder.querygenerator.version");
			if (queryGeneratorVersion==null||queryGeneratorVersion.equalsIgnoreCase("1.7"))
				return false;
		} catch (I2B2Exception e) {
			return false;
		}
		
		//check query def query timing
		if (queryDefRequestType.getQueryDefinition().getQueryTiming() != null) { 
			if (queryDefRequestType.getQueryDefinition().getQueryTiming().trim().length()>0) { 
				if (!queryDefRequestType.getQueryDefinition().getQueryTiming().equalsIgnoreCase("ANY")) { 
					log.debug("Setfinder query without temp table [Querydefinition's query timing is not ANY]");
					return false;
				}
			}
		}
		
		//check if all panel are same timing and the concept in the panel are regular concepts
		String prevPanelTiming = null, currPanelTiming = null;
		List<PanelType> panelList = queryDefRequestType.getQueryDefinition().getPanel();
		int conceptCount = 0;
		for (Iterator<PanelType> iterator = panelList.iterator(); iterator.hasNext();) { 
			PanelType panelType = iterator.next();
			TotalItemOccurrences totOcc = panelType.getTotalItemOccurrences();
			
			if (totOcc != null && totOcc.getValue()>1) { 
				totOccurencesMoreThanOne = true;
				log.debug("Setfinder query without temp table panel's total occurences greater than 1 [ " + totOcc.getValue() + " ]");
				break;
			}
			
			
			/*int accuracyScale = panelType.getPanelAccuracyScale();
			if (accuracyScale > 0) {
				accuracyScaleFlag = true;
				log.debug("Setfinder query without temp table panel's accuracy scale greater than 1 [ " + accuracyScale + " ]");
				break;
			}*/
			
			//check if the panel timing is ANY
			currPanelTiming = panelType.getPanelTiming();
			if (currPanelTiming != null && currPanelTiming.trim().length()>0) { 
				if (!currPanelTiming.equalsIgnoreCase("ANY")) { 
					log.debug("Setfinder query without temp table the panel timing not ANY, it is [" + currPanelTiming + "]");
					samePanelTiming = false;
					break;
				}
			}
			
			int invert = panelType.getInvert();
			if (invert > 0 ) {
				log.debug("Setfinder query without temp table the panel invert is ON");
				panelInvert = true;
				break;
			}
			
			
			
			//check if the item in the panel is not special item 
			if (panelType.getItem() != null) {
				List<ItemType> itemList  = panelType.getItem();
				for (Iterator<ItemType> itemIterator = itemList.iterator();itemIterator.hasNext();) { 
					conceptCount++;
					ItemType item = itemIterator.next();
					if (!item.getItemKey().trim().startsWith("\\")) {
						log.debug("Setfinder query without temp table special item check [" + item.getItemKey() + "]");
						itemWithStandardConcept = false;
						break;
					}
					
					//check if the max concept count is reached
					if (conceptCount>maxConceptCount) { 
						totConceptCount = false;
						log.debug("Setfinder query without temp table reached the max concept count maxCount [" + maxConceptCount + " ]");
					}
				}
			}
			
		
			
		}
		
		//check if the result list has only the patient count xml
		if (resultOutputList != null && resultOutputList.getResultOutput() != null)  {
			if  (resultOutputList.getResultOutput().size()<=1) {
				notMorethanOneResultType = true;
			}
		
			for (Iterator<ResultOutputOptionType> iterator = resultOutputList.getResultOutput().iterator();iterator.hasNext();) {
				ResultOutputOptionType resultOutputType = iterator.next();
				if (resultOutputType.getName().equalsIgnoreCase(QtQueryResultType.PATIENT_COUNT_XML)) {
					patientCountResultTypeFlag = true;
					break;
				}
			}
		} 
		
		
		if (patientCountResultTypeFlag && notMorethanOneResultType && itemWithStandardConcept && samePanelTiming &&  totConceptCount && 
				!totOccurencesMoreThanOne && !panelInvert && !accuracyScaleFlag) { 
			log.info("Setfinder query without temp table is [true]"); 
			return true;
		} else  {
			log.debug("Setfinder query without temp table [false]");
			return false;
		}
	}
}
