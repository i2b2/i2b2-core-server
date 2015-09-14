/**
 * @projectDescription	The controller for the Plugin Viewer window (GUI-only controller).
 * @inherits	i2b2.PLUGINMGR.view
 * @namespace	i2b2.PLUGINMGR.view.PluginView
 * @author	Nick Benik, Griffin Weber MD PhD
 * @version 	1.3
 * ----------------------------------------------------------------------------------------
 * updated 10-30-08: Initial Launch [Nick Benik] 
 */
console.group('Load & Execute component file: PLUGINMGR > view > PlugView');
console.time('execute time');


// ********* View: PlugView ********* 
// create and save the view object
i2b2.PLUGINMGR.view.PlugView = new i2b2Base_cellViewController(i2b2.PLUGINMGR, 'PlugView');
i2b2.PLUGINMGR.view.PlugView.visible = false;


/*
 * Adjust width of PLUGINMGR after users drags splitter
 */
//================================================================================================== //
i2b2.PLUGINMGR.view.PlugView.splitterDragged = function()
{
	//var viewPortDim = document.viewport.getDimensions();
	
	    var w =  window.innerWidth || (window.document.documentElement.clientWidth || window.document.body.clientWidth);
	
	var splitter = $( i2b2.hive.mySplitter.name );
	var pluginBox = $("anaPluginViewBox");
	
	if (splitter.style.visibility=="visible")
	{
		var basicWidth	= parseInt(w) - parseInt(splitter.style.left) - parseInt(splitter.offsetWidth);
		pluginBox.style.left				= parseInt(splitter.offsetWidth) + parseInt(splitter.style.left) + 3 + "px";
		pluginBox.style.width				= Math.max(basicWidth - 24, 0) + "px";
	}
	else
	{
		pluginBox.style.left				= 15 + "px";
		pluginBox.style.width				= Math.max( parseInt(w)-36, 0) + "px";
	}	
}

// ================================================================================================== //
i2b2.PLUGINMGR.view.PlugView.ResizeHeight = function(e) {
	// this function provides the resize functionality needed for this screen
	var viewObj = i2b2.PLUGINMGR.view.PlugView;
	var ve = $('anaPluginViewBox');
	if (viewObj.visible) {
		
	    var h =  window.innerHeight || (window.document.documentElement.clientHeight || window.document.body.clientHeight);

		//var ds = document.viewport.getDimensions();
		//var h = ds.height;
		if (h < 517) {h = 517;}
		ve.show();
		// resize our visual components
		switch(i2b2.hive.MasterView.getViewMode()) {
			case "Admin":
				break;
			case "Analysis":
				ve.show();
				if (i2b2.WORK && i2b2.WORK.isLoaded) {
					var z = h - 390 + 142;
				} else {
					var z = h - 390 + 100;
				}
				$('anaPluginViewFrame').style.height = z;
				break;

			case "AnalysisZoomed":
				ve.show();
				h = h - 98;
				ve.style.left = '';
				$('anaPluginViewFrame').style.height = h;				
				break;
		}
		var t = i2b2.PLUGINMGR.ctrlr.main.currentPluginCtrlr;
		if (t) {
			if (t.cfg.config.plugin.standardTabs) {
				// special resizing for tabs
				var tn = $('anaPluginViewBox').select('DIV.yui-content')[0];
				if (i2b2.hive.MasterView.getViewMode() == "AnalysisZoomed") {
					tn.style.height = (h - 34);
				} else {
					tn.style.height = (h - 283);
				}
			}
			if (t.Resize) {
				// a plugin is currently loaded and has a Resize() routine, pass it redraw data
				var rdp = Element.cumulativeOffset(ve);
				var rdd = Element.getDimensions(ve);
				var rd = {
					height: rdd.height,
					width: rdd.width,
					left: rdp.left,
					top: rdp.top
				};
				t.Resize(rd);
			}
		}
	} else {
		ve.hide();
	}
}


// ================================================================================================== //
i2b2.PLUGINMGR.view.PlugView.Resize = function(e) {
	// this function provides the resize functionality needed for this screen
	var viewObj = i2b2.PLUGINMGR.view.PlugView;
	var ve = $('anaPluginViewBox');
	if (viewObj.visible) {
		//var ds = document.viewport.getDimensions();
		//var w = ds.width;
		//var h = ds.height;
		
	    var w =  window.innerWidth || (window.document.documentElement.clientWidth || window.document.body.clientWidth);
	    var h =  window.innerHeight || (window.document.documentElement.clientHeight || window.document.body.clientHeight);

		if (w < 840) {w = 840;}
		if (h < 517) {h = 517;}
		ve.show();
		// resize our visual components
		switch(i2b2.hive.MasterView.getViewMode()) {
			case "Analysis":
				ve.show();
				w = w - 18;
				ve.style.left = parseInt(w/3) + 22;
				ve.style.width = (parseInt(w/3)*2) - 28;
				if (i2b2.WORK && i2b2.WORK.isLoaded) {
					var z = h - 390 + 142;
				} else {
					var z = h - 390 + 100;
				}
				$('anaPluginViewFrame').style.height = z;
				break;

			case "AnalysisZoomed":
				ve.show();
				w = w - 40;
				h = h - 98;
//				t.style.left = 16;
//				t.style.width = w;
//				var attribs1 = { width: {to: w}, left: {to: 16} };
//				var anim1 = new YAHOO.util.Anim('anaPluginViewBox', attribs1, 1, YAHOO.util.Easing.easeOut); 
//				anim1.animate();
//				var attribs2 = {height: {to: h}};
//				var anim2 = new YAHOO.util.Anim('anaPluginViewFrame', attribs2, 1, YAHOO.util.Easing.easeOut); 
//				anim2.animate();
				ve.style.width = w;
				ve.style.left = '';
				$('anaPluginViewFrame').style.height = h;				
				break;
		}
		var t = i2b2.PLUGINMGR.ctrlr.main.currentPluginCtrlr;
		if (t) {
			if (t.cfg.config.plugin.standardTabs) {
				// special resizing for tabs
				var tn = $('anaPluginViewBox').select('DIV.yui-content')[0];
				if (i2b2.hive.MasterView.getViewMode() == "AnalysisZoomed") {
					tn.style.height = (h - 34);
				} else {
					tn.style.height = (h - 283);
				}
/*
				var tn = tn.parentNode;
				while (tn && tn != i2b2.PLUGINMGR.ctrlr.main.currentPluginParentDiv ) {
					// size the parent node as well and dig up the tree
					tn.style.height = h;
					tn = tn.parentNode;
				}
*/
			}
			if (t.Resize) {
				// a plugin is currently loaded and has a Resize() routine, pass it redraw data
				var rdp = Element.cumulativeOffset(ve);
				var rdd = Element.getDimensions(ve);
				var rd = {
					height: rdd.height,
					width: rdd.width,
					left: rdp.left,
					top: rdp.top
				};
				t.Resize(rd);
			}
		}
	} else {
		ve.hide();
	}
}
// attach resize events
//YAHOO.util.Event.addListener(window, "resize", i2b2.PLUGINMGR.view.PlugView.Resize, i2b2.PLUGINMGR.view.PlugView); // tdw9


