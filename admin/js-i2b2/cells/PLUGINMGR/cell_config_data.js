// this file contains a list of all files that need to be loaded dynamically for this i2b2 Cell
// every file in this list will be loaded after the cell's Init function is called
{
	files: [
		"PLUGINMGR_ctrlr_general.js",
		"PLUGINMGR_view_list.js",
		"PLUGINMGR_view_PlugView.js"
	],
	css: [ "main_list.css" ],  // ONLY USE 1 STYLE SHEET: http://support.microsoft.com/kb/262161
	config: {
		// additional configuration variables that are set by the system
		i2b2Only: true,
		name: "Plugin Viewer",
		description: "This client-side cell is used to load and view other cells",
		icons: {
			size32x32: "PLUGIN_icon_32x32.gif"
		},
		defaultListIcons: {
			size16x16: "DEFAULTLIST_icon_14x14.gif",
			size32x32: "DEFAULTLIST_icon_32x32.gif"
		},
		category: ["core","celless"]
	}
}