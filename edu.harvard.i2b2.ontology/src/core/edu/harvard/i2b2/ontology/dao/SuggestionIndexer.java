package edu.harvard.i2b2.ontology.dao;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVWriter;

import edu.harvard.i2b2.ontology.dao.lucene.SuggestionPayload;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.search.suggest.analyzing.AnalyzingInfixSuggester;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.BytesRef;

/**
 * Java translation of SuggestionIndexer.scala
 */
public class SuggestionIndexer {

	// Recreate the stop words list used in the Scala version
	public static final CharArraySet STOP_WORDS;

	protected final static Log logesapi = LogFactory.getLog("SuggestionIndexer");

	static {
		Set<String> stop = new HashSet<>(Arrays.asList(
				"a", "an", "and", "are", "as", "at", "be", "but", "by",
				"for", "if", "in", "into", "is", "it",
				"no", "not", "of", "on", "or", "such",
				"that", "the", "their", "then", "there", "these",
				"they", "this", "to", "was", "will", "with"
				));
		STOP_WORDS = new CharArraySet(stop, true);
	}

	private static final ObjectMapper PAYLOAD_MAPPER = new ObjectMapper();
	private final Directory suggestIndexDirectory;
	private final String suggestOutputDirName;
	private final String suggestOutputZipFileName;
	private final boolean verbose;
	private final Map<String, File> suggestIndexDumpFilesMap;

	// suggestion -> occurrences
	private record Suggestion(String name, SuggestionPayload.Metadata metadata) {
	}

	private final Map<Suggestion, Integer> suggestionToOccurrencesMap = new LinkedHashMap<>();

	// prefix -> (suggestion -> occurrences)
	private final Map<String, Map<String, Integer>> suggestionToOccurrencesMapsForCsv = new LinkedHashMap<>();

	public SuggestionIndexer(int maxWordsInSuggestion,
			Directory suggestIndexDirectory,
			String suggestOutputDirName,
			String suggestOutputZipFileName,
			boolean verbose,
			Map<String, File> suggestIndexDumpFilesMap) {
		this.suggestIndexDirectory = suggestIndexDirectory;
		this.suggestOutputDirName = suggestOutputDirName;
		this.suggestOutputZipFileName = suggestOutputZipFileName;
		this.verbose = verbose;
		this.suggestIndexDumpFilesMap = suggestIndexDumpFilesMap == null ? Collections.emptyMap() : suggestIndexDumpFilesMap;

		for (String prefix : this.suggestIndexDumpFilesMap.keySet()) {
			this.suggestionToOccurrencesMapsForCsv.put(prefix, new HashMap<>());
		}
	}

	public void generateSuggestionsFromConceptName(String conceptName, int totalNum) {
		generateSuggestionsFromConceptName(conceptName, new SuggestionPayload(null, totalNum));
	}

	public void generateSuggestionsFromConceptName(String conceptName, SuggestionPayload payload) {
		SuggestionPayload.Metadata metadata = payload.metadata();
		suggestionToOccurrencesMap.merge(new Suggestion(conceptName, metadata), payload.totalNum(), Integer::sum);

		for (Map.Entry<String, Map<String, Integer>> dump : suggestionToOccurrencesMapsForCsv.entrySet()) {
			if (conceptName.startsWith(dump.getKey())) {
				// Keep the existing format for optional CSV exports.
				String csvSuggestion = conceptName;
				if (metadata != null) {
					csvSuggestion += "~|" + metadata.tableName() + "~|" + metadata.dimCode()
							+ "~|" + metadata.baseCode() + "~|" + metadata.keyName()
							+ "~|" + metadata.visualAttributes() + "~|" + metadata.tooltip();
				}
				dump.getValue().merge(csvSuggestion, 1, Integer::sum);
			}
		}
	}

	public void buildSuggestionIndex() throws IOException {
		try (StandardAnalyzer analyzer = new StandardAnalyzer();
				AnalyzingInfixSuggester suggester = new AnalyzingInfixSuggester(
						suggestIndexDirectory, analyzer, analyzer, 3, true)) {
			Set<BytesRef> contexts = Collections.singleton(new BytesRef("all"));
			for (Map.Entry<Suggestion, Integer> entry : suggestionToOccurrencesMap.entrySet()) {
				Suggestion suggestion = entry.getKey();
				int totalNum = entry.getValue();
				SuggestionPayload payload = new SuggestionPayload(suggestion.metadata(), totalNum);
				suggester.add(new BytesRef(suggestion.name()), contexts, totalNum,
						new BytesRef(PAYLOAD_MAPPER.writeValueAsBytes(payload)));
				if (verbose) {
					logesapi.info(totalNum + " : " + suggestion.name());
				}
			}
			suggester.commit();
			logesapi.error("Suggester word count: " + suggester.getCount());

			for (String prefix : suggestIndexDumpFilesMap.keySet()) {
				generateCSV(prefix, suggestIndexDumpFilesMap.get(prefix), suggestionToOccurrencesMapsForCsv.get(prefix));
			}
			suggestionToOccurrencesMap.clear();
			suggestionToOccurrencesMapsForCsv.clear();
		}
	}

	private void generateCSV(String prefix, File suggestIndexDumpFile, Map<String, Integer> suggestionToOccurrencesMap) {
		logesapi.info("Generating CSV dump of all suggestions that contain '" + prefix + "' and their number of occurrences (in " + suggestIndexDumpFile.getName() + ")");

		try (CSVWriter csvWriter = new CSVWriter(new FileWriter(suggestIndexDumpFile))) {
			// sort suggestions alphabetically
			TreeMap<String, Integer> sortedSuggestions = new TreeMap<>(suggestionToOccurrencesMap);
			for (Map.Entry<String, Integer> e : sortedSuggestions.entrySet()) {
				String[] arr = new String[] { e.getKey(), e.getValue().toString() };
				csvWriter.writeNext(arr);
			}
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}

		logesapi.info("DONE Generating CSV dump of all suggestions matching prefix " + prefix + " and their number of occurrences (in " + suggestIndexDumpFile.getName() + ")");
	}
}
