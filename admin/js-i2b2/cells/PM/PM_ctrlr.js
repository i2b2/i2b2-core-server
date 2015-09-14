/**
 * @projectDescription	Controller object for Project Management.
 * @inherits 	i2b2
 * @namespace	i2b2.PM
 * @author		Nick Benik, Griffin Weber MD PhD
 * @version 	1.5
 * ----------------------------------------------------------------------------------------
 * updated 9-15-09: Refactor loading process to allow CELL loading timeouts and failures [Nick Benik] 
 * updated 11-9-09: Changes to announcement dialog functionality [Charles McGow]
 * updated 11-23-09: Bug Fix for Firefox's 4k XML node text limit [Nick Benik]
 */
console.group('Load & Execute component file: cells > PM > ctrlr');
console.time('execute time');

// ================================================================================================== //
i2b2.PM.doLogin = function() {
	i2b2.PM.model.shrine_domain = false;
	// change the cursor
	// show on GUI that work is being done
	i2b2.h.LoadingMask.show();
	
	// copy the selected domain info into our main data model
	var e = 'The following problems were encountered:';
	var val = i2b2.PM.udlogin.inputUser.value;
	if (!val.blank()) {
		var login_username = val;
	} else {
		e += "\n  Username is empty";
	}
	var val = i2b2.PM.udlogin.inputPass.value;
	if (!val.blank()) {
		var login_password = val;
	} else {
		e += "\n  Password is empty";
	}
	var p = i2b2.PM.udlogin.inputDomain;
	var val = p.options[p.selectedIndex].value;
	if (!val.blank()) {
		var p = i2b2.PM.model.Domains;
		if (p[val]) {
			// copy information from the domain record
			var login_domain = p[val].domain;
			var login_url = p[val].urlCellPM;
			i2b2.PM.model.url = login_url;
			var shrine_domain = Boolean.parseTo(p[val].isSHRINE);
			var login_project = p[val].project;
			if (p[val].debug != undefined) {
				i2b2.PM.model.login_debugging = Boolean.parseTo(p[val].debug);
			} else {
				i2b2.PM.model.login_debugging = false;
			}
			if (p[val].allowAnalysis != undefined) {
				i2b2.PM.model.allow_analysis = Boolean.parseTo(p[val].allowAnalysis);
			} else {
				i2b2.PM.model.allow_analysis = true;
			}
			if (p[val].adminOnly != undefined) {
				i2b2.PM.model.admin_only = Boolean.parseTo(p[val].adminOnly);
			} else {
				i2b2.PM.model.admin_only = false;
			}
			
		}
	} else {
		e += "\n  No login channel was selected";
	}
	// call the PM Cell's communicator Object
	var callback = new i2b2_scopedCallback(i2b2.PM._processUserConfig, i2b2.PM);
	var parameters = {
		domain: login_domain, 
		is_shrine: shrine_domain,
		project: login_project,
		username: login_username,
		password_text: login_password
	};
	var transportOptions = {
		url: login_url,
		user: login_username,
		password: login_password,
		domain: login_domain,
		project: login_project
	};
	i2b2.PM.ajax.getUserAuth("PM:Login", parameters, callback, transportOptions);

}


