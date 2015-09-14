// this file contains a list of all files that need to be loaded dynamically for the i2b2 framework
// every file in this list will be loaded after page load
{
	files: [
		"hive_globals.js",
		"hive_helpers.js",
		"hive_SDX.js",
		"msg_sniffer.js",
		"help_viewer.js",
		"master_view_ctrlr.js",
		"i2b2_cell_communicator.js"
	],
	config: {
		// additional configuration variables that are set by the system
		validMasterViews: ['Patients', 'Analysis']
	}
}