/**
 * @projectDescription	Initialize the i2b2 framework & load the hive configuration information for the core I2B2 Framework.
 * @inherits 	i2b2
 * @namespace		i2b2
 * @author		Nick Benik, Griffin Weber MD PhD
 * @version 	1.3
 * ----------------------------------------------------------------------------------------
 * updated 9-15-08: RC4 launch [Nick Benik] 
 */


// build the global i2b2.hive namespace
var i2b2 = {sdx:{TypeControllers:{},Master:{_sysData:{}}},events:{},hive:{cfg:{},helpers:{},base_classes:{}},h:{}};
if (undefined==i2b2) { var i2b2 = {}; }
if (undefined==i2b2.sdx) { i2b2.sdx = {}; }	
if (undefined==i2b2.events) { i2b2.events = {}; }	
if (undefined==i2b2.hive) { i2b2.hive = {}; }	
if (undefined==i2b2.hive.cfg) { i2b2.hive.cfg = {}; }	
if (undefined==i2b2.h) { i2b2.h = {}; }
if (undefined==i2b2.hive.base_classes) { i2b2.hive.base_classes = {}; }


i2b2.ClientVersion = "1.6"; 

//     ||
//     ||		
//   \\||//		Configure the loading of cells BELOW
//    \\//
//     vv
// ================================================================================================== //
// THESE ARE ALL THE CELLS THAT ARE INSTALLED ONTO THE SERVER
i2b2.hive.tempCellsList = [
		{ code: "PM",
		  forceLoading: true 			// <----- this must be set to true for the PM cell!
		},
//		{ code: "ONT"	},
//		{ code: "CRC"	},
//		{ code: "WORK"},
//		{ code: "SHRINE"},
		{ code:	"PLUGINMGR",
		   forceLoading: true,
		   forceConfigMsg: { params: [] }
		},
//		{ code:	"ExampHello",
//		   forceLoading: true,
//		   forceConfigMsg: { params: [] },
//		   forceDir: "cells/plugins/examples"
//		},
//		{ code:	"ExampTabs",
//		   forceLoading: true,
//		   forceConfigMsg: { params: [] },
//		   forceDir: "cells/plugins/examples"
//		},
//		{ code:	"ExampPDO",
//		   forceLoading: true,
//		   forceConfigMsg: { params: [] },
//		   forceDir: "cells/plugins/examples"
//		},
//		{ code:	"ExampComm",
//		   forceLoading: true,
//		   forceConfigMsg: { params: [] },
//		   forceDir: "cells/plugins/examples"
//		},
		{ code:	"Dem1Set",
		   forceLoading: true,
		   forceConfigMsg: { params: [] },
		   roles: [ "DATA_LDS", "DATA_DEID", "DATA_PROT" ],
		   forceDir: "cells/plugins/standard"
		},
		{ code:	"Dem2Set",
		   forceLoading: true,
		   forceConfigMsg: { params: [] },
		   roles: [ "DATA_LDS", "DATA_DEID", "DATA_PROT" ],
		   forceDir: "cells/plugins/standard"
		},
		{ code:	"Timeline",
		   forceLoading: true,
		   forceConfigMsg: { params: [] },
		   roles: [ "DATA_LDS", "DATA_DEID", "DATA_PROT" ],
		   forceDir: "cells/plugins/standard"
		},
        { code: "ProjectRequest",
            forceLoading: true,
            forceConfigMsg: { params: [] },
		   roles: [ "DATA_LDS", "DATA_DEID", "DATA_PROT" ],
            forceDir: "cells/plugins/standard"
        }
		
	];
// ================================================================================================== //
//     ^^
//    //\\
//   //||\\		
//     ||		Configure the loading of cells ABOVE
//     ||		
//     ||
















