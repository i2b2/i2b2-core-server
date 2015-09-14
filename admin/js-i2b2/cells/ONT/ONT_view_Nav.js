/**
 * @projectDescription	View controller for ONT's "Navigate Terms" tab.
 * @inherits 	i2b2.ONT.view
 * @namespace	i2b2.ONT.view.nav
 * @author		Nick Benik, Griffin Weber MD PhD
 * @version 	1.3
 * ----------------------------------------------------------------------------------------
 * updated 9-15-08: RC4 launch [Nick Benik] 
 */
console.group('Load & Execute component file: ONT > view > Nav');
console.time('execute time');


// create and save the view object
i2b2.ONT.view.nav = new i2b2Base_cellViewController(i2b2.ONT, 'nav');
i2b2.ONT.view.nav.visible = false;
i2b2.ONT.view.nav.modifier = false; 


// define the option functions
// ================================================================================================== //
i2b2.ONT.view.nav.showOptions = function(subScreen) {
	if (!this.modalOptions) {
		var handleSubmit = function() {
			// submit value(s)
			if(this.submit()) {
				i2b2.ONT.view['nav'].params.max = parseInt($('ONTNAVMaxQryDisp').value,10);
				i2b2.ONT.view['nav'].params.synonyms = $('ONTNAVshowSynonyms').checked;
				i2b2.ONT.view['nav'].params.hiddens = $('ONTNAVshowHiddens').checked;
				i2b2.ONT.view['nav'].params.modifiers = $('ONTNAVdisableModifiers').checked;
				i2b2.ONT.view.nav.doRefreshAll();
			}
		}
		var handleCancel = function() {
			this.cancel();
		}
		this.modalOptions = new YAHOO.widget.SimpleDialog("optionsOntNav",
		{ width : "400px", 
			fixedcenter : true, 
			constraintoviewport : true, 
			modal: true,
			zindex: 700,
			buttons : [ { text:"OK", handler:handleSubmit, isDefault:true }, 
				    { text:"Cancel", handler:handleCancel } ] 
		} ); 
		$('optionsOntNav').show();
		this.modalOptions.validate = function() {
			if (parseInt($('ONTNAVMaxQryDisp').value,10) <= 0) {
				alert('You must display at least one child!');
				return false;
			}
			return true;
		};
		this.modalOptions.render(document.body);
	}
	this.modalOptions.show();
	// load settings from html
	i2b2.ONT.view['nav'].params.max = parseInt($('ONTNAVMaxQryDisp').value,10);
	i2b2.ONT.view['nav'].params.synonyms = $('ONTNAVshowSynonyms').checked;
	i2b2.ONT.view['nav'].params.hiddens = $('ONTNAVshowHiddens').checked;
	i2b2.ONT.view['nav'].params.modifiers = $('ONTNAVdisableModifiers').checked;

	//$('ONTNAVMaxQryDisp').value = this.params.max;
	//$('ONTNAVshowSynonyms').checked = parseBoolean(this.params.synonyms);
	//$('ONTNAVshowHiddens').checked = parseBoolean(this.params.hiddens);		
}

// ================================================================================================== //
i2b2.ONT.view.nav.showView = function() {
	$('tabNavigate').addClassName('active'); 
	$('ontNavDisp').style.display = 'block';
}

// ================================================================================================== //
i2b2.ONT.view.nav.hideView = function() {
	$('tabNavigate').removeClassName('active');
	$('ontNavDisp').style.display = 'none';
}

// ================================================================================================== //
i2b2.ONT.view.nav.ToggleNode = function(divTarg, divTreeID) {
	// get the i2b2 data from the yuiTree node
	var tvTree = YAHOO.widget.TreeView.findTreeByChildDiv(divTarg.id);
	var tvNode = tvTree.getNodeByProperty('nodeid', divTarg.id);
	tvNode.toggle();
}

