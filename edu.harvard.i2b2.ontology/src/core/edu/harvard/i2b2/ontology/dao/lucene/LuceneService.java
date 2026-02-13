package edu.harvard.i2b2.ontology.dao.lucene;


import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.harvard.i2b2.ontology.datavo.pm.ProjectType;
import edu.harvard.i2b2.ontology.datavo.vdo.ConceptType;
import edu.harvard.i2b2.ontology.datavo.vdo.ConceptsType;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.apache.lucene.queryparser.classic.ParseException;

/**
 * Java translation of OntologyService.scala.
 *
 * Note: instead of binding to http4s, this class exposes methods that return a lightweight
 * ServiceResponse (status + JSON body). Integrate with your HTTP framework by delegating
 * to these methods.
 */
public final class LuceneService {

   private final ObjectMapper mapper = new ObjectMapper();

    public LuceneService() {}

    public ServiceResponse ping() {
        return ServiceResponse.ok("pong");
    }

    public ServiceResponse getRoot() throws IOException, ParseException {
        List<CodeCategoryTerm> terms = LuceneSearcher.getRootTerms();
        if (terms == null || terms.isEmpty()) return ServiceResponse.noContent();
        return toJsonResponse(terms);
    }

    public ServiceResponse getFilterOptions() throws IOException, ParseException {
        List<FilterOption> opts = LuceneSearcher.getFilterOptions();
        if (opts == null) opts = Collections.emptyList();
        return toJsonResponse(opts);
    }

    public ServiceResponse getChildren(OntologyPath path) throws IOException {
        List<OntologyTerm> children = LuceneSearcher.getChildren(path);
        if (children == null || children.isEmpty()) return ServiceResponse.noContent();
        return toJsonResponse(children);
    }

    public ServiceResponse getConceptInfo(OntologyPath path) throws IOException {
        Optional<ConceptInfo> info = LuceneSearcher.getConceptInfo(path);
        if (info == null || !info.isPresent()) return ServiceResponse.notFound();
        return toJsonResponse(info.get());
    }

    public ServiceResponse search(SearchQuery q) throws Exception {
        SearchResults results = LuceneSearcher.searchIO(q);
        return toJsonResponse(results);
    }

    public LuceneSuggester getSuggester(String projectInfo) {
    	
    	return new LuceneSuggester(projectInfo);
    	
    }
    public ConceptsType suggest(SuggestQuery q, String projectInfo) {
        return  LuceneSuggester.getSuggestions(q, projectInfo);
        //return suggestions;
       // return toJsonResponse(suggestions);
    }

    public ServiceResponse getLabDetails(OntologyPath path) throws IOException {
        Optional<LabDetail> ld = LuceneSearcher.getLabDetailsIO(path);
        if (ld == null || !ld.isPresent()) return ServiceResponse.noContent();
        return toJsonResponse(ld.get());
    }

    private ServiceResponse toJsonResponse(Object o) {
        try {
            String body = mapper.writeValueAsString(o);
            return ServiceResponse.ok(body);
        } catch (Exception e) {
            return ServiceResponse.serverError(e.getMessage());
        }
    }

    // Lightweight HTTP-like response container
    public static final class ServiceResponse {
        public final int status;
        public final String body;

        private ServiceResponse(int status, String body) {
            this.status = status;
            this.body = body;
        }

        public static ServiceResponse ok(String body) { return new ServiceResponse(200, body); }
        public static ServiceResponse noContent() { return new ServiceResponse(204, ""); }
        public static ServiceResponse notFound() { return new ServiceResponse(404, ""); }
        public static ServiceResponse serverError(String message) { return new ServiceResponse(500, message == null ? "" : message); }
    }

    // --- POJOs translated from Scala case classes ---

    public static final class CodeCategoryTerm {
        public String displayName;
        public ConceptType conceptType;
        public boolean isActive;
        public List<OntologyTerm> children;

        public CodeCategoryTerm() {}
        public CodeCategoryTerm(String displayName, ConceptType conceptType, boolean isActive, List<OntologyTerm> children) {
            this.displayName = displayName; this.conceptType = conceptType; this.isActive = isActive; this.children = children;
        }
    }

