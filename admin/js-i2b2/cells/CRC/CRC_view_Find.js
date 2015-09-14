/**
 * @projectDescription	View controller for the Find viewport. (CRC's "previous queries" window)
 * @inherits 	i2b2.CRC.view
 * @namespace	i2b2.CRC.view.find
 * @author		Nick Benik, Griffin Weber MD PhD
 * @version 	1.3
 * ----------------------------------------------------------------------------------------
 * updated 9-15-08: RC4 launch [Nick Benik] 
 */
console.group('Load & Execute component file: CRC > view > Find');
console.time('execute time');


// create and save the screen objects
i2b2.CRC.view.find = new i2b2Base_cellViewController(i2b2.CRC, 'find');
i2b2.CRC.view.find.visible = false;
// define the option functions
// ================================================================================================== //
i2b2.CRC.view.find.showOptions = function(subScreen){
	if (!this.modalOptions) {
		var handleSubmit = function(){
			// submit value(s)
			if (this.submit()) {
				if ($('HISTsortOrderASC').checked) {
					tmpValue = 'ASC';
				}
				else {
					tmpValue = 'DESC';
				}
				i2b2.CRC.view['find'].params.sortOrder = tmpValue;
				if ($('HISTsortByNAME').checked) {
					tmpValue = 'NAME';
				}
				else {
					tmpValue = 'DATE';
				}
				i2b2.CRC.view['find'].params.sortBy = tmpValue;
				tmpValue = parseInt($('HISTMaxQryDisp').value, 10);
				i2b2.CRC.view['find'].params.maxQueriesDisp = tmpValue;
				// requery the find list
				i2b2.CRC.ctrlr.find.Refresh();
			}
		}
		var handleCancel = function(){
			this.cancel();
		}
		this.modalOptions = new YAHOO.widget.SimpleDialog("optionsFind", {
			width: "400px",
			fixedcenter: true,
			constraintoviewport: true,
			modal: true,
			zindex: 700,
			buttons: [{
				text: "OK",
				handler: handleSubmit,
				isDefault: true
			}, {
				text: "Cancel",
				handler: handleCancel
			}]
		});
		$('optionsFind').show();
		this.modalOptions.validate = function(){
			// now process the form data
			var tmpValue = parseInt($('HISTMaxQryDisp').value, 10);
			if (!isNaN(tmpValue) && tmpValue <= 0) {
				alert("The max number of Queries must be a whole number larger then zero.");
				return false;
			}
			return true;
		};
		this.modalOptions.render(document.body);
	} 
	this.modalOptions.show();
	// load settings
	if (this.params.sortOrder=="ASC") {
		$('HISTsortOrderASC').checked = true;
	} else {
		$('HISTsortOrderDESC').checked = true;
	}
	if (this.params.sortBy=="NAME") {
		$('HISTsortByNAME').checked = true;
	} else {
		$('HISTsortByDATE').checked = true;
	}
	$('HISTMaxQryDisp').value = this.params.maxQueriesDisp;		
}

// ================================================================================================== //
i2b2.CRC.view.find.ToggleNode = function(divTarg, divTreeID) {
	// get the i2b2 data from the yuiTree node
	var tvTree = YAHOO.widget.TreeView.getTree(divTreeID);
	var tvNode = tvTree.getNodeByProperty('nodeid', divTarg.id);
	tvNode.toggle();
}





// ================================================================================================== //
i2b2.CRC.view.find.PopulateQueryMasters = function(dm_ptr, dm_name, options) {
	var thisview = i2b2.CRC.view.find;
	// clear the data first
	var tvTree = i2b2.CRC.view.find.yuiTree;
	var tvRoot = tvTree.getRoot();
	tvTree.removeChildren(tvRoot);
	tvTree.locked = false;
	
	// sort by the options
	if (Object.isUndefined(options)) { var options = {}; }
	if (!options.sortBy) { options.sortBy = 'DATE'; }
	if (!options.sortOrder) { options.sortBy = 'DESC'; }
	if (options.sortBy=='NAME') {
		var compareAttrib = 'name';
	} else {
		var compareAttrib = 'created';
	}
	if (options.sortOrder=='ASC') {
		var reverseSort = false;
	} else {
		var reverseSort = true;
	}

	// NEW SORT METHOD USING prototype Enumerators
	var QM_sortVal = function(rec) {
		var hash_key = rec[0]; 
		var sdxExtObj = rec[1];
		var cl_compareAttrib = compareAttrib;  // <---- closure var
		var t = sdxExtObj.origData[cl_compareAttrib];
		if (cl_compareAttrib=="created") {
			// proper date handling (w/improper handling for latest changes to output format)
			var sd = t.toUpperCase();
			if (sd.indexOf('Z') != -1 || sd.indexOf('T') != -1) {
				t = t.toLowerCase();
			} else {
				t = Date.parse(t);
			}
		} else {
			t = String(t.toLowerCase() );
		}
		return t;
	};
	var sortFinal = i2b2.CRC.model.QueryMasters.sortBy(QM_sortVal);
	// reverse if needed
	if (reverseSort) { sortFinal.reverse(true); }
	
	// populate the Query Masters into the treeview
	for (var i=0; i<sortFinal.length; i++) {
		// add categories to ONT navigate tree
		var sdxDataNode = sortFinal[i][1];
		
		if (sdxDataNode.origData.master_type_cd == "TEMPORAL")
		{
			icon = "sdx_CRC_QMT.gif";
			iconExp = "sdx_CRC_QMT_exp.gif";
		}
		else
		{
			
			icon = "sdx_CRC_QM.gif";
			iconExp = "sdx_CRC_QM_exp.gif";		
		}
		var renderOptions = {
			title: sdxDataNode.origData.name,
			dragdrop: "i2b2.sdx.TypeControllers.QM.AttachDrag2Data",
			dblclick: "i2b2.CRC.view.find.ToggleNode(this,'"+tvTree.id+"')",
			icon: icon,
			iconExp: iconExp
		};
		var sdxRenderData = i2b2.sdx.Master.RenderHTML(tvTree.id, sdxDataNode, renderOptions);
		i2b2.sdx.Master.AppendTreeNode(tvTree, tvRoot, sdxRenderData);
	}
	tvTree.draw();
};


