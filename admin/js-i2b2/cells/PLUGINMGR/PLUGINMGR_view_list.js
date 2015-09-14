/**
 * @projectDescription	View controller for the Plugin list window (GUI-related functionality).
 * @inherits	i2b2.PLUGINMGR.view
 * @namespace	i2b2.PLUGINMGR.view.list
 * @author	Nick Benik, Griffin Weber MD PhD
 * @version 	1.5
 * ----------------------------------------------------------------------------------------
 * updated 9-18-09: Updates required by PM update [Nick Benik]
 */
console.group('Load & Execute component file: PLUGINMGR > view > list');
console.time('execute time');


// ********* View: List ********* 
// create and save the view object
i2b2.PLUGINMGR.view.list = new i2b2Base_cellViewController(i2b2.PLUGINMGR, 'list');
i2b2.PLUGINMGR.view.list.visible = false;


/*
 * Adjust width of PLUGINMGR after users drags splitter
 */
//================================================================================================== //
i2b2.PLUGINMGR.view.list.splitterDragged = function()
{
	//var viewPortDim = document.viewport.getDimensions();
	
	var w =  window.innerWidth || (window.document.documentElement.clientWidth || window.document.body.clientWidth);
	
	var splitter = $( i2b2.hive.mySplitter.name );
	var pluginListBox = $("anaPluginListBox");
	
	var basicWidth	= parseInt(w) - parseInt(splitter.style.left) - parseInt(splitter.offsetWidth);

	pluginListBox.style.left				= parseInt(splitter.offsetWidth) + parseInt(splitter.style.left) + 3 + "px";
	pluginListBox.style.width				= Math.max(basicWidth - 24, 0) + "px";	
}

//================================================================================================== //
i2b2.PLUGINMGR.view.list.ResizeHeight = function(e) {
	var viewObj = i2b2.PLUGINMGR.view.list;
	if (viewObj.visible) {
		//var ds = document.viewport.getDimensions();
		
	    var h =  window.innerHeight || (window.document.documentElement.clientHeight || window.document.body.clientHeight);

		//var h = ds.height;
		if (h < 517) {h = 517;}	
		// resize our visual components
		var ve = $('anaPluginListBox').style;
		var le = $('anaPluginList').style;
		switch(viewObj.viewMode) {
			case "Analysis":
				if (viewObj.isZoomed) {
						le.height = h-95-27;
						ve.top = '';
				} else {
					if (i2b2.WORK && i2b2.WORK.isLoaded) {
						le.height = 102-27;
						ve.top = h-196+44;
					} else {
						le.height = 146-28;
						ve.top = h-196;
					}
				}
				break;
			default:
				break;
		}
	}
}


// ================================================================================================== //
i2b2.PLUGINMGR.view.list.Resize = function(e) {
	var viewObj = i2b2.PLUGINMGR.view.list;
	if (viewObj.visible) {
		
	    var w =  window.innerWidth || (window.document.documentElement.clientWidth || window.document.body.clientWidth);
	    var h =  window.innerHeight || (window.document.documentElement.clientHeight || window.document.body.clientHeight);

		//var ds = document.viewport.getDimensions();
		//var w = ds.width;
		//var h = ds.height;
		if (w < 840) {w = 840;}
		if (h < 517) {h = 517;}	
		// resize our visual components
		var ve = $('anaPluginListBox').style;
		var le = $('anaPluginList').style;
		switch(viewObj.viewMode) {
			case "Analysis":
				w = w - 18;
				ve.left = parseInt(w/3) + 22;
				ve.width = (parseInt(w/3)*2) - 28;
				if (viewObj.isZoomed) {
						le.height = h-95-27;
						ve.top = '';
				} else {
					if (i2b2.WORK && i2b2.WORK.isLoaded) {
						le.height = 102-27;
						ve.top = h-196+44;
					} else {
						le.height = 146-28;
						ve.top = h-196;
					}
				}
				break;
			default:
				break;
		}
	}
}
// attach resize events
//YAHOO.util.Event.addListener(window, "resize", i2b2.PLUGINMGR.view.list.Resize, i2b2.PLUGINMGR.view.list); // tdw9



// ================================================================================================== //
i2b2.PLUGINMGR.view.list.show = function() {
	var t = i2b2.PLUGINMGR.view.list;
	t.visible = true;
	$('anaPluginListBox').show();
	t.Resize();
};


// ================================================================================================== //
i2b2.PLUGINMGR.view.list.hide = function() {
	i2b2.PLUGINMGR.view.list.visible = false;
	$('anaPluginListBox').hide();
};


// process view mode changes (via EVENT CAPTURE)
// ================================================================================================== //
i2b2.events.changedViewMode.subscribe((function(eventTypeName, newMode) {
	newMode = newMode[0];
	var t = i2b2.PLUGINMGR.view.list;
	t.viewMode = newMode;
	switch(newMode) {
		case "Analysis":
			t.show();
			t.BuildCategories();
			t.Render();
			t.splitterDragged();
			//t.Resize();
			break;
		default:
			t.hide();
			break;
	}
}),'',i2b2.PLUGINMGR);


