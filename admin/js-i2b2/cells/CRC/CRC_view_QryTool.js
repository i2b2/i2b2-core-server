/**
 * @projectDescription	View controller for CRC Query Tool window.
 * @inherits 	i2b2.CRC.view
 * @namespace	i2b2.CRC.view.QT
 * @author		Nick Benik, Griffin Weber MD PhD
 * @version 	1.3
 * ----------------------------------------------------------------------------------------
 * updated 9-15-08: RC4 launch [Nick Benik] 
 */
console.group('Load & Execute component file: CRC > view > Main');
console.time('execute time');

 
// create and save the screen objects
i2b2.CRC.view['QT'] = new i2b2Base_cellViewController(i2b2.CRC, 'QT');

var queryTimingButton;
// define the option functions
// ================================================================================================== //
i2b2.CRC.view.QT.showOptions = function(subScreen) {
	if (!this.modalOptions) {
		var handleSubmit = function() {
			// submit value(s)
			if(this.submit()) {
				var tmpValue = parseInt($('QryTimeout').value,10);
				i2b2.CRC.view['QT'].params.queryTimeout = tmpValue;
	//			var tmpValue = parseInt($('MaxChldDisp').value,10);
	//			i2b2.CRC.view['QT'].params.maxChildren = tmpValue;
			}
		}
		var handleCancel = function() {
			this.cancel();
		}
		this.modalOptions = new YAHOO.widget.SimpleDialog("optionsQT",
		{ width : "400px", 
			fixedcenter : true, 
			constraintoviewport : true, 
			modal: true,
			zindex: 700,
			buttons : [ { text:"OK", handler:handleSubmit, isDefault:true }, 
				    { text:"Cancel", handler:handleCancel } ] 
		} ); 
		$('optionsQT').show();
		this.modalOptions.validate = function() {
			// now process the form data
			var msgError = '';
	//		var tmpValue = parseInt($('MaxChldDisp').value,10);
	//		if (!isNaN(tmpValue) && tmpValue <= 0) {
	//			msgError += "The max number of Children to display must be a whole number larger then zero.\n";
	//		}
			var tmpValue = parseInt($('QryTimeout').value,10);
			if (!isNaN(tmpValue) && tmpValue <= 0) {
				msgError += "The the query timeout period must be a whole number larger then zero.\n";
			}
			if (msgError) {
				alert(msgError);
				return false;
			}
			return true;
		};
		this.modalOptions.render(document.body);
	}
	this.modalOptions.show();
	// load settings
//	$('MaxChldDisp').value = this.params.maxChildren;
	$('QryTimeout').value = this.params.queryTimeout;
}

// ================================================================================================== //
i2b2.CRC.view.QT.ContextMenuPreprocess = function(p_oEvent) {
	var clickId = false;
	var clickPanel = false;
	var isDone = false;
	var currentNode = this.contextEventTarget;
	var doNotShow = false;
	
	while (!isDone) {
		// save the first DOM node found with an ID
		if (currentNode.id && !clickId)  {
			clickId = currentNode.id;
		}
		// save and exit when we find the linkback to the panel controller
		if (currentNode.linkbackPanelController) {
			// we are at the tree root... 
			var clickPanel = currentNode.linkbackPanelController;
			isDone = true;
		}
		if (currentNode.parentNode) {
			currentNode = currentNode.parentNode;
		} else {
			// we have recursed up the tree to the window/document DOM...
			isDone = true;
		}
	}
	if (!clickId || !clickPanel) {
		// something is missing, exit
		this.cancel();
		return;
	}
	// see if the ID maps back to a treenode with SDX data
	var tvNode = clickPanel.yuiTree.getNodeByProperty('nodeid', clickId);
	if (tvNode) {
		if (!Object.isUndefined(tvNode.data.i2b2_SDX)) {
			// Make sure the clicked node is at the root level
			if (tvNode.parent == clickPanel.yuiTree.getRoot()) {
				if (p_oEvent == "beforeShow") {
					i2b2.CRC.view.QT.contextRecord = tvNode.data.i2b2_SDX;
					i2b2.CRC.view.QT.contextPanelCtrlr = clickPanel;
					// custom build the context menu according to the concept that was clicked
					var mil = [];
					var op = i2b2.CRC.view.QT;
					// all nodes can be deleted
					mil.push( { text: "Delete", onclick: { fn: op.ContextMenuRouter, obj: 'delete' }} );
					if (i2b2.CRC.view.QT.contextRecord.origData.isModifier) {
						
						//Get the blob for this now.
					//	if (i2b2.CRC.view.QT.contextRecord.origData.xmlOrig != null) {
							var cdetails = i2b2.ONT.ajax.GetModifierInfo("CRC:QueryTool", {modifier_applied_path:i2b2.CRC.view.QT.contextRecord.origData.applied_path, modifier_key_value:i2b2.CRC.view.QT.contextRecord.origData.key, ont_synonym_records: true, ont_hidden_records: true} );
							// this is what comes out of the old AJAX call
							if (isActiveXSupported) {
								//Internet Explorer
								xmlDocRet = new ActiveXObject("Microsoft.XMLDOM");
								xmlDocRet.async = "false";
								xmlDocRet.loadXML(cdetails.msgResponse);
								xmlDocRet.setProperty("SelectionLanguage", "XPath");
								var c = i2b2.h.XPath(xmlDocRet, 'descendant::modifier');						
							} else {					 
								var c = i2b2.h.XPath(cdetails.refXML, 'descendant::modifier');
							}
							if (c.length > 0) {
									i2b2.CRC.view.QT.contextRecord.origData.xmlOrig = c[0];
							}
					//	}
										
						
						var lvMetaDatas1 = i2b2.h.XPath(i2b2.CRC.view.QT.contextRecord.origData.xmlOrig, 'metadataxml/ValueMetadata[string-length(Version)>0]');
						if (lvMetaDatas1.length > 0) {
						
							mil.push( { text: "Set Modifier Value", onclick: { fn: op.ContextMenuRouter, obj: 'setmodifier' }} );					
						}
						var lvMetaDatas2 = i2b2.h.XPath(i2b2.CRC.view.QT.contextRecord.origData.parent.xmlOrig, 'metadataxml/ValueMetadata[string-length(Version)>0]');
						if (lvMetaDatas2.length > 0) {
							mil.push( { text: "Set Value...", onclick: { fn: op.ContextMenuRouter, obj: 'labvalues' }} );
						}
						
					} else {
						// For lab tests...
						
						if (!Object.isUndefined(i2b2.CRC.view.QT.contextRecord.origData.key)) {
							var cdetails = i2b2.ONT.ajax.GetTermInfo("CRC:QueryTool", {concept_key_value:i2b2.CRC.view.QT.contextRecord.origData.key, ont_synonym_records: true, ont_hidden_records: true} );
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
									i2b2.CRC.view.QT.contextRecord.origData.xmlOrig = c[0];
							}
						}

						var lvMetaDatas = i2b2.h.XPath(i2b2.CRC.view.QT.contextRecord.origData.xmlOrig, 'metadataxml/ValueMetadata[string-length(Version)>0]');
						if (lvMetaDatas.length > 0) {
							mil.push( { text: "Set Value...", onclick: { fn: op.ContextMenuRouter, obj: 'labvalues' }} );
						}
					}
					i2b2.CRC.view.QT.ContextMenu.clearContent();
					i2b2.CRC.view.QT.ContextMenu.addItems(mil);
					i2b2.CRC.view.QT.ContextMenu.render();
				}
			} else {
				// not root level node
				doNotShow = true;
			}
		} else {
			// no SDX data
			doNotShow = true;
		}
	} else {
		// not a treenode
		doNotShow = true;
	}
	if (doNotShow) {
		if (p_oEvent == "beforeShow") { i2b2.CRC.view.QT.ContextMenu.clearContent(); }
		if (p_oEvent == "triggerContextMenu") { this.cancel(); }
	}
}

// ================================================================================================== //
i2b2.CRC.view.QT.ContextMenuRouter = function(a, b, actionName) {
	// this is used to route the event to the correct handler
	var op = i2b2.CRC.view.QT;  // object path
	var cdat = { // context node data
		data: op.contextRecord,
		ctrlr: op.contextPanelCtrlr
	};
	// route accordingly
	switch(actionName) {
		case "delete":
			// delete item from the panel
			cdat.ctrlr._deleteConcept(cdat.data.renderData.htmlID, cdat.data);
			break;
		case "labvalues":
			cdat.ctrlr.showLabValues(cdat.data.sdxInfo.sdxKeyValue, cdat.data);
			break;
		case "setmodifier":
			cdat.ctrlr.showModValues(cdat.data.sdxInfo.sdxKeyValue, cdat.data);
			break;
		default:
			alert('context event was not found for event "'+actionName+'"');
	}
 }

