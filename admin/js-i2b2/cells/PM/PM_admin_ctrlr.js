/**
 * @projectDescription	PM Administration Module
 * @inherits			i2b2
 * @namespace			i2b2.PM
 * @author			Nick Benik, Mike Mendis, Griffin Weber MD PhD
 * @version			1.0
 */
 
console.group('Load & Execute component file: cells > PM > Admin');
i2b2.PM.admin = {};
i2b2.PM.model.admin = {};

 over_cal = false;


// create view controller [i2b2.PM.view.admin]
// --------------------------------------------------------------------------------------------------------------------
i2b2.PM.view.admin = new i2b2Base_cellViewController(i2b2.PM, 'admin');
i2b2.PM.view.admin.visible = false;
i2b2.PM.view.admin.yuiControls = {};
i2b2.PM.view.admin.yuiControls.primaryGrid = {};
i2b2.PM.view.admin.yuiControls.secondaryGrid = {};
i2b2.PM.view.admin.yuiTreeNodePROJECTS = false;
i2b2.PM.view.admin.yuiTreeNodeUSERS = false;
i2b2.PM.view.admin.yuiTreeNodeREQUEST = false;
i2b2.PM.view.admin.yuiTreeNodeAPPROVALS = false;
i2b2.PM.view.admin.yuiTreeNodeCELLS = false;
i2b2.PM.view.admin.Resize = function(e){
//	var t = $('pmNavTreeview');
	//var ds = document.viewport.getDimensions();
	
	var w =  window.innerWidth || (window.document.documentElement.clientWidth || window.document.body.clientWidth);
	var h =  window.innerHeight || (window.document.documentElement.clientHeight || window.document.body.clientHeight);
	h = h - 50;
	//var w = ds.width;
	//var h = ds.height-50;
	//if (w < 840) { w = 840; }
	if (h < 170) { h = 170; }	
	$('pmNavTreeview').style.height = h - 47;
	$('pmAdminMainView').style.height = h - 44;
	$('pmNav').style.height = h;
	$('pmMain').style.left = 200;
	$('pmMain').style.height = h;
	$('pmMain').style.width = Math.max(w - 225, 0);
	$('pmAdminMainView').style.width = Math.max(w - 240, 0);
}

// attach resize events
YAHOO.util.Event.addListener(window, "resize", i2b2.PM.view.admin.Resize, i2b2.PM.view.admin);

// capture view mode changes (via EVENT CAPTURE)
// ================================================================================================== //
i2b2.events.changedViewMode.subscribe((function(eventTypeName, newMode){
	newMode = newMode[0];
	this.viewMode = newMode;
	if (newMode=="Admin") {
		i2b2.PM.view.admin.parentID = false;
		i2b2.PM.view.admin.configScreen = false;
		var pu = $('pmNav');
		pu.show();
		pu = pu.style;
		pu.width = 170;
		pu.height = 144;
		if (!i2b2.PM.view.admin.yuiControls.pmNavTreeview) {
			var tree = new YAHOO.widget.TreeView("pmNavTreeview");
			i2b2.PM.view.admin.yuiControls.pmNavTreeview = tree;
			var root = tree.getRoot(); 
			var tmpNode = new YAHOO.widget.TextNode({label: "Manage Hive", expanded: false}, root);
			tmpNode.data.i2b2NodeType = "HIVE";
			var tmpNode2 = new YAHOO.widget.TextNode({label: "Domains", expanded: false}, tmpNode);
			tmpNode2.data.i2b2NodeType = "HIVEDOMAINS";
			
			
			i2b2.PM.view.admin.yuiTreeNodeCELLS = new YAHOO.widget.TextNode({label: "Manage Cells", expanded: false}, root);
			i2b2.PM.view.admin.yuiTreeNodeCELLS.data.i2b2NodeType = "HIVECELLS";
			i2b2.PM.view.admin.yuiTreeNodeCELLS.setDynamicLoad(i2b2.PM.admin.refreshTree);

			i2b2.PM.view.admin.yuiTreeNodePARAMS= new YAHOO.widget.TextNode({label: "Global Params", expanded: false}, tmpNode);
			//tmpNode2.data.i2b2NodeType = "HIVEGLOBALS";
			//tmpNode2.id = d.id;
			i2b2.PM.view.admin.yuiTreeNodePARAMS.data.xmlId = "";
			i2b2.PM.view.admin.yuiTreeNodePARAMS.data.i2b2NodeType = "PARAMS";
			i2b2.PM.view.admin.yuiTreeNodePARAMS.data.i2b2Table = "global";
			i2b2.PM.view.admin.yuiTreeNodePARAMS.data.xmlData = "/";
			
			
			i2b2.PM.view.admin.yuiTreeNodePARAMS.setDynamicLoad(i2b2.PM.admin.refreshParameters);	
			
			
			// save this for later use (refresh project list)
			i2b2.PM.view.admin.yuiTreeNodePROJECTS = new YAHOO.widget.TextNode({label: "Manage Projects", expanded: false}, root);
			i2b2.PM.view.admin.yuiTreeNodePROJECTS.data.i2b2NodeType = "PROJECTS";
			i2b2.PM.view.admin.yuiTreeNodePROJECTS.setDynamicLoad(i2b2.PM.admin.refreshProjects);
			//var tmpNode = new YAHOO.widget.TextNode({label: "Manage Users", expanded: false}, root);
			//tmpNode.data.i2b2NodeType = "USERS";
			i2b2.PM.view.admin.yuiTreeNodeUSERS = new YAHOO.widget.TextNode({label: "Manage Users", expanded: false}, root);
			i2b2.PM.view.admin.yuiTreeNodeUSERS.data.i2b2NodeType = "USERS";
			i2b2.PM.view.admin.yuiTreeNodeUSERS.setDynamicLoad(i2b2.PM.admin.refreshUsers);			


			i2b2.PM.view.admin.yuiTreeNodeAPPROVALS = new YAHOO.widget.TextNode({label: "Manage Approvals", expanded: false}, root);
			i2b2.PM.view.admin.yuiTreeNodeAPPROVALS.data.i2b2NodeType = "APPROVALS";
			i2b2.PM.view.admin.yuiTreeNodeAPPROVALS.setDynamicLoad(i2b2.PM.admin.refreshTree);

			i2b2.PM.view.admin.yuiTreeNodeREQUEST = new YAHOO.widget.TextNode({label: "Project Requests", expanded: false}, root);
			i2b2.PM.view.admin.yuiTreeNodeREQUEST.data.i2b2NodeType = "PROJECTREQUESTS";
			i2b2.PM.view.admin.yuiTreeNodeREQUEST.setDynamicLoad(i2b2.PM.admin.refreshTree);
			
			tree.render(); 
			tree.subscribe('clickEvent', i2b2.PM.view.admin.treeClick);
		}
		i2b2.PM.view.admin.configScreenDispay(0);
		i2b2.PM.view.admin.showInfoPanel("LOADED");
		$('pmMain').show();
		$('pmNav').show();
	} else {
		$('pmMain').hide();
		$('pmNav').hide();		
		this.visible = false;
	}
	this.Resize();	
}),'',i2b2.PM.view.admin);





