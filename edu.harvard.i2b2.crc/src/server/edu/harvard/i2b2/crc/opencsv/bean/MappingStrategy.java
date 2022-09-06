package edu.harvard.i2b2.crc.opencsv.bean;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.IOException;

import edu.harvard.i2b2.crc.opencsv.CSVReader;

/**
 * The interface for the classes that handle translating between the columns in the csv file
 * to an actual object.
 *
 * @param <T> type of object you are converting the data to.
 */
public interface MappingStrategy<T> {
    /**
     * Implementation will have to return a property descriptor from a bean based on the current column.
     *
     * @param col the column to find the description for
     * @return the related PropertyDescriptor
     * @throws IntrospectionException - thrown on error loading the property descriptors.
     */
    PropertyDescriptor findDescriptor(int col) throws IntrospectionException;

    /**
     * Implementation will have to return - based on the current column - a BeanField containing
     * the {@link java.lang.reflect.Field} and a boolean representing whether the field is required (mandatory) or not.
     *
     * @param col the column to find the field for
     * @return BeanField containing Field and whether it is required
     */
    BeanField findField(int col);

    /**
     * Implementation will return a bean of the type of object you are mapping.
     *
     * @return A new instance of the class being mapped.
     * @throws InstantiationException - thrown on error creating object.
     * @throws IllegalAccessException - thrown on error creating object.
     */
    T createBean() throws InstantiationException, IllegalAccessException;

    /**
     * Implementation of this method can grab the header line before parsing begins to use to map columns
     * to bean properties.
     *
     * @param reader the CSVReader to use for header parsing
     * @throws java.io.IOException if parsing fails
     */
    void captureHeader(CSVReader reader) throws IOException;

    /**
     * Gets the column index that corresponds to a specific colum name.
     * If the CSV file doesn't have a header row, this method will always return
     * null.
     *
     * @param name the column name
     * @return the column index, or null if the name doesn't exist
     */
    Integer getColumnIndex(String name);

    /**
     * Determines whether the mapping strategy is driven by {@link edu.harvard.i2b2.crc.opencsv.bean.CsvBind} annotations.
     *
     * @return whether the mapping strategy is driven by annotations
     */
    boolean isAnnotationDriven();
}