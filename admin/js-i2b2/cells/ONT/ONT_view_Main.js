/**
 * @projectDescription	Special view controller for ONT that manages/routes events to "Find" tab or "Navigate" tab view controllers.
 * @inherits 	i2b2.ONT.view
 * @namespace	i2b2.ONT.view.main
 * @author		Nick Benik, Griffin Weber MD PhD
 * @version 	1.3
 * ----------------------------------------------------------------------------------------
 * updated 9-15-08: RC4 launch [Nick Benik] 
 */
console.group('Load & Execute component file: ONT > view > Main');
console.time('execute time');


// create and save the view object
i2b2.ONT.view.main = new i2b2Base_cellViewController(i2b2.ONT, 'main');
i2b2.ONT.view.main.visible = false;
// redefine the option functions
// ================================================================================================== //
i2b2.ONT.view.main.showOptions = function(subscreen) {
	// route the call to the correct screen
	if (i2b2.ONT.view[this.currentTab]) {
		i2b2.ONT.view[this.currentTab].showOptions(subscreen);
	}
}

// ================================================================================================== //
i2b2.ONT.view.main.selectTab = function(tabCode) {
	// toggle between the Navigate and Find Terms tabs
	switch (tabCode) {
		case "find":
			this.currentTab = 'find';
			this.cellRoot.view['nav'].hideView();
			this.cellRoot.view['find'].showView();
		break;
		case "nav":
			this.currentTab = 'nav';
			this.cellRoot.view['nav'].showView();
			this.cellRoot.view['find'].hideView();
		break;
	}
}


// ================================================================================================== //
i2b2.ONT.view.main.Resize = function(e) {
	// this function provides the resize functionality needed for this screen
	var viewObj = i2b2.ONT.view.main;
	var ve = $('ontMainBox');
	if (viewObj.visible) {
		//var ds = document.viewport.getDimensions();
	    var w =  window.innerWidth || (window.document.documentElement.clientWidth || window.document.body.clientWidth);
	    var h =  window.innerHeight || (window.document.documentElement.clientHeight || window.document.body.clientHeight);
		if (w < 840) {w = 840;}
		if (h < 517) {h = 517;}
		// resize our visual components
		ve.show();
		var ve = ve.style
		switch(i2b2.hive.MasterView.getViewMode()) {
			case "Patients":
				ve.width = Math.max(initBrowserViewPortDim.width-rightSideWidth, 0);
				//debugOnScreen("ONT.view.main.ResizeResize: style.width set to be " + ve.width );
				if (i2b2.WORK && i2b2.WORK.isLoaded) {
					var z = parseInt((h - 321)/2) + 16;
					ve.height = z;
				} else {
					ve.height = h-289;
				}
				
				break;
			case "Analysis":
				w = parseInt(w/3); 
				   // + 300;
				ve.width = w-10;				
				if (i2b2.WORK && i2b2.WORK.isLoaded) {
					var z = parseInt((h - 321)/2) + 16;
					ve.height = z;
				} else {
					ve.height = h-289;
				}
				break;
		}
		$$('DIV#ontMainBox DIV#ontNavDisp')[0].style.width = (parseInt(ve.width)-20) + 'px';  // was -20
		//$$('DIV#ontMainBox DIV#ontFindDisp')[0].style.width = (parseInt(ve.width)-20) + 'px';  // was -20
		//$$('DIV#ontMainBox DIV#ontTopTabs')[0].style.width = (parseInt(ve.width)-330) + 'px'; 		
		$$('DIV#ontMainBox DIV#ontFindFrameModifier')[0].style.width = (parseInt(ve.width)-14) + 'px';
		$$('DIV#ontMainBox DIV#ontSearchNamesResults')[0].style.width = (parseInt(ve.width)-14) + 'px';
		$$('DIV#ontMainBox DIV#ontSearchCodesResults')[0].style.width = (parseInt(ve.width)-14) + 'px';
		if (viewObj.isZoomed) { ve.height = h-93; }
	} else {
		ve.hide();
	}
}

//YAHOO.util.Event.addListener(window, "resize", i2b2.ONT.view.main.Resize, i2b2.ONT.view.main); // tdw9


/*
 * Adjust width of ontMainBox after users drags splitter
 */
i2b2.ONT.view.main.splitterDragged = function()
{
	var splitter = $( i2b2.hive.mySplitter.name );
	var ont = $("ontMainBox");
	ont.style.width	= Math.max((parseInt(splitter.style.left) - ont.offsetLeft - 3), 0) + "px";
	
	$$('DIV#ontMainBox DIV#ontNavDisp')[0].style.width = Math.max((parseInt(ont.style.width)-20), 0) + 'px';
	$$('DIV#ontMainBox DIV#ontSearchNamesResults')[0].style.width = Math.max((parseInt(ont.style.width)-14), 0) + 'px';
	$$('DIV#ontMainBox DIV#ontSearchCodesResults')[0].style.width = Math.max((parseInt(ont.style.width)-14), 0) + 'px';
}

/*
 * Window Resized
 */
