/**
 * @projectDescription	The SDX controller library for the PatientRecordSet data-type.
 * @namespace	i2b2.sdx.TypeControllers.ENS
 * @inherits 	i2b2.sdx.TypeControllers
 * @author		Mike Mendis
 * @version 	1.6
 * @see 		i2b2.sdx
 * ----------------------------------------------------------------------------------------
 */
console.group('Load & Execute component file: CRC > SDX > Encounter Record Set');
console.time('execute time');


i2b2.sdx.TypeControllers.ENS = {};
i2b2.sdx.TypeControllers.ENS.model = {};
// *********************************************************************************
//	ENCAPSULATE DATA
// *********************************************************************************
i2b2.sdx.TypeControllers.ENS.getEncapsulateInfo = function() {
	// this function returns the encapsulation head information
	return {sdxType: 'ENS', sdxKeyName: 'result_instance_id', sdxControlCell:'CRC', sdxDisplayNameKey: 'title'};
}

i2b2.sdx.TypeControllers.ENS.SaveToDataModel = function(sdxData, sdxParentNode) {
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


i2b2.sdx.TypeControllers.ENS.LoadFromDataModel = function(key_value) {}


i2b2.sdx.TypeControllers.ENS.ClearAllFromDataModel= function(sdxOptionalParent) {
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


// *********************************************************************************
//	GENERATE HTML (DEFAULT HANDLER)
// *********************************************************************************
i2b2.sdx.TypeControllers.ENS.RenderHTML= function(sdxData, options, targetDiv) {    
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

	if (Object.isUndefined(options.cssClass)) { options.cssClass = 'sdxDefaultPRS';}

	// user can override
	bCanExp = true;
	if (Object.isBoolean(options.showchildren)) { 
		bCanExp = options.showchildren;
	}
	render.canExpand = bCanExp;
	render.iconType = "PRS";
	
	if (!Object.isUndefined(options.icon)) { render.icon = i2b2.hive.cfg.urlFramework + 'cells/CRC/assets/'+ options.icon }
	if (!Object.isUndefined(options.iconExp)) { render.iconExp = i2b2.hive.cfg.urlFramework + 'cells/CRC/assets/'+ options.iconExp }
	// in cases of one set icon, copy valid icon to the missing icon
	if (Object.isUndefined(render.icon) && !Object.isUndefined(render.iconExp)) {	render.icon = render.iconExp; }
	if (!Object.isUndefined(render.icon) && Object.isUndefined(render.iconExp)) {	render.iconExp = render.icon; }

	// handle the event controllers
	var sMainEvents = sDD;
	var sImgEvents = sDD;

	// **** Render the HTML ***
	var retHtml = '<DIV id="' + id + '" ' + sMainEvents + ' style="white-space:nowrap;cursor:pointer;">';
	retHtml += '<DIV ';
	if (Object.isString(options.cssClass)) {
		retHtml += ' class="'+options.cssClass+'" ';
	} else {
		retHtml += ' class= "sdxDefaultPRS" ';
	}
	retHtml += sImgEvents;
	retHtml += '>';
	retHtml += '<IMG src="'+render.icon+'"/> '; 
	if (!Object.isUndefined(options.title)) {
		retHtml += options.title;
	} else {
		console.warn('[SDX RenderHTML] no title was given in the creation options for an CRC > ENS node!');
		retHtml += ' PRS '+id;
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
i2b2.sdx.TypeControllers.ENS.onHoverOver = function(e, id, ddProxy) {    
	var el = $(id);	
	if (el) { Element.addClassName(el,'ddPRSTarget'); }
}

// *********************************************************************************
//	HANDLE HOVER OVER TARGET EXIT (DEFAULT HANDLER)
// *********************************************************************************
i2b2.sdx.TypeControllers.ENS.onHoverOut = function(e, id, ddProxy) { 
	var el = $(id);	
	if (el) { Element.removeClassName(el,'ddPRSTarget'); }
}


// *********************************************************************************
//	ADD DATA TO TREENODE (DEFAULT HANDLER)
// *********************************************************************************
i2b2.sdx.TypeControllers.ENS.AppendTreeNode = function(yuiTree, yuiRootNode, sdxDataPack, callbackLoader) {    
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


// *********************************************************************************
//	ATTACH DRAG TO DATA (DEFAULT HANDLER)
// *********************************************************************************
i2b2.sdx.TypeControllers.ENS.AttachDrag2Data = function(divParentID, divDataID){
	if (Object.isUndefined($(divDataID))) {	return false; }
	
	// get the i2b2 data from the yuiTree node
	var tvTree = YAHOO.widget.TreeView.getTree(divParentID);
	var tvNode = tvTree.getNodeByProperty('nodeid', divDataID);
	if (!Object.isUndefined(tvNode.DDProxy)) { return true; }
	
	// attach DD
	var t = new i2b2.sdx.TypeControllers.ENS.DragDrop(divDataID)
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

/** 
 * Get the child records for the given PatientRecordSet.
 * @param {Object} sdxParentNode	The parent node we want to find the children of.
 * @param {ScopedCallback} onCompleteCallback A scoped-callback function to be executed using the results array.
 * @return {Boolean} Returns true to the calling function.
 * @return {Array} Returns an array of PatientRecord data represented as SDX Objects (without render data) to the callback function passed.
 * @memberOf i2b2.sdx.TypeControllers.QI
 * @method
 * @author Nick Benik
 * @version 1.0
 * @alias i2b2.sdx.Master.getChildRecords
 */
i2b2.sdx.TypeControllers.ENS.getChildRecords = function(sdxParentNode, onCompleteCallback, options) {
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

		// extract records from XML msg
		var ps = results.refXML.getElementsByTagName('event');
		var dm = i2b2.CRC.model.QueryMasters;
		for(var i1=0; i1<ps.length; i1++) {
			var o = new Object;
			o.xmlOrig = ps[i1];
			o.patient_id = i2b2.h.getXNodeVal(ps[i1],'patient_id');
			o.event_id = i2b2.h.getXNodeVal(ps[i1],'event_id');
			//o.vital_status = i2b2.h.XPath(ps[i1],'param[@name="vital_status_cd"]/text()')[0].nodeValue;
			//o.age = i2b2.h.XPath(ps[i1],'param[@name="age_in_years_num"]/text()')[0].nodeValue;
			//o.sex = i2b2.h.XPath(ps[i1],'param[@name="sex_cd"]/text()')[0].nodeValue;
			//o.race = i2b2.h.XPath(ps[i1],'param[@name="race_cd"]/text()')[0].nodeValue;
			//o.title = o.patient_id+" ["+o.age+" y/o "+ o.sex+" "+o.race+"]";
			var sdxDataNode = i2b2.sdx.Master.EncapsulateData('PR',o);
			// save record in the SDX system
			sdxDataNode = i2b2.sdx.Master.Save(sdxDataNode, cl_node);
			// append the data node to our returned results
			retChildren.push(sdxDataNode);
		}
		cl_node.children.loaded = true;
		// TODO: broadcast a data update event of the CRC data model
		retMsg.results = retChildren;
		if (getObjectClass(cl_onCompleteCB)=='i2b2_scopedCallback') {
			cl_onCompleteCB.callback.call(cl_onCompleteCB.scope, retMsg);
		} else {
			cl_onCompleteCB(retMsg);
		}
	}
	
	
	var msg_filter = '<input_list>\n' +
	'	<event_list max="1000000" min="0">\n'+
	'		<patient_event_coll_id>'+sdxParentNode.sdxInfo.sdxKeyValue+'</patient_event_coll_id>\n'+
	'	</event_list>\n'+
	'</input_list>\n'+
	'<filter_list/>\n'+
	'<output_option>\n'+
	'	<event_set select="using_input_list" onlykeys="true"/>\n'+
	'</output_option>\n';

	// AJAX CALL
	var options  = {
		patient_limit: 0,
		PDO_Request: msg_filter
	};
	i2b2.CRC.ajax.getPDO_fromInputList("CRC:SDX:PatientRecordSet", options, scopedCallback);
}



i2b2.sdx.TypeControllers.ENS.LoadChildrenFromTreeview = function(node, onCompleteCallback) {
	var scopedCallback = new i2b2_scopedCallback();
	scopedCallback.scope = node.data.i2b2_SDX;
	scopedCallback.callback = function(retCellPack) {
		var cl_node = node;
		var cl_onCompleteCB = onCompleteCallback;

		var results = retCellPack.results;			
		for(var i1=0; i1<1*results.length; i1++) {
			var o = results[i1];
			var renderOptions = {
				dragdrop: "i2b2.sdx.TypeControllers.PRC.AttachDrag2Data",
				icon: "sdx_CRC_PR.jpg",
				title: o.sdxInfo.sdxDisplayName, 
				showchildren: false
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
	var options = i2b2.CRC.params;
	i2b2.sdx.Master.getChildRecords(sdxParentNode, scopedCallback, options);
}





// *********************************************************************************
//	DRAG DROP PROXY CONTROLLER
// *********************************************************************************
i2b2.sdx.TypeControllers.ENS.DragDrop = function(id, config) {
	if (id) {
		this.init(id, 'ENS',{isTarget:false});
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
YAHOO.extend(i2b2.sdx.TypeControllers.ENS.DragDrop, YAHOO.util.DDProxy);
i2b2.sdx.TypeControllers.ENS.DragDrop.prototype.startDrag = function(x, y) {
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
i2b2.sdx.TypeControllers.ENS.DragDrop.prototype.endDrag = function(e) {
	// remove DragDrop targeting CCS
	var targets = YAHOO.util.DDM.getRelated(this, true); 
	for (var i=0; i<targets.length; i++) {      
		var targetEl = targets[i]._domRef; 
		i2b2.sdx.Master.onHoverOut('ENS', e, targetEl, this);
	} 
};
i2b2.sdx.TypeControllers.ENS.DragDrop.prototype.alignElWithMouse = function(el, iPageX, iPageY) {
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
//		var scrSize = document.viewport.getDimensions();
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
i2b2.sdx.TypeControllers.ENS.DragDrop.prototype.onDragOver = function(e, id) {
	// fire the onHoverOver (use SDX so targets can override default event handler)
	i2b2.sdx.Master.onHoverOver('ENS', e, id, this);
};
i2b2.sdx.TypeControllers.ENS.DragDrop.prototype.onDragOut = function(e, id) {
	// fire the onHoverOut handler (use SDX so targets can override default event handlers)
	i2b2.sdx.Master.onHoverOut('ENS', e, id, this);
};
i2b2.sdx.TypeControllers.ENS.DragDrop.prototype.onDragDrop = function(e, id) {
	i2b2.sdx.Master.onHoverOut('ENS', e, id, this);
	// retreive the concept data from the dragged element
	draggedData = this.yuiTreeNode.data.i2b2_SDX;
	i2b2.sdx.Master.ProcessDrop(draggedData, id);
};
	


// *********************************************************************************
//	<BLANK> DROP HANDLER 
//	!!!! DO NOT EDIT - ATTACH YOUR OWN CUSTOM ROUTINE USING
//	!!!! THE i2b2.sdx.Master.setHandlerCustom FUNCTION
// *********************************************************************************
i2b2.sdx.TypeControllers.ENS.DropHandler = function(sdxData) {
	alert('[PatientRecordSet DROPPED] You need to create your own custom drop event handler.');
}


console.timeEnd('execute time');
console.groupEnd();