i2b2.PLUGINMGR.view.list.BuildCategories = function() {
	// clear plugins category list
	var domDD = $('anaPluginCats');
	while( domDD.hasChildNodes() ) { domDD.removeChild( domDD.lastChild ); }
	var catList = [];

	var cellsLoaded = i2b2.hive.cfg.LoadedCells;
	for (var idx in cellsLoaded) {
		if (cellsLoaded[idx]) {
			if (i2b2[idx].cfg.config.plugin) {
				catList.push(i2b2[idx].cfg.config.category);
			}
		}
	}
	// flattent and remove duplicates
	catList = catList.flatten();
	catList = catList.uniq();

	// remove non-relevent categories
	catList = catList.without('cell','celless','core','plugin');

	// populate dropdown list
	var dno = document.createElement('OPTION');
	dno.setAttribute('value', "ALL");
	var dnt = document.createTextNode("ALL");
	dno.appendChild(dnt);
	domDD.appendChild(dno);
	for (var i1=0; i1<catList.length; i1++) {
		var dno = document.createElement('OPTION');
		dno.setAttribute('value', catList[i1]);
		var dnt = document.createTextNode(catList[i1]);
		dno.appendChild(dnt);
		domDD.appendChild(dno);
	}

}


i2b2.PLUGINMGR.view.list.Render = function() {
	// clear plugins List
	var domContainer = $('anaPluginList');
	domContainer.hide();
	while( domContainer.hasChildNodes() ) { domContainer.removeChild( domContainer.lastChild ); }

	var cn = $("anaPluginCats");
	if (!cn) {
		var cFilter = false;
	} else {
		var cFilter = cn.options[cn.selectedIndex].value;
	}

	// get our render template	
	var xTemplate = $('anaPluginView');
	xTemplate = xTemplate.options[xTemplate.selectedIndex].value;
	if (xTemplate == "DETAIL") {
		xTemplate = $('plugListRecDETAIL-CLONE');
		var xIconVarName = 'size32x32';
	} else {
		xTemplate = $('plugListRecSUMMARY-CLONE');
		var xIconVarName = 'size16x16';
	}
	
	
	// loop through all plugins in the framework
	var cellsLoaded = i2b2.hive.cfg.LoadedCells;
	for (var pluginName in cellsLoaded) {
		if (cellsLoaded[pluginName] && !i2b2[pluginName].cfg.config.plugin) { 
			pluginName = false; 
		} else if (cFilter!="ALL" && i2b2[pluginName].cfg.config.category.indexOf(cFilter)==-1) { 
			pluginName = false; 
		}
		if (pluginName) {
			// clone the record DIV and add it to the display list
			var rec = xTemplate.cloneNode(true);
			// change the entry id
			rec.id = "pluginViewList-"+pluginName;
			var t_ref = i2b2[pluginName].cfg.config;
			// extract the 
			// change the plugin's icon
			try {
				var part = rec.select('.Icon > IMG')[0];
				if (t_ref.icons[xIconVarName]) {
					part.src = t_ref.assetDir + t_ref.icons[xIconVarName];
				} else {
					part.src = i2b2.PLUGINMGR.cfg.config.assetDir+i2b2.PLUGINMGR.cfg.config.defaultListIcons[xIconVarName];
				}
			} catch(e) {}
			// change name and description
			try {
				var part = rec.select('DIV.Name')[0];
				if (t_ref.name) {
					part.innerHTML = t_ref.name;
				} else {
					part.innerHTML = pluginName + " Plugin";
				}
			} catch(e) {}
			try {
				var part = rec.select('DIV.Descript')[0];
				if (t_ref.description) {
					part.innerHTML = t_ref.description;
				} else {
					part.innerHTML = "<I>No description available.</I>";
				}
			} catch(e) {}
			// attach the record into our DOM tree
			domContainer.appendChild(rec);
			YAHOO.util.Event.addListener(rec.id, "click", i2b2.PLUGINMGR.view.list.recordClick); 
			rec.show();
		}
	}
	domContainer.show();
};

i2b2.PLUGINMGR.view.list.recordClick = function(e) {
	var tn;
	if (this.hasClassName('pluginRecordBox')) {
		var loc = this.id.indexOf('-') + 1;
		if (loc > 0) {
			var pCode = this.id.substr(loc);
			i2b2.PLUGINMGR.ctrlr.main.selectPlugin(pCode);
			// GUI - unzoom to show plugin loaded
			if (i2b2.PLUGINMGR.view.list.isZoomed) {
				i2b2.PLUGINMGR.view.list.ZoomView();
			}
		}
	}
}

// ================================================================================================== //
i2b2.PLUGINMGR.view.list.ZoomView = function() {
	i2b2.hive.MasterView.toggleZoomWindow("PLUGINLST");
}



// ================================================================================================== //
i2b2.events.changedZoomWindows.subscribe((function(eventTypeName, zoomMsg) {
	newMode = zoomMsg[0];
	if (!newMode.action) { return; }
	if (newMode.window == "PLUGINLST") {
		if (newMode.action == "ADD") {
			this.visible = true;
			this.isZoomed = true;
		} else {
			this.isZoomed = false;
			this.visible = true;
		}
		this.ResizeHeight();
	}
}),'',i2b2.PLUGINMGR.view.list);

i2b2.PLUGINMGR.view.list.showOptions = function() {
	alert('show options for plugin viewer list');
}


// set image icon
$('pluglstZoomImg').src = i2b2.PLUGINMGR.cfg.config.assetDir+"zoom_icon.gif";


console.timeEnd('execute time');
console.groupEnd();