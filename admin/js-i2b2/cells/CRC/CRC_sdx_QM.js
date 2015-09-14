/**
 * @projectDescription	The SDX controller library for the QueryMaster data-type.
 * @namespace	i2b2.sdx.TypeControllers.QM
 * @inherits 	i2b2.sdx.TypeControllers
 * @author		Nick Benik, Griffin Weber MD PhD
 * @version 	1.3
 * @see 		i2b2.sdx
 * ----------------------------------------------------------------------------------------
 * updated 9-15-08: RC4 launch [Nick Benik] 
 */
console.group('Load & Execute component file: CRC > SDX > QueryMaster');
console.time('execute time');


i2b2.sdx.TypeControllers.QM = {};
i2b2.sdx.TypeControllers.QM.model = {};
// *********************************************************************************
//	ENCAPSULATE DATA
// *********************************************************************************
/** 
 * Get the sdxInfo data template for all QueryMaster.
 * @memberOf i2b2.sdx.TypeControllers.QM
 * @method
 * @return {Object} Returns a data object containing sdxType, sdxKeyName, sdxControlCell info for QueryMaster-type objects.
 * @author Nick Benik
 * @version 1.0
 * @alias i2b2.sdx.Master.EncapsulateData
 */
i2b2.sdx.TypeControllers.QM.getEncapsulateInfo = function() {
	// this function returns the encapsulation head information
	return {sdxType: 'QM', sdxKeyName: 'id', sdxControlCell:'CRC', sdxDisplayNameKey:'name'};
}


// *********************************************************************************
//	GENERATE HTML (DEFAULT HANDLER)
// *********************************************************************************
i2b2.sdx.TypeControllers.QM.RenderHTML= function(sdxData, options, targetDiv) {    
	// OPTIONS:
	//	title: string
	//	showchildren: true | false
	//	cssClass: string
	//	icon: [data object]
	//		icon: 		(filename of img, appended to i2b2_root+cellDir + '/assets')
	//		iconExp: 	(filename of img, appended to i2b2_root+cellDir + '/assets')
	//	dragdrop: string (function name)
	//	context: string
	//	click: string 
	//	dblclick: string
	
	if (Object.isUndefined(options)) { options = {}; }
	var render = {html: retHtml, htmlID: id};
	var conceptId = sdxData.name;
	var id = "CRC_ID-" + i2b2.GUID();
	
	
	if (Object.isUndefined(conceptId))
	{
		options.title = sdxData.sdxInfo.sdxDisplayName;	
	}

	
	// process drag drop controllers
	if (!Object.isUndefined(options.dragdrop)) {
// NOTE TO SELF: should attachment of node dragdrop controller be handled by the SDX system as well? 
// This would ensure removal of the onmouseover call in a cross-browser way
		var sDD = '  onmouseover="' + options.dragdrop + '(\''+ targetDiv.id +'\',\'' + id + '\')" ';
	} else {
		var sDD = '';
	}

	if (Object.isUndefined(options.cssClass)) { options.cssClass = 'sdxDefaultQM';}

	// user can override
	bCanExp = true;
	if (Object.isBoolean(options.showchildren)) { 
		bCanExp = options.showchildren;
	}
	render.canExpand = bCanExp;
	render.iconType = "QM";
	
	if (!Object.isUndefined(options.icon)) { render.icon = i2b2.hive.cfg.urlFramework + 'cells/CRC/assets/'+ options.icon }
	if (!Object.isUndefined(options.iconExp)) { render.iconExp = i2b2.hive.cfg.urlFramework + 'cells/CRC/assets/'+ options.iconExp }
	// in cases of one set icon, copy valid icon to the missing icon
	if (Object.isUndefined(render.icon) && !Object.isUndefined(render.iconExp)) {	render.icon = render.iconExp; }
	if (!Object.isUndefined(render.icon) && Object.isUndefined(render.iconExp)) {	render.iconExp = render.icon; }

	// handle the event controllers
	var sMainEvents = sDD;
	var sImgEvents = sDD;
	if (options.click) {sMainEvents += ' onclick="'+ options.click +'" '; }
	if (options.dblclick) {sMainEvents += ' ondblclick="'+ options.dblclick +'" '; }
	if (options.context) {sMainEvents += ' oncontext="'+ options.context +'" '; } else {retHtml += ' oncontextmenu="return false" '; }
	// **** Render the HTML ***
	var retHtml = '<DIV id="' + id + '" ' + sMainEvents + ' style="white-space:nowrap;cursor:pointer;">';
	retHtml += '<DIV ';
	if (Object.isString(options.cssClass)) {
		retHtml += ' class="'+options.cssClass+'" ';
	} else {
		retHtml += ' class= "sdxDefaultQM" ';
	}
	retHtml += sImgEvents;
	retHtml += '>';
	retHtml += '<IMG src="'+render.icon+'"/> '; 
	if (!Object.isUndefined(options.title)) {
		retHtml += options.title;
	} else {
		console.warn('[SDX RenderHTML] no title was given in the creation options for an CRC>QM node!');
		retHtml += ' QM '+id;
	}
	retHtml += '</DIV></DIV>';
	render.html = retHtml;
	render.htmlID =  id;
	var retObj = {};
	Object.extend(retObj, sdxData);
	retObj.renderData = render;
	return retObj;
}


