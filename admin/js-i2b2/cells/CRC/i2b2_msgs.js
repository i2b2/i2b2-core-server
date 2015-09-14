/**
 * @projectDescription	Messages used by the CRC cell communicator object
 * @inherits 	i2b2.CRC.cfg
 * @namespace	i2b2.CRC.cfg.msgs
 * @author		Nick Benik, Griffin Weber MD PhD
 * @version 	1.3
 * ----------------------------------------------------------------------------------------
 * updated 9-15-08: RC4 launch [Nick Benik] 
 */

// create the communicator Object
i2b2.CRC.ajax = i2b2.hive.communicatorFactory("CRC");

// create namespaces to hold all the communicator messages and parsing routines
i2b2.CRC.cfg.msgs = {};
i2b2.CRC.cfg.parsers = {};


// ================================================================================================== //
i2b2.CRC.cfg.msgs.getQueryMasterList_fromUserId = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>\n'+
'<ns6:request xmlns:ns4="http://www.i2b2.org/xsd/cell/crc/psm/1.1/" xmlns:ns7="http://www.i2b2.org/xsd/cell/crc/psm/querydefinition/1.1/" xmlns:ns3="http://www.i2b2.org/xsd/cell/crc/pdo/1.1/" xmlns:ns5="http://www.i2b2.org/xsd/hive/plugin/" xmlns:ns2="http://www.i2b2.org/xsd/hive/pdo/1.1/" xmlns:ns6="http://www.i2b2.org/xsd/hive/msg/1.1/">\n'+
'	<message_header>\n'+
'		{{{proxy_info}}}\n'+
'		<sending_application>\n'+
'			<application_name>i2b2_QueryTool</application_name>\n'+
'			<application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'		</sending_application>\n'+
'		<sending_facility>\n'+
'			<facility_name>PHS</facility_name>\n'+
'		</sending_facility>\n'+
'		<receiving_application>\n'+
'			<application_name>i2b2_DataRepositoryCell</application_name>\n'+
'			<application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'		</receiving_application>\n'+
'		<receiving_facility>\n'+
'			<facility_name>PHS</facility_name>\n'+
'		</receiving_facility>\n'+
'		<security>\n'+
'			<domain>{{{sec_domain}}}</domain>\n'+
'			<username>{{{sec_user}}}</username>\n'+
'			{{{sec_pass_node}}}\n'+
'		</security>\n'+
'		<message_type>\n'+
'			<message_code>Q04</message_code>\n'+
'			<event_type>EQQ</event_type>\n'+
'		</message_type>\n'+
'		<message_control_id>\n'+
'			<message_num>{{{header_msg_id}}}</message_num>\n'+
'			<instance_num>0</instance_num>\n'+
'		</message_control_id>\n'+
'		<processing_id>\n'+
'			<processing_id>P</processing_id>\n'+
'			<processing_mode>I</processing_mode>\n'+
'		</processing_id>\n'+
'		<accept_acknowledgement_type>AL</accept_acknowledgement_type>\n'+
'		<project_id>{{{sec_project}}}</project_id>\n'+
'		<country_code>US</country_code>\n'+
'	</message_header>\n'+
'	<request_header>\n'+
'		<result_waittime_ms>{{{result_wait_time}}}000</result_waittime_ms>\n'+
'	</request_header>\n'+
'	<message_body>\n'+
'		<ns4:psmheader>\n'+
'			<user login="{{{sec_user}}}">{{{sec_user}}}</user>\n'+
'			<patient_set_limit>0</patient_set_limit>\n'+
'			<estimated_time>0</estimated_time>\n'+
'			<request_type>{{{crc_user_type}}}</request_type>\n'+
'		</ns4:psmheader>\n'+
'		<ns4:request xsi:type="ns4:user_requestType" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">\n'+
'			<user_id>{{{sec_user}}}</user_id>\n'+
'			<group_id>{{{sec_project}}}</group_id>\n'+
'			<fetch_size>{{{crc_max_records}}}</fetch_size>\n'+
'		</ns4:request>\n'+
'	</message_body>\n'+
'</ns6:request>\n';
i2b2.CRC.cfg.parsers.getQueryMasterList_fromUserId = function() {
	if (!this.error) {
		this.model = [];		
		var qm = this.refXML.getElementsByTagName('query_master');
		for(var i=0; i<1*qm.length; i++) {
			var o = new Object;
			o.xmlOrig = qm[i];
			o.id = i2b2.h.getXNodeVal(qm[i],'query_master_id');
			o.name = i2b2.h.getXNodeVal(qm[i],'name');
			o.userid = i2b2.h.getXNodeVal(qm[i],'user_id');
			o.group = i2b2.h.getXNodeVal(qm[i],'group_id');
			o.created = i2b2.h.getXNodeVal(qm[i],'create_date');
			var dStr = '';
			var d = o.created.match(/^[0-9\-]*/).toString();
			if (d) {
				d = d.replace(/-/g,'/');
				d = new Date(Date.parse(d));
				if (d) {
					dStr = ' [' + (d.getMonth()+1) + '-' + d.getDate() + '-' + d.getFullYear().toString() + ']';
				}
			}
			o.name += dStr + ' ['+o.userid+']';
			// encapsulate into an SDX package
			var sdxDataPack = i2b2.sdx.Master.EncapsulateData('QM',o);
			this.model.push(sdxDataPack);
		}
	} else {
		this.model = false;
		console.error("[getQueryMasterList_fromUserId] Could not parse() data!");
	}
	return this;
}
i2b2.CRC.ajax._addFunctionCall(	"getQueryMasterList_fromUserId",
								"{{{URL}}}request",
								i2b2.CRC.cfg.msgs.getQueryMasterList_fromUserId,
								null,
								i2b2.CRC.cfg.parsers.getQueryMasterList_fromUserId);


