package edu.harvard.i2b2.ontology.dao.lucene;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;

import com.opencsv.CSVWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
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

    private final int maxWordsInSuggestion;
    private final Directory suggestIndexDirectory;
    private final String suggestOutputDirName;
    private final String suggestOutputZipFileName;
    private final boolean verbose;
    private final Map<String, File> suggestIndexDumpFilesMap;

    // suggestion -> occurrences
    private final Map<String, Integer> suggestionToOccurrencesMap = new HashMap<>();

    // prefix -> (suggestion -> occurrences)
    private final Map<String, Map<String, Integer>> suggestionToOccurrencesMapsForCsv = new LinkedHashMap<>();

    private final StandardAnalyzer conceptNameAnalyzer = new StandardAnalyzer(STOP_WORDS);

    public SuggestionIndexer(int maxWordsInSuggestion,
                             Directory suggestIndexDirectory,
                             String suggestOutputDirName,
                             String suggestOutputZipFileName,
                             boolean verbose,
                             Map<String, File> suggestIndexDumpFilesMap) {
        this.maxWordsInSuggestion = maxWordsInSuggestion;
        this.suggestIndexDirectory = suggestIndexDirectory;
        this.suggestOutputDirName = suggestOutputDirName;
        this.suggestOutputZipFileName = suggestOutputZipFileName;
        this.verbose = verbose;
        this.suggestIndexDumpFilesMap = suggestIndexDumpFilesMap == null ? Collections.emptyMap() : suggestIndexDumpFilesMap;

        for (String prefix : this.suggestIndexDumpFilesMap.keySet()) {
            this.suggestionToOccurrencesMapsForCsv.put(prefix, new HashMap<>());
        }
    }

    /**
     * Tokenize a concept name (removing stop words), generate 1..maxWordsInSuggestion
     * ordered combinations and tally them.
     */
    public void generateSuggestionsFromConceptName(String conceptName) {
        try {
            org.apache.lucene.analysis.TokenStream suggestTokenStream = conceptNameAnalyzer.tokenStream(null, conceptName);
            CharTermAttribute cattr = suggestTokenStream.addAttribute(CharTermAttribute.class);
            suggestTokenStream.reset();

            // Use a sorted set of words (alphabetical) like the Scala MutableSortedSet
            SortedSet<String> wordsInConceptName = new TreeSet<>();
            while (suggestTokenStream.incrementToken()) {
                String word = cattr.toString();
                wordsInConceptName.add(word);
            }

            if (verbose) logesapi.info("----------------- updated <word-combinations> -> <occurrences> based on the concept name tokens " + wordsInConceptName + ":");

            String[] wordsInNameArr = wordsInConceptName.toArray(new String[0]);
            int l = wordsInNameArr.length;
            for (int i1 = 0; i1 < l; i1++) {
                String w1 = wordsInNameArr[i1];
                tallySuggestion(Collections.singletonList(w1));
                if (maxWordsInSuggestion > 1) {
                    for (int i2 = i1 + 1; i2 < l; i2++) {
                        String w2 = wordsInNameArr[i2];
                        tallySuggestion(Arrays.asList(w1, w2));
                        if (maxWordsInSuggestion > 2) {
                            for (int i3 = i2 + 1; i3 < l; i3++) {
                                String w3 = wordsInNameArr[i3];
                                tallySuggestion(Arrays.asList(w1, w2, w3));
                            }
                        }
                    }
                }
            }

            suggestTokenStream.end();
            suggestTokenStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void tallySuggestion(List<String> suggestionWords) {
        String suggestion = String.join(" ", suggestionWords);
        suggestionToOccurrencesMap.merge(suggestion, 1, Integer::sum);
        if (verbose) logesapi.info(suggestion + " -> " + suggestionToOccurrencesMap.get(suggestion));

        if (!suggestIndexDumpFilesMap.isEmpty()) {
            for (String p : suggestionToOccurrencesMapsForCsv.keySet()) {
                boolean prefixFound = false;
                for (String w : suggestionWords) {
                    if (w.startsWith(p)) prefixFound = true;
                }
                if (prefixFound) {
                    Map<String, Integer> m = suggestionToOccurrencesMapsForCsv.get(p);
                    m.merge(suggestion, 1, Integer::sum);
                    if (verbose) logesapi.info("For CSV dump, prefix '" + p + "': " + suggestion + " -> " + m.get(suggestion));
                }
            }
        }
    }

    public void buildSuggestionIndex() throws IOException {
        StandardAnalyzer analyzer = new StandardAnalyzer();
        AnalyzingInfixSuggester suggester = new AnalyzingInfixSuggester(suggestIndexDirectory, analyzer, analyzer, 3, true);

        try {
            TreeMap<Integer, SortedSet<String>> occurrenceToSuggestionsMap = generateOccurrenceToSuggestionsMap();

            logesapi.info("Ordering suggestions by weight");
            generateOrderedByWeightSuggestionsMap(occurrenceToSuggestionsMap, suggester);

            occurrenceToSuggestionsMap.clear();

            logesapi.info("Suggester word count: " + suggester.getCount());

            // Create CSV dump files for prefixes
            for (String p : suggestIndexDumpFilesMap.keySet()) {
                generateCSV(p, suggestIndexDumpFilesMap.get(p), suggestionToOccurrencesMapsForCsv.get(p));
            }

            suggestionToOccurrencesMapsForCsv.clear();
        } finally {
            suggester.close();
        }
    }

    private static final Comparator<String> DESCENDING_ALPHABET_ORDERING = (a, b) -> b.compareTo(a);

    private TreeMap<Integer, SortedSet<String>> generateOccurrenceToSuggestionsMap() {
        logesapi.debug("Generating the occurrence to suggestions map  from " + suggestionToOccurrencesMap.size() + " suggestions");

        TreeMap<Integer, SortedSet<String>> occurrenceToSuggestionsMap = new TreeMap<>();

        // suggestionKeys iteration
        for (Map.Entry<String, Integer> e : new ArrayList<>(suggestionToOccurrencesMap.entrySet())) {
            String suggestion = e.getKey();
            int occurrences = e.getValue();

            occurrenceToSuggestionsMap.computeIfAbsent(occurrences, k -> new TreeSet<>(DESCENDING_ALPHABET_ORDERING)).add(suggestion);

            suggestionToOccurrencesMap.remove(suggestion);
        }

        suggestionToOccurrencesMap.clear();

        return occurrenceToSuggestionsMap;
    }

    private void generateOrderedByWeightSuggestionsMap(TreeMap<Integer, SortedSet<String>> occurrenceToSuggestionsMap,
                                                       AnalyzingInfixSuggester suggester) throws IOException {
        int w = 0;

        if (verbose) {
            logesapi.info("==============================================");
            logesapi.info("Weighted suggestions:");
            logesapi.info("weight (#occurrences) : suggestion");
        }

        // iterate over occurrence keys in ascending order
        for (Map.Entry<Integer, SortedSet<String>> entry : new ArrayList<>(occurrenceToSuggestionsMap.entrySet())) {
            int occurrences = entry.getKey();
            SortedSet<String> suggestions = entry.getValue();

            for (String s : suggestions) {
                w += 1;
                Set<BytesRef> contexts = new HashSet<>();
                try {
                    contexts.add(new BytesRef("all".getBytes("UTF8")));
                    suggester.add(new BytesRef(s.getBytes("UTF8")), contexts, w, new BytesRef(String.valueOf(occurrences).getBytes("UTF8")));
                    if (verbose) {
                        logesapi.info(w + " (" + occurrences + ") : " + s);
                    }
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        suggester.commit();
        // do not close here; caller will close in finally

        if (verbose) {
            logesapi.info("==============================================");
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
