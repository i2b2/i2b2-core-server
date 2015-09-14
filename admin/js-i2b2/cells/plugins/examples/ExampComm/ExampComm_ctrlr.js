/**
 * @projectDescription	Tool for interacting with standard Communicator objects within the web client (controller code).
 * @inherits	i2b2
 * @namespace	i2b2.ExampComm
 * @author	Nick Benik, Griffin Weber MD PhD
 * @version 	1.3
 * ----------------------------------------------------------------------------------------
 * updated 3-3-09: 	Initial Launch [Nick Benik] 
 */

i2b2.ExampComm.Init = function(loadedDiv) {
	// this function is called after the HTML is loaded into the viewer DIV
	var selTrgt = $$("DIV#ExampComm-mainDiv SELECT#ExampComm-objList")[0];
	for (var mname in i2b2) {
		if (i2b2[mname].ajax) {
			if (Object.getClass(i2b2[mname].ajax) == "i2b2Base_communicator") {
				var t = "i2b2." + mname + ".ajax";
				var n = new Option(t, t);
				selTrgt.options[selTrgt.length] = n;
			}
		}
	}
	// populate function call list for defaultly selected communicator
	if (selTrgt.length > -1) {
		i2b2.ExampComm.model.commObj = $$("DIV#ExampComm-mainDiv SELECT#ExampComm-objList")[0].value;
		i2b2.ExampComm.loadCalls(i2b2.ExampComm.model.commObj);
	}

	// manage YUI tabs
	var cfgObj = {activeIndex : 0};
	this.yuiTabs = new YAHOO.widget.TabView("ExampComm-TABS", cfgObj);
	this.yuiTabs.on('activeTabChange', function(ev) { 
		//Tabs have changed 
		if (ev.newValue.get('id')=="ExampComm-TAB1") {
			i2b2.ExampComm.buildMsg();
		}
	});
};



i2b2.ExampComm.buildMsg = function(){
	var tag_values = {};
	var self = i2b2.ExampComm.model;
	var vals = {};
	var noesc = [];
	var cc = $$("DIV#ExampComm-mainDiv DIV.taglist DIV.tagrow");
	for (var i = 0; i < cc.length; i++) {
		var tname = cc[i].select('.fieldname')[0].innerHTML;
		var tval = cc[i].select('textarea')[0].value;
		if (i2b2.ExampComm.model.tags[tname] && !i2b2.ExampComm.model.tags[tname].escaped) {
			tval = i2b2.h.Unescape(tval);
			noesc.push(tname);
		}
		i2b2.ExampComm.model.tags[tname].value = tval;
		vals[tname] = tval;
	}
	
	if (i == 0) {
		// no input grid variables were processed... 
		$$("DIV#ExampComm-mainDiv DIV.ExampComm-MainContent DIV.results-msgs")[0].hide();
		$$("DIV#ExampComm-mainDiv DIV.ExampComm-MainContent DIV.results-directions")[0].show();
		return;
	}

	// apply message values to message template
	i2b2.h.EscapeTemplateVars(vals, noesc);
	var syntax = /(^|.|\r|\n)(\{{{\s*(\w+)\s*}}})/; //matches symbols like '{{{ field }}}'
	var t = new Template(self.msg, syntax);
	var sMessage = t.evaluate(vals);
	self.SendMsg = i2b2.h.Escape(sMessage);

	// display in GUI
	$$("DIV#ExampComm-mainDiv DIV.ExampComm-MainContent DIV.results-directions")[0].hide();
	$$("DIV#ExampComm-mainDiv DIV.ExampComm-MainContent DIV.results-msgs")[0].show();
	var msgPREs = $$("DIV#ExampComm-TABS DIV.results-msgs TABLE PRE");
	msgPREs[0].innerHTML = self.SendMsg;
	msgPREs[1].innerHTML = "Waiting for response...";

	// build callback handler to be executed when the Communicator results are returned (ASYNC method)
	var scoped_callback = new i2b2_scopedCallback;
	scoped_callback.scope = msgPREs[1];
	scoped_callback.callback = function(cbResults){
		this.innerHTML = i2b2.h.Escape(cbResults.msgResponse);
		console.dir(cbResults);
	}
	// fire the request
	var commObjRef = eval("("+self.commObj+")");
	commObjRef[self.commFunc]("PLUGIN:CommunicatorTool", vals, scoped_callback);
};



