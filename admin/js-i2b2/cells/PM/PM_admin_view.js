 
/**
 * @projectDescription	PM Administration Module
 * @inherits			i2b2
 * @namespace			i2b2.PM
 * @author			Nick Benik, Mike Mendis, Griffin Weber MD PhD
 * @version			1.0
 */
i2b2.PM.model.helpMSGS = {};
i2b2.PM.model.helpMSGS.LOADED = "<div><img width=152 height=152 style=\"padding: 10px; vertical-align: middle;\" src=js-i2b2/cells/PM/assets/i2b2_hive.png><span style=\"font-size: 36pt; color: rgb(54, 95, 145);\">i2b2 Administration</span></div><br/>" 
    +"Welcome to the <b>i2b2 Administration module</b> of the <i>project management interface</i>.<br><br>"
	+ "The pages within i2b2 Admin are designed to assist with workflow and overall ease of use, individuals can easily save data and parameters for the hive, projects and users.<br/><br/><table>"
	+ "<tr><td><b>Manage Hive</b></td>"
	+ "<td>View domain information and capture cell data and global parameters.</td></tr>"
	+ "<tr><td><b>Manage Project</b></td>"
	+ "<td>General information about the project as well as project specific cell data, parameters, and user access and roles.</td></tr>"
	+ "<tr><td><b>Manage Users</b></td>"
	+ "<td>General information about a user.</td></tr>"
	+ "<tr><td><b>Manage Approvals</b></td>"
	+ "<td>Captures general information used to authorize projects.</td></tr>"
	+ "<tr><td><b>Project Requests</b></td>"
	+ "<td>View information about submitted project requests.</td></tr></table>"
	+"<br>The <b>navigation bar</b> on the left side of the page can be used to access individual pages.";
i2b2.PM.model.helpMSGS.HIVE = "Hive Configuration<br />Describe what these functions are for.";
i2b2.PM.model.helpMSGS.HIVEDOMAIN = "<form><p>Please update hive infomation.</p>"+
	'<div>'+
	'<table border="0"><tbody><tr><td valign="middle"><b>Domain Id:</b></td><td><input type="TEXT" id="pmAdmin-hiveID" maxlength="50" style="width:250px"/></td></tr>'+
	'<tr><td valign="middle"><b>Help URL:</b></td><td><input type="TEXT" maxlength="255" id="pmAdmin-hiveHelpURL" style="width:250px"/></td></tr>'+
	'<tr><td valign="middle"><b>Domain Name:</b></td><td><input type="TEXT" maxlength="255" id="pmAdmin-hiveName" style="width:250px"/></td></tr>'+
	'<tr><td valign="middle"><b>Environment:</b></td><td><SELECT style="width:250px" id="pmAdmin-hiveEnvironment"><OPTION value="DEVELOPMENT">Development</option><OPTION value="PRODUCTION">Production</option><OPTION value="TEST">Test</option></select></td></tr>'+	
	'<tr><td></td><td align="right"><input type="HIDDEN" value="" id="pmAdmin-projOrigKey"/> <input type="BUTTON" value="Save" onclick="i2b2.PM.admin.saveDomain();"/> <input type="BUTTON" value="Cancel" onclick="i2b2.PM.view.admin.refreshScreen();"/></td></tr>'+
	'</table></div></form>';
i2b2.PM.model.helpMSGS.PROJECTREQUEST = "<form><p>Click on \"Project Request\" in the navigation bar to refresh the list of project requests.<br />Please select a project request on the left to see it's properties</p>";	
i2b2.PM.model.helpMSGS.PROJECTREQUESTREC = '<div id="project-request-viewer-body">empty</project-request-viewer-body>';
i2b2.PM.model.helpMSGS.APPROVAL = "<form><p>Click on \"Approval\" in the navigation bar to refresh the list of approval.<br />Please select a approval on the left to edit it's properties</p>"+
	'<div id="AddNewApprovalBtnDIV"><input type="BUTTON" value="Add New Approval" onclick="$(\'AddNewApprovalBtnDIV\').hide(); $(\'pmAdminMainTableview\').hide(); $(\'AddNewApprovalDIV\').show();"></div>'+
	'<div id="AddNewApprovalDIV" style="display:none">'+
	'<table border="0"><tbody><tr><td valign="middle"><b>Approval Id:</b></td><td><input type="TEXT" id="pmAdmin-approvalID" maxlength="50" style="width:250px"/></td></tr>'+
	'<tr><td valign="middle"><b>Approval Name:</b></td><td><input type="TEXT" maxlength="255" id="pmAdmin-approvalName" style="width:250px"/></td></tr>'+
	'<tr><td valign="middle"><b>Approval Description:</b></td><td><input type="TEXT" maxlength="255" id="pmAdmin-approvalDescription" style="width:250px"/></td></tr>'+
	'<tr><td valign="middle"><b>Activation Date:</b></td><td><input type="TEXT" maxlength="255" id="pmAdmin-approvalActivation" style="width:250px"/></td></tr>'+
	'<tr><td valign="middle"><b>Expiration Date:</b></td><td><input type="TEXT" maxlength="255" id="pmAdmin-approvalExpiration" style="width:250px"/></td></tr>'+
	'<tr><td></td><td align="right"><input type="HIDDEN" value="" id="pmAdmin-projOrigKey"/><input type="BUTTON" value="Save" onclick="i2b2.PM.admin.saveApproval();"/> <input type="BUTTON" value="Cancel" onclick="$(\'AddNewApprovalBtnDIV\').show(); $(\'AddNewApprovalDIV\').hide();"/></td></tr>'+
	'</table></div></form><div id="cal1Container"></div> ';	
i2b2.PM.model.helpMSGS.APPROVALREC = "<form><p>Please select which approval screen you want to access.</p>"+
	'<div>'+
	'<table border="0"><tbody><tr><td valign="middle"><b>Approval Id:</b></td><td><input type="TEXT" id="pmAdmin-approvalID" maxlength="50" style="width:250px"/></td></tr>'+
	'<tr><td valign="middle"><b>Approval Name:</b></td><td><input type="TEXT" maxlength="255" id="pmAdmin-approvalName" style="width:250px"/></td></tr>'+
	'<tr><td valign="middle"><b>Approval Description:</b></td><td><input type="TEXT" maxlength="255" id="pmAdmin-approvalDescription" style="width:250px"/></td></tr>'+
	'<tr><td valign="middle"><b>Activation Date:</b></td><td><input type="TEXT" maxlength="255" id="pmAdmin-approvalActivation" style="width:250px"/></td></tr>'+
	'<tr><td valign="middle"><b>Expiration Date:</b></td><td><input type="TEXT" maxlength="255" id="pmAdmin-approvalExpiration" style="width:250px"/></td></tr>'+
	'<tr><td></td><td align="right"><input type="BUTTON" value="Delete" onclick="i2b2.PM.admin.deleteApproval();"/><input type="BUTTON" value="Save" onclick="i2b2.PM.admin.saveApproval();"/> <input type="BUTTON" value="Cancel" onclick="$(\'AddNewApprovalBtnDIV\').show(); $(\'AddNewApprovalDIV\').hide();"/></td></tr>'+
	'</table></div></form><div id="cal1Container"></div> ';	
i2b2.PM.model.helpMSGS.CELL = "<form><p>Click on \"Cell\" in the navigation bar to refresh the list of cell.<br />Please select a cell on the left to edit it's properties</p>"+
	'<div id="AddNewCellBtnDIV"><input type="BUTTON" value="Add New Cell" onclick="$(\'AddNewCellBtnDIV\').hide();$(\'pmAdminMainTableview\').hide(); $(\'AddNewCellDIV\').show();"></div>'+
	'<div id="AddNewCellDIV" style="display:none">'+
	'<table border="0"><tbody><tr><td valign="middle"><b>Cell Id:</b></td><td><input type="TEXT" id="pmAdmin-cellID" maxlength="50" style="width:250px"/></td></tr>'+
	'<tr><td valign="middle"><b>Cell Name:</b></td><td><input type="TEXT" maxlength="255" id="pmAdmin-cellName" style="width:250px"/></td></tr>'+
	'<tr><td valign="middle"><b>Cell URL:</b></td><td><input type="TEXT" maxlength="255" id="pmAdmin-cellURL" style="width:250px"/></td></tr>'+
	'<tr><td valign="middle"><b>Project Path:</b></td><td><input type="TEXT" maxlength="255" id="pmAdmin-cellProjPath" style="width:250px"/></td></tr>'+
	'<tr><td valign="middle"><b>Method:</b></td><td><SELECT style="width:250px" id="pmAdmin-cellMethod"><OPTION value="REST">REST</option><OPTION value="SOAP">SOAP</option><OPTION value="OTHER">OTHER</option></select></td></tr>'+		
	'<tr><td></td><td align="right"><input type="HIDDEN" value="" id="pmAdmin-projOrigKey"/><input type="BUTTON" value="Save" onclick="i2b2.PM.admin.saveCell();"/> <input type="BUTTON" value="Cancel" onclick="$(\'AddNewCellBtnDIV\').show(); $(\'AddNewCellDIV\').hide();"/></td></tr>'+
	'</table></div></form>';	
i2b2.PM.model.helpMSGS.CELLREC = "<form><p>Please select which cell configuration screen you want to access.</p>"+
	'<div>'+
	'<table border="0"><tbody><tr><td valign="middle"><b>Cell Id:</b></td><td><input type="TEXT" id="pmAdmin-cellID" maxlength="50" style="width:250px"/></td></tr>'+
	'<tr><td valign="middle"><b>Cell Name:</b></td><td><input type="TEXT" maxlength="255" id="pmAdmin-cellName" style="width:250px"/></td></tr>'+
	'<tr><td valign="middle"><b>Cell URL:</b></td><td><input type="TEXT" maxlength="255" id="pmAdmin-cellURL" style="width:250px"/></td></tr>'+
	'<tr><td valign="middle"><b>Project Path:</b></td><td><input type="TEXT" maxlength="255" id="pmAdmin-cellProjPath" style="width:250px"/></td></tr>'+
	'<tr><td valign="middle"><b>Method:</b></td><td><SELECT style="width:250px" id="pmAdmin-cellMethod"><OPTION value="REST">REST</option><OPTION value="SOAP">SOAP</option><OPTION value="OTHER">OTHER</option></select></td></tr>'+		
	'<tr><td></td><td align="right"><input type="HIDDEN" value="" id="pmAdmin-projOrigKey"/><input type="BUTTON" value="Delete" onclick="i2b2.PM.admin.deleteCell();"/> <input type="BUTTON" value="Save" onclick="i2b2.PM.admin.saveCell();"/> <input type="BUTTON" value="Cancel" onclick="$(\'AddNewCellBtnDIV\').show(); $(\'AddNewCellDIV\').hide();"/></td></tr>'+
	'</table></div></form>';