// Click handlers for action buttons
// --------------------------------------------------------------------------------------------------------------------
i2b2.PM.admin.clickActionBtn = function(btnLevel, btnCommand) {
	var errAlertMissing = function() {alert("Unable to process. Required information is missing from the record.") };
	// identify targeted grid
	if (btnLevel==1) {
		var trgtGrid = i2b2.PM.view.admin.yuiControls.primaryGrid;
		var trgtColDefs = i2b2.PM.admin.grdPrimaryColumnDefs;
	} else {
		var trgtGrid = i2b2.PM.view.admin.yuiControls.secondaryGrid;
		var trgtColDefs = i2b2.PM.admin.grdSecondaryColumnDefs;
	}
	
	// get the selected rows (if any)
	var trgtRows = trgtGrid.getSelectedRows();
	switch(btnCommand) {
		case "DELETE":
			if (trgtRows.length == 0) {
				alert("Please select a record to delete.");
				return;
			}
			var deleteRow = trgtGrid.getRecord(trgtRows[0]).getData();
			switch (i2b2.PM.view.admin.configScreen) {
				case "HIVEDOMAINS":
					if (btnLevel==1) {
						// DELETE A HIVE DOMAIN
						if (!Object.isUndefined(deleteRow.domain_id)) {
							i2b2.PM.ajax.deleteHive("PM:Admin",  {domain_id:deleteRow.domain_id}, i2b2.PM.view.admin.refreshScreen);
						} else {
							errAlertMissing();
						}
					} else {
						// DELETE A HIVE DOMAIN PARAMETER
						try {
							// get the required username from the selected row in the primary grid
							var pgrd = i2b2.PM.view.admin.yuiControls.primaryGrid;
							var un = pgrd.getSelectedRows();
							un = pgrd.getRecord(un[0]).getData();
							if (!Object.isUndefined(deleteRow.id)) {
								i2b2.PM.ajax.deleteParam("PM:Admin", {table:"hive", msg_xml:deleteRow.id}, (function(result) {
									i2b2.PM.view.admin.showDomainParams(un.domain_id);
								}));
								i2b2.PM.view.admin.yuiControls.secondaryGrid.isDirty = false;
								i2b2.PM.view.admin.yuiControls.secondaryGrid.unselectAllRows();
							} else {
								errAlertMissing();
							}
						} catch(e) {
							var s="Failed to delete the record";
							console.error(s);
							console.dir(e);
							alert(s);
						}
					}
					break;
				case "HIVECELLS":
				case "PROJECTREC-CELLS":
					if (btnLevel==1) {
						// DELETE HIVE CELL
						if (!Object.isUndefined(deleteRow.id, deleteRow.project_path)) {
							i2b2.PM.ajax.deleteCell("PM:Admin",  {id:deleteRow.id, project_path:deleteRow.project_path }, i2b2.PM.view.admin.refreshScreen);
						} else {
							errAlertMissing();
						}
					} else {
						//DELETE HIVE CELL PARAMETER
						try {
							// get the required username from the selected row in the primary grid
							var pgrd = i2b2.PM.view.admin.yuiControls.primaryGrid;
							var un = pgrd.getSelectedRows();
							un = pgrd.getRecord(un[0]).getData();
							if (!Object.isUndefined(deleteRow.id)) {
								i2b2.PM.ajax.deleteParam("PM:Admin", {table:"cell", msg_xml:deleteRow.id}, (function(result) {
									if (i2b2.PM.view.admin.currentProject) {
										i2b2.PM.view.admin.showCellParams(selectedRec.id, "/"+i2b2.PM.view.admin.currentProject.i2b2NodeKey);
									} else {
										i2b2.PM.view.admin.showCellParams(un.id);
									}
								}));
								i2b2.PM.view.admin.yuiControls.secondaryGrid.isDirty = false;
								i2b2.PM.view.admin.yuiControls.secondaryGrid.unselectAllRows();
							} else {
								errAlertMissing();
							}
						} catch(e) {
							var s="Failed to delete the record";
							console.error(s);
							console.dir(e);
							alert(s);
						}						
					}
					break;
				case "HIVEGLOBALS":
					if (!Object.isUndefined(deleteRow.id)) {
						i2b2.PM.ajax.deleteGlobal("PM:Admin",  {param_id:deleteRow.id}, i2b2.PM.view.admin.refreshScreen);
					} else {
						errAlertMissing();
					}
					break;
				case "PROJECTREC-PARAMS":
					// DELETE PROJECT PARAMETER
					if (!Object.isUndefined(i2b2.PM.view.admin.currentProject.i2b2NodeKey, deleteRow.id)) {
						// get the required project ID 
						var ma = ' id="'+i2b2.PM.view.admin.currentProject.i2b2NodeKey+'" ';
						i2b2.PM.ajax.deleteParam("PM:Admin",  {table:"project", msg_attrib: ma, msg_xml: deleteRow.id}, (function() {
							i2b2.PM.view.admin.showProjectParams();
						}));
						i2b2.PM.view.admin.yuiControls.primaryGrid.isDirty = false;
						i2b2.PM.view.admin.yuiControls.primaryGrid.unselectAllRows();
						i2b2.PM.view.admin.yuiControls.secondaryGrid.isDirty = false;	
					} else {
						errAlertMissing();
					}
					break;								
				case "PROJECTREC-USERS":
					if (btnLevel==1) {
						// DELETE PROJECT USER
						if (!confirm('Are you sure you want to delete username "'+deleteRow.user_name+'" from the project?')) { return false; }
						// VERIFY THAT USERNAME IS VALID!
						var usrList = i2b2.PM.ajax.getAllUser("PM:Admin", {});
						var c = i2b2.h.XPath(usrList.refXML, '//user/user_name[text() = "'+deleteRow.user_name+'"]');
						if (c.length == 0) {
							alert('The username "'+deleteRow.user_name+'" was not found!');
							return;
						}
						// GET THE USER'S EXISTING ROLES
						var roleList = i2b2.PM.ajax.getAllRole("PM:Admin", {id: i2b2.PM.view.admin.currentProject.i2b2NodeKey, proj_path:"/"+i2b2.PM.view.admin.currentProject.i2b2NodeKey});
						var c = i2b2.h.XPath(roleList.refXML, '//user_name[text() = "'+deleteRow.user_name+'"]/../role/text()');
						var l = c.length;
						var actions = {};
						for (var i=0; i<l; i++) {
							var result = i2b2.PM.ajax.deleteRole("PM:Admin", {user_id: deleteRow.user_name, user_role: c[i].nodeValue, project_id:i2b2.PM.view.admin.currentProject.i2b2NodeKey});
						}
						i2b2.PM.view.admin.yuiControls.primaryGrid.isDirty = false;
						i2b2.PM.view.admin.yuiControls.primaryGrid.unselectAllRows();
						i2b2.PM.view.admin.yuiControls.secondaryGrid.isDirty = false;
						i2b2.PM.view.admin.showProjectUsers();
					} else {
						// DELETE PROJECT USER PARAMETER
						try {
							// get the required username from the selected row in the primary grid
							var pgrd = i2b2.PM.view.admin.yuiControls.primaryGrid;
							var un = pgrd.getSelectedRows();
							un = pgrd.getRecord(un[0]).getData();
							if (!Object.isUndefined(un.user_name, deleteRow.id)) {
								i2b2.PM.ajax.deleteParam("PM:Admin", {table:"project_user", msg_xml:deleteRow.id}, (function(result) {
									i2b2.PM.view.admin.showUserProjParams(un.user_name, i2b2.PM.view.admin.currentProject.i2b2NodeKey);
								}));
								i2b2.PM.view.admin.yuiControls.secondaryGrid.isDirty = false;
								i2b2.PM.view.admin.yuiControls.secondaryGrid.unselectAllRows();
							} else {
								errAlertMissing();
							}
						} catch(e) {
							var s="Failed to delete the record";
							console.error(s);
							console.dir(e);
							alert(s);
						}
					}
					break;
				case "APPROVALS": 
					if (btnLevel==1) {
						// DELETE USER
						if (!Object.isUndefined(deleteRow.id)) {
							i2b2.PM.ajax.deleteApproval("PM:Admin", {id:deleteRow.id}, i2b2.PM.view.admin.refreshScreen);
						} else {
							errAlertMissing();
						}
					} else {
						// DELETE USER PARAMETER
						try {
							// get the required username from the selected row in the primary grid
							var pgrd = i2b2.PM.view.admin.yuiControls.primaryGrid;
							var un = pgrd.getSelectedRows();
							un = pgrd.getRecord(un[0]).getData();
							if (!Object.isUndefined(un.id, deleteRow.name, deleteRow.value)) {
								i2b2.PM.ajax.deleteParam("PM:Admin", {table:"approval", msg_xml:deleteRow.id}, (function(result) {
									i2b2.PM.view.admin.showUserParams(un.id);
								}));
								i2b2.PM.view.admin.yuiControls.secondaryGrid.isDirty = false;
								i2b2.PM.view.admin.yuiControls.secondaryGrid.unselectAllRows();
							} else {
								errAlertMissing();
							}
						} catch(e) {
							var s="Failed to delete the record";
							console.error(s);
							console.dir(e);
							alert(s);
						}
					}
					break;					
				case "USERS":
					if (btnLevel==1) {
						// DELETE USER
						if (!Object.isUndefined(deleteRow.user_name)) {
							i2b2.PM.ajax.deleteUser("PM:Admin", {user_name:deleteRow.user_name}, i2b2.PM.view.admin.refreshScreen);
						} else {
							errAlertMissing();
						}
					} else {
						// DELETE USER PARAMETER
						try {
							// get the required username from the selected row in the primary grid
							var pgrd = i2b2.PM.view.admin.yuiControls.primaryGrid;
							var un = pgrd.getSelectedRows();
							un = pgrd.getRecord(un[0]).getData();
							if (!Object.isUndefined(un.user_name, deleteRow.name, deleteRow.value)) {
								i2b2.PM.ajax.deleteParam("PM:Admin", {table:"user", msg_xml:deleteRow.id}, (function(result) {
									i2b2.PM.view.admin.showUserParams(un.user_name);
								}));
								i2b2.PM.view.admin.yuiControls.secondaryGrid.isDirty = false;
								i2b2.PM.view.admin.yuiControls.secondaryGrid.unselectAllRows();
							} else {
								errAlertMissing();
							}
						} catch(e) {
							var s="Failed to delete the record";
							console.error(s);
							console.dir(e);
							alert(s);
						}
					}
					break;
				}

			break;
		case "UPDATE":
			if (trgtRows.length == 0) {
				alert("Please select a record to update.");
				return;
			}
			if (i2b2.PM.view.admin.yuiControls.primaryGrid.isDirty || i2b2.PM.view.admin.yuiControls.secondaryGrid.isDirty) {
				var updateRow = trgtGrid.getRecord(trgtRows[0]).getData();
				switch (i2b2.PM.view.admin.configScreen) {
					case "HIVEDOMAINS":
						if (btnLevel==1) {
							// UPDATE THE HIVE DOMAIN
							if (!Object.isUndefined(updateRow.domain_id, updateRow.domain_name, updateRow.environment, updateRow.helpURL)) {
								i2b2.PM.ajax.setHive("PM:Admin", {domain_id:updateRow.domain_id, domain_name:updateRow.domain_name, environment:updateRow.environment, helpURL:updateRow.helpURL}, i2b2.PM.view.admin.refreshScreen);
							} else {
								errAlertMissing();
							}
						} else {
							// UPDATE A HIVE DOMAIN PARAM
							if (!Object.isUndefined(updateRow.name, updateRow.value)) {
								// get the required cell ID from the selected row in the primary grid
								var pgrd = i2b2.PM.view.admin.yuiControls.primaryGrid;
								var un = pgrd.getSelectedRows();
								un = pgrd.getRecord(un[0]).getData();
								var ma = 'id="'+un.domain_id+'"';
								var ma = '';
								if (updateRow.id) {
									var mx = '<project_path>' +un.path +'</project_path><domain_id>'+un.domain_id+'</domain_id><param datatype="'+updateRow.datatype+'" id="'+updateRow.id+'" name="'+updateRow.name+'">'+updateRow.value+"</param>";
								} else {
									var mx = '<project_path>' +un.path +'</project_path><domain_id>'+un.domain_id+'</domain_id><param datatype="'+updateRow.datatype+'" name="'+updateRow.name+'">'+updateRow.value+"</param>";
								}
								i2b2.PM.ajax.setParam("PM:Admin",  {table:"hive", msg_attrib: ma, msg_xml: mx}, (function() {
									i2b2.PM.view.admin.showDomainParams(un.domain_id);
								}));
								i2b2.PM.view.admin.yuiControls.secondaryGrid.isDirty = false;
								i2b2.PM.view.admin.yuiControls.secondaryGrid.unselectAllRows();
							} else {
								errAlertMissing();
							}							
						}
						break;
					case "HIVECELLS":
						if (btnLevel==1) {
							// UDATE HIVE CELL
							if (!Object.isUndefined(updateRow.id, updateRow.name, updateRow.method, updateRow.url)) {
								i2b2.PM.ajax.setCell("PM:Admin",  {cell_id:updateRow.id, project_path:"/", name:updateRow.name, method:updateRow.method, can_override:true, url:updateRow.url}, i2b2.PM.view.admin.refreshScreen);
							} else {
								errAlertMissing();
							}
						} else {
							// UPDATE HIVE CELL PARAMETER
							if (!Object.isUndefined(updateRow.name, updateRow.value)) {
								// get the required cell ID from the selected row in the primary grid
								var pgrd = i2b2.PM.view.admin.yuiControls.primaryGrid;
								var un = pgrd.getSelectedRows();
								un = pgrd.getRecord(un[0]).getData();
								var ma = ' id="'+un.id+'" ';
								if (updateRow.id) {
									var mx = '<project_path>/</project_path><param datatype="'+updateRow.datatype+'" id="'+updateRow.id+'" name="'+updateRow.name+'">'+updateRow.value+"</param>";
								} else {
									var mx = '<project_path>/</project_path><param datatype="'+updateRow.datatype+'" name="'+updateRow.name+'">'+updateRow.value+"</param>";
								}
								i2b2.PM.ajax.setParam("PM:Admin",  {table:"cell", msg_attrib: ma, msg_xml: mx}, (function() {
									i2b2.PM.view.admin.showCellParams(un.id);
								}));
								i2b2.PM.view.admin.yuiControls.secondaryGrid.isDirty = false;
								i2b2.PM.view.admin.yuiControls.secondaryGrid.unselectAllRows();
							} else {
								errAlertMissing();
							}
						}
						break;
					case "HIVEGLOBALS":
						// UPDATE GLOBALS
						if (!Object.isUndefined(updateRow.name, updateRow.value)) {
							if (updateRow.id) {
								var t = ' id="'+updateRow.id+'" ';
							} else {
								var t = '';
							}
							i2b2.PM.ajax.setGlobal("PM:Admin", {param_name:updateRow.name, param_datatype:updateRow.datatype, param_value:updateRow.value, param_id_attrib:t, can_override:"Y"});
							i2b2.PM.view.admin.yuiControls.primaryGrid.isDirty = false;
							i2b2.PM.view.admin.yuiControls.primaryGrid.unselectAllRows();
						} else {
							errAlertMissing();
						}
						break;
					case "PROJECTREC-CELLS":
						if (btnLevel==1) {
							// UPDATE THE PROJECT CELL
							if (!Object.isUndefined(updateRow.id, updateRow.name, updateRow.method, updateRow.url)) {
								i2b2.PM.ajax.setCell("PM:Admin",  {cell_id:updateRow.id, project_path:"/"+i2b2.PM.view.admin.currentProject.i2b2NodeKey, name:updateRow.name, method:updateRow.method, can_override:true, url:updateRow.url}, i2b2.PM.view.admin.refreshScreen);
							} else {
								errAlertMissing();
							}
						} else {
							// UPDATE THE PROJECT CELL PARAMETER
							if (!Object.isUndefined(updateRow.name, updateRow.value)) {
								// get the required cell ID from the selected row in the primary grid
								var pgrd = i2b2.PM.view.admin.yuiControls.primaryGrid;
								var un = pgrd.getSelectedRows();
								un = pgrd.getRecord(un[0]).getData();
								var ma = ' id="'+un.id+'" ';
								if (updateRow.id) {
									var mx = '<project_path>/'+i2b2.PM.view.admin.currentProject.i2b2NodeKey+'</project_path><param datatype="'+updateRow.datatype+'" id="'+updateRow.id+'" name="'+updateRow.name+'">'+updateRow.value+"</param>";
								} else {
									var mx = '<project_path>/'+i2b2.PM.view.admin.currentProject.i2b2NodeKey+'</project_path><param datatype="'+updateRow.datatype+'" name="'+updateRow.name+'">'+updateRow.value+"</param>";
								}
								i2b2.PM.ajax.setParam("PM:Admin",  {table:"cell", msg_attrib: ma, msg_xml: mx}, (function() {
									i2b2.PM.view.admin.showCellParams(selectedRec.id, "/"+i2b2.PM.view.admin.currentProject.i2b2NodeKey);
								}));
								i2b2.PM.view.admin.yuiControls.secondaryGrid.isDirty = false;
								i2b2.PM.view.admin.yuiControls.secondaryGrid.unselectAllRows();
							} else {
								errAlertMissing();
							}
						}
						break;
					case "PROJECTREC-PARAMS":
						// UPDATE PROJECT PARAMETER
						if (!Object.isUndefined(updateRow.name, updateRow.value)) {
							if (!Object.isUndefined(updateRow.name, updateRow.value)) {
								// get the required project ID 
								var ma = ' id="'+i2b2.PM.view.admin.currentProject.i2b2NodeKey+'" ';
								if (updateRow.id) {
									var mx = '<param id="'+updateRow.id+'" datatype="'+updateRow.datatype+'" name="'+updateRow.name+'">'+updateRow.value+"</param>";
								} else {
									var mx = '<param datatype="'+updateRow.datatype+'" name="'+updateRow.name+'">'+updateRow.value+"</param>";
								}
								i2b2.PM.ajax.setParam("PM:Admin",  {table:"project", msg_attrib: ma, msg_xml: mx}, (function() {
									i2b2.PM.view.admin.showProjectParams();
								}));
								i2b2.PM.view.admin.yuiControls.primaryGrid.isDirty = false;
								i2b2.PM.view.admin.yuiControls.primaryGrid.unselectAllRows();
							} else {
								errAlertMissing();
							}
						} else {
							errAlertMissing();
						}	
						break;
					case "PROJECTREC-USERS":
						if (btnLevel==1) {
							// VERIFY THAT USERNAME IS VALID!
							var usrList = i2b2.PM.ajax.getAllUser("PM:Admin", {});
							var c = i2b2.h.XPath(usrList.refXML, '//user/user_name[text() = "'+updateRow.user_name+'"]');
							if (c.length == 0) {
								alert('The username "'+updateRow.user_name+'" was not found.\nPlease check the spelling, verify that the user is active, or add the username to the hive before adding project permissions.');
								return;
							}
							// VERIFY THAT AT LEAST ONE ROLE HAS BEEN SET
							if (undefined==updateRow.roles || updateRow.roles.length == 0) {
								alert('The username "'+updateRow.user_name+'" has no roles selected.\nPlease select one or more roles or use the delete button to remove the user from the project.');
								return;
							}
							var c = updateRow.roles;

							var t = i2b2.PM.cfg.config.authRoles;

							for (var i=0; i<t.length; i++) {

								var result = i2b2.PM.ajax.deleteRole("PM:Admin", {user_id: updateRow.user_name, user_role: t[i].code, project_id:i2b2.PM.view.admin.currentProject.i2b2NodeKey});

							}

							for (var i=0; i<c.length; i++) { 

										var result = i2b2.PM.ajax.setRole("PM:Admin", {user_id: updateRow.user_name, user_role: c[i], project_id:i2b2.PM.view.admin.currentProject.i2b2NodeKey});


							}
							i2b2.PM.view.admin.yuiControls.primaryGrid.isDirty = false;
							i2b2.PM.view.admin.yuiControls.primaryGrid.unselectAllRows();
							i2b2.PM.view.admin.showProjectUsers();
						} else {
							// UPDATE USER-PROJECT PARAM
							if (!Object.isUndefined(updateRow.name, updateRow.value)) {
								// get the required cell ID from the selected row in the primary grid
								var pgrd = i2b2.PM.view.admin.yuiControls.primaryGrid;
								var un = pgrd.getSelectedRows();
								un = pgrd.getRecord(un[0]).getData();
								var ma = 'id="'+i2b2.PM.view.admin.currentProject.i2b2NodeKey+'"';
								var mx = '<user_name>'+un.user_name+'</user_name>';
								if (updateRow.id) {
									mx = mx + '<param id="'+updateRow.id+'" datatype="'+updateRow.datatype+'" name="'+updateRow.name+'">'+updateRow.value+"</param>";
								} else {
									mx = mx + '<param datatype="'+updateRow.datatype+'" name="'+updateRow.name+'">'+updateRow.value+"</param>";
								}
								i2b2.PM.ajax.setParam("PM:Admin",  {table:"project_user", msg_attrib: ma, msg_xml: mx}, (function() {
									i2b2.PM.view.admin.showUserProjParams(un.user_name, i2b2.PM.view.admin.currentProject.i2b2NodeKey);
								}));
								i2b2.PM.view.admin.yuiControls.secondaryGrid.isDirty = false;
								i2b2.PM.view.admin.yuiControls.secondaryGrid.unselectAllRows();
							} else {
								errAlertMissing();
							}
						}
						break;
					case "APPROVALS":
						if (btnLevel==1) {
							// UPDATE USER
							if (!Object.isUndefined(updateRow.name, updateRow.id)) {
				
									if (!Object.isUndefined(updateRow.expiration_date)) {
										var edate = YAHOO.util.Date.format(updateRow.expiration_date, { format: "%F"}) + "T12:00:00.000-04:00"; //  updateRow.expiration_date;
									} else {
										var edate = "";
									}
									if (!Object.isUndefined(updateRow.activation_date)) {
										var adate = YAHOO.util.Date.format(updateRow.activation_date, { format: "%F"}) +  "T12:00:00.000-04:00"; //updateRow.activation_date;
									} else {
										var adate = "";
									}

								i2b2.PM.ajax.setApproval("PM:Admin", {id:updateRow.id, name:updateRow.name, description:updateRow.description, activation_date:adate,expiration_date:edate}, i2b2.PM.view.admin.refreshScreen);
							} else {
								errAlertMissing();
							}
						} else {
							// UPDATE USER PARAMETER
							try {
								// get the required username from the selected row in the primary grid
								var pgrd = i2b2.PM.view.admin.yuiControls.primaryGrid;
								var un = pgrd.getSelectedRows();
								un = pgrd.getRecord(un[0]).getData();
								if (!Object.isUndefined(un.id, updateRow.name, updateRow.value)) {
									if (updateRow.id) {
										var t = 'id="'+updateRow.id+'"';
									} else {
										var t = "";
									}
									var vals = '<user_name>'+un.user_name+'</user_name><param '+t+' datatype="'+updateRow.datatype+'" name="'+updateRow.name+'">'+updateRow.value+'</param>';
									i2b2.PM.ajax.setParam("PM:Admin", {table:"approval", msg_xml:vals}, (function(result) {
										i2b2.PM.view.admin.showApprovalParams(un.id);
									}));
									i2b2.PM.view.admin.yuiControls.secondaryGrid.isDirty = false;
									i2b2.PM.view.admin.yuiControls.secondaryGrid.unselectAllRows();
								} else {
									errAlertMissing();
								}
							} catch(e) {
								var s="Failed to update the record";
								console.error(s);
								console.dir(e);
								alert(s);
							}
						}	
						break;						
					case "USERS":
						if (btnLevel==1) {
							// UPDATE USER
							if (!Object.isUndefined(updateRow.full_name, updateRow.user_name)) {
								if (updateRow.password != "") {
									updateRow.password = "<password>"+updateRow.password+"</password>";
								}
								i2b2.PM.ajax.setUser("PM:Admin", {user_name:updateRow.user_name, full_name:updateRow.full_name, is_admin:updateRow.is_admin, email:updateRow.email, password:updateRow.password}, i2b2.PM.view.admin.refreshScreen);
							} else {
								errAlertMissing();
							}
						} else {
							// UPDATE USER PARAMETER
							try {
								// get the required username from the selected row in the primary grid
								var pgrd = i2b2.PM.view.admin.yuiControls.primaryGrid;
								var un = pgrd.getSelectedRows();
								un = pgrd.getRecord(un[0]).getData();
								if (!Object.isUndefined(un.user_name, updateRow.name, updateRow.value)) {
									if (updateRow.id) {
										var t = 'id="'+updateRow.id+'"';
									} else {
										var t = "";
									}
									var vals = '<user_name>'+un.user_name+'</user_name><param '+t+' datatype="'+updateRow.datatype+'" name="'+updateRow.name+'">'+updateRow.value+'</param>';
									i2b2.PM.ajax.setParam("PM:Admin", {table:"user", msg_xml:vals}, (function(result) {
										i2b2.PM.view.admin.showUserParams(un.user_name);
									}));
									i2b2.PM.view.admin.yuiControls.secondaryGrid.isDirty = false;
									i2b2.PM.view.admin.yuiControls.secondaryGrid.unselectAllRows();
								} else {
									errAlertMissing();
								}
							} catch(e) {
								var s="Failed to update the record";
								console.error(s);
								console.dir(e);
								alert(s);
							}
						}	
						break;
				}
			}
			break;
		case "NEW":
			// abandon dirty data?
			try {
				if (i2b2.PM.view.admin.yuiControls.primaryGrid.isDirty || i2b2.PM.view.admin.yuiControls.secondaryGrid.isDirty) {
					if (confirm("Abandon Changes?")) {
						// clear previously added/edited row in primary data
						i2b2.PM.view.admin.treeClick(false,true);
					}
					return;
				} 
			} catch(e) {}
			// create blank record
			var t = {};
			t[trgtColDefs[0].key] = "";
			trgtGrid.unselectAllRows();
			trgtGrid.set("sortedBy", null);
			trgtGrid.addRow(t,0);
			if (!Object.isUndefined(trgtGrid.configs.paginator)) {
				trgtGrid.configs.paginator.setPage(1);			
			}
			trgtGrid.selectRow(0);
			trgtGrid.isDirty = true;
			if (btnLevel == 1) {
				// hide the secondary grid
				$('pmAdminParamTableview').hide();
				$('pmAdminParamTableviewButtons').hide();
			}
	}		
};