//================================================================================================== //

i2b2.CRC.view.QT.enableSameTiming = function() {

		if (YAHOO.util.Dom.inDocument(queryTimingButton.getMenu().element)) {

		var t = queryTimingButton.getMenu().getItems();
		if (t.length == 2) {
				//	queryTimingButton.getMenu().clearContent();
				//	queryTimingButton.getMenu().addItems([ 	 
				//						{ text: "Treat Independently", value: "ANY"}]);	
				//	queryTimingButton.getMenu().addItems([ 	 
				//						{ text: "Selected groups occur in the same financial encounter", value: "SAMEVISIT" }]);	 
					queryTimingButton.getMenu().addItems([ 	 
										{ text: "Items Instance will be the samer", value: "SAMEINSTANCENUM" }]);	 
					queryTimingButton.getMenu().render();
		}
		} else {
			queryTimingButton.itemData =[{ text: "Treat Independently", value: "ANY"},
										{ text: "Selected groups occur in the same financial encounter", value: "SAMEVISIT"},
										{text: "Items Instance will be the same", value: "SAMEINSTANCENUM" }];
				}
}

i2b2.CRC.view.QT.clearTemportal = function() {
	i2b2.CRC.view.QT.setQueryTiming("ANY");

	var t = defineTemporalButton.getMenu().getItems();
	if (t.length > 4) {

		defineTemporalButton.getMenu().clearContent();
		defineTemporalButton.getMenu().addItems([ 	 
		 										{ text: "Population in which events occur" , value: "0" }]);		 
		defineTemporalButton.getMenu().addItems([ 	 
			 										{ text: "Event 1" , value: "1" }]);	
		defineTemporalButton.getMenu().addItems([ 	 
			 										{ text: "Event 2" , value: "2" }]);	 
		defineTemporalButton.getMenu().addItems([ 	 
			 										{ text: "Define order of events" , value: "BUILDER" }]);	
		defineTemporalButton.getMenu().render();			

	}
	i2b2.CRC.view.QT.ResizeHeight();

}
// ================================================================================================== //

i2b2.CRC.view.QT.setQueryTiming = function(sText) {
	
	//TODO cleanup
	
		if (YAHOO.util.Dom.inDocument(queryTimingButton.getMenu().element)) {

					queryTimingButton.getMenu().clearContent();
					queryTimingButton.getMenu().addItems([ 	 
										{ text: "Treat Independently", value: "ANY"}]);	
					queryTimingButton.getMenu().addItems([ 	 
										{ text: "Selected groups occur in the same financial encounter", value: "SAMEVISIT" }]);
					queryTimingButton.getMenu().addItems([
										{ text: "Define sequence of Events", value: "TEMPORAL" }]);									
					if (sText == "SAMEINSTANCENUM") {
										queryTimingButton.getMenu().addItems([ 	 
										{ text: "Items Instance will be the same", value: "SAMEINSTANCENUM" }]);	 
	
						
					}
					queryTimingButton.getMenu().render();
		}  else {
			
			if (sText =="TEMPORAL") {
					queryTimingButton.set("label",  "Define sequence of Events");	
					
										i2b2.CRC.ctrlr.QT.queryTiming = "TEMPORAL";
					$('defineTemporalBar').show();	
	
			} else if (sText =="SAMEVISIT") {
					queryTimingButton.set("label",  "Selected groups occur in the same financial encounter");	
			} else if (sText == "ANY") {
					queryTimingButton.set("label",  "Treat Independently");	
					$('defineTemporalBar').hide();					
			}

		}
	
		queryTimingButton.getMenu().render();
		var menu = queryTimingButton.getMenu();

		if (sText == "SAMEINSTANCENUM" )
		{
			var item = menu.getItem(3);
		} else if (sText == "SAMEVISIT" )
		{
			var item = menu.getItem(1);
		} else if (sText == "TEMPORAL" )
		{
			var item = menu.getItem(2);
		} else
		{
			var item = menu.getItem(0);		
		}
		queryTimingButton.set("selectedMenuItem", item);
		
}

//================================================================================================== //

i2b2.CRC.view.QT.setPanelTiming = function(panelNum, sText) {
	if (panelNum > 3) {return}
	if (sText == "SAMEVISIT" )
	{
		$("queryPanelTimingB" + (panelNum) +  "-button").innerHTML = "Occurs in Same Encounter";	
		i2b2.CRC.ctrlr.QT.panelControllers[panelNum - 1].doTiming(sText);
		i2b2.CRC.ctrlr.QT.panelControllers[panelNum - 1].refTiming.set('disabled', false);	
	} else if (sText == "SAMEINSTANCENUM") {
		$("queryPanelTimingB" + (panelNum) +  "-button").innerHTML = "Items Instance will be the same";	
		i2b2.CRC.ctrlr.QT.panelControllers[panelNum - 1].doTiming(sText);
		i2b2.CRC.ctrlr.QT.panelControllers[panelNum - 1].refTiming.set('disabled', false);	
	} else {
		$("queryPanelTimingB" + (panelNum) +  "-button").innerHTML = "Treat Independently";	
		i2b2.CRC.ctrlr.QT.panelControllers[panelNum - 1].doTiming(sText);
	}
}

// ================================================================================================== //
i2b2.CRC.view.QT.ZoomView = function() {
	i2b2.hive.MasterView.toggleZoomWindow("QT");
}

// ================================================================================================== //
i2b2.CRC.view.QT.Resize = function(e) {
	//var ds = document.viewport.getDimensions();
	//var w = ds.width;
	//var h = ds.height
    var w =  window.innerWidth || (window.document.documentElement.clientWidth || window.document.body.clientWidth);
    var h =  window.innerHeight || (window.document.documentElement.clientHeight || window.document.body.clientHeight);

	
	if (w < 840) {w = 840;}
	if (h < 517) {h = 517;}
	
	// resize our visual components
	//var queryToolWidth = ds.width * 0.6;
	//$('crcQueryToolBox').style.left = w-queryToolWidth;
	//debugOnScreen("crcQueryToolBox.width = " + queryToolWidth );
	
	$('crcQueryToolBox').style.left = w-550;
	if (i2b2.WORK && i2b2.WORK.isLoaded) {
		var z = h - 438; //392 + 44 - 17 - 25;
		if (i2b2.CRC.view.QT.isZoomed) { z += 196 - 44; }	
	} else {
		var z = h - 348;
		if (i2b2.CRC.view.QT.isZoomed) { z += 196; }
	}
	// display the topic selector bar if we are in SHRINE-mode
 	if (i2b2.h.isSHRINE()) {
		$('queryTopicPanel').show();
		z = z - 28;
	}
			
	$('QPD1').style.height = z;
	$('QPD2').style.height = z;
	$('QPD3').style.height = z;	
	$('temporalbuilders').style.height = z + 50;	
}
//YAHOO.util.Event.addListener(window, "resize", i2b2.CRC.view.QT.Resize, i2b2.CRC.view.QT); // tdw9


