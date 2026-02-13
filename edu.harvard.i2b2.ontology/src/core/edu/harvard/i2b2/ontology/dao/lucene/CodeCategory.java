package edu.harvard.i2b2.ontology.dao.lucene;


import java.util.Objects;

/**
 * Java translation of Scala: case class CodeCategory(name: String)
 */
public final class CodeCategory {
    private final String name;

    public CodeCategory(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CodeCategory)) return false;
        CodeCategory that = (CodeCategory) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "CodeCategory{" +
                "name='" + name + '\'' +
                '}';
    }
}