i2b2.PM.model.helpMSGS.PROJECT = "<form><p>Click on \"Project\" in the navigation bar to refresh the list of projects.<br />Please select a project on the left to edit it's properties</p>"+
	'<div id="AddNewProjBtnDIV"><input type="BUTTON" value="Add New Project" onclick="$(\'AddNewProjBtnDIV\').hide(); $(\'pmAdminMainTableview\').hide();$(\'AddNewProjDIV\').show();"></div>'+
	'<div id="AddNewProjDIV" style="display:none">'+
	'<table border="0"><tbody><tr><td valign="middle"><b>Project Id:</b></td><td><input type="TEXT" id="pmAdmin-projID" maxlength="50" style="width:250px"/></td></tr>'+
	'<tr><td valign="middle"><b>Project Name:</b></td><td><input type="TEXT" maxlength="255" id="pmAdmin-projName" style="width:250px"/></td></tr>'+
	'<tr><td valign="middle"><b>Project Wiki:</b></td><td><input type="TEXT" maxlength="255" id="pmAdmin-projWiki" style="width:250px"/></td></tr>'+
	'<tr><td valign="middle"><b>Project Key:</b></td><td><input type="TEXT" maxlength="255" id="pmAdmin-projKey" style="width:250px"/></td></tr>'+
	'<tr><td valign="middle"><b>Project Description:</b></td><td><textarea maxlength="2000" id="pmAdmin-projDesc" style="width:250px; height:126px"/></textarea></td></tr>'+
	'<tr><td valign="middle"><b>Project Path:</b></td><td><input type="TEXT" maxlength="255" id="pmAdmin-projPath" style="width:250px"/></td></tr>'+
	'<tr><td></td><td align="right"><input type="HIDDEN" value="" id="pmAdmin-projOrigKey"/><input type="BUTTON" value="Save" onclick="i2b2.PM.admin.saveProject();"/> <input type="BUTTON" value="Cancel" onclick="$(\'AddNewProjBtnDIV\').show(); $(\'AddNewProjDIV\').hide();"/></td></tr>'+
	'</table></div></form>';
i2b2.PM.model.helpMSGS.PROJECTREC = "<form><p>Please select which project configuration screen you want to access.</p>"+
	'<div>'+
	'<table border="0"><tbody><tr><td valign="middle"><b>Project Id:</b></td><td><input type="TEXT" id="pmAdmin-projID" maxlength="50" style="width:250px"/></td></tr>'+
	'<tr><td valign="middle"><b>Project Name:</b></td><td><input type="TEXT" maxlength="255" id="pmAdmin-projName" style="width:250px"/></td></tr>'+
	'<tr><td valign="middle"><b>Project Wiki:</b></td><td><input type="TEXT" maxlength="255" id="pmAdmin-projWiki" style="width:250px"/></td></tr>'+
	'<tr><td valign="middle"><b>Project Key:</b></td><td><input type="TEXT" maxlength="255" id="pmAdmin-projKey" style="width:250px"/></td></tr>'+
	'<tr><td valign="middle"><b>Project Description:</b></td><td><textarea maxlength="2000" id="pmAdmin-projDesc" style="width:250px; height:126px"/></textarea></td></tr>'+
	'<tr><td valign="middle"><b>Project Path:</b></td><td><input type="TEXT" maxlength="255" id="pmAdmin-projPath" style="width:250px"/></td></tr>'+
	'<tr><td></td><td align="right"><input type="HIDDEN" value="" id="pmAdmin-projOrigKey"/><input type="BUTTON" value="Delete" onclick="i2b2.PM.admin.deleteProject();"/> <input type="BUTTON" value="Save Updates" onclick="i2b2.PM.admin.saveProject();"/> <input type="BUTTON" value="Cancel" onclick="i2b2.PM.view.admin.refreshScreen();"/></td></tr>'+
	'</table></div></form>';
i2b2.PM.model.helpMSGS.USER =  "<form><p>Click on \"User\" in the navigation bar to refresh the list of users.<br />Please select a user on the left to edit it's properties</p>"+
	'<div id="AddNewUserBtnDIV"><input type="BUTTON" value="Add New User" onclick="$(\'AddNewUserBtnDIV\').hide(); $(\'pmAdminMainTableview\').hide(); $(\'AddNewUserDIV\').show();"></div>'+
	'<div id="AddNewUserDIV" style="display:none">'+
	'<table border="0"><tbody><tr><td valign="middle"><b>User Name:</b></td><td><input type="TEXT" id="pmAdmin-userName" maxlength="50" style="width:250px"/></td></tr>'+
	'<tr><td valign="middle"><b>User Full Name:</b></td><td><input type="TEXT" maxlength="255" id="pmAdmin-userFullname" style="width:250px"/></td></tr>'+
	'<tr><td valign="middle"><b>User Email:</b></td><td><input type="TEXT" maxlength="255" id="pmAdmin-userEmail" style="width:250px"/></td></tr>'+
	'<tr><td valign="middle"><b>User Password:</b></td><td><input type="password" maxlength="255" id="pmAdmin-userPasswd1" style="width:250px"/></td></tr>'+
	'<tr><td valign="middle"><b>User Password (verify):</b></td><td><input type="password" maxlength="255" id="pmAdmin-userPasswd2" style="width:250px"/></td></tr>'+
	'<tr><td valign="middle"><b>Is Admin:</b></td><td><SELECT style="width:250px" id="pmAdmin-userIsAdmin"><OPTION value="false">No</option><OPTION value="true">Yes</option></select></td></tr>'+
	'<tr><td></td><td align="right"><input type="hidden" id="pmAdmin-newUser" value="true"><input type="BUTTON" value="Save" onclick="i2b2.PM.admin.saveUser();"/> <input type="BUTTON" value="Cancel" onclick="i2b2.PM.view.admin.refreshScreen();"/></td></tr>'+
	'</table></div></form>';
i2b2.PM.model.helpMSGS.USERREC = "<form><p>Please select which user configuration screen you want to access.</p>"+
	'<div>'+
	'<table border="0"><tbody><tr><td valign="middle"><b>User Name:</b></td><td><input type="TEXT" id="pmAdmin-userName" maxlength="50" style="width:250px"/></td></tr>'+
	'<tr><td valign="middle"><b>User Full Name:</b></td><td><input type="TEXT" maxlength="255" id="pmAdmin-userFullname" style="width:250px"/></td></tr>'+
	'<tr><td valign="middle"><b>User Email:</b></td><td><input type="TEXT" maxlength="255" id="pmAdmin-userEmail" style="width:250px"/></td></tr>'+
	'<tr><td valign="middle"><b>User Password:</b></td><td><input type="password" maxlength="255" id="pmAdmin-userPasswd1" style="width:250px"/></td></tr>'+
	'<tr><td valign="middle"><b>User Password (verify):</b></td><td><input type="password" maxlength="255" id="pmAdmin-userPasswd2" style="width:250px"/></td></tr>'+
	'<tr><td valign="middle"><b>Is Admin:</b></td><td><SELECT style="width:250px" id="pmAdmin-userIsAdmin"><OPTION value="false">No</option><OPTION value="true">Yes</option></select></td></tr>'+
	'<tr><td></td><td align="right"><input type="hidden" id="pmAdmin-newUser" value="false"><input type="BUTTON" value="Delete" onclick="i2b2.PM.admin.deleteUser();"/> <input type="BUTTON" value="Save" onclick="i2b2.PM.admin.saveUser();"/> <input type="BUTTON" value="Cancel" onclick="i2b2.PM.view.admin.refreshScreen();"/></td></tr>'+
	'</table></div></form>';
i2b2.PM.model.helpMSGS.PARAM =  "<form><p>Click on \"Parameter\" in the navigation bar to refresh the list of parameters.<br />Please select a parameter on the left to edit it's properties</p>"+
	'<div id="AddNewParameterBtnDIV"><input type="BUTTON" value="Add New Parameter" onclick="$(\'AddNewParameterBtnDIV\').hide(); $(\'AddNewParameterDIV\').show();"></div>'+
	'<div id="AddNewParameterDIV" style="display:none">'+
	'<table border="0"><tbody><tr><td valign="middle"><b>Parameter Name:</b></td><td><input type="TEXT" id="pmAdmin-paramName" maxlength="50" style="width:250px"/></td></tr>'+
	'<tr><td valign="middle"><b>Parameter Value:</b></td><td><input type="TEXT" maxlength="255" id="pmAdmin-paramValue" style="width:250px"/></td></tr>'+
	'<tr><td valign="middle"><b>Parameter Data Type:</b></td><td><SELECT style="width:250px" id="pmAdmin-paramDatatype"><OPTION value="T">Text</option><OPTION value="C">Reference Binary</option><OPTION value="N">Numeric</option><OPTION value="D">Date</option><OPTION value="I">Integer</option><OPTION value="B">Boolean</option><OPTION value="RTF">RTF</option><OPTION value="XLS">Excel</option><OPTION value="XML">XML</option><OPTION value="DOC">Word</option></select></td></tr>'+
	'<tr><td></td><td align="right"><input type="HIDDEN" value="" id="pmAdmin-paramId"/><input type="HIDDEN" value="" id="pmAdmin-paramTable"/><input type="BUTTON" value="Delete" onclick="i2b2.PM.admin.deleteParameter();"/> <input type="BUTTON" value="Save" onclick="i2b2.PM.admin.saveParameter();"/> <input type="BUTTON" value="Cancel" onclick="i2b2.PM.view.admin.refreshScreen();"/></td></tr>'+
	'</table></div></form>';
