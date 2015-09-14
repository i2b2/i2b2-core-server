console.group('Load & Execute component file: WORK > ctrl > general');
console.time('execute time');


// main plugin handler
i2b2.WORK.visible = false;
i2b2.WORK.ctrlr.main = {};


i2b2.WORK.ctrlr.main._generateRefreshList = function(tvTargets) {
	// this function is used to generate a list of treenodes that need to be refreshed
	// it's primary goal is to prevent errors caused by trying to refresh a child
	// who does not exist after it's parent has been refreshed
	if (!tvTargets.length) { return []; }

	var i1, i2, tNode, aNode, isDone, wasChanged;
	// ALGORITHM: pick a node, climb tree and see if ancestor node is in nodes list that was passed,
	// if ancestor is found, select that as our new working node and remove the previously selected
	// node, repeat.  Exit when algorithm has been applied to all nodes in the list.
	for (i1=0; i1<tvTargets.length; i1++) {
		if (tvTargets[i1]) {
			// get first ancestor
			if ((tvTargets[i1].depth!=0) && tvTargets[i1].parent) {
				// not at tree root yet, get next ancestor
				aNode = tvTargets[i1].parent;
				tNodeIdx = i1;
			} else {
				aNode = false;
			}
			while (aNode) {
				// is ancestor in list?
				for (i2=0; i2<tvTargets.length; i2++) {
					if (aNode == tvTargets[i2]) {
						// ancestor found, delete its child
						tvTargets[tNodeIdx] = undefined;
						// flag the ancestor node found as the next node for deletion if another ancestor is found in the list
						tNodeIdx = i2;
						break;  // out of this search loop
					}
				}
				// get next ancestor
				if ((aNode.depth!=0) && aNode.parent) {
					// not at tree root yet, get next ancestor
					aNode = aNode.parent;
				} else {
					aNode = false;
				}
			}
		}
	}
	// remove empty array entries
	return tvTargets.compact();
};


i2b2.WORK.ctrlr.main.moveFolder = function(target_nodeTV, new_parent_nodeTV) {
	// create callback display routine
	var scopedCallback = new i2b2_scopedCallback();
	scopedCallback.scope = new_parent_nodeTV;
	scopedCallback.callback = function(results) {
		i2b2.WORK.view.main.queryResponse = results.msgResponse;
		i2b2.WORK.view.main.queryRequest = results.msgRequest;
		var cl_target_node = target_nodeTV;
		var cl_parent_node = new_parent_nodeTV;
		if (results.error) {
			alert("An error occurred while trying to move a work item folder!");
		} else {
			var rl = [];
			rl.push(cl_target_node.parent);  // the target node itself is not refreshed but its parent
			rl.push(cl_parent_node);
			rl = i2b2.WORK.ctrlr.main._generateRefreshList(rl);
			// whack the "already loaded" status out of the parent node and initiate a 
			// dynamic loading of the childs nodes (including our newest addition)
			for (var i=0; i<rl.length; i++) {
				rl[i].collapse();
				rl[i].dynamicLoadComplete = false;
				rl[i].childrenRendered = false;
				rl[i].tree.removeChildren(rl[i]);
				rl[i].expand();
			}
		}
	}
	var trgtID = target_nodeTV.data.i2b2_SDX.sdxInfo.sdxKeyValue;
	var destID = new_parent_nodeTV.data.i2b2_SDX.sdxInfo.sdxKeyValue;
	destID = destID.substr(destID.lastIndexOf("\\")+1);  // we must make sure only the parent node's ID is used not it's full path 
	var varData = {
		result_wait_time: 180,
		target_node_id: trgtID, 
		new_parent_node_id: destID
	};
	i2b2.WORK.ajax.moveChild("WORK:Workplace", varData, scopedCallback);
}

