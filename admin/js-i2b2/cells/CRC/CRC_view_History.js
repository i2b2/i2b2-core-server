/**
 * @projectDescription	View controller for the history viewport. (CRC's "previous queries" window)
 * @inherits 	i2b2.CRC.view
 * @namespace	i2b2.CRC.view.history
 * @author		Nick Benik, Griffin Weber MD PhD
 * @version 	1.3
 * ----------------------------------------------------------------------------------------
 * updated 9-15-08: RC4 launch [Nick Benik] 
 */
console.group('Load & Execute component file: CRC > view > History');
console.time('execute time');


// create and save the screen objects
i2b2.CRC.view.history = new i2b2Base_cellViewController(i2b2.CRC, 'history');
i2b2.CRC.view.history.visible = false;
// define the option functions
// ================================================================================================== //
i2b2.CRC.view.history.showOptions = function(subScreen){
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
				i2b2.CRC.view['history'].params.sortOrder = tmpValue;
				if ($('HISTsortByNAME').checked) {
					tmpValue = 'NAME';
				}
				else {
					tmpValue = 'DATE';
				}
				i2b2.CRC.view['history'].params.sortBy = tmpValue;
				tmpValue = parseInt($('HISTMaxQryDisp').value, 10);
				i2b2.CRC.view['history'].params.maxQueriesDisp = tmpValue;
				// requery the history list
				i2b2.CRC.ctrlr.history.Refresh();
			}
		}
		var handleCancel = function(){
			this.cancel();
		}
		this.modalOptions = new YAHOO.widget.SimpleDialog("optionsHistory", {
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
		$('optionsHistory').show();
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
i2b2.CRC.view.history.ToggleNode = function(divTarg, divTreeID) {
	// get the i2b2 data from the yuiTree node
	var tvTree = YAHOO.widget.TreeView.getTree(divTreeID);
	var tvNode = tvTree.getNodeByProperty('nodeid', divTarg.id);
	tvNode.toggle();
}

i2b2.CRC.view.history.selectTab = function(tabCode) {
	// toggle between the Navigate and Find Terms tabs
	switch (tabCode) {
		case "find":
			$('crctabFind').addClassName('active');
			$('crctabNavigate').removeClassName('active');
			$('crcNavDisp').hide();
			$('crcFindDisp').show();
		break;
		case "nav":
			$('crctabNavigate').addClassName('active');
			$('crctabFind').removeClassName('active');
			$('crcFindDisp').hide();
			$('crcNavDisp').show();
		break;
	}
}

// ================================================================================================== //
i2b2.CRC.view.history.Resize = function(e) {
	// this function provides the resize functionality needed for this screen
	var viewObj = i2b2.CRC.view.history;
	var ve = $('crcHistoryBox');
	if (viewObj.visible) {
		ve.show();
		// var ds = document.viewport.getDimensions();
	    var w =  window.innerWidth || (window.document.documentElement.clientWidth || window.document.body.clientWidth);
	    var h =  window.innerHeight || (window.document.documentElement.clientHeight || window.document.body.clientHeight);
		if (w < 840) {w = 840;}
		if (h < 517) {h = 517;}
		ve = ve.style;
		// resize our visual components
		switch(i2b2.hive.MasterView.getViewMode()) {
			case "Patients":
				if (i2b2.WORK && i2b2.WORK.isLoaded) {
					// make room for the workspace window
					ve.width = Math.max(initBrowserViewPortDim.width-rightSideWidth, 0);
					ve.top = h-196+44;
					$('crcHistoryData').style.height = '100px';
					$('crcSearchNamesResults').style.height = '72px';
				} else {
					ve.width = w-578;
					ve.top = h-196;
					$('crcHistoryData').style.height = '144px';
					$('crcSearchNamesResults').style.height = '116px';
				}
				break;
			case "Analysis":
				if (i2b2.WORK && i2b2.WORK.isLoaded) {
					// make room for the workspace window
					w = parseInt(w/3)-10;
					ve.width = w;
					ve.top = h-196+44;
					$('crcHistoryData').style.height = '100px';
					$('crcSearchNamesResults').style.height = '72px';					
				} else {
					w = parseInt(w/3)-10;
					ve.width = w;
					ve.top = h-196;
					$('crcHistoryData').style.height = '144px';
					$('crcSearchNamesResults').style.height = '116px';
				}
				break;
		}
		if (viewObj.isZoomed) {
			ve.top = '';
			$('crcHistoryData').style.height = h-97; 
			$('crcSearchNamesResults').style.height = h-125;			
		}
		$$('DIV#crcHistoryBox DIV#crcHistoryData')[0].style.width = (parseInt(ve.width)-24) + 'px';
		$$('DIV#crcHistoryBox DIV#crcSearchNamesResults')[0].style.width = (parseInt(ve.width)-24) + 'px';		
	} else {
		ve.hide();
	}
}

//YAHOO.util.Event.addListener(window, "resize", i2b2.CRC.view.history.Resize, i2b2.CRC.view.history); // tdw9
i2b2.CRC.view.history.Resize();


//================================================================================================== //
i2b2.CRC.view.history.splitterDragged = function()
{
	var splitter = $( i2b2.hive.mySplitter.name );
	var CRCHist = $("crcHistoryBox");
	CRCHist.style.width	= Math.max((parseInt(splitter.style.left) - CRCHist.offsetLeft - 3), 0) + "px";
	$$('DIV#crcHistoryBox DIV#crcHistoryData')[0].style.width = Math.max((parseInt(CRCHist.style.width)-24), 0) + 'px';
	$$('DIV#crcHistoryBox DIV#crcSearchNamesResults')[0].style.width = Math.max((parseInt(CRCHist.style.width)-24), 0) + 'px';
}

//================================================================================================== //
i2b2.CRC.view.history.ResizeHeight = function() {
	// this function provides the resize functionality needed for this screen
	var viewObj = i2b2.CRC.view.history;
	var ve = $('crcHistoryBox');
	if (viewObj.visible) {
		ve.show();
		// var ds = document.viewport.getDimensions();
	    var h =  window.innerHeight || (window.document.documentElement.clientHeight || window.document.body.clientHeight);
		if (h < 517) {h = 517;}
		ve = ve.style;
		// resize our visual components
		switch(i2b2.hive.MasterView.getViewMode()) {
			case "Patients":
				if (i2b2.WORK && i2b2.WORK.isLoaded) {
					// make room for the workspace window
					ve.top = h-196+44;
					$('crcHistoryData').style.height = '100px';
					$('crcSearchNamesResults').style.height = '72px';
				} else {
					ve.top = h-196;
					$('crcHistoryData').style.height = '144px';
					$('crcSearchNamesResults').style.height = '144px';
				}
				break;
			case "Analysis":
				if (i2b2.WORK && i2b2.WORK.isLoaded) {
					// make room for the workspace window
					ve.top = h-196+44;
					$('crcHistoryData').style.height = '100px';
					$('crcSearchNamesResults').style.height = '72px';
				} else {
					ve.top = h-196;
					$('crcHistoryData').style.height = '144px';
					$('crcSearchNamesResults').style.height = '144px';
				}
				break;
		}
		if (viewObj.isZoomed) {
			ve.top = '';
			$('crcHistoryData').style.height = h-97; 
			$('crcSearchNamesResults').style.height = h-125; 
		}
	} else {
		ve.hide();
	}
}


// ================================================================================================== //
i2b2.CRC.view.history.PopulateQueryMasters = function(dm_ptr, dm_name, options) {
	var thisview = i2b2.CRC.view.history;
	// clear the data first
	var tvTree = i2b2.CRC.view.history.yuiTree;
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
			dblclick: "i2b2.CRC.view.history.ToggleNode(this,'"+tvTree.id+"')",
			icon: icon,
			iconExp: iconExp
		};
		var sdxRenderData = i2b2.sdx.Master.RenderHTML(tvTree.id, sdxDataNode, renderOptions);
		i2b2.sdx.Master.AppendTreeNode(tvTree, tvRoot, sdxRenderData);
	}
	tvTree.draw();
};


