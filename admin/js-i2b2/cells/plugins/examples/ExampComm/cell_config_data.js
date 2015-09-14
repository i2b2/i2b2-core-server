// this file contains a list of all files that need to be loaded dynamically for this plugin
// every file in this list will be loaded after the plugin's Init function is called
{
	files:[ "ExampComm_ctrlr.js" ],
	css:[ "vwExampComm.css" ],
	config: {
		// additional configuration variables that are set by the system
		short_name: "Communicator Tool",
		name: "Example #5 - Communicator Tool",
		description: "This plugin allows you to interact directly with standard Cell Communicator objects within the web client framework.",
		category: ["celless","plugin","examples"],
		plugin: {
			isolateHtml: false,  
			isolateComm: false,  
			standardTabs: true,
			html: {
				source: 'injected_screens.html',
				mainDivId: 'ExampComm-mainDiv'
			}
		}
	}
}