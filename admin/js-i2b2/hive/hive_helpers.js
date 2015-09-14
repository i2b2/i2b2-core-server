/**
 * @projectDescription	Various helper functions used by the i2b2 framework and cells.
 * @inherits 	i2b2
 * @namespace	i2b2
 * @author		Nick Benik, Griffin Weber MD PhD
 * @version 	1.3
 * ----------------------------------------------------------------------------------------
 * updated 9-15-08: RC4 launch [Nick Benik] 
 */
console.group('Load & Execute component file: hive > helpers');
console.time('execute time');


if (Object.isUndefined(i2b2.h)) i2b2.h = {};
// ================================================================================================== //
// Hack PrototypeJS to accept multiple parameters for its Object.isUndefined function call
// (utilize a closure to store a protected reference to prototype's original Object.isUndefined object)
i2b2.protoObjhack = (function() {
	var origFunction = Object.isUndefined;
	var newFunction = function(){
		for(var i=0; i<arguments.length; i++) {
			if (origFunction(arguments[i])) { return true; }
		}
		return false;
	};
	return newFunction;
});
Object.isUndefined = i2b2.protoObjhack();
delete i2b2.protoObjhack;


// ================================================================================================== //
i2b2.h.parseXml = function(xmlString){
	var xmlDocRet = false;
	var isActiveXSupported = false;

	try { new ActiveXObject ("MSXML2.DOMDocument.6.0"); isActiveXSupported =  true; } catch (e) { isActiveXSupported =  false; }

	if (isActiveXSupported) {
		//Internet Explorer
		xmlDocRet = new ActiveXObject("Microsoft.XMLDOM");
		xmlDocRet.async = "false";
		xmlDocRet.loadXML(xmlString);
		xmlDocRet.setProperty("SelectionLanguage", "XPath");
	} else {
		//Firefox, Mozilla, Opera, etc.
		parser = new DOMParser();
		xmlDocRet = parser.parseFromString(xmlString, "text/xml");
	}

	return xmlDocRet;
};


i2b2.h.XPath = function(xmlDoc, xPath) {
	var retArray = [];
	if (!xmlDoc) { 
		console.warn("An invalid XMLDoc was passed to i2b2.h.XPath");
		return retArray;
	}
	try {
		if (window.ActiveXObject  || "ActiveXObject" in window) {
			// Microsoft's XPath implementation
			// HACK: setProperty attempts execution when placed in IF statements' test condition, forced to use try-catch
			try {  
				xmlDoc.setProperty("SelectionLanguage", "XPath");
			} catch(e) {
				try {
					xmlDoc.ownerDocument.setProperty("SelectionLanguage", "XPath");
				} catch(e) {}
			} 
			retArray = xmlDoc.selectNodes(xPath);
		}
		else if (document.implementation && document.implementation.createDocument) {
			// W3C XPath implementation (Internet standard)
			var ownerDoc = xmlDoc.ownerDocument;
			if (!ownerDoc) {ownerDoc = xmlDoc; }
			var nodes = ownerDoc.evaluate(xPath, xmlDoc, null, XPathResult.ANY_TYPE, null);
			var rec = nodes.iterateNext();
			while (rec) {
				retArray.push(rec);
				rec = nodes.iterateNext();
			}
		}
	} catch (e) {
		console.error("An error occurred while trying to perform XPath query.");
		console.dir(e);
	}
	return retArray;
};

i2b2.h.getXNodeVal = function(xmlElement, nodeName, includeChildren) {
	var gotten = i2b2.h.XPath(xmlElement, "descendant-or-self::"+nodeName+"/text()");
	var final = "";
	if (gotten.length > 0) {
		if (includeChildren == true || includeChildren == true) {
			for (var i=0; i<gotten.length; i++) {
				final += gotten[i].nodeValue;
			}
		} else {
			for (var i=0; i<gotten.length; i++) {
				final += gotten[i].nodeValue;
			}
		}
	} else {
		final = undefined;
	}
	return final;
}

