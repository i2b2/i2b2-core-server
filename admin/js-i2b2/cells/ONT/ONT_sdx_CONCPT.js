/**
 * @projectDescription	Ontology Concept SDX data controller object.
 * @inherits 	i2b2.sdx.TypeControllers
 * @namespace	i2b2.sdx.TypeControllers.CONCPT
 * @author		Nick Benik, Griffin Weber MD PhD
 * @version 	1.3
 * ----------------------------------------------------------------------------------------
 * updated 9-15-08: RC4 launch [Nick Benik] 
 */
console.group('Load & Execute component file: ONT > SDX > CONCPT');
console.time('execute time');


i2b2.sdx.TypeControllers.CONCPT = {};
i2b2.sdx.TypeControllers.CONCPT.model = {};
// *********************************************************************************
//	ENCAPSULATE DATA
// *********************************************************************************
i2b2.sdx.TypeControllers.CONCPT.getEncapsulateInfo = function() {
	// this function returns the encapsulation head information
	return {sdxType: 'CONCPT', sdxKeyName: 'key', sdxControlCell:'ONT', sdxDisplayNameKey: 'name'};
}


// *********************************************************************************
//	GENERATE HTML (DEFAULT HANDLER)
// *********************************************************************************
i2b2.sdx.TypeControllers.CONCPT.RenderHTML= function(sdxData, options, targetDiv) {
	// OPTIONS:
	//	title: string
	//	showchildren: true | false
	//	cssClass: string
	//	icon: [data object]
	//		root: 		(filename of img, appended to i2b2_root+cellDir + '/assets')
	//		rootExp: 	(filename of img, appended to i2b2_root+cellDir + '/assets')
	//		branch:	(filename of img, appended to i2b2_root+cellDir + '/assets')
	//		branchExp:	(filename of img, appended to i2b2_root+cellDir + '/assets')
	//		leaf:		(filename of img, appended to i2b2_root+cellDir + '/assets')
	//		leafExp:	(filename of img, appended to i2b2_root+cellDir + '/assets')
	//	dragdrop: string (function name)
	//	context: string
	//	click: string 
	//	dblclick: string
	
	if (Object.isUndefined(options)) { options = {}; }
	var render = {html: retHtml, htmlID: id};
	var conceptId = sdxData.name;
	var id = "ONT_TID-" + i2b2.GUID();
	
	// process drag drop controllers
	if (!Object.isUndefined(options.dragdrop)) {
// NOTE TO SELF: should attachment of node dragdrop controller be handled by the SDX system as well? 
// This would ensure removal of the onmouseover call in a cross-browser way
		var sDD = '  onmouseover="' + options.dragdrop + '(\''+ targetDiv.id +'\',\'' + id + '\')" ';
	} else {
		var sDD = '';
	}

	// process allowing children to be viewed
	var bCanExp = false;
	if (sdxData.origData.hasChildren.substring(1,0) === "C"){
		// render as category
		icon = 'root';
		sDD = '';
		sIG = ' isGroup="Y"';
		bCanExp = true;
	} else if (sdxData.origData.hasChildren.substring(1,0) === "F")  {
		// render as possibly having children
		icon = 'branch';
		bCanExp = true;
		//var sCanExpand = ' canExpand="Y"';
	} else if (sdxData.origData.hasChildren.substring(1,0) === "O")  {
		// render as possibly having children
		icon = 'root';
		bCanExp = true;
		//var sCanExpand = ' canExpand="Y"';		
	} else if (sdxData.origData.hasChildren.substring(1,0) === "D") {
		// render as possibly having children
		icon = 'branch';
		bCanExp = true;
		//var sCanExpand = ' canExpand="Y"';

	} else {
		// render as not having children
		var icon = 'leaf';
		bCanExp = false;
	}
	// user can override
	if (Object.isBoolean(options.showchildren)) { 
		if (!options.showchildren) bCanExp = false;
	}
	if (sdxData.origData.hasChildren.substring(2,1) === "I")
	{
		bCanExp = true;
		sDD = " style='color: #c0c0c0;' ";		
	}
	else if (sdxData.origData.hasChildren.substring(2,1) === "H")
	{
		sDD += " style='color: #c00000;' ";
	}
	else if ((!Object.isUndefined(sdxData.origData.synonym_cd)) && (sdxData.origData.synonym_cd != 'N')) 
	{
		sDD += " style='color: #0000ff;' ";
	}		
	render.canExpand = bCanExp;
	render.iconType = 'CONCPT_'+icon;
	if (!Object.isUndefined(icon)) {
		var icn = (eval('options.icon.'+icon));
		if (!Object.isUndefined(icn)) { render.icon = i2b2.hive.cfg.urlFramework + 'cells/ONT/assets/'+ icn }
		var icn = (eval('options.icon.'+icon+'Exp'));
		if (!Object.isUndefined(icn)) { render.iconExp = i2b2.hive.cfg.urlFramework + 'cells/ONT/assets/'+ icn }
		// in cases of one set icon, copy valid icon to the missing icon
		if (Object.isUndefined(render.icon) && !Object.isUndefined(render.iconExp)) {	sdxData.icon = render.iconExp; }
		if (!Object.isUndefined(render.icon) && Object.isUndefined(render.iconExp)) {	sdxData.iconExp = render.icon; }
	}
	// cleanup
	if (Object.isUndefined(render.icon)) {
		console.warn("[SDX RenderHTML] no '"+icon+"' icon has been set in the options passed");
		console.dir(options);
		render.icon = '';
		render.iconExp = '';
	}
	
	// handle the event controllers
	var sMainEvents = sDD;
	var sImgEvents = sDD;
	switch(icon) {
		case "root":
			if (options.click) {sMainEvents += ' onclick="'+ options.click +'" '; }
			if (options.dblclick) {sMainEvents += ' ondblclick="'+ options.dblclick +'" '; }
			if (options.context) {sMainEvents += ' oncontext="'+ options.context +'" '; } else {retHtml += ' oncontextmenu="return false" '; }
			break;
		case "branch":
			if (options.click) { sMainEvents += ' onclick="'+ options.click +'" '; }
			if (options.dblclick) { sMainEvents += ' ondblclick="'+ options.dblclick +'" '; }
			if (options.context) { sMainEvents += ' oncontext="'+ options.context +'" '; } else {retHtml += ' oncontextmenu="return false" '; }
			break;
		default:
			sMainEvents += ' oncontextmenu="return false" ';
	}

	//Added to provide tooltip information for concepts/terms
	var v_tooltip = '';
	
	try{
		if (($('ONTNAVshowShortTooltips').checked) || ($('ONTFINDshowShortTooltips').checked) )
		{
			v_tooltip += 'title="'+ sdxData.origData.name;
		} else 
		{
			v_tooltip += 'title="'+ sdxData.origData.tooltip;			
		}
		
		if ((($('ONTNAVshowCodeTooltips').checked) || ($('ONTFINDshowCodeTooltips').checked) )
 && !Object.isUndefined(sdxData.origData.basecode))
		{
			v_tooltip += " - " + sdxData.origData.basecode;
		}
		
		v_tooltip += '" ';
	}
	catch(e){
		v_tooltip = '';
	}
	
	// **** Render the HTML ***
	var retHtml = '<DIV id="' + id + '" '+ v_tooltip + sMainEvents + ' style="cursor:pointer;">';
	retHtml += '<DIV ';
	if (Object.isString(options.cssClass)) {
		retHtml += ' class="'+options.cssClass+'" ';
	} else {
		retHtml += ' class= "sdxDefaultCONCPT" ';
	}
	retHtml += sImgEvents;
	retHtml += '>';
	retHtml += '<IMG src="'+render.icon+'"/>'; 
	if ($('ONTNAVshowPatientCounts').checked && sdxData.origData.total_num == 0)
	{
		retHtml += "[";
	}
	if (!Object.isUndefined(options.title)) {
		// BUG FIX: Partners uses "zz " to move items to the bottom of lists, java client removes the "zz " prefix.
		if (options.title.substr(0,3) == "zz ") { options.title = options.title.substr(3); }
		retHtml += options.title;
	} else {
		console.warn('[SDX RenderHTML] no title was given in the creation options for an ONT>CONCPT node!');
		retHtml += ' CONCPT '+id;
	}
	if ($('ONTNAVshowPatientCounts').checked)
	{
		if (!Object.isUndefined(sdxData.origData.total_num)) {
			retHtml += " - " + sdxData.origData.total_num;
		}
		if (sdxData.origData.total_num == 0)
		{
			retHtml += "]";
		}
	}
	retHtml += '</DIV></DIV>';
	render.html = retHtml;
	render.htmlID =  id;
	return { renderData: render, origData: sdxData.origData, sdxInfo: sdxData.sdxInfo };
}