i2b2.PM.model.helpMSGS.PARAMREC = "<form><p>Please select which parameter screen you want to access.</p>"+
	'<div>'+
	'<table border="0"><tbody><tr><td valign="middle"><b>Parameter Name:</b></td><td><input type="TEXT" id="pmAdmin-paramName" maxlength="50" style="width:250px"/></td></tr>'+
	'<tr><td valign="middle"><b>Parameter Value:</b></td><td><input type="TEXT" maxlength="255" id="pmAdmin-paramValue" style="width:250px"/></td></tr>'+
	'<tr><td valign="middle"><b>Parameter Data Type:</b></td><td><SELECT style="width:250px" id="pmAdmin-paramDatatype"><OPTION value="T">Text</option><OPTION value="C">Reference Binary</option><OPTION value="N">Numeric</option><OPTION value="D">Date</option><OPTION value="I">Integer</option><OPTION value="B">Boolean</option><OPTION value="RTF">RTF</option><OPTION value="XLS">Excel</option><OPTION value="XML">XML</option><OPTION value="DOC">Word</option></select></td></tr>'+
	'<tr><td></td><td align="right"><input type="HIDDEN" value="" id="pmAdmin-paramId"/><input type="HIDDEN" value="" id="pmAdmin-paramTable"/><input type="BUTTON" value="Delete" onclick="i2b2.PM.admin.deleteParameter();"/> <input type="BUTTON" value="Save" onclick="i2b2.PM.admin.saveParameter();"/> <input type="BUTTON" value="Cancel" onclick="i2b2.PM.view.admin.refreshScreen();"/></td></tr>'+
	'</table></div></form>';	
i2b2.PM.model.helpMSGS.PROJECTUSER  = '<form> <fieldset id="checkboxbuttonsfrommarkup"><table valign="top"><tbody><!-- Header --><tr valign="top"><th width="150px">Admin Path</th><th width="150px">Data Path</th><th width="150px">Custom Path</th></tr><tr valign="top"><td><input type="checkbox" id="RoleMANAGER" onClick="i2b2.PM.view.admin.onCheckedChange(\'MANAGER\');" name="MANAGER">Manager<br><input type="checkbox" id="RoleUSER" onClick="i2b2.PM.view.admin.onCheckedChange(\'USER\');" name="USER">User<br>' +
'</td><td><input type="checkbox" id="RoleDATA_PROT" onClick="i2b2.PM.view.admin.onCheckedChange(\'DATA_PROT\');" name="DATA_PROT">Protected<br><input type="checkbox" id="RoleDATA_DEID" onClick="i2b2.PM.view.admin.onCheckedChange(\'DATA_DEID\');" name="DATA_DEID">De-identified Data<br><input type="checkbox" id="RoleDATA_LDS" onClick="i2b2.PM.view.admin.onCheckedChange(\'DATA_LDS\');" name="DATA_LDS">Limited Data Set<br><input type="checkbox" id="RoleDATA_AGG" onClick="i2b2.PM.view.admin.onCheckedChange(\'DATA_AGG\');"  name="DATA_AGG">Aggregated<br><input type="checkbox" id="RoleDATA_OBFSC"onClick="i2b2.PM.view.admin.onCheckedChange(\'DATA_OBFSC\');"   name="DATA_OBFSC">Obfuscated<br></td><td><input type="checkbox" id="RoleEDITOR" name="EDITOR">Editor</td></tr>' +
	'<tr><td></td><td colspan="2" align="right"><input type="HIDDEN" value="" id="pmAdmin-projOrigKey"/><input type="BUTTON" value="Delete" onclick="i2b2.PM.admin.deleteProjectUser();"/> <input type="BUTTON" value="Save" onclick="i2b2.PM.admin.saveProjectUser();"/> <input type="BUTTON" value="Cancel" onclick="i2b2.PM.view.admin.refreshScreen();"/></td></tr>'+
	'</table></div></fieldset></form>';
i2b2.PM.model.helpMSGS.PROJECTUSERS = "<form><p>Click on \"User\" in the navigation bar to refresh the list of users.<br />Please select a user on the left to edit it's properties</p>"+
	'<div id="AddNewUserBtnDIV"><input type="BUTTON" value="Add User to Project" onclick="$(\'AddNewUserBtnDIV\').hide();  $(\'AddNewUserDIV\').show();"></div>'+
	'<div id="AddNewUserDIV" style="display:none">'+
	'<table border="0"><tbody><tr><td valign="middle"><b>User Name:</b></td><td><input type="TEXT" id="pmAdmin-userName" maxlength="50" style="width:250px"/></td></tr>'+
	'<tr><td></td><td align="right"> <input type="BUTTON" value="Add User to Project" onclick="i2b2.PM.admin.addUserProject();"/> <input type="BUTTON" value="Cancel" onclick="i2b2.PM.view.admin.refreshScreen();"/></td></tr>'+
	'</table></div></form>';
i2b2.PM.model.adminButtonsPrimary = {};
i2b2.PM.model.adminButtonsPrimary["HIVEDOMAINS"] = "<form><input type='button' value='Save Updates' onclick=\"i2b2.PM.admin.clickActionBtn(1, 'UPDATE');\"/></form>";
i2b2.PM.model.adminButtonsPrimary["HIVECELLS"] = "<form><input type='button' value='Save Updates' onclick=\"i2b2.PM.admin.clickActionBtn(1, 'UPDATE');\"/><input type='button' value='Delete' onclick=\"i2b2.PM.admin.clickActionBtn(1, 'DELETE');\"/><input type='button' value='Add New' onclick=\"i2b2.PM.admin.clickActionBtn(1,'NEW');\"/></form>";
i2b2.PM.model.adminButtonsPrimary["HIVEGLOBALS"] = "<form><input type='button' value='Save Updates' onclick=\"i2b2.PM.admin.clickActionBtn(1, 'UPDATE');\"/><input type='button' value='Delete' onclick=\"i2b2.PM.admin.clickActionBtn(1, 'DELETE');\"/><input type='button' value='Add New' onclick=\"i2b2.PM.admin.clickActionBtn(1,'NEW');\"/></form>";
i2b2.PM.model.adminButtonsPrimary["USERS"] = "<form><input type='button' value='Save Updates' onclick=\"i2b2.PM.admin.clickActionBtn(1, 'UPDATE');\"/><input type='button' value='Delete' onclick=\"i2b2.PM.admin.clickActionBtn(1, 'DELETE');\"/><input type='button' value='Add New' onclick=\"i2b2.PM.admin.clickActionBtn(1,'NEW');\"/></form>";
i2b2.PM.model.adminButtonsPrimary["APPROVALS"] = "<form><input type='button' value='Save Updates' onclick=\"i2b2.PM.admin.clickActionBtn(1, 'UPDATE');\"/><input type='button' value='Delete' onclick=\"i2b2.PM.admin.clickActionBtn(1, 'DELETE');\"/><input type='button' value='Add New' onclick=\"i2b2.PM.admin.clickActionBtn(1,'NEW');\"/></form>";
i2b2.PM.model.adminButtonsPrimary["PROJECTREC-PARAMS"] = "<form><input type='button' value='Save Updates' onclick=\"i2b2.PM.admin.clickActionBtn(1, 'UPDATE');\"/><input type='button' value='Delete' onclick=\"i2b2.PM.admin.clickActionBtn(1, 'DELETE');\"/><input type='button' value='Add New' onclick=\"i2b2.PM.admin.clickActionBtn(1,'NEW');\"/></form>";
i2b2.PM.model.adminButtonsPrimary["PROJECTREC-CELLS"] = "<form><input type='button' value='Save Updates' onclick=\"i2b2.PM.admin.clickActionBtn(1, 'UPDATE');\"/><input type='button' value='Delete' onclick=\"i2b2.PM.admin.clickActionBtn(1, 'DELETE');\"/><input type='button' value='Add New' onclick=\"i2b2.PM.admin.clickActionBtn(1,'NEW');\"/></form>";
i2b2.PM.model.adminButtonsPrimary["PROJECTREC-USERS"] = "<form><input type='button' value='Save Updates' onclick=\"i2b2.PM.admin.clickActionBtn(1, 'UPDATE');\"/><input type='button' value='Delete' onclick=\"i2b2.PM.admin.clickActionBtn(1, 'DELETE');\"/><input type='button' value='Add New' onclick=\"i2b2.PM.admin.clickActionBtn(1,'NEW');\"/></form>";


