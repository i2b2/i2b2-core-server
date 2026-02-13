package edu.harvard.i2b2.ontology.dao.lucene.parser;
import com.opencsv.bean.BeanVerifier;
import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.HeaderColumnNameMappingStrategy;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
//import com.opencsv.bean.mappingstrategy.HeaderColumnNameTranslateMappingStrategy;
import com.opencsv.exceptions.CsvException;

import edu.harvard.i2b2.ontology.dao.TermInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
//import org.apache.commons.text.StringEscapeUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.*;

/**
 * Java translation of OntologyFileParser.scala
 */
public class OntologyFileParser {


	protected final static Log logesapi = LogFactory.getLog("OntologyFileParser");


	public static List<CloseableCsvToBean> ontIterator(String directory) throws IOException {
		return ontIterator(directory, '|');
	}

	public static List<CloseableCsvToBean> ontIterator(String directory, char delimiter) throws IOException {
		File file = new File(directory);
		File[] files = file.listFiles((f) -> f.isFile());
		if (files == null) return Collections.emptyList();
		Arrays.sort(files);

		List<CloseableCsvToBean> result = new ArrayList<>();
		for (File f : files) {
			CloseableCsvToBean cb = withBufferedFileReader(f, r -> {
				try {
					CsvToBeanBuilder<OntologyRow> builder = new CsvToBeanBuilder<OntologyRow>(r)
							.withSeparator(delimiter)
							.withIgnoreQuotations(false)
							.withType(OntologyRow.class)
							.withThrowExceptions(false)
							.withSkipLines(0);

					// We will use a custom mapping strategy implemented below
					/*
                    OntologyMappingStrategy ms = new OntologyMappingStrategy();
                    CsvToBean<OntologyRow> csvToBean = builder.withMappingStrategy(ms)
                            .withEscapeChar('\u0000')
                            .withVerifier(new OntologyVerifier())
                            .build();
					 */
					CsvToBean<OntologyRow> csvToBean = null;
					if (f.getName().equalsIgnoreCase("TABLE_ACCESS"))
					{
						 csvToBean = new CsvToBeanBuilder<OntologyRow>(r)
								.withSeparator(delimiter)
								.withIgnoreQuotations(false)
								.withType(RootInfo.class)
								.withEscapeChar('\u0000')
								.withIgnoreLeadingWhiteSpace(true)
								.withSkipLines(1)
								.build();
					} else {
						 csvToBean = new CsvToBeanBuilder<OntologyRow>(r)
									.withSeparator(delimiter)
									.withIgnoreQuotations(false)
									.withType(TermInfo.class)
									.withEscapeChar('\u0000')
									.withIgnoreLeadingWhiteSpace(true)
									.withSkipLines(1)
									.build();
					}

					return new CloseableCsvToBean(csvToBean, r, f.getName());
				} catch (Exception e) {
					logesapi.debug("Error parsing file: " + f, e);
					throw e;
				}
			});

			result.add(cb);
		}

		return result;
	}