// ================================================================================================== //
i2b2.PLUGINMGR.view.PlugView.show = function() {
	i2b2.PLUGINMGR.view.PlugView.visible = true;
	$('anaPluginViewBox').show();
	i2b2.PLUGINMGR.view.PlugView.ResizeHeight();
	var t = i2b2.PLUGINMGR.ctrlr.main.currentPluginCtrlr;
	if (t && t.wasShown) {
		t.wasShown();
	}
};


// ================================================================================================== //
i2b2.PLUGINMGR.view.PlugView.hide = function() {
	i2b2.PLUGINMGR.view.PlugView.visible = false;
	$('anaPluginViewBox').hide();
	var t = i2b2.PLUGINMGR.ctrlr.main.currentPluginCtrlr;
	if (t && t.wasHidden) {
		t.wasHidden();
	}
};


// ================================================================================================== //
i2b2.PLUGINMGR.view.PlugView.UsesTabs = function(bIn) {
	i2b2.PLUGINMGR.view.PlugView._isUsingTabs = bIn;
	if (bIn) {
		// use tabs
		$('anaPluginViewFrame').addClassName('USETABS');
//		$$('#ExampTabs-mainDiv .yui-content')[0].style.height = (parseInt($('anaPluginViewFrame').style.height) - 34)+'px';
	} else {
		// don't use tabs
		$('anaPluginViewFrame').removeClassName('USETABS');		
	}
}



// process view mode changes (via EVENT CAPTURE)
// ================================================================================================== //
i2b2.events.changedViewMode.subscribe((function(eventTypeName, newMode) {
	newMode = newMode[0];
	this.viewMode = newMode;
	switch(newMode) {
		case "AnalysisZoomed":
		case "Analysis":
			// check if other windows are zoomed and blocking us
			var zw = i2b2.hive.MasterView.getZoomWindows();
			if (zw.member("PLUGINLST"))
				this.view.PlugView.hide();
			else 
			{
				this.view.PlugView.show();
				var splitter = $( i2b2.hive.mySplitter.name );
				if ( newMode === "AnalysisZoomed")
					splitter.style.visibility="hidden";
				else
					splitter.style.visibility="visible";
				i2b2.PLUGINMGR.view.PlugView.splitterDragged();
				// bug fix for IE
				//setTimeout("i2b2.PLUGINMGR.view.PlugView.Resize()", 100); // tdw9
			}
			break;
		default:
			this.view.PlugView.hide();
			var splitter = $( i2b2.hive.mySplitter.name ); // make splitter visible
			splitter.style.visibility="visible";
			break;
	}
}),'',i2b2.PLUGINMGR);


// ================================================================================================== //
i2b2.PLUGINMGR.view.PlugView.ZoomView = function() {
	i2b2.hive.MasterView.toggleZoomWindow("PLUGINLST");
}


//================================================================================================== //
i2b2.events.initView.subscribe((function(eventTypeName, zoomMsg) 
{
	newMode = zoomMsg[0];
	if (!newMode.action) { return; }
	if (newMode.window == "PLUGINLST") {
		if (newMode.action == "ADD") {
			this.visible = false;
			this.isZoomed = false;
		} else {
			this.isZoomed = false;
			this.visible = true;
		}
		this.Resize();
	}
}),'',i2b2.PLUGINMGR.view.PlugView);



// ================================================================================================== //
i2b2.events.changedZoomWindows.subscribe((function(eventTypeName, zoomMsg) {
	newMode = zoomMsg[0];
	if (!newMode.action) { return; }
	if (newMode.window == "PLUGINLST") {
		if (newMode.action == "ADD") {
			this.visible = false;
			this.isZoomed = false;
		} else {
			this.isZoomed = false;
			this.visible = true;
		}
		this.ResizeHeight();
	}
}),'',i2b2.PLUGINMGR.view.PlugView);



i2b2.PLUGINMGR.view.PlugView.showOptions = function() {
	var t = i2b2.PLUGINMGR.ctrlr.main.currentPluginCtrlr;
	if (t && t.ShowOptions) {
		t.ShowOptions();
	}
};

// set image icon
$('plugviewZoomImg').src = i2b2.PLUGINMGR.cfg.config.assetDir+"zoom_icon.gif"

console.timeEnd('execute time');
console.groupEnd();
