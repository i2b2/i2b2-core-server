/**
 * @projectDescription	Messages used by the PM cell communicator object.
 * @inherits 	i2b2.PM.cfg
 * @namespace	i2b2.PM.cfg.msgs
 * @author		Nick Benik, Griffin Weber MD PhD
 * @version 	1.3
 * ----------------------------------------------------------------------------------------
 * updated 9-15-08: RC4 launch [Nick Benik] 
 */

i2b2.PM.model.attemptingLogin = false;
i2b2.PM.cfg.msgs = {};	
i2b2.PM.cfg.parsers = {};
// create the communicator Object
i2b2.PM.ajax = i2b2.hive.communicatorFactory("PM");

// ================================================================================================== //
i2b2.PM.cfg.msgs.getUserAuth = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>\n'+
'<i2b2:request xmlns:i2b2="http://www.i2b2.org/xsd/hive/msg/1.1/" xmlns:pm="http://www.i2b2.org/xsd/cell/pm/1.1/">\n'+
'    <message_header>\n'+
'        {{{proxy_info}}}\n'+
'        <i2b2_version_compatible>1.1</i2b2_version_compatible>\n'+
'        <hl7_version_compatible>2.4</hl7_version_compatible>\n'+
'        <sending_application>\n'+
'            <application_name>i2b2 Project Management</application_name>\n'+
'            <application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'        </sending_application>\n'+
'        <sending_facility>\n'+
'            <facility_name>i2b2 Hive</facility_name>\n'+
'        </sending_facility>\n'+
'        <receiving_application>\n'+
'            <application_name>Project Management Cell</application_name>\n'+
'            <application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'        </receiving_application>\n'+
'        <receiving_facility>\n'+
'            <facility_name>i2b2 Hive</facility_name>\n'+
'        </receiving_facility>\n'+
'        <datetime_of_message>{{{header_msg_datetime}}}</datetime_of_message>\n'+
'		<security>\n'+
'			<domain>{{{sec_domain}}}</domain>\n'+
'			<username>{{{sec_user}}}</username>\n'+
'			<password>{{{password_text}}}</password>\n'+
'		</security>\n'+
'        <message_control_id>\n'+
'            <message_num>{{{header_msg_id}}}</message_num>\n'+
'            <instance_num>0</instance_num>\n'+
'        </message_control_id>\n'+
'        <processing_id>\n'+
'            <processing_id>P</processing_id>\n'+
'            <processing_mode>I</processing_mode>\n'+
'        </processing_id>\n'+
'        <accept_acknowledgement_type>AL</accept_acknowledgement_type>\n'+
'        <application_acknowledgement_type>AL</application_acknowledgement_type>\n'+
'        <country_code>US</country_code>\n'+
'        <project_id>{{{sec_project}}}</project_id>\n'+
'    </message_header>\n'+
'    <request_header>\n'+
'        <result_waittime_ms>{{{result_wait_time}}}000</result_waittime_ms>\n'+
'    </request_header>\n'+
'    <message_body>\n'+
'        <pm:get_user_configuration>\n'+
'            <project>{{{sec_project}}}</project>\n'+
'        </pm:get_user_configuration>\n'+
'    </message_body>\n'+
'</i2b2:request>';
i2b2.PM.ajax._addFunctionCall("getUserAuth","{{{URL}}}getServices", i2b2.PM.cfg.msgs.getUserAuth);


//================================================================================================== //
i2b2.PM.cfg.msgs.setPassword = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>\n'+
'<i2b2:request xmlns:i2b2="http://www.i2b2.org/xsd/hive/msg/1.1/" xmlns:pm="http://www.i2b2.org/xsd/cell/pm/1.1/">\n'+
'    <message_header>\n'+
'        {{{proxy_info}}}\n'+
'        <i2b2_version_compatible>1.1</i2b2_version_compatible>\n'+
'        <hl7_version_compatible>2.4</hl7_version_compatible>\n'+
'        <sending_application>\n'+
'            <application_name>i2b2 Project Management</application_name>\n'+
'            <application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'        </sending_application>\n'+
'        <sending_facility>\n'+
'            <facility_name>i2b2 Hive</facility_name>\n'+
'        </sending_facility>\n'+
'        <receiving_application>\n'+
'            <application_name>Project Management Cell</application_name>\n'+
'            <application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'        </receiving_application>\n'+
'        <receiving_facility>\n'+
'            <facility_name>i2b2 Hive</facility_name>\n'+
'        </receiving_facility>\n'+
'        <datetime_of_message>{{{header_msg_datetime}}}</datetime_of_message>\n'+
'               <security>\n'+
'                       <domain>{{{sec_domain}}}</domain>\n'+
'                       <username>{{{sec_user}}}</username>\n'+
'                       <password>{{{sec_oldpassword}}}</password>\n'+
'               </security>\n'+
'        <message_control_id>\n'+
'            <message_num>{{{header_msg_id}}}</message_num>\n'+
'            <instance_num>0</instance_num>\n'+
'        </message_control_id>\n'+
'        <processing_id>\n'+
'            <processing_id>P</processing_id>\n'+
'            <processing_mode>I</processing_mode>\n'+
'        </processing_id>\n'+
'        <accept_acknowledgement_type>AL</accept_acknowledgement_type>\n'+
'        <application_acknowledgement_type>AL</application_acknowledgement_type>\n'+
'        <country_code>US</country_code>\n'+
'        <project_id>{{{sec_project}}}</project_id>\n'+
'    </message_header>\n'+
'    <request_header>\n'+
'        <result_waittime_ms>{{{result_wait_time}}}000</result_waittime_ms>\n'+
'    </request_header>\n'+
'    <message_body>\n'+
'        <pm:set_password>\n'+
'{{{sec_newpassword}}}'+
'        </pm:set_password>\n'+
'    </message_body>\n'+
'</i2b2:request>';
i2b2.PM.ajax._addFunctionCall("setPassword","{{{URL}}}getServices", i2b2.PM.cfg.msgs.setPassword);

// ================================================================================================== //
i2b2.PM.cfg.msgs.setProjectRequest = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>\n'+
'<i2b2:request xmlns:i2b2="http://www.i2b2.org/xsd/hive/msg/1.1/" xmlns:pm="http://www.i2b2.org/xsd/cell/pm/1.1/">\n'+
'    <message_header>\n'+
'        {{{proxy_info}}}\n'+
'        <i2b2_version_compatible>1.1</i2b2_version_compatible>\n'+
'        <hl7_version_compatible>2.4</hl7_version_compatible>\n'+
'        <sending_application>\n'+
'            <application_name>i2b2 Project Management</application_name>\n'+
'            <application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'        </sending_application>\n'+
'        <sending_facility>\n'+
'            <facility_name>i2b2 Hive</facility_name>\n'+
'        </sending_facility>\n'+
'        <receiving_application>\n'+
'            <application_name>Project Management Cell</application_name>\n'+
'            <application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'        </receiving_application>\n'+
'        <receiving_facility>\n'+
'            <facility_name>i2b2 Hive</facility_name>\n'+
'        </receiving_facility>\n'+
'        <datetime_of_message>{{{header_msg_datetime}}}</datetime_of_message>\n'+
'		<security>\n'+
'			<domain>{{{sec_domain}}}</domain>\n'+
'			<username>{{{sec_user}}}</username>\n'+
'			{{{sec_pass_node}}}\n'+
'		</security>\n'+
'        <message_control_id>\n'+
'            <message_num>{{{header_msg_id}}}</message_num>\n'+
'            <instance_num>0</instance_num>\n'+
'        </message_control_id>\n'+
'        <processing_id>\n'+
'            <processing_id>P</processing_id>\n'+
'            <processing_mode>I</processing_mode>\n'+
'        </processing_id>\n'+
'        <accept_acknowledgement_type>AL</accept_acknowledgement_type>\n'+
'        <application_acknowledgement_type>AL</application_acknowledgement_type>\n'+
'        <country_code>US</country_code>\n'+
'        <project_id>{{{sec_project}}}</project_id>\n'+
'    </message_header>\n'+
'    <request_header>\n'+
'        <result_waittime_ms>{{{result_wait_time}}}000</result_waittime_ms>\n'+
'    </request_header>\n'+
'    <message_body>\n'+
'        <pm:set_project_request>\n' +
'                <title>{{{title}}}</title>\n' +
'                <request_xml>{{{Project_Request}}}</request_xml>\n' +
'        </pm:set_project_request>\n' +
'    </message_body>\n'+
'</i2b2:request>';
i2b2.PM.cfg.parsers.setProjectRequest = function() {
	if (!this.error) {
		this.model = [];		
		// extract records from XML msg
		var c = this.refXML.getElementsByTagName('project');
		var l = c.length;
		for (var i=0; i<l; i++) {
			var tmpRec = {};
			tmpRec.id = i2b2.h.XPath(c[i], "attribute::id")[0].nodeValue;
			tmpRec.name = i2b2.h.getXNodeVal(c[i], "name");
			tmpRec.key = i2b2.h.getXNodeVal(c[i], "key");
			tmpRec.wiki = i2b2.h.getXNodeVal(c[i], "wiki");
			tmpRec.description = i2b2.h.getXNodeVal(c[i], "description");
			tmpRec.path = i2b2.h.getXNodeVal(c[i],"path");
			this.model.push(tmpRec);
		}
	} else {
		this.model = false;
		console.error("[setProjectRequest] Could not parse() data!");
	}
	return this;
};
i2b2.PM.ajax._addFunctionCall("setProjectRequest", "{{{URL}}}getServices", i2b2.PM.cfg.msgs.setProjectRequest, null, i2b2.PM.cfg.parsers.setProjectRequest);




// ================================================================================================== //
i2b2.PM.cfg.msgs.getAllProjectRequest = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>\n'+
'<i2b2:request xmlns:i2b2="http://www.i2b2.org/xsd/hive/msg/1.1/" xmlns:pm="http://www.i2b2.org/xsd/cell/pm/1.1/">\n'+
'    <message_header>\n'+
'        {{{proxy_info}}}\n'+
'        <i2b2_version_compatible>1.1</i2b2_version_compatible>\n'+
'        <hl7_version_compatible>2.4</hl7_version_compatible>\n'+
'        <sending_application>\n'+
'            <application_name>i2b2 Project Management</application_name>\n'+
'            <application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'        </sending_application>\n'+
'        <sending_facility>\n'+
'            <facility_name>i2b2 Hive</facility_name>\n'+
'        </sending_facility>\n'+
'        <receiving_application>\n'+
'            <application_name>Project Management Cell</application_name>\n'+
'            <application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'        </receiving_application>\n'+
'        <receiving_facility>\n'+
'            <facility_name>i2b2 Hive</facility_name>\n'+
'        </receiving_facility>\n'+
'        <datetime_of_message>{{{header_msg_datetime}}}</datetime_of_message>\n'+
'		<security>\n'+
'			<domain>{{{sec_domain}}}</domain>\n'+
'			<username>{{{sec_user}}}</username>\n'+
'			{{{sec_pass_node}}}\n'+
'		</security>\n'+
'        <message_control_id>\n'+
'            <message_num>{{{header_msg_id}}}</message_num>\n'+
'            <instance_num>0</instance_num>\n'+
'        </message_control_id>\n'+
'        <processing_id>\n'+
'            <processing_id>P</processing_id>\n'+
'            <processing_mode>I</processing_mode>\n'+
'        </processing_id>\n'+
'        <accept_acknowledgement_type>AL</accept_acknowledgement_type>\n'+
'        <application_acknowledgement_type>AL</application_acknowledgement_type>\n'+
'        <country_code>US</country_code>\n'+
'        <project_id>{{{sec_project}}}</project_id>\n'+
'    </message_header>\n'+
'    <request_header>\n'+
'        <result_waittime_ms>{{{result_wait_time}}}000</result_waittime_ms>\n'+
'    </request_header>\n'+
'    <message_body>\n'+
'        <pm:get_all_project_request>\n'+
'        </pm:get_all_project_request>\n'+
'    </message_body>\n'+
'</i2b2:request>';
i2b2.PM.cfg.parsers.getAllProjectRequest = function() {
	if (!this.error) {
		this.model = [];		
		// extract records from XML msg
		var c = this.refXML.getElementsByTagName('project_request');
		var l = c.length;
		for (var i=0; i<l; i++) {
			var tmpRec = {};
			tmpRec.id = i2b2.h.XPath(c[i], "attribute::id")[0].nodeValue;
			tmpRec.project_id = i2b2.h.getXNodeVal(c[i], "project_id");
			tmpRec.request_xml = i2b2.h.getXNodeVal(c[i], "request_xml");
			tmpRec.submit_char = i2b2.h.getXNodeVal(c[i], "submit_char");
			tmpRec.entry_date = i2b2.h.getXNodeVal(c[i], "entry_date");
			if (!Object.isUndefined(tmpRec.entry_date))
			{
				tmpRec.entry_date = (tmpRec.entry_date).substring(0,10);
			}
			tmpRec.title = i2b2.h.getXNodeVal(c[i], "title");
			this.model.push(tmpRec);
		}
	} else {
		this.model = false;
		console.error("[getAllProjectRequest] Could not parse() data!");
	}
	return this;
};
i2b2.PM.ajax._addFunctionCall("getAllProjectRequest","{{{URL}}}getServices", i2b2.PM.cfg.msgs.getAllProjectRequest, null, i2b2.PM.cfg.parsers.getAllProjectRequest);