// ================================================================================================== //
i2b2.PM._processUserConfig = function (data) {
	console.group("PROCESS Login XML");
	console.debug(" === run the following command in Firebug to view message sniffer: i2b2.hive.MsgSniffer.show() ===");



	// save the valid data that was passed into the PM cell's data model
	i2b2.PM.model.login_username = data.msgParams.sec_user;
	try {
		var t = i2b2.h.XPath(data.refXML, '//user/password')[0]; //[@token_ms_timeout]
		i2b2.PM.model.login_password = i2b2.h.Xml2String(t);
		
		var timeout = t.getAttribute('token_ms_timeout');
		if (timeout == undefined ||  timeout < 300001)
		{
		 i2b2.PM.model.IdleTimer.start(1800000-300000); //timeout); //timeout-60000);		
			
		} else {
		
		 i2b2.PM.model.IdleTimer.start(timeout-300000); //timeout); //timeout-60000);		
		}
	} catch (e) {
		//console.error("Could not find returned password node in login XML");
		i2b2.PM.model.login_password = "<password>"+data.msgParams.sec_pass+"</password>\n";
	}	
	// clear the password
	i2b2.PM.udlogin.inputPass.value = "";
	
	if (i2b2.PM.model.reLogin) {
		i2b2.h.LoadingMask.hide();
		try { i2b2.PM.view.modal.login.hide(); } catch(e) {}
		i2b2.PM.model.reLogin = false;
		
		return;
	}	
	
		i2b2.PM.model.isAdmin = false;
	try { 
		var t = i2b2.h.XPath(data.refXML, '//user/full_name')[0];
		i2b2.PM.model.login_fullname = i2b2.h.Xml2String(t);
	} catch(e) {}	
	try { 
		var t = i2b2.h.XPath(data.refXML, '//user/is_admin')[0];
		if (Boolean.parseTo(i2b2.h.getXNodeVal(t, 'is_admin')) ) {
			i2b2.PM.model.isAdmin = true;
		}		
	} catch(e) {}		
	i2b2.PM.model.login_domain = data.msgParams.sec_domain;
	i2b2.PM.model.shrine_domain = Boolean.parseTo(data.msgParams.is_shrine);
	i2b2.PM.model.login_project = data.msgParams.sec_project;
	i2b2.PM.model.loginXML = data.refXML; 
	console.info("AJAX Login Successful! Updated: i2b2.PM.model");

	// hide the modal form if needed
	try { i2b2.PM.view.modal.login.hide(); } catch(e) {}

	i2b2.PM.cfg.cellURL = i2b2.PM.model.url;  // remember the url
	// if user has more than one project display a modal dialog box to have them select one
	var xml = data.refXML;

	var projs = i2b2.h.XPath(xml, 'descendant::user/project[@id]');	
	console.debug(projs.length+' project(s) discovered for user');
	// populate the Project data into the data model
	i2b2.PM.model.projects = {};
	for (var i=0; i<projs.length; i++) {
		// save data into model
		var code = projs[i].getAttribute('id');
		i2b2.PM.model.projects[code] = {};
		i2b2.PM.model.projects[code].name = i2b2.h.getXNodeVal(projs[i], 'name');
		i2b2.PM.model.projects[code].path = i2b2.h.getXNodeVal(projs[i], 'path');
		var roledetails = i2b2.h.XPath(projs[i], 'descendant-or-self::role');
		i2b2.PM.model.projects[code].roles = {};
		/*
		for (var d=0; d<roledetails.length; d++) {
			//alert(roledetails[d].textContent);
			// BUG FIX - Firefox splits large values into multiple 4k text nodes... use Firefox-specific function to read concatenated value
			if ((roledetails[d].textContent) && (roledetails[d].textContent  == "ADMIN")) {
				i2b2.PM.model.isAdmin = true
			} else if ((roledetails[d].firstChild) && (roledetails[d].firstChild.nodeValue.unescapeHTML() == "ADMIN")) {
				i2b2.PM.model.isAdmin = true		
			}
		}
		*/
		// details`
		var projdetails = i2b2.h.XPath(projs[i], 'descendant-or-self::param[@name]');
		i2b2.PM.model.projects[code].details = {};
		for (var d=0; d<projdetails.length; d++) {
			var paramName = projdetails[d].getAttribute('name');
			// BUG FIX - Firefox splits large values into multiple 4k text nodes... use Firefox-specific function to read concatenated value
			if (projdetails[d].textContent) {
				i2b2.PM.model.projects[code].details[paramName] = projdetails[d].textContent;
			} else if (projdetails[d].firstChild) {
				i2b2.PM.model.projects[code].details[paramName] = projdetails[d].firstChild.nodeValue.unescapeHTML();				
			}
		}
	}

	 if (!i2b2.PM.model.isAdmin && i2b2.PM.model.admin_only)
	{
		if (data.msgResponse == "")
					alert("The PM Cell is down or the address in the properties file is incorrect.");	
		else
			alert("Requires ADMIN role, please contact your system administrator");
		try { i2b2.PM.view.modal.login.show(); } catch(e) {}
		return true;
	} else if (i2b2.PM.model.admin_only) {	
		// default to the first project
		$('crcQueryToolBox').hide(); 
		i2b2.PM.model.login_project = ""; //i2b2.h.XPath(projs[0], 'attribute::id')[0].nodeValue;
		i2b2.PM._processLaunchFramework();
	} else 	if (projs.length == 0) {
		// show project selection dialog if needed	
		try { i2b2.h.LoadingMask.hide(); } catch(e) {}
		// better error messages
		var s = i2b2.h.XPath(xml, 'descendant::result_status/status[@type="ERROR"]');
		if (s.length > 0) {
			// we have a proper error msg
			try {
					alert("ERROR: "+s[0].firstChild.nodeValue);				
			} catch (e) {
				alert("An unknown error has occured during your login attempt!");
			}
		} else if (i2b2.PM.model.login_fullname != "") {
			alert("Your account does not have access to any i2b2 projects.");		
		//} else if (s == null || s == "") {
		//	alert("The PM Cell is down or the address in the properties file is incorrect.");	
		} else {
			alert("The PM Cell is down or the address in the properties file is incorrect.");	
			//alert("Your account does not have access to any i2b2 projects.");		
		}
		try { i2b2.PM.view.modal.login.show(); } catch(e) {}
		return true;
	} else if (projs.length == 1) {
		// default to the only project the user has access to
		i2b2.PM.model.login_project = i2b2.h.XPath(projs[0], 'attribute::id')[0].nodeValue;
		i2b2.PM.model.login_projectname = i2b2.h.getXNodeVal(projs[0], "name");
		try {
			var announcement = i2b2.PM.model.projects[i2b2.PM.model.login_project].details.announcement;
			if (announcement) {
				i2b2.PM.view.modal.announcementDialog.showAnnouncement(announcement);
				return;
			}
		} catch(e) {}
		i2b2.PM._processLaunchFramework();
	} else {
		// display list of possible projects for the user to select
		i2b2.PM.view.modal.projectDialog.showProjects();
	}

}