i2b2.PM.view.admin.refreshScreen = function() {
	if (i2b2.PM.view.admin.yuiControls.primaryGrid) {
		i2b2.PM.view.admin.yuiControls.primaryGrid.isDirty = false;
	}
	if (i2b2.PM.view.admin.yuiControls.secondaryGrid) {
		i2b2.PM.view.admin.yuiControls.secondaryGrid.isDirty = false;
	}
	i2b2.PM.view.admin.treeClick(null,true);
};

i2b2.PM.admin.deleteProject = function() {
	var proj_id = $('pmAdmin-projID').value; 
	var proj_path = $('pmAdmin-projPath').value;
	if (proj_id=="" || proj_path=="") {
		alert('Project ID and project path are required!');
		return false;
	}
	if (confirm("Are you sure you want to delete this project?")) {
		i2b2.PM.ajax.deleteProject("PM:Admin", {project_id:proj_id, project_path:proj_path}, (function(result) {
			// restore screen
			$('pmMainTitle').innerHTML = "Project List";
			i2b2.PM.view.admin.showInfoPanel("PROJECT");
			i2b2.PM.view.admin.configScreenDispay(0);
			// refresh the project listings
			var evt = {node: i2b2.PM.view.admin.yuiTreeNodePROJECTS};
			i2b2.PM.view.admin.treeClick(evt, false);
		}));
	}
};