i2b2.h.GenerateAlphaNumId = function(ReqIdLength) {
	var IdLen = 0;
	var retId = "";
	while (IdLen <= ReqIdLength) {
		IdLen++;
		switch(Math.floor(Math.random() * 3)) {
			case 0:
				// add a number
				retId += Math.floor(Math.random() * 10).toString();
				break;
			case 1:
				// upper case letter
				retId += String.fromCharCode(Math.floor(Math.random() * 26)+65);
				break;
			case 2:
				// lower case letter
				retId += String.fromCharCode(Math.floor(Math.random() * 26)+97);				
				break;
		}
	}
	return retId;
};


i2b2.h.GenerateISO8601DateTime = function(inDate) {
	if (!inDate) { inDate = new Date(); }
	var year = inDate.getYear();
	if (year < 2000) year = year + 1900;
	var month = inDate.getMonth() + 1;
	var day = inDate.getDate();
	var hour = inDate.getHours();
	var hourUTC = inDate.getUTCHours();
	var diff = hour - hourUTC;
	var hourdifference = Math.abs(diff);
	var minute = inDate.getMinutes();
	var minuteUTC = inDate.getUTCMinutes();
	var minutedifference;
	var second = inDate.getSeconds();
	var timezone;
	if (minute != minuteUTC && minuteUTC < 30 && diff < 0) { hourdifference--; }
	if (minute != minuteUTC && minuteUTC > 30 && diff > 0) { hourdifference--; }
	if (minute != minuteUTC) {
		minutedifference = ":30";
	} else {
		minutedifference = ":00";
	}
	if (hourdifference < 10) {
		timezone = "0" + hourdifference + minutedifference;
	} else {
		timezone = "" + hourdifference + minutedifference;
	}
	if (diff < 0) {
		timezone = "-" + timezone;
	} else {
		timezone = "+" + timezone;
	}
	if (month <= 9) month = "0" + month;
	if (day <= 9) day = "0" + day;
	if (hour <= 9) hour = "0" + hour;
	if (minute <= 9) minute = "0" + minute;
	if (second <= 9) second = "0" + second;
	return (year + "-" + month + "-" + day + "T" + hour + ":" + minute + ":" + second + timezone);
};


i2b2.h.Escape = function(inStrValue) {
	if (typeof inStrValue == "number") {
		var t = inStrValue.toString();
	} else {
		var t = new String(inStrValue);
	}
	t = t.replace(/&/g, "&amp;");
	t = t.replace(/</g, "&lt;");
	t = t.replace(/>/g, "&gt;");
	return t;
};

i2b2.h.Unescape = function(inStrValue) {
	var t = new String(inStrValue);
	t = t.replace(/&gt;/g, ">");
	t = t.replace(/&lt;/g, "<");
	t = t.replace(/&amp;/g, "&");
	return t;
};

i2b2.h.EscapeTemplateVars = function(refTemplateVals, arryIgnoreVars) {
	for (var vname in refTemplateVals) {
		var ignore = false;
		for (var i=0; i<arryIgnoreVars.length; i++) {
			if (vname == arryIgnoreVars[i]) {
				ignore = true;
				break;
			}
		}
		if (!ignore) {
			// escaping value
			refTemplateVals[vname] = this.Escape(refTemplateVals[vname]);
		}
	}
};

i2b2.h.LoadingMask = {
	show: function() {
		var sz = document.viewport.getDimensions();

    		var w =  window.innerWidth || (window.document.documentElement.clientWidth || window.document.body.clientWidth);
    		var h =  window.innerHeight || (window.document.documentElement.clientHeight || window.document.body.clientHeight);

		if (w < 840) {w = 840;}
		if (h < 517) {h = 517;}
		var mn = $('topMask');
		mn.style.width=w-10;
		mn.style.height=h-10;
		mn.innerHTML = "<TABLE height='100%' width='100%'><TR><TD align='center' valign='center'><BR/><H1><FONT size='12' color='white'>LOADING</FONT></H1></TD></TR></TABLE>";
		mn.zindex = 50000;
//		mn.style.cursor = 'wait';
		mn.show();
	},
	hide: function() {
		// hide the loading mask
		var mn = $('topMask');
		mn.innerHTML='';
		mn.hide();
	}
};


