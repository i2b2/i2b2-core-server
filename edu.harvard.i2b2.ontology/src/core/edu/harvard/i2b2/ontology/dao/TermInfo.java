
package edu.harvard.i2b2.ontology.dao;

import java.util.Optional;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvBindByPosition;

import edu.harvard.i2b2.ontology.dao.lucene.parser.OntologyRow;

public  class TermInfo implements OntologyRow {
    	public int getHlevel() {
			return hlevel;
		}



		public void setHlevel(int hlevel) {
			this.hlevel = hlevel;
		}



		public String getPath() {
			return path;
		}



		public void setPath(String path) {
			this.path = path;
		}



		public String getName() {
			return name;
		}



		public void setName(String name) {
			this.name = name;
		}



		public String getSynonymCd() {
			return synonymCd;
		}



		public void setSynonymCd(String synonymCd) {
			this.synonymCd = synonymCd;
		}



		public String getVisualAttributes() {
			return visualAttributes;
		}



		public void setVisualAttributes(String visualAttributes) {
			this.visualAttributes = visualAttributes;
		}



		public String getBasecodeOption() {
			return basecodeOption;
		}



		public void setBasecodeOption(String  basecodeOption) {
			this.basecodeOption = basecodeOption;
		}



		public String getMetadataXmlOption() {
			return metadataXmlOption;
		}



		public void setMetadataXmlOption(String metadataXmlOption) {
			this.metadataXmlOption = metadataXmlOption;
		}



		public String getTooltipOption() {
			return tooltipOption;
		}



		public void setTooltipOption(String tooltipOption) {
			this.tooltipOption = tooltipOption;
		}



		public String getMAppliedPathOption() {
			return mAppliedPathOption;
		}



		public void setMAppliedPathOption(String mAppliedPathOption) {
			this.mAppliedPathOption = mAppliedPathOption;
		}

        public boolean isHidden() { return visualAttributes.length() > 1 && visualAttributes.charAt(1) == 'H'; }
        public boolean isSynonym() { return "Y".equals(synonymCd); }
        public boolean isRoot() { return false; }
        public boolean isModifier() { return mAppliedPathOption != null|| !mAppliedPathOption.contains("@"); }

//"C_HLEVEL"|"C_FULLNAME"|"C_NAME"|"C_SYNONYM_CD"|"C_VISUALATTRIBUTES"|"C_BASECODE"|"C_METADATAXML"|"C_TOOLTIP"|"M_APPLIED_PATH"
        @CsvBindByPosition(position = 0)
    	@CsvBindByName(column = "C_HLEVEL")
        public  int hlevel;
		 @CsvBindByPosition(position = 1)
		@CsvBindByName(column = "C_FULLNAME")
        public  String path;
		 @CsvBindByPosition(position = 2)
    	@CsvBindByName(column = "C_NAME")
        public  String name;
		 @CsvBindByPosition(position = 3)
    	@CsvBindByName(column = "C_SYNONYM_CD")
        public  String synonymCd;
		 @CsvBindByPosition(position = 4)
    	@CsvBindByName(column = "C_VISUALATTRIBUTES")
        public  String visualAttributes;
		 @CsvBindByPosition(position = 5)
    	@CsvBindByName(column = "C_BASECODE")
        public String basecodeOption;
		 @CsvBindByPosition(position = 6)
    	@CsvBindByName(column = "C_METADATAXML")
        public  String metadataXmlOption;
		 @CsvBindByPosition(position = 7)
    	@CsvBindByName(column = "C_TOOLTIP")
        public  String tooltipOption;
		 @CsvBindByPosition(position = 8)
    	@CsvBindByName(column = "M_APPLIED_PATH")
        public  String mAppliedPathOption;

    	public TermInfo()
    	{
    	
    	}
 /*
        public TermInfo(int hlevel, String path, String name, String synonymCd, String visualAttributes, Optional<String> basecodeOption, Optional<String> metadataXmlOption, Optional<String> tooltipOption, Optional<String> mAppliedPathOption) {
            this.hlevel = hlevel;
            this.path = path;
            this.name = name;
            this.synonymCd = synonymCd;
            this.visualAttributes = visualAttributes;
            this.basecodeOption = basecodeOption;
            this.metadataXmlOption = metadataXmlOption;
            this.tooltipOption = tooltipOption;
            this.mAppliedPathOption = mAppliedPathOption;
        }
        */
/*
        public int getHlevel() { return hlevel; }
         public String getPath() { return path; }
         public String getName() { return name; }
         public String getSynonymCd() { return synonymCd; }
         public String getVisualAttributes() { return visualAttributes; }
         public Optional<String> getBasecodeOption() { return basecodeOption; }
         public Optional<String> getMetadataXmlOption() { return metadataXmlOption; }
         public Optional<String> getTooltipOption() { return tooltipOption; }
        public Optional<String> getMAppliedPathOption() { return mAppliedPathOption; }
         public boolean isHidden() { return visualAttributes.length() > 1 && visualAttributes.charAt(1) == 'H'; }
         public boolean isSynonym() { return "Y".equals(synonymCd); }
         public boolean isRoot() { return false; }
        public boolean isModifier() { return !mAppliedPathOption.isPresent() || !mAppliedPathOption.get().contains("@"); }
       */ 
    }