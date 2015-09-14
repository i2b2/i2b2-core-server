/**
 * @projectDescription	Standard Data Exchange (SDX) subsystem's core message router.
 * @inherits 	i2b2
 * @namespace	i2b2
 * @author		Nick Benik, Griffin Weber MD PhD
 * @version 	1.3
 * ----------------------------------------------------------------------------------------
 * updated 9-15-08: RC4 launch [Nick Benik] 
 */
console.group('Load & Execute component file: hive > SDX');
console.time('execute time');


// ================================================================================================== //
i2b2.sdx.Master.EncapsulateData = function(inType, inData) {
	if (!Object.isObject(inData)) { 
		console.error("Data to encapsulate into SDX package is not an object");
		return false;
	}
	
	try {
		var headInfo = i2b2.sdx.TypeControllers[inType].getEncapsulateInfo();
	} catch(e) {
		console.error('SDX Controller for Data Type: '+inType+' does not allow Encapsulation!');
		return false;
	}
	
	// class for all SDX communications
	function i2b2_SDX_Encapsulation() {}
	// create an instance and populate with info
	var sdxEncap = new i2b2_SDX_Encapsulation();
	sdxEncap.sdxInfo = headInfo;
	if (undefined==inData[headInfo.sdxKeyName]) {
		console.error('Key information was not found during an attempt to encapsulate '+inType+' data');
		console.group('(more info)');
		console.info('SDX Encapsulation header');
		console.dir(headInfo);
		console.info('Data sent to be Encapsulated');
		console.dir(inData);
		console.groupEnd();
		return false;
	}
	sdxEncap.sdxInfo.sdxKeyValue = inData[headInfo.sdxKeyName];
	if (headInfo.sdxDisplayNameKey) {
		var t = inData[headInfo.sdxDisplayNameKey];
		if (t) {
			sdxEncap.sdxInfo.sdxDisplayName = t;
			delete sdxEncap.sdxInfo.sdxDisplayNameKey;
		}
	}
	sdxEncap.origData = inData;
	return sdxEncap;
}


// ================================================================================================== //
i2b2.sdx.Master._KeyHash = function(key) {
	// create hash from key
	var kh = escape(key);
	kh = kh.replace('%','_');
	return "H$__"+kh;
}


// ================================================================================================== //
i2b2.sdx.Master.ClearAll = function(type, sdxParentOptional) {
	try {
		var sdxClearFunc = i2b2.sdx.TypeControllers[type].ClearAllFromDataModel; 
	} catch(e) {
		console.error(type+' SDX Controller not loaded or is missing the SaveToDataModel function');
		return false;
	}
	// save data
	try {
		var success = sdxClearFunc(sdxParentOptional);
		if (success) { 
			return true;
		} else {
			throw("ClearAll Error in SDX type controller");
		}
	} catch(e) {
		console.error("An error occurred while trying to clear all data of "+type+" object in the data model");
	}
}

// ================================================================================================== //
i2b2.sdx.Master.Save = function(sdxData, sdxParentNode) {
	// extract the sdx type
	try {
		var type = sdxData.sdxInfo.sdxType;
		var sdxSaveFunc = i2b2.sdx.TypeControllers[type].SaveToDataModel; 
	} catch(e) {
		console.error(type+' SDX Controller not loaded or is missing the SaveToDataModel function');
		return false;
	}
	// save data
	try {
		var success = sdxSaveFunc(sdxData, sdxParentNode);
		if (success) { 
			return success;
		} else {
			throw("Save Error in SDX type controller");
		}
	} catch(e) {
		console.error("An error occurred while trying to save a "+type+" object into the data model");
	}
}

// ================================================================================================== //
i2b2.sdx.Master.Load = function(type, key) {
	// extract the sdx type
	try {
		var sdxLoadFunc = i2b2.sdx.TypeControllers[type].LoadFromDataModel; 
	} catch(e) {
		console.error(type+' SDX Controller not loaded or is missing the LoadFromDataModel function');
		return false;
	}
	// load data
	try {
		var success = sdxLoadFunc(key);
		if (success) { 
			return success;
		} else {
			throw("Load Error in SDX type controller");
		}
	} catch(e) {
		console.error("An error occurred while trying to Load a "+type+" object from the data model");
	}
}

// ================================================================================================== //
i2b2.sdx.Master.Click = function(type,id, domNode) {
	// route the click to the proper type controller
	if (undefined===i2b2.sdx.TypeControllers[type]) { return; }
	if (undefined!==i2b2.sdx.TypeControllers[type].Click) { i2b2.sdx.TypeControllers[type].Click(id, domNode); }
}

