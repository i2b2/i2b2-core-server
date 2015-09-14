/**
 * @projectDescription	Example using the Patient Data Object (PDO).
 * @inherits	i2b2
 * @namespace	i2b2.ProjectRequest
 * ----------------------------------------------------------------------------------------
 */

var Dom = YAHOO.util.Dom;
var Event = YAHOO.util.Event;
var DDM = YAHOO.util.DragDropMgr;

//var this.yuiTabs = null;

i2b2.ProjectRequest.showPart2 = function() {
document.getElementById('ProjectRequest-part2a').style.display = '';
document.getElementById('ProjectRequest-part2b').style.display = '';
document.getElementById('ProjectRequest-part2c').style.display = '';
document.getElementById('ProjectRequest-part2d').style.display = '';
document.getElementById('ProjectRequest-part2e').style.display = '';
i2b2.ProjectRequest.roleRender();
}


i2b2.ProjectRequest.doSubmit = function() {
	
//	this.yuiTabs.set('activeIndex', 1);

this.yuiTabs = new YAHOO.widget.TabView("ProjectRequest-TABS", {activeIndex:1});

this.yuiTabs.set('activeIndex', 1);
//gotoTab(1);
	// recalculate the results only if the input data has changed
	i2b2.ProjectRequest.getResults();
}
//////////////////////////////////////////////////////////////////////////////
// example app
//////////////////////////////////////////////////////////////////////////////
i2b2.ProjectRequest.DDApp = {
    init: function() {

        var rows=3,cols=2,i,j;
        for (i=1;i<cols+1;i=i+1) {
            new YAHOO.util.DDTarget("ul"+i);
        }

        for (i=1;i<cols+1;i=i+1) {
            for (j=1;j<rows+1;j=j+1) {
                new i2b2.ProjectRequest.DDList("li" + i + "_" + j);
            }
        }

        Event.on("showButton", "click", this.showOrder);
        Event.on("switchButton", "click", this.switchStyles);
    },

    showOrder: function() {
        var parseList = function(ul, title) {
            var items = ul.getElementsByTagName("li");
            var out = title + ": ";
            for (i=0;i<items.length;i=i+1) {
                out += items[i].id + " ";
            }
            return out;
        };

        var ul1=Dom.get("ul1"), ul2=Dom.get("ul2");
        alert(parseList(ul1, "List 1") + "\n" + parseList(ul2, "List 2"));

    },

    switchStyles: function() {
        Dom.get("ul1").className = "draglist_alt";
        Dom.get("ul2").className = "draglist_alt";
    }
};

//////////////////////////////////////////////////////////////////////////////
// custom drag and drop implementation
//////////////////////////////////////////////////////////////////////////////

i2b2.ProjectRequest.DDList = function(id, sGroup, config) {

    i2b2.ProjectRequest.DDList.superclass.constructor.call(this, id, sGroup, config);

    this.logger = this.logger || YAHOO;
    var el = this.getDragEl();
    Dom.setStyle(el, "opacity", 0.67); // The proxy is slightly transparent

    this.goingUp = false;
    this.lastY = 0;
};

YAHOO.extend(i2b2.ProjectRequest.DDList, YAHOO.util.DDProxy, {

    startDrag: function(x, y) {
        this.logger.log(this.id + " startDrag");

        // make the proxy look like the source element
        var dragEl = this.getDragEl();
        var clickEl = this.getEl();
        Dom.setStyle(clickEl, "visibility", "hidden");

        dragEl.innerHTML = clickEl.innerHTML;

        Dom.setStyle(dragEl, "color", Dom.getStyle(clickEl, "color"));
        Dom.setStyle(dragEl, "backgroundColor", Dom.getStyle(clickEl, "backgroundColor"));
        Dom.setStyle(dragEl, "border", "2px solid gray");
    },

    endDrag: function(e) {

        var srcEl = this.getEl();
        var proxy = this.getDragEl();

        // Show the proxy element and animate it to the src element's location
        Dom.setStyle(proxy, "visibility", "");
        var a = new YAHOO.util.Motion( 
            proxy, { 
                points: { 
                    to: Dom.getXY(srcEl)
                }
            }, 
            0.2, 
            YAHOO.util.Easing.easeOut 
        )
        var proxyid = proxy.id;
        var thisid = this.id;

        // Hide the proxy and show the source element when finished with the animation
        a.onComplete.subscribe(function() {
                Dom.setStyle(proxyid, "visibility", "hidden");
                Dom.setStyle(thisid, "visibility", "");
            });
        a.animate();
    },

    onDragDrop: function(e, id) {

        // If there is one drop interaction, the li was dropped either on the list,
        // or it was dropped on the current location of the source element.
        if (DDM.interactionInfo.drop.length === 1) {

            // The position of the cursor at the time of the drop (YAHOO.util.Point)
            var pt = DDM.interactionInfo.point; 

            // The region occupied by the source element at the time of the drop
            var region = DDM.interactionInfo.sourceRegion; 

            // Check to see if we are over the source element's location.  We will
            // append to the bottom of the list once we are sure it was a drop in
            // the negative space (the area of the list without any list items)
            if (!region.intersect(pt)) {
                var destEl = Dom.get(id);
                var destDD = DDM.getDDById(id);
                destEl.appendChild(this.getEl());
                destDD.isEmpty = false;
                DDM.refreshCache();
            }

        }
    },

    onDrag: function(e) {

        // Keep track of the direction of the drag for use during onDragOver
        var y = Event.getPageY(e);

        if (y < this.lastY) {
            this.goingUp = true;
        } else if (y > this.lastY) {
            this.goingUp = false;
        }

        this.lastY = y;
    },

    onDragOver: function(e, id) {
    
        var srcEl = this.getEl();
        var destEl = Dom.get(id);

        // We are only concerned with list items, we ignore the dragover
        // notifications for the list.
        if (destEl.nodeName.toLowerCase() == "li") {
            var orig_p = srcEl.parentNode;
            var p = destEl.parentNode;

            if (this.goingUp) {
                p.insertBefore(srcEl, destEl); // insert above
            } else {
                p.insertBefore(srcEl, destEl.nextSibling); // insert below
            }

            DDM.refreshCache();
        }
    }
});