//================================================================================================== //
i2b2.CRC.view.QT.splitterDragged = function()
{
	//var viewPortDim = document.viewport.getDimensions();
	var w =  window.innerWidth || (window.document.documentElement.clientWidth || window.document.body.clientWidth);

	var splitter = $( i2b2.hive.mySplitter.name );	
	var CRCQT = $("crcQueryToolBox");
	var CRCQTBodyBox = $("crcQueryToolBox.bodyBox");
	
	var CRCQueryName 			= $("queryName");
	var CRCQueryNameBar 		= $("queryNameBar");
	var temporalConstraintBar 	= $("temporalConstraintBar");
	var defineTemporalBar 		= $("defineTemporalBar");
	var temporalConstraintLabel = $("temporalConstraintLabel");
	var temporalConstraintDiv	= $("temporalConstraintDiv");
	var queryTiming				= $("queryTiming");
	var queryTimingButton		= $("queryTiming-button");
	
	var defineTemporal				= $("defineTemporal");
	var defineTemporalButton		= $("defineTemporal-button");

	var CRCQueryPanels 			= $("crcQryToolPanels");
	var CRCinnerQueryPanel		= $("crc.innerQueryPanel");
	var CRCtemoralBuilder		= $("crc.temoralBuilder");
	var basicWidth					= parseInt(w) - parseInt(splitter.style.left) - parseInt(splitter.offsetWidth);

	/* Title, buttons, and panels */		
	CRCQT.style.left				= parseInt(splitter.offsetWidth) + parseInt(splitter.style.left) + 3 + "px";
	CRCQT.style.width				= Math.max(basicWidth - 24, 0) + "px";
	CRCQTBodyBox.style.width 		= Math.max(basicWidth - 41, 0) + "px";
	
	CRCQueryNameBar.style.width 		= Math.max(basicWidth - 38, 0) + "px";
	temporalConstraintBar.style.width 	= Math.max(basicWidth - 38, 0) + "px";
	defineTemporalBar.style.width 	= Math.max(basicWidth - 38, 0) + "px";
	temporalConstraintDiv.style.width 	= Math.max( parseInt(temporalConstraintBar.style.width) - parseInt(temporalConstraintLabel.style.width)-2, 0) + "px";
	queryTimingButton.style.width 		= Math.max( parseInt(temporalConstraintBar.style.width) - 250,0) + "px";
	defineTemporalButton.style.width 		= Math.max( parseInt(temporalConstraintBar.style.width) - 250,0) + "px";
	//parseInt(temporalConstraintLabel.style.width)-23, 0) + "px";
	
	CRCQueryName.style.width			= Math.max(basicWidth - 128, 0) + "px"; // use max to avoid negative width
	
	CRCQueryPanels.style.width		= Math.max(basicWidth - 30, 0) + "px";
	CRCinnerQueryPanel.style.width	= Math.max(basicWidth - 36, 0) + "px";
	CRCtemoralBuilder.style.width	= Math.max(basicWidth - 36, 0) + "px";
	
	
	var panelWidth = (basicWidth - 36)/3 - 4;
	
	var panels = CRCinnerQueryPanel.childNodes;
	var panelArray = new Array(3);
	var panelCount = 0;
	for ( var i = 0; i < panels.length; i++ )
	{
		if ( panels[i].className === "qryPanel")
		{
			panels[i].style.width = Math.max(panelWidth, 0) + "px";
			var nodes = panels[i].childNodes;
			for ( var j = 0; j < nodes.length; j++ )
			{
				if (nodes[j].className === "qryPanelTitle")
					nodes[j].style.width = Math.max(panelWidth - 2, 0) + "px";
				else if ( nodes[j].className === "qryPanelButtonBar" )
				{
					nodes[j].style.width = Math.max(panelWidth, 0) + "px";
					var buttons = nodes[j].childNodes;
					for ( var k = 0; k < buttons.length; k++)
					{
						if ( buttons[k].className === "qryButtonOccurs")
							buttons[k].style.width = Math.max(panelWidth - 88, 0) + "px";	
					}
				}
				else if ( nodes[j].className === "qryPanelTiming" )
				{
					nodes[j].style.width = Math.max(panelWidth, 0) + "px";
					var queryPanelTimingChildren = nodes[j].childNodes;
					for ( var k = 0; k < queryPanelTimingChildren.length; k++)
					{
						if ( queryPanelTimingChildren[k].style == null )
							continue;
						queryPanelTimingChildren[k].style.width = Math.max(panelWidth - 4, 0) + "px";
					}
					//handle the special "queryPanelTimingB1"
					var queryPanelTimingB1 = $("queryPanelTimingB1");
					queryPanelTimingB1.style.width = Math.max(panelWidth - 4, 0) + "px";
					}
				else if ( nodes[j].className === "queryPanel" || nodes[j].className === "queryPanel queryPanelDisabled" ) // QueryPanel or disabled QueryPanel
					nodes[j].style.width =  Math.max(panelWidth - 8, 0) + "px";
			}
			panelArray[panelCount] = panels[i];
			panelCount++;
		}
		else
			continue;
	}
	
	/* Deal with Footer and its components */	
	var footer = $("qryToolFooter");	// footer
	var printBox = $('printQueryBox');	// print query
	var groupCount = $("groupCount");	// # of groups
	var scrollBox = $("scrollBox");		// scroll control
	
	footer.style.width = Math.max(basicWidth - 40, 0) + "px"; // adjust footer width	
	groupCount.style.width =  Math.max(parseInt(footer.style.width) - (printBox.offsetLeft + printBox.offsetWidth) - scrollBox.offsetWidth - 5, 0) + "px"; // adjust groupCount width
	
	/* Deal with Baloons */
	var baloonBox	= $("queryBalloonBox");
	var baloons = baloonBox.getElementsByTagName("div");
	
	for ( var i = 0; i < baloons.length; i++ )
	{
		if ( i%2 === 0) // even baloons 
		{
			var index = i/2;
			if ( index < baloons.length)
				baloons[i].style.left	= panelArray[index].offsetLeft + parseInt(panelArray[index].style.width)/2 - 35 + "px";			
		}
		else
		{
			var index = Math.floor(i/2);
			baloons[i].style.left	= panelArray[index].offsetLeft + parseInt(panelArray[index].style.width) - 22.5 + "px";
		}
	}
}

//================================================================================================== //
i2b2.CRC.view.QT.ResizeHeight = function() {
	//var ds = document.viewport.getDimensions();
	//var h = ds.height;
	//var h = window.document.documentElement.clientHeight;
	var h = window.innerHeight || (window.document.documentElement.clientHeight || window.document.body.clientHeight);
	
	if (h < 517) {h = 517;}
	// resize our visual components
	if (i2b2.WORK && i2b2.WORK.isLoaded) {
		var z = h - 438;
		if (i2b2.CRC.view.QT.isZoomed) { z += 196 - 44; }	
	} else {
		var z = h - 472;
		if (i2b2.CRC.view.QT.isZoomed) { z += 196; }
	}
	// display the topic selector bar if we are in SHRINE-mode
 	if (i2b2.h.isSHRINE()) {
		$('queryTopicPanel').show();
		z = z - 28;
	}
	
	if ($('defineTemporalBar').style.display === '')			
		z = z - 20;
	$('QPD1').style.height = z;
	$('QPD2').style.height = z;
	$('QPD3').style.height = z;	

	
	$('temporalbuilders').style.height = z + 50;	

}

i2b2.CRC.view.QT.addNewTemporalGroup = function() {

					i2b2.CRC.ctrlr.QT.temporalGroup = i2b2.CRC.model.queryCurrent.panels.length;
					//i2b2.CRC.ctrlr.QT.temporalGroup = i2b2.CRC.ctrlr.QT.temporalGroup + 1;
					
					if (YAHOO.util.Dom.inDocument(defineTemporalButton.getMenu().element)) {					
						defineTemporalButton.getMenu().addItems([ 	 
										{ text: "Event " + (i2b2.CRC.ctrlr.QT.temporalGroup), value: i2b2.CRC.ctrlr.QT.temporalGroup}]);	 
						defineTemporalButton.getMenu().render();	
					} else {
						var aMenuItemData=[];
						 aMenuItemData[0] = {text: "Event " + (i2b2.CRC.ctrlr.QT.temporalGroup), value: i2b2.CRC.ctrlr.QT.temporalGroup} ;
						defineTemporalButton.getMenu().itemData = aMenuItemData  ;
						}
					
					i2b2.CRC.model.queryCurrent.panels[i2b2.CRC.ctrlr.QT.temporalGroup] = {};
					this.yuiTree = new YAHOO.widget.TreeView("QPD1");
					i2b2.CRC.ctrlr.QT.panelAdd(this.yuiTree);
					i2b2.CRC.ctrlr.QT._redrawAllPanels();	
					
					//Add to define a query	
					var select = document.getElementById("instancevent1[0]");
					select.options[select.options.length] = new Option( 'Event '+i2b2.CRC.ctrlr.QT.temporalGroup, i2b2.CRC.ctrlr.QT.temporalGroup);
	
					 select = document.getElementById("instancevent2[0]");
					select.options[select.options.length] = new Option( 'Event '+i2b2.CRC.ctrlr.QT.temporalGroup, i2b2.CRC.ctrlr.QT.temporalGroup);

}