i2b2.PM.model.adminColumnDef = {};
i2b2.PM.model.adminColumnDef["HIVEDOMAINS"] = [
	{key:"domain_id",sortable:true, resizeable:true, editor: new YAHOO.widget.TextboxCellEditor({disableBtns:true})}, 
	{key:"active",sortable:true, resizeable:true, editor: new YAHOO.widget.RadioCellEditor({radioOptions:["Active"],disableBtns:true})}, 
	{key:"environment", sortable:true, resizeable:true, editor: new YAHOO.widget.RadioCellEditor({radioOptions:["DEVELOPMENT","PRODUCTION", "TEST"],disableBtns:true})}, 
	{key:"domain_name",sortable:true, resizeable:true, editor: new YAHOO.widget.TextboxCellEditor({disableBtns:true})}, 
	{key:"helpURL",sortable:true, resizeable:true, editor: new YAHOO.widget.TextboxCellEditor({disableBtns:true})}
];
i2b2.PM.model.adminColumnDef["HIVECELLS"] = [
	{key:"id", sortable:true, resizeable:true, editor: new YAHOO.widget.TextboxCellEditor({disableBtns:true})}, 
	{key:"name", sortable:true, resizeable:true, editor: new YAHOO.widget.TextboxCellEditor({disableBtns:true})}, 
	{key:"method",sortable:true, resizeable:true, editor: new YAHOO.widget.RadioCellEditor({radioOptions:["SOAP","REST","OTHER"],disableBtns:true})}, 
	{key:"url",sortable:true, resizeable:true, editor: new YAHOO.widget.TextboxCellEditor({disableBtns:true})},
	{key:"project_path",sortable:true, resizeable:true, editor: new YAHOO.widget.TextboxCellEditor({disableBtns:true})}	
];
i2b2.PM.model.adminColumnDef["HIVEGLOBALS"] = [
	{key:"name", sortable:true, resizeable:true, editor: new YAHOO.widget.TextboxCellEditor({disableBtns:true})}, 
        {key:"datatype", sortable:true, resizeable:true, editor: new YAHOO.widget.RadioCellEditor({radioOptions:[{label:"Text", value:"T"}, {label:"Reference Text", value:"M"}, {label:"Reference Binary", value:"C"},{label:"Numeric", value:"N"},{label:"Date", value:"D"}, {label:"Integer", value:"I"}, {label:"Boolean", value:"B"}, {label:"RTF", value:"RTF"}, {label:"Excel", value:"XLS"}, {label:"XML", value:"XML"}, {label:"Word", value:"Doc"}],disableBtns:true})},
	{key:"value",sortable:true, resizeable:true,editor: new YAHOO.widget.TextboxCellEditor({disableBtns:true})}
];
i2b2.PM.model.adminColumnDef["USERPARAMS"] = [
	{key:"name", sortable:true, resizeable:true, editor: new YAHOO.widget.TextboxCellEditor({disableBtns:true})}, 
       {key:"datatype", sortable:true, resizeable:true, editor: new YAHOO.widget.RadioCellEditor({radioOptions:[{label:"Text", value:"T"}, {label:"Reference Text", value:"M"}, {label:"Reference Binary", value:"C"},{label:"Numeric", value:"N"},{label:"Date", value:"D"}, {label:"Integer", value:"I"}, {label:"Boolean", value:"B"}, {label:"RTF", value:"RTF"}, {label:"Excel", value:"XLS"}, {label:"XML", value:"XML"}, {label:"Word", value:"Doc"}],disableBtns:true})},
	{key:"value",sortable:true, resizeable:true,editor: new YAHOO.widget.TextboxCellEditor({disableBtns:true})}
];
i2b2.PM.model.adminColumnDef["PROJPARAMS"] = [
	{key:"name", sortable:true, resizeable:true, editor: new YAHOO.widget.TextboxCellEditor({disableBtns:true})}, 
       {key:"datatype", sortable:true, resizeable:true, editor: new YAHOO.widget.RadioCellEditor({radioOptions:[{label:"Text", value:"T"}, {label:"Reference Text", value:"M"}, {label:"Reference Binary", value:"C"},{label:"Numeric", value:"N"},{label:"Date", value:"D"}, {label:"Integer", value:"I"}, {label:"Boolean", value:"B"}, {label:"RTF", value:"RTF"}, {label:"Excel", value:"XLS"}, {label:"XML", value:"XML"}, {label:"Word", value:"Doc"}],disableBtns:true})},
	{key:"value",sortable:true, resizeable:true,editor: new YAHOO.widget.TextboxCellEditor({disableBtns:true})}
];
i2b2.PM.model.adminColumnDef["HIVECELLPARAMS"] = [
	{key:"name", sortable:true, resizeable:true, editor: new YAHOO.widget.TextboxCellEditor({disableBtns:true})}, 
       {key:"datatype", sortable:true, resizeable:true, editor: new YAHOO.widget.RadioCellEditor({radioOptions:[{label:"Text", value:"T"}, {label:"Reference Text", value:"M"}, {label:"Reference Binary", value:"C"},{label:"Numeric", value:"N"},{label:"Date", value:"D"}, {label:"Integer", value:"I"}, {label:"Boolean", value:"B"}, {label:"RTF", value:"RTF"}, {label:"Excel", value:"XLS"}, {label:"XML", value:"XML"}, {label:"Word", value:"Doc"}],disableBtns:true})},
	{key:"value",sortable:true, resizeable:true,editor: new YAHOO.widget.TextboxCellEditor({disableBtns:true})}
];

i2b2.PM.model.adminColumnDef["PROJECTREQUESTS"] = [
	{key:"id", sortable:true, resizeable:true}, 
	{key:"project_id",sortable:true, resizeable:true}, 
	{key:"title",sortable:true, resizeable:true}, 
	{key:"entry_date",sortable:true, resizeable:true, formatter:YAHOO.widget.DataTable.formatDate},
	{key:"submit_char",sortable:true, resizeable:true},
	{key:"request_xml",sortable:true, resizeable:true}

];

i2b2.PM.model.adminColumnDef["PROJECTS"] = [
	{key:"id", sortable:true, resizeable:true, editor: new YAHOO.widget.TextboxCellEditor({disableBtns:true})}, 
	{key:"name",sortable:true, resizeable:true, editor: new YAHOO.widget.TextboxCellEditor({disableBtns:true})}, 
	{key:"description",sortable:true, resizeable:true, editor: new YAHOO.widget.TextboxCellEditor({disableBtns:true})}, 
	{key:"key",sortable:true, resizeable:true, editor: new YAHOO.widget.TextboxCellEditor({disableBtns:true})}, 
	{key:"wiki",sortable:true, resizeable:true, editor: new YAHOO.widget.TextboxCellEditor({disableBtns:true})}, 
	{key:"path",sortable:true, resizeable:true, editor: new YAHOO.widget.TextboxCellEditor({disableBtns:true})}
]; 

i2b2.PM.model.adminColumnDef["APPROVALS"] = [
	{key:"id", sortable:true, resizeable:true, editor: new YAHOO.widget.TextboxCellEditor({disableBtns:true})}, 
	{key:"name",sortable:true, resizeable:true, editor: new YAHOO.widget.TextboxCellEditor({disableBtns:true})}, 
	{key:"description",sortable:true, resizeable:true, editor: new YAHOO.widget.TextboxCellEditor({disableBtns:true})}, 
	{key:"activation_date",sortable:true, resizeable:true, formatter:"myDate", editor: new YAHOO.widget.DateCellEditor()}, 
	{key:"expiration_date",sortable:true, resizeable:true,formatter:"myDate", editor: new YAHOO.widget.DateCellEditor()}
]; 

i2b2.PM.model.adminColumnDef["USERS"] = [
	{key:"full_name", sortable:true, resizeable:true, editor: new YAHOO.widget.TextboxCellEditor({disableBtns:true})}, 
	{key:"user_name",sortable:true, resizeable:true, editor: new YAHOO.widget.TextboxCellEditor({disableBtns:true})}, 
	{key:"email",sortable:true, resizeable:true, editor: new YAHOO.widget.TextboxCellEditor({disableBtns:true})}, 
	{key:"is_admin",sortable:true, resizeable:true, editor: new YAHOO.widget.TextboxCellEditor({disableBtns:true})}	
];

		
// build the columndef for project-user dynamically
var t = i2b2.PM.cfg.config.authRoles;
var l = t.length;
var codes = [];

for (var i=0; i<l; i++) {
	codes.push(t[i].code);
}
i2b2.PM.model.adminColumnDef["PROJECTREC-USERS"] = [
	{key:"user_name", sortable:true, resizeable:true, editor: new YAHOO.widget.TextboxCellEditor({disableBtns:true})},
	{key:"roles", sortable:false, resizeable:true, editor: new YAHOO.widget.CheckboxCellEditor({checkboxOptions:codes, disableBtns:false})}
];

i2b2.PM.model.adminButtonsSecondary = {};
i2b2.PM.model.adminButtonsSecondary["USERS"] = "<form><input type='button' value='Save Updates' onclick=\"i2b2.PM.admin.clickActionBtn(2, 'UPDATE');\"/><input type='button' value='Delete' onclick=\"i2b2.PM.admin.clickActionBtn(2, 'DELETE');\"/><input type='button' value='Add New' onclick=\"i2b2.PM.admin.clickActionBtn(2,'NEW');\"/></form>";
i2b2.PM.model.adminButtonsSecondary["HIVECELLS"] = "<form><input type='button' value='Save Updates' onclick=\"i2b2.PM.admin.clickActionBtn(2, 'UPDATE');\"/><input type='button' value='Delete' onclick=\"i2b2.PM.admin.clickActionBtn(2, 'DELETE');\"/><input type='button' value='Add New' onclick=\"i2b2.PM.admin.clickActionBtn(2,'NEW');\"/></form>";
i2b2.PM.model.adminButtonsSecondary["HIVEDOMAINS"] = "<form><input type='button' value='Save Updates' onclick=\"i2b2.PM.admin.clickActionBtn(2, 'UPDATE');\"/><input type='button' value='Delete' onclick=\"i2b2.PM.admin.clickActionBtn(2, 'DELETE');\"/><input type='button' value='Add New' onclick=\"i2b2.PM.admin.clickActionBtn(2,'NEW');\"/></form>";
i2b2.PM.model.adminButtonsSecondary["PROJECTREC-CELLS"] = "<form><input type='button' value='Save Updates' onclick=\"i2b2.PM.admin.clickActionBtn(2, 'UPDATE');\"/><input type='button' value='Delete' onclick=\"i2b2.PM.admin.clickActionBtn(2, 'DELETE');\"/><input type='button' value='Add New' onclick=\"i2b2.PM.admin.clickActionBtn(2,'NEW');\"/></form>";
i2b2.PM.model.adminButtonsSecondary["PROJECTREC-USERS"] = "<form><input type='button' value='Save Updates' onclick=\"i2b2.PM.admin.clickActionBtn(2, 'UPDATE');\"/><input type='button' value='Delete' onclick=\"i2b2.PM.admin.clickActionBtn(2, 'DELETE');\"/><input type='button' value='Add New' onclick=\"i2b2.PM.admin.clickActionBtn(2,'NEW');\"/></form>";



// visual screen configuration
// --------------------------------------------------------------------------------------------------------------------
i2b2.PM.view.admin.showInfoPanel = function(infoID) {
	if (!infoID) {
		$('pmAdminHelp').hide();
	} else {
		if (i2b2.PM.model.helpMSGS[infoID]) {
			$('pmAdminHelp').innerHTML = i2b2.PM.model.helpMSGS[infoID];
		} else {
			$('pmAdminHelp').innerHTML = "Could not find help message ["+infoID+"]";
		}
		$('pmAdminHelp').show();
	}
};