// ================================================================================================== //
i2b2.ONT.view.nav.PopulateCategories = function() {		
	// insert the categories nodes into the Nav Treeview
	console.info("Populating Nav treeview with Categories");
	if (!this.yuiTree) { 
		console.error("YUI Treeview not set");
		return false; 
	}
	var tvRoot = this.yuiTree.getRoot();
	// clear the data first
	this.yuiTree.removeChildren(tvRoot);
	// populate the Categories from the data model
	
	i2b2.ONT.view['nav'].params.max = parseInt($('ONTNAVMaxQryDisp').value,10);
	i2b2.ONT.view['nav'].params.synonyms = $('ONTNAVshowSynonyms').checked;
	i2b2.ONT.view['nav'].params.hiddens = $('ONTNAVshowHiddens').checked;
	i2b2.ONT.view['nav'].params.modifiers = $('ONTNAVdisableModifiers').checked;

	for (var i=0; i<i2b2.ONT.model.Categories.length; i++) {
		var catData = i2b2.ONT.model.Categories[i];
		// add categories to ONT navigate tree
		var sdxDataNode = i2b2.sdx.Master.EncapsulateData('CONCPT',catData);
		if (!sdxDataNode) {
			console.error("SDX could not encapsulate CONCPT data!");
			console.dir(catData);
			return false;
		}
		var renderOptions = {
			title: catData.name,
			dragdrop: "i2b2.sdx.TypeControllers.CONCPT.AttachDrag2Data",			
			dblclick: "i2b2.ONT.view.nav.ToggleNode(this,'"+this.yuiTree.id+"')",
			icon: {
				root: "sdx_ONT_CONCPT_root.gif",
				rootExp: "sdx_ONT_CONCPT_root-exp.gif",
				branch: "sdx_ONT_CONCPT_branch.gif",
				branchExp: "sdx_ONT_CONCPT_branch-exp.gif",
				leaf: "sdx_ONT_CONCPT_leaf.gif"
			}
		};
		var sdxRenderData = i2b2.sdx.Master.RenderHTML(this.yuiTree.id, sdxDataNode, renderOptions);
		i2b2.sdx.Master.AppendTreeNode(this.yuiTree, this.yuiTree.root, sdxRenderData);
		
	}
	this.yuiTree.draw();
}

// ================================================================================================== //
i2b2.ONT.view.nav.Resize = function(e) {
	// this function provides the resize functionality needed for this screen
	var viewObj = i2b2.ONT.view.nav;
	//var ds = document.viewport.getDimensions();
    var w =  window.innerWidth || (window.document.documentElement.clientWidth || window.document.body.clientWidth);
    var h =  window.innerHeight || (window.document.documentElement.clientHeight || window.document.body.clientHeight);
	if (w < 840) {w = 840;}
	if (h < 517) {h = 517;}
	switch(i2b2.hive.MasterView.getViewMode()) {
		case "Patients":
			var ve = $('ontNavDisp').style;
			if (i2b2.WORK && i2b2.WORK.isLoaded) {
				var z = parseInt((h - 321)/2) + 8;
				ve.height = z;
			} else {
				ve.height = h-297;
			}
			break;
		case "Analysis":
			var ve = $('ontNavDisp').style;
			if (i2b2.WORK && i2b2.WORK.isLoaded) {
				var z = parseInt((h - 321)/2) + 8;
				ve.height = z;
			} else {
				ve.height = h-297;
			}
			break;
		default:
			break;
	}
	if (i2b2.ONT.view.main.isZoomed) { ve.height = h-101; }

}
//YAHOO.util.Event.addListener(window, "resize", i2b2.ONT.view.nav.Resize, i2b2); //tdw9

//================================================================================================== //
i2b2.ONT.view.nav.ResizeHeight = function() {
	// this function provides the resize functionality needed for this screen
	var viewObj = i2b2.ONT.view.nav;
	//var ds = document.viewport.getDimensions();
    var h =  window.innerHeight || (window.document.documentElement.clientHeight || window.document.body.clientHeight);
	if (h < 517) {h = 517;}
	switch(i2b2.hive.MasterView.getViewMode()) {
		case "Patients":
			var ve = $('ontNavDisp').style;
			if (i2b2.WORK && i2b2.WORK.isLoaded) {
				var z = parseInt((h - 321)/2) + 8;
				ve.height = z;
			} else {
				ve.height = h-297;
			}
			break;
		case "Analysis":
			var ve = $('ontNavDisp').style;
			if (i2b2.WORK && i2b2.WORK.isLoaded) {
				var z = parseInt((h - 321)/2) + 8;
				ve.height = z;
			} else {
				ve.height = h-297;
			}
			break;
		default:
			break;
	}
	if (i2b2.ONT.view.main.isZoomed) { ve.height = h-101; }
}


