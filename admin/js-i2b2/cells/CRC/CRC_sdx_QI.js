/**
 * @projectDescription	The SDX controller library for the QueryInstance data-type.
 * @namespace	i2b2.sdx.TypeControllers.QI
 * @inherits 	i2b2.sdx.TypeControllers
 * @author		Nick Benik, Griffin Weber MD PhD
 * @version 	1.3
 * @see 		i2b2.sdx
 * ----------------------------------------------------------------------------------------
 * updated 9-15-08: RC4 launch [Nick Benik] 
 */
console.group('Load & Execute component file: CRC > SDX > QueryInstance');
console.time('execute time');


i2b2.sdx.TypeControllers.QI = {};
i2b2.sdx.TypeControllers.QI.model = {};
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
i2b2.sdx.TypeControllers.QI.getEncapsulateInfo = function() {
	// this function returns the encapsulation head information
	return {sdxType: 'QI', sdxKeyName: 'query_instance_id', sdxControlCell:'CRC', sdxDisplayNameKey:'title'};
}


// *********************************************************************************
//	GENERATE HTML (DEFAULT HANDLER)
// *********************************************************************************
i2b2.sdx.TypeControllers.QI.RenderHTML= function(sdxData, options, targetDiv) {    
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
	
	// process drag drop controllers
	if (!Object.isUndefined(options.dragdrop)) {
// NOTE TO SELF: should attachment of node dragdrop controller be handled by the SDX system as well? 
// This would ensure removal of the onmouseover call in a cross-browser way
		var sDD = '  onmouseover="' + options.dragdrop + '(\''+ targetDiv.id +'\',\'' + id + '\')" ';
	} else {
		var sDD = '';
	}

	if (Object.isUndefined(options.cssClass)) { options.cssClass = 'sdxDefaultQI';}

	// user can override
	bCanExp = true;
	if (Object.isBoolean(options.showchildren)) { 
		bCanExp = options.showchildren;
	}
	render.canExpand = bCanExp;
	render.iconType = "QI";
	
	if (!Object.isUndefined(options.icon)) { render.icon = i2b2.hive.cfg.urlFramework + 'cells/CRC/assets/'+ options.icon }
	if (!Object.isUndefined(options.iconExp)) { render.iconExp = i2b2.hive.cfg.urlFramework + 'cells/CRC/assets/'+ options.iconExp }
	// in cases of one set icon, copy valid icon to the missing icon
	if (Object.isUndefined(render.icon) && !Object.isUndefined(render.iconExp)) {	render.icon = sdxData.iconExp; }
	if (!Object.isUndefined(render.icon) && Object.isUndefined(render.iconExp)) {	render.iconExp = sdxData.icon; }

	// handle the event controllers
	var sMainEvents = sDD;
	var sImgEvents = sDD;

	// **** Render the HTML ***
	var retHtml = '<DIV id="' + id + '" ' + sMainEvents + ' style="white-space:nowrap;cursor:pointer;">';
	retHtml += '<DIV ';
	if (Object.isString(options.cssClass)) {
		retHtml += ' class="'+options.cssClass+'" ';
	} else {
		retHtml += ' class= "sdxDefaultQI" ';
	}
	retHtml += sImgEvents;
	retHtml += '>';
	retHtml += '<IMG src="'+render.icon+'"/> '; 
	if (!Object.isUndefined(options.title)) {
		retHtml += options.title;
	} else {
		console.warn('[SDX RenderHTML] no title was given in the creation options for an CRC>QI node!');
		retHtml += ' QI '+id;
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
i2b2.sdx.TypeControllers.QI.onHoverOver = function(e, id, ddProxy) {    
	var el = $(id);	
	if (el) { Element.addClassName(el,'ddQITarget'); }
}


// *********************************************************************************
//	HANDLE HOVER OVER TARGET EXIT (DEFAULT HANDLER)
// *********************************************************************************
i2b2.sdx.TypeControllers.QI.onHoverOut = function(e, id, ddProxy) { 
	var el = $(id);	
	if (el) { Element.removeClassName(el,'ddQITarget'); }
}


// *********************************************************************************
//	ADD DATA TO TREENODE (DEFAULT HANDLER)
// *********************************************************************************
i2b2.sdx.TypeControllers.QI.AppendTreeNode = function(yuiTree, yuiRootNode, sdxDataPack, callbackLoader) {    
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
 * Get the child records for the given QueryInstance.
 * @param {Object} sdxParentNode	The parent node we want to find the children of.
 * @param {ScopedCallback} onCompleteCallback A scoped-callback function to be executed using the results array.
 * @return {Boolean} Returns true to the calling function.
 * @return {Array} Returns an array of QueryInstance data represented as SDX Objects (without render data) to the callback function passed.
 * @memberOf i2b2.sdx.TypeControllers.QI
 * @method
 * @author Nick Benik
 * @version 1.0
 * @alias i2b2.sdx.Master.getChildRecords
 */
i2b2.sdx.TypeControllers.QI.getChildRecords = function(sdxParentNode, onCompleteCallback, options) {
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

		// find parent node	QI node in data model
		var dm = i2b2.CRC.model.QueryMasters;
		var dm_loc = 'i2b2.CRC.model.QueryMasters';
		
		// Here comes REAL fun, a self-recursive anonymous function instantiated by iteration 
		// via recursive "collection" calls on Hash objects (Prototype toolkit)
		var findFunc = function(item_rec) {
			var hash_key = item_rec.key;
			var gen_rec = item_rec.value;
			var cl_keyValue = keyValue;  // <-- closure variable: keyValue must be set before running function
			if (gen_rec.sdxInfo.sdxType=="QI") {
				// see if this record matches our search
				if (gen_rec.sdxInfo.sdxKeyValue == cl_keyValue) { return gen_rec; }
			} else {
				// recurse into the object's children
				var match_children = gen_rec.children.collect(findFunc);
				return match_children;
			}
		}
		var keyValue = cl_node.sdxInfo.sdxKeyValue.toString();
		var parent_QM = dm.collect(findFunc);
		parent_QM = parent_QM.flatten();
		parent_QM = parent_QM.compact();
		if (parent_QM[0]) {
			var pn = parent_QM[0];
		} else {
			console.error('Parent QM node was not found in the CRC data model!');
			return false;
		}

		// extract records from XML msg
		var ps = results.refXML.getElementsByTagName('query_result_instance');
		var dm = i2b2.CRC.model.QueryMasters;
		for(var i1=0; i1<ps.length; i1++) {
			var o = new Object;
			o.xmlOrig = ps[i1];
			o.QI_id = pn.sdxInfo.sdxKeyValue;
			o.QM_id = pn.parent.sdxInfo.sdxKeyValue;
			o.size = i2b2.h.getXNodeVal(ps[i1],'set_size');
			o.start_date = i2b2.h.getXNodeVal(ps[i1],'start_date');
			o.end_date = i2b2.h.getXNodeVal(ps[i1],'end_date');
			try {
				o.title = i2b2.h.getXNodeVal(ps[i1],'description'); //[0].nodeValue;
			} catch (e) {
				o.title = i2b2.h.getXNodeVal(ps[i1],'name');
			}
			if (i2b2.h.XPath(ps[i1],'query_status_type/name/text()')[0].nodeValue != "COMPLETED")
			{
				o.title += " - " +  i2b2.h.XPath(ps[i1],'query_status_type/name/text()')[0].nodeValue;	
			}

			o.result_type = i2b2.h.XPath(ps[i1],'query_result_type/name/text()')[0].nodeValue;
			var addme = false;
			switch (o.result_type) {
				case "PATIENT_ENCOUNTER_SET":
					o.PRS_id = i2b2.h.getXNodeVal(ps[i1],'result_instance_id');
					// use given title if it exist otherwise generate a title
					/*
					try {
						var t = i2b2.h.XPath(temp,'self::description')[0].firstChild.nodeValue;
					} catch(e) { var t = null; }
					if (!t) { t="Encounter Set"; }
					// create the title using shrine setting
					if (o.size >= 10) {
						if (i2b2.PM.model.userRoles.length == 1 && i2b2.PM.model.userRoles[0] == "DATA_OBFSC") {
							o.title = t+" - "+o.size+"&plusmn;3 encounters";
						} else {
							o.title = t+" - "+o.size+" encounters";
						}
					} else {
						if (i2b2.PM.model.userRoles.length == 1 && i2b2.PM.model.userRoles[0] == "DATA_OBFSC") {
							o.title = t+" - 10 encounters or less";
						} else {
							o.title = t+" - "+o.size+" encounters";
						}
					} */
					o.titleCRC = o.title;
					o.title = pn.parent.sdxInfo.sdxDisplayName + ' [PATIENT_ENCOUNTER_SET_'+o.PRS_id+']';
					o.result_instance_id = o.PRS_id;
					var sdxDataNode = i2b2.sdx.Master.EncapsulateData('ENS',o);
					addme = true;
					break;				
				case "PATIENTSET":
					o.PRS_id = i2b2.h.getXNodeVal(ps[i1],'result_instance_id');
					o.titleCRC = o.title;
					o.title = pn.parent.sdxInfo.sdxDisplayName + ' [PATIENTSET_'+o.PRS_id+']';
					o.result_instance_id = o.PRS_id;
					var sdxDataNode = i2b2.sdx.Master.EncapsulateData('PRS',o);
					addme = true;
					break;
				default:
				
					o.PRC_id = i2b2.h.getXNodeVal(ps[i1],'result_instance_id');
					o.titleCRC = o.title;
					//o.title = pn.parent.sdxInfo.sdxDisplayName + ' [PATIENT_COUNT_XML_'+o.PRC_id+']';
					//o.title = 'PATIENT_COUNT_XML_'+o.PRC_id;
					o.result_instance_id = o.PRC_id;
					var sdxDataNode = i2b2.sdx.Master.EncapsulateData('PRC',o);
					addme = true;
					break;					
			}
			if (addme) {
			// save record in the SDX system
			sdxDataNode = i2b2.sdx.Master.Save(sdxDataNode, pn);
			// append the data node to our returned results
			retChildren.push(sdxDataNode);
			}
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
	i2b2.CRC.ajax.getQueryResultInstanceList_fromQueryInstanceId("CRC:SDX:QueryInstance", {qi_key_value: sdxParentNode.sdxInfo.sdxKeyValue}, scopedCallback);
}

i2b2.sdx.TypeControllers.QI.SaveToDataModel = function(sdxData, sdxParentNode) {
	// save to CRC data model
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


i2b2.sdx.TypeControllers.QI.LoadFromDataModel = function(key_value) {}


i2b2.sdx.TypeControllers.QI.ClearAllFromDataModel= function(sdxOptionalParent) {
	if (sdxOptionalParent) {
		try {
			var findFunc = function(item_rec) {
				// this function expects the second argument to be the target node's key value
				var hash_key = item_rec[0];
				var QM_rec = item_rec[1];
				if (QM_rec.sdxInfo.sdxKeyValue == this.strip()) { return true; }
			}
			var dm_loc = 'i2b2.CRC.model.QueryMasters.'+i2b2.sdx.Master._KeyHash(sdxOptionalParent.sdxInfo.sdxKeyValue);
			var targets = i2b2.CRC.model.QueryMasters.findAll(findFunc, sdxOptionalParent.sdxInfo.sdxKeyValue);
			for (var i=0; i < targets.length; i++) {
				var t = parent_QM[i].value;
				t.children = new Hash();
			}
		} catch(e) { console.error('Could not clear children of given parent node!'); }
	} else {
		var dm_loc = 'i2b2.CRC.model.QueryMasters';
		i2b2.CRC.model.QueryMasters.each(function(item_rec) {
			try {
				item_rec[1].children = new Hash();
			} catch(e) { console.error('Could not clear children of all QueryMasters'); }
		});
	}
	// TODO: send data update signal (use JOINING-MUTEX or AGGREGATING-MUTEX to avoid rapid fire of event!)
	// updated dm_loc of the data model
	return true;
}


i2b2.sdx.TypeControllers.QI.LoadChildrenFromTreeview = function(node, onCompleteCallback) {
	var scopedCallback = new i2b2_scopedCallback();
	scopedCallback.scope = node.data.i2b2_SDX;
	scopedCallback.callback = function(retCellPack) {
		var cl_node = node;
		var cl_onCompleteCB = onCompleteCallback;

		var results = retCellPack.results;			
		for(var i1=0; i1<1*results.length; i1++) {
			var o = results[i1];
			
			
			// add visual element
			switch (o.sdxInfo.sdxType) {
				case "PRS":	// patient record set
					// add visual element
					 if (i2b2.PM.model.userRoles.indexOf("DATA_LDS") == -1) {
						var renderOptions = {
							dragdrop: "i2b2.sdx.TypeControllers.PRS.AttachDrag2Data",
							icon: "sdx_CRC_PRS.jpg",
							title: o.origData.titleCRC, 
							showchildren: false
						};
					} else
					{
						var renderOptions = {
							dragdrop: "i2b2.sdx.TypeControllers.PRS.AttachDrag2Data",
							icon: "sdx_CRC_PRS.jpg",
							title: o.origData.titleCRC, 
							showchildren: true
						};					
					}
					break;
				case "ENS":	// encounter record set
					// add visual element
					 if (i2b2.PM.model.userRoles.indexOf("DATA_LDS") == -1) {
						var renderOptions = {
							dragdrop: "i2b2.sdx.TypeControllers.ENS.AttachDrag2Data",
							icon: "sdx_CRC_PRS.jpg",
							title: o.origData.titleCRC, 
							showchildren: false
						};
					} else {
						var renderOptions = {
							dragdrop: "i2b2.sdx.TypeControllers.ENS.AttachDrag2Data",
							icon: "sdx_CRC_PRS.jpg",
							title: o.origData.titleCRC, 
							showchildren: true						
						};
						}
					break;					
				case "PRC":	// patient record count
					var renderOptions = {
						dragdrop: "i2b2.sdx.TypeControllers.PRC.AttachDrag2Data",
						icon: "sdx_CRC_PRC.jpg",
						title: o.origData.titleCRC, 
						showchildren: false
					};
					break;
			}

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
	var options = i2b2.CRC.params;
	i2b2.sdx.Master.getChildRecords(sdxParentNode, scopedCallback, options);
}



// *********************************************************************************
//	ATTACH DRAG TO DATA (DEFAULT HANDLER)
// *********************************************************************************
i2b2.sdx.TypeControllers.QI.AttachDrag2Data = function(divParentID, divDataID){
	if (Object.isUndefined($(divDataID))) {	return false; }
	
	// get the i2b2 data from the yuiTree node
	var tvTree = YAHOO.widget.TreeView.getTree(divParentID);
	var tvNode = tvTree.getNodeByProperty('nodeid', divDataID);
	if (!Object.isUndefined(tvNode.DDProxy)) { return true; }
	
	// attach DD
	var t = new i2b2.sdx.TypeControllers.QI.DragDrop(divDataID);
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
i2b2.sdx.TypeControllers.QI.DragDrop = function(id, config) {
	if (id) {
		this.init(id, 'QI',{isTarget:false});
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
	this.addToGroup("PRS");
};
YAHOO.extend(i2b2.sdx.TypeControllers.QI.DragDrop, YAHOO.util.DDProxy);
i2b2.sdx.TypeControllers.QI.DragDrop.prototype.startDrag = function(x, y) {
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
i2b2.sdx.TypeControllers.QI.DragDrop.prototype.endDrag = function(e) {
	// remove DragDrop targeting CCS
	var targets = YAHOO.util.DDM.getRelated(this, true); 
	for (var i=0; i<targets.length; i++) {      
		var targetEl = targets[i]._domRef;
		try {
			var ddCtrlr = YAHOO.util.DragDropMgr.getDDById(targetEl.id);
			if(ddCtrlr.groups['QI']) { i2b2.sdx.Master.onHoverOut('QI', e, targetEl, this); }
		} catch(e) {}
	} 
};
i2b2.sdx.TypeControllers.QI.DragDrop.prototype.alignElWithMouse = function(el, iPageX, iPageY) {
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
i2b2.sdx.TypeControllers.QI.DragDrop.prototype.onDragOver = function(e, id) {
	// check to see if on-the-fly object type translation is needed
	var translateTo = false;
	if (Object.isUndefined(this.DDM.dragOvers[id])) { return false; }
	// we must save which target we are over for the DragOut event later
	this.lastDragOver = this.DDM.dragOvers[id]; 
	var t = this.DDM.dragOvers[id].groups;
	if (!t['QI']) {
		// TRANSLATION NEEDED!
		if (t['PRS']) {
			translateTo = "PRS";
		}
	} else {
		var translateTo = "QI";
	}	
	// fire the onHoverOver (use SDX so targets can override default event handler)
	if (translateTo) { i2b2.sdx.Master.onHoverOver(translateTo, e, id, this); }
};
i2b2.sdx.TypeControllers.QI.DragDrop.prototype.onDragOut = function(e, id) {
	// fire the onHoverOut handler (use SDX so targets can override default event handlers)
	// check to see if on-the-fly object type translation is needed
	var translateTo = false;
	if (Object.isUndefined(this.lastDragOver)) { return false; }
	var t = this.lastDragOver.groups;
	if (!t['QI']) {
		// TRANSLATION NEEDED!
		if (t['PRS']) {
			translateTo = "PRS";
		}
	} else {
		var translateTo = "QI";
	}	
	// fire the onHoverOver (use SDX so targets can override default event handler)
	if (translateTo) { i2b2.sdx.Master.onHoverOut(translateTo, e, id, this); }
};
i2b2.sdx.TypeControllers.QI.DragDrop.prototype.onDragDrop = function(e, id) {
	// check to see if on-the-fly object type translation is needed
	var translateTo = false;
	if (Object.isUndefined(this.lastDragOver)) { return false; }
	var t = this.lastDragOver.groups;
	if (!t['QI']) {
		// TRANSLATION NEEDED!
		if (t['PRS']) {
			translateTo = "PRS";
		}
	} else {
		var translateTo = "QI";
	}	
	// fire the onHoverOver (use SDX so targets can override default event handler)
	if (translateTo) { 
		i2b2.sdx.Master.onHoverOut(translateTo, e, id, this);
		// retreive the concept data from the dragged element
		// PERFROM on-the-fly OBJECT TRANSLATION HERE!!
		var draggedData;
		switch (translateTo) {
			case "QI":
				// no translation needed
				var draggedData = this.yuiTreeNode.data.i2b2_SDX;
				// send no so translated info to the drop target handler
				i2b2.sdx.Master.ProcessDrop(draggedData, id);	
				break;
			case "PRS":
				var draggedData = this.yuiTreeNode.data.i2b2_SDX;
				if (draggedData.children.size() == 0) {
					// a little explaination about the below code: loading/expanding the
					// treeview recursively via asynchronous calls
					i2b2.sdx.Master.LoadChildrenFromTreeview(this.yuiTreeNode, (function(){
						var cl_tn1 = this.yuiTreeNode;
						var cl_id1 = id;
						cl_tn1.dynamicLoadComplete = true;
						cl_tn1.expand();
						for (var i1=0; i1<cl_tn1.children.length; i1++) {
							var draggedData = cl_tn1.children[i1].data.i2b2_SDX;
							// send translated info to the drop target handler
							i2b2.sdx.Master.ProcessDrop(draggedData, cl_id2);
						}
					}));
				} else {
					// dump PRS hash as an array of SDX objects
					i2b2.sdx.Master.ProcessDrop(draggedData.children.values(), id);
				}
				break;
		}
	}
};



// *********************************************************************************
//	<BLANK> DROP HANDLER 
//	!!!! DO NOT EDIT - ATTACH YOUR OWN CUSTOM ROUTINE USING
//	!!!! THE i2b2.sdx.Master.setHandlerCustom FUNCTION
// *********************************************************************************
i2b2.sdx.TypeControllers.QI.DropHandler = function(sdxData) {
	alert('[QueryInstance DROPPED] You need to create your own custom drop event handler.');
}


console.timeEnd('execute time');
console.groupEnd();