Event.onDOMReady(i2b2.ProjectRequest.DDApp.init, i2b2.ProjectRequest.DDApp, true);

i2b2.ProjectRequest.Init = function(loadedDiv) {
	// register DIV as valid DragDrop target for Patient Record Sets (PRS) objects
	var op_trgt = {dropTarget:true};
	i2b2.sdx.Master.AttachType("ProjectRequest-CONCPTDROP", "CONCPT", op_trgt);
	i2b2.sdx.Master.AttachType("ProjectRequest-PRSDROP", "PRS", op_trgt);
	i2b2.sdx.Master.AttachType("ProjectRequest-EXCPRSDROP", "PRS", op_trgt);
	i2b2.sdx.Master.AttachType("ProjectRequest-EXCCONCPTDROP", "CONCPT", op_trgt);
	i2b2.sdx.Master.AttachType("ProjectRequest-ICDROP", "PRS", op_trgt);
	i2b2.sdx.Master.AttachType("ProjectRequest-EXCICDROP", "PRS", op_trgt);

	// drop event handlers used by this plugin
	i2b2.sdx.Master.setHandlerCustom("ProjectRequest-CONCPTDROP", "CONCPT", "DropHandler", i2b2.ProjectRequest.conceptDropped);
	i2b2.sdx.Master.setHandlerCustom("ProjectRequest-EXCCONCPTDROP", "CONCPT", "DropHandler", i2b2.ProjectRequest.excconceptDropped);
	i2b2.sdx.Master.setHandlerCustom("ProjectRequest-PRSDROP", "PRS", "DropHandler", i2b2.ProjectRequest.prsDropped);
	i2b2.sdx.Master.setHandlerCustom("ProjectRequest-EXCPRSDROP", "PRS", "DropHandler", i2b2.ProjectRequest.excprsDropped);
	i2b2.sdx.Master.setHandlerCustom("ProjectRequest-ICDROP", "PRS", "DropHandler", i2b2.ProjectRequest.icDropped);
	i2b2.sdx.Master.setHandlerCustom("ProjectRequest-EXCICDROP", "PRS", "DropHandler", i2b2.ProjectRequest.excicDropped);

	// set default output options
	i2b2.ProjectRequest.model.outputOptions = {};
	i2b2.ProjectRequest.model.outputOptions.patients = true;
	i2b2.ProjectRequest.model.outputOptions.events = true;
	i2b2.ProjectRequest.model.outputOptions.observations = true;

	// array to store concepts
	i2b2.ProjectRequest.model.concepts = [];
	i2b2.ProjectRequest.model.excconcepts = [];

	// array to store patient sets
	i2b2.ProjectRequest.model.prs = [];
	i2b2.ProjectRequest.model.excprs = [];

	// array to store patient sets
	i2b2.ProjectRequest.model.ic = [];
	i2b2.ProjectRequest.model.excic = [];


	// array to store Users
	i2b2.ProjectRequest.model.users = [];

	// Create all the roles needed
	//i2b2.ProjectRequest.roleRender();
	
	// Add all the topics
	i2b2.ProjectRequest.addTopic();

	// manage YUI tabs
	this.yuiTabs = new YAHOO.widget.TabView("ProjectRequest-TABS", {activeIndex:0});
	/*
	this.yuiTabs.on('activeTabChange', function(ev) { 
		//Tabs have changed 
		if (ev.newValue.get('id')=="ProjectRequest-TAB1") {
			// user switched to Results tab
			//if (i2b2.ProjectRequest.model.conceptRecord && i2b2.ProjectRequest.model.prsRecord && i2b2.ProjectRequest.model.excconceptRecord && i2b2.ProjectRequest.model.excprsRecord && i2b2.ProjectRequest.model.icRecord && i2b2.ProjectRequest.model.excicRecord) {
			// contact PDO only if we have data
		//	if (i2b2.ProjectRequest.model.dirtyResultsData) {
					// recalculate the results only if the input data has changed
					i2b2.ProjectRequest.getResults();
			//	}
		//	}
		}
	});
	*/
		z = $('anaPluginViewFrame').getHeight() - 34;
	$$('DIV#ProjectRequest-TABS DIV.ProjectRequest-MainContent')[0].style.height = z;
	$$('DIV#ProjectRequest-TABS DIV.ProjectRequest-MainContent')[1].style.height = z;
	$$('DIV#ProjectRequest-TABS DIV.ProjectRequest-MainContent')[2].style.height = z;


};