// ================================================================================================== //
i2b2.CRC.cfg.msgs.getNameInfo = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>\n'+
'<ns6:request xmlns:ns4="http://www.i2b2.org/xsd/cell/crc/psm/1.1/" xmlns:ns7="http://www.i2b2.org/xsd/cell/crc/psm/querydefinition/1.1/" xmlns:ns3="http://www.i2b2.org/xsd/cell/crc/pdo/1.1/" xmlns:ns5="http://www.i2b2.org/xsd/hive/plugin/" xmlns:ns2="http://www.i2b2.org/xsd/hive/pdo/1.1/" xmlns:ns6="http://www.i2b2.org/xsd/hive/msg/1.1/">\n'+
'	<message_header>\n'+
'		{{{proxy_info}}}\n'+
'		<sending_application>\n'+
'			<application_name>i2b2_QueryTool</application_name>\n'+
'			<application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'		</sending_application>\n'+
'		<sending_facility>\n'+
'			<facility_name>PHS</facility_name>\n'+
'		</sending_facility>\n'+
'		<receiving_application>\n'+
'			<application_name>i2b2_DataRepositoryCell</application_name>\n'+
'			<application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'		</receiving_application>\n'+
'		<receiving_facility>\n'+
'			<facility_name>PHS</facility_name>\n'+
'		</receiving_facility>\n'+
'		<security>\n'+
'			<domain>{{{sec_domain}}}</domain>\n'+
'			<username>{{{sec_user}}}</username>\n'+
'			{{{sec_pass_node}}}\n'+
'		</security>\n'+
'		<message_type>\n'+
'			<message_code>Q04</message_code>\n'+
'			<event_type>EQQ</event_type>\n'+
'		</message_type>\n'+
'		<message_control_id>\n'+
'			<message_num>{{{header_msg_id}}}</message_num>\n'+
'			<instance_num>0</instance_num>\n'+
'		</message_control_id>\n'+
'		<processing_id>\n'+
'			<processing_id>P</processing_id>\n'+
'			<processing_mode>I</processing_mode>\n'+
'		</processing_id>\n'+
'		<accept_acknowledgement_type>AL</accept_acknowledgement_type>\n'+
'		<project_id>{{{sec_project}}}</project_id>\n'+
'		<country_code>US</country_code>\n'+
'	</message_header>\n'+
'	<request_header>\n'+
'		<result_waittime_ms>{{{result_wait_time}}}000</result_waittime_ms>\n'+
'	</request_header>\n'+
'	<message_body>\n'+
'		<ns4:psmheader>\n'+
'			<user login="{{{sec_user}}}">{{{sec_user}}}</user>\n'+
'			<patient_set_limit>0</patient_set_limit>\n'+
'			<estimated_time>0</estimated_time>\n'+
'			<request_type>{{{crc_user_type}}}</request_type>\n'+
'		</ns4:psmheader>\n'+
'   	<ns4:get_name_info category="{{{crc_find_category}}}" max="{{{crc_max_records}}}">\n'+
'      		<match_str strategy="{{{crc_find_strategy}}}">{{{crc_find_string}}}</match_str>\n'+
'      		<ascending>{{{crc_sort_order}}}</ascending>\n'+
' 	    </ns4:get_name_info>\n'+
'	</message_body>\n'+
'</ns6:request>\n';
i2b2.CRC.cfg.parsers.getNameInfo = function() {
	if (!this.error) {
		this.model = [];		
		var qm = this.refXML.getElementsByTagName('query_master');
		for(var i=0; i<1*qm.length; i++) {
			var o = new Object;
			o.xmlOrig = qm[i];
			o.id = i2b2.h.getXNodeVal(qm[i],'query_master_id');
			o.name = i2b2.h.getXNodeVal(qm[i],'name');
			o.userid = i2b2.h.getXNodeVal(qm[i],'user_id');
			o.group = i2b2.h.getXNodeVal(qm[i],'group_id');
			o.created = i2b2.h.getXNodeVal(qm[i],'create_date');
			var dStr = '';
			var d = o.created.match(/^[0-9\-]*/).toString();
			if (d) {
				d = d.replace(/-/g,'/');
				d = new Date(Date.parse(d));
				if (d) {
					dStr = ' [' + (d.getMonth()+1) + '-' + d.getDate() + '-' + d.getFullYear().toString() + ']';
				}
			}
			o.name += dStr + ' ['+o.userid+']';
			// encapsulate into an SDX package
			var sdxDataPack = i2b2.sdx.Master.EncapsulateData('QM',o);
			this.model.push(sdxDataPack);
		}
	} else {
		this.model = false;
		console.error("[getNameInfo] Could not parse() data!");
	}
	return this;
}
i2b2.CRC.ajax._addFunctionCall(	"getNameInfo",
								"{{{URL}}}getNameInfo",
								i2b2.CRC.cfg.msgs.getNameInfo,
								null,
								i2b2.CRC.cfg.parsers.getNameInfo);



// ================================================================================================== //
i2b2.CRC.cfg.msgs.getQueryInstanceList_fromQueryMasterId = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>\n'+
'<ns6:request xmlns:ns4="http://www.i2b2.org/xsd/cell/crc/psm/1.1/" xmlns:ns7="http://www.i2b2.org/xsd/cell/crc/psm/querydefinition/1.1/" xmlns:ns3="http://www.i2b2.org/xsd/cell/crc/pdo/1.1/" xmlns:ns5="http://www.i2b2.org/xsd/hive/plugin/" xmlns:ns2="http://www.i2b2.org/xsd/hive/pdo/1.1/" xmlns:ns6="http://www.i2b2.org/xsd/hive/msg/1.1/">\n'+
'	<message_header>\n'+
'		{{{proxy_info}}}\n'+
'		<sending_application>\n'+
'			<application_name>i2b2_QueryTool</application_name>\n'+
'			<application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'		</sending_application>\n'+
'		<sending_facility>\n'+
'			<facility_name>PHS</facility_name>\n'+
'		</sending_facility>\n'+
'		<receiving_application>\n'+
'			<application_name>i2b2_DataRepositoryCell</application_name>\n'+
'			<application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'		</receiving_application>\n'+
'		<receiving_facility>\n'+
'		<facility_name>PHS</facility_name>\n'+
'		</receiving_facility>\n'+
'			<security>\n'+
'				<domain>{{{sec_domain}}}</domain>\n'+
'				<username>{{{sec_user}}}</username>\n'+
'				{{{sec_pass_node}}}\n'+
'			</security>\n'+
'		<message_type>\n'+
'			<message_code>Q04</message_code>\n'+
'			<event_type>EQQ</event_type>\n'+
'		</message_type>\n'+
'		<message_control_id>\n'+
'			<message_num>{{{header_msg_id}}}</message_num>\n'+
'			<instance_num>0</instance_num>\n'+
'		</message_control_id>\n'+
'		<processing_id>\n'+
'			<processing_id>P</processing_id>\n'+
'			<processing_mode>I</processing_mode>\n'+
'		</processing_id>\n'+
'		<accept_acknowledgement_type>messageId</accept_acknowledgement_type>\n'+
'		<project_id>{{{sec_project}}}</project_id>\n'+
'	</message_header>\n'+
'	<request_header>\n'+
'		<result_waittime_ms>{{{result_wait_time}}}000</result_waittime_ms>\n'+
'	</request_header>\n'+
'	<message_body>\n'+
'		<ns4:psmheader>\n'+
'			<user login="{{{sec_user}}}">{{{sec_user}}}</user>\n'+
'			<patient_set_limit>0</patient_set_limit>\n'+
'			<estimated_time>0</estimated_time>\n'+
'			<request_type>CRC_QRY_getQueryInstanceList_fromQueryMasterId</request_type>\n'+
'		</ns4:psmheader>\n'+
'		<ns4:request xsi:type="ns4:master_requestType" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">\n'+
'			<query_master_id>{{{qm_key_value}}}</query_master_id>\n'+
'		</ns4:request>\n'+
'	</message_body>\n'+
'</ns6:request>';
i2b2.CRC.cfg.parsers.getQueryInstanceList_fromQueryMasterId = function(){
	if (!this.error) {
		this.model = [];
		var qi = this.refXML.getElementsByTagName('query_instance');
		for(var i1=0; i1<1*qi.length; i1++) {
			var o = new Object;
			o.xmlOrig = qi[i1];
			o.query_master_id = i2b2.h.getXNodeVal(qi[i1],'query_master_id');
			o.query_instance_id = i2b2.h.getXNodeVal(qi[i1],'query_instance_id');
			o.id = o.query_instance_id;
			o.batch_mode = i2b2.h.getXNodeVal(qi[i1],'batch_mode');
			o.start_date = i2b2.h.getXNodeVal(qi[i1],'start_date');
			o.end_date = i2b2.h.getXNodeVal(qi[i1],'end_date');
			o.query_status_type = i2b2.h.getXNodeVal(qi[i1],'query_status_type');
			var sdxDataPack = i2b2.sdx.Master.EncapsulateData('QI',o);
			this.model.push(sdxDataPack);
		}
	} else {
		this.model = false;
		console.error("[getQueryInstanceList_fromQueryMasterId] Could not parse() data!");
	}
	return this;
}
i2b2.CRC.ajax._addFunctionCall(	"getQueryInstanceList_fromQueryMasterId",
								"{{{URL}}}request",
								i2b2.CRC.cfg.msgs.getQueryInstanceList_fromQueryMasterId,
								null,
								i2b2.CRC.cfg.parsers.getQueryInstanceList_fromQueryMasterId);
			