// ================================================================================================== //
i2b2.PM.doLogout = function() {
	// bug fix - must reload page to avoid dirty data from lingering
	window.location.reload();
}


i2b2.PM.changePassword = {
	show: function() {
		if (!i2b2.PM.changePassword.yuiPanel) {
					// load the help page
		
			// show non-modal dialog with help documentation		
			var panel = new YAHOO.widget.Panel("changepassword-viewer-panel", { 
				draggable: true,
				zindex:10000,
				width: "300px", 
				height: "200px", 
				autofillheight: "body", 
				constraintoviewport: true, 
				context: ["showbtn", "tl", "bl"]
			}); 
			$("changepassword-viewer-panel").show();
			panel.render(document.body); 
			panel.show(); 
			i2b2.PM.changePassword.yuiPanel = panel;
			
			
		} else {
			i2b2.PM.changePassword.yuiPanel.show();
		}
		// load the help page
		
	},
	hide: function() {
		try {
			i2b2.PM.changePassword.yuiPanel.hide();
			//$("changepassword-viewer-panel").hide();
		} catch (e) {}
	},
	run: function() {
		try {
			var curpass = $('curpass').value;
			var newpass = $('newpass').value;
			var retypepass = $('retypepass').value;
			
			if (newpass != retypepass) {
				alert("New password and Retype Password dont match");
			} else { 
				
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
						alert('Current password is incorrect');
						console.error("Bad Results from Cell Communicator: ",results);
						return false;
					}
					alert("Password successfully changed");	
					i2b2.PM.changePassword.yuiPanel.hide();


				}
				
				// AJAX CALL USING THE EXISTING CRC CELL COMMUNICATOR
				//i2b2.CRC.ajax.getPDO_fromInputList
				i2b2.PM.ajax.setPassword("Plugin:PM", {sec_oldpassword:curpass, sec_newpassword: newpass}, scopedCallback);
				
			}
			//$("changepassword-viewer-panel").hide();
		} catch (e) {}
	}
};



