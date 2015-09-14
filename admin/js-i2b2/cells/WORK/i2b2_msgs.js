/**
 * @projectDescription	Messages to configure and build a WORK cell communicator object.
 * @namespace	i2b2.WORK.ajax
 * @author		Nick Benik, Griffin Weber MD PhD
 * @version 	1.3
 * updated 2-20-09: Refactor Communicator layer [Nick Benik] 
 */

// create the communicator Object
i2b2.WORK.ajax = i2b2.hive.communicatorFactory("WORK");

// create namespaces to hold all the communicator messages and parsing routines
i2b2.WORK.cfg.msgs = {};
i2b2.WORK.cfg.parsers = {};
		
// ================================================================================================== //
//i2b2.WORK.cfg.msgs.getFoldersByProject = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>\n'+ "NO EXAMPLE OF THIS FUNCTION BEING CALLED WAS FOUND."


// ================================================================================================== //
i2b2.WORK.cfg.msgs.moveChild = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>\n'+
'<ns3:request xmlns:ns3="http://www.i2b2.org/xsd/hive/msg/1.1/" '+
'	xmlns:ns4="http://www.i2b2.org/xsd/cell/work/1.1/" '+
'	xmlns:ns2="http://www.i2b2.org/xsd/hive/plugin/" '+
'	xmlns:ns5="http://www.i2b2.org/xsd/cell/ont/1.1/">\n'+
'	<message_header>\n'+
'		{{{proxy_info}}}\n'+
'		<i2b2_version_compatible>1.1</i2b2_version_compatible>\n'+
'			<sending_application>\n'+
'			<application_name>i2b2 Ontology</application_name>\n'+
'			<application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'		</sending_application>\n'+
'		<sending_facility>\n'+
'			<facility_name>i2b2 Hive</facility_name>\n'+
'		</sending_facility>\n'+
'		<receiving_application>\n'+
'			<application_name>1.1</application_name>\n'+
'			<application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'		</receiving_application>\n'+
'		<receiving_facility>\n'+
'			<facility_name>i2b2 Hive</facility_name>\n'+
'		</receiving_facility>\n'+
'		<datetime_of_message>{{{header_msg_datetime}}}</datetime_of_message>\n'+
'		<security>\n'+
'			<domain>{{{sec_domain}}}</domain>\n'+
'			<username>{{{sec_user}}}</username>\n'+
'			{{{sec_pass_node}}}\n'+'	</security>\n'+
'		<message_control_id>\n'+
'			<message_num>{{{header_msg_id}}}</message_num>\n'+
'			<instance_num>0</instance_num>\n'+
'		</message_control_id>\n'+
'		<processing_id>\n'+
'			<processing_id>P</processing_id>\n'+
'			<processing_mode>I</processing_mode>\n'+
'		</processing_id>\n'+
'		<accept_acknowledgement_type>AL</accept_acknowledgement_type>\n'+
'		<application_acknowledgement_type>AL</application_acknowledgement_type>\n'+
'		<country_code>US</country_code>\n'+
'		<project_id>{{{sec_project}}}</project_id>\n'+
'	</message_header>\n'+
'	<request_header>\n'+
'		<result_waittime_ms>{{{result_wait_time}}}000</result_waittime_ms>\n'+
'	</request_header>\n'+
'	<message_body>\n'+
'		<ns4:move_child>\n'+
'			<node>{{{target_node_id}}}</node>\n'+
'			<parent>{{{new_parent_node_id}}}</parent>\n'+
'		</ns4:move_child>\n'+
'	</message_body>\n'+
'</ns3:request>';
i2b2.WORK.ajax._addFunctionCall("moveChild","{{{URL}}}moveChild", i2b2.WORK.cfg.msgs.moveChild);