// ================================================================================================== //
i2b2.CRC.cfg.msgs.getQueryResultInstanceList_fromQueryResultInstanceId = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>\n'+
'<ns6:request xmlns:ns4="http://www.i2b2.org/xsd/cell/crc/psm/1.1/" xmlns:ns7="http://www.i2b2.org/xsd/cell/crc/psm/querydefinition/1.1/" xmlns:ns3="http://www.i2b2.org/xsd/cell/crc/pdo/1.1/" xmlns:ns5="http://www.i2b2.org/xsd/hive/plugin/" xmlns:ns2="http://www.i2b2.org/xsd/hive/pdo/1.1/" xmlns:ns6="http://www.i2b2.org/xsd/hive/msg/1.1/">\n'+
'	<message_header>\n'+
'		{{{proxy_info}}}\n'+
'		<sending_application>\n'+
'			<application_name>i2b2_QueryTool</application_name>\n'+
'			<application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'		</sending_application>\n'+
'		<sending_facility>\n'+
'			<facility_name>PHS</facility_name>\n'+
'		</sending_facility>\n'+
'		<receiving_application>\n'+
'			<application_name>i2b2_DataRepositoryCell</application_name>\n'+
'			<application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'		</receiving_application>\n'+
'		<receiving_facility>\n'+
'		<facility_name>PHS</facility_name>\n'+
'		</receiving_facility>\n'+
'			<security>\n'+
'				<domain>{{{sec_domain}}}</domain>\n'+
'				<username>{{{sec_user}}}</username>\n'+
'				{{{sec_pass_node}}}\n'+
'			</security>\n'+
'		<message_type>\n'+
'			<message_code>Q04</message_code>\n'+
'			<event_type>EQQ</event_type>\n'+
'		</message_type>\n'+
'		<message_control_id>\n'+
'			<message_num>{{{header_msg_id}}}</message_num>\n'+
'			<instance_num>0</instance_num>\n'+
'		</message_control_id>\n'+
'		<processing_id>\n'+
'			<processing_id>P</processing_id>\n'+
'			<processing_mode>I</processing_mode>\n'+
'		</processing_id>\n'+
'		<accept_acknowledgement_type>messageId</accept_acknowledgement_type>\n'+
'		<project_id>{{{sec_project}}}</project_id>\n'+
'	</message_header>\n'+
'	<request_header>\n'+
'		<result_waittime_ms>{{{result_wait_time}}}000</result_waittime_ms>\n'+
'	</request_header>\n'+
'	<message_body>\n'+
'		<ns4:psmheader>\n'+
'			<user login="{{{sec_user}}}">{{{sec_user}}}</user>\n'+
'			<patient_set_limit>0</patient_set_limit>\n'+
'			<estimated_time>0</estimated_time>\n'+
'			<request_type>CRC_QRY_getResultDocument_fromResultInstanceId</request_type>\n'+
'		</ns4:psmheader>\n'+
'		<ns4:request xsi:type="ns4:result_requestType" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">\n'+
'			<query_result_instance_id>{{{qr_key_value}}}</query_result_instance_id>\n'+
'		</ns4:request>\n'+
'	</message_body>\n'+
'</ns6:request>';
i2b2.CRC.cfg.parsers.getQueryResultInstanceList_fromQueryResultInstanceId = function(){
	if (!this.error) {
		this.model = [];
		var qi = this.refXML.getElementsByTagName('query_result_instance');
		for(var i1=0; i1<1*qi.length; i1++) {
			var o = new Object;
			o.xmlOrig = qi[i1];
			o.result_instance_id = i2b2.h.getXNodeVal(qi[i1],'result_instance_id');
			o.query_instance_id = i2b2.h.getXNodeVal(qi[i1],'query_instance_id');
			o.id = o.query_instance_id;
			o.batch_mode = i2b2.h.getXNodeVal(qi[i1],'batch_mode');
			o.start_date = i2b2.h.getXNodeVal(qi[i1],'start_date');
			o.end_date = i2b2.h.getXNodeVal(qi[i1],'end_date');
			o.query_status_type = i2b2.h.getXNodeVal(qi[i1],'query_status_type');
			var sdxDataPack = i2b2.sdx.Master.EncapsulateData('QI',o);
			this.model.push(sdxDataPack);
		}
	} else {
		this.model = false;
		console.error("[getQueryResultInstanceList_fromQueryResultInstanceId] Could not parse() data!");
	}
	return this;
}
i2b2.CRC.ajax._addFunctionCall(	"getQueryResultInstanceList_fromQueryResultInstanceId",
								"{{{URL}}}request",
								i2b2.CRC.cfg.msgs.getQueryResultInstanceList_fromQueryResultInstanceId,
								null,
								i2b2.CRC.cfg.parsers.getQueryResultInstanceList_fromQueryResultInstanceId);
			
			