// *********************************************************************************
//	HANDLE HOVER OVER TARGET ENTRY (DEFAULT HANDLER)
// *********************************************************************************
i2b2.sdx.TypeControllers.CONCPT.onHoverOver = function(e, id, ddProxy) {    
	var el = $(id);	
	if (el) { Element.addClassName(el,"ddCONCPTTarget"); }
}


// *********************************************************************************
//	HANDLE HOVER OVER TARGET EXIT (DEFAULT HANDLER)
// *********************************************************************************
i2b2.sdx.TypeControllers.CONCPT.onHoverOut = function(e, id, ddProxy) { 
	var el = $(id);	
	if (el) { Element.removeClassName(el,"ddCONCPTTarget"); }
}


// *********************************************************************************
//	ADD DATA TO TREENODE (DEFAULT HANDLER)
// *********************************************************************************
i2b2.sdx.TypeControllers.CONCPT.AppendTreeNode = function(yuiTree, yuiRootNode, sdxDataPack, callbackLoader) {    
	var myobj = { html: sdxDataPack.renderData.html, nodeid: sdxDataPack.renderData.htmlID}
	var tmpNode = new YAHOO.widget.HTMLNode(myobj, yuiRootNode, false, true);
	if (sdxDataPack.renderData.iconType != 'CONCPT_item' && !Object.isUndefined(callbackLoader)) {
		// add the callback to load child nodes
		sdxDataPack.sdxInfo.sdxLoadChildren = callbackLoader;
	}
	tmpNode.data.i2b2_SDX = sdxDataPack;
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
	if (sdxDataPack.renderData.iconType == 'CONCPT_leaf' || !sdxDataPack.renderData.canExpand) { tmpNode.dynamicLoadComplete = true; }
	return tmpNode;
}