i2b2.PM.view.modal.projectDialog = {
	loginXML: false,
	showProjects: function() {
		var dataXML = i2b2.PM.model.loginXML;
		var thisRef = i2b2.PM.view.modal.projectDialog;
		if (!$("i2b2_projects_modal_dialog")) {
			var htmlFrag = i2b2.PM.model.html.projDialog;
			Element.insert(document.body,htmlFrag);
		
			if (!thisRef.yuiDialog) {
				thisRef.yuiDialog = new YAHOO.widget.SimpleDialog("i2b2_projects_modal_dialog", {
					zindex: 700,
					width: "400px",
					fixedcenter: true,
					constraintoviewport: true,
					close: false
				});
				var kl = new YAHOO.util.KeyListener("i2b2_projects_modal_dialog", { keys:13 },  							
																  { fn:i2b2.PM.view.modal.projectDialog.loadProject,
																	scope:i2b2.PM.view.modal.projectDialog,
																	correctScope:true }, "keydown" );
				thisRef.yuiDialog.cfg.queueProperty("keylisteners", kl);
				thisRef.yuiDialog.render(document.body);
				// show the form
				thisRef.yuiDialog.show();
			}
		}
		// show the form
		thisRef.yuiDialog.show();
		$('loginProjs').focus();
		// load the project data
		var pli = $('loginProjs');
		while( pli.hasChildNodes() ) { pli.removeChild( pli.lastChild ); }
		// populate the Project data into the form
		for (var code in i2b2.PM.model.projects) {
			// dropdown
			pno = document.createElement('OPTION');
			pno.setAttribute('value', code);
			var pnt = document.createTextNode(i2b2.PM.model.projects[code].name);
			pno.appendChild(pnt);
			pli.appendChild(pno);			
		}
		// select first project
		$('loginProjs').selectedIndex = 0;

		// display the details for the currently selected project
		i2b2.PM.view.modal.projectDialog.renderDetails();
	},
	renderDetails: function() {
		// clear the details display
		var pli = $('projectAttribs');
		while( pli.hasChildNodes() ) { pli.removeChild( pli.lastChild ); }
		
		// get the currently selected project
		var p = $('loginProjs');
		var projectCode = p.options[p.selectedIndex].value;
		
		// show details
		for (var i in i2b2.PM.model.projects[projectCode].details) {
			// ignore "announcement" param
			if (i != "announcement") {
				// clone the record DIV and add it to the display list
				var rec = $('projDetailRec-CLONE').cloneNode(true);
				// change the entry id
				rec.id = "";
				rec.style.display = "";
				try {
					var part = rec.select('.name')[0];
					part.innerHTML = i;
					part = rec.select('.value')[0];
					part.innerHTML = i2b2.PM.model.projects[projectCode].details[i];
				} catch(e) {}
				pli.appendChild(rec);
			}
		}
		if (!i) {
			Element.insert(pli,'<DIV class="NoDetails">No additional information is available.</DIV>');
		} else {
			Element.insert(pli,'<DIV style="clear:both;"></DIV>');
		}
	},
	loadProject: function(ProjId) {
		if (!ProjId) {
			// get the ID of the currently selected project in the dropdown
			var p = $('loginProjs');
			ProjId = p.options[p.selectedIndex].value;
			ProjName = p.options[p.selectedIndex].text;
		}
		i2b2.PM.model.login_project = ProjId;
		i2b2.PM.model.login_projectname = ProjName;
		i2b2.PM.view.modal.projectDialog.yuiDialog.destroy();
		try {
			var announcement = i2b2.PM.model.projects[ProjId].details.announcement;
			if (announcement) {
				i2b2.PM.view.modal.announcementDialog.showAnnouncement(announcement);
				return;
			}
		} catch(e) {}
		i2b2.PM._processLaunchFramework();
	}
}