// ================================================================================================== //
i2b2.CRC.view.history.ZoomView = function() {
	i2b2.hive.MasterView.toggleZoomWindow("HISTORY");
}


// =========== Context Menu Suff =========== 
// ================================================================================================== //
i2b2.CRC.view.history.doRename = function() { 
	var op = i2b2.CRC.view.history.contextRecord; // object path
	i2b2.CRC.ctrlr.history.queryRename(op.sdxInfo.sdxKeyValue, false, op); 
}

// ================================================================================================== //
i2b2.CRC.view.history.doDelete = function() { 
	i2b2.CRC.ctrlr.history.queryDelete(i2b2.CRC.view.history.contextRecord.sdxInfo.sdxKeyValue); 
}

// ================================================================================================== //
i2b2.CRC.view.history.doRefreshAll = function() { i2b2.CRC.ctrlr.history.Refresh(); }

// ================================================================================================== //
i2b2.CRC.view.history.ContextMenuValidate = function(p_oEvent) {
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
	var tvNode = i2b2.CRC.view.history.yuiTree.getNodeByProperty('nodeid', clickId);
	if (tvNode) {
		if (tvNode.data.i2b2_SDX) {
			if (tvNode.data.i2b2_SDX.sdxInfo.sdxType == "QM") {
				i2b2.CRC.view.history.contextRecord = tvNode.data.i2b2_SDX;
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
			var thisview = i2b2.CRC.view.history;
			thisview.Resize();
			// initialize treeview
			if (!thisview.yuiTree) {
				thisview.yuiTree = new YAHOO.widget.TreeView("crcHistoryData");
				thisview.yuiTree.setDynamicLoad(i2b2.sdx.Master.LoadChildrenFromTreeview,1);
				// register the treeview with the SDX subsystem to be a container for QM, QI, PRS, PRC objects
				i2b2.sdx.Master.AttachType("crcHistoryData","QM");
				i2b2.sdx.Master.AttachType("crcHistoryData","QI");
				i2b2.sdx.Master.AttachType("crcHistoryData","ENS");
				i2b2.sdx.Master.AttachType("crcHistoryData","PRC");
				i2b2.sdx.Master.AttachType("crcHistoryData","PRS");
				i2b2.sdx.Master.AttachType("crcHistoryData","PR");

			}
                        // initialize treeview
                        if (!thisview.yuiFindTree) {
                                thisview.yuiFindTree = new YAHOO.widget.TreeView("crcSearchNamesResults");
                                thisview.yuiFindTree.setDynamicLoad(i2b2.sdx.Master.LoadChildrenFromTreeview,1);
                                // register the treeview with the SDX subsystem to be a container for QM, QI, PRS, PRC objects
                                i2b2.sdx.Master.AttachType("crcSearchNamesResults","QM");
                                i2b2.sdx.Master.AttachType("crcSearchNamesResults","QI");
                                i2b2.sdx.Master.AttachType("crcSearchNamesResults","ENS");
                                i2b2.sdx.Master.AttachType("crcSearchNamesResults","PRC");
                                i2b2.sdx.Master.AttachType("crcSearchNamesResults","PRS");
                                i2b2.sdx.Master.AttachType("crcSearchNamesResults","PR");

			}			
			// we need to make sure everything is loaded
			setTimeout("i2b2.CRC.ctrlr.history.Refresh();",300);			
			
// -------------------------------------------------------
			i2b2.CRC.ctrlr.history.events.onDataUpdate.subscribe(
				(function(en,co) {
					console.group("[EVENT CAPTURED i2b2.CRC.ctrlr.history.events.onDataUpdate]");
					console.dir(co[0]);
					var dm_loc = co[0].DataLocation;
					var dm_ptr = co[0].DataRef;
					if (dm_loc=='i2b2.CRC.model.QueryMasters') {
						console.debug("Processing QueryMasters update");
						var options = {};
						options.sortBy = i2b2.CRC.view['history'].params.sortBy;
						options.sortOrder = i2b2.CRC.view['history'].params.sortOrder;
						options.max = i2b2.CRC.view['history'].params.maxQueriesDisp;
						i2b2.CRC.view.history.PopulateQueryMasters(dm_ptr, dm_loc, options);
					}
					console.groupEnd();
				})
			);
			
			
			
// -------------------------------------------------------
		// override default handler (we need this so that we can properly capture the XML request/response messagees
		 i2b2.sdx.Master.setHandlerCustom('crcHistoryData', 'QM', 'LoadChildrenFromTreeview', (function(node, onCompleteCallback) {
			var scopedCallback = new i2b2_scopedCallback();
			scopedCallback.scope = node.data.i2b2_SDX;
			scopedCallback.callback = function(cellResult) {
				var cl_node = node;
				var cl_onCompleteCB = onCompleteCallback;
				// THIS function is used to process the AJAX results of the getChild call
				//		results data object contains the following attributes:
				//			refXML: xmlDomObject <--- for data processing
				//			msgRequest: xml (string)
				//			msgResponse: xml (string)
				//			error: boolean
				//			errorStatus: string [only with error=true]
				//			errorMsg: string [only with error=true]
		// <THIS IS WHY WE ARE CREATING CUSTOMER HANDLERS FOR THE Query Tool CONTROL!>
						i2b2.CRC.view.history.queryResponse = cellResult.msgResponse;
						i2b2.CRC.view.history.queryRequest = cellResult.msgRequest;
						i2b2.CRC.view.history.queryUrl = cellResult.msgUrl;
		// </THIS IS WHY WE ARE CREATING CUSTOMER HANDLERS FOR THE QueryTool CONTROL!>					
				for(var i1=0; i1<1*cellResult.results.length; i1++) {
					var o = cellResult.results[i1];
					var renderOptions = {
						title: o.origData.title,
						dragdrop: "i2b2.sdx.TypeControllers.QI.AttachDrag2Data",
						dblclick: "i2b2.CRC.view.history.ToggleNode(this,'"+cl_node.tree.id+"')",
						icon: "sdx_CRC_QI.gif",
						iconExp: "sdx_CRC_QI_exp.gif"
					};
					var sdxRenderData = i2b2.sdx.Master.RenderHTML(cl_node.tree.id, o, renderOptions);
					i2b2.sdx.Master.AppendTreeNode(cl_node.tree, cl_node, sdxRenderData);
				}
				// handle the YUI treeview	
				if (getObjectClass(cl_onCompleteCB)=='i2b2_scopedCallback') {
					cl_onCompleteCB.callback.call(cl_onCompleteCB.scope, cellResult);
				} else {
					cl_onCompleteCB(cellResult);
				}
			}
			var sdxParentNode = node.data.i2b2_SDX;
			i2b2.sdx.Master.getChildRecords(sdxParentNode, scopedCallback);
		}));

                 i2b2.sdx.Master.setHandlerCustom('crcSearchNamesResults', 'QM', 'LoadChildrenFromTreeview', (function(node, onCompleteCallback) {
                        var scopedCallback = new i2b2_scopedCallback();
                        scopedCallback.scope = node.data.i2b2_SDX;
                        scopedCallback.callback = function(cellResult) {
                                var cl_node = node;
                                var cl_onCompleteCB = onCompleteCallback;
                                // THIS function is used to process the AJAX results of the getChild call
                                //              results data object contains the following attributes:
                                //                      refXML: xmlDomObject <--- for data processing
                                //                      msgRequest: xml (string)
                                //                      msgResponse: xml (string)
                                //                      error: boolean
                                //                      errorStatus: string [only with error=true]
                                //                      errorMsg: string [only with error=true]
                // <THIS IS WHY WE ARE CREATING CUSTOMER HANDLERS FOR THE Query Tool CONTROL!>
                                                i2b2.CRC.view.history.queryResponse = cellResult.msgResponse;
                                                i2b2.CRC.view.history.queryRequest = cellResult.msgRequest;
                                                i2b2.CRC.view.history.queryUrl = cellResult.msgUrl;
                // </THIS IS WHY WE ARE CREATING CUSTOMER HANDLERS FOR THE QueryTool CONTROL!>                                  
                                for(var i1=0; i1<1*cellResult.results.length; i1++) {
                                        var o = cellResult.results[i1];
                                        var renderOptions = {
                                                title: o.origData.title,
                                                dragdrop: "i2b2.sdx.TypeControllers.QI.AttachDrag2Data",
                                                dblclick: "i2b2.CRC.view.history.ToggleNode(this,'"+cl_node.tree.id+"')",
                                                icon: "sdx_CRC_QI.gif",
                                                iconExp: "sdx_CRC_QI_exp.gif"
                                        };
                                        var sdxRenderData = i2b2.sdx.Master.RenderHTML(cl_node.tree.id, o, renderOptions);
                                        i2b2.sdx.Master.AppendTreeNode(cl_node.tree, cl_node, sdxRenderData);
                                }
                                // handle the YUI treeview      
                                if (getObjectClass(cl_onCompleteCB)=='i2b2_scopedCallback') {
                                        cl_onCompleteCB.callback.call(cl_onCompleteCB.scope, cellResult);
                                } else {
                                        cl_onCompleteCB(cellResult);
                                }
                        }
                        var sdxParentNode = node.data.i2b2_SDX;
                        i2b2.sdx.Master.getChildRecords(sdxParentNode, scopedCallback);
                }));
// -------------------------------------------------------
		
			i2b2.CRC.view.history.ContextMenu = new YAHOO.widget.ContextMenu( 
					"divContextMenu-History",  
					{ lazyload: true,
					trigger: $('crcNavDisp'), 
					itemdata: [
						{ text: "Rename", 	onclick: { fn: i2b2.CRC.view.history.doRename } },
						{ text: "Delete", 		onclick: { fn: i2b2.CRC.view.history.doDelete } },
						{ text: "Refresh All",	onclick: { fn: i2b2.CRC.view.history.doRefreshAll } }
					] }  
			); 
			i2b2.CRC.view.history.ContextMenu.subscribe("triggerContextMenu",i2b2.CRC.view.history.ContextMenuValidate); 

// =========================================================			
		}
	})
);