// *********************************************************************************
//	GET CHILD RECORDS (DEFAULT HANDELER)
// *********************************************************************************
i2b2.sdx.TypeControllers.CONCPT.LoadChildrenFromTreeview = function(node, onCompleteCallback) {
	 if ((node.tree.id == 'ontSearchModifiersResults') || (!$('ONTNAVdisableModifiers').checked && node.tree.id == 'ontNavResults')) {
			i2b2.sdx.TypeControllers.CONCPT.LoadModifiers(node, onCompleteCallback, true);
		} else {
			i2b2.sdx.TypeControllers.CONCPT.LoadConcepts(node, onCompleteCallback, false);
	 }
}


i2b2.sdx.TypeControllers.CONCPT.LoadConcepts = function(node, onCompleteCallback, modifier) {
	var scopedCallback = new i2b2_scopedCallback();
	scopedCallback.scope = node.data.i2b2_SDX;
	scopedCallback.callback = function(results){
		var cl_node = node;
		var cl_key = key;
		var cl_onCompleteCB = onCompleteCallback;
		var cl_options = options;
		// THIS function is used to process the AJAX results of the getChild call
		//		results data object contains the following attributes:
		//			refXML: xmlDomObject <--- for data processing
		//			msgRequest: xml (string)
		//			msgResponse: xml (string)
		//			error: boolean
		//			errorStatus: string [only with error=true]
		//			errorMsg: string [only with error=true]
		
// TODO: REFACTOR THIS! (Roll into COMM message sniffer?)
		try {
			i2b2.ONT.view[i2b2.ONT.view.main.currentTab].queryRequest = results.msgRequest;
			i2b2.ONT.view[i2b2.ONT.view.main.currentTab].queryResponse = results.msgResponse;
		} catch(e) {}

		// handle any errors in the message
		if (results.error) {
			// process the specific error
			switch (node.tree.id) {
				case "ontNavResults":
					var t = i2b2.ONT.view.nav.params;
					break;
				case "ontSearchCodesResults":
					var t = i2b2.ONT.view.find.params;
					break;
				case "ontSearchModifiersResults":
					var t = i2b2.ONT.view.find.params;
					break;
				case "ontSearchNamesResults":
					var t = i2b2.ONT.view.find.params;
					break;
				default:
					var t = i2b2.ONT.params;
			}
			var errorCode = results.refXML.getElementsByTagName('status')[0].firstChild.nodeValue;
			var eaction = false;
			if (errorCode == "MAX_EXCEEDED") {
				var eaction = confirm("The number of terms that were returned exceeds the maximum number currently set as " + t.max + ". Would you like to increase it to " + (t.max * 5) + " so you can try again?");
			}
			else {
				alert("The following error has occurred:\n" + errorCode);
			}
			// re-fire the call with no max limit if the user requested so
			if (eaction) {
				// TODO: Implement param routing from node's container
				//var mod_options = Object.clone(cl_options);
				//delete mod_options.ont_max_records;
				//i2b2.ONT.ajax.GetChildConcepts("ONT:SDX:Concept", mod_options, scopedCallback );
				//   return true;
				t.max = t.max * 5;
			}
			// ROLLBACK the tree changes
			cl_onCompleteCB();
			// reset dynamic load state for the node (total hack of YUI Treeview)
			node.collapse();
			node.dynamicLoadComplete = false;
			node.expanded = false;
			node.childrenRendered = false;
			node._dynLoad = true;
			// uber-elite code (fix the style settings)
			var tc = node.getToggleEl().className;
			tc = tc.substring(0, tc.length - 1) + 'p';
			node.getToggleEl().className = tc;
			// fix the icon image
			var img = node.getContentEl();
			img = Element.select(img, 'img')[0];
			img.src = node.data.i2b2_SDX.renderData.icon;
			return false;
		}
		if (modifier) {	
			var c = results.refXML.getElementsByTagName('modifier');
		} else {
			var c = results.refXML.getElementsByTagName('concept');			
		}
		for(var i=0; i<1*c.length; i++) {
			/*
			var o = new Object;
			o.xmlOrig = c[i];
			o.parent = this.origData;
			if (modifier) {	
				o.isModifier = true;
				o.applied_path = i2b2.h.getXNodeVal(c[i],'applied_path');
			} else {
				o.isModifier = false;				
			}
			o.name = i2b2.h.getXNodeVal(c[i],'name');
			//o.hasChildren = i2b2.h.getXNodeVal(c[i],'visualattributes').substring(0,2);
			o.hasChildren = YAHOO.lang.trim(i2b2.h.getXNodeVal(c[i],'visualattributes').substring(0,3)); 
			o.level = i2b2.h.getXNodeVal(c[i],'level');
			o.key = i2b2.h.getXNodeVal(c[i],'key');
			if (cl_options.ont_short_tooltip) {
				o.tooltip = o.name;
			} else {
				o.tooltip = i2b2.h.getXNodeVal(c[i],'tooltip');
			}
			o.icd9 = '';
			o.table_name = i2b2.h.getXNodeVal(c[i],'tablename');
			o.column_name = i2b2.h.getXNodeVal(c[i],'columnname');
			o.operator = i2b2.h.getXNodeVal(c[i],'operator');
			o.total_num = i2b2.h.getXNodeVal(c[i],'totalnum');
			o.dim_code = i2b2.h.getXNodeVal(c[i],'dimcode');
			o.basecode = i2b2.h.getXNodeVal(c[i],'basecode');
			if (cl_options.ont_show_concept_code && o.basecode != undefined) {
				o.tooltip  += "(" + o.basecode + ")";
			}
			// append the data node
			var sdxDataNode = i2b2.sdx.Master.EncapsulateData('CONCPT',o);
			*/
			var sdxDataNode = i2b2.sdx.TypeControllers.CONCPT.MakeObject(c[i], modifier, cl_options, this.origData);
			if (modifier) {				
				var renderOptions = {
					title: i2b2.h.getXNodeVal(c[i],'name'),
					dragdrop: "i2b2.sdx.TypeControllers.CONCPT.AttachDrag2Data",
					dblclick: "i2b2.ONT.view.nav.ToggleNode(this,'"+cl_node.tree.id+"')",
					icon: {
						root: "sdx_ONT_MODIFIER_root.gif",
						rootExp: "sdx_ONT_MODIFIER_root-exp.gif",
						branch: "sdx_ONT_MODIFIER_branch.gif",
						branchExp: "sdx_ONT_MODIFIER_branch-exp.gif",
						leaf: "sdx_ONT_MODIFIER_leaf.gif"
					}
				};
			} else {
				var renderOptions = {
					title: i2b2.h.getXNodeVal(c[i],'name'),
					dragdrop: "i2b2.sdx.TypeControllers.CONCPT.AttachDrag2Data",
					dblclick: "i2b2.ONT.view.nav.ToggleNode(this,'"+cl_node.tree.id+"')",
					icon: {
						root: "sdx_ONT_CONCPT_root.gif",
						rootExp: "sdx_ONT_CONCPT_root-exp.gif",
						branch: "sdx_ONT_CONCPT_branch.gif",
						branchExp: "sdx_ONT_CONCPT_branch-exp.gif",
						leaf: "sdx_ONT_CONCPT_leaf.gif"
					}
				};				
			}

			var sdxRenderData = i2b2.sdx.Master.RenderHTML(cl_node.tree.id, sdxDataNode, renderOptions);
			i2b2.sdx.Master.AppendTreeNode(cl_node.tree, cl_node, sdxRenderData);
		}
		// handle the YUI treeview	
		cl_onCompleteCB();
	}
	var key = node.data.i2b2_SDX.sdxInfo.sdxKeyValue;
	// TODO: Implement param routing from node's container
	var options = {};
	switch (node.tree.id) {
		case "ontNavResults":
			var t = i2b2.ONT.view.nav.params;
			break;
		case "ontSearchCodesResults":
			var t = i2b2.ONT.view.find.params;
			break;
		case "ontSearchModifiersResults":
			var t = i2b2.ONT.view.find.params;
			break;
		case "ontSearchNamesResults":
			var t = i2b2.ONT.view.find.params;
			break;
		default:
			var t = i2b2.ONT.params;
	}
	options.ont_hidden_records = t.hiddens;
	options.ont_max_records = "max='"+t.max+"' ";
	options.ont_synonym_records = t.synonyms;
	options.ont_patient_count = t.patientCount;
	options.ont_short_tooltip = t.shortTooltip;
	options.ont_show_concept_code = t.showConceptCode;
	options.concept_key_value = key;
	if (t.modifiers == undefined || t.modifiers == false)
	{
		options.version = i2b2.ClientVersion;	
	} else {
		options.version = "1.5";	
	}
	i2b2.ONT.ajax.GetChildConcepts("ONT:SDX:Concept", options, scopedCallback );		
	
}

