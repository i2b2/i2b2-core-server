// this file contains a list of all files that need to be loaded dynamically for this i2b2 Cell
// every file in this list will be loaded after the cell's Init function is called
{
	files:[
		"Dem2Set_ctrlr.js"
	],
	css:[ 
		"vwDem2Set.css"
	],
	config: {
		// additional configuration variables that are set by the system
		short_name: "Demographics",
		name: "Demographics (2 Patient Sets) - Simple Counts",
		description: "This plugin compares the demographic break-down of two Patient Sets.",
		category: ["celless","plugin","standard","demographics"],
		plugin: {
			isolateHtml: false,  // this means do not use an IFRAME
			isolateComm: false,  // this means to expect the plugin to use AJAX communications provided by the framework
			standardTabs: true,  // this means the plugin uses standard tabs at top
			html: {
				source: 'injected_screens.html',
				mainDivId: 'Dem2Set-mainDiv'
			}
		}
	}
}