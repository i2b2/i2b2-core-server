/**
 * @projectDescription	Example using the Patient Data Object (PDO).
 * @inherits	i2b2
 * @namespace	i2b2.ExampPDO
 * @author	Nick Benik, Griffin Weber MD PhD
 * @version 	1.3
 * ----------------------------------------------------------------------------------------
 * updated 11-06-08: 	Initial Launch [Nick Benik] 
 */

i2b2.ExampPDO.Init = function(loadedDiv) {
	// register DIV as valid DragDrop target for Patient Record Sets (PRS) objects
	var op_trgt = {dropTarget:true};
	i2b2.sdx.Master.AttachType("ExampPDO-CONCPTDROP", "CONCPT", op_trgt);
	i2b2.sdx.Master.AttachType("ExampPDO-PRSDROP", "PRS", op_trgt);
	// drop event handlers used by this plugin
	i2b2.sdx.Master.setHandlerCustom("ExampPDO-CONCPTDROP", "CONCPT", "DropHandler", i2b2.ExampPDO.conceptDropped);
	i2b2.sdx.Master.setHandlerCustom("ExampPDO-PRSDROP", "PRS", "DropHandler", i2b2.ExampPDO.prsDropped);
	// set default output options
	i2b2.ExampPDO.model.outputOptions = {};
	i2b2.ExampPDO.model.outputOptions.patients = true;
	i2b2.ExampPDO.model.outputOptions.events = true;
	i2b2.ExampPDO.model.outputOptions.observations = true;
	i2b2.ExampPDO.model.outputOptions.modifiers = true;
	i2b2.ExampPDO.model.outputOptions.observers = true;

	// manage YUI tabs
	this.yuiTabs = new YAHOO.widget.TabView("ExampPDO-TABS", {activeIndex:0});
	this.yuiTabs.on('activeTabChange', function(ev) { 
		//Tabs have changed 
		if (ev.newValue.get('id')=="ExampPDO-TAB1") {
			// user switched to Results tab
			if (i2b2.ExampPDO.model.conceptRecord && i2b2.ExampPDO.model.prsRecord) {
			// contact PDO only if we have data
			if (i2b2.ExampPDO.model.dirtyResultsData) {
					// recalculate the results only if the input data has changed
					i2b2.ExampPDO.getResults();
				}
			}
		}
	});
};

i2b2.ExampPDO.Unload = function() {
	// purge old data
	i2b2.ExampPDO.model.prsRecord = false;
	i2b2.ExampPDO.model.conceptRecord = false;
	i2b2.ExampPDO.model.dirtyResultsData = true;
	i2b2.ExampPDO.model.outputOptions.patients = true;
	i2b2.ExampPDO.model.outputOptions.events = true;
	i2b2.ExampPDO.model.outputOptions.observations = true;
	i2b2.ExampPDO.model.outputOptions.modifiers = true;
	i2b2.ExampPDO.model.outputOptions.observers = true;	
	return true;
};

i2b2.ExampPDO.prsDropped = function(sdxData) {
	sdxData = sdxData[0];	// only interested in first record
	// save the info to our local data model
	i2b2.ExampPDO.model.prsRecord = sdxData;
	// let the user know that the drop was successful by displaying the name of the patient set
	$("ExampPDO-PRSDROP").innerHTML = i2b2.h.Escape(sdxData.sdxInfo.sdxDisplayName);
	// temporarly change background color to give GUI feedback of a successful drop occuring
	$("ExampPDO-PRSDROP").style.background = "#CFB";
	setTimeout("$('ExampPDO-PRSDROP').style.background='#DEEBEF'", 250);	
	// optimization to prevent requerying the hive for new results if the input dataset has not changed
	i2b2.ExampPDO.model.dirtyResultsData = true;		
};

i2b2.ExampPDO.conceptDropped = function(sdxData) {
	sdxData = sdxData[0];	// only interested in first record
	// save the info to our local data model
	i2b2.ExampPDO.model.conceptRecord = sdxData;
	// let the user know that the drop was successful by displaying the name of the concept
	$("ExampPDO-CONCPTDROP").innerHTML = i2b2.h.Escape(sdxData.sdxInfo.sdxDisplayName);
	// temporarly change background color to give GUI feedback of a successful drop occuring
	$("ExampPDO-CONCPTDROP").style.background = "#CFB";
	setTimeout("$('ExampPDO-CONCPTDROP').style.background='#DEEBEF'", 250);	
	// optimization to prevent requerying the hive for new results if the input dataset has not changed
	i2b2.ExampPDO.model.dirtyResultsData = true;		
};

i2b2.ExampPDO.chgOutputOption = function(ckBox,option) {
	i2b2.ExampPDO.model.outputOptions[option] = ckBox.checked;
	i2b2.ExampPDO.model.dirtyResultsData = true;
};