//================================================================================================== //
i2b2.events.initView.subscribe((function(eventTypeName, newMode) {
// -------------------------------------------------------
	this.visible = true;
	if (i2b2.WORK && i2b2.WORK.isLoaded) {
		$('crcHistoryData').style.height = '100px';
		$('crcSearchNamesResults').style.height = '72px';
	} else {
		$('crcHistoryData').style.height = '144px';
		$('crcSearchNamesResults').style.height = '116px';
	}
	$('crcHistoryBox').show();
	this.Resize();
// -------------------------------------------------------
}),'',i2b2.CRC.view.history);


// ================================================================================================== //
i2b2.events.changedViewMode.subscribe((function(eventTypeName, newMode) {
		newMode = newMode[0];
		this.viewMode = newMode;
		switch(newMode) {
			case "Patients":
			case "Analysis":
				var wlst = i2b2.hive.MasterView.getZoomWindows();
				if (wlst.indexOf("ONT")!=-1 || wlst.indexOf("WORK")!=-1) { return; }
				this.visible = true;
				if (i2b2.WORK && i2b2.WORK.isLoaded) {
					$('crcHistoryData').style.height = '100px';
					$('crcSearchNamesResults').style.height = '72px';
				} else {
					$('crcHistoryData').style.height = '144px';
					$('crcSearchNamesResults').style.height = '116px';
				}
				$('crcHistoryBox').show();
				//this.Resize(); // tdw9
				this.splitterDragged();
				this.ResizeHeight();
				break;
			default:
				this.visible = false;
				$('crcHistoryBox').hide();
				break;
		}
}),'',i2b2.CRC.view.history);



// ================================================================================================== //
i2b2.events.changedZoomWindows.subscribe((function(eventTypeName, zoomMsg) {
	newMode = zoomMsg[0];
	if (!newMode.action) { return; }
	if (newMode.action == "ADD") {
		switch (newMode.window) {
			case "HISTORY":
				this.isZoomed = true;
				this.visible = true;
				break;
			case "ONT":
			case "WORK":
				this.visible = false;
				this.isZoomed = false;
		}
	} else {
		switch (newMode.window) {
			case "HISTORY":
			case "ONT":
			case "WORK":
				this.isZoomed = false;
				this.visible = true;
		}
	}
	this.ResizeHeight();
	this.splitterDragged();
}),'',i2b2.CRC.view.history);


console.timeEnd('execute time');
console.groupEnd();
