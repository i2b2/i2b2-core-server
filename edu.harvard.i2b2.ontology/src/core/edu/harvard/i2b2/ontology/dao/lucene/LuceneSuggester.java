package edu.harvard.i2b2.ontology.dao.lucene;


import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.search.suggest.Lookup;
import org.apache.lucene.search.suggest.analyzing.AnalyzingInfixSuggester;
import org.apache.lucene.search.suggest.analyzing.AnalyzingSuggester;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.ontology.dao.ConceptDao;
import edu.harvard.i2b2.ontology.dao.lucene.LuceneService.SuggestQuery;
import edu.harvard.i2b2.ontology.datavo.i2b2message.SecurityType;
import edu.harvard.i2b2.ontology.datavo.pm.ParamType;
import edu.harvard.i2b2.ontology.datavo.pm.ProjectType;
import edu.harvard.i2b2.ontology.datavo.vdo.ConceptType;
import edu.harvard.i2b2.ontology.datavo.vdo.ConceptsType;
import edu.harvard.i2b2.ontology.datavo.vdo.VocabRequestType;
import edu.harvard.i2b2.ontology.util.OntologyUtil;
import edu.harvard.i2b2.pm.ws.PMServiceDriver;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Java translation of LuceneSuggester.scala
 */
public  class LuceneSuggester {

	private   String suggestIndexDirectoryName;
	private   int suggestionsReturnedCount;
	private   File suggestIndexDirectory;
	private   FSDirectory luceneDirectory;
	private   StandardAnalyzer analyzer;
	private   AnalyzingInfixSuggester analyzingInfixSuggester;
	private   AnalyzingSuggester analyzingSuggester;

	private  Log log = LogFactory.getLog(LuceneSuggester.class);
	private static final ObjectMapper PAYLOAD_MAPPER = new ObjectMapper();
	public LuceneSuggester (String projectInfo ){

		suggestIndexDirectoryName = System.getProperty("user.dir") + File.separatorChar + "standalone" + File.separatorChar + "autosuggest_index" + File.separatorChar + projectInfo; //orElseThrow(() -> new IllegalArgumentException("suggest index dir required"));
		try {

			if (OntologyUtil.getInstance().getAutosuggestIndexDirectory() != null && OntologyUtil.getInstance().getAutosuggestIndexDirectory() != "")
				suggestIndexDirectoryName = OntologyUtil.getInstance().getAutosuggestIndexDirectory() + File.separatorChar + projectInfo;
						
		} catch (Exception e) {}
		try {
			suggestionsReturnedCount = -1; 
			suggestIndexDirectory = new File(suggestIndexDirectoryName);
			luceneDirectory = FSDirectory.open(suggestIndexDirectory.toPath());
			analyzer = new StandardAnalyzer();
			analyzingInfixSuggester = new AnalyzingInfixSuggester(luceneDirectory, analyzer, analyzer, 3, true);
			//analyzingSuggester = new AnalyzingSuggester(luceneDirectory,"temp_prefix" , analyzer); //, analyzer, 3, true);
		} catch (RuntimeException | IOException e) {
			log.error("Suggestor not loaded for : " + suggestIndexDirectoryName);
			throw new ExceptionInInitializerError(e);
		}

	}