// ================================================================================================== //
i2b2.sdx.Master.AppendChild = function(tvNode, type, key, data) {
	// make sure datatype controllers are configured
	if (undefined===i2b2.sdx.TypeControllers[type]) { 
		alert('SDX Controller not loaded for Data Type: '+type);
		return false;
	}
	if (undefined!==data) {
		// load data
		var vdata = this.Load.call(this, type, key);
		if (undefined===vdata) {
			console.warn('SDX_AppendChild: Data not provided / not cached');
			alert('SDX_AppendChild: Data not provided / not cached');
			return false;
		}
	} else {
		// save the data
		var vdata = this.Save.call(this, type, key, data);
	}
	// add the node using the SDX treeview controller
	
}

// ================================================================================================== //
i2b2.sdx.Master.Attach2Data = function(domNode, type, key) {
	// ID = DOMNode ID or reference to DOMNode
	// type = registered Data type (Ex. "QM")
	// key = key for data to be associated with this node
	if (undefined===i2b2.sdx.TypeControllers[type]) {
		alert('Cannot attach DragDrop, "'+type+'" Type Controller is not registered');
	} else {
		if (undefined===i2b2.sdx.TypeControllers[type].DragDrop) {
			alert('Cannot attach DragDrop, "'+type+'" DDProxy Controller is not registered');
		} else {
			// OK to setup
			var t = new i2b2.sdx.TypeControllers[type].DragDrop(domNode, key, {isTarget: false});
			t.isTarget = false;
		}
	}	
	// disable attachement event
	try {
		if (undefined!=domNode.setAttribute) {
			domNode.setAttribute('onmouseover', null);
		}
	} catch(e) {}
	return t;
}

// ================================================================================================== //

// ================================================================================================== //
i2b2.sdx.Master.ProcessDrop = function(sdxData, DroppedOnID){
	console.group("SDX Process DropEvent on container ID:"+DroppedOnID);
	console.dir(sdxData);
//TODO: clean up these array processing hacks
	if (sdxData[0]) {
		var typeCode = sdxData[0].sdxInfo.sdxType;
	} else {
		var typeCode = sdxData.sdxInfo.sdxType;
	}
	// do we have the container registered?
	try {
		var t = this._sysData[DroppedOnID][typeCode].DropHandler;
	} catch(e) {
		console.error("SDX DropHandler does not exist for drop target!");
		console.groupEnd();
		return false;
	}
	console.groupEnd();
	// TODO: perform any needed type translation
	var sdxObjs = [];
//TODO: clean up these array processing hacks
	if (sdxData[0]) {
		sdxData.each(function(sdxRec){
			if (sdxRec.sdxInfo.sdxType == typeCode) { sdxObjs.push(sdxRec); }
		});
	} else {
		sdxObjs.push(sdxData);
	}
	this._sysData[DroppedOnID][typeCode].DropHandler(sdxObjs, DroppedOnID);
}

// ================================================================================================== //
i2b2.sdx.Master.AttachType = function(containerID, typeCode, options) {
	console.group("ATTACHING SDX "+typeCode+" Type handlers to container ID:"+containerID);
	if (undefined == i2b2.sdx.TypeControllers[typeCode]) {
		console.error("SDX TypeController does not exist for data type: " + typeCode);
		console.groupEnd();
		return false;
	}
	// do we have the container registered?
	if (undefined == this._sysData[containerID]) {
		console.debug("New container ID registered");
		this._sysData[containerID] = {}; 
	}
	// is the type code already registered to this container?
	if (undefined == this._sysData[containerID][typeCode]) {		
		this._sysData[containerID][typeCode] = {}; 
	} else {
		console.warn("The container identified by ID:"+containerID+" is already registered as accepting '"+typeCode+"' data.");
		console.warn("Existing SDX handlers being replaced with default '"+typeCode+"' SDX handlers.");
	}
	// ADD ALL MANDITORY HANDLERS
	i2b2.sdx.Master.setHandlerDefault(containerID, typeCode, "RenderHTML");
	i2b2.sdx.Master.setHandlerDefault(containerID, typeCode, "AppendTreeNode");
	i2b2.sdx.Master.setHandlerDefault(containerID, typeCode, "LoadChildrenFromTreeview");
	i2b2.sdx.Master.setHandlerDefault(containerID, typeCode, "onHoverOver");
	i2b2.sdx.Master.setHandlerDefault(containerID, typeCode, "onHoverOut");

	// add additional handlers like DragDrop
	if (!Object.isUndefined(options)) {
		if (options.dropTarget) {
			i2b2.sdx.Master.setHandlerDefault(containerID, typeCode, "DropHandler");
			if (Object.isUndefined(this._sysData[containerID]._yuiDragDrop)) {
				this._sysData[containerID]._yuiDragDrop = new YAHOO.util.DDTarget(containerID, typeCode);
			} else {
				this._sysData[containerID]._yuiDragDrop.addToGroup(typeCode);
			}
		}
	}
	console.groupEnd();
}
	
