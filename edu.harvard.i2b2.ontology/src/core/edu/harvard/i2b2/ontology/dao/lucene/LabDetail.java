package edu.harvard.i2b2.ontology.dao.lucene;


import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.xml.sax.InputSource;

/**
 * Java translation of LabDetail.scala
 */
public final class LabDetail {
    private final Optional<List<String>> flagValues;
    private final Optional<List<String>> units;
    private final Optional<List<String>> enumValues;

    public LabDetail(Optional<List<String>> flagValues, Optional<List<String>> units, Optional<List<String>> enumValues) {
        this.flagValues = flagValues == null ? Optional.empty() : flagValues;
        this.units = units == null ? Optional.empty() : units;
        this.enumValues = enumValues == null ? Optional.empty() : enumValues;
    }

    public Optional<List<String>> getFlagValues() { return flagValues; }
    public Optional<List<String>> getUnits() { return units; }
    public Optional<List<String>> getEnumValues() { return enumValues; }

    /**
     * Parse labDetailXml and return Optional.empty() if parsing fails or the XML is invalid.
     */
    public static Optional<LabDetail> fromXml(String labDetailXml, String path) {
        if (labDetailXml == null || labDetailXml.trim().isEmpty()) return Optional.empty();

        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(false);
            DocumentBuilder db = dbf.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(labDetailXml));
            Document doc = db.parse(is);

            // Ensure a Version node exists
            NodeList versionNodes = doc.getElementsByTagName("Version");
            if (versionNodes == null || versionNodes.getLength() == 0) {
                return Optional.empty();
            }

            // Flagstouse
            String flagsToUse = "";
            NodeList flagNodes = doc.getElementsByTagName("Flagstouse");
            if (flagNodes != null && flagNodes.getLength() > 0) {
                flagsToUse = safeText(flagNodes.item(0));
            }

            Optional<List<String>> flagsOption = Optional.empty();
            if (flagsToUse != null && !flagsToUse.trim().isEmpty()) {
                if (flagsToUse.trim().equalsIgnoreCase("HL")) {
                    flagsOption = Optional.of(List.of("Normal", "High", "Low"));
                } else if (flagsToUse.trim().equalsIgnoreCase("A")) {
                    flagsOption = Optional.of(List.of("Normal", "Abnormal"));
                }
            }

            // Units: NormalUnits, EqualUnits, ConvertingUnits -> Units
            LinkedHashSet<String> unitsSet = new LinkedHashSet<>();

            NodeList normalNodes = doc.getElementsByTagName("NormalUnits");
            for (int i = 0; normalNodes != null && i < normalNodes.getLength(); i++) {
                String t = safeText(normalNodes.item(i));
                if (t != null && !t.isBlank()) unitsSet.add(t);
            }

            NodeList equalNodes = doc.getElementsByTagName("EqualUnits");
            for (int i = 0; equalNodes != null && i < equalNodes.getLength(); i++) {
                String t = safeText(equalNodes.item(i));
                if (t != null && !t.isBlank()) unitsSet.add(t);
            }

            NodeList convertingNodes = doc.getElementsByTagName("ConvertingUnits");
            for (int i = 0; convertingNodes != null && i < convertingNodes.getLength(); i++) {
                Node conv = convertingNodes.item(i);
                if (conv instanceof Element) {
                    Element convEl = (Element) conv;
                    NodeList unitNodes = convEl.getElementsByTagName("Units");
                    for (int j = 0; unitNodes != null && j < unitNodes.getLength(); j++) {
                        String t = safeText(unitNodes.item(j));
                        if (t != null && !t.isBlank()) unitsSet.add(t);
                    }
                }
            }

            Optional<List<String>> unitsOption = Optional.empty();
            if (!unitsSet.isEmpty()) {
                List<String> unitsList = new ArrayList<>(unitsSet);
                unitsOption = Optional.of(Collections.unmodifiableList(unitsList));
            }

            // Enum values: <EnumValues><Val>...</Val></EnumValues>
            NodeList enumValNodes = doc.getElementsByTagName("Val");
            List<String> enumList = new ArrayList<>();
            for (int i = 0; enumValNodes != null && i < enumValNodes.getLength(); i++) {
                String t = safeText(enumValNodes.item(i));
                if (t != null && !t.isBlank()) enumList.add(t);
            }
            Optional<List<String>> enumOption = Optional.empty();
            if (!enumList.isEmpty()) enumOption = Optional.of(Collections.unmodifiableList(enumList));

            return Optional.of(new LabDetail(flagsOption, unitsOption, enumOption));
        } catch (Exception e) {
            // parsing or any other error -> return empty, matching Scala's Try/Failure -> None
            return Optional.empty();
        }
    }

    private static String safeText(Node node) {
        if (node == null) return "";
        String txt = node.getTextContent();
        return txt == null ? "" : txt.trim();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LabDetail)) return false;
        LabDetail that = (LabDetail) o;
        return Objects.equals(flagValues, that.flagValues) && Objects.equals(units, that.units) && Objects.equals(enumValues, that.enumValues);
    }

    @Override
    public int hashCode() {
        return Objects.hash(flagValues, units, enumValues);
    }

    @Override
    public String toString() {
        return "LabDetail{" +
                "flagValues=" + flagValues +
                ", units=" + units +
                ", enumValues=" + enumValues +
                '}';
    }
}
