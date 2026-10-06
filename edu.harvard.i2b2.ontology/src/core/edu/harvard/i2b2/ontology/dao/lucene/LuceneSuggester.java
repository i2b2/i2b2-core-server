package edu.harvard.i2b2.ontology.dao.lucene;


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
import edu.harvard.i2b2.ontology.datavo.pm.ProjectType;
import edu.harvard.i2b2.ontology.datavo.vdo.ConceptType;
import edu.harvard.i2b2.ontology.datavo.vdo.ConceptsType;
import edu.harvard.i2b2.ontology.datavo.vdo.VocabRequestType;
import edu.harvard.i2b2.ontology.util.OntologyUtil;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Java translation of LuceneSuggester.scala
 */
public final class LuceneSuggester {

	private static  String suggestIndexDirectoryName;
	private static  int suggestionsReturnedCount;
	private static  File suggestIndexDirectory;
	private static  FSDirectory luceneDirectory;
	private static  StandardAnalyzer analyzer;
	private static  AnalyzingInfixSuggester analyzingInfixSuggester;
	private static  AnalyzingSuggester analyzingSuggester;

	private static Log log = LogFactory.getLog(LuceneSuggester.class);
	public LuceneSuggester (String projectInfo ){

		try {
			suggestIndexDirectoryName = System.getProperty("user.dir") + File.separatorChar + "standalone" + File.separatorChar + "autosuggest_index" + File.separatorChar + projectInfo; //orElseThrow(() -> new IllegalArgumentException("suggest index dir required"));

			if (OntologyUtil.getInstance().getAutosuggestIndexDirectory() != null && OntologyUtil.getInstance().getAutosuggestIndexDirectory() != "")
				suggestIndexDirectoryName = OntologyUtil.getInstance().getAutosuggestIndexDirectory();
			suggestionsReturnedCount = -1; 
			suggestIndexDirectory = new File(suggestIndexDirectoryName);
			luceneDirectory = FSDirectory.open(suggestIndexDirectory.toPath());
			analyzer = new StandardAnalyzer();
			analyzingInfixSuggester = new AnalyzingInfixSuggester(luceneDirectory, analyzer, analyzer, 3, true);
			//analyzingSuggester = new AnalyzingSuggester(luceneDirectory,"temp_prefix" , analyzer); //, analyzer, 3, true);
		} catch (RuntimeException | IOException | I2B2Exception e) {
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
	public static ConceptsType getSuggestions(final SuggestQuery suggestQuery, String projectId, VocabRequestType vocabType, boolean isoOfuscated ) {
		if (suggestQuery == null) return null;

		if (suggestIndexDirectoryName == null)
			return null;

		String suggestString;
		try {
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
		for (int i = 0; i < results.size(); i++) {

			AutoSuggestResult a = AutoSuggestResult.fromLookup(results.get(i));


			ConceptType b = new ConceptType();
			String[] suggestion = a.suggestion.split("~\\|");
			if (!vocabType.isReducedResults() && suggestion.length < 3)
				continue;

			if (vocabType.getCategory() == null || vocabType.getCategory().equals("@") ||
					(suggestion.length > 1 && vocabType.getCategory().equalsIgnoreCase(suggestion[1])) ){
			//if (suggestion.length > 1) {
				b.setName(suggestion[0]);
				if (suggestion.length > 1)
					b.setTablename(suggestion[1]);

				if (suggestion.length > 2 && StringUtils.isNumeric(suggestion[2]))
					b.setTotalnum(Integer.valueOf(suggestion[2]));

				if (suggestion.length > 3)
					b.setBasecode(suggestion[3]);

				if (suggestion.length > 4)
					b.setVisualattributes(suggestion[4]);

				b.setLevel((int) a.occurrences);

				 if ( suggestion.length < 2 && conceptCount > 0)
				{
					suggestions.getConcept().add(b);
					conceptCount--;

				} else if ( suggestion.length > 3 && patientCount > 0 ) {
					if (isoOfuscated)
						b.setTotalnum(-1);
					suggestions.getConcept().add(b);
					patientCount--;
				} 
			}
			//suggestions.add(AutoSuggestResult.fromLookup(results.get(i)));
		}
		return suggestions;
	}

	// AutoSuggestResult equivalent
	public static class AutoSuggestResult {
		public final String suggestion;
		public final long occurrences;
		public final long weight;

		public AutoSuggestResult(String suggestion, long occurrences, long weight) {
			this.suggestion = suggestion;
			this.occurrences = occurrences;
			this.weight = weight;
		}

		public static AutoSuggestResult fromLookup(Lookup.LookupResult result) {
			if (result == null) return new AutoSuggestResult("", 0L, 0L);
			long occurrences = 0L;
			if (result.payload != null && result.payload.bytes != null) {
				String payload = new String(result.payload.bytes, StandardCharsets.UTF_8);
				try {
					occurrences = Long.parseLong(payload);
				} catch (NumberFormatException e) {
					occurrences = 0L;
				}
			}
			return new AutoSuggestResult(result.key.toString(), occurrences, result.value);
		}
	}
}

