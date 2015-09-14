/**
 * @projectDescription	View controller for PM module's login form(s).
 * @inherits 	i2b2
 * @namespace	i2b2.PM
 * @author		Nick Benik, Griffin Weber MD PhD
 * @version 	1.3
 * ----------------------------------------------------------------------------------------
 * updated 9-15-08: RC4 launch [Nick Benik] 
 */
console.group('Load & Execute component file: cells > PM > view');
console.time('execute time');

var myDataTable = {};
var mySubDataTable = {};
var callerid = "";
var parentid = "";


i2b2.PM.ShowParameter = function(origin, index) {

	if (index=-100) {
	var hiveid = this.myDataTable.getRecord(index);///.getData("domain_name");


	i2b2.PM.getAll("param-" + origin, ["param","name"], hiveid);
	var myColumnDefs = [
				{key:"id"},
				{key:"name",sortable:true, resizeable:true, editor: new YAHOO.widget.TextboxCellEditor({disableBtns:true})}, 
				{key:"param",sortable:true, resizeable:true,editor: new YAHOO.widget.TextboxCellEditor({disableBtns:true})},		
				{key:"action"}
			];

	mySubDataTable = new YAHOO.widget.DataTable("pmParamTableview", myColumnDefs, i2b2.PM.getHiveDataSource, {});
	var pu = $('pmParam');
	pu.show();
	pu = pu.style;
	pu.width = 800;
	pu.top = 350;
	pu.left = 200;
	pu.height = 144;	
	
	
			  switch(origin) {
                    case "project":
							 parentid =  hiveid.getData("id");	
							 break;
                    case "cell":
							 parentid =  hiveid.getData("domain_id");				
						 break;
                    case "user":
							 parentid =  hiveid.getData("user_name");				
							 break;
					case "hive":
							 parentid =  hiveid.getData("domain_id");		
							 break;
	}
	


	for(var i=0; i<mySubDataTable.getRecordSet().getLength(); i++) { 
		mySubDataTable.updateCell(mySubDataTable.getRecord(i), "action",  "<a href=\"JavaScript:i2b2.PM.UpdateRow('param','" + (mySubDataTable.getRecord(i).getId())	 + "');\">Save</a>&nbsp;<a href=\"JavaScript:i2b2.PM.DeleteRow('param','" + (mySubDataTable.getRecord(i).getId())	 + "');\">Delete</a>");	
		mySubDataTable.updateCell(mySubDataTable.getRecord(i), "id", parentid);
	}


	// Set up editing flow 
	var highlightEditableCell = function(oArgs) { 
		var elCell = oArgs.target; 
		if(YAHOO.util.Dom.hasClass(elCell, "yui-dt-editable")) { 
			this.highlightCell(elCell); 
		} 
	}; 
        // Enable row highlighting
	        mySubDataTable.subscribe("rowMouseoverEvent", mySubDataTable.onEventHighlightRow); 
	        mySubDataTable.subscribe("rowMouseoutEvent", mySubDataTable.onEventUnhighlightRow); 
	 	
	//mySubDataTable.subscribe("cellMouseoverEvent", highlightEditableCell); 
	//mySubDataTable.subscribe("cellMouseoutEvent", mySubDataTable.onEventUnhighlightCell); 
	mySubDataTable.subscribe("cellClickEvent", mySubDataTable.onEventShowCellEditor); 
	}
}