// ================================================================================================== //
i2b2.sdx.Master.setHandlerCustom = function(containerID, typeCode, handlerName, newHandlerFunction) {
	// containerID: string
	// typeCode: string
	// handlerName: string (example: Render, AddChild, ddStart, ddMove)
	// newHandlerFunction: function to be used
	if (!i2b2.sdx.TypeControllers[typeCode]) {
		console.error("SDX TypeController does not exist for data type: " + typeCode);
		return false;
	}
	// do we have the container registered?
	if (Object.isUndefined(this._sysData[containerID])) {
		console.error("SDX does not have any references to a containerID: " + containerID);
		return false;
	}
	this._sysData[containerID][typeCode][handlerName] = newHandlerFunction;
	console.info("ATTACHED custom SDX handler '"+handlerName+"' handler for "+typeCode);
	return true;

}

// ================================================================================================== //
i2b2.sdx.Master.setHandlerDefault = function(containerID, typeCode, handlerName) {
	// containerID: string
	// typeCode: string
	// handlerName: string (example: Render, AddChild, ddStart, ddMove)
	// newHandlerFunction: function to be used
	if (undefined == i2b2.sdx.TypeControllers[typeCode]) {
		console.error("SDX TypeController does not exist for data type: " + typeCode);
		return false;
	}
	// do we have the container registered?
	if (Object.isUndefined(this._sysData[containerID])) {
		console.error("SDX does not have any references to a containerID: " + containerID);
		return false;
	}
	if (Object.isUndefined(i2b2.sdx.TypeControllers[typeCode][handlerName])) {
		console.warn("No default SDX '"+handlerName+"' handler exists for "+typeCode);
	} else {
		this._sysData[containerID][typeCode][handlerName] = i2b2.sdx.TypeControllers[typeCode][handlerName];
		console.info("ATTACHED default SDX '"+handlerName+"' handler for "+typeCode);
	}
	return true;
}


// *****************************************************************************
//  THE BELOW FUNCTIONS ARE IMPLEMENTION OF A ROUTER PATTERN 
//  WHICH ROUTES REQUESTS DEPENDANT ON: DATA TYPE, 
//  TARGET CONTAINER AND REGISTRATION ENTRIES OF SDX CONTROLLERS
// *****************************************************************************
// <BEGIN Router Pattern using dynamic registration and per-target overrides BEGIN>
// ================================================================================================== //
i2b2.sdx.Master.RenderHTML = function(targetDivID, sdxDataPackage, options) {
	var funcName = "[i2b2.sdx.Master.RenderHTML] ";
	var tdiv = $(targetDivID);
	if (Object.isUndefined(tdiv) || Object.isNull(tdiv)) {
		console.error(funcName+'the targeted container does not exist!');
		return false;
	}
	if (Object.isUndefined(sdxDataPackage)) {
		console.error(funcName+'the SDX Data Package is empty!');
		return false;
	}
	
	try {
		var sdxType = sdxDataPackage.sdxInfo.sdxType;
	} catch (e) {
		console.error(funcName+'the data object type is not valid!');
		return false;
	}
	
	try {
		var t = i2b2.sdx.TypeControllers[sdxType].RenderHTML;
	} catch (e) {
		console.error(funcName+'the SDX Controller for '+sdxType+' does not handle RenderHTML!');
		return false;
	}

	// do we have the container registered?
	if (Object.isUndefined(i2b2.sdx.Master._sysData[tdiv.id])) {
		console.error(funcName+"DIV has not previously been registered to the SDX system");
		return false;
	}
	if (Object.isUndefined(i2b2.sdx.Master._sysData[tdiv.id][sdxType])) {
		console.error(funcName+"DIV has not previously been registered to the SDX system as accepting data type" + sdxType);
		return false;
	}

	if (!i2b2.sdx.Master._sysData[tdiv.id][sdxType].RenderHTML) {
		console.error(funcName+"The give DIV has been previously been registered to the SDX system but does not have a RenderHTML routine defined");
		return false;
	}
	return i2b2.sdx.Master._sysData[tdiv.id][sdxType].RenderHTML(sdxDataPackage,options, tdiv);
}