// ================================================================================================== //
i2b2.PM.cfg.msgs.getProjectRequest = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>\n'+
'<i2b2:request xmlns:i2b2="http://www.i2b2.org/xsd/hive/msg/1.1/" xmlns:pm="http://www.i2b2.org/xsd/cell/pm/1.1/">\n'+
'    <message_header>\n'+
'        {{{proxy_info}}}\n'+
'        <i2b2_version_compatible>1.1</i2b2_version_compatible>\n'+
'        <hl7_version_compatible>2.4</hl7_version_compatible>\n'+
'        <sending_application>\n'+
'            <application_name>i2b2 Project Management</application_name>\n'+
'            <application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'        </sending_application>\n'+
'        <sending_facility>\n'+
'            <facility_name>i2b2 Hive</facility_name>\n'+
'        </sending_facility>\n'+
'        <receiving_application>\n'+
'            <application_name>Project Management Cell</application_name>\n'+
'            <application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'        </receiving_application>\n'+
'        <receiving_facility>\n'+
'            <facility_name>i2b2 Hive</facility_name>\n'+
'        </receiving_facility>\n'+
'        <datetime_of_message>{{{header_msg_datetime}}}</datetime_of_message>\n'+
'		<security>\n'+
'			<domain>{{{sec_domain}}}</domain>\n'+
'			<username>{{{sec_user}}}</username>\n'+
'			{{{sec_pass_node}}}\n'+
'		</security>\n'+
'        <message_control_id>\n'+
'            <message_num>{{{header_msg_id}}}</message_num>\n'+
'            <instance_num>0</instance_num>\n'+
'        </message_control_id>\n'+
'        <processing_id>\n'+
'            <processing_id>P</processing_id>\n'+
'            <processing_mode>I</processing_mode>\n'+
'        </processing_id>\n'+
'        <accept_acknowledgement_type>AL</accept_acknowledgement_type>\n'+
'        <application_acknowledgement_type>AL</application_acknowledgement_type>\n'+
'        <country_code>US</country_code>\n'+
'        <project_id>{{{sec_project}}}</project_id>\n'+
'    </message_header>\n'+
'    <request_header>\n'+
'        <result_waittime_ms>{{{result_wait_time}}}000</result_waittime_ms>\n'+
'    </request_header>\n'+
'    <message_body>\n'+
'        <pm:get_project_request id="{{{id}}}">\n'+
'        </pm:get_project_request>\n'+
'    </message_body>\n'+
'</i2b2:request>';
i2b2.PM.cfg.parsers.getProjectRequest = function() {
	if (!this.error) {
		this.model = [];		
		// extract records from XML msg
		var c = this.refXML.getElementsByTagName('project_request');
		var l = c.length;
		for (var i=0; i<l; i++) {
			var tmpRec = {};
			tmpRec.id = i2b2.h.XPath(c[i], "attribute::id")[0].nodeValue;
			tmpRec.project_id = i2b2.h.getXNodeVal(c[i], "project_id");
			tmpRec.request_xml = i2b2.h.getXNodeVal(c[i], "request_xml");
			tmpRec.submit_char = i2b2.h.getXNodeVal(c[i], "submit_char");
			tmpRec.entry_date = i2b2.h.getXNodeVal(c[i], "entry_date");
			if (!Object.isUndefined(tmpRec.entry_date))
			{
				tmpRec.entry_date = (tmpRec.entry_date).substring(0,10);
			}
			tmpRec.title = i2b2.h.getXNodeVal(c[i], "title");
			this.model.push(tmpRec);
		}
	} else {
		this.model = false;
		console.error("[getAllProjectRequest] Could not parse() data!");
	}
	return this;
};
i2b2.PM.ajax._addFunctionCall("getProjectRequest","{{{URL}}}getServices", i2b2.PM.cfg.msgs.getProjectRequest, null, i2b2.PM.cfg.parsers.getProjectRequest);





// ================================================================================================== //
i2b2.PM.cfg.msgs.getApproval = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>\n'+
'<i2b2:request xmlns:i2b2="http://www.i2b2.org/xsd/hive/msg/1.1/" xmlns:pm="http://www.i2b2.org/xsd/cell/pm/1.1/">\n'+
'    <message_header>\n'+
'        {{{proxy_info}}}\n'+
'        <i2b2_version_compatible>1.1</i2b2_version_compatible>\n'+
'        <hl7_version_compatible>2.4</hl7_version_compatible>\n'+
'        <sending_application>\n'+
'            <application_name>i2b2 Project Management</application_name>\n'+
'            <application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'        </sending_application>\n'+
'        <sending_facility>\n'+
'            <facility_name>i2b2 Hive</facility_name>\n'+
'        </sending_facility>\n'+
'        <receiving_application>\n'+
'            <application_name>Project Management Cell</application_name>\n'+
'            <application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'        </receiving_application>\n'+
'        <receiving_facility>\n'+
'            <facility_name>i2b2 Hive</facility_name>\n'+
'        </receiving_facility>\n'+
'        <datetime_of_message>{{{header_msg_datetime}}}</datetime_of_message>\n'+
'		<security>\n'+
'			<domain>{{{sec_domain}}}</domain>\n'+
'			<username>{{{sec_user}}}</username>\n'+
'			{{{sec_pass_node}}}\n'+
'		</security>\n'+
'        <message_control_id>\n'+
'            <message_num>{{{header_msg_id}}}</message_num>\n'+
'            <instance_num>0</instance_num>\n'+
'        </message_control_id>\n'+
'        <processing_id>\n'+
'            <processing_id>P</processing_id>\n'+
'            <processing_mode>I</processing_mode>\n'+
'        </processing_id>\n'+
'        <accept_acknowledgement_type>AL</accept_acknowledgement_type>\n'+
'        <application_acknowledgement_type>AL</application_acknowledgement_type>\n'+
'        <country_code>US</country_code>\n'+
'        <project_id>{{{sec_project}}}</project_id>\n'+
'    </message_header>\n'+
'    <request_header>\n'+
'        <result_waittime_ms>{{{result_wait_time}}}000</result_waittime_ms>\n'+
'    </request_header>\n'+
'    <message_body>\n'+ 
'        <pm:get_approval {{{id}}}>\n'+
//'            <search by="user">{{{sec_user}}}</search>\n'+
'        </pm:get_approval>\n'+
'    </message_body>\n'+
'</i2b2:request>';
i2b2.PM.cfg.parsers.getApproval = function() {
	if (!this.error) {
		this.model = [];		
		// extract records from XML msg
		var c = this.refXML.getElementsByTagName('approval');
		var l = c.length;
		for (var i=0; i<l; i++) {
			var tmpRec = {};
			tmpRec.id = i2b2.h.XPath(c[i], "attribute::id")[0].nodeValue;
			tmpRec.name = i2b2.h.getXNodeVal(c[i], "name");
			tmpRec.description = i2b2.h.getXNodeVal(c[i], "description");
			tmpRec.activation_date = i2b2.h.getXNodeVal(c[i], "activation_date");
			if (!Object.isUndefined(tmpRec.activation_date))
			{
				tmpRec.activation_date = (tmpRec.activation_date).substring(0,10);
			}
			tmpRec.expiration_date = i2b2.h.getXNodeVal(c[i], "expiration_date");
			if (!Object.isUndefined(tmpRec.expiration_date))
			{
				tmpRec.expiration_date = (tmpRec.expiration_date).substring(0,10);
			}
			
			this.model.push(tmpRec);
		}
	} else {
		this.model = false;
		console.error("[getApproval] Could not parse() data!");
	}
	return this;
};
i2b2.PM.ajax._addFunctionCall("getApproval","{{{URL}}}getServices", i2b2.PM.cfg.msgs.getApproval, null, i2b2.PM.cfg.parsers.getApproval);




// ================================================================================================== //
i2b2.PM.cfg.msgs.getAllHive = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>\n'+
'<i2b2:request xmlns:i2b2="http://www.i2b2.org/xsd/hive/msg/1.1/" xmlns:pm="http://www.i2b2.org/xsd/cell/pm/1.1/">\n'+
'    <message_header>\n'+
'        {{{proxy_info}}}\n'+
'        <i2b2_version_compatible>1.1</i2b2_version_compatible>\n'+
'        <hl7_version_compatible>2.4</hl7_version_compatible>\n'+
'        <sending_application>\n'+
'            <application_name>i2b2 Project Management</application_name>\n'+
'            <application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'        </sending_application>\n'+
'        <sending_facility>\n'+
'            <facility_name>i2b2 Hive</facility_name>\n'+
'        </sending_facility>\n'+
'        <receiving_application>\n'+
'            <application_name>Project Management Cell</application_name>\n'+
'            <application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'        </receiving_application>\n'+
'        <receiving_facility>\n'+
'            <facility_name>i2b2 Hive</facility_name>\n'+
'        </receiving_facility>\n'+
'        <datetime_of_message>{{{header_msg_datetime}}}</datetime_of_message>\n'+
'		<security>\n'+
'			<domain>{{{sec_domain}}}</domain>\n'+
'			<username>{{{sec_user}}}</username>\n'+
'			{{{sec_pass_node}}}\n'+
'		</security>\n'+
'        <message_control_id>\n'+
'            <message_num>{{{header_msg_id}}}</message_num>\n'+
'            <instance_num>0</instance_num>\n'+
'        </message_control_id>\n'+
'        <processing_id>\n'+
'            <processing_id>P</processing_id>\n'+
'            <processing_mode>I</processing_mode>\n'+
'        </processing_id>\n'+
'        <accept_acknowledgement_type>AL</accept_acknowledgement_type>\n'+
'        <application_acknowledgement_type>AL</application_acknowledgement_type>\n'+
'        <country_code>US</country_code>\n'+
'        <project_id>{{{sec_project}}}</project_id>\n'+
'    </message_header>\n'+
'    <request_header>\n'+
'        <result_waittime_ms>{{{result_wait_time}}}000</result_waittime_ms>\n'+
'    </request_header>\n'+
'    <message_body>\n'+
'        <pm:get_all_hive>\n'+
//'            <project>{{{sec_project}}}</project>\n'+
'        </pm:get_all_hive>\n'+
'    </message_body>\n'+
'</i2b2:request>';
i2b2.PM.cfg.parsers.getHives = function() {
	if (!this.error) {
		this.model = [];		
		// extract records from XML msg
		var c = this.refXML.getElementsByTagName('hive');
		var l = c.length;
		for (var i=0; i<l; i++) {
			var tmpRec = {};
			tmpRec.environment = i2b2.h.getXNodeVal(c[i], "environment");
			tmpRec.helpURL = i2b2.h.getXNodeVal(c[i], "helpURL");
			tmpRec.domain_name = i2b2.h.getXNodeVal(c[i], "domain_name");
			tmpRec.domain_id = i2b2.h.getXNodeVal(c[i], "domain_id");
			tmpRec.active = i2b2.h.getXNodeVal(c[i], "active");
			this.model.push(tmpRec);
		}
	} else {
		this.model = false;
		console.error("[GetHives] Could not parse() data!");
	}
	return this;
};
i2b2.PM.ajax._addFunctionCall("getAllHive", "{{{URL}}}getServices", i2b2.PM.cfg.msgs.getAllHive, null, i2b2.PM.cfg.parsers.getHives);



// ================================================================================================== //
i2b2.PM.cfg.msgs.getAllParam = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>\n'+
'<i2b2:request xmlns:i2b2="http://www.i2b2.org/xsd/hive/msg/1.1/" xmlns:pm="http://www.i2b2.org/xsd/cell/pm/1.1/">\n'+
'    <message_header>\n'+
'        {{{proxy_info}}}\n'+
'        <i2b2_version_compatible>1.1</i2b2_version_compatible>\n'+
'        <hl7_version_compatible>2.4</hl7_version_compatible>\n'+
'        <sending_application>\n'+
'            <application_name>i2b2 Project Management</application_name>\n'+
'            <application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'        </sending_application>\n'+
'        <sending_facility>\n'+
'            <facility_name>i2b2 Hive</facility_name>\n'+
'        </sending_facility>\n'+
'        <receiving_application>\n'+
'            <application_name>Project Management Cell</application_name>\n'+
'            <application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'        </receiving_application>\n'+
'        <receiving_facility>\n'+
'            <facility_name>i2b2 Hive</facility_name>\n'+
'        </receiving_facility>\n'+
'        <datetime_of_message>{{{header_msg_datetime}}}</datetime_of_message>\n'+
'		<security>\n'+
'			<domain>{{{sec_domain}}}</domain>\n'+
'			<username>{{{sec_user}}}</username>\n'+
'			{{{sec_pass_node}}}\n'+
'		</security>\n'+
'        <message_control_id>\n'+
'            <message_num>{{{header_msg_id}}}</message_num>\n'+
'            <instance_num>0</instance_num>\n'+
'        </message_control_id>\n'+
'        <processing_id>\n'+
'            <processing_id>P</processing_id>\n'+
'            <processing_mode>I</processing_mode>\n'+
'        </processing_id>\n'+
'        <accept_acknowledgement_type>AL</accept_acknowledgement_type>\n'+
'        <application_acknowledgement_type>AL</application_acknowledgement_type>\n'+
'        <country_code>US</country_code>\n'+
'        <project_id>{{{sec_project}}}</project_id>\n'+
'    </message_header>\n'+
'    <request_header>\n'+
'        <result_waittime_ms>{{{result_wait_time}}}000</result_waittime_ms>\n'+
'    </request_header>\n'+
'    <message_body>\n'+
'        <pm:get_all_{{{table}}} {{{param_xml}}}>{{{id_xml}}}</pm:get_all_{{{table}}}>\n'+
'    </message_body>\n'+
'</i2b2:request>';
i2b2.PM.cfg.parsers.getAllParam = function(username) {
	if (!this.error) {
		this.model = [];		
		// extract records from XML msg (filter by username if given)
		if (Object.isUndefined(username)) {
			var c = i2b2.h.XPath(this.refXML,  "//message_body/descendant::param[@name]");
		} else {
			var c = i2b2.h.XPath(this.refXML, "//user[user_name/text()='"+username+"']/param");
		}
		var l = c.length;
		for (var i=0; i<l; i++) {
			try {
				var tmpRec = {};
				tmpRec.name = i2b2.h.XPath(c[i], "attribute::name")[0].nodeValue;
				tmpRec.id = i2b2.h.XPath(c[i], "attribute::id")[0].nodeValue;
				tmpRec.datatype = i2b2.h.XPath(c[i], "attribute::datatype")[0].nodeValue;
				tmpRec.value = i2b2.h.XPath(c[i], "text()")[0].nodeValue;
				this.model.push(tmpRec);
			} catch(e) {}
		}
	} else {
		this.model = false;
		console.error("[GetHives] Could not parse() data!");
	}
	return this;
};
i2b2.PM.ajax._addFunctionCall("getAllParam", "{{{URL}}}getServices", i2b2.PM.cfg.msgs.getAllParam, ["id_xml"], i2b2.PM.cfg.parsers.getAllParam);



