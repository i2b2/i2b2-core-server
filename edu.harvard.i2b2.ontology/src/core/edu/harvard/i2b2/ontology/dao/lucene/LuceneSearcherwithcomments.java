package edu.harvard.i2b2.ontology.dao.lucene;

//import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.StoredFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.classic.QueryParserBase;
import org.apache.lucene.search.*;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.search.highlight.SimpleSpanFragmenter;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

import edu.harvard.i2b2.ontology.dao.lucene.LuceneService.CODE_CATEGORY;
import edu.harvard.i2b2.ontology.dao.lucene.LuceneService.CODE_SET;
import edu.harvard.i2b2.ontology.dao.lucene.LuceneService.CodeCategoryTerm;
import edu.harvard.i2b2.ontology.dao.lucene.LuceneService.ConceptType;
import edu.harvard.i2b2.ontology.dao.lucene.LuceneService.FilterData;
import edu.harvard.i2b2.ontology.dao.lucene.LuceneService.FilterOption;
import edu.harvard.i2b2.ontology.dao.lucene.LuceneService.FilterableType;
import edu.harvard.i2b2.ontology.dao.lucene.LuceneService.NO_FILTER;
import edu.harvard.i2b2.ontology.dao.lucene.LuceneService.OntologyPath;
import edu.harvard.i2b2.ontology.dao.lucene.LuceneService.OntologyTerm;
import edu.harvard.i2b2.ontology.dao.lucene.LuceneService.SearchQuery;
import edu.harvard.i2b2.ontology.dao.lucene.LuceneService.SearchResults;
import edu.harvard.i2b2.ontology.dao.lucene.LuceneService.SearchResultsMetadata;
import edu.harvard.i2b2.ontology.dao.lucene.LuceneService.VisualAttributes;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Java translation of LuceneSearcher.scala adjusted for Lucene 10.
 * Notes:
 * - Uses SortField.Type.STRING for sorting. The original Scala code used a custom comparator to do case-insensitive
 *   ordering and special nPath ordering. Implementing an exact FieldComparator equivalent for Lucene 10 is possible
 *   but requires a more detailed FieldComparator implementation; if you need exact behavior we can add it next.
 */
public final class LuceneSearcherwithcomments { // implements Loggable {

	private static final String indexFileLocation = "/Users/mem61/Downloads/lucene_i2b2/lucene_index"; //ConfigSource.config.getString("shrine.lucene.directory");
	private static final File indexDir = new File(indexFileLocation);
	private static final FSDirectory directory;
	public static final IndexSearcher searcher;
	private static final StandardAnalyzer analyzer = new StandardAnalyzer();
	//private static final ObjectMapper mapper = new ObjectMapper();


	protected final static Log logesapi = LogFactory.getLog("LuceneSearcher");


	static {
		try {
			directory = FSDirectory.open(indexDir.toPath());
			searcher = new IndexSearcher(DirectoryReader.open(directory));
		} catch (IOException e) {
			throw new ExceptionInInitializerError(e);
		}
	}
	
	
	
    public static void main(String[] args) throws Exception {
    	
    	List<CodeCategoryTerm> myroot = getRootTerms();
    	 SearchQuery mysearch = new SearchQuery();
    	 mysearch.searchString = "Asthma";
    	 mysearch.filterData = new FilterData(new NO_FILTER(), "All Concepts");
		 SearchResults mylist = searchIO(mysearch);
//    	Optional<OntologyTerm> mylist = getSingleTermByPathAndDisplayName("\\i2b2\\Diagnoses\\|Diagnoses", "Asthma");
    	
        long startTime = System.currentTimeMillis();


        
    }

	public static List<CodeCategoryTerm> getRootTerms() throws IOException, ParseException {
		TopFieldDocs foundDocs = getRootFieldDocs();

		// group by codeCategory
		Map<String, List<ScoreDoc>> docsToCodeCategories = Arrays.stream(foundDocs.scoreDocs)
				.collect(Collectors.groupingBy(sd -> getDocValue(sd).get("codeCategory")));

		List<String> sortedKeys = new ArrayList<>(docsToCodeCategories.keySet());
		Collections.sort(sortedKeys);

		List<CodeCategoryTerm> result = new ArrayList<>();
		for (String codeCategory : sortedKeys) {
			List<ScoreDoc> sds = docsToCodeCategories.get(codeCategory);
			List<OntologyTerm> ontologyTerms = sds.stream().map(LuceneSearcherwithcomments::extractOntologyTermFromScoreDoc).collect(Collectors.toList());
			result.add(new CodeCategoryTerm(codeCategory, null, false, ontologyTerms));
		}

		return result;
	}

