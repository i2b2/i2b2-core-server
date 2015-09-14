/**
 * @projectDescription	Displays demographic information for a single patient set.
 * @inherits	i2b2
 * @namespace	i2b2.Dem1Set
 * @author	Nick Benik, Griffin Weber MD PhD
 * @version 	1.3
 * ----------------------------------------------------------------------------------------
 * updated 12-22-08: 	Initial Launch [Griffin Weber] 
 */

i2b2.Dem1Set.Init = function(loadedDiv) {
	// register DIV as valid DragDrop target for Patient Record Sets (PRS) objects
	var op_trgt = {dropTarget:true};
	i2b2.sdx.Master.AttachType("Dem1Set-PRSDROP", "PRS", op_trgt);
	// drop event handlers used by this plugin
	i2b2.sdx.Master.setHandlerCustom("Dem1Set-PRSDROP", "PRS", "DropHandler", i2b2.Dem1Set.prsDropped);

	// manage YUI tabs
	this.yuiTabs = new YAHOO.widget.TabView("Dem1Set-TABS", {activeIndex:0});
	this.yuiTabs.on('activeTabChange', function(ev) { 
		//Tabs have changed 
		if (ev.newValue.get('id')=="Dem1Set-TAB1") {
			// user switched to Results tab
			if (i2b2.Dem1Set.model.prsRecord) {
				// contact PDO only if we have data
				if (i2b2.Dem1Set.model.dirtyResultsData) {
					// recalculate the results only if the input data has changed
					i2b2.Dem1Set.getResults();
				}
			}
		}
	});
	
	z = $('anaPluginViewFrame').getHeight() - 34;
	$$('DIV#Dem1Set-TABS DIV.Dem1Set-MainContent')[0].style.height = z;
	$$('DIV#Dem1Set-TABS DIV.Dem1Set-MainContent')[1].style.height = z;
	$$('DIV#Dem1Set-TABS DIV.Dem1Set-MainContent')[2].style.height = z;
	
};

i2b2.Dem1Set.Unload = function() {
	// purge old data
	i2b2.Dem1Set.model.prsRecord = false;
	return true;
};

i2b2.Dem1Set.prsDropped = function(sdxData) {
	sdxData = sdxData[0];	// only interested in first record
	// save the info to our local data model
	i2b2.Dem1Set.model.prsRecord = sdxData;
	// let the user know that the drop was successful by displaying the name of the patient set
	$("Dem1Set-PRSDROP").innerHTML = i2b2.h.Escape(sdxData.sdxInfo.sdxDisplayName);
	// temporarly change background color to give GUI feedback of a successful drop occuring
	$("Dem1Set-PRSDROP").style.background = "#CFB";
	setTimeout("$('Dem1Set-PRSDROP').style.background='#DEEBEF'", 250);	
	// optimization to prevent requerying the hive for new results if the input dataset has not changed
	i2b2.Dem1Set.model.dirtyResultsData = true;		
};

i2b2.Dem1Set.getResults = function() {
	if (i2b2.Dem1Set.model.dirtyResultsData) {

		var msg_filter = '<input_list>\n' +
			'	<patient_list max="99999" min="0">\n' +
			'		<patient_set_coll_id>'+i2b2.Dem1Set.model.prsRecord.sdxInfo.sdxKeyValue+'</patient_set_coll_id>\n'+
			'	</patient_list>\n'+
			'</input_list>\n'+
			'<filter_list />\n'+
			'<output_option>\n'+
			'	<patient_set select="using_input_list" onlykeys="false"/>\n'+
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

			$$("DIV#Dem1Set-mainDiv DIV#Dem1Set-TABS DIV.results-working")[0].hide();			
			$$("DIV#Dem1Set-mainDiv DIV#Dem1Set-TABS DIV.results-finished")[0].show();


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
			i2b2.Dem1Set.model.sumCounts = eval("("+hData.toJSON()+")");

			var s = '';
			
			s += '<div class="intro">';
			s += 'Below are the demographic details for the selected patient set. ';
			s += 'For each demographic category, the values, number of patients, and a histogram are shown.';
			s += '</div>';

			s += '<div class="resultLV">';
			s += '<div class="resultLbl">Patient Set:</div>';
			s += '<div class="resultVal">' + i2b2.Dem1Set.model.prsRecord.sdxInfo.sdxDisplayName + '</div>';
			s += '</div>'
	
			s += '<div class="resultLV">';
			s += '<div class="resultLbl">Patient Count:</div>';
			s += '<div class="resultVal">' + i2b2.Dem1Set.model.prsRecord.origData.size + '</div>';
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
				var DemCatVals = [];
				var maxVal = 0;
				for (var DemCatVal in i2b2.Dem1Set.model.sumCounts[DemCat]) {
					var tempVal = i2b2.Dem1Set.model.sumCounts[DemCat][DemCatVal];
					DemCatVals.push( [DemCatVal, tempVal] );
					//if (tempVal > maxVal) {maxVal = tempVal;}
					maxVal += tempVal;
				}
				maxVal *= 1.0;
				if (maxVal > 0) {
					s += '<div class="demcatTitle">' + DemCats[DemCat] + '</div>';
					s += '<table>';
					DemCatVals.sort();
					//DemCatVals.sort(function() {return arguments[0][0] > arguments[1][0]});
					for (i1=0; i1<DemCatVals.length; i1++) {
						var barWidth = 200 * (DemCatVals[i1][1]/maxVal);
						s += '<tr>';
						s += '<th>' + DemCatVals[i1][0] + '</th>';
						s += '<td>' + DemCatVals[i1][1] + '</td>';
						s += '<td class="barTD"><div class="bar" style="width:' + barWidth + 'px;"></div></td>';
						s += '</tr>';
					}
					s += '</table>';
				}
			}
			s += '</div>';
			
			s += '<br/>';

			$$("DIV#Dem1Set-mainDiv DIV#Dem1Set-TABS DIV.results-finished")[0].innerHTML = s;

			// optimization - only requery when the input data is changed
			i2b2.Dem1Set.model.dirtyResultsData = false;		
		}
		
		$$("DIV#Dem1Set-mainDiv DIV#Dem1Set-TABS DIV.results-directions")[0].hide();
		$$("DIV#Dem1Set-mainDiv DIV#Dem1Set-TABS DIV.results-finished")[0].hide();
		$$("DIV#Dem1Set-mainDiv DIV#Dem1Set-TABS DIV.results-working")[0].show();		
		
		// AJAX CALL USING THE EXISTING CRC CELL COMMUNICATOR
		i2b2.CRC.ajax.getPDO_fromInputList("Plugin:Dem1Set", {PDO_Request: msg_filter}, scopedCallback);
	}
}
