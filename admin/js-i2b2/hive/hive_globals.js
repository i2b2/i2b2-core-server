/**
 * @projectDescription	Various objects/classes used by the i2b2 Framework.
 * @namespace	
 * @inherits 	
 * @author		Nick Benik, Griffin Weber MD PhD
 * @version 	1.3
 * ----------------------------------------------------------------------------------------
 * updated 9-15-08: RC4 launch [Nick Benik] 
 */
console.group('Load & Execute component file: hive > globals');
console.time('execute time');

// View Controllers
// ================================================================================================== //
function i2b2Base_cellViewController(parentObj, viewName) { 
	// attributes
	this.cellRoot = parentObj; 
	this.viewName = viewName; 
	this.queryRequest = '';
	this.queryResponse = '';
	this.params = new Object;
	// functions
	this.showOptions = _showOptions;
	this.Render = _doRender;
	this.Resize = _doResize;
	function _doRender() {			alert("[Cell:"+this.cellRoot.cellCode+"] DEFAULT doRender() function for '"+this.viewName+"' View");	}
	function _doResize(width,height) {	alert("[Cell:"+this.cellRoot.cellCode+"] DEFAULT Resize("+width+","+height+") function for '"+this.viewName+"' View");	}
	function _showOptions(subScreen) {	alert("[Cell:"+this.cellRoot.cellCode+"] DEFAULT showOptions() function for '"+this.viewName+"' View (request subscreen:'"+subScreen+"')");	}
	function _saveOptions(subScreen) {	alert("[Cell:"+this.cellRoot.cellCode+"] DEFAULT saveOptions() function for '"+this.viewName+"' View (request subscreen:'"+subScreen+"')");	}
	function _cancelOptions(subScreen) {	alert("[Cell:"+this.cellRoot.cellCode+"] DEFAULT cancelOptions() function for '"+this.viewName+"' View (request subscreen:'"+subScreen+"')");	}
};


// base class for sending re-scoped callbacks
// ================================================================================================== //
function i2b2_scopedCallback(refFunction, refScope) {
	this.callback = refFunction;
	this.scope = refScope;
}


// base class for all cells
// ================================================================================================== //
function i2b2_BaseCell(configObj) {
	// this function expects the configuration object to have at least a "code" attribute
	
	if (!configObj || !configObj.code) { return false; }
	this.cellCode = configObj.code;
	
	// build out the default structure for the cell
	this.cfg = {};
	this.cfg.params = {};
	this.model = {};
	this.view = {};
	this.view.modal = {};
	this.ctrlr = {};
	this.ajax = {};
	this.sdx = {};
	this.isLoaded = false;
	if (!configObj.name) { 	this.name = configObj.name; }
	// special processing for data in the cell registry (i2b2_loader.js)
	var baseDir = i2b2.hive.cfg.urlFramework;
	if (configObj.forceDir) {
		baseDir += configObj.forceDir+'/'+this.cellCode+'/';
	} else {
		baseDir += 'cells/'+this.cellCode+'/';
	}
	// default directory that the Cell's assets would be in
	var assetDir = baseDir + 'assets/';

	
	// load the cell's configuration info into the base cell object being constructed
	var config_data = i2b2.h.getJsonConfig(baseDir+'cell_config_data.js');
	if (!config_data) {
		console.error("The " + this.cellCode + " Cell's configuration file is invalid");
		return false;
	}
	this.cfg = config_data;
	this.cfg.baseDir = baseDir;
	this.cfg.config.assetDir = assetDir;

	// create an initialization function which will load all of the cell's files (Lazy Loader pattern)
	this.Init = (function(inURL, inParams) {
		console.debug('i2b2_BaseCell superclass Initialize function for Cell ['+this.cellCode+']');
		if (Object.isUndefined(inParams)) { inParams=[]; }
		// onSucess handler function
		var cellLoadSig = function() {
			var cellCode = configObj.code;
			var cellParams = inParams;
			// copy all params to all View Controllers
			for (var i in i2b2[cellCode].view) {
				if (getObjectClass(i2b2[cellCode].view[i])=='i2b2Base_cellViewController') { i2b2[cellCode].view[i].params = Object.clone(i2b2[cellCode].cfg.params); }
			}
			// send the signal that the Cell is now loaded
			console.info('EVENT FIRED i2b2.events.afterCellInit['+cellCode+']');
			i2b2[cellCode].isLoaded = true;
			i2b2.events.afterCellInit.fire(i2b2[cellCode]);
		}
		// onFailure handler function
		var cellLoadFail = function() {
			var cellCode = configObj.code;
			var cellParams = inParams;
			console.error(cellCode+" Cell Failed to load all files in it's configuration file!");
		}
		// save configuration info
		this.cfg.cellURL = inURL;
		this.cfg.cellParams = inParams;

		// load the script files
		var fl = [];
		for (var i=0; i<this.cfg.files.length; i++) {
			fl.push(this.cfg.baseDir+this.cfg.files[i]);
		}
		YAHOO.util.Get.script(fl, {onSuccess:cellLoadSig, onFailure:cellLoadFail, data: config_data, autopurge: !(i2b2.PM.model.login_debugging)});


		// load any external CSS files needed
		var fl = [];
		if (this.cfg.css) {
			for (var i=0; i<this.cfg.css.length; i++) {
				fl.push(this.cfg.config.assetDir+this.cfg.css[i]);
			}
			YAHOO.util.Get.css(fl, {data: config_data});
		}
		
		// Cell Parameters Descriptor Array: XML<->ThinClient translation / default values
		if (Object.isUndefined(i2b2[configObj.code].cfg.config.paramTranslation)) { i2b2[configObj.code].cfg.config.paramTranslation = []; }
		var paramsInfo = i2b2[configObj.code].cfg.config.paramTranslation;
		
		// record every param that has a defaultValue set
		i2b2[configObj.code].cfg.params = {};
		for (var i1=0; i1<paramsInfo.length; i1++) {
			if (!Object.isUndefined(paramsInfo[i1].defaultValue)) { i2b2[configObj.code].cfg.params[paramsInfo[i1].thinClientName] = paramsInfo[i1].defaultValue; }
		}
		// insert translated XML params into the cell's params Object
		for (var paramName in inParams) {
			var found = false;
			var pName = paramName;
			var pValue = inParams[paramName]; 
			for (var i2=0; i2<paramsInfo.length; i2++) {
				if (pName == paramsInfo[i2].xmlName) {
					// we found a parameter descriptor record
					if (paramsInfo[i2].thinClientName) { var pName = paramsInfo[i2].thinClientName; }
					if (!Object.isUndefined(paramsInfo[i2].defaultValue)) {
						// convert the type to the same as defaultValue if needed
						switch ((typeof paramsInfo[i2].defaultValue)) {
							case "number": 
								pValue = parseFloat(pValue);
								break;
							case "boolean":
								pValue = parseBoolean(pValue);
								break;
						}
					}
					break;
				}
			}
			// insert into Cell's param Object
			i2b2[configObj.code].cfg.params[pName] = pValue;
		}
	});
}

console.timeEnd('execute time');
console.groupEnd();