// created this object to allow the joining of forked execution paths (Waiting for Multiple AJAX calls)
i2b2.h.JoiningMutex = { 
	_contexts: {},
	_contextGenID: 0,
	_createContextProxy: function(contextRef) {
		// create a proxy object (via closure) to encapsulate data 
		// and route actions to the JoinMutex singleton
		var cl_JoinMutextRef = contextRef;
		function JoiningMutexContextProxy() {
			this._JoiningMutexContext = cl_JoinMutextRef;
			this._alreadyRun = false;
			this.name = function() { return this._JoiningMutexContext.name; };
			this.openThreads = function() { return this._JoiningMutexContext.openThreads; };
			this.executeOnce = function() { return this._JoiningMutexContext.executeOnce; };
			this.executionCount = function() { return this._JoiningMutexContext.executionCount; };
			this.isActive = function() { return this._JoiningMutexContext.active; };
			this.ThreadFinished = function() {
				if (!this._JoiningMutexContext.active) {
					return {error: true, errorObj: undefined, errorMsg: 'JoiningMutexProxy.ThreadFinished() failed because the giving context is no longer active'};
				}
				if (this._JoiningMutexContext._alreadyRun) {
					return {error: true, errorObj: undefined, errorMsg: 'JoiningMutexProxy.ThreadFinished() failed because the MutexProxy has already been run'};
				}
				if (this._JoiningMutexContext.openThreads > 0) {
					this._JoiningMutexContext.openThreads--;
					this._alreadyRun = true;
					if (this._JoiningMutexContext.openThreads == 0) {
						// all threads finished
						if (this._JoiningMutexContext.executeOnce) {
							// this is going to be our only run of the callback function
							this._JoiningMutexContext.active = false;
						}
						this._JoiningMutexContext.executionCount++;
						this._JoiningMutexContext.callbackFinished();
						return true;
					} else {
						// everything is OK but there are still outstanding threads to finish
						return false;
					}
				} else {
					return {error: true, errorObj: undefined, errorMsg: 'JoiningMutexProxy.ThreadFinished() failed because there are no outstanding thread executions'};
				}
			};
		}
		return new JoiningMutexContextProxy;
	},
	contextCreate: function(sContextName, fZeroRunFunction, bSingleRun) {
		// make sure context is new
		var validName = sContextName;
		try {
			if (!validName) {
				this._contextGenID++;
				validName = "AUTOGEN-"+this._contextGenID;
			}
			if (this._contexts[validName]) {
				return {error: true, errorObj: undefined, errorMsg: 'JoiningMutex.contextCreate() failed because the giving context name already exists'};
			}
			// verify that the name can be used as an object identifier (with throw an error if invalid)
			this._contexts[validName] = true;
			delete this._contexts[validName];
		} catch(e) {
			return {error: true, errorObj: e, errorMsg: 'an error occurred within JoiningMutex.contextCreate()'};
		}
		// create new context object
		function JoiningMutexContext(inName, inFinishFunction, inSingleExecution) {
			this.name = inName;
			this.callbackFinished = inFinishFunction;
			this.openThreads = 0;
			this.executeOnce = inSingleExecution;
			this.executionCount = 0;
			this.active = true;
		}
		var bSingleRun = Boolean.parseTo(bSingleRun);
		this._contexts[validName] = new JoiningMutexContext(validName, fZeroRunFunction, bSingleRun);
		// add ourselves to the thread count
		var cl_JoinMutextRef = this._contexts[validName];
		cl_JoinMutextRef.openThreads++;
		return this._createContextProxy(cl_JoinMutextRef);
	},
	contextJoin: function(sContextName) {
		// make sure context already exists
		var validName = sContextName;
		if (!this._contexts[validName]) {
			return {error: true, errorObj: undefined, errorMsg: 'JoiningMutex.contextCreate() failed because the context name does not exist'};
		}
		var cl_JoinMutextRef = this._contexts[validName];
		// Add this thread to the count
		cl_JoinMutextRef.openThreads++;
		return this._createContextProxy(cl_JoinMutextRef);
	},
	contextJoinCreate: function(sContextName, fZeroRunFunction, bSingleRun) {
		// Join context or create it if it exists
		var ctx = false;
		if (!sContextName || !this._contexts[sContextName]) {
			ctx = this.contextCreate.call(this, sContextName, fZeroRunFunction, bSingleRun);
		} else {
			ctx = this.contextJoin.call(this, sContextName);
		}
		return ctx;
	},
	contextDestroy: function(sContextName) {
		if (!sContextName || !this._contexts[sContextName]) {
			return false;
		} else {
			// Garbage collection will not execute until all 
			// the JoiningMutexContextProxy are deleted so 
			// invalidate the context as well as delete it!
			this._contexts[sContextName].active = false;
			this._contexts[sContextName].callbackFinished = function() { return null; };
			delete this._contexts[sContextName];
			return true;
		}
	}
};