i2b2.ExampPDO.getResults = function() {
	if (i2b2.ExampPDO.model.dirtyResultsData) {

		// translate the concept XML for injection as PDO item XML
		var t = i2b2.ExampPDO.model.conceptRecord.origData.xmlOrig;
		var cdata = {};
		cdata.level = i2b2.h.getXNodeVal(t, "level");
		cdata.key = i2b2.h.getXNodeVal(t, "key");
		cdata.tablename = i2b2.h.getXNodeVal(t, "tablename");
		cdata.dimcode = i2b2.h.getXNodeVal(t, "dimcode");
		cdata.synonym = i2b2.h.getXNodeVal(t, "synonym_cd");

		var output_options = '';
		if (i2b2.ExampPDO.model.outputOptions.patients) {
			output_options += '	<patient_set select="using_input_list" onlykeys="false"/>\n';
		}
		if (i2b2.ExampPDO.model.outputOptions.events) {
			output_options += '	<event_set select="using_input_list" onlykeys="false"/>\n';
		}
		if (i2b2.ExampPDO.model.outputOptions.modifiers) {
			output_options += '	<modifier_set blob="false" onlykeys="false"/>\n';
			isModifier = ' withmodifiers="true" ';
		} else {
			isModifier = '';	
		}
		if (i2b2.ExampPDO.model.outputOptions.observations) {
			
			output_options += '	<observation_set blob="false" ' + isModifier + ' onlykeys="false"/>\n';
		}
		if (i2b2.ExampPDO.model.outputOptions.observers) {
			output_options += '	<observer_set blob="false" onlykeys="false"/>\n';
		}
		if (i2b2.ExampPDO.model.outputOptions.modifiers) {
			output_options += '	<modifier_set blob="false" onlykeys="false"/>\n';
		}
		var msg_filter = '<input_list>\n' +
			'	<patient_list max="6" min="1">\n'+   // <--- only the first 5 records
			'		<patient_set_coll_id>'+i2b2.ExampPDO.model.prsRecord.sdxInfo.sdxKeyValue+'</patient_set_coll_id>\n'+
			'	</patient_list>\n'+
			'</input_list>\n'+
			'<filter_list>\n'+
			'	<panel name="'+cdata.key+'">\n'+
			'		<panel_number>0</panel_number>\n'+
			'		<panel_accuracy_scale>0</panel_accuracy_scale>\n'+
			'		<invert>0</invert>\n';
			
		if (i2b2.ExampPDO.model.conceptRecord.origData.isModifier) {
			msg_filter += '		<item>\n'+
				'			<item_key>'+i2b2.ExampPDO.model.conceptRecord.origData.parent.key+'</item_key>\n'+
				'			     <constrain_by_modifier>\n'+
     			'			     <applied_path>'+i2b2.ExampPDO.model.conceptRecord.origData.parent.dim_code+'%</applied_path>\n'+
     			'			     <modifier_key>'+i2b2.ExampPDO.model.conceptRecord.origData.key+'</modifier_key>\n'+
			    '			     </constrain_by_modifier>\n'+
				'		</item>\n';
		} else {	
			msg_filter += '		<item>\n'+
			'			<hlevel>'+cdata.level+'</hlevel>\n'+
			'			<item_key>'+cdata.key+'</item_key>\n'+
			'			<dim_tablename>'+cdata.tablename+'</dim_tablename>\n'+
			'			<dim_dimcode>'+cdata.dimcode+'</dim_dimcode>\n'+
			'			<item_is_synonym>'+cdata.synonym+'</item_is_synonym>\n'+
			'		</item>\n';
		}
		
		
		msg_filter += '	</panel>\n'+
			'</filter_list>\n'+
			'<output_option>\n'+
				output_options+
			'</output_option>\n';


		// callback processor
		var scopedCallback = new i2b2_scopedCallback();
		scopedCallback.scope = this;
		scopedCallback.callback = function(results) {
			// THIS function is used to process the AJAX results of the getChild call
			//		results data object contains the following attributes:
			//			refXML: xmlDomObject <--- for data processing
			//			msgRequest: xml (string)
			//			msgResponse: xml (string)
			//			error: boolean
			//			errorStatus: string [only with error=true]
			//			errorMsg: string [only with error=true]
			
			// check for errors
			if (results.error) {
				alert('The results from the server could not be understood.  Press F12 for more information.');
				console.error("Bad Results from Cell Communicator: ",results);
				return false;
			}

			$$("DIV#ExampPDO-mainDiv DIV#ExampPDO-TABS DIV.results-working")[0].hide();
			$$("DIV#ExampPDO-mainDiv DIV#ExampPDO-TABS DIV.results-finished")[0].show();
			var divResults = $$("DIV#ExampPDO-mainDiv DIV#ExampPDO-InfoPDO")[0];
			Element.select(divResults, '.InfoPDO-Request .originalXML')[0].innerHTML = '<pre>'+i2b2.h.Escape(results.msgRequest)+'</pre>';
			Element.select(divResults, '.InfoPDO-Response .originalXML')[0].innerHTML = '<pre>'+i2b2.h.Escape(results.msgResponse)+'</pre>';

			// optimization - only requery when the input data is changed
			i2b2.ExampPDO.model.dirtyResultsData = false;
		}
		
		$$("DIV#ExampPDO-mainDiv DIV#ExampPDO-TABS DIV.results-directions")[0].hide();
		$$("DIV#ExampPDO-mainDiv DIV#ExampPDO-TABS DIV.results-finished")[0].hide();
		$$("DIV#ExampPDO-mainDiv DIV#ExampPDO-TABS DIV.results-working")[0].show();		
		// AJAX CALL USING THE EXISTING CRC CELL COMMUNICATOR
		i2b2.CRC.ajax.getPDO_fromInputList("Plugin:ExampPDO", {patient_limit:5, PDO_Request: msg_filter}, scopedCallback);
	}
}
