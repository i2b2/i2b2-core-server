
console.group('Load & Execute component file: WORK > view > main');
console.time('execute time');


// ********* View: List ********* 
// create and save the view object
i2b2.WORK.view.main = new i2b2Base_cellViewController(i2b2.WORK, 'main');
i2b2.WORK.view.main.visible = false;


i2b2.WORK.view.main.Refresh = function(e){
    $("refWorkQS").setStyle({
        display:'none'
    });
    $("refWork2QS").setStyle({
        display:'inline'
    });
    
    
    
    
    $("refWork2QS").setStyle({
        display:'none'
    });
    $("refWorkQS").setStyle({
        display:'inline'
    });

},
// ================================================================================================== //
i2b2.WORK.view.main.Resize = function(e){
    // this function provides the resize functionality needed for this screen
    var viewObj = i2b2.WORK.view.main;
	var t = $('wrkWorkplace');
    if (viewObj.visible) {
		$('wrkWorkplace').show();
        //var ds = document.viewport.getDimensions();
        var w =  window.innerWidth || (window.document.documentElement.clientWidth || window.document.body.clientWidth);
        var h =  window.innerHeight || (window.document.documentElement.clientHeight || window.document.body.clientHeight);
		if (w < 840) { w = 840; }
		if (h < 517) { h = 517; }
		// resize our visual components
		switch(i2b2.hive.MasterView.getViewMode()) {
			case "Patients":
				w = Math.max(initBrowserViewPortDim.width-rightSideWidth, 0);
				break;
			case "Analysis":
				w = parseInt(w/3)-10;
				break;			
			default:
				return true;
				break;
        }
		t.style.width = w;
		if (viewObj.isZoomed) {
			t.style.top = '';
			$('wrkTreeview').style.height = h - 97;
		} else {
			var hz = parseInt((h - 321) / 2);
			t.style.top = hz + 108;
			$('wrkTreeview').style.height = hz + 8;
        }
        t.show();
	} else {
		t.hide();
    }
}
// attach resize events
// YAHOO.util.Event.addListener(window, "resize", i2b2.WORK.view.main.Resize, i2b2.WORK.view.main); // tdw9

//================================================================================================== //
i2b2.WORK.view.main.splitterDragged = function()
{
	var splitter = $( i2b2.hive.mySplitter.name );
	var work = $("wrkWorkplace");
	work.style.width	= Math.max((parseInt(splitter.style.left) - work.offsetLeft - 3), 0) + "px";	
}

//================================================================================================== //
i2b2.WORK.view.main.ResizeHeight = function(){
    // this function provides the resize functionality needed for this screen
    var viewObj = i2b2.WORK.view.main;
	var t = $('wrkWorkplace');
    if (viewObj.visible) {
		$('wrkWorkplace').show();
        //var ds = document.viewport.getDimensions();
        var h =  window.innerHeight || (window.document.documentElement.clientHeight || window.document.body.clientHeight);
		if (h < 517) { h = 517; }
		// resize our visual components
		if (viewObj.isZoomed) {
			t.style.top = '';
			$('wrkTreeview').style.height = h - 97;
		} else {
			var hz = parseInt((h - 321) / 2);
			t.style.top = hz + 108;
			$('wrkTreeview').style.height = hz + 8;
        }
        t.show();
	} else {
		t.hide();
    }
}



// ================================================================================================== //
i2b2.WORK.view.main.ZoomView = function() {
	i2b2.hive.MasterView.toggleZoomWindow("WORK");
}

//================================================================================================== //
i2b2.events.initView.subscribe((function(eventTypeName, newMode) {
// -------------------------------------------------------
	this.visible = true;
	this.Resize();
// -------------------------------------------------------
}),'',i2b2.WORK.view.main);


// process view mode changes (via EVENT CAPTURE)
// ================================================================================================== //
i2b2.events.changedViewMode.subscribe((function(eventTypeName, newMode){
    newMode = newMode[0];
    this.viewMode = newMode;
    switch (newMode) {
		case "Patients":
		case "Analysis":
			// check if other windows are zoomed and blocking us
			var zw = i2b2.hive.MasterView.getZoomWindows();
			if (zw.member("ONT") || zw.member("HISTORY")) {
				this.visible = false;
			} else {
				this.visible = true;
			}
			break;
		case "AnalysisZoomed":
			this.visible = false;
			break;
		default:
			this.visible = false;
			break;
	}
	if ( this.visible )
		$('wrkWorkplace').show();
	else
		$('wrkWorkplace').hide();
	i2b2.WORK.view.main.splitterDragged();
	//this.Resize(); // tdw9
}),'',i2b2.WORK.view.main);


// ================================================================================================== //
i2b2.events.changedZoomWindows.subscribe((function(eventTypeName, zoomMsg) {
	newMode = zoomMsg[0];
	if (!newMode.action) { return; }
	if (newMode.action == "ADD") {
		switch (newMode.window) {
			case "WORK":
				this.visible = true;
				this.isZoomed = true;
				break;
			case "ONT":
			case "HISTORY":
				this.visible = false;
				this.isZoomed = false;				
		}
	} else {
		switch (newMode.window) {
			case "WORK":
			case "ONT":
			case "HISTORY":
				this.isZoomed = false;
				this.visible = true;
		}
	}
	this.ResizeHeight();
	this.splitterDragged();
}),'',i2b2.WORK.view.main);