i2b2.ProjectRequest.Unload = function() {
	// purge old data
	i2b2.ProjectRequest.model.prsRecord = false;
	i2b2.ProjectRequest.model.excprsRecord = false;
	i2b2.ProjectRequest.model.conceptRecord = false;
	i2b2.ProjectRequest.model.excconceptRecord = false;
	i2b2.ProjectRequest.model.icRecord = false;
	i2b2.ProjectRequest.model.excicRecord = false;
	i2b2.ProjectRequest.model.dirtyResultsData = true;
	i2b2.ProjectRequest.model.outputOptions.patients = true;
	i2b2.ProjectRequest.model.outputOptions.events = true;
	i2b2.ProjectRequest.model.outputOptions.observations = true;
	return true;
};

i2b2.ProjectRequest.prsDropped = function(sdxData) {
	sdxData = sdxData[0];	// only interested in first record
		// save the info to our local data model
	i2b2.ProjectRequest.model.prs.push(sdxData);
	// sort and display the concept list
	i2b2.ProjectRequest.prsRender();
	// optimization to prevent requerying the hive for new results if the input dataset has not changed
	i2b2.ProjectRequest.model.dirtyResultsData = true;		
};

i2b2.ProjectRequest.excprsDropped = function(sdxData) {
	sdxData = sdxData[0];	// only interested in first record
		// save the info to our local data model
	i2b2.ProjectRequest.model.excprs.push(sdxData);
	// sort and display the concept list
	i2b2.ProjectRequest.excprsRender();
	// optimization to prevent requerying the hive for new results if the input dataset has not changed
	i2b2.ProjectRequest.model.dirtyResultsData = true;		
};


i2b2.ProjectRequest.conceptDropped = function(sdxData) {
	sdxData = sdxData[0];	// only interested in first record
	if (sdxData.origData.isModifier) {
			alert("Modifier item being dropped is not supported.");
			return false;	
	}	
		// save the info to our local data model
	i2b2.ProjectRequest.model.concepts.push(sdxData);
	// sort and display the concept list
	i2b2.ProjectRequest.conceptsRender();
	// optimization to prevent requerying the hive for new results if the input dataset has not changed
	i2b2.ProjectRequest.model.dirtyResultsData = true;		
};

i2b2.ProjectRequest.excconceptDropped = function(sdxData) {
	sdxData = sdxData[0];	// only interested in first record
	if (sdxData.origData.isModifier) {
			alert("Modifier item being dropped is not supported.");
			return false;	
	}
	
		// save the info to our local data model
	i2b2.ProjectRequest.model.excconcepts.push(sdxData);
	// sort and display the concept list
	i2b2.ProjectRequest.excconceptsRender();
	// optimization to prevent requerying the hive for new results if the input dataset has not changed
	i2b2.ProjectRequest.model.dirtyResultsData = true;		
};


i2b2.ProjectRequest.icDropped = function(sdxData) {
	sdxData = sdxData[0];	// only interested in first record
		// save the info to our local data model
	i2b2.ProjectRequest.model.ic.push(sdxData);
	// sort and display the concept list
	i2b2.ProjectRequest.icRender();
	// optimization to prevent requerying the hive for new results if the input dataset has not changed
	i2b2.ProjectRequest.model.dirtyResultsData = true;		
};

i2b2.ProjectRequest.excicDropped = function(sdxData) {
	sdxData = sdxData[0];	// only interested in first record
		// save the info to our local data model
	i2b2.ProjectRequest.model.excic.push(sdxData);
	// sort and display the concept list
	i2b2.ProjectRequest.excicRender();
	// optimization to prevent requerying the hive for new results if the input dataset has not changed
	i2b2.ProjectRequest.model.dirtyResultsData = true;		
};



i2b2.ProjectRequest.conceptDelete = function(concptIndex) {
	// remove the selected concept
	i2b2.ProjectRequest.model.concepts.splice(concptIndex,1);
	// sort and display the concept list
	i2b2.ProjectRequest.conceptsRender();
	// optimization to prevent requerying the hive for new results if the input dataset has not changed
	i2b2.ProjectRequest.model.dirtyResultsData = true;		
};

i2b2.ProjectRequest.excconceptDelete = function(excconcptIndex) {
	// remove the selected concept
	i2b2.ProjectRequest.model.excconcepts.splice(excconcptIndex,1);
	// sort and display the concept list
	i2b2.ProjectRequest.excconceptsRender();
	// optimization to prevent requerying the hive for new results if the input dataset has not changed
	i2b2.ProjectRequest.model.dirtyResultsData = true;		
};


i2b2.ProjectRequest.prsDelete = function(prsIndex) {
	// remove the selected patient set
	i2b2.ProjectRequest.model.prs.splice(prsIndex,1);
	// sort and display the patient set list
	i2b2.ProjectRequest.prsRender();
	// optimization to prevent requerying the hive for new results if the input dataset has not changed
	i2b2.ProjectRequest.model.dirtyResultsData = true;		
};

i2b2.ProjectRequest.excprsDelete = function(excprsIndex) {
	// remove the selected patient set
	i2b2.ProjectRequest.model.excprs.splice(excprsIndex,1);
	// sort and display the patient set list
	i2b2.ProjectRequest.excprsRender();
	// optimization to prevent requerying the hive for new results if the input dataset has not changed
	i2b2.ProjectRequest.model.dirtyResultsData = true;		
};


i2b2.ProjectRequest.icDelete = function(icIndex) {
	// remove the selected patient set
	i2b2.ProjectRequest.model.ic.splice(icIndex,1);
	// sort and display the patient set list
	i2b2.ProjectRequest.icRender();
	// optimization to prevent requerying the hive for new results if the input dataset has not changed
	i2b2.ProjectRequest.model.dirtyResultsData = true;		
};