// ================================================================================================== //
// URL: Address: http://127.0.0.1:7070/i2b2/rest/WorkplaceService/getFoldersByUser
i2b2.WORK.cfg.msgs.getFoldersByUserId = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>\n'+
'<ns3:request xmlns:ns3="http://www.i2b2.org/xsd/hive/msg/1.1/" '+
'	xmlns:ns4="http://www.i2b2.org/xsd/cell/work/1.1/" '+
'	xmlns:ns2="http://www.i2b2.org/xsd/hive/plugin/" '+
'	xmlns:ns5="http://www.i2b2.org/xsd/cell/ont/1.1/">\n'+
'	<message_header>\n'+
'		{{{proxy_info}}}\n'+
'		<i2b2_version_compatible>1.1</i2b2_version_compatible>\n'+
'		<sending_application>\n'+
'			<application_name>i2b2 Ontology</application_name>\n'+
'			<application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'		</sending_application>\n'+
'		<sending_facility>\n'+
'			<facility_name>i2b2 Hive</facility_name>\n'+
'		</sending_facility>\n'+
'		<receiving_application>\n'+
'			<application_name>1.1</application_name>\n'+
'			<application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'		</receiving_application>\n'+
'		<receiving_facility>\n'+
'			<facility_name>i2b2 Hive</facility_name>\n'+
'		</receiving_facility>\n'+
'		<datetime_of_message>{{{header_msg_datetime}}}</datetime_of_message>\n'+
'		<security>\n'+
'			<domain>{{{sec_domain}}}</domain>\n'+
'			<username>{{{sec_user}}}</username>\n'+
'			{{{sec_pass_node}}}\n'+
'		</security>\n'+
'		<message_control_id>\n'+
'			<message_num>{{{header_msg_id}}}</message_num>\n'+
'			<instance_num>0</instance_num>\n'+
'		</message_control_id>\n'+
'		<processing_id>\n'+
'			<processing_id>P</processing_id>\n'+
'			<processing_mode>I</processing_mode>\n'+
'		</processing_id>\n'+
'		<accept_acknowledgement_type>AL</accept_acknowledgement_type>\n'+
'		<application_acknowledgement_type>AL</application_acknowledgement_type>\n'+
'		<country_code>US</country_code>\n'+
'		<project_id>{{{sec_project}}}</project_id>\n'+
'	</message_header>\n'+
'	<request_header>\n'+
'		<result_waittime_ms>{{{result_wait_time}}}000</result_waittime_ms>\n'+
'	</request_header>\n'+
'	<message_body>\n'+
'		<ns4:get_folders_by_userId type="core" />\n'+
'	</message_body>\n'+
'</ns3:request>';
i2b2.WORK.cfg.parsers.getFoldersByUser = function(){
	if (!this.error) {
		this.model = [];		
		var nlst = i2b2.h.XPath(this.refXML, "//folder[name and share_id and index and visual_attributes]");
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
			nodeData.isRoot = true;
			// encapsulate into SDX object
			var sdxDataPack = i2b2.sdx.Master.EncapsulateData('WRK', nodeData);
			this.model.push(sdxDataPack);
		}
	} else {
		this.model = false;
		console.error("[getFoldersByUserId] Could not parse() data!");
	}
	return this;
}
i2b2.WORK.ajax._addFunctionCall(	"getFoldersByUserId",
									"{{{URL}}}getFoldersByUserId",
									i2b2.WORK.cfg.msgs.getFoldersByUserId,
									null,
									i2b2.WORK.cfg.parsers.getFoldersByUserId);



// ================================================================================================== //
// URL: Address: http://127.0.0.1:7070/i2b2/rest/WorkplaceService/getFoldersByProject
i2b2.WORK.cfg.msgs.getFoldersByProject = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>\n'+
'<ns3:request xmlns:ns3="http://www.i2b2.org/xsd/hive/msg/1.1/" '+
'	xmlns:ns4="http://www.i2b2.org/xsd/cell/work/1.1/" '+
'	xmlns:ns2="http://www.i2b2.org/xsd/hive/plugin/" '+
'	xmlns:ns5="http://www.i2b2.org/xsd/cell/ont/1.1/">\n'+
'	<message_header>\n'+
'		{{{proxy_info}}}\n'+
'		<i2b2_version_compatible>1.1</i2b2_version_compatible>\n'+
'		<sending_application>\n'+
'			<application_name>i2b2 Ontology</application_name>\n'+
'			<application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'		</sending_application>\n'+
'		<sending_facility>\n'+
'			<facility_name>i2b2 Hive</facility_name>\n'+
'		</sending_facility>\n'+
'		<receiving_application>\n'+
'			<application_name>1.1</application_name>\n'+
'			<application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'		</receiving_application>\n'+
'		<receiving_facility>\n'+
'			<facility_name>i2b2 Hive</facility_name>\n'+
'		</receiving_facility>\n'+
'		<datetime_of_message>{{{header_msg_datetime}}}</datetime_of_message>\n'+
'		<security>\n'+
'			<domain>{{{sec_domain}}}</domain>\n'+
'			<username>{{{sec_user}}}</username>\n'+
'			{{{sec_pass_node}}}\n'+
'		</security>\n'+
'		<message_control_id>\n'+
'			<message_num>{{{header_msg_id}}}</message_num>\n'+
'			<instance_num>0</instance_num>\n'+
'		</message_control_id>\n'+
'		<processing_id>\n'+
'			<processing_id>P</processing_id>\n'+
'			<processing_mode>I</processing_mode>\n'+
'		</processing_id>\n'+
'		<accept_acknowledgement_type>AL</accept_acknowledgement_type>\n'+
'		<application_acknowledgement_type>AL</application_acknowledgement_type>\n'+
'		<country_code>US</country_code>\n'+
'		<project_id>{{{sec_project}}}</project_id>\n'+
'	</message_header>\n'+
'	<request_header>\n'+
'		<result_waittime_ms>{{{result_wait_time}}}000</result_waittime_ms>\n'+
'	</request_header>\n'+
'	<message_body>\n'+
'		<ns4:get_folders_by_project type="core" />\n'+
'	</message_body>\n'+
'</ns3:request>';
i2b2.WORK.cfg.parsers.getFoldersByProject = function(){
	if (!this.error) {
		this.model = [];		
		var nlst = i2b2.h.XPath(this.refXML, "//folder[name and share_id and index and visual_attributes]");
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
			nodeData.isRoot = true;
			// encapsulate into SDX object
			var sdxDataPack = i2b2.sdx.Master.EncapsulateData('WRK', nodeData);
			this.model.push(sdxDataPack);
		}
	} else {
		this.model = false;
		console.error("[getQueryMasterList_fromUserId] Could not parse() data!");
	}
	return this;
}
i2b2.WORK.ajax._addFunctionCall(	"getFoldersByProject",
									"{{{URL}}}getFoldersByProject",
									i2b2.WORK.cfg.msgs.getFoldersByProject,
									null,
									i2b2.WORK.cfg.parsers.getFoldersByProject);