// ================================================================================================== //			
i2b2.CRC.cfg.msgs.getQueryResultInstanceList_fromQueryInstanceId = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>\n'+
'<ns6:request xmlns:ns4="http://www.i2b2.org/xsd/cell/crc/psm/1.1/" xmlns:ns7="http://www.i2b2.org/xsd/cell/crc/psm/querydefinition/1.1/" xmlns:ns3="http://www.i2b2.org/xsd/cell/crc/pdo/1.1/" xmlns:ns5="http://www.i2b2.org/xsd/hive/plugin/" xmlns:ns2="http://www.i2b2.org/xsd/hive/pdo/1.1/" xmlns:ns6="http://www.i2b2.org/xsd/hive/msg/1.1/">\n'+
'	<message_header>\n'+
'		{{{proxy_info}}}\n'+
'		<sending_application>\n'+
'			<application_name>i2b2_QueryTool</application_name>\n'+
'			<application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'		</sending_application>\n'+
'		<sending_facility>\n'+
'			<facility_name>PHS</facility_name>\n'+
'		</sending_facility>\n'+
'		<receiving_application>\n'+
'			<application_name>i2b2_DataRepositoryCell</application_name>\n'+
'			<application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'		</receiving_application>\n'+
'		<receiving_facility>\n'+
'			<facility_name>PHS</facility_name>\n'+
'		</receiving_facility>\n'+
'		<security>\n'+
'			<domain>{{{sec_domain}}}</domain>\n'+
'			<username>{{{sec_user}}}</username>\n'+
'			{{{sec_pass_node}}}\n'+
'		</security>\n'+
'		<message_type>\n'+
'			<message_code>Q04</message_code>\n'+
'			<event_type>EQQ</event_type>\n'+
'		</message_type>\n'+
'		<message_control_id>\n'+
'			<message_num>{{{header_msg_id}}}</message_num>\n'+
'			<instance_num>0</instance_num>\n'+
'		</message_control_id>\n'+
'		<processing_id>\n'+
'			<processing_id>P</processing_id>\n'+
'			<processing_mode>I</processing_mode>\n'+
'		</processing_id>\n'+
'		<accept_acknowledgement_type>messageId</accept_acknowledgement_type>\n'+
'		<project_id>{{{sec_project}}}</project_id>\n'+
'	</message_header>\n'+
'	<request_header>\n'+
'		<result_waittime_ms>{{{result_wait_time}}}000</result_waittime_ms>\n'+
'	</request_header>\n'+
'	<message_body>\n'+
'		<ns4:psmheader>\n'+
'			<user login="{{{sec_user}}}">{{{sec_user}}}</user>\n'+
'			<patient_set_limit>0</patient_set_limit>\n'+
'			<estimated_time>0</estimated_time>\n'+
'			<request_type>CRC_QRY_getQueryResultInstanceList_fromQueryInstanceId</request_type>\n'+
'		</ns4:psmheader>\n'+
'		<ns4:request xsi:type="ns4:instance_requestType" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">\n'+
'			<query_instance_id>{{{qi_key_value}}}</query_instance_id>\n'+
'		</ns4:request>\n'+
'	</message_body>\n'+
'</ns6:request>';
i2b2.CRC.cfg.parsers.getQueryResultInstanceList_fromQueryInstanceId = function(){
	if (!this.error) {
		this.model = [];		
		// extract records from XML msg
		var ps = this.refXML.getElementsByTagName('query_result_instance');
		for(var i1=0; i1<ps.length; i1++) {
			var o = new Object;
			o.xmlOrig = ps[i1];
//			o.QI_id = pn.sdxInfo.sdxKeyValue;
//			o.QM_id = pn.parent.sdxInfo.sdxKeyValue;
			o.size = i2b2.h.getXNodeVal(ps[i1],'set_size');
			o.start_date = i2b2.h.getXNodeVal(ps[i1],'start_date');
			o.end_date = i2b2.h.getXNodeVal(ps[i1],'end_date');
			o.result_type = i2b2.h.XPath(ps[i1],'query_result_type/name/text()')[0].nodeValue;
			switch (o.result_type) {
				case "PATIENT_ENCOUNTER_SET":
					// create the title using shrine setting
					try {
						var t = i2b2.h.XPath(temp,'self::description')[0].firstChild.nodeValue;
					} catch(e) { var t = null; }
					if (t) {
						o.title = t;
					} else {
						if (o.size > 10) {
							o.title = "Encounter Set - "+o.size+" encounters";
						} else {
							if (i2b2.h.isSHRINE()) {
								o.title = "Encounter Set - 10 encounters or less";
							} else {
								o.title = "Encounter Set - "+o.size+" encounters";
							}
						}
					}
					o.PRS_id = i2b2.h.getXNodeVal(ps[i1],'result_instance_id');
					o.titleCRC = o.title;
//					o.title = pn.parent.sdxInfo.sdxDisplayName + ' [PATIENTSET_'+o.PRS_id+']';
					o.result_instance_id = o.PRS_id;
					var sdxDataNode = i2b2.sdx.Master.EncapsulateData('ENS',o);
					break;				
				case "PATIENTSET":
					// create the title using shrine setting
					try {
						var t = i2b2.h.XPath(temp,'self::description')[0].firstChild.nodeValue;
					} catch(e) { var t = null; }
					if (t) {
						o.title = t;
					} else {
						if (o.size > 10) {
							o.title = "Patient Set - "+o.size+" patients";
						} else {
							if (i2b2.h.isSHRINE()) {
								o.title = "Patient Set - 10 patients or less";
							} else {
								o.title = "Patient Set - "+o.size+" patients";
							}
						}
					}
					o.PRS_id = i2b2.h.getXNodeVal(ps[i1],'result_instance_id');
					o.titleCRC = o.title;
//					o.title = pn.parent.sdxInfo.sdxDisplayName + ' [PATIENTSET_'+o.PRS_id+']';
					o.result_instance_id = o.PRS_id;
					var sdxDataNode = i2b2.sdx.Master.EncapsulateData('PRS',o);
					break;
				case "PATIENT_COUNT_XML":
					// create the title using shrine setting
					try {
						var t = i2b2.h.XPath(temp,'self::description')[0].firstChild.nodeValue;
					} catch(e) { var t = null; }
					if (t) {
						o.title = t;
					} else {
						if (o.size > 10) {
							o.title = "Patient Count - "+o.size+" patients";
						} else {
							if (i2b2.h.isSHRINE()) {
								o.title = "Patient Count - 10 patients or less";
							} else {
								o.title = "Patient Count - "+o.size+" patients";
							}
						}
					}
					o.PRC_id = i2b2.h.getXNodeVal(ps[i1],'result_instance_id');
					o.titleCRC = o.title;
//					o.title = pn.parent.sdxInfo.sdxDisplayName + ' [PATIENT_COUNT_XML_'+o.PRC_id+']';
					o.result_instance_id = o.PRC_id;
					var sdxDataNode = i2b2.sdx.Master.EncapsulateData('PRC',o);
					break;
			}
			this.model.push(sdxDataNode);
		}
	} else {
		this.model = false;
		console.error("[getQueryResultInstanceList_fromQueryInstanceId] Could not parse() data!");
	}
	return this;
}
i2b2.CRC.ajax._addFunctionCall( "getQueryResultInstanceList_fromQueryInstanceId",
								"{{{URL}}}request", 
								i2b2.CRC.cfg.msgs.getQueryResultInstanceList_fromQueryInstanceId, 
								null, 
								i2b2.CRC.cfg.parsers.getQueryResultInstanceList_fromQueryInstanceId);


