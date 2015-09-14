// this file contains a list of all files that need to be loaded dynamically for this i2b2 Cell
// every file in this list will be loaded after the cell's Init function is called
{
	files: [
		"ONT_view_Main.js",
		"ONT_view_Nav.js",
		"ONT_view_Find.js",
		"ONT_ctrlr_FindBy.js",
		"ONT_ctrlr_general.js",
		"ONT_sdx_CONCPT.js",
		"i2b2_msgs.js"
	],
	// ONLY USE 1 STYLE SHEET: http://support.microsoft.com/kb/262161
	css: ["main_list.css"],  
	config: {
		// additional configuration variables that are set by the system
		name: "Ontology",
		description: "The Ontology cell manages all concepts used to describe the data processed by all the Cells within i2b2 Hive.",
		icons: {
			size32x32: "ONT_icon_32x32.gif"
		},
		category: ["core","cell"],
		paramTranslation: [
			{xmlName:'OntMax', thinClientName:'max', defaultValue:500},
			{xmlName:'OntHiddens', thinClientName:'hiddens', defaultValue:false},
			{xmlName:'OntSynonyms', thinClientName:'synonyms', defaultValue:true}
		]
	}
}