// ================================================================================================== //
i2b2.Init = function() {
	//load the (user configured) i2b2Hive configuration via JSON config file
	var config_data = i2b2.h.getJsonConfig('i2b2_config_data.js');
	if (!config_data) {
		alert("The user-defined I2B2 Hive Configuration message is invalid (try looking for an extra comma)");
		return false;
	} else {
		i2b2.hive.cfg = config_data;
		i2b2.hive.cfg.lstCells = {};
		var l = i2b2.hive.tempCellsList.length;
		for (var i=0; i<l; i++) {
			i2b2.hive.cfg.lstCells[i2b2.hive.tempCellsList[i].code] = Object.clone(i2b2.hive.tempCellsList[i]);
			i2b2.hive.cfg.lstCells[i2b2.hive.tempCellsList[i].code].forceConfigMsg = false;
			if (i2b2.hive.tempCellsList[i].forceConfigMsg) {
				i2b2.hive.cfg.lstCells[i2b2.hive.tempCellsList[i].code].forceConfigMsg = Object.clone(i2b2.hive.tempCellsList[i].forceConfigMsg);
			}
			if (i2b2.hive.tempCellsList[i].roles) {
				i2b2.hive.cfg.lstCells[i2b2.hive.tempCellsList[i].code].roles = i2b2.hive.tempCellsList[i].roles;
			} else {
				i2b2.hive.cfg.lstCells[i2b2.hive.tempCellsList[i].code].roles = ["DATA_OBFSC"];							
			}
			i2b2.hive.cfg.lstCells[i2b2.hive.tempCellsList[i].code].params = {};
		}
	}
		
	// load the rest of the i2b2 framework files
	var config_data = i2b2.h.getJsonConfig(i2b2.hive.cfg.urlFramework+'hive/hive_config_data.js');
	if (!config_data) {
		alert("The I2B2 Hive Components Load message is invalid (try looking for an extra comma)");
		return false;
	} else {
		var successHandler = function(oData) { 
		
			//code to execute when all requested scripts have been 
			//loaded; this code can make use of the contents of those 
			//scripts, whether it's functional code or JSON data. 
			console.info("EVENT FIRED i2b2.events.afterFrameworkInit");
			i2b2.events.afterFrameworkInit.fire();
			// Loading the hive cells
			for (var cellKey in i2b2.hive.cfg.lstCells) {
				i2b2[cellKey] = new i2b2_BaseCell(i2b2.hive.cfg.lstCells[cellKey]);
			}
			// we must always fully initialize the PM cell
			if (i2b2['PM']) { 
				// the project manager cell must fire the afterProjMngtInit event signal
				i2b2['PM'].Init();
			};			
			// trigger user events after everything is loaded
			console.info("EVENT FIRED i2b2.events.afterHiveInit");
			i2b2.events.afterHiveInit.fire();
		};
		var failureHandler = function(oData) {
			alert('i2b2 Framework file failed to load!\n'+oData);
		};
		
		var fl = [];
		for (var i=0; i<config_data.files.length; i++) {
			fl.push(i2b2.hive.cfg.urlFramework+'hive/'+config_data.files[i]);
		}
		
		YAHOO.util.Get.script(fl, { 
				onSuccess: successHandler, 
				onFailure: failureHandler,
				data:      config_data
		});
	}
}


// create our custom events
// ================================================================================================== //
i2b2.events.afterFrameworkInit = new YAHOO.util.CustomEvent("afterInit", i2b2);
i2b2.events._privLoadCells = new YAHOO.util.CustomEvent("priv_doLoadCells", i2b2);
i2b2.events._privRemoveInitFuncs = new YAHOO.util.CustomEvent("priv_doRemoveInit", i2b2);
i2b2.events.afterHiveInit = new YAHOO.util.CustomEvent("afterInit", i2b2);
i2b2.events.afterCellInit = new YAHOO.util.CustomEvent("afterInit", i2b2);
i2b2.events.afterLogin = new YAHOO.util.CustomEvent("afterLogin", i2b2);
i2b2.events.afterAllCellsLoaded = new YAHOO.util.CustomEvent("afterAllCellsLoaded", i2b2);



// *******************************************************
//  i2b2.h.getJsonConfig
//    
//    @descript This function retreves a JSON-defined configuration object from the given URL
// *******************************************************
i2b2.h.getJsonConfig = function(url) {
	var json = new Ajax.Request(url, {
		contentType: 'text/xml',
		method:'get', 
		asynchronous:false, 
		sanitizeJSON:true
	});
	try {
		var co = eval('('+json.transport.responseText+')');
	} catch(e) {
		var co = false;
	}
	return co;

}
