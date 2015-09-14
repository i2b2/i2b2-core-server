/**
 * @projectDescription	Example using tabs and SDX DragDrop integration (controller code).
 * @inherits	i2b2
 * @namespace	i2b2.ExampTabs
 * @author	Nick Benik, Griffin Weber MD PhD
 * @version 	1.3
 * ----------------------------------------------------------------------------------------
 * updated 9-28-08: 	Initial Launch [Nick Benik] 
 * updated 10-30-08:	GUI revisions [Griffin Weber] 
 */

i2b2.ExampTabs.Init = function(loadedDiv) {
	// this function is called after the HTML is loaded into the viewer DIV
	
	// register DIV as valid DragDrop target for Patient Record Sets (PRS) objects
	var divName = "ExampTabs-PRSDROP";
	// register for drag drop events for the following data types: CONCPT, QM, QI, PRS, PRC
	var op_trgt = {dropTarget:true};
	i2b2.sdx.Master.AttachType(divName, 'CONCPT', op_trgt);
	i2b2.sdx.Master.AttachType(divName, 'QM', op_trgt);
	i2b2.sdx.Master.AttachType(divName, 'QI', op_trgt);
	i2b2.sdx.Master.AttachType(divName, 'PRS', op_trgt);
	i2b2.sdx.Master.AttachType(divName, 'PRC', op_trgt);
	i2b2.sdx.Master.AttachType(divName, 'PR', op_trgt);	
	i2b2.sdx.Master.AttachType(divName, 'QDEF', op_trgt);	
	i2b2.sdx.Master.AttachType(divName, 'QGDEF', op_trgt);
	i2b2.sdx.Master.AttachType(divName, 'XML', op_trgt);	
	// route event callbacks to a single drop event handler used by this plugin
	var eventRouterFunc = (function(sdxData) { i2b2.ExampTabs.doDrop(sdxData); });
	i2b2.sdx.Master.setHandlerCustom(divName, 'CONCPT', 'DropHandler', eventRouterFunc);
	i2b2.sdx.Master.setHandlerCustom(divName, 'QM', 'DropHandler', eventRouterFunc);
	i2b2.sdx.Master.setHandlerCustom(divName, 'QI', 'DropHandler', eventRouterFunc);
	i2b2.sdx.Master.setHandlerCustom(divName, 'PRS', 'DropHandler', eventRouterFunc);
	i2b2.sdx.Master.setHandlerCustom(divName, 'PRC', 'DropHandler', eventRouterFunc);
	i2b2.sdx.Master.setHandlerCustom(divName, 'PR', 'DropHandler', eventRouterFunc);
	i2b2.sdx.Master.setHandlerCustom(divName, 'QDEF', 'DropHandler', eventRouterFunc);
	i2b2.sdx.Master.setHandlerCustom(divName, 'QGDEF', 'DropHandler', eventRouterFunc);
	i2b2.sdx.Master.setHandlerCustom(divName, 'XML', 'DropHandler', eventRouterFunc);

	// manage YUI tabs
	var cfgObj = {activeIndex : 0};
	this.yuiTabs = new YAHOO.widget.TabView("ExampTabs-TABS", cfgObj);
	this.yuiTabs.on('activeTabChange', function(ev) { 
		//Tabs have changed 
		if (ev.newValue.get('id')=="ExampTabs-TAB1") {
			// user switched to Results tab
			if (i2b2.ExampTabs.model.currentRec) { 
				// gather statistics only if we have data
				if (i2b2.ExampTabs.model.dirtyResultsData) {
					// recalculate the results only if the input data has changed
					i2b2.ExampTabs.getResults();
				}
			}
		}
	});
};


i2b2.ExampTabs.Unload = function() {
	// this function is called before the plugin is unloaded by the framework
	return true;
};


i2b2.ExampTabs.doDrop = function(sdxData) {
	sdxData = sdxData[0];	// only interested in first record
	// save the info to our local data model
	i2b2.ExampTabs.model.currentRec = sdxData;
	// let the user know that the drop was successful by displaying the name of the object
	$("ExampTabs-PRSDROP").innerHTML = i2b2.h.Escape(sdxData.sdxInfo.sdxDisplayName);
	// optimization to prevent requerying the hive for new results if the input dataset has not changed
	i2b2.ExampTabs.model.dirtyResultsData = true;		
}


i2b2.ExampTabs.getResults = function() {
	// Refresh the display with info of the SDX record that was DragDropped
	if (i2b2.ExampTabs.model.dirtyResultsData) {
		var dropRecord = i2b2.ExampTabs.model.currentRec;
		$$("DIV#ExampTabs-mainDiv DIV#ExampTabs-TABS DIV.results-directions")[0].hide();
		$$("DIV#ExampTabs-mainDiv DIV#ExampTabs-TABS DIV.results-finished")[0].show();		
		var sdxDisplay = $$("DIV#ExampTabs-mainDiv DIV#ExampTabs-InfoSDX")[0];
		Element.select(sdxDisplay, '.sdxDisplayName')[0].innerHTML = dropRecord.sdxInfo.sdxDisplayName;
		Element.select(sdxDisplay, '.sdxType')[0].innerHTML = dropRecord.sdxInfo.sdxType;
		Element.select(sdxDisplay, '.sdxControlCell')[0].innerHTML = dropRecord.sdxInfo.sdxControlCell;
		Element.select(sdxDisplay, '.sdxKeyName')[0].innerHTML = dropRecord.sdxInfo.sdxKeyName;
		Element.select(sdxDisplay, '.sdxKeyValue')[0].innerHTML = dropRecord.sdxInfo.sdxKeyValue;
		// we must escape the xml text or the browser will attempt to interpret it as HTML
		var xmlDisplay = i2b2.h.Xml2String(dropRecord.origData.xmlOrig);
		xmlDisplay = '<pre>'+i2b2.h.Escape(xmlDisplay)+'</pre>';
		Element.select(sdxDisplay, '.originalXML')[0].innerHTML = xmlDisplay;
	}
	
	// optimization - only requery when the input data is changed
	i2b2.ExampTabs.model.dirtyResultsData = false;		
}
