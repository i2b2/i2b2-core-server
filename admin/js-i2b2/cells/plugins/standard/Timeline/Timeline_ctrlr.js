/**
 * @projectDescription	Visual display of PDO results in a timeline format.
 * @inherits	i2b2
 * @namespace	i2b2.Timeline
 * @author	Nick Benik, Griffin Weber MD PhD
 * @version 	1.3
 * ----------------------------------------------------------------------------------------
 * updated 11-06-08: 	Initial Launch [Griffin Weber] 
 * updated 01-13-09:	Performance tuning, added details dialogs [Nick Benik]
 */

i2b2.Timeline.Init = function(loadedDiv) {
	// register DIV as valid DragDrop target for Patient Record Sets (PRS) objects
	var op_trgt = {dropTarget:true};
	i2b2.sdx.Master.AttachType("Timeline-CONCPTDROP", "CONCPT", op_trgt);
	i2b2.sdx.Master.AttachType("Timeline-PRSDROP", "PRS", op_trgt);
	// drop event handlers used by this plugin
	i2b2.sdx.Master.setHandlerCustom("Timeline-CONCPTDROP", "CONCPT", "DropHandler", i2b2.Timeline.conceptDropped);
	i2b2.sdx.Master.setHandlerCustom("Timeline-PRSDROP", "PRS", "DropHandler", i2b2.Timeline.prsDropped);
	// array to store concepts
	i2b2.Timeline.model.concepts = [];
	// set initial pagination values
	i2b2.Timeline.model.pgstart = 1;
	i2b2.Timeline.model.pgsize = 10;
	// set initial zoom values
	i2b2.Timeline.model.zoomScale = 1.0;
	i2b2.Timeline.model.zoomPan = 1.0;

	i2b2.Timeline.model.showMetadataDialog = true;
	// manage YUI tabs
	this.yuiTabs = new YAHOO.widget.TabView("Timeline-TABS", {activeIndex:0});
	this.yuiTabs.on('activeTabChange', function(ev) { 
		//Tabs have changed 
		if (ev.newValue.get('id')=="Timeline-TAB1") {
			// user switched to Results tab
			if ((i2b2.Timeline.model.concepts.length>0) && i2b2.Timeline.model.prsRecord) {
			// contact PDO only if we have data
				if (i2b2.Timeline.model.dirtyResultsData) {
					// recalculate the results only if the input data has changed
					$('Timeline-pgstart').value = '1';
					$('Timeline-pgsize').value = '10';
					i2b2.Timeline.pgGo(0);
				}
			}
		}
	});
};

i2b2.Timeline.setShowMetadataDialog = function(sdxData) {
	i2b2.Timeline.model.showMetadataDialog = sdxData;
}

i2b2.Timeline.Unload = function() {
	// purge old data
	i2b2.Timeline.model = {};
	i2b2.Timeline.model.prsRecord = false;
	i2b2.Timeline.model.conceptRecord = false;
	i2b2.Timeline.model.dirtyResultsData = true;
	try { i2b2.Timeline.yuiPanel.destroy(); } catch(e) {}
	return true;
};

i2b2.Timeline.prsDropped = function(sdxData) {
	sdxData = sdxData[0];	// only interested in first record
	// save the info to our local data model
	i2b2.Timeline.model.prsRecord = sdxData;
	// let the user know that the drop was successful by displaying the name of the patient set
	$("Timeline-PRSDROP").innerHTML = i2b2.h.Escape(sdxData.sdxInfo.sdxDisplayName);
	// temporarly change background color to give GUI feedback of a successful drop occuring
	$("Timeline-PRSDROP").style.background = "#CFB";
	setTimeout("$('Timeline-PRSDROP').style.background='#DEEBEF'", 250);	
	// optimization to prevent requerying the hive for new results if the input dataset has not changed
	i2b2.Timeline.model.dirtyResultsData = true;		
};