// ================================================================================================== //			
i2b2.CRC.cfg.msgs.getRequestXml_fromQueryMasterId = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>\n'+
'<ns6:request xmlns:ns4="http://www.i2b2.org/xsd/cell/crc/psm/1.1/" xmlns:ns7="http://www.i2b2.org/xsd/cell/crc/psm/querydefinition/1.1/" xmlns:ns3="http://www.i2b2.org/xsd/cell/crc/pdo/1.1/" xmlns:ns5="http://www.i2b2.org/xsd/hive/plugin/" xmlns:ns2="http://www.i2b2.org/xsd/hive/pdo/1.1/" xmlns:ns6="http://www.i2b2.org/xsd/hive/msg/1.1/">\n'+
'	<message_header>\n'+
'		{{{proxy_info}}}\n'+
'		<sending_application>\n'+
'			<application_name>i2b2_QueryTool</application_name>\n'+
'			<application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'		</sending_application>\n'+
'		<sending_facility>\n'+
'			<facility_name>PHS</facility_name>\n'+
'		</sending_facility>\n'+
'		<receiving_application>\n'+
'			<application_name>i2b2_DataRepositoryCell</application_name>\n'+
'			<application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'		</receiving_application>\n'+
'		<receiving_facility>\n'+
'			<facility_name>PHS</facility_name>\n'+
'		</receiving_facility>\n'+
'		<security>\n'+
'			<domain>{{{sec_domain}}}</domain>\n'+
'			<username>{{{sec_user}}}</username>\n'+
'			{{{sec_pass_node}}}\n'+
'		</security>\n'+
'		<message_type>\n'+
'			<message_code>Q04</message_code>\n'+
'			<event_type>EQQ</event_type>\n'+
'		</message_type>\n'+
'		<message_control_id>\n'+
'			<message_num>{{{header_msg_id}}}</message_num>\n'+
'			<instance_num>0</instance_num>\n'+
'		</message_control_id>\n'+
'		<processing_id>\n'+
'			<processing_id>P</processing_id>\n'+
'			<processing_mode>I</processing_mode>\n'+
'		</processing_id>\n'+
'		<accept_acknowledgement_type>messageId</accept_acknowledgement_type>\n'+
'			<project_id>{{{sec_project}}}</project_id>\n'+
'	</message_header>\n'+
'	<request_header>\n'+
'		<result_waittime_ms>{{{result_wait_time}}}000</result_waittime_ms>\n'+
'	</request_header>\n'+
'	<message_body>\n'+
'		<ns4:psmheader>\n'+
'			<user login="{{{sec_user}}}">{{{sec_user}}}</user>\n'+
'			<patient_set_limit>0</patient_set_limit>\n'+
'			<estimated_time>0</estimated_time>\n'+
'			<request_type>CRC_QRY_getRequestXml_fromQueryMasterId</request_type>\n'+
'		</ns4:psmheader>\n'+
'		<ns4:request xsi:type="ns4:master_requestType" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">\n'+
'			<query_master_id>{{{qm_key_value}}}</query_master_id>\n'+
'		</ns4:request>\n'+
'	</message_body>\n'+
'</ns6:request>';
i2b2.CRC.ajax._addFunctionCall("getRequestXml_fromQueryMasterId","{{{URL}}}request", i2b2.CRC.cfg.msgs.getRequestXml_fromQueryMasterId);
			

// ================================================================================================== //
i2b2.CRC.cfg.msgs.runQueryInstance_fromQueryDefinition = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>\n'+
'<ns6:request xmlns:ns4="http://www.i2b2.org/xsd/cell/crc/psm/1.1/" xmlns:ns7="http://www.i2b2.org/xsd/cell/ont/1.1/" xmlns:ns3="http://www.i2b2.org/xsd/cell/crc/pdo/1.1/" xmlns:ns5="http://www.i2b2.org/xsd/hive/plugin/" xmlns:ns2="http://www.i2b2.org/xsd/hive/pdo/1.1/" xmlns:ns6="http://www.i2b2.org/xsd/hive/msg/1.1/" xmlns:ns8="http://www.i2b2.org/xsd/cell/crc/psm/querydefinition/1.1/">\n'+
'	<message_header>\n'+
'		{{{proxy_info}}}\n'+
'		<sending_application>\n'+
'			<application_name>i2b2_QueryTool</application_name>\n'+
'			<application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'		</sending_application>\n'+
'		<sending_facility>\n'+
'			<facility_name>PHS</facility_name>\n'+
'		</sending_facility>\n'+
'		<receiving_application>\n'+
'			<application_name>i2b2_DataRepositoryCell</application_name>\n'+
'			<application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'		</receiving_application>\n'+
'		<receiving_facility>\n'+
'			<facility_name>PHS</facility_name>\n'+
'		</receiving_facility>\n'+
'		<security>\n'+
'			<domain>{{{sec_domain}}}</domain>\n'+
'			<username>{{{sec_user}}}</username>\n'+
'			{{{sec_pass_node}}}\n'+
'		</security>\n'+
'		<message_type>\n'+
'			<message_code>Q04</message_code>\n'+
'			<event_type>EQQ</event_type>\n'+
'		</message_type>\n'+
'		<message_control_id>\n'+
'			<message_num>{{{header_msg_id}}}</message_num>\n'+
'			<instance_num>0</instance_num>\n'+
'		</message_control_id>\n'+
'		<processing_id>\n'+
'			<processing_id>P</processing_id>\n'+
'			<processing_mode>I</processing_mode>\n'+
'		</processing_id>\n'+
'		<accept_acknowledgement_type>messageId</accept_acknowledgement_type>\n'+
'		<project_id>{{{sec_project}}}</project_id>\n'+
'	</message_header>\n'+
'	<request_header>\n'+
'		<result_waittime_ms>{{{result_wait_time}}}000</result_waittime_ms>\n'+
'	</request_header>\n'+
'	<message_body>\n'+
'		<ns4:psmheader>\n'+
'			<user group="{{{sec_project}}}" login="{{{sec_user}}}">{{{sec_user}}}</user>\n'+
'			<patient_set_limit>0</patient_set_limit>\n'+
'			<estimated_time>0</estimated_time>\n'+
'			<query_mode>optimize_without_temp_table</query_mode>\n'+
'			<request_type>CRC_QRY_runQueryInstance_fromQueryDefinition</request_type>\n'+
'		</ns4:psmheader>\n'+
'		<ns4:request xsi:type="ns4:query_definition_requestType" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">\n'+
'			{{{psm_query_definition}}}\n'+
'			{{{psm_result_output}}}\n'+
'		</ns4:request>\n'+
'		{{{shrine_topic}}}\n'+
'	</message_body>\n'+
'</ns6:request>\n';
i2b2.CRC.ajax._addFunctionCall(	"runQueryInstance_fromQueryDefinition", 
								"{{{URL}}}request", 
								i2b2.CRC.cfg.msgs.runQueryInstance_fromQueryDefinition, 
								["psm_result_output","psm_query_definition","shrine_topic"]);


