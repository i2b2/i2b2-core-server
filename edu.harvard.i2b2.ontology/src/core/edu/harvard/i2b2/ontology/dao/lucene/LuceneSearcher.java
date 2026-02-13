package edu.harvard.i2b2.ontology.dao.lucene;

import org.apache.axiom.om.OMElement;

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

import edu.harvard.i2b2.ontology.dao.lucene.LuceneSuggester.AutoSuggestResult;
import edu.harvard.i2b2.ontology.ws.OntologyService;
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
import edu.harvard.i2b2.ontology.dao.lucene.LuceneService.SuggestQuery;
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
public final class LuceneSearcher { // implements Loggable {

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
	
	
	/*
    public static void main(String[] args) throws Exception {
    	
    	
    	OntologyService o = new OntologyService();
    	//OMElement tt = o.findDocuments("diag");
    	
    	SuggestQuery a = new SuggestQuery();
    	a.suggestString = "diag";
    	List<AutoSuggestResult> mylu = LuceneSuggester.getSuggestions(a);
    	List<CodeCategoryTerm> myroot = getRootTerms();
    	 SearchQuery mysearch = new SearchQuery();
    	 mysearch.searchString = "diag";
    	 mysearch.filterData = new FilterData(new NO_FILTER(), "All Concepts");
		 SearchResults mylist = searchIO(mysearch);
//    	Optional<OntologyTerm> mylist = getSingleTermByPathAndDisplayName("\\i2b2\\Diagnoses\\|Diagnoses", "Asthma");
    	
        long startTime = System.currentTimeMillis();


        
    }
    */

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
			List<OntologyTerm> ontologyTerms = sds.stream().map(LuceneSearcher::extractOntologyTermFromScoreDoc).collect(Collectors.toList());
			result.add(new CodeCategoryTerm(codeCategory, null, false, ontologyTerms));
		}

		return result;
	}

	private static Map<String, String> getDocValue(ScoreDoc sd) {
		try {

			StoredFields storedFields = LuceneSearcher.searcher.storedFields();
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
		StoredFields storedFields = LuceneSearcher.searcher.storedFields();
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
		return sds.stream().map(LuceneSearcher::extractOntologyTermFromScoreDoc).collect(Collectors.toList());
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
		StoredFields storedFields = LuceneSearcher.searcher.storedFields();
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
			StoredFields storedFields = LuceneSearcher.searcher.storedFields();

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

		StoredFields storedFields = LuceneSearcher.searcher.storedFields();

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
			StoredFields storedFields = LuceneSearcher.searcher.storedFields();
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
				StoredFields storedFields = LuceneSearcher.searcher.storedFields();
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
