/**
 * @projectDescription	The SDX controller library for the Workplace Object datatype.
 * @namespace	i2b2
 * @inherits 		i2b2
 * @author		Nick Benik
 * @version 		1.0
 * @see 		i2b2.sdx
 * ----------------------------------------------------------------------------------------
 * updated 7-31-08: initial launch [Nick Benik] 
 * updated 1-12-09: added QDEF, QGDEF and default XML object handling for SDX subsystem
 */

console.group('Load & Execute component file: WORK > SDX > Workplace Object');
console.time('execute time');

// ********************************* Patient Record Set Stuff *********************************
i2b2.sdx.TypeControllers.WRK = {};
i2b2.sdx.TypeControllers.WRK.model = {};
// ********************************* Patient Record Set Stuff *********************************

// *********************************************************************************
//	ENCAPSULATE DATA
// *********************************************************************************
i2b2.sdx.TypeControllers.WRK.getEncapsulateInfo = function() {
	// this function returns the encapsulation head information
	return {sdxType: 'WRK', sdxKeyName: 'index', sdxControlCell:'WORK', sdxDisplayNameKey: 'name'};
}

i2b2.sdx.TypeControllers.WRK.SaveToDataModel = function(sdxData, sdxParentNode) {
	// save to WORK data model
	if (!sdxParentNode) { return false; }
	var qm_id = sdxData.sdxInfo.sdxKeyValue;
	var qm_hash = i2b2.sdx.Master._KeyHash(qm_id);

	// class for all SDX communications
	function i2b2_SDX_Encapsulation_EXTENDED() {}
	// create an instance and populate with info
	var t = new i2b2_SDX_Encapsulation_EXTENDED();
	t.origData = Object.clone(sdxData.origData);
	t.sdxInfo = Object.clone(sdxData.sdxInfo);
	t.parent = sdxParentNode;
	t.children = new Hash();
	t.children.loaded = false;
	// add to hash
	sdxParentNode.children.set(qm_hash, t);
	// TODO: send data update signal (use JOINING-MUTEX or AGGREGATING-MUTEX to avoid rapid fire of event!)
	return t;
}


i2b2.sdx.TypeControllers.WRK.LoadFromDataModel = function(key_value) {}


i2b2.sdx.TypeControllers.WRK.ClearAllFromDataModel= function(sdxOptionalParent) { return false; }