i2b2.ProjectRequest.excicDelete = function(excicIndex) {
	// remove the selected patient set
	i2b2.ProjectRequest.model.excic.splice(excicIndex,1);
	// sort and display the patient set list
	i2b2.ProjectRequest.excicRender();
	// optimization to prevent requerying the hive for new results if the input dataset has not changed
	i2b2.ProjectRequest.model.dirtyResultsData = true;		
};



i2b2.ProjectRequest.chgOutputOption = function(ckBox,option) {
	i2b2.ProjectRequest.model.outputOptions[option] = ckBox.checked;
	i2b2.ProjectRequest.model.dirtyResultsData = true;
};



i2b2.ProjectRequest.addTopic = function() {
	 
	// Get the roles
	var proj_data = i2b2.PM.view.admin.currentProject;
	
	var recList = i2b2.PM.ajax.getApproval("PM:Admin", {});
	// custom parse functionality
	
	
	recList.parse();
	var tmp = {};
	var l = recList.model.length;
	for (var i=0; i<l; i++) {
			var id = recList.model[i].id;
			var name = recList.model[i].name;
			var description = recList.model[i].description;
			
			var select = document.getElementById("ProjectRequest-OutputApproval");
			select.options[select.options.length] = new Option(name + " - " + description, id);
	}
	delete recList;

};

i2b2.ProjectRequest.roleRender = function() {
	var s = '<table width="100%"><tr><td>User</td><td>Data Role</td><td>Admin Role</td></tr>';
 
	// Get the roles
	var proj_data = i2b2.PM.view.admin.currentProject;
	
	//var recList = i2b2.PM.ajax.getAllParam("PM:Admin", {id: "ra_mart_test", proj_path:"/"});
	
	
	//User Params
	var recList = i2b2.PM.ajax.getAllParam("PM:Admin", {table:"user_param", id_xml:"<param name='APPROVAL_ID'>"+ document.getElementById("ProjectRequest-OutputApproval").value +"</param>"});
	// custom parse functionality
	var tmp = [];
	var c = i2b2.h.XPath(recList.refXML, "//user[user_name and param]");
	var l = c.length;
	for (var i=0; i<l; i++) {
		try {
			//var tmpRec = {};
			//tmpRec.name = i2b2.h.XPath(c[i], "attribute::name")[0].nodeValue;
			//tmpRec.id = i2b2.h.XPath(c[i], "attribute::id")[0].nodeValue;
			//tmpRec.datatype = i2b2.h.XPath(c[i], "attribute::datatype")[0].nodeValue;
			//tmpRec.value = i2b2.h.XPath(c[i], "text()")[0].nodeValue;
			var username = i2b2.h.XPath(c[i], "descendant-or-self::user/user_name/text()")[0].nodeValue;

			//tmp.push(tmpRec);
			i2b2.ProjectRequest.model.users.push(username);

			
			s += '<tr><td>' + username + '</td>';
			s += '<td> <select id="ProjectRequest-UserDataRole-'+ username +'"><option>None</option><option name="DATA_PROT">DATA_PROT</option><option name="DATA_DEID">DATA_DEID</option><option name="DATA_LDS">DATA_LDS</option><option name="DATA_AGG">DATA_AGG</option><option name="DATA_OBFSC">DATA_OBFSC</option></select></td><td> <select id="ProjectRequest-UserAdminRole-'+ username +'"><option>None</option><option name="ADMIN">ADMIN</option><option name="MANAGER">MANAGER</option><option name="EDITOR">EDITOR</option><option name="USER">USER</option></select></td></tr>';
	
		} catch(e) {}
	}

	
	//Project-User param
		var recList = i2b2.PM.ajax.getAllParam("PM:Admin", {table:"project_user", param_xml:' id="'+i2b2.PM.model.login_project+'"', id_xml:"<param name='APPROVAL_ID'>"+ document.getElementById("ProjectRequest-OutputApproval").value +"</param>"});
	// custom parse functionality
	var tmp = [];
	var c = i2b2.h.XPath(recList.refXML, "//user[user_name and param]");
	var l = c.length;
	for (var i=0; i<l; i++) {
		try {
			//var tmpRec = {};
			//tmpRec.name = i2b2.h.XPath(c[i], "attribute::name")[0].nodeValue;
			//tmpRec.id = i2b2.h.XPath(c[i], "attribute::id")[0].nodeValue;
			//tmpRec.datatype = i2b2.h.XPath(c[i], "attribute::datatype")[0].nodeValue;
			//tmpRec.value = i2b2.h.XPath(c[i], "text()")[0].nodeValue;
			var username = i2b2.h.XPath(c[i], "descendant-or-self::user/user_name/text()")[0].nodeValue;

			//tmp.push(tmpRec);
			
			
			s += '<tr><td>' + username + '</td>';
			s += '<td> <select id="ProjectRequest-OuputApproval-'+ username +'"><option>None</option><option name="DATA_PROT">DATA_PROT</option><option name="DATA_DEID">DATA_DEID</option><option name="DATA_LDS">DATA_LDS</option><option name="DATA_AGG">DATA_AGG</option><option name="DATA_OBFSC">DATA_OBFSC</option></select></td><td> <select id="ProjectRequest-OuputApproval"><option>None</option><option name="ADMIN">ADMIN</option><option name="MANAGER">MANAGER</option><option name="EDITOR">EDITOR</option><option name="USER">USER</option></select></td></tr>';
	
		} catch(e) {}
	}

	
	
	// custom parse functionality
	/*
	var tmpRoles = {};
	var c = i2b2.h.XPath(recList.refXML, "//role[user_name and role]");
	var l = c.length;
	for (var i=0; i<l; i++) {
		try {
			var name = i2b2.h.XPath(c[i], "descendant-or-self::role/user_name/text()")[0].nodeValue;
			if (!tmpRoles[name]) {
				
				
				tmpRoles[name] = [];
		s += '<tr><td>' + name + '</td>';
		s += '<td> <select id="ProjectRequest-OuputApproval"><option>None</option><option name="DATA-DEID">DATA_DEID</option><option name="DATA_AGG">DATA_AGG</option><option name="">DATA_LDS</option><option name="DATA_OBFSC">DATA_OBFSC</option></select></td><td> <select id="ProjectRequest-OuputApproval"><option>None</option><option name="ADMIN">ADMIN</option><option name="MANAGER">MANAGER</option><option name="EDITOR">EDITOR</option><option name="USER">USER</option></select></td></tr>';
				
			}
			tmpRoles[name].push(i2b2.h.XPath(c[i], "descendant-or-self::role/role/text()")[0].nodeValue);
		} catch(e) {}
	}
	*/
	s += '</table>';
	// update html
	$("ProjectRequest-roleItem").innerHTML = s;
};

