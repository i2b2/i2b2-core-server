package edu.harvard.i2b2.ontology.dao.lucene.parser;


import java.io.*;
import java.nio.file.attribute.FileTime;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.suggest.InputIterator;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

import edu.harvard.i2b2.ontology.dao.SuggestionIndexer;
import edu.harvard.i2b2.ontology.dao.TermInfo;
import edu.harvard.i2b2.ontology.dao.lucene.parser.OntologyFileParser.CloseableCsvToBean;
import edu.harvard.i2b2.ontology.ejb.DBInfoType;
import edu.harvard.i2b2.ontology.ejb.TableAccessType;

/**
 * Java translation of LuceneIndexer.scala. This keeps the same high-level
 * behavior: createIndexes, indexFromFile, zip utilities, and suggestion
 * iterator implementation.
 *
 * Note: Several project-specific classes (SuggestionIndexer, SearchIndexer,
 * OntologyRow, etc.) are assumed to exist elsewhere in the codebase. This
 * translation focuses on structure and logic, not on reimplementing those
 * domain classes.
 */
public final class LuceneIndexer {

	public static Map<String, Long> parentChildMap = new HashMap<>(); //Collections.emptyMap();
	public static Map<String, List<String>> pathToTermMap = new HashMap<>(); //Collections.emptyMap();
	public static Map<String, List<String>> pathToOntPathMap = new HashMap<>(); //Collections.emptyMap();
	public static Map<String, String> pathPrefixMap = new HashMap<>(); //Collections.emptyMap();

	protected final static Log logesapi = LogFactory.getLog("LuceneIndexer");

	public LuceneIndexer() {}