// ================================================================================================== //
i2b2.sdx.Master.AppendTreeNode = function(yuiTree, yuiRootNode, sdxDataPackage, sdxLoaderCallback) {
	var funcName = '[i2b2.sdx.Master.AppendTreeNode] ';
	if (Object.isUndefined(yuiTree)) {
		console.error(funcName+'yuiTree param is missing!');
		return false;
	}
	var tvid = $(yuiTree.id);
	if (Object.isUndefined(tvid)) {
		console.error(funcName+"yuiTree's DIV does not exist!");
		return false;
	}
	var tvid = tvid.id;

	if (Object.isUndefined(sdxDataPackage)) {
		console.error(funcName+'the SDX Render Package is missing!');
		return false;
	}
	if (!sdxDataPackage.renderData.html || !sdxDataPackage.renderData.htmlID || !sdxDataPackage.origData || !sdxDataPackage.sdxInfo) {
		console.error(funcName+'the data object is not a valid SDX Render Package!');
		return false;
	}
	var sdxType = sdxDataPackage.sdxInfo.sdxType;
	if (Object.isUndefined(i2b2.sdx.TypeControllers[sdxType])) {
		console.error(funcName+'the no SDX Controller for '+sdxType+' type exists!');
		return false;
	}
	
	if (Object.isUndefined(i2b2.sdx.TypeControllers[sdxType].RenderHTML)) {
		console.error(funcName+'the SDX Controller for '+sdxType+' does not handle RenderHTML!');
		return false;
	}

	// do we have the container registered?
	if (Object.isUndefined(i2b2.sdx.Master._sysData[tvid])) {
		console.error(funcName+"DIV has not previously been registered to the SDX system");
		return false;
	}
	if (Object.isUndefined(i2b2.sdx.Master._sysData[tvid][sdxType])) {
		console.error(funcName+"DIV has not previously been registered to the SDX system as accepting data type" + sdxType);
		return false;
	}

	if (!i2b2.sdx.Master._sysData[tvid][sdxType].AppendTreeNode) {
		console.error("The give DIV has been previously been registered to the SDX system but does not have a AppendTreeNode routine defined");
		return false;
	}
	return i2b2.sdx.Master._sysData[tvid][sdxType].AppendTreeNode(yuiTree, yuiRootNode, sdxDataPackage, sdxLoaderCallback);
}