i2b2.PM.view.admin.onCheckedChange = function(items) {
	var items = items;
			if (items == "MANAGER")
			{
				document.getElementById("RoleMANAGER").checked = true;
				document.getElementById("RoleUSER").checked = true;
			} else 			if (items == "USER")
			{
				document.getElementById("RoleMANAGER").checked = false;
				document.getElementById("RoleUSER").checked = true;
			} else 			if (items == "DATA_PROT")
			{
				document.getElementById("RoleDATA_PROT").checked = true;
				document.getElementById("RoleDATA_DEID").checked = true;
				document.getElementById("RoleDATA_LDS").checked = true;
				document.getElementById("RoleDATA_AGG").checked = true;			
				document.getElementById("RoleDATA_OBFSC").checked = true;
			} else 			if (items == "DATA_DEID")
			{
				document.getElementById("RoleDATA_PROT").checked = false;
				document.getElementById("RoleDATA_DEID").checked = true;
				document.getElementById("RoleDATA_LDS").checked = true;
				document.getElementById("RoleDATA_AGG").checked = true;			
				document.getElementById("RoleDATA_OBFSC").checked = true;
			} else 			if (items == "DATA_LDS")
			{
				document.getElementById("RoleDATA_PROT").checked = false;
				document.getElementById("RoleDATA_DEID").checked = false;
				document.getElementById("RoleDATA_LDS").checked = true;
				document.getElementById("RoleDATA_AGG").checked = true;			
				document.getElementById("RoleDATA_OBFSC").checked = true;
			} else 			if (items == "DATA_AGG")
			{
				document.getElementById("RoleDATA_PROT").checked = false;
				document.getElementById("RoleDATA_DEID").checked = false;
				document.getElementById("RoleDATA_LDS").checked = false;
				document.getElementById("RoleDATA_AGG").checked = true;			
				document.getElementById("RoleDATA_OBFSC").checked = true;
			} else 			if (items == "DATA_OBFSC")
			{
				document.getElementById("RoleDATA_PROT").checked = false;
				document.getElementById("RoleDATA_DEID").checked = false;
				document.getElementById("RoleDATA_LDS").checked = false;
				document.getElementById("RoleDATA_AGG").checked = false;			
				document.getElementById("RoleDATA_OBFSC").checked = true;
			}
};

i2b2.PM.view.admin.configScreenDispay = function(dispLevel) {
	var configScreen = i2b2.PM.view.admin.configScreen;
	switch(dispLevel) {
		case 0:
			// no config
			Element.hide('pmAdminMainTableview');
			Element.hide('pmAdminTableviewButtons');
			Element.hide('pmAdminParamTableview');
			Element.hide('pmAdminParamTableviewButtons');
			break;
		case 1:
			// only the main grid
			$('pmAdminMainTableview').show();
			// display action buttons for main grid
			/*
			if (i2b2.PM.model.adminButtonsPrimary[configScreen]) {
				$('pmAdminTableviewButtons').innerHTML = i2b2.PM.model.adminButtonsPrimary[configScreen];
				Element.show('pmAdminTableviewButtons');
			} else {
				Element.hide('pmAdminTableviewButtons');
			}
			*/
			Element.hide('pmAdminParamTableview');
			Element.hide('pmAdminParamTableviewButtons');
			break;
		case 2:
			// main grid and parameters grid
			Element.show('pmAdminMainTableview');
			// display action buttons for main grid
			/*
			if (i2b2.PM.model.adminButtonsPrimary[configScreen]) {
				$('pmAdminTableviewButtons').innerHTML = i2b2.PM.model.adminButtonsPrimary[configScreen];
				Element.show('pmAdminTableviewButtons');
			} else {
				Element.hide('pmAdminTableviewButtons');
			}
			*/
			Element.show('pmAdminParamTableview');
			if (i2b2.PM.model.adminButtonsSecondary[configScreen]) {
				$('pmAdminParamTableviewButtons').innerHTML = i2b2.PM.model.adminButtonsSecondary[configScreen];
				Element.show('pmAdminParamTableviewButtons');
			} else {
				Element.hide('pmAdminParamTableviewButtons');
			}
			break;
	}
};



//i2b2.PM.view.admin.ProjectRequestViewer = {
//	show: function(request_xml) {
i2b2.PM.view.admin.ProjectRequestViewer =  function(request_xml) {
		var docXML = i2b2.h.parseXml(request_xml);

		var c = docXML.getElementsByTagName('project_request'); // YAHOO.DataType.XML.parse(request_xml); //loadXMLString(request_xml); 
		var l = c.length;
		for (var i=0; i<l; i++) {
			var tmpRec = {};
			var s = 	"<div class='ProjectRequest-MainContent'>" +
        "    <div class='droptrgtlbl'>Title:</div>" +
        "    <div class='outputOptions'>" + i2b2.h.getXNodeVal(c[i], "title") +
        "    </div>" +
        "    <br clear='all'>" +
        "    <div class='droptrgtlbl'>Approval:</div>" +
        "    <div class='outputOptions'>" + i2b2.h.getXNodeVal(c[i], "approval") +
        "    </div>" +
        "    <br clear='all'>" +
        "    <div style='display: block;' id='part2'>" +
        "      <div class='droptrgtlbl'>Users:</div>" +
        "      <div class='outputOptions'>" +
        "        <div id='ProjectRequest-roleItem' class='workarea'><table width='100%'><tbody><tr><td>User</td><td>Data Role</td><td>Admin Role</td></tr>";
		
					// include cases
			var c2 = i2b2.h.XPath(c[i], '//users/user');
			var l2 = c2.length;
							
			for (var i2=0; i2<l2; i2++) {
				s = s + 
					"        <tr><td>" + i2b2.h.getXNodeVal(c2[i2], "username") + "</td><td>" + i2b2.h.getXNodeVal(c2[i2], "data_role") + "</td><td>" + i2b2.h.getXNodeVal(c2[i2], "admin_role") + "</td></tr>";
			}
	
		s = s + "</tbody></table></div></div>";
		
		
			// include cases
			var c2 = i2b2.h.XPath(c[i], '//cases/item');
			var l2 = c2.length;
							
			if (l2 > 0)
				s = s +
					"      <br clear='all'>" +
       				"      <div class='droptrgtlbl'>Included cases:</div>" +
       		 		"      <div id='ProjectRequest-PRSDROP' class='droptrgt SDX-PRS'>";
			for (var i2=0; i2<l2; i2++) {
				s = s + 
					"        <div class='prsItem'>" + i2b2.h.getXNodeVal(c2[i2], "query_result_instance/description") + " - " + i2b2.h.getXNodeVal(c2[i2], "query_result_instance/result_instance_id") + "</div>";
			}

			if (l2 > 0)
        		s = s + "      </div>";
				
			// excluded patient cases
			var c2 = i2b2.h.XPath(c[i], '//exclude_cases/item');
			var l2 = c2.length;
							
			if (l2 > 0)
				s = s +
					"      <br clear='all'>" +
       				"      <div class='droptrgtlbl'>Exclude these patients from the cases:</div>" +
       		 		"      <div id='ProjectRequest-PRSDROP' class='droptrgt SDX-PRS'>";
			for (var i2=0; i2<l2; i2++) {
				s = s + 
					"        <div class='prsItem'>" + i2b2.h.getXNodeVal(c2[i2], "query_result_instance/description") + " - " + i2b2.h.getXNodeVal(c2[i2], "query_result_instance/result_instance_id") + "</div>";
			}

			if (l2 > 0)
        		s = s + "      </div>";				
	
			// include patient controls
			var c2 = i2b2.h.XPath(c[i], '//controls/item');
			var l2 = c2.length;
							
			if (l2 > 0)
				s = s +
					"      <br clear='all'>" +
       				"      <div class='droptrgtlbl'>Identify controls:</div>" +
       		 		"      <div id='ProjectRequest-PRSDROP' class='droptrgt SDX-PRS'>";
			for (var i2=0; i2<l2; i2++) {
				s = s + 
					"        <div class='prsItem'>" + i2b2.h.getXNodeVal(c2[i2], "query_result_instance/description") + " - " + i2b2.h.getXNodeVal(c2[i2], "query_result_instance/result_instance_id") + "</div>";
			}

			if (l2 > 0)
        		s = s + "      </div>";

		
			// excluded patient controls
			var c2 = i2b2.h.XPath(c[i], '//exclude_controls/item');
			var l2 = c2.length;
							
			if (l2 > 0)
				s = s +
					"      <br clear='all'>" +
       				"      <div class='droptrgtlbl'>Exclude these patients from the controls:</div>" +
       		 		"      <div id='ProjectRequest-PRSDROP' class='droptrgt SDX-PRS'>";
			for (var i2=0; i2<l2; i2++) {
				s = s + 
					"        <div class='prsItem'>" + i2b2.h.getXNodeVal(c2[i2], "query_result_instance/description") + " - " + i2b2.h.getXNodeVal(c2[i2], "query_result_instance/result_instance_id") + "</div>";
			}

			if (l2 > 0)
        		s = s + "      </div>";
				
		
			// Concept Included
			var c2 = i2b2.h.XPath(c[i], '//concept_include/item');
			var l2 = c2.length;
							
			if (l2 > 0)
				s = s +
					"      <br clear='all'>" +
       				"      <div class='droptrgtlbl'>Identify Concepts to be Included:</div>" +
       		 		"      <div id='ProjectRequest-PRSDROP' class='droptrgt SDX-PRS'>";
			for (var i2=0; i2<l2; i2++) {
				s = s + 
					"        <div class='prsItem'>" + i2b2.h.getXNodeVal(c2[i2], "concept/name") + " - " + i2b2.h.getXNodeVal(c2[i2], "concept/dimcode") + "</div>";
			}

			if (l2 > 0)
        		s = s + "      </div>";
		

			//Concept Excluded	
			var c2 = i2b2.h.XPath(c[i], '//concept_exclude/item');
			var l2 = c2.length;
							
			if (l2 > 0)  
				s = s +
					"      <br clear='all'>" +
       				"      <div class='droptrgtlbl'>Exclude these concepts:</div>" +
       		 		"      <div id='ProjectRequest-PRSDROP' class='droptrgt SDX-PRS'>";
			for (var i2=0; i2<l2; i2++) {
				s = s + 
					"        <div class='prsItem'>" + i2b2.h.getXNodeVal(c2[i2], "concept/name") + " - " + i2b2.h.getXNodeVal(c2[i2], "concept/dimcode") + "</div>";
			} 

			if (l2 > 0)
        		s = s + "      </div>";

		
        	s = s +"    </div>" +
        		"  </div>";

		}
		
		$('pmAdminHelp').innerHTML = s;

		
		//"<pre>" + request_xml + "</pre>";
		// load the help page
		//new Ajax.Updater('help-viewer-body', 'help/default.htm', {method: 'get', parameters: { cell: 'CORE', page:'ROOT' }});
//	}
	//,
	//hide: function() {
//	try {
//			i2b2.PM.view.admin.ProjectRequestViewer.yuiPanel.hide();
	//	} catch (e) {}
//	}
};