i2b2.PM.ShowRole = function(index) {
	var record = this.myDataTable.getRecord(index); //.getData("id");

	i2b2.PM.getAll("role",["project_path","user_id","role"], record); //["project_path","user_id","user_role"]);
	var myColumnDefs = [
	                    {key:"project_path",sortable:true, resizeable:true,editor: new YAHOO.widget.TextboxCellEditor({disableBtns:true})}, 
	                    {key:"user_id",sortable:true, resizeable:true,editor: new YAHOO.widget.TextboxCellEditor({disableBtns:true})}, 
	                    {key:"role",sortable:true, resizeable:true, editor: new YAHOO.widget.RadioCellEditor({radioOptions:["Active","Inactive"],disableBtns:true})}, 
	                    {key:"action"}
	                    ];

	mySubDataTable = new YAHOO.widget.DataTable("pmParamTableview",
			myColumnDefs, i2b2.PM.getHiveDataSource, {});


	var pu = $('pmParam');
	pu.show();
	pu = pu.style;
	pu.width = 800;
	pu.top = 350;
	pu.left = 200;
	pu.height = 144;	



	for(var i=0; i<mySubDataTable.getRecordSet().getLength(); i++) { 
		mySubDataTable.updateCell(mySubDataTable.getRecord(i), "action",  "<a href=\"JavaScript:i2b2.PM.UpdateRow('role','" + (mySubDataTable.getRecord(i).getId())	 + "');\">Save</a>&nbsp;<a href=\"JavaScript:i2b2.PM.DeleteRow('role','" + (mySubDataTable.getRecord(i).getId())	 + "');\">Delete</a>");			
	}




	// Set up editing flow 
	var highlightEditableCell = function(oArgs) { 
		var elCell = oArgs.target; 
		if(YAHOO.util.Dom.hasClass(elCell, "yui-dt-editable")) { 
			this.highlightCell(elCell); 
		} 
	}; 
        // Enable row highlighting 
	        mySubDataTable.subscribe("rowMouseoverEvent", mySubDataTable.onEventHighlightRow); 
	       mySubDataTable.subscribe("rowMouseoutEvent", mySubDataTable.onEventUnhighlightRow); 

	//mySubDataTable.subscribe("cellMouseoverEvent", highlightEditableCell); 
	//mySubDataTable.subscribe("cellMouseoutEvent", mySubDataTable.onEventUnhighlightCell); 
	mySubDataTable.subscribe("cellClickEvent", mySubDataTable.onEventShowCellEditor); 


}


i2b2.PM.DeleteRow = function(origin, index) {
	//         this.myDataTable.deleteRow(index);

	// var trEl = this.myDataTable.getTrEl(index);
	// trEl.style.display = 'none'; //set 'none' to hide...

	answer = confirm("Do you really want to delete this item?");
	
	if (answer !=0)
	
	{
		switch(origin.substring(4,0)) {
		case "role":
			var record = mySubDataTable.getRecord(index);
			i2b2.PM.deleteData(origin, record);
	
			mySubDataTable.deleteRow(index);
	
			break;
		case "para":
			var record = mySubDataTable.getRecord(index);
			i2b2.PM.deleteData(origin, record);
	
			mySubDataTable.deleteRow(index);
	
			break;
		default:
			var record = this.myDataTable.getRecord(index);
		i2b2.PM.deleteData(origin, record);
	
		this.myDataTable.deleteRow(index);
		break;
		}
	}
}

i2b2.PM.AddRow = function() {
		this.myDataTable.set("sortedBy", null);
		this.myDataTable.addRow({ "action":""}, 0); //this.myDataTable.getRecordSet().getLength());
		//this.myDataTable.updateRow(this.myDataTable.getRecord(this.myDataTable.getRecordSet().getLength()-1),{ "action": "<a href=\"JavaScript:i2b2.PM.InsertRow('" + callerid + "','" + (this.myDataTable.getRecord(this.myDataTable.getRecordSet().getLength()-1).getId())	 + "');\"><img src=\"js-i2b2/cells/PM/assets/add.gif\" border=\"0\"/></a>"});
		this.myDataTable.updateRow(this.myDataTable.getRecord(0),{ "action": "<a href=\"JavaScript:i2b2.PM.InsertRow('" + callerid + "','" + (this.myDataTable.getRecord(0).getId())	 + "');\">Save</a>"});	

}

