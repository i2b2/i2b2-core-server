/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
package edu.harvard.i2b2.crc.dao.setfinder.querybuilder;
 
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.stream.XMLStreamException;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.exception.StackTraceUtil;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.crc.datavo.i2b2message.SecurityType;
import edu.harvard.i2b2.crc.datavo.ontology.ConceptType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.ItemType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.PanelType;
import edu.harvard.i2b2.crc.delegate.ontology.CallOntologyUtil;
import edu.harvard.i2b2.crc.util.ItemKeyUtil;
import edu.harvard.i2b2.crc.util.QueryProcessorUtil;

public class SortPanel {
	/** log **/
	protected final Log log = LogFactory.getLog(getClass());

	public List<PanelType> sortedPanelList(List<PanelType> panelList,
			SecurityType securityType, String projectId) throws I2B2Exception {

		Map<Integer, Integer> panelTotalMap = new HashMap<Integer, Integer>();
		Map<Integer, Integer> invertPanelTotalMap = new HashMap<Integer, Integer>();
		Map<Integer, PanelType> panelMap = new HashMap<Integer, PanelType>();
		Map<Integer, PanelType> invertPanelMap = new HashMap<Integer, PanelType>();
		int panelIndex = 0, invertPanelIndex = 0;
		List<PanelType> sortedPanelArray = new ArrayList<PanelType>();
		List<PanelType> sortedInvertPanelArray = new ArrayList<PanelType>();
		for (PanelType panelType : panelList) {
			
			List<ItemType> itemList = panelType.getItem();
			int invert = panelType.getInvert();
			// calculate the total for each item
			int panelTotal = 0;
			String itemKey = null;
			for (ItemType itemType : itemList) {
				ConceptType conceptType = null;

				try {
					itemKey = itemType.getItemKey();
						if (ItemKeyUtil.isConceptKey(itemKey)) { 
						//conceptType = ontologyUtil.callOntology(itemKey);
						 conceptType = CallOntologyUtil.callOntology(itemKey, securityType, projectId, QueryProcessorUtil.getInstance().getOntologyUrl());

						if (conceptType != null && conceptType.getTotalnum() !=null) {
							panelTotal += conceptType.getTotalnum();
						}
					}

				} catch (AxisFault e) {
					log.error("Error while fetching metadata [" + itemKey
							+ "] from ontology ", e);
					throw new OntologyException(
							"Error while fetching metadata [" + itemKey
									+ "] from ontology "
									+ StackTraceUtil.getStackTrace(e));
				} catch (XMLStreamException e) {
					log.error("Error while fetching metadata [" + itemKey
							+ "] from ontology ", e);
					throw new OntologyException(
							"Error while fetching metadata [" + itemKey
									+ "] from ontology "
									+ StackTraceUtil.getStackTrace(e));
				} catch (JAXBUtilException e) {
					log.error("Error while fetching metadata [" + itemKey
							+ "] from ontology ", e);
					throw new OntologyException(
							"Error while fetching metadata [" + itemKey
									+ "] from ontology "
									+ StackTraceUtil.getStackTrace(e));
				}

			}
			
			
			
			if (invert == 1) { 
				invertPanelIndex++;
				invertPanelTotalMap.put(invertPanelIndex, panelTotal*-1);
				invertPanelMap.put(invertPanelIndex, panelType);
				//panelTotalMap.put(panelIndex, panelTotal);
			} else {
				panelIndex++;
				panelTotalMap.put(panelIndex, panelTotal);
				panelMap.put(panelIndex, panelType);
			}
			
			log.debug("Panel's Total num [" + panelTotal
					+ "] and the panel index [" + panelIndex + "]");

		}

		
		HashMap yourMap = new HashMap();
		HashMap map = new LinkedHashMap();
		List yourMapKeys = new ArrayList(panelTotalMap.keySet());
		List yourMapValues = new ArrayList(panelTotalMap.values());
		List sortedMapValues = new ArrayList(yourMapValues);
		Collections.sort(sortedMapValues);
		int size = yourMapValues.size();
		int indexInMapValues = 0;
		for (int i = 0; i < size; i++) {
			indexInMapValues = yourMapValues.indexOf(sortedMapValues.get(i));
			map.put(yourMapKeys.get(indexInMapValues), sortedMapValues.get(i));
			yourMapValues.set(indexInMapValues, -1);
		}
		Set ref = map.keySet();
		Iterator it = ref.iterator();
		int panelIndexHash = 0;
		while (it.hasNext()) {
			panelIndexHash = (Integer) it.next();
			sortedPanelArray.add(panelMap.get(panelIndexHash));
		}
		log.debug("size of sorted panel array [" + sortedPanelArray.size() + "]");
		
		HashMap yourInvertMap = new HashMap();
		HashMap invertMap = new LinkedHashMap();
		List yourInvertMapKeys = new ArrayList(invertPanelTotalMap.keySet());
		List yourInvertMapValues = new ArrayList(invertPanelTotalMap.values());
		List sortedInvertMapValues = new ArrayList(yourInvertMapValues);
		Collections.sort(sortedInvertMapValues);
		
		 size = yourInvertMapValues.size();
		 indexInMapValues = 0;
		for (int i = 0; i < size; i++) {
			indexInMapValues = yourInvertMapValues.indexOf(sortedInvertMapValues.get(i));
			invertMap.put(yourInvertMapKeys.get(indexInMapValues), sortedInvertMapValues.get(i));
			yourInvertMapValues.set(indexInMapValues, -1);
		}
		Set ref1 = invertMap.keySet();
		Iterator it1 = ref1.iterator();
		panelIndexHash = 0;
		while (it1.hasNext()) {
			panelIndexHash = (Integer) it1.next();
			sortedInvertPanelArray.add(invertPanelMap.get(panelIndexHash));
		}
		
		log.debug("size of sorted exclude panel array [" + sortedInvertPanelArray.size() + "]");
		
		sortedPanelArray.addAll(sortedInvertPanelArray);
		
		
		return sortedPanelArray;

	}
}