// This is done once the entire cell has been loaded
// ================================================================================================== //
console.info("SUBSCRIBED TO i2b2.events.afterCellInit");
i2b2.events.afterCellInit.subscribe(
	(function(en,co) {
		if (co[0].cellCode=='ONT') {
// ===================================================================
			console.debug('[EVENT CAPTURED i2b2.events.afterCellInit]');
			var thisview = i2b2.ONT.view.nav;
			thisview.Resize();
			// initialize treeview
			if (!thisview.yuiTree) {
				thisview.yuiTree = new YAHOO.widget.TreeView("ontNavResults");
				thisview.yuiTree.setDynamicLoad(i2b2.sdx.Master.LoadChildrenFromTreeview,1);
				// register the treeview with the SDX subsystem to be a container for CONCPT objects
				i2b2.sdx.Master.AttachType("ontNavResults","CONCPT");
			}
			
			i2b2.ONT.ctrlr.gen.events.onDataUpdate.subscribe(
				(function(en,co) {
					console.group("[EVENT CAPTURED i2b2.ONT.ctrlr.gen.events.onDataUpdate]");
					console.dir(co[0]);
					var dm_loc = co[0].DataLocation;
					var dm_ptr = co[0].DataRef;
					if (dm_loc=='i2b2.ONT.model.Categories') {
						console.debug("Processing Category update");
						i2b2.ONT.view.nav.PopulateCategories();
					}
					console.groupEnd();
				})
			);

			i2b2.ONT.view.nav.ContextMenu = new YAHOO.widget.ContextMenu( 
					"divContextMenu-Nav",  
						{ lazyload: true,
						trigger: $('ontNavDisp'), 
						itemdata: [
							{ text: "Refresh All",	onclick: { fn: i2b2.ONT.view.nav.doRefreshAll } }
					] }  
			); 
			i2b2.ONT.view.nav.ContextMenu.subscribe("triggerContextMenu",i2b2.ONT.view.nav.ContextMenuValidate);			
// ===================================================================
		}
	})
);

//================================================================================================== //
i2b2.ONT.view.nav.setChecked = function(here) {
	//var oCheckedItem = here.parent.checkedItem;
    if (here.cfg.getProperty("checked")) {//(oCheckedItem != here) {
          here.cfg.setProperty("checked", false);
         // here.parent.checkedItem = here;
    } else {
		   here.cfg.setProperty("checked", true);
	}
}

//================================================================================================== //
i2b2.ONT.view.nav.doRefreshAll = function() { 
	i2b2.ONT.ctrlr.gen.loadCategories();
	i2b2.ONT.view.nav.PopulateCategories();
}

//================================================================================================== //
i2b2.ONT.view.nav.ContextMenuValidate = function(p_oEvent) {
	var clickId = null;
	var currentNode = this.contextEventTarget;
	while (!currentNode.id) {
		if (currentNode.parentNode) {
			currentNode = currentNode.parentNode;
		} else {
			// we have recursed up the tree to the window/document DOM... it's a bad click
			this.cancel();
			return;
		}
	}
	clickId = currentNode.id;
	// see if the ID maps back to a treenode with SDX data
	var tvNode = i2b2.ONT.view.nav.yuiTree.getNodeByProperty('nodeid', clickId);
	if (tvNode) {
		if (tvNode.data.i2b2_SDX) {
			if (tvNode.data.i2b2_SDX.sdxInfo.sdxType == "CONCPT") {
				i2b2.ONT.view.nav.contextRecord = tvNode.data.i2b2_SDX;
			} else {
				this.cancel();
				return;
			}
		}
	}
};


//================================================================================================== //
i2b2.events.initView.subscribe((function(eventTypeName, newMode) 
{
// -------------------------------------------------------
	this.visible = true;
	this.Resize();
// -------------------------------------------------------
}),'',i2b2.ONT.view.nav);


//================================================================================================== //
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
			this.Resize();
			break;
		default:
			this.visible = false;
			break;
	}
// -------------------------------------------------------
}),'',i2b2.ONT.view.nav);

// ================================================================================================== //
i2b2.events.changedZoomWindows.subscribe((function(eventTypeName, zoomMsg) {
// -------------------------------------------------------
	newMode = zoomMsg[0];
	switch (newMode.window) {
		case "ONT":
		case "HISTORY":
		case "WORK":
			this.ResizeHeight();
			//this.Resize();	// tdw9
	}
// -------------------------------------------------------
}),'',i2b2.ONT.view.nav);


console.timeEnd('execute time');
console.groupEnd();