	private static CloseableCsvToBean withBufferedFileReader(File file, java.util.function.Function<BufferedReader, CloseableCsvToBean> op) throws IOException {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			return op.apply(reader);
		} catch (IOException e) {
			logesapi.error("An error occurred trying to read file: " + file, e);
			throw e;
		}
	}

	public static void main(String[] args) throws Exception {
		String dir = args[0];
		List<CloseableCsvToBean> ontIt = ontIterator(dir);
		for (CloseableCsvToBean closeableCsvToBean : ontIt) {
			Iterator<OntologyRow> it = (Iterator<OntologyRow>) closeableCsvToBean.csvToBean.iterator();
			while (it.hasNext()) {
				//	TermInfo row = it.next();
				// noop in original
			}
			closeableCsvToBean.close();
		}
	}

	public static class OntologyHeader {
		public static final String FULL_NAME = "C_FULLNAME";
		public static final String HLEVEL = "C_HLEVEL";
		public static final String NAME = "C_NAME";
		public static final String SYNONYM_CD = "C_SYNONYM_CD";
		public static final String VISUALATTRIBUTES = "C_VISUALATTRIBUTES";
		public static final String BASECODE = "C_BASECODE";
		public static final String METADATAXML = "C_METADATAXML";
		public static final String TABLENAME = "C_TABLENAME";
		public static final String TOOLTIP = "C_TOOLTIP";
		public static final String APPLIED_PATH = "M_APPLIED_PATH";
		public static final String TABLE_CD = "C_TABLE_CD";
	}
	/*
    public interface OntologyRow {
        int getHlevel();
        String getPath();
        String getName();
        String getSynonymCd();
        String getVisualAttributes();
        Optional<String> getBasecodeOption();
        Optional<String> getMetadataXmlOption();
        Optional<String> getTooltipOption();
        boolean isHidden();
        boolean isSynonym();
        boolean isRoot();

    }
	 */
	/*

    public static class RootInfo implements OntologyRow {
    	@CsvBindByName(column = "C_TABLE_CD")
        public final String tableCd;
    	@CsvBindByName(column = "C_TABLENAME")
        public final String tableName;
    	@CsvBindByName(column = "C_HLEVEL")
        public final int hlevel;
    	@CsvBindByName(column = "C_PATH")
        public final String path;
    	@CsvBindByName(column = "C_NAME")
        public final String name;
    	@CsvBindByName(column = "C_SYNONYM_CD")
        public final String synonymCd;
    	@CsvBindByName(column = "C_VISUALATTRIBUTES")
        public final String visualAttributes;
    	@CsvBindByName(column = "C_BASECODE")
        public final Optional<String> basecodeOption;
    	@CsvBindByName(column = "C_METADATAXML")
        public final Optional<String> metadataXmlOption;
    	@CsvBindByName(column = "C_TOOLTIP")
        public final Optional<String> tooltipOption;

        public RootInfo(String tableCd, String tableName, int hlevel, String path, String name, String synonymCd, String visualAttributes, Optional<String> basecodeOption, Optional<String> metadataXmlOption, Optional<String> tooltipOption) {
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
        @Override public Optional<String> getBasecodeOption() { return basecodeOption; }
        @Override public Optional<String> getMetadataXmlOption() { return metadataXmlOption; }
        @Override public Optional<String> getTooltipOption() { return tooltipOption; }
        @Override public boolean isHidden() { return visualAttributes.length() > 1 && visualAttributes.charAt(1) == 'H'; }
        @Override public boolean isSynonym() { return "Y".equals(synonymCd); }
        @Override public boolean isRoot() { return true; }
    }
	 */

	// Simplified mapping strategy—original used HeaderNameBaseMappingStrategy
	public static class OntologyMappingStrategy extends HeaderColumnNameMappingStrategy<OntologyRow> {

		@Override
		public OntologyRow populateNewBean(String[] line) {
			// This is a simplified implementation; real implementation should map header indices to fields
			// For now, we won't attempt to dynamically map; instead callers using CsvToBean with this strategy
			// must provide a way to populate OntologyRow. Implementing full header-based mapping in Java
			// requires more scaffolding which depends on how CsvToBeanBuilder is used in your project.
			return null;
		}
	}

	public static class OntologyVerifier implements BeanVerifier<OntologyRow> {
		@Override
		public boolean verifyBean(OntologyRow ontologyRow) {
			if (ontologyRow instanceof TermInfo) {
				TermInfo termInfo = (TermInfo) ontologyRow;
				if (termInfo.isModifier()) return false;
			}

			return ontologyRow.getHlevel() >= 0 && !ontologyRow.isHidden() && !ontologyRow.isSynonym();
		}
	}

	public static class CloseableCsvToBean {
		public final CsvToBean<OntologyRow> csvToBean;
		public final Reader reader;
		public final String fileName;

		public CloseableCsvToBean(CsvToBean<OntologyRow> csvToBean, Reader reader, String fileName) {
			this.csvToBean = csvToBean;
			this.reader = reader;
			this.fileName = fileName;
		}

		public void close() throws IOException {
			reader.close();
		}
	}

	public static class ParserUtil {
		public static String cleanupForIndexing(String inStr) {
			if (inStr == null) return "";
			// Add two leading 0's to unicodes with only 2 hexadecimal characters, such as &#xab; --> &#x00ab;
			// return StringEscapeUtils.unescapeXml(inStr.replaceAll("&#x([0-9a-fA-F][0-9a-fA-F]);*", "&#x00$1;"));
			return inStr.replaceAll("&#x([0-9a-fA-F][0-9a-fA-F]);*", "&#x00$1;");
		}
	}
}