// ================================================================================================== //1
i2b2.CRC.cfg.msgs.deleteQueryMaster = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>\n'+
'<ns6:request xmlns:ns4="http://www.i2b2.org/xsd/cell/crc/psm/1.1/" xmlns:ns7="http://www.i2b2.org/xsd/cell/crc/psm/querydefinition/1.1/" xmlns:ns3="http://www.i2b2.org/xsd/cell/crc/pdo/1.1/" xmlns:ns5="http://www.i2b2.org/xsd/hive/plugin/" xmlns:ns2="http://www.i2b2.org/xsd/hive/pdo/1.1/" xmlns:ns6="http://www.i2b2.org/xsd/hive/msg/1.1/">\n'+
'	<message_header>\n'+
'		{{{proxy_info}}}\n'+
'		<sending_application>\n'+
'			<application_name>i2b2_QueryTool</application_name>\n'+
'			<application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'		</sending_application>\n'+
'		<sending_facility>\n'+
'			<facility_name>PHS</facility_name>\n'+
'		</sending_facility>\n'+
'		<receiving_application>\n'+
'			<application_name>i2b2_DataRepositoryCell</application_name>\n'+
'			<application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'		</receiving_application>\n'+
'		<receiving_facility>\n'+
'			<facility_name>PHS</facility_name>\n'+
'		</receiving_facility>\n'+
'		<security>\n'+
'			<domain>{{{sec_domain}}}</domain>\n'+
'			<username>{{{sec_user}}}</username>\n'+
'			{{{sec_pass_node}}}\n'+
'		</security>\n'+
'		<message_type>\n'+
'			<message_code>Q04</message_code>\n'+
'			<event_type>EQQ</event_type>\n'+
'		</message_type>\n'+
'		<message_control_id>\n'+
'			<message_num>{{{header_msg_id}}}</message_num>\n'+
'			<instance_num>0</instance_num>\n'+
'		</message_control_id>\n'+
'		<processing_id>\n'+
'			<processing_id>P</processing_id>\n'+
'			<processing_mode>I</processing_mode>\n'+
'		</processing_id>\n'+
'		<accept_acknowledgement_type>messageId</accept_acknowledgement_type>\n'+
'		<project_id>{{{sec_project}}}</project_id>\n'+
'	</message_header>\n'+
'	<request_header>\n'+
'		<result_waittime_ms>{{{result_wait_time}}}000</result_waittime_ms>\n'+
'	</request_header>\n'+
'	<message_body>\n'+
'		<ns4:psmheader>\n'+
'			<user login="{{{sec_user}}}">{{{sec_user}}}</user>\n'+
'			<patient_set_limit>0</patient_set_limit>\n'+
'			<estimated_time>0</estimated_time>\n'+
'			<request_type>CRC_QRY_deleteQueryMaster</request_type>\n'+
'		</ns4:psmheader>\n'+
'		<ns4:request xsi:type="ns4:master_delete_requestType" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">\n'+
'			<user_id>{{{sec_user}}}</user_id>\n'+
'			<query_master_id>{{{qm_key_value}}}</query_master_id>\n'+
'		</ns4:request>\n'+
'	</message_body>\n'+
'</ns6:request>\n';
i2b2.CRC.ajax._addFunctionCall("deleteQueryMaster","{{{URL}}}request", i2b2.CRC.cfg.msgs.deleteQueryMaster);


// ================================================================================================== //
i2b2.CRC.cfg.msgs.renameQueryMaster = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>\n'+
'<ns6:request xmlns:ns4="http://www.i2b2.org/xsd/cell/crc/psm/1.1/" xmlns:ns7="http://www.i2b2.org/xsd/cell/crc/psm/querydefinition/1.1/" xmlns:ns3="http://www.i2b2.org/xsd/cell/crc/pdo/1.1/" xmlns:ns5="http://www.i2b2.org/xsd/hive/plugin/" xmlns:ns2="http://www.i2b2.org/xsd/hive/pdo/1.1/" xmlns:ns6="http://www.i2b2.org/xsd/hive/msg/1.1/">\n'+
'	<message_header>\n'+
'		{{{proxy_info}}}\n'+
'		<sending_application>\n'+
'			<application_name>i2b2_QueryTool</application_name>\n'+
'			<application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'		</sending_application>\n'+
'		<sending_facility>\n'+
'			<facility_name>PHS</facility_name>\n'+
'		</sending_facility>\n'+
'		<receiving_application>\n'+
'			<application_name>i2b2_DataRepositoryCell</application_name>\n'+
'			<application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'		</receiving_application>\n'+
'		<receiving_facility>\n'+
'			<facility_name>PHS</facility_name>\n'+
'		</receiving_facility>\n'+
'		<security>\n'+
'			<domain>{{{sec_domain}}}</domain>\n'+
'			<username>{{{sec_user}}}</username>\n'+
'			{{{sec_pass_node}}}\n'+
'		</security>\n'+
'		<message_type>\n'+
'			<message_code>Q04</message_code>\n'+
'			<event_type>EQQ</event_type>\n'+
'		</message_type>\n'+
'		<message_control_id>\n'+
'			<message_num>{{{header_msg_id}}}</message_num>\n'+
'			<instance_num>0</instance_num>\n'+
'		</message_control_id>\n'+
'		<processing_id>\n'+
'			<processing_id>P</processing_id>\n'+
'			<processing_mode>I</processing_mode>\n'+
'		</processing_id>\n'+
'		<accept_acknowledgement_type>messageId</accept_acknowledgement_type>\n'+
'			<project_id>{{{sec_project}}}</project_id>\n'+
'	</message_header>\n'+
'	<request_header>\n'+
'		<result_waittime_ms>{{{result_wait_time}}}000</result_waittime_ms>\n'+
'	</request_header>\n'+
'	<message_body>\n'+
'		<ns4:psmheader>\n'+
'			<user login="{{{sec_user}}}">{{{sec_user}}}</user>\n'+
'			<patient_set_limit>0</patient_set_limit>\n'+
'			<estimated_time>0</estimated_time>\n'+
'			<request_type>CRC_QRY_renameQueryMaster</request_type>\n'+
'		</ns4:psmheader>\n'+
'		<ns4:request xsi:type="ns4:master_rename_requestType" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">\n'+
'			<user_id>{{{sec_user}}}</user_id>\n'+
'			<query_master_id>{{{qm_key_value}}}</query_master_id>\n'+
'			<query_name>{{{qm_name}}}</query_name>\n'+
'		</ns4:request>\n'+
'	</message_body>\n'+
'</ns6:request>';
i2b2.CRC.ajax._addFunctionCall("renameQueryMaster","{{{URL}}}request", i2b2.CRC.cfg.msgs.renameQueryMaster);