	private static IndexWriter createSearchIndexWriter(FSDirectory searchIndexDirectory) throws IOException {
		IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());
		config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
		config.setRAMBufferSizeMB(1024.0);
		return new IndexWriter(searchIndexDirectory, config);
	}

	public static void createIndexes(
			String filename,
			char fileDelimiter,
			String codeCategoryFilename,
			char categoryFileDelimiter,
			String searchIndexDirName,
			String searchIndexZipFileName,
			String suggestIndexDirNameOption,
			String suggestIndexZipFileNameOption,
			int maxWordsInSuggestion,
			Optional<List<String>> suggestIndexDumpPrefixesOption,
			boolean createAutoSuggest,
			boolean includeTooltips,
			boolean verbose
			) throws Exception {

		if (createAutoSuggest) {
			String suggestIndexDirName = suggestIndexDirNameOption; //orElseThrow(() -> new IllegalArgumentException("suggest index dir required"));
			String suggestIndexZipFileName = suggestIndexZipFileNameOption; //orElse("suggest_index.zip");
			List<String> suggestIndexDumpPrefixes = suggestIndexDumpPrefixesOption.orElse(Collections.emptyList());

			Map<String, String> suggestIndexDumpNames = new LinkedHashMap<>();
			for (String p : suggestIndexDumpPrefixes) {
				if (p != null && !p.trim().isEmpty()) {
					suggestIndexDumpNames.put(p, String.format("%s.%s.csv", suggestIndexDirName, p.trim()));
				}
			}

			File suggestIndexDir = new File(suggestIndexDirName);
			logesapi.info("Creating auto-suggest index in directory " + suggestIndexDir);

			Map<String, File> suggestIndexDumpFilesMap = new LinkedHashMap<>();
			for (Map.Entry<String, String> n : suggestIndexDumpNames.entrySet()) {
				logesapi.info(String.format("Creating CSV dump file for prefix '%s': %s", n.getKey(), n.getValue()));
				suggestIndexDumpFilesMap.put(n.getKey(), new File(n.getValue()));
			}

			suggestIndexDir.mkdir();
			FSDirectory suggestIndexDirectory = FSDirectory.open(suggestIndexDir.toPath());

			SuggestionIndexer suggestionIndexer = new SuggestionIndexer(
					maxWordsInSuggestion,
					suggestIndexDirectory,
					suggestIndexDirName,
					suggestIndexZipFileName,
					verbose,
					suggestIndexDumpFilesMap
					);


			SuggestionIndexInfo suggestIndexInfo = new SuggestionIndexInfo(suggestionIndexer, suggestIndexDirName, suggestIndexZipFileName);

			indexFromFile(Optional.empty(), Optional.of(suggestIndexInfo), filename, fileDelimiter);

			// Close the directory
			suggestIndexDirectory.close();
			logesapi.debug("Finished creating the ontology auto-suggest indices");
		} else {
			File searchIndexDir = new File(searchIndexDirName);
			logesapi.info("Creating search index in directory " + searchIndexDir);
			searchIndexDir.mkdir();
			FSDirectory searchIndexDirectory = FSDirectory.open(searchIndexDir.toPath());
			IndexWriter searchIndexWriter = createSearchIndexWriter(searchIndexDirectory);

			SearchIndexer searchIndexer = new SearchIndexer(
					searchIndexWriter,
					codeCategoryFilename,
					filename,
					fileDelimiter,
					categoryFileDelimiter,
					includeTooltips
					);

			SearchIndexInfo searchIndexInfo = new SearchIndexInfo(searchIndexer, searchIndexDirName, searchIndexZipFileName);

			indexFromFile(Optional.of(searchIndexInfo), Optional.empty(), filename, fileDelimiter);

			searchIndexDirectory.close();
			logesapi.debug("Finished creating the ontology search indices");
		}
	}

	public static final class SuggestionIndexInfo {
		public final SuggestionIndexer suggestionIndexer;
		public final String suggestOutputDirName;
		public final String suggestOutputZipFileName;

		public SuggestionIndexInfo(SuggestionIndexer suggestionIndexer, String suggestOutputDirName, String suggestOutputZipFileName) {
			this.suggestionIndexer = suggestionIndexer;
			this.suggestOutputDirName = suggestOutputDirName;
			this.suggestOutputZipFileName = suggestOutputZipFileName;
		}
	}

	public static final class SearchIndexInfo {
		public final SearchIndexer searchIndexer;
		public final String searchOutputDirName;
		public final String searchOutputZipFileName;

		public SearchIndexInfo(SearchIndexer searchIndexer, String searchOutputDirName, String searchOutputZipFileName) {
			this.searchIndexer = searchIndexer;
			this.searchOutputDirName = searchOutputDirName;
			this.searchOutputZipFileName = searchOutputZipFileName;
		}
	}

	public static void indexFromFile(Optional<SearchIndexInfo> searchIndexInfoOption,
			Optional<SuggestionIndexInfo> suggestionIndexInfoOption,
			String directory,
			char fileDelimiter) throws Exception {

		long startTime = System.currentTimeMillis();
		int numDataRead = 0;

		logesapi.debug("starting index of ontology");

		List<CloseableCsvToBean> ontIt = OntologyFileParser.ontIterator(directory, fileDelimiter);

		for (CloseableCsvToBean closeableCsvToBean : ontIt) {
			logesapi.debug("Parsing ontology file: " + closeableCsvToBean.fileName);
			try {
				Iterator<OntologyRow> it = closeableCsvToBean.csvToBean.iterator();
				while (it.hasNext()) {

					OntologyRow ontologyRow = it.next();
					numDataRead++;
					if (suggestionIndexInfoOption.isPresent()) {

						if (ontologyRow instanceof TermInfo)
							suggestionIndexInfoOption.get().suggestionIndexer.generateSuggestionsFromConceptName(ontologyRow.getName()); //.toString());
					} else {
						searchIndexInfoOption.get().searchIndexer.createAndAddDocument(ontologyRow);
						if (numDataRead % 200000 == 0) {
							searchIndexInfoOption.get().searchIndexer.commit();
						}
					}

					if (numDataRead % 200000 == 0) {
						logesapi.debug("Read " + numDataRead + " docs");
						long endTime = System.currentTimeMillis();
						long elapsedSeconds = (endTime - startTime) / 1000;
						logesapi.debug("Total elapsed time so far: " + elapsedSeconds + " seconds");
					}
				}
			} finally {
				closeableCsvToBean.close();
			}
		}

		ontIt = Collections.emptyList();

		if (searchIndexInfoOption.isPresent()) {
			searchIndexInfoOption.get().searchIndexer.commit();
			compressIndex(searchIndexInfoOption.get().searchOutputDirName, searchIndexInfoOption.get().searchOutputZipFileName);

			logesapi.info(String.format("done indexing ontology, read: %d, assigned concept categories: %d times, assigned code categories: %d times, and assigned code sets: %d times",
					numDataRead,
					searchIndexInfoOption.get().searchIndexer.mappedConceptCategoriesCount,
					searchIndexInfoOption.get().searchIndexer.mappedCodeCategoriesCount,
					searchIndexInfoOption.get().searchIndexer.mappedCodeSetCount));
		} else {
			logesapi.info("Building the suggestion index");
			suggestionIndexInfoOption.get().suggestionIndexer.buildSuggestionIndex();
			compressIndex(suggestionIndexInfoOption.get().suggestOutputDirName, suggestionIndexInfoOption.get().suggestOutputZipFileName);
		}

		long endTime = System.currentTimeMillis();
		long elapsedSeconds = (endTime - startTime) / 1000;
		logesapi.debug("Total elapsed time: " + elapsedSeconds + " seconds");
	}


	public static void indexFromDB(//Optional<SearchIndexInfo> searchIndexInfoOption,
			SuggestionIndexInfo suggestionIndexInfoOption,
			List<TableAccessType> tableAccessType, DataSource dataSource, DBInfoType dbInfo) throws Exception {

		long startTime = System.currentTimeMillis();
		int numDataRead = 0;

		logesapi.debug("starting index of ontology");

		Connection conn = dataSource.getConnection();
		ResultSet resultSet = null;
		PreparedStatement query = null;


		try {
			for (TableAccessType tableName : tableAccessType) {
				logesapi.info("Parsing ontology file: " + tableName.getFullName());

				// Iterator<OntologyRow> it = closeableCsvToBean.csvToBean.iterator();
				String stageSql = " select distinct C_NAME, C_TOTALNUM from "  
						+ dbInfo.getDb_fullSchema() + tableName.getTableName() + " where c_visualattributes not like '_H%'";


				query = conn.prepareStatement(stageSql);
				resultSet = query.executeQuery();

				while (resultSet.next()) {


					suggestionIndexInfoOption.suggestionIndexer.generateSuggestionsFromConceptName(resultSet.getString("C_NAME") + " (" + resultSet.getString("C_TOTALNUM") + ") - " + tableName.getName()) ; //.toString());


					/*
                	OntologyRow ontologyRow = new ontologyRow();
                	numDataRead++;
                    if (suggestionIndexInfoOption.isPresent()) {

                    	if (ontologyRow instanceof TermInfo)
                    		suggestionIndexInfoOption.get().suggestionIndexer.generateSuggestionsFromConceptName(ontologyRow.getName()); //.toString());
                    } else {
                        searchIndexInfoOption.get().searchIndexer.createAndAddDocument(ontologyRow);
                        if (numDataRead % 200000 == 0) {
                            searchIndexInfoOption.get().searchIndexer.commit();
                        }
                    }
					 */
					numDataRead++;
					if (numDataRead % 200000 == 0) {
						logesapi.debug("Read " + numDataRead + " docs");
						long endTime = System.currentTimeMillis();
						long elapsedSeconds = (endTime - startTime) / 1000;
						logesapi.debug("Total elapsed time so far: " + elapsedSeconds + " seconds");
						break;
					}
				}
				resultSet.close();

				// ontIt = Collections.emptyList();

				//suggestionIndexInfoOption.suggestionIndexer.buildSuggestionIndex();
				//compressIndex(suggestionIndexInfoOption.suggestOutputDirName, suggestionIndexInfoOption.suggestOutputZipFileName);
			}
			suggestionIndexInfoOption.suggestionIndexer.buildSuggestionIndex();
		} catch (Exception e)
		{
			e.printStackTrace();

		} finally {
			resultSet.close();


			conn.close();
			
		}
		long endTime = System.currentTimeMillis();
		long elapsedSeconds = (endTime - startTime) / 1000;
		logesapi.debug("Total elapsed time: " + elapsedSeconds + " seconds");
		logesapi.info("Parsing ontology Done:"); 
	}




	public static String cleanupForIndexing(String inStr) {
		// pad short hex unicode escapes then unescape XML
		String padded = inStr.replaceAll("&#x([0-9a-fA-F][0-9a-fA-F]);*", "&#x00$1;");
		//        return StringEscapeUtils.unescapeXml(padded);
		return padded;
	}

	public static void compressIndex(String dirName, String zipFileName) {
		ZipUtil.createZip(zipFileName, dirName);
		logesapi.debug("Created zip file " + zipFileName + " from directory " + dirName);
	}

	public static void main(String[] args) throws Exception {
		long startTime = System.currentTimeMillis();

		// Minimal argument handling: require ontology dir and categories file
		if (args.length < 2) {
			System.err.println("Usage: LuceneIndexer <ontologyDirectory> <codeCategoryFilename> [options]");
			throw new WrongNumberOfArguments("Not enough arguments");
		}

		// For simplicity assume positional args used in expected order (mirrors Scala defaults)
		String ontologyDirectory = args[0];
		String codeCategoryFilename = args[1];
		String searchIndexDir = args.length > 2 ? args[2] : "lucene_index";
		String searchIndexZipFile = args.length > 3 ? args[3] : "lucene_index.zip";

		startAppPulse();

		// Call createIndexes with defaults for omitted parameters
		createIndexes(
				ontologyDirectory,
				'|',
				codeCategoryFilename,
				',',
				searchIndexDir,
				searchIndexZipFile,
				searchIndexDir,
				searchIndexZipFile,
				3,
				Optional.of(Collections.emptyList()),
				true, //false,
				false,
				false
				);

		long endTime = System.currentTimeMillis();
		long elapsedSeconds = (endTime - startTime) / 1000;
		logesapi.debug("TOTAL elapsed time: " + elapsedSeconds + " seconds");
		System.exit(0);
	}

	private static void startAppPulse() {
		final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
		Runnable pulse = () -> logesapi.info("........Still Running........");
		// schedule every 120 seconds
		scheduler.scheduleAtFixedRate(pulse, 120, 120, TimeUnit.SECONDS);
		// Note: we don't call shutdown here; the program will exit when work completes
	}

}