// *********************************************************************************
//	GENERATE HTML (DEFAULT HANDLER)
// *********************************************************************************
i2b2.sdx.TypeControllers.WRK.RenderHTML= function(sdxData, options, targetDiv) {    
	// this function extracts the datatype from the SDX's original XML object and relies upon it's 
	// original SDX type controller to render the HTML

	var sdxCode = false;
	var sdxPackage = {};
	var subclassHTML = "";
	var newOptions = options;
	newOptions.title = "";
	newOptions.showchildren = false;
	newOptions.click = "";
	newOptions.dblclick = "";
	switch (sdxData.origData.encapType) {
		case "PREV_QUERY":
			// this is a QueryMaster object
			sdxCode = "QM";
			// XPath query exploits faults in XML message namespace declarations to avoid creation of namespace resolver kluges that perform no resolving
			var x = i2b2.h.XPath(sdxData.origData.xmlOrig, "work_xml/descendant::query_master_id/..")[0];
			// extract and create an SDX object for this node
			var o = {};
			o.xmlOrig = x;
			o.query_master_id = i2b2.h.getXNodeVal(x, "query_master_id");
			o.id = o.query_master_id;
			o.name = i2b2.h.getXNodeVal(x, "name");
			o.group = i2b2.h.getXNodeVal(x, "group_id");
			o.userid = i2b2.h.getXNodeVal(x, "user_id");
			o.created = null;
			newOptions.icon = "sdx_CRC_QM_workplace.jpg";
			newOptions.showchildren = false;
			newOptions.title = o.name;
			break;
		case "PATIENT_COLL":
			// this is a PatientRecordSet object
			sdxCode = "PRS";
			// XPath query exploits faults in XML message namespace declarations to avoid creation of namespace resolver kluges that perform no resolving
			var x = i2b2.h.XPath(sdxData.origData.xmlOrig, "work_xml/descendant::result_instance_id/..")[0];
			var o = {};
			o.xmlOrig = x;
			o.result_type = "PATIENTSET";
			o.size = i2b2.h.getXNodeVal(x, "set_size");
			o.result_instance_id = i2b2.h.getXNodeVal(x, "result_instance_id");
			o.PRS_id = o.result_instance_id;
			o.QI_id = i2b2.h.getXNodeVal(x, "query_instance_id");
			o.QM_id = o.QI_id; // TODO: This needs to be properly resolved
			o.start_date = i2b2.h.getXNodeVal(x, "start_date");
			o.end_date = i2b2.h.getXNodeVal(x, "end_date");
			o.description = i2b2.h.getXNodeVal(x, "description");
			o.title = sdxData.sdxInfo.sdxDisplayName
			newOptions.icon = "sdx_CRC_PRS.jpg";
			newOptions.showchildren = false;
			newOptions.title = o.title;
			break;
		case "ENCOUNTER_COLL":
			// this is a EncounterRecordSet object
			sdxCode = "ENS";
			// XPath query exploits faults in XML message namespace declarations to avoid creation of namespace resolver kluges that perform no resolving
			var x = i2b2.h.XPath(sdxData.origData.xmlOrig, "work_xml/descendant::result_instance_id/..")[0];
			var o = {};
			o.xmlOrig = x;
			o.result_type = "ENCOUNTERSET";
			o.size = i2b2.h.getXNodeVal(x, "set_size");
			o.result_instance_id = i2b2.h.getXNodeVal(x, "result_instance_id");
			o.PRS_id = o.result_instance_id;
			o.QI_id = i2b2.h.getXNodeVal(x, "query_instance_id");
			o.QM_id = o.QI_id; // TODO: This needs to be properly resolved
			o.description = i2b2.h.getXNodeVal(x, "description");
			o.start_date = i2b2.h.getXNodeVal(x, "start_date");
			o.end_date = i2b2.h.getXNodeVal(x, "end_date");
			o.title = sdxData.sdxInfo.sdxDisplayName
			newOptions.icon = "sdx_CRC_PRS.jpg";
			newOptions.showchildren = false;
			newOptions.title = o.title;
			break;			
		case "PATIENT":
			// this is an PatientRecord object
			sdxCode = "PR";
			// XPath query exploits faults in XML message namespace declarations to avoid creation of namespace resolver kluges that perform no resolving
			var x = i2b2.h.XPath(sdxData.origData.xmlOrig, "work_xml/descendant::patient/..")[0];
			var o = {};
			o.xmlOrig = x;
			o.result_type = "PATIENTSET";
			o.patient_id = i2b2.h.XPath(x, "descendant::patient/patient_id/text()")[0].nodeValue;
			o.PRS_id = i2b2.h.XPath(x, "@patient_set_id")[0].nodeValue;
			o.PRS_name = i2b2.h.XPath(x, "@patient_set_name")[0].nodeValue;
			o.title = sdxData.origData.name;
			newOptions.icon = "sdx_CRC_PR.jpg";
			newOptions.showchildren = false;
			newOptions.title = o.title;
			break;
		case "CONCEPT":
			sdxCode = "CONCPT";
			var x = i2b2.h.XPath(sdxData.origData.xmlOrig, "work_xml/descendant-or-self::concept")[0];
			var o = {};
			o.xmlOrig = x;
			o.key = i2b2.h.getXNodeVal(x, "key");
			o.level = i2b2.h.getXNodeVal(x, "level");
			o.name = i2b2.h.getXNodeVal(x, "name");
			o.column_name = i2b2.h.getXNodeVal(x, "columnname");
			o.dim_code = i2b2.h.getXNodeVal(x, "dimcode");
			o.operator = i2b2.h.getXNodeVal(x, "operator");
			o.table_name = i2b2.h.getXNodeVal(x, "tablename");
			o.tooltip = i2b2.h.getXNodeVal(x, "tooltip");
			o.hasChildren = i2b2.h.getXNodeVal(x, "visualattributes");
			
			
			var bCanExp = false;
			if (o.hasChildren.substring(1,0) === "C"){
				// render as category
				icon = 'root';
				sDD = '';
				sIG = ' isGroup="Y"';
				bCanExp = true;
			} else if (o.hasChildren.substring(1,0) === "F")  {
				// render as possibly having children
				icon = 'branch';
				bCanExp = true;
				//var sCanExpand = ' canExpand="Y"';
			} else if (o.hasChildren.substring(1,0) === "O")  {
				// render as possibly having children
				icon = 'root';
				bCanExp = true;
				//var sCanExpand = ' canExpand="Y"';		
			} else if (o.hasChildren.substring(1,0) === "D") {
				// render as possibly having children
				icon = 'branch';
				bCanExp = true;
				//var sCanExpand = ' canExpand="Y"';
		
			} else {
				// render as not having children
				var icon = 'leaf';
				bCanExp = false;
			}
						
			newOptions.icon = {
				root: "sdx_ONT_CONCPT_"+icon+".gif",
				branch: "sdx_ONT_CONCPT_"+icon+".gif",
				leaf: "sdx_ONT_CONCPT_"+icon+".gif"
			};
			newOptions.showchildren = false;
			newOptions.title = o.name;
			break;
		case "PATIENT_COUNT_XML":
			// Patient Record Count
			sdxCode = "PRC";
			// XPath query exploits faults in XML message namespace declarations to avoid creation of namespace resolver kluges that perform no resolving
			var x = i2b2.h.XPath(sdxData.origData.xmlOrig, "work_xml/descendant::result_instance_id/..")[0];
			var o = {};
			o.xmlOrig = x;
			o.result_instance_id = i2b2.h.getXNodeVal(x, "result_instance_id");
			o.PRC_id = o.result_instance_id;
			o.QI_id = i2b2.h.getXNodeVal(x, "query_instance_id");
			o.QM_id = "";
			o.start_date = i2b2.h.getXNodeVal(x, "start_date");
			o.end_date = i2b2.h.getXNodeVal(x, "end_date");
			o.result_type = "PATIENT_COUNT_XML";
			o.size = i2b2.h.getXNodeVal(x, "set_size");
			if (o.size > 10) {
				o.title = "Patient Count - "+o.size+" patients";
			} else {
				if (i2b2.h.isSHRINE()) {
					o.title = "Patient Count - 10 patients or less";
				} else {
					o.title = "Patient Count - "+o.size+" patients";
				}
			}				
			newOptions.showchildren = false;
			newOptions.icon = "sdx_CRC_PRC.jpg"
			newOptions.title = o.title;
			break;
		case "GROUP_TEMPLATE":
			// Query Group Definition (Query Panel)
			sdxCode = "QGDEF";
			// XPath query exploits faults in XML message namespace declarations to avoid creation of namespace resolver kluges
			var x = i2b2.h.XPath(sdxData.origData.xmlOrig, "work_xml/descendant::panel_number/..")[0];
			var o = {};
			o.xmlOrig = x;
			o.result_type = "GROUP_TEMPLATE";
			o.QGDEF_name = i2b2.h.XPath(x, "@name")[0].nodeValue;
			o.key = false;
			newOptions.showchildren = false;
			newOptions.icon = "sdx_CRC_QGDEF.jpg"
			break;
		case "QUERY_DEFINITION":
			sdxCode = "QDEF";			
			x = i2b2.h.XPath(sdxData.origData.xmlOrig, "work_xml/descendant::query_name/..")[0];
			var o = {};
			o.xmlOrig = x;
			o.result_type = "QUERY_DEFINITION";
			o.QDEF_name = i2b2.h.XPath(x,"//descendant::query_name/text()")[0].nodeValue;
			o.key = false;
			newOptions.showchildren = false;
			newOptions.icon = "sdx_CRC_QDEF.jpg";
			break;
		default:
			var t = "No SDX Object exists to represent data-type "+sdxData.origData.encapType;
			console.warn(t);
			// encapsulate as a Generic XML object
			sdxCode = "XML";
			var o = {};
			var t = i2b2.h.XPath(sdxData.origData.xmlOrig, "descendant::work_xml")[0].childNodes;
			for (i=0; i<t.length; i++) {
				if (t[i].nodeType==1) {
					o.xmlOrig = t[i];
					break;
				}
			}
			o.result_type = "UNKNOWN";
			o.name = sdxData.origData.name; // inherit name from Workplace Node
			o.key = false;
			newOptions.showchildren = false;
			newOptions.icon = "sdx_WORK_XML.gif";
			break;
	}

	
	newOptions.title = sdxData.origData.name;
	if (sdxCode) {
		var sdxDataNode = i2b2.sdx.Master.EncapsulateData(sdxCode, o);
		sdxDataNode.origData.name = options.title;
		sdxDataNode.sdxInfo.sdxDisplayName = options.title;
		sdxData.sdxUnderlyingPackage = sdxDataNode;
		subclassHTML = i2b2.sdx.Master.RenderHTML(targetDiv, sdxDataNode, newOptions);
	}
	return subclassHTML;
}