// data retreval and display

i2b2.PM.view.admin.showProjectUsers = function() {
	var proj_data = i2b2.PM.view.admin.currentProject;
	$('pmMainTitle').innerHTML = 'Project &gt; "'+proj_data.label+'" &gt; Users &gt; "' + proj_data.i2b2NodeUsername +'" &gt; Roles';
	i2b2.PM.view.admin.showInfoPanel(false);
	//i2b2.PM.view.admin.configScreenDispay(1);
	// get data
	var recList = i2b2.PM.ajax.getAllRoleUser("PM:Admin", {id: proj_data.i2b2NodeKey, proj_path:"/"+proj_data.i2b2NodeKey, username:proj_data.i2b2NodeUsername});
		
	i2b2.PM.view.admin.showInfoPanel("PROJECTUSER");
	i2b2.PM.view.admin.configScreenDispay(0);

	// custom parse functionality
	//var tmpRoles = {};
	var c = i2b2.h.XPath(recList.refXML, "//role[user_name and role]");
	var l = c.length;
	for (var i=0; i<l; i++) {
		try {
			
			document.getElementById("Role" + i2b2.h.XPath(c[i], "descendant-or-self::role/role/text()")[0].nodeValue).checked = true; 
			//var name = i2b2.h.XPath(c[i], "descendant-or-self::role/user_name/text()")[0].nodeValue;
			//if (!tmpRoles[name]) { tmpRoles[name] = []; }
			//tmpRoles[name].push(i2b2.h.XPath(c[i], "descendant-or-self::role/role/text()")[0].nodeValue);
		} catch(e) {}
	}
	
	delete recList;
};


i2b2.PM.view.admin.showUserParams = function(usrname) {
	// show the secondary grid
	i2b2.PM.view.admin.configScreenDispay(2);
	// get data
	var recList = i2b2.PM.ajax.getAllParam("PM:Admin", {table:"user", id_xml:"<user_name>"+usrname+"</user_name>"});
	recList.parse(usrname);
	var tmp = recList.model;
	// create datasource
	i2b2.PM.admin.dsSecondary = new YAHOO.util.DataSource(tmp);
	i2b2.PM.admin.dsSecondary.responseType = YAHOO.util.DataSource.TYPE_JSARRAY;
	i2b2.PM.admin.dsSecondary.responseType.responseSchema = { fields: ["name","datatype","value"] };
	delete recList.model;
	delete recList;
	// create grid
	i2b2.PM.admin.grdSecondaryColumnDefs = i2b2.PM.model.adminColumnDef.USERPARAMS;
	if (l > 10)
    var oConfigs = { 
	                paginator: new YAHOO.widget.Paginator({ 
	                    rowsPerPage: 10 
	                }), 
	                initialRequest: "results=" + l 
	        }; 	
	var t = new YAHOO.widget.DataTable("pmAdminParamTableview", i2b2.PM.admin.grdSecondaryColumnDefs, i2b2.PM.admin.dsSecondary, oConfigs);
	i2b2.PM.view.admin.yuiControls.secondaryGrid = t;
	t.isDirty = false;
	t.subscribe("rowMouseoverEvent", t.onEventHighlightCell);
	t.subscribe("rowMouseoutEvent", t.onEventUnhighlightCell);
	t.subscribe("cellClickEvent", i2b2.PM.view.admin.paramgridClickHandler);
	t.subscribe("editorSaveEvent", i2b2.PM.view.admin.editorSaved);
};


i2b2.PM.view.admin.showCellParams = function(cellName, path) {
	if (undefined == path) { path = "/"; }
	// show the secondary grid
	i2b2.PM.view.admin.configScreenDispay(2);
	// get data
	var recList = i2b2.PM.ajax.getAllParam("PM:Admin", {table:"cell", param_xml: " id='"+cellName+"' ", id_xml:"<project_path>"+path+"</project_path>"});
	// custom parse functionality
	var tmp = [];
	var c = i2b2.h.XPath(recList.refXML, "//param[@name and @id]");
	var l = c.length;
	for (var i=0; i<l; i++) {
		try {
			var tmpRec = {};
			tmpRec.name = i2b2.h.XPath(c[i], "attribute::name")[0].nodeValue;
			tmpRec.id = i2b2.h.XPath(c[i], "attribute::id")[0].nodeValue;
			tmpRec.datatype = i2b2.h.XPath(c[i], "attribute::datatype")[0].nodeValue;
			tmpRec.value = i2b2.h.XPath(c[i], "text()")[0].nodeValue;
			tmp.push(tmpRec);
		} catch(e) {}
	}
	// create datasource
	i2b2.PM.admin.dsSecondary = new YAHOO.util.DataSource(tmp);
	i2b2.PM.admin.dsSecondary.responseType = YAHOO.util.DataSource.TYPE_JSARRAY;
	i2b2.PM.admin.dsSecondary.responseType.responseSchema = { fields: ["name","datatype","value"] };
	delete recList.model;
	delete recList;
	// create grid
	i2b2.PM.admin.grdSecondaryColumnDefs = i2b2.PM.model.adminColumnDef.HIVECELLPARAMS;
	if (l > 10)
    var oConfigs = { 
	                paginator: new YAHOO.widget.Paginator({ 
	                    rowsPerPage: 10 
	                }), 
	                initialRequest: "results=" + l 
	        }; 	
	var t = new YAHOO.widget.DataTable("pmAdminParamTableview", i2b2.PM.admin.grdSecondaryColumnDefs, i2b2.PM.admin.dsSecondary, oConfigs);
	i2b2.PM.view.admin.yuiControls.secondaryGrid = t;
	t.isDirty = false;
	t.subscribe("rowMouseoverEvent", t.onEventHighlightCell);
	t.subscribe("rowMouseoutEvent", t.onEventUnhighlightCell);
	t.subscribe("cellClickEvent", i2b2.PM.view.admin.paramgridClickHandler);
	t.subscribe("editorSaveEvent", i2b2.PM.view.admin.editorSaved);
};


i2b2.PM.view.admin.showDomainParams = function(DomainID) {
	// show the secondary grid
	i2b2.PM.view.admin.configScreenDispay(2);
	// get data
	var recList = i2b2.PM.ajax.getAllParam("PM:Admin", {table:"hive", id_xml:DomainID});
	// custom parse functionality
	var tmp = [];
	var c = i2b2.h.XPath(recList.refXML, "//param[@name and @id]");
	var l = c.length;
	for (var i=0; i<l; i++) {
		try {
			var tmpRec = {};
			tmpRec.name = i2b2.h.XPath(c[i], "attribute::name")[0].nodeValue;
			tmpRec.id = i2b2.h.XPath(c[i], "attribute::id")[0].nodeValue;
			tmpRec.datatype = i2b2.h.XPath(c[i], "attribute::datatype")[0].nodeValue;
			tmpRec.value = i2b2.h.XPath(c[i], "text()")[0].nodeValue;
			tmp.push(tmpRec);
		} catch(e) {}
	}
	// create datasource
	i2b2.PM.admin.dsSecondary = new YAHOO.util.DataSource(tmp);
	i2b2.PM.admin.dsSecondary.responseType = YAHOO.util.DataSource.TYPE_JSARRAY;
	i2b2.PM.admin.dsSecondary.responseType.responseSchema = { fields: ["name","datatype","value"] };
	delete recList.model;
	delete recList;
	// create grid
	i2b2.PM.admin.grdSecondaryColumnDefs = i2b2.PM.model.adminColumnDef.HIVECELLPARAMS;
	if (l > 10)
    var oConfigs = { 
	                paginator: new YAHOO.widget.Paginator({ 
	                    rowsPerPage: 10 
	                }), 
	                initialRequest: "results=" + l 
	        }; 	
	var t = new YAHOO.widget.DataTable("pmAdminParamTableview", i2b2.PM.admin.grdSecondaryColumnDefs, i2b2.PM.admin.dsSecondary, oConfigs);
	i2b2.PM.view.admin.yuiControls.secondaryGrid = t;
	t.isDirty = false;
	t.subscribe("rowMouseoverEvent", t.onEventHighlightCell);
	t.subscribe("rowMouseoutEvent", t.onEventUnhighlightCell);
	t.subscribe("cellClickEvent", i2b2.PM.view.admin.paramgridClickHandler);
	t.subscribe("editorSaveEvent", i2b2.PM.view.admin.editorSaved);
};


i2b2.PM.view.admin.showProjectCells = function() {
	var proj_data = i2b2.PM.view.admin.currentProject;
	$('pmMainTitle').innerHTML = 'Project &gt; "'+proj_data.label+'" &gt; Cells';
	i2b2.PM.view.admin.showInfoPanel(false);
	i2b2.PM.view.admin.configScreenDispay(1);
	// create the filtered DataSource
	i2b2.PM.admin.refreshCellListData(); 
	var tmp = [];
	for (var idx in i2b2.PM.model.admin.CellList) {
		if (i2b2.PM.model.admin.CellList[idx].project_path == "/"+proj_data.i2b2NodeKey) {
			tmp.push(i2b2.PM.model.admin.CellList[idx]);
		}
	}
	i2b2.PM.admin.dsPrimary = new YAHOO.util.DataSource(tmp);
	i2b2.PM.admin.dsPrimary.responseType = YAHOO.util.DataSource.TYPE_JSARRAY;
	i2b2.PM.admin.dsPrimary.responseType.responseSchema = {
		fields: ["id","method","name","url"]
	};
	// create the grid
	i2b2.PM.admin.grdPrimaryColumnDefs = i2b2.PM.model.adminColumnDef.HIVECELLS;
	if (l > 10)
    var oConfigs = { 
	                paginator: new YAHOO.widget.Paginator({ 
	                    rowsPerPage: 10 
	                }), 
	                initialRequest: "results=" + l 
	        }; 	
	var t = new YAHOO.widget.DataTable("pmAdminMainTableview", i2b2.PM.admin.grdPrimaryColumnDefs, i2b2.PM.admin.dsPrimary, oConfigs);
	i2b2.PM.view.admin.yuiControls.primaryGrid = t;
	t.isDirty = false;
	//t.subscribe("rowMouseoverEvent", t.onEventHighlightCell);
	//t.subscribe("rowMouseoutEvent", t.onEventUnhighlightCell);
	//t.subscribe("cellClickEvent", i2b2.PM.view.admin.gridClickHandler);
	//t.subscribe("editorSaveEvent", i2b2.PM.view.admin.editorSaved);
};