// this function will hide/show elements tagged with the "debug" classname
i2b2.h.debugElements = function(rootElement) {
	var el = $(rootElement);
	if (!el) { return; }
	var tns = el.select('.debug');
	var sflg = i2b2.h.inDebugMode();
	for (var i=0; i<tns.length; i++) {
		if (sflg) {
			tns[i].show();
		} else {
			tns[i].hide();
		}
	}
}
i2b2.events.afterLogin.subscribe(
	(function(en,co,a) {
		// remove debugging functionality from GUI
		i2b2.h.debugElements(document.documentElement);
		// remove the analysis link if configuration tells us to
		if (!i2b2.h.allowAnalysis()) {  $('allowAnalysis').hide();  }
	})
);
i2b2.h.isBadObjPath = function(sObjectHierarchy) {
	try {
		var t = eval("("+sObjectHierarchy+")");
		return Object.isUndefined(t);
	} catch (e) {
		return true;
	}
}
	
//  Extend JS Objects
// ================================================================================================== //
String.prototype.repeat = function(l){
	return new Array(l+1).join(this);
};

// ================================================================================================== //
function padNumber(num, req_digits) {
	num = parseInt(num, 10)+"";
	var pad = req_digits - num.length;
	if (pad > 0 ) num = "0".repeat(pad)+num;
	return num;
}


// wrap YUI functions into the JS Object constructor to augment Protype-JS functions
// ================================================================================================== //
Object.isObject = function(testObj) { return YAHOO.lang.isObject(testObj); }
Object.isNull = function(testObj) { return YAHOO.lang.isNull(testObj); }
Object.isBoolean = function(testObj) { return YAHOO.lang.isBoolean(testObj); }
Boolean.parseTo = function(inputValue) {
	if (typeof inputValue == "string") {
		return /\btrue/i.match(inputValue);
	}
	return (inputValue==true);
}


// This is a special closure-based function to return a unique ID every time called.
// The closure counter gets wrapped into a private "bubble" within the JsVM that
// is not accessable from other calls...  Let the scope-chain bending begin!
// ================================================================================================== //
i2b2.GUID = (function() {
	var closure_Counter = 0 ;
	function closure_nextID() {
		var prevID = closure_Counter;
		closure_Counter++;
		if (prevID > closure_Counter) console.error("[i2b2.GUID function's internal counter encountered an overflow!!!]");
		return closure_Counter;
	}
	return (function() { return closure_nextID(); });
})();


// Global helper functions 
// ================================================================================================== //
function parseBoolean(inputStr) { return /\btrue/i.match(inputStr); }


// function needed to perform processing 
// only on objects that match a specific type
// ================================================================================================== //
Object.getClass = getObjectClass;
function getObjectClass(obj) {
    if (obj && obj.constructor && obj.constructor.toString) {
        var arr = obj.constructor.toString().match(/function\s*(\w+)/);
        if (arr && arr.length == 2) { return arr[1]; }
    }
    return undefined;
}




// Extend the YAHOO.widget.TreeView object
// ================================================================================================== //
YAHOO.widget.TreeView.findTreeByChildDiv = function(divChild) {
	var parentNode;
	var isDone = false;	
	var tdiv = $(divChild);
	if (!tdiv) { return undefined; }
	do {
		if (tdiv.id) {
			var tree = this.getTree(tdiv.id);
			if (tree) { isDone = true; }
		}
		if (tdiv.parentNode) { 
			tdiv = tdiv.parentNode;
		} else {
			isDone = true; 
		}
	} while (!isDone)
	return tree;
}

