/**
 * @projectDescription	Event controller for general ONT functionality.
 * @inherits 	i2b2.ONT.ctrlr
 * @namespace	i2b2.ONT.ctrlr.FindBy
 * @author		Nick Benik, Griffin Weber MD PhD
 * @version 	1.3
 * ----------------------------------------------------------------------------------------
 * updated 9-15-08: RC4 launch [Nick Benik] 
 */
console.group('Load & Execute component file: ONT > ctrlr > general');
console.time('execute time');


i2b2.ONT.ctrlr.gen = new Object;
// ================================================================================================== //
i2b2.ONT.ctrlr.gen.loadCategories = function() {
	console.info("CALLED i2b2.ONT.ctrlr.gen.loadCategories()");
	// THIS FUNCTION DOES THE FOLLOWING:
	//	1) fires a call to ajax.getCategories(), 
	//	2) interprets the XML / populates the ONT data model, 
	//	3) fires it's onDataUpdate event


	// make sure the categories section of the data model exists
	if (!i2b2.ONT.model.Categories) { i2b2.ONT.model.Categories = []; }
	// define the XML processing function
	var processXML = function(i2b2CellMsg) {
		console.group("CALLBACK Processing AJAX i2b2CellMsg");
		console.dir(i2b2CellMsg);
		// the THIS scope is already set to i2b2.ONT.model.Categories
		this.clear();
		i2b2.ONT.view.nav.queryResponse = i2b2CellMsg.msgResponse;
		i2b2.ONT.view.nav.queryRequest = i2b2CellMsg.msgRequest;
		if (!i2b2CellMsg.error) {		
			var c = i2b2CellMsg.refXML.getElementsByTagName('concept');
			for(var i=0; i<1*c.length; i++) {
				var o = new Object;
				o.xmlOrig = c[i];
				o.name = i2b2.h.getXNodeVal(c[i],'name');
				o.hasChildren = i2b2.h.getXNodeVal(c[i],'visualattributes').substring(0,2);
				o.level = i2b2.h.getXNodeVal(c[i],'level');
				o.key = i2b2.h.getXNodeVal(c[i],'key');
				o.tooltip = i2b2.h.getXNodeVal(c[i],'tooltip');
				o.icd9 = '';
				o.table_name = i2b2.h.getXNodeVal(c[i],'tablename');
				o.column_name = i2b2.h.getXNodeVal(c[i],'columnname');
				o.operator = i2b2.h.getXNodeVal(c[i],'operator');
				o.dim_code = i2b2.h.getXNodeVal(c[i],'dimcode');
				// save the node to the ONT data model
				this.push(o);
			}
		} else {
			alert("An error has occurred in the Cell's AJAX library.\n Press F12 for more information");
		}
		// Broadcast an update event letting interested view controllers know that the Categories data model has been updated
		var DataUpdateSignal = {
			DataLocation: "i2b2.ONT.model.Categories",
			DataRef: i2b2.ONT.model.Categories
		}
		console.info("EVENT FIRE i2b2.ONT.ctrlr.gen.events.onDataUpdate; Msg:",DataUpdateSignal);
		console.groupEnd();
		i2b2.ONT.ctrlr.gen.events.onDataUpdate.fire(DataUpdateSignal);
	};
	// create a scoped callback message to pass the XML to our function defined above
	var scopeCB = new i2b2_scopedCallback(processXML,i2b2.ONT.model.Categories);
	// fire the AJAX call
	var options = {}
	options.ont_hidden_records = i2b2.ONT.view['nav'].params.hiddens
	options.ont_synonym_records = i2b2.ONT.view['nav'].params.synonyms	
	i2b2.ONT.ajax.GetCategories("ONT:generalView", options, scopeCB);
}

// ================================================================================================== //
i2b2.ONT.ctrlr.gen.loadSchemes = function() {
	console.info("CALLED i2b2.ONT.ctrlr.gen.loadSchemes()");
	// THIS FUNCTION DOES THE FOLLOWING:
	//	1) fires a call to ajax.getCategories(), 
	//	2) interprets the XML / populates the ONT data model, 
	//	3) fires it's onDataUpdate event

	// make sure the schemes section of the data model exists
	if (!i2b2.ONT.model.Schemes) { i2b2.ONT.model.Schemes = []; }
	// define the XML processing function
	var processXML = function(i2b2CellMsg) {
		console.group("CALLBACK Processing AJAX i2b2CellMsg");
		console.dir(i2b2CellMsg);
		// the THIS scope is already set to i2b2.ONT.model.Categories
		this.clear();
		i2b2.ONT.view.nav.queryResponse = i2b2CellMsg.msgResponse;
		i2b2.ONT.view.nav.queryRequest = i2b2CellMsg.msgRequest;
		if (!i2b2CellMsg.error) {		
			var c = i2b2CellMsg.refXML.getElementsByTagName('concept');
			for(var i=0; i<1*c.length; i++) {
				var o = new Object;
				o.name = i2b2.h.getXNodeVal(c[i],'name');
				o.key = i2b2.h.getXNodeVal(c[i],'key');
				// save the node to the ONT data model
				i2b2.ONT.model.Schemes.push(o);
			}
		} else {
			alert("An error has occurred in the Cell's AJAX library.\n Press F12 for more information");
		}
		// Broadcast an update event letting interested view controllers know that the Categories data model has been updated
		var DataUpdateSignal = {
			DataLocation: "i2b2.ONT.model.Schemes",
			DataRef: i2b2.ONT.model.Schemes
		}
		console.info("EVENT FIRED i2b2.ONT.ctrlr.gen.events.onDataUpdate");
		console.dir(DataUpdateSignal);
		console.groupEnd();
		i2b2.ONT.ctrlr.gen.events.onDataUpdate.fire(DataUpdateSignal);
	};
	// create a scoped callback message to pass the XML to our function defined above
	var scopeCB = new i2b2_scopedCallback(processXML,i2b2.ONT.model.Schemes);
	// fire the AJAX call
	i2b2.ONT.ajax.GetSchemes("ONT:generalView", {}, scopeCB);
}


// signal that is fired when the ONT cell's data model is updated
// ================================================================================================== //
i2b2.ONT.ctrlr.gen.events = new Object;
i2b2.ONT.ctrlr.gen.events.onDataUpdate = new YAHOO.util.CustomEvent("DataUpdate", i2b2.ONT);


// after the cell is initialized 
// ================================================================================================== //
i2b2.events.afterCellInit.subscribe(
	(function(en,co,a) {
		if (co[0].cellCode=='ONT') {
			i2b2.ONT.ctrlr.gen.loadCategories(); 	// load categories into the data model
			i2b2.ONT.ctrlr.gen.loadSchemes();		// load categories into the data model
		}
	})
);

console.timeEnd('execute time');
console.groupEnd();
