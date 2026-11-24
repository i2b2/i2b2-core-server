package edu.harvard.i2b2.ontology.dao.lucene;

public record SuggestionPayload(Metadata metadata, int totalNum) {
	public record Metadata(String tableName, String dimCode, String baseCode,
			String keyName, String visualAttributes, String tooltip) {
	}
}