i2b2.Timeline.conceptDropped = function(sdxData, showDialog) {
	sdxData = sdxData[0];	// only interested in first record
	if (sdxData.origData.isModifier) {
			alert("Modifier item being dropped is not supported.");
			return false;	
	}
	
	// save the info to our local data model
	i2b2.Timeline.model.concepts.push(sdxData);
	
					var cdetails = i2b2.ONT.ajax.GetTermInfo("CRC:QueryTool", {concept_key_value:sdxData.origData.key, ont_synonym_records: true, ont_hidden_records: true} );
					
					try { new ActiveXObject ("MSXML2.DOMDocument.6.0"); isActiveXSupported =  true; } catch (e) { isActiveXSupported =  false; }
	
					if (isActiveXSupported) {
						//Internet Explorer
						xmlDocRet = new ActiveXObject("Microsoft.XMLDOM");
						xmlDocRet.async = "false";
						xmlDocRet.loadXML(cdetails.msgResponse);
						xmlDocRet.setProperty("SelectionLanguage", "XPath");
						var c = i2b2.h.XPath(xmlDocRet, 'descendant::concept');						
					} else {										
						var c = i2b2.h.XPath(cdetails.refXML, 'descendant::concept');
					}
					if (c.length > 0) {
							sdxData.origData.xmlOrig = c[0];					
					 }	
	
	 var sdxDataNode = i2b2.sdx.Master.EncapsulateData('CONCPT',sdxData.origData);	
	//var sdxRenderData = i2b2.sdx.Master.RenderHTML(tvTree.id, sdxDataNode, renderOptions);
	
	var lvMetaDatas1 = i2b2.h.XPath(sdxData.origData.xmlOrig, 'metadataxml/ValueMetadata[string-length(Version)>0]');
	if ( (lvMetaDatas1.length > 0) && (i2b2.Timeline.model.showMetadataDialog)) {
		//bring up popup
		i2b2.Timeline.view.modalLabValues.show(this, sdxData.origData.key, sdxData, false);
	//	this.showModValues(sdxConcept.origData.key, sdxRenderData);			
	}	
		
	
	
	// sort and display the concept list
	i2b2.Timeline.conceptsRender();
	// optimization to prevent requerying the hive for new results if the input dataset has not changed
	i2b2.Timeline.model.dirtyResultsData = true;		
};

i2b2.Timeline.conceptDelete = function(concptIndex) {
	// remove the selected concept
	i2b2.Timeline.model.concepts.splice(concptIndex,1);
	// sort and display the concept list
	i2b2.Timeline.conceptsRender();
	// optimization to prevent requerying the hive for new results if the input dataset has not changed
	i2b2.Timeline.model.dirtyResultsData = true;		
};

i2b2.Timeline.Resize = function() {
	var h = parseInt( $('anaPluginViewFrame').style.height ) - 61 - 17;
	$$("DIV#Timeline-mainDiv DIV#Timeline-TABS DIV.results-timelineBox")[0].style.height = h + 'px';
	try { i2b2.Timeline.yuiPanel.destroy(); } catch(e) {}
};

i2b2.Timeline.wasHidden = function() {
	try { i2b2.Timeline.yuiPanel.destroy(); } catch(e) {}
}	

