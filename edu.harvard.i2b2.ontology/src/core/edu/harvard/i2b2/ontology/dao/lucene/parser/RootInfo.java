package edu.harvard.i2b2.ontology.dao.lucene.parser;

import java.util.Optional;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvBindByPosition;

public  class RootInfo implements OntologyRow {
//	"C_TABLE_CD"|"C_TABLE_NAME"|"C_HLEVEL"|"C_FULLNAME"|"C_NAME"|"C_SYNONYM_CD"|"C_VISUALATTRIBUTES"|"C_BASECODE"|"C_METADATAXML"|"C_TOOLTIP"

    public String getTableCd() {
		return tableCd;
	}


	public void setTableCd(String tableCd) {
		this.tableCd = tableCd;
	}


	public String getTableName() {
		return tableName;
	}


	public void setTableName(String tableName) {
		this.tableName = tableName;
	}


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


	public void setBasecodeOption(String basecodeOption) {
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

    @CsvBindByPosition(position = 0)
	@CsvBindByName(column = "C_TABLE_CD")
    public  String tableCd;
	@CsvBindByPosition(position = 1)
	@CsvBindByName(column = "C_TABLE_NAME")
    public  String tableName;
    @CsvBindByPosition(position = 2)
	@CsvBindByName(column = "C_HLEVEL")
    public  int hlevel;
    @CsvBindByPosition(position = 3)
	@CsvBindByName(column = "C_FULLNAME")
    public  String path;
    @CsvBindByPosition(position = 4)
	@CsvBindByName(column = "C_NAME")
    public  String name;
    @CsvBindByPosition(position = 5)
	@CsvBindByName(column = "C_SYNONYM_CD")
    public  String synonymCd;
    @CsvBindByPosition(position = 6)
	@CsvBindByName(column = "C_VISUALATTRIBUTES")
    public  String visualAttributes;
    @CsvBindByPosition(position = 7)
	@CsvBindByName(column = "C_BASECODE")
    public  String basecodeOption;
    @CsvBindByPosition(position = 8)
	@CsvBindByName(column = "C_METADATAXML")
    public  String metadataXmlOption;
    @CsvBindByPosition(position = 9)
	@CsvBindByName(column = "C_TOOLTIP")
    public  String tooltipOption;

    @Override public boolean isHidden() { return visualAttributes.length() > 1 && visualAttributes.charAt(1) == 'H'; }
    @Override public boolean isSynonym() { return "Y".equals(synonymCd); }
    @Override public boolean isRoot() { return true; }

    
	public RootInfo()
	{
	
	}
	/*
    public RootInfo(String tableCd, String tableName, int hlevel, String path, String name, String synonymCd, String visualAttributes, String basecodeOption, String metadataXmlOption, String tooltipOption) {
        this.tableCd = tableCd;
        this.tableName = tableName;
        this.hlevel = hlevel;
        this.path = path;
        this.name = name;
        this.synonymCd = synonymCd;
        this.visualAttributes = visualAttributes;
        this.basecodeOption = basecodeOption;
        this.metadataXmlOption = metadataXmlOption;
        this.tooltipOption = tooltipOption;
    }
    

    @Override public int getHlevel() { return hlevel; }
    @Override public String getPath() { return path; }
    @Override public String getName() { return name; }
    @Override public String getSynonymCd() { return synonymCd; }
    @Override public String getVisualAttributes() { return visualAttributes; }
    @Override public String getBasecodeOption() { return basecodeOption; }
    @Override public String getMetadataXmlOption() { return metadataXmlOption; }
    @Override public String getTooltipOption() { return tooltipOption; }
    @Override public boolean isHidden() { return visualAttributes.length() > 1 && visualAttributes.charAt(1) == 'H'; }
    @Override public boolean isSynonym() { return "Y".equals(synonymCd); }
    @Override public boolean isRoot() { return true; }
    */
}