i2b2.ONT.view.main.ResizeHeight = function() 
{
	// this function provides the resize functionality needed for this screen
	var viewObj = i2b2.ONT.view.main;
	var ve = $('ontMainBox');
	if (viewObj.visible) {
		//var ds = document.viewport.getDimensions();
	    var h =  window.innerHeight || (window.document.documentElement.clientHeight || window.document.body.clientHeight);
		if (h < 517) {h = 517;}
		// resize our visual components
		ve.show();
		var ve = ve.style
		switch(i2b2.hive.MasterView.getViewMode()) 
		{
			case "Patients":
				if (i2b2.WORK && i2b2.WORK.isLoaded) {
					var z = parseInt((h - 321)/2) + 16;
					ve.height = z;
				} else {
					ve.height = h-289;
				}
				break;
			case "Analysis":
				if (i2b2.WORK && i2b2.WORK.isLoaded) 
				{
					var z = parseInt((h - 321)/2) + 16;
					ve.height = z;
				} else {
					ve.height = h-289;
				}
				break;
		}
		if (viewObj.isZoomed) { ve.height = h-93; }
	} else {
		ve.hide();
	}
}


// ================================================================================================== //
i2b2.ONT.view.main.ZoomView = function() {
	i2b2.hive.MasterView.toggleZoomWindow("ONT");
}



// ================================================================================================== //
i2b2.ONT.view.main.hballoon = {
	canShowItemBalloons: true,
	delayItemBalloons: false,
	hideBalloons: function () {
		var thisObj = i2b2.ONT.view.main.hballoon;
		thisObj.canShowItemBalloons = false;
		clearTimeout(thisObj.delayItemBalloons);
		$('ontBalloonBox').hide();
		YAHOO.util.Event.removeListener(document, "mousemove", thisObj.showBalloons);
		YAHOO.util.Event.addListener(document, "mousemove", thisObj.showBalloons);
	},
	_showItemBalloons: function() {
		var thisObj = i2b2.ONT.view.main.hballoon;
		if (thisObj.canShowItemBalloons) {
			$('ontBalloonBox').show();
			YAHOO.util.Event.removeListener(document, "mousemove", thisObj.showBalloons);
		}
	},
	showBalloons: function(e) {
		var thisObj = i2b2.ONT.view.main.hballoon;
		var x = YAHOO.util.Event.getPageX(e);
		var y = YAHOO.util.Event.getPageY(e);
		var elW = $('ontNavDisp').getWidth();
		if (isNaN(elW)) {elW = 200;}
		var elY = $('ontNavDisp').getHeight;
		if (isNaN(elY)) {elY = 390;}
		elY = elY + 100 - 112;
		if ( (x < 15-5) || (x > elW+20+5) || (y < elY-5) || (y > elY+117+5) ) {
			if (!thisObj.canShowItemBalloons) {
				thisObj.canShowItemBalloons = true;
				thisObj.delayItemBalloons = setTimeout("i2b2.ONT.view.main.hballoon._showItemBalloons()",200);
			}
		} else {
			thisObj.canShowItemBalloons = false;
			clearTimeout(thisObj.delayItemBalloons);
		}
	}
}
// ================================================================================================== //



// This is done once the entire cell has been loaded
// ================================================================================================== //
console.info("SUBSCRIBED TO i2b2.events.afterCellInit");
i2b2.events.afterCellInit.subscribe(
	(function(en,co) {
		if (co[0].cellCode=='ONT') {
// -------------------------------------------------------
			console.info("EVENT RECEIVED i2b2.events.afterCellInit; Data:",en,co);
			// perform visual actions
			i2b2.ONT.view.main.currentTab = 'nav';  	// define the initial view (AKA "tab") is visible
			i2b2.ONT.view.main.Resize();
			$('ontMainDisp').show();
						
// -------------------------------------------------------
		}
	})
);

//================================================================================================== //
i2b2.events.initView.subscribe((function(eventTypeName, newMode) {
// -------------------------------------------------------
	newMode = newMode[0];
	this.viewMode = newMode;
	this.visible = true;
	$('ontMainBox').show();
	this.Resize();
	
	i2b2.hive.mySplitter.events.ONTInitialized.fire(); 	// initialize splitter's starting position
// -------------------------------------------------------
}),'',i2b2.ONT.view.main);


// ================================================================================================== //
i2b2.events.changedViewMode.subscribe((function(eventTypeName, newMode) {
// -------------------------------------------------------
	newMode = newMode[0];
	this.viewMode = newMode;
	switch(newMode) {
		case "Patients":
		case "Analysis":
			var wlst = i2b2.hive.MasterView.getZoomWindows();
			if (wlst.indexOf("HISTORY")!=-1 || wlst.indexOf("WORK")!=-1) { return; }
			this.visible = true;
			$('ontMainBox').show();
			i2b2.ONT.view.main.splitterDragged();
			//;this.Resize(); //tdw9
			break;
		default:
			this.visible = false;
			$('ontMainBox').hide();
			i2b2.ONT.view.main.splitterDragged();
			//this.Resize(); //tdw9
			break;
	}
// -------------------------------------------------------
}),'',i2b2.ONT.view.main);


// ================================================================================================== //
i2b2.events.changedZoomWindows.subscribe((function(eventTypeName, newMode) {
// -------------------------------------------------------
	newMode = newMode[0];
	if (!newMode.action) { return; } 
	if (newMode.action == "ADD") {
		switch (newMode.window) {
			case "ONT":
				this.isZoomed = true;
				this.visible = true;
				break;
			case "HISTORY":
			case "WORK":
				this.visible = false;
				this.isZoomed = false;
		}
	} else {
		switch (newMode.window) {
			case "ONT":
			case "HISTORY":
			case "WORK":
				this.isZoomed = false;
				this.visible = true;
		}
	}
	this.ResizeHeight();
	this.splitterDragged();
// -------------------------------------------------------
}),'',i2b2.ONT.view.main);


console.timeEnd('execute time');
console.groupEnd();