// This is done once the entire cell has been loaded
console.info("SUBSCRIBED TO i2b2.events.afterCellInit");
i2b2.events.afterCellInit.subscribe(
	(function(en,co) {
		if (co[0].cellCode=='CRC') {
// ================================================================================================== //
			console.debug('[EVENT CAPTURED i2b2.events.afterCellInit]');
			//Update the result types from ajax call
	var scopedCallback = new i2b2_scopedCallback();
		scopedCallback.callback = function(results) {
		//var cl_onCompleteCB = onCompleteCallback;
		// THIS function is used to process the AJAX results of the getChild call
		//		results data object contains the following attributes:
		//			refXML: xmlDomObject <--- for data processing
		//			msgRequest: xml (string)
		//			msgResponse: xml (string)
		//			error: boolean
		//			errorStatus: string [only with error=true]
		//			errorMsg: string [only with error=true]
	
		var retMsg = {
			error: results.error,
			msgRequest: results.msgRequest,
			msgResponse: results.msgResponse,
			msgUrl: results.msgUrl,
			results: null
		};
		var retChildren = [];

		// extract records from XML msg
		var newHTML = "";
		var ps = results.refXML.getElementsByTagName('query_result_type');
		for(var i1=0; i1<ps.length; i1++) {
			var o = new Object;
			o.result_type_id = i2b2.h.getXNodeVal(ps[i1],'result_type_id');
			o.name = i2b2.h.getXNodeVal(ps[i1],'name');

			var checked = "";
			switch(o.name) {
				case "PATIENT_COUNT_XML":
				//	o.name = "PRS";
					checked = "checked=\"checked\"";
					break;
				//case "PATIENT_ENCOUNTER_SET":
				//	o.name = "ENS";
				//	checked = "checked=\"checked\"";
				//	break;
				//case "PATIENT_COUNT_XML":
				//	o.name = "PRC";
				//	checked = "checked=\"checked\"";
				//	break;
			}

			o.display_type = i2b2.h.getXNodeVal(ps[i1],'display_type');
			o.visual_attribute_type = i2b2.h.getXNodeVal(ps[i1],'visual_attribute_type');
			o.description = i2b2.h.getXNodeVal(ps[i1],'description');
			// need to process param columns 
			//o. = i2b2.h.getXNodeVal(ps[i1],'');
			//this.model.events.push(o);
			if (o.visual_attribute_type == "LA") {
				newHTML += 	"			<div id=\"crcDlgResultOutput" + o.name + "\"><input type=\"checkbox\" class=\"chkQueryType\" name=\"queryType\" value=\"" + o.name + "\" " + checked + "/> " + o.description + "</div>";
			}
		}		
		
		$('dialogQryRunResultType').innerHTML = newHTML;
	}
	
		i2b2.CRC.ajax.getQRY_getResultType("CRC:SDX:PatientRecordSet", null, scopedCallback);

			
			
			
			
			// register the query panels as valid DragDrop targets for Ontology Concepts (CONCPT) and query master (QM) objects
			var op_trgt = {dropTarget:true};
			i2b2.sdx.Master.AttachType('QPD1', 'CONCPT', op_trgt);
			i2b2.sdx.Master.AttachType('QPD2', 'CONCPT', op_trgt);
			i2b2.sdx.Master.AttachType('QPD3', 'CONCPT', op_trgt);
			i2b2.sdx.Master.AttachType('QPD1', 'ENS', op_trgt);
			i2b2.sdx.Master.AttachType('QPD2', 'ENS', op_trgt);
			i2b2.sdx.Master.AttachType('QPD3', 'ENS', op_trgt);
			i2b2.sdx.Master.AttachType('QPD1', 'PRS', op_trgt);
			i2b2.sdx.Master.AttachType('QPD2', 'PRS', op_trgt);
			i2b2.sdx.Master.AttachType('QPD3', 'PRS', op_trgt);
			i2b2.sdx.Master.AttachType('QPD1', 'QM', op_trgt);
			i2b2.sdx.Master.AttachType('QPD2', 'QM', op_trgt);
			i2b2.sdx.Master.AttachType('QPD3', 'QM', op_trgt);
			i2b2.sdx.Master.AttachType('queryName', 'QM', op_trgt);
			
			//======================= <Define Hover Handlers> =======================
			var funcHovOverQM = function(e, id, ddProxy) {
				var el = $(id);
				 // apply DragDrop targeting CCS
				var targets = YAHOO.util.DDM.getRelated(ddProxy, true);
				for (var i=0; i<targets.length; i++) {
					Element.addClassName(targets[i]._domRef,"ddQMTarget");
				} 
			}
			var funcHovOutQM = function(e, id, ddProxy) {
				var el = $(id);
				 // apply DragDrop targeting CCS
				var targets = YAHOO.util.DDM.getRelated(ddProxy, true);
				for (var i=0; i<targets.length; i++) {
					Element.removeClassName(targets[i]._domRef,"ddQMTarget");
				} 
			}
			var funcHovOverCONCPT = function(e, id, ddProxy) {
				var el = $(id);
				if (Object.isUndefined(el.linkbackPanelController)) { return false;}
				var panelController = el.linkbackPanelController;
				// see if the panel controller is enabled
				if (panelController.isActive == 'Y') {										
					Element.addClassName(panelController.refDispContents,'ddCONCPTTarget');
				}
			}
			var funcHovOutCONCPT = function(e, id, ddProxy) {
				var el = $(id);
				if (Object.isUndefined(el.linkbackPanelController)) { return false;}
				var panelController = el.linkbackPanelController;
				// see if the panel controller is enabled
				if (panelController.isActive == 'Y') {
					Element.removeClassName(panelController.refDispContents,'ddCONCPTTarget');
				}
			}
			i2b2.sdx.Master.setHandlerCustom('QPD1', 'QM', 'onHoverOut', funcHovOutCONCPT);
			i2b2.sdx.Master.setHandlerCustom('QPD2', 'QM', 'onHoverOut', funcHovOutCONCPT);
			i2b2.sdx.Master.setHandlerCustom('QPD3', 'QM', 'onHoverOut', funcHovOutCONCPT);
			i2b2.sdx.Master.setHandlerCustom('queryName', 'QM', 'onHoverOut', funcHovOutQM);
			i2b2.sdx.Master.setHandlerCustom('QPD1', 'QM', 'onHoverOver', funcHovOverCONCPT);
			i2b2.sdx.Master.setHandlerCustom('QPD2', 'QM', 'onHoverOver', funcHovOverCONCPT);
			i2b2.sdx.Master.setHandlerCustom('QPD3', 'QM', 'onHoverOver', funcHovOverCONCPT);
			i2b2.sdx.Master.setHandlerCustom('queryName', 'QM', 'onHoverOver', funcHovOverQM);
			i2b2.sdx.Master.setHandlerCustom('QPD1', 'CONCPT', 'onHoverOut', funcHovOutCONCPT);
			i2b2.sdx.Master.setHandlerCustom('QPD2', 'CONCPT', 'onHoverOut', funcHovOutCONCPT);
			i2b2.sdx.Master.setHandlerCustom('QPD3', 'CONCPT', 'onHoverOut', funcHovOutCONCPT);
			i2b2.sdx.Master.setHandlerCustom('QPD1', 'CONCPT', 'onHoverOver', funcHovOverCONCPT);
			i2b2.sdx.Master.setHandlerCustom('QPD2', 'CONCPT', 'onHoverOver', funcHovOverCONCPT);
			i2b2.sdx.Master.setHandlerCustom('QPD3', 'CONCPT', 'onHoverOver', funcHovOverCONCPT);
			i2b2.sdx.Master.setHandlerCustom('QPD1', 'ENS', 'onHoverOut', funcHovOutCONCPT);
			i2b2.sdx.Master.setHandlerCustom('QPD2', 'ENS', 'onHoverOut', funcHovOutCONCPT);
			i2b2.sdx.Master.setHandlerCustom('QPD3', 'ENS', 'onHoverOut', funcHovOutCONCPT);
			i2b2.sdx.Master.setHandlerCustom('QPD1', 'ENS', 'onHoverOver', funcHovOverCONCPT);
			i2b2.sdx.Master.setHandlerCustom('QPD2', 'ENS', 'onHoverOver', funcHovOverCONCPT);
			i2b2.sdx.Master.setHandlerCustom('QPD3', 'ENS', 'onHoverOver', funcHovOverCONCPT);			
			i2b2.sdx.Master.setHandlerCustom('QPD1', 'PRS', 'onHoverOut', funcHovOutCONCPT);
			i2b2.sdx.Master.setHandlerCustom('QPD2', 'PRS', 'onHoverOut', funcHovOutCONCPT);
			i2b2.sdx.Master.setHandlerCustom('QPD3', 'PRS', 'onHoverOut', funcHovOutCONCPT);
			i2b2.sdx.Master.setHandlerCustom('QPD1', 'PRS', 'onHoverOver', funcHovOverCONCPT);
			i2b2.sdx.Master.setHandlerCustom('QPD2', 'PRS', 'onHoverOver', funcHovOverCONCPT);
			i2b2.sdx.Master.setHandlerCustom('QPD3', 'PRS', 'onHoverOver', funcHovOverCONCPT);			
			//======================= <Define Drop Handlers> =======================

			//======================= <Define Drop Handlers> =======================
			i2b2.sdx.Master.setHandlerCustom('QPD1', 'CONCPT', 'DropHandler', (function(sdxData) { 
				sdxData = sdxData[0];	// only interested in first record
				var t = i2b2.CRC.ctrlr.QT.panelControllers[0];
				if (t.isActive=="Y") { t.doDrop(sdxData); }
			}));
			i2b2.sdx.Master.setHandlerCustom('QPD2', 'CONCPT', 'DropHandler', (function(sdxData) { 
				sdxData = sdxData[0];	// only interested in first record
				var t = i2b2.CRC.ctrlr.QT.panelControllers[1];
				if (t.isActive=="Y") { t.doDrop(sdxData); }
			}));
			i2b2.sdx.Master.setHandlerCustom('QPD3', 'CONCPT', 'DropHandler', (function(sdxData) { 
				sdxData = sdxData[0];	// only interested in first record
				var t = i2b2.CRC.ctrlr.QT.panelControllers[2];
				if (t.isActive=="Y") { t.doDrop(sdxData); }
			}));
					
			i2b2.sdx.Master.setHandlerCustom('QPD1', 'ENS', 'DropHandler', (function(sdxData) { 
				sdxData = sdxData[0];	// only interested in first record
				var t = i2b2.CRC.ctrlr.QT.panelControllers[0];
				if (t.isActive=="Y") { t.doDrop(sdxData); }
			}));
			i2b2.sdx.Master.setHandlerCustom('QPD2', 'ENS', 'DropHandler', (function(sdxData) { 
				sdxData = sdxData[0];	// only interested in first record
				var t = i2b2.CRC.ctrlr.QT.panelControllers[1];
				if (t.isActive=="Y") { t.doDrop(sdxData); }
			}));
			i2b2.sdx.Master.setHandlerCustom('QPD3', 'ENS', 'DropHandler', (function(sdxData) { 
				sdxData = sdxData[0];	// only interested in first record
				var t = i2b2.CRC.ctrlr.QT.panelControllers[2];
				if (t.isActive=="Y") { t.doDrop(sdxData); }
			}));

			i2b2.sdx.Master.setHandlerCustom('QPD1', 'PRS', 'DropHandler', (function(sdxData) { 
				sdxData = sdxData[0];	// only interested in first record
				var t = i2b2.CRC.ctrlr.QT.panelControllers[0];
				if (t.isActive=="Y") { t.doDrop(sdxData); }
			}));
			i2b2.sdx.Master.setHandlerCustom('QPD2', 'PRS', 'DropHandler', (function(sdxData) { 
				sdxData = sdxData[0];	// only interested in first record
				var t = i2b2.CRC.ctrlr.QT.panelControllers[1];
				if (t.isActive=="Y") { t.doDrop(sdxData); }
			}));
			i2b2.sdx.Master.setHandlerCustom('QPD3', 'PRS', 'DropHandler', (function(sdxData) { 
				sdxData = sdxData[0];	// only interested in first record
				var t = i2b2.CRC.ctrlr.QT.panelControllers[2];
				if (t.isActive=="Y") { t.doDrop(sdxData); }
			}));
			
			i2b2.sdx.Master.setHandlerCustom('QPD1', 'QM', 'DropHandler', (function(sdxData) { 
				sdxData = sdxData[0];	// only interested in first record
				var t = i2b2.CRC.ctrlr.QT.panelControllers[0];
				if (t.isActive=="Y") { t.doDrop(sdxData); }
			}));
			i2b2.sdx.Master.setHandlerCustom('QPD2', 'QM', 'DropHandler', (function(sdxData) { 
				sdxData = sdxData[0];	// only interested in first record
				var t = i2b2.CRC.ctrlr.QT.panelControllers[1];
				if (t.isActive=="Y") { t.doDrop(sdxData); }
			}));
			i2b2.sdx.Master.setHandlerCustom('QPD3', 'QM', 'DropHandler', (function(sdxData) { 
				sdxData = sdxData[0];	// only interested in first record
				var t = i2b2.CRC.ctrlr.QT.panelControllers[2];
				if (t.isActive=="Y") { t.doDrop(sdxData); }
			}))			


			var funcATN = function(yuiTree, yuiParentNode, sdxDataPack, callbackLoader) { 
				var myobj = { html: sdxDataPack.renderData.html, nodeid: sdxDataPack.renderData.htmlID}
				// if the treenode we are appending to is the root node then do not show the [+] infront
				if (yuiTree.getRoot() == yuiParentNode) {
					var tmpNode = new YAHOO.widget.HTMLNode(myobj, yuiParentNode, false, false);
				} else {
					var tmpNode = new YAHOO.widget.HTMLNode(myobj, yuiParentNode, false, true);
				}
				if (sdxDataPack.renderData.iconType != 'CONCPT_item' && !Object.isUndefined(callbackLoader)) {
					// add the callback to load child nodes
					sdxDataPack.sdxInfo.sdxLoadChildren = callbackLoader;
				}
				tmpNode.data.i2b2_SDX= sdxDataPack;
				tmpNode.toggle = function() {
					if (!this.tree.locked && ( this.hasChildren(true) ) ) {
							var data = this.data.i2b2_SDX.renderData;
							var img = this.getContentEl();
							img = Element.select(img,'img')[0];
							if (this.expanded) { 
								img.src = data.icon;
								this.collapse(); 
							} else { 
								img.src = data.iconExp;
								this.expand(); 
							}
						}
				};
				if (sdxDataPack.renderData.iconType == 'CONCPT_leaf' || !sdxDataPack.renderData.canExpand) { tmpNode.dynamicLoadComplete = true; }
			}
			i2b2.sdx.Master.setHandlerCustom('QPD1', 'CONCPT', 'AppendTreeNode', funcATN);
			i2b2.sdx.Master.setHandlerCustom('QPD2', 'CONCPT', 'AppendTreeNode', funcATN);
			i2b2.sdx.Master.setHandlerCustom('QPD3', 'CONCPT', 'AppendTreeNode', funcATN);

			i2b2.sdx.Master.setHandlerCustom('QPD1', 'ENS', 'AppendTreeNode', funcATN);
			i2b2.sdx.Master.setHandlerCustom('QPD2', 'ENS', 'AppendTreeNode', funcATN);
			i2b2.sdx.Master.setHandlerCustom('QPD3', 'ENS', 'AppendTreeNode', funcATN);

			i2b2.sdx.Master.setHandlerCustom('QPD1', 'PRS', 'AppendTreeNode', funcATN);
			i2b2.sdx.Master.setHandlerCustom('QPD2', 'PRS', 'AppendTreeNode', funcATN);
			i2b2.sdx.Master.setHandlerCustom('QPD3', 'PRS', 'AppendTreeNode', funcATN);

			var funcQMDH = function(sdxData) {
				sdxData = sdxData[0];	// only interested in first record
				// pass the QM ID to be loaded
				var qm_id = sdxData.sdxInfo.sdxKeyValue;
				i2b2.CRC.ctrlr.QT.doQueryLoad(qm_id)
			};
			i2b2.sdx.Master.setHandlerCustom('QPD1', 'QM', 'AppendTreeNode', funcATN);
			i2b2.sdx.Master.setHandlerCustom('QPD2', 'QM', 'AppendTreeNode', funcATN);
			i2b2.sdx.Master.setHandlerCustom('QPD3', 'QM', 'AppendTreeNode', funcATN);
			i2b2.sdx.Master.setHandlerCustom('queryName', 'QM', 'DropHandler', funcQMDH);
			//======================= </Define Drop Handlers> =======================
			
			
			// ========= Override default LoadChildrenFromTreeview handler (we need this so that we can properly capture the XML request/response messages) ========= 
			var funcLCFT = function(node, onCompleteCallback) {
				var scopedCallback = new i2b2_scopedCallback();
				scopedCallback.scope = node.data.i2b2_SDX;
				scopedCallback.callback = function(results) {
					var cl_node = node;
					var cl_onCompleteCB = onCompleteCallback;
					var cl_options = options;
					// THIS function is used to process the AJAX results of the getChild call
					//		results data object contains the following attributes:
					//			refXML: xmlDomObject <--- for data processing
					//			msgRequest: xml (string)
					//			msgResponse: xml (string)
					//			error: boolean
					//			errorStatus: string [only with error=true]
					//			errorMsg: string [only with error=true]
						
					
		// <THIS IS WHY WE ARE CREATING CUSTOMER HANDLERS FOR THE Query Tool CONTROL!>
					i2b2.CRC.view.QT.queryResponse = results.msgResponse;
					i2b2.CRC.view.QT.queryRequest = results.msgRequest;
					i2b2.CRC.view.QT.queryUrl = results.msgUrl;
		// </THIS IS WHY WE ARE CREATING CUSTOMER HANDLERS FOR THE QueryTool CONTROL!>					

					// clear the drop-lock so the node can be requeried if anything bad happens below
					node.data.i2b2_dropLock = false;

					
					// handle any errors
					if (results.error) {
						// process the specific error
						var errorCode = results.refXML.getElementsByTagName('status')[0].firstChild.nodeValue;
						if (errorCode == "MAX_EXCEEDED") {
							var eaction = confirm("The number of children in this node exceeds the maximum number you specified in options.\n Displaying all children may take a long time to do.");
						}
						else {
							alert("The following error has occurred:\n" + errorCode);
						}
						// re-fire the call with no max limit if the user requested so
						if (eaction) {
							var mod_options = Object.clone(cl_options);
							delete mod_options.ont_max_records;
							i2b2.ONT.ajax.GetChildConcepts("CRC:QueryTool", mod_options, scopedCallback);
							return true;
						}
						// ROLLBACK the tree changes
						cl_onCompleteCB();
						// reset dynamic load state for the node (total hack of YUI Treeview)
						node.collapse();
						node.dynamicLoadComplete = false;
						node.expanded = false;
						node.childrenRendered = false;
						node._dynLoad = true;
						// uber-elite code (fix the style settings)
						var tc = node.getToggleEl().className;
						tc = tc.substring(0, tc.length - 1) + 'p';
						node.getToggleEl().className = tc;
						// fix the icon image
						var img = node.getContentEl();
						img = Element.select(img, 'img')[0];
						img.src = node.data.i2b2_SDX.sdxInfo.icon;
						return false;
					}
					
					var c = results.refXML.getElementsByTagName('concept');
					for(var i=0; i<1*c.length; i++) {
						var o = new Object;
						o.xmlOrig = c[i];
						o.name = i2b2.h.getXNodeVal(c[i],'name');
						o.hasChildren = i2b2.h.getXNodeVal(c[i],'visualattributes').substring(0,2);
						o.level = i2b2.h.getXNodeVal(c[i],'level');
						o.key = i2b2.h.getXNodeVal(c[i],'key');
						o.tooltip = i2b2.h.getXNodeVal(c[i],'tooltip');
						o.icd9 = '';
						o.table_name = i2b2.h.getXNodeVal(c[i],'tablename');
						o.column_name = i2b2.h.getXNodeVal(c[i],'columnname');
						o.operator = i2b2.h.getXNodeVal(c[i],'operator');
						o.dim_code = i2b2.h.getXNodeVal(c[i],'dimcode');
						// append the data node
						var sdxDataNode = i2b2.sdx.Master.EncapsulateData('CONCPT',o);
						var renderOptions = {
							title: o.name,
							dblclick: "i2b2.ONT.view.nav.ToggleNode(this,'"+cl_node.tree.id+"')",
							icon: {
								root: "sdx_ONT_CONCPT_root.gif",
								rootExp: "sdx_ONT_CONCPT_root-exp.gif",
								branch: "sdx_ONT_CONCPT_branch.gif",
								branchExp: "sdx_ONT_CONCPT_branch-exp.gif",
								leaf: "sdx_ONT_CONCPT_leaf.gif"
							}
						};
						var sdxRenderData = i2b2.sdx.Master.RenderHTML(cl_node.tree.id, sdxDataNode, renderOptions);
						i2b2.sdx.Master.AppendTreeNode(cl_node.tree, cl_node, sdxRenderData);
					}
					// handle the YUI treeview	
					cl_onCompleteCB();
				}
				
				// fix double loading error via node level dropping-lock
				if (node.data.i2b2_dropLock) { return true; }
				node.data.i2b2_dropLock = true;
				
				var options = {};
				options.ont_max_records = "max='" +i2b2.CRC.cfg.params.maxChildren + "'";
				options.result_wait_time= i2b2.CRC.cfg.params.queryTimeout;
				options.ont_synonym_records = i2b2.ONT.cfg.params.synonyms;
				options.ont_hidden_records = i2b2.ONT.cfg.params.hiddens;
				// parent key
				options.concept_key_value = node.data.i2b2_SDX.sdxInfo.sdxKeyValue;
				options.version = i2b2.ClientVersion;
				i2b2.ONT.ajax.GetChildConcepts("CRC:QueryTool", options, scopedCallback);
			}
			i2b2.sdx.Master.setHandlerCustom('QPD1', 'CONCPT', 'LoadChildrenFromTreeview', funcLCFT);
			i2b2.sdx.Master.setHandlerCustom('QPD2', 'CONCPT', 'LoadChildrenFromTreeview', funcLCFT);
			i2b2.sdx.Master.setHandlerCustom('QPD3', 'CONCPT', 'LoadChildrenFromTreeview', funcLCFT);
			// ========= END Override default LoadChildrenFromTreeview handler (we need this so that we can properly capture the XML request/response messages)  END ========= 

			

			//======================= <Initialization> =======================
			// Connect the panel controllers to the DOM nodes in the document
			var t = i2b2.CRC.ctrlr.QT;
			
			queryTimingButton =  new YAHOO.widget.Button("queryTiming", 
					{ lazyLoad: "false", type: "menu", menu: "menubutton1select", name:"querytiming" });

		defineTemporalButton = new YAHOO.widget.Button( "defineTemporal", 
					{ lazyLoad: false, type: "menu", menu: "menubutton2select", name:"definetemporal" });


			var addDefineGroup = new YAHOO.widget.Button("addDefineGroup"); 
				addDefineGroup.on("click", function (event) {
					i2b2.CRC.view.QT.addNewTemporalGroup();

						});

		
			queryTimingButton.on("mousedown", function (event) {
				//i2b2.CRC.ctrlr.QT.panelControllers[0].doTiming(p_oItem.value);
				if ((i2b2.CRC.ctrlr.QT.hasModifier) && (queryTimingButton.getMenu().getItems().length == 3))  {
					queryTimingButton.getMenu().addItems([ 	 
										{ text: "Items Instance will be the same", value: "SAMEINSTANCENUM" }]);	 
					queryTimingButton.getMenu().render();
				}
			});
		
		
		
			defineTemporalButton.on("selectedMenuItemChange", function (event) {
				//i2b2.CRC.ctrlr.QT.panelControllers[0].doTiming(p_oItem.value);
				var oMenuItem = event.newValue; 
				
				var sText = oMenuItem.value;
							defineTemporalButton.set("label",oMenuItem.cfg.getProperty("text"));	
		
				if (sText != "BUILDER")
				{
					$('crc.temoralBuilder').hide();		

					$('crc.innerQueryPanel').show();
					i2b2.CRC.ctrlr.QT.temporalGroup = sText;
					i2b2.CRC.ctrlr.QT._redrawAllPanels();
					
					
					if (sText == "0")
					{
						$('QPD1').style.background = '#FFFFFF';
						$('queryPanelTitle1').innerHTML = 'Group 1';
						i2b2.CRC.ctrlr.QT.panelControllers[0].refTiming.set('disabled', false);
					} else {
						$('QPD1').style.background = '#D9ECF0';
						$('queryPanelTitle1').innerHTML = 'Anchoring Observation';	
						i2b2.CRC.ctrlr.QT.panelControllers[0].doTiming("SAMEINSTANCENUM");
					    i2b2.CRC.ctrlr.QT.panelControllers[0].refTiming.set('disabled', true);
						i2b2.CRC.ctrlr.QT.panelControllers[0].refTiming.set("label", "Items Instance will be the same");		


					
					}
				} else {
					$('crc.innerQueryPanel').hide();
					$('crc.temoralBuilder').show();	
	//				queryTimingButton.set("label", "Temporal Contraint Builder");
				}
				i2b2.CRC.view.QT.ResizeHeight();
						}); 

			queryTimingButton.on("selectedMenuItemChange", function (event) {
				//i2b2.CRC.ctrlr.QT.panelControllers[0].doTiming(p_oItem.value);
				var oMenuItem = event.newValue; 
				
					if (oMenuItem == 0)
				{
					var sValue = "ANY";
					var sText = "Treat all groups independently";
				} else if (oMenuItem == 1)
				{
					var sValue = "SAME";
					var sText = "Selected groups occur in the same financial encounter";
				} else {
					var sValue = oMenuItem.value;
					var sText = oMenuItem.cfg.getProperty("text");
				}
				
				if (sValue != "TEMPORAL") {
					var dm = i2b2.CRC.model.queryCurrent;
					for (var k=0; k<dm.panels[i2b2.CRC.ctrlr.QT.temporalGroup].length; k++) {
						dm.panels[i2b2.CRC.ctrlr.QT.temporalGroup][k].timing = sValue;
					}
				}
				
				//var sText = oMenuItem.cfg.getProperty("text");
				
				var length = i2b2.CRC.ctrlr.QT.panelControllers.length;
			
				queryTimingButton.set("label", sText);		
				
				if (sValue != "TEMPORAL") {
					$('QPD1').style.background = '#FFFFFF';
					$('defineTemporalBar').hide();
					$('crc.temoralBuilder').hide();	
					$('crc.innerQueryPanel').show();
				}
				if (sValue == "SAMEVISIT") {
					i2b2.CRC.ctrlr.QT.queryTiming = "SAMEVISIT";
					for (var i=0; i<length; i++) {
						//$("queryPanelTimingB" + (i+1) +  "-button").disabled = false;					
						//$("queryPanelTimingB" + (i+1) +  "-button").innerHTML = "Occurs in Same Encounter";	
						i2b2.CRC.ctrlr.QT.panelControllers[i].refTiming.set('disabled', false);					
						i2b2.CRC.ctrlr.QT.panelControllers[i].refTiming.set("label",  "Occurs in Same Encounter");	
						if (YAHOO.util.Dom.inDocument(i2b2.CRC.ctrlr.QT.panelControllers[i].refTiming.getMenu().element)) {
		
							i2b2.CRC.ctrlr.QT.panelControllers[i].refTiming.getMenu().clearContent();
							i2b2.CRC.ctrlr.QT.panelControllers[i].refTiming.getMenu().addItems([ 	 
												{ text: "Treat Independently", value: "ANY"}]);	
							i2b2.CRC.ctrlr.QT.panelControllers[i].refTiming.getMenu().addItems([ 	 
												{ text: "Occurs in Same Encounter", value: "SAMEVISIT" }]);	 
							i2b2.CRC.ctrlr.QT.panelControllers[i].refTiming.getMenu().addItems([ 												
												{ text: "Items Instance will be the same", value: "SAMEINSTANCENUM" }]);	 
							i2b2.CRC.ctrlr.QT.panelControllers[i].refTiming.getMenu().render();
						} else {
							i2b2.CRC.ctrlr.QT.panelControllers[i].refTiming.itemData ={ text: "Treat Independently", value: "ANY",
												text: "Occurs in Same Encounter", value: "SAMEVISIT",
											    text: "Items Instance will be the same", value: "SAMEINSTANCENUM"  };
						}
								i2b2.CRC.ctrlr.QT.panelControllers[i].doTiming(sValue);
					}
	
				} else if (sValue == "ANY") {
					i2b2.CRC.ctrlr.QT.queryTiming = "ANY";
					
					i2b2.CRC.ctrlr.QT.temporalGroup = 0;
					i2b2.CRC.ctrlr.QT._redrawAllPanels();
					
					for (var i=0; i<length; i++) {
						i2b2.CRC.ctrlr.QT.panelControllers[i].refTiming.set("label", "Treat Independently");		
						i2b2.CRC.ctrlr.QT.panelControllers[i].refTiming.set('disabled', true);				
						i2b2.CRC.ctrlr.QT.panelControllers[i].doTiming(sValue);
					}
				} else if (sValue == "ENCOUNTER") {
					i2b2.CRC.ctrlr.QT.queryTiming = "ENCOUNTER";
					for (var i=0; i<length; i++) {
						//$("queryPanelTimingB" + (i+1) +  "-button").disabled = false;					
						//$("queryPanelTimingB" + (i+1) +  "-button").innerHTML = "Occurs in Same Encounter";	
						i2b2.CRC.ctrlr.QT.panelControllers[i].refTiming.set('disabled', false);					
						i2b2.CRC.ctrlr.QT.panelControllers[i].refTiming.set("label",  "Treat Independently");	
						if (YAHOO.util.Dom.inDocument(i2b2.CRC.ctrlr.QT.panelControllers[i].refTiming.getMenu().element)) {
		
							i2b2.CRC.ctrlr.QT.panelControllers[i].refTiming.getMenu().clearContent();
							i2b2.CRC.ctrlr.QT.panelControllers[i].refTiming.getMenu().addItems([ 	 
												{ text: "Treat Independently", value: "ANY"}]);	
							for (var j=0; j<length; j++) {
								i2b2.CRC.ctrlr.QT.panelControllers[i].refTiming.getMenu().addItems([ 	 
												{ text: "Occurs (" + (j+1) + ")", value: "OCCUR"+j }]);	 
							}
							i2b2.CRC.ctrlr.QT.panelControllers[i].refTiming.getMenu().render();
						} else {
							i2b2.CRC.ctrlr.QT.panelControllers[i].refTiming.itemData ={ text: "Treat Independently", value: "ANY",
												text: "Occurs", value: "OCCUR0" };
						}
						i2b2.CRC.ctrlr.QT.panelControllers[i].doTiming(sValue);
					}					
				} else if  (sValue == "TEMPORAL") {
					i2b2.CRC.ctrlr.QT.queryTiming = "TEMPORAL";
					$('defineTemporalBar').show();	
					for (var i=0; i<length; i++) {

						i2b2.CRC.ctrlr.QT.panelControllers[i].refTiming.set('disabled', false);					
						//i2b2.CRC.ctrlr.QT.panelControllers[i].refTiming.set("label", "Items Instance will be the same");
						
					}					
					//$('QPD1').style.background = '#D9ECF0';
					//$('queryPanelTitle1').innerHTML = 'Anchoring Observation';
					
				} else {
					i2b2.CRC.ctrlr.QT.queryTiming = "SAMEINSTANCENUM";
					for (var i=0; i<length; i++) {

						i2b2.CRC.ctrlr.QT.panelControllers[i].refTiming.set('disabled', false);					
						i2b2.CRC.ctrlr.QT.panelControllers[i].refTiming.set("label", sText);
						
						if (YAHOO.util.Dom.inDocument(i2b2.CRC.ctrlr.QT.panelControllers[i].refTiming.getMenu().element)) {
		
							i2b2.CRC.ctrlr.QT.panelControllers[i].refTiming.getMenu().clearContent();
							i2b2.CRC.ctrlr.QT.panelControllers[i].refTiming.getMenu().addItems([ 	 
												{ text: "Treat Independently", value: "ANY"}]);	
							i2b2.CRC.ctrlr.QT.panelControllers[i].refTiming.getMenu().addItems([ 	 
												{ text: "Occurs in Same Encounter", value: "SAMEVISIT" }]);	 
							i2b2.CRC.ctrlr.QT.panelControllers[i].refTiming.getMenu().addItems([ 												
												{ text: "Items Instance will be the same", value: "SAMEINSTANCENUM" }]);	 
							i2b2.CRC.ctrlr.QT.panelControllers[i].refTiming.getMenu().render();
						} else {
							i2b2.CRC.ctrlr.QT.panelControllers[i].refTiming.itemData =[{ text: "Treat Independently", value: "ANY"},
												{ text: "Occurs in Same Encounter", value: "SAMEVISIT"} ,
											    { text: "Items Instance will be the same", value: "SAMEINSTANCENUM"  }];
						}				
					
					
						i2b2.CRC.ctrlr.QT.panelControllers[i].doTiming(sValue);
					}
					
				}
				i2b2.CRC.view.QT.ResizeHeight();
			
			}); 
			
			//var qryButtonTiming = {};
			for (var i=0; i<3; i++) {
				
				var onSelectedMenuItemChange = function (event) { 
			    	var oMenuItem = event.newValue; 
	 
	    			this.set("label", ("<em class=\"yui-button-label\">" +  
	        	        oMenuItem.cfg.getProperty("text") + "</em>")); 
	 
	 				if (event.newvalue != event.prevValue) {		
						var panelNumber = this.toString();
						panelNumber = panelNumber.substring( panelNumber.length-1, panelNumber.length-0);
				 			i2b2.CRC.ctrlr.QT.panelControllers[panelNumber-1].doTiming(oMenuItem.value);	
					}
					if (oMenuItem.value.substring(0,5) == "OCCUR") {
						this.setStyle('width', 130);
						$("qryButtonLimitB1").show();
						//$('qryPanelTiming Button').style.width = 120;
					} else {
						this.setStyle('width', 160);
						$("qryButtonLimitB1").hide();
						//$('qryPanelTiming Button').style.width = 160;
						//$(this._button.id).clientWidth = 160;
					}
				}; 
				
				//var panelControl = t.panelControllers[i];
				
				t.panelControllers[i].ctrlIndex = i;
				t.panelControllers[i].refTitle = $("queryPanelTitle"+(i+1));
				t.panelControllers[i].refButtonExclude = $("queryPanelExcludeB"+(i+1));
				t.panelControllers[i].refButtonDates = $("queryPanelDatesB"+(i+1));
				t.panelControllers[i].refButtonOccurs = $("queryPanelOccursB"+(i+1));
				t.panelControllers[i].refButtonOccursNum = $("QP"+(i+1)+"Occurs");
				t.panelControllers[i].refBalloon = $("queryBalloon"+(i+1));
				t.panelControllers[i].refDispContents = $("QPD"+(i+1));
				
				
				//t.panelControllers[i].refTiming = $("queryPanelTimingB"+(i+1));
				//t.panelControllers[i].refTiming = $("queryPanelTimingB"+(i+1));
				var qryButtonTiming =  new YAHOO.widget.Button("queryPanelTimingB"+(i+1), 
							{ type: "menu", menu: "menubutton1select", name:"querytiming" });
				//qryButtonTiming.set('disabled', true);
				 qryButtonTiming.on("selectedMenuItemChange", onSelectedMenuItemChange); 
				 qryButtonTiming.setStyle('width', 160);

				t.panelControllers[i].refTiming = qryButtonTiming;
				t.panelControllers[i].refTiming.set('disabled', true);				

				// create a instance of YUI Treeview
				if (!t.panelControllers[i].yuiTree) {
					t.panelControllers[i].yuiTree = new YAHOO.widget.TreeView("QPD"+(i+1));
					t.panelControllers[i].yuiTree.setDynamicLoad(t.panelControllers[i]._loadTreeDataForNode,1);
					// forward reference from DOM Node to tree obj
					$("QPD"+(i+1)).tree = t.panelControllers[i].yuiTree;
					// linkback on the treeview to allow it to find its PanelController
					t.panelControllers[i].refDispContents.linkbackPanelController = t.panelControllers[i];
				}
			}
			// display the panels
			t.doScrollFirst();
			t._redrawPanelCount();
			i2b2.CRC.ctrlr.QT.doShowFrom(0);
			i2b2.CRC.ctrlr.history.Refresh();
			//======================= </Initialization> =======================


			 function qryPanelTimingClick(p_sType, p_aArgs) {
		
					var oEvent = p_aArgs[0],	//	DOM event

				oMenuItem = p_aArgs[1];	//	MenuItem instance that was the 
										//	target of the event

			if (oMenuItem) {
				YAHOO.log("[MenuItem Properties] text: " + 
							oMenuItem.cfg.getProperty("text") + ", value: " + 
							oMenuItem.value);
			}
			
			qryButtonTiming.set("label", qryButtonTiming.getMenu().activeItem.srcElement.text );


	//		i2b2.CRC.ctrlr.QT.panelControllers[0].doTiming(p_oItem.value);
	//		var sText = p_oItem.cfg.getProperty("text");
    //		oMenuPanelTiming1.set("label", sText);		
			
		}


			// attach the context controller to all panel controllers objects
			var op = i2b2.CRC.view.QT; // object path 
			i2b2.CRC.view.QT.ContextMenu = new YAHOO.widget.ContextMenu( 
					"divContextMenu-QT",  
					{ lazyload: true,
					trigger: [$('QPD1'), $('QPD2'), $('QPD3')],
					itemdata: [
						{ text: "Delete", 		onclick: { fn: op.ContextMenuRouter, obj: 'delete' } },
						{ text: "Lab Values", 	onclick: { fn: op.ContextMenuRouter, obj: 'labvalues' } }
					] }  
			); 
			
			i2b2.CRC.view.QT.ContextMenu.subscribe("triggerContextMenu", i2b2.CRC.view.QT.ContextMenuPreprocess); 
			i2b2.CRC.view.QT.ContextMenu.subscribe("beforeShow", i2b2.CRC.view.QT.ContextMenuPreprocess);
			
			i2b2.CRC.view.QT.splitterDragged();					// initialize query tool's elements
// ================================================================================================== //
		}
	})
);


