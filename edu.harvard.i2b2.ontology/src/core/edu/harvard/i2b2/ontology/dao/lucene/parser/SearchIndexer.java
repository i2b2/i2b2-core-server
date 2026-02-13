package edu.harvard.i2b2.ontology.dao.lucene.parser;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReaderBuilder;
/*
import net.shrine.logesapi.Log;
import net.shrine.ontology.LabDetail;
import net.shrine.ontology.indexer.LuceneIndexer;
import net.shrine.ontology.indexer.parser.CloseableCsvToBean;
import net.shrine.ontology.indexer.parser.OntologyFileParser;
import net.shrine.ontology.indexer.parser.OntologyRow;
import net.shrine.ontology.indexer.parser.RootInfo;
import net.shrine.ontology.indexer.parser.TermInfo;
 */
import com.opencsv.bean.CsvToBean;

import edu.harvard.i2b2.ontology.dao.TermInfo;
import edu.harvard.i2b2.ontology.dao.lucene.parser.OntologyFileParser.CloseableCsvToBean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.SortedDocValuesField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.util.BytesRef;

/**
 * Java translation of SearchIndexer.scala
 */
public class SearchIndexer {


	protected final static Log logesapi = LogFactory.getLog("SuggestionIndexer");

	private final IndexWriter searchIndexWriter;
	private final String codeCategoryFilename;
	private final String ontDirectory;
	private final char fileDelimiter;
	private final char categoryFileDelimiter;
	private final boolean includeTooltips;

	public final Map<String, CodeAndConceptCategory> codeCategories;

	public int mappedCodeCategoriesCount = 0;
	public int mappedConceptCategoriesCount = 0;
	public int mappedCodeSetCount = 0;

	public SearchIndexer(IndexWriter searchIndexWriter,
			String codeCategoryFilename,
			String ontDirectory,
			char fileDelimiter,
			char categoryFileDelimiter,
			boolean includeTooltips) throws Exception {
		this.searchIndexWriter = searchIndexWriter;
		this.codeCategoryFilename = codeCategoryFilename;
		this.ontDirectory = ontDirectory;
		this.fileDelimiter = fileDelimiter;
		this.categoryFileDelimiter = categoryFileDelimiter;
		this.includeTooltips = includeTooltips;

		this.codeCategories = CodeCategories.extractCodeCategoriesMappings(codeCategoryFilename, categoryFileDelimiter);

		createPathMap();
	}

