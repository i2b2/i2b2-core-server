/**
 * @projectDescription	Messages used by the ONT cell communicator object.
 * @inherits 	i2b2.ONT.cfg
 * @namespace	i2b2.ONT.cfg.msgs
 * @author		Nick Benik, Griffin Weber MD PhD
 * @version 	1.3
 * ----------------------------------------------------------------------------------------
 * updated 9-15-08: RC4 launch [Nick Benik] 
 */


// create the communicator Object
i2b2.ONT.ajax = i2b2.hive.communicatorFactory("ONT");
i2b2.ONT.cfg.msgs = {};
i2b2.ONT.cfg.parsers = {};
i2b2.ONT.cfg.parsers.ExtractConcepts = function(){
	if (!this.error) {
		this.model = [];		
		// extract records from XML msg
		var c = this.refXML.getElementsByTagName('concept');
		for(var i=0; i<1*c.length; i++) {
			var o = new Object;
			o.xmlOrig = c[i];
			o.name = i2b2.h.getXNodeVal(c[i],'name');
			o.hasChildren = i2b2.h.getXNodeVal(c[i],'visualattributes').substring(0,2);
			o.level = i2b2.h.getXNodeVal(c[i],'level');
			o.key = i2b2.h.getXNodeVal(c[i],'key');
			o.tooltip = i2b2.h.getXNodeVal(c[i],'tooltip');
			o.synonym = i2b2.h.getXNodeVal(c[i],'synonym_cd');
			o.visual_attributes = i2b2.h.getXNodeVal(c[i],'visualattributes');
			o.totalnum = i2b2.h.getXNodeVal(c[i],'totalnum');
			o.basecode = i2b2.h.getXNodeVal(c[i],'basecode');;
			o.fact_table_column = i2b2.h.getXNodeVal(c[i],'facttablecolumn');
			o.table_name = i2b2.h.getXNodeVal(c[i],'tablename');
			o.column_name = i2b2.h.getXNodeVal(c[i],'columnname');
			o.column_datatype = i2b2.h.getXNodeVal(c[i],'columndatatype');
			o.operator = i2b2.h.getXNodeVal(c[i],'operator');
			o.dim_code = i2b2.h.getXNodeVal(c[i],'dimcode');
			// encapsulate the data node into SDX package
			var sdxDataPack = i2b2.sdx.Master.EncapsulateData('CONCPT',o);
			// save extracted info
			this.model.push(sdxDataPack);
		}
	} else {
		this.model = false;
		console.error("[ExtractConcepts] Could not parse() data!");
	}
	return this;
};


// ================================================================================================== //
i2b2.ONT.cfg.msgs.GetChildConcepts = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>\n'+
'<ns3:request xmlns:ns3="http://www.i2b2.org/xsd/hive/msg/1.1/" xmlns:ns4="http://www.i2b2.org/xsd/cell/ont/1.1/" xmlns:ns2="http://www.i2b2.org/xsd/hive/plugin/">\n'+
'    <message_header>\n'+
'        {{{proxy_info}}}'+
'        <i2b2_version_compatible>1.1</i2b2_version_compatible>\n'+
'        <hl7_version_compatible>2.4</hl7_version_compatible>\n'+
'        <sending_application>\n'+
'            <application_name>i2b2 Ontology </application_name>\n'+
'            <application_version>{{{version}}}</application_version>\n'+
'        </sending_application>\n'+
'        <sending_facility>\n'+
'            <facility_name>i2b2 Hive</facility_name>\n'+
'        </sending_facility>\n'+
'        <receiving_application>\n'+
'            <application_name>Ontology Cell</application_name>\n'+
'            <application_version>{{{version}}}</application_version>\n'+
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
'        <ns4:get_children blob="false" type="core" {{{ont_max_records}}} synonyms="{{{ont_synonym_records}}}" hiddens="{{{ont_hidden_records}}}">\n'+
'            <parent>{{{concept_key_value}}}</parent>\n'+
'        </ns4:get_children>\n'+
'    </message_body>\n'+
'</ns3:request>';
i2b2.ONT.ajax._addFunctionCall(	"GetChildConcepts",
								"{{{URL}}}getChildren",
								i2b2.ONT.cfg.msgs.GetChildConcepts,
								null,
								i2b2.ONT.cfg.parsers.ExtractConcepts);