i2b2.WORK.view.main.Render = function(){
    var domContainer = $('wrkTreeview');
    domContainer.hide();
    while (domContainer.hasChildNodes()) {
        domContainer.removeChild(domContainer.lastChild);
    }
    domContainer.show();
};

i2b2.WORK.view.main.showOptions = function(){ alert('show options for Workplace'); }

i2b2.WORK.view.main._generateTvNode = function(title, nodeData, parentNode){
    var funcAddWrkNode = function(renderInfo){
        var id = "WRK_TV-" + i2b2.GUID();
        var retHtml = '<DIV id="' + id + '" style="white-space:nowrap;cursor:pointer;">';
        retHtml += '<DIV class= "' + renderInfo.cssClass + '" >';
        retHtml += '<IMG src="' + renderInfo.icon + '"/> ' + title;
        retHtml += '</DIV></DIV>';
        var render = {
            html: retHtml,
            nodeid: id
        };
        var tmpNode = new YAHOO.widget.HTMLNode(render, parentNode, false, true);
        
        var sdxDataNode = i2b2.sdx.Master.EncapsulateData('WRK', nodeData);
        
        tmpNode.data.i2b2_SDX = sdxDataNode;
        tmpNode.data.i2b2_NodeRenderData = renderInfo;
        tmpNode.toggle = function(){
            if (!this.tree.locked && (this.hasChildren(true))) {
                var data = this.data.i2b2_NodeRenderData;
                var img = this.getContentEl();
                img = Element.select(img, "img")[0];
                if (this.expanded) {
                    img.src = data.icon;
                    this.collapse();
                }
                else {
                    img.src = data.iconExp;
                    this.expand();
                }
            }
        };
        return tmpNode;
    };
    var render = {};
    switch (nodeData.visual) {
        case "CA":
            render.cssClass = "wrkRoot";
            render.canExpand = true;
            render.iconType = "WRKROOT";
            render.icon = i2b2.hive.cfg.urlFramework + 'cells/WORK/assets/WORK_root.gif';
            render.iconExp = i2b2.hive.cfg.urlFramework + 'cells/WORK/assets/WORK_root_exp.gif';
            var renderObj = funcAddWrkNode(render);
            var id = renderObj.data.nodeid;
            var ddProxy = i2b2.sdx.Master.Attach2Data(id, "WRK", id);
            ddProxy.yuiTreeNode = renderObj;
            var optDD = {
                dropTarget: true
            };
			i2b2.sdx.Master.AttachType(id, "QM", optDD);
			i2b2.sdx.Master.AttachType(id, "PRC", optDD);
			i2b2.sdx.Master.AttachType(id, "PRS", optDD);
			i2b2.sdx.Master.AttachType(id, "ENS", optDD);
			i2b2.sdx.Master.AttachType(id, "PR", optDD);
			i2b2.sdx.Master.AttachType(id, "CONCPT", optDD);
			i2b2.sdx.Master.AttachType(id, "QDEF", optDD);
			i2b2.sdx.Master.AttachType(id, "QGDEF", optDD);
			i2b2.sdx.Master.AttachType(id, "XML", optDD);
			i2b2.sdx.Master.AttachType(id, "WRK", optDD);
            
			i2b2.sdx.Master.setHandlerCustom(id, "QM", "DropHandler", i2b2.WORK.ctrlr.main.HandleDrop);
			i2b2.sdx.Master.setHandlerCustom(id, "PRC", "DropHandler", i2b2.WORK.ctrlr.main.HandleDrop);
			i2b2.sdx.Master.setHandlerCustom(id, "PRS", "DropHandler", i2b2.WORK.ctrlr.main.HandleDrop);
			i2b2.sdx.Master.setHandlerCustom(id, "ENS", "DropHandler", i2b2.WORK.ctrlr.main.HandleDrop);
			i2b2.sdx.Master.setHandlerCustom(id, "PR", "DropHandler", i2b2.WORK.ctrlr.main.HandleDrop);
			i2b2.sdx.Master.setHandlerCustom(id, "CONCPT", "DropHandler", i2b2.WORK.ctrlr.main.HandleDrop);
			i2b2.sdx.Master.setHandlerCustom(id, "QDEF", "DropHandler", i2b2.WORK.ctrlr.main.HandleDrop);
			i2b2.sdx.Master.setHandlerCustom(id, "QGDEF", "DropHandler", i2b2.WORK.ctrlr.main.HandleDrop);
			i2b2.sdx.Master.setHandlerCustom(id, "XML", "DropHandler", i2b2.WORK.ctrlr.main.HandleDrop);
			i2b2.sdx.Master.setHandlerCustom(id, "WRK", "DropHandler", i2b2.WORK.ctrlr.main.HandleDrop);
			
			i2b2.sdx.Master.setHandlerCustom(id, "QM", "onHoverOver", i2b2.WORK.view.main.ddHoverOver);
			i2b2.sdx.Master.setHandlerCustom(id, "PRC", "onHoverOver", i2b2.WORK.view.main.ddHoverOver);
			i2b2.sdx.Master.setHandlerCustom(id, "PRS", "onHoverOver", i2b2.WORK.view.main.ddHoverOver);
			i2b2.sdx.Master.setHandlerCustom(id, "ENS", "onHoverOver", i2b2.WORK.view.main.ddHoverOver);
			i2b2.sdx.Master.setHandlerCustom(id, "PR", "onHoverOver", i2b2.WORK.view.main.ddHoverOver);
			i2b2.sdx.Master.setHandlerCustom(id, "CONCPT", "onHoverOver", i2b2.WORK.view.main.ddHoverOver);
			i2b2.sdx.Master.setHandlerCustom(id, "QDEF", "onHoverOver", i2b2.WORK.view.main.ddHoverOver);
			i2b2.sdx.Master.setHandlerCustom(id, "QGDEF", "onHoverOver", i2b2.WORK.view.main.ddHoverOver);
			i2b2.sdx.Master.setHandlerCustom(id, "XML", "onHoverOver", i2b2.WORK.view.main.ddHoverOver);
			i2b2.sdx.Master.setHandlerCustom(id, "WRK", "onHoverOver", i2b2.WORK.view.main.ddHoverOver);
			
			i2b2.sdx.Master.setHandlerCustom(id, "QM", "onHoverOut", i2b2.WORK.view.main.ddHoverOut);
			i2b2.sdx.Master.setHandlerCustom(id, "PRC", "onHoverOut", i2b2.WORK.view.main.ddHoverOut);
			i2b2.sdx.Master.setHandlerCustom(id, "PRS", "onHoverOut", i2b2.WORK.view.main.ddHoverOut);
			i2b2.sdx.Master.setHandlerCustom(id, "ENS", "onHoverOut", i2b2.WORK.view.main.ddHoverOut);
			i2b2.sdx.Master.setHandlerCustom(id, "PR", "onHoverOut", i2b2.WORK.view.main.ddHoverOut);
			i2b2.sdx.Master.setHandlerCustom(id, "CONCPT", "onHoverOut", i2b2.WORK.view.main.ddHoverOut);
			i2b2.sdx.Master.setHandlerCustom(id, "QDEF", "onHoverOut", i2b2.WORK.view.main.ddHoverOut);
			i2b2.sdx.Master.setHandlerCustom(id, "QGDEF", "onHoverOut", i2b2.WORK.view.main.ddHoverOut);
			i2b2.sdx.Master.setHandlerCustom(id, "XML", "onHoverOut", i2b2.WORK.view.main.ddHoverOut);
			i2b2.sdx.Master.setHandlerCustom(id, "WRK", "onHoverOut", i2b2.WORK.view.main.ddHoverOut);
            break;
        case "FA":
            render.cssClass = "wrkFolder";
            render.canExpand = true;
            render.iconType = "WRKFOLDER";
            render.icon = i2b2.hive.cfg.urlFramework + 'cells/WORK/assets/WORK_folder.gif';
            render.iconExp = i2b2.hive.cfg.urlFramework + 'cells/WORK/assets/WORK_folder_exp.gif';
            var renderObj = funcAddWrkNode(render);
            
            var id = renderObj.data.nodeid;
            var ddProxy = i2b2.sdx.Master.Attach2Data(id, "WRK", id);
            ddProxy.yuiTreeNode = renderObj;
            var optDD = {
                dropTarget: true
            };
			i2b2.sdx.Master.AttachType(id, "QM", optDD);
			i2b2.sdx.Master.AttachType(id, "PRC", optDD);
			i2b2.sdx.Master.AttachType(id, "PRS", optDD);
			i2b2.sdx.Master.AttachType(id, "ENS", optDD);
			i2b2.sdx.Master.AttachType(id, "PR", optDD);
			i2b2.sdx.Master.AttachType(id, "CONCPT", optDD);
			i2b2.sdx.Master.AttachType(id, "QDEF", optDD);
			i2b2.sdx.Master.AttachType(id, "QGDEF", optDD);
			i2b2.sdx.Master.AttachType(id, "XML", optDD);
			i2b2.sdx.Master.AttachType(id, "WRK", optDD);
			
			i2b2.sdx.Master.setHandlerCustom(id, "QM", "DropHandler", i2b2.WORK.ctrlr.main.HandleDrop);
			i2b2.sdx.Master.setHandlerCustom(id, "PRC", "DropHandler", i2b2.WORK.ctrlr.main.HandleDrop);
			i2b2.sdx.Master.setHandlerCustom(id, "PRS", "DropHandler", i2b2.WORK.ctrlr.main.HandleDrop);
			i2b2.sdx.Master.setHandlerCustom(id, "ENS", "DropHandler", i2b2.WORK.ctrlr.main.HandleDrop);
			i2b2.sdx.Master.setHandlerCustom(id, "PR", "DropHandler", i2b2.WORK.ctrlr.main.HandleDrop);
			i2b2.sdx.Master.setHandlerCustom(id, "CONCPT", "DropHandler", i2b2.WORK.ctrlr.main.HandleDrop);
			i2b2.sdx.Master.setHandlerCustom(id, "QDEF", "DropHandler", i2b2.WORK.ctrlr.main.HandleDrop);
			i2b2.sdx.Master.setHandlerCustom(id, "QGDEF", "DropHandler", i2b2.WORK.ctrlr.main.HandleDrop);
			i2b2.sdx.Master.setHandlerCustom(id, "XML", "DropHandler", i2b2.WORK.ctrlr.main.HandleDrop);
			i2b2.sdx.Master.setHandlerCustom(id, "WRK", "DropHandler", i2b2.WORK.ctrlr.main.HandleDrop);
			
			i2b2.sdx.Master.setHandlerCustom(id, "QM", "onHoverOver", i2b2.WORK.view.main.ddHoverOver);
			i2b2.sdx.Master.setHandlerCustom(id, "PRC", "onHoverOver", i2b2.WORK.view.main.ddHoverOver);
			i2b2.sdx.Master.setHandlerCustom(id, "PRS", "onHoverOver", i2b2.WORK.view.main.ddHoverOver);
			i2b2.sdx.Master.setHandlerCustom(id, "ENS", "onHoverOver", i2b2.WORK.view.main.ddHoverOver);
			i2b2.sdx.Master.setHandlerCustom(id, "PR", "onHoverOver", i2b2.WORK.view.main.ddHoverOver);
			i2b2.sdx.Master.setHandlerCustom(id, "CONCPT", "onHoverOver", i2b2.WORK.view.main.ddHoverOver);
			i2b2.sdx.Master.setHandlerCustom(id, "QDEF", "onHoverOver", i2b2.WORK.view.main.ddHoverOver);
			i2b2.sdx.Master.setHandlerCustom(id, "QGDEF", "onHoverOver", i2b2.WORK.view.main.ddHoverOver);
			i2b2.sdx.Master.setHandlerCustom(id, "XML", "onHoverOver", i2b2.WORK.view.main.ddHoverOver);
			i2b2.sdx.Master.setHandlerCustom(id, "WRK", "onHoverOver", i2b2.WORK.view.main.ddHoverOver);
			
			i2b2.sdx.Master.setHandlerCustom(id, "QM", "onHoverOut", i2b2.WORK.view.main.ddHoverOut);
			i2b2.sdx.Master.setHandlerCustom(id, "PRC", "onHoverOut", i2b2.WORK.view.main.ddHoverOut);
			i2b2.sdx.Master.setHandlerCustom(id, "PRS", "onHoverOut", i2b2.WORK.view.main.ddHoverOut);
			i2b2.sdx.Master.setHandlerCustom(id, "ENS", "onHoverOut", i2b2.WORK.view.main.ddHoverOut);
			i2b2.sdx.Master.setHandlerCustom(id, "PR", "onHoverOut", i2b2.WORK.view.main.ddHoverOut);
			i2b2.sdx.Master.setHandlerCustom(id, "CONCPT", "onHoverOut", i2b2.WORK.view.main.ddHoverOut);
			i2b2.sdx.Master.setHandlerCustom(id, "QDEF", "onHoverOut", i2b2.WORK.view.main.ddHoverOut);
			i2b2.sdx.Master.setHandlerCustom(id, "QGDEF", "onHoverOut", i2b2.WORK.view.main.ddHoverOut);
			i2b2.sdx.Master.setHandlerCustom(id, "XML", "onHoverOut", i2b2.WORK.view.main.ddHoverOut);
			i2b2.sdx.Master.setHandlerCustom(id, "WRK", "onHoverOut", i2b2.WORK.view.main.ddHoverOut);
            break;
        case "ZA":
            // create a new WORK SDX object
            var o = nodeData;
            o.index = nodeData.key;
            try {
                var sdxDataNode = i2b2.sdx.Master.EncapsulateData('WRK', o);
                var sdxRenderData = i2b2.sdx.Master.RenderHTML(parentNode.tree.id, sdxDataNode, {
                    'title': title
                });
				if (!sdxRenderData) { break; }
                var renderObj = i2b2.sdx.Master.AppendTreeNode(parentNode.tree, parentNode, sdxRenderData);
                renderObj.data.i2b2_SDX = sdxDataNode;
                var id = renderObj.data.nodeid;
                var ddProxy = i2b2.sdx.Master.Attach2Data(id, "WRK", id);
                // attach encapsulated data type as well
                ddProxy.addToGroup(sdxDataNode.sdxUnderlyingPackage.sdxInfo.sdxType);
                ddProxy.yuiTreeNode = renderObj;
                ddProxy.config.isTarget = false;
                ddProxy.isTarget = false;
            } 
            catch (e) {
            }
    }
    return renderObj;
}