// *********************************************************************************
//	HANDLE HOVER OVER TARGET ENTRY (DEFAULT HANDLER)
// *********************************************************************************
i2b2.sdx.TypeControllers.WRK.onHoverOver = function(e, id, ddProxy) {    
	var el = $(id);	
	if (el) { Element.addClassName(el,'ddWRKTarget'); }
}

// *********************************************************************************
//	HANDLE HOVER OVER TARGET EXIT (DEFAULT HANDLER)
// *********************************************************************************
i2b2.sdx.TypeControllers.WRK.onHoverOut = function(e, id, ddProxy) { 
	var el = $(id);	
	if (el) { Element.removeClassName(el,'ddWRKTarget'); }
}


// *********************************************************************************
//	ADD DATA TO TREENODE (DEFAULT HANDLER)
// *********************************************************************************
i2b2.sdx.TypeControllers.WRK.AppendTreeNode = function(yuiTree, yuiRootNode, sdxDataPack, callbackLoader) {}


// *********************************************************************************
//	ATTACH DRAG TO DATA (DEFAULT HANDLER)
// *********************************************************************************
i2b2.sdx.TypeControllers.WRK.AttachDrag2Data = function(divParentID, divDataID){
	if (Object.isUndefined($(divDataID))) {	return false; }
	
	// get the i2b2 data from the yuiTree node
	var tvTree = YAHOO.widget.TreeView.getTree(divParentID);
	var tvNode = tvTree.getNodeByProperty('nodeid', divDataID);
	if (!Object.isUndefined(tvNode.DDProxy)) { return true; }
	
	// attach DD
	var t = new i2b2.sdx.TypeControllers.WRK.DragDrop(divDataID)
	t.yuiTree = tvTree;
	t.yuiTreeNode = tvNode;
	tvNode.DDProxy = t;
}