i2b2.Timeline.conceptsRender = function() {
	var s = '';
	// are there any concepts in the list
	if (i2b2.Timeline.model.concepts.length) {
		// sort the concepts in alphabetical order
		i2b2.Timeline.model.concepts.sort(function() {return arguments[0].sdxInfo.sdxDisplayName > arguments[1].sdxInfo.sdxDisplayName});
		// draw the list of concepts
		for (var i1 = 0; i1 < i2b2.Timeline.model.concepts.length; i1++) {
			if (i1 > 0) { s += '<div class="concptDiv"></div>'; }
			
			var values = i2b2.Timeline.model.concepts[i1].LabValues;	
			var tt = "";
			if (undefined  != values) {
				    switch(values.MatchBy) {
								case "FLAG":
									tt =  ' = '+i2b2.h.Escape(values.ValueFlag);
									break;
								case "VALUE":
									if (values.GeneralValueType=="ENUM") {
										var sEnum = [];
										for (var i2=0;i2<values.ValueEnum.length;i2++) {
											sEnum.push(i2b2.h.Escape(values.NameEnum[i2].text));
										}
										sEnum = sEnum.join("\", \"");
										sEnum = ' =  ("'+sEnum+'")';
										tt = sEnum;
									} else if (values.GeneralValueType=="LARGESTRING") {
										tt = ' [contains "' + i2b2.h.Escape(values.ValueString) + '"]';
									} else if (values.GeneralValueType=="STRING")  {
										if (values.StringOp == undefined )
										{
											var stringOp = "";
										} else {
										switch(values.StringOp) {
											case "LIKE[exact]":
												var  stringOp = "Exact: ";
												break;
											case "LIKE[begin]":
												var  stringOp = "Starts With: ";
												break;
											case "LIKE[end]":
												var  stringOp = "Ends With: ";
												break;
											case "LIKE[contains]":
												var  stringOp = "Contains: ";
												break;
											default:
												var stringOp = "";
												break;
										}
										}
										tt = ' ['+stringOp + i2b2.h.Escape(values.ValueString) + "]";
									} else {
										if (!Object.isUndefined(values.UnitsCtrl))
										{
											tt = values.UnitsCtrl;				
										}
										
										if (values.NumericOp == 'BETWEEN') {
											tt = i2b2.h.Escape(values.ValueLow)+' - '+i2b2.h.Escape(values.ValueHigh);
										} else {
											switch(values.NumericOp) {
											case "LT":
												var numericOp = " < ";
												break;
											case "LE":
												var numericOp = " <= ";
												break;
											case "EQ":
												var numericOp = " = ";
												break;
											case "GT":
												var numericOp = " > ";
												break;
											case "GE":
												var numericOp = " >= ";
												break;
												
											case "":
												break;	
											}
											tt = numericOp +i2b2.h.Escape(values.Value) ;
										}
									}
									break;
								case "":
									break;
							}
				}			
			
			
			s += '<a class="concptItem" href="JavaScript:i2b2.Timeline.conceptDelete('+i1+');">' + i2b2.h.Escape(i2b2.Timeline.model.concepts[i1].sdxInfo.sdxDisplayName) + tt + '</a>';
		}
		// show the delete message
		$("Timeline-DeleteMsg").style.display = 'block';
	} else {
		// no concepts selected yet
		s = '<div class="concptItem">Drop one or more Concepts here</div>';
		$("Timeline-DeleteMsg").style.display = 'none';
}
	// update html
	$("Timeline-CONCPTDROP").innerHTML = s;
};