// QueryTool Helper Balloons
// ================================================================================================== //
i2b2.CRC.view.QT.hballoon = {
	canShowQueryBalloons: true,
	delayQueryBalloons: false,
	hideBalloons: function() {
		var thisObj = i2b2.CRC.view.QT.hballoon;
		thisObj.canShowQueryBalloons = false;
		clearTimeout(thisObj.delayQueryBalloons);
		$('queryBalloonBox').hide();
		YAHOO.util.Event.removeListener(document, "mousemove", thisObj.showBalloons);
		YAHOO.util.Event.addListener(document, "mousemove", thisObj.showBalloons);
	},
	showBalloons: function(e) {
		var thisObj = i2b2.CRC.view.QT.hballoon;
		var x = YAHOO.util.Event.getPageX(e);
		var y = YAHOO.util.Event.getPageY(e);
		var elX = parseInt($('crcQueryToolBox').style.left);
		if (isNaN(elX)) {elX = 241;}
		var elY = $('crcQueryToolBox').getHeight();
		if (isNaN(elY)) {elY = 280;}
		elY = elY + 76 - 135;
		if ( (x < elX-5) || (x > elX+524+5) || (y < elY-15) || (y > elY+110) ) {
			if (!thisObj.canShowQueryBalloons) {
				thisObj.canShowQueryBalloons = true;
				thisObj.delayQueryBalloons = setTimeout("i2b2.CRC.view.QT.hballoon._showQueryBalloons()",200);
			}
		} else {
			thisObj.canShowQueryBalloons = false;
			clearTimeout(thisObj.delayQueryBalloons);
		}
	},
	_showQueryBalloons: function() {
		var thisObj = i2b2.CRC.view.QT.hballoon;
		if (thisObj.canShowQueryBalloons) {
			$('queryBalloonBox').show();
			YAHOO.util.Event.removeListener(document, "mousemove", thisObj.showBalloons);
		}
	}
};