i2b2.sdx.TypeControllers.CONCPT.MakeObject = function(c, modifier, cl_options, origData, objectType) {
			var o = new Object;
			o.xmlOrig = c;
			o.parent = origData;
			if (modifier) {	
				o.isModifier = true;
				o.applied_path = i2b2.h.getXNodeVal(c,'applied_path');
			} else {
				o.isModifier = false;				
			}
			o.name = i2b2.h.getXNodeVal(c,'name');
			if (objectType != undefined && objectType == "QM") {
				o.id = i2b2.h.getXNodeVal(c,'query_master_id');
				o.title = "(PrevQuery)" + o.name;
			} else 			if (objectType != undefined && (objectType == "PRS" || objectType == "ENS")) {
				o.result_instance_id = i2b2.h.getXNodeVal(c,'result_instance_id');
				o.title = i2b2.h.getXNodeVal(c,'description');
			}
			//o.hasChildren = i2b2.h.getXNodeVal(c[i],'visualattributes').substring(0,2);
			if (i2b2.h.getXNodeVal(c,'visualattributes') != undefined) {
				o.hasChildren = YAHOO.lang.trim(i2b2.h.getXNodeVal(c,'visualattributes').substring(0,3)); 
			} 
			o.level = i2b2.h.getXNodeVal(c,'level');
			o.key = i2b2.h.getXNodeVal(c,'key');
			if (cl_options != undefined && cl_options.ont_short_tooltip) {
				o.tooltip = o.name;
			} else {
				o.tooltip = i2b2.h.getXNodeVal(c,'tooltip');
			}
			o.icd9 = '';
			o.table_name = i2b2.h.getXNodeVal(c,'tablename');
			o.column_name = i2b2.h.getXNodeVal(c,'columnname');
			o.operator = i2b2.h.getXNodeVal(c,'operator');
			o.total_num = i2b2.h.getXNodeVal(c,'totalnum');
			o.synonym_cd = i2b2.h.getXNodeVal(c,'synonym_cd');
			o.dim_code = i2b2.h.getXNodeVal(c,'dimcode');
			o.basecode = i2b2.h.getXNodeVal(c,'basecode');
			if (cl_options != undefined && cl_options.ont_show_concept_code && o.basecode != undefined) {
				o.tooltip  += "(" + o.basecode + ")";
			}
			// append the data node
			if (objectType != undefined && (objectType == "QM" || objectType == "PRS" || objectType == "ENS")) {
				return (i2b2.sdx.Master.EncapsulateData(objectType,o));				
			} else {
				return (i2b2.sdx.Master.EncapsulateData('CONCPT',o));
			}
}

