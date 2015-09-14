// this file contains a list of all files that need to be loaded dynamically for this i2b2 Cell
// every file in this list will be loaded after the cell's Init function is called
{
	files:[
		"Dem1Set_ctrlr.js"
	],
	css:[ 
		"vwDem1Set.css"
	],
	config: {
		// additional configuration variables that are set by the system
		short_name: "Demographics",
		name: "Demographics (1 Patient Set) - Simple Counts",
		description: "This plugin displays a demographic break-down of a Patient Set.",
		category: ["celless","plugin","standard","demographics"],
		plugin: {
			isolateHtml: false,  // this means do not use an IFRAME
			isolateComm: false,  // this means to expect the plugin to use AJAX communications provided by the framework
			standardTabs: true,  // this means the plugin uses standard tabs at top
			html: {
				source: 'injected_screens.html',
				mainDivId: 'Dem1Set-mainDiv'
			}
		}
	}
}