i2b2.PM.admin.saveProject = function() {
	var projData = {};
	// verify all required info is presented
	var errstr = "";
	var t = $('pmAdmin-projID').value; 
	if (t=="") {
		errstr = errstr + '\n Project ID is a required field';
	} else {
		projData.id = t;
	}
	var t = $('pmAdmin-projName').value;
	if (t=="") {
		errstr = errstr + '\n Project Name is a required field';
	} else {
		projData.name = t;
	}
	if (errstr != "") {
		alert("The following errors have occured:\n"+errstr);
		return;
	}
	// send data
	projData.wiki = $('pmAdmin-projWiki').value;
	projData.key = $('pmAdmin-projKey').value;
	if (projData.key == "") {
		projData.key = $('pmAdmin-projOrigKey').value;
	} else {
		var t = hex_md5(projData.key);
		projData.key = t.substr(0,3);
	}
	projData.description = $('pmAdmin-projDesc').value;
	projData.path = $('pmAdmin-projPath').value;
	i2b2.PM.ajax.setProject("PM:Admin", projData, i2b2.PM.admin.refreshProjects);
	// restore screen
	$('pmMainTitle').innerHTML = "Project List";
	i2b2.PM.view.admin.showInfoPanel("PROJECT");
	i2b2.PM.view.admin.configScreenDispay(0); 
};

i2b2.PM.admin.saveProjectUser = function() {
	var projData = {};
	// verify all required info is presented
	

	var result = i2b2.PM.ajax.deleteRole("PM:Admin", {user_id: i2b2.PM.view.admin.currentProject.i2b2NodeUsername, user_role: "USER", project_id:i2b2.PM.view.admin.currentProject.i2b2NodeKey});
	var result = i2b2.PM.ajax.deleteRole("PM:Admin", {user_id: i2b2.PM.view.admin.currentProject.i2b2NodeUsername, user_role: "DATA_OBFSC", project_id:i2b2.PM.view.admin.currentProject.i2b2NodeKey});	

	var tRole = "USER";
	if ( $('RoleMANAGER').checked) {
		tRole = "MANAGER";	
	}
	i2b2.PM.ajax.setRole("PM:Admin", {user_id: i2b2.PM.view.admin.currentProject.i2b2NodeUsername, user_role: tRole, project_id:i2b2.PM.view.admin.currentProject.i2b2NodeKey});
	tRole = "DATA_OBFSC";
	if ( $('RoleDATA_PROT').checked) {
		tRole = "DATA_PROT"	
	} else if ( $('RoleDATA_DEID').checked) {
		tRole = "DATA_DEID";	
	} else if ( $('RoleDATA_LDS').checked) {
		tRole = "DATA_LDS";	
	} else if ( $('RoleDATA_AGG').checked) {
		tRole = "DATA_AGG";	
	} 
	i2b2.PM.ajax.setRole("PM:Admin", {user_id: i2b2.PM.view.admin.currentProject.i2b2NodeUsername, user_role: tRole, project_id:i2b2.PM.view.admin.currentProject.i2b2NodeKey});


	if ( $('RoleEDITOR').checked) {
		i2b2.PM.ajax.setRole("PM:Admin", {user_id: i2b2.PM.view.admin.currentProject.i2b2NodeUsername, user_role: "EDITOR", project_id:i2b2.PM.view.admin.currentProject.i2b2NodeKey});
	} else {
		i2b2.PM.ajax.deleteRole("PM:Admin", {user_id: i2b2.PM.view.admin.currentProject.i2b2NodeUsername, user_role: "EDITOR", project_id:i2b2.PM.view.admin.currentProject.i2b2NodeKey});		
	}





//	i2b2.PM.ajax.setProject("PM:Admin", projData, i2b2.PM.admin.refreshProjects);
	// restore screen
	$('pmMainTitle').innerHTML = "Project List";
	i2b2.PM.view.admin.showInfoPanel("PROJECT");
	i2b2.PM.view.admin.configScreenDispay(0); 
};

i2b2.PM.admin.deleteProjectUser = function() {
	var projData = {};
	// verify all required info is presented
	

	var result = i2b2.PM.ajax.deleteRole("PM:Admin", {user_id: i2b2.PM.view.admin.currentProject.i2b2NodeUsername, user_role: "USER", project_id:i2b2.PM.view.admin.currentProject.i2b2NodeKey});
	var result = i2b2.PM.ajax.deleteRole("PM:Admin", {user_id: i2b2.PM.view.admin.currentProject.i2b2NodeUsername, user_role: "DATA_OBFSC", project_id:i2b2.PM.view.admin.currentProject.i2b2NodeKey});	
	var result = i2b2.PM.ajax.deleteRole("PM:Admin", {user_id: i2b2.PM.view.admin.currentProject.i2b2NodeUsername, user_role: "EDITOR", project_id:i2b2.PM.view.admin.currentProject.i2b2NodeKey});	
	$('pmMainTitle').innerHTML = "Project List";
	i2b2.PM.view.admin.showInfoPanel("PROJECT");
	i2b2.PM.view.admin.configScreenDispay(0); 
};



i2b2.PM.admin.deleteApproval = function() {
	var proj_id = $('pmAdmin-approvalID').value; 
	if (proj_id=="") {
		alert('Approval ID is required!');
		return false;
	}
	if (confirm("Are you sure you want to delete this approval?")) {
		i2b2.PM.ajax.deleteApproval("PM:Admin", {id:proj_id}, (function(result) {
			// restore screen
			$('pmMainTitle').innerHTML = "Approval List";
			i2b2.PM.view.admin.showInfoPanel("APPROVAL");
			i2b2.PM.view.admin.configScreenDispay(0);
			// refresh the project listings
			var evt = {node: i2b2.PM.view.admin.yuiTreeNodeAPPROVALS};
			i2b2.PM.view.admin.treeClick(evt, false);
		}));
	}
};

i2b2.PM.admin.saveApproval = function() {
	var projData = {};
	// verify all required info is presented
	var errstr = "";
	var t = $('pmAdmin-approvalID').value; 
	if (t=="") {
		errstr = errstr + '\n Approval ID is a required field';
	} else {
		projData.id = t;
	}
	var t = $('pmAdmin-approvalName').value;
	if (t=="") {
		errstr = errstr + '\n Approval Name is a required field';
	} else {
		projData.name = t;
	}
	if (errstr != "") {
		alert("The following errors have occured:\n"+errstr);
		return;
	}
	// send data
	projData.description = $('pmAdmin-approvalDescription').value;
	projData.activation_date = $('pmAdmin-approvalActivation').value;
	projData.expiration_date = $('pmAdmin-approvalExpiration').value;
	
	
	if (!Object.isUndefined(projData.expiration_date)) {
			 projData.expiration_date = YAHOO.util.Date.format(projData.expiration_date, { format: "%F"}) + "T12:00:00.000-04:00"; 
	} else {
			 projData.expiration_date = "";
	}
	if (!Object.isUndefined(projData.activation_date)) {
			 projData.activation_date = YAHOO.util.Date.format(projData.activation_date, { format: "%F"}) +  "T12:00:00.000-04:00"; 
	} else {
			 projData.activation_date = "";
	}
	
	
	i2b2.PM.ajax.setApproval("PM:Admin", projData, i2b2.PM.admin.refreshApprovals);
	// restore screen
	$('pmMainTitle').innerHTML = "Approval List";
	i2b2.PM.view.admin.showInfoPanel("APPROVAL");
	i2b2.PM.view.admin.configScreenDispay(0); 
};


i2b2.PM.admin.saveDomain = function() {
	var projData = {};
	// verify all required info is presented
	var errstr = "";
	var t = $('pmAdmin-hiveID').value; 
	if (t=="") {
		errstr = errstr + '\n Hive ID is a required field';
	} else {
		projData.id = t;
	}
	var t = $('pmAdmin-hiveName').value;
	if (t=="") {
		errstr = errstr + '\n Hive Name is a required field';
	} else {
		projData.name = t;
	}
	if (errstr != "") {
		alert("The following errors have occured:\n"+errstr);
		return;
	}
	// send data
	projData.helpurl = $('pmAdmin-hiveHelpURL').value;
	projData.environment = $('pmAdmin-hiveEnvironment').value;

	i2b2.PM.ajax.setHive("PM:Admin", {domain_id:projData.id, domain_name:projData.name, environment:projData.environment, helpURL:projData.helpurl});
	//i2b2.PM.ajax.setProject("PM:Admin", projData, i2b2.PM.admin.refreshProjects);
	// restore screen
	$('pmMainTitle').innerHTML = "Hive List";
	i2b2.PM.view.admin.showInfoPanel("HIVE");
	i2b2.PM.view.admin.configScreenDispay(0); 
};

i2b2.PM.admin.deleteUser = function() {
	var user_name = $('pmAdmin-userName').value; 
	if (user_name =="") {
		alert('User Name are required!');
		return false;
	}
	if (confirm("Are you sure you want to delete this User?")) {
		i2b2.PM.ajax.deleteUser("PM:Admin", {user_name:user_name}, (function(result) {
			// restore screen
			$('pmMainTitle').innerHTML = "User List";
			i2b2.PM.view.admin.showInfoPanel("USER");
			i2b2.PM.view.admin.configScreenDispay(0);
			// refresh the project listings
			var evt = {node: i2b2.PM.view.admin.yuiTreeNodeUSERS};
			i2b2.PM.view.admin.treeClick(evt, false);
		}));
	}
};