i2b2.sdx.TypeControllers.CONCPT.LoadModifiers = function(node, onCompleteCallback, modifier) {
	var scopedCallback = new i2b2_scopedCallback();
	scopedCallback.scope = node.data.i2b2_SDX;
	scopedCallback.callback = function(results){
		var cl_node = node;
		var cl_key = key;
		var cl_onCompleteCB = onCompleteCallback;
		var cl_options = options;
		// THIS function is used to process the AJAX results of the getChild call
		//		results data object contains the following attributes:
		//			refXML: xmlDomObject <--- for data processing
		//			msgRequest: xml (string)
		//			msgResponse: xml (string)
		//			error: boolean
		//			errorStatus: string [only with error=true]
		//			errorMsg: string [only with error=true]
		
// TODO: REFACTOR THIS! (Roll into COMM message sniffer?)
		try {
			i2b2.ONT.view[i2b2.ONT.view.main.currentTab].queryRequest = results.msgRequest;
			i2b2.ONT.view[i2b2.ONT.view.main.currentTab].queryResponse = results.msgResponse;
		} catch(e) {}

		// handle any errors in the message
		if (results.error) {
			// process the specific error
			// ROLLBACK the tree changes
			cl_onCompleteCB();
			// reset dynamic load state for the node (total hack of YUI Treeview)
			node.collapse();
			node.dynamicLoadComplete = false;
			node.expanded = false;
			node.childrenRendered = false;
			node._dynLoad = true;
			// uber-elite code (fix the style settings)
			var tc = node.getToggleEl().className;
			tc = tc.substring(0, tc.length - 1) + 'p';
			node.getToggleEl().className = tc;
			// fix the icon image
			var img = node.getContentEl();
			img = Element.select(img, 'img')[0];
			img.src = node.data.i2b2_SDX.renderData.icon;
			i2b2.sdx.TypeControllers.CONCPT.LoadConcepts(node, onCompleteCallback, false);
			return false;
		}
		var c = results.refXML.getElementsByTagName('modifier');
		for(var i=0; i<1*c.length; i++) {
			var sdxDataNode = i2b2.sdx.TypeControllers.CONCPT.MakeObject(c[i], modifier, cl_options, this.origData);
			if (modifier) {				
				var renderOptions = {
					title: i2b2.h.getXNodeVal(c[i],'name'),
					dragdrop: "i2b2.sdx.TypeControllers.CONCPT.AttachDrag2Data",
					dblclick: "i2b2.ONT.view.nav.ToggleNode(this,'"+cl_node.tree.id+"')",
					icon: {
						root: "sdx_ONT_MODIFIER_root.gif",
						rootExp: "sdx_ONT_MODIFIER_root-exp.gif",
						branch: "sdx_ONT_MODIFIER_branch.gif",
						branchExp: "sdx_ONT_MODIFIER_branch-exp.gif",
						leaf: "sdx_ONT_MODIFIER_leaf.gif"
					}
				};
			} else {
				var renderOptions = {
					title: i2b2.h.getXNodeVal(c[i],'name'),
					dragdrop: "i2b2.sdx.TypeControllers.CONCPT.AttachDrag2Data",
					dblclick: "i2b2.ONT.view.nav.ToggleNode(this,'"+cl_node.tree.id+"')",
					icon: {
						root: "sdx_ONT_CONCPT_root.gif",
						rootExp: "sdx_ONT_CONCPT_root-exp.gif",
						branch: "sdx_ONT_CONCPT_branch.gif",
						branchExp: "sdx_ONT_CONCPT_branch-exp.gif",
						leaf: "sdx_ONT_CONCPT_leaf.gif"
					}
				};				
			}

			var sdxRenderData = i2b2.sdx.Master.RenderHTML(cl_node.tree.id, sdxDataNode, renderOptions);
	
			i2b2.sdx.Master.AppendTreeNode(cl_node.tree, cl_node, sdxRenderData);
		}
		// handle the YUI treeview	
		//mm 10-7 cl_onCompleteCB();
		if ((node.data.i2b2_SDX.origData.hasChildren != "DA") && (node.data.i2b2_SDX.origData.hasChildren != "OAE") &&
		(node.data.i2b2_SDX.origData.hasChildren != "DAE") && (node.data.i2b2_SDX.origData.hasChildren != "OA") ){
			i2b2.sdx.TypeControllers.CONCPT.LoadConcepts(node, onCompleteCallback, false);
		} else {
			cl_onCompleteCB();
		}
	}
	var key = node.data.i2b2_SDX.sdxInfo.sdxKeyValue;
	// TODO: Implement param routing from node's container
	var options = {};
	switch (node.tree.id) {
		case "ontNavResults":
			var t = i2b2.ONT.view.nav.params;
			break;
		case "ontSearchModifiersResults":
			var t = i2b2.ONT.view.find.params;
			break;			
		case "ontSearchCodesResults":
			var t = i2b2.ONT.view.find.params;
			break;					
		case "ontSearchNamesResults": 
//		case "ontSearchCodesResults", "ontSearchNamesResults", "ontSearchModifiersResults":
			var t = i2b2.ONT.view.find.params;
			break;
		default:
			var t = i2b2.ONT.params;
	}
	options.ont_hidden_records = t.hiddens;
	options.ont_max_records = "max='"+t.max+"' ";
	options.ont_synonym_records = t.synonyms;
	options.ont_patient_count = t.patientCount;
	options.ont_short_tooltip = t.shortTooltip;
	options.ont_show_concept_code = t.showConceptCode;
	options.concept_key_value = key;
	
	if ((node.data.i2b2_SDX.origData.hasChildren == "DA") ||
	(node.data.i2b2_SDX.origData.hasChildren == "DAE") ||
	(node.data.i2b2_SDX.origData.hasChildren == "OA") ||
	(node.data.i2b2_SDX.origData.hasChildren == "OAE") 	
	){
		options.modifier_key_value = node.data.i2b2_SDX.origData.key;
		options.modifier_applied_path = node.data.i2b2_SDX.origData.applied_path;
		
		var realdata = node.data.i2b2_SDX.origData;
		while ((realdata.hasChildren != "FA") && (realdata.hasChildren != "CA")
		&& (realdata.hasChildren != "FAE") && (realdata.hasChildren != "CAE")

		) {
			realdata  = realdata.parent;	
		}		
		options.modifier_applied_concept = realdata.key;//node.data.i2b2_SDX.origData.parent.key;
		i2b2.ONT.ajax.GetChildModifiers("ONT:SDX:Concept", options, scopedCallback );	
	} else {
		i2b2.ONT.ajax.GetModifiers("ONT:SDX:Concept", options, scopedCallback );
	}
}


// *********************************************************************************
//	ATTACH DRAG TO DATA (DEFAULT HANDLER)
// *********************************************************************************
i2b2.sdx.TypeControllers.CONCPT.AttachDrag2Data = function(divParentID, divDataID){
	if (Object.isUndefined($(divDataID))) {	return false; }
	
	// get the i2b2 data from the yuiTree node
	var tvTree = YAHOO.widget.TreeView.getTree(divParentID);
	var tvNode = tvTree.getNodeByProperty('nodeid', divDataID);
	if (!Object.isUndefined(tvNode.DDProxy)) { return true; }
	
	// attach DD
	var t = new i2b2.sdx.TypeControllers.CONCPT.DragDrop(divDataID)
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
i2b2.sdx.TypeControllers.CONCPT.DragDrop = function(id, config) {
	if (id) {
		this.init(id, 'CONCPT',{isTarget:false});
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
YAHOO.extend(i2b2.sdx.TypeControllers.CONCPT.DragDrop, YAHOO.util.DDProxy);
i2b2.sdx.TypeControllers.CONCPT.DragDrop.prototype.startDrag = function(x, y) {
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
i2b2.sdx.TypeControllers.CONCPT.DragDrop.prototype.endDrag = function(e) {
	// remove DragDrop targeting CCS
	var targets = YAHOO.util.DDM.getRelated(this, true); 
	for (var i=0; i<targets.length; i++) {      
		var targetEl = targets[i]._domRef; 
		i2b2.sdx.Master.onHoverOut('CONCPT', e, targetEl, this);
	} 
};
i2b2.sdx.TypeControllers.CONCPT.DragDrop.prototype.alignElWithMouse = function(el, iPageX, iPageY) {
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
i2b2.sdx.TypeControllers.CONCPT.DragDrop.prototype.onDragOver = function(e, id) {
	// fire the onHoverOver (use SDX so targets can override default event handler)
	i2b2.sdx.Master.onHoverOver('CONCPT', e, id, this);
};
i2b2.sdx.TypeControllers.CONCPT.DragDrop.prototype.onDragOut = function(e, id) {
	// fire the onHoverOut handler (use SDX so targets can override default event handlers)
	i2b2.sdx.Master.onHoverOut('CONCPT', e, id, this);
};
i2b2.sdx.TypeControllers.CONCPT.DragDrop.prototype.onDragDrop = function(e, id) {
	i2b2.sdx.Master.onHoverOut('CONCPT', e, id, this);
	// retreive the concept data from the dragged element
	draggedData = this.yuiTreeNode.data.i2b2_SDX;
	// exit if we are a root node
	if ((draggedData.origData.hasChildren=="CA") || (draggedData.origData.hasChildren=="OA") || (draggedData.origData.hasChildren=="OAE"))  { return false; }
	i2b2.sdx.Master.ProcessDrop(draggedData, id);
};


// *********************************************************************************
//	<BLANK> DROP HANDLER 
//	!!!! DO NOT EDIT - ATTACH YOUR OWN CUSTOM ROUTINE USING
//	!!!! THE i2b2.sdx.Master.setHandlerCustom FUNCTION
// *********************************************************************************
i2b2.sdx.TypeControllers.CONCPT.DropHandler = function(sdxData) {
	alert('[Concept DROPPED] You need to create your own custom drop event handler.');
}


console.timeEnd('execute time');
console.groupEnd();