// ================================================================================================== //
// URL: Address: http://127.0.0.1:7070/i2b2/rest/WorkplaceService/getChildren
i2b2.WORK.cfg.msgs.getChildren = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>\n'+
'<ns3:request xmlns:ns3="http://www.i2b2.org/xsd/hive/msg/1.1/" '+
'	xmlns:ns4="http://www.i2b2.org/xsd/cell/work/1.1/" '+
'	xmlns:ns2="http://www.i2b2.org/xsd/hive/plugin/" '+
'	xmlns:ns5="http://www.i2b2.org/xsd/cell/ont/1.1/">\n'+
'	<message_header>\n'+
'		{{{proxy_info}}}\n'+
'		<i2b2_version_compatible>1.1</i2b2_version_compatible>\n'+
'		<sending_application>\n'+
'			<application_name>i2b2 Ontology</application_name>\n'+
'			<application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'		</sending_application>\n'+
'		<sending_facility>\n'+
'			<facility_name>i2b2 Hive</facility_name>\n'+
'		</sending_facility>\n'+
'		<receiving_application>\n'+
'			<application_name>1.1</application_name>\n'+
'			<application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'		</receiving_application>\n'+
'		<receiving_facility>\n'+
'			<facility_name>i2b2 Hive</facility_name>\n'+
'		</receiving_facility>\n'+
'		<datetime_of_message>{{{header_msg_datetime}}}</datetime_of_message>\n'+
'		<security>\n'+
'			<domain>{{{sec_domain}}}</domain>\n'+
'			<username>{{{sec_user}}}</username>\n'+
'			{{{sec_pass_node}}}\n'+
'		</security>\n'+
'		<message_control_id>\n'+
'			<message_num>{{{header_msg_id}}}</message_num>\n'+
'			<instance_num>0</instance_num>\n'+
'		</message_control_id>\n'+
'		<processing_id>\n'+
'			<processing_id>P</processing_id>\n'+
'			<processing_mode>I</processing_mode>\n'+
'		</processing_id>\n'+
'		<accept_acknowledgement_type>AL</accept_acknowledgement_type>\n'+
'		<application_acknowledgement_type>AL</application_acknowledgement_type>\n'+
'		<country_code>US</country_code>\n'+
'		<project_id>{{{sec_project}}}</project_id>\n'+
'	</message_header>\n'+
'	<request_header>\n'+
'		<result_waittime_ms>{{{result_wait_time}}}000</result_waittime_ms>\n'+
'	</request_header>\n'+
'	<message_body>\n'+
'		<ns4:get_children blob="true">\n'+
'			<parent>{{{parent_key_value}}}</parent>\n'+
'		</ns4:get_children>\n'+
'	</message_body>\n'+
'</ns3:request>';
i2b2.WORK.cfg.parsers.getChildren = function(){
	if (!this.error) {
		this.model = [];
		var nlst = i2b2.h.XPath(this.refXML, "//folder[name and share_id and index and visual_attributes]");
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
			nodeData.isRoot = false;
			// encapsulate into SDX object
			var sdxDataPack = i2b2.sdx.Master.EncapsulateData('WRK', nodeData);
			this.model.push(sdxDataPack);
		}
	} else {
		this.model = false;
		console.error("[getQueryMasterList_fromUserId] Could not parse() data!");
	}
	return this;
}
i2b2.WORK.ajax._addFunctionCall("getChildren",
								"{{{URL}}}getChildren",
								i2b2.WORK.cfg.msgs.getChildren, 
								null,
								i2b2.WORK.cfg.parsers.getChildren);