i2b2.PM.UpdateRow = function(origin, index) {
	//         this.myDataTable.deleteRow(index);

	switch(origin) {
	case "role":
		var record = mySubDataTable.getRecord(index);
		i2b2.PM.setData(origin, record);
		break;
	default:
		var record = this.myDataTable.getRecord(index);
		i2b2.PM.setData(origin, record);
		break;
	}
	i2b2.PM.showItem(callerid);
}

i2b2.PM.InsertRow = function(origin, index) {
	//         this.myDataTable.deleteRow(index);

	switch(origin.substring(4,0)) {
	case "para":
		var record = mySubDataTable.getRecord(index);
		i2b2.PM.setData(origin, record);
		break;
	default:
		var record = this.myDataTable.getRecord(index);
		i2b2.PM.setData(origin, record);
		break;
	}
	i2b2.PM.showItem(callerid);
}


i2b2.PM.showItem = function(inputItem) {
	var ph = $('pmMain'); // $(inputItem);
	ph.show();
	ph = ph.style;
	ph.left = 200;

	switch(inputItem) {
	case "cell_data":

			var myColumnDefs = [
		                    {key:"project_path", sortable:true, resizeable:true, editor: new YAHOO.widget.TextboxCellEditor({disableBtns:true})}, 
		                    {key:"id", sortable:true, resizeable:true, editor: new YAHOO.widget.TextboxCellEditor({disableBtns:true})}, 
		                    {key:"name", sortable:true, resizeable:true, editor: new YAHOO.widget.TextboxCellEditor({disableBtns:true})}, 
		                    {key:"method",sortable:true, resizeable:true, editor: new YAHOO.widget.RadioCellEditor({radioOptions:["SOAP","REST","OTHER"],disableBtns:true})}, 
		                    {key:"url",sortable:true, resizeable:true,editor: new YAHOO.widget.TextboxCellEditor({disableBtns:true})}, 
		                    {key:"action"},
		                    {key:"param"}
		                    ];

		this.myDataTable = i2b2.PM.getAll("cell_data", ["project_path", "id", "name","method","url"], null, myColumnDefs, this.myDataTable);

		document.getElementById("pmMainTitle").innerHTML="Hive Data &raquo; Cell Data";
		document.getElementById("pmAddButton").innerHTML="Add Cell Data";

		//btn.label("Add New Cell");
		break;
	case "project":

		var myColumnDefs = [
		                    {key:"id", sortable:true, resizeable:true, editor: new YAHOO.widget.TextboxCellEditor({disableBtns:true})}, 
		                    {key:"name", sortable:true, resizeable:true, editor: new YAHOO.widget.TextboxCellEditor({disableBtns:true})}, 
		                    {key:"key",sortable:true, resizeable:true, editor: new YAHOO.widget.MD5CellEditor({disableBtns:false})}, 
		                    {key:"wiki",sortable:true, resizeable:true,editor: new YAHOO.widget.TextboxCellEditor({disableBtns:true})}, 
		                    {key:"action"},
		                    {key:"param"}
		                    ];

		i2b2.PM.getAll("project", ["id", "name","key","wiki"],null,  myColumnDefs, this.myDataTable);

		document.getElementById("pmMainTitle").innerHTML="Project Data";
		document.getElementById("pmAddButton").innerHTML="Add Project Data";		
		break;	
	case "approval":

		var myColumnDefs = [
		                    {key:"id", sortable:true, resizeable:true, editor: new YAHOO.widget.TextboxCellEditor({disableBtns:true})}, 
		                    {key:"name", sortable:true, resizeable:true, editor: new YAHOO.widget.TextboxCellEditor({disableBtns:true})}, ,
		                    {key:"param"}
		                    ];

		i2b2.PM.getAll("approval", ["id", "name"],null,  myColumnDefs, this.myDataTable);

		document.getElementById("pmMainTitle").innerHTML="Approval Data";
		document.getElementById("pmAddButton").innerHTML="Add Approval Data";		
		break;			
	case "user":

			var myColumnDefs = [
		                    {key:"full_name", sortable:true, resizeable:true, editor: new YAHOO.widget.TextboxCellEditor({disableBtns:true})}, 
		                    {key:"user_name",sortable:true, resizeable:true,editor: new YAHOO.widget.TextboxCellEditor({disableBtns:true})}, 
		                    {key:"email",sortable:true, resizeable:true,editor: new YAHOO.widget.TextboxCellEditor({disableBtns:true})}, 
		                    {key:"password",resizeable:true,editor: new YAHOO.widget.PasswordCellEditor({disableBtns:false}),formatter: DataTableUtils.PasswordFormatter}, 
		                    {key:"action"},
		                    {key:"param"}
		                    ];

	

	 	this.myDataTable = i2b2.PM.getAll( "user", ["full_name","user_name","email"],null, myColumnDefs, this.myDataTable);

		document.getElementById("pmMainTitle").innerHTML="Users";
		document.getElementById("pmAddButton").innerHTML="Add User";		
		break;
	case "global":



		var myColumnDefs = [
		                    {key:"name", sortable:true, resizeable:true, editor: new YAHOO.widget.TextboxCellEditor({disableBtns:true})}, 
		                    {key:"param",sortable:true, resizeable:true,editor: new YAHOO.widget.TextboxCellEditor({disableBtns:true})}, 
		                    {key:"action"}
		                    ];

		i2b2.PM.getAll("global", ["name","param"], null, myColumnDefs, this.myDataTable);

	
		for(var i=0; i<this.myDataTable.getRecordSet().getLength(); i++)  {
			this.myDataTable.updateCell(this.myDataTable.getRecord(i), "action",  "<a href=\"JavaScript:i2b2.PM.DeleteRow('goloal','" + (this.myDataTable.getRecord(i).getId())	 + "');\">Delete</a>");
		}
		document.getElementById("pmMainTitle").innerHTML="Hive Data &raquo; Global Params";
		break;
	case "param-user":



		var myColumnDefs = [
		                    {key:"name", sortable:true, resizeable:true, editor: new YAHOO.widget.TextboxCellEditor({disableBtns:true})}, 
		                    {key:"param",sortable:true, resizeable:true,editor: new YAHOO.widget.TextboxCellEditor({disableBtns:true})}, 
		                    {key:"action"}
		                    ];

		i2b2.PM.getAll("param-user", ["name","param"], new Array(""), myColumnDefs, this.myDataTable);

	
		for(var i=0; i<this.myDataTable.getRecordSet().getLength(); i++)  {
			this.myDataTable.updateCell(this.myDataTable.getRecord(i), "action",  "<a href=\"JavaScript:i2b2.PM.DeleteRow('user_param','" + (this.myDataTable.getRecord(i).getId())	 + "');\">Delete</a>");
		}
		document.getElementById("pmMainTitle").innerHTML="Manage Users &raquo; Users Params";
		break;
	case "hive":

			var myColumnDefs = [
		                    {key:"domain_id",sortable:true}, 
		                    {key:"active",sortable:true, resizeable:true, editor: new YAHOO.widget.RadioCellEditor({radioOptions:["Active","Inactive"],disableBtns:true})}, 
		                    {key:"environment", sortable:true, resizeable:true, editor: new YAHOO.widget.RadioCellEditor({radioOptions:["Development","Production", "Test"],disableBtns:true})}, 
		                    {key:"domain_name",sortable:true, resizeable:true,editor: new YAHOO.widget.TextboxCellEditor({disableBtns:true})}, 
		                    {key:"helpURL",sortable:true, resizeable:true,editor: new YAHOO.widget.TextboxCellEditor({disableBtns:true})},
		                    {key:"action"},
		                    {key:"param"}
		                    ];
	
	i2b2.PM.getAll("hive", ["domain_id", "active","environment","domain_name","helpURL"],null, myColumnDefs, this.myDataTable);


	
		for(var i=0; i<this.myDataTable.getRecordSet().getLength(); i++) { 
			this.myDataTable.updateCell(this.myDataTable.getRecord(i), "action",  "<a href=\"JavaScript:i2b2.PM.UpdateRow('hive','" + (this.myDataTable.getRecord(i).getId())	 + "');\">Save</a>&nbsp;<a href=\"JavaScript:i2b2.PM.DeleteRow('hive','" + (this.myDataTable.getRecord(i).getId())	 + "');\">Delete</a>");
				this.myDataTable.updateCell(this.myDataTable.getRecord(i), "param",  "<a href=\"JavaScript:i2b2.PM.ShowParameter('hive','" + (this.myDataTable.getRecord(i).getId()) + "');\">Detail</a>");
		}
		document.getElementById("pmMainTitle").innerHTML="Hive Data";
		document.getElementById("pmAddButton").innerHTML="Add Hive Data";
		break;			 

	}
};