// ================================================================================================== //
i2b2.ONT.cfg.msgs.GetChildModifiers = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>\n'+
'<ns3:request xmlns:ns3="http://www.i2b2.org/xsd/hive/msg/1.1/" xmlns:ns4="http://www.i2b2.org/xsd/cell/ont/1.1/" xmlns:ns2="http://www.i2b2.org/xsd/hive/plugin/">\n'+
'    <message_header>\n'+
'        {{{proxy_info}}}'+
'        <i2b2_version_compatible>1.1</i2b2_version_compatible>\n'+
'        <hl7_version_compatible>2.4</hl7_version_compatible>\n'+
'        <sending_application>\n'+
'            <application_name>i2b2 Ontology </application_name>\n'+
'            <application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'        </sending_application>\n'+
'        <sending_facility>\n'+
'            <facility_name>i2b2 Hive</facility_name>\n'+
'        </sending_facility>\n'+
'        <receiving_application>\n'+
'            <application_name>Ontology Cell</application_name>\n'+
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
'        <ns4:get_modifier_children blob="false" type="limited" {{{ont_max_records}}} synonyms="{{{ont_synonym_records}}}" hiddens="{{{ont_hidden_records}}}">\n'+
'            <parent>{{{modifier_key_value}}}</parent>\n'+
'			<applied_path>{{{modifier_applied_path}}}</applied_path>\n'+
'			<applied_concept>{{{modifier_applied_concept}}}</applied_concept>\n'+
'        </ns4:get_modifier_children>\n'+
'    </message_body>\n'+
'</ns3:request>';
i2b2.ONT.ajax._addFunctionCall(	"GetChildModifiers",
								"{{{URL}}}getModifierChildren",
								i2b2.ONT.cfg.msgs.GetChildModifiers,
								null,
								i2b2.ONT.cfg.parsers.GetModifiers);





// ================================================================================================== //
i2b2.ONT.cfg.msgs.GetCategories = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>\n'+
'<ns3:request xmlns:ns3="http://www.i2b2.org/xsd/hive/msg/1.1/" xmlns:ns4="http://www.i2b2.org/xsd/cell/ont/1.1/" xmlns:ns2="http://www.i2b2.org/xsd/hive/plugin/">\n'+
'    <message_header>\n'+
'        {{{proxy_info}}}\n'+
'        <i2b2_version_compatible>1.1</i2b2_version_compatible>\n'+
'        <hl7_version_compatible>2.4</hl7_version_compatible>\n'+
'        <sending_application>\n'+
'            <application_name>i2b2 Ontology</application_name>\n'+
'            <application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'        </sending_application>\n'+
'        <sending_facility>\n'+
'            <facility_name>i2b2 Hive</facility_name>\n'+
'        </sending_facility>\n'+
'        <receiving_application>\n'+
'            <application_name>Ontology Cell</application_name>\n'+
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
'        <ns4:get_categories  synonyms="{{{ont_synonym_records}}}" hiddens="{{{ont_hidden_records}}}" type="core"/>\n'+
'    </message_body>\n'+
'</ns3:request>';
/*
i2b2.ONT.cfg.parsers.GetCategories = function(){
	if (!this.error) {
		this.model = [];		
		// extract records from XML msg
		var c = this.refXML.getElementsByTagName('concept');
		for(var i=0; i<1*c.length; i++) {
			var o = new Object;
			o.xmlOrig = c[i];
			o.level = i2b2.h.getXNodeVal(c[i],'level');
			o.key = i2b2.h.getXNodeVal(c[i],'key');
			o.name = i2b2.h.getXNodeVal(c[i],'name');
			o.total_num = i2b2.h.getXNodeVal(c[i],'totalnum');
			// save extracted info
			this.model.push(0);
		}
	} else {
		this.model = false;
		console.error("[GetCategories] Could not parse() data!");
	}
	return this;
};
*/
i2b2.ONT.ajax._addFunctionCall(	"GetCategories",
								"{{{URL}}}getCategories",
								i2b2.ONT.cfg.msgs.GetCategories,
								null,
								i2b2.ONT.cfg.parsers.ExtractConcepts);


// ================================================================================================== //