// ================================================================================================== //
i2b2.PM.cfg.msgs.getAllCell = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>\n'+
'<i2b2:request xmlns:i2b2="http://www.i2b2.org/xsd/hive/msg/1.1/" xmlns:pm="http://www.i2b2.org/xsd/cell/pm/1.1/">\n'+
'    <message_header>\n'+
'        {{{proxy_info}}}\n'+
'        <i2b2_version_compatible>1.1</i2b2_version_compatible>\n'+
'        <hl7_version_compatible>2.4</hl7_version_compatible>\n'+
'        <sending_application>\n'+
'            <application_name>i2b2 Project Management</application_name>\n'+
'            <application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'        </sending_application>\n'+
'        <sending_facility>\n'+
'            <facility_name>i2b2 Hive</facility_name>\n'+
'        </sending_facility>\n'+
'        <receiving_application>\n'+
'            <application_name>Project Management Cell</application_name>\n'+
'            <application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'        </receiving_application>\n'+
'        <receiving_facility>\n'+
'            <facility_name>i2b2 Hive</facility_name>\n'+
'        </receiving_facility>\n'+
'        <datetime_of_message>{{{header_msg_datetime}}}</datetime_of_message>\n'+
'		<security>\n'+
'			<domain>{{{sec_domain}}}</domain>\n'+
'			<username>{{{sec_user}}}</username>\n'+
'			{{{sec_pass_node}}}\n'+
'		</security>\n'+
'        <message_control_id>\n'+
'            <message_num>{{{header_msg_id}}}</message_num>\n'+
'            <instance_num>0</instance_num>\n'+
'        </message_control_id>\n'+
'        <processing_id>\n'+
'            <processing_id>P</processing_id>\n'+
'            <processing_mode>I</processing_mode>\n'+
'        </processing_id>\n'+
'        <accept_acknowledgement_type>AL</accept_acknowledgement_type>\n'+
'        <application_acknowledgement_type>AL</application_acknowledgement_type>\n'+
'        <country_code>US</country_code>\n'+
'        <project_id>{{{sec_project}}}</project_id>\n'+
'    </message_header>\n'+
'    <request_header>\n'+
'        <result_waittime_ms>{{{result_wait_time}}}000</result_waittime_ms>\n'+
'    </request_header>\n'+
'    <message_body>\n'+
'        <pm:get_all_cell>\n'+
'            <project>{{{sec_project}}}</project>\n'+
'        </pm:get_all_cell>\n'+
'    </message_body>\n'+
'</i2b2:request>';
i2b2.PM.cfg.parsers.getAllCell = function() {
	if (!this.error) {
		this.model = [];		
		// extract records from XML msg
		var c = this.refXML.getElementsByTagName('cell_data');
		var l = c.length;
		for (var i=0; i<l; i++) {
			var tmpRec = {};
			tmpRec.id = i2b2.h.XPath(c[i], "attribute::id")[0].nodeValue;
			tmpRec.name = i2b2.h.getXNodeVal(c[i], "name");
			tmpRec.url = i2b2.h.getXNodeVal(c[i], "url");
			tmpRec.project_path = i2b2.h.getXNodeVal(c[i], "project_path");
			tmpRec.method = i2b2.h.getXNodeVal(c[i], "method");
			tmpRec.can_override = i2b2.h.getXNodeVal(c[i], "can_override");
			this.model.push(tmpRec);
		}
	} else {
		this.model = false;
		console.error("[getAllCell] Could not parse() data!");
	}
	return this;
};
i2b2.PM.ajax._addFunctionCall("getAllCell","{{{URL}}}getServices", i2b2.PM.cfg.msgs.getAllCell, null, i2b2.PM.cfg.parsers.getAllCell);



// ================================================================================================== //
i2b2.PM.cfg.msgs.getAllProject = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>\n'+
'<i2b2:request xmlns:i2b2="http://www.i2b2.org/xsd/hive/msg/1.1/" xmlns:pm="http://www.i2b2.org/xsd/cell/pm/1.1/">\n'+
'    <message_header>\n'+
'        {{{proxy_info}}}\n'+
'        <i2b2_version_compatible>1.1</i2b2_version_compatible>\n'+
'        <hl7_version_compatible>2.4</hl7_version_compatible>\n'+
'        <sending_application>\n'+
'            <application_name>i2b2 Project Management</application_name>\n'+
'            <application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'        </sending_application>\n'+
'        <sending_facility>\n'+
'            <facility_name>i2b2 Hive</facility_name>\n'+
'        </sending_facility>\n'+
'        <receiving_application>\n'+
'            <application_name>Project Management Cell</application_name>\n'+
'            <application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'        </receiving_application>\n'+
'        <receiving_facility>\n'+
'            <facility_name>i2b2 Hive</facility_name>\n'+
'        </receiving_facility>\n'+
'        <datetime_of_message>{{{header_msg_datetime}}}</datetime_of_message>\n'+
'		<security>\n'+
'			<domain>{{{sec_domain}}}</domain>\n'+
'			<username>{{{sec_user}}}</username>\n'+
'			{{{sec_pass_node}}}\n'+
'		</security>\n'+
'        <message_control_id>\n'+
'            <message_num>{{{header_msg_id}}}</message_num>\n'+
'            <instance_num>0</instance_num>\n'+
'        </message_control_id>\n'+
'        <processing_id>\n'+
'            <processing_id>P</processing_id>\n'+
'            <processing_mode>I</processing_mode>\n'+
'        </processing_id>\n'+
'        <accept_acknowledgement_type>AL</accept_acknowledgement_type>\n'+
'        <application_acknowledgement_type>AL</application_acknowledgement_type>\n'+
'        <country_code>US</country_code>\n'+
'        <project_id>{{{sec_project}}}</project_id>\n'+
'    </message_header>\n'+
'    <request_header>\n'+
'        <result_waittime_ms>{{{result_wait_time}}}000</result_waittime_ms>\n'+
'    </request_header>\n'+
'    <message_body>\n'+
'        <pm:get_all_project>\n'+
'        </pm:get_all_project>\n'+
'    </message_body>\n'+
'</i2b2:request>';
i2b2.PM.cfg.parsers.getAllProject = function() {
	if (!this.error) {
		this.model = [];		
		// extract records from XML msg
		var c = this.refXML.getElementsByTagName('project');
		var l = c.length;
		for (var i=0; i<l; i++) {
			var tmpRec = {};
			tmpRec.id = i2b2.h.XPath(c[i], "attribute::id")[0].nodeValue;
			tmpRec.name = i2b2.h.getXNodeVal(c[i], "name");
			tmpRec.key = i2b2.h.getXNodeVal(c[i], "key");
			tmpRec.wiki = i2b2.h.getXNodeVal(c[i], "wiki");
			tmpRec.description = i2b2.h.getXNodeVal(c[i], "description");
			tmpRec.path = i2b2.h.getXNodeVal(c[i],"path");
			this.model.push(tmpRec);
		}
	} else {
		this.model = false;
		console.error("[getAllProject] Could not parse() data!");
	}
	return this;
};
i2b2.PM.ajax._addFunctionCall("getAllProject", "{{{URL}}}getServices", i2b2.PM.cfg.msgs.getAllProject, null, i2b2.PM.cfg.parsers.getAllProject);




// ================================================================================================== //
i2b2.PM.cfg.msgs.getAllRole = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>\n'+
'<i2b2:request xmlns:i2b2="http://www.i2b2.org/xsd/hive/msg/1.1/" xmlns:pm="http://www.i2b2.org/xsd/cell/pm/1.1/">\n'+
'    <message_header>\n'+
'        {{{proxy_info}}}\n'+
'        <i2b2_version_compatible>1.1</i2b2_version_compatible>\n'+
'        <hl7_version_compatible>2.4</hl7_version_compatible>\n'+
'        <sending_application>\n'+
'            <application_name>i2b2 Project Management</application_name>\n'+
'            <application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'        </sending_application>\n'+
'        <sending_facility>\n'+
'            <facility_name>i2b2 Hive</facility_name>\n'+
'        </sending_facility>\n'+
'        <receiving_application>\n'+
'            <application_name>Project Management Cell</application_name>\n'+
'            <application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'        </receiving_application>\n'+
'        <receiving_facility>\n'+
'            <facility_name>i2b2 Hive</facility_name>\n'+
'        </receiving_facility>\n'+
'        <datetime_of_message>{{{header_msg_datetime}}}</datetime_of_message>\n'+
'		<security>\n'+
'			<domain>{{{sec_domain}}}</domain>\n'+
'			<username>{{{sec_user}}}</username>\n'+
'			{{{sec_pass_node}}}\n'+
'		</security>\n'+
'        <message_control_id>\n'+
'            <message_num>{{{header_msg_id}}}</message_num>\n'+
'            <instance_num>0</instance_num>\n'+
'        </message_control_id>\n'+
'        <processing_id>\n'+
'            <processing_id>P</processing_id>\n'+
'            <processing_mode>I</processing_mode>\n'+
'        </processing_id>\n'+
'        <accept_acknowledgement_type>AL</accept_acknowledgement_type>\n'+
'        <application_acknowledgement_type>AL</application_acknowledgement_type>\n'+
'        <country_code>US</country_code>\n'+
'        <project_id>{{{sec_project}}}</project_id>\n'+
'    </message_header>\n'+
'    <request_header>\n'+
'        <result_waittime_ms>{{{result_wait_time}}}000</result_waittime_ms>\n'+
'    </request_header>\n'+
'    <message_body>\n'+
'        <pm:get_all_role>\n'+
'            <project_id>{{{id}}}</project_id>\n'+
'        </pm:get_all_role>\n'+
'    </message_body>\n'+
'</i2b2:request>';
i2b2.PM.cfg.parsers.getAllRole = function() {
	if (!this.error) {
		this.model = [];		
		// extract records from XML msg
		var c = this.refXML.getElementsByTagName('role');
		var l = c.length;
		for (var i=0; i<l; i++) {
			var tmpRec = {};
			//tmpRec.id = i2b2.h.XPath(c[i], "attribute::id")[0].nodeValue;
			tmpRec.projectid = i2b2.h.getXNodeVal(c[i], "project_id");
			tmpRec.username = i2b2.h.getXNodeVal(c[i], "user_name");
			tmpRec.role = i2b2.h.getXNodeVal(c[i], "role");
			this.model.push(tmpRec);
		}
	} else {
		this.model = false;
		console.error("[getAllRole] Could not parse() data!");
	}
	return this;
};
i2b2.PM.ajax._addFunctionCall("getAllRole","{{{URL}}}getServices", i2b2.PM.cfg.msgs.getAllRole,null,i2b2.PM.cfg.parsers.getAllRole)	;


// ================================================================================================== //
i2b2.PM.cfg.msgs.getAllRoleUser = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>\n'+
'<i2b2:request xmlns:i2b2="http://www.i2b2.org/xsd/hive/msg/1.1/" xmlns:pm="http://www.i2b2.org/xsd/cell/pm/1.1/">\n'+
'    <message_header>\n'+
'        {{{proxy_info}}}\n'+
'        <i2b2_version_compatible>1.1</i2b2_version_compatible>\n'+
'        <hl7_version_compatible>2.4</hl7_version_compatible>\n'+
'        <sending_application>\n'+
'            <application_name>i2b2 Project Management</application_name>\n'+
'            <application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'        </sending_application>\n'+
'        <sending_facility>\n'+
'            <facility_name>i2b2 Hive</facility_name>\n'+
'        </sending_facility>\n'+
'        <receiving_application>\n'+
'            <application_name>Project Management Cell</application_name>\n'+
'            <application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'        </receiving_application>\n'+
'        <receiving_facility>\n'+
'            <facility_name>i2b2 Hive</facility_name>\n'+
'        </receiving_facility>\n'+
'        <datetime_of_message>{{{header_msg_datetime}}}</datetime_of_message>\n'+
'		<security>\n'+
'			<domain>{{{sec_domain}}}</domain>\n'+
'			<username>{{{sec_user}}}</username>\n'+
'			{{{sec_pass_node}}}\n'+
'		</security>\n'+
'        <message_control_id>\n'+
'            <message_num>{{{header_msg_id}}}</message_num>\n'+
'            <instance_num>0</instance_num>\n'+
'        </message_control_id>\n'+
'        <processing_id>\n'+
'            <processing_id>P</processing_id>\n'+
'            <processing_mode>I</processing_mode>\n'+
'        </processing_id>\n'+
'        <accept_acknowledgement_type>AL</accept_acknowledgement_type>\n'+
'        <application_acknowledgement_type>AL</application_acknowledgement_type>\n'+
'        <country_code>US</country_code>\n'+
'        <project_id>{{{sec_project}}}</project_id>\n'+
'    </message_header>\n'+
'    <request_header>\n'+
'        <result_waittime_ms>{{{result_wait_time}}}000</result_waittime_ms>\n'+
'    </request_header>\n'+
'    <message_body>\n'+
'        <pm:get_all_role>\n'+
'            <user_name>{{{username}}}</user_name>\n'+
'            <project_id>{{{id}}}</project_id>\n'+
'        </pm:get_all_role>\n'+
'    </message_body>\n'+
'</i2b2:request>';
i2b2.PM.cfg.parsers.getAllRoleUser = function() {
	if (!this.error) {
		this.model = [];		
		// extract records from XML msg
		var c = this.refXML.getElementsByTagName('role');
		var l = c.length;
		for (var i=0; i<l; i++) {
			var tmpRec = {};
			//tmpRec.id = i2b2.h.XPath(c[i], "attribute::id")[0].nodeValue;
			tmpRec.projectid = i2b2.h.getXNodeVal(c[i], "project_id");
			tmpRec.username = i2b2.h.getXNodeVal(c[i], "user_name");
			tmpRec.role = i2b2.h.getXNodeVal(c[i], "role");
			this.model.push(tmpRec);
		}
	} else {
		this.model = false;
		console.error("[getAllRole] Could not parse() data!");
	}
	return this;
};
i2b2.PM.ajax._addFunctionCall("getAllRoleUser","{{{URL}}}getServices", i2b2.PM.cfg.msgs.getAllRoleUser,null,i2b2.PM.cfg.parsers.getAllRoleUser)	;