class WrongNumberOfArguments extends Exception {
	public WrongNumberOfArguments(String message) { super(message); }
}

class CodeAndConceptCategory {
	public final String codeCategory;
	public final String conceptCategory;
	public final Optional<String> codeSet;

	public CodeAndConceptCategory(String codeCategory, String conceptCategory, Optional<String> codeSet) {
		this.codeCategory = codeCategory;
		this.conceptCategory = conceptCategory;
		this.codeSet = codeSet;
	}
}

class ZipUtil {
	protected final static Log logesapi = LogFactory.getLog("ZipUtil.class");

	public static void createZip(String zipFileName, String dirName) {
		try (FileOutputStream fos = new FileOutputStream(zipFileName);
				ZipOutputStream zipStream = new ZipOutputStream(fos)) {

			File zf = new File(dirName);
			File[] files = zf.listFiles();
			if (files != null) {
				for (File file : files) {
					try (FileInputStream fis = new FileInputStream(file)) {
						addToZipFile(zf.getName() + "/" + file.getName(), file.lastModified(), fis, zipStream);
					}
				}
			}
		} catch (Exception e) {
			logesapi.error("Error while zipping " + zipFileName, e);
		}
	}

	private static void addToZipFile(String inputFileName, long lastModified, InputStream inputStream, ZipOutputStream zipStream) throws IOException {
		try {
			ZipEntry entry = new ZipEntry(inputFileName);
			entry.setCreationTime(FileTime.fromMillis(lastModified));
			entry.setComment("Created by SHRINE");
			zipStream.putNextEntry(entry);
			logesapi.info("Generated new entry for: " + inputFileName);

			byte[] readBuffer = new byte[2048];
			int amountRead;
			int written = 0;
			amountRead = inputStream.read(readBuffer);
			while (amountRead > 0) {
				zipStream.write(readBuffer, 0, amountRead);
				written += amountRead;
				amountRead = inputStream.read(readBuffer);
			}
			logesapi.info("Stored " + written + " bytes to " + inputFileName);
		} catch (IOException e) {
			throw new IOException("Unable to process " + inputFileName, e);
		} finally {
			if (inputStream != null) inputStream.close();
		}
	}
}