	/**
	 * Returns a CompletableFuture that runs the suggestion lookup async.

    public static CompletableFuture<List<AutoSuggestResult>> getSuggestionsIO(final SuggestQuery suggestQuery) {
        return CompletableFuture.supplyAsync(() -> getSuggestions(suggestQuery));
    }
	 */
	/**
	 * Return an empty list if the input contains fewer than 3 non-blank characters
	 * Note that if the input contains double-quotes, an empty list will also be returned
	 * because double quotes are not indexed.
	 * @param vocabType 
	 */
	public  ConceptsType getSuggestions(final SuggestQuery suggestQuery, String projectId, SecurityType security, VocabRequestType vocabType, boolean isoOfuscated, boolean onlyWithBaseCode ) {
		if (suggestQuery == null) return null;

		if (suggestIndexDirectoryName == null)
			return null;
	

		String suggestString;
		try {
			ParamType paramIndexStatus = PMServiceDriver.getProjectParam(
					"AUTOSUGGEST_INDEX_STATUS",  security, projectId,
					OntologyUtil.getInstance()
					.getPmEndpointReference());
			
			
			if (paramIndexStatus == null || !paramIndexStatus.getValue().equals("FINISHED"))
				return null;

			// Scala case class provides a suggestString() accessor
			suggestString = suggestQuery.getSuggestString(); //(String) suggestQuery.getClass().getMethod("suggestString").invoke(suggestQuery);
		} catch (Exception e) {
			// Fallback: try a field access (less likely)
			log.error("Error: " + e.getMessage() + " for folder " + suggestIndexDirectoryName);
			try {
				Object val = suggestQuery.getClass().getField("suggestString").get(suggestQuery);
				suggestString = val == null ? "" : val.toString();
			} catch (Exception ex) {
				return null;
			}
		}

		if (suggestString.replace(" ", "").length() < 3) {
			return null;
		}

		final String contextStr = "all";
		final Set<BytesRef> contexts = new HashSet<>();
		contexts.add(new BytesRef(contextStr.getBytes(StandardCharsets.UTF_8)));

		List<Lookup.LookupResult> results;
		try {
			//if (vocabType.isReducedResults())
			//	suggestString += "~|~|~|";
			//results = suggester.lookup(suggestString, contexts, vocabType.getMax(), true, true);
			results = analyzingInfixSuggester.lookup(suggestString,  10000, true, true);
			//results = analyzingSuggester.lookup(suggestString, false, 10000);
		} catch (Exception e) {
			log.error("Error: " + e.getMessage() + " for folder " + suggestIndexDirectoryName);
			throw new RuntimeException(e);
		}

		if (results == null || results.isEmpty()) return null;

		//final int limit = Math.min(results.size(), suggestionsReturnedCount);
		// List<AutoSuggestResult> suggestions = new ArrayList<>(limit);
		ConceptsType suggestions = new  ConceptsType();
		int patientCount = vocabType.getMax();
		int conceptCount = vocabType.getMax();
		if (onlyWithBaseCode)
			conceptCount = 0;
		for (int i = 0; i < results.size(); i++) {

			AutoSuggestResult result;
			try {
				result = decodeSuggestion(results.get(i));
			} catch (IOException e) {
				log.warn("Unable to read autosuggest payload for " + suggestIndexDirectoryName, e);
				continue;
			}

			SuggestionPayload payload = result.payload();
			SuggestionPayload.Metadata metadata = payload.metadata();
			if (!Boolean.TRUE.equals(vocabType.isReducedResults()) && (metadata == null || metadata.dimCode() == null))
				continue;

			String category = vocabType.getCategory();
			if (category != null && !category.equals("@")
					&& (metadata == null || !category.equalsIgnoreCase(metadata.tableName())))
				continue;

			ConceptType concept = new ConceptType();
			concept.setName(result.name());
			concept.setLevel(0);
			concept.setTotalnum(payload.totalNum());
			if (metadata != null) {
				concept.setTablename(metadata.tableName());
				concept.setDimcode(metadata.dimCode());
				concept.setBasecode(metadata.baseCode());
				concept.setKeyName(metadata.keyName());
				concept.setVisualattributes(metadata.visualAttributes());
				concept.setTooltip(metadata.tooltip());
			}

			if (metadata == null && conceptCount > 0) {
				suggestions.getConcept().add(concept);
				conceptCount--;
			} else if (metadata != null && metadata.baseCode() != null && patientCount > 0) {
				if (isoOfuscated)
					concept.setTotalnum(-1);
				suggestions.getConcept().add(concept);
				patientCount--;
			}
		}
		return suggestions;
	}

	public record AutoSuggestResult(String name, SuggestionPayload payload) {
	}

	private static AutoSuggestResult decodeSuggestion(Lookup.LookupResult result) throws IOException {
		String name = result.key.toString();
		BytesRef bytes = result.payload;
		if (bytes != null && bytes.length > 0 && bytes.bytes[bytes.offset] == '{') {
			SuggestionPayload payload = PAYLOAD_MAPPER.readValue(
					bytes.bytes, bytes.offset, bytes.length, SuggestionPayload.class);
			return new AutoSuggestResult(name, payload);
		}

		// Older indexes store metadata in the key or after the count in the payload.
		String count = bytes == null ? "" : bytes.utf8ToString();
		int metadataStart = count.indexOf("~|");
		if (metadataStart >= 0) {
			name += count.substring(metadataStart);
			count = count.substring(0, metadataStart);
		}
		int totalNum = NumberUtils.toInt(count);
		String[] fields = name.split("~\\|");
		SuggestionPayload.Metadata metadata = null;
		if (fields.length > 1) {
			String[] metadataFields = Arrays.copyOf(fields, 7);
			metadata = new SuggestionPayload.Metadata(metadataFields[1], metadataFields[2], metadataFields[3],
					metadataFields[4], metadataFields[5], metadataFields[6]);
		}
		String legacyName = fields.length == 0 ? "" : fields[0];
		return new AutoSuggestResult(legacyName, new SuggestionPayload(metadata, totalNum));
	}
}