// login screen
// ================================================================================================== //
i2b2.PM.doConnectForm = function(inputUser, inputPass, inputDomain, inputSubmit) {
	console.debug("i2b2.PM.doConnectForm(",inputUser, inputPass, inputDomain, inputSubmit,")");
	i2b2.PM.udlogin = {};
	// function used to save references to the inputs that make up the login screen
	var ref = i2b2.PM.udlogin;
	ref.inputUser = inputUser;
	ref.inputPass = inputPass;
	ref.inputDomain = inputDomain;
	ref.inputSubmitBtn = inputSubmit;
	YAHOO.util.Event.addListener(inputSubmit.id, "click", i2b2.PM.doLogin); 
	i2b2.PM._redrawConnectedForm();
}

// ================================================================================================== //
i2b2.PM._redrawConnectedForm = function() {
	var ref = i2b2.PM.udlogin;
	// repopulate the domain information
	//	ref.inputUser.value = '';
	//	ref.inputPass.value = '';
	// clear the list
	var dli = ref.inputDomain;
	while( dli.hasChildNodes() ) { dli.removeChild( dli.lastChild ); }
	// populate the Categories from the data model
	var dml = i2b2.PM.model.Domains;
	for (var i=0; i<dml.length; i++) {
		// ONT options dropdown
		dno = document.createElement('OPTION');
		dno.setAttribute('value', i);
		var dnt = document.createTextNode(dml[i].name);
		dno.appendChild(dnt);
		dli.appendChild(dno);
	}
}