YAHOO.widget.TreeView.getNode_orig = YAHOO.widget.TreeView.getNode;
YAHOO.widget.TreeView.getNode = function(treeId, nodeIndex) {
	// This is an enhanced version which overrides the default function within the
	// YUI library,  This is needed to handle HTML <-> data structure mismatches
	// which occurs because we are separating and redisplaying our Root nodes
	// in different treesview controller instances.  This function falls back on
	// the original function calls which should prevent any problems with future versions
	var retNode = YAHOO.widget.TreeView.getNode_orig(treeId, nodeIndex);
	if (!retNode) {
		// start special processing to find our node in the list of other tree controllers
		for (var ntn in YAHOO.widget.TreeView.trees) {
			var nexttree = YAHOO.widget.TreeView.trees[ntn];
			if (nexttree.toString) {
				var n = nexttree.toString();
				var s = n.split(' ',2);
				if (s[0] == "TreeView") {
					retNode = YAHOO.widget.TreeView.getNode_orig(s[1], nodeIndex);
					if (retNode) { break; }
				}
			}
		}
	}
	return retNode;
}


// ================================================================================================== //
YAHOO.widget.Node.prototype.refresh_orig = YAHOO.widget.Node.prototype.refresh;
YAHOO.widget.Node.prototype.refresh = function() {
	// Intercept call to default node.refresh() and extend it to fire original call
	// then fire the YAHOO.util.DDM.reattachHandlers() function to reattach
	// drag & drop handlers that are used by the trees.
	var ret = this.refresh_orig.call(this);
	YAHOO.util.DDM.reattachHandlers();
console.warn("reattach DD handlers");
	return ret;
}

// ================================================================================================== //
YAHOO.widget.TreeView.attachRootNodeToTree = function(obRootNode, objTree) {
	if (obRootNode == objTree.root) return true;

	// this is our recursive set tree function
	var recursiveSetTree = function(oTree, oNode, recFunc) {
		if (oNode.tree) { oNode.tree =  oTree; }
		try {
			for (var i=0; i<oNode.children.length; i++) {
				recFunc(oTree, oNode.children[i], recFunc);
			}
		} catch(e) {}
	}

	var recursiveTreePopulate = function(oTreeArrayNode, oNode, recFunc) {
		if (oNode.index) { oTreeArrayNode[oNode.index] = oNode; }
		try {
			for (var i=0; i<oNode.children.length; i++) {
				recFunc(oTreeArrayNode, oNode.children[i], recFunc);
			}
		} catch(e) {}
	}

	var recursiveApplyParent = function(oNode, recFunc) {
		try {
			for (var i=0; i<oNode.children.length; i++) {
				oNode.children[i].applyParent(oNode);
				recFunc(oNode.children[i], recFunc);
			}
		} catch(e) {}
	}

	objTree._nodes.clear();
	// attach the new root node to the tree		
	objTree.root = obRootNode;
	obRootNode.tree = objTree;
	for (var i=0; i<obRootNode.children.length; i++) {
		obRootNode.children[i].applyParent(obRootNode);
	}
	//recursiveSetTree(objTree, objTree.root, recursiveSetTree);
	recursiveTreePopulate(objTree._nodes, objTree.root, recursiveTreePopulate);
	// reattach the dynamic load event if it was lost
	objTree.setDynamicLoad(i2b2.CRC.ctrlr.QT._loadTreeDataForNode,1);				

	return true;
}



YAHOO.widget.Node.prototype.isDescendant = function(srchNode) {
	// is the current node a descendant of srchNode? Used to prevent parents being dragged into its child nodes.
	var thisNode = this;
	if (!srchNode) { return undefined; }
	while (thisNode.parent) {
		if (thisNode == srchNode) { 
			return true;
		}
		thisNode = thisNode.parent;
	} 
	return false;
}