// ================================================================================================== //
// URL: Address: http://127.0.0.1:7070/i2b2/rest/WorkplaceService/addChild
i2b2.WORK.cfg.msgs.addChild = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>\n'+
'<ns3:request xmlns:ns3="http://www.i2b2.org/xsd/hive/msg/1.1/" '+
'	xmlns:ns4="http://www.i2b2.org/xsd/cell/work/1.1/" '+
'	xmlns:ns2="http://www.i2b2.org/xsd/hive/plugin/" '+
'	xmlns:ns5="http://www.i2b2.org/xsd/cell/ont/1.1/">\n'+
'	<message_header>\n'+
'		{{{proxy_info}}}\n'+
'		<i2b2_version_compatible>1.1</i2b2_version_compatible>\n'+
'		<sending_application>\n'+
'			<application_name>i2b2 Ontology</application_name>\n'+
'			<application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'		</sending_application>\n'+
'		<sending_facility>\n'+
'			<facility_name>i2b2 Hive</facility_name>\n'+
'		</sending_facility>\n'+
'		<receiving_application>\n'+
'			<application_name>1.1</application_name>\n'+
'			<application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'		</receiving_application>\n'+
'		<receiving_facility>\n'+
'			<facility_name>i2b2 Hive</facility_name>\n'+
'		</receiving_facility>\n'+
'		<datetime_of_message>{{{header_msg_datetime}}}</datetime_of_message>\n'+
'		<security>\n'+
'			<domain>{{{sec_domain}}}</domain>\n'+
'			<username>{{{sec_user}}}</username>\n'+
'			{{{sec_pass_node}}}\n'+
'		</security>\n'+
'		<message_control_id>\n'+
'			<message_num>{{{header_msg_id}}}</message_num>\n'+
'			<instance_num>0</instance_num>\n'+
'		</message_control_id>\n'+
'		<processing_id>\n'+
'			<processing_id>P</processing_id>\n'+
'			<processing_mode>I</processing_mode>\n'+
'		</processing_id>\n'+
'		<accept_acknowledgement_type>AL</accept_acknowledgement_type>\n'+
'		<application_acknowledgement_type>AL</application_acknowledgement_type>\n'+
'		<country_code>US</country_code>\n'+
'		<project_id>{{{sec_project}}}</project_id>\n'+
'	</message_header>\n'+
'	<request_header>\n'+
'		<result_waittime_ms>{{{result_wait_time}}}000</result_waittime_ms>\n'+
'	</request_header>\n'+
'	<message_body>\n'+
'		<ns4:add_child>\n'+
'			<name>{{{child_name}}}</name>\n'+
'			<user_id>{{{sec_user}}}</user_id>\n'+
'			<group_id>{{{sec_project}}}</group_id>\n'+
'			<share_id>{{{share_id}}}</share_id>\n'+
'			<index>{{{child_index}}}</index>\n'+
'			<parent_index>{{{parent_key_value}}}</parent_index>\n'+
'			<visual_attributes>{{{child_visual_attributes}}}</visual_attributes>\n'+
'			<tooltip>{{{child_annotation}}}</tooltip>\n'+
'			<work_xml_i2b2_type>{{{child_work_type}}}</work_xml_i2b2_type>\n'+
'			{{{child_work_xml}}}\n'+
'		</ns4:add_child>\n'+
'	</message_body>\n'+
'</ns3:request>';
// ================================================================================================== //
i2b2.WORK.ajax._addFunctionCall("addChild","{{{URL}}}addChild", i2b2.WORK.cfg.msgs.addChild, ['child_work_xml']);