// ================================================================================================== //
i2b2.PM.doLoginDialog = function() {
	// this displays the login dialogue box (auto generated popup)
	if (!$("i2b2_login_modal_dialog")) {
		var htmlFrag = i2b2.PM.model.html.loginDialog;
		Element.insert(document.body,htmlFrag);

		if (!i2b2.PM.view.modal.login) {
			i2b2.PM.view.modal.login = new YAHOO.widget.Panel("i2b2_login_modal_dialog", {
				zindex: 700,
				width: "501px",
				fixedcenter: true,
				constraintoviewport: true,
				close: false,
				draggable: true
			});
			var kl = new YAHOO.util.KeyListener("i2b2_login_modal_dialog", { keys:13 },  							
					{ fn:i2b2.PM.doLogin,
				scope:i2b2.PM.view.modal.login,
				correctScope:true }, "keydown" );
			i2b2.PM.view.modal.login.cfg.queueProperty("keylisteners", kl);
			i2b2.PM.view.modal.login.render(document.body);

			// show the form
			i2b2.PM.view.modal.login.show();
			$('loginusr').focus();
			// connect the form to the PM controller
			i2b2.PM.udlogin = {};
			i2b2.PM.udlogin.inputUser = $('loginusr');
			i2b2.PM.udlogin.inputPass = $('loginpass');
			i2b2.PM.udlogin.inputDomain = $('logindomain');
			// load the domains
			i2b2.PM._redrawConnectedForm();
		}
	}
	// show the form
	i2b2.PM.view.modal.login.show();
}


console.timeEnd('execute time');
console.groupEnd();