	private void createPathMap() throws Exception {
		logesapi.debug("Creating maps");

		List<CloseableCsvToBean> ontIt = OntologyFileParser.ontIterator(ontDirectory, fileDelimiter);
		for (CloseableCsvToBean closeableCsvToBean : ontIt) {
			try {
				//Iterator<OntologyRow> it = closeableCsvToBean.csvToBean.iterator(); //().iterator();
				/*
            	 // CsvToBean<OntologyRow> cvsbean = closeableCsvToBean.reader
            	// cvsbean.
            	  CSVReader reader = closeableCsvToBean.reader;
            	  List<OntologyRow> emps = new ArrayList<OntologyRow>();

          		// read line by line
          		String[] record = null;

          		while ((record = reader.readNext()) != null) {
          			OntologyRow emp = new OntologyRow();
          			emp.setId(record[0]);
          			emp.setName(record[1]);
          			emp.setAge(record[2]);
          			emp.setCountry(record[3]);
          			emps.add(emp);
          		}

				 */
				CsvToBean<OntologyRow> cvsbean = closeableCsvToBean.csvToBean;
				Iterator<OntologyRow> it = cvsbean.iterator();

				while (it.hasNext()) {
					Object obj = it.next();
					OntologyRow ontologyRow = (OntologyRow) obj;

					boolean isRoot = false;
					if (ontologyRow instanceof RootInfo) {
						RootInfo rootInfo = (RootInfo) ontologyRow;
						String tableCd = rootInfo.tableCd;
						// prefix key is rootInfo.path
						LuceneIndexer.pathPrefixMap.put(rootInfo.path, tableCd);
						isRoot = true;
					}

					String metadataXmlOption = ontologyRow.getMetadataXmlOption();
					String hasLabDataString = "FALSE";
					if (metadataXmlOption != null && metadataXmlOption != "") {
						String metadataXml = metadataXmlOption;
						if (metadataXml == null || metadataXml.equals("") || metadataXml.equals("\\N")) hasLabDataString = "FALSE";
						else hasLabDataString = "TRUE";
					}

					List<String> result = LuceneIndexer.pathToTermMap.get(ontologyRow.getPath());
					if (result == null) {
						LuceneIndexer.pathToTermMap = new LinkedHashMap<>(LuceneIndexer.pathToTermMap);
						LuceneIndexer.pathToTermMap.put(ontologyRow.getPath(), Arrays.asList(ontologyRow.getName(), ontologyRow.getVisualAttributes(), hasLabDataString));
					} else {
						if (isRoot) {
							LuceneIndexer.pathToTermMap = new LinkedHashMap<>(LuceneIndexer.pathToTermMap);
							LuceneIndexer.pathToTermMap.put(ontologyRow.getPath(), Arrays.asList(ontologyRow.getName(), ontologyRow.getVisualAttributes(), hasLabDataString));
						}
					}
				}
			} catch (Exception e)
			{
				e.printStackTrace();
			} finally {
				closeableCsvToBean.close();
			}
		}

		logesapi.debug("Creating path to natural ordering");
		for (String fullPath : new ArrayList<>(LuceneIndexer.pathToTermMap.keySet())) {
			String[] pathItems = fullPath.split("\\\\");
			String newpath = "";
			String visAttrPath = "";
			String isLabPath = "";
			boolean rootFound = false;

			String currentPath = "";
			for (String item : pathItems) {
				currentPath = currentPath + "\\" + item;

				if (LuceneIndexer.pathPrefixMap.containsKey(currentPath.substring(1) + "\\")) {
					rootFound = true;
				}

				List<String> m = LuceneIndexer.pathToTermMap.get(currentPath.substring(1) + "\\");
				if (m != null) {
					if (rootFound) {
						newpath = newpath + "\\" + m.get(0);
						visAttrPath = visAttrPath + "\\" + m.get(1);
						isLabPath = isLabPath + "\\" + m.get(2);
					}
				}
			}

			if (rootFound) {
				newpath = newpath + "\\";
				visAttrPath = visAttrPath + "\\";
				isLabPath = isLabPath + "\\";
				LuceneIndexer.pathToOntPathMap = new LinkedHashMap<>(LuceneIndexer.pathToOntPathMap);
				LuceneIndexer.pathToOntPathMap.put(fullPath, Arrays.asList(newpath, visAttrPath, isLabPath));
			}
		}

		LuceneIndexer.pathToTermMap = Collections.emptyMap();
		logesapi.debug("Completed path to natural ordering creation");
	}