    public static final class OntologyTerm {
        public String displayName;
        public Optional<String> highlightedName;
        public String path;
        public String conceptCategory;
        public ConceptType conceptType;
        public boolean isActive;
        public boolean isLab;
        public Optional<String> metadata;
        public Optional<List<OntologyTerm>> children;

        public OntologyTerm() {}

        public OntologyTerm updateChildren(Optional<List<OntologyTerm>> newChildren) {
            return new OntologyTerm(this.displayName, newChildren);
        }

        private OntologyTerm(String displayName, Optional<List<OntologyTerm>> children) {
            this.displayName = displayName;
            this.children = children;
        }
    }

    public static final class OntologyPath {
        public String path;
        public OntologyPath() {}
        public OntologyPath(String path) { this.path = path; }
    }

    public static final class FilterOption {
        public FilterType filterType;
        public String filterValue;
        public String displayableName;
        public FilterOption() {}
        public FilterOption(FilterType t, String v, String d) { this.filterType = t; this.filterValue = v; this.displayableName = d; }
    }

    public interface FilterType {}

    public static final class NO_FILTER implements FilterType {}

    public static abstract class FilterableType implements FilterType {
        public final String value;
        protected FilterableType(String value) { this.value = value; }
    }

    public static final class CODE_SET extends FilterableType { public CODE_SET() { super("codeSet"); } }
    public static final class CODE_CATEGORY extends FilterableType { public CODE_CATEGORY() { super("codeCategory"); } }

    public static final class FilterData {
        public FilterType filterType;
        public String filterValue;
        public FilterData() {}
        public FilterData(FilterType t, String v) { this.filterType = t; this.filterValue = v; }
    }

    public static final class SearchQuery {
        public String searchString;
        public FilterData filterData;
        public Optional<SearchResultsMetadata> previousSearchMetadata;
        public SearchQuery() {}
        public SearchQuery(String s, FilterData f, Optional<SearchResultsMetadata> m) { this.searchString = s; this.filterData = f; this.previousSearchMetadata = m; }
    }

    public static final class SearchResultsMetadata { public int lastDocId; public String sortFieldValue; public SearchResultsMetadata() {} public SearchResultsMetadata(int a, String b) { this.lastDocId = a; this.sortFieldValue = b; } }
    public static final class SearchResults { public long totalHits; public Optional<SearchResultsMetadata> searchResultsMetadata; public List<CodeCategoryTerm> results; public SearchResults() {}

	public SearchResults(long totalHits2, Optional<SearchResultsMetadata> ofNullable,
			List<CodeCategoryTerm> codeCategoryTerms) {
		// TODO Auto-generated constructor stub
	} }

    public static final class SuggestQuery { public String getSuggestString() {
			return suggestString;
		}

		public void setSuggestString(String suggestString) {
			this.suggestString = suggestString;
		}

	public String suggestString; public SuggestQuery() {} public SuggestQuery(String s) { this.suggestString = s; } }

    public interface ConceptType { String name(); }
    public static final class Container implements ConceptType { public String name = "Container"; public String name() { return name; } }
    public static final class Folder implements ConceptType { public String name = "Folder"; public String name() { return name; } }
    public static final class Leaf implements ConceptType { public String name = "Leaf"; public String name() { return name; } }

    public static final class VisualAttributes {
        public ConceptType conceptType;
        public boolean isActive;
        public VisualAttributes() {}
        public VisualAttributes(ConceptType c, boolean a) { this.conceptType = c; this.isActive = a; }

        public boolean isActive() {return isActive;}
        public ConceptType getConceptType() {return conceptType;}
        public static VisualAttributes fromString(String visualAttributes) {
            if (visualAttributes == null || visualAttributes.length() < 2) return new VisualAttributes(new Container(), true);
            char t = visualAttributes.charAt(0);
            ConceptType ct = switch (t) {
                case 'C' -> new Container();
                case 'F' -> new Folder();
                case 'L', 'M' -> new Leaf();
                default -> new Container();
            };
            boolean active = visualAttributes.charAt(1) == 'A';
            return new VisualAttributes(ct, active);
        }
    }
}
