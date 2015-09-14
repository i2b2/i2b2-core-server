// this file contains a list of all files that need to be loaded dynamically for this i2b2 Cell
// every file in this list will be loaded after the cell's Init function is called
{
	files:[
		"Timeline_ctrlr.js",
		"Timeline_modLabRange.js"
	],
	css:[ 
		"vwTimeline.css"
	],
	config: {
		// additional configuration variables that are set by the system
		short_name: "Timeline",
		name: "Timeline",
		description: "This plugin creates a visual representation of when selected observations occur within a given patient set.",
		//icons: { size32x32: "Timeline_icon_32x32.gif" },
		category: ["celless","plugin","standard", "temporal"],
		plugin: {
			isolateHtml: false,  // this means do not use an IFRAME
			isolateComm: false,  // this means to expect the plugin to use AJAX communications provided by the framework
			standardTabs: true, // this means the plugin uses standard tabs at top
			html: {
				source: 'injected_screens.html',
				mainDivId: 'Timeline-mainDiv'
			}
		}
	}
}