i2b2.Timeline.showObservation = function(localkey) {
	try { i2b2.Timeline.yuiPanel.destroy(); } catch(e) {}
	var t = i2b2.Timeline.model.observation_PKs[localkey];

	// Get the blob

		var msg_filter = '<fact_primary_key>\n' +
			'		<event_id>'+t.event_id+'</event_id>\n'+
			'		<patient_id>'+t.patient_id+'</patient_id>\n'+
			'		<concept_cd>'+t.concept_id+'</concept_cd>\n'+
			'		<observer_id>'+t.observer_id+'</observer_id>\n'+
			'</fact_primary_key>\n<fact_output_option blob="true" onlykeys="false"/>\n';


		i2b2.Timeline.model.pData = "";
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

			var s = '';

			var patients = {};
			
			// get all the patient records
			 i2b2.Timeline.model.pData = i2b2.h.getXNodeVal(results.refXML, 'observation_blob');
			
			
	//end get blob
	
	var disp =	"\nevent_id: " + t.event_id + "<br />" +
				"\npatient_id: " + t.patient_id + "<br />" +
				"\nconcept_id: " + t.concept_id + "<br />" +
				"\nobserver_id: " + t.observer_id + "<br />" +
				"\nstart_date: " + t.start_date_key;
				
	if ( i2b2.h.getXNodeVal(results.refXML, 'end_date') != undefined)
		disp +=  "<br />\nend_date:" +	i2b2.h.getXNodeVal(results.refXML, 'end_date') ;	
	if ( i2b2.h.getXNodeVal(results.refXML, 'tval_char') != undefined)
			disp +=  "<br />\ntval_char:" +	i2b2.h.getXNodeVal(results.refXML, 'tval_char') ;	
	if ( i2b2.h.getXNodeVal(results.refXML, 'nval_num') != undefined)
			disp +=  "<br />\nnval_num:" +	i2b2.h.getXNodeVal(results.refXML, 'nval_num') ;	
	if ( i2b2.h.getXNodeVal(results.refXML, 'valueflag_cd') != undefined)
			disp +=  "<br />\nvalueflag_cd:" +	i2b2.h.getXNodeVal(results.refXML, 'valueflag_cd') ;	
	if ( i2b2.h.getXNodeVal(results.refXML, 'units_cd') != undefined)
			disp +=  "<br />\nunits_cd:" +	i2b2.h.getXNodeVal(results.refXML, 'units_cd') ;	
	if ( i2b2.h.getXNodeVal(results.refXML, 'modifier_cd') != undefined)
		disp +=  "<br />\nmodifier_cd:" +	i2b2.h.getXNodeVal(results.refXML, 'modifier_cd') ;	
			
	if (i2b2.Timeline.model.pData != undefined)
	{
		disp += "<hr/><pre>" + i2b2.Timeline.model.pData + "</pre>";	
	}
	i2b2.Timeline.yuiPanel = new YAHOO.widget.Panel("Timeline-InfoPanel", { width:"330px",height:"560px",
					zindex: 10000, 
					constraintoviewport: true,
					 autofillheight: "body", 
					 draggable: true,
					visible: true, 
					x: 17,
					y: 45,
					context: ["TIMELINEOBS-"+localkey,"tr","bl", ["beforeShow", "windowResize", "windowScroll"]] } );   
	i2b2.Timeline.yuiPanel.setHeader("Observation Details");
	i2b2.Timeline.yuiPanel.setBody(disp);
	i2b2.Timeline.yuiPanel.render(document.body);
	
	
	var resize = new YAHOO.util.Resize('Timeline-InfoPanel', { 
	      handles: ['br'], 
	      autoRatio: false, 
	      minWidth: 300, 
	      minHeight: 100, 
	      status: false 
	}); 
	
	resize.on('resize', function(args) { 
	    var panelHeight = args.height; 
	    this.cfg.setProperty("height", panelHeight + "px"); 
	}, panel, true);
	
	
	resize.on('startResize', function(args) { 
	 
	    if (this.cfg.getProperty("constraintoviewport")) { 
	        var D = YAHOO.util.Dom; 
	 
	        var clientRegion = D.getClientRegion(); 
	        var elRegion = D.getRegion(this.element); 
	 
	        resize.set("maxWidth", clientRegion.right - elRegion.left - YAHOO.widget.Overlay.VIEWPORT_OFFSET); 
	        resize.set("maxHeight", clientRegion.bottom - elRegion.top - YAHOO.widget.Overlay.VIEWPORT_OFFSET); 
	    } else { 
	        resize.set("maxWidth", null); 
	        resize.set("maxHeight", null); 
	    } 
	}, panel, true);  
		}
	
			// AJAX CALL USING THE EXISTING CRC CELL COMMUNICATOR
		i2b2.CRC.ajax.getIbservationfact_byPrimaryKey("Plugin:Timeline", {PDO_Request:msg_filter}, scopedCallback);
	
	
}

i2b2.Timeline.pgGo = function(dir) {
	var formStart = parseInt($('Timeline-pgstart').value);
	var formSize = parseInt($('Timeline-pgsize').value);
	if (!formStart) {formStart = 1;}
	if (!formSize) {formSize = 10;}
	if (formSize<1) {formSize = 1;}
	formStart = formStart + formSize * dir;
	if (formStart<1) {formStart = 1;}
	i2b2.Timeline.model.pgstart = formStart;
	i2b2.Timeline.model.pgsize = formSize;
	$('Timeline-pgstart').value = formStart;
	$('Timeline-pgsize').value = formSize;
	i2b2.Timeline.model.dirtyResultsData = true;
	//remove old results
	$$("DIV#Timeline-mainDiv DIV#Timeline-TABS DIV.results-directions")[0].hide();
	$('Timeline-results-scaleLbl1').innerHTML = '';
	$('Timeline-results-scaleLbl2').innerHTML = '';
	$('Timeline-results-scaleLbl3').innerHTML = '';
	$$("DIV#Timeline-mainDiv DIV#Timeline-TABS DIV.results-timeline")[0].innerHTML = '<div class="results-progress">Please wait while the timeline is being drawn...</div><div class="results-progressIcon"></div>';
	$$("DIV#Timeline-mainDiv DIV#Timeline-TABS DIV.results-finished")[0].show();		
	//reset zoom key
	$$("DIV#Timeline-mainDiv DIV#Timeline-TABS DIV.zoomKeyRange")[0].style.width = '90px';
	$$("DIV#Timeline-mainDiv DIV#Timeline-TABS DIV.zoomKeyRange")[0].style.left = '0px';
	// give a brief pause for the GUI to catch up
	setTimeout('i2b2.Timeline.getResults();', 50);
};