// ================================================================================================== //
i2b2.CRC.view.find.ZoomView = function() {
	i2b2.hive.MasterView.toggleZoomWindow("HISTORY");
}


// =========== Context Menu Suff =========== 
// ================================================================================================== //
i2b2.CRC.view.find.doRename = function() { 
	var op = i2b2.CRC.view.find.contextRecord; // object path
	i2b2.CRC.ctrlr.history.queryRename(op.sdxInfo.sdxKeyValue, false, op); 
}

// ================================================================================================== //
i2b2.CRC.view.find.doDelete = function() { 
	i2b2.CRC.ctrlr.history.queryDelete(i2b2.CRC.view.find.contextRecord.sdxInfo.sdxKeyValue); 
}

// ================================================================================================== //
i2b2.CRC.view.find.doRefreshAll = function() { i2b2.CRC.ctrlr.find.Refresh(); }

// ================================================================================================== //
i2b2.CRC.view.find.ContextMenuValidate = function(p_oEvent) {
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
	var tvNode = i2b2.CRC.view.find.yuiTree.getNodeByProperty('nodeid', clickId);
	if (tvNode) {
		if (tvNode.data.i2b2_SDX) {
			if (tvNode.data.i2b2_SDX.sdxInfo.sdxType == "QM") {
				i2b2.CRC.view.find.contextRecord = tvNode.data.i2b2_SDX;
			} else {
				this.cancel();
				return;
			}
		}
	}
};



// This is done once the entire cell has been loaded
// ================================================================================================== //
console.info("SUBSCRIBED TO i2b2.events.afterCellInit");
i2b2.events.afterCellInit.subscribe(
	(function(en,co) {
		if (co[0].cellCode=='CRC') {
// =========================================================			
			
			console.debug('[EVENT CAPTURED i2b2.events.afterCellInit]');			
			var thisview = i2b2.CRC.view.find;
			//thisview.Resize();
			// initialize treeview
			if (!thisview.yuiTree) {
				thisview.yuiTree = new YAHOO.widget.TreeView("crcSearchNamesResults");
				thisview.yuiTree.setDynamicLoad(i2b2.sdx.Master.LoadChildrenFromTreeview,1);
				// register the treeview with the SDX subsystem to be a container for QM, QI, PRS, PRC objects
				i2b2.sdx.Master.AttachType("crcHistoryData","QM");
				i2b2.sdx.Master.AttachType("crcHistoryData","QI");
				i2b2.sdx.Master.AttachType("crcHistoryData","ENS");
				i2b2.sdx.Master.AttachType("crcHistoryData","PRC");
				i2b2.sdx.Master.AttachType("crcHistoryData","PRS");
				i2b2.sdx.Master.AttachType("crcHistoryData","PR");

			}
			// we need to make sure everything is loaded
			setTimeout("i2b2.CRC.ctrlr.find.Refresh();",300);			
			
// -------------------------------------------------------
			i2b2.CRC.ctrlr.find.events.onDataUpdate.subscribe(
				(function(en,co) {
					console.group("[EVENT CAPTURED i2b2.CRC.ctrlr.history.events.onDataUpdate]");
					console.dir(co[0]);
					var dm_loc = co[0].DataLocation;
					var dm_ptr = co[0].DataRef;
					if (dm_loc=='i2b2.CRC.model.QueryMasters') {
						console.debug("Processing QueryMasters update");
						var options = {};
						options.sortBy = i2b2.CRC.view['find'].params.sortBy;
						options.sortOrder = i2b2.CRC.view['find'].params.sortOrder;
						options.max = i2b2.CRC.view['find'].params.maxQueriesDisp;
						i2b2.CRC.view.find.PopulateQueryMasters(dm_ptr, dm_loc, options);
					}
					console.groupEnd();
				})
			);
			
			
			
// -------------------------------------------------------
		// override default handler (we need this so that we can properly capture the XML request/response messagees


// -------------------------------------------------------
		
			i2b2.CRC.view.find.ContextMenu = new YAHOO.widget.ContextMenu( 
					"divContextMenu-Find",  
					{ lazyload: true,
					trigger: $('crcFindDisp'), 
					itemdata: [
						{ text: "Rename", 	onclick: { fn: i2b2.CRC.view.find.doRename } },
						{ text: "Delete", 		onclick: { fn: i2b2.CRC.view.find.doDelete } }
					] }  
			); 
			i2b2.CRC.view.find.ContextMenu.subscribe("triggerContextMenu",i2b2.CRC.view.find.ContextMenuValidate); 

// =========================================================			
		}
	})
);


//================================================================================================== //
i2b2.events.initView.subscribe((function(eventTypeName, newMode) {
// -------------------------------------------------------
	this.visible = true;
	if (i2b2.WORK && i2b2.WORK.isLoaded) {
		$('crcSearchNamesResults').style.height = '72px';
	} else {
		$('crcSearchNamesResults').style.height = '116px';
	}
	$('crcHistoryBox').show();
	//this.Resize();
// -------------------------------------------------------
}),'',i2b2.CRC.view.find);






console.timeEnd('execute time');
console.groupEnd();