i2b2.WORK.view.main.ddHoverOver = function(e, id, ddProxy){
    var el = $(ddProxy.getDragEl());
    if (el) {
        Element.addClassName(el, "ddDropToWorkplace");
    }
};

i2b2.WORK.view.main.ddHoverOut = function(e, id, ddProxy){
    var el = $(ddProxy.getDragEl());
    if (el) {
        Element.removeClassName(el, "ddDropToWorkplace");
    }
};


// ================================================================================================== //
i2b2.WORK.view.main.ContextMenuRouter = function(a1, a2, a3){
    var ctxData = i2b2.WORK.view.main.contextTvNode;
    switch (a3) {
        case 'newFolder':
            i2b2.WORK.ctrlr.main.NewFolder(ctxData);
            break;
        case 'rename':
            i2b2.WORK.ctrlr.main.Rename(ctxData);
            break;
        case 'annotate':
            i2b2.WORK.ctrlr.main.Annotate(ctxData);
            break;
        case 'delete':
            i2b2.WORK.ctrlr.main.Delete(ctxData);
            break;
    }
}

// ================================================================================================== //
i2b2.WORK.view.main.ContextMenuPreprocess = function(p_oEvent){
    var clickId = null;
    var currentNode = this.contextEventTarget;
    var doNotShow = false;
    while (!currentNode.id) {
        if (currentNode.parentNode) {
            currentNode = currentNode.parentNode;
        }
        else {
            // we have recursed up the tree to the window/document DOM... it's a bad click
            this.cancel();
            return;
        }
    }
    clickId = currentNode.id;
    // see if the ID maps back to a treenode with SDX data
    var tvNode = i2b2.WORK.view.main.yuiTree.getNodeByProperty('nodeid', clickId);
    if (tvNode) {
        i2b2.WORK.view.main.contextTvNode = tvNode;
        // custom build the context menu according to the concept that was clicked
        var mil = [];
        var op = i2b2.WORK.view.main.ContextMenuRouter;
        if (p_oEvent == "beforeShow") {
            switch (tvNode.data.i2b2_SDX.origData.visual) {
                case "CA":
                    // root node
                    mil.push({text: "New Folder",	onclick: {fn: op,obj: 'newFolder'}	});
                    break;
                case "FA":
                    // folder node
                    mil.push({text: "Rename",		onclick: {fn: op, obj: 'rename'}	});
                    mil.push({text: "Annotate",	onclick: {fn: op, obj: 'annotate'}	});
                    mil.push({text: "Delete",		onclick: {fn: op, obj: 'delete'}	});
                    mil.push({text: "New Folder",	onclick: {fn: op, obj: 'newFolder'}	});
                    break;
                case "ZA":
                    // data saved to workplace
                    mil.push({text: "Rename",		onclick: {fn: op, obj: 'rename'}	});
                    mil.push({text: "Annotate",	onclick: {fn: op, obj: 'annotate'}	});
                    mil.push({text: "Delete",		onclick: {fn: op, obj: 'delete'}	});
                    break;
                default:
                    doNotShow = true;
            }
            if (!doNotShow) {
                i2b2.WORK.view.main.ContextMenu.clearContent();
                i2b2.WORK.view.main.ContextMenu.addItems(mil);
                i2b2.WORK.view.main.ContextMenu.render();
            }
        }
    }
    else {
        doNotShow = true;
    }
    if (doNotShow) {
        if (p_oEvent == "beforeShow") {
            i2b2.WORK.view.main.ContextMenu.clearContent();
        }
        if (p_oEvent == "triggerContextMenu") {
            this.cancel();
        }
    }
};

