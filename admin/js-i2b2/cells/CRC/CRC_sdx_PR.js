/**
 * @projectDescription	The SDX controller library for the PatientRecord data-type.
 * @namespace	i2b2.sdx.TypeControllers.PR
 * @inherits 	i2b2.sdx.TypeControllers
 * @author		Nick Benik, Griffin Weber MD PhD
 * @version 	1.3
 * @see 		i2b2.sdx
 * ----------------------------------------------------------------------------------------
 * updated 9-15-08: RC4 launch [Nick Benik]
 */
console.group('Load & Execute component file: CRC > SDX > Patient Record');
console.time('execute time');


i2b2.sdx.TypeControllers.PR = {};
i2b2.sdx.TypeControllers.PR.model = {};
// *********************************************************************************
//	ENCAPSULATE DATA
// *********************************************************************************
i2b2.sdx.TypeControllers.PR.getEncapsulateInfo = function() {
	// this function returns the encapsulation head information
	return {sdxType: 'PR', sdxKeyName: 'patient_id', sdxControlCell:'CRC', sdxDisplayNameKey: 'title'};
}

i2b2.sdx.TypeControllers.PR.SaveToDataModel = function(sdxData, sdxParentNode) {
	// do not save save to CRC data model because of sheer 
	if (!sdxParentNode) { return false; }
	var pr_id = sdxData.sdxInfo.sdxKeyValue;
	var pr_hash = i2b2.sdx.Master._KeyHash(pr_id);

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
	sdxParentNode.children.set(pr_hash, t);
	// TODO: send data update signal (use JOINING-MUTEX or AGGREGATING-MUTEX to avoid rapid fire of event!)
	return t;
}


i2b2.sdx.TypeControllers.PR.LoadFromDataModel = function(key_value) {}


i2b2.sdx.TypeControllers.PR.ClearAllFromDataModel= function(sdxOptionalParent) {
	// never called
	return true;
}


// *********************************************************************************
//	GENERATE HTML (DEFAULT HANDLER)
// *********************************************************************************
i2b2.sdx.TypeControllers.PR.RenderHTML= function(sdxData, options, targetDiv) {
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
	var id = "CRC_ID-" + i2b2.GUID();
	
	// process drag drop controllers
	if (!Object.isUndefined(options.dragdrop)) {
// NOTE TO SELF: should attachment of node dragdrop controller be handled by the SDX system as well? 
// This would ensure removal of the onmouseover call in a cross-browser way
		var sDD = '  onmouseover="' + options.dragdrop + '(\''+ targetDiv.id +'\',\'' + id + '\')" ';
	} else {
		var sDD = '';
	}

	if (Object.isUndefined(options.cssClass)) { options.cssClass = 'sdxDefaultPR';}

	// user can override
	bCanExp = true;
	if (Object.isBoolean(options.showchildren)) { 
		bCanExp = options.showchildren;
	}
	render.canExpand = bCanExp;
	render.iconType = "PR";
	
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
		retHtml += ' class= "sdxDefaultPR" ';
	}
	retHtml += sImgEvents;
	retHtml += '>';
	retHtml += '<IMG src="'+render.icon+'"/> '; 
	if (!Object.isUndefined(options.title)) {
		retHtml += options.title;
	} else {
		console.warn('[SDX RenderHTML] no title was given in the creation options for an CRC > PR node!');
		retHtml += ' PR '+id;
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
i2b2.sdx.TypeControllers.PR.onHoverOver = function(e, id, ddProxy) {    
	var el = $(id);	
	if (el) { Element.addClassName(el,'ddPRTarget'); }
}

// *********************************************************************************
//	HANDLE HOVER OVER TARGET EXIT (DEFAULT HANDLER)
// *********************************************************************************
i2b2.sdx.TypeControllers.PR.onHoverOut = function(e, id, ddProxy) { 
	var el = $(id);	
	if (el) { Element.removeClassName(el,'ddPRTarget'); }
}


// *********************************************************************************
//	ADD DATA TO TREENODE (DEFAULT HANDLER)
// *********************************************************************************
i2b2.sdx.TypeControllers.PR.AppendTreeNode = function(yuiTree, yuiRootNode, sdxDataPack, callbackLoader) {    
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
i2b2.sdx.TypeControllers.PR.AttachDrag2Data = function(divParentID, divDataID){
	if (Object.isUndefined($(divDataID))) {	return false; }
	
	// get the i2b2 data from the yuiTree node
	var tvTree = YAHOO.widget.TreeView.getTree(divParentID);
	var tvNode = tvTree.getNodeByProperty('nodeid', divDataID);
	if (!Object.isUndefined(tvNode.DDProxy)) { return true; }
	
	// attach DD
	var t = new i2b2.sdx.TypeControllers.PR.DragDrop(divDataID)
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
i2b2.sdx.TypeControllers.PR.DragDrop = function(id, config) {
	if (id) {
		this.init(id, 'PR',{isTarget:false});
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
YAHOO.extend(i2b2.sdx.TypeControllers.PR.DragDrop, YAHOO.util.DDProxy);
i2b2.sdx.TypeControllers.PR.DragDrop.prototype.startDrag = function(x, y) {
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
i2b2.sdx.TypeControllers.PR.DragDrop.prototype.endDrag = function(e) {
	// remove DragDrop targeting CCS
	var targets = YAHOO.util.DDM.getRelated(this, true); 
	for (var i=0; i<targets.length; i++) {      
		var targetEl = targets[i]._domRef; 
		i2b2.sdx.Master.onHoverOut('PR', e, targetEl, this);
	} 
};
i2b2.sdx.TypeControllers.PR.DragDrop.prototype.alignElWithMouse = function(el, iPageX, iPageY) {
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
i2b2.sdx.TypeControllers.PR.DragDrop.prototype.onDragOver = function(e, id) {
	// fire the onHoverOver (use SDX so targets can override default event handler)
	i2b2.sdx.Master.onHoverOver('PR', e, id, this);
};
i2b2.sdx.TypeControllers.PR.DragDrop.prototype.onDragOut = function(e, id) {
	// fire the onHoverOut handler (use SDX so targets can override default event handlers)
	i2b2.sdx.Master.onHoverOut('PR', e, id, this);
};
i2b2.sdx.TypeControllers.PR.DragDrop.prototype.onDragDrop = function(e, id) {
	i2b2.sdx.Master.onHoverOut('PR', e, id, this);
	// retreive the concept data from the dragged element
	draggedData = this.yuiTreeNode.data.i2b2_SDX;
	i2b2.sdx.Master.ProcessDrop(draggedData, id);
};
	


// *********************************************************************************
//	<BLANK> DROP HANDLER 
//	!!!! DO NOT EDIT - ATTACH YOUR OWN CUSTOM ROUTINE USING
//	!!!! THE i2b2.sdx.Master.setHandlerCustom FUNCTION
// *********************************************************************************
i2b2.sdx.TypeControllers.PR.DropHandler = function(sdxData) {
	alert('[PatientRecord DROPPED] You need to create your own custom drop event handler.');
}


console.timeEnd('execute time');
console.groupEnd();