i2b2.PM.view.admin.showProjectParams = function() {
	var proj_data = i2b2.PM.view.admin.currentProject;
	$('pmMainTitle').innerHTML = 'Project &gt; "'+proj_data.label+'" &gt; Params';
	i2b2.PM.view.admin.showInfoPanel(false);
	i2b2.PM.view.admin.configScreenDispay(1);
	// get data
	var recList = i2b2.PM.ajax.getAllParam("PM:Admin", {table:'project', id_xml:proj_data.i2b2NodeKey});
	// custom parse functionality
	var tmp = [];
	var c = i2b2.h.XPath(recList.refXML, "//param[@name and @id]");
	var l = c.length;
	for (var i=0; i<l; i++) {
		try {
			var tmpRec = {};
			tmpRec.name = i2b2.h.XPath(c[i], "attribute::name")[0].nodeValue;
			tmpRec.id = i2b2.h.XPath(c[i], "attribute::id")[0].nodeValue;
			tmpRec.datatype = i2b2.h.XPath(c[i], "attribute::datatype")[0].nodeValue;
			tmpRec.value = i2b2.h.XPath(c[i], "text()")[0].nodeValue;
			tmp.push(tmpRec);
		} catch(e) {}
	}
	// create datasource
	i2b2.PM.admin.dsPrimary = new YAHOO.util.DataSource(tmp);
	i2b2.PM.admin.dsPrimary.responseType = YAHOO.util.DataSource.TYPE_JSARRAY;
	i2b2.PM.admin.dsPrimary.responseType.responseSchema = {fields: ["name","datatype","value"]};
	delete recList;
	// create grid
	i2b2.PM.admin.grdPrimaryColumnDefs = i2b2.PM.model.adminColumnDef.PROJPARAMS;
	if (l > 10)
    var oConfigs = { 
	                paginator: new YAHOO.widget.Paginator({ 
	                    rowsPerPage: 10 
	                }), 
	                initialRequest: "results=" + l 
	        }; 	
	var t = new YAHOO.widget.DataTable("pmAdminMainTableview", i2b2.PM.admin.grdPrimaryColumnDefs, i2b2.PM.admin.dsPrimary, oConfigs);
	i2b2.PM.view.admin.yuiControls.primaryGrid = t;
	t.isDirty = false;
	t.subscribe("rowMouseoverEvent", t.onEventHighlightCell);
	t.subscribe("rowMouseoutEvent", t.onEventUnhighlightCell);
	t.subscribe("cellClickEvent", i2b2.PM.view.admin.gridClickHandler);
	t.subscribe("editorSaveEvent", i2b2.PM.view.admin.editorSaved);
};

i2b2.PM.view.admin.showProjectRequests = function() {
	$('pmMainTitle').innerHTML = "Project Requests";
	i2b2.PM.view.admin.showInfoPanel(false);
	i2b2.PM.view.admin.configScreenDispay(1);
	// get a list of user info
	var usrList = i2b2.PM.ajax.getAllProjectRequest("PM:Admin", {});
	usrList.parse();
	var tmp = [];
	var l = usrList.model.length;
	for (var i=0; i<l; i++) {
		tmp.push(usrList.model[i]);
	}
	delete usrList.model;
	delete usrList;		
	i2b2.PM.admin.dsPrimary = new YAHOO.util.DataSource(tmp);
	i2b2.PM.admin.dsPrimary.responseType = YAHOO.util.DataSource.TYPE_JSARRAY;
	i2b2.PM.admin.dsPrimary.responseType.responseSchema = {fields: ["id","project_id","submit_char","title",{key:"entry_date",parser:myDateParser},"request_xml"]};
	//i2b2.PM.admin.dsPrimary.hideColumn("request_xml"); 
	// create the grid
	i2b2.PM.admin.grdPrimaryColumnDefs = i2b2.PM.model.adminColumnDef.PROJECTREQUESTS;
	if (l > 10)
    var oConfigs = { 
	                paginator: new YAHOO.widget.Paginator({ 
	                    rowsPerPage: 10 
	                }), 
	                initialRequest: "results=" + l 
	        }; 	
	var t = new YAHOO.widget.DataTable("pmAdminMainTableview", i2b2.PM.admin.grdPrimaryColumnDefs, i2b2.PM.admin.dsPrimary, oConfigs);
	t.hideColumn("request_xml"); 
	i2b2.PM.view.admin.yuiControls.primaryGrid = t;
	t.isDirty = false;
	//t.subscribe("rowMouseoverEvent", t.onEventHighlightCell);
	//t.subscribe("rowMouseoutEvent", t.onEventUnhighlightCell);
	//t.subscribe("cellClickEvent", i2b2.PM.view.admin.gridClickHandler);
	//t.subscribe("editorSaveEvent", i2b2.PM.view.admin.editorSaved);
};


var myDateParser = function (sDate) {
// break up the date components and return a date object

	//var t = YAHOO.widget.DateMath.add(new Date(sDate), YAHOO.widget.DateMath.DAY, 1);new Date(sDate);
	//return t;
	//alert(sDate);
	
		//var t = YAHOO.widget.DateMath.add(new Date(sDate), YAHOO.widget.DateMath.DAY, 1);new Date(sDate);
		//alert(sDate);
		//2010-10-20
		//1234567890
		if (!Object.isUndefined(sDate)) {
			//alert(sDate.substring(0,4) + ":" + sDate.substring(5,7)  + ":" + sDate.substring(8,10));
			var t =  new Date(sDate.substring(0,4), sDate.substring(5,7)-1, sDate.substring(8,10), 12,0,0,0);
			return t;
		}
		else
		{
			return new Date(sDate);
		}
	//return new Date();
};

i2b2.PM.view.admin.showApprovals = function() {
	$('pmMainTitle').innerHTML = "Manage Approvals";
	i2b2.PM.view.admin.showInfoPanel(false);
	i2b2.PM.view.admin.configScreenDispay(1);
	// get a list of user info
	var usrList = i2b2.PM.ajax.getAllApproval("PM:Admin", {});
	usrList.parse();
	var tmp = [];
	var l = usrList.model.length;
	for (var i=0; i<l; i++) {
		tmp.push(usrList.model[i]);
	}
	delete usrList.model;
	delete usrList;		
	i2b2.PM.admin.dsPrimary = new YAHOO.util.DataSource(tmp);
	i2b2.PM.admin.dsPrimary.responseType = YAHOO.util.DataSource.TYPE_JSARRAY;
	i2b2.PM.admin.dsPrimary.responseType.responseSchema = {fields: ["id","name","description", {key:"activation_date",parser:myDateParser}, {key:"expiration_date",parser:myDateParser}, "object"]};
	// create the grid
	i2b2.PM.admin.grdPrimaryColumnDefs = i2b2.PM.model.adminColumnDef.APPROVALS;
	if (l > 10)	
    var oConfigs = { 
	                paginator: new YAHOO.widget.Paginator({ 
	                    rowsPerPage: 10 
	                }), 
	                initialRequest: "results=" + l 
	        }; 	
	var t = new YAHOO.widget.DataTable("pmAdminMainTableview", i2b2.PM.admin.grdPrimaryColumnDefs, i2b2.PM.admin.dsPrimary, oConfigs);
	i2b2.PM.view.admin.yuiControls.primaryGrid = t;
	t.isDirty = false;
	//t.subscribe("rowMouseoverEvent", t.onEventHighlightCell);
	//t.subscribe("rowMouseoutEvent", t.onEventUnhighlightCell);
	//t.subscribe("cellClickEvent", i2b2.PM.view.admin.gridClickHandler);
	//t.subscribe("editorSaveEvent", i2b2.PM.view.admin.editorSaved);
};



i2b2.PM.view.admin.showProjects = function() {
	$('pmMainTitle').innerHTML = "Manage Projects";
	i2b2.PM.view.admin.showInfoPanel(false);
	i2b2.PM.view.admin.configScreenDispay(1);
	// get a list of user info
	var usrList = i2b2.PM.ajax.getAllProject("PM:Admin", {});
	usrList.parse();
	var tmp = [];
	var l = usrList.model.length;
	for (var i=0; i<l; i++) {
		tmp.push(usrList.model[i]);
	}
	delete usrList.model;
	delete usrList;		
	i2b2.PM.admin.dsPrimary = new YAHOO.util.DataSource(tmp);
	i2b2.PM.admin.dsPrimary.responseType = YAHOO.util.DataSource.TYPE_JSARRAY;
	i2b2.PM.admin.dsPrimary.responseType.responseSchema = {fields: ["id","name","key", "wiki", "path", "description"]};
	// create the grid
	i2b2.PM.admin.grdPrimaryColumnDefs = i2b2.PM.model.adminColumnDef.PROJECTS;
	if (l > 10)	 
	        var oConfigs = { 
	                paginator: new YAHOO.widget.Paginator({ 
	                    rowsPerPage: 10 
	                }), 
	                initialRequest: "results=" + l 
	        }; 
	var t = new YAHOO.widget.DataTable("pmAdminMainTableview", i2b2.PM.admin.grdPrimaryColumnDefs, i2b2.PM.admin.dsPrimary, oConfigs);
	i2b2.PM.view.admin.yuiControls.primaryGrid = t;
	t.isDirty = false;
	//t.subscribe("rowMouseoverEvent", t.onEventHighlightCell);
	//t.subscribe("rowMouseoutEvent", t.onEventUnhighlightCell);
	//t.subscribe("cellClickEvent", i2b2.PM.view.admin.gridClickHandler);
	//t.subscribe("editorSaveEvent", i2b2.PM.view.admin.editorSaved);
};