i2b2.PM.view.modal.announcementDialog = {
	showAnnouncement: function(msg) {
		var thisRef = i2b2.PM.view.modal.announcementDialog;
		if (!thisRef.yuiDialog) {
			thisRef.yuiDialog = new YAHOO.widget.SimpleDialog("PM-announcement-panel", {
				zindex: 700,
				width: "400px",
				fixedcenter: true,
				constraintoviewport: true,
				close: false
			});
			thisRef.yuiDialog.cfg.queueProperty("buttons",
				[{text: "Yes, I agree", handler:i2b2.PM.view.modal.announcementDialog.clickOK, isDefault:true},
				 {text: "No, I disagree", handler:i2b2.PM.view.modal.announcementDialog.clickCancel}
				]);
			thisRef.yuiDialog.render(document.body);
			// show the form
			thisRef.yuiDialog.show();
		}
		
		// display the announcement text
		$('PM-announcement-title').innerHTML = i2b2.PM.model.login_project + " Announcements";
		$('PM-announcement-body').innerHTML =  msg.replace(/&lt;/g, '<', 'gm').replace(/&gt;/g, '>');
			// show the form
		$('PM-announcement-panel').show();
		thisRef.yuiDialog.show();
		thisRef.yuiDialog.center();
	},
	clickOK: function() {
		this.hide();
		if (!i2b2.hive.isLoaded) {
			i2b2.PM._processLaunchFramework();
		}
	},
	clickCancel: function(){
		this.hide();
		i2b2.PM.doLogout();
	}
}