i2b2.Timeline.updateZoomScaleLabels = function() {
	var z = i2b2.Timeline.model.zoomScale*1.0;
	var p = i2b2.Timeline.model.zoomPan*1.0;
	// update zoom key
	$$("DIV#Timeline-mainDiv DIV#Timeline-TABS DIV.zoomKeyRange")[0].style.width = (90/z) + 'px';
	$$("DIV#Timeline-mainDiv DIV#Timeline-TABS DIV.zoomKeyRange")[0].style.left = ((p*90)-(90/z)) + 'px';
	// calculate date labels
	var first_time = i2b2.Timeline.model.first_time;
	var last_time = i2b2.Timeline.model.last_time;
	var lf = last_time - first_time;
	var t3 = first_time + lf*p;
	var t1 = t3 - lf/z;
	var t2 = (t1+t3)/2;
	var d1 = new Date(t1);
	var d2 = new Date(t2);
	var d3 = new Date(t3);
	// update labels
	$('Timeline-results-scaleLbl1').innerHTML = (d1.getMonth()+1) + '/' + d1.getDate() + '/' + d1.getFullYear();
	$('Timeline-results-scaleLbl2').innerHTML = (d2.getMonth()+1) + '/' + d2.getDate() + '/' + d2.getFullYear();
	$('Timeline-results-scaleLbl3').innerHTML = (d3.getMonth()+1) + '/' + d3.getDate() + '/' + d3.getFullYear();
}