i2b2.PM.view.admin.showUsers = function() {
	$('pmMainTitle').innerHTML = "Manage Users";
	i2b2.PM.view.admin.showInfoPanel(false);
	i2b2.PM.view.admin.configScreenDispay(1);
	// get a list of user info
	var usrList = i2b2.PM.ajax.getAllUser("PM:Admin", {});
	usrList.parse();
	var tmp = [];
	var l = usrList.model.length;
	for (var i=0; i<l; i++) {
		tmp.push(usrList.model[i]);
	}
	delete usrList.model;
	delete usrList;		
	i2b2.PM.admin.dsPrimary = new YAHOO.util.DataSource(tmp);
	i2b2.PM.admin.dsPrimary.responseType = YAHOO.util.DataSource.TYPE_JSARRAY;
	i2b2.PM.admin.dsPrimary.responseType.responseSchema = {fields: ["user_name","full_name","password", "email", "is_admin"]};
	// create the grid
	i2b2.PM.admin.grdPrimaryColumnDefs = i2b2.PM.model.adminColumnDef.USERS;
	if (l > 10)	 
	        var oConfigs = { 
	                paginator: new YAHOO.widget.Paginator({ 
	                    rowsPerPage: 10 
	                }), 
	                initialRequest: "results=" + l 
	        }; 
	var t = new YAHOO.widget.DataTable("pmAdminMainTableview", i2b2.PM.admin.grdPrimaryColumnDefs, i2b2.PM.admin.dsPrimary, oConfigs);
	i2b2.PM.view.admin.yuiControls.primaryGrid = t;
	t.isDirty = false;
	//t.subscribe("rowMouseoverEvent", t.onEventHighlightCell);
	//t.subscribe("rowMouseoutEvent", t.onEventUnhighlightCell);
	//t.subscribe("cellClickEvent", i2b2.PM.view.admin.gridClickHandler);
	//t.subscribe("editorSaveEvent", i2b2.PM.view.admin.editorSaved);
};



i2b2.PM.view.admin.showHiveDomains = function() {
	$('pmMainTitle').innerHTML = "Hive &gt; Domains";
	i2b2.PM.view.admin.showInfoPanel(false);
	i2b2.PM.view.admin.configScreenDispay(1);
	// get data
	var recList = i2b2.PM.ajax.getAllHive("PM:Admin", {});
	recList.parse();
	var tmp = recList.model;
	var l = tmp.length;
	for (var i=0;i<l;i++) {
		if (Boolean.parseTo(tmp[i].active)) {
			tmp[i].active = "Active";
		} else {
			tmp[i].active = "Inactive";
		}
	}

	// create datasource
	i2b2.PM.admin.dsPrimary = new YAHOO.util.DataSource(tmp);
	i2b2.PM.admin.dsPrimary.responseType = YAHOO.util.DataSource.TYPE_JSARRAY;
	i2b2.PM.admin.dsPrimary.responseType.responseSchema = {
		fields: ["domain_id","domain_name","environment","helpURL","active"]
	};
	delete recList.model;
	delete recList;
	// create grid
	i2b2.PM.admin.grdPrimaryColumnDefs = i2b2.PM.model.adminColumnDef.HIVEDOMAINS;
	if (l > 10)
    var oConfigs = { 
	                paginator: new YAHOO.widget.Paginator({ 
	                    rowsPerPage: 10 
	                }), 
	                initialRequest: "results=" + l 
	        }; 	
	var t = new YAHOO.widget.DataTable("pmAdminMainTableview", i2b2.PM.admin.grdPrimaryColumnDefs, i2b2.PM.admin.dsPrimary, oConfigs);
	i2b2.PM.view.admin.yuiControls.primaryGrid = t;
	t.isDirty = false;
	t.subscribe("rowMouseoverEvent", t.onEventHighlightCell);
	t.subscribe("rowMouseoutEvent", t.onEventUnhighlightCell);
	t.subscribe("cellClickEvent", i2b2.PM.view.admin.gridClickHandler);
	t.subscribe("editorSaveEvent", i2b2.PM.view.admin.editorSaved);
};

i2b2.PM.view.admin.showHiveCells = function() {
	$('pmMainTitle').innerHTML = "Cells";
	i2b2.PM.view.admin.showInfoPanel(false);
	i2b2.PM.view.admin.configScreenDispay(1);
	// refresh data
	i2b2.PM.admin.refreshCellListData();
	var l = 0;
	// create the filtered DataSource
	var tmp = [];
	for (var idx in i2b2.PM.model.admin.CellList) {
		//if (i2b2.PM.model.admin.CellList[idx].project_path == "/") {
			tmp.push(i2b2.PM.model.admin.CellList[idx]);
			l++;
		//}
	}
	i2b2.PM.admin.dsPrimary = new YAHOO.util.DataSource(tmp);
	i2b2.PM.admin.dsPrimary.responseType = YAHOO.util.DataSource.TYPE_JSARRAY;
	i2b2.PM.admin.dsPrimary.responseType.responseSchema = {
		fields: ["id","method","name","url","project_path"]
	};
	// create the grid
	i2b2.PM.admin.grdPrimaryColumnDefs = i2b2.PM.model.adminColumnDef.HIVECELLS;
	if (l > 10)
    var oConfigs = { 
	                paginator: new YAHOO.widget.Paginator({ 
	                    rowsPerPage: 10 
	                }), 
	                initialRequest: "results=" + l 
	        }; 	
	var t = new YAHOO.widget.DataTable("pmAdminMainTableview", i2b2.PM.admin.grdPrimaryColumnDefs, i2b2.PM.admin.dsPrimary, oConfigs);
	i2b2.PM.view.admin.yuiControls.primaryGrid = t;
	t.isDirty = false;
	//t.subscribe("rowMouseoverEvent", t.onEventHighlightCell);
	//t.subscribe("rowMouseoutEvent", t.onEventUnhighlightCell);
	//t.subscribe("cellClickEvent", i2b2.PM.view.admin.gridClickHandler);
	//t.subscribe("editorSaveEvent", i2b2.PM.view.admin.editorSaved);
};

i2b2.PM.view.admin.showGlobals = function() {
	$('pmMainTitle').innerHTML = "Hive &gt; Global Parameters";
	i2b2.PM.view.admin.showInfoPanel(false);
	i2b2.PM.view.admin.configScreenDispay(1);
	// get data
	var recList = i2b2.PM.ajax.getAllGlobal("PM:Admin", {});i2b2.PM.model.admin.CellList
	recList.parse();
	var tmp = recList.model;
	// create datasource
	i2b2.PM.admin.dsPrimary = new YAHOO.util.DataSource(tmp);
	i2b2.PM.admin.dsPrimary.responseType = YAHOO.util.DataSource.TYPE_JSARRAY;
	i2b2.PM.admin.dsPrimary.responseType.responseSchema = {fields: ["name","value"]};
	delete recList.model;
	delete recList;
	// create grid
	i2b2.PM.admin.grdPrimaryColumnDefs = i2b2.PM.model.adminColumnDef.HIVEGLOBALS;
	if (l > 10)
	    var oConfigs = { 
	                paginator: new YAHOO.widget.Paginator({ 
	                    rowsPerPage: 10 
	                }), 
	                initialRequest: "results=" + l 
	        }; 	
	var t = new YAHOO.widget.DataTable("pmAdminMainTableview", i2b2.PM.admin.grdPrimaryColumnDefs, i2b2.PM.admin.dsPrimary, oConfigs);
	i2b2.PM.view.admin.yuiControls.primaryGrid = t;
	t.isDirty = false;
	t.subscribe("rowMouseoverEvent", t.onEventHighlightCell);
	t.subscribe("rowMouseoutEvent", t.onEventUnhighlightCell);
	t.subscribe("cellClickEvent", i2b2.PM.view.admin.gridClickHandler);
	t.subscribe("editorSaveEvent", i2b2.PM.view.admin.editorSaved);
};

i2b2.PM.view.admin.showUserProjParams = function(username, project) {
	i2b2.PM.view.admin.configScreenDispay(2);
	// get data
	var recList = i2b2.PM.ajax.getAllParam("PM:Admin", {table:"project_user", param_xml:' id="'+project+'"', id_xml:"<user_name>"+username+"</user_name>"});
	// custom parse functionality
	var tmp = [];
	var c = i2b2.h.XPath(recList.refXML, "//param[@name and @id]");
	var l = c.length;
	for (var i=0; i<l; i++) {
		try {
			var tmpRec = {};
			tmpRec.name = i2b2.h.XPath(c[i], "attribute::name")[0].nodeValue;
			tmpRec.id = i2b2.h.XPath(c[i], "attribute::id")[0].nodeValue;
			tmpRec.datatype = i2b2.h.XPath(c[i], "attribute::datatype")[0].nodeValue;
			tmpRec.value = i2b2.h.XPath(c[i], "text()")[0].nodeValue;
			tmp.push(tmpRec);
		} catch(e) {}
	}
	// create datasource
	i2b2.PM.admin.dsSecondary = new YAHOO.util.DataSource(tmp);
	i2b2.PM.admin.dsSecondary.responseType = YAHOO.util.DataSource.TYPE_JSARRAY;
	i2b2.PM.admin.dsSecondary.responseType.responseSchema = { fields: ["name","datatype","value"] };
	delete recList;
	// create grid
	i2b2.PM.admin.grdSecondaryColumnDefs = i2b2.PM.model.adminColumnDef.USERPARAMS;
	if (l > 10)
    var oConfigs = { 
	                paginator: new YAHOO.widget.Paginator({ 
	                    rowsPerPage: 10 
	                }), 
	                initialRequest: "results=" + l 
	        }; 	
	var t = new YAHOO.widget.DataTable("pmAdminParamTableview", i2b2.PM.admin.grdSecondaryColumnDefs, i2b2.PM.admin.dsSecondary, oConfigs);
	i2b2.PM.view.admin.yuiControls.secondaryGrid = t;
	t.isDirty = false;
	t.subscribe("rowMouseoverEvent", t.onEventHighlightCell);
	t.subscribe("rowMouseoutEvent", t.onEventUnhighlightCell);
	t.subscribe("cellClickEvent", i2b2.PM.view.admin.paramgridClickHandler);
	t.subscribe("editorSaveEvent", i2b2.PM.view.admin.editorSaved);
	
};