// ================================================================================================== //
i2b2.CRC.cfg.msgs.getPDO_fromInputList = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>\r'+
'<ns6:request xmlns:ns4="http://www.i2b2.org/xsd/cell/crc/psm/1.1/"\r'+
'  xmlns:ns7="http://www.i2b2.org/xsd/cell/crc/psm/querydefinition/1.1/"\r'+
'  xmlns:ns3="http://www.i2b2.org/xsd/cell/crc/pdo/1.1/"\r'+
'  xmlns:ns5="http://www.i2b2.org/xsd/hive/plugin/"\r'+
'  xmlns:ns2="http://www.i2b2.org/xsd/hive/pdo/1.1/"\r'+
'  xmlns:ns6="http://www.i2b2.org/xsd/hive/msg/1.1/">\r'+
'	<message_header>\n'+
'		{{{proxy_info}}}'+
'		<sending_application>\n'+
'			<application_name>i2b2_QueryTool</application_name>\n'+
'			<application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'		</sending_application>\n'+
'		<sending_facility>\n'+
'			<facility_name>PHS</facility_name>\n'+
'		</sending_facility>\n'+
'		<receiving_application>\n'+
'			<application_name>i2b2_DataRepositoryCell</application_name>\n'+
'			<application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'		</receiving_application>\n'+
'		<receiving_facility>\n'+
'			<facility_name>PHS</facility_name>\n'+
'		</receiving_facility>\n'+
'		<message_type>\n'+
'			<message_code>Q04</message_code>\n'+
'			<event_type>EQQ</event_type>\n'+
'		</message_type>\n'+
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
'		<accept_acknowledgement_type>messageId</accept_acknowledgement_type>\n'+
'		<project_id>{{{sec_project}}}</project_id>\n'+
'	</message_header>\n'+
'	<request_header>\n'+
'		<result_waittime_ms>{{{result_wait_time}}}000</result_waittime_ms>\n'+
'	</request_header>\n'+
'	<message_body>\n'+
'		<ns3:pdoheader>\n'+
'			<patient_set_limit>{{{patient_limit}}}</patient_set_limit>\n'+
'			<estimated_time>{{{result_wait_time}}}000</estimated_time>\n'+
'			<request_type>getPDO_fromInputList</request_type>\n'+
'		</ns3:pdoheader>\n'+
'		<ns3:request xsi:type="ns3:GetPDOFromInputList_requestType" \n'+
'		  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">\n'+
'			{{{PDO_Request}}}'+
'		</ns3:request>\n'+
'	</message_body>\n'+
'</ns6:request>';
i2b2.CRC.cfg.parsers.getPDO_fromInputList = function(){
	if (!this.error) {
		this.model = {
			patients: [],
			events: [],
			observations: []
		};		
		// extract event records
		var ps = this.refXML.getElementsByTagName('event');
		for(var i1=0; i1<ps.length; i1++) {
			var o = new Object;
			o.xmlOrig = ps[i1];
			o.event_id = i2b2.h.getXNodeVal(ps[i1],'event_id');
			o.patient_id = i2b2.h.getXNodeVal(ps[i1],'patient_id');
			o.start_date = i2b2.h.getXNodeVal(ps[i1],'start_date');
			o.end_date = i2b2.h.getXNodeVal(ps[i1],'end_date');
			// need to process param columns 
			//o. = i2b2.h.getXNodeVal(ps[i1],'');
			this.model.events.push(o);
		}
		// extract observation records
		var ps = this.refXML.getElementsByTagName('observation');
		for(var i1=0; i1<ps.length; i1++) {
			var o = new Object;
			o.xmlOrig = ps[i1];
			o.event_id = i2b2.h.getXNodeVal(ps[i1],'event_id');
			o.patient_id = i2b2.h.getXNodeVal(ps[i1],'patient_id');
			o.concept_cd = i2b2.h.getXNodeVal(ps[i1],'concept_cd');
			o.observer_cd = i2b2.h.getXNodeVal(ps[i1],'observer_cd');
			o.start_date = i2b2.h.getXNodeVal(ps[i1],'start_date');
			o.modifier_cd = i2b2.h.getXNodeVal(ps[i1],'modifier_cd');
			o.tval_char = i2b2.h.getXNodeVal(ps[i1],'tval_char');
			o.nval_num = i2b2.h.getXNodeVal(ps[i1],'nval_num');
			o.valueflag_cd = i2b2.h.getXNodeVal(ps[i1],'valueflag_cd');
			o.units_cd = i2b2.h.getXNodeVal(ps[i1],'units_cd');
			o.end_date = i2b2.h.getXNodeVal(ps[i1],'end_date');
			o.location_cd = i2b2.h.getXNodeVal(ps[i1],'location_cd');
			this.model.observations.push(o);
		}
		var ps = this.refXML.getElementsByTagName('patient');
		for(var i1=0; i1<ps.length; i1++) {
			var o = new Object;
			o.xmlOrig = ps[i1];
			o.patient_id = i2b2.h.getXNodeVal(ps[i1],'patient_id');
			var params = i2b2.h.XPath(ps[i1], 'descendant::param[@name]/text()/..');
			for (var i2 = 0; i2 < params.length; i2++) {
				var name = params[i2].getAttribute("name");
				o[name] = params[i2].firstChild.nodeValue;
			}		
			this.model.patients.push(o);
		}
	} else {
		this.model = false;
		console.error("[getPDO_fromInputList] Could not parse() data!");
	}
	return this;
}
i2b2.CRC.ajax._addFunctionCall(	"getPDO_fromInputList",
								"{{{URL}}}pdorequest",
								i2b2.CRC.cfg.msgs.getPDO_fromInputList,
								["PDO_Request"],
								i2b2.CRC.cfg.parsers.getPDO_fromInputList);