i2b2.Timeline.zoom = function(op) {
	if (op == '+') {
		i2b2.Timeline.model.zoomScale *= 2.0;
	}
	if (op == '-') {
		i2b2.Timeline.model.zoomScale *= 0.5;
	}
	if (op == '<') {
		i2b2.Timeline.model.zoomPan -= 0.25/(i2b2.Timeline.model.zoomScale*1.0);
	}
	if (op == '>') {
		i2b2.Timeline.model.zoomPan += 0.25/(i2b2.Timeline.model.zoomScale*1.0);
	}
	if (i2b2.Timeline.model.zoomScale < 1) {
		i2b2.Timeline.model.zoomScale = 1.0;
	}
	if (i2b2.Timeline.model.zoomPan > 1) {
		i2b2.Timeline.model.zoomPan = 1.0;
	}
	if (i2b2.Timeline.model.zoomPan < 1/(i2b2.Timeline.model.zoomScale*1.0)) {
		i2b2.Timeline.model.zoomPan = 1/(i2b2.Timeline.model.zoomScale*1.0);
	}
	i2b2.Timeline.updateZoomScaleLabels();
	var z = i2b2.Timeline.model.zoomScale*1.0;
	var p = i2b2.Timeline.model.zoomPan*1.0;
	p = 100.0 * (1 - z*p);
	z = 100.0 * z;
	var o = $$("DIV#Timeline-mainDiv DIV#Timeline-TABS DIV.results-finished DIV.ptObsZoom");
	for (var i=0; i<o.length; i++) {
		o[i].style.width = z + '%';
		o[i].style.left = p + '%';
	}
};


	i2b2.Timeline.getValues = function(lvd) {
							var s = '\t\t\t<constrain_by_value>\n';
							//var lvd = sdxData.LabValues;
							switch(lvd.MatchBy) {
								case "FLAG":
									s += '\t\t\t\t<value_type>FLAG</value_type>\n';
									s += '\t\t\t\t<value_operator>EQ</value_operator>\n';
									s += '\t\t\t\t<value_constraint>'+i2b2.h.Escape(lvd.ValueFlag)+'</value_constraint>\n';
									break;
								case "VALUE":
									if (lvd.GeneralValueType=="ENUM") {
										var sEnum = [];
										for (var i2=0;i2<lvd.ValueEnum.length;i2++) {
											sEnum.push(i2b2.h.Escape(lvd.ValueEnum[i2]));
										}
										//sEnum = sEnum.join("\", \"");
										sEnum = sEnum.join("\',\'");
										sEnum = '(\''+sEnum+'\')';
										s += '\t\t\t\t<value_type>TEXT</value_type>\n';
										s += '\t\t\t\t<value_constraint>'+sEnum+'</value_constraint>\n';
										s += '\t\t\t\t<value_operator>IN</value_operator>\n';								
									} else if (lvd.GeneralValueType=="STRING") {
										s += '\t\t\t\t<value_type>TEXT</value_type>\n';
										s += '\t\t\t\t<value_operator>'+lvd.StringOp+'</value_operator>\n';
										s += '\t\t\t\t<value_constraint><![CDATA['+i2b2.h.Escape(lvd.ValueString)+']]></value_constraint>\n';
									} else if (lvd.GeneralValueType=="LARGESTRING") {
										if (lvd.DbOp) {
											s += '\t\t\t\t<value_operator>CONTAINS[database]</value_operator>\n';
										} else {
											s += '\t\t\t\t<value_operator>CONTAINS</value_operator>\n';											
										}
										s += '\t\t\t\t<value_type>LARGETEXT</value_type>\n';
										s += '\t\t\t\t<value_constraint><![CDATA['+i2b2.h.Escape(lvd.ValueString)+']]></value_constraint>\n';
									} else {
										s += '\t\t\t\t<value_type>'+lvd.GeneralValueType+'</value_type>\n';
										s += '\t\t\t\t<value_unit_of_measure>'+lvd.UnitsCtrl+'</value_unit_of_measure>\n';
										s += '\t\t\t\t<value_operator>'+lvd.NumericOp+'</value_operator>\n';
										if (lvd.NumericOp == 'BETWEEN') {
											s += '\t\t\t\t<value_constraint>'+i2b2.h.Escape(lvd.ValueLow)+' and '+i2b2.h.Escape(lvd.ValueHigh)+'</value_constraint>\n';
										} else {
											s += '\t\t\t\t<value_constraint>'+i2b2.h.Escape(lvd.Value)+'</value_constraint>\n';
										}
									}
									break;
								case "":
									break;
							}
							s += '\t\t\t</constrain_by_value>\n';
		return s;
	}
	