i2b2.WORK.view.main.TreeviewLoader = function(tv_node, onCompleteCallback){
    if (Object.isUndefined(tv_node.data.i2b2_SDX)) {
        console.error('i2b2.WORK.view.main.TreeviewLoader could not find tv_node.data.i2b2_SDX');
        onCompleteCallback();
    }
    // create callback display routine
    var scopedCallback = new i2b2_scopedCallback();
    scopedCallback.scope = i2b2.WORK;
    scopedCallback.callback = function(results){
		i2b2.WORK.view.main.queryResponse = results.msgResponse;
		i2b2.WORK.view.main.queryRequest = results.msgRequest;
        var cl_yuiCallback = onCompleteCallback;
        var cl_tvParentNode = tv_node;
        var nlst = i2b2.h.XPath(results.refXML, "//folder[name and share_id and index and visual_attributes]");
        for (var i = 0; i < nlst.length; i++) {
            var s = nlst[i];
            var nodeData = {};
            nodeData.xmlOrig = s;
            nodeData.index = i2b2.h.getXNodeVal(s, "index");
            nodeData.key = nodeData.index;
            nodeData.name = i2b2.h.getXNodeVal(s, "folder/name");
            nodeData.annotation = i2b2.h.getXNodeVal(s, "tooltip");
            nodeData.share_id = i2b2.h.getXNodeVal(s, "share_id");
            nodeData.visual = String(i2b2.h.getXNodeVal(s, "visual_attributes")).strip();
            nodeData.encapType = i2b2.h.getXNodeVal(s, "work_xml_i2b2_type");
            nodeData.isRoot = false;
            // create new root node
            var tmpNode = i2b2.WORK.view.main._generateTvNode(nodeData.name, nodeData, cl_tvParentNode);
        }
        // render tree
        cl_yuiCallback();
    };
    // ajax communicator call
    var varInput = {
        parent_key_value: tv_node.data.i2b2_SDX.sdxInfo.sdxKeyValue,
		result_wait_time: 180
    };
    i2b2.WORK.ajax.getChildren("WORK:Workplace", varInput, scopedCallback);
}


