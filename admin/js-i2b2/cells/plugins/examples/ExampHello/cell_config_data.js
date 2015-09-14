// this file contains a list of all files that need to be loaded dynamically for this plugin
// every file in this list will be loaded after the plugin's Init function is called
{
	files:[ "ExampHello.js" ],
	css:[ "ExampHello.css" ],
	config: {
		// additional configuration variables that are set by the system
		short_name: "Hello World",
		name: "Example #1 - Hello World",
		description: "This plugin demonstrates how to register a plugin within the i2b2 web client framework.",
		category: ["celless","plugin","examples"],
		plugin: {
			isolateHtml: false,  	// this means do not use an IFRAME
			isolateComm: true,	// this means to expect the plugin to use AJAX communications provided by the framework
			html: {
				source: 'injected_screens.html',
				mainDivId: 'ExampHello-mainDiv'
			}
		}
	}
}