i2b2.PM.admin.saveUser = function() {
	var userData = {};
	// verify all required info is presented
	var errstr = "";
	var t = $('pmAdmin-userName').value; 
	if (t=="") {
		errstr = errstr + '\n User Name is a required field';
	} else {
		userData.user_name = t;
	}
	var t = $('pmAdmin-userFullname').value;
	if (t=="") {
		errstr = errstr + '\n Full Name is a required field';
	} else {
		userData.full_name = t;
	}
    if ( ($('pmAdmin-newUser').value == "true") && ($('pmAdmin-userPasswd1').value == "") )
	{
		errstr = errstr + '\n Password is a required field';
	}
	userData.is_admin = $('pmAdmin-userIsAdmin').value; 

	userData.password = "";
	if ($('pmAdmin-userPasswd1').value!=$('pmAdmin-userPasswd2').value) {
		errstr = errstr + '\n Passwords need to be the same';
	} else {
		if ($('pmAdmin-userPasswd1').value != "") {
			userData.password = "<password>"+ $('pmAdmin-userPasswd1').value +"</password>";
		}
	}	
	if (errstr != "") {
		alert("The following errors have occured:\n"+errstr);
		return;
	}
	// send data
	userData.email = $('pmAdmin-userEmail').value;
	i2b2.PM.ajax.setUser("PM:Admin", userData, i2b2.PM.admin.refreshUsers);
	// restore screen
	$('pmMainTitle').innerHTML = "User List";
	i2b2.PM.view.admin.showInfoPanel("USER");
	i2b2.PM.view.admin.configScreenDispay(0); 
};



i2b2.PM.admin.addUserProject = function() {
	var userData = {};
	// verify all required info is presented
	var errstr = "";
	var t = $('pmAdmin-userName').value; 
	if (t=="") {
		errstr = errstr + '\n User Name is a required field';
	} else {
		userData.user_name = t;
	}


	var usrList = i2b2.PM.ajax.getAllUser("PM:Admin", {});
	var c = i2b2.h.XPath(usrList.refXML, '//user/user_name[text() = "'+userData.user_name+'"]');
	if (c.length == 0) {
			errstr = 'The username "'+userData.user_name+'" was not found.\nPlease check the spelling, verify that the user is active, or add the username to the hive before adding project permissions.';
	}
	
	if (errstr != "") {
		alert("The following errors have occured:\n"+errstr);
		return;
	}

	var result = i2b2.PM.ajax.setRole("PM:Admin", {user_id: t, user_role: "USER", project_id:i2b2.PM.view.admin.currentProject.i2b2NodeKey});
	var result = i2b2.PM.ajax.setRole("PM:Admin", {user_id: t, user_role: "DATA_OBFSC", project_id:i2b2.PM.view.admin.currentProject.i2b2NodeKey},i2b2.PM.admin.refreshProjects);


	//i2b2.PM.ajax.setUser("PM:Admin", userData, i2b2.PM.admin.refreshUsers);
	// restore screen
	$('pmMainTitle').innerHTML = "Project List";
	i2b2.PM.view.admin.showInfoPanel("PROJECTUSERS");
	i2b2.PM.view.admin.configScreenDispay(0); 
};




i2b2.PM.admin.deleteCell = function() {
	var id = $('pmAdmin-cellID').value; 
	if (id =="") {
		alert('Cell Id are required!');
		return false;
	}
	var project_path = $('pmAdmin-cellProjPath').value;
	if (confirm("Are you sure you want to delete this Cell?")) {
		i2b2.PM.ajax.deleteCell("PM:Admin", {id:id, project_path:project_path}, (function(result) {
			// restore screen
			$('pmMainTitle').innerHTML = "Cell List";
			i2b2.PM.view.admin.showInfoPanel("CELL");
			i2b2.PM.view.admin.configScreenDispay(0);
			// refresh the project listings
			var evt = {node: i2b2.PM.view.admin.yuiTreeNodeCELLS};
			i2b2.PM.view.admin.treeClick(evt, false);
		}));
	}
};

i2b2.PM.admin.saveCell = function() {
	var userData = {};
	// verify all required info is presented
	var errstr = "";
	var t = $('pmAdmin-cellID').value; 
	if (t=="") {
		errstr = errstr + '\n Cell Id is a required field';
	} else {
		userData.cell_id = t;
	}
	var t = $('pmAdmin-cellName').value;
	if (t=="") {
		errstr = errstr + '\n Cell Name is a required field';
	} else {
		userData.name = t;
	}
	var t = $('pmAdmin-cellURL').value;
	if (t=="") {
		errstr = errstr + '\n Cell URL is a required field';
	} else {
		userData.url = t;
	}
	var t = $('pmAdmin-cellProjPath').value;
	if (t=="") {
		userData.project_path = '\\';
	} else {
		userData.project_path = t;
	}

	
	if (errstr != "") {
		alert("The following errors have occured:\n"+errstr);
		return;
	}
	// send data
	userData.method = $('pmAdmin-cellMethod').value;
	i2b2.PM.ajax.setCell("PM:Admin", userData, i2b2.PM.admin.refreshCells);
	// restore screen
	$('pmMainTitle').innerHTML = "Cell List";
	i2b2.PM.view.admin.showInfoPanel("CELL");
	i2b2.PM.view.admin.configScreenDispay(0); 
};





i2b2.PM.admin.deleteParameter = function() {
	var id = $('pmAdmin-paramId').value; 
	if (id =="") {
		alert('ID are required!');
		return false;
	}
	if (confirm("Are you sure you want to delete this Parameter?")) {
		i2b2.PM.ajax.deleteParam("PM:Admin", {msg_xml:id, table:i2b2.PM.view.admin.parentParams.data.i2b2Table}, (function(result) {
			// restore screen
			$('pmMainTitle').innerHTML = "Parameter List";
			i2b2.PM.view.admin.showInfoPanel("PARAM");
			i2b2.PM.view.admin.configScreenDispay(0);
			// refresh the project listings
			var evt = {node: i2b2.PM.view.admin.yuiTreeNodeUSERS};
			i2b2.PM.view.admin.treeClick(evt, false);
		}));
	}
};

i2b2.PM.admin.saveParameter = function() {
	var userData = {};
	// verify all required info is presented
	var errstr = "";
	var t = $('pmAdmin-paramName').value; 
	if (t=="") {
		errstr = errstr + '\n Name is a required field';
	} else {
		userData.name = t;
	}
	var t = $('pmAdmin-paramValue').value;
	if (t=="") {
		errstr = errstr + '\n Parameter Value is a required field';
	} else {
		userData.value = t;
	}

	if (errstr != "") {
		alert("The following errors have occured:\n"+errstr);
		return;
	}
	// send data
	//userData.table = $('pmAdmin-paramTable').value;
	userData.datatype = $('pmAdmin-paramDatatype').value;
	userData.id = $('pmAdmin-paramId').value;
	//	userData.//<param name="{{{param_name}}}" datatype="{{{param_datatype}}}" {{{param_id_attrib}}} >{{{param_value}}}</param>\n'+
	
	
					switch (i2b2.PM.view.admin.parentParams.data.i2b2Table) {
					case "global":
							// UPDATE A HIVE DOMAIN PARAM
						//var ma = 'id="'+un.domain_id+'"';
								var ma = '';
								if (userData.id) {
									var mx = '<project_path>/</project_path><can_override>Y</can_override><param datatype="'+userData.datatype+'" id="'+userData.id+'" name="'+userData.name+'">'+userData.value+"</param>";
								} else {
									var mx = '<project_path>/</project_path><can_override>Y</can_override><param datatype="'+userData.datatype+'" name="'+userData.name+'">'+userData.value+"</param>";
								}					
						break;						
					case "hive_param":
							// UPDATE A HIVE DOMAIN PARAM
						//var ma = 'id="'+un.domain_id+'"';
								var ma = '';
								if (userData.id) {
									var mx = '<project_path>' +userData.path +'</project_path><domain_id>'+userData.domain_id+'</domain_id><param datatype="'+userData.datatype+'" id="'+userData.id+'" name="'+userData.name+'">'+userData.value+"</param>";
								} else {
									var mx = '<project_path>' +userData.path +'</project_path><domain_id>'+userData.domain_id+'</domain_id><param datatype="'+userData.datatype+'" name="'+userData.name+'">'+userData.value+"</param>";
								}					
						break;
					case "cell_param":
								var ma = ' id="'+i2b2.PM.view.admin.parentParams.data.id+'" ';
								if (userData.id) {
									var mx =  i2b2.PM.view.admin.parentParams.data.xmlData + '<param datatype="'+userData.datatype+'" id="'+userData.id+'" name="'+userData.name+'">'+userData.value+"</param>";
								} else {
									var mx = i2b2.PM.view.admin.parentParams.data.xmlData + '<param datatype="'+userData.datatype+'" name="'+userData.name+'">'+userData.value+"</param>";
								}
						break;
					case "user_param":
								var ma = '';
								if (userData.id) {
										var t = 'id="'+userData.id+'"';
									} else {
										var t = "";
									}
									
									var mx = i2b2.PM.view.admin.parentParams.data.xmlData + '<param '+t+' datatype="'+userData.datatype+'" name="'+userData.name+'">'+userData.value+'</param>';
						break;			
					case "project_param":
								var ma = ' id="'+i2b2.PM.view.admin.parentParams.id+'" ';
								if (userData.id) {
									var mx = '<project_path>'+i2b2.PM.view.admin.parentParams.data.i2b2NodePath+'</project_path><param id="'+userData.id+'" datatype="'+userData.datatype+'" name="'+userData.name+'">'+userData.value+"</param>";
								} else {
									var mx = '<project_path>'+i2b2.PM.view.admin.parentParams.data.i2b2NodePath+'</project_path><param datatype="'+userData.datatype+'" name="'+userData.name+'">'+userData.value+"</param>";
								}
						break;
					case "project_user_param":
								var ma = ' id="'+i2b2.PM.view.admin.parentParams.data.id+'" ';
								if (!Object.isUndefined(userData.id)) {
									var mx = i2b2.PM.view.admin.parentParams.data.xmlData+'<param id="'+userData.id+'" datatype="'+userData.datatype+'" name="'+userData.name+'">'+userData.value+"</param>";
								} else {
									var mx = i2b2.PM.view.admin.parentParams.data.xmlData+'<param datatype="'+userData.datatype+'" name="'+userData.name+'">'+userData.value+"</param>";
								}
						break;
						
					}

	
	i2b2.PM.ajax.setParam("PM:Admin",  {table: i2b2.PM.view.admin.parentParams.data.i2b2Table, msg_attrib: ma, msg_xml: mx}, i2b2.PM.admin.refreshParameters);
	//i2b2.PM.ajax.setParam("PM:Admin", userData, i2b2.PM.admin.refreshParameters);
	// restore screen
	$('pmMainTitle').innerHTML = "Parameter List";
	i2b2.PM.view.admin.showInfoPanel("PARAM");
	i2b2.PM.view.admin.configScreenDispay(0); 
};