i2b2.ExampComm.Unload = function() {
	// this function is called before the plugin is unloaded by the framework
	return true;
};



i2b2.ExampComm.addCommObj = function(){
	var cn = prompt("Please enter the namespace of the Standard Communicator object within the web client.","i2b2.PLUGINCODE.ajax");
	if (!cn) { return; }
	try {
		if (i2b2.h.isBadObjPath(cn)) {
			alert("Sorry, an error occurred while trying to access the object. Recheck the object's location within the framework's namespace and try again.");
			return;
		}
		var co = eval("("+cn+")");
		if (Object.getClass(co) == "i2b2Base_communicator") {
			// we should really be checking to see if the object is already listed
			var selTrgt = $$("DIV#ExampComm-mainDiv SELECT#ExampComm-objList")[0];
			var n = new Option(cn, cn);
			selTrgt.options[selTrgt.length] = n;
		} else {
			alert("The object was found but is not a standard Communicator object created by i2b2.hive.communicatorFactory()");
		}
	} catch (e) {
		alert("Sorry, an general error has occurred.");
	}	
}

i2b2.ExampComm.loadCalls = function(commNamespace){
	var selTrgt = $$("DIV#ExampComm-mainDiv SELECT#ExampComm-funcList")[0];
	// delete function call list
	while (selTrgt.firstChild) {
		selTrgt.removeChild(selTrgt.firstChild);
	}
	// populate list
	try {
		if (i2b2.h.isBadObjPath(commNamespace)) {
			alert("Sorry, an error occurred while trying to access the object. Recheck the object's location within the framework's namespace and try again.");
			return;
		}
		var co = eval("("+commNamespace+")");
		if (Object.getClass(co) == "i2b2Base_communicator") {
			// read all the AJAX calls registered to the object
			for (var fn in co._commData) {
				var n = new Option(fn, fn);
				selTrgt.options[selTrgt.length] = n;
			}
		} else {
			alert("The object was found but is not a standard Communicator object created by i2b2.hive.communicatorFactory()");
		}
	} catch (e) {
		alert("Sorry, an general error has occurred.");
	}	
}


i2b2.ExampComm.getTemplateVars = function(){
	try {
		var co = $$("DIV#ExampComm-mainDiv SELECT#ExampComm-objList")[0].value;
		var cc = $$("DIV#ExampComm-mainDiv SELECT#ExampComm-funcList")[0].value;

		if (co.length < 1 || cc.length < 1) {
			return false;
		}
		var commObj = eval("(" + co + ")");
		var commMsg = commObj._commData[cc];
		
		var syntax = /(^|.|\r|\n)(\{{{\s*(\w+)\s*}}})/; //matches symbols like '{{{ field }}}'
		var tags = [];
		commMsg.msg.scan(syntax, function(match){ tags.push(match[3])});
		tags = tags.uniq();
		var ret = {
			tags: tags,
			noEscape: commMsg.dont_escape_params,
			msg: commMsg.msg
		}
		// proxy server data
		ret.proxy_info = ''	
		var sUrl = i2b2[commObj.ParentCell].cfg.cellURL;
		sUrl = i2b2.h.Escape(sUrl);
		var t = new Template(commMsg.url, syntax);
		sUrl = t.evaluate({URL: sUrl});	
		ret.funcURL = sUrl;
		var sProxy_Url = i2b2.h.getProxy();
		if (sProxy_Url) {
			ret.proxy_info = '<proxy>\n            <redirect_url>' + sUrl + '</redirect_url>\n        </proxy>\n';
		} else {
			sProxy_Url = sUrl;
		}
		ret.proxyURL = sProxy_Url;
		return ret;
	} 
	catch (e) { 
		return false;
	}
}


