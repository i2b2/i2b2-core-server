package edu.harvard.i2b2.ontology.dao.lucene;


import org.apache.lucene.document.Document;
import org.apache.lucene.index.StoredFields;
import org.apache.lucene.search.ScoreDoc;

import edu.harvard.i2b2.ontology.dao.lucene.LuceneSearcher.HtmlHighlighter;
import edu.harvard.i2b2.ontology.dao.lucene.LuceneService.ConceptType;
import edu.harvard.i2b2.ontology.dao.lucene.LuceneService.VisualAttributes;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Java translation of the Scala ConceptInfo case class and companion object.
 */
public final class ConceptInfo {
    private final String nPath;
    private final String displayName;
    private final Optional<String> highlightedName;
    private final ConceptType conceptType;
    private final boolean isActive;
    private final Optional<List<ConceptInfo>> children;

    public ConceptInfo(String nPath,
                       String displayName,
                       Optional<String> highlightedName,
                       ConceptType conceptType,
                       boolean isActive,
                       Optional<List<ConceptInfo>> children) {
        this.nPath = nPath;
        this.displayName = displayName;
        this.highlightedName = highlightedName == null ? Optional.empty() : highlightedName;
        this.conceptType = conceptType;
        this.isActive = isActive;
        this.children = children == null ? Optional.empty() : children;
    }

    public String getnPath() { return nPath; }
    public String getDisplayName() { return displayName; }
    public Optional<String> getHighlightedName() { return highlightedName; }
    public ConceptType getConceptType() { return conceptType; }
    public boolean isActive() { return isActive; }
    public Optional<List<ConceptInfo>> getChildren() { return children; }

    /**
     * Returns a new ConceptInfo with children replaced by newChildren (immutable copy semantics).
     */
    public ConceptInfo updateChildren(Optional<List<ConceptInfo>> newChildren) {
        return new ConceptInfo(this.nPath, this.displayName, this.highlightedName, this.conceptType, this.isActive, newChildren);
    }

    /**
     * Create a ConceptInfo from a Lucene ScoreDoc. Mirrors the Scala companion apply(scoreDoc, ...).
     */
    public static ConceptInfo fromScoreDoc(ScoreDoc scoreDoc, boolean highlight, Optional<List<ConceptInfo>> childConcepts) {
        try {
        	StoredFields storedFields = LuceneSearcher.searcher.storedFields();
        	Document doc = storedFields.document(scoreDoc.doc);
//            Document doc = LuceneSearcher.searcher.doc(scoreDoc.doc);
            String nPath = doc.get("nPath");
            String displayName = doc.get("displayName");
            String visualAttr = doc.get("visualAttributes");

            VisualAttributes vis = VisualAttributes.fromString(visualAttr);
            return of(nPath, displayName, vis, childConcepts, highlight);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Create a ConceptInfo from explicit pieces. Mirrors the Scala companion apply(...).
     */
    public static ConceptInfo of(String nPath,
                                 String displayName,
                                 VisualAttributes visualAttributes,
                                 Optional<List<ConceptInfo>> childrenConcepts,
                                 boolean highlight) {

        Optional<String> highlighted = highlight ? Optional.of(HtmlHighlighter.highlight(displayName)) : Optional.empty();
        ConceptType ct = visualAttributes.conceptType;
        boolean active = visualAttributes.isActive;

        return new ConceptInfo(nPath, displayName, highlighted, ct, active, childrenConcepts);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConceptInfo)) return false;
        ConceptInfo that = (ConceptInfo) o;
        return isActive == that.isActive && Objects.equals(nPath, that.nPath) && Objects.equals(displayName, that.displayName) && Objects.equals(highlightedName, that.highlightedName) && Objects.equals(conceptType, that.conceptType) && Objects.equals(children, that.children);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nPath, displayName, highlightedName, conceptType, isActive, children);
    }

    @Override
    public String toString() {
        return "ConceptInfo{" +
                "nPath='" + nPath + '\'' +
                ", displayName='" + displayName + '\'' +
                ", highlightedName=" + highlightedName +
                ", conceptType=" + conceptType +
                ", isActive=" + isActive +
                ", children=" + children +
                '}';
    }
}