i2b2.ProjectRequest.conceptsRender = function() {
	var s = '';
	// are there any concepts in the list
	if (i2b2.ProjectRequest.model.concepts.length) {
		// sort the concepts in alphabetical order
		i2b2.ProjectRequest.model.concepts.sort(function() {return arguments[0].sdxInfo.sdxDisplayName > arguments[1].sdxInfo.sdxDisplayName});
		// draw the list of concepts
		for (var i1 = 0; i1 < i2b2.ProjectRequest.model.concepts.length; i1++) {
			if (i1 > 0) { s += '<div class="concptDiv"></div>'; }
			s += '<a class="concptItem" href="JavaScript:i2b2.ProjectRequest.conceptDelete('+i1+');">' + i2b2.h.Escape(i2b2.ProjectRequest.model.concepts[i1].sdxInfo.sdxDisplayName) + '</a>';
		}
		// show the delete message
		$("ProjectRequest-DeleteMsgConcept").style.display = 'block';
	} else {
		// no concepts selected yet
		s = '<div class="concptItem">Drop one or more Concepts here</div>';
		$("ProjectRequest-DeleteMsgConcept").style.display = 'none';
	}
	// update html
	$("ProjectRequest-CONCPTDROP").innerHTML = s;
};

i2b2.ProjectRequest.excconceptsRender = function() {
	var s = '';
	// are there any concepts in the list
	if (i2b2.ProjectRequest.model.excconcepts.length) {
		// sort the concepts in alphabetical order
		i2b2.ProjectRequest.model.excconcepts.sort(function() {return arguments[0].sdxInfo.sdxDisplayName > arguments[1].sdxInfo.sdxDisplayName});
		// draw the list of concepts
		for (var i1 = 0; i1 < i2b2.ProjectRequest.model.excconcepts.length; i1++) {
			if (i1 > 0) { s += '<div class="excconcptDiv"></div>'; }
			s += '<a class="excconcptItem" href="JavaScript:i2b2.ProjectRequest.excconceptDelete('+i1+');">' + i2b2.h.Escape(i2b2.ProjectRequest.model.excconcepts[i1].sdxInfo.sdxDisplayName) + '</a>';
		}
		// show the delete message
		$("ProjectRequest-DeleteMsgExcConcept").style.display = 'block';
	} else {
		// no concepts selected yet
		s = '<div class="excconcptItem">Drop one or more Concepts here</div>';
		$("ProjectRequest-DeleteMsgExcConcept").style.display = 'none';
	}
	// update html
	$("ProjectRequest-EXCCONCPTDROP").innerHTML = s;
};

i2b2.ProjectRequest.prsRender = function() {
	var s = '';
	// are there any patient set in the list
	if (i2b2.ProjectRequest.model.prs.length) {
		// sort the concepts in alphabetical order
		i2b2.ProjectRequest.model.prs.sort(function() {return arguments[0].sdxInfo.sdxDisplayName > arguments[1].sdxInfo.sdxDisplayName});
		// draw the list of patient set
		for (var i1 = 0; i1 < i2b2.ProjectRequest.model.prs.length; i1++) {
			if (i1 > 0) { s += '<div class="prsDiv"></div>'; }
			s += '<a class="prsItem" href="JavaScript:i2b2.ProjectRequest.prsDelete('+i1+');">' + i2b2.h.Escape(i2b2.ProjectRequest.model.prs[i1].sdxInfo.sdxDisplayName) + '</a>';
		}
		// show the delete message
		$("ProjectRequest-DeleteMsgPRS").style.display = 'block';
	} else {
		// no patient set selected yet
		s = '<div class="prsItem">Drop one or more Patient Set here</div>';
		$("ProjectRequest-DeleteMsgPRS").style.display = 'none';
	}
	// update html
	$("ProjectRequest-PRSDROP").innerHTML = s;
};

