/**
 * @projectDescription	(GUI-only) Controller for CRC Query Tool's Lab Values constraint dialog box.
 * @inherits 	i2b2.CRC.view
 * @namespace	i2b2.CRC.view.modalLabValues
 * @author		Nick Benik, Griffin Weber MD PhD, Shawn Murphy
 * @version 	1.6
 * ----------------------------------------------------------------------------------------
 * updated 9-15-08: 1.3 RC4 launch [Nick Benik] 
 * updated 7-28-12  1.6 [Shawn Murphy]
 */

// Known Bugs:
// double value to right of concept name when populate previous query

i2b2.CRC.view.modalLabValues = {
	formdata: {},
	cfgTestInfo: {
		name: 'RND-TEST',
		flagType: 'NA',
		flags: [{name:'Abnormal', value:'A'},{name:'Normal', value:'@'}],
		valueValidate: {
			onlyPos: true,
			onlyInt: true,
			maxString: 0 
		},
		valueType: 'PosFloat',
		valueUnitsCurrent: 0, // index into Units[]
		valueUnits: {},
		rangeInfo: {},
		enumInfo: {}
	},

// snm0
//===================================================================================
// Gets called when a click occurs on the value bar graphic and the operator and 
// values are set.
//
	updateValue: function(e) {
		// The method is to embed the reference to the value label of the bar
		// in the anchor of the href and then get it and extract the value and
		// place it in the select drop down and text box.
		try {
			var targ; // href of bar item that was clicked
			if (!e) var e = window.event;
			if (e.target) targ = e.target;
			else if (e.srcElement) targ = e.srcElement;
			if (targ.nodeType == 3) // defeat Safari bug
				targ = targ.parentNode;
			// make href into a string and get the anchor of it
			var sTarg = targ.toString();
			var iTargAnchor = sTarg.lastIndexOf("#");
			if (iTargAnchor < 0) return; // no anchor
			var sTargAnchor = sTarg.substring(iTargAnchor+1);
			if (sTargAnchor.length <= 1) return; //no anchor
			//alert(sTargAnchor);
			var sTargNumber = $(sTargAnchor).innerHTML;
			// after getting the bar label get the value and put it in the slots
			if ((sTargAnchor == 'lblToxL') || (sTargAnchor == 'lblLofL') || (sTargAnchor == 'lblHofL')) {
				$('mlvfrmOperator').selectedIndex=1;
				//$('mlvfrmOperator').value='LE';
				i2b2.CRC.view.modalLabValues.formdata.numericOperator = 'LE';
				$('mlvfrmNumericValue').value = sTargNumber;
			}
			if ((sTargAnchor == 'lblToxH') || (sTargAnchor == 'lblLofH') || (sTargAnchor == 'lblHofH')) {
				$('mlvfrmOperator').selectedIndex=5;
				i2b2.CRC.view.modalLabValues.formdata.numericOperator = 'GE';
				$('mlvfrmNumericValue').value = sTargNumber;
			}
		}
		catch(eError) {
				alert("Error: updateValue: " + eError.description);
		}
	},
// snm0	
// ================================================================================================== //
	show: function(panelIndex, queryPanelController, key, extData, isModifier) {
		if (Object.isUndefined(i2b2.CRC.model.queryCurrent.panels[i2b2.CRC.ctrlr.QT.temporalGroup][panelIndex])) { return; }
		var fd = i2b2.CRC.view.modalLabValues.formdata;
		var dm = i2b2.CRC.model.queryCurrent.panels[i2b2.CRC.ctrlr.QT.temporalGroup][panelIndex];
		// save info for callback
		this.qpi = panelIndex;
		this.cpc = queryPanelController;
		i2b2.CRC.view.modalLabValues.isModifier = isModifier;
		i2b2.CRC.view.modalLabValues.itemNumber = extData.itemNumber;
		//this.isModifier = isMod;
		this.key = key;
		this.i2b2Data = extData;
		// Create SimpleDialog control
		if (!this.sd) {
			this.sd = new YAHOO.widget.SimpleDialog("itemLabRange", {
				zindex: 700,
				width: "600px",
				fixedcenter: true,
				constraintoviewport: true,
				modal: true,
				buttons: [{
					text: "OK",
					isDefault: true,
					handler: 
						(function() {
							var closure_qpi = i2b2.CRC.view.modalLabValues.qpi;
							var closure_cpc = i2b2.CRC.view.modalLabValues.cpc;
							var closure_key = i2b2.CRC.view.modalLabValues.key;
							var closure_number = i2b2.CRC.view.modalLabValues.itemNumber;
							// submit value(s)
							if (this.submit()) {
								var pd = i2b2.CRC.model.queryCurrent.panels[i2b2.CRC.ctrlr.QT.temporalGroup][closure_qpi];
								// find the correct item in the panel
								for (var i=0; i<pd.items.length; i++) {
									//if (pd.items[i].sdxInfo.sdxKeyValue==closure_key) {
									if (pd.items[i].itemNumber==closure_number) {
										if (i2b2.CRC.view.modalLabValues.isModifier) {
											pd.items[i].ModValues = i2b2.CRC.view.modalLabValues.i2b2Data.ModValues;
										} else {
											pd.items[i].LabValues = i2b2.CRC.view.modalLabValues.i2b2Data.LabValues;										
										}
										break;
									}
								}
								// update the panel/query tool GUI
								i2b2.CRC.ctrlr.QT.doSetQueryName.call(this, '');
								
								//queryPanelController._renameConcept(closure_key, i2b2.CRC.view.modalLabValues.isModifier, pd);
								
								queryPanelController._renameConcept(i2b2.CRC.view.modalLabValues.i2b2Data.itemNumber, i2b2.CRC.view.modalLabValues.isModifier, pd);
								
								
								delete i2b2.CRC.view.modalLabValues.isModifier;
							
							}
						})
					}, {
					text: "Cancel",
					handler: (function(){ this.cancel(); })			
				}]
			});
			$('itemLabRange').show();
			this.sd.validate = this.ValidateSave;  // attach the validation function from this class
			this.sd.render(document.body);
			// register for actions upon the modal DOM elements
			YAHOO.util.Event.addListener("mlvfrmTypeNONE", "click", this.changeHandler);
			YAHOO.util.Event.addListener("mlvfrmTypeFLAG", "click", this.changeHandler);
			YAHOO.util.Event.addListener("mlvfrmTypeVALUE", "click", this.changeHandler);
			YAHOO.util.Event.addListener("mlvfrmFlagValue", "change", this.changeHandler);
			YAHOO.util.Event.addListener("mlvfrmEnumValue", "change", this.changeHandler);
			YAHOO.util.Event.addListener("mlvfrmOperator", "change", this.changeHandler);
			YAHOO.util.Event.addListener("mlvfrmDbOperator", "change", this.changeHandler);
			YAHOO.util.Event.addListener("mlvfrmStringOperator", "change", this.changeHandler);
			YAHOO.util.Event.addListener("mlvfrmUnits", "change", this.changeHandler);
			YAHOO.util.Event.addListener("mlvfrmStrValue", "keypress", (function(e) {
				// anonymous function
				if (e.keyCode==8 || e.keyCode==46) { return true; }
				var msl = i2b2.CRC.view.modalLabValues.cfgTestInfo.valueValidate.maxString;
				if (!msl || this.value.length < msl) {
					delete i2b2.CRC.view.modalLabValues.formdata.lastValidStr;
					return true;
				} else {
					if (!i2b2.CRC.view.modalLabValues.formdata.lastValidStr) {
						i2b2.CRC.view.modalLabValues.formdata.lastValidStr = this.value;
					}
					return true;
				}
			
			}));
			YAHOO.util.Event.addListener("mlvfrmStrValue", "keyup", (function(e) {
				// anonymous function
				if (i2b2.CRC.view.modalLabValues.formdata.lastValidStr) {
					this.value = i2b2.CRC.view.modalLabValues.formdata.lastValidStr;
				}
			}));
		}
				
		// configure the form
		var mdnodes = i2b2.h.XPath(extData.origData.xmlOrig, 'descendant::metadataxml/ValueMetadata[Version]');
		if (mdnodes.length > 0) {
			this.cfgByMetadata(mdnodes[0]);
		} else {
			// no LabValue configuration
			return false;
		}
		if (i2b2.CRC.view.modalLabValues.isModifier) {
			if (!this.i2b2Data.ModValues && this.i2b2Data.origData.ModValues) {
				// copy server delivered Lab Values to our scope
				this.i2b2Data.ModValues = this.i2b2Data.origData.ModValues;
			}
			var tmpLab = this.i2b2Data.ModValues;
		} else {	
			if (!this.i2b2Data.LabValues && this.i2b2Data.origData.LabValues) {
				// copy server delivered Lab Values to our scope
				this.i2b2Data.LabValues = this.i2b2Data.origData.LabValues;
			}
			var tmpLab = this.i2b2Data.LabValues;
		}
		// load any data already attached to the node
		if (tmpLab) {
			switch(tmpLab.MatchBy) {
				case "FLAG":
					fd.selectedType = "FLAG";
					$("mlvfrmTypeFLAG").checked = true;
					var tn = $("mlvfrmFlagValue");
					for (var i=0; i<tn.options.length; i++) {
						if (tn.options[i].value == tmpLab.ValueFlag) {
							tn.selectedIndex = i;
							fd.flagValue = i;
							break;
						}
					}
					break;
				case "VALUE":					
					fd.selectedType = "VALUE";
					$("mlvfrmTypeVALUE").checked = true;
					// select the correct numeric matching operator
					if (tmpLab.NumericOp) {
						var tn = $("mlvfrmOperator");
						for (var i=0; i<tn.options.length; i++) {
							if (tn.options[i].value == tmpLab.NumericOp) {
								tn.selectedIndex = i;
								fd.numericOperator = tmpLab.NumericOp;
								break;
							}
						}
						// load the values if any
						if (tmpLab.Value) 		{ $('mlvfrmNumericValue').value = tmpLab.Value; }
						if (tmpLab.ValueHigh) 	{ $('mlvfrmNumericValueHigh').value = tmpLab.ValueHigh; }
						if (tmpLab.ValueLow) 	{ $('mlvfrmNumericValueLow').value = tmpLab.ValueLow; }
					}
					if (tmpLab.ValueString) {
						$('mlvfrmStrValue').value = tmpLab.ValueString;
						var tn = $("mlvfrmStringOperator");
						for (var i=0; i<tn.options.length; i++) {
							if (tn.options[i].value == tmpLab.StringOp) {
								tn.selectedIndex = i;
								fd.numericOperator = tmpLab.StringOp;
								break;
							}
						}
					}
					if (tmpLab.DbOp) {
						var tn = $("mlvfrmDbOperator");
						tn.checked = true;
					}
					if (tmpLab.ValueEnum) 	{ 
						var tn = $("mlvfrmEnumValue");
						for (var i=0; i<tn.options.length; i++) {
							if (tmpLab.ValueEnum.indexOf(tn.options[i].text) > -1) {
								tn.options[i].selected = true;
							} else {
								tn.options[i].selected = false;
							}
						}
					}
					break;
			}
		} else {
// snm0
			// set the form to show value selection if available
			$("mlvfrmTypeVALUE").checked = true;
			fd.selectedType = 'VALUE';
// snm0
		}
		// show the form
		this.sd.show();
		this.Redraw();
	},
	
// ================================================================================================== //
	changeHandler: function(e) {
		var dm = i2b2.CRC.view.modalLabValues.cfgTestInfo;
		var fd = i2b2.CRC.view.modalLabValues.formdata;
		if (fd.ignoreChanges) { return true; }
		// get the DOM node that fired the event
		var tn;
		if (e.target) {
			tn = e.target;
		} else {
			if (e.srcElement) tn = e.srcElement;
			if (tn.nodeType == 3) tn = tn.parentNode;
		}
		// process
		switch(tn.id) {
			case "mlvfrmTypeNONE":
				fd.selectedType = 'NONE';
				break;
			case "mlvfrmTypeFLAG":
				fd.selectedType = 'FLAG';
				break;
			case "mlvfrmTypeVALUE":
				fd.selectedType = 'VALUE';
				break;
			case "mlvfrmFlagValue":
				fd.flagValue = tn.options[tn.selectedIndex].value;
				break;
			case "mlvfrmOperator":
				var i1 = $('mlvfrmUnits');
				fd.numericOperator = tn.options[tn.selectedIndex].value;
				fd.valueUnitsCurrent = i1.selectedIndex;
				break;
			case "mlvfrmStringOperator":
				fd.stringOperator = tn.options[tn.selectedIndex].value;	
				break;
			case "mlvfrmDbOperator":
				fd.dbOperator = tn.checked;	
				break;
			case "mlvfrmEnumValue":
				fd.enumIndex = tn.selectedIndex;
				fd.enumValue = tn.options[fd.enumIndex].innerHTML;
				break;
			case "mlvfrmUnits":
				
				var u1 = $('mlvfrmUnits');
				// convert entered values
				var cvD = dm.valueUnits[fd.unitIndex].multFactor;
				var cvM = dm.valueUnits[u1.selectedIndex].multFactor;
				var lst = [$('mlvfrmNumericValue'), $('mlvfrmNumericValueLow'), $('mlvfrmNumericValueHigh')];
				/*
				for (var i=0;i<lst.length;i++) {
					try {
						var t2 = lst[i].value;
						var t = (parseFloat(lst[i].value) / cvD) * cvM;
						if (isNaN(t)) { t = '';	}
						lst[i].value = t;
					} catch(e) {}
				}
				*/
				// save the new Units
				fd.unitIndex = u1.selectedIndex;
				// message if selected Unit is excluded from use
				if (dm.valueUnits[u1.selectedIndex].excluded) {
					Element.show($('mlvUnitExcluded'));
					$('mlvfrmNumericValue').disabled = true;
					$('mlvfrmNumericValueLow').disabled = true;
					$('mlvfrmNumericValueHigh').disabled = true;
				} else {
					Element.hide($('mlvUnitExcluded'));
					$('mlvfrmNumericValue').disabled = false;
					$('mlvfrmNumericValueLow').disabled = false;
					$('mlvfrmNumericValueHigh').disabled = false;
				}	
				
				break;
			default:
				console.warn("onClick element was not captured for ID:"+tn.id)
		}
		tn.blur();
		// save the changes
		i2b2.CRC.view.modalLabValues.formdata = fd;
		i2b2.CRC.view.modalLabValues.Redraw();
	},
	
// ================================================================================================== //
	cfgByMetadata: function(refXML){
		// load and process the xml info
		i2b2.CRC.view.modalLabValues.formdata.ignoreChanges = true;
		var dm = i2b2.CRC.view.modalLabValues.cfgTestInfo;
		var fd = i2b2.CRC.view.modalLabValues.formdata;
		fd.selectedType= "NONE";
		

		// process flag info
		dm.flag = false;
		try { 
			var t = i2b2.h.getXNodeVal(refXML, 'Flagstouse'); 
			if (t) {
				if (t == "A") {
					dm.flagType = 'NA';
					dm.flags = [{name:'Normal', value:'@'},{name:'Abnormal', value:'A'}];
				} else if (t == "HL") {
					dm.flagType = 'HL';
					dm.flags = [{name:'Normal', value:'@'},{name:'High', value:'H'},{name:'Low', value:'L'}];
				} else {
					dm.flagType = false;
				}
			} else {
				dm.flagType = false;
			}

			// insert the flags into the range select control
			var sn = $('mlvfrmFlagValue');
			while( sn.hasChildNodes() ) { sn.removeChild( sn.lastChild ); }
			for (var i=0; i<dm.flags.length; i++) {
				// ONT options dropdown
				var sno = document.createElement('OPTION');
				sno.setAttribute('value', dm.flags[i].value);
				var snt = document.createTextNode(dm.flags[i].name);
				sno.appendChild(snt);
				sn.appendChild(sno);
			}
		} catch(e) { 
			var t = false;
			dm.flags = [];
		}
		// work with the data type
		dm.enumInfo = [];
		dm.valueUnits = [];
		try {
			var t = i2b2.h.getXNodeVal(refXML, 'DataType');
			switch(t) {
				case "PosFloat":
					dm.valueType = "POSFLOAT";
					dm.valueValidate.onlyPos = true;
					dm.valueValidate.onlyInt = false;
					dm.valueValidate.maxString = false; 
					break;
				case "PosInteger":
					dm.valueType = "POSINT";
					dm.valueValidate.onlyPos = true;
					dm.valueValidate.onlyInt = true;
					dm.valueValidate.maxString = false; 
					break;
				case "Float":
					dm.valueType = "FLOAT";
					dm.valueValidate.onlyPos = false;
					dm.valueValidate.onlyInt = false;
					dm.valueValidate.maxString = false; 
					break;
				case "Integer":
					dm.valueType = "INT";
					dm.valueValidate.onlyPos = true;
					dm.valueValidate.onlyInt = true;
					dm.valueValidate.maxString = false; 
					break;
				case "String":
					dm.valueType = "STR";
					dm.valueValidate.onlyPos = false;
					dm.valueValidate.onlyInt = false;
					// extract max string setting
					try {
						var t = refXML.getElementsByTagName('MaxStringLength')[0].firstChild.nodeValue;
						t = parseInt(t);
					} catch(e) { 
						var t = -1;
					}
					if (t > 0) {
						dm.valueValidate.maxString = t;
					} else {
						dm.valueValidate.maxString = false;
					}
					break;
				case "largestring":
					dm.valueType = "LRGSTR";
					dm.valueValidate.onlyPos = false;
					dm.valueValidate.onlyInt = false;
					// extract max string setting
					try {
						var t = refXML.getElementsByTagName('MaxStringLength')[0].firstChild.nodeValue;
						t = parseInt(t);
					} catch(e) { 
						var t = -1;
					}
					if (t > 0) {
						dm.valueValidate.maxString = t;
					} else {
						dm.valueValidate.maxString = false;
					}
					break;					
				case "Enum":
					dm.valueType = "ENUM";
					dm.valueValidate.onlyPos = false;
					dm.valueValidate.onlyInt = false;
					dm.valueValidate.maxString = false;
					// extract the enum data
					var t1 = i2b2.h.XPath(refXML,"descendant::EnumValues/Val");
					//var t = i2b2.h.XPath(refXML,"descendant::EnumValues/Val/text()");
					//var t2 = [];
					var sn = $('mlvfrmEnumValue');
					// clear the drop down
					while( sn.hasChildNodes() ) { sn.removeChild( sn.lastChild ); }			
					
					var t2 = new Array();
					for (var i=0; i<t1.length; i++) {
						if (t1[i].attributes[0].nodeValue != "" ) {
							//t2.push(t[i].attributes[0].nodeValue);
							var name = t1[i].attributes[0].nodeValue;
						} else {
							//t2.push(t[i].childNodes[0].nodeValue);
							var name = t1[i].childNodes[0].nodeValue;
						}
						t2[(t1[i].childNodes[0].nodeValue)] = name;
						

						var sno = document.createElement('OPTION');
						sno.setAttribute('value', (t1[i].childNodes[0].nodeValue));
						var snt = document.createTextNode(name);
						sno.appendChild(snt);
						sn.appendChild(sno);
							
					}
					dm.enumInfo = t2;

					// remove any Enums found in <CommentsDeterminingExclusion> section
					
					var t = i2b2.h.XPath(refXML,"descendant::CommentsDeterminingExclusion/Com/text()");
					var t2 = [];
					for (var i=0; i<t.length; i++) {
						t2.push(t[i].nodeValue);
					}
					t = t2.uniq();
					if (t.length > 0) {
						for (var i=0;i<t.length; i++){
							for (var i2=0;i2<dm.enumInfo.length; i2++) {
								if (dm.enumInfo[i2].indexOf(t[i]) > -1 ) {
									dm.enumInfo[i2] = null;
								}
							}
							// clean up the array
							dm.enumInfo = dm.enumInfo.compact();
						}
					}
					// clear & populate the Enum dropdown
					// populate values
					var count = 0;
					//for (var i in dm.enumInfo) {
					
					/*for (var i in dm.enumInfo) {
					//for (var i=0; i<dm.enumInfo.length; i++) {
						var sno = document.createElement('OPTION');
						sno.setAttribute('value', i);
						var snt = document.createTextNode(dm.enumInfo[i]);
						sno.appendChild(snt);
						sn.appendChild(sno);
						count ++;
						//mm  if (count == t1.length) {break;}
					}*/
					break;
				default:
					dm.valueType = false;
			}
		} catch(e) {
			dm.valueType = false;
			dm.valueValidate.onlyPos = false;
			dm.valueValidate.onlyInt = false;
			dm.valueValidate.maxString = false; 
			$('mlvfrmTypeVALUE').parentNode.hide();
		}
	
		// set the title bar (TestName and TestID are assumed to be mandatory)
		this.sd.setHeader("Choose value of "+i2b2.h.getXNodeVal(refXML, 'TestName')+" (Test:"+i2b2.h.getXNodeVal(refXML, 'TestID')+")");
	
		$('mlvfrmTypeNONE').nextSibling.nodeValue = "No Value";
		$('mlvfrmTypeFLAG').nextSibling.nodeValue = "By Flag"; // snm0
		$('mlvfrmTypeVALUE').nextSibling.nodeValue = "By Value";
	
		if (dm.valueType == "LRGSTR") {
			$('valueContraintText').innerHTML = "You are allowed to search within the narrative text associated with the term " + i2b2.h.getXNodeVal(refXML, 'TestName');
			this.sd.setHeader("Search within the "+i2b2.h.getXNodeVal(refXML, 'TestName'));
			$('mlvfrmTypeNONE').nextSibling.nodeValue = "No Search Requested";
			$('mlvfrmTypeVALUE').nextSibling.nodeValue = "Search within Text";
		} else if (i2b2.CRC.view.modalLabValues.isModifier) {
				$('valueContraintText').innerHTML = "Searches by Modifier values can be constrained by either a flag set by the sourcesystem or by the values themselves.";
		} else {
			 $('valueContraintText').innerHTML = "Searches by Lab values can be constrained by the high/low flag set by the performing laboratory, or by the values themselves.";
		}	
	
		// extract and populate unit info for all dropdowns
		var tProcessing = new Hash();
		try {
			// save list of all possible units (from)
			var t = i2b2.h.XPath(refXML,"descendant::UnitValues/descendant::text()[parent::NormalUnits or parent::EqualUnits or parent::Units]");
			var t2 = [];
			for (var i=0; i<t.length; i++) {
				t2.push(t[i].nodeValue);
			}
			t = t2.uniq();
			for (var i=0;i<t.length;i++) {
				var d = {name: t[i]};
				// is unit excluded?
				//if (i2b2.h.XPath(refXML,"descendant::UnitValues/descendant::ExcludingUnits[text()='"+t[i]+"']").length>0) {
				//	d.excluded = true;
				//}
				
				// Equal Units
				//if (i2b2.h.XPath(refXML,"descendant::UnitValues/descendant::ExcludingUnits[text()='"+t[i]+"']").length>0) {
				//	d.excluded = true;
				//}
				
				// does unit require conversion?
				try {
					d.multFactor = i2b2.h.XPath(refXML,"descendant::UnitValues/descendant::ConvertingUnits[Units/text()='"+t[i]+"']/MultiplyingFactor/text()")[0].nodeValue;
				} catch(e) {
					d.multFactor = 1;
				}
				tProcessing.set(t[i], d);
			}
			// get our master unit (the first NormalUnits encountered that is not disabled)
			var t = i2b2.h.XPath(refXML,"descendant::UnitValues/descendant::NormalUnits/text()");
			var t2 = [];
			for (var i=0; i<t.length; i++) {
				t2.push(t[i].nodeValue);
			}
			t = t2.uniq();
			var masterUnit = false;
			for (var i=0;i<t.length;i++) {
				var d = tProcessing.get(t[i]);
				if (!d.excluded && d.multFactor==1) {
					masterUnit = t[i];
					d.masterUnit = true;
					tProcessing.set(t[i], d);
					break;
				}
			}
			if (!masterUnit) {
				masterUnit = t[0];
				if (masterUnit) {
					var d = tProcessing.get(masterUnit);
					d.masterUnit = true;
					d.masterUnitViolation = true;
					tProcessing.set(masterUnit, d);
				}
			}
		} catch(e) { 
			console.error("Problem was encountered when processing given Units");
		}

		dm.valueUnits = tProcessing.values();

		// update the unit drop downs
		var ud = [ $('mlvfrmUnits')];
		for (var cud=0; cud < ud.length; cud++) {
			var sn = ud[cud];
			// clear the drop down
			while( sn.hasChildNodes() ) { sn.removeChild( sn.lastChild ); }			
			// populate values
			for (var i=0; i<dm.valueUnits.length; i++) {
				var sno = document.createElement('OPTION');
				sno.setAttribute('value', i);
				if (dm.valueUnits[i].masterUnit) { sno.setAttribute('selected', true); }				
				var snt = document.createTextNode(dm.valueUnits[i].name);
				sno.appendChild(snt);
				sn.appendChild(sno);
			}
		}
		// hide or show DIV
		if (dm.valueUnits.length==0) {
			Element.hide($('mlvfrmUnitsContainer'));
		} else {
			// message if selected Unit is excluded from use
			if (dm.valueUnits[ud[0].options[ud[0].selectedIndex].value].excluded) {
				Element.show($('mlvUnitExcluded'));
				$('mlvfrmNumericValue').disabled = true;
				$('mlvfrmNumericValueLow').disabled = true;
				$('mlvfrmNumericValueHigh').disabled = true;
			} else {
				Element.hide($('mlvUnitExcluded'));
				$('mlvfrmNumericValue').disabled = false;
				$('mlvfrmNumericValueLow').disabled = false;
				$('mlvfrmNumericValueHigh').disabled = false;
			}
			Element.show($('mlvfrmUnitsContainer'));
		}

// snm0		
		// Extract the value range info and display it on the range bar
		// The bar is 520 pixels long, fixed  
		//
		var nBarLength = 520; // fixed width of bar
		fd.bHidebar = false;  // set to true if decide bar not worth showing
		var nSituation = 0; // how many values are there?
		dm.rangeInfo = {};
		//
		// get preliminary bar length results and set up array
		try {
			dm.rangeInfo.LowOfToxic = parseFloat(refXML.getElementsByTagName('LowofToxicValue')[0].firstChild.nodeValue);
			nSituation = nSituation +1;
		} catch(e) {}
		try {
			dm.rangeInfo.LowOfLow = parseFloat(refXML.getElementsByTagName('LowofLowValue')[0].firstChild.nodeValue);
			if ((isFinite(dm.rangeInfo.LowOfToxic)) && (dm.rangeInfo.LowOfToxic == dm.rangeInfo.LowOfLow)) {
				dm.rangeInfo.LowOfLowRepeat = true;
			}
			else {
				dm.rangeInfo.LowOfLowRepeat = false;
				nSituation = nSituation +1;
			}
		} catch(e) {}
		try {
			dm.rangeInfo.HighOfLow = parseFloat(refXML.getElementsByTagName('HighofLowValue')[0].firstChild.nodeValue);	
			if ((isFinite(dm.rangeInfo.LowOfLow)) && (dm.rangeInfo.LowOfLow == dm.rangeInfo.HighOfLow)) {
				dm.rangeInfo.HighOfLowRepeat = true;
			}
			else {
				dm.rangeInfo.HighOfLowRepeat = false;
				nSituation = nSituation +1;
			}
		} catch(e) {}
		try {
			dm.rangeInfo.HighOfToxic = parseFloat(refXML.getElementsByTagName('HighofToxicValue')[0].firstChild.nodeValue);
			nSituation = nSituation +1;
		} catch(e) {}
		try {
			dm.rangeInfo.HighOfHigh = parseFloat(refXML.getElementsByTagName('HighofHighValue')[0].firstChild.nodeValue);
			if ((isFinite(dm.rangeInfo.HighOfToxic)) && (dm.rangeInfo.HighOfToxic == dm.rangeInfo.HighOfHigh)) {
				dm.rangeInfo.HighOfHighRepeat = true;
			}
			else {
				dm.rangeInfo.HighOfHighRepeat = false;
				nSituation = nSituation +1;
			}
		} catch(e) {}
		try {
			dm.rangeInfo.LowOfHigh = parseFloat(refXML.getElementsByTagName('LowofHighValue')[0].firstChild.nodeValue);
			if ((isFinite(dm.rangeInfo.HighOfHigh)) && (dm.rangeInfo.HighOfHigh == dm.rangeInfo.LowOfHigh)) {
				dm.rangeInfo.LowOfHighhRepeat = true;
			}
			else {
				dm.rangeInfo.LowOfHighRepeat = false;
				nSituation = nSituation +1;
			}
		} catch(e) {}
		//
		// get full situation of bar to be shown
		try {
			if (nSituation != 0) {
				var nPixelPerBar = nBarLength / (nSituation + 1);
				$('lblNorm').style.width = nPixelPerBar + "px";
				$('barNorm').style.width = nPixelPerBar + "px";	
				if (isFinite(dm.rangeInfo.LowOfToxic)) {
					$('lblToxL').innerHTML = dm.rangeInfo.LowOfToxic;
					$('lblToxL').style.width = nPixelPerBar + "px";
					$('barToxL').style.width = nPixelPerBar + "px";
				}
				else {
					$('lblToxL').innerHTML = "";
					$('lblToxL').style.width = "0px";
					$('barToxL').style.width = "0px";
				}
				if (isFinite(dm.rangeInfo.LowOfLow) && (dm.rangeInfo.LowOfLowRepeat == false)) {
					$('lblLofL').innerHTML = dm.rangeInfo.LowOfLow;
					$('lblLofL').style.width = nPixelPerBar + "px";
					$('barLofL').style.width = nPixelPerBar + "px";
				}
				else {
					$('lblLofL').innerHTML = "";
					$('lblLofL').style.width = "0px";
					$('barLofL').style.width = "0px";
				}
				if (isFinite(dm.rangeInfo.HighOfLow) && (dm.rangeInfo.HighOfLowRepeat == false)) {
					$('lblHofL').innerHTML = dm.rangeInfo.HighOfLow;
					$('lblHofL').style.width = nPixelPerBar + "px";
					$('barHofL').style.width = nPixelPerBar + "px";
				}
				else {
					$('lblHofL').innerHTML = "";
					$('lblHofL').style.width = "0px";
					$('barHofL').style.width = "0px";
				}
				if (isFinite(dm.rangeInfo.LowOfHigh) && (dm.rangeInfo.LowOfHighRepeat == false)) {
					$('lblLofH').innerHTML = dm.rangeInfo.LowOfHigh;
					$('lblLofH').style.width = nPixelPerBar + "px";
					$('barLofH').style.width = nPixelPerBar + "px";
				}
				else {
					$('lblLofH').innerHTML = "";
					$('lblLofH').style.width = "0px";
					$('barLofH').style.width = "0px";
				}
				if (isFinite(dm.rangeInfo.HighOfHigh) && (dm.rangeInfo.HighOfHighRepeat == false)) {
					$('lblHofH').innerHTML = dm.rangeInfo.HighOfHigh;
					$('lblHofH').style.width = nPixelPerBar + "px";
					$('barHofH').style.width = nPixelPerBar + "px";
				}
				else {
					$('lblHofH').innerHTML = "";
					$('lblHofH').style.width = "0px";
					$('barHofH').style.width = "0px";
				}
				if (isFinite(dm.rangeInfo.HighOfToxic)) {
					$('lblToxH').innerHTML = dm.rangeInfo.HighOfToxic;
					$('lblToxH').style.width = nPixelPerBar + "px";
					$('barToxH').style.width = nPixelPerBar + "px";
				}
				else {
					$('lblToxH').innerHTML = "";
					$('lblToxH').style.width = "0px";
					$('barToxH').style.width = "0px";
				}
			}
			else {
				fd.bHidebar = true;
			}
		} 
		catch(e) {
		   	var errString = "Description: " + e.description;
			alert(errString);
		}
		// show the right parts of the form
		if (dm.valueType) {
			$('mlvfrmTypeVALUE').checked = true;
			$('mlvfrmFLAG').hide();
			$('mlvfrmVALUE').show();
		}
		else if (dm.flagType) {
			$('mlvfrmTypeFLAG').checked = true;
			$('mlvfrmFLAG').show();
			$('mlvfrmVALUE').hide();
		}
		else {
			$('mlvfrmTypeNONE').checked = true;
			$('mlvfrmFLAG').hide();
			$('mlvfrmVALUE').hide();
		}
// snm0
		// clear the other data input elements
		$('mlvfrmOperator').selectedIndex = 0;
		$('mlvfrmStringOperator').selectedIndex = 0;
		$('mlvfrmFlagValue').selectedIndex = 0;
		$('mlvfrmNumericValueLow').value = '';
		$('mlvfrmNumericValueHigh').value = '';
		$('mlvfrmNumericValue').value = '';
		$('mlvfrmStrValue').value = '';
		$('mlvfrmDbOperator').checked = false;
		$('mlvfrmEnumValue').selectedIndex = 0;

		// save the initial values into the data model
		var tn = $("mlvfrmOperator");
		fd.numericOperator = tn.options[tn.selectedIndex].value;
		var tn = $("mlvfrmStringOperator");
		fd.stringOperator = tn.options[tn.selectedIndex].value;		
		var tn = $("mlvfrmOperator");
		fd.flagValue = tn.options[tn.selectedIndex].value;
		fd.unitIndex = $('mlvfrmUnits').selectedIndex;
		fd.dbOperator = $("mlvfrmDbOperator").checked;
		i2b2.CRC.view.modalLabValues.formdata.ignoreChanges = false;
		i2b2.CRC.view.modalLabValues.setUnits();
		i2b2.CRC.view.modalLabValues.Redraw();
	},
	
// ================================================================================================== //
	setUnits: function(newUnitIndex) {
		// this function is used to change all the dropdowns and convert the range values
		if (!newUnitIndex) { newUnitIndex = this.formdata.unitIndex; }
		if (newUnitIndex==-1) { return; }
		var dm = this.cfgTestInfo;
		var ri = this.cfgTestInfo.rangeInfo;
		var cv = dm.valueUnits[newUnitIndex].multFactor;
		var t;
		var el;
		$('mlvfrmLblUnits').innerHTML = dm.valueUnits[newUnitIndex].name;
		try {
			t = dm.rangeInfo.LowOfLow * cv;
		} catch(e) {}
		try {
			t = dm.rangeInfo.HighOfLow * cv;
			if (isNaN(t)) { t = '';	}
			el = $("mlvfrmLblHighOfLow");
			el.innerHTML = t;
			el.style.left = (Element.getWidth(el)/ 2);
		} catch(e) {}
		try {
			t = dm.rangeInfo.LowOfHigh * cv;
			if (isNaN(t)) { t = '';	}
			el = $("mlvfrmLblLowOfHigh");
			el.innerHTML = t;
			el.style.left = (Element.getWidth(el)/ 2);
		} catch(e) {}
		try {
			t = dm.rangeInfo.HighOfHigh * cv;
		} catch(e) {}
		try {
			t = dm.rangeInfo.LowOfToxic * cv;
			if (isNaN(t)) { t = '';	}
			el = $("mlvfrmLblLowToxic");
			el.innerHTML = t;
			el.style.left = (Element.getWidth(el)/ 2);
		} catch(e) {}
		try {
			t = dm.rangeInfo.LowOfLowValue * cv;
			if (isNaN(t)) { t = '';	}
			el = $("mlvfrmLblHighToxic");
			el.innerHTML = t;
			el.style.left = (Element.getWidth(el)/ 2);
		} catch(e) {}
	},
	
// ================================================================================================== //
	Redraw: function(){
		if (i2b2.CRC.view.modalLabValues.formdata.ignoreChanges) return;
		i2b2.CRC.view.modalLabValues.formdata.ignoreChanges = true;
		var fd = i2b2.CRC.view.modalLabValues.formdata;
		var dm = i2b2.CRC.view.modalLabValues.cfgTestInfo;
		// hide show radios according to configuration
		if (dm.valueType) {
			Element.show($('mlvfrmTypeVALUE').parentNode);
//			$('mlvfrmTypeVALUE').parentNode.show();		
		} else {
			if (fd.selectedType == "VALUE") {
// snm0
				// when value is available, always show the value dialog
				$('mlvfrmTypeVALUE').checked=true;
				fd.selectedType= "VALUE";
// snm0
			}
			Element.hide($('mlvfrmTypeVALUE').parentNode);
//			$('mlvfrmTypeVALUE').parentNode.hide();
		}
		if (dm.flagType) {
			Element.show($('mlvfrmTypeFLAG').parentNode);
//			$('mlvfrmTypeFLAG').parentNode.show();			
		} else {
			if (fd.selectedType == "FLAG") {
				$('mlvfrmTypeNONE').checked=true;
				fd.selectedType = "NONE";
			}
			Element.hide($('mlvfrmTypeFLAG').parentNode);
//			$('mlvfrmTypeFLAG').parentNode.hide();
		}

		// redraw the info panel according to saved selection value (radio selectors)
		switch (fd.selectedType) {
			case "NONE":
				$('mlvfrmFLAG').hide();
				$('mlvfrmVALUE').hide();
// snm0
				$('mlvfrmBarContainer').hide();
				$('mlvfrmUnitsContainer').hide();
//
				break;
			case "FLAG":
				$('mlvfrmVALUE').hide();
				$('mlvfrmFLAG').show();
// snm0
				$('mlvfrmBarContainer').hide();
				$('mlvfrmUnitsContainer').hide();
//
				break;
			case "VALUE":
				$('mlvfrmVALUE').show();
				$('mlvfrmFLAG').hide();
				// hide all inputs panels
				$('mlvfrmEnterOperator').hide();
				$('mlvfrmEnterStringOperator').hide();					
				$('mlvfrmEnterVal').hide();
				$('mlvfrmEnterVals').hide();
				$('mlvfrmEnterStr').hide();
				$('mlvfrmEnterEnum').hide();
				$('mlvfrmEnterDbOperator').hide();
// snm0
				$('mlvfrmBarContainer').hide();
				$('mlvfrmUnitsContainer').hide();
//
				// display what we need
				switch(dm.valueType) {
					case "POSFLOAT":
					case "POSINT":
					case "FLOAT":
					case "INT":
						$('mlvfrmEnterOperator').show();
						// are we showing two input boxes?
						if (fd.numericOperator=="BETWEEN") {
							$('mlvfrmEnterVals').show();
						} else {
							$('mlvfrmEnterVal').show();
						}
						i2b2.CRC.view.modalLabValues.setUnits();
// snm0
						// this is the only location that determines to show the 
						// value bar
						if (fd.bHidebar) {
							$('mlvfrmBarContainer').hide();
						}
						else {
							$('mlvfrmBarContainer').show();
						}
						$('mlvfrmUnitsContainer').show();
//
						break;
					case "LRGSTR":
						$('mlvfrmEnterStr').show();
						$('mlvfrmEnterDbOperator').show();
						break;
					case "STR":
						$('mlvfrmEnterStringOperator').show();
						$('mlvfrmEnterStr').show();
						break;
					case "ENUM":
						$('mlvfrmEnterStr').hide();
						$('mlvfrmEnterEnum').show();
						break;
				}
				break;
		}			
		i2b2.CRC.view.modalLabValues.formdata.ignoreChanges = false;
	},
	
// ================================================================================================== //
	ValidateSave: function() {
		var dm = i2b2.CRC.view.modalLabValues.cfgTestInfo;
		var fd = i2b2.CRC.view.modalLabValues.formdata;
		var tmpLabValue = {};
		var errorMsg = [];
		switch (fd.selectedType) {
			case "NONE":
			    if (i2b2.CRC.view.modalLabValues.isModifier) {
					delete i2b2.CRC.view.modalLabValues.i2b2Data.ModValues;
				} else {
					delete i2b2.CRC.view.modalLabValues.i2b2Data.LabValues;					
				}
				return true;
				break;
			case "FLAG":
				tmpLabValue.MatchBy = "FLAG";
				var tn = $('mlvfrmFlagValue');
				tmpLabValue.ValueFlag = tn.options[tn.selectedIndex].value;
				tmpLabValue.FlagsToUse = dm.flagType;
				break;
			case "VALUE":
				tmpLabValue.MatchBy = "VALUE";
				// validate the data entry boxes
				switch(dm.valueType) {
					case "POSFLOAT":
					case "POSINT":
					case "FLOAT":
					case "INT":
						tmpLabValue.GeneralValueType = "NUMBER";
						tmpLabValue.SpecificValueType = dm.valueType;
						var valInputs = [];
						tmpLabValue.NumericOp = fd.numericOperator;
						if (fd.numericOperator=="BETWEEN") {
							// verify that Low/High are correct
							var iv1 = $('mlvfrmNumericValueLow');
							var iv2 = $('mlvfrmNumericValueHigh');							
							iv1.value = iv1.value.strip();
							iv2.value = iv2.value.strip();
//snm0
							if (iv1.value == "") {
								tmpLabValue.ValueLow = "";
								valInputs.push(NaN);
							}
							else {
								tmpLabValue.ValueLow = Number(iv1.value);
								valInputs.push(iv1);
							}
							if (iv2.value == "") {
								tmpLabValue.ValueHigh = "";
								valInputs.push(NaN);
							}
							else {
								tmpLabValue.ValueHigh = Number(iv2.value);
								valInputs.push(iv2);
							}
							tmpLabValue.UnitsCtrl = $('mlvfrmUnits'); 
						} else {
							var iv1 = $('mlvfrmNumericValue');
							iv1.value = iv1.value.strip();
							if (iv1.value == "") {
								tmpLabValue.Value = "";
								valInputs.push(NaN);
							}
							else {
								tmpLabValue.Value = Number(iv1.value);
								valInputs.push(iv1);
							}
//snm0
							tmpLabValue.UnitsCtrl = $('mlvfrmUnits'); 
						}
						// loop through all the 
						for(var i=0; i<valInputs.length; i++){
							var tn = Number(valInputs[i].value);
							if (!isFinite(tn)) {
								errorMsg.push(" - One or more inputs are not a valid number\n");	
							}
							if (dm.valueValidate.onlyInt) {
								if (parseInt(valInputs[i].value) != valInputs[i].value) {
									errorMsg.push(" - One or more inputs are not integers\n");	
								}
							}
							if (dm.valueValidate.onlyPos) {
								if (parseFloat(valInputs[i].value) < 0) {
									errorMsg.push(" - One or more inputs have a negative value\n");	
								}
							}
						}
						// make sure the values are in the correct order
						if (fd.numericOperator=="BETWEEN" && (parseFloat(iv1) > parseFloat(iv2))) {
							errorMsg.push(" - The low value is larger than the high value\n");
						}
//snm0						
						// CONVERT VALUES TO MASTER UNITS
						if (fd.unitIndex == -1) { // no units were in XML
							tmpLabValue.UnitsCtrl = "";
							break;
						}
						if ((dm.valueUnits[fd.unitIndex] == undefined) || (dm.valueUnits[fd.unitIndex] == "")) {
							alert('The units for this value are blank.');
							return false;
						}
// snm0
						if (dm.valueUnits[fd.unitIndex].excluded) {
							alert('You cannot set a numerical value using the current Unit Of Measure.');
							return false;
						}
						if (dm.valueUnits.find(function(o){ return ((o.masterUnit === true) && (o.excluded===true)); })) {
							alert('You cannot set a numerical value because the master Unit Of Measure is declared as invalid.');
							return false;
						}
						try {
							var convtMult = dm.valueUnits[fd.unitIndex].multFactor;
							if (tmpLabValue.ValueHigh) tmpLabValue.ValueHigh = (tmpLabValue.ValueHigh * convtMult);
							if (tmpLabValue.ValueLow) tmpLabValue.ValueLow = (tmpLabValue.ValueLow * convtMult);
							if (tmpLabValue.Value) tmpLabValue.Value = (tmpLabValue.Value * convtMult);
							for (var i=0; i<dm.valueUnits.length;i++){
								if (dm.valueUnits[i].masterUnit) {
									tmpLabValue.UnitsCtrl = dm.valueUnits[i].name;
									break;
								}
							}
						} catch(e) {
							alert('An error was encountered while converting Units!');
							return false;
						}
						break;
					case "LRGSTR":
						tmpLabValue.GeneralValueType = "LARGESTRING";
						tmpLabValue.SpecificValueType = "LARGESTRING";
						tmpLabValue.DbOp = fd.dbOperator;
						var sv = $('mlvfrmStrValue').value;
						if (dm.valueValidate.maxString && (sv.length > dm.valueValidate.maxString)) {
							errorMsg.push(" - Input is over the "+dm.valueValidate.maxString+" character limit.\n");
						} else if (sv.length == 0) {
							errorMsg.push("The text for this value are blank.");
						} else {
							tmpLabValue.ValueString = $('mlvfrmStrValue').value;
						}
						break;						
					case "STR":
						tmpLabValue.GeneralValueType = "STRING";
						tmpLabValue.SpecificValueType = "STRING";
						tmpLabValue.StringOp = fd.stringOperator;
						var sv = $('mlvfrmStrValue').value;
						if (dm.valueValidate.maxString && (sv.length > dm.valueValidate.maxString)) {
							errorMsg.push(" - Input is over the "+dm.valueValidate.maxString+" character limit.\n");
						} else if (sv.length == 0) {
							errorMsg.push("The text for this value are blank.");
						} else {
							tmpLabValue.ValueString = $('mlvfrmStrValue').value;
						}
						break;
					case "ENUM":
						tmpLabValue.GeneralValueType = "ENUM";
						tmpLabValue.SpecificValueType = "ENUM";
						tmpLabValue.ValueEnum = [];
						tmpLabValue.NameEnum = [];
						var t = $('mlvfrmEnumValue').options;
						for (var i=0; i<t.length;i++) {
							if (t[i].selected) {
								tmpLabValue.ValueEnum.push(t[i].value); //dm.enumInfo[t[i].value]);
								tmpLabValue.NameEnum.push(t[i]);
							}
						}
						break;
				}
				break;
		}
		// bail on errors
		if (errorMsg.length != 0) {
			var errlst = errorMsg.uniq();
			var errlst = errlst.toString();
			alert('The following errors have occurred:\n'+errlst);
			delete tmpLabValue;
			return false;
		}
		// save the labValues data into the node's data element
		if (i2b2.CRC.view.modalLabValues.isModifier) {
			if (tmpLabValue) {
				i2b2.CRC.view.modalLabValues.i2b2Data.ModValues = tmpLabValue;
			} else {
				delete i2b2.CRC.view.modalLabValues.i2b2Data.ModValues;
			}
		} else { 
			if (tmpLabValue) {
				i2b2.CRC.view.modalLabValues.i2b2Data.LabValues = tmpLabValue;
			} else {
				delete i2b2.CRC.view.modalLabValues.i2b2Data.LabValues;
			}
		}
		return true;
	}
}