i2b2.WORK.view.main.DropHandler = function(a1, a2, a3, a4, a5, a6){
    alert("i2b2.WORK.view.main.DropHandler() received a drop event");
}

i2b2.WORK.view.main.refreshTree = function() {

	var thisview = i2b2.WORK.view.main;
    // initialize treeview
	thisview.yuiTree = null;
    if (!thisview.yuiTree) {
        thisview.yuiTree = new YAHOO.widget.TreeView("wrkTreeview");
        thisview.yuiTree.setDynamicLoad(i2b2.WORK.view.main.TreeviewLoader, 1);
        var yuiRootNode = thisview.yuiTree.getRoot();
        // register the treeview with the SDX subsystem to be a container for QM, QI, PRS, CONCPT and WORK objects
        var optDD = {
            dropTarget: false
        };
        i2b2.sdx.Master.AttachType("wrkTreeview", "QM", optDD);
        i2b2.sdx.Master.AttachType("wrkTreeview", "QI");
        i2b2.sdx.Master.AttachType("wrkTreeview", "PRC", optDD);
        i2b2.sdx.Master.AttachType("wrkTreeview", "PRS", optDD);
        i2b2.sdx.Master.AttachType("wrkTreeview", "ENS", optDD);
        i2b2.sdx.Master.AttachType("wrkTreeview", "PR", optDD);
        i2b2.sdx.Master.AttachType("wrkTreeview", "CONCPT", optDD);
        i2b2.sdx.Master.AttachType("wrkTreeview", "QDEF", optDD);
        i2b2.sdx.Master.AttachType("wrkTreeview", "QGDEF", optDD);
        i2b2.sdx.Master.AttachType("wrkTreeview", "XML", optDD);
        i2b2.sdx.Master.AttachType("wrkTreeview", "WRK");
        
        var funcNull = function(){
            return true;
        };
        i2b2.sdx.Master.setHandlerCustom("wrkTreeview", "QM", "LoadChildrenFromTreeview", funcNull);
        i2b2.sdx.Master.setHandlerCustom("wrkTreeview", "PRC", "LoadChildrenFromTreeview", funcNull);
        i2b2.sdx.Master.setHandlerCustom("wrkTreeview", "PRS", "LoadChildrenFromTreeview", funcNull);
        i2b2.sdx.Master.setHandlerCustom("wrkTreeview", "ENS", "LoadChildrenFromTreeview", funcNull);
        i2b2.sdx.Master.setHandlerCustom("wrkTreeview", "PR", "LoadChildrenFromTreeview", funcNull);
        i2b2.sdx.Master.setHandlerCustom("wrkTreeview", "CONCPT", "LoadChildrenFromTreeview", funcNull)
;
        i2b2.sdx.Master.setHandlerCustom("wrkTreeview", "QDEF", "LoadChildrenFromTreeview", funcNull);
        i2b2.sdx.Master.setHandlerCustom("wrkTreeview", "QGDEF", "LoadChildrenFromTreeview", funcNull);
        i2b2.sdx.Master.setHandlerCustom("wrkTreeview", "XML", "LoadChildrenFromTreeview", funcNull);
        i2b2.sdx.Master.setHandlerCustom("wrkTreeview", "WRK", "LoadChildrenFromTreeview", funcNull);
        
        i2b2.sdx.Master.setHandlerCustom("wrkTreeview", "QM", "DropHandler", i2b2.WORK.view.main.DropHandler);
        i2b2.sdx.Master.setHandlerCustom("wrkTreeview", "PRC", "DropHandler", i2b2.WORK.view.main.DropHandler);
        i2b2.sdx.Master.setHandlerCustom("wrkTreeview", "PRS", "DropHandler", i2b2.WORK.view.main.DropHandler);
        i2b2.sdx.Master.setHandlerCustom("wrkTreeview", "ENS", "DropHandler", i2b2.WORK.view.main.DropHandler);
        i2b2.sdx.Master.setHandlerCustom("wrkTreeview", "PR", "DropHandler", i2b2.WORK.view.main.DropHandler);
        i2b2.sdx.Master.setHandlerCustom("wrkTreeview", "CONCPT", "DropHandler", i2b2.WORK.view.main.DropHandler);
        i2b2.sdx.Master.setHandlerCustom("wrkTreeview", "QDEF", "DropHandler", i2b2.WORK.view.main.DropHandler);
        i2b2.sdx.Master.setHandlerCustom("wrkTreeview", "QGDEF", "DropHandler", i2b2.WORK.view.main.DropHandler);
        i2b2.sdx.Master.setHandlerCustom("wrkTreeview", "XML", "DropHandler", i2b2.WORK.view.main.DropHandler);
        i2b2.sdx.Master.setHandlerCustom("wrkTreeview", "WRK", "DropHandler", i2b2.WORK.view.main.DropHandler);
        
        // create initial loader display routine
        var scopedCallback = new i2b2_scopedCallback();
        scopedCallback.scope = i2b2.WORK;
        scopedCallback.callback = function(results){
			i2b2.WORK.view.main.queryResponse = results.msgResponse;
			i2b2.WORK.view.main.queryRequest = results.msgRequest;
            var nlst = i2b2.h.XPath(results.refXML, "//folder[name and share_id and index and visual_attributes]");
            var yuiRoot = i2b2.WORK.view.main.yuiTree.getRoot();
            for (var i = 0; i < nlst.length; i++) {
                var s = nlst[i];
                var nodeData = {};
                nodeData.xmlOrig = s;
                nodeData.index = i2b2.h.getXNodeVal(s, "index");
                nodeData.key = nodeData.index;
                nodeData.name = i2b2.h.getXNodeVal(s, "name");
                nodeData.annotation = i2b2.h.getXNodeVal(s, "tooltip");
                nodeData.share_id = i2b2.h.getXNodeVal(s, "share_id");
                nodeData.visual = String(i2b2.h.getXNodeVal(s, "visual_attributes")).strip();
                nodeData.encapType = i2b2.h.getXNodeVal(s, "work_xml_i2b2_type");
                nodeData.isRoot = true;
                // create new root node
                i2b2.WORK.view.main._generateTvNode(nodeData.name, nodeData, yuiRoot);
            }
            // render tree
            i2b2.WORK.view.main.yuiTree.draw();
			$('refreshWorkImg').src = "assets/images/refreshButton.JPG";

        };
				$('refreshWorkImg').src="assets/images/spin.gif";		

        // ajax communicator call
		if (i2b2.PM.model.userRoles.indexOf("MANAGER") == -1) {		
			i2b2.WORK.ajax.getFoldersByUserId("WORK:Workplace", {}, scopedCallback);
		} else {
	        i2b2.WORK.ajax.getFoldersByProject("WORK:Workplace", {}, scopedCallback);
		}
    }
    // -------------------------------------------------------
    i2b2.WORK.view.main.ContextMenu = new YAHOO.widget.ContextMenu("divContextMenu-Workplace", {
		zIndex: 5000,
        lazyload: true,
        trigger: $('wrkTreeview'),
        itemdata: []
    });
	
    i2b2.WORK.view.main.ContextMenu.subscribe("triggerContextMenu", i2b2.WORK.view.main.ContextMenuPreprocess);
    i2b2.WORK.view.main.ContextMenu.subscribe("beforeShow", i2b2.WORK.view.main.ContextMenuPreprocess);

}