// ================================================================================================== //
i2b2.PM.cfg.msgs.getAllUser = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>\n'+
'<i2b2:request xmlns:i2b2="http://www.i2b2.org/xsd/hive/msg/1.1/" xmlns:pm="http://www.i2b2.org/xsd/cell/pm/1.1/">\n'+
'    <message_header>\n'+
'        {{{proxy_info}}}\n'+
'        <i2b2_version_compatible>1.1</i2b2_version_compatible>\n'+
'        <hl7_version_compatible>2.4</hl7_version_compatible>\n'+
'        <sending_application>\n'+
'            <application_name>i2b2 Project Management</application_name>\n'+
'            <application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'        </sending_application>\n'+
'        <sending_facility>\n'+
'            <facility_name>i2b2 Hive</facility_name>\n'+
'        </sending_facility>\n'+
'        <receiving_application>\n'+
'            <application_name>Project Management Cell</application_name>\n'+
'            <application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'        </receiving_application>\n'+
'        <receiving_facility>\n'+
'            <facility_name>i2b2 Hive</facility_name>\n'+
'        </receiving_facility>\n'+
'        <datetime_of_message>{{{header_msg_datetime}}}</datetime_of_message>\n'+
'		<security>\n'+
'			<domain>{{{sec_domain}}}</domain>\n'+
'			<username>{{{sec_user}}}</username>\n'+
'			{{{sec_pass_node}}}\n'+
'		</security>\n'+
'        <message_control_id>\n'+
'            <message_num>{{{header_msg_id}}}</message_num>\n'+
'            <instance_num>0</instance_num>\n'+
'        </message_control_id>\n'+
'        <processing_id>\n'+
'            <processing_id>P</processing_id>\n'+
'            <processing_mode>I</processing_mode>\n'+
'        </processing_id>\n'+
'        <accept_acknowledgement_type>AL</accept_acknowledgement_type>\n'+
'        <application_acknowledgement_type>AL</application_acknowledgement_type>\n'+
'        <country_code>US</country_code>\n'+
'        <project_id>{{{sec_project}}}</project_id>\n'+
'    </message_header>\n'+
'    <request_header>\n'+
'        <result_waittime_ms>{{{result_wait_time}}}000</result_waittime_ms>\n'+
'    </request_header>\n'+
'    <message_body>\n'+
'        <pm:get_all_user>\n'+
'        </pm:get_all_user>\n'+
'    </message_body>\n'+
'</i2b2:request>';
i2b2.PM.cfg.parsers.getAllUser = function() {
	if (!this.error) {
		this.model = [];		
		// extract records from XML msg
		var c = this.refXML.getElementsByTagName('user');
		var l = c.length;
		for (var i=0; i<l; i++) {
			var tmpRec = {};
			tmpRec.full_name = i2b2.h.getXNodeVal(c[i], "full_name");
			tmpRec.user_name = i2b2.h.getXNodeVal(c[i], "user_name");
			tmpRec.email = i2b2.h.getXNodeVal(c[i], "email");
			tmpRec.password = i2b2.h.getXNodeVal(c[i], "password");
			tmpRec.is_admin = i2b2.h.getXNodeVal(c[i], "is_admin");
			this.model.push(tmpRec);
		}
	} else {
		this.model = false;
		console.error("[getAllUser] Could not parse() data!");
	}
	return this;
};
i2b2.PM.ajax._addFunctionCall("getAllUser","{{{URL}}}getServices", i2b2.PM.cfg.msgs.getAllUser, null, i2b2.PM.cfg.parsers.getAllUser);



// ================================================================================================== //
i2b2.PM.cfg.msgs.getAllApproval = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>\n'+
'<i2b2:request xmlns:i2b2="http://www.i2b2.org/xsd/hive/msg/1.1/" xmlns:pm="http://www.i2b2.org/xsd/cell/pm/1.1/">\n'+
'    <message_header>\n'+
'        {{{proxy_info}}}\n'+
'        <i2b2_version_compatible>1.1</i2b2_version_compatible>\n'+
'        <hl7_version_compatible>2.4</hl7_version_compatible>\n'+
'        <sending_application>\n'+
'            <application_name>i2b2 Project Management</application_name>\n'+
'            <application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'        </sending_application>\n'+
'        <sending_facility>\n'+
'            <facility_name>i2b2 Hive</facility_name>\n'+
'        </sending_facility>\n'+
'        <receiving_application>\n'+
'            <application_name>Project Management Cell</application_name>\n'+
'            <application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'        </receiving_application>\n'+
'        <receiving_facility>\n'+
'            <facility_name>i2b2 Hive</facility_name>\n'+
'        </receiving_facility>\n'+
'        <datetime_of_message>{{{header_msg_datetime}}}</datetime_of_message>\n'+
'		<security>\n'+
'			<domain>{{{sec_domain}}}</domain>\n'+
'			<username>{{{sec_user}}}</username>\n'+
'			{{{sec_pass_node}}}\n'+
'		</security>\n'+
'        <message_control_id>\n'+
'            <message_num>{{{header_msg_id}}}</message_num>\n'+
'            <instance_num>0</instance_num>\n'+
'        </message_control_id>\n'+
'        <processing_id>\n'+
'            <processing_id>P</processing_id>\n'+
'            <processing_mode>I</processing_mode>\n'+
'        </processing_id>\n'+
'        <accept_acknowledgement_type>AL</accept_acknowledgement_type>\n'+
'        <application_acknowledgement_type>AL</application_acknowledgement_type>\n'+
'        <country_code>US</country_code>\n'+
'        <project_id>{{{sec_project}}}</project_id>\n'+
'    </message_header>\n'+
'    <request_header>\n'+
'        <result_waittime_ms>{{{result_wait_time}}}000</result_waittime_ms>\n'+
'    </request_header>\n'+
'    <message_body>\n'+
'        <pm:get_all_approval>\n'+
'        </pm:get_all_approval>\n'+
'    </message_body>\n'+
'</i2b2:request>';
i2b2.PM.cfg.parsers.getAllApproval = function() {
	if (!this.error) {
		this.model = [];		
		// extract records from XML msg
		var c = this.refXML.getElementsByTagName('approval');
		var l = c.length;
		for (var i=0; i<l; i++) {
			var tmpRec = {};
			tmpRec.id = i2b2.h.XPath(c[i], "attribute::id")[0].nodeValue;
			tmpRec.name = i2b2.h.getXNodeVal(c[i], "name");
			tmpRec.description = i2b2.h.getXNodeVal(c[i], "description");
			tmpRec.activation_date = i2b2.h.getXNodeVal(c[i], "activation_date");
			if (!Object.isUndefined(tmpRec.activation_date))
			{
				tmpRec.activation_date = (tmpRec.activation_date).substring(0,10);
			}
			tmpRec.expiration_date = i2b2.h.getXNodeVal(c[i], "expiration_date");
			if (!Object.isUndefined(tmpRec.expiration_date))
			{
				tmpRec.expiration_date = (tmpRec.expiration_date).substring(0,10);
			}
			this.model.push(tmpRec);
		}
	} else {
		this.model = false;
		console.error("[getAllApproval] Could not parse() data!");
	}
	return this;
};
i2b2.PM.ajax._addFunctionCall("getAllApproval","{{{URL}}}getServices", i2b2.PM.cfg.msgs.getAllApproval, null, i2b2.PM.cfg.parsers.getAllApproval);




// ================================================================================================== //
i2b2.PM.cfg.msgs.getAllGlobal = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>\n'+
'<i2b2:request xmlns:i2b2="http://www.i2b2.org/xsd/hive/msg/1.1/" xmlns:pm="http://www.i2b2.org/xsd/cell/pm/1.1/">\n'+
'    <message_header>\n'+
'        {{{proxy_info}}}\n'+
'        <i2b2_version_compatible>1.1</i2b2_version_compatible>\n'+
'        <hl7_version_compatible>2.4</hl7_version_compatible>\n'+
'        <sending_application>\n'+
'            <application_name>i2b2 Project Management</application_name>\n'+
'            <application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'        </sending_application>\n'+
'        <sending_facility>\n'+
'            <facility_name>i2b2 Hive</facility_name>\n'+
'        </sending_facility>\n'+
'        <receiving_application>\n'+
'            <application_name>Project Management Cell</application_name>\n'+
'            <application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'        </receiving_application>\n'+
'        <receiving_facility>\n'+
'            <facility_name>i2b2 Hive</facility_name>\n'+
'        </receiving_facility>\n'+
'        <datetime_of_message>{{{header_msg_datetime}}}</datetime_of_message>\n'+
'		<security>\n'+
'			<domain>{{{sec_domain}}}</domain>\n'+
'			<username>{{{sec_user}}}</username>\n'+
'			{{{sec_pass_node}}}\n'+
'		</security>\n'+
'        <message_control_id>\n'+
'            <message_num>{{{header_msg_id}}}</message_num>\n'+
'            <instance_num>0</instance_num>\n'+
'        </message_control_id>\n'+
'        <processing_id>\n'+
'            <processing_id>P</processing_id>\n'+
'            <processing_mode>I</processing_mode>\n'+
'        </processing_id>\n'+
'        <accept_acknowledgement_type>AL</accept_acknowledgement_type>\n'+
'        <application_acknowledgement_type>AL</application_acknowledgement_type>\n'+
'        <country_code>US</country_code>\n'+
'        <project_id>{{{sec_project}}}</project_id>\n'+
'    </message_header>\n'+
'    <request_header>\n'+
'        <result_waittime_ms>{{{result_wait_time}}}000</result_waittime_ms>\n'+
'    </request_header>\n'+
'    <message_body>\n'+
'        <pm:get_all_global>\n'+
'        </pm:get_all_global>\n'+
'    </message_body>\n'+
'</i2b2:request>';
i2b2.PM.cfg.parsers.getAllGlobal = function() {
	if (!this.error) {
		this.model = [];		
		// extract records from XML msg
		var c = i2b2.h.XPath(this.refXML, "//message_body/descendant::param[@name]");
		var l = c.length;
		for (var i=0; i<l; i++) {
			var tmpRec = {};
			tmpRec.name = i2b2.h.XPath(c[i], "attribute::name")[0].nodeValue;
			tmpRec.id = i2b2.h.XPath(c[i], "attribute::id")[0].nodeValue;
			tmpRec.value = i2b2.h.XPath(c[i], "text()")[0].nodeValue;
			this.model.push(tmpRec);
		}
	} else {
		this.model = false;
		console.error("[getAllGlobals] Could not parse() data!");
	}
	return this;
};
i2b2.PM.ajax._addFunctionCall("getAllGlobal","{{{URL}}}getServices", i2b2.PM.cfg.msgs.getAllGlobal, null, i2b2.PM.cfg.parsers.getAllGlobal);



// ================================================================================================== //
i2b2.PM.cfg.msgs.getUser = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>\n'+
'<i2b2:request xmlns:i2b2="http://www.i2b2.org/xsd/hive/msg/1.1/" xmlns:pm="http://www.i2b2.org/xsd/cell/pm/1.1/">\n'+
'    <message_header>\n'+
'        {{{proxy_info}}}\n'+
'        <i2b2_version_compatible>1.1</i2b2_version_compatible>\n'+
'        <hl7_version_compatible>2.4</hl7_version_compatible>\n'+
'        <sending_application>\n'+
'            <application_name>i2b2 Project Management</application_name>\n'+
'            <application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'        </sending_application>\n'+
'        <sending_facility>\n'+
'            <facility_name>i2b2 Hive</facility_name>\n'+
'        </sending_facility>\n'+
'        <receiving_application>\n'+
'            <application_name>Project Management Cell</application_name>\n'+
'            <application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'        </receiving_application>\n'+
'        <receiving_facility>\n'+
'            <facility_name>i2b2 Hive</facility_name>\n'+
'        </receiving_facility>\n'+
'        <datetime_of_message>{{{header_msg_datetime}}}</datetime_of_message>\n'+
'		<security>\n'+
'			<domain>{{{sec_domain}}}</domain>\n'+
'			<username>{{{sec_user}}}</username>\n'+
'			{{{sec_pass_node}}}\n'+
'		</security>\n'+
'        <message_control_id>\n'+
'            <message_num>{{{header_msg_id}}}</message_num>\n'+
'            <instance_num>0</instance_num>\n'+
'        </message_control_id>\n'+
'        <processing_id>\n'+
'            <processing_id>P</processing_id>\n'+
'            <processing_mode>I</processing_mode>\n'+
'        </processing_id>\n'+
'        <accept_acknowledgement_type>AL</accept_acknowledgement_type>\n'+
'        <application_acknowledgement_type>AL</application_acknowledgement_type>\n'+
'        <country_code>US</country_code>\n'+
'        <project_id>{{{sec_project}}}</project_id>\n'+
'    </message_header>\n'+
'    <request_header>\n'+
'        <result_waittime_ms>{{{result_wait_time}}}000</result_waittime_ms>\n'+
'    </request_header>\n'+
'    <message_body>\n'+
'        <pm:get_user>{{{user_id}}}</pm:get_user>\n'+
'    </message_body>\n'+
'</i2b2:request>';
i2b2.PM.cfg.parsers.getUser = function() {
	if (!this.error) {
		this.model = [];		
		// extract records from XML msg
		var c = this.refXML.getElementsByTagName('user');
		var l = c.length;
		if (l == 0){
			var c = this.refXML.getElementsByTagName('ns4:user');
			var l = c.length;
		}

		for (var i=0; i<l; i++) {
			var tmpRec = {};
			tmpRec.full_name = i2b2.h.getXNodeVal(c[i], "full_name");
			tmpRec.user_name = i2b2.h.getXNodeVal(c[i], "user_name");
			tmpRec.email = i2b2.h.getXNodeVal(c[i], "email");
			tmpRec.password = i2b2.h.getXNodeVal(c[i], "password");
			tmpRec.is_admin = i2b2.h.getXNodeVal(c[i], "is_admin");			
			this.model.push(tmpRec);
		}
	} else {
		this.model = false;
		console.error("[getAllUser] Could not parse() data!");
	}
	return this;
};
i2b2.PM.ajax._addFunctionCall("getUser","{{{URL}}}getServices", i2b2.PM.cfg.msgs.getUser, null, i2b2.PM.cfg.parsers.getUser);