// refresh treeview lists
// --------------------------------------------------------------------------------------------------------------------
i2b2.PM.admin.refreshProjects = function(tvNode, onCompleteCallback) {
	i2b2.PM.admin.refreshProjectListData();
	for (var idx in i2b2.PM.model.admin.ProjectList) {
		var d = i2b2.PM.model.admin.ProjectList[idx];
		var tmpNode = new YAHOO.widget.TextNode({label: d.name, expanded: false}, tvNode);
		tmpNode.data.i2b2NodeType = "PROJECTREC";
		tmpNode.data.i2b2NodeKey = d.id;
		tmpNode.data.i2b2NodePath = d.path;
	//	var tmpNode2 = new YAHOO.widget.TextNode({label: "Cells", expanded: false}, tmpNode);
	//	tmpNode2.data.i2b2NodeType = "PROJECTREC-CELLS";
		i2b2.PM.view.admin.yuiTreeNodePARAMS = new YAHOO.widget.TextNode({label: "Params", expanded: false}, tmpNode);
			i2b2.PM.view.admin.yuiTreeNodePARAMS.id = d.id;
			i2b2.PM.view.admin.yuiTreeNodePARAMS.data.xmlId = "";
			i2b2.PM.view.admin.yuiTreeNodePARAMS.data.i2b2NodeType = "PARAMS";
			i2b2.PM.view.admin.yuiTreeNodePARAMS.data.i2b2Table = "project_param";
			i2b2.PM.view.admin.yuiTreeNodePARAMS.data.i2b2NodePath = d.path;
			
			i2b2.PM.view.admin.yuiTreeNodePARAMS.data.xmlData = d.id;
			i2b2.PM.view.admin.yuiTreeNodePARAMS.setDynamicLoad(i2b2.PM.admin.refreshParameters);	


		//tmpNode2.data.i2b2NodeType = "PROJECTREC-PARAMS";
		
		i2b2.PM.view.admin.yuiTreeNodePROJECTSUSERS = new YAHOO.widget.TextNode({label: "Users", expanded: false}, tmpNode);
		i2b2.PM.view.admin.yuiTreeNodePROJECTSUSERS.id = d.id;
		i2b2.PM.view.admin.yuiTreeNodePROJECTSUSERS.data.i2b2NodeType = "PROJECTUSERS";
		i2b2.PM.view.admin.yuiTreeNodePROJECTSUSERS.setDynamicLoad(i2b2.PM.admin.refreshProjectsUsers);		
		//var tmpNode2 = new YAHOO.widget.TextNode({label: "Users", expanded: false}, tmpNode);
		//tmpNode2.data.i2b2NodeType = "PROJECTREC-USERS";
	}
	if (onCompleteCallback) { onCompleteCallback(); }
};

// refresh treeview lists
// --------------------------------------------------------------------------------------------------------------------
i2b2.PM.admin.refreshUsers = function(tvNode, onCompleteCallback) {
	i2b2.PM.admin.refreshUserListData();
	for (var idx in i2b2.PM.model.admin.UserList) {
		var d = i2b2.PM.model.admin.UserList[idx];
		var tmpNode = new YAHOO.widget.TextNode({label: d.full_name, expanded: false}, tvNode);
		tmpNode.data.i2b2NodeType = "USERREC";
		tmpNode.data.i2b2NodeUserName = d.user_name;
		//var tmpNode2 = new YAHOO.widget.TextNode({label: "Params", expanded: false}, tmpNode);
		//tmpNode2.data.i2b2NodeType = "USERREC-PARAMS";
			i2b2.PM.view.admin.yuiTreeNodePARAMS = new YAHOO.widget.TextNode({label: "Params", expanded: false}, tmpNode);
			i2b2.PM.view.admin.yuiTreeNodePARAMS.id = d.id;
			i2b2.PM.view.admin.yuiTreeNodePARAMS.data.xmlId = "";
			i2b2.PM.view.admin.yuiTreeNodePARAMS.data.i2b2NodeType = "PARAMS";
			i2b2.PM.view.admin.yuiTreeNodePARAMS.data.i2b2Table = "user_param";
			i2b2.PM.view.admin.yuiTreeNodePARAMS.data.xmlData = "<user_name>"+d.user_name+"</user_name>";
			i2b2.PM.view.admin.yuiTreeNodePARAMS.setDynamicLoad(i2b2.PM.admin.refreshParameters);		
		
		
	}
	if (onCompleteCallback) { onCompleteCallback(); }
};

// refresh treeview lists
// --------------------------------------------------------------------------------------------------------------------
i2b2.PM.admin.refreshTree = function(tvNode, onCompleteCallback) {
		switch(tvNode.data.i2b2NodeType) {
		case "HIVECELLS":
			i2b2.PM.model.admin.CellList = i2b2.PM.admin.refreshTreeListData( i2b2.PM.ajax.getAllCell("PM:Admin", {}));
			for (var idx in i2b2.PM.model.admin.CellList) {
				var d = i2b2.PM.model.admin.CellList[idx];
				var tmpNode = new YAHOO.widget.TextNode({label: d.name, expanded: false}, tvNode);
				tmpNode.data.i2b2NodeType = "CELLREC";
				tmpNode.data.i2b2NodePath = d.project_path;
				tmpNode.data.i2b2NodeKey = d.id;
				i2b2.PM.view.admin.yuiTreeNodePARAMS = new YAHOO.widget.TextNode({label: "Params", expanded: false}, tmpNode);
				i2b2.PM.view.admin.yuiTreeNodePARAMS.data.xmlId = "id='" + d.id + "'";
				i2b2.PM.view.admin.yuiTreeNodePARAMS.data.id = d.id;
				i2b2.PM.view.admin.yuiTreeNodePARAMS.data.i2b2Table = "cell_param";
				i2b2.PM.view.admin.yuiTreeNodePARAMS.data.xmlData = "<project_path>"+d.project_path+"</project_path>";
				
				i2b2.PM.view.admin.yuiTreeNodePARAMS.data.i2b2NodeType = "PARAMS";
				i2b2.PM.view.admin.yuiTreeNodePARAMS.setDynamicLoad(i2b2.PM.admin.refreshParameters);			
			}
			break;
		case "PROJECTREQUESTS":
			i2b2.PM.model.admin.PrjRequestList = i2b2.PM.admin.refreshTreeListData( i2b2.PM.ajax.getAllProjectRequest("PM:Admin", {}));
			for (var idx in i2b2.PM.model.admin.PrjRequestList) {
				var d = i2b2.PM.model.admin.PrjRequestList[idx];
				var tmpNode = new YAHOO.widget.TextNode({label: d.title, expanded: false}, tvNode);
				tmpNode.data.i2b2NodeType = "PROJECTREQUESTREC";
				tmpNode.data.i2b2NodeTitle = d.title;
				tmpNode.data.i2b2NodeId = d.id;
				tmpNode.data.i2b2ProjectId = d.project_id;
				tmpNode.data.i2b2RequestXml = d.request_xml;
				tmpNode.data.i2b2SubmitBy = d.submit_char;
				tmpNode.data.i2b2EntryDate = d.entry_date;
			}

			break;
		case "APPROVALS":
			i2b2.PM.model.admin.ApprovalList = i2b2.PM.admin.refreshTreeListData( i2b2.PM.ajax.getAllApproval("PM:Admin", {}));
			for (var idx in i2b2.PM.model.admin.ApprovalList) {
				var d = i2b2.PM.model.admin.ApprovalList[idx];
				var tmpNode = new YAHOO.widget.TextNode({label: d.name, expanded: false}, tvNode);
				tmpNode.data.i2b2NodeType = "APPROVALREC";
				tmpNode.data.i2b2NodeName = d.name;
				tmpNode.data.i2b2NodeId = d.id;
			}
		
			break;
	}
	if (onCompleteCallback) { onCompleteCallback(); }
};



// refresh treeview lists
// --------------------------------------------------------------------------------------------------------------------
i2b2.PM.admin.refreshProjectsUsers = function(tvNode, onCompleteCallback) {
	i2b2.PM.view.admin.currentProject = tvNode.parent.data;
	i2b2.PM.admin.refreshProjectUserListData();
	for (var idx in i2b2.PM.model.admin.ProjectUserList) {
		if (idx != "undefined") {
			var d = i2b2.PM.model.admin.ProjectUserList[idx];
			var tmpNode = new YAHOO.widget.TextNode({label: d.username, expanded: false}, tvNode);
			tmpNode.data.i2b2NodeType = "PROJECTREC-USERS-ROLESNEW";
			tmpNode.data.i2b2NodeUsername = d.username;
			var tmpNode2 = new YAHOO.widget.TextNode({label: "Roles", expanded: false}, tmpNode);
			tmpNode2.data.i2b2NodeType = "PROJECTREC-USERS-ROLES";
			
	
			i2b2.PM.view.admin.yuiTreeNodePARAMS = new YAHOO.widget.TextNode({label: "Params", expanded: false}, tmpNode);
			i2b2.PM.view.admin.yuiTreeNodePARAMS.data.id = d.projectid;
			i2b2.PM.view.admin.yuiTreeNodePARAMS.data.xmlId = "";
			i2b2.PM.view.admin.yuiTreeNodePARAMS.data.i2b2Table = "project_user_param";
			i2b2.PM.view.admin.yuiTreeNodePARAMS.data.xmlData = "<path>"+d.projectid+"</path><user_name>"+d.username+"</user_name>";
			
			i2b2.PM.view.admin.yuiTreeNodePARAMS.data.i2b2NodeType = "PARAMS";
			i2b2.PM.view.admin.yuiTreeNodePARAMS.setDynamicLoad(i2b2.PM.admin.refreshParameters);		
		}
				
	}	
	if (onCompleteCallback) { onCompleteCallback(); }
};

// refresh treeview lists
// --------------------------------------------------------------------------------------------------------------------
i2b2.PM.admin.refreshParameters = function(tvNode, onCompleteCallback) {
	i2b2.PM.admin.refreshParameterListData(tvNode.data.i2b2Table, tvNode.data.xmlData, tvNode.data.xmlId);
										   //"<user_name>"+tvNode.parent.data.i2b2NodeUserName+"</user_name>");
										   
	i2b2.PM.view.admin.parentParams = tvNode;									   
	for (var idx in i2b2.PM.model.admin.ParameterList) {
		var d = i2b2.PM.model.admin.ParameterList[idx];
		var tmpNode = new YAHOO.widget.TextNode({label: d.name, expanded: false}, tvNode);
		tmpNode.data.i2b2NodeType = "REC-PARAMS";
		tmpNode.data.i2b2Name = d.name;
		tmpNode.data.i2b2Id = d.id;

}	
	if (onCompleteCallback) { onCompleteCallback(); }
};

