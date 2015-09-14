/**
 * @projectDescription	The main controller for the history viewport. (CRC's "previous queries" functionality)
 * @inherits 	i2b2.CRC.ctrlr
 * @namespace	i2b2.CRC.ctrlr.find
 * @author		Nick Benik, Griffin Weber MD PhD
 * @version 	1.3
 * ----------------------------------------------------------------------------------------
 * updated 9-15-08: RC4 launch [Nick Benik] 
 */
console.group('Load & Execute component file: CRC > ctrlr > History');
console.time('execute time');


i2b2.CRC.ctrlr.find = {
	clickedID: false,
	queryNewName: false,
	
// ================================================================================================== //
	Refresh: function() {
		console.info("CALLED i2b2.CRC.ctrlr.find.Refresh()");
		// THIS FUNCTION DOES THE FOLLOWING:
		//	1) fires a call to ajax.getCategories(), 
		//	2) interprets the XML / populates the ONT data model, 
		//	3) fires it's onDataUpdate event

		
	},

	clickSearchName: function() {
		console.info("CALLED i2b2.CRC.ctrlr.find.clickSearchName()");
		// THIS FUNCTION DOES THE FOLLOWING:
		//	1) fires a call to ajax.getCategories(), 
		//	2) interprets the XML / populates the ONT data model, 
		//	3) fires it's onDataUpdate event


		
		// create a scoped callback message
		var scopeCB = new i2b2_scopedCallback();
		scopeCB.scope = i2b2.CRC.model.QueryMasters;
		scopeCB.callback = function(i2b2CellMsg) {
			document.getElementById('crcFindNameButtonWorking').style.display = 'block';
			document.getElementById('crcFindButton').style.display = 'none';

			// define the XML processing function
			console.group("CALLBACK Processing AJAX i2b2CellMsg");
			console.dir(i2b2CellMsg);
			
			i2b2.CRC.view.history.queryResponse = i2b2CellMsg.msgResponse;
			i2b2.CRC.view.history.queryRequest = i2b2CellMsg.msgRequest;
			i2b2.CRC.view.history.queryUrl = i2b2CellMsg.msgUrl;
			// the THIS scope is already set to i2b2.CRC.model.QueryMasters
			i2b2.sdx.Master.ClearAll('QM');
			if (!i2b2CellMsg.error) {		

                                var condition = i2b2CellMsg.refXML.getElementsByTagName('condition')[0];
                                if (condition.getAttribute("type") == "ERROR")
                                {
                                        alert( 'ERROR: ' + condition.firstChild.textContent);
                                        return;
                                }
                                

				var qm = i2b2CellMsg.refXML.getElementsByTagName('query_master');
				var totalCount = 0;
				for(var i=0; i<1*qm.length; i++) {
					totalCount++;
					var o = new Object;
					o.xmlOrig = qm[i];
					o.id = i2b2.h.getXNodeVal(qm[i],'query_master_id');
					o.realname = i2b2.h.getXNodeVal(qm[i],'name');
					o.userid = i2b2.h.getXNodeVal(qm[i],'user_id');
					o.group = i2b2.h.getXNodeVal(qm[i],'group_id');
					o.created = i2b2.h.getXNodeVal(qm[i],'create_date');
					o.master_type_cd = i2b2.h.getXNodeVal(qm[i],'master_type_cd');
				
					var dStr = '';
					var d = o.created.match(/^[0-9\-]*/).toString();
					if (d) {
						d = d.replace(/-/g,'/');
						d = new Date(Date.parse(d));
						if (d) {
							dStr = ' [' + (d.getMonth()+1) + '-' + d.getDate() + '-' + d.getFullYear().toString() + ']';
						}
					}
					o.name = o.realname +  dStr + ' ['+o.userid+']';
					// encapsulate into an SDX package
					var sdxDataPack = i2b2.sdx.Master.EncapsulateData('QM',o);
					// save the node to the CRC data model
					i2b2.sdx.Master.Save(sdxDataPack, null);
				}

			        if (totalCount == 0)
		                {
                		        alert("No Records Found");
                		}

			} else {
				alert("An error has occurred in the Cell's AJAX library.\n Press F12 for more information");
			}
			// Broadcast an update event letting interested view controllers know that the Categories data model has been updated
			var DataUpdateSignal = {
				DataLocation: "i2b2.CRC.model.QueryMasters",
				DataRef: i2b2.CRC.model.QueryMasters
			}
			console.info("EVENT FIRE i2b2.CRC.ctrlr.gen.events.onDataUpdate; Msg:",DataUpdateSignal);
			console.groupEnd();
			document.getElementById('crcFindButton').style.display = 'block';
			document.getElementById('crcFindNameButtonWorking').style.display = 'none';

			i2b2.CRC.ctrlr.find.events.onDataUpdate.fire(DataUpdateSignal);
		};
		
		// fire the AJAX call
		var options = {
			result_wait_time: 180,
			crc_max_records: i2b2.CRC.view['history'].params.maxQueriesDisp,
			crc_sort_by: i2b2.CRC.view['history'].params.sortBy,
			crc_user_type: 	(i2b2.PM.model.userRoles.indexOf("MANAGER") == -1? 	"CRC_QRY_getQueryMasterList_fromUserId" : "CRC_QRY_getQueryMasterList_fromGroupId"),	
			crc_sort_order: (i2b2.CRC.view['history'].params.sortOrder.indexOf("DESC") == -1?"true": "false"),
			crc_find_category: $('crcFindCategory').value,
			crc_find_strategy: $('crcFindStrategy').value,
			crc_find_string: $('crcFindNameMatch').value
		};
		i2b2.CRC.ajax.getNameInfo("CRC:History", options, scopeCB);

	},

// ================================================================================================== //
	queryDelete: function(qmID) {
		// function requires a Query Master ID
		var t = i2b2.sdx.Master.Load('QM',qmID);
		var QueryName = t.origData.name;
		if (confirm('Delete Query "' + QueryName + '"?')) {
			// create a scoped callback message 
			var scopeCB = new i2b2_scopedCallback();
			scopeCB.scope = i2b2.CRC.model.QueryMasters;
			scopeCB.callback = function(i2b2CellMsg) {
				// define the XML processing function
				console.group("CALLBACK Processing AJAX i2b2CellMsg");
				console.dir(i2b2CellMsg);
				i2b2.CRC.view.history.queryResponse = i2b2CellMsg.msgResponse;
				i2b2.CRC.view.history.queryRequest = i2b2CellMsg.msgRequest;
				i2b2.CRC.view.history.queryUrl = i2b2CellMsg.msgUrl;
				if (i2b2CellMsg.error) {		
					alert("An error has occurred in the Cell's AJAX library.\n Press F12 for more information");
				}
				// refresh the Query History data
				i2b2.CRC.ctrlr.find.Refresh();
			};
			
			// fire the AJAX call
			var options = {
				result_wait_time: 180,
				qm_key_value: qmID
			};
			i2b2.CRC.ajax.deleteQueryMaster("CRC:History", options, scopeCB);
		}
	},
	
	queryDeleteNoPrompt: function(qmID) {
		// function requires a Query Master ID
			// create a scoped callback message 
			var scopeCB = new i2b2_scopedCallback();
			scopeCB.scope = i2b2.CRC.model.QueryMasters;
			scopeCB.callback = function(i2b2CellMsg) {
				// define the XML processing function
				console.group("CALLBACK Processing AJAX i2b2CellMsg");
				console.dir(i2b2CellMsg);
				i2b2.CRC.view.history.queryResponse = i2b2CellMsg.msgResponse;
				i2b2.CRC.view.history.queryRequest = i2b2CellMsg.msgRequest;
				i2b2.CRC.view.history.queryUrl = i2b2CellMsg.msgUrl;
				//if (i2b2CellMsg.error) {		
				//	alert("An error has occurred in the Cell's AJAX library.\n Press F12 for more information");
				//}
				// refresh the Query History data
				i2b2.CRC.ctrlr.find.Refresh();
			};
			
			// fire the AJAX call
			var options = {
				result_wait_time: 180,
				qm_key_value: qmID
			};
			i2b2.CRC.ajax.deleteQueryMaster("CRC:History", options, scopeCB);
	},
	
// ================================================================================================== //
	queryRename: function(qmID, newQueryName, sdxPackage) {
		this.queryNewName = newQueryName || false;
		if (!this.queryNewName) {
			// callback for dialog submission
			var handleSubmit = function() {
				// submit value(s)
				var closure_qmID = qmID;
				if(this.submit()) {
					// run the query
					i2b2.CRC.ctrlr.find.queryRename(closure_qmID, $('inputQueryName').value);
				}
			}
			// display the query name input dialog
			this._queryPromptName(handleSubmit);
			// get the old name (and trim whitespace)
			$('inputQueryName').value = sdxPackage.origData.realname;
			return;
		}
		
		// create a scoped callback message 
		var scopeCB = new i2b2_scopedCallback();
		scopeCB.scope = this;
		scopeCB.callback = function(i2b2CellMsg) {
			// define the XML processing function
			console.group("CALLBACK Processing AJAX i2b2CellMsg");
			console.dir(i2b2CellMsg);
			i2b2.CRC.view.history.queryResponse = i2b2CellMsg.msgResponse;
			i2b2.CRC.view.history.queryRequest = i2b2CellMsg.msgRequest;
			i2b2.CRC.view.history.queryUrl = i2b2CellMsg.msgUrl;
			if (i2b2CellMsg.error) {		
				alert("An error has occurred in the Cell's AJAX library.\n Press F12 for more information");
			}
			// refresh the Query History data
			i2b2.CRC.ctrlr.find.Refresh();
			console.groupEnd();
		};
		// fire the AJAX call
		var options = {
			qm_key_value: qmID,
			qm_name: newQueryName
		};
		i2b2.CRC.ajax.renameQueryMaster("CRC:History", options, scopeCB);
	},
	
// ================================================================================================== //
	_queryPromptName: function(handleSubmit) {
		if (!i2b2.CRC.view.dialogQmName) {
			var handleCancel = function() {
				this.cancel();
			};
			var loopBackSubmit = function() {
				i2b2.CRC.view.dialogQmName.submitterFunction();
			};
			i2b2.CRC.view.dialogQmName = new YAHOO.widget.SimpleDialog("dialogQmName", {
					width: "400px",
					fixedcenter: true,
					constraintoviewport: true,
					modal: true,
					zindex: 700,
					buttons: [{
						text: "OK",
						handler: loopBackSubmit,
						isDefault: true
					}, {
						text: "Cancel",
						handler: handleCancel
					}]
				});
			$('dialogQmName').show();
			i2b2.CRC.view.dialogQmName.validate = function(){
				// now process the form data
				var msgError = '';
				var queryNameInput = $('inputQueryName');
				if (!queryNameInput || queryNameInput.value.blank()) {
					alert('Please enter a name for this query.');
					return false;
				}
				return true;
			};
			i2b2.CRC.view.dialogQmName.render(document.body);
		}
		// manage the event handler for submit
		delete i2b2.CRC.view.dialogQmName.submitterFunction;
		i2b2.CRC.view.dialogQmName.submitterFunction = handleSubmit;
		// display the dialoge
		i2b2.CRC.view.dialogQmName.center();
		i2b2.CRC.view.dialogQmName.show();
	}
}



// signal that is fired when the CRC cell's data model is updated
// ================================================================================================== //
i2b2.CRC.ctrlr.find.events = new Object;
i2b2.CRC.ctrlr.find.events.onDataUpdate = new YAHOO.util.CustomEvent("DataUpdate", i2b2.CRC);


console.timeEnd('execute time');
console.groupEnd();