i2b2.ONT.cfg.msgs.GetModifiers = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>\n'+
'<ns3:request xmlns:ns3="http://www.i2b2.org/xsd/hive/msg/1.1/" xmlns:ns4="http://www.i2b2.org/xsd/cell/ont/1.1/" xmlns:ns2="http://www.i2b2.org/xsd/hive/plugin/">\n'+
'    <message_header>\n'+
'        {{{proxy_info}}}\n'+
'        <i2b2_version_compatible>1.1</i2b2_version_compatible>\n'+
'        <hl7_version_compatible>2.4</hl7_version_compatible>\n'+
'        <sending_application>\n'+
'            <application_name>i2b2 Ontology</application_name>\n'+
'            <application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'        </sending_application>\n'+
'        <sending_facility>\n'+
'            <facility_name>i2b2 Hive</facility_name>\n'+
'        </sending_facility>\n'+
'        <receiving_application>\n'+
'            <application_name>Ontology Cell</application_name>\n'+
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
'       <message_control_id>\n'+
'           <message_num>{{{header_msg_id}}}</message_num>\n'+
'           <instance_num>0</instance_num>\n'+
'       </message_control_id>\n'+
'       <processing_id>\n'+
'           <processing_id>P</processing_id>\n'+
'           <processing_mode>I</processing_mode>\n'+
'       </processing_id>\n'+
'       <accept_acknowledgement_type>AL</accept_acknowledgement_type>\n'+
'       <application_acknowledgement_type>AL</application_acknowledgement_type>\n'+
'       <country_code>US</country_code>\n'+
'       <project_id>{{{sec_project}}}</project_id>\n'+
'   </message_header>\n'+
'   <request_header>\n'+
'       <result_waittime_ms>{{{result_wait_time}}}000</result_waittime_ms>\n'+
'   </request_header>\n'+
'   <message_body>\n'+
'        <ns4:get_modifiers synonyms="{{{ont_synonym_records}}}" hiddens="{{{ont_hidden_records}}}"> \n'+
'            <self>{{{concept_key_value}}}</self> \n'+
'        </ns4:get_modifiers>\n'+
'   </message_body>\n'+
'</ns3:request>';
i2b2.ONT.cfg.parsers.GetModifiers = function(){
	if (!this.error) {
		this.model = [];		
		// extract records from XML msg
		var c = this.refXML.getElementsByTagName('modifier');
		for(var i=0; i<1*c.length; i++) {
			var o = new Object;
			o.xmlOrig = c[i];
			o.level = i2b2.h.getXNodeVal(c[i],'level');
			o.basecode = i2b2.h.getXNodeVal(c[i],'basecode');
			o.name = i2b2.h.getXNodeVal(c[i],'name');
			o.total_num = i2b2.h.getXNodeVal(c[i],'totalnum');
			// save extracted info
			this.model.push(o);
		}
	} else {
		this.model = false;
		console.error("[GetModifiers] Could not parse() data!");
		return null;
	}
	return this;
};
i2b2.ONT.ajax._addFunctionCall(	"GetModifiers",
								"{{{URL}}}getModifiers",
								i2b2.ONT.cfg.msgs.GetModifiers,
								null,
								i2b2.ONT.cfg.parsers.GetModifiers);


// ================================================================================================== //