i2b2.Timeline.getResults = function() {

	if (i2b2.Timeline.model.dirtyResultsData) {
		// translate the concept XML for injection as PDO item XML
		var filterList = '';
		for (var i1=0; i1<i2b2.Timeline.model.concepts.length; i1++) {
			var t = i2b2.Timeline.model.concepts[i1].origData.xmlOrig;
		var cdata = {};
		cdata.level = i2b2.h.getXNodeVal(t, "level");
		cdata.key = i2b2.h.getXNodeVal(t, "key");
		cdata.tablename = i2b2.h.getXNodeVal(t, "tablename");
		cdata.dimcode = i2b2.h.getXNodeVal(t, "dimcode");
		cdata.synonym = i2b2.h.getXNodeVal(t, "synonym_cd");
			filterList +=
			'	<panel name="'+cdata.key+'">\n'+
			'		<panel_number>0</panel_number>\n'+
			'		<panel_accuracy_scale>0</panel_accuracy_scale>\n'+
			'		<invert>0</invert>\n'+
			'		<item>\n'+
			'			<hlevel>'+cdata.level+'</hlevel>\n'+
			'			<item_key>'+cdata.key+'</item_key>\n'+
			'			<dim_tablename>'+cdata.tablename+'</dim_tablename>\n'+
			'			<dim_dimcode>'+cdata.dimcode+'</dim_dimcode>\n'+
			'			<item_is_synonym>'+cdata.synonym+'</item_is_synonym>\n';
	
		
						if (i2b2.Timeline.model.concepts[i1].LabValues) {
							//s += '\t\t\t<constrain_by_value>\n';
							filterList += i2b2.Timeline.getValues( i2b2.Timeline.model.concepts[i1].LabValues);
						}
			
			filterList +='		</item>\n'+
			'	</panel>\n';
		}

		var pgstart = i2b2.Timeline.model.pgstart;
		var pgend = pgstart + i2b2.Timeline.model.pgsize - 1;
		var msg_filter = '<input_list>\n' +
			'	<patient_list max="'+pgend+'" min="'+pgstart+'">\n'+
			'		<patient_set_coll_id>'+i2b2.Timeline.model.prsRecord.sdxInfo.sdxKeyValue+'</patient_set_coll_id>\n'+
			'	</patient_list>\n'+
			'</input_list>\n'+
			'<filter_list>\n'+
				filterList+
			'</filter_list>\n'+
			'<output_option names="asattributes">\n'+
			'	<observation_set blob="false" onlykeys="false"/>\n'+
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

			var s = '';

			var patients = {};
			
			// get all the patient records
			var pData = i2b2.h.XPath(results.refXML, '//patient');

			for (var i1=0; i1<pData.length; i1++) {
				var patientID = i2b2.h.getXNodeVal(pData[i1], "patient_id");
				var patientName = '';
				patientName += 'Person_#';
				patientName += patientID;
				var sex_cd = i2b2.h.XPath(pData[i1], 'descendant-or-self::param[@column="sex_cd"]/text()');
				if (sex_cd.length) {
					patientName += '__';
					var sex_cd_val = sex_cd[0].nodeValue;
					if (sex_cd_val == 'M') {sex_cd_val = 'Male';}
					if (sex_cd_val == 'F') {sex_cd_val = 'Female';}
					if (sex_cd_val == 'U') {sex_cd_val = 'Unknown';}
					patientName += sex_cd_val;
				}
				var age_in_years = i2b2.h.XPath(pData[i1], 'descendant-or-self::param[@column="age_in_years_num"]/text()');
				if (age_in_years.length) {
					patientName += '__';
					patientName += age_in_years[0].nodeValue + 'yrold';
				}
				var race_cd = i2b2.h.XPath(pData[i1], 'descendant-or-self::param[@column="race_cd"]/text()');
				if (race_cd.length) {
					patientName += '__';
					patientName += race_cd[0].nodeValue.substring(0,1).toUpperCase() + race_cd[0].nodeValue.substring(1);
				}
				patients[patientID] = {};
				patients[patientID].name = patientName;
				patients[patientID].concepts = [];
				for (var i2=0; i2<i2b2.Timeline.model.concepts.length; i2++) {
					patients[patientID].concepts[i2] = [];
				}
			}
			
			// get all the observations
			var first_date = new Date('1/1/2500');
			var last_date = new Date('1/1/1500');
			var osData = i2b2.h.XPath(results.refXML, '//*[local-name() = "observation_set"]');
			for (var i1=0; i1<osData.length; i1++) {
				var oData = i2b2.h.XPath(osData[i1], 'descendant::observation');
				for (var i2=0; i2<oData.length; i2++) {
					var patientID = i2b2.h.getXNodeVal(oData[i2], "patient_id");
					var o = {};
					o.event_id = i2b2.h.getXNodeVal(oData[i2], "event_id");
					o.concept_cd = i2b2.h.getXNodeVal(oData[i2], "concept_cd");
					o.observer_id = i2b2.h.getXNodeVal(oData[i2], "observer_cd");
					o.start_date_key = i2b2.h.getXNodeVal(oData[i2], "start_date");
					var d = i2b2.h.getXNodeVal(oData[i2], "start_date");
					if (d) { d = d.match(/^[0-9\-]*/).toString(); }
					if (d) { d = d.replace(/-/g,'/'); }
					if (d) { d = new Date(Date.parse(d)); }
					if (d) { o.start_date = d; }
					d = i2b2.h.getXNodeVal(oData[i2], "end_date");
 					if (d === undefined || d == null || d.length <= 0){  
 						d = i2b2.h.getXNodeVal(oData[i2], "start_date");
 					}					
					if (d) { d = d.match(/^[0-9\-]*/).toString(); }
					if (d) { d = d.replace(/-/g,'/'); }
					if (d) { d = new Date(Date.parse(d)); }
					if (d) { o.end_date = d; }
					if ( o.concept_cd && o.start_date && o.end_date && patients[patientID]) {
						patients[patientID].concepts[i1].push(o);
						if (o.start_date < first_date) {first_date = o.start_date;}
						if (o.end_date > last_date) {last_date = o.end_date;}
					}
				}
			}
			
			//i2b2.Timeline.model.patients = patients;
			
			var first_time = first_date.getTime()*1.0;
			var last_time = last_date.getTime()*1.0;
			var lf = last_time - first_time + 1;
			
			i2b2.Timeline.model.first_time = first_time;
			i2b2.Timeline.model.last_time = last_time;
			
			var observation_keys = new Array();
			for (var patientID in patients) {
				s += '<div class="ptBox">';
				s += '<div class="ptName">' + patients[patientID].name + '</div>';
				s += '<table class="ptData">';
				for (i1=0; i1<i2b2.Timeline.model.concepts.length; i1++) {
					if (patients[patientID].concepts[i1].length) {
						s += '<tr>';
						s += '<td class="ptPanel">' + i2b2.h.Escape(i2b2.Timeline.model.concepts[i1].sdxInfo.sdxDisplayName) + '</td>';
						s += '<td class="ptObsTD" valign="top"><div class="spacer">&nbsp;</div>';
						s += '<div class="ptObsDIV">';
						s += '<div class="ptObsBack"></div>';
						s += '<div class="ptObsLine"></div>';
						s += '<div class="ptObs">';
						s += '<div class="ptObsZoom">';
						for (i2=0; i2<patients[patientID].concepts[i1].length; i2++) {
							var d1 = patients[patientID].concepts[i1][i2].start_date;
							var d2 = patients[patientID].concepts[i1][i2].end_date;
							var w = (d1.getTime() - first_time)/lf;
							var w2 = (d2.getTime() - first_time)/lf;
							// used to lookup the primary DB key when an observation is clicked
							var obs_keyval = {
								event_id: patients[patientID].concepts[i1][i2].event_id,
								patient_id: patientID,
								concept_id: patients[patientID].concepts[i1][i2].concept_cd,
								observer_id: patients[patientID].concepts[i1][i2].observer_id,
								start_date_key: patients[patientID].concepts[i1][i2].start_date_key
							};
							observation_keys.push(obs_keyval);
							var obs_key = observation_keys.length - 1;

							if ( (w<=1) && (w2<=1) ) {
								if (w2 > w) {
									s += '<div class="ptOb2" style="left:' + (100*w) + '%;width:' + (100*(w2-w)) + '%;"></div>';
								}
								if (w > .99) { w = .98;}
								s += '<a id="TIMELINEOBS-'+obs_key+'" title="'+ i2b2.h.Escape(obs_keyval.concept_id) +'" href="Javascript:i2b2.Timeline.showObservation('+ obs_key +');" class="ptOb" style="left:' + (100*w) + '%;"></a>';
							}
						}
						s += '</div>';
						s += '</div>';
						s += '</div>';
						s += '</td>';
						s += '</tr>';
					}
				}
				
				// save the DB key lookup table
				i2b2.Timeline.model.observation_PKs = observation_keys;
				
				
				s += '</table>';
				s += '</div>';
			}

			i2b2.Timeline.model.zoomScale = 1.0;
			i2b2.Timeline.model.zoomPan = 1.0;
			i2b2.Timeline.updateZoomScaleLabels();
			
			$$("DIV#Timeline-mainDiv DIV#Timeline-TABS DIV.results-timeline")[0].innerHTML = s;

			// optimization - only requery when the input data is changed
			i2b2.Timeline.model.dirtyResultsData = false;		
		}
		
		// AJAX CALL USING THE EXISTING CRC CELL COMMUNICATOR
		i2b2.CRC.ajax.getPDO_fromInputList("Plugin:Timeline", {PDO_Request:msg_filter}, scopedCallback);
	}
}