// ================================================================================================== //
i2b2.sdx.Master.LoadChildrenFromTreeview = function(node, onCompleteCallback) {
	var funcName = "[i2b2.sdx.Master.LoadChildrenFromTreeview] ";
	var tvid = node.tree.id;
	if (Object.isUndefined(node.data.i2b2_SDX) ) {
		console.error(funcName+'the passed treeview node contains no SDX data!');
		return false;
	}
	var data = node.data.i2b2_SDX;
	if (Object.isUndefined(data.sdxInfo)) {
		console.error(funcName+'the SDX Data Package is empty!');
		return false;
	}
	if (!data.sdxInfo.sdxType) {
		console.error(funcName+'the data object is not a valid SDX Data Package!');
		return false;
	}
	var sdxType = data.sdxInfo.sdxType;
	if (Object.isUndefined(i2b2.sdx.TypeControllers[sdxType])) {
		console.error(funcName+'the no SDX Controller for '+sdxType+' type exists!');
		return false;
	}
	
	if (Object.isUndefined(i2b2.sdx.TypeControllers[sdxType].LoadChildrenFromTreeview)) {
		console.error(funcName+'the SDX Controller for '+sdxType+' does not handle RenderHTML!');
		return false;
	}

	// do we have the container registered?
	if (Object.isUndefined(i2b2.sdx.Master._sysData[tvid])) {
		console.error(funcName+"DIV has not previously been registered to the SDX system");
		return false;
	}
	if (Object.isUndefined(i2b2.sdx.Master._sysData[tvid][sdxType])) {
		console.error(funcName+"DIV has not previously been registered to the SDX system as accepting data type" + sdxType);
		return false;
	}

	if (!i2b2.sdx.Master._sysData[tvid][sdxType].LoadChildrenFromTreeview) {
		console.error(funcName+"DIV has been previously been registered to the SDX system but does not have a LoadChildrenFromTreeview routine defined");
		return false;
	}
	return i2b2.sdx.Master._sysData[tvid][sdxType].LoadChildrenFromTreeview(node, onCompleteCallback);
}
// ================================================================================================== //
i2b2.sdx.Master.getChildRecords = function(sdxParent, onCompleteCallback) {
	var funcName = "[i2b2.sdx.Master.getChildRecords] ";
	if (!Object.isUndefined(sdxParent.sdxInfo) ) {
		var sdxType = sdxParent.sdxInfo.sdxType;
	} else {
		console.error(funcName+"the parent node's package does not contain SDX information!");
		return false;
	}
	if (Object.isUndefined(i2b2.sdx.TypeControllers[sdxType])) {
		console.error(funcName+'the no SDX Controller for '+sdxType+' type exists!');
		return false;
	}
	
	if (Object.isUndefined(i2b2.sdx.TypeControllers[sdxType].getChildRecords)) {
		console.error(funcName+'the SDX Controller for '+sdxType+' does not handle getChildRecords!');
		return false;
	}

	return i2b2.sdx.TypeControllers[sdxType].getChildRecords(sdxParent, onCompleteCallback);
}
// ================================================================================================== //
i2b2.sdx.Master.onHoverOver = function(sdxType, eventObj, targetID, ddProxyObj) {
	var funcName = '[i2b2.sdx.Master.onHoverOver] ';
	if (Object.isUndefined(eventObj)) {
		console.error(funcName+'eventObj param is missing!');
		return false;
	}
	var tid = $(targetID);
	if (Object.isUndefined(tid)) {
		console.error(funcName+"DIV does not exist by given ID!");
		return false;
	}
	var tid = tid.id;
	if (Object.isUndefined(i2b2.sdx.TypeControllers[sdxType])) {
		console.error(funcName+'the no SDX Controller for '+sdxType+' type exists!');
		return false;
	}
	
	if (Object.isUndefined(i2b2.sdx.TypeControllers[sdxType].onHoverOver)) {
		console.warning(funcName+'the SDX Controller for '+sdxType+' does not handle onHoverOver!');
		return false;
	}

	// do we have the container registered?
	if (Object.isUndefined(i2b2.sdx.Master._sysData[tid])) {
		console.error(funcName+"DIV has not previously been registered to the SDX system");
		return false;
	}
	if (Object.isUndefined(i2b2.sdx.Master._sysData[tid][sdxType])) {
		console.error(funcName+"DIV has not previously been registered to the SDX system as accepting data type" + sdxType);
		return false;
	}

	if (!i2b2.sdx.Master._sysData[tid][sdxType].onHoverOver) {
		console.error("The give DIV has been previously been registered to the SDX system but does not have a AppendTreeNode routine defined");
		return false;
	}
	return i2b2.sdx.Master._sysData[tid][sdxType].onHoverOver(eventObj, tid, ddProxyObj);
}
// ================================================================================================== //
i2b2.sdx.Master.onHoverOut = function(sdxType, eventObj, targetID, ddProxyObj) {
	var funcName = '[i2b2.sdx.Master.onHoverOut] ';
	if (Object.isUndefined(eventObj)) {
		console.error(funcName+'eventObj param is missing!');
		return false;
	}
	var tid = $(targetID);
	if (!tid) {
		console.error(funcName+"DIV does not exist by given ID!");
		return false;
	}
	var tid = tid.id;
	if (Object.isUndefined(i2b2.sdx.TypeControllers[sdxType])) {
		console.error(funcName+'the no SDX Controller for '+sdxType+' type exists!');
		return false;
	}
	
	if (Object.isUndefined(i2b2.sdx.TypeControllers[sdxType].onHoverOut)) {
		console.warning(funcName+'the SDX Controller for '+sdxType+' does not handle onHoverOver!');
		return false;
	}

	// do we have the container registered?
	if (Object.isUndefined(i2b2.sdx.Master._sysData[tid])) {
		console.error(funcName+"DIV has not previously been registered to the SDX system");
		return false;
	}
	if (Object.isUndefined(i2b2.sdx.Master._sysData[tid][sdxType])) {
		console.error(funcName+"DIV has not previously been registered to the SDX system as accepting data type" + sdxType);
		return false;
	}

	if (!i2b2.sdx.Master._sysData[tid][sdxType].onHoverOut) {
		console.error("The give DIV has been previously been registered to the SDX system but does not have a AppendTreeNode routine defined");
		return false;
	}
	return i2b2.sdx.Master._sysData[tid][sdxType].onHoverOut(eventObj, tid, ddProxyObj);
}



// <END Router Pattern using dynamic registration and per-target overrides END>


console.timeEnd('execute time');
console.groupEnd();