// *********************************************************************************
//	DRAG DROP PROXY CONTROLLER
// *********************************************************************************
i2b2.sdx.TypeControllers.WRK.DragDrop = function(id, config) {
	if (id) {
		this.init(id, 'WRK',{isTarget:true});
		this.initFrame();
	}
	var s = this.getDragEl().style;
	s.borderColor = "transparent";
	s.opacity = 0.75;
	s.filter = "alpha(opacity=75)";
	s.whiteSpace = "nowrap";
	s.overflow = "hidden";
	s.textOverflow = "ellipsis";
};
YAHOO.extend(i2b2.sdx.TypeControllers.WRK.DragDrop, YAHOO.util.DDProxy);
i2b2.sdx.TypeControllers.WRK.DragDrop.prototype.startDrag = function(x, y) {
	var dragEl = this.getDragEl();
	var clickEl = this.getEl();
	dragEl.innerHTML = clickEl.innerHTML;
	dragEl.className = clickEl.className;
	dragEl.style.backgroundColor = '#FFFFEE';
	dragEl.style.color = clickEl.style.color;
	dragEl.style.border = "1px solid blue";
	dragEl.style.width = "160px";
	dragEl.style.height = "20px";
	this.setDelta(15,10);
};
i2b2.sdx.TypeControllers.WRK.DragDrop.prototype.endDrag = function(e) {};
i2b2.sdx.TypeControllers.WRK.DragDrop.prototype.alignElWithMouse = function(el, iPageX, iPageY) {
	var oCoord = this.getTargetCoord(iPageX, iPageY);
	if (!this.deltaSetXY) {
		var aCoord = [oCoord.x, oCoord.y];
		YAHOO.util.Dom.setXY(el, aCoord);
		var newLeft = parseInt( YAHOO.util.Dom.getStyle(el, "left"), 10 );
		var newTop  = parseInt( YAHOO.util.Dom.getStyle(el, "top" ), 10 );
		this.deltaSetXY = [ newLeft - oCoord.x, newTop - oCoord.y ];
	} else {
		var posX = (oCoord.x + this.deltaSetXY[0]);
		var posY = (oCoord.y + this.deltaSetXY[1]);
		//var scrSize = document.viewport.getDimensions();

	    var w =  window.innerWidth || (window.document.documentElement.clientWidth || window.document.body.clientWidth);
	    var h =  window.innerHeight || (window.document.documentElement.clientHeight || window.document.body.clientHeight);

		var maxX = parseInt(w-25-160);
		var maxY = parseInt(h-25);
		if (posX > maxX) {posX = maxX;}
		if (posX < 6) {posX = 6;}
		if (posY > maxY) {posY = maxY;}
		if (posY < 6) {posY = 6;}
		YAHOO.util.Dom.setStyle(el, "left", posX + "px");
		YAHOO.util.Dom.setStyle(el, "top",  posY + "px");
	}
	this.cachePosition(oCoord.x, oCoord.y);
	this.autoScroll(oCoord.x, oCoord.y, el.offsetHeight, el.offsetWidth);
};
i2b2.sdx.TypeControllers.WRK.DragDrop.prototype.onDragOver = function(e, id) {
	// fire the onHoverOver (use SDX so targets can override default event handler)
	var t = this.yuiTreeNode.data.i2b2_SDX.sdxUnderlyingPackage;
	if (t) {
		try {
			if (this.DDM.ids[t.sdxInfo.sdxType][id]) {
				i2b2.sdx.Master.onHoverOver(t.sdxInfo.sdxType, e, id, this); 
			} else {
				// fall back to WRK type processing
				i2b2.sdx.Master.onHoverOver('WRK', e, id, this);
			}
		} catch(e) {}
	} else {
		i2b2.sdx.Master.onHoverOver('WRK', e, id, this);
	}
};
i2b2.sdx.TypeControllers.WRK.DragDrop.prototype.onDragOut = function(e, id) {
	// fire the onHoverOut handler (use SDX so targets can override default event handlers)
	// fire the onHoverOver (use SDX so targets can override default event handler)
	var t = this.yuiTreeNode.data.i2b2_SDX.sdxUnderlyingPackage;
	if (t) {
		try {
			if (this.DDM.ids[t.sdxInfo.sdxType][id]) { 
				i2b2.sdx.Master.onHoverOut(t.sdxInfo.sdxType, e, id, this);
			} else {
				// fall back to WRK type processing
				i2b2.sdx.Master.onHoverOut('WRK', e, id, this);
			}
		} catch(e) {}
	} else {
		i2b2.sdx.Master.onHoverOut('WRK', e, id, this);
	}
};
i2b2.sdx.TypeControllers.WRK.DragDrop.prototype.onDragDrop = function(e, id) {
	// retreive the concept data from the dragged element
	var draggedTvNode = this.yuiTreeNode;
	var draggedData = draggedTvNode.data.i2b2_SDX;
	var t = this.yuiTreeNode.data.i2b2_SDX.sdxUnderlyingPackage;
	if (t) {
		try {
			if (this.DDM.ids[t.sdxInfo.sdxType][id]) { 
				i2b2.sdx.Master.onHoverOut(t.sdxInfo.sdxType, e, id, this); 
			} else {
				i2b2.sdx.Master.onHoverOut('WRK', e, id, this);
			}
		} catch(e) { i2b2.sdx.Master.onHoverOut('WRK', e, id, this); }
	} else {
		i2b2.sdx.Master.onHoverOut('WRK', e, id, this);
	}
	try {
		// restraints on Workplace moves
		var parentTree = YAHOO.widget.TreeView.findTreeByChildDiv(id);
		var targetTvNode = parentTree.getNodeByProperty('nodeid', id);
		if (targetTvNode.isDescendant(draggedTvNode)) { return false; } // can't drag parents into their children
		if (draggedTvNode.parent.data.nodeid == targetTvNode.data.nodeid) { return false; } // ignore moving a child onto it's current parent
		
		if (targetTvNode.data.i2b2_SDX.sdxInfo.sdxType=="WRK") {
			// drop onto a WRK node
			if (Object.isUndefined(draggedData.sdxUnderlyingPackage)) {
				// dragging a folder object
				i2b2.WORK.ctrlr.main.moveFolder(draggedTvNode, targetTvNode);
			} else {
				// dragging encapsulated data node
				// START bug fix: handle a bug in YUI (dragdrop.js @ lines 872-881)
				if (this._handledDragDropAlready) {
					return true;
				} else {
					this._handledDragDropAlready = true;
					var scopeHackThis = this;
					var resetClosure = function() {
						delete scopeHackThis._handledDragDropAlready;
					};
					setTimeout(resetClosure, 100);
				}
				// END bug fix

				// Server handles moving of non-folder items by deleting them from their old location and creating a new record as a child of the new location

				// Filter list of TV nodes to refresh so attempts are not made to refresh non-existent children (and kill DD reattachment)
				var tvRefreshList = [];
				tvRefreshList.push(targetTvNode);
				tvRefreshList.push(draggedTvNode.parent);
				tvRefreshList = i2b2.WORK.ctrlr.main._generateRefreshList(tvRefreshList);
				var funcRefresh = function() {
					// this function is fired after all the threads in the mutex have finished execution
					var cl_tvRefreshList = tvRefreshList;  // closure var
					// whack the "already loaded" status out of the node that need refreshing and 
					// initiate a dynamic loading of the childs nodes (including our newest addition)
					for (var i=0; i<cl_tvRefreshList.length; i++) {
						cl_tvRefreshList[i].collapse();
						cl_tvRefreshList[i].dynamicLoadComplete = false;
						cl_tvRefreshList[i].childrenRendered = false;
						cl_tvRefreshList[i].tree.removeChildren(cl_tvRefreshList[i]);
						cl_tvRefreshList[i].expand();
					}
				}
				var funcThreadDone = function(results) {
					// "this" needs to be scoped as the mutex token
					var r = this.ThreadFinished();
					if (r.error) { console.warn(r.errorMsg); }
				}

				// A mutex is needed to correctly time the TV refresh, create the initial context and get the first mutex token
				var mux1 = i2b2.h.JoiningMutex.contextCreate(null, funcRefresh, true);
				var muxName = mux1.name();
				// create a scoped callback object to for the AJAX to call after the server responds
				var scopedCB1 = new i2b2_scopedCallback();
				scopedCB1.scope = mux1;
				scopedCB1.callback = funcThreadDone;
				// get a second mutex token for the 2nd AJAX call
				var mux2 = i2b2.h.JoiningMutex.contextJoin(muxName);
				// scoped callback object to for AJAX call
				var scopedCB2 = new i2b2_scopedCallback();
				scopedCB2.scope = mux2;
				scopedCB2.callback = funcThreadDone;
				// fire AJAX
				i2b2.WORK.ctrlr.main.moveFolder(draggedTvNode, targetTvNode); 
			}
		} else {
			// translate - we are not dropping onto another WRK node
			console.error("A unique execution event has occurred.  The event may or may not have been processed correctly.");
			i2b2.sdx.Master.ProcessDrop(draggedData.sdxUnderlyingPackage, id);
		}
	} catch(e) {
		if (draggedData.sdxUnderlyingPackage) {
			i2b2.sdx.Master.ProcessDrop(draggedData.sdxUnderlyingPackage, id);
		} else {
			i2b2.sdx.Master.ProcessDrop(draggedData, id);
		}
	}
};
	


// *********************************************************************************
//	<BLANK> DROP HANDLER 
//	!!!! DO NOT EDIT - ATTACH YOUR OWN CUSTOM ROUTINE USING
//	!!!! THE i2b2.sdx.Master.setHandlerCustom FUNCTION
// *********************************************************************************
i2b2.sdx.TypeControllers.WRK.DropHandler = function(sdxData) {
	alert('[Workplace Object DROPPED] You need to create your own custom drop event handler.');
}


console.timeEnd('execute time');
console.groupEnd();