	public void createAndAddDocument(Object ontologyRowObj) throws IOException {
		OntologyRow ontologyRow = (OntologyRow) ontologyRowObj;

		if (LuceneIndexer.pathPrefixMap.containsKey(ontologyRow.getPath()) && !ontologyRow.isRoot()) {
			logesapi.info("Filtering out duplicate root path " + ontologyRow.getPath() + " with name " + ontologyRow.getName());
			return;
		}

		TextField displayField = new TextField("displayName", ontologyRow.getName(), Field.Store.YES);
		SortedDocValuesField displaySortField = new SortedDocValuesField("displayName", new BytesRef(ontologyRow.getName()));
		StringField isRootField = new StringField("isRoot", Boolean.toString(ontologyRow.isRoot()), Field.Store.YES);
		StoredField visualAttrField = new StoredField("visualAttributes", ontologyRow.getVisualAttributes());

		Document searchDoc = new Document();
		searchDoc.add(displayField);
		searchDoc.add(displaySortField);
		searchDoc.add(isRootField);
		searchDoc.add(visualAttrField);

		if (includeTooltips) {
			String tooltipOption = ontologyRow.getTooltipOption();
			if (tooltipOption != null && tooltipOption != "") {
				searchDoc.add(new StoredField("tooltip", tooltipOption));
			}
		}

		String basecodeOption = ontologyRow.getBasecodeOption();
		if (basecodeOption != null && basecodeOption != "") {
			searchDoc.add(new StringField("basecode", basecodeOption, Field.Store.YES));
		}

		String  metadataXmlOption = ontologyRow.getMetadataXmlOption();
		if (metadataXmlOption != null && metadataXmlOption != "") {
			String metadataXml = metadataXmlOption;
			// In Scala they attempted to parse LabDetail; here we store raw metadata if present
			// If a LabDetail parsing method is available, prefer using it.
			try {
				// Attempt to construct a LabDetail using a constructor if available
				// Fallback: store raw metadata
				searchDoc.add(new StoredField("labDetail", metadataXml));
			} catch (Throwable t) {
				searchDoc.add(new StoredField("labDetail", metadataXml));
			}
		}

		String fullNameWithTableNamePrefix = "";

		// add the table prefix to the path name
		for (Map.Entry<String, String> prefixEntry : LuceneIndexer.pathPrefixMap.entrySet()) {
			if (ontologyRow.getPath().startsWith(prefixEntry.getKey())) {
				String tablePrefix = prefixEntry.getValue();
				fullNameWithTableNamePrefix = "\\\\" + tablePrefix + ontologyRow.getPath();

				searchDoc.add(new StringField("path", fullNameWithTableNamePrefix, Field.Store.YES));

				if (!ontologyRow.isRoot()) {
					String[] parts = fullNameWithTableNamePrefix.split("\\\\");
					if (parts.length > 1) {
						String parentPath = Arrays.stream(parts).limit(parts.length - 1).collect(Collectors.joining("\\"));
						searchDoc.add(new StringField("parentPath", parentPath, Field.Store.NO));
					}
				}

				break;
			}
		}

		// Determine code category mapping
		Map.Entry<String, CodeAndConceptCategory> found = null;
		for (Map.Entry<String, CodeAndConceptCategory> c : codeCategories.entrySet()) {
			if (fullNameWithTableNamePrefix.contains(c.getKey())) {
				found = c;
				break;
			}
		}

		if (found == null) {
			logesapi.error("No category found for path=" + ontologyRow.getPath() + " and name=" + ontologyRow.getName());
			return;
		} else {
			CodeAndConceptCategory c = found.getValue();
			mappedConceptCategoriesCount++;
			searchDoc.add(new StoredField("conceptCategory", c.conceptCategory));
			mappedCodeCategoriesCount++;
			searchDoc.add(new StringField("codeCategory", c.codeCategory, Field.Store.YES));
			if (c.codeSet != null) {
				mappedCodeSetCount++;
				searchDoc.add(new StringField("codeSet", c.codeSet.get(), Field.Store.YES));
			}

			List<String> naturalPathAndVisAttrAndLabList = LuceneIndexer.pathToOntPathMap.getOrDefault(ontologyRow.getPath(), Collections.emptyList());
			String naturalPath = naturalPathAndVisAttrAndLabList.size() > 0 ? naturalPathAndVisAttrAndLabList.get(0) : "";
			String visPath = naturalPathAndVisAttrAndLabList.size() > 1 ? naturalPathAndVisAttrAndLabList.get(1) : "";
			String isLabPath = naturalPathAndVisAttrAndLabList.size() > 2 ? naturalPathAndVisAttrAndLabList.get(2) : "";
			String naturalPathWithCategory = "\\" + c.codeCategory + naturalPath;
			searchDoc.add(new StringField("nPath", naturalPathWithCategory, Field.Store.YES));
			searchDoc.add(new SortedDocValuesField("nPath", new BytesRef(naturalPathWithCategory)));
			searchDoc.add(new StoredField("visPath", visPath));
			searchDoc.add(new StoredField("isLabPath", isLabPath));

			addDocument(searchDoc);
		}
	}

	private void addDocument(Document searchDoc) throws IOException {
		searchIndexWriter.addDocument(searchDoc);
	}

	public void commit() throws IOException {
		searchIndexWriter.commit();
	}

	static class CodeCategories {
		public static Map<String, CodeAndConceptCategory> extractCodeCategoriesMappings(String filename, char categoryFileDelimiter) throws IOException {
			CSVParserBuilder parserBuilder = new CSVParserBuilder().withSeparator(categoryFileDelimiter).withEscapeChar('\u0000');
			List<Map.Entry<String, CodeAndConceptCategory>> entries = new ArrayList<>();
			Map<String, CodeAndConceptCategory> codeCategories = new LinkedHashMap<>();

			logesapi.info("Parsing category definition file " + filename);
			try (BufferedReader br = new BufferedReader(new FileReader(new File(filename)))) {
				com.opencsv.CSVReader csvReader = new CSVReaderBuilder(br).withCSVParser(parserBuilder.build()).build();
				try {
					Iterator<String[]> iter = csvReader.iterator();
					while (iter.hasNext()) {
						String[] line = iter.next();
						if (line.length == 3) {
							codeCategories.put(line[0].trim(), new CodeAndConceptCategory(line[1], line[2], null));
							//  } else if (line.length == 4) {
							//     codeCategories.put(line[0].trim(), new CodeAndConceptCategory(line[1], line[2], line[3]));
						} else {
							logesapi.error("Error parsing line: " + String.join(String.valueOf(categoryFileDelimiter), line));
						}
					}
					logesapi.info("Found " + codeCategories.size() + " code category listings");
				} finally {
					csvReader.close();
				}
			} catch (IOException e) {
				logesapi.error("An error occurred trying to read file: " + filename, e);
				throw e;
			}

			return codeCategories;
		}
	}
}