i2b2.ExampComm.createTemplateGrid = function(){

	var co = $$("DIV#ExampComm-mainDiv SELECT#ExampComm-objList")[0].value;
	var cc = $$("DIV#ExampComm-mainDiv SELECT#ExampComm-funcList")[0].value;
	var domContainer = $$("DIV#ExampComm-mainDiv DIV.taglist")[0];
	var xTemplate = $('ExampComm-tagrowTEMPLATE');
	var xTags = i2b2.ExampComm.getTemplateVars();
				
	// save in global model
	i2b2.ExampComm.model.commObj = co;
	i2b2.ExampComm.model.commFunc = cc;
	i2b2.ExampComm.model.tags = {};
	i2b2.ExampComm.model.msg = xTags.msg;

	// clear existing input grid
	while (domContainer.firstChild) {
		domContainer.removeChild(domContainer.firstChild);
	}
	for (var i = 0; i < xTags.tags.length; i++) {
		var tagname = xTags.tags[i];
		
		i2b2.ExampComm.model.tags[tagname] = {};
		
		// clone the record DIV and add it to the display list
		var rec = xTemplate.cloneNode(true);
		rec.id ="";
		// change the fieldname
		var part = Element.select(rec, '.fieldname')[0];
		part.innerHTML = tagname;
		// is the field escaped
		var part = Element.select(rec,'.escaped')[0];
		if (xTags.noEscape.indexOf(tagname) != -1) {
			part.innerHTML = "&nbsp;";
			i2b2.ExampComm.model.tags[tagname].escaped = false;
		} else {
			i2b2.ExampComm.model.tags[tagname].escaped = true;
		}
		// populate the default values of common tags
		var tag_val = '';
		switch (tagname) {
			case "proxy_info":
				tag_val = xTags.proxy_info;
				break;
			case "sec_user":
				tag_val = i2b2.h.getUser();
				break;
			case "sec_pass_node":
			case "sec_pass":
				tag_val = i2b2.h.getPass();
				break;
			case "sec_domain":
				tag_val = i2b2.h.getDomain();
				break;
			case "sec_project":
				tag_val = i2b2.h.getProject();
				break;
			case "header_msg_id":
				tag_val = i2b2.h.GenerateAlphaNumId(20);
				break;
			case "result_wait_time":
				tag_val = 180;
				break;
			case "header_msg_datetime":
				tag_val = i2b2.h.GenerateISO8601DateTime();
				break;
		}
		var part = Element.select(rec, 'TEXTAREA')[0];
		part.value = tag_val;
		i2b2.ExampComm.model.tags[tagname].value = tag_val;
		// remove the break line if we are the last row
		if (i == xTags.tags.length - 1) {
			part = rec.select('.bline')[0];
			part.style.border = "none";
		}
		
		// attach the record into our DOM tree
		domContainer.appendChild(rec);
		Element.show(rec);
	}
	domContainer.show();	
}


i2b2.ExampComm.getResults = function() {
	// Refresh the display with info of the SDX record that was DragDropped
	if (i2b2.ExampComm.model.dirtyResultsData) {
		var dropRecord = i2b2.ExampComm.model.currentRec;
		$$("DIV#ExampComm-mainDiv DIV#ExampComm-TABS DIV.results-directions")[0].hide();
		$$("DIV#ExampComm-mainDiv DIV#ExampComm-TABS DIV.results-finished")[0].show();		
		var sdxDisplay = $$("DIV#ExampComm-mainDiv DIV#ExampComm-InfoSDX")[0];
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
	i2b2.ExampComm.model.dirtyResultsData = false;		
}