i2b2.ProjectRequest.excprsRender = function() {
	var s = '';
	// are there any patient set in the list
	if (i2b2.ProjectRequest.model.excprs.length) {
		// sort the concepts in alphabetical order
		i2b2.ProjectRequest.model.excprs.sort(function() {return arguments[0].sdxInfo.sdxDisplayName > arguments[1].sdxInfo.sdxDisplayName});
		// draw the list of patient set
		for (var i1 = 0; i1 < i2b2.ProjectRequest.model.excprs.length; i1++) {
			if (i1 > 0) { s += '<div class="excprsDiv"></div>'; }
			s += '<a class="excprsItem" href="JavaScript:i2b2.ProjectRequest.excprsDelete('+i1+');">' + i2b2.h.Escape(i2b2.ProjectRequest.model.excprs[i1].sdxInfo.sdxDisplayName) + '</a>';
		}
		// show the delete message
		$("ProjectRequest-DeleteMsgExcPRS").style.display = 'block';
	} else {
		// no patient set selected yet
		s = '<div class="excprsItem">Drop one or more Patient Set here</div>';
		$("ProjectRequest-DeleteMsgExcPRS").style.display = 'none';
	}
	// update html
	$("ProjectRequest-EXCPRSDROP").innerHTML = s;
};


i2b2.ProjectRequest.icRender = function() {
	var s = '';
	// are there any patient set in the list
	if (i2b2.ProjectRequest.model.ic.length) {
		// sort the concepts in alphabetical order
		i2b2.ProjectRequest.model.ic.sort(function() {return arguments[0].sdxInfo.sdxDisplayName > arguments[1].sdxInfo.sdxDisplayName});
		// draw the list of patient set
		for (var i1 = 0; i1 < i2b2.ProjectRequest.model.ic.length; i1++) {
			if (i1 > 0) { s += '<div class="icDiv"></div>'; }
			s += '<a class="prsItem" href="JavaScript:i2b2.ProjectRequest.icDelete('+i1+');">' + i2b2.h.Escape(i2b2.ProjectRequest.model.ic[i1].sdxInfo.sdxDisplayName) + '</a>';
		}
		// show the delete message
		$("ProjectRequest-DeleteMsgIC").style.display = 'block';
	} else {
		// no patient set selected yet
		s = '<div class="icItem">Drop one or more Patient Set here</div>';
		$("ProjectRequest-DeleteMsgIC").style.display = 'none';
	}
	// update html
	$("ProjectRequest-ICDROP").innerHTML = s;
};

i2b2.ProjectRequest.excicRender = function() {
	var s = '';
	// are there any patient set in the list
	if (i2b2.ProjectRequest.model.excic.length) {
		// sort the concepts in alphabetical order
		i2b2.ProjectRequest.model.excic.sort(function() {return arguments[0].sdxInfo.sdxDisplayName > arguments[1].sdxInfo.sdxDisplayName});
		// draw the list of patient set
		for (var i1 = 0; i1 < i2b2.ProjectRequest.model.excic.length; i1++) {
			if (i1 > 0) { s += '<div class="excicDiv"></div>'; }
			s += '<a class="excprsItem" href="JavaScript:i2b2.ProjectRequest.excicDelete('+i1+');">' + i2b2.h.Escape(i2b2.ProjectRequest.model.excic[i1].sdxInfo.sdxDisplayName) + '</a>';
		}
		// show the delete message
		$("ProjectRequest-DeleteMsgExcIC").style.display = 'block';
	} else {
		// no patient set selected yet
		s = '<div class="excicItem">Drop one or more Patient Set here</div>';
		$("ProjectRequest-DeleteMsgExcIC").style.display = 'none';
	}
	// update html
	$("ProjectRequest-EXCICDROP").innerHTML = s;
};

