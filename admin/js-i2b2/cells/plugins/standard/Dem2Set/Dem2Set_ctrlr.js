/**
 * @projectDescription	Displays demographic information for a single patient set.
 * @inherits	i2b2
 * @namespace	i2b2.Dem2Set
 * @author	Nick Benik, Griffin Weber MD PhD
 * @version 	1.3
 * ----------------------------------------------------------------------------------------
 * updated 12-22-08: 	Initial Launch [Griffin Weber] 
 */

i2b2.Dem2Set.Init = function(loadedDiv) {
	// register DIV as valid DragDrop target for Patient Record Sets (PRS) objects
	var op_trgt = {dropTarget:true};
	i2b2.sdx.Master.AttachType("Dem2Set-PRSDROP1", "PRS", op_trgt);
	i2b2.sdx.Master.AttachType("Dem2Set-PRSDROP2", "PRS", op_trgt);
	// drop event handlers used by this plugin
	i2b2.sdx.Master.setHandlerCustom("Dem2Set-PRSDROP1", "PRS", "DropHandler", i2b2.Dem2Set.prsDropped1);
	i2b2.sdx.Master.setHandlerCustom("Dem2Set-PRSDROP2", "PRS", "DropHandler", i2b2.Dem2Set.prsDropped2);
	// array for patient sets and stats
	i2b2.Dem2Set.model.prsRecord = [];
	i2b2.Dem2Set.model.sumCounts = [];

	// manage YUI tabs
	this.yuiTabs = new YAHOO.widget.TabView("Dem2Set-TABS", {activeIndex:0});
	this.yuiTabs.on('activeTabChange', function(ev) { 
		//Tabs have changed 
		if (ev.newValue.get('id')=="Dem2Set-TAB1") {
			// user switched to Results tab
			if (i2b2.Dem2Set.model.prsRecord[0] && i2b2.Dem2Set.model.prsRecord[1]) {
				// contact PDO only if we have data
				if (i2b2.Dem2Set.model.dirtyResultsData) {
					// recalculate the results only if the input data has changed
					i2b2.Dem2Set.getResults();
				}
			}
		}
	});
		z = $('anaPluginViewFrame').getHeight() - 34;
	$$('DIV#Dem2Set-TABS DIV.Dem2Set-MainContent')[0].style.height = z;
	$$('DIV#Dem2Set-TABS DIV.Dem2Set-MainContent')[1].style.height = z;
	$$('DIV#Dem2Set-TABS DIV.Dem2Set-MainContent')[2].style.height = z;

};

i2b2.Dem2Set.Unload = function() {
	// purge old data
	i2b2.Dem2Set.model.prsRecord[0] = false;
	i2b2.Dem2Set.model.prsRecord[1] = false;
	i2b2.Dem2Set.model.sumCounts[0] = false;
	i2b2.Dem2Set.model.sumCounts[1] = false;
	return true;
};

i2b2.Dem2Set.prsDropped1 = function(sdxData) {
	sdxData = sdxData[0];	// only interested in first record
	// save the info to our local data model
	i2b2.Dem2Set.model.prsRecord[0] = sdxData;
	// let the user know that the drop was successful by displaying the name of the patient set
	$("Dem2Set-PRSDROP1").innerHTML = i2b2.h.Escape(sdxData.sdxInfo.sdxDisplayName);
	// temporarly change background color to give GUI feedback of a successful drop occuring
	$("Dem2Set-PRSDROP1").style.background = "#CFB";
	setTimeout("$('Dem2Set-PRSDROP1').style.background='#DEEBEF'", 250);	
	// optimization to prevent requerying the hive for new results if the input dataset has not changed
	i2b2.Dem2Set.model.dirtyResultsData = true;		
};

i2b2.Dem2Set.prsDropped2 = function(sdxData) {
	sdxData = sdxData[0];	// only interested in first record
	// save the info to our local data model
	i2b2.Dem2Set.model.prsRecord[1] = sdxData;
	// let the user know that the drop was successful by displaying the name of the patient set
	$("Dem2Set-PRSDROP2").innerHTML = i2b2.h.Escape(sdxData.sdxInfo.sdxDisplayName);
	// temporarly change background color to give GUI feedback of a successful drop occuring
	$("Dem2Set-PRSDROP2").style.background = "#CFB";
	setTimeout("$('Dem2Set-PRSDROP2').style.background='#DEEBEF'", 250);	
	// optimization to prevent requerying the hive for new results if the input dataset has not changed
	i2b2.Dem2Set.model.dirtyResultsData = true;		
};