// ================================================================================================== //
i2b2.CRC.cfg.msgs.getIbservationfact_byPrimaryKey = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>\r'+
'<ns6:request xmlns:ns4="http://www.i2b2.org/xsd/cell/crc/psm/1.1/"\r'+
'  xmlns:ns7="http://www.i2b2.org/xsd/cell/crc/psm/querydefinition/1.1/"\r'+
'  xmlns:ns3="http://www.i2b2.org/xsd/cell/crc/pdo/1.1/"\r'+
'  xmlns:ns5="http://www.i2b2.org/xsd/hive/plugin/"\r'+
'  xmlns:ns2="http://www.i2b2.org/xsd/hive/pdo/1.1/"\r'+
'  xmlns:ns6="http://www.i2b2.org/xsd/hive/msg/1.1/">\r'+
'	<message_header>\n'+
'		{{{proxy_info}}}'+
'		<sending_application>\n'+
'			<application_name>i2b2_QueryTool</application_name>\n'+
'			<application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'		</sending_application>\n'+
'		<sending_facility>\n'+
'			<facility_name>PHS</facility_name>\n'+
'		</sending_facility>\n'+
'		<receiving_application>\n'+
'			<application_name>i2b2_DataRepositoryCell</application_name>\n'+
'			<application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'		</receiving_application>\n'+
'		<receiving_facility>\n'+
'			<facility_name>PHS</facility_name>\n'+
'		</receiving_facility>\n'+
'		<message_type>\n'+
'			<message_code>Q04</message_code>\n'+
'			<event_type>EQQ</event_type>\n'+
'		</message_type>\n'+
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
'		<accept_acknowledgement_type>messageId</accept_acknowledgement_type>\n'+
'		<project_id>{{{sec_project}}}</project_id>\n'+
'	</message_header>\n'+
'	<request_header>\n'+
'		<result_waittime_ms>{{{result_wait_time}}}000</result_waittime_ms>\n'+
'	</request_header>\n'+
'	<message_body>\n'+
'		<ns3:pdoheader>\n'+
'			<request_type>get_observationfact_by_primary_key</request_type>\n'+
'		</ns3:pdoheader>\n'+
'		<ns3:request xsi:type="ns3:GetObservationFactByPrimaryKey_requestType" \n'+
'		  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">\n'+
'			{{{PDO_Request}}}'+
'		</ns3:request>\n'+
'	</message_body>\n'+
'</ns6:request>';
i2b2.CRC.cfg.parsers.getIbservationfact_byPrimaryKey = function(){
	if (!this.error) {
		this.model = {
			observations: []
		};		
		// extract observation records
		var ps = this.refXML.getElementsByTagName('observation');
		for(var i1=0; i1<ps.length; i1++) {
			var o = new Object;
			o.xmlOrig = ps[i1];
			o.event_id = i2b2.h.getXNodeVal(ps[i1],'event_id');
			o.patient_id = i2b2.h.getXNodeVal(ps[i1],'patient_id');
			o.concept_cd = i2b2.h.getXNodeVal(ps[i1],'concept_cd');
			o.observer_cd = i2b2.h.getXNodeVal(ps[i1],'observer_cd');
			o.start_date = i2b2.h.getXNodeVal(ps[i1],'start_date');
			o.modifier_cd = i2b2.h.getXNodeVal(ps[i1],'modifier_cd');
			o.tval_char = i2b2.h.getXNodeVal(ps[i1],'tval_char');
			o.nval_num = i2b2.h.getXNodeVal(ps[i1],'nval_num');
			o.valueflag_cd = i2b2.h.getXNodeVal(ps[i1],'valueflag_cd');
			o.units_cd = i2b2.h.getXNodeVal(ps[i1],'units_cd');
			o.end_date = i2b2.h.getXNodeVal(ps[i1],'end_date');
			o.location_cd = i2b2.h.getXNodeVal(ps[i1],'location_cd');
			this.model.observations.push(o);
		}
	} else {
		this.model = false;
		console.error("[getIbservationfact_byPrimaryKey] Could not parse() data!");
	}
	return this;
}
i2b2.CRC.ajax._addFunctionCall(	"getIbservationfact_byPrimaryKey",
								"{{{URL}}}pdorequest",
								i2b2.CRC.cfg.msgs.getIbservationfact_byPrimaryKey,
								["PDO_Request"],
								i2b2.CRC.cfg.parsers.getIbservationfact_byPrimaryKey);



// ================================================================================================== //
i2b2.CRC.cfg.msgs.getQRY_getResultType = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>\r'+
'<ns6:request xmlns:ns4="http://www.i2b2.org/xsd/cell/crc/psm/1.1/"\r'+
'  xmlns:ns7="http://www.i2b2.org/xsd/cell/crc/psm/querydefinition/1.1/"\r'+
'  xmlns:ns3="http://www.i2b2.org/xsd/cell/crc/pdo/1.1/"\r'+
'  xmlns:ns5="http://www.i2b2.org/xsd/hive/plugin/"\r'+
'  xmlns:ns2="http://www.i2b2.org/xsd/hive/pdo/1.1/"\r'+
'  xmlns:ns6="http://www.i2b2.org/xsd/hive/msg/1.1/">\r'+
'	<message_header>\n'+
'		{{{proxy_info}}}'+
'		<sending_application>\n'+
'			<application_name>i2b2_QueryTool</application_name>\n'+
'			<application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'		</sending_application>\n'+
'		<sending_facility>\n'+
'			<facility_name>PHS</facility_name>\n'+
'		</sending_facility>\n'+
'		<receiving_application>\n'+
'			<application_name>i2b2_DataRepositoryCell</application_name>\n'+
'			<application_version>' + i2b2.ClientVersion + '</application_version>\n'+
'		</receiving_application>\n'+
'		<receiving_facility>\n'+
'			<facility_name>PHS</facility_name>\n'+
'		</receiving_facility>\n'+
'		<message_type>\n'+
'			<message_code>Q04</message_code>\n'+
'			<event_type>EQQ</event_type>\n'+
'		</message_type>\n'+
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
'		<accept_acknowledgement_type>messageId</accept_acknowledgement_type>\n'+
'		<project_id>{{{sec_project}}}</project_id>\n'+
'	</message_header>\n'+
'	<request_header>\n'+
'		<result_waittime_ms>{{{result_wait_time}}}000</result_waittime_ms>\n'+
'	</request_header>\n'+
'	<message_body>\n'+
'		<ns4:psmheader>\n'+
'            <user login="{{{sec_user}}}">{{{sec_user}}}</user>\n'+
'            <patient_set_limit>0</patient_set_limit>\n'+
'            <estimated_time>0</estimated_time>\n'+
'            <request_type>CRC_QRY_getResultType</request_type>\n'+
'		</ns4:psmheader>\n'+
'	</message_body>\n'+
'</ns6:request>';
i2b2.CRC.cfg.parsers.getQRY_getResultType = function(){
	if (!this.error) {
		this.model = {
			result: []
		};		
		// extract event records
		var ps = this.refXML.getElementsByTagName('query_result_type');
		for(var i1=0; i1<ps.length; i1++) {
			var o = new Object;
			o.result_type_id = i2b2.h.getXNodeVal(ps[i1],'result_type_id');
			o.name = i2b2.h.getXNodeVal(ps[i1],'name');
			o.display_type = i2b2.h.getXNodeVal(ps[i1],'display_type');
			o.visual_attribute_type = i2b2.h.getXNodeVal(ps[i1],'visual_attribute_type');
			o.description = i2b2.h.getXNodeVal(ps[i1],'description');
			// need to process param columns 
			//o. = i2b2.h.getXNodeVal(ps[i1],'');
			this.model.events.push(o);
		}
	} else {
		this.model = false;
		console.error("[getQRY_getResultType] Could not parse() data!");
	}
	return this;
}
i2b2.CRC.ajax._addFunctionCall(	"getQRY_getResultType",
								"{{{URL}}}request",
								i2b2.CRC.cfg.msgs.getQRY_getResultType,
								["Request"],
								i2b2.CRC.cfg.parsers.getQRY_getResultType);