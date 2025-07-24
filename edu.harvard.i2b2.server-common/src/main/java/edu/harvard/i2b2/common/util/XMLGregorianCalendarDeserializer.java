package edu.harvard.i2b2.common.util;

import java.lang.reflect.Type;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

public class XMLGregorianCalendarDeserializer
implements JsonDeserializer<XMLGregorianCalendar> {
	@Override
	public XMLGregorianCalendar deserialize(JsonElement json, Type typeOfT,
			JsonDeserializationContext context) throws JsonParseException {
		XMLGregorianCalendarAdapterClass ac = 
				new Gson().fromJson(json, 
						XMLGregorianCalendarAdapterClass.class);
		try {
			return DatatypeFactory.newInstance()
					.newXMLGregorianCalendar(ac.getYear(), ac.getMonth(), 
							ac.getDay(), ac.getHour(),        
							ac.getMinute(), ac.getSecond(),
							ac.getFractionalSecond(), ac.getTimezone());
		} catch (DatatypeConfigurationException e) {
			e.printStackTrace();
		}
		return null;
	}



}