// URL: <none> (used by addChild to encapsulate non-WRK SDX type of CONCPT)
i2b2.WORK.cfg.msgs.encapsulateCONCPT = ''+
'			<work_xml>\n'+
'				<ns2:plugin_drag_drop xmlns:ns2="http://www.i2b2.org/xsd/hive/plugin/">\n'+
'					<ns5:concepts xmlns:ns5="http://www.i2b2.org/xsd/cell/ont/1.1/">\n'+
'						<concept>\n'+
'							<level>{{{concept_level}}}</level>\n'+
'							<key>{{{concept_id}}}</key>\n'+
'							<name>{{{concept_name}}}</name>\n'+
'							<synonym_cd>{{{concept_synonym}}}</synonym_cd>\n'+
'							<visualattributes>{{{concept_visual_attributes}}}</visualattributes>\n'+
'							<totalnum>{{{concept_total}}}</totalnum>\n'+
'							<basecode>{{{concept_basecode}}}</basecode>\n'+
'							<facttablecolumn>{{{concept_fact_table_column}}}</facttablecolumn>\n'+
'							<tablename>{{{concept_table_name}}}</tablename>\n'+
'							<columnname>{{{concept_column_name}}}</columnname>\n'+
'							<columndatatype>{{{concept_column_data_type}}}</columndatatype>\n'+
'							<operator>{{{concept_operator}}}</operator>\n'+
'							<dimcode>{{{concept_dimcode}}}</dimcode>\n'+
'							<comment>{{{concept_comment}}}</comment>\n'+
'							<tooltip>{{{concept_tooltip}}}</tooltip>\n'+
'						</concept>\n'+
'					</ns5:concepts>\n'+
'				</ns2:plugin_drag_drop>\n'+
'			</work_xml>\n';
// ================================================================================================== //


// URL: <none> (used by addChild to encapsulate non-WRK SDX type of PRS)
i2b2.WORK.cfg.msgs.encapsulatePRS = ''+
'			<work_xml>\n'+
'				<ns5:plugin_drag_drop xmlns:ns5="http://www.i2b2.org/xsd/hive/plugin/">\n'+
'					<ns4:query_result_instance xmlns:ns4="http://www.i2b2.org/xsd/cell/crc/psm/1.1/">\n'+
'						<result_instance_id>{{{prs_id}}}</result_instance_id>\n'+
'						<query_instance_id>{{{qi_id}}}</query_instance_id>\n'+
'						<description>{{{prs_description}}}</description>\n' +
'						<query_result_type>\n'+
'							<result_type_id>1</result_type_id>\n'+
'							<name>PATIENTSET</name>\n'+
'							<display_type>LIST</display_type>\n' +
'							<visual_attribute_type>LA</visual_attribute_type>\n' +
'							<description>Patient list</description>\n'+
'						</query_result_type>\n'+
'						<set_size>{{{prs_set_size}}}</set_size>\n'+
'						<start_date>{{{prs_start_date}}}</start_date>\n'+
'						<end_date>{{{prs_end_date}}}</end_date>\n'+
//'						<query_status_type>\n'+
//'							<status_type_id>3</status_type_id>\n'+
//'							<name>FINISHED</name>\n'+
//'							<description>FINISHED</description>\n'+
//'						</query_status_type>\n'+
'					</ns4:query_result_instance>\n'+
'				</ns5:plugin_drag_drop>\n'+
'			</work_xml>\n';
// ================================================================================================== //

// URL: <none> (used by addChild to encapsulate non-WRK SDX type of PRS)
i2b2.WORK.cfg.msgs.encapsulateENS = ''+
'			<work_xml>\n'+
'				<ns5:plugin_drag_drop xmlns:ns5="http://www.i2b2.org/xsd/hive/plugin/">\n'+
'					<ns4:query_result_instance xmlns:ns4="http://www.i2b2.org/xsd/cell/crc/psm/1.1/">\n'+
'						<result_instance_id>{{{prs_id}}}</result_instance_id>\n'+
'						<query_instance_id>{{{qi_id}}}</query_instance_id>\n'+
'						<description>{{{prs_description}}}</description>\n' +
'						<query_result_type>\n'+
'							<result_type_id>1</result_type_id>\n'+
'							<name>ENCOUNTERSET</name>\n'+
'							<display_type>LIST</display_type>\n' +
'							<visual_attribute_type>LA</visual_attribute_type>\n' +
'							<description>Encounter list</description>\n'+
'						</query_result_type>\n'+
'						<set_size>{{{prs_set_size}}}</set_size>\n'+
'						<start_date>{{{prs_start_date}}}</start_date>\n'+
'						<end_date>{{{prs_end_date}}}</end_date>\n'+
'					</ns4:query_result_instance>\n'+
'				</ns5:plugin_drag_drop>\n'+
'			</work_xml>\n';
// ================================================================================================== //