i2b2.PM.cfg.msgs.getCell = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>\n'+
'<i2b2:request xmlns:i2b2="http://www.i2b2.org/xsd/hive/msg/1.1/" xmlns:pm="http://www.i2b2.org/xsd/cell/pm/1.1/">\n'+
'    <message_header>\n'+
'        {{{proxy_info}}}\n'+
'        <i2b2_version_compatible>1.1</i2b2_version_compatible>\n'+
'        <hl7_version_compatible>2.4</hl7_version_compatible>\n'+
'        <sending_application>\n'+
'            <application_name>i2b2 Project Management</application_name>\n'+
'            <application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'        </sending_application>\n'+
'        <sending_facility>\n'+
'            <facility_name>i2b2 Hive</facility_name>\n'+
'        </sending_facility>\n'+
'        <receiving_application>\n'+
'            <application_name>Project Management Cell</application_name>\n'+
'            <application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'        </receiving_application>\n'+
'        <receiving_facility>\n'+
'            <facility_name>i2b2 Hive</facility_name>\n'+
'        </receiving_facility>\n'+
'        <datetime_of_message>{{{header_msg_datetime}}}</datetime_of_message>\n'+
'		<security>\n'+
'			<domain>{{{sec_domain}}}</domain>\n'+
'			<username>{{{sec_user}}}</username>\n'+
'			{{{sec_pass_node}}}\n'+
'		</security>\n'+
'        <message_control_id>\n'+
'            <message_num>{{{header_msg_id}}}</message_num>\n'+
'            <instance_num>0</instance_num>\n'+
'        </message_control_id>\n'+
'        <processing_id>\n'+
'            <processing_id>P</processing_id>\n'+
'            <processing_mode>I</processing_mode>\n'+
'        </processing_id>\n'+
'        <accept_acknowledgement_type>AL</accept_acknowledgement_type>\n'+
'        <application_acknowledgement_type>AL</application_acknowledgement_type>\n'+
'        <country_code>US</country_code>\n'+
'        <project_id>{{{sec_project}}}</project_id>\n'+
'    </message_header>\n'+
'    <request_header>\n'+
'        <result_waittime_ms>{{{result_wait_time}}}000</result_waittime_ms>\n'+
'    </request_header>\n'+
'    <message_body>\n'+
'        <pm:get_cell id="{{{cell_id}}}"><project_path>{{{proj_path}}}</project_path></pm:get_cell>\n'+
'    </message_body>\n'+
'</i2b2:request>';
i2b2.PM.cfg.parsers.getCell = function() {
	if (!this.error) {
		this.model = [];		
		// extract records from XML msg
		var c = this.refXML.getElementsByTagName('cell');
		var l = c.length;
		if (l == 0){
			var c = this.refXML.getElementsByTagName('ns4:cell');
			var l = c.length;
		}
		for (var i=0; i<l; i++) {
			var tmpRec = {};	
			tmpRec.id = i2b2.h.XPath(c[i], "attribute::id")[0].nodeValue;
			tmpRec.name = i2b2.h.getXNodeVal(c[i], "name");
			tmpRec.url = i2b2.h.getXNodeVal(c[i], "url");
			tmpRec.project_path = i2b2.h.getXNodeVal(c[i], "project_path");
			tmpRec.method = i2b2.h.getXNodeVal(c[i], "method");
			tmpRec.can_override = i2b2.h.getXNodeVal(c[i], "can_override");
			this.model.push(tmpRec);
		}
	} else {
		this.model = false;
		console.error("[getAllUser] Could not parse() data!");
	}
	return this;
};
i2b2.PM.ajax._addFunctionCall("getCell","{{{URL}}}getServices", i2b2.PM.cfg.msgs.getCell, null, i2b2.PM.cfg.parsers.getCell);


// ================================================================================================== //
i2b2.PM.cfg.msgs.getParam = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>\n'+
'<i2b2:request xmlns:i2b2="http://www.i2b2.org/xsd/hive/msg/1.1/" xmlns:pm="http://www.i2b2.org/xsd/cell/pm/1.1/">\n'+
'    <message_header>\n'+
'        {{{proxy_info}}}\n'+
'        <i2b2_version_compatible>1.1</i2b2_version_compatible>\n'+
'        <hl7_version_compatible>2.4</hl7_version_compatible>\n'+
'        <sending_application>\n'+
'            <application_name>i2b2 Project Management</application_name>\n'+
'            <application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'        </sending_application>\n'+
'        <sending_facility>\n'+
'            <facility_name>i2b2 Hive</facility_name>\n'+
'        </sending_facility>\n'+
'        <receiving_application>\n'+
'            <application_name>Project Management Cell</application_name>\n'+
'            <application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'        </receiving_application>\n'+
'        <receiving_facility>\n'+
'            <facility_name>i2b2 Hive</facility_name>\n'+
'        </receiving_facility>\n'+
'        <datetime_of_message>{{{header_msg_datetime}}}</datetime_of_message>\n'+
'		<security>\n'+
'			<domain>{{{sec_domain}}}</domain>\n'+
'			<username>{{{sec_user}}}</username>\n'+
'			{{{sec_pass_node}}}\n'+
'		</security>\n'+
'        <message_control_id>\n'+
'            <message_num>{{{header_msg_id}}}</message_num>\n'+
'            <instance_num>0</instance_num>\n'+
'        </message_control_id>\n'+
'        <processing_id>\n'+
'            <processing_id>P</processing_id>\n'+
'            <processing_mode>I</processing_mode>\n'+
'        </processing_id>\n'+
'        <accept_acknowledgement_type>AL</accept_acknowledgement_type>\n'+
'        <application_acknowledgement_type>AL</application_acknowledgement_type>\n'+
'        <country_code>US</country_code>\n'+
'        <project_id>{{{sec_project}}}</project_id>\n'+
'    </message_header>\n'+
'    <request_header>\n'+
'        <result_waittime_ms>{{{result_wait_time}}}000</result_waittime_ms>\n'+
'    </request_header>\n'+
'    <message_body>\n'+
'        <pm:get_{{{table}}} {{{id_param}}}>{{{id_xml}}}</pm:get_{{{table}}}>\n'+
'    </message_body>\n'+
'</i2b2:request>';

i2b2.PM.cfg.parsers.getParam = function() {
if (!this.error) {
		this.model = [];		
		// extract records from XML msg
		var c = this.refXML.getElementsByTagName('param');
//		var c = i2b2.h.XPath(this.refXML, "//message_body/descendant::param[@name]");
		var l = c.length;
		if (l == 0){
			var c = this.refXML.getElementsByTagName('ns4:param');
			var l = c.length;
		}

		for (var i=0; i<l; i++) {
			var tmpRec = {};
			tmpRec.name = i2b2.h.XPath(c[i], "attribute::name")[0].nodeValue;
			tmpRec.id = i2b2.h.XPath(c[i], "attribute::id")[0].nodeValue;
			tmpRec.dataType = i2b2.h.XPath(c[i], "attribute::datatype")[0].nodeValue;
			tmpRec.value = i2b2.h.XPath(c[i], "text()")[0].nodeValue;
			this.model.push(tmpRec);
		}
	} else {
		this.model = false;
		console.error("[GetHives] Could not parse() data!");
	}
	return this;
};
i2b2.PM.ajax._addFunctionCall("getParam", "{{{URL}}}getServices", i2b2.PM.cfg.msgs.getParam, ["id_xml"], i2b2.PM.cfg.parsers.getParam);



// ================================================================================================== //
i2b2.PM.cfg.msgs.getGlobal = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>\n'+
'<i2b2:request xmlns:i2b2="http://www.i2b2.org/xsd/hive/msg/1.1/" xmlns:pm="http://www.i2b2.org/xsd/cell/pm/1.1/">\n'+
'    <message_header>\n'+
'        {{{proxy_info}}}\n'+
'        <i2b2_version_compatible>1.1</i2b2_version_compatible>\n'+
'        <hl7_version_compatible>2.4</hl7_version_compatible>\n'+
'        <sending_application>\n'+
'            <application_name>i2b2 Project Management</application_name>\n'+
'            <application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'        </sending_application>\n'+
'        <sending_facility>\n'+
'            <facility_name>i2b2 Hive</facility_name>\n'+
'        </sending_facility>\n'+
'        <receiving_application>\n'+
'            <application_name>Project Management Cell</application_name>\n'+
'            <application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'        </receiving_application>\n'+
'        <receiving_facility>\n'+
'            <facility_name>i2b2 Hive</facility_name>\n'+
'        </receiving_facility>\n'+
'        <datetime_of_message>{{{header_msg_datetime}}}</datetime_of_message>\n'+
'		<security>\n'+
'			<domain>{{{sec_domain}}}</domain>\n'+
'			<username>{{{sec_user}}}</username>\n'+
'			{{{sec_pass_node}}}\n'+
'		</security>\n'+
'        <message_control_id>\n'+
'            <message_num>{{{header_msg_id}}}</message_num>\n'+
'            <instance_num>0</instance_num>\n'+
'        </message_control_id>\n'+
'        <processing_id>\n'+
'            <processing_id>P</processing_id>\n'+
'            <processing_mode>I</processing_mode>\n'+
'        </processing_id>\n'+
'        <accept_acknowledgement_type>AL</accept_acknowledgement_type>\n'+
'        <application_acknowledgement_type>AL</application_acknowledgement_type>\n'+
'        <country_code>US</country_code>\n'+
'        <project_id>{{{sec_project}}}</project_id>\n'+
'    </message_header>\n'+
'    <request_header>\n'+
'        <result_waittime_ms>{{{result_wait_time}}}000</result_waittime_ms>\n'+
'    </request_header>\n'+
'    <message_body>\n'+
'        <pm:get_{{{table}}} {{{id_param}}}>{{{id_xml}}}</pm:get_{{{table}}}>\n'+
'    </message_body>\n'+
'</i2b2:request>';

i2b2.PM.cfg.parsers.getGlobal = function() {
if (!this.error) {
		this.model = [];		
		// extract records from XML msg
		var c = this.refXML.getElementsByTagName('param');
//		var c = i2b2.h.XPath(this.refXML, "//message_body/descendant::param[@name]");
		var l = c.length;
		for (var i=0; i<l; i++) {
			var tmpRec = {};
			tmpRec.name = i2b2.h.XPath(c[i], "attribute::name")[0].nodeValue;
			tmpRec.id = i2b2.h.XPath(c[i], "attribute::id")[0].nodeValue;
			tmpRec.dataType = i2b2.h.XPath(c[i], "attribute::datatype")[0].nodeValue;
			tmpRec.value = i2b2.h.XPath(c[i], "text()")[0].nodeValue;
			this.model.push(tmpRec);
		}
	} else {
		this.model = false;
		console.error("[GetHives] Could not parse() data!");
	}
	return this;
};
i2b2.PM.ajax._addFunctionCall("getGlobal", "{{{URL}}}getServices", i2b2.PM.cfg.msgs.getGlobal, ["id_xml"], i2b2.PM.cfg.parsers.getGlobal);




// ================================================================================================== //
i2b2.PM.cfg.msgs.getProject = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>\n'+
'<i2b2:request xmlns:i2b2="http://www.i2b2.org/xsd/hive/msg/1.1/" xmlns:pm="http://www.i2b2.org/xsd/cell/pm/1.1/">\n'+
'    <message_header>\n'+
'        {{{proxy_info}}}\n'+
'        <i2b2_version_compatible>1.1</i2b2_version_compatible>\n'+
'        <hl7_version_compatible>2.4</hl7_version_compatible>\n'+
'        <sending_application>\n'+
'            <application_name>i2b2 Project Management</application_name>\n'+
'            <application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'        </sending_application>\n'+
'        <sending_facility>\n'+
'            <facility_name>i2b2 Hive</facility_name>\n'+
'        </sending_facility>\n'+
'        <receiving_application>\n'+
'            <application_name>Project Management Cell</application_name>\n'+
'            <application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'        </receiving_application>\n'+
'        <receiving_facility>\n'+
'            <facility_name>i2b2 Hive</facility_name>\n'+
'        </receiving_facility>\n'+
'        <datetime_of_message>{{{header_msg_datetime}}}</datetime_of_message>\n'+
'		<security>\n'+
'			<domain>{{{sec_domain}}}</domain>\n'+
'			<username>{{{sec_user}}}</username>\n'+
'			{{{sec_pass_node}}}\n'+
'		</security>\n'+
'        <message_control_id>\n'+
'            <message_num>{{{header_msg_id}}}</message_num>\n'+
'            <instance_num>0</instance_num>\n'+
'        </message_control_id>\n'+
'        <processing_id>\n'+
'            <processing_id>P</processing_id>\n'+
'            <processing_mode>I</processing_mode>\n'+
'        </processing_id>\n'+
'        <accept_acknowledgement_type>AL</accept_acknowledgement_type>\n'+
'        <application_acknowledgement_type>AL</application_acknowledgement_type>\n'+
'        <country_code>US</country_code>\n'+
'        <project_id>{{{sec_project}}}</project_id>\n'+
'    </message_header>\n'+
'    <request_header>\n'+
'        <result_waittime_ms>{{{result_wait_time}}}000</result_waittime_ms>\n'+
'    </request_header>\n'+
'    <message_body>\n'+
'        <pm:get_project id="{{{proj_code}}}"><path>{{{proj_path}}}</path></pm:get_project>\n'+
'    </message_body>\n'+
'</i2b2:request>';
i2b2.PM.cfg.parsers.getProject = function() {
	if (!this.error) {
		this.model = [];		
		// extract records from XML msg
		var c = i2b2.h.XPath(this.refXML, '//name/..');
		var l = c.length;
		for (var i=0; i<l; i++) {
			var tmpRec = {};
			tmpRec.id = i2b2.h.XPath(c[i], "attribute::id")[0].nodeValue;
			tmpRec.name = i2b2.h.getXNodeVal(c[i], "name");
			tmpRec.key = i2b2.h.getXNodeVal(c[i], "key");
			tmpRec.wiki = i2b2.h.getXNodeVal(c[i], "wiki");
			tmpRec.description = i2b2.h.getXNodeVal(c[i], "description");
			tmpRec.path = i2b2.h.getXNodeVal(c[i], "path");
			this.model.push(tmpRec);
		}
	} else {
		this.model = false;
		console.error("[getAllProject] Could not parse() data!");
	}
	return this;
};
i2b2.PM.ajax._addFunctionCall("getProject", "{{{URL}}}getServices", i2b2.PM.cfg.msgs.getProject, null, i2b2.PM.cfg.parsers.getProject);





















// ================================================================================================== //
i2b2.PM.cfg.msgs.setRole = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>\n'+
'<i2b2:request xmlns:i2b2="http://www.i2b2.org/xsd/hive/msg/1.1/" xmlns:pm="http://www.i2b2.org/xsd/cell/pm/1.1/">\n'+
'    <message_header>\n'+
'        {{{proxy_info}}}\n'+
'        <i2b2_version_compatible>1.1</i2b2_version_compatible>\n'+
'        <hl7_version_compatible>2.4</hl7_version_compatible>\n'+
'        <sending_application>\n'+
'            <application_name>i2b2 Project Management</application_name>\n'+
'            <application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'        </sending_application>\n'+
'        <sending_facility>\n'+
'            <facility_name>i2b2 Hive</facility_name>\n'+
'        </sending_facility>\n'+
'        <receiving_application>\n'+
'            <application_name>Project Management Cell</application_name>\n'+
'            <application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'        </receiving_application>\n'+
'        <receiving_facility>\n'+
'            <facility_name>i2b2 Hive</facility_name>\n'+
'        </receiving_facility>\n'+
'        <datetime_of_message>{{{header_msg_datetime}}}</datetime_of_message>\n'+
'		<security>\n'+
'			<domain>{{{sec_domain}}}</domain>\n'+
'			<username>{{{sec_user}}}</username>\n'+
'			{{{sec_pass_node}}}\n'+
'		</security>\n'+
'        <message_control_id>\n'+
'            <message_num>{{{header_msg_id}}}</message_num>\n'+
'            <instance_num>0</instance_num>\n'+
'        </message_control_id>\n'+
'        <processing_id>\n'+
'            <processing_id>P</processing_id>\n'+
'            <processing_mode>I</processing_mode>\n'+
'        </processing_id>\n'+
'        <accept_acknowledgement_type>AL</accept_acknowledgement_type>\n'+
'        <application_acknowledgement_type>AL</application_acknowledgement_type>\n'+
'        <country_code>US</country_code>\n'+
'        <project_id>{{{sec_project}}}</project_id>\n'+
'    </message_header>\n'+
'    <request_header>\n'+
'        <result_waittime_ms>{{{result_wait_time}}}000</result_waittime_ms>\n'+
'    </request_header>\n'+
'    <message_body>\n'+
'        <pm:set_role>\n'+
'			<user_name>{{{user_id}}}</user_name>\n'+
'			<role>{{{user_role}}}</role>\n'+
'			<project_id>{{{project_id}}}</project_id>\n'+
'        </pm:set_role>\n'+
'    </message_body>\n'+
'</i2b2:request>';
i2b2.PM.ajax._addFunctionCall("setRole","{{{URL}}}getServices", i2b2.PM.cfg.msgs.setRole,null);