i2b2.ProjectRequest.getResults = function() {
	if (i2b2.ProjectRequest.model.dirtyResultsData) {

		var msg_filter = '<project_request>\n';
		for (var i1=0; i1<i2b2.ProjectRequest.model.concepts.length; i1++) {
			
			if (i1 == 0)
				msg_filter += '<concept_include>\n';
			msg_filter += '<item>\n'+ i2b2.h.Xml2String(i2b2.ProjectRequest.model.concepts[i1].origData.xmlOrig) + '</item>\n';
			
			/*
			var t = i2b2.ProjectRequest.model.concepts[i1].origData.xmlOrig;
			var cdata = {};
			cdata.level = i2b2.h.getXNodeVal(t, "level");
			cdata.key = i2b2.h.getXNodeVal(t, "key");
			cdata.name = i2b2.h.getXNodeVal(t, "name");
			cdata.tablename = i2b2.h.getXNodeVal(t, "tablename");
			cdata.dimcode = i2b2.h.getXNodeVal(t, "dimcode");
			cdata.synonym = i2b2.h.getXNodeVal(t, "synonym_cd");
				msg_filter +=
				'		<item>\n'+
				'			<hlevel>'+cdata.level+'</hlevel>\n'+
				'			<item_key>'+cdata.key+'</item_key>\n'+
				'			<dim_tablename>'+cdata.tablename+'</dim_tablename>\n'+
				'			<dim_dimcode>'+cdata.dimcode+'</dim_dimcode>\n'+
				'			<item_is_synonym>'+cdata.synonym+'</item_is_synonym>\n'+
				'		</item>\n';
			*/
			if (i1 == i2b2.ProjectRequest.model.concepts.length-1)
				msg_filter += '</concept_include>\n';
		}
			
		for (var i1=0; i1<i2b2.ProjectRequest.model.excconcepts.length; i1++) {
				if (i1 == 0)
				msg_filter += '<concept_exclude>\n';

			msg_filter += '<item>\n'+ i2b2.h.Xml2String(i2b2.ProjectRequest.model.excconcepts[i1].origData.xmlOrig) + '</item>\n';

			/*
			var t = i2b2.ProjectRequest.model.excconcepts[i1].origData.xmlOrig;
			var cdata = {};
			cdata.level = i2b2.h.getXNodeVal(t, "level");
			cdata.key = i2b2.h.getXNodeVal(t, "key");
			cdata.tablename = i2b2.h.getXNodeVal(t, "tablename");
			cdata.name = i2b2.h.getXNodeVal(t, "name");
			cdata.dimcode = i2b2.h.getXNodeVal(t, "dimcode");
			cdata.synonym = i2b2.h.getXNodeVal(t, "synonym_cd");
				msg_filter +=
				'		<item>\n'+
				'			<name>'+cdata.level+'</name>\n'+
				'			<hlevel>'+cdata.level+'</hlevel>\n'+
				'			<item_key>'+cdata.key+'</item_key>\n'+
				'			<dim_tablename>'+cdata.tablename+'</dim_tablename>\n'+
				'			<dim_dimcode>'+cdata.dimcode+'</dim_dimcode>\n'+
				'			<item_is_synonym>'+cdata.synonym+'</item_is_synonym>\n'+
				'		</item>\n';
			*/
			if (i1 == i2b2.ProjectRequest.model.excconcepts.length-1)
				msg_filter += '</concept_exclude>\n';
			
		}
		
		
			for (var i1=0; i1<i2b2.ProjectRequest.model.prs.length; i1++) {
		
			if (i1 == 0)
					msg_filter += '<cases>\n';
				msg_filter += '<item>\n'+ i2b2.h.Xml2String(i2b2.ProjectRequest.model.prs[i1].origData.xmlOrig) + '</item>\n';

		/*
			var t = i2b2.ProjectRequest.model.prs[i1].origData.xmlOrig;
			var cdata = {};

			msg_filter +=
				'		<patient_set_coll_id>'+i2b2.ProjectRequest.model.prs[i1].sdxInfo.sdxKeyValue+'</patient_set_coll_id>\n';
		*/
			if (i1 == i2b2.ProjectRequest.model.prs.length-1)
					msg_filter += '</cases>\n';

		}
			
		
		for (var i1=0; i1<i2b2.ProjectRequest.model.excprs.length; i1++) {
			if (i1 == 0)
					msg_filter += '<exclude_cases>\n';
				msg_filter += '<item>\n'+ i2b2.h.Xml2String(i2b2.ProjectRequest.model.excprs[i1].origData.xmlOrig) + '</item>\n';
		/*
			msg_filter +=
				'		<patient_set_coll_id>'+i2b2.ProjectRequest.model.excprs[i1].sdxInfo.sdxKeyValue+'</patient_set_coll_id>\n';
		*/
			if (i1 == i2b2.ProjectRequest.model.excprs.length-1)
					msg_filter += '</exclude_cases>\n';
		}
	
			
		for (var i1=0; i1<i2b2.ProjectRequest.model.ic.length; i1++) {
			if (i1 == 0)
		msg_filter += '<controls>\n';
					msg_filter += '<item>\n'+ i2b2.h.Xml2String(i2b2.ProjectRequest.model.ic[i1].origData.xmlOrig) + '</item>\n';

		/*
			msg_filter +=
				'		<patient_set_coll_id>'+i2b2.ProjectRequest.model.ic[i1].sdxInfo.sdxKeyValue+'</patient_set_coll_id>\n';
		*/
			if (i1 == i2b2.ProjectRequest.model.ic.length-1)
		msg_filter += '</controls>\n';
		}
		
		
		for (var i1=0; i1<i2b2.ProjectRequest.model.excic.length; i1++) {
			if (i1 == 0)
		msg_filter += '<exclude_controls>\n';
		msg_filter += '<item>\n'+ i2b2.h.Xml2String(i2b2.ProjectRequest.model.excic[i1].origData.xmlOrig) + '</item>\n';

		/*
			msg_filter +=
				'		<patient_set_coll_id>'+i2b2.ProjectRequest.model.excic[i1].sdxInfo.sdxKeyValue+'</patient_set_coll_id>\n';
		*/
			if (i1 == i2b2.ProjectRequest.model.excic.length-1)
		msg_filter += '</exclude_controls>\n';
		}
	
	
		msg_filter += '<users>\n';
		for (var i1=0; i1<i2b2.ProjectRequest.model.users.length; i1++) {
		
			var username = i2b2.ProjectRequest.model.users[i1];
			
			if (document.getElementById("ProjectRequest-UserDataRole-"+ username ).value != 'None')
			{
				msg_filter += '<user>\n<username>' + username + '</username>\n';
				msg_filter += '<data_role>' + document.getElementById("ProjectRequest-UserDataRole-"+ username).value  + '</data_role>\n';
				msg_filter += '<admin_role>' + document.getElementById("ProjectRequest-UserAdminRole-"+ username).value  + '</admin_role>\n</user>\n';
				
			}
		//		'		<patient_set_coll_id>'+i2b2.ProjectRequest.model.excic[i1].sdxInfo.sdxKeyValue+'</patient_set_coll_id>\n';
		}
		msg_filter += '</users>\n';
	
	// array to store concepts
	
	// array to store patient sets
//	i2b2.ProjectRequest.model.prs = [];
//	i2b2.ProjectRequest.model.excprs = [];

	// array to store patient sets
//	i2b2.ProjectRequest.model.ic = [];
//	i2b2.ProjectRequest.model.excic = [];
		
			var title =  document.getElementById("title").value ;
			msg_filter += 	
			'		<title>'+ document.getElementById("title").value + '</title>\n';	
		
		msg_filter += 	
			'		<approval>'+ document.getElementById("ProjectRequest-OutputApproval").value + '</approval></project_request>\n\n';
//			'	    <user_list>\n'+
	//		'			<user>'+i2b2.PM.model.login_username+'</user>\n'+
		//	'	    </user_list>\n';
		/*
		// translate the concept XML for injection as PDO item XML
		var t = i2b2.ProjectRequest.model.conceptRecord.origData.xmlOrig;
		var cdata = {};
		cdata.level = i2b2.h.getXNodeVal(t, "level");
		cdata.key = i2b2.h.getXNodeVal(t, "key");
		cdata.tablename = i2b2.h.getXNodeVal(t, "tablename");
		cdata.dimcode = i2b2.h.getXNodeVal(t, "dimcode");
		cdata.synonym = i2b2.h.getXNodeVal(t, "synonym_cd");

		var msg_filter = 
			'		<item>\n'+
			'			<hlevel>'+cdata.level+'</hlevel>\n'+
			'			<item_key>'+cdata.key+'</item_key>\n'+
			'			<dim_tablename>'+cdata.tablename+'</dim_tablename>\n'+
			'			<dim_dimcode>'+cdata.dimcode+'</dim_dimcode>\n'+
			'			<item_is_synonym>'+cdata.synonym+'</item_is_synonym>\n'+
			'		</item>\n'+

			'	    <user_list>\n'+
			'			<user>'+i2b2.PM.model.login_username+'</user>\n'+
			'	    </user_list>\n'+

			'		<patient_set_coll_id>'+i2b2.ProjectRequest.model.prsRecord.sdxInfo.sdxKeyValue+'</patient_set_coll_id>\n';
 		*/
 
		// callback processor
		var scopedCallback = new i2b2_scopedCallback();
		scopedCallback.scope = this;
		scopedCallback.callback = function(results) {
			// THIS function is used to process the AJAX results of the getChild call
			//		results data object contains the following attributes:
			//			refXML: xmlDomObject <--- for data processing
			//			msgRequest: xml (string)
			//			msgResponse: xml (string)
			//			error: boolean
			//			errorStatus: string [only with error=true]
			//			errorMsg: string [only with error=true]
			
			// check for errors
			if (results.error) {
				alert('The results from the server could not be understood.  Press F12 for more information.');
				console.error("Bad Results from Cell Communicator: ",results);
				return false;
			}

			$$("DIV#ProjectRequest-mainDiv DIV#ProjectRequest-TABS DIV.results-working")[0].hide();
			$$("DIV#ProjectRequest-mainDiv DIV#ProjectRequest-TABS DIV.results-finished")[0].show();
			var divResults = $$("DIV#ProjectRequest-mainDiv DIV#ProjectRequest-InfoPDO")[0];
			
			
			var beginid = results.msgResponse.indexOf('<ns4:response>');
			var beginend = results.msgResponse.indexOf('</ns4:response>');
			var requestid = results.msgResponse.substring(beginid+14,beginend);
/*
		var c = i2b2.h.XPath(results.refXML, '//message_body');
			var Sstring = c.toString();
	
		var d2 = i2b2.h.XPath(results.refXML, '//message_body/response');
	
	var d = i2b2.h.XPath(results.refXML, '//message_body/ns4:response');
		var l = c.length;
	for (var i=0; i<l; i++) {
		try {
			//var t = i2b2.h.getXNodeVal(c[i], "response");
				var oData = i2b2.h.XPath(c[i],'descendant-or-self::message_body/text()');
			var username = i2b2.h.XPath(c[i],'descendant-or-self::message_body/text()')[1].nodeValue;
				var oData2 = i2b2.h.XPath(c[i], 'rs4:response');
			var username2 = i2b2.h.XPath(c[i],'response');

		var test = "";
		} catch(e) {}
	}	
		*/
			
			//Element.select(divResults, '.InfoPDO-Request .originalXML')[0].innerHTML = '<pre>'+i2b2.h.Escape(results.msgRequest)+'</pre>';
			Element.select(divResults, '.InfoPDO-Response')[0].innerHTML = requestid; //'<pre>'+i2b2.h.Escape(results.msgResponse)+'</pre>';

			// optimization - only requery when the input data is changed
			i2b2.ProjectRequest.model.dirtyResultsData = false;
		}
		
		$$("DIV#ProjectRequest-mainDiv DIV#ProjectRequest-TABS DIV.results-directions")[0].hide();
		$$("DIV#ProjectRequest-mainDiv DIV#ProjectRequest-TABS DIV.results-finished")[0].hide();
		$$("DIV#ProjectRequest-mainDiv DIV#ProjectRequest-TABS DIV.results-working")[0].show();		
		// AJAX CALL USING THE EXISTING CRC CELL COMMUNICATOR
		//i2b2.CRC.ajax.getPDO_fromInputList
		i2b2.PM.ajax.setProjectRequest("Plugin:ProjectRequest", {patient_limit:5, Project_Request: msg_filter, title: title}, scopedCallback);
	}
}
