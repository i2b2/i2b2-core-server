/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/

package edu.harvard.i2b2.crc.dao.redcap;

import com.google.gson.annotations.SerializedName;

public class DataCollectionInstrumentMetadata {

	@SerializedName("field_name")
	private String fieldName;

	@SerializedName("field_label")
	private String fieldLabel;

	@SerializedName("field_note")
	private String fieldNote;


	@SerializedName("field_type")
	private String fieldType; 
	
	@SerializedName("text_validation_type_or_show_slider_number")
	private String fieldContent;

	@SerializedName("select_choices_or_calculations")
	private String choices;

	private String[] choicesArr;

	@SerializedName("identifier")
	private String isPHI;

	@SerializedName("form_name")
	private String formName;

	private String value;

	public DataCollectionInstrumentMetadata() {
		super();
	}

	public DataCollectionInstrumentMetadata(String record, String fieldName, String eventName, String value) {
		this.fieldName = fieldName;
		this.value = value;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getFieldName() {
		return fieldName;
	}

	public String getValue() {
		return value;
	}

	public String getFieldLabel() {
		return fieldLabel;
	}

	public void setFieldLabel(String fieldLabel) {
		this.fieldLabel = fieldLabel;
	}

	public String getFieldType() {
		if (fieldContent.equals("date_mdy"))
			return "date";
		else 
			return fieldType;
	}

	public void setFieldType(String fieldType) {

		this.fieldType = fieldType;
	}

	public boolean isHasCheckBoxOptions() {
		//yesno, text, radio, descriptive, checkbox, notes, dropdown
		if (fieldType.equals("checkbox") )
			return true;
		else
			return  false;
	}

	public boolean isHasEnumOptions() {
		//yesno, text, radio, descriptive, checkbox, notes, dropdown
		if ((fieldType.equals("yesno") || fieldType.equals("radio") ||  fieldType.equals("dropdown")))
			return true;
		else
			return  false;

	}

	public boolean isNoteOptions() {
		if ((fieldType.equals("descriptive") || fieldType.equals("notes")))
			return true;
		else
			return false;
	}
	public boolean isTextOptions() {
		if ((fieldType.equals("text") && (!getFieldType().equals("date"))))
			return true;
		else
			return false;
	}

	public String[] getChoices() {
		String[] tmpChoices =  new String[0];
		if (fieldType.equals("yesno"))
		{
			choicesArr = new String[] {"Yes", "No"};
		} else {
			if ((choices == null || choices.length() == 0))
				return tmpChoices;

			tmpChoices = choices.split("\\|");
			this.choicesArr = tmpChoices;
		}

		return choicesArr;
	}

	public String getChoice(int i) {

		String[] tmpChoices = choices.split("\\|");
		if (fieldType.equals("yesno"))
			tmpChoices = new String[] {"1, Yes", "2, No"};
		int startComma = tmpChoices[i].indexOf(',') + 2;
		return tmpChoices[i].substring(startComma);

	}

	public boolean isPHI() {
		if (isPHI.equalsIgnoreCase("Y"))
			return  true;
		else
			return  false;
	}

	public String getFieldNote() {
		return fieldNote;
	}

	public void setFieldNote(String fieldNote) {
		this.fieldNote = fieldNote;
	}

	public String getFormName() {
		return formName;
	}

	public void setFormName(String formName) {
		this.formName = formName;
	}

}