// =========================================================
i2b2.events.afterCellInit.subscribe((function(en, co){
    if (co[0].cellCode.indexOf("WORK") < 0) {
        return;
	}
	console.debug('[EVENT CAPTURED i2b2.events.afterLogin]');
	var thisview = i2b2.WORK.view.main;
	thisview.visible = true;
	thisview.Resize();
}));
console.info("SUBSCRIBED TO i2b2.events.afterCellInit");
// =========================================================


// =========================================================			
i2b2.events.afterLogin.subscribe((function(en, co){
    console.debug('[EVENT CAPTURED i2b2.events.afterLogin]');
	var thisview = i2b2.WORK.view.main;
    // initialize treeview
    if (!thisview.yuiTree) {
        thisview.yuiTree = new YAHOO.widget.TreeView("wrkTreeview");
        thisview.yuiTree.setDynamicLoad(i2b2.WORK.view.main.TreeviewLoader, 1);
        var yuiRootNode = thisview.yuiTree.getRoot();
        // register the treeview with the SDX subsystem to be a container for QM, QI, PRS, CONCPT and WORK objects
        var optDD = {
            dropTarget: false
        };
        i2b2.sdx.Master.AttachType("wrkTreeview", "QM", optDD);
        i2b2.sdx.Master.AttachType("wrkTreeview", "QI");
        i2b2.sdx.Master.AttachType("wrkTreeview", "PRC", optDD);
        i2b2.sdx.Master.AttachType("wrkTreeview", "PRS", optDD);
        i2b2.sdx.Master.AttachType("wrkTreeview", "ENS", optDD);
        i2b2.sdx.Master.AttachType("wrkTreeview", "PR", optDD);
        i2b2.sdx.Master.AttachType("wrkTreeview", "CONCPT", optDD);
        i2b2.sdx.Master.AttachType("wrkTreeview", "QDEF", optDD);
        i2b2.sdx.Master.AttachType("wrkTreeview", "QGDEF", optDD);
        i2b2.sdx.Master.AttachType("wrkTreeview", "XML", optDD);
        i2b2.sdx.Master.AttachType("wrkTreeview", "WRK");
        
        var funcNull = function(){
            return true;
        };
        i2b2.sdx.Master.setHandlerCustom("wrkTreeview", "QM", "LoadChildrenFromTreeview", funcNull);
        i2b2.sdx.Master.setHandlerCustom("wrkTreeview", "PRC", "LoadChildrenFromTreeview", funcNull);
        i2b2.sdx.Master.setHandlerCustom("wrkTreeview", "PRS", "LoadChildrenFromTreeview", funcNull);
        i2b2.sdx.Master.setHandlerCustom("wrkTreeview", "ENS", "LoadChildrenFromTreeview", funcNull);
        i2b2.sdx.Master.setHandlerCustom("wrkTreeview", "PR", "LoadChildrenFromTreeview", funcNull);
        i2b2.sdx.Master.setHandlerCustom("wrkTreeview", "CONCPT", "LoadChildrenFromTreeview", funcNull);
        i2b2.sdx.Master.setHandlerCustom("wrkTreeview", "QDEF", "LoadChildrenFromTreeview", funcNull);
        i2b2.sdx.Master.setHandlerCustom("wrkTreeview", "QGDEF", "LoadChildrenFromTreeview", funcNull);
        i2b2.sdx.Master.setHandlerCustom("wrkTreeview", "XML", "LoadChildrenFromTreeview", funcNull);
        i2b2.sdx.Master.setHandlerCustom("wrkTreeview", "WRK", "LoadChildrenFromTreeview", funcNull);
        
        i2b2.sdx.Master.setHandlerCustom("wrkTreeview", "QM", "DropHandler", i2b2.WORK.view.main.DropHandler);
        i2b2.sdx.Master.setHandlerCustom("wrkTreeview", "PRC", "DropHandler", i2b2.WORK.view.main.DropHandler);
        i2b2.sdx.Master.setHandlerCustom("wrkTreeview", "PRS", "DropHandler", i2b2.WORK.view.main.DropHandler);
        i2b2.sdx.Master.setHandlerCustom("wrkTreeview", "ENS", "DropHandler", i2b2.WORK.view.main.DropHandler);
        i2b2.sdx.Master.setHandlerCustom("wrkTreeview", "PR", "DropHandler", i2b2.WORK.view.main.DropHandler);
        i2b2.sdx.Master.setHandlerCustom("wrkTreeview", "CONCPT", "DropHandler", i2b2.WORK.view.main.DropHandler);
        i2b2.sdx.Master.setHandlerCustom("wrkTreeview", "QDEF", "DropHandler", i2b2.WORK.view.main.DropHandler);
        i2b2.sdx.Master.setHandlerCustom("wrkTreeview", "QGDEF", "DropHandler", i2b2.WORK.view.main.DropHandler);
        i2b2.sdx.Master.setHandlerCustom("wrkTreeview", "XML", "DropHandler", i2b2.WORK.view.main.DropHandler);
        i2b2.sdx.Master.setHandlerCustom("wrkTreeview", "WRK", "DropHandler", i2b2.WORK.view.main.DropHandler);
        
        // create initial loader display routine
        var scopedCallback = new i2b2_scopedCallback();
        scopedCallback.scope = i2b2.WORK;
        scopedCallback.callback = function(results){
			i2b2.WORK.view.main.queryResponse = results.msgResponse;
			i2b2.WORK.view.main.queryRequest = results.msgRequest;
            var nlst = i2b2.h.XPath(results.refXML, "//folder[name and share_id and index and visual_attributes]");
            var yuiRoot = i2b2.WORK.view.main.yuiTree.getRoot();
            for (var i = 0; i < nlst.length; i++) {
                var s = nlst[i];
                var nodeData = {};
                nodeData.xmlOrig = s;
                nodeData.index = i2b2.h.getXNodeVal(s, "index");
                nodeData.key = nodeData.index;
                nodeData.name = i2b2.h.getXNodeVal(s, "name");
                nodeData.annotation = i2b2.h.getXNodeVal(s, "tooltip");
                nodeData.share_id = i2b2.h.getXNodeVal(s, "share_id");
                nodeData.visual = String(i2b2.h.getXNodeVal(s, "visual_attributes")).strip();
                nodeData.encapType = i2b2.h.getXNodeVal(s, "work_xml_i2b2_type");
                nodeData.isRoot = true;
                // create new root node
                i2b2.WORK.view.main._generateTvNode(nodeData.name, nodeData, yuiRoot);
            }
            // render tree
            i2b2.WORK.view.main.yuiTree.draw();
        };
        // ajax communicator call
		if (i2b2.PM.model.userRoles.indexOf("MANAGER") == -1) {		
			i2b2.WORK.ajax.getFoldersByUserId("WORK:Workplace", {}, scopedCallback);
		} else {
	        i2b2.WORK.ajax.getFoldersByProject("WORK:Workplace", {}, scopedCallback);
		}
    }
    // -------------------------------------------------------
    i2b2.WORK.view.main.ContextMenu = new YAHOO.widget.ContextMenu("divContextMenu-Workplace", {
		zIndex: 5000,
        lazyload: true,
        trigger: $('wrkTreeview'),
        itemdata: []
    });
    i2b2.WORK.view.main.ContextMenu.subscribe("triggerContextMenu", i2b2.WORK.view.main.ContextMenuPreprocess);
    i2b2.WORK.view.main.ContextMenu.subscribe("beforeShow", i2b2.WORK.view.main.ContextMenuPreprocess);
}));
console.info("SUBSCRIBED TO i2b2.events.afterCellInit");
// =========================================================



console.timeEnd('execute time');
console.groupEnd();
