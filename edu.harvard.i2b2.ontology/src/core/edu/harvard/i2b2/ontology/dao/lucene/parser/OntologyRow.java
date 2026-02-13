package edu.harvard.i2b2.ontology.dao.lucene.parser;

import java.util.Optional;


	  public interface OntologyRow {
	        int getHlevel();
	        String getPath();
	        String getName();
	        String getSynonymCd();
	        String getVisualAttributes();
	        String getBasecodeOption();
	        String getMetadataXmlOption();
	        String getTooltipOption();
	        boolean isHidden();
	        boolean isSynonym();
	        boolean isRoot();
	       
	    }