i2b2.ONT.cfg.msgs.GetSchemes = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>\n'+
'<ns3:request xmlns:ns3="http://www.i2b2.org/xsd/hive/msg/1.1/" xmlns:ns4="http://www.i2b2.org/xsd/cell/ont/1.1/" xmlns:ns2="http://www.i2b2.org/xsd/hive/plugin/">\n'+
'    <message_header>\n'+
'        {{{proxy_info}}}\n'+
'        <i2b2_version_compatible>1.1</i2b2_version_compatible>\n'+
'        <hl7_version_compatible>2.4</hl7_version_compatible>\n'+
'        <sending_application>\n'+
'            <application_name>i2b2 Ontology</application_name>\n'+
'            <application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'        </sending_application>\n'+
'        <sending_facility>\n'+
'            <facility_name>i2b2 Hive</facility_name>\n'+
'        </sending_facility>\n'+
'        <receiving_application>\n'+
'            <application_name>Ontology Cell</application_name>\n'+
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
'       <message_control_id>\n'+
'           <message_num>{{{header_msg_id}}}</message_num>\n'+
'           <instance_num>0</instance_num>\n'+
'       </message_control_id>\n'+
'       <processing_id>\n'+
'           <processing_id>P</processing_id>\n'+
'           <processing_mode>I</processing_mode>\n'+
'       </processing_id>\n'+
'       <accept_acknowledgement_type>AL</accept_acknowledgement_type>\n'+
'       <application_acknowledgement_type>AL</application_acknowledgement_type>\n'+
'       <country_code>US</country_code>\n'+
'       <project_id>{{{sec_project}}}</project_id>\n'+
'   </message_header>\n'+
'   <request_header>\n'+
'       <result_waittime_ms>{{{result_wait_time}}}000</result_waittime_ms>\n'+
'   </request_header>\n'+
'   <message_body>\n'+
'       <ns4:get_schemes type="default"/>\n'+
'   </message_body>\n'+
'</ns3:request>';
i2b2.ONT.cfg.parsers.GetSchemes = function(){
	if (!this.error) {
		this.model = [];		
		// extract records from XML msg
		var c = this.refXML.getElementsByTagName('concept');
		for(var i=0; i<1*c.length; i++) {
			var o = new Object;
			o.xmlOrig = c[i];
			o.level = i2b2.h.getXNodeVal(c[i],'level');
			o.key = i2b2.h.getXNodeVal(c[i],'key');
			o.name = i2b2.h.getXNodeVal(c[i],'name');
			o.total_num = i2b2.h.getXNodeVal(c[i],'totalnum');
			// save extracted info
			this.model.push(o);
		}
	} else {
		this.model = false;
		console.error("[GetSchemes] Could not parse() data!");
	}
	return this;
};
i2b2.ONT.ajax._addFunctionCall(	"GetSchemes",
								"{{{URL}}}getSchemes",
								i2b2.ONT.cfg.msgs.GetSchemes,
								null,
								i2b2.ONT.cfg.parsers.GetSchemes);


// ================================================================================================== //
i2b2.ONT.cfg.msgs.GetNameInfo = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>\n'+
'<ns3:request xmlns:ns3="http://www.i2b2.org/xsd/hive/msg/1.1/" xmlns:ns4="http://www.i2b2.org/xsd/cell/ont/1.1/" xmlns:ns2="http://www.i2b2.org/xsd/hive/plugin/">\n'+
'    <message_header>\n'+
'        {{{proxy_info}}}\n'+
'        <i2b2_version_compatible>1.1</i2b2_version_compatible>\n'+
'        <hl7_version_compatible>2.4</hl7_version_compatible>\n'+
'        <sending_application>\n'+
'            <application_name>i2b2 Ontology</application_name>\n'+
'            <application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'        </sending_application>\n'+
'        <sending_facility>\n'+
'            <facility_name>i2b2 Hive</facility_name>\n'+
'        </sending_facility>\n'+
'        <receiving_application>\n'+
'            <application_name>Ontology Cell</application_name>\n'+
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
'        <ns4:get_name_info blob="true" type="core" {{{ont_max_records}}} hiddens="{{{ont_hidden_records}}}" synonyms="{{{ont_synonym_records}}}" category="{{{ont_category}}}">\n'+
'            <match_str strategy="{{{ont_search_strategy}}}">{{{ont_search_string}}}</match_str>\n'+
'        </ns4:get_name_info>\n'+
'    </message_body>\n'+
'</ns3:request>';
i2b2.ONT.ajax._addFunctionCall(	"GetNameInfo",
								"{{{URL}}}getNameInfo", 
								i2b2.ONT.cfg.msgs.GetNameInfo,
								null,
								i2b2.ONT.cfg.parsers.ExtractConcepts);