// treeview click handler & action router
// --------------------------------------------------------------------------------------------------------------------
i2b2.PM.view.admin.treeClick = function(tvEvent, override) {
	if (i2b2.PM.view.admin.checkDirtyFlags()) { return; }
	if (override) {
		var info = i2b2.PM.view.admin.clickedTreeNode;
	} else {
		var info = tvEvent.node.data;
		i2b2.PM.view.admin.clickedTreeNode = info;
	}
	console.debug("treeview node clicked: "+info.i2b2NodeType);
	i2b2.PM.view.admin.configScreen = info.i2b2NodeType;
	switch(i2b2.PM.view.admin.configScreen) {
		case "HIVE":
			delete i2b2.PM.view.admin.currentProject;
			$('pmMainTitle').innerHTML = "Hive Overview";
			i2b2.PM.view.admin.showInfoPanel("HIVE");
			i2b2.PM.view.admin.configScreenDispay(0);
		case "HIVEDOMAINS":
			delete i2b2.PM.view.admin.currentProject;
			i2b2.PM.view.admin.showInfoPanel("HIVEDOMAIN");
			i2b2.PM.view.admin.configScreenDispay(0);
			
			try {				
				var response = i2b2.PM.ajax.getAllHive("PM:Admin", {proj_code:info.i2b2NodeKey, proj_path:info.i2b2NodePath});
				response.parse();
				
				var tmp = response.model;
				var l = tmp.length;
				for (var i=0;i<l;i++) {
					if (Boolean.parseTo(tmp[i].active)) {
						if (tmp[i].environment) { $('pmAdmin-hiveEnvironment').value = tmp[i].environment; }
						if (tmp[i].helpURL) { $('pmAdmin-hiveHelpURL').value = tmp[i].helpURL; }
						if (tmp[i].domain_name) { $('pmAdmin-hiveName').value = tmp[i].domain_name; }
						if (tmp[i].domain_id) { $('pmAdmin-hiveID').value = tmp[i].domain_id; }
					}
				}
			} catch (e) {}			
			//i2b2.PM.view.admin.showHiveDomains();
			break;
		case "HIVECELLS":
			delete i2b2.PM.view.admin.currentProject;
			$('pmMainTitle').innerHTML = "Cell List";
			i2b2.PM.view.admin.showHiveCells();
			i2b2.PM.view.admin.showInfoPanel("CELL");	
			tvEvent.node.tree.removeChildren(tvEvent.node);
			
		break;
		case "CELLREC":
			try {
				$('pmMainTitle').innerHTML = 'Cell &gt; "'+info.i2b2NodeKey+'"';
				i2b2.PM.view.admin.showInfoPanel("CELLREC");
				var response = i2b2.PM.ajax.getCell("PM:Admin", {cell_id:info.i2b2NodeKey, proj_path:info.i2b2NodePath});
				response.parse();
				var data = response.model[0];
			
				if (data.id) { $('pmAdmin-cellID').value = data.id; }
				if (data.name) { $('pmAdmin-cellName').value = data.name; }
				if (data.url) { $('pmAdmin-cellURL').value = data.url; }
				if (data.method) { $('pmAdmin-cellMethod').value = data.method; }
				if (data.project_path) {
					$('pmAdmin-cellProjPath').value = data.project_path; 
				} else {
					$('pmAdmin-cellProjPath').value = '/';
				}
			} catch (e) {}
			i2b2.PM.view.admin.configScreenDispay(0);
			break;			
		case "HIVEGLOBALS":
			delete i2b2.PM.view.admin.currentProject;
			i2b2.PM.view.admin.showGlobals();
			break;
		case "PROJECTS":
			var proj_data = tvEvent.node.data;
			i2b2.PM.view.admin.currentProject = proj_data;
			$('pmMainTitle').innerHTML = "Project List";
			i2b2.PM.view.admin.showProjects();
			i2b2.PM.view.admin.showInfoPanel("PROJECT");
			tvEvent.node.tree.removeChildren(tvEvent.node);
			break;
		case "APPROVALREC":
			try {
				$('pmMainTitle').innerHTML = 'APPROVAL &gt; "'+info.i2b2NodeKey+'"';
				i2b2.PM.view.admin.showInfoPanel("APPROVALREC");
				var response = i2b2.PM.ajax.getApproval("PM:Admin", {id:'id = "' + info.i2b2NodeId + '"'});
				response.parse();
				var data = response.model[0];
				if (data.id) { $('pmAdmin-approvalID').value = data.id; }
				if (data.name) { $('pmAdmin-approvalName').value = data.name; }
				if (data.description) { $('pmAdmin-approvalDescription').value = data.description; }
				if (data.activation_date)  { $('pmAdmin-approvalActivation').value = data.activation_date; }
				if (data.expiration_date)  { $('pmAdmin-approvalExpiration').value = data.expiration_date; }
			} catch (e) {}
			i2b2.PM.view.admin.configScreenDispay(0);
			
			cal1 = new YAHOO.widget.Calendar("cal1","cal1Container"); 
	        cal1.selectEvent.subscribe(i2b2.PM.view.admin.getDate, cal1, true); 
	        cal1.renderEvent.subscribe(i2b2.PM.view.admin.setupListeners, cal1, true); 
	        Event.addListener(['pmAdmin-approvalActivation', 'pmAdmin-approvalExpiration'], 'focus', i2b2.PM.view.admin.showCal); 
	        Event.addListener(['pmAdmin-approvalActivation', 'pmAdmin-approvalExpiration'], 'blur', i2b2.PM.view.admin.hideCal); 
	        cal1.render(); 
			i2b2.PM.view.admin.hideCal();
			break;
		case "PROJECTREC":
			try {
				$('pmMainTitle').innerHTML = 'Project &gt; "'+info.i2b2NodeKey+'"';
				i2b2.PM.view.admin.showInfoPanel("PROJECTREC");
				var response = i2b2.PM.ajax.getProject("PM:Admin", {proj_code:info.i2b2NodeKey, proj_path:info.i2b2NodePath});
				response.parse();
				var data = response.model[0];
				if (data.id) { $('pmAdmin-projID').value = data.id; }
				if (data.name) { $('pmAdmin-projName').value = data.name; }
				if (data.wiki) { $('pmAdmin-projWiki').value = data.wiki; }
				if (data.key) { 
					$('pmAdmin-projOrigKey').value = data.key; 
				}
				$('pmAdmin-projKey').value = ""; 
				if (data.description) { $('pmAdmin-projDesc').value = data.description; }
				if (data.path) {
					$('pmAdmin-projPath').value = data.path; 
				} else {
					$('pmAdmin-projPath').value = '/';
				}
				$('pmAdmin-projStatus').value = 'A';
			} catch (e) {}
			i2b2.PM.view.admin.configScreenDispay(0);
			break;
		case "USERREC":
			try {
				$('pmMainTitle').innerHTML = 'User &gt; "'+info.i2b2NodeKey+'"';
				i2b2.PM.view.admin.showInfoPanel("USERREC");
				
				var response = i2b2.PM.ajax.getUser("PM:Admin", {user_id:info.i2b2NodeUserName});
				response.parse();
				var data = response.model[0];
				if (data.user_name) { $('pmAdmin-userName').value = data.user_name; }
				if (data.full_name) { $('pmAdmin-userFullname').value = data.full_name; }
				if (data.email) { $('pmAdmin-userEmail').value = data.email; }
				if (data.is_admin) { $('pmAdmin-userIsAdmin').value = data.is_admin; }
					
			} catch (e) {}
			i2b2.PM.view.admin.configScreenDispay(0);
			break;			
		case "PROJECTUSERS":
			try {
				i2b2.PM.view.admin.currentProject = tvEvent.node.parent.data;
				var ttt = i2b2.PM.view.admin.currentProject;
				$('pmMainTitle').innerHTML = 'Project &gt; "'+i2b2.PM.view.admin.currentProject.i2b2NodeKey+'"';
				i2b2.PM.view.admin.showInfoPanel("PROJECTUSERS");
				i2b2.PM.admin.refreshProjectsUsers(info.i2b2NodeKey);
			} catch (e) {}
			i2b2.PM.view.admin.configScreenDispay(0);
			tvEvent.node.tree.removeChildren(tvEvent.node);			
			break;
		case "PROJECTREC-USERS-ROLESNEW":
			break;
		case "PROJECTREC-USERS-ROLES":
			// get parent record information
			i2b2.PM.view.admin.currentProject = tvEvent.node.parent.parent.parent.data;
			i2b2.PM.view.admin.currentProject.i2b2NodeUsername = tvEvent.node.parent.data.i2b2NodeUsername;
			i2b2.PM.view.admin.showProjectUsers();
			break;
		case "PROJECTREC-CELLS":
			// get parent record information
			i2b2.PM.view.admin.currentProject = tvEvent.node.parent.data;
			i2b2.PM.view.admin.showProjectCells();
			break;
		case "PROJECTREC-PARAMS":
			// get parent record information
			i2b2.PM.view.admin.currentProject = tvEvent.node.parent.data;
			i2b2.PM.view.admin.showProjectParams();
			break;
		case "PROJECTREQUESTS":
			delete i2b2.PM.view.admin.currentProject;
			$('pmMainTitle').innerHTML = "Project Request List";
			//i2b2.PM.view.admin.showInfoPanel("PROJECTREQUEST");
			i2b2.PM.view.admin.showProjectRequests();
			tvEvent.node.tree.removeChildren(tvEvent.node);
			
			break;
		case "PROJECTREQUESTREC":
			try {
				$('pmMainTitle').innerHTML = 'Project Request &gt; "'+info.i2b2NodeKey+'"';
				i2b2.PM.view.admin.showInfoPanel("PROJECTREQUESTREC");
				
				i2b2.PM.view.admin.ProjectRequestViewer(info.i2b2RequestXml);
			} catch (e) {}
			i2b2.PM.view.admin.configScreenDispay(0);
			break;
		case "APPROVALS":
			delete i2b2.PM.view.admin.currentProject;
			$('pmMainTitle').innerHTML = "Approval List";
			i2b2.PM.view.admin.showApprovals();
			i2b2.PM.view.admin.showInfoPanel("APPROVAL");			
			tvEvent.node.tree.removeChildren(tvEvent.node);
			break;
		case "USERS":
			delete i2b2.PM.view.admin.currentProject;
			
			//var proj_data = tvEvent.node.data;
			//i2b2.PM.view.admin.currentProject = proj_data;
			$('pmMainTitle').innerHTML = "User List";
			i2b2.PM.view.admin.showUsers();			
			i2b2.PM.view.admin.showInfoPanel("USER");
			//i2b2.PM.view.admin.configScreenDispay(0);
			tvEvent.node.tree.removeChildren(tvEvent.node);			
			
			
			
			break;
		case "REC-PARAMS":
			try {
				$('pmMainTitle').innerHTML = '"' + tvEvent.node.parent.parent.label + '" &gt; Parameter &gt; "'+info.label+'"';
				i2b2.PM.view.admin.showInfoPanel("PARAMREC");
				
				if (tvEvent.node.parent.data.i2b2Table == "global")
				{
					var response = i2b2.PM.ajax.getGlobal("PM:Admin", {table:tvEvent.node.parent.data.i2b2Table,id_xml:info.i2b2Id});					
				} else {
					var response = i2b2.PM.ajax.getParam("PM:Admin", {table:tvEvent.node.parent.data.i2b2Table,id_xml:info.i2b2Id});
				}
				response.parse();
				var data = response.model[0];
				if (data.name) { $('pmAdmin-paramName').value = data.name; }
				if (data.value) { $('pmAdmin-paramValue').value = data.value; }
				if (data.dataType) {  $('pmAdmin-paramDatatype').value = data.dataType; }
				if (data.id) { $('pmAdmin-paramId').value = data.id; }
				//$('pmAdmin-paramId').value = tvEvent.node.parent.data.i2b2Table;
			} catch (e) {}
			i2b2.PM.view.admin.configScreenDispay(0);
			break;			
			
		case "PARAMS":
			delete i2b2.PM.view.admin.currentProject;
			
			//var proj_data = tvEvent.node.data;
			//i2b2.PM.view.admin.currentProject = proj_data;
			$('pmMainTitle').innerHTML = "Parameter List";
			i2b2.PM.view.admin.showInfoPanel("PARAM");
			i2b2.PM.view.admin.configScreenDispay(0);
			tvEvent.node.tree.removeChildren(tvEvent.node);			
			$('pmAdmin-paramTable').value = info.i2b2Table; 
			
			//i2b2.PM.view.admin.showUsers();
			break;
			
		default:
			delete i2b2.PM.view.admin.currentProject;
			alert(i2b2.PM.view.admin.configScreen);
	}
};