i2b2.Dem2Set.getCounts = function(results,which) {
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

	// get all the patient records
	var pData = i2b2.h.XPath(results.refXML, 'descendant::patient/param[@column]/text()/..');
	var hData = new Hash();
	for (var i1=0; i1<pData.length; i1++) {
		var n = pData[i1].getAttribute('column');
		var t1 = hData.get(n);
		if (!t1) { t1 = new Hash(); }
		var v = pData[i1].firstChild.nodeValue;
		if (n=="birth_date") {
			v = v.substring(0, v.indexOf("T"));
		}
		if (n=="age_in_years_num") {
			v = Math.floor(v/10);
			if (v<0) {v=0;}
			if (v>9) {v=9;}
			if (v==0) {v='0-10';} else {v=v+'0-'+(v+1)+'0';}
		}
		var t2 = t1.get(v);
		if (!t2) {
			t2 = 1;
		} else {
			t2++;
		}
		t1.set(v, t2);
		hData.set(n, t1);
	}

	// collapse the hash objects to regular objects and save to the Plugin's data model
	i2b2.Dem2Set.model.sumCounts[which] = eval("("+hData.toJSON()+")");
	
	if (i2b2.Dem2Set.model.sumCounts[0] && i2b2.Dem2Set.model.sumCounts[1]) {
		i2b2.Dem2Set.drawResults();
	}
	
};

i2b2.Dem2Set.drawResults = function() {

	var s = '';

	s += '<div class="intro">';
	s += 'Below are the demographic details for the selected patient sets. ';
	s += 'For each demographic category, the values, number of patients, and a histogram are shown. ';
	s += 'The pink bar and first patient count for each category value corresponds to Patient Set 1. ';
	s += 'The green bar and second patient count for each category value corresponds to Patient Set 2. ';
	s += '</div>';

	s += '<div class="resultLV1">';
	s += '<div class="resultLbl">Patient Set 1:</div>';
	s += '<div class="resultVal">' + i2b2.Dem2Set.model.prsRecord[0].sdxInfo.sdxDisplayName + '</div>';
	s += '</div>'

	s += '<div class="resultLV2">';
	s += '<div class="resultLbl">Patient Set 2:</div>';
	s += '<div class="resultVal">' + i2b2.Dem2Set.model.prsRecord[1].sdxInfo.sdxDisplayName + '</div>';
	s += '</div>'

	s += '<div class="resultLV1">';
	s += '<div class="resultLbl">Patient Count 1:</div>';
	s += '<div class="resultVal">' + i2b2.Dem2Set.model.prsRecord[0].origData.size + '</div>';
	s += '</div>'

	s += '<div class="resultLV2">';
	s += '<div class="resultLbl">Patient Count 2:</div>';
	s += '<div class="resultVal">' + i2b2.Dem2Set.model.prsRecord[1].origData.size + '</div>';
	s += '</div>'


	s += '<div class="demTables">';
	var DemCats = {age_in_years_num:'Age in Years', 
			sex_cd:'Sex',
			race_cd:'Race',
			language_cd:'Language',
			marital_status:'Marital Status',
			religion_cd:'Religion',
			vital_status_cd:'Vital Status (Deceased)'
		};
		
	for (var DemCat in DemCats) {
		var DemCatVals = {};
		var DemCatValsList = [];
		var maxVal = [];

		for (var i1=0; i1<2; i1++) {
			maxVal[i1] = 0;
			for (var DemCatVal in i2b2.Dem2Set.model.sumCounts[i1][DemCat]) {
				var tempVal = i2b2.Dem2Set.model.sumCounts[i1][DemCat][DemCatVal];
				if (!DemCatVals[DemCatVal]) {
					DemCatVals[DemCatVal] = [0,0];
					DemCatValsList.push(DemCatVal);
				}
				DemCatVals[DemCatVal][i1] = tempVal;
				maxVal[i1] += tempVal;
			}
			maxVal[i1] *= 1.0;
		}
				
		if ((maxVal[0] > 0)&&(maxVal[1] > 0)) {
			s += '<div class="demcatTitle">' + DemCats[DemCat] + '</div>';
			s += '<table>';
			DemCatValsList.sort();
			for (var i2=0; i2<DemCatValsList.length; i2++) {
				var DemCatVal = DemCatValsList[i2];
				var barWidth1 = 200 * (DemCatVals[DemCatVal][0]/maxVal[0]);
				var barWidth2 = 200 * (DemCatVals[DemCatVal][1]/maxVal[1]);
				s += '<tr>';
				s += '<th rowspan="2">' + DemCatVal + '</th>';
				s += '<td>' + DemCatVals[DemCatVal][0] + '</td>';
				s += '<td class="barTD1" valign="top"><div class="bar1" style="width:' + barWidth1 + 'px;"></div></td>';
				s += '</tr>';
				s += '<tr>';
				s += '<td>' + DemCatVals[DemCatVal][1] + '</td>';
				s += '<td class="barTD2" valign="top"><div class="bar2" style="width:' + barWidth2 + 'px;"></div></td>';
				s += '</tr>';
			}
			s += '</table>';
		}

	}
	
	s += '</div>';

	s += '<br/>';

	$$("DIV#Dem2Set-mainDiv DIV#Dem2Set-TABS DIV.results-finished")[0].innerHTML = s;
	$$("DIV#Dem2Set-mainDiv DIV#Dem2Set-TABS DIV.results-working")[0].hide();			
	$$("DIV#Dem2Set-mainDiv DIV#Dem2Set-TABS DIV.results-finished")[0].show();

	// optimization - only requery when the input data is changed
	i2b2.Dem2Set.model.dirtyResultsData = false;		
};