// *********************************************************************************
//	HANDLE HOVER OVER TARGET ENTRY (DEFAULT HANDLER)
// *********************************************************************************
i2b2.sdx.TypeControllers.QM.onHoverOver = function(e, id, ddProxy) {    
	var el = $(id);	
	if (el) { Element.addClassName(el,'ddQMTarget'); }
}


// *********************************************************************************
//	HANDLE HOVER OVER TARGET EXIT (DEFAULT HANDLER)
// *********************************************************************************
i2b2.sdx.TypeControllers.QM.onHoverOut = function(e, id, ddProxy) { 
	var el = $(id);	
	if (el) { Element.removeClassName(el,'ddQMTarget'); }
}


// *********************************************************************************
//	ADD DATA TO TREENODE (DEFAULT HANDLER)
// *********************************************************************************
i2b2.sdx.TypeControllers.QM.AppendTreeNode = function(yuiTree, yuiRootNode, sdxDataPack, callbackLoader) {    
	var myobj = { html: sdxDataPack.renderData.html, nodeid: sdxDataPack.renderData.htmlID}
	var tmpNode = new YAHOO.widget.HTMLNode(myobj, yuiRootNode, false, true);
	if (sdxDataPack.renderData.canExpand && !Object.isUndefined(callbackLoader)) {
		// add the callback to load child nodes
		sdxDataPack.sdxInfo.sdxLoadChildren = callbackLoader;
	}
	tmpNode.data.i2b2_SDX= sdxDataPack;
	tmpNode.toggle = function() {
		if (!this.tree.locked && ( this.hasChildren(true) ) ) {
			var data = this.data.i2b2_SDX.renderData;
			var img = this.getContentEl();
			img = Element.select(img,'img')[0];
			if (this.expanded) { 
				img.src = data.icon;
				this.collapse(); 
			} else { 
				img.src = data.iconExp;
				this.expand(); 
			}
		}
	};
	if (!sdxDataPack.renderData.canExpand) { tmpNode.dynamicLoadComplete = true; }
	return tmpNode;
}


/** 
 * Get the child QueryInstance records for the given QueryMaster.
 * @param {Object} sdxParentNode	The parent node we want to find the children of.
 * @param {ScopedCallback} onCompleteCallback A scoped-callback function to be executed using the results array.
 * @return {Boolean} Returns true to the calling function.
 * @return {Array} Returns an array of QueryInstance data represented as SDX Objects (without render data) to the callback function passed.
 * @memberOf i2b2.sdx.TypeControllers.QM
 * @method
 * @author Nick Benik
 * @version 1.0
 * @alias i2b2.sdx.Master.getChildRecords
 */