i2b2.PM.view.admin.setupListeners = function() {
        Event.addListener('cal1Container', 'mouseover', function() {
            over_cal = true;
        });
        Event.addListener('cal1Container', 'mouseout', function() {
            over_cal = false;
        });
    }

i2b2.PM.view.admin.getDate = function() {
            var calDate = this.getSelectedDates()[0];
            calDate = calDate.getFullYear() + '-' + (calDate.getMonth() + 1) + '-' + calDate.getDate();
            cur_field.value = calDate;            
            over_cal = false;
            hideCal();
    }

i2b2.PM.view.admin.showCal = function(ev) {
        var tar = Event.getTarget(ev);
        cur_field = tar;
    
        var xy = Dom.getXY(tar),
            date = Dom.get(tar).value;
        if (date) {
            cal1.cfg.setProperty('selected', date);
            cal1.cfg.setProperty('pagedate', new Date(date), true);
        } else {
            cal1.cfg.setProperty('selected', '');
            cal1.cfg.setProperty('pagedate', new Date(), true);
        }
        cal1.render();
        Dom.setStyle('cal1Container', 'display', 'block');
        xy[1] = xy[1] + 20;
        Dom.setXY('cal1Container', xy);
    }

i2b2.PM.view.admin.hideCal = function() {
        if (!over_cal) {
            Dom.setStyle('cal1Container', 'display', 'none');
        }
    }


i2b2.PM.view.admin.editorSaved = function(editObj) {
	if (editObj.newData != editObj.oldData) {
		editObj.editor._oDataTable.isDirty = true;
	}
};


i2b2.PM.view.admin.gridClickHandler = function(evtData) {
	if (this.isSelected(evtData.target)==true) {
		if (this.isDirty) {
			this.onEventShowCellEditor(evtData);
		} else {
			// block editing of some cells
			var srcn = i2b2.PM.view.admin.configScreen;
			var column = this.getColumn(evtData.target).field;
			if (!((srcn == "HIVEDOMAINS" && column == "domain_id") || (srcn == "PROJECTREC-USERS" && column == "user_name"))) {
				this.onEventShowCellEditor(evtData);
			}
		}
	} else {
		// abandon dirty data?
		try {
			if (i2b2.PM.view.admin.yuiControls.primaryGrid.isDirty || i2b2.PM.view.admin.yuiControls.secondaryGrid.isDirty) {
				if (confirm("Abandon Changes?")) {
					// clear previously added/edited row in primary data
					i2b2.PM.view.admin.treeClick(false,true);
				}
				return;
			}
		} catch(e) {}

		// deal with params grid
		try {
			i2b2.PM.view.admin.yuiControls.secondaryGrid.unselectAllRows();
		} catch (e) {}

		// unselect all rows
		this.unselectAllRows();
		this.selectRow(evtData.target);
		var configScreen = i2b2.PM.view.admin.configScreen;
		var selectedRec = this.getRecord(evtData.target)._oData;		
		switch(configScreen) {
			case "PROJECTREQUESTS":
				i2b2.PM.view.admin.ProjectRequestViewer.show(selectedRec.request_xml);
				break;
			case "HIVEDOMAINS":
				i2b2.PM.view.admin.showDomainParams(selectedRec.domain_id);
				break;
			case "HIVECELLS":
				i2b2.PM.view.admin.showCellParams(selectedRec.id);
				break;
			case "":
				// load the HiveParams in the secondary grid
				i2b2.PM.view.admin.configScreenDispay(2);
				// get data
				var recList = i2b2.PM.ajax.getAllParam("PM:Admin", {table:"hive", id_xml:selectedRec.domain_id});
				recList.parse();
				var tmp = recList.model;
				// create datasource
				i2b2.PM.admin.dsSecondary = new YAHOO.util.DataSource(tmp);
				i2b2.PM.admin.dsSecondary.responseType = YAHOO.util.DataSource.TYPE_JSARRAY;
				i2b2.PM.admin.dsSecondary.responseType.responseSchema = {
					fields: ["name","value"]
				};
				delete recList.model;
				delete recList;
				// create grid
				i2b2.PM.admin.grdSecondaryColumnDefs = i2b2.PM.model.adminColumnDef.USERPARAMS;
				var t = new YAHOO.widget.DataTable("pmAdminParamTableview", i2b2.PM.admin.grdSecondaryColumnDefs, i2b2.PM.admin.dsSecondary, {});
				i2b2.PM.view.admin.yuiControls.secondaryGrid = t;
				t.isDirty = false;
				t.subscribe("rowMouseoverEvent", t.onEventHighlightRow);
				t.subscribe("rowMouseoutEvent", t.onEventUnhighlightRow);
				t.subscribe("cellClickEvent", i2b2.PM.view.admin.gridClickHandler);
				t.subscribe("editorSaveEvent", i2b2.PM.view.admin.editorSaved);
				break;
			case "PROJECTREC-CELLS":
				i2b2.PM.view.admin.showCellParams(selectedRec.user_name, "/"+i2b2.PM.view.admin.currentProject.i2b2NodeKey);
				break;
			case "PROJECTREC-USERS":
				i2b2.PM.view.admin.showUserProjParams(selectedRec.user_name, i2b2.PM.view.admin.currentProject.i2b2NodeKey);
				break;
			case "USERS":
				i2b2.PM.view.admin.showUserParams(selectedRec.user_name);
				break;
			case "APPROVALS":
				i2b2.PM.view.admin.showApprovalParams(selectedRec.id);
				break;				
		}
	}
};





i2b2.PM.view.admin.paramgridClickHandler = function(evtData) {
	if (this.isSelected(evtData.target)==true) {
		if (this.isDirty) {
			this.onEventShowCellEditor(evtData);
		} else {
			// block editing of some cells
			var srcn = i2b2.PM.view.admin.configScreen;
			var column = this.getColumn(evtData.target).field;
			if (!(srcn == "HIVEDOMAINS" && column == "domain_id")) {
				this.onEventShowCellEditor(evtData);
			}
		}
	} else {
		// abandon dirty data?
		try {
			if (i2b2.PM.view.admin.yuiControls.secondaryGrid.isDirty) {
				if (confirm("Abandon Changes?")) {
					// clear previously added/edited row in secondary data grid
					this.deleteRow(0);
					this.isDirty = false;
				}
				return;
			}
		} catch(e) {}
		// unselect all rows
		this.unselectAllRows();
		this.selectRow(evtData.target);
	}
};









i2b2.PM.view.admin.checkDirtyFlags = function() {
	try {
		if (i2b2.PM.view.admin.yuiControls.primaryGrid.isDirty || i2b2.PM.view.admin.yuiControls.secondaryGrid.isDirty) {
			if (confirm("Abandon Changes?")) {
				// clear previously added/edited row in primary data
				i2b2.PM.view.admin.yuiControls.primaryGrid.isDirty = false;
				i2b2.PM.view.admin.yuiControls.secondaryGrid.isDirty = false;
				i2b2.PM.view.admin.yuiControls.primaryGrid.isNew = false;
				i2b2.PM.view.admin.yuiControls.secondaryGrid.isNew = false;
				i2b2.PM.view.admin.treeClick(false,true);
				return false;	
			}
			return true;
		} else {
			return false;
		}
	} catch(e) {}
}


// data layer stuff
// --------------------------------------------------------------------------------------------------------------------
i2b2.PM.admin.refreshTreeListData = function(projList) {
	projList.parse();
	var tmp = {};
	var l = projList.model.length;
	for (var i=0; i<l; i++) {
		tmp[projList.model[i].id] = projList.model[i];
	}
	delete projList;
	return tmp;
};


i2b2.PM.admin.refreshCellListData = function() {
	var cellList = i2b2.PM.ajax.getAllCell("PM:Admin", {});
	cellList.parse();
	var tmp = {};
	var l = cellList.model.length;
	for (var i=0; i<l; i++) {
		tmp[cellList.model[i].id+cellList.model[i].project_path] = cellList.model[i];
	}
	delete cellList;
	i2b2.PM.model.admin.CellList = tmp;
};
i2b2.PM.admin.refreshProjectListData = function() {
	var projList = i2b2.PM.ajax.getAllProject("PM:Admin", {});
	projList.parse();
	var tmp = {};
	var l = projList.model.length;
	for (var i=0; i<l; i++) {
		tmp[projList.model[i].id] = projList.model[i];
	}
	delete projList;
	i2b2.PM.model.admin.ProjectList = tmp;
};

i2b2.PM.admin.refreshUserListData = function() {
	var projList = i2b2.PM.ajax.getAllUser("PM:Admin", {});
	projList.parse();
	var tmp = {};
	var l = projList.model.length;
	for (var i=0; i<l; i++) {
		tmp[projList.model[i].user_name] = projList.model[i];
	}
	delete projList;
	i2b2.PM.model.admin.UserList = tmp;
};


i2b2.PM.admin.refreshProjectUserListData = function() {
	var projUserList = i2b2.PM.ajax.getAllRole("PM:Admin", { id: i2b2.PM.view.admin.currentProject.i2b2NodeKey });
	projUserList.parse();
	var tmp = {};
	var l = projUserList.model.length;
	for (var i=0; i<l; i++) {
		tmp[projUserList.model[i].username] = projUserList.model[i];
	}
	delete projUserList;
	i2b2.PM.model.admin.ProjectUserList = tmp;
};

i2b2.PM.admin.refreshParameterListData = function(tablename, param, id) {
	var projUserList = i2b2.PM.ajax.getAllParam("PM:Admin", {table:tablename, id_xml:param, param_xml:id});
		//i2b2.PM.ajax.getAllParams("PM:Admin", { id: i2b2.PM.view.admin.currentProject.i2b2NodeKey, });
	projUserList.parse();
	var tmp = {};
	var l = projUserList.model.length;
	for (var i=0; i<l; i++) {
		tmp[projUserList.model[i].name] = projUserList.model[i];
	}
	delete projUserList;
	i2b2.PM.model.admin.ParameterList = tmp;
};

console.timeEnd('execute time');
console.groupEnd();
