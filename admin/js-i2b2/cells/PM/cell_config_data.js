// this file contains a list of all files that need to be loaded dynamically for this i2b2 Cell
// every file in this list will be loaded after the cell's Init function is called
{
	files: [
		"PM_misc.js",
		"PM_view.js",
		"PM_ctrlr.js",
		"PM_admin_ctrlr.js",
		"PM_admin_view.js",
		"i2b2_msgs.js"
	],
	css: ["modProjects.css"],  // ONLY USE 1 STYLE SHEET: http://support.microsoft.com/kb/262161
	config: {
		// additional configuration variables that are set by the system
		name: "Project Management",
		description: "The Project Management cell is responsible for authentication and access control to other Cells within the i2b2 Hive.",
		icons: {
			size32x32: "PM_icon_32x32.gif"
		},
		category: ["core","cell"],
		authRoles: [
			{code:"MANAGER", name:"Hive Manager"},
			{code:"EDITOR", name:"Hive Editor"},
			{code:"USER", name:"Hive User"},
			{code:"DATA_PROT", name:"Data Protect - DATA_PROT"},
			{code:"DATA_DEID", name:"Data Protect - DATA_DEID"},
			{code:"DATA_LDS", name:"Data Protect - DATA_LDS"},
			{code:"DATA_AGG", name:"Data Protect - DATA_AGG"},
			{code:"DATA_OBFSC", name:"Data Protect - DATA_OBFSC"}
		]
	}
}