// ================================================================================================================================
// NEW FRAMEWORK LAUNCH CODE (cells can timeout on failure instead of hanging the entire load process)
// ================================================================================================================================
i2b2.PM._processLaunchFramework = function() {
	i2b2.hive.isLoaded = false;
	var oXML = i2b2.PM.model.loginXML;

	// create signal sender for afterLogin event
	i2b2.events.afterCellInit.subscribe((function(type,args) {
		if (i2b2.hive.isLoaded) { 
			// turn off our watchdog timer
			if (i2b2.PM.WDT) { clearTimeout(i2b2.PM.WDT); }
			return;
		}

		// keep track of cells loading and fire "afterAllCellsLoaded"
		// event after all cells are confirmed as loaded
		var loadedCells = [];
		for (var cellKey in i2b2.hive.cfg.LoadedCells) {
			if ((i2b2.hive.cfg.LoadedCells[cellKey] && i2b2[cellKey]) && !i2b2[cellKey].isLoaded) {
				return true;
			}
			loadedCells.push(cellKey);
		}

		// all cells are loaded, fire the "all go" signal if all cells are loaded
		console.info("EVENT FIRE i2b2.events.afterLogin");
		i2b2.events.afterLogin.fire(loadedCells);
		delete i2b2.hive.tempCellsList;
		i2b2.hive.isLoaded = true;
		// turn off our watchdog timer
		if (i2b2.PM.WDT) { clearTimeout(i2b2.PM.WDT); }
		// hide the "loading" mask
		i2b2.h.LoadingMask.hide();
		// clear our cached copy of the xml message
		delete i2b2.PM.model.loginXML;
	}));

	// extract additional user/project information
	i2b2.PM.model.userRoles = [];
	i2b2.PM.model.isObfuscated = true;
	var roles = i2b2.h.XPath(oXML, "//user/project[@id='"+i2b2.PM.model.login_project+"']/role/text()");
	var l = roles.length;
	for (var i=0; i<l; i++) {
		if (i2b2.PM.model.userRoles.indexOf(roles[i].nodeValue) == -1)
			i2b2.PM.model.userRoles.push(roles[i].nodeValue);
			if (roles[i].nodeValue == "DATA_AGG")
			{
				i2b2.PM.model.isObfuscated = false;	
			}
	}

	// process cell listing
	var cellIDs = {};
	var c = i2b2.h.XPath(oXML, "//cell_data/@id");
	var l = c.length;
	for (var i=0; i<l; i++) {
		try {
			cellIDs[c[i].nodeValue] = true;
		} catch(e) {
			console.error("Invalid Node Info!");
		}
	}
	// add additional provided by the server or flag for deletion;
	var deleteKeys = {};
	for (var cellKey in i2b2.hive.cfg.lstCells) {
		if (cellIDs[cellKey]) {
			try {
				// server requested loading of cell
				var cellRef = i2b2.hive.cfg.lstCells[cellKey];
				cellRef.serverLoaded = true;
				// load the rest of the info provided by the server
				var  y = i2b2.h.XPath(oXML, "//cell_data[@id='"+cellKey+"']");
				
				//First find the Cells that in the proejct selected.
				for (var i=y.length; i>=0; i--)
				{					
					var  x = i2b2.h.XPath(oXML, "//cell_data[@id='"+cellKey+"']")[i-1];
				
					if ( i2b2.h.getXNodeVal(x, "project_path") == i2b2.PM.model.projects[i2b2.PM.model.login_project].path )
					{
					cellRef.name = i2b2.h.getXNodeVal(x, "name");
					cellRef.project_path = i2b2.h.getXNodeVal(x, "project_path");
					cellRef.url = i2b2.h.getXNodeVal(x, "url");
					cellRef.xmlStr = i2b2.h.Xml2String(x);
					}
				}
				
				//If no cell is found that get the '/'
				if (!cellRef.name)
				{
					for (var i=0; i<y.length; i++)
					{					
						var  x = i2b2.h.XPath(oXML, "//cell_data[@id='"+cellKey+"']")[i];
						if ( i2b2.h.getXNodeVal(x, "project_path") == "/" )
						{
							cellRef.name = i2b2.h.getXNodeVal(x, "name");
							cellRef.project_path = i2b2.h.getXNodeVal(x, "project_path");
							cellRef.url = i2b2.h.getXNodeVal(x, "url");
							cellRef.xmlStr = i2b2.h.Xml2String(x);	
						}
					}
				}
				// params
				var x = i2b2.h.XPath(oXML, "//cell_data[@id='"+cellKey+"']/param[@name]");
				var l = x.length;
				for (var i=0; i<l; i++) {
					var n = i2b2.h.XPath(x[i], "attribute::name")[0].nodeValue;
					cellRef.params[n] = x[i].firstChild.nodeValue;
				}
				// do not save cell info unless the URL attribute has been set (exception is PM cell)
				if (cellRef.url == "" && cellKey != "PM") {
					deleteKeys[cellKey] = true;
				} else {
					i2b2.hive.cfg.lstCells[cellKey] = cellRef;
				}
			} catch (e) {
				console.error("Error occurred while processing PM cell config msg about cell:"+cellKey);
				deleteKeys[cellKey] = true;
			}
		} else {
			//Remove cells.plugins that dont have right access to
			 //if (i2b2.PM.model.userRoles.indexOf(i2b2.hive.cfg.lstCells[cellKey].roles) == -1) {
			//	deleteKeys[cellKey] = true;				 
			 //}
			 if (!i2b2.PM.model.admin_only) {
			 var roleFound = -1;
			for (var i=0; i<i2b2.hive.cfg.lstCells[cellKey].roles.length; i++) {
				roleFound = 0;
				 if (i2b2.PM.model.userRoles.indexOf(i2b2.hive.cfg.lstCells[cellKey].roles[i]) != -1)
				 {
					roleFound = 1;
					break; 
				 }
			 }
			 if (roleFound == 0)
			 {
				 deleteKeys[cellKey] = true;				 
			 }
			 }
			// no need to load the cell unless forced
			if (cellKey != "PM" && !i2b2.hive.cfg.lstCells[cellKey].forceLoading) {
				// add to the delete list
				deleteKeys[cellKey] = true;
			}
		}
	}

    // purge all non-used cells from the cells listing and i2b2 namespace
	for (var cellKey in deleteKeys) {
		delete i2b2.hive.cfg.lstCells[cellKey];
		if (i2b2[cellKey] && i2b2[cellKey].cellCode) {
			// made sure it's a "cell" we are about to delete
			delete i2b2[cellKey];
		}
	}

	// see if Shrine was loaded by the server
	var t = i2b2.hive.cfg.lstCells["SHRINE"];
	if (!Object.isUndefined(t) && t.serverLoaded) {
		i2b2.PM.model.shrine_domain = true;
	}
	delete t;


	// create a list of valid Cells that are loaded for this session
	var t = {};
	for (var cellKey in i2b2) {
		// is it a cell
		if (i2b2[cellKey].cellCode) {
			// valid config file?
			if (i2b2.hive.cfg.lstCells[cellKey]) {
				t[cellKey] = true;
			} else {
				console.error("CELL CONFIGURATION ERROR! ["+cellKey+"]");
				delete i2b2[cellKey];
			}
		}
	}
	i2b2.hive.cfg.LoadedCells = t;
	delete t;

	// start our watchdog time (WDT) 
	if (i2b2.hive.cfg.loginTimeout) { 
		var t = i2b2.hive.cfg.loginTimeout;
	} else {
		var t = 120;
	}
	t = t * 1000;
	i2b2.PM.WDT = setTimeout("i2b2.PM.trigger_WDT()", t);
	
	// Initialize the Cell Stubs
	for (var cellKey in i2b2.hive.cfg.LoadedCells) {
		// is it a cell
		if (cellKey != "PM" && !i2b2[cellKey].isLoaded) {
			try {
				var cfg = i2b2.hive.cfg.lstCells[cellKey];
				i2b2[cellKey].Init(cfg.url, cfg.params);
			} catch(e) {
				console.error("CELL INITIALIZATION FAILURE! ["+cellKey+"]");
				delete i2b2[cellKey];
				i2b2.hive.cfg.LoadedCells[cellKey] = false;
			}
		}
	}
	console.groupEnd("PROCESSED Login XML");
};


