// this file contains a list of all files that need to be loaded dynamically for this i2b2 Cell
// every file in this list will be loaded after the cell's Init function is called
{
	files:[
		"ProjectRequest_ctrlr.js"
	],
	css:[ 
		"vwProjectRequest.css"
	],
	config: {
		// additional configuration variables that are set by the system
		short_name: "Project Request",
		name: "Project Request",
		description: "This plugin is uesed to generate a request for a new project based on concepts, patients and sets..",
		category: ["celless","plugin","examples"],
		plugin: {
			isolateHtml: false,  // this means do not use an IFRAME
			isolateComm: false,  // this means to expect the plugin to use AJAX communications provided by the framework
			standardTabs: true, // this means the plugin uses standard tabs at top
			html: {
				source: 'injected_screens.html',
				mainDivId: 'ProjectRequest-mainDiv'
			}
		}
	}
}