// this function is a very clever hack to reattach all YUI drag & drop handlers which have
// gotten detached and have (newer copies of) their elements in the visible DOM tree
YAHOO.util.DDM.reattachHandlers = function() {
	for (var grps in YAHOO.util.DDM.ids) {
		// interaction groups
		for (var el in YAHOO.util.DDM.ids[grps]) {
			try {
				var t = YAHOO.util.DDM.ids[grps][el];
				// copy the info
				var ddId = t.id;
				var ddGrps = Object.clone(t.groups);
				var ddCfg = Object.clone(t.config);
				var ddTargeted = t.isTarget;
				var fEl = $(el);
				if (!fEl) {
					if (t.available)  {
						// remove DD for non-existent HTML elements and
						// only after the DD has been initialized
						t._domRef = null;
						t.unreg();
					}
				} else {
					// reattach
					if (t._domRef != fEl) {
						t._domRef = fEl;
						t.init(ddId);
						t.groups = Object.clone(ddGrps);
						t.isTarget = ddTargeted;
					}
				}
			} catch(e) { console.warn("Error while reattaching DD handlers: "+e.message); }
		}
	}
}


// ================================================================================================== //
// Patch a bug in the YUI overlay subsystem which ignores/destroys zIndex parameter given 
// to modal forms during initial creation.  TODO: REMOVE THIS WHEN YAHOO FIXES THIS BUG!
i2b2.bugfix1 = function () {};
i2b2.bugfix1.prototype.bringToTop = function() {
	var Dom = YAHOO.util.Dom;
	var Overlay = YAHOO.widget.Overlay;
	var Lang = YAHOO.lang;

	var aOverlays = [],
		oElement = this.element;

	function compareZIndexDesc(p_oOverlay1, p_oOverlay2) {

		var sZIndex1 = Dom.getStyle(p_oOverlay1, "zIndex"),
		sZIndex2 = Dom.getStyle(p_oOverlay2, "zIndex"),

		nZIndex1 = (!sZIndex1 || isNaN(sZIndex1)) ? 0 : parseInt(sZIndex1, 10),
		nZIndex2 = (!sZIndex2 || isNaN(sZIndex2)) ? 0 : parseInt(sZIndex2, 10);

		if (nZIndex1 > nZIndex2) {
			return -1;
		} else if (nZIndex1 < nZIndex2) {
			return 1;
		} else {
			return 0;
		}
	}

	function isOverlayElement(p_oElement) {

		var oOverlay = Dom.hasClass(p_oElement, Overlay.CSS_OVERLAY),
			Panel = YAHOO.widget.Panel;

		if (oOverlay && !Dom.isAncestor(oElement, oOverlay)) {
			if (Panel && Dom.hasClass(p_oElement, Panel.CSS_PANEL)) {
				aOverlays[aOverlays.length] = p_oElement.parentNode;
			} else {
				aOverlays[aOverlays.length] = p_oElement;
			}
		}
	}

	Dom.getElementsBy(isOverlayElement, "DIV", document.body);

	aOverlays.sort(compareZIndexDesc);

	var oTopOverlay = aOverlays[0],
		nTopZIndex;

	if (oTopOverlay) {
		nTopZIndex = Dom.getStyle(oTopOverlay, "zIndex");

		if (!isNaN(nTopZIndex)) {
			var bRequiresBump = false;

			if (oTopOverlay != oElement) {
				bRequiresBump = true;
			} else if (aOverlays.length > 1) {
				var nNextZIndex = Dom.getStyle(aOverlays[1], "zIndex");
				// Don't rely on DOM order to stack if 2 overlays are at the same zindex.
				if (!isNaN(nNextZIndex) && (nTopZIndex == nNextZIndex)) {
					bRequiresBump = true;
				}
			}
			
			// <PATCH: make sure the zIndex is >= any zIndex given in the initial creation settings>
			if (!Lang.isUndefined(this.cfg.config.zindex)) {
				if (parseInt(this.cfg.config.zindex.value, 10) >= parseInt(nTopZIndex,10)) {
					nTopZIndex = this.cfg.config.zindex.value;
					bRequiresBump = true;
				}
			}
			// </PATCH: make sure the zIndex is >= any zIndex given in the initial creation settings>
			
			if (bRequiresBump) {
				this.cfg.setProperty("zindex", (parseInt(nTopZIndex, 10) + 2));
			}
		}
	}
};
i2b2.bugfix2 = function() {
	YAHOO.lang.augmentProto(YAHOO.widget.Overlay, i2b2.bugfix1, true);
	delete i2b2.bugfix1;
	delete i2b2.bugfix2;
};
setTimeout('i2b2.bugfix2()',200);




