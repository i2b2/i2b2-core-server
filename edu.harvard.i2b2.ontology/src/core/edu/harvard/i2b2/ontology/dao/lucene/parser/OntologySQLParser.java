package edu.harvard.i2b2.ontology.dao.lucene.parser;
import com.opencsv.bean.BeanVerifier;
import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.HeaderColumnNameMappingStrategy;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
//import com.opencsv.bean.mappingstrategy.HeaderColumnNameTranslateMappingStrategy;
import com.opencsv.exceptions.CsvException;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.ontology.dao.TermInfo;
import edu.harvard.i2b2.ontology.dao.lucene.parser.OntologyFileParser.CloseableCsvToBean;
import edu.harvard.i2b2.ontology.datavo.vdo.ConceptType;
import edu.harvard.i2b2.ontology.util.OntologyUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessException;
//import org.apache.commons.text.StringEscapeUtils;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Java translation of OntologyFileParser.scala
 */
public class OntologySQLParser {


	protected final static Log logesapi = LogFactory.getLog("OntologyFileParser");
	private JdbcTemplate jt;

	public  List<CloseableSQLToBean> ontIterator(String directory) throws IOException, I2B2Exception {
		return ontIterator(directory, '|');
	}

	
	private String getMetadataSchema() throws I2B2Exception{

		return OntologyUtil.getInstance().getMetaDataSchemaName();
	}

	
	public  List<CloseableSQLToBean> ontIterator(String directory, char delimiter) throws IOException, I2B2Exception {

		
		String categoriesSql = "select C_TABLE_CD, C_TABLE_NAME, C_HLEVEL, C_FULLNAME, C_NAME, C_SYNONYM_CD, C_VISUALATTRIBUTES, C_BASECODE, C_METADATAXML, C_TOOLTIP\n"
				 + " from " +  getMetadataSchema() +  "table_access where c_visualattributes not like '_H%'";

		List queryResult = null;
		
		try {
			Object returnType;
			Object projectInfo;
			Object obfuscatedUserFlag;
			//queryResult = jt.query(categoriesSql, getConceptFullNameMapper(returnType, projectInfo, obfuscatedUserFlag));
		} catch (DataAccessException e) {
			logesapi.error("Get Categories " +e.getMessage());
			//throw new Exception("Database Error");
		}

		List<CloseableSQLToBean> result = new ArrayList<>();
		File[] files = null;
		for (File f : files) {
			CloseableSQLToBean cb = withBufferedFileReader(f, r -> {
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

					return new CloseableSQLToBean();
				} catch (Exception e) {
					logesapi.debug("Error parsing file: " + f, e);
					throw e;
				}
			});

			result.add(cb);
		}

		return result;
	}

	private static CloseableSQLToBean withBufferedFileReader(File file, java.util.function.Function<BufferedReader, CloseableSQLToBean> op) throws IOException {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			return op.apply(reader);
		} catch (IOException e) {
			logesapi.error("An error occurred trying to read file: " + file, e);
			throw e;
		}
	}


	public ConceptType mapRow(ResultSet rs, int rowNum) throws SQLException {
		ConceptType child = new ConceptType();
		//TODO fix this for all
		child.setKey("\\\\" + rs.getString("c_table_cd")+ rs.getString("c_fullname")); 
		child.setName(rs.getString("c_name"));

			child.setBasecode(rs.getString("c_basecode"));
			child.setLevel(rs.getInt("c_hlevel"));
			child.setSynonymCd(rs.getString("c_synonym_cd"));
			child.setVisualattributes(rs.getString("c_visualattributes"));

			child.setTooltip(rs.getString("c_tooltip"));
			child.setValuetypeCd(rs.getString("valuetype_cd"));
			child.setProtectedAccess(rs.getString("c_protected_access"));
			child.setOntologyProtection(rs.getString("c_ontology_protection"));


			child.setFacttablecolumn(rs.getString("c_facttablecolumn" ));
			child.setTablename(rs.getString("c_dimtablename")); 
			child.setColumnname(rs.getString("c_columnname")); 
			child.setColumndatatype(rs.getString("c_columndatatype")); 
			child.setOperator(rs.getString("c_operator")); 
			child.setDimcode(rs.getString("c_dimcode")); 
			child.setTooltip(rs.getString("c_tooltip"));
			child.setValuetypeCd(rs.getString("valuetype_cd"));
		
	
		return child;
	}

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

	public static class CloseableSQLToBean {
		public  OntologyRow ontologyRow;
		public  ResultSet resultSet;
		public  String table;

		public void ontologyRow(OntologyRow ontologyRow, ResultSet resultSet, String table) {
			this.ontologyRow = ontologyRow;
			this.resultSet = resultSet;
			this.table = table;
		}

		public void close() throws  SQLException {
			resultSet.close();
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
