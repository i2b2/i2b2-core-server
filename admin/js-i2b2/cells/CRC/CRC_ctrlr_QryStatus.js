/**
 * @projectDescription	The Asynchronous Query Status controller (GUI-only controller).
 * @inherits 	i2b2.CRC.ctrlr
 * @namespace	i2b2.CRC.ctrlr.QueryStatus
 * @author		Nick Benik, Griffin Weber MD PhD
 * @version 	1.0
 * ----------------------------------------------------------------------------------------
 * updated 8-10-09: Initial Creation [Nick Benik] 
 */

i2b2.CRC.ctrlr.QueryStatus = function(dispDIV) { this.dispDIV = dispDIV; };

i2b2.CRC.ctrlr.QueryStatus._GetTitle = function(resultType, oRecord, oXML) {
	var title = "";
	switch (resultType) {
		case "PATIENT_ENCOUNTER_SET":
			// use given title if it exist otherwise generate a title
			try {
				var t = i2b2.h.XPath(oXML,'self::query_result_instance/description')[0].firstChild.nodeValue;
			} catch(e) {
				var t = null;
			}
			if (!t) { t = "Encounter Set"; }
			// create the title using shrine setting
			if (oRecord.size >= 10) {
				if (i2b2.PM.model.isObfuscated) {
					title = t+" - "+oRecord.size+"&plusmn;3 encounters";
				} else {
					title = t; //+" - "+oRecord.size+" encounters";
				}
			} else {
				if (i2b2.PM.model.isObfuscated) {
					title = t+" - 10 encounters or less";
				} else {
					title = t; //+" - "+oRecord.size+" encounters";
				}
			}
			break;		
		case "PATIENTSET":
			// use given title if it exist otherwise generate a title
			try {
				var t = i2b2.h.XPath(oXML,'self::query_result_instance/description')[0].firstChild.nodeValue;
			} catch(e) {
				var t = null;
			}
			if (!t) { t = "Patient Set"; }
			// create the title using shrine setting
			if (oRecord.size >= 10) {
				if (i2b2.PM.model.isObfuscated) {
					title = t+" - "+oRecord.size+"&plusmn;3 patients";
				} else {
					title = t; //+" - "+oRecord.size+" patients";
				}
			} else {
				if (i2b2.PM.model.isObfuscated) {
					title = t+" - 10 patients or less";
				} else {
					title = t; //+" - "+oRecord.size+" patients";
				}
			}
			break;
		case "PATIENT_COUNT_XML":
			// use given title if it exist otherwise generate a title
			try {
				var t = i2b2.h.XPath(oXML,'self::query_result_instance/description')[0].firstChild.nodeValue;
			} catch(e) {
				var t = null;
			}
			if (!t) { t="Patient Count"; }
			// create the title using shrine setting
			if (oRecord.size >= 10) {
				if (i2b2.PM.model.isObfuscated) {
					title = t+" - "+oRecord.size+"&plusmn;3 patients";
				} else {
					title = t+" - "+oRecord.size+" patients";
				}
			} else {
				if (i2b2.PM.model.isObfuscated) {
					title = t+" - 10 patients or less";
				} else {
					title = t+" - "+oRecord.size+" patients";
				}
			}
			break;
		default : 
			try {
				title = i2b2.h.XPath(oXML,'self::query_result_instance/query_result_type/description')[0].firstChild.nodeValue;
			} catch(e) {
			}		
			break;
	}

	return title;
};


function trim(sString)
{
while (sString.substring(0,1) == '\n')
{
sString = sString.substring(1, sString.length);
}
while (sString.substring(sString.length-1, sString.length) == '\n')
{
sString = sString.substring(0,sString.length-1);
}
return sString;
} 