// ================================================================================================== //
i2b2.PM.cfg.msgs.setHive = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>\n'+
'<i2b2:request xmlns:i2b2="http://www.i2b2.org/xsd/hive/msg/1.1/" xmlns:pm="http://www.i2b2.org/xsd/cell/pm/1.1/">\n'+
'    <message_header>\n'+
'        {{{proxy_info}}}\n'+
'        <i2b2_version_compatible>1.1</i2b2_version_compatible>\n'+
'        <hl7_version_compatible>2.4</hl7_version_compatible>\n'+
'        <sending_application>\n'+
'            <application_name>i2b2 Project Management</application_name>\n'+
'            <application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'        </sending_application>\n'+
'        <sending_facility>\n'+
'            <facility_name>i2b2 Hive</facility_name>\n'+
'        </sending_facility>\n'+
'        <receiving_application>\n'+
'            <application_name>Project Management Cell</application_name>\n'+
'            <application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'        </receiving_application>\n'+
'        <receiving_facility>\n'+
'            <facility_name>i2b2 Hive</facility_name>\n'+
'        </receiving_facility>\n'+
'        <datetime_of_message>{{{header_msg_datetime}}}</datetime_of_message>\n'+
'		<security>\n'+
'			<domain>{{{sec_domain}}}</domain>\n'+
'			<username>{{{sec_user}}}</username>\n'+
'			{{{sec_pass_node}}}\n'+
'		</security>\n'+
'        <message_control_id>\n'+
'            <message_num>{{{header_msg_id}}}</message_num>\n'+
'            <instance_num>0</instance_num>\n'+
'        </message_control_id>\n'+
'        <processing_id>\n'+
'            <processing_id>P</processing_id>\n'+
'            <processing_mode>I</processing_mode>\n'+
'        </processing_id>\n'+
'        <accept_acknowledgement_type>AL</accept_acknowledgement_type>\n'+
'        <application_acknowledgement_type>AL</application_acknowledgement_type>\n'+
'        <country_code>US</country_code>\n'+
'        <project_id>{{{sec_project}}}</project_id>\n'+
'    </message_header>\n'+
'    <request_header>\n'+
'        <result_waittime_ms>{{{result_wait_time}}}000</result_waittime_ms>\n'+
'    </request_header>\n'+
'    <message_body>\n'+
'        <pm:set_hive id="{{{domain_id}}}">\n'+
'			<environment>{{{environment}}}</environment>\n'+
'			<domain_name>{{{domain_name}}}</domain_name>\n'+
'			<helpURL>{{{helpURL}}}</helpURL>\n'+
'                    <active>true</active>\n'+
'        </pm:set_hive>\n'+
'    </message_body>\n'+
'</i2b2:request>';
i2b2.PM.ajax._addFunctionCall("setHive","{{{URL}}}getServices", i2b2.PM.cfg.msgs.setHive,null);


// ================================================================================================== //
i2b2.PM.cfg.msgs.setGlobal = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>\n'+
'<i2b2:request xmlns:i2b2="http://www.i2b2.org/xsd/hive/msg/1.1/" xmlns:pm="http://www.i2b2.org/xsd/cell/pm/1.1/">\n'+
'    <message_header>\n'+
'        {{{proxy_info}}}\n'+
'        <i2b2_version_compatible>1.1</i2b2_version_compatible>\n'+
'        <hl7_version_compatible>2.4</hl7_version_compatible>\n'+
'        <sending_application>\n'+
'            <application_name>i2b2 Project Management</application_name>\n'+
'            <application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'        </sending_application>\n'+
'        <sending_facility>\n'+
'            <facility_name>i2b2 Hive</facility_name>\n'+
'        </sending_facility>\n'+
'        <receiving_application>\n'+
'            <application_name>Project Management Cell</application_name>\n'+
'            <application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'        </receiving_application>\n'+
'        <receiving_facility>\n'+
'            <facility_name>i2b2 Hive</facility_name>\n'+
'        </receiving_facility>\n'+
'        <datetime_of_message>{{{header_msg_datetime}}}</datetime_of_message>\n'+
'		<security>\n'+
'			<domain>{{{sec_domain}}}</domain>\n'+
'			<username>{{{sec_user}}}</username>\n'+
'			{{{sec_pass_node}}}\n'+
'		</security>\n'+
'        <message_control_id>\n'+
'            <message_num>{{{header_msg_id}}}</message_num>\n'+
'            <instance_num>0</instance_num>\n'+
'        </message_control_id>\n'+
'        <processing_id>\n'+
'            <processing_id>P</processing_id>\n'+
'            <processing_mode>I</processing_mode>\n'+
'        </processing_id>\n'+
'        <accept_acknowledgement_type>AL</accept_acknowledgement_type>\n'+
'        <application_acknowledgement_type>AL</application_acknowledgement_type>\n'+
'        <country_code>US</country_code>\n'+
'        <project_id>{{{sec_project}}}</project_id>\n'+
'    </message_header>\n'+
'    <request_header>\n'+
'        <result_waittime_ms>{{{result_wait_time}}}000</result_waittime_ms>\n'+
'    </request_header>\n'+
'    <message_body>\n'+
'        <pm:set_global>\n'+
'           <param name="{{{param_name}}}" datatype="{{{param_datatype}}}" {{{param_id_attrib}}} >{{{param_value}}}</param>\n'+
'           <can_override>Y</can_override>\n'+
'           <project_path>/</project_path>'+
'        </pm:set_global>\n'+
'    </message_body>\n'+
'</i2b2:request>';
i2b2.PM.ajax._addFunctionCall("setGlobal","{{{URL}}}getServices", i2b2.PM.cfg.msgs.setGlobal, ["param_id_attrib"]);



// ================================================================================================== //
i2b2.PM.cfg.msgs.setParam = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>\n'+
'<i2b2:request xmlns:i2b2="http://www.i2b2.org/xsd/hive/msg/1.1/" xmlns:pm="http://www.i2b2.org/xsd/cell/pm/1.1/">\n'+
'    <message_header>\n'+
'        {{{proxy_info}}}\n'+
'        <i2b2_version_compatible>1.1</i2b2_version_compatible>\n'+
'        <hl7_version_compatible>2.4</hl7_version_compatible>\n'+
'        <sending_application>\n'+
'            <application_name>i2b2 Project Management</application_name>\n'+
'            <application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'        </sending_application>\n'+
'        <sending_facility>\n'+
'            <facility_name>i2b2 Hive</facility_name>\n'+
'        </sending_facility>\n'+
'        <receiving_application>\n'+
'            <application_name>Project Management Cell</application_name>\n'+
'            <application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'        </receiving_application>\n'+
'        <receiving_facility>\n'+
'            <facility_name>i2b2 Hive</facility_name>\n'+
'        </receiving_facility>\n'+
'        <datetime_of_message>{{{header_msg_datetime}}}</datetime_of_message>\n'+
'		<security>\n'+
'			<domain>{{{sec_domain}}}</domain>\n'+
'			<username>{{{sec_user}}}</username>\n'+
'			{{{sec_pass_node}}}\n'+
'		</security>\n'+
'        <message_control_id>\n'+
'            <message_num>{{{header_msg_id}}}</message_num>\n'+
'            <instance_num>0</instance_num>\n'+
'        </message_control_id>\n'+
'        <processing_id>\n'+
'            <processing_id>P</processing_id>\n'+
'            <processing_mode>I</processing_mode>\n'+
'        </processing_id>\n'+
'        <accept_acknowledgement_type>AL</accept_acknowledgement_type>\n'+
'        <application_acknowledgement_type>AL</application_acknowledgement_type>\n'+
'        <country_code>US</country_code>\n'+
'        <project_id>{{{sec_project}}}</project_id>\n'+
'    </message_header>\n'+
'    <request_header>\n'+
'        <result_waittime_ms>{{{result_wait_time}}}000</result_waittime_ms>\n'+
'    </request_header>\n'+
'    <message_body>\n'+
'        <pm:set_{{{table}}} {{{msg_attrib}}}>\n'+
'{{{msg_xml}}}'+
//'			<{{{table}}} id="{{{id}}}">\n'+
//'            <param name="{{{name}}}">{{{param}}}</param>\n'+
//'			</{{{table}}}>\n'+
'        </pm:set_{{{table}}}>\n'+
'    </message_body>\n'+
'</i2b2:request>';
i2b2.PM.ajax._addFunctionCall("setParam","{{{URL}}}getServices", i2b2.PM.cfg.msgs.setParam,["msg_xml"]);



// ================================================================================================== //
i2b2.PM.cfg.msgs.setCell = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>\n'+
'<i2b2:request xmlns:i2b2="http://www.i2b2.org/xsd/hive/msg/1.1/" xmlns:pm="http://www.i2b2.org/xsd/cell/pm/1.1/">\n'+
'    <message_header>\n'+
'        {{{proxy_info}}}\n'+
'        <i2b2_version_compatible>1.1</i2b2_version_compatible>\n'+
'        <hl7_version_compatible>2.4</hl7_version_compatible>\n'+
'        <sending_application>\n'+
'            <application_name>i2b2 Project Management</application_name>\n'+
'            <application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'        </sending_application>\n'+
'        <sending_facility>\n'+
'            <facility_name>i2b2 Hive</facility_name>\n'+
'        </sending_facility>\n'+
'        <receiving_application>\n'+
'            <application_name>Project Management Cell</application_name>\n'+
'            <application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'        </receiving_application>\n'+
'        <receiving_facility>\n'+
'            <facility_name>i2b2 Hive</facility_name>\n'+
'        </receiving_facility>\n'+
'        <datetime_of_message>{{{header_msg_datetime}}}</datetime_of_message>\n'+
'		<security>\n'+
'			<domain>{{{sec_domain}}}</domain>\n'+
'			<username>{{{sec_user}}}</username>\n'+
'			{{{sec_pass_node}}}\n'+
'		</security>\n'+
'        <message_control_id>\n'+
'            <message_num>{{{header_msg_id}}}</message_num>\n'+
'            <instance_num>0</instance_num>\n'+
'        </message_control_id>\n'+
'        <processing_id>\n'+
'            <processing_id>P</processing_id>\n'+
'            <processing_mode>I</processing_mode>\n'+
'        </processing_id>\n'+
'        <accept_acknowledgement_type>AL</accept_acknowledgement_type>\n'+
'        <application_acknowledgement_type>AL</application_acknowledgement_type>\n'+
'        <country_code>US</country_code>\n'+
'        <project_id>{{{sec_project}}}</project_id>\n'+
'    </message_header>\n'+
'    <request_header>\n'+
'        <result_waittime_ms>{{{result_wait_time}}}000</result_waittime_ms>\n'+
'    </request_header>\n'+
'    <message_body>\n'+
'        <pm:set_cell id="{{{cell_id}}}">\n'+
'			<project_path>{{{project_path}}}</project_path>\n'+
'			<name>{{{name}}}</name>\n'+
'			<url>{{{url}}}</url>\n'+
'			<method>{{{method}}}</method>\n'+
'			<can_override>{{{can_override}}}</can_override>\n'+
'        </pm:set_cell>\n'+
'    </message_body>\n'+
'</i2b2:request>';
i2b2.PM.ajax._addFunctionCall("setCell","{{{URL}}}getServices", i2b2.PM.cfg.msgs.setCell,null);



// ================================================================================================== //
i2b2.PM.cfg.msgs.setProject = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>\n'+
'<i2b2:request xmlns:i2b2="http://www.i2b2.org/xsd/hive/msg/1.1/" xmlns:pm="http://www.i2b2.org/xsd/cell/pm/1.1/">\n'+
'    <message_header>\n'+
'        {{{proxy_info}}}\n'+
'        <i2b2_version_compatible>1.1</i2b2_version_compatible>\n'+
'        <hl7_version_compatible>2.4</hl7_version_compatible>\n'+
'        <sending_application>\n'+
'            <application_name>i2b2 Project Management</application_name>\n'+
'            <application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'        </sending_application>\n'+
'        <sending_facility>\n'+
'            <facility_name>i2b2 Hive</facility_name>\n'+
'        </sending_facility>\n'+
'        <receiving_application>\n'+
'            <application_name>Project Management Cell</application_name>\n'+
'            <application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'        </receiving_application>\n'+
'        <receiving_facility>\n'+
'            <facility_name>i2b2 Hive</facility_name>\n'+
'        </receiving_facility>\n'+
'        <datetime_of_message>{{{header_msg_datetime}}}</datetime_of_message>\n'+
'		<security>\n'+
'			<domain>{{{sec_domain}}}</domain>\n'+
'			<username>{{{sec_user}}}</username>\n'+
'			{{{sec_pass_node}}}\n'+
'		</security>\n'+
'        <message_control_id>\n'+
'            <message_num>{{{header_msg_id}}}</message_num>\n'+
'            <instance_num>0</instance_num>\n'+
'        </message_control_id>\n'+
'        <processing_id>\n'+
'            <processing_id>P</processing_id>\n'+
'            <processing_mode>I</processing_mode>\n'+
'        </processing_id>\n'+
'        <accept_acknowledgement_type>AL</accept_acknowledgement_type>\n'+
'        <application_acknowledgement_type>AL</application_acknowledgement_type>\n'+
'        <country_code>US</country_code>\n'+
'        <project_id>{{{sec_project}}}</project_id>\n'+
'    </message_header>\n'+
'    <request_header>\n'+
'        <result_waittime_ms>{{{result_wait_time}}}000</result_waittime_ms>\n'+
'    </request_header>\n'+
'    <message_body>\n'+
'        <pm:set_project id="{{{id}}}">\n'+
'            <name>{{{name}}}</name>\n'+
'            <key>{{{key}}}</key>\n'+
'            <wiki>{{{wiki}}}</wiki>\n'+
'            <description>{{{description}}}</description>\n'+
'            <path>{{{path}}}</path>\n'+
'        </pm:set_project>\n'+
'    </message_body>\n'+
'</i2b2:request>';
i2b2.PM.ajax._addFunctionCall("setProject","{{{URL}}}getServices", i2b2.PM.cfg.msgs.setProject,null);