i2b2.PM.trigger_WDT = function() {
	console.warn('CHECKING FOR STUCK CELLS');
	var foundStuckCells = false
	for (var cellKey in i2b2.hive.cfg.LoadedCells) {
		if (i2b2.hive.cfg.LoadedCells[cellKey] && !i2b2[cellKey].isLoaded) { 
			// clear stuck module
			console.error("FOUND STUCK CELL: "+cellKey);
			foundStuckCells = true;
		}
	}
	if (!foundStuckCells) { return true; }
	if (confirm("Some modules are still attempted to load.\nDo you want to continue waiting?")) {
		// reset WDT
		if (i2b2.hive.cfg.loginTimeout) { 
			var t = i2b2.hive.cfg.loginTimeout;
		} else {
			var t = 120;
		}
		t = t * 1000;
		i2b2.PM.WDT = setTimeout("i2b2.PM.trigger_WDT()", t);
		// recheck loading status
		i2b2.events.afterCellInit.fire();
	} else {
		// clear any unloaded modules
		console.warn("CLEARING STUCK CELLS...");
		for (var cellKey in i2b2.hive.cfg.LoadedCells) {
			if (i2b2.hive.cfg.LoadedCells[cellKey] && !i2b2[cellKey].isLoaded) { 
				// clear stuck module
				console.error("CELL FORCEFULLY UNLOADED: "+cellKey);
				i2b2.hive.cfg.LoadedCells[cellKey] = false;
				delete i2b2[cellKey];
			}
		}				
		// recheck loading status (which will now pass)
		i2b2.events.afterCellInit.fire();
	}
};


console.timeEnd('execute time');
console.groupEnd();