i2b2.Dem2Set.getResults = function() {
	if (i2b2.Dem2Set.model.dirtyResultsData) {

		var msg_filter1 = '<input_list>\n' +
			'	<patient_list max="99999" min="0">\n' +
			'		<patient_set_coll_id>'+i2b2.Dem2Set.model.prsRecord[0].sdxInfo.sdxKeyValue+'</patient_set_coll_id>\n'+
			'	</patient_list>\n'+
			'</input_list>\n'+
			'<filter_list />\n'+
			'<output_option>\n'+
			'	<patient_set select="using_input_list" onlykeys="false"/>\n'+
			'</output_option>\n';
			
		var msg_filter2 = '<input_list>\n' +
			'	<patient_list max="99999" min="0">\n' +
			'		<patient_set_coll_id>'+i2b2.Dem2Set.model.prsRecord[1].sdxInfo.sdxKeyValue+'</patient_set_coll_id>\n'+
			'	</patient_list>\n'+
			'</input_list>\n'+
			'<filter_list />\n'+
			'<output_option>\n'+
			'	<patient_set select="using_input_list" onlykeys="false"/>\n'+
			'</output_option>\n';

		// callback processor (set 1)
		var scopedCallback1 = new i2b2_scopedCallback();
		scopedCallback1.scope = this;
		scopedCallback1.callback = function(results) {i2b2.Dem2Set.getCounts(results,0);}

		// callback processor (set 2)
		var scopedCallback2 = new i2b2_scopedCallback();
		scopedCallback2.scope = this;
		scopedCallback2.callback = function(results) {i2b2.Dem2Set.getCounts(results,1);}
		
		$$("DIV#Dem2Set-mainDiv DIV#Dem2Set-TABS DIV.results-directions")[0].hide();
		$$("DIV#Dem2Set-mainDiv DIV#Dem2Set-TABS DIV.results-finished")[0].hide();
		$$("DIV#Dem2Set-mainDiv DIV#Dem2Set-TABS DIV.results-working")[0].show();		
		
		// AJAX CALL USING THE EXISTING CRC CELL COMMUNICATOR
		i2b2.CRC.ajax.getPDO_fromInputList("Plugin:Dem2Set", {PDO_Request: msg_filter1}, scopedCallback1);
		i2b2.CRC.ajax.getPDO_fromInputList("Plugin:Dem2Set", {PDO_Request: msg_filter2}, scopedCallback2);
	}
}