i2b2.sdx.TypeControllers.QM.getChildRecords = function(sdxParentNode, onCompleteCallback) {
	var scopedCallback = new i2b2_scopedCallback();
	scopedCallback.scope = sdxParentNode;
	scopedCallback.callback = function(results) {
		var cl_node = sdxParentNode;
		var cl_onCompleteCB = onCompleteCallback;
		// THIS function is used to process the AJAX results of the getChild call
		//		results data object contains the following attributes:
		//			refXML: xmlDomObject <--- for data processing
		//			msgRequest: xml (string)
		//			msgResponse: xml (string)
		//			error: boolean
		//			errorStatus: string [only with error=true]
		//			errorMsg: string [only with error=true]
	
		var retMsg = {
			error: results.error,
			msgRequest: results.msgRequest,
			msgResponse: results.msgResponse,
			msgUrl: results.msgUrl,
			results: null
		};
		var retChildren = [];

		// find parent node		
		var dm = i2b2.CRC.model.QueryMasters;
		var dm_loc = 'i2b2.CRC.model.QueryMasters';
		var findFunc = function(item_rec) {
			// this function expects the second argument to be the target node's key value
			var hash_key = item_rec[0];
			var QM_rec = item_rec[1];
			if (QM_rec.sdxInfo.sdxKeyValue == this.strip()) { return true; }
		}
		var parent_QM = dm.findAll(findFunc, cl_node.sdxInfo.sdxKeyValue);
		if (parent_QM[0]) {
			var pn = parent_QM[0].value;
		} else {
			console.error('Parent QM node was not found in the CRC data model!');
			return false;
		}
		// extract records from XML msg
		var qi = results.refXML.getElementsByTagName('query_instance');
		for(var i1=0; i1<1*qi.length; i1++) {
			var o = new Object;
			o.xmlOrig = qi[i1];
			o.query_master_id = i2b2.h.getXNodeVal(qi[i1],'query_master_id');
			o.query_instance_id = i2b2.h.getXNodeVal(qi[i1],'query_instance_id');
			o.id = o.query_instance_id;
			o.batch_mode = i2b2.h.getXNodeVal(qi[i1],'batch_mode');
			o.start_date = i2b2.h.getXNodeVal(qi[i1],'start_date');
			o.end_date = i2b2.h.getXNodeVal(qi[i1],'end_date');
			o.query_status_type = i2b2.h.getXNodeVal(qi[i1],'query_status_type');
			o.title = "Results of " + pn.origData.name;
			if (i2b2.h.getXNodeVal(qi[i1],'description') != "COMPLETED")
			{
				o.title += " - " + i2b2.h.getXNodeVal(qi[i1],'description');
			}
			var sdxDataNode = i2b2.sdx.Master.EncapsulateData('QI',o);
			// append to the QI record onto the QM in CRC data model
			var sdxDataNode = i2b2.sdx.Master.Save(sdxDataNode, pn);
			// append the data node to our returned results
			retChildren.push(sdxDataNode);
		}
		pn.children.loaded = true;
		// TODO: broadcast a data update event of the CRC data model
		retMsg.results = retChildren;
		if (getObjectClass(cl_onCompleteCB)=='i2b2_scopedCallback') {
			cl_onCompleteCB.callback.call(cl_onCompleteCB.scope, retMsg);
		} else {
			cl_onCompleteCB(retMsg);
		}
	}
	i2b2.CRC.ajax.getQueryInstanceList_fromQueryMasterId("CRC:SDX:QueryMaster", {qm_key_value: sdxParentNode.sdxInfo.sdxKeyValue}, scopedCallback);
}


i2b2.sdx.TypeControllers.QM.SaveToDataModel = function(sdxData) {
	// save to CRC data model
	var dm_loc = 'i2b2.CRC.model.QueryMasters';
	var qm_id = sdxData.sdxInfo.sdxKeyValue;
	var qm_hash = i2b2.sdx.Master._KeyHash(qm_id);

	// class for all SDX communications
	function i2b2_SDX_Encapsulation_EXTENDED() {}
	// create an instance and populate with info
	var t = new i2b2_SDX_Encapsulation_EXTENDED();
	t.origData = Object.clone(sdxData.origData);
	t.sdxInfo = Object.clone(sdxData.sdxInfo);
	t.parent = null;
	t.children = new Hash();
	t.children.loaded = false;
	// add to hash
	i2b2.CRC.model.QueryMasters.set(qm_hash, t);

	// TODO: send data update signal (use JOINING-MUTEX or AGGREGATING-MUTEX to avoid rapid fire of event!)
	return t;
}


i2b2.sdx.TypeControllers.QM.LoadFromDataModel = function(key_value) {
	// load from CRC data model
	var dm = i2b2.CRC.model.QueryMasters;
	var dm_loc = 'i2b2.CRC.model.QueryMasters';
	var qm_id = key_value;
	var qm_hash = i2b2.sdx.Master._KeyHash(qm_id);
	// retreve the record
	return dm.get(qm_hash);
}