//================================================================================================== //
i2b2.events.initView.subscribe((function(eventTypeName, newMode) {
// -------------------------------------------------------
	this.visible = true;
	$('crcQueryToolBox').show();
	this.Resize();
	
	// initialize the dropdown menu for query timing
	var temporalConstraintBar 	= $("temporalConstraintBar");
	var temporalConstraintLabel = $("temporalConstraintLabel");
	var queryTimingButton		= $("queryTiming-button");
	temporalConstraintDiv.style.width 	= Math.max( parseInt(temporalConstraintBar.style.width) - parseInt(temporalConstraintLabel.style.width)-2, 0) + "px";
	queryTimingButton.style.width 		= Math.max( parseInt(temporalConstraintBar.style.width) - parseInt(temporalConstraintLabel.style.width)-6, 0) + "px";
	
	// -------------------------------------------------------
}),'',i2b2.CRC.view.QT);


// ================================================================================================== //
i2b2.events.changedViewMode.subscribe((function(eventTypeName, newMode) {
// -------------------------------------------------------
	newMode = newMode[0];
	this.viewMode = newMode;
	switch(newMode) {
		case "Patients":
			this.visible = true;
			$('crcQueryToolBox').show();
			i2b2.CRC.view.QT.splitterDragged();
			//this.Resize();
			break;
		default:
			this.visible = false;
			$('crcQueryToolBox').hide();
			break;
	}
// -------------------------------------------------------
}),'', i2b2.CRC.view.QT);


// ================================================================================================== //
i2b2.events.changedZoomWindows.subscribe((function(eventTypeName, zoomMsg) {
	newMode = zoomMsg[0];
	if (!newMode.action) { return; }
	if (newMode.action == "ADD") {
		switch (newMode.window) {
			case "QT":
				this.isZoomed = true;
				this.visible = true;
				break;
		}
	} else {
		switch (newMode.window) {
			case "QT":
				this.isZoomed = false;
				this.visible = true;
		}
	}
	this.ResizeHeight();
}),'',i2b2.CRC.view.QT);


console.timeEnd('execute time');
console.groupEnd();