// AJAX/XML
// ================================================================================================== //
function getHtmlText(node) {
	if (node.innerText) { // IE;
		return node.innerText;
	} else {
		if (node.textContent) {
			return node.textContent;
		}
	}
	console.error('getHtmlText(): no innerText or textContent.');
}

// ================================================================================================== //
function showXML(c,r,t) {
	switch (t) {
		case "Request":
			var s = i2b2[c].view[r]['query'+t];
			if (s) {
				s = s.replace(new RegExp('<','g'),'&lt;');
				s = s.replace(new RegExp('>','g'),'&gt;');
			} else {
				s = '';
			}
			i2b2.hive.MsgSniffer.showSingleMsgRequest(s);
			break;
		case "Response":
			var s = i2b2[c].view[r]['query'+t];
			if (s) {
				s = s.replace(new RegExp('<','g'),'&lt;');
				s = s.replace(new RegExp('>','g'),'&gt;');
			} else {
				s = '';
			}
			i2b2.hive.MsgSniffer.showSingleMsgResponse(s);
			break;
		case "Stack":
			if (c=="WORK" && r=="main") { 
				i2b2.hive.MsgSniffer.showStack("Workplace", ["WORK:Workplace"], ["WORK"]);
				return;
			}
			if (c=="ONT" && r=="nav") { 
				i2b2.hive.MsgSniffer.showStack("Ontology Navigation", ["ONT:SDX:Concept","ONT:generalView"], ["ONT"]);
				return;
			}
			if (c=="ONT" && r=="find") { 
				i2b2.hive.MsgSniffer.showStack("Ontology Search", ["ONT:SDX:Concept","ONT:FindBy","ONT:generalView"], ["ONT"]);
				return;
			}
			if (c=="CRC" && r=="history") { 
				i2b2.hive.MsgSniffer.showStack("History", ["CRC:History", "CRC:SDX:QueryMaster", "CRC:SDX:QueryInstance", "CRC:SDX:PatientRecordSet"], ["CRC"]);
				return;
			}
			if (c=="CRC" && r=="QT") { 
				i2b2.hive.MsgSniffer.showStack("Query Tool", ["CRC:QueryTool"]);
				return;
			}
			if (c=="PLUGINMGR" && r=="PlugView") { 
				i2b2.hive.MsgSniffer.show();
				return;
			}			
			if (c=="PM" && r=="Admin") { 
				i2b2.hive.MsgSniffer.showStack("PM Admin", ["PM:Admin"]);
				return;
			}			
			alert("captured "+c+" :: "+r+" :: "+t);
			break;
	}
}

// ================================================================================================== //
i2b2.h.Xml2String = function(x) {
	var xmlSerializer;
	try {
		xmlSerializer = new XMLSerializer();
		var s = xmlSerializer.serializeToString(x);
		if (s) {
			return s;
		} else {
			return 'Browser not supported yet. (Try Firefox)';
		}
	}
	catch (e) {
		return x.xml;
	}
}



// helper function to build a sniffer package for Cell Communications events
i2b2.h.BuildSniffPack = function(cellName, cellFuncName, results, signalOrigin) {
//i2b2.h.BuildSniffPack = function(viewName, cellName, cellFuncName, results) {
	function i2b2_CellCommPackage() {}
	var sniffPackage = new i2b2_CellCommPackage();
//	if (undefined == viewName) {
//		sniffPackage.ViewName = viewName;
//	}
	sniffPackage.CellName = cellName;
	sniffPackage.CellAction = cellFuncName;
	sniffPackage.CommDetails = results;
	sniffPackage.SignalOrigin = signalOrigin;
	return sniffPackage;
}




console.timeEnd('execute time');
console.groupEnd();