// ================================================================================================== //
i2b2.PM.cfg.msgs.setUser = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>\n'+
'<i2b2:request xmlns:i2b2="http://www.i2b2.org/xsd/hive/msg/1.1/" xmlns:pm="http://www.i2b2.org/xsd/cell/pm/1.1/">\n'+
'    <message_header>\n'+
'        {{{proxy_info}}}\n'+
'        <i2b2_version_compatible>1.1</i2b2_version_compatible>\n'+
'        <hl7_version_compatible>2.4</hl7_version_compatible>\n'+
'        <sending_application>\n'+
'            <application_name>i2b2 Project Management</application_name>\n'+
'            <application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'        </sending_application>\n'+
'        <sending_facility>\n'+
'            <facility_name>i2b2 Hive</facility_name>\n'+
'        </sending_facility>\n'+
'        <receiving_application>\n'+
'            <application_name>Project Management Cell</application_name>\n'+
'            <application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'        </receiving_application>\n'+
'        <receiving_facility>\n'+
'            <facility_name>i2b2 Hive</facility_name>\n'+
'        </receiving_facility>\n'+
'        <datetime_of_message>{{{header_msg_datetime}}}</datetime_of_message>\n'+
'		<security>\n'+
'			<domain>{{{sec_domain}}}</domain>\n'+
'			<username>{{{sec_user}}}</username>\n'+
'			{{{sec_pass_node}}}\n'+
'		</security>\n'+
'        <message_control_id>\n'+
'            <message_num>{{{header_msg_id}}}</message_num>\n'+
'            <instance_num>0</instance_num>\n'+
'        </message_control_id>\n'+
'        <processing_id>\n'+
'            <processing_id>P</processing_id>\n'+
'            <processing_mode>I</processing_mode>\n'+
'        </processing_id>\n'+
'        <accept_acknowledgement_type>AL</accept_acknowledgement_type>\n'+
'        <application_acknowledgement_type>AL</application_acknowledgement_type>\n'+
'        <country_code>US</country_code>\n'+
'        <project_id>{{{sec_project}}}</project_id>\n'+
'    </message_header>\n'+
'    <request_header>\n'+
'        <result_waittime_ms>{{{result_wait_time}}}000</result_waittime_ms>\n'+
'    </request_header>\n'+
'    <message_body>\n'+
'        <pm:set_user>\n'+
'			<user_name>{{{user_name}}}</user_name>\n'+
'			<full_name>{{{full_name}}}</full_name>\n'+
'			<email>{{{email}}}</email>\n'+
'			<is_admin>{{{is_admin}}}</is_admin>\n'+
'			{{{password}}}\n'+
'        </pm:set_user>\n'+
'    </message_body>\n'+
'</i2b2:request>';
i2b2.PM.ajax._addFunctionCall("setUser","{{{URL}}}getServices", i2b2.PM.cfg.msgs.setUser, ["password"]);




// ================================================================================================== //
i2b2.PM.cfg.msgs.setApproval = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>\n'+
'<i2b2:request xmlns:i2b2="http://www.i2b2.org/xsd/hive/msg/1.1/" xmlns:pm="http://www.i2b2.org/xsd/cell/pm/1.1/">\n'+
'    <message_header>\n'+
'        {{{proxy_info}}}\n'+
'        <i2b2_version_compatible>1.1</i2b2_version_compatible>\n'+
'        <hl7_version_compatible>2.4</hl7_version_compatible>\n'+
'        <sending_application>\n'+
'            <application_name>i2b2 Project Management</application_name>\n'+
'            <application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'        </sending_application>\n'+
'        <sending_facility>\n'+
'            <facility_name>i2b2 Hive</facility_name>\n'+
'        </sending_facility>\n'+
'        <receiving_application>\n'+
'            <application_name>Project Management Cell</application_name>\n'+
'            <application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'        </receiving_application>\n'+
'        <receiving_facility>\n'+
'            <facility_name>i2b2 Hive</facility_name>\n'+
'        </receiving_facility>\n'+
'        <datetime_of_message>{{{header_msg_datetime}}}</datetime_of_message>\n'+
'		<security>\n'+
'			<domain>{{{sec_domain}}}</domain>\n'+
'			<username>{{{sec_user}}}</username>\n'+
'			{{{sec_pass_node}}}\n'+
'		</security>\n'+
'        <message_control_id>\n'+
'            <message_num>{{{header_msg_id}}}</message_num>\n'+
'            <instance_num>0</instance_num>\n'+
'        </message_control_id>\n'+
'        <processing_id>\n'+
'            <processing_id>P</processing_id>\n'+
'            <processing_mode>I</processing_mode>\n'+
'        </processing_id>\n'+
'        <accept_acknowledgement_type>AL</accept_acknowledgement_type>\n'+
'        <application_acknowledgement_type>AL</application_acknowledgement_type>\n'+
'        <country_code>US</country_code>\n'+
'        <project_id>{{{sec_project}}}</project_id>\n'+
'    </message_header>\n'+
'    <request_header>\n'+
'        <result_waittime_ms>{{{result_wait_time}}}000</result_waittime_ms>\n'+
'    </request_header>\n'+
'    <message_body>\n'+
'        <pm:set_approval id="{{{id}}}">\n'+
'            <name>{{{name}}}</name>\n'+
'            <description>{{{description}}}</description>\n'+
'            <object_cd>APPROVAL</object_cd>\n'+
'			 <activation_date>{{{activation_date}}}</activation_date>\n'+
'			<expiration_date>{{{expiration_date}}}</expiration_date>\n'+
'        </pm:set_approval>\n'+
'    </message_body>\n'+
'</i2b2:request>';
i2b2.PM.ajax._addFunctionCall("setApproval","{{{URL}}}getServices", i2b2.PM.cfg.msgs.setApproval, null);




// ================================================================================================== //
i2b2.PM.cfg.msgs.deleteGlobal = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>\n'+
'<i2b2:request xmlns:i2b2="http://www.i2b2.org/xsd/hive/msg/1.1/" xmlns:pm="http://www.i2b2.org/xsd/cell/pm/1.1/">\n'+
'    <message_header>\n'+
'        {{{proxy_info}}}\n'+
'        <i2b2_version_compatible>1.1</i2b2_version_compatible>\n'+
'        <hl7_version_compatible>2.4</hl7_version_compatible>\n'+
'        <sending_application>\n'+
'            <application_name>i2b2 Project Management</application_name>\n'+
'            <application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'        </sending_application>\n'+
'        <sending_facility>\n'+
'            <facility_name>i2b2 Hive</facility_name>\n'+
'        </sending_facility>\n'+
'        <receiving_application>\n'+
'            <application_name>Project Management Cell</application_name>\n'+
'            <application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'        </receiving_application>\n'+
'        <receiving_facility>\n'+
'            <facility_name>i2b2 Hive</facility_name>\n'+
'        </receiving_facility>\n'+
'        <datetime_of_message>{{{header_msg_datetime}}}</datetime_of_message>\n'+
'		<security>\n'+
'			<domain>{{{sec_domain}}}</domain>\n'+
'			<username>{{{sec_user}}}</username>\n'+
'			{{{sec_pass_node}}}\n'+
'		</security>\n'+
'        <message_control_id>\n'+
'            <message_num>{{{header_msg_id}}}</message_num>\n'+
'            <instance_num>0</instance_num>\n'+
'        </message_control_id>\n'+
'        <processing_id>\n'+
'            <processing_id>P</processing_id>\n'+
'            <processing_mode>I</processing_mode>\n'+
'        </processing_id>\n'+
'        <accept_acknowledgement_type>AL</accept_acknowledgement_type>\n'+
'        <application_acknowledgement_type>AL</application_acknowledgement_type>\n'+
'        <country_code>US</country_code>\n'+
'        <project_id>{{{sec_project}}}</project_id>\n'+
'    </message_header>\n'+
'    <request_header>\n'+
'        <result_waittime_ms>{{{result_wait_time}}}000</result_waittime_ms>\n'+
'    </request_header>\n'+
'    <message_body>\n'+
'        <pm:delete_global>{{{param_id}}}</pm:delete_global>\n'+
'    </message_body>\n'+
'</i2b2:request>';
i2b2.PM.ajax._addFunctionCall("deleteGlobal","{{{URL}}}getServices", i2b2.PM.cfg.msgs.deleteGlobal, null);



// ================================================================================================== //
i2b2.PM.cfg.msgs.deleteHive = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>\n'+
'<i2b2:request xmlns:i2b2="http://www.i2b2.org/xsd/hive/msg/1.1/" xmlns:pm="http://www.i2b2.org/xsd/cell/pm/1.1/">\n'+
'    <message_header>\n'+
'        {{{proxy_info}}}\n'+
'        <i2b2_version_compatible>1.1</i2b2_version_compatible>\n'+
'        <hl7_version_compatible>2.4</hl7_version_compatible>\n'+
'        <sending_application>\n'+
'            <application_name>i2b2 Project Management</application_name>\n'+
'            <application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'        </sending_application>\n'+
'        <sending_facility>\n'+
'            <facility_name>i2b2 Hive</facility_name>\n'+
'        </sending_facility>\n'+
'        <receiving_application>\n'+
'            <application_name>Project Management Cell</application_name>\n'+
'            <application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'        </receiving_application>\n'+
'        <receiving_facility>\n'+
'            <facility_name>i2b2 Hive</facility_name>\n'+
'        </receiving_facility>\n'+
'        <datetime_of_message>{{{header_msg_datetime}}}</datetime_of_message>\n'+
'		<security>\n'+
'			<domain>{{{sec_domain}}}</domain>\n'+
'			<username>{{{sec_user}}}</username>\n'+
'			{{{sec_pass_node}}}\n'+
'		</security>\n'+
'        <message_control_id>\n'+
'            <message_num>{{{header_msg_id}}}</message_num>\n'+
'            <instance_num>0</instance_num>\n'+
'        </message_control_id>\n'+
'        <processing_id>\n'+
'            <processing_id>P</processing_id>\n'+
'            <processing_mode>I</processing_mode>\n'+
'        </processing_id>\n'+
'        <accept_acknowledgement_type>AL</accept_acknowledgement_type>\n'+
'        <application_acknowledgement_type>AL</application_acknowledgement_type>\n'+
'        <country_code>US</country_code>\n'+
'        <project_id>{{{sec_project}}}</project_id>\n'+
'    </message_header>\n'+
'    <request_header>\n'+
'        <result_waittime_ms>{{{result_wait_time}}}000</result_waittime_ms>\n'+
'    </request_header>\n'+
'    <message_body>\n'+
'        <pm:delete_hive id="{{{domain_id}}}"></pm:delete_hive>\n'+
'    </message_body>\n'+
'</i2b2:request>';
i2b2.PM.ajax._addFunctionCall("deleteHive","{{{URL}}}getServices", i2b2.PM.cfg.msgs.deleteHive,null);



// ================================================================================================== //
i2b2.PM.cfg.msgs.deleteCell = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>\n'+
'<i2b2:request xmlns:i2b2="http://www.i2b2.org/xsd/hive/msg/1.1/" xmlns:pm="http://www.i2b2.org/xsd/cell/pm/1.1/">\n'+
'    <message_header>\n'+
'        {{{proxy_info}}}\n'+
'        <i2b2_version_compatible>1.1</i2b2_version_compatible>\n'+
'        <hl7_version_compatible>2.4</hl7_version_compatible>\n'+
'        <sending_application>\n'+
'            <application_name>i2b2 Project Management</application_name>\n'+
'            <application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'        </sending_application>\n'+
'        <sending_facility>\n'+
'            <facility_name>i2b2 Hive</facility_name>\n'+
'        </sending_facility>\n'+
'        <receiving_application>\n'+
'            <application_name>Project Management Cell</application_name>\n'+
'            <application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'        </receiving_application>\n'+
'        <receiving_facility>\n'+
'            <facility_name>i2b2 Hive</facility_name>\n'+
'        </receiving_facility>\n'+
'        <datetime_of_message>{{{header_msg_datetime}}}</datetime_of_message>\n'+
'		<security>\n'+
'			<domain>{{{sec_domain}}}</domain>\n'+
'			<username>{{{sec_user}}}</username>\n'+
'			{{{sec_pass_node}}}\n'+
'		</security>\n'+
'        <message_control_id>\n'+
'            <message_num>{{{header_msg_id}}}</message_num>\n'+
'            <instance_num>0</instance_num>\n'+
'        </message_control_id>\n'+
'        <processing_id>\n'+
'            <processing_id>P</processing_id>\n'+
'            <processing_mode>I</processing_mode>\n'+
'        </processing_id>\n'+
'        <accept_acknowledgement_type>AL</accept_acknowledgement_type>\n'+
'        <application_acknowledgement_type>AL</application_acknowledgement_type>\n'+
'        <country_code>US</country_code>\n'+
'        <project_id>{{{sec_project}}}</project_id>\n'+
'    </message_header>\n'+
'    <request_header>\n'+
'        <result_waittime_ms>{{{result_wait_time}}}000</result_waittime_ms>\n'+
'    </request_header>\n'+
'    <message_body>\n'+
'        <pm:delete_cell id="{{{id}}}">\n'+
'			<project_path>{{{project_path}}}</project_path>\n'+
'		 </pm:delete_cell>\n'+
'    </message_body>\n'+
'</i2b2:request>';
i2b2.PM.ajax._addFunctionCall("deleteCell","{{{URL}}}getServices", i2b2.PM.cfg.msgs.deleteCell,null);