class ErrorParsingLabXml extends Exception {
	public ErrorParsingLabXml(String xml, String path) {
		super("Unable to parse lab xml with path " + path + ":  " + xml);
	}
}

class SuggestionItem {
	public final String suggestionText;
	public final long occurrences;
	public final long weight;
	public final List<String> contexts;

	public SuggestionItem(String suggestionText, long occurrences, long weight, List<String> contexts) {
		this.suggestionText = suggestionText;
		this.occurrences = occurrences;
		this.weight = weight;
		this.contexts = contexts;
	}
}

class SuggestionItemIterator implements InputIterator {

	private final Iterator<SuggestionItem> entityIterator;
	private SuggestionItem currentItem;

	public SuggestionItemIterator(Iterator<SuggestionItem> entityIterator) { this.entityIterator = entityIterator; }

	@Override
	public boolean hasContexts() { return true; }

	@Override
	public boolean hasPayloads() { return true; }

	@Override
	public BytesRef next() {
		if (entityIterator.hasNext()) {
			currentItem = entityIterator.next();
			try {
				return new BytesRef(currentItem.suggestionText.getBytes("UTF8"));
			} catch (UnsupportedEncodingException e) {
				throw new Error("Couldn't convert to UTF-8", e);
			}
		} else {
			return null;
		}
	}

	@Override
	public BytesRef payload() {
		try {
			return new BytesRef(String.valueOf(currentItem.occurrences).getBytes("UTF8"));
		} catch (UnsupportedEncodingException e) {
			throw new Error("Could not convert to UTF-8", e);
		}
	}

	@Override
	public Set<BytesRef> contexts() {
		try {
			Set<BytesRef> contexts = new HashSet<>();
			for (String c : currentItem.contexts) {
				contexts.add(new BytesRef(c.getBytes("UTF8")));
			}
			return contexts;
		} catch (UnsupportedEncodingException e) {
			throw new Error("Couldn't convert to UTF-8", e);
		}
	}

	@Override
	public long weight() { return currentItem.weight; }
}