// URL: <none> (used by addChild to encapsulate non-WRK SDX type of PRC)
i2b2.WORK.cfg.msgs.encapsulatePRC = ''+
'			<work_xml>\n'+
'				<ns5:plugin_drag_drop xmlns:ns5="http://www.i2b2.org/xsd/hive/plugin/">\n'+
'					<ns4:query_result_instance xmlns:ns4="http://www.i2b2.org/xsd/cell/crc/psm/1.1/">\n'+
'						<result_instance_id>{{{prc_id}}}</result_instance_id>\n'+
'						<query_instance_id>{{{qi_id}}}</query_instance_id>\n'+
'						<query_result_type>\n'+
'							<result_type_id>4</result_type_id>\n'+
'							<name>PATIENT_COUNT_XML</name>\n'+
'							<description>Number of patients</description>\n'+
'						</query_result_type>\n'+
'						<set_size>{{{prc_set_size}}}</set_size>\n'+
'						<start_date>{{{prc_start_date}}}</start_date>\n'+
'						<end_date>{{{prc_end_date}}}</end_date>\n'+
//'						<query_status_type>\n'+
//'							<status_type_id>3</status_type_id>\n'+
//'							<name>FINISHED</name>\n'+
//'							<description>FINISHED</description>\n'+
//'						</query_status_type>\n'+
'					</ns4:query_result_instance>\n'+
'				</ns5:plugin_drag_drop>\n'+
'			</work_xml>\n';
// ================================================================================================== //


// URL: <none> (used by addChild to encapsulate non-WRK SDX type of PRC)
i2b2.WORK.cfg.msgs.encapsulateQM = ''+
'			<work_xml>\n'+
'				<ns5:plugin_drag_drop xmlns:ns5="http://www.i2b2.org/xsd/hive/plugin/">\n'+
'					<ns4:query_master xmlns:ns4="http://www.i2b2.org/xsd/cell/crc/psm/1.1/">\n'+
'						<query_master_id>{{{qm_id}}}</query_master_id>\n'+
'						<name>{{{qm_name}}}</name>\n'+
'						<user_id>{{{qm_user_id}}}</user_id>\n'+
'						<group_id>{{{qm_user_group_id}}}</group_id>\n'+
'					</ns4:query_master>\n'+
'				</ns5:plugin_drag_drop>\n'+
'			</work_xml>\n';
// ================================================================================================== //

// URL: <none> (used by addChild to encapsulate non-WRK SDX type of PR)
i2b2.WORK.cfg.msgs.encapsulatePR = ''+
'			<work_xml>\n'+
'				<ns5:plugin_drag_drop xmlns:ns5="http://www.i2b2.org/xsd/hive/plugin/" \n'+
'				xmlns:ns2="http://www.i2b2.org/xsd/hive/pdo/1.1/" \n'+
'				xmlns:ns3="http://www.i2b2.org/xsd/cell/crc/pdo/1.1/" \n'+
'				xmlns:ns4="http://www.i2b2.org/xsd/cell/crc/psm/1.1/" \n'+
'				xmlns:ns6="http://www.i2b2.org/xsd/hive/msg/1.1/" \n'+
'				xmlns:ns7="http://www.i2b2.org/xsd/cell/crc/psm/querydefinition/1.1/"> \n'+
'					<ns5:patient_set patient_set_id="{{{parent_prs_id}}}" patient_set_name="{{{parent_prs_name}}}"> \n'+
'						<patient><patient_id>{{{pr_id}}}</patient_id></patient> \n'+
'			    	</ns5:patient_set> \n'+
'				</ns5:plugin_drag_drop> \n'+
'			</work_xml>\n';
// ================================================================================================== //


// URL: <none> (used by addChild to encapsulate non-WRK SDX type of QDEF)
i2b2.WORK.cfg.msgs.encapsulateQDEF = ''+
'			<work_xml>\n'+
'				<ns5:plugin_drag_drop xmlns:ns4="http://www.i2b2.org/xsd/cell/crc/psm/1.1/" xmlns:ns7="http://www.i2b2.org/xsd/cell/ont/1.1/" xmlns:ns3="http://www.i2b2.org/xsd/cell/crc/pdo/1.1/" xmlns:ns2="http://www.i2b2.org/xsd/hive/pdo/1.1/" xmlns:ns5="http://www.i2b2.org/xsd/hive/plugin/" xmlns:ns6="http://www.i2b2.org/xsd/hive/msg/1.1/" xmlns:ns8="http://www.i2b2.org/xsd/cell/crc/psm/querydefinition/1.1/">\n'+
'{{{query_def}}}\n'+
'				</ns5:plugin_drag_drop>\n'+
'			</work_xml>\n';
// ================================================================================================== //


// URL: <none> (used by addChild to encapsulate non-WRK SDX type of QGDEF)
i2b2.WORK.cfg.msgs.encapsulateQGDEF = ''+
'			<work_xml>\n'+
'				<ns5:plugin_drag_drop xmlns:ns4="http://www.i2b2.org/xsd/cell/crc/psm/1.1/" xmlns:ns7="http://www.i2b2.org/xsd/cell/ont/1.1/" xmlns:ns3="http://www.i2b2.org/xsd/cell/crc/pdo/1.1/" xmlns:ns2="http://www.i2b2.org/xsd/hive/pdo/1.1/" xmlns:ns5="http://www.i2b2.org/xsd/hive/plugin/" xmlns:ns6="http://www.i2b2.org/xsd/hive/msg/1.1/" xmlns:ns8="http://www.i2b2.org/xsd/cell/crc/psm/querydefinition/1.1/">\n'+
'{{{query_def}}}\n'+
'				</ns5:plugin_drag_drop> \n'+
'			</work_xml>\n';
// ================================================================================================== //


//URL: Address: http://127.0.0.1:7070/i2b2/rest/WorkplaceService/renameChild
i2b2.WORK.cfg.msgs.renameChild = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>\n'+
'<ns3:request xmlns:ns3="http://www.i2b2.org/xsd/hive/msg/1.1/" '+
'	xmlns:ns4="http://www.i2b2.org/xsd/cell/work/1.1/" '+
'	xmlns:ns2="http://www.i2b2.org/xsd/hive/plugin/" '+
'	xmlns:ns5="http://www.i2b2.org/xsd/cell/ont/1.1/">\n'+
'	<message_header>\n'+
'		{{{proxy_info}}}\n'+
'		<i2b2_version_compatible>1.1</i2b2_version_compatible>\n'+
'		<sending_application>\n'+
'			<application_name>i2b2 Ontology</application_name>\n'+
'			<application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'		</sending_application>\n'+
'		<sending_facility>\n'+
'			<facility_name>i2b2 Hive</facility_name>\n'+
'		</sending_facility>\n'+
'		<receiving_application>\n'+
'			<application_name>1.1</application_name>\n'+
'			<application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'		</receiving_application>\n'+
'		<receiving_facility>\n'+
'			<facility_name>i2b2 Hive</facility_name>\n'+
'		</receiving_facility>\n'+
'		<datetime_of_message>{{{header_msg_datetime}}}</datetime_of_message>\n'+
'		<security>\n'+
'			<domain>{{{sec_domain}}}</domain>\n'+
'			<username>{{{sec_user}}}</username>\n'+
'			{{{sec_pass_node}}}\n'+
'		</security>\n'+
'		<message_control_id>\n'+
'			<message_num>{{{header_msg_id}}}</message_num>\n'+
'			<instance_num>0</instance_num>\n'+
'		</message_control_id>\n'+
'		<processing_id>\n'+
'			<processing_id>P</processing_id>\n'+
'			<processing_mode>I</processing_mode>\n'+
'		</processing_id>\n'+
'		<accept_acknowledgement_type>AL</accept_acknowledgement_type>\n'+
'		<application_acknowledgement_type>AL</application_acknowledgement_type>\n'+
'		<country_code>US</country_code>\n'+
'		<project_id>{{{sec_project}}}</project_id>\n'+
'	</message_header>\n'+
'	<request_header>\n'+
'		<result_waittime_ms>{{{result_wait_time}}}000</result_waittime_ms>\n'+
'	</request_header>\n'+
'	<message_body>\n'+
'		<ns4:rename_child>\n'+
'			<node>{{{rename_target_id}}}</node>\n'+
'			<name>{{{rename_text}}}</name>\n'+
'		</ns4:rename_child>\n'+
'	</message_body>\n'+
'</ns3:request>\n';
// ================================================================================================== //
i2b2.WORK.ajax._addFunctionCall("renameChild","{{{URL}}}renameChild", i2b2.WORK.cfg.msgs.renameChild);




// URL: Address: http://127.0.0.1:7070/i2b2/rest/WorkplaceService/annotateChild
i2b2.WORK.cfg.msgs.annotateChild = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>\n'+
'<ns3:request xmlns:ns3="http://www.i2b2.org/xsd/hive/msg/1.1/" '+
'	xmlns:ns4="http://www.i2b2.org/xsd/cell/work/1.1/" '+
'	xmlns:ns2="http://www.i2b2.org/xsd/hive/plugin/" '+
'	xmlns:ns5="http://www.i2b2.org/xsd/cell/ont/1.1/">\n'+
'	<message_header>\n'+
'		{{{proxy_info}}}\n'+
'		<i2b2_version_compatible>1.1</i2b2_version_compatible>\n'+
'		<sending_application>\n'+
'			<application_name>i2b2 Ontology</application_name>\n'+
'			<application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'		</sending_application>\n'+
'		<sending_facility>\n'+
'			<facility_name>i2b2 Hive</facility_name>\n'+
'		</sending_facility>\n'+
'		<receiving_application>\n'+
'			<application_name>1.1</application_name>\n'+
'			<application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'		</receiving_application>\n'+
'		<receiving_facility>\n'+
'			<facility_name>i2b2 Hive</facility_name>\n'+
'		</receiving_facility>\n'+
'		<datetime_of_message>{{{header_msg_datetime}}}</datetime_of_message>\n'+
'		<security>\n'+
'			<domain>{{{sec_domain}}}</domain>\n'+
'			<username>{{{sec_user}}}</username>\n'+
'			{{{sec_pass_node}}}\n'+
'		</security>\n'+
'		<message_control_id>\n'+
'			<message_num>{{{header_msg_id}}}</message_num>\n'+
'			<instance_num>0</instance_num>\n'+
'		</message_control_id>\n'+
'		<processing_id>\n'+
'			<processing_id>P</processing_id>\n'+
'			<processing_mode>I</processing_mode>\n'+
'		</processing_id>\n'+
'		<accept_acknowledgement_type>AL</accept_acknowledgement_type>\n'+
'		<application_acknowledgement_type>AL</application_acknowledgement_type>\n'+
'		<country_code>US</country_code>\n'+
'		<project_id>{{{sec_project}}}</project_id>\n'+
'	</message_header>\n'+
'	<request_header>\n'+
'		<result_waittime_ms>{{{result_wait_time}}}000</result_waittime_ms>\n'+
'	</request_header>\n'+
'	<message_body>\n'+
'		<ns4:annotate_child>\n'+
'			<node>{{{annotation_target_id}}}</node>\n'+
'			<tooltip>{{{annotation_text}}}</tooltip>\n'+
'		</ns4:annotate_child>\n'+
'	</message_body>\n'+
'</ns3:request>';
// ================================================================================================== //
i2b2.WORK.ajax._addFunctionCall("annotateChild","{{{URL}}}annotateChild", i2b2.WORK.cfg.msgs.annotateChild);



// URL: Address: http://127.0.0.1:7070/i2b2/rest/WorkplaceService/deleteChild
i2b2.WORK.cfg.msgs.deleteChild = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>\n'+
'<ns3:request xmlns:ns3="http://www.i2b2.org/xsd/hive/msg/1.1/" '+
'	xmlns:ns4="http://www.i2b2.org/xsd/cell/work/1.1/" '+
'	xmlns:ns2="http://www.i2b2.org/xsd/hive/plugin/" '+
'	xmlns:ns5="http://www.i2b2.org/xsd/cell/ont/1.1/">\n'+
'	<message_header>\n'+
'		{{{proxy_info}}}\n'+
'		<i2b2_version_compatible>1.1</i2b2_version_compatible>\n'+
'		<sending_application>\n'+
'			<application_name>i2b2 Ontology</application_name>\n'+
'			<application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'		</sending_application>\n'+
'		<sending_facility>\n'+
'			<facility_name>i2b2 Hive</facility_name>\n'+
'		</sending_facility>\n'+
'		<receiving_application>\n'+
'			<application_name>1.1</application_name>\n'+
'			<application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'		</receiving_application>\n'+
'		<receiving_facility>\n'+
'			<facility_name>i2b2 Hive</facility_name>\n'+
'		</receiving_facility>\n'+
'		<datetime_of_message>{{{header_msg_datetime}}}</datetime_of_message>\n'+
'		<security>\n'+
'			<domain>{{{sec_domain}}}</domain>\n'+
'			<username>{{{sec_user}}}</username>\n'+
'			{{{sec_pass_node}}}\n'+
'		</security>\n'+
'		<message_control_id>\n'+
'			<message_num>{{{header_msg_id}}}</message_num>\n'+
'			<instance_num>0</instance_num>\n'+
'		</message_control_id>\n'+
'		<processing_id>\n'+
'			<processing_id>P</processing_id>\n'+
'			<processing_mode>I</processing_mode>\n'+
'		</processing_id>\n'+
'		<accept_acknowledgement_type>AL</accept_acknowledgement_type>\n'+
'		<application_acknowledgement_type>AL</application_acknowledgement_type>\n'+
'		<country_code>US</country_code>\n'+
'		<project_id>{{{sec_project}}}</project_id>\n'+
'	</message_header>\n'+
'	<request_header>\n'+
'		<result_waittime_ms>{{{result_wait_time}}}000</result_waittime_ms>\n'+
'	</request_header>\n'+
'	<message_body>\n'+
'		<ns4:delete_child>\n'+
'			<node>{{{delete_target_id}}}</node>\n'+
'		</ns4:delete_child>\n'+
'	</message_body>\n'+
'</ns3:request>\n';
// ================================================================================================== //
i2b2.WORK.ajax._addFunctionCall("deleteChild","{{{URL}}}deleteChild", i2b2.WORK.cfg.msgs.deleteChild);
