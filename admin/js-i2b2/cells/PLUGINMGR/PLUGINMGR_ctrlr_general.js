/**
 * @projectDescription	Main controller for the Plugin Manager system.
 * @inherits	i2b2
 * @namespace	i2b2.PLUGINMGR
 * @author	Nick Benik, Griffin Weber MD PhD
 * @version 	1.4
 * ----------------------------------------------------------------------------------------
 * updated 9-18-09: Updates required by PM update [Nick Benik]
 */
console.group('Load & Execute component file: PLUGINMGR > ctrl > general');
console.time('execute time');


// helper function to be used by plugins to create canvas objects
i2b2.PLUGINMGR.createCanvas = function(containerDIV){
	// we must dynamically create our CANVAS node
	var canvas = document.createElement("canvas");
	var trgtDIV = $(containerDIV);
	if (!trgtDIV.appendChild) {
		console.error("[i2b2.PLUGINMGR.createCanvas] Bad canvas container DIV provided");
		return false;
	}
	trgtDIV.appendChild(canvas);
	
	// dynamically initialize canvas object (only on IE)
	if (/msie/i.test(navigator.userAgent)) {
	    var guid = i2b2.h.GenerateAlphaNumId(6);
		canvas.id = guid;
		G_vmlCanvasManager.initElement(canvas);
		var canvas = $(guid);
		canvas.id = "";
	}
	return canvas;	
};


// main plugin handler
i2b2.PLUGINMGR.visible = false;
i2b2.PLUGINMGR.ctrlr.main = 
{
	ZoomView: function() {
		if (i2b2.hive.MasterView.getViewMode()=="Analysis") {
			i2b2.hive.MasterView.setViewMode('AnalysisZoomed');
		} else {
			i2b2.hive.MasterView.setViewMode('Analysis');
		}
	},
/*
	getPluginList: function() {
		var results = {};
		var cellsLoaded = i2b2.hive.cfg.LoadedCells;
		for (var i1=0; i1<cellsLoaded.length; i1++) {
			var pluginName = cellsLoaded[i1].code;
			if (!i2b2[pluginName].cfg.config.plugin) {  pluginName = false; }
			if (pluginName) {
				results[pluginName] = i2b2[pluginName];
			}
		}
		return results;
	},
*/
	selectPlugin: function(pluginCode) {
		// function to load selected plugin
		if (i2b2.h.isBadObjPath('i2b2.'+pluginCode+'.cfg.config.plugin')) {
			alert("The plugin you clicked does not contain all the required information needed to load.");
			return false;
		}
		
		// send unload single to any plugin that is currently loaded
		if (!i2b2.h.isBadObjPath('i2b2.PLUGINMGR.ctrlr.main.currentPluginCtrlr')) {
			// Something loaded. fire Unload() and exit if cancel is requested
			var t = i2b2.PLUGINMGR.ctrlr.main.currentPluginCtrlr.Unload;
			if (!t || t()) {
				// remove plugin name from tab
				try {
					$('anaPluginViewBox').select('DIV.tabBox DIV')[0].innerHTML = "Plugin Viewer";
				} catch(e) {
					console.warn("Could not change PluginViewBox tab label!");
				}
				// clear plugin view window
				if (i2b2.PLUGINMGR.ctrlr.main.currentPluginCtrlr.cfg.config.plugin.isolateHtml) {
					// clear IFRAME
					var doc = $('anaPluginIFRAME').contentDocument;
					if (!doc) {var doc = $('anaPluginIFRAME').contentWindow.document; }
					doc.open(); 
					doc.write('Select a plugin to load from the "Plugins" window.');
					doc.close();
				} else {
					// clear DIV
					var trgt = $('anaPluginViewFrame');
					while (trgt.childNodes.length > 0) { trgt.removeChild(trgt.firstChild); }
					trgt.innerHTML = '<div class="initialMsg">Select a plugin to load from the "Plugins" window.</div>';
				}				
				delete i2b2.PLUGINMGR.ctrlr.main.currentPluginCtrlr;
			} else {
				return false;
			}
		}
		
		// show the plugin name on the main container tab
		try {
			if (i2b2[pluginCode].cfg.config.short_name) {
				sName = i2b2[pluginCode].cfg.config.short_name;
			} else {
				sName = i2b2[pluginCode].cfg.config.name;
			}
			$('anaPluginViewBox').select('DIV.tabBox DIV')[0].innerHTML = sName;
		} catch(e) {
			console.warn("Could not change PluginViewBox tab label!");
		}
		// LOAD PLUGIN GUI
		if (!i2b2.h.isBadObjPath('i2b2.'+pluginCode+'.cfg.config.plugin.iframe')) {
			// the plugin requires loading into an IFRAME			
		} else if (!i2b2.h.isBadObjPath('i2b2.'+pluginCode+'.cfg.config.plugin.html')) {
			// the plugin requires direct loading into the DOM tree
			var url = i2b2[pluginCode].cfg.config.assetDir + i2b2[pluginCode].cfg.config.plugin.html.source;
			var response = new Ajax.Request(url, {method: 'get', asynchronous: false});
			console.dir(response);
			if (response.transport.statusText=="OK") {
				// load the html into the IFRAME
				var doc = $('anaPluginIFRAME').contentDocument;
				if (!doc) {var doc = $('anaPluginIFRAME').contentWindow.document; }
				doc.open();
				doc.write(response.transport.responseText);
				doc.close();
				// get the main screen DIV
				var mainDiv = doc.getElementById(i2b2[pluginCode].cfg.config.plugin.html.mainDivId);
				if (!mainDiv) {
					alert("The Plugin's screen was loaded but had errors.");
					return false;
				}
				// clear plugin view window
				var trgt = $('anaPluginViewFrame');
				while (trgt.childNodes.length > 0) { trgt.removeChild(trgt.firstChild); }
				// copy the div to our working window and display it.
				if (mainDiv.outerHTML) {
					// fix for IE issues
					trgt.innerHTML = mainDiv.outerHTML;
					var scrn = trgt.firstChild;
					scrn.style.display = '';
				} else {
					var scrn = mainDiv.cloneNode(true);
					trgt.appendChild(scrn);
					scrn.show();
				}
				// perform GUI changes if the plugin is going to be using standard tabs at the top
				var useTabs = false;
				if (!i2b2.h.isBadObjPath('i2b2.'+pluginCode+'.cfg.config.plugin.standardTabs')) {
					useTabs = Boolean.parseTo(i2b2[pluginCode].cfg.config.plugin.standardTabs);
				}
				i2b2.PLUGINMGR.view.PlugView.UsesTabs(useTabs);
				// show plugin
				trgt.show();
				// notify the plugin's controller class to activate
				try {
					i2b2[pluginCode].Init(scrn);
					i2b2.PLUGINMGR.ctrlr.main.currentPluginCtrlr = i2b2[pluginCode];
					i2b2.PLUGINMGR.ctrlr.main.currentPluginParentDiv = scrn;
				} catch(e) {
					alert('An error has occurred while trying to initialize the Plugin.');
				}
				// fire the plugin's Resize() function
				i2b2.PLUGINMGR.view.PlugView.ResizeHeight();
				// Reconnect any drag drop handlers that have been dropped, if this plugin has been previously loaded (a YUI issue)
				YAHOO.util.DDM.reattachHandlers();
			} else {
				alert('A problem was encounter while loading the plugin.');
				return false;
			}
		} else {
			alert("The plugin you clicked does not specify how it needs to be loaded.");
			return false;
		}
	}	
};



console.timeEnd('execute time');
console.groupEnd();