// ================================================================================================== //
i2b2.ONT.cfg.msgs.GetModifierCodeInfo = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>\n'+
'<ns3:request xmlns:ns3="http://www.i2b2.org/xsd/hive/msg/1.1/" xmlns:ns4="http://www.i2b2.org/xsd/cell/ont/1.1/" xmlns:ns2="http://www.i2b2.org/xsd/hive/plugin/">\n'+
'    <message_header>\n'+
'        {{{proxy_info}}}\n'+
'        <i2b2_version_compatible>1.1</i2b2_version_compatible>\n'+
'        <hl7_version_compatible>2.4</hl7_version_compatible>\n'+
'        <sending_application>\n'+
'            <application_name>i2b2 Ontology</application_name>\n'+
'            <application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'        </sending_application>\n'+
'        <sending_facility>\n'+
'            <facility_name>i2b2 Hive</facility_name>\n'+
'        </sending_facility>\n'+
'        <receiving_application>\n'+
'            <application_name>Ontology Cell</application_name>\n'+
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
'        <ns4:get_modifier_code_info  blob="false" {{{ont_max_records}}} type="core" synonyms="{{{ont_synonym_records}}}" hiddens="{{{ont_hidden_records}}}">\n'+
'			<self>{{{modifier_key_value}}}</self>\n'+
'            <match_str strategy="{{{ont_search_strategy}}}">{{{ont_search_string}}}</match_str>\n'+
'        </ns4:get_modifier_code_info >\n'+
'    </message_body>\n'+
'</ns3:request>';
i2b2.ONT.ajax._addFunctionCall(	"GetModifierCodeInfo",
								"{{{URL}}}getModifierCodeInfo", 
								i2b2.ONT.cfg.msgs.GetModifierCodeInfo,
								null,
								i2b2.ONT.cfg.parsers.GetModifiers);

// ================================================================================================== //
i2b2.ONT.cfg.msgs.GetModifierNameInfo = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>\n'+
'<ns3:request xmlns:ns3="http://www.i2b2.org/xsd/hive/msg/1.1/" xmlns:ns4="http://www.i2b2.org/xsd/cell/ont/1.1/" xmlns:ns2="http://www.i2b2.org/xsd/hive/plugin/">\n'+
'    <message_header>\n'+
'        {{{proxy_info}}}\n'+
'        <i2b2_version_compatible>1.1</i2b2_version_compatible>\n'+
'        <hl7_version_compatible>2.4</hl7_version_compatible>\n'+
'        <sending_application>\n'+
'            <application_name>i2b2 Ontology</application_name>\n'+
'            <application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'        </sending_application>\n'+
'        <sending_facility>\n'+
'            <facility_name>i2b2 Hive</facility_name>\n'+
'        </sending_facility>\n'+
'        <receiving_application>\n'+
'            <application_name>Ontology Cell</application_name>\n'+
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
'        <ns4:get_modifier_name_info  blob="false" {{{ont_max_records}}} type="core" synonyms="{{{ont_synonym_records}}}" hiddens="{{{ont_hidden_records}}}">\n'+
'			<self>{{{modifier_key_value}}}</self>\n'+
'            <match_str strategy="{{{ont_search_strategy}}}">{{{ont_search_string}}}</match_str>\n'+
'        </ns4:get_modifier_name_info >\n'+
'    </message_body>\n'+
'</ns3:request>';
i2b2.ONT.ajax._addFunctionCall(	"GetModifierNameInfo",
								"{{{URL}}}getModifierNameInfo", 
								i2b2.ONT.cfg.msgs.GetModifierNameInfo,
								null,
								i2b2.ONT.cfg.parsers.GetModifiers);


// ================================================================================================== //
i2b2.ONT.cfg.msgs.GetCodeInfo = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>\n'+
'<ns3:request xmlns:ns3="http://www.i2b2.org/xsd/hive/msg/1.1/" xmlns:ns4="http://www.i2b2.org/xsd/cell/ont/1.1/" xmlns:ns2="http://www.i2b2.org/xsd/hive/plugin/">\n'+
'    <message_header>\n'+
'        {{{proxy_info}}}\n'+
'        <i2b2_version_compatible>1.1</i2b2_version_compatible>\n'+
'        <hl7_version_compatible>2.4</hl7_version_compatible>\n'+
'        <sending_application>\n'+
'            <application_name>i2b2 Ontology</application_name>\n'+
'            <application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'        </sending_application>\n'+
'        <sending_facility>\n'+
'            <facility_name>i2b2 Hive</facility_name>\n'+
'        </sending_facility>\n'+
'        <receiving_application>\n'+
'            <application_name>Ontology Cell</application_name>\n'+
'            <application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'        </receiving_application>\n'+
'        <receiving_facility>\n'+
'            <facility_name>i2b2 Hive</facility_name>\n'+
'        </receiving_facility>\n'+
'        <datetime_of_message>{{{header_msg_datetime}}}</datetime_of_message>\n'+
'        <security>\n'+
'            <domain>{{{sec_domain}}}</domain>\n'+
'            <username>{{{sec_user}}}</username>\n'+
'            {{{sec_pass_node}}}\n'+
'        </security>\n'+
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
'        <ns4:get_code_info blob="true" type="core" {{{ont_max_records}}} synonyms="{{{ont_synonym_records}}}" hiddens="{{{ont_hidden_records}}}">\n'+
'            <match_str strategy="{{{ont_search_strategy}}}">{{{ont_search_coding}}}{{{ont_search_string}}}</match_str>\n'+
'        </ns4:get_code_info>\n'+
'    </message_body>\n'+
'</ns3:request>';
i2b2.ONT.ajax._addFunctionCall(	"GetCodeInfo",
								"{{{URL}}}getCodeInfo", 
								i2b2.ONT.cfg.msgs.GetCodeInfo, 
								null, 
								i2b2.ONT.cfg.parsers.ExtractConcepts);