i2b2.CRC.ctrlr.QueryStatus.prototype = function() {
	var private_singleton_isRunning = false;
	var private_startTime = false; 
	var private_refreshInterrupt = false;
		
	function private_pollStatus() {
		var self = i2b2.CRC.ctrlr.currentQueryStatus;
		// this is a private function that is used by all QueryStatus object instances to check their status
		// callback processor to check the Query Instance
		var scopedCallbackQI = new i2b2_scopedCallback();
		scopedCallbackQI.scope = self;
		scopedCallbackQI.callback = function(results) {
			if (results.error) {
				alert(results.errorMsg);
				return;
			} else {
				// find our query instance
				var qi_list = results.refXML.getElementsByTagName('query_instance');
				var l = qi_list.length;
				for (var i=0; i<l; i++) {
					var temp = qi_list[i];
					var qi_id = i2b2.h.XPath(temp, 'descendant-or-self::query_instance_id')[0].firstChild.nodeValue;
					
						this.QI.message = i2b2.h.getXNodeVal(temp, 'message');
						this.QI.start_date = i2b2.h.getXNodeVal(temp, 'start_date');
						if (!Object.isUndefined(this.QI.start_date)) {
							//alert(sDate.substring(0,4) + ":" + sDate.substring(5,7)  + ":" + sDate.substring(8,10));
							//012345678901234567890123
							//2010-12-21T16:12:01.427
							this.QI.start_date =  new Date(this.QI.start_date.substring(0,4), this.QI.start_date.substring(5,7)-1, this.QI.start_date.substring(8,10), this.QI.start_date.substring(11,13),this.QI.start_date.substring(14,16),this.QI.start_date.substring(17,19),this.QI.start_date.substring(20,23));
						}						
						this.QI.end_date = i2b2.h.getXNodeVal(temp, 'end_date');
						if (!Object.isUndefined(this.QI.end_date)) {
							//alert(sDate.substring(0,4) + ":" + sDate.substring(5,7)  + ":" + sDate.substring(8,10));
							this.QI.end_date =  new Date(this.QI.end_date.substring(0,4), this.QI.end_date.substring(5,7)-1, this.QI.end_date.substring(8,10), this.QI.end_date.substring(11,13),this.QI.end_date.substring(14,16),this.QI.end_date.substring(17,19),this.QI.end_date.substring(20,23));
						}					
					
					if (qi_id == this.QI.id) {
						// found the query instance, extract the info
						this.QI.status = i2b2.h.XPath(temp, 'descendant-or-self::query_status_type/name')[0].firstChild.nodeValue;
						this.QI.statusID = i2b2.h.XPath(temp, 'descendant-or-self::query_status_type/status_type_id')[0].firstChild.nodeValue;
						private_singleton_isRunning = false;
						
						i2b2.CRC.ajax.getQueryResultInstanceList_fromQueryInstanceId("CRC:QueryStatus", {qi_key_value: self.QI.id}, scopedCallbackQRS);
							// force a final redraw
					//		i2b2.CRC.ctrlr.currentQueryStatus.refreshStatus();
							// refresh the query history window
					//		i2b2.CRC.ctrlr.history.Refresh();
					//		}
						break;
					}
				}
			}
		}
		

		// callback processor to check the Query Result Set
		var scopedCallbackQRS = new i2b2_scopedCallback();
		scopedCallbackQRS.scope = self;
		scopedCallbackQRS.callback = function(results) {
			if (results.error) {
				alert(results.errorMsg);
				return;
			} else {
				// find our query instance
				var qrs_list = results.refXML.getElementsByTagName('query_result_instance');
				var l = qrs_list.length;
				for (var i=0; i<l; i++) {
					var temp = qrs_list[i];
					var qrs_id = i2b2.h.XPath(temp, 'descendant-or-self::result_instance_id')[0].firstChild.nodeValue;
					if (self.QRS.hasOwnProperty(qrs_id)) {
						var rec = self.QRS[qrs_id];
					} else {
						var rec = new Object();
						rec.QRS_ID = qrs_id;
						rec.size = i2b2.h.getXNodeVal(temp, 'set_size');
						rec.start_date = i2b2.h.getXNodeVal(temp, 'start_date');
						if (!Object.isUndefined(rec.start_date)) {
							//alert(sDate.substring(0,4) + ":" + sDate.substring(5,7)  + ":" + sDate.substring(8,10));
							//012345678901234567890123
							//2010-12-21T16:12:01.427
							rec.start_date =  new Date(rec.start_date.substring(0,4), rec.start_date.substring(5,7)-1, rec.start_date.substring(8,10), rec.start_date.substring(11,13),rec.start_date.substring(14,16),rec.start_date.substring(17,19),rec.start_date.substring(20,23));
						}						
						rec.end_date = i2b2.h.getXNodeVal(temp, 'end_date');
						if (!Object.isUndefined(rec.end_date)) {
							//alert(sDate.substring(0,4) + ":" + sDate.substring(5,7)  + ":" + sDate.substring(8,10));
							rec.end_date =  new Date(rec.end_date.substring(0,4), rec.end_date.substring(5,7)-1, rec.end_date.substring(8,10), rec.end_date.substring(11,13),rec.end_date.substring(14,16),rec.end_date.substring(17,19),rec.end_date.substring(20,23));
						}						
						
						rec.QRS_DisplayType = i2b2.h.XPath(temp, 'descendant-or-self::query_result_type/display_type')[0].firstChild.nodeValue;						
						rec.QRS_Type = i2b2.h.XPath(temp, 'descendant-or-self::query_result_type/name')[0].firstChild.nodeValue;
						rec.QRS_Description = i2b2.h.XPath(temp, 'descendant-or-self::description')[0].firstChild.nodeValue;						
						rec.QRS_TypeID = i2b2.h.XPath(temp, 'descendant-or-self::query_result_type/result_type_id')[0].firstChild.nodeValue;
					}
					rec.QRS_Status = i2b2.h.XPath(temp, 'descendant-or-self::query_status_type/name')[0].firstChild.nodeValue;
					rec.QRS_Status_ID = i2b2.h.XPath(temp, 'descendant-or-self::query_status_type/status_type_id')[0].firstChild.nodeValue;
					// create execution time string
					var d = new Date();
					var t = Math.floor((d.getTime() - private_startTime)/100)/10;
					var exetime = t.toString();
					if (exetime.indexOf('.') < 0) {
						exetime += '.0';
					}
					// deal with time/status setting
					if (!rec.QRS_time) { rec.QRS_time = exetime; }
					
					// set the proper title if it was not already set
					if (!rec.title) {
						rec.title = i2b2.CRC.ctrlr.QueryStatus._GetTitle(rec.QRS_Type, rec, temp);
					}				
					self.QRS[qrs_id] = rec;
				}
				i2b2.CRC.ctrlr.history.Refresh();
			}
			// force a redraw
			i2b2.CRC.ctrlr.currentQueryStatus.refreshStatus();
			//i2b2.CRC.view.graphs.clearGraphs('making graphs ...');
		}
		
		
		// fire off the ajax calls
		i2b2.CRC.ajax.getQueryInstanceList_fromQueryMasterId("CRC:QueryStatus", {qm_key_value: self.QM.id}, scopedCallbackQI);


                // make graph - snm0
								//alert("HERE WITH THIS:\n" + sCompiledResultsTest); //snm0 
		//i2b2.CRC.view.graphs.createGraphs("infoQueryStatusChart", i2b2.CRC.view.graphs.returnTestString(false)), false);  // single site testing
		//i2b2.CRC.view.graphs.createGraphs("infoQueryStatusChart", i2b2.CRC.view.graphs.returnTestString(true), true);  // multisite testing


    }  // end of private_pollStatus
	
	function private_refresh_status() {
		var sCompiledResultsTest = "";  // snm0 - this is the text for the graph display
				// callback processor to check the Query Instance
		var scopedCallbackQRSI = new i2b2_scopedCallback();
		scopedCallbackQRSI.scope = self;
		// This is where each breakdown in the results is obtained
		// each breakdown comes through here separately
		scopedCallbackQRSI.callback = function(results) {
			if (results.error) {
				alert(results.errorMsg);
				return;
			} else {
				// find our query instance

				var ri_list = results.refXML.getElementsByTagName('query_result_instance');
				var l = ri_list.length;
				for (var i=0; i<l; i++) {
					var temp = ri_list[i];
					// get the query name for display in the box
					var description = i2b2.h.XPath(temp, 'descendant-or-self::description')[0].firstChild.nodeValue;
					self.dispDIV.innerHTML += "<div style=\"clear: both;  padding-top: 10px; font-weight: bold;\">" + description + "</div>";					
					sCompiledResultsTest += description + '\n';  //snm0
				} 
				var crc_xml = results.refXML.getElementsByTagName('crc_xml_result');
				l = crc_xml.length;
				for (var i=0; i<l; i++) {			
					var temp = crc_xml[i];
					var xml_value = i2b2.h.XPath(temp, 'descendant-or-self::xml_value')[0].firstChild.nodeValue;

					var xml_v = i2b2.h.parseXml(xml_value);	
						
					var params = i2b2.h.XPath(xml_v, 'descendant::data[@column]/text()/..');
					for (var i2 = 0; i2 < params.length; i2++) {
						var name = params[i2].getAttribute("name");
						if (i2b2.PM.model.isObfuscated) {
							if ( params[i2].firstChild.nodeValue < 4) {
								var value = "<3";	
							} else {
								var value = params[i2].firstChild.nodeValue + "&plusmn;3" ;
							}
						} else
						{
							var value = params[i2].firstChild.nodeValue;							
						}
						// display a line of results in the status box
						self.dispDIV.innerHTML += "<div style=\"clear: both; margin-left: 20px; float: left; height: 16px; line-height: 16px;\">" + params[i2].getAttribute("column") + ": <font color=\"#0000dd\">" + value  + "</font></div>";
						sCompiledResultsTest += params[i2].getAttribute("column") + " : " + value + "\n"; //snm0
					}
					var ri_id = i2b2.h.XPath(temp, 'descendant-or-self::result_instance_id')[0].firstChild.nodeValue;
				}
				//alert(sCompiledResultsTest); //snm0 
				i2b2.CRC.view.graphs.createGraphs("infoQueryStatusChart", sCompiledResultsTest, i2b2.CRC.view.graphs.bIsSHRINE);
				if (i2b2.CRC.view.graphs.bisGTIE8) i2b2.CRC.view.status.selectTab('graphs');
				//self.dispDIV.innerHTML += this.dispMsg;
			}
		}

		
		var self = i2b2.CRC.ctrlr.currentQueryStatus;
		// this private function refreshes the display DIV
					var d = new Date();
			var t = Math.floor((d.getTime() - private_startTime)/100)/10;
			var s = t.toString();
			if (s.indexOf('.') < 0) {
				s += '.0';
			}
		if (private_singleton_isRunning) {
			self.dispDIV.innerHTML = '<div style="clear:both;"><div style="float:left; font-weight:bold">Running Query: "'+self.QM.name+'"</div>';
			// display the current run duration

			self.dispDIV.innerHTML += '<div style="float:right">['+s+' secs]</div>';
		} else {
			self.dispDIV.innerHTML = '<div style="clear:both;"><div style="float:left; font-weight:bold">Finished Query: "'+self.QM.name+'"</div>';
			self.dispDIV.innerHTML += '<div style="float:right">['+s+' secs]</div>';
			
			//		self.dispDIV.innerHTML += '<div style="margin-left:20px; clear:both; height:16px; line-height:16px; "><div height:16px; line-height:16px; ">Compute Time: ' + (Math.floor((self.QI.end_date - self.QI.start_date)/100))/10 + ' secs</div></div>';
			//		self.dispDIV.innerHTML += '</div>';
			$('runBoxText').innerHTML = "Run Query";

		}
		self.dispDIV.innerHTML += '</div>';
		if ((!private_singleton_isRunning) && (undefined != self.QI.end_date)){
			self.dispDIV.innerHTML += '<div style="margin-left:20px; clear:both; line-height:16px; ">Compute Time: '+ (Math.floor((self.QI.end_date - self.QI.start_date)/100))/10 +' secs</div>';
		}
		
		var foundError = false;

		for (var i in self.QRS) {
			var rec = self.QRS[i];			
			if (rec.QRS_time) {
				var t = '<font color="';
				// display status of query in box
				switch(rec.QRS_Status) {
					case "ERROR":
						self.dispDIV.innerHTML += '<div style="clear:both; height:16px; line-height:16px; "><div style="float:left; font-weight:bold; height:16px; line-height:16px; ">'+rec.title+'</div><div style="float:right; height:16px; line-height:16px; "><font color="#dd0000">ERROR</font></div>';
	//					self.dispDIV.innerHTML += '<div style="float:right; height:16px; line-height:16px; "><font color="#dd0000">ERROR</font></div>'; //['+rec.QRS_time+' secs]</div>';
						foundError = true;
						break;
					case "COMPLETED":
					case "FINISHED":
						foundError = false;
						//t += '#0000dd">'+rec.QRS_Status;
						break;
					case "INCOMPLETE":
					case "WAITTOPROCESS":
					case "PROCESSING":
						self.dispDIV.innerHTML += '<div style="clear:both; height:16px;line-height:16px; "><div style="float:left; font-weight:bold;  height:16px; line-height:16px; ">'+rec.title+'</div><div style="float:right; height:16px; line-height:16px; "><font color="#00dd00">PROCESSING</font></div>';	
		//				self.dispDIV.innerHTML += '<div style="float:right; height:16px; line-height:16px; "><font color="#00dd00">PROCESSING</font></div>'; //['+rec.QRS_time+' secs]</div>';
						alert('Your query has timed out and has been rescheduled to run in the background.  The results will appear in "Previous Queries"');
						foundError = true;
					
						//t += '#00dd00">'+rec.QRS_Status;
						break;
				}
				t += '</font> ';
				//self.dispDIV.innerHTML += '<div style="float:right; height:16px; line-height:16px; ">'+t+'['+rec.QRS_time+' secs]</div>';
			}
			self.dispDIV.innerHTML += '</div>';
			if (foundError == false) {
				if (rec.QRS_DisplayType == "CATNUM") {
					i2b2.CRC.ajax.getQueryResultInstanceList_fromQueryResultInstanceId("CRC:QueryStatus", {qr_key_value: rec.QRS_ID}, scopedCallbackQRSI);
				} else if ((rec.QRS_DisplayType == "LIST") && (foundError == false)) {
					self.dispDIV.innerHTML += "<div style=\"clear: both; padding-top: 10px; font-weight: bold;\">" + rec.QRS_Description + "</div>";
				} 
				if (rec.QRS_Type == "PATIENTSET") {
				
					// Check to see if timeline is checked off, if so switch to timeline
					var t2 = $('dialogQryRun').select('INPUT.chkQueryType');
					for (var i=0;i<t2.length; i++) {
						var curItem = t2[i].nextSibling.data;
						if (curItem != undefined)
						{
							curItem = curItem.toLowerCase();
							//curitem = curItem.trim();
						}
						if ((t2[i].checked == true) && (rec.size > 0) && (curItem == " timeline")  
						&& !(i2b2.h.isBadObjPath('i2b2.Timeline.cfg.config.plugin'))
						) {

							i2b2.hive.MasterView.setViewMode('Analysis');
							i2b2.PLUGINMGR.ctrlr.main.selectPlugin("Timeline");
					
							//Process PatientSet
							rec.QM_id = self.QM.id;
							rec.QI_id = self.QI.id;
							rec.PRS_id = rec.QRS_ID;
							rec.result_instance_id = rec.PRS_id;
							var sdxData = {};
							sdxData[0] = i2b2.sdx.Master.EncapsulateData('PRS', rec);							
							i2b2.Timeline.prsDropped(sdxData);
							
							i2b2.Timeline.setShowMetadataDialog(false);
							
							//Process Concepts, put all concepts in one large set
							sdxData = {};
							for (var j2 = 0; j2 < i2b2.CRC.model.queryCurrent.panels.length; j2++) {
							var panel_list = i2b2.CRC.model.queryCurrent.panels[j2]
							var panel_cnt = panel_list.length;
							
							for (var p2 = 0; p2 < panel_cnt; p2++) {
								// Concepts
								for (var i2=0; i2 < panel_list[p2].items.length; i2++) {
									sdxData[0] = panel_list[p2].items[i2];
									i2b2.Timeline.conceptDropped(sdxData);
								}
							}
							}
							//$('Timeline-pgstart').value = '1';
							//$('Timeline-pgsize').value = '10';
							//i2b2.Timeline.pgGo(0);
							i2b2.Timeline.yuiTabs.set('activeIndex', 1);
							
							i2b2.Timeline.setShowMetadataDialog(true);
						}
					} 
				}
			}
		}
		if ((undefined != self.QI.message)  && (foundError == false))
		{
			self.dispDIV.innerHTML += '<div style="clear:both; float:left;  padding-top: 10px; font-weight:bold">Status</div>';
			
			
			var mySplitResult = self.QI.message.split("<?xml");

			for(i3 = 1; i3 < mySplitResult.length; i3++){

				var xml_v = i2b2.h.parseXml(trim("<?xml " + mySplitResult[i3]));	
							
							//var qrs_list = results.refXML.getElementsByTagName('process_step_timing');
					
				//var params = i2b2.h.XPath(xml_v,'descendant::name/text()/..');
				//var name2 = i2b2.h.XPath(xml_v, 'descendant::process_step_timing/text()/..');
				//var name33 = i2b2.h.XPath(xml_v, 'descendant::name[@column]/text()/..');
				for (var i2 = 0; i2 < xml_v.childNodes.length; i2++) {
					try { 
						if (i2b2.PM.model.isObfuscated) {
							if (i2b2.h.XPath(xml_v, 'descendant::total_time_second/text()/..')[i2].firstChild.nodeValue < 4)
							{
							    var value = "<3";
							} else {
								var value = i2b2.h.XPath(xml_v, 'descendant::total_time_second/text()/..')[i2].firstChild.nodeValue + "&plusmn;3";
							}
						} else
						{
							var value =  i2b2.h.XPath(xml_v, 'descendant::total_time_second/text()/..')[i2].firstChild.nodeValue;							
						}					
					self.dispDIV.innerHTML += '<div style="margin-left:20px; clear:both; line-height:16px; ">' + i2b2.h.XPath(xml_v, 'descendant::name/text()/..')[i2].firstChild.nodeValue + '<font color="#0000dd">: ' + value + ' secs</font></div>';
					//snm0
					//alert('<div style="margin-left:20px; clear:both; line-height:16px; ">' + i2b2.h.XPath(xml_v, 'descendant::name/text()/..')[i2].firstChild.nodeValue + '<font color="#0000dd">: ' + value + ' secs</font></div>');
					//self.dispDIV.innerHTML += '<div style="float: left; height: 16px; margin-right: 100px; line-height: 16px;"><font color="#0000dd">: ' + i2b2.h.XPath(xml_v, 'descendant::total_time_second/text()/..')[i2].firstChild.nodeValue + ' secs</font></div>';
					} catch (e) {}
				}
			}
		}
		// self.QI.message = null;
		//mm self.dispDIV.innerHTML = dispMsg;
		//self.dispDIV.style.backgroundColor = '#F00';
		self.dispDIV.style.display = 'none';
		self.dispDIV.style.display = 'block';

		if (!private_singleton_isRunning && private_refreshInterrupt) {
			// make sure our refresh interrupt is turned off
			try {
				clearInterval(private_refreshInterrupt);
				private_refreshInterrupt = false;
			} catch (e) {}
		}
		

	}

	
	function private_cancelQuery() {
		if (private_singleton_isRunning) {
			try {
				var self = i2b2.CRC.ctrlr.currentQueryStatus;
				i2b2.CRC.ctrlr.history.queryDeleteNoPrompt(self.QM.id);
					clearInterval(private_refreshInterrupt);
					private_refreshInterrupt = false;
					private_singleton_isRunning = false;
					$('runBoxText').innerHTML = "Run Query";
					self.dispDIV.innerHTML += '<div style="clear:both; height:16px; line-height:16px; text-align:center; color:r#ff0000;">QUERY CANCELLED</div>';
					i2b2.CRC.ctrlr.currentQueryStatus = false; 
			} catch (e) {}	
		}
	}

	function private_startQuery() {
		var self = i2b2.CRC.ctrlr.currentQueryStatus;
		if (private_singleton_isRunning) { return false; }
		private_singleton_isRunning = true;
		self.dispDIV.innerHTML = '<b>Processing Query: "'+this.name+'"</b>';
		self.QM.name = this.name; 
		self.QRS = {};
		 self.QI = {};
		
		// callback processor to run the query from definition
		this.callbackQueryDef = new i2b2_scopedCallback();
		this.callbackQueryDef.scope = this;
		this.callbackQueryDef.callback = function(results) {
				
			if (results.error) {
					var temp = results.refXML.getElementsByTagName('response_header')[0];
					if (undefined != temp) {
						results.errorMsg = i2b2.h.XPath(temp, 'descendant-or-self::result_status/status')[0].firstChild.nodeValue;
						if (results.errorMsg.substring(0,9) == "LOCKEDOUT")
						{
							results.errorMsg = 'As an "obfuscated user" you have exceeded the allowed query repeat and are now LOCKED OUT, please notify your system administrator.';
						}
					}
				alert(results.errorMsg);
				private_cancelQuery();
				return;
			} else {
				//		"results" object contains the following attributes:
				//			refXML: xmlDomObject <--- for data processing
				//			msgRequest: xml (string)
				//			msgResponse: xml (string)
				//			error: boolean
				//			errorStatus: string [only with error=true]
				//			errorMsg: string [only with error=true]
				// save the query master
				
				//Check to see if condition failed
				var condition = results.refXML.getElementsByTagName('condition')[0];
				if (condition.getAttribute("type") == "ERROR")
				{
					
					results.errorMsg = 'ERROR: ' + condition.firstChild.nodeValue;
					alert(results.errorMsg);
					private_cancelQuery();
					return;
				}
				var temp = results.refXML.getElementsByTagName('query_master')[0];
				self.QM.id = i2b2.h.getXNodeVal(temp, 'query_master_id');
				self.QM.name = i2b2.h.XPath(temp, 'descendant-or-self::name')[0].firstChild.nodeValue;

				// save the query instance
				var temp = results.refXML.getElementsByTagName('query_instance')[0];
				self.QI.id = i2b2.h.XPath(temp, 'descendant-or-self::query_instance_id')[0].firstChild.nodeValue;
				self.QI.status = i2b2.h.XPath(temp, 'descendant-or-self::query_status_type/name')[0].firstChild.nodeValue;
				self.QI.statusID = i2b2.h.XPath(temp, 'descendant-or-self::query_status_type/status_type_id')[0].firstChild.nodeValue;
				
				// we don't need to poll, all Result instances are listed in this message
				if (false && (self.QI.status == "INCOMPLETE" || self.QI.status == "COMPLETED" || self.QI.status == "ERROR")) {
					// create execution time string
					var d = new Date();
					var t = Math.floor((d.getTime() - private_startTime)/100)/10;
					var exetime = t.toString();
					if (exetime.indexOf('.') < 0) {
						exetime += '.0';
					}
					var qi_list = results.refXML.getElementsByTagName('query_result_instance');
					var l = qi_list.length;
					for (var i=0; i<l; i++) {
						try {
							var qi = qi_list[i];
							var temp = new Object();
							temp.size = i2b2.h.getXNodeVal(qi, 'set_size');
							temp.QI_ID = i2b2.h.getXNodeVal(qi, 'query_instance_id');
							temp.QRS_ID = i2b2.h.getXNodeVal(qi, 'result_instance_id');
							temp.QRS_Type = i2b2.h.XPath(qi, 'descendant-or-self::query_result_type/name')[0].firstChild.nodeValue;
							temp.QRS_TypeID = i2b2.h.XPath(qi, 'descendant-or-self::query_result_type/result_type_id')[0].firstChild.nodeValue;
							temp.QRS_Status = i2b2.h.XPath(qi, 'descendant-or-self::query_status_type/name')[0].firstChild.nodeValue;
							temp.QRS_Status_ID = i2b2.h.XPath(qi, 'descendant-or-self::query_status_type/status_type_id')[0].firstChild.nodeValue;
							temp.QRS_time = exetime;
							// set the proper title if it was not already set
							if (!temp.title) {
								temp.title = i2b2.CRC.ctrlr.QueryStatus._GetTitle(temp.QRS_Type, temp, qi);
							}
							self.QRS[temp.QRS_ID] = temp;
						} catch	(e) {}
					}
					private_singleton_isRunning = false;
				} else {
					// another poll is required
					setTimeout("i2b2.CRC.ctrlr.currentQueryStatus.pollStatus()", this.polling_interval);
				}				
			}
		}
		
		// switch to status tab
		i2b2.CRC.view.status.showDisplay();

		// timer and display refresh stuff
		private_startTime = new Date();
		private_refreshInterrupt = setInterval("i2b2.CRC.ctrlr.currentQueryStatus.refreshStatus()", 100);

		// AJAX call
		i2b2.CRC.ajax.runQueryInstance_fromQueryDefinition("CRC:QueryTool", this.params, this.callbackQueryDef);
	}
	return {
		name: "",
		polling_interval: 1000,
		QM: {id:false, status:""},
		QI: {id:false, status:""},
		QRS:{},
		displayDIV: false,
		running: false,
		started: false,
		startQuery: function(queryName, ajaxParams) {
			this.name = queryName;
			this.params = ajaxParams;
			private_startQuery.call(this);
		},
		cancelQuery: function() {
			private_cancelQuery();
		},
		isQueryRunning: function() {
			return private_singleton_isRunning;
		},
		refreshStatus: function() {
			private_refresh_status();
		},
		pollStatus: function() {
			private_pollStatus();
		}
	};
}();

i2b2.CRC.ctrlr.currentQueryStatus = false; 