	private static Map<String, String> getDocValue(ScoreDoc sd) {
		try {

			StoredFields storedFields = LuceneSearcherwithcomments.searcher.storedFields();
			// Document doc = storedFields.document(sd.doc);
			Document doc = storedFields.document(sd.doc);
			Map<String, String> map = new HashMap<>();
			for (IndexableField f : doc.getFields()) {
				String name = f.name();
				String val = doc.get(name);
				map.put(name, val);
			}
			return map;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static Optional<OntologyTerm> getSingleTermByPathAndDisplayName(String path, String displayName) throws IOException {
		BooleanQuery.Builder bq = new BooleanQuery.Builder();
		bq.add(new TermQuery(new Term("path", path)), BooleanClause.Occur.MUST);
		StoredFields storedFields = LuceneSearcherwithcomments.searcher.storedFields();
		List<ScoreDoc> sds = getRawSearchResults(bq.build());
		if (sds.size() > 1) {
			logesapi.warn("displayName and path should be unique to an ontology term. For path (" + path + ") and displayName (" + displayName + ") found " + sds.size() + " results");
			Optional<ScoreDoc> match = sds.stream().filter(sd -> {
				try {
					String dn = storedFields.document(sd.doc).get("displayName");
					String isRoot = storedFields.document(sd.doc).get("isRoot");
					return displayName.equals(dn) || Boolean.parseBoolean(isRoot);
				} catch (IOException e) { return false; }
			}).findFirst();

			if (match.isPresent()) return Optional.of(extractOntologyTermFromScoreDoc(match.get()));
			else return Optional.of(extractOntologyTermFromScoreDoc(sds.get(0)));
		} else if (sds.size() == 1) {
			return Optional.of(extractOntologyTermFromScoreDoc(sds.get(0)));
		} else {
			return Optional.empty();
		}
	}

	public static List<FilterOption> getFilterOptions() throws IOException, ParseException {
		FilterOption defaultOption = new FilterOption(new NO_FILTER(), "All Concepts", "All Concepts");
		TopFieldDocs foundDocs = getRootFieldDocs();

		// distinct code categories
		Set<String> allCodeCategories = Arrays.stream(foundDocs.scoreDocs)
				.map(sd -> getDocValue(sd).get("codeCategory"))
				.filter(Objects::nonNull)
				.collect(Collectors.toCollection(LinkedHashSet::new));

		List<FilterOption> codeCategoryOptions = allCodeCategories.stream().map(codeCategory -> {
			String displayableName;
			switch (codeCategory) {
			case "Diagnoses": displayableName = "Diagnoses (All)"; break;
			case "Laboratory Tests": displayableName = "Laboratory Tests (LOINC)"; break;
			case "Medications": displayableName = "Medications (RxNORM)"; break;
			case "Procedures": displayableName = "Procedures (All)"; break;
			default: displayableName = codeCategory; break;
			}
			return new FilterOption(new CODE_CATEGORY(), codeCategory, displayableName);
		}).collect(Collectors.toList());

		// codeSet map
		Map<String, List<FilterOption>> codeSetMap = Arrays.stream(foundDocs.scoreDocs)
				.map(sd -> new AbstractMap.SimpleEntry<>(getDocValue(sd).get("codeCategory"), getDocValue(sd).get("codeSet")))
				.filter(e -> e.getValue() != null && !e.getValue().isEmpty())
				.collect(Collectors.groupingBy(Map.Entry::getKey, LinkedHashMap::new, Collectors.mapping(e -> new FilterOption(new CODE_SET(), e.getValue(), e.getValue()), Collectors.toList())));

		List<FilterOption> combined = new ArrayList<>();
		for (FilterOption cat : codeCategoryOptions) {
			combined.add(cat);
			List<FilterOption> sets = codeSetMap.get(cat.filterValue);
			if (sets != null) combined.addAll(sets);
		}

		List<FilterOption> allOptions = new ArrayList<>();
		allOptions.add(defaultOption);
		allOptions.addAll(combined);
		return allOptions;
	}

	public static Map<OntologyPath, CodeCategory> getCodeCategoriesMap() throws IOException, ParseException {
		TopFieldDocs foundDocs = getRootFieldDocs();
		Map<OntologyPath, CodeCategory> map = new LinkedHashMap<>();
		for (ScoreDoc sd : foundDocs.scoreDocs) {
			Map<String,String> doc = getDocValue(sd);
			map.put(new OntologyPath(doc.get("path")), new CodeCategory(doc.get("codeCategory")));
		}
		return map;
	}

	public static List<ScoreDoc> getRawSearchResults(Query query) throws IOException {
		int maxHits = Integer.MAX_VALUE;
		TopFieldDocs foundDocs = searcher.search(query, maxHits, CaseInsensitiveSort("displayName"));
		return Arrays.asList(foundDocs.scoreDocs);
	}

	public static List<OntologyTerm> getChildren(OntologyPath ontologyPath) throws IOException {
		String parentPath = ontologyPath.path.substring(0, Math.max(0, ontologyPath.path.length() - 1));
		Query childrenQuery = new TermQuery(new Term("parentPath", parentPath));
		List<ScoreDoc> sds = getRawSearchResults(childrenQuery);
		return sds.stream().map(LuceneSearcherwithcomments::extractOntologyTermFromScoreDoc).collect(Collectors.toList());
	}

	private static boolean isNotSamePath(StoredFields storedFields, ScoreDoc sd, String ontologyPath) {
		try {
		String path = storedFields.document(sd.doc).get("path");
		return !Objects.equals(path, ontologyPath);
		} catch (IOException e) {
		// optionally log the exception here
		return false; // same behavior as original: treat as excluded
		}
		}
	
	public static <R> Optional<ConceptInfo> getConceptInfo(OntologyPath ontologyPath) throws IOException {
		Query pathQuery = new TermQuery(new Term("path", ontologyPath.path));
		Query childrenQuery = new TermQuery(new Term("parentPath", ontologyPath.path.substring(0, Math.max(0, ontologyPath.path.length() - 1))));
		BooleanQuery.Builder bq = new BooleanQuery.Builder();
		bq.add(pathQuery, BooleanClause.Occur.SHOULD);
		bq.add(childrenQuery, BooleanClause.Occur.SHOULD);
		StoredFields storedFields = LuceneSearcherwithcomments.searcher.storedFields();
		List<ScoreDoc> sds = getRawSearchResults(bq.build());

	//	List<ConceptInfo> childConcepts = sds.stream()
	//			.filter(sd -> isNotSamePath(storedFields, sd, ontologyPath.path)
	//			.map(ConceptInfo::SoundDocs.scoreDocs))
	//			.collect(Collectors.toList());
		
		

		
		
	//	List<ConceptInfo> childConcepts = sds.stream().filter(sd -> {
	//		try { return !Objects.equals(storedFields.document(sd.doc).get("path"), ontologyPath.path); } catch (IOException e) { return false; }
	//	}).map(ConceptInfo::fromScoreDoc).collect(Collectors.toList());

		Optional<ScoreDoc> scoreDocOption = sds.stream().filter(sd -> {
			try { return Objects.equals(storedFields.document(sd.doc).get("path"), ontologyPath.path); } catch (IOException e) { return false; }
		}).findFirst();

		if (!scoreDocOption.isPresent()) return Optional.empty();

		ScoreDoc scoreDoc = scoreDocOption.get();
		
        BooleanQuery booleanQuery = new BooleanQuery.Builder()
                .add(pathQuery, BooleanClause.Occur.SHOULD)
                .add(childrenQuery, BooleanClause.Occur.SHOULD)
                .build();
        
    
		 List<ScoreDoc> scoreDocs = getRawSearchResults(booleanQuery);
       // Function ConceptInfo;
		List<ConceptInfo> childConcepts = (List<edu.harvard.i2b2.ontology.dao.lucene.ConceptInfo>) scoreDocs.stream()
                .filter(sd -> {
                    try { return !Objects.equals(storedFields.document(sd.doc).get("path"), ontologyPath.path); } catch (IOException e) { return false; }
                })
                .map((Function<? super ScoreDoc, ? extends R>) scoreDocs)
                .collect(Collectors.toList());
		
		Document doc = storedFields.document(scoreDoc.doc);
		String nPath = doc.get("nPath");
		String visAttrPath = "\\CA" + doc.get("visPath");

		String[] pathElements = Arrays.stream(nPath.split("\\\\")).filter(s -> !s.isEmpty()).toArray(String[]::new);
		String[] pathElementsReverse = reverse(pathElements);
		String[] visPathElementsReverse = reverse(Arrays.stream(visAttrPath.split("\\\\")).filter(s -> !s.isEmpty()).toArray(String[]::new));

		ConceptInfo selected = ConceptInfo.of(nPath, pathElementsReverse[0], VisualAttributes.fromString(visPathElementsReverse[0]), Optional.of(childConcepts), true);

		ConceptInfo current = selected;
		String[] remainingPathElems = Arrays.copyOfRange(pathElementsReverse, 1, pathElementsReverse.length);
		String[] remainingVis = Arrays.copyOfRange(visPathElementsReverse, 1, visPathElementsReverse.length);
		String[] nPaths = Arrays.stream(nPath.split("\\\\")).filter(s -> !s.isEmpty()).toArray(String[]::new);

		for (int i = 0; i < remainingPathElems.length; i++) {
			String pathElem = remainingPathElems[i];
			String vis = remainingVis[i];
			String path = "\\" + String.join("\\", Arrays.copyOfRange(nPaths, 0, nPaths.length - 1 - i)) + "\\";
			ConceptInfo parent = ConceptInfo.of(path, pathElem, VisualAttributes.fromString(vis), Optional.of(Collections.singletonList(current)), false);
			current = parent;
		}

		return Optional.of(current);
	}

	private static String[] reverse(String[] arr) {
		String[] r = new String[arr.length];
		for (int i = 0; i < arr.length; i++) r[i] = arr[arr.length - 1 - i];
		return r;
	}

	public static OntologyTerm extractOntologyTermFromScoreDoc(ScoreDoc sd) {
		try {
			StoredFields storedFields = LuceneSearcherwithcomments.searcher.storedFields();

			Document doc = storedFields.document(sd.doc);
			String path = doc.get("path");
			String displayName = doc.get("displayName");
			IndexableField labField = doc.getField("labDetail");
			boolean isLab = labField != null;
			String visualAttributes = doc.get("visualAttributes");
			String conceptCategory = doc.get("conceptCategory");
			String metadata = doc.get("tooltip");
			return extractOntologyTerm(path, displayName, visualAttributes, conceptCategory, isLab, Optional.ofNullable(metadata));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static OntologyTerm extractOntologyTerm(String path, String displayName, String visualAttr, String conceptCategory, boolean isLab, Optional<String> metadata) {
		VisualAttributes va = VisualAttributes.fromString(visualAttr);
		OntologyTerm ontTerm = new OntologyTerm();
		ontTerm.displayName = displayName;
		ontTerm.highlightedName = Optional.empty();
		ontTerm.path = path;
		ontTerm.conceptCategory = conceptCategory;
		ontTerm.conceptType = va.conceptType;
		ontTerm.isActive = va.isActive;
		ontTerm.metadata = metadata;
		ontTerm.isLab = isLab;
		ontTerm.children = Optional.of(Collections.emptyList());
		return ontTerm;
	}

	public static SearchResults searchIO(SearchQuery searchQuery) throws IOException, ParseException {
		String sortTerm = "nPath";

		QueryParser parser = new QueryParser("displayName", analyzer);
		parser.setDefaultOperator(QueryParser.Operator.AND);

		String escapedSearchString;
		if (searchQuery.searchString.matches(".*\\s+$")) {
			escapedSearchString = QueryParserBase.escape(searchQuery.searchString.trim());
		} else {
			escapedSearchString = QueryParserBase.escape(searchQuery.searchString) + "*";
		}

		Query displayNameQuery = parser.parse(escapedSearchString);
		TermQuery basecodeQuery = new TermQuery(new Term("basecode", searchQuery.searchString));

		BooleanQuery.Builder b = new BooleanQuery.Builder();
		b.add(displayNameQuery, BooleanClause.Occur.SHOULD);
		b.add(basecodeQuery, BooleanClause.Occur.SHOULD);
		BooleanQuery displayNameAndBasecodeQuery = b.setMinimumNumberShouldMatch(1).build();

		Query filtered = makeFilteredQuery(displayNameAndBasecodeQuery, searchQuery.filterData);

		int maxResults = 100; //MM ConfigSource.config.getInt("shrine.lucene.maxSearchResults");
		Sort sort = CaseInsensitiveSort(sortTerm);

		TopFieldDocs foundDocs;
		if (searchQuery.previousSearchMetadata != null && searchQuery.previousSearchMetadata.isPresent()) {
			SearchResultsMetadata meta = searchQuery.previousSearchMetadata.get();
			Object sortValue = new BytesRef(meta.sortFieldValue);
			FieldDoc last = new FieldDoc(meta.lastDocId, Float.NaN, new Object[]{sortValue});
			foundDocs = (TopFieldDocs) searcher.searchAfter(last, filtered, maxResults, sort);
		} else {
			foundDocs = searcher.search(filtered, maxResults, sort);
		}

		Highlighter highlighter = HtmlHighlighter.createHighlighter(filtered);

		ScoreDoc[] scoreDocs = foundDocs.scoreDocs;
		long totalHits = foundDocs.totalHits.value();

		StoredFields storedFields = LuceneSearcherwithcomments.searcher.storedFields();

		Map<String, List<ScoreDoc>> grouped = Arrays.stream(scoreDocs)
				.collect(Collectors.groupingBy(sd -> getCodeCategorySafe(storedFields, sd)));


		//Map<String, List<ScoreDoc>> grouped = Arrays.stream(scoreDocs).collect(Collectors.groupingBy(sd -> {
		//	try { return storedFields.document(sd.doc).get("codeCategory"); } catch (IOException e) { return null; }
		//}));

		List<String> keys = new ArrayList<>(grouped.keySet());
		Collections.sort(keys);

		List<CodeCategoryTerm> codeCategoryTerms = new ArrayList<>();
		for (String key : keys) {
			List<ScoreDoc> sds = grouped.get(key);
			Trie trie = new Trie(highlighter);
			for (ScoreDoc sd : sds) trie.insert(sd);
			List<OntologyTerm> ontTerms = buildOntologyTerms(trie.root, "");
			codeCategoryTerms.add(new CodeCategoryTerm(key, null, false, ontTerms));
		}
		ScoreDoc lastSd = scoreDocs.length > 0 ? scoreDocs[scoreDocs.length - 1] : null;
		SearchResultsMetadata metadata = null;
		if (lastSd != null) {
			String sortFieldValue = storedFields.document(lastSd.doc).get(sortTerm);
			metadata = new SearchResultsMetadata(lastSd.doc, sortFieldValue);
		}
		return new SearchResults(totalHits, Optional.ofNullable(metadata), codeCategoryTerms);

		//     return new SearchResults(totalHits, Optional.ofNullable(metadata), codeCategoryTerms);
	}



	private static String getCodeCategorySafe(StoredFields storedFields, ScoreDoc sd) {
		try {
			return storedFields.document(sd.doc).get("codeCategory");
		} catch (IOException e) {
			// log if desired
			return null;
		}
	}


	private static Query makeFilteredQuery(Query baseQuery, FilterData filter) {
		if (filter == null || filter.filterType instanceof NO_FILTER) return baseQuery;
		if (filter.filterType instanceof FilterableType) {
			String field = ((FilterableType) filter.filterType).value;
			Query filterQuery = new TermQuery(new Term(field, filter.filterValue));
			BooleanQuery.Builder b = new BooleanQuery.Builder();
			b.add(baseQuery, BooleanClause.Occur.MUST);
			b.add(filterQuery, BooleanClause.Occur.MUST);
			return b.build();
		}
		return baseQuery;
	}

	public static List<OntologyTerm> buildOntologyTerms(TrieNode node, String path) {
		List<OntologyTerm> results = new ArrayList<>();
		for (Map.Entry<String, TrieNode> entry : node.children.entrySet()) {
			TrieNode child = entry.getValue();
			String childKey = entry.getKey();
			String currentPath = path + childKey + "\\";
			OntologyTerm ont = child.ontTerm.orElse(null);
			List<OntologyTerm> childTerms = buildOntologyTerms(child, currentPath);
			if (ont != null) ont.children = Optional.of(childTerms);
			results.add(ont);
		}
		return results;
	}

	public static Optional<LabDetail> getLabDetailsIO(OntologyPath ontologyPath) throws IOException {
		TermQuery q = new TermQuery(new Term("path", ontologyPath.path));
		TopDocs found = searcher.search(q, 1);
		if (found.scoreDocs.length == 0) return Optional.empty();
		return extractLabDetail(found.scoreDocs[0]);
	}

	private static Optional<LabDetail> extractLabDetail(ScoreDoc sd) {
		try {
			StoredFields storedFields = LuceneSearcherwithcomments.searcher.storedFields();
			Document doc = storedFields.document(sd.doc);
			IndexableField labField = doc.getField("labDetail");
			if (labField == null) return Optional.empty();
			String json = labField.stringValue();
			LabDetail ld = null; //MM mapper.readValue(json, LabDetail.class);
			return Optional.ofNullable(ld);
		} catch (Exception e) {
			return Optional.empty();
		}
	}

	private static TopFieldDocs getRootFieldDocs() throws IOException, ParseException {
		int maxHits = Integer.MAX_VALUE;
		QueryParser qp = new QueryParser("isRoot", analyzer);
		Query rootQuery = qp.parse("true");
		return searcher.search(rootQuery, maxHits, CaseInsensitiveSort("displayName"));
	}

	// --- Trie and helper classes ---
	public static class TrieNode {
		public final String value;
		public String visAttr;
		public Optional<OntologyTerm> ontTerm = Optional.empty();
		public final LinkedHashMap<String, TrieNode> children = new LinkedHashMap<>();
		public boolean isLeaf = false;

		public TrieNode(String value) { this.value = value; }
	}

	public static class Trie {
		public final TrieNode root = new TrieNode("");
		private final Highlighter highlighter;
		public Trie(Highlighter highlighter) { this.highlighter = highlighter; }

		public void insert(ScoreDoc sd) {
			try {
				StoredFields storedFields = LuceneSearcherwithcomments.searcher.storedFields();
				Document doc = storedFields.document(sd.doc);
				String conceptCategory = doc.get("conceptCategory");
				String path = doc.get("path");
				String browsePath = doc.get("nPath");
				String visAttrPath = doc.get("visPath");
				String isLabPath = doc.get("isLabPath");
				String metadata = doc.get("tooltip");
				String text = doc.get("displayName");

				String[] browsePathElements = Arrays.stream(browsePath.split("\\\\")).filter(s -> !s.isEmpty()).toArray(String[]::new);
				List<String> pathElems = Arrays.stream(path.split("\\\\")).filter(s -> !s.isEmpty()).collect(Collectors.toList());

				int minPathElemIndex = (int) (pathElems.size() - (browsePathElements.length - 1)) + 1;
				List<String> pathSectionsList;
				if (minPathElemIndex != 0) {
					String firstElement = String.join("\\", pathElems.subList(0, minPathElemIndex));
					List<String> remaining = pathElems.subList(minPathElemIndex, pathElems.size());
					pathSectionsList = new ArrayList<>();
					pathSectionsList.add(firstElement);
					pathSectionsList.addAll(remaining);
				} else {
					pathSectionsList = pathElems;
				}

				String[] visPathElements = Arrays.stream(visAttrPath.split("\\\\")).filter(s -> !s.isEmpty()).toArray(String[]::new);
				String[] isLabPathElements = Arrays.stream(isLabPath.split("\\\\")).filter(s -> !s.isEmpty()).toArray(String[]::new);

				TrieNode cur = root;
				String currentFullPath = "\\";
				String categoryPathElement = browsePathElements[0];
				String currentFullBrowsePath = "\\" + categoryPathElement + "\\";

				String vispath = "";
				boolean isLab = false;

				int index = 0;
				String[] bp = Arrays.copyOfRange(browsePathElements, 1, browsePathElements.length);
				for (String pathElement : pathSectionsList) {
					currentFullPath = currentFullPath + pathElement + "\\";
					currentFullBrowsePath = currentFullBrowsePath + bp[index] + "\\";

					vispath = visPathElements[index];
					isLab = Boolean.parseBoolean(isLabPathElements[index]);

					if (cur.children.containsKey(currentFullBrowsePath)) {
						cur = cur.children.get(currentFullBrowsePath);
					} else {
						OntologyTerm ot = extractOntologyTerm(currentFullPath, bp[index], vispath, conceptCategory, isLab, Optional.ofNullable(metadata));
						TrieNode temp = new TrieNode(currentFullPath.substring(0, currentFullPath.length() - 1));
						temp.ontTerm = Optional.of(ot);
						cur.children.put(currentFullBrowsePath, temp);
						cur = temp;
					}
					index++;
				}

				cur.isLeaf = true;
				OntologyTerm leaf = extractOntologyTerm(path, bp[bp.length - 1], vispath, conceptCategory, isLab, Optional.ofNullable(metadata));
				cur.ontTerm = Optional.of(leaf);

			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public static class HtmlHighlighter {
		public static final String preTag = "<span class=\"highlight\">";
		public static final String postTag = "</span>";

		public static Highlighter createHighlighter(Query query) {
			QueryScorer scorer = new QueryScorer(query);
			SimpleHTMLFormatter formatter = new SimpleHTMLFormatter(preTag, postTag);
			Highlighter highlighter = new Highlighter(formatter, scorer);
			SimpleSpanFragmenter fragmenter = new SimpleSpanFragmenter(scorer);
			highlighter.setTextFragmenter(fragmenter);
			return highlighter;
		}

		public static String highlight(String s) { return preTag + s + postTag; }
	}

	public static Sort CaseInsensitiveSort(String fieldName) {
		// For Lucene 10 we use STRING sort. This is case-sensitive by default. Implement custom FieldComparatorSource
		// if you need exact case-insensitive semantics.
		SortField sf = new SortField(fieldName, SortField.Type.STRING);
		return new Sort(sf);
	}
}

/*
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

//import net.shrine.config.ConfigSource;
//import net.shrine.ontology.LabDetail;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.classic.QueryParserBase;
import org.apache.lucene.search.FieldDoc;
//import org.apache.lucene.search.Highlighter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopFieldDocs;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

import edu.harvard.i2b2.ontology.dao.lucene.OntologyService.CodeCategoryTerm;
import edu.harvard.i2b2.ontology.dao.lucene.OntologyService.ConceptType;
import edu.harvard.i2b2.ontology.dao.lucene.OntologyService.FilterData;
import edu.harvard.i2b2.ontology.dao.lucene.OntologyService.FilterOption;
import edu.harvard.i2b2.ontology.dao.lucene.OntologyService.FilterableType;
import edu.harvard.i2b2.ontology.dao.lucene.OntologyService.OntologyPath;
import edu.harvard.i2b2.ontology.dao.lucene.OntologyService.OntologyTerm;
import edu.harvard.i2b2.ontology.dao.lucene.OntologyService.SearchQuery;
import edu.harvard.i2b2.ontology.dao.lucene.OntologyService.SearchResults;
import edu.harvard.i2b2.ontology.dao.lucene.OntologyService.SearchResultsMetadata;
import edu.harvard.i2b2.ontology.dao.lucene.OntologyService.VisualAttributes;

//import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Java translation of the Scala LuceneSearcher. This class attempts to keep the
 * same behavior but exposes synchronous methods (the original Scala version
 * wrapped many methods in cats.effect.IO). I made the decision to return
 * plain Java collections and Optional values to make the class usable from
 * Java.
 *
 * Assumptions made:
 * - Several domain types (OntologyTerm, CodeCategoryTerm, FilterOption,
 *   FilterData, FilterableType, NO_FILTER, CODE_CATEGORY, CODE_SET,
 *   OntologyPath, SearchQuery, SearchResults, SearchResultsMetadata,
 *   VisualAttributes) exist elsewhere in the project and are referenced
 *   here.

public final class LuceneSearcher {

    private static final String indexFileLocation = ""; //ConfigSource.config.getString("shrine.lucene.directory");
    private static final File indexDir = new File(indexFileLocation);
    private static final FSDirectory directory;
    public static final IndexSearcher searcher;

    static {
        try {
            directory = FSDirectory.open(indexDir.toPath());
            searcher = new IndexSearcher(DirectoryReader.open(directory));
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private LuceneSearcher() {}

    private static CodeCategoryTerm createCodeCategoryTerm(String displayName, List<OntologyTerm> children) {
        return new CodeCategoryTerm(displayName, null, false, children);
    }

    public static List<CodeCategoryTerm> getRootTerms() throws IOException {
        TopFieldDocs foundDocs = getRootFieldDocs();

        Map<Object, List<ScoreDoc>> docsToCodeCategories = Arrays.stream(foundDocs.scoreDocs)
                .collect(Collectors.groupingBy(sd -> storedFields.document(sd.doc).get("codeCategory")));

        TreeMap<String, List<ScoreDoc>> sorted = new TreeMap<>();
        sorted.putAll(docsToCodeCategories.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (u, v) -> u, LinkedHashMap::new)));

        List<CodeCategoryTerm> result = new ArrayList<>();
        for (Map.Entry<String, List<ScoreDoc>> e : sorted.entrySet()) {
            List<OntologyTerm> ontologyTermsList = e.getValue().stream()
                    .map(LuceneSearcher::extractOntologyTermFromScoreDoc)
                    .collect(Collectors.toList());
            result.add(createCodeCategoryTerm(e.getKey(), ontologyTermsList));
        }

        return result;
    }

    /**
 * Retrieves a single term by its ontology path and display name ignoring any children elements.

    public static Optional<OntologyTerm> getSingleTermByPathAndDisplayName(String path, String displayName) throws IOException {
        BooleanQuery pathAndNameQuery = new BooleanQuery.Builder()
                .add(new TermQuery(new Term("path", path)), BooleanClause.Occur.MUST)
                .build();

        List<ScoreDoc> sds = getRawSearchResults(pathAndNameQuery);
        if (sds.size() > 1) {
            // warn: not emitting logger here; assume external logging if needed
            Optional<ScoreDoc> displayNameOrIsRootMatch = sds.stream().filter(scoreDoc -> {
                try {
                    String dn = storedFields.document(scoreDoc.doc).get("displayName");
                    String isRoot = storedFields.document(scoreDoc.doc).get("isRoot");
                    return (dn != null && dn.equals(displayName)) || (isRoot != null && Boolean.parseBoolean(isRoot));
                } catch (IOException ex) {
                    return false;
                }
            }).findFirst();

            if (displayNameOrIsRootMatch.isPresent()) {
                return Optional.of(extractOntologyTermFromScoreDoc(displayNameOrIsRootMatch.get()));
            } else {
                return sds.stream().findFirst().map(LuceneSearcher::extractOntologyTermFromScoreDoc);
            }
        } else {
            return sds.stream().findFirst().map(LuceneSearcher::extractOntologyTermFromScoreDoc);
        }
    }

    public static List<FilterOption> getFilterOptions() throws IOException {
        FilterOption defaultOption = new FilterOption(new NO_FILTER(), "All Concepts", "All Concepts");

        TopFieldDocs foundDocs = getRootFieldDocs();

        Set<String> allCodeCategories = Arrays.stream(foundDocs.scoreDocs)
                .map(sd -> {
                    try { return storedFields.document(sd.doc).get("codeCategory"); } catch (IOException e) { return null; }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        List<FilterOption> codeCategoryOptions = allCodeCategories.stream().map(codeCategory -> {
            String displayableName;
            switch (codeCategory) {
                case "Diagnoses": displayableName = "Diagnoses (All)"; break;
                case "Laboratory Tests": displayableName = "Laboratory Tests (LOINC)"; break;
                case "Medications": displayableName = "Medications (RxNORM)"; break;
                case "Procedures": displayableName = "Procedures (All)"; break;
                default: displayableName = codeCategory; break;
            }
            return new FilterOption(new CODE_CATEGORY(), codeCategory, displayableName);
        }).collect(Collectors.toList());

        Map<String, List<FilterOption>> codeSetMap = Arrays.stream(foundDocs.scoreDocs)
                .map(sd -> {
                    try {
                        String cc = storedFields.document(sd.doc).get("codeCategory");
                        String cs = storedFields.document(sd.doc).get("codeSet");
                        return new AbstractMap.SimpleEntry<>(cc, cs);
                    } catch (IOException e) { return null; }
                })
                .filter(Objects::nonNull)
                .filter(e -> e.getValue() != null && !e.getValue().isEmpty())
                .collect(Collectors.groupingBy(Map.Entry::getKey,
                        LinkedHashMap::new,
                        Collectors.mapping(e -> new FilterOption(new CODE_SET(), e.getValue(), e.getValue()), Collectors.toList())));

        List<FilterOption> codeSetAndCategoryOptions = new ArrayList<>();
        for (FilterOption co : codeCategoryOptions) {
            codeSetAndCategoryOptions.add(co);
            if (codeSetMap.containsKey(co.filterValue())) {
                codeSetAndCategoryOptions.addAll(codeSetMap.get(co.filterValue()));
            }
        }

        List<FilterOption> allOptions = new ArrayList<>();
        allOptions.add(defaultOption);
        allOptions.addAll(codeSetAndCategoryOptions);
        return allOptions;
    }

    public static Map<OntologyPath, CodeCategory> getCodeCategoriesMap() throws IOException {
        TopFieldDocs foundDocs = getRootFieldDocs();

        List<Map.Entry<OntologyPath, CodeCategory>> pathToCodeCategory = Arrays.stream(foundDocs.scoreDocs)
                .map(sd -> {
                    try {
                        String codeCategory = storedFields.document(sd.doc).get("codeCategory");
                        String path = storedFields.document(sd.doc).get("path");
                        return new AbstractMap.SimpleEntry<>(new OntologyPath(path), new CodeCategory(codeCategory));
                    } catch (IOException e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        Map<OntologyPath, CodeCategory> map = new LinkedHashMap<>();
        for (Map.Entry<OntologyPath, CodeCategory> e : pathToCodeCategory) map.put(e.getKey(), e.getValue());
        return map;
    }

    public static List<ScoreDoc> getRawSearchResults(Query query) throws IOException {
        int maxHits = Integer.MAX_VALUE;
        TopFieldDocs foundDocs = searcher.search(query, maxHits, CaseInsensitiveSort.apply("displayName"));
        return Arrays.asList(foundDocs.scoreDocs);
    }

    public static List<OntologyTerm> getChildren(OntologyPath ontologyPath) throws IOException {
        String parentPath = ontologyPath.path.dropRight(1);
        Query childrenQuery = new TermQuery(new Term("parentPath", parentPath));
        List<ScoreDoc> scoreDocs = getRawSearchResults(childrenQuery);
        List<OntologyTerm> results = scoreDocs.stream().map(LuceneSearcher::extractOntologyTermFromScoreDoc).collect(Collectors.toList());
        return results;
    }

    public static Optional<ConceptInfo> getConceptInfo(OntologyPath ontologyPath) throws IOException {
        Query pathQuery = new TermQuery(new Term("path", ontologyPath.path));
        Query childrenQuery = new TermQuery(new Term("parentPath", ontologyPath.path.dropRight(1)));

        BooleanQuery booleanQuery = new BooleanQuery.Builder()
                .add(pathQuery, BooleanClause.Occur.SHOULD)
                .add(childrenQuery, BooleanClause.Occur.SHOULD)
                .build();

        List<ScoreDoc> scoreDocs = getRawSearchResults(booleanQuery);

        List<ConceptInfo> childConcepts = scoreDocs.stream()
                .filter(sd -> {
                    try { return !Objects.equals(storedFields.document(sd.doc).get("path"), ontologyPath.path); } catch (IOException e) { return false; }
                })
                .map(ConceptInfo::new)
                .collect(Collectors.toList());

        Optional<ScoreDoc> scoreDocOption = scoreDocs.stream()
                .filter(p -> {
                    try { return Objects.equals(storedFields.document(p.doc).get("path"), ontologyPath.path); } catch (IOException e) { return false; }
                }).findFirst();

        if (!scoreDocOption.isPresent()) return Optional.empty();

        ScoreDoc scoreDoc = scoreDocOption.get();

        String nPath = storedFields.document(scoreDoc.doc).get("nPath");
        String visAttrPath = "\\CA" + storedFields.document(scoreDoc.doc).get("visPath");

        String[] pathElements = nPath.split("\\\\");
        List<String> pathElementsList = Arrays.stream(pathElements).filter(s -> !s.isEmpty()).collect(Collectors.toList());
        Collections.reverse(pathElementsList);

        String[] visPathElements = Arrays.stream(visAttrPath.split("\\\\")).filter(s -> !s.isEmpty()).toArray(String[]::new);
        List<String> visPathList = Arrays.stream(visPathElements).filter(s -> !s.isEmpty()).collect(Collectors.toList());
        Collections.reverse(visPathList);

        VisualAttributes firstVis = new VisualAttributes().fromString(visAttrPath).get(0);

        ConceptInfo selected = new ConceptInfo(nPath, pathElementsList.get(0), Optional.of(firstVis).map(va -> HtmlHighlighter.highlight(pathElementsList.get(0))).orElse(null), firstVis, true, Optional.of(childConcepts));

        // Reconstruct parent chain
        ConceptInfo current = selected;
        List<String> remainingPathElems = new ArrayList<>(pathElementsList.subList(1, pathElementsList.size()));
        List<String> remainingVis = new ArrayList<>(visPathList.subList(1, visPathList.size()));
        List<String> nPaths = Arrays.stream(nPath.split("\\\\")).filter(s -> !s.isEmpty()).collect(Collectors.toList());

        for (int i = 0; i < remainingPathElems.size(); i++) {
            String pe = remainingPathElems.get(i);
            String vis = remainingVis.size() > i ? remainingVis.get(i) : "";
            String path = "\\" + String.join("\\", nPaths.subList(nPaths.size() - remainingPathElems.size() - 1, nPaths.size() - remainingPathElems.size() + i)) + "\\";
            List<ConceptInfo> childList = Collections.singletonList(current);
            VisualAttributes va = new VisualAttributes().fromString(vis);
            ConceptInfo ci = new ConceptInfo(path, pe, va, Optional.of(childList), false);
            current = ci;
        }

        return Optional.of(current);
    }

    public static OntologyTerm extractOntologyTermFromScoreDoc(ScoreDoc sd) {
        return extractOntologyTermFromScoreDoc(sd, null);
    }

    public static OntologyTerm extractOntologyTermFromScoreDoc(ScoreDoc sd, List<ScoreDoc> childrenScoreDocs) {
        try {
            String path = storedFields.document(sd.doc).get("path");
            String displayName = storedFields.document(sd.doc).get("displayName");
            IndexableField labField = storedFields.document(sd.doc).getField("labDetail");
            boolean isLab = labField != null;
            String visualAttributes = storedFields.document(sd.doc).get("visualAttributes");
            String conceptCategory = storedFields.document(sd.doc).get("conceptCategory");
            String metadata = storedFields.document(sd.doc).get("tooltip");

            return extractOntologyTerm(path, displayName, visualAttributes, conceptCategory, isLab, Optional.ofNullable(metadata), childrenScoreDocs, null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static OntologyTerm extractOntologyTerm(String path,
                                                   String displayName,
                                                   String visualAttr,
                                                   String conceptCategory,
                                                   boolean isLab,
                                                   Optional<String> metadata,
                                                   List<ScoreDoc> childrenScoreDocs,
                                                   String highlightedName) {

        VisualAttributes va = new VisualAttributes(visualAttr);

        List<OntologyTerm> children = childrenScoreDocs == null ? Collections.emptyList() : childrenScoreDocs.stream().map(LuceneSearcher::extractOntologyTermFromScoreDoc).collect(Collectors.toList());

        OntologyTerm ontTerm = new OntologyTerm(displayName, highlightedName, path, conceptCategory, va.getConceptType(), va.isActive(), metadata.orElse(null), children.isEmpty() ? null : children, isLab);
        return ontTerm;
    }

    public static SearchResults search(SearchQuery searchQuery) throws Exception {
        return searchIO(searchQuery);
    }

    public static SearchResults searchIO(SearchQuery searchQuery) throws Exception {
        class Helper {
            Query makeFilteredQuery(Query baseQuery, FilterData filter) {
                if (filter.filterType() instanceof FilterableType) {
                    Query filterQuery = new TermQuery(new Term(((FilterableType)filter.filterType()).value(), filter.filterValue()));
                    return new BooleanQuery.Builder()
                            .add(baseQuery, BooleanClause.Occur.MUST)
                            .add(filterQuery, BooleanClause.Occur.MUST)
                            .build();
                } else {
                    return baseQuery;
                }
            }
        }

        String sortTerm = "nPath";

        QueryParser displayNameQueryParser = new QueryParser("displayName", new StandardAnalyzer());
        displayNameQueryParser.setDefaultOperator(QueryParser.Operator.AND);

        String escapedSearchString = searchQuery.searchString().matches(".*\\s+$") ?
                QueryParserBase.escape(searchQuery.searchString().trim()) :
                QueryParserBase.escape(searchQuery.searchString()) + "*";

        Query displayNameQuery = displayNameQueryParser.parse(escapedSearchString);
        TermQuery basecodeQuery = new TermQuery(new Term("basecode", searchQuery.searchString()));

        BooleanQuery displayNameAndBasecodeQuery = new BooleanQuery.Builder()
                .add(displayNameQuery, BooleanClause.Occur.SHOULD)
                .add(basecodeQuery, BooleanClause.Occur.SHOULD)
                .setMinimumNumberShouldMatch(1)
                .build();

        Query filteredQuery = new Helper().makeFilteredQuery(displayNameAndBasecodeQuery, searchQuery.filterData());

        int maxResults = ConfigSource.config.getInt("shrine.lucene.maxSearchResults");
        Sort sort = CaseInsensitiveSort.apply(sortTerm);

        SearchResultsMetadata prev = searchQuery.previousSearchMetadata();
        TopFieldDocs foundDocs;
        if (prev == null) {
            foundDocs = searcher.search(filteredQuery, maxResults, sort);
        } else {
            Object field = new BytesRef(prev.sortFieldValue());
            Object[] fields = new Object[] { field };
            FieldDoc lastScoreDoc = new FieldDoc(prev.lastDocId(), Float.NaN, fields);
            foundDocs = searcher.searchAfter(lastScoreDoc, filteredQuery, maxResults, sort, false);
        }

        Highlighter highlighter = HtmlHighlighter.createHighlighter(filteredQuery);

        ScoreDoc[] scoreDocs = foundDocs.scoreDocs;
        long totalHits = foundDocs.totalHits.value;

        Map<String, List<ScoreDoc>> codeCategoriesToDocsWithNulls = Arrays.stream(scoreDocs)
                .collect(Collectors.groupingBy(sd -> {
                    try { return storedFields.document(sd.doc).get("codeCategory"); } catch (IOException e) { return null; }
                }));

        Map<String, List<ScoreDoc>> codeCategoriesToDocs = codeCategoriesToDocsWithNulls.entrySet().stream()
                .filter(e -> e.getKey() != null)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (u,v)->u, TreeMap::new));

        if (codeCategoriesToDocsWithNulls.containsKey(null)) {
            // log error in original; here we ignore or could throw
        }

        List<CodeCategoryTerm> codeCategoryTerms = new ArrayList<>();
        for (Map.Entry<String, List<ScoreDoc>> e : codeCategoriesToDocs.entrySet()) {
            Trie trie = new Trie(highlighter);
            e.getValue().forEach(trie::insert);

            List<OntologyTerm> ontTermsList = buildOntologyTerms(trie.root, "");
            codeCategoryTerms.add(createCodeCategoryTerm(e.getKey(), ontTermsList));
        }

        ScoreDoc lastScoreDoc = scoreDocs.length > 0 ? scoreDocs[scoreDocs.length - 1] : null;
        SearchResultsMetadata metadata = null;
        if (lastScoreDoc != null) {
            try {
                metadata = new SearchResultsMetadata(lastScoreDoc.doc, storedFields.document(lastScoreDoc.doc).get(sortTerm));
            } catch (IOException ignored) {}
        }

        return new SearchResults(totalHits, Optional.ofNullable(metadata), codeCategoryTerms);
    }

    public static List<OntologyTerm> buildOntologyTerms(TrieNode node, String path) {
        List<OntologyTerm> results = new ArrayList<>();
        for (Map.Entry<String, TrieNode> childEntry : node.children.entrySet()) {
            String childKey = childEntry.getKey();
            TrieNode child = childEntry.getValue();

            String currentPath = path + childKey + "\\";
            OntologyTerm ontTerm = child.ontTerm.orElse(null);
            List<OntologyTerm> childOntTerms = buildOntologyTerms(child, currentPath);
            if (ontTerm != null) ontTerm.updateChildren(childOntTerms.isEmpty() ? null : childOntTerms);
            if (ontTerm != null) results.add(ontTerm);
        }
        return results;
    }

    public static Optional<LabDetail> getLabDetails(OntologyPath ontologyPath) throws IOException {
        TermQuery nPathQuery = new TermQuery(new Term("path", ontologyPath.path));
        TopFieldDocs foundDocs = searcher.search(nPathQuery, 1);
        if (foundDocs.scoreDocs.length == 0) return Optional.empty();
        return extractLabDetail(foundDocs.scoreDocs[0]);
    }

    private static Optional<LabDetail> extractLabDetail(ScoreDoc scoreDoc) throws IOException {
        IndexableField labField = storedFields.document(scoreDoc.doc).getField("labDetail");
        if (labField == null) return Optional.empty();
        String labDetailJson = labField.stringValue();
        ObjectMapper mapper = new ObjectMapper();
        try {
            LabDetail ld = mapper.readValue(labDetailJson, LabDetail.class);
            return Optional.ofNullable(ld);
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    private static TopFieldDocs getRootFieldDocs() throws IOException {
        int maxHits = Integer.MAX_VALUE;
        QueryParser qp = new QueryParser("isRoot", new StandardAnalyzer());
        Query rootQuery = qp.parse("true");
        return searcher.search(rootQuery, maxHits, CaseInsensitiveSort.apply("displayName"));
    }

}

class TrieNode {
    public final String value;
    public final String visAttr;
    public Optional<OntologyTerm> ontTerm = Optional.empty();
    public final SortedMap<String, TrieNode> children;
    public boolean isLeaf = false;

    public TrieNode(String value) {
        this(value, "", Optional.empty());
    }

    public TrieNode(String value, String visAttr, Optional<OntologyTerm> ontTerm) {
        this.value = value;
        this.visAttr = visAttr;
        this.ontTerm = ontTerm;
        this.children = new TreeMap<>(CaseInsensitivePathOrder.INSTANCE);
    }
}

class Trie {
    public final TrieNode root = new TrieNode("");
    private final Highlighter highlighter;

    public Trie(Highlighter highlighter) { this.highlighter = highlighter; }

    public void insert(ScoreDoc scoreDoc) {
        try {
            String conceptCategory = LuceneSearcher.storedFields.document(scoreDoc.doc).get("conceptCategory");
            String path = LuceneSearcher.storedFields.document(scoreDoc.doc).get("path");
            String browsePath = LuceneSearcher.storedFields.document(scoreDoc.doc).get("nPath");
            String visAttrPath = LuceneSearcher.storedFields.document(scoreDoc.doc).get("visPath");
            String isLabPath = LuceneSearcher.storedFields.document(scoreDoc.doc).get("isLabPath");
            String metadata = LuceneSearcher.storedFields.document(scoreDoc.doc).get("tooltip");

            String text = LuceneSearcher.storedFields.document(scoreDoc.doc).get("displayName");
            String[] highlightedNameFragments = highlighter.getBestFragments(new StandardAnalyzer(), "displayName", text, 100);
            String highlightedNameOption = (highlightedNameFragments != null && highlightedNameFragments.length > 0) ? highlightedNameFragments[0] : null;

            List<String> browsePathElements = Arrays.stream(browsePath.split("\\\\")).filter(s -> !s.isEmpty()).collect(Collectors.toList());
            List<String> pathElems = Arrays.stream(path.split("\\\\")).filter(s -> !s.isEmpty()).collect(Collectors.toList());

            int minPathElemIndex = (pathElems.size() - (browsePathElements.size() - 1)) + 1;

            List<String> pathSectionsList;
            if (minPathElemIndex != 0) {
                String firstElement = String.join("\\", pathElems.subList(0, Math.min(minPathElemIndex, pathElems.size())));
                List<String> remaining = pathElems.subList(Math.min(minPathElemIndex, pathElems.size()), pathElems.size()).stream().collect(Collectors.toList());
                pathSectionsList = new ArrayList<>();
                pathSectionsList.add(firstElement);
                pathSectionsList.addAll(remaining);
            } else {
                pathSectionsList = pathElems;
            }

            String[] visPathElements = Arrays.stream(visAttrPath.split("\\\\")).filter(s -> !s.isEmpty()).toArray(String[]::new);
            String[] isLabPathElements = Arrays.stream(isLabPath.split("\\\\")).filter(s -> !s.isEmpty()).toArray(String[]::new);

            TrieNode curNode = root;
            StringBuilder currentFullPath = new StringBuilder("\\\\");
            String categoryPathElement = browsePathElements.get(0);
            StringBuilder currentFullBrowsePath = new StringBuilder("\\\\" + categoryPathElement + "\\");

            String vispath = "";
            boolean isLab = false;

            int index = 0;
            List<String> remainingBrowse = browsePathElements.subList(1, browsePathElements.size());
            for (String pathElement : pathSectionsList) {
                currentFullPath.append(pathElement).append("\\");
                currentFullBrowsePath.append(remainingBrowse.get(index)).append("\\");

                Map<String, TrieNode> children = curNode.children;

                vispath = visPathElements.length > index ? visPathElements[index] : "";
                isLab = isLabPathElements.length > index && Boolean.parseBoolean(isLabPathElements[index]);

                String key = currentFullBrowsePath.toString();
                if (children.containsKey(key)) {
                    curNode = children.get(key);
                } else {
                    OntologyTerm ontTerm = LuceneSearcher.extractOntologyTerm(path, remainingBrowse.get(index), vispath, conceptCategory, isLab, Optional.ofNullable(metadata), null, null);
                    TrieNode temp = new TrieNode(currentFullPath.toString().substring(0, Math.max(0, currentFullPath.length()-1)), vispath, Optional.ofNullable(ontTerm));
                    children.put(key, temp);
                    curNode = temp;
                }

                index++;
            }

            curNode.isLeaf = true;
            String lastBrowse = remainingBrowse.get(remainingBrowse.size()-1);
            curNode.ontTerm = Optional.of(LuceneSearcher.extractOntologyTerm(path, lastBrowse, vispath, conceptCategory, isLab, Optional.ofNullable(metadata), null, highlightedNameOption));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

class HtmlHighlighter {
    public static final String preTag = "<span class=\"highlight\">";
    public static final String postTag = "</span>";

    public static Highlighter createHighlighter(Query query) {
        org.apache.lucene.search.highlight.QueryScorer scorer = new org.apache.lucene.search.highlight.QueryScorer(query);
        org.apache.lucene.search.highlight.SimpleHTMLFormatter formater = new org.apache.lucene.search.highlight.SimpleHTMLFormatter(preTag, postTag);
        Highlighter highlighter = new Highlighter(formater, scorer);
        org.apache.lucene.search.highlight.SimpleSpanFragmenter fragmenter = new org.apache.lucene.search.highlight.SimpleSpanFragmenter(scorer);
        highlighter.setTextFragmenter(fragmenter);
        return highlighter;
    }

    public static String highlight(String name) {
        return preTag + name + postTag;
    }
}

class ConceptInfo {
    public final String nPath;
    public final String displayName;
    public final String highlightedName; // may be null
    public final ConceptType conceptType;
    public final boolean isActive;
    public List<ConceptInfo> children; // nullable

    public ConceptInfo(String nPath, String displayName, String highlightedName, ConceptType conceptType, boolean isActive, Optional<List<ConceptInfo>> children) {
        this.nPath = nPath;
        this.displayName = displayName;
        this.highlightedName = highlightedName;
        this.conceptType = conceptType;
        this.isActive = isActive;
        this.children = children.orElse(null);
    }

    // Scala-style constructors
    public ConceptInfo(ScoreDoc scoreDoc) {
        try {
            this.nPath = LuceneSearcher.storedFields.document(scoreDoc.doc).get("nPath");
            this.displayName = LuceneSearcher.storedFields.document(scoreDoc.doc).get("displayName");
            String visualAttr = LuceneSearcher.storedFields.document(scoreDoc.doc).get("visualAttributes");
            VisualAttributes va = new VisualAttributes().fromString(visualAttr);
            this.highlightedName = null;
            this.conceptType = va.getConceptType();
            this.isActive = va.isActive();
            this.children = null;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public ConceptInfo(String nPath, String displayName, VisualAttributes visualAttributes, Optional<List<ConceptInfo>> childrenConceptInfo, boolean highlight) {
        this.nPath = nPath;
        this.displayName = displayName;
        this.highlightedName = highlight ? HtmlHighlighter.highlight(displayName) : null;
        this.conceptType = visualAttributes.getConceptType();
        this.isActive = visualAttributes.isActive();
        this.children = childrenConceptInfo.orElse(null);
    }

    public ConceptInfo updateChildren(Optional<List<ConceptInfo>> newChildren) {
        this.children = newChildren.orElse(null);
        return this;
    }
}

class CodeCategory {
    public final String name;
    public CodeCategory(String name) { this.name = name; }
}

class CaseInsensitivePathOrder implements Comparator<String> {
    public static final CaseInsensitivePathOrder INSTANCE = new CaseInsensitivePathOrder();

    @Override
    public int compare(String str1, String str2) {
        String[] s1 = str1.split("\\\\");
        String[] s2 = str2.split("\\\\");

        int comp = 0;
        int len = Math.min(s1.length, s2.length);
        for (int i = 0; i < len && comp == 0; i++) {
            comp = s1[i].compareToIgnoreCase(s2[i]);
        }
        if (comp == 0 && s1.length > s2.length) return 1;
        if (comp == 0 && s1.length < s2.length) return -1;
        return comp;
    }
}

class CaseInsensitiveSort {
    public static Sort apply(String fieldName) {
        SortField sortField = new SortField(fieldName, new CaseInsensitiveComparatorSource());
        return new Sort(sortField);
    }

    static class CaseInsensitiveComparatorSource extends SortField.Type {
        // placeholder - we need a FieldComparatorSource equivalent; Lucene exposes FieldComparatorSource class in Java
    }
}
 */
/*
import java.io.File;
import cats.effect.IO;
import net.shrine.config.ConfigSource;
import net.shrine.http4s.catsio.ExecutionContexts;
import net.shrine.log.Loggable;
import net.shrine.ontology.LabDetail;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.classic.QueryParserBase;
import org.apache.lucene.search.highlight.Highlighter;
import org.http4s.EntityDecoder;

import org.apache.logging.log4j.Logger;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.FieldDoc;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopFieldDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

import java.util.SortedMap;
import java.util.stream.Collectors;

import org.apache.lucene.store.FSDirectory;

import java.util.Map;
import java.util.Optional;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;





public class LuceneSearcher { //implements Logger {


    public static void main(String[] args) throws IOException {

    	Directory oldDir = FSDirectory.open(Paths.get("path/to/lucene9/index"));
    	DirectoryReader oldReader = DirectoryReader.open(oldDir);

    	// Lucene 10 (new index)
    	Directory newDir = FSDirectory.open(Paths.get("path/to/lucene10/index"));
    	IndexWriterConfig newConfig = new IndexWriterConfig(); // Configure as needed for Lucene 10
    	IndexWriter newWriter = new IndexWriter(newDir, newConfig);

    	// Iterate and reindex
    	for (int i = 0; i < oldReader.maxDoc(); i++) {
    	    if (!oldReader.isDeleted(i)) {
    	        Document oldDoc = oldReader.document(i);
    	        Document newDoc = new Document();
    	        // Copy fields from oldDoc to newDoc, potentially adapting to Lucene 10 changes
    	        for (IndexableField field : oldDoc.getFields()) {
    	            newDoc.add(field); // This might require adaptation depending on field types and Lucene 10 changes
    	        }
    	        newWriter.addDocument(newDoc);
    	    }
    	}

    	// Close resources
    	newWriter.close();
    	oldReader.close();
    	oldDir.close();
    	newDir.close();


        // 1. Create or open the FSDirectory
        Path indexPath = Paths.get("/Users/mem61/Downloads/lucene_index");
        Directory directory = FSDirectory.open(indexPath);

        // 2. Create an IndexWriter to add documents
        StandardAnalyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        IndexWriter writer = new IndexWriter(directory, config);


        // 3. Add documents to the index
        addDocument(writer, "Lucene is a powerful search library.", "title1", "content1");
      //  addDocument(writer, "Apache Lucene provides full-text search capabilities.", "title2", "content2");
      //  addDocument(writer, "FSDirectory stores the index on the file system.", "title3", "content3");

        writer.close(); // Close the writer to flush changes

        // 4. Search the index
        DirectoryReader reader = DirectoryReader.open(directory);
        IndexSearcher searcher = new IndexSearcher(reader);

        try {
            QueryParser parser = new QueryParser("displayName", analyzer); // Search the 'content' field
            Query query = parser.parse("Asthma"); // Example query

            TopDocs topDocs = searcher.search(query, 10); // Get top 10 results

            System.out.println("Search results for: 'lucene search'");
            for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
              //  Document doc = storedFields.document(scoreDoc.doc).get("displayName");

                Document doc = searcher.storedFields().document(scoreDoc.doc);
                System.out.println("Title: " + doc.get("title") + ", Content: " + doc.get("content") + ", Score: " + scoreDoc.score);
            }
        } catch (Exception e) {
            System.err.println("Error during search: " + e.getMessage());
        } finally {
            reader.close(); // Close the reader
            directory.close(); // Close the directory
        }



    }

    private static void addDocument(IndexWriter writer, String content, String title, String id) throws IOException {
        Document doc = new Document();
        doc.add(new TextField("id", id, Field.Store.YES));
        doc.add(new TextField("title", title, Field.Store.YES));
        doc.add(new TextField("content", content, Field.Store.YES));
        writer.addDocument(doc);
    }
 */
/*
	private static final String indexFileLocation = ConfigSource.config.getString("shrine.lucene.directory");
	private static final File indexDir = new File(indexFileLocation);
	private static final FSDirectory directory;

	static {
		FSDirectory tempDir = null;
		try {
			tempDir = FSDirectory.open(indexDir.toPath());
		} catch (Exception e) {
			// handle exception or rethrow as unchecked
			e.printStackTrace();
		}
		directory = tempDir;
	}

	public static final IndexSearcher searcher;

	static {
		IndexSearcher tempSearcher = null;
		try {
			tempSearcher = new IndexSearcher(DirectoryReader.open(directory));
		} catch (Exception e) {
			e.printStackTrace();
		}
		searcher = tempSearcher;
	}

	private static CodeCategoryTerm createCodeCategoryTerm(String displayName, List<OntologyTerm> children) {
		return new CodeCategoryTerm(displayName, children);
	}

	public static IO<List<CodeCategoryTerm>> getRootTerms() {
		IO<TopFieldDocs> foundDocsIO = getRootFieldDocs();
		return foundDocsIO.map(foundDocs -> {
			Map<String, List<ScoreDoc>> docsToCodeCategories = Arrays.stream(foundDocs.scoreDocs)
					.collect(Collectors.groupingBy(sd -> {
						try {
							return storedFields.document(sd.doc).get("codeCategory");
						} catch (Exception e) {
							throw new RuntimeException(e);
						}
					}));

			// SortedMap by key (codeCategory)
			SortedMap<String, List<ScoreDoc>> sortedDocsToCategories = new TreeMap<>(docsToCodeCategories);

			List<CodeCategoryTerm> codeCategoryTerms = new ArrayList<>();
			for (Map.Entry<String, List<ScoreDoc>> entry : sortedDocsToCategories.entrySet()) {
				String codeCategory = entry.getKey();
				List<ScoreDoc> scoreDocs = entry.getValue();

				List<OntologyTerm> ontologyTermsList = scoreDocs.stream()
						.map(LuceneSearcher::extractOntologyTermFromScoreDoc)
						.collect(Collectors.toList());

				codeCategoryTerms.add(createCodeCategoryTerm(codeCategory, ontologyTermsList));
			}

			return codeCategoryTerms;
		});
	}


 * Retrieves a single term by its ontology path and display name ignoring any children elements.
	public static IO<Optional<OntologyTerm>> getSingleTermByPathAndDisplayName(String path, String displayName) {
		BooleanQuery.Builder builder = new BooleanQuery.Builder();
		builder.add(new TermQuery(new org.apache.lucene.index.Term("path", path)), BooleanClause.Occur.MUST);
		BooleanQuery pathAndNameQuery = builder.build();

		IO<List<ScoreDoc>> scoreDocsIO = getRawSearchResults(pathAndNameQuery);
		return scoreDocsIO.map(sds -> {
			if (sds.size() > 1) {
				warn(String.format("displayName and path should be unique to an ontology term. For path (%s) and displayName (%s) found %d results",
						path, displayName, sds.size()));

				Optional<ScoreDoc> displayNameOrIsRootMatch = sds.stream()
						.filter(scoreDoc -> {
							try {
								String docDisplayName = storedFields.document(scoreDoc.doc).get("displayName");
								String isRootStr = storedFields.document(scoreDoc.doc).get("isRoot");
								boolean isRoot = isRootStr != null && Boolean.parseBoolean(isRootStr);
								return displayName.equals(docDisplayName) || isRoot;
							} catch (Exception e) {
								throw new RuntimeException(e);
							}
						})
						.findFirst();

				if (displayNameOrIsRootMatch.isPresent()) {
					return Optional.of(extractOntologyTermFromScoreDoc(displayNameOrIsRootMatch.get()));
				} else {
					return sds.stream().findFirst().map(LuceneSearcher::extractOntologyTermFromScoreDoc);
				}
			} else {
				return sds.stream().findFirst().map(LuceneSearcher::extractOntologyTermFromScoreDoc);
			}
		});
	}

	// Placeholder for methods and classes used in the Scala code that are not defined here:
	private static IO<TopFieldDocs> getRootFieldDocs() {
		// Implementation needed
		return null;
	}

	private static IO<List<ScoreDoc>> getRawSearchResults(Query query) {
		// Implementation needed
		return null;
	}

	private static OntologyTerm extractOntologyTermFromScoreDoc(ScoreDoc sd) {
		// Implementation needed
		return null;
	}

	private static void warn(String message) {
		// Implementation needed, e.g., logging
		System.err.println("WARN: " + message);
	}

	// Dummy classes to represent the Scala classes used
	public static class CodeCategoryTerm {
		private final String displayName;
		private final List<OntologyTerm> children;

		public CodeCategoryTerm(String displayName, List<OntologyTerm> children) {
			this.displayName = displayName;
			this.children = children;
		}

		// getters, setters, etc.
	}

	public static class OntologyTerm {
		// Implementation needed
	}

	// IO class placeholder (you may replace with your own effect type or library)
	public static class IO<T> {
		private final java.util.function.Supplier<T> supplier;

		public IO(java.util.function.Supplier<T> supplier) {
			this.supplier = supplier;
		}

		public <R> IO<R> map(java.util.function.Function<T, R> mapper) {
			return new IO<>(() -> mapper.apply(supplier.get()));
		}

		public T unsafeRun() {
			return supplier.get();
		}
	}

	// Loggable interface placeholder
	public interface Loggable {
		default void warn(String message) {
			System.err.println("WARN: " + message);
		}
	}
}



 * Retrieves a single term by its ontology path and display name ignoring any children elements.
public CompletableFuture<Optional<OntologyTerm>> getSingleTermByPathAndDisplayName(String path, String displayName) {
	BooleanQuery pathAndNameQuery = new BooleanQuery.Builder()
			.add(new TermQuery(new Term("path", path)), BooleanClause.Occur.MUST)
			.build();

	CompletableFuture<List<ScoreDoc>> scoreDocsFuture = getRawSearchResults(pathAndNameQuery);
	return scoreDocsFuture.thenApply(sds -> {
		if (sds.size() > 1) {
			warn(String.format("displayName and path should be unique to an ontology term. For path (%s) and displayName (%s) found %d results", path, displayName, sds.size()));
			Optional<ScoreDoc> displayNameOrisRootMatch = sds.stream()
					.filter(scoreDoc -> {
						try {
							String docDisplayName = storedFields.document(scoreDoc.doc).get("displayName");
							String isRootStr = storedFields.document(scoreDoc.doc).get("isRoot");
							boolean isRoot = isRootStr != null && Boolean.parseBoolean(isRootStr);
							return displayName.equals(docDisplayName) || isRoot;
						} catch (Exception e) {
							return false;
						}
					})
					.findFirst();

			if (displayNameOrisRootMatch.isPresent()) {
				return extractOntologyTermFromScoreDoc(displayNameOrisRootMatch.get());
			} else {
				return sds.stream().findFirst().map(this::extractOntologyTermFromScoreDoc).orElse(null);
			}
		} else {
			return sds.stream().findFirst().map(this::extractOntologyTermFromScoreDoc).orElse(null);
		}
	});
}

public CompletableFuture<List<FilterOption>> getFilterOptions() {
	FilterOption defaultOption = new FilterOption(NO_FILTER(), "All Concepts", "All Concepts");

	CompletableFuture<TopFieldDocs> foundDocsFuture = getRootFieldDocs();
	return foundDocsFuture.thenApply(foundDocs -> {
		try {
			List<ScoreDoc> scoreDocs = Arrays.asList(foundDocs.scoreDocs);

			// Extract distinct code categories
			List<String> allCodeCategories = scoreDocs.stream()
					.map(sd -> {
						try {
							return storedFields.document(sd.doc).get("codeCategory");
						} catch (Exception e) {
							return null;
						}
					})
					.filter(Objects::nonNull)
					.distinct()
					.collect(Collectors.toList());

			// Map code categories to FilterOptions with their displayable name
			List<FilterOption> codeCategoryOptions = allCodeCategories.stream()
					.map(codeCategory -> {
						String displayableName;
						switch (codeCategory) {
						case "Diagnoses":
							displayableName = "Diagnoses (All)";
							break;
						case "Laboratory Tests":
							displayableName = "Laboratory Tests (LOINC)";
							break;
						case "Medications":
							displayableName = "Medications (RxNORM)";
							break;
						case "Procedures":
							displayableName = "Procedures (All)";
							break;
						default:
							displayableName = codeCategory;
							break;
						}
						return new FilterOption(CODE_CATEGORY(), codeCategory, displayableName);
					})
					.collect(Collectors.toList());

			// Map from code category to a list of corresponding code set FilterOptions
			Map<String, List<FilterOption>> codeSetMap = scoreDocs.stream()
					.map(sd -> {
						try {
							String codeCategory = storedFields.document(sd.doc).get("codeCategory");
							String codeSet = storedFields.document(sd.doc).get("codeSet");
							return new AbstractMap.SimpleEntry<>(codeCategory, codeSet);
						} catch (Exception e) {
							return null;
						}
					})
					.filter(Objects::nonNull)
					.filter(entry -> entry.getValue() != null && !entry.getValue().isEmpty())
					.collect(Collectors.groupingBy(
							Map.Entry::getKey,
							Collectors.mapping(
									entry -> new FilterOption(CODE_SET(), entry.getValue(), entry.getValue()),
									Collectors.toList()
									)
							));

			// Combine code set and category lists so that code set options are directly after their code category
			List<FilterOption> codeSetAndCategoryOptions = new ArrayList<>();
			for (FilterOption categoryOption : codeCategoryOptions) {
				codeSetAndCategoryOptions.add(categoryOption);
				if (codeSetMap.containsKey(categoryOption.getFilterValue())) {
					codeSetAndCategoryOptions.addAll(codeSetMap.get(categoryOption.getFilterValue()));
				}
			}

			List<FilterOption> allOptions = new ArrayList<>();
			allOptions.add(defaultOption);
			allOptions.addAll(codeSetAndCategoryOptions);

			return allOptions;
		} catch (Exception e) {
			// Handle exceptions if needed
			return Collections.singletonList(defaultOption);
		}
	});
}

// Placeholder methods and fields to make the above code compile
private void warn(String message) {
	// Implement logging or warning mechanism
}

private CompletableFuture<List<ScoreDoc>> getRawSearchResults(Query query) {
	// Implement actual search logic returning CompletableFuture
	return null;
}

private CompletableFuture<TopFieldDocs> getRootFieldDocs() {
	// Implement actual retrieval logic returning CompletableFuture
	return null;
}

private OntologyTerm extractOntologyTermFromScoreDoc(ScoreDoc scoreDoc) {
	// Implement extraction logic
	return null;
}

private Object NO_FILTER() {
	// Implement or return appropriate filter type
	return null;
}

private Object CODE_CATEGORY() {
	// Implement or return appropriate filter type
	return null;
}

private Object CODE_SET() {
	// Implement or return appropriate filter type
	return null;
}

// Assume searcher is defined elsewhere in the class
private IndexSearcher searcher;

// Placeholder classes for OntologyTerm and FilterOption
public static class OntologyTerm {
	// Implementation here
}

public static class FilterOption {
	private Object filterType;
	private String filterValue;
	private String displayName;

	public FilterOption(Object filterType, String filterValue, String displayName) {
		this.filterType = filterType;
		this.filterValue = filterValue;
		this.displayName = displayName;
	}

	public Object getFilterType() {
		return filterType;
	}

	public String getFilterValue() {
		return filterValue;
	}

	public String getDisplayName() {
		return displayName;
	}
}

}
 */