i2b2.sdx.TypeControllers.QM.ClearAllFromDataModel= function(sdxOptionalParent) {
	i2b2.CRC.model.QueryMasters = new Hash();
	var dm_loc = 'i2b2.CRC.model.QueryMasters';
	// TODO: send data update signal (use JOINING-MUTEX or AGGREGATING-MUTEX to avoid rapid fire of event!)
	return true;
}

i2b2.sdx.TypeControllers.QM.LoadChildrenFromTreeview = function(node, onCompleteCallback) {
	var scopedCallback = new i2b2_scopedCallback();
	scopedCallback.scope = node.data.i2b2_SDX;
	scopedCallback.callback = function(retCellPack) {
		var cl_node = node;
		var cl_onCompleteCB = onCompleteCallback;

		var results = retCellPack.results;			
		for(var i1=0; i1<1*results.length; i1++) {
			var o = results[i1];
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
		// handle the callback
		if (getObjectClass(cl_onCompleteCB)=='i2b2_scopedCallback') {
			cl_onCompleteCB.callback.call(cl_onCompleteCB.scope, retCellPack);
		} else {
			cl_onCompleteCB(retCellPack);
		}
	}
	var sdxParentNode = node.data.i2b2_SDX;
	i2b2.sdx.Master.getChildRecords(sdxParentNode, scopedCallback);
}


// *********************************************************************************
//	ATTACH DRAG TO DATA (DEFAULT HANDLER)
// *********************************************************************************
i2b2.sdx.TypeControllers.QM.AttachDrag2Data = function(divParentID, divDataID){
	if (Object.isUndefined($(divDataID))) {	return false; }
	
	// get the i2b2 data from the yuiTree node
	var tvTree = YAHOO.widget.TreeView.getTree(divParentID);
	var tvNode = tvTree.getNodeByProperty('nodeid', divDataID);
	if (!Object.isUndefined(tvNode.DDProxy)) { return true; }
	
	//If termporal query than dont allow drag
	//if (!Object.isUndefined(tvNode.data.i2b2_SDX.origData.master_type_cd)   && tvNode.data.i2b2_SDX.origData.master_type_cd == "TEMPORAL") { return true; }
	
	// attach DD
	var t = new i2b2.sdx.TypeControllers.QM.DragDrop(divDataID)
	t.yuiTree = tvTree;
	t.yuiTreeNode = tvNode;
	tvNode.DDProxy = t;
	
	// clear the mouseover attachment function
	var tdn = $(divDataID);
	if (!Object.isUndefined(tdn.onmouseover)) { 
		try {
			delete tdn.onmouseover; 
		} catch(e) {
			tdn.onmouseover; 
		}
	}
	if (!Object.isUndefined(tdn.attributes)) {
		for (var i=0;i<tdn.attributes.length; i++) {
			if (tdn.attributes[i].name=="onmouseover") { 
				try {
					delete tdn.onmouseover; 
				} catch(e) {
					tdn.onmouseover; 
				}
			}
		}
	}
}




// *********************************************************************************
//	DRAG DROP PROXY CONTROLLER
// *********************************************************************************
i2b2.sdx.TypeControllers.QM.DragDrop = function(id, config) {
	if (id) {
		this.init(id, 'QM', {isTarget:false});
		this.initFrame();
	}
	var s = this.getDragEl().style;
	s.borderColor = "transparent";
	s.opacity = 0.75;
	s.filter = "alpha(opacity=75)";
	s.whiteSpace = "nowrap";
	s.overflow = "hidden";
	s.textOverflow = "ellipsis";
	// add this QM to other DragDrop groups (for translation functionality)
	this.addToGroup("QI");
	this.addToGroup("PRS");
};
YAHOO.extend(i2b2.sdx.TypeControllers.QM.DragDrop, YAHOO.util.DDProxy);
i2b2.sdx.TypeControllers.QM.DragDrop.prototype.startDrag = function(x, y) {
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
i2b2.sdx.TypeControllers.QM.DragDrop.prototype.endDrag = function(e, sdxType) {
	// remove DragDrop targeting CCS
	var targets = YAHOO.util.DDM.getRelated(this, true); 
	for (var i=0; i<targets.length; i++) {      
		var targetEl = targets[i]._domRef;
		try {
			var ddCtrlr = YAHOO.util.DragDropMgr.getDDById(targetEl.id);
			if(ddCtrlr.groups['QM']) { i2b2.sdx.Master.onHoverOut('QM', e, targetEl, this); }
		} catch(e) {}
	} 
};
i2b2.sdx.TypeControllers.QM.DragDrop.prototype.alignElWithMouse = function(el, iPageX, iPageY) {
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
i2b2.sdx.TypeControllers.QM.DragDrop.prototype.onDragOver = function(e, id, sdxType) {
	// check to see if on-the-fly object type translation is needed
	var translateTo = false;
	if (Object.isUndefined(this.DDM.dragOvers[id])) { return false; }
	// we must save which target we are over for the DragOut event later
	this.lastDragOver = this.DDM.dragOvers[id]; 
	var t = this.DDM.dragOvers[id].groups;
	if (!t['QM']) {
		// TRANSLATION NEEDED!
		if (t['QI']) {
			translateTo = "QI";
		} else if (t['PRS']) {
			translateTo = "PRS";
		}
	} else {
		var translateTo = "QM";
	}	
	// fire the onHoverOver (use SDX so targets can override default event handler)
	if (translateTo) { i2b2.sdx.Master.onHoverOver(translateTo, e, id, this); }
};
i2b2.sdx.TypeControllers.QM.DragDrop.prototype.onDragOut = function(e, id, sdxType) {
	// fire the onHoverOut handler (use SDX so targets can override default event handlers)
	// check to see if on-the-fly object type translation is needed
	var translateTo = false;
	if (Object.isUndefined(this.lastDragOver)) { return false; }
	var t = this.lastDragOver.groups;
	if (!t['QM']) {
		// TRANSLATION NEEDED!
		if (t['QI']) {
			translateTo = "QI";
		} else if (t['PRS']) {
			translateTo = "PRS";
		}
	} else {
		var translateTo = "QM";
	}	
	// fire the onHoverOver (use SDX so targets can override default event handler)
	if (translateTo) { i2b2.sdx.Master.onHoverOut(translateTo, e, id, this); }
};
i2b2.sdx.TypeControllers.QM.DragDrop.prototype.onDragDrop = function(e, id, sdxType) {
	// handle a bug in YUI (dragdrop.js @ lines 872-881)
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
	// check to see if on-the-fly object type translation is needed
	var translateTo = false;
	if (Object.isUndefined(this.lastDragOver)) { return false; }
	var t = this.lastDragOver.groups;
	if (!t['QM']) {
		// TRANSLATION NEEDED!
		if (t['QI']) {
			translateTo = "QI";
		} else if (t['PRS']) {
			translateTo = "PRS";
		}
	} else {
		translateTo = "QM";
	}	
	// fire the onHoverOver (use SDX so targets can override default event handler)
	if (translateTo) { 
		i2b2.sdx.Master.onHoverOut(translateTo, e, id, this);
		// retreive the concept data from the dragged element
		// PERFROM on-the-fly OBJECT TRANSLATION HERE!!
		var draggedData;
		switch (translateTo) {
			case "QM":
				// no translation needed
				var draggedData = [];
				draggedData.push(this.yuiTreeNode.data.i2b2_SDX);
				i2b2.sdx.Master.ProcessDrop(draggedData, id);
				break;
			case "QI":
				var draggedData = this.yuiTreeNode.data.i2b2_SDX;
				if (draggedData.children.size() == 0) {
					alert("Translate QM to QI");
				} else {
					alert("QI already loaded, drop existing children");
//					i2b2.sdx.Master.ProcessDrop(draggedData, id);	
				}
				break;
			case "PRS":
				var draggedData = this.yuiTreeNode.data.i2b2_SDX;
				if (!draggedData.children.loaded) {
					// a little explaination about the below code: loading/expanding the
					// treeview recursively via asynchronous calls
					var tn1 = this.yuiTreeNode;
					var id1 = id;
					i2b2.sdx.Master.LoadChildrenFromTreeview(this.yuiTreeNode, (function(){
						// hated closures are the most loved
						var cl_tn1 = tn1;
						var cl_id1 = id1;
						cl_tn1.dynamicLoadComplete = true;
						cl_tn1.expand();
						for (var i1=0; i1<cl_tn1.children.length; i1++) {
							var tn2 = cl_tn1.children[i1];
							var id2 = cl_id1;
							i2b2.sdx.Master.LoadChildrenFromTreeview(tn2, (function(){
								// hated closures are the most loved
								var cl_tn2 = tn2;
								var cl_id2 = id2;
								cl_tn2.dynamicLoadComplete = true;
								cl_tn2.expand();
								var draggedData = [];
								for (var i2=0; i2<cl_tn2.children.length; i2++) {
									if (cl_tn2.children[i2].data.i2b2_SDX.sdxInfo.sdxType == 'PRS') {
									draggedData.push(cl_tn2.children[i2].data.i2b2_SDX);
								}
								}
								// send translated info to the drop target handler
								i2b2.sdx.Master.ProcessDrop(draggedData, cl_id2);
							}));
								}
							}));
				} else {
					// our children are already loaded, loop thru and process
					var tn1 = this.yuiTreeNode;
					var id1 = id;
					draggedData.children.each(function(item_rec) {
						var cl_tn1 = tn1;
						var cl_id1 = id1;
						try {
							var cn = item_rec.value;
							if (!cn.children.loaded) {
								i2b2.sdx.Master.LoadChildrenFromTreeview(cl_tn1, (function(){
									// hated closures are the most loved
									var cl_tn2 = cl_tn1;
									var cl_id2 = cl_id1;
									cl_tn2.dynamicLoadComplete = true;
									cl_tn2.expand();
									var draggedData = [];
									for (var i2=0; i2<cl_tn2.children.length; i2++) {
										if (cl_tn2.children[i2].data.i2b2_SDX.sdxInfo.sdxType == 'PRS') {
										draggedData.push(cl_tn2.children[i2].data.i2b2_SDX);
						}
						}
									// send translated info to the drop target handler
									i2b2.sdx.Master.ProcessDrop(draggedData, cl_id2);	
					}));
				} else {
								// children already loaded
								var draggedData = [];
								cl_tn1.expand();
								for (var i1=0; i1<cl_tn1.children.length; i1++) {
									// looping QI
									var tn2 = cl_tn1.children[i1];
									tn2.expand();
									for (var i2=0; i2<tn2.children.length; i2++) {
										// looping PRS
										if (tn2.children[i2].data.i2b2_SDX.sdxInfo.sdxType == 'PRS') {
										draggedData.push(tn2.children[i2].data.i2b2_SDX);
										}
									}
								}
								// send translated info to the drop target handler
								i2b2.sdx.Master.ProcessDrop(draggedData, cl_id1);	
							}
						} catch(e) { console.error("An error has occurred while trying to translate a QM into PRS data"); }
					});
				}
				break;
		}
	}
};




//---------------------------------------------------------------------------------
// YUI's got a bug!!!!
//---------------------------------------------------------------------------------
//
// 872 for (var b = 0, len = tmp.length; b < len; ++b) {
// 873 	if (dc.events[b4]) {

// 			dc[b4](e, tmp[b].id, inGroups[X]);   <-- should fire multiple times for each inGroups Record?
//	 		dc.fireEvent(cev, { event: e, info: tmp[b].id, group: inGroups[X] }); <-- should fire multiple times for each inGroups Record?

// 874 		dc[b4](e, tmp[b].id, inGroups[0]);
// 875 		dc.fireEvent(b4 + 'Event', { event: e, info: tmp[b].id, group: inGroups[0] });
// 876 	}
// 877 	if (dc.events[check]) {
// 878 		dc[ev](e, tmp[b].id, inGroups[0]);

// 			dc[b4](e, tmp[b].id, inGroups[X]);   <-- should fire multiple times for each inGroups Record?
//	 		dc.fireEvent(cev, { event: e, info: tmp[b].id, group: inGroups[X] }); <-- should fire multiple times for each inGroups Record?

// 879 		dc.fireEvent(cev, { event: e, info: tmp[b].id, group: inGroups[0] });
// 880 	}
// 881 }





// *********************************************************************************
//	<BLANK> DROP HANDLER 
//	!!!! DO NOT EDIT - ATTACH YOUR OWN CUSTOM ROUTINE USING
//	!!!! THE i2b2.sdx.Master.setHandlerCustom FUNCTION
// *********************************************************************************
i2b2.sdx.TypeControllers.QM.DropHandler = function(sdxData) {
	alert('[QueryMaster DROPPED] You need to create your own custom drop event handler.');
}


console.timeEnd('execute time');
console.groupEnd();