// ================================================================================================== //
i2b2.PM.cfg.msgs.deleteProject = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>\n'+
'<i2b2:request xmlns:i2b2="http://www.i2b2.org/xsd/hive/msg/1.1/" xmlns:pm="http://www.i2b2.org/xsd/cell/pm/1.1/">\n'+
'    <message_header>\n'+
'        {{{proxy_info}}}\n'+
'        <i2b2_version_compatible>1.1</i2b2_version_compatible>\n'+
'        <hl7_version_compatible>2.4</hl7_version_compatible>\n'+
'        <sending_application>\n'+
'            <application_name>i2b2 Project Management</application_name>\n'+
'            <application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'        </sending_application>\n'+
'        <sending_facility>\n'+
'            <facility_name>i2b2 Hive</facility_name>\n'+
'        </sending_facility>\n'+
'        <receiving_application>\n'+
'            <application_name>Project Management Cell</application_name>\n'+
'            <application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'        </receiving_application>\n'+
'        <receiving_facility>\n'+
'            <facility_name>i2b2 Hive</facility_name>\n'+
'        </receiving_facility>\n'+
'        <datetime_of_message>{{{header_msg_datetime}}}</datetime_of_message>\n'+
'		<security>\n'+
'			<domain>{{{sec_domain}}}</domain>\n'+
'			<username>{{{sec_user}}}</username>\n'+
'			{{{sec_pass_node}}}\n'+
'		</security>\n'+
'        <message_control_id>\n'+
'            <message_num>{{{header_msg_id}}}</message_num>\n'+
'            <instance_num>0</instance_num>\n'+
'        </message_control_id>\n'+
'        <processing_id>\n'+
'            <processing_id>P</processing_id>\n'+
'            <processing_mode>I</processing_mode>\n'+
'        </processing_id>\n'+
'        <accept_acknowledgement_type>AL</accept_acknowledgement_type>\n'+
'        <application_acknowledgement_type>AL</application_acknowledgement_type>\n'+
'        <country_code>US</country_code>\n'+
'        <project_id>{{{sec_project}}}</project_id>\n'+
'    </message_header>\n'+
'    <request_header>\n'+
'        <result_waittime_ms>{{{result_wait_time}}}000</result_waittime_ms>\n'+
'    </request_header>\n'+
'    <message_body>\n'+
'        <pm:delete_project id="{{{project_id}}}"><path>{{{project_path}}}</path></pm:delete_project>\n'+
'    </message_body>\n'+
'</i2b2:request>';
i2b2.PM.ajax._addFunctionCall("deleteProject","{{{URL}}}getServices", i2b2.PM.cfg.msgs.deleteProject,null);



// ================================================================================================== //
i2b2.PM.cfg.msgs.deleteRole = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>\n'+
'<i2b2:request xmlns:i2b2="http://www.i2b2.org/xsd/hive/msg/1.1/" xmlns:pm="http://www.i2b2.org/xsd/cell/pm/1.1/">\n'+
'    <message_header>\n'+
'        {{{proxy_info}}}\n'+
'        <i2b2_version_compatible>1.1</i2b2_version_compatible>\n'+
'        <hl7_version_compatible>2.4</hl7_version_compatible>\n'+
'        <sending_application>\n'+
'            <application_name>i2b2 Project Management</application_name>\n'+
'            <application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'        </sending_application>\n'+
'        <sending_facility>\n'+
'            <facility_name>i2b2 Hive</facility_name>\n'+
'        </sending_facility>\n'+
'        <receiving_application>\n'+
'            <application_name>Project Management Cell</application_name>\n'+
'            <application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'        </receiving_application>\n'+
'        <receiving_facility>\n'+
'            <facility_name>i2b2 Hive</facility_name>\n'+
'        </receiving_facility>\n'+
'        <datetime_of_message>{{{header_msg_datetime}}}</datetime_of_message>\n'+
'		<security>\n'+
'			<domain>{{{sec_domain}}}</domain>\n'+
'			<username>{{{sec_user}}}</username>\n'+
'			{{{sec_pass_node}}}\n'+
'		</security>\n'+
'        <message_control_id>\n'+
'            <message_num>{{{header_msg_id}}}</message_num>\n'+
'            <instance_num>0</instance_num>\n'+
'        </message_control_id>\n'+
'        <processing_id>\n'+
'            <processing_id>P</processing_id>\n'+
'            <processing_mode>I</processing_mode>\n'+
'        </processing_id>\n'+
'        <accept_acknowledgement_type>AL</accept_acknowledgement_type>\n'+
'        <application_acknowledgement_type>AL</application_acknowledgement_type>\n'+
'        <country_code>US</country_code>\n'+
'        <project_id>{{{sec_project}}}</project_id>\n'+
'    </message_header>\n'+
'    <request_header>\n'+
'        <result_waittime_ms>{{{result_wait_time}}}000</result_waittime_ms>\n'+
'    </request_header>\n'+
'    <message_body>\n'+
'        <pm:delete_role>\n'+
'            <project_id>{{{project_id}}}</project_id>\n'+
'            <user_name>{{{user_id}}}</user_name>\n'+
'            <role>{{{user_role}}}</role>\n'+
'        </pm:delete_role>\n'+
'    </message_body>\n'+
'</i2b2:request>';
i2b2.PM.ajax._addFunctionCall("deleteRole","{{{URL}}}getServices", i2b2.PM.cfg.msgs.deleteRole,null);



// ================================================================================================== //
i2b2.PM.cfg.msgs.deleteUser = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>\n'+
'<i2b2:request xmlns:i2b2="http://www.i2b2.org/xsd/hive/msg/1.1/" xmlns:pm="http://www.i2b2.org/xsd/cell/pm/1.1/">\n'+
'    <message_header>\n'+
'        {{{proxy_info}}}\n'+
'        <i2b2_version_compatible>1.1</i2b2_version_compatible>\n'+
'        <hl7_version_compatible>2.4</hl7_version_compatible>\n'+
'        <sending_application>\n'+
'            <application_name>i2b2 Project Management</application_name>\n'+
'            <application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'        </sending_application>\n'+
'        <sending_facility>\n'+
'            <facility_name>i2b2 Hive</facility_name>\n'+
'        </sending_facility>\n'+
'        <receiving_application>\n'+
'            <application_name>Project Management Cell</application_name>\n'+
'            <application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'        </receiving_application>\n'+
'        <receiving_facility>\n'+
'            <facility_name>i2b2 Hive</facility_name>\n'+
'        </receiving_facility>\n'+
'        <datetime_of_message>{{{header_msg_datetime}}}</datetime_of_message>\n'+
'		<security>\n'+
'			<domain>{{{sec_domain}}}</domain>\n'+
'			<username>{{{sec_user}}}</username>\n'+
'			{{{sec_pass_node}}}\n'+
'		</security>\n'+
'        <message_control_id>\n'+
'            <message_num>{{{header_msg_id}}}</message_num>\n'+
'            <instance_num>0</instance_num>\n'+
'        </message_control_id>\n'+
'        <processing_id>\n'+
'            <processing_id>P</processing_id>\n'+
'            <processing_mode>I</processing_mode>\n'+
'        </processing_id>\n'+
'        <accept_acknowledgement_type>AL</accept_acknowledgement_type>\n'+
'        <application_acknowledgement_type>AL</application_acknowledgement_type>\n'+
'        <country_code>US</country_code>\n'+
'        <project_id>{{{sec_project}}}</project_id>\n'+
'    </message_header>\n'+
'    <request_header>\n'+
'        <result_waittime_ms>{{{result_wait_time}}}000</result_waittime_ms>\n'+
'    </request_header>\n'+
'    <message_body>\n'+
'        <pm:delete_user>{{{user_name}}}</pm:delete_user>\n'+
'    </message_body>\n'+
'</i2b2:request>';
i2b2.PM.ajax._addFunctionCall("deleteUser","{{{URL}}}getServices", i2b2.PM.cfg.msgs.deleteUser,null);


// ================================================================================================== //
i2b2.PM.cfg.msgs.deleteApproval = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>\n'+
'<i2b2:request xmlns:i2b2="http://www.i2b2.org/xsd/hive/msg/1.1/" xmlns:pm="http://www.i2b2.org/xsd/cell/pm/1.1/">\n'+
'    <message_header>\n'+
'        {{{proxy_info}}}\n'+
'        <i2b2_version_compatible>1.1</i2b2_version_compatible>\n'+
'        <hl7_version_compatible>2.4</hl7_version_compatible>\n'+
'        <sending_application>\n'+
'            <application_name>i2b2 Project Management</application_name>\n'+
'            <application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'        </sending_application>\n'+
'        <sending_facility>\n'+
'            <facility_name>i2b2 Hive</facility_name>\n'+
'        </sending_facility>\n'+
'        <receiving_application>\n'+
'            <application_name>Project Management Cell</application_name>\n'+
'            <application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'        </receiving_application>\n'+
'        <receiving_facility>\n'+
'            <facility_name>i2b2 Hive</facility_name>\n'+
'        </receiving_facility>\n'+
'        <datetime_of_message>{{{header_msg_datetime}}}</datetime_of_message>\n'+
'		<security>\n'+
'			<domain>{{{sec_domain}}}</domain>\n'+
'			<username>{{{sec_user}}}</username>\n'+
'			{{{sec_pass_node}}}\n'+
'		</security>\n'+
'        <message_control_id>\n'+
'            <message_num>{{{header_msg_id}}}</message_num>\n'+
'            <instance_num>0</instance_num>\n'+
'        </message_control_id>\n'+
'        <processing_id>\n'+
'            <processing_id>P</processing_id>\n'+
'            <processing_mode>I</processing_mode>\n'+
'        </processing_id>\n'+
'        <accept_acknowledgement_type>AL</accept_acknowledgement_type>\n'+
'        <application_acknowledgement_type>AL</application_acknowledgement_type>\n'+
'        <country_code>US</country_code>\n'+
'        <project_id>{{{sec_project}}}</project_id>\n'+
'    </message_header>\n'+
'    <request_header>\n'+
'        <result_waittime_ms>{{{result_wait_time}}}000</result_waittime_ms>\n'+
'    </request_header>\n'+
'    <message_body>\n'+
'        <pm:delete_approval id="{{{id}}}"></pm:delete_approval>\n'+
'    </message_body>\n'+
'</i2b2:request>';
i2b2.PM.ajax._addFunctionCall("deleteApproval","{{{URL}}}getServices", i2b2.PM.cfg.msgs.deleteApproval,null);


// ================================================================================================== //
i2b2.PM.cfg.msgs.deleteParam = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>\n'+
'<i2b2:request xmlns:i2b2="http://www.i2b2.org/xsd/hive/msg/1.1/" xmlns:pm="http://www.i2b2.org/xsd/cell/pm/1.1/">\n'+
'    <message_header>\n'+
'        {{{proxy_info}}}\n'+
'        <i2b2_version_compatible>1.1</i2b2_version_compatible>\n'+
'        <hl7_version_compatible>2.4</hl7_version_compatible>\n'+
'        <sending_application>\n'+
'            <application_name>i2b2 Project Management</application_name>\n'+
'            <application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'        </sending_application>\n'+
'        <sending_facility>\n'+
'            <facility_name>i2b2 Hive</facility_name>\n'+
'        </sending_facility>\n'+
'        <receiving_application>\n'+
'            <application_name>Project Management Cell</application_name>\n'+
'            <application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'        </receiving_application>\n'+
'        <receiving_facility>\n'+
'            <facility_name>i2b2 Hive</facility_name>\n'+
'        </receiving_facility>\n'+
'        <datetime_of_message>{{{header_msg_datetime}}}</datetime_of_message>\n'+
'		<security>\n'+
'			<domain>{{{sec_domain}}}</domain>\n'+
'			<username>{{{sec_user}}}</username>\n'+
'			{{{sec_pass_node}}}\n'+
'		</security>\n'+
'        <message_control_id>\n'+
'            <message_num>{{{header_msg_id}}}</message_num>\n'+
'            <instance_num>0</instance_num>\n'+
'        </message_control_id>\n'+
'        <processing_id>\n'+
'            <processing_id>P</processing_id>\n'+
'            <processing_mode>I</processing_mode>\n'+
'        </processing_id>\n'+
'        <accept_acknowledgement_type>AL</accept_acknowledgement_type>\n'+
'        <application_acknowledgement_type>AL</application_acknowledgement_type>\n'+
'        <country_code>US</country_code>\n'+
'        <project_id>{{{sec_project}}}</project_id>\n'+
'    </message_header>\n'+
'    <request_header>\n'+
'        <result_waittime_ms>{{{result_wait_time}}}000</result_waittime_ms>\n'+
'    </request_header>\n'+
'    <message_body>\n'+
'        <pm:delete_{{{table}}} {{{msg_attrib}}}>\n'+
'{{{msg_xml}}}'+
'        </pm:delete_{{{table}}}>\n'+
'    </message_body>\n'+
'</i2b2:request>';
i2b2.PM.ajax._addFunctionCall("deleteParam","{{{URL}}}getServices", i2b2.PM.cfg.msgs.deleteParam,["msg_xml"]);

//================================================================================================== //
i2b2.PM.cfg.msgs.setPassword = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>\n'+
'<i2b2:request xmlns:i2b2="http://www.i2b2.org/xsd/hive/msg/1.1/" xmlns:pm="http://www.i2b2.org/xsd/cell/pm/1.1/">\n'+
'    <message_header>\n'+
'        {{{proxy_info}}}\n'+
'        <i2b2_version_compatible>1.1</i2b2_version_compatible>\n'+
'        <hl7_version_compatible>2.4</hl7_version_compatible>\n'+
'        <sending_application>\n'+
'            <application_name>i2b2 Project Management</application_name>\n'+
'            <application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'        </sending_application>\n'+
'        <sending_facility>\n'+
'            <facility_name>i2b2 Hive</facility_name>\n'+
'        </sending_facility>\n'+
'        <receiving_application>\n'+
'            <application_name>Project Management Cell</application_name>\n'+
'            <application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'        </receiving_application>\n'+
'        <receiving_facility>\n'+
'            <facility_name>i2b2 Hive</facility_name>\n'+
'        </receiving_facility>\n'+
'        <datetime_of_message>{{{header_msg_datetime}}}</datetime_of_message>\n'+
'               <security>\n'+
'                       <domain>{{{sec_domain}}}</domain>\n'+
'                       <username>{{{sec_user}}}</username>\n'+
'                       <password>{{{sec_oldpassword}}}</password>\n'+
'               </security>\n'+
'        <message_control_id>\n'+
'            <message_num>{{{header_msg_id}}}</message_num>\n'+
'            <instance_num>0</instance_num>\n'+
'        </message_control_id>\n'+
'        <processing_id>\n'+
'            <processing_id>P</processing_id>\n'+
'            <processing_mode>I</processing_mode>\n'+
'        </processing_id>\n'+
'        <accept_acknowledgement_type>AL</accept_acknowledgement_type>\n'+
'        <application_acknowledgement_type>AL</application_acknowledgement_type>\n'+
'        <country_code>US</country_code>\n'+
'        <project_id>{{{sec_project}}}</project_id>\n'+
'    </message_header>\n'+
'    <request_header>\n'+
'        <result_waittime_ms>{{{result_wait_time}}}000</result_waittime_ms>\n'+
'    </request_header>\n'+
'    <message_body>\n'+
'        <pm:set_password>\n'+
'{{{sec_newpassword}}}'+
'        </pm:set_password>\n'+
'    </message_body>\n'+
'</i2b2:request>';
i2b2.PM.ajax._addFunctionCall("setPassword","{{{URL}}}getServices", i2b2.PM.cfg.msgs.setPassword);