// ================================================================================================== //=
i2b2.ONT.cfg.msgs.GetTermInfo = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>\n'+
'<ns3:request xmlns:ns3="http://www.i2b2.org/xsd/hive/msg/1.1/" xmlns:ns4="http://www.i2b2.org/xsd/cell/ont/1.1/" xmlns:ns2="http://www.i2b2.org/xsd/hive/plugin/">\n'+
'	<message_header>\n'+
'		{{{proxy_info}}}\n'+
'		<i2b2_version_compatible>1.1</i2b2_version_compatible>\n'+
'		<hl7_version_compatible>2.4</hl7_version_compatible>\n'+
'		<sending_application>\n'+
'			<application_name>i2b2 Ontology</application_name>\n'+
'			<application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'		</sending_application>\n'+
'		<sending_facility>\n'+
'			<facility_name>i2b2 Hive</facility_name>\n'+
'		</sending_facility>\n'+
'		<receiving_application>\n'+
'			<application_name>Ontology Cell</application_name>\n'+
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
'		<ns4:get_term_info blob="true" type="core" {{{ont_max_records}}} synonyms="{{{ont_synonym_records}}}" hiddens="{{{ont_hidden_records}}}">\n'+
'			<self>{{{concept_key_value}}}</self>\n'+
'		</ns4:get_term_info>\n'+
'	</message_body>\n'+
'</ns3:request>';
i2b2.ONT.ajax._addFunctionCall(	"GetTermInfo",
								"{{{URL}}}getTermInfo",
								i2b2.ONT.cfg.msgs.GetTermInfo,
								null,
								i2b2.ONT.cfg.parsers.ExtractConcepts);

// ================================================================================================== //=
i2b2.ONT.cfg.msgs.GetModifierInfo = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>\n'+
'<ns3:request xmlns:ns3="http://www.i2b2.org/xsd/hive/msg/1.1/" xmlns:ns4="http://www.i2b2.org/xsd/cell/ont/1.1/" xmlns:ns2="http://www.i2b2.org/xsd/hive/plugin/">\n'+
'	<message_header>\n'+
'		{{{proxy_info}}}\n'+
'		<i2b2_version_compatible>1.1</i2b2_version_compatible>\n'+
'		<hl7_version_compatible>2.4</hl7_version_compatible>\n'+
'		<sending_application>\n'+
'			<application_name>i2b2 Ontology</application_name>\n'+
'			<application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'		</sending_application>\n'+
'		<sending_facility>\n'+
'			<facility_name>i2b2 Hive</facility_name>\n'+
'		</sending_facility>\n'+
'		<receiving_application>\n'+
'			<application_name>Ontology Cell</application_name>\n'+
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
'		<ns4:get_modifier_info blob="true" type="core" {{{ont_max_records}}} synonyms="{{{ont_synonym_records}}}" hiddens="{{{ont_hidden_records}}}">\n'+
'			<self>{{{modifier_key_value}}}</self>\n'+
'			<applied_path>{{{modifier_applied_path}}}</applied_path>\n'+
'		</ns4:get_modifier_info>\n'+
'	</message_body>\n'+
'</ns3:request>';
i2b2.ONT.ajax._addFunctionCall(	"GetModifierInfo",
								"{{{URL}}}getModifierInfo",
								i2b2.ONT.cfg.msgs.GetModifierInfo,
								null,
								i2b2.ONT.cfg.parsers.GetModifiers);
