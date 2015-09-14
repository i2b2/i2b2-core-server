// this file contains a list of all files that need to be loaded dynamically for this plugin
// every file in this list will be loaded after the plugin's Init function is called
{
	files:[ "ExampTabs_ctrlr.js" ],
	css:[ "vwExampTabs.css" ],
	config: {
		// additional configuration variables that are set by the system
		short_name: "Tabs and DragDrop",
		name: "Example #2 - Tabs and DragDrop",
		description: "This plugin demonstrates how to use tabs and interact with i2b2 objects by accepting DragDrop messages (SDX Objects).",
		category: ["celless","plugin","examples"],
		plugin: {
			isolateHtml: false,  // this means do not use an IFRAME
			isolateComm: false,  // this means to expect the plugin to use AJAX communications provided by the framework
			standardTabs: true, // this means the plugin uses standard tabs at top
			html: {
				source: 'injected_screens.html',
				mainDivId: 'ExampTabs-mainDiv'
			}
		}
	}
}