i2b2.WORK.ctrlr.main.NewFolder = function(parent_node) {
	var fldrName = prompt("What name should be used for the new folder?", "New Folder");
	if (!fldrName) { return false; }

	// create callback display routine
	var scopedCallback = new i2b2_scopedCallback();
	scopedCallback.scope = parent_node;
	scopedCallback.callback = function(results) {
		i2b2.WORK.view.main.queryResponse = results.msgResponse;
		i2b2.WORK.view.main.queryRequest = results.msgRequest;
		var cl_new_key = newChildKey;
		var cl_parent_node = parent_node;
		if (results.error) {
			alert("An error occurred while trying to create a new work item!");
		} else {
			// whack the "already loaded" status out of the parent node and initiate a 
			// dynamic loading of the childs nodes (including our newest addition)
			cl_parent_node.collapse();
			cl_parent_node.dynamicLoadComplete = false;
			cl_parent_node.childrenRendered = false;
			cl_parent_node.tree.removeChildren(cl_parent_node);
			cl_parent_node.expand();
		}
	}
	var newChildKey = i2b2.h.GenerateAlphaNumId(20);
	var pn = parent_node.data.i2b2_SDX;
	var varInput = {
			child_name: fldrName, 
			share_id: pn.origData.share_id,
			child_index: newChildKey,
			parent_key_value: pn.sdxInfo.sdxKeyValue,
			child_visual_attributes: "FA",
			child_annotation: "FOLDER:"+fldrName,
			child_work_type: "FOLDER",
			result_wait_time: 180
	};
	i2b2.WORK.ajax.addChild("WORK:Workplace", varInput, scopedCallback);
};


i2b2.WORK.ctrlr.main.Rename = function(target_node) { 
	var nd = target_node.data.i2b2_SDX;
	if (Object.isUndefined(nd)) {
		console.error("The target node does not have SDX data attached.");
		return false;
	}
	var origName = nd.origData.name;
	var newName = prompt('Rename this work item to:', origName);
	if (!newName || newName==origName) { return false; }

	// create callback display routine
	var scopedCallback = new i2b2_scopedCallback();
	scopedCallback.scope = target_node;
	scopedCallback.callback = function(results) {
		i2b2.WORK.view.main.queryResponse = results.msgResponse;
		i2b2.WORK.view.main.queryRequest = results.msgRequest;
		var cl_parent_node = target_node.parent;
		if (results.error) {
			alert("An error occurred while trying to rename the selected item!");
		} else {
			// whack the "already loaded" status out of the parent node and initiate a 
			// dynamic loading of the childs nodes (including our newest addition)
			cl_parent_node.collapse();
			cl_parent_node.dynamicLoadComplete = false;
			cl_parent_node.childrenRendered = false;
			cl_parent_node.tree.removeChildren(cl_parent_node);
			cl_parent_node.expand();
		}
	}
	var pn = target_node.data.i2b2_SDX;
	//var myXml = i2b2.h.XPath( pn.origData.xmlOrig,'work_xml')[0];  //descendant::metadataxml
	//var myXml2 = i2b2.h.Xml2String(myXml.childNodes[1]);
	//var myXml2 = myXml2.replace(/&lt;/g,'<');
	//	var myXml2 = myXml2.replace(/&gt;/g,'>');

	var varInput = {
		rename_text: newName,
		rename_target_id: pn.sdxInfo.sdxKeyValue,
		result_wait_time: 180
	};
	i2b2.WORK.ajax.renameChild("WORK:Workplace", varInput, scopedCallback);
};


i2b2.WORK.ctrlr.main.Delete = function(target_node, options) { 
	var nd = target_node.data.i2b2_SDX;
	if (!options) { options = {}; }
	if (!options.silent) {
		var nodeTitle = getHtmlText($(target_node.data.nodeid).select("DIV")[0]);
		nodeTitle = String(nodeTitle).strip();
		if (nd.origData.encapType == "FOLDER") {
			var go = confirm('Are you sure you want to delete the folder "'+nodeTitle+'" and all of it\'s contents?');
		} else {
			var go = confirm('Are you sure you want to delete "'+nodeTitle+'"?');
		}
		if (!go) { return false; }
	}

	if (options.callback) {
		var scopedCallback = options.callback;
	} else {
		// create callback GUI update routine
		var scopedCallback = new i2b2_scopedCallback();
		scopedCallback.scope = target_node;
		scopedCallback.callback = function(results) {
			i2b2.WORK.view.main.queryResponse = results.msgResponse;
			i2b2.WORK.view.main.queryRequest = results.msgRequest;
			if (results.error) {
				alert("An error occurred while trying to delete the selected item!");
			} else {
				this.tree.removeNode(this, true);
			}
		}
	}
	var varInput = {
		delete_target_id: nd.sdxInfo.sdxKeyValue, 
		result_wait_time: 180
	};
	i2b2.WORK.ajax.deleteChild("WORK:Workplace", varInput, scopedCallback);
}


i2b2.WORK.ctrlr.main.Annotate = function(target_node) { 
	var nd = target_node.data.i2b2_SDX;
	if (Object.isUndefined(nd)) {
		console.error("The target node does not have SDX data attached.");
		return false;
	}
	var origAnno = nd.origData.annotation;
	if (!origAnno) { origAnno = ''; }
	var newAnno = prompt('Change this work item\'s annotation to:', origAnno);
	if (!newAnno || newAnno==origAnno) { return false; }

	// create callback display routine
	var scopedCallback = new i2b2_scopedCallback();
	scopedCallback.scope = target_node;
	scopedCallback.callback = function(results) {
		i2b2.WORK.view.main.queryResponse = results.msgResponse;
		i2b2.WORK.view.main.queryRequest = results.msgRequest;
		if (results.error) {
			alert("An error occurred while trying to annotate the selected item!");
		} else {
			// GUI refresh is not needed
			this.data.i2b2_SDX.origData.annotation = newAnno;
		}
	}
	var pn = target_node.data.i2b2_SDX;
	var varInput = {
		annotation_text: newAnno, 
		annotation_target_id: pn.sdxInfo.sdxKeyValue,
		result_wait_time: 180
	};
	i2b2.WORK.ajax.annotateChild("WORK:Workplace", varInput, scopedCallback);
};




i2b2.WORK.ctrlr.main.HandleDrop = function(sdxDropped) {
	var ddm = YAHOO.util.DDM;
	var trgtEl = ddm.getBestMatch(ddm.interactionInfo.drop);
	var trgtElID = trgtEl.id
	var yuiTV = YAHOO.widget.TreeView.findTreeByChildDiv(trgtElID);
	var trgtNode = yuiTV.getNodeByProperty("nodeid", trgtElID);
	var trgtSdx = trgtNode.data.i2b2_SDX;
	for (var i=0; i<sdxDropped.length; i++) {
		var cSDX = sdxDropped[i];
		if (cSDX.sdxInfo.sdxType=="WORK") {
			console.error("The action has been prevented.  This action should have been managed by the default WRK SDX controller");
			return false;
		} else if (cSDX.origData.isModifier) {
			alert("Work item being dropped is not supported.");
			return false;			
		} else {
			// add the new work item
			i2b2.WORK.ctrlr.main.AddWorkItem(cSDX, trgtNode);
		}
	}
};




i2b2.WORK.ctrlr.main.AddWorkItem = function(sdxChild, targetTvNode, options) { 
	if (!options) { options={}; }
	// sanity check can only add children to WRK folders
	if (targetTvNode.data.i2b2_SDX.sdxInfo.sdxType!="WRK") {
		console.error("This operation was refused. Only a Workplace folder can contain child work items.");
		return false;
	}

	// TODO: filter out WRK objects, if anything process using the sdxUnderlyingData info

	// generate the AJAX submessage depending upon SDX type
	var encapXML = "";
	var encapWorkType = "";
	var encapValues = {};
	var encapTitle = "";
	var encapNoEscape = [];
	switch(sdxChild.sdxInfo.sdxType) {
		case "CONCPT":
			encapXML = i2b2.WORK.cfg.msgs.encapsulateCONCPT;
			encapWorkType = "CONCEPT";
			encapValues.concept_level = sdxChild.origData.level;
			encapValues.concept_id = sdxChild.sdxInfo.sdxKeyValue;
			encapValues.concept_name = sdxChild.origData.name;
			encapTitle = encapValues.concept_name;
			encapValues.concept_synonym = i2b2.h.getXNodeVal(sdxChild.origData.xmlOrig,'synonym_cd');
			encapValues.concept_visual_attributes = i2b2.h.getXNodeVal(sdxChild.origData.xmlOrig,'visualattributes');
			encapValues.concept_total = i2b2.h.getXNodeVal(sdxChild.origData.xmlOrig,'totalnum');
			encapValues.concept_basecode = "";
			encapValues.concept_fact_table_column = i2b2.h.getXNodeVal(sdxChild.origData.xmlOrig,'facttablecolumn');;
			encapValues.concept_table_name = sdxChild.origData.table_name;
			encapValues.concept_column_name = sdxChild.origData.column_name;
			encapValues.concept_column_data_type = i2b2.h.getXNodeVal(sdxChild.origData.xmlOrig,'columndatatype');
			encapValues.concept_operator = sdxChild.origData.operator;
			encapValues.concept_dimcode = sdxChild.origData.dim_code;
			encapValues.concept_comment = i2b2.h.getXNodeVal(sdxChild.origData.xmlOrig,'comment');
			if (!encapValues.concept_comment) { encapValues.concept_comment=''; }
			encapValues.concept_tooltip = sdxChild.origData.tooltip;
			break;
		case "PRS":
			encapXML = i2b2.WORK.cfg.msgs.encapsulatePRS;
			encapWorkType = "PATIENT_COLL";
			encapValues.prs_id = sdxChild.sdxInfo.sdxKeyValue;
			encapValues.qi_id = sdxChild.origData.QI_id;
			encapValues.prs_name = sdxChild.origData.title;
			encapTitle = sdxChild.origData.titleCRC;
			encapValues.prs_description = sdxChild.origData.titleCRC;
			encapValues.prs_set_size = sdxChild.origData.size;
			encapValues.prs_start_date = sdxChild.origData.start_date;
			encapValues.prs_end_date = sdxChild.origData.end_date;
			break;
		case "ENS":
			encapXML = i2b2.WORK.cfg.msgs.encapsulateENS;
			encapWorkType = "ENCOUNTER_COLL";
			encapValues.prs_id = sdxChild.sdxInfo.sdxKeyValue;
			encapValues.qi_id = sdxChild.origData.QI_id;
			encapValues.prs_name = sdxChild.origData.title;
			encapTitle = sdxChild.origData.titleCRC;
			encapValues.prs_description = sdxChild.origData.titleCRC;
			encapValues.prs_set_size = sdxChild.origData.size;
			encapValues.prs_start_date = sdxChild.origData.start_date;
			encapValues.prs_end_date = sdxChild.origData.end_date;
			break;
		case "PRC":
			encapXML = i2b2.WORK.cfg.msgs.encapsulatePRC;
			encapWorkType = "PATIENT_COUNT_XML";
			encapValues.prc_id = sdxChild.sdxInfo.sdxKeyValue;
			encapValues.qi_id = sdxChild.origData.QI_id;
			encapValues.prc_name = sdxChild.origData.title;
			encapTitle = encapValues.prc_name;
			encapValues.prc_set_size = sdxChild.origData.size;
			encapValues.prc_start_date = sdxChild.origData.start_date;
			encapValues.prc_end_date = sdxChild.origData.end_date;
			break;
		case "QM":
			encapXML = i2b2.WORK.cfg.msgs.encapsulateQM;
			encapWorkType = "PREV_QUERY";
			encapValues.qm_id = sdxChild.sdxInfo.sdxKeyValue;
			encapValues.qm_name = sdxChild.origData.name;
			encapTitle = encapValues.qm_name;
			encapValues.qm_user_id = sdxChild.origData.userid;
			encapValues.qm_user_group_id = sdxChild.origData.group;
			break;
		case "PR":
			encapXML = i2b2.WORK.cfg.msgs.encapsulatePR;
			encapWorkType = "PATIENT";
			encapValues.pr_id = sdxChild.sdxInfo.sdxKeyValue;
			encapValues.pr_name = sdxChild.sdxInfo.sdxDisplayName;
			encapValues.parent_prs_id = sdxChild.parent.sdxInfo.sdxKeyValue;
			encapValues.parent_prs_name = sdxChild.parent.sdxInfo.sdxDisplayName;
			encapTitle = encapValues.pr_name;
			break;
		case "QDEF":
			encapXML = i2b2.WORK.cfg.msgs.encapsulateQDEF;
			encapWorkType = "QUERY_DEFINITION";
			encapValues.query_def = i2b2.h.Xml2String(sdxChild.origData.xmlOrig);
			encapTitle = sdxChild.sdxInfo.sdxDisplayName;
			encapNoEscape.push("query_def");
			break;
		case "QGDEF":
			encapXML = i2b2.WORK.cfg.msgs.encapsulateQGDEF;
			encapWorkType = "GROUP_TEMPLATE";
			encapValues.query_def = i2b2.h.Xml2String(sdxChild.origData.xmlOrig);
			encapValues.name = sdxChild.sdxInfo.sdxDisplayName;
			encapTitle = encapValues.name;
			encapNoEscape.push("query_def");
			break;
		default:
			console.error("The operation failed. "+sdxChild.sdxInfo.sdxType+" is an unhandled SDX Type");
			return false;
	}
	// package the work_xml snippet
	i2b2.h.EscapeTemplateVars(encapValues, encapNoEscape);
	var syntax = /(^|.|\r|\n)(\{{{\s*(\w+)\s*}}})/; //matches symbols like '{{{ field }}}'
	var t = new Template(encapXML, syntax);
	var encapMsg = t.evaluate(encapValues); 

	// gather primary message info
	var newChildKey = i2b2.h.GenerateAlphaNumId(20);
	var varInput = {
		child_name: encapTitle,
		child_index: newChildKey,
		parent_key_value: targetTvNode.data.i2b2_SDX.sdxInfo.sdxKeyValue,
		share_id: targetTvNode.data.i2b2_SDX.origData.share_id,
		child_visual_attributes: "ZA",
		child_annotation: "",
		child_work_type: encapWorkType,
		child_work_xml: encapMsg,
		result_wait_time: 180
	};
	if (options.callback) {
		var scopedCallback = options.callback;
	} else {
		// create callback display routine
		var scopedCallback = new i2b2_scopedCallback();
		scopedCallback.scope = targetTvNode;
		scopedCallback.callback = function(results) {
			i2b2.WORK.view.main.queryResponse = results.msgResponse;
			i2b2.WORK.view.main.queryRequest = results.msgRequest;
			var cl_new_key = newChildKey;
			var cl_parent_node = targetTvNode;
			if (results.error) {
				alert("An error occurred while trying to create a new work item!");
			} else {
				// whack the "already loaded" status out of the parent node and initiate a 
				// dynamic loading of the childs nodes (including our newest addition)
				cl_parent_node.collapse();
				cl_parent_node.dynamicLoadComplete = false;
				cl_parent_node.childrenRendered = false;
				cl_parent_node.tree.removeChildren(cl_parent_node);
				cl_parent_node.expand();
			}
		}
	}
	i2b2.WORK.ajax.addChild("WORK:Workplace", varInput, scopedCallback);
}


console.timeEnd('execute time');
console.groupEnd();
