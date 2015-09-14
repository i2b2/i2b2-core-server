	/**
 * @projectDescription	Event controller for Query Tool's three query panels. (GUI-only controller).
 * @inherits 	
 * @namespace	
 * @author		Nick Benik, Griffin Weber MD PhD
 * @version 	1.3
 * ----------------------------------------------------------------------------------------
 * updated 9-15-08: RC4 launch [Nick Benik] 
 */
console.group('Load & Execute component file: CRC > ctrlr > QueryPanel');
console.time('execute time');


function i2b2_PanelController(parentCtrlr) {
	// this is the base class for the single panel controllers
	this.panelCurrentIndex = false;
	this.actualPanelIndex = false;
	this.QTController = parentCtrlr;
	this.refTitle = undefined;
	this.refButtonExclude = undefined;
	this.refButtonDates = undefined;
	this.refButtonOccurs = undefined;
	this.refButtonOccursNum = undefined;
	this.refDispContents = undefined;
	this.refButtonTiming = undefined;
	this.refBalloon = undefined;
	this.itemNumber = 1;
	
	var Event = YAHOO.util.Event;

// ================================================================================================== //
	this.doRedraw = function() {
		if (this.panelCurrentIndex===false) { return true; }
		if (!i2b2.CRC.model.queryCurrent.panels) { 
			i2b2.CRC.model.queryCurrent.panels = [];
			i2b2.CRC.model.queryCurrent.panels[0] = new Array();
			i2b2.CRC.model.queryCurrent.panels[1] = new Array();
			i2b2.CRC.model.queryCurrent.panels[2] = new Array();
			// new Array(new Array());
		}
		var dm = i2b2.CRC.model.queryCurrent;
		
		// retreve/initialize the display data
//		if ((this.panelCurrentIndex < dm.panels[i2b2.CRC.ctrlr.QT.temporalGroup].length)
//		|| (this.panelCurrentIndex == 0)) {
		var pd = dm.panels[i2b2.CRC.ctrlr.QT.temporalGroup][this.panelCurrentIndex];
		this._redrawPanelStyle(pd);
		// protip: use null data to get other redraws to work
		if (undefined===pd) {
			pd = {};
			pd._nullRecord = true;
			pd.dateTo = false;
			pd.dateFrom = false;
			pd.exclude = false;
			pd.timing = i2b2.CRC.ctrlr.QT.queryTiming; //'ANY';
			pd.occurs = '0';
			pd.items = [];
		}
		// do redraw
		this._redrawTree(pd);
		this._redrawButtons(pd);
		this._redrawTiming(pd);
		//}
	}

// ================================================================================================== //
	this._redrawPanelStyle = function(pd) {
		if (undefined===pd) {
			// disable visual changes on hover
			Element.removeClassName(this.refButtonExclude,'queryPanelButtonHover');
			Element.removeClassName(this.refButtonOccurs,'queryPanelButtonHover');
			Element.removeClassName(this.refButtonDates,'queryPanelButtonHover');
			Element.removeClassName(this.refButtonTiming,'queryPanelButtonHover');
			Element.removeClassName(this.refButtonExclude,'queryPanelButtonSelected');
			Element.removeClassName(this.refButtonOccurs,'queryPanelButtonSelected');
			Element.removeClassName(this.refButtonDates,'queryPanelButtonSelected');
			Element.removeClassName(this.refButtonTiming,'queryPanelButtonSelected');
			
			// is this panel one up from the max number of panels?
			if (this.panelCurrentIndex == i2b2.CRC.model.queryCurrent.panels[i2b2.CRC.ctrlr.QT.temporalGroup].length) {
				this.isActive = 'Y';
				if (this.QTController.queryTiming == "SAME") {
					this.refButtonTiming.set('disabled', true);
					//MM $("queryPanelTimingB" + (this.panelCurrentIndex+1) +  "-button").disabled = false;			
				}
				Element.removeClassName(this.refDispContents,'queryPanelHover');
				Element.removeClassName(this.refDispContents,'queryPanelDisabled');
				this.refBalloon.style.display = 'block';
				this.refBalloon.innerHTML = 'drop a<br />term<br />on here';
				this.refBalloon.style.background = '#FFFF99';
			} else {
				this.isActive = 'N';
				Element.addClassName(this.refDispContents,'queryPanelDisabled');
				this.refBalloon.style.display = 'none';
			}
		} else {
			// enable visual changes on hover
			Element.addClassName(this.refButtonExclude,'queryPanelButtonHover');
			Element.addClassName(this.refButtonTiming,'queryPanelButtonHover');
			Element.addClassName(this.refButtonOccurs,'queryPanelButtonHover');
			Element.addClassName(this.refButtonDates,'queryPanelButtonHover');
			this.isActive = 'Y';
			Element.removeClassName(this.refDispContents,'queryPanelHover');
			Element.removeClassName(this.refDispContents,'queryPanelDisabled');
			this.refBalloon.style.display = 'block';
		}
	}

// ================================================================================================== //
	this._redrawTree = function(pd) {
		if (undefined===pd.tvRootNode) {
			pd.tvRootNode = new YAHOO.widget.RootNode(this.yuiTree);
		}
		// reconnect the root node with the treeview
		YAHOO.widget.TreeView.attachRootNodeToTree(pd.tvRootNode, this.yuiTree);
		// cause the treeview to redraw
		this.yuiTree.draw();
		for (var i=0; i<pd.tvRootNode.children.length; i++) {
			// fix the folder icon for expanded folders
			var n = pd.tvRootNode.children[i];
			this._redrawTreeFix.call(this, n);
		}
	}

// ================================================================================================== //
	this._redrawTreeFix = function(tvNode) {
		// this is a recursive function used to fix all the folder images in a treeview after initial redraw
		if (!tvNode.tree.locked && (tvNode.hasChildren(true))) {
			if (tvNode.children.length > 0 && tvNode.expanded) {
				var imgs = $(tvNode.contentElId);
				if (imgs) { 
					imgs = imgs.select('img'); 
				} else {
					imgs = [];
				}
				if (imgs.length > 0) { 
					var isrc = imgs[0].getAttribute('src').replace('.gif','-exp.gif');
					imgs[0].setAttribute('src', isrc);
				}
			}
			if (!tvNode.isLeaf) {
				tvNode._dynLoad = true;
				// reattach the dynamic load event if it was lost
				this.yuiTree.setDynamicLoad(i2b2.CRC.ctrlr.QT._loadTreeDataForNode,1);
			}
			for (var i=0; i<tvNode.children.length; i++) {
				this._redrawTreeFix.call(this, tvNode.children[i]);
			}
		}
	}

// ================================================================================================== //
	this._redrawTiming = function(pd) {
		// set panel GUI according to data in the "pd" object
		if (undefined===pd) { pd = i2b2.CRC.model.queryCurrent.panels[i2b2.CRC.ctrlr.QT.temporalGroup][this.panelCurrentIndex]; }

	if (this.actualPanelIndex > 3) {return}
	if (pd.timing == "SAMEVISIT" )
	{ 
	//this.refTiming.innerHTML = "Occurs in Same Encounter";
	$("queryPanelTimingB" + (this.actualPanelIndex+1) + "-button").innerHTML = "Occurs in Same Encounter";
	} else if (pd.timing == "SAMEINSTANCENUM") {
	//	this.refTiming.innerHTML ="Items Instance will be the same";
	$("queryPanelTimingB" + (this.actualPanelIndex+1) + "-button").innerHTML = "Items Instance will be the same";
	} else {
	//	this.refTiming.innerHTML = "Treat Independently";
	$("queryPanelTimingB" + (this.actualPanelIndex+1) + "-button").innerHTML = "Treat Independently";
	} 

//		i2b2.CRC.view.QT.setPanelTiming(this.panelCurrentIndex + 1, pd.timing);
	}

// ================================================================================================== //
	this._redrawButtons = function(pd) {
		$('infoQueryStatusText').innerHTML = "";		
		$('infoQueryStatusChart').innerHTML = "";

		// set panel GUI according to data in the "pd" object
		if (undefined===pd) { pd = i2b2.CRC.model.queryCurrent.panels[i2b2.CRC.ctrlr.QT.temporalGroup][this.panelCurrentIndex]; }
		if (pd.exclude) {
			Element.addClassName(this.refButtonExclude,'queryPanelButtonSelected');
			this.refBalloon.style.background = '#FF9999';
			this.refBalloon.innerHTML = 'none<br />of<br />these';
		} else {
			Element.removeClassName(this.refButtonExclude,'queryPanelButtonSelected');
			if (pd._nullRecord) {
				this.refBalloon.style.background = '#FFFF99';
				this.refBalloon.innerHTML = 'drop a<br />term<br />on here';
			} else {
				this.refBalloon.style.background = '#99EE99';
				this.refBalloon.innerHTML = 'one or<br />more of<br />these';
			}
		}
		if (pd.occurs > 0) {
			Element.addClassName(this.refButtonOccurs,'queryPanelButtonSelected');
		} else {
			Element.removeClassName(this.refButtonOccurs,'queryPanelButtonSelected');
		}
		this.refButtonOccursNum.innerHTML = pd.occurs;
		if (pd.dateTo || pd.dateFrom) {
			Element.addClassName(this.refButtonDates,'queryPanelButtonSelected');					
		} else {
			Element.removeClassName(this.refButtonDates,'queryPanelButtonSelected');					
		}
	}

// ================================================================================================== //
	this.showLabValues = function(key, extData) {
		i2b2.CRC.view.modalLabValues.show(this.panelCurrentIndex, this, key, extData, false);
	}

// ================================================================================================== //
	this.showModValues = function(key, extData) {
		i2b2.CRC.view.modalLabValues.show(this.panelCurrentIndex, this, key, extData, true);
	}
	
	
// ================================================================================================== //
	this.showOccurs = function(iMinCount) {
		if (i2b2.CRC.model.queryCurrent.panels[i2b2.CRC.ctrlr.QT.temporalGroup].length==0) { return;}
		var dm = i2b2.CRC.model.queryCurrent.panels[i2b2.CRC.ctrlr.QT.temporalGroup][this.panelCurrentIndex];
		if (undefined!==dm) {
			
			if (i2b2.CRC.ctrlr.QT.queryTiming == "ENCOUNTER")
			{
				$('constraintEncounterBased').show();
				$('constraintTextBased').hide();
			} else {
					$('constraintEncounterBased').hide();
					$('constraintTextBased').hide();
			}
			// load value
			$('constraintOccursInput').value = dm.occurs;
			// prep variables for JS closure
			var qpi = this.panelCurrentIndex;
			var cpc = this;
			// show occurs window
			if (!Object.isUndefined(this.modalOccurs)) { delete this.modalOccurs; } 
			if (!this.modalOccurs) {
				if (!Object.isUndefined(handleSubmit)) { delete handleSubmit; } 
				
				//init slider
				var slider, 
				 bg="slider-bg", thumb="slider-thumb", 
				valuearea="slider-value", textfield="slider-converted-value";
		
				// The slider can move 0 pixels up
				var topConstraint = 0;
			
				// The slider can move 200 pixels down
				var bottomConstraint = 200;
			
				// Custom scale factor for converting the pixel offset into a real value
				var scaleFactor = 2;
			
				// The amount the slider moves when the value is changed with the arrow
				// keys
				var keyIncrement = 10;
			
				var tickSize = 10;
			
				Event.onDOMReady(function() {
			
					slider = YAHOO.widget.Slider.getHorizSlider(bg, 
									 thumb, topConstraint, bottomConstraint);
			
					// Sliders with ticks can be animated without YAHOO.util.Anim
					slider.animate = true;
					slider.setValue(dm.relevance * 2);
			
					slider.getRealValue = function() {
						return Math.round(this.getValue() / scaleFactor);
					}
			
					slider.subscribe("change", function(offsetFromStart) {
			
						var valnode = Dom.get(valuearea);
						
			
						// Display the pixel value of the control
						valnode.innerHTML =  slider.getRealValue()// offsetFromStart ;
			// slider.getRealValue(); 
			
						//var fld = Dom.get(textfield);
					});
					Event.on(textfield, "keydown", function(e) {
			
						// set the value when the 'return' key is detected
						if (Event.getCharCode(e) === 13) {
							var v = parseFloat(this.value, 10);
							v = (lang.isNumber(v)) ? v : 0;
			
							// convert the real value into a pixel offset
							slider.setValue(Math.round(v/scaleFactor));
						}
					});
					
					// Use setValue to reset the value to white:
					Event.on("putval", "click", function(e) {
						slider.setValue(100, false); //false here means to animate if possible
					});
				});		
				
				var handleSubmit = function(){
					var closure_qpi = qpi;
					var closure_cpc = cpc;
					// submit value(s)
					if (this.submit()) {
						var pd = i2b2.CRC.model.queryCurrent.panels[i2b2.CRC.ctrlr.QT.temporalGroup][closure_qpi];
						pd.occurs = parseInt($('constraintOccursInput').value, 10);
						pd.relevance = slider.getRealValue();
						slider.setValue(0);
						delete(slider);
						closure_cpc._redrawButtons(pd);
						i2b2.CRC.ctrlr.QT.doSetQueryName.call(this, '');
					}
				}
				var handleCancel = function(){
					this.cancel();
				}
				var handleHelp = function(){
					alert("Help soon");
				}
				this.modalOccurs = new YAHOO.widget.SimpleDialog("constraintOccurs", {
					width: "400px",
					fixedcenter: true,
					constraintoviewport: true,
					modal: true,
					zindex: 700,
					buttons: [{
						text: "OK",
						handler: handleSubmit,
						isDefault: true
					}, {
						text: "Cancel",
						handler: handleCancel
					}]
				});
				$('constraintOccurs').show();
				this.modalOccurs.validate = function(){
					// now process the form data
					var t = parseInt($('constraintOccursInput').value, 10);
					if (isNaN(t)) {
						alert('The number you entered could not be understood.\nPlease make sure that you entered a valid number.');
						return false;
					}
					if (t > 19) {
						alert('The number you entered was too large.\nThe maximum number you can enter is 19.');
						return false;
					}
					if (t < 0) {
						alert('The number you entered was too small.\nThe minimum number you can enter is 0.');
						return false;
					}
					return true;
				};
				this.modalOccurs.render(document.body);
			}
			//$('constraintOccurs_c').show();
			this.modalOccurs.show();
			//this.modalOccurs.visible = true;
		}
	}
	
	this.showLimit = function(iMinCount) {
		if (i2b2.CRC.model.queryCurrent.panels[i2b2.CRC.ctrlr.QT.temporalGroup].length==0) { return;}
		var dm = i2b2.CRC.model.queryCurrent.panels[i2b2.CRC.ctrlr.QT.temporalGroup][this.panelCurrentIndex];
		if (undefined!==dm) {
			
			// prep variables for JS closure
			var qpi = this.panelCurrentIndex;
			var cpc = this;
			// show occurs window
			if (!Object.isUndefined(this.modalLimits)) { delete this.modalLimits } 
			if (!this.modalLimits) {
				if (!Object.isUndefined(handleSubmit)) { delete handleSubmit; } 
			
				var handleSubmit = function(){
					var closure_qpi = qpi;
					var closure_cpc = cpc;
					// submit value(s)
					if (this.submit()) {
						var pd = i2b2.CRC.model.queryCurrent.panels[i2b2.CRC.ctrlr.QT.temporalGroup][closure_qpi];
						closure_cpc._redrawButtons(pd);
						i2b2.CRC.ctrlr.QT.doSetQueryName.call(this, '');
					}
				}
				var handleCancel = function(){
					this.cancel();
				}
				var handleHelp = function(){
					alert("Help soon");
				}
				this.modalLimits = new YAHOO.widget.SimpleDialog("constraintLimits", {
					width: "400px",
					fixedcenter: true,
					constraintoviewport: true,
					modal: true,
					zindex: 700,
					buttons: [{
						text: "OK",
						handler: handleSubmit,
						isDefault: true
					}, {
						text: "Cancel",
						handler: handleCancel
					}, {
						text: "Help",
						handler: handleHelp
					}]
				});
				$('constraintLimits').show();
				this.modalLimits.validate = function(){
					// now process the form data

					return true;
				};
				this.modalLimits.render(document.body);
			}
			//$('constraintOccurs_c').show();
			this.modalLimits.show();
			//this.modalOccurs.visible = true;
		}
	}	

// ================================================================================================== //
	this.doExclude = function(bExclude) { 
		if (i2b2.CRC.model.queryCurrent.panels[i2b2.CRC.ctrlr.QT.temporalGroup].length==0) { return;}
		var bVal;
		var dm = i2b2.CRC.model.queryCurrent.panels[i2b2.CRC.ctrlr.QT.temporalGroup][this.panelCurrentIndex];
		if (undefined!==dm) {
			if (undefined!=bExclude) {
				bVal = bExclude;
			} else {
				bVal = !Boolean(dm.exclude);
			}
			dm.exclude = bVal;
			this._redrawButtons(dm);
		}
		// clear the query name and set the query as having dirty data
		var QT = i2b2.CRC.ctrlr.QT;
		QT.doSetQueryName.call(QT,'');
	}

// ================================================================================================== //
	this.doTiming = function(sTiming) { 
		$('infoQueryStatusText').innerHTML = "";	
		$('infoQueryStatusChart').innerHTML = "";

		if (i2b2.CRC.model.queryCurrent.panels[i2b2.CRC.ctrlr.QT.temporalGroup].length==0) { return;}
		var bVal;
		var dm = i2b2.CRC.model.queryCurrent.panels[i2b2.CRC.ctrlr.QT.temporalGroup][this.panelCurrentIndex];
		
		//this.QTController.panelControllers[this.panelCurrentIndex];
		if (undefined!==dm) {
			if (undefined!=sTiming) {
				bVal = sTiming;
			} else {
				bVal = i2b2.CRC.ctrlr.QT.queryTiming; //'ANY';
			}
			dm.timing = bVal;
			//this._redrawButtons(dm);
		}
		// clear the query name and set the query as having dirty data
		//var QT = i2b2.CRC.ctrlr.QT;
		//QT.doSetQueryName.call(QT,'');
	}
	

	this.clone = function(obj){


		/*
	var outpurArr = {};
		for (var i in obj) {
		outpurArr[i] = typeof (obj[i]) == 'object' ? this.clone(obj[i]) : obj[i]; 
		} 
	return outpurArr;
*/
	
	//return YAHOO.lang.JSON.parse( YAHOO.lang.JSON.stringify( obj ) );
		
		
		if ((null == obj) || ("object" != (typeof obj))) return obj;
		
		var copy = obj.constructor();
		for (var attr in obj) {
			if (obj.hasOwnProperty(attr)) copy[attr] = obj[attr];
		}	
		copy.itemNumber = this.itemNumber++;
		return copy;
/* *
		if(obj == null || typeof(obj) != 'object')
			return obj;
	
	
	        var copy = {};
        for (var attr in obj) {
            if (obj.hasOwnProperty(attr)) copy[attr] = this.clone(obj[attr]);
        }
        return copy;
 */
	/*
        var clone = {};
        for(var i in obj) {
            if(typeof(obj[i])=="object")
                clone[i] = this.clone(obj[i]);
            else
                clone[i] = obj[i];
        }
        return clone;

*/
	}


// ================================================================================================== //
	this.doDrop = function(sdxConceptOrig) {	// function to handle drag and drop
	
		//Clone it
		//var sdxConcept  = this.clone(sdxConceptOrig);
		
		var sdxConcept = i2b2.sdx.TypeControllers.CONCPT.MakeObject(sdxConceptOrig.origData.xmlOrig, sdxConceptOrig.origData.isModifier, null, sdxConceptOrig.origData.parent, sdxConceptOrig.sdxInfo.sdxType);
		
		// insert concept into our panel's items array;
		var dm = i2b2.CRC.model.queryCurrent;
		var repos = false;
		var targetPanelIndex = this.panelCurrentIndex;
		if (Object.isUndefined(dm.panels[i2b2.CRC.ctrlr.QT.temporalGroup][targetPanelIndex])) { 
			this.QTController.panelAdd(this.yuiTree);
			repos = true;
		} 
		var il = dm.panels[i2b2.CRC.ctrlr.QT.temporalGroup][targetPanelIndex].items;
		// check for duplicate data
		//if (sdxConcept.origData.isModifier != true) {
		//	for (var i=0; i<il.length; i++) {
		//		if (il[i].sdxInfo.sdxKeyValue==sdxConcept.sdxInfo.sdxKeyValue) {return false; }					
		//	}
		//}
		
		//Delete any lab or modifiers that exist
		//delete sdxConcept.LabValues;
		//delete sdxConcept.ModValues;
		sdxConcept.itemNumber = this.itemNumber++;
		
		// save data
		this._addConcept(sdxConcept,this.yuiTree.root, true);
		// reset the query name to be blank and flag as having dirty data
		i2b2.CRC.ctrlr.QT.doSetQueryName('');
		this.QTController._redrawAllPanels();
	}

// ================================================================================================== //
	this._addConcept = function (sdxConcept, tvParent, isDragged) {
		var tmpNode = this._addConceptVisuals.call(this, sdxConcept, tvParent, isDragged);
		// add concept to data model record for panel
		var panel = i2b2.CRC.model.queryCurrent.panels[i2b2.CRC.ctrlr.QT.temporalGroup][this.panelCurrentIndex];
		panel.items[panel.items.length] = sdxConcept;
		return tmpNode;
	}

// ================================================================================================== //
	this._addConceptVisuals = function (sdxConcept, tvParent, isDragged) {
		var tvTree = tvParent.tree;
		if (sdxConcept.sdxInfo.sdxType == "CONCPT") { 
		    var sdxDataNode = i2b2.sdx.Master.EncapsulateData('CONCPT',sdxConcept.origData);
			
			
				var title = "";
			
				if (sdxConcept.origData.isModifier) {
					var values = sdxConcept.ModValues;
				} else  {
					var values = sdxConcept.LabValues;				
				}
				if (values != null) {
					switch(values.MatchBy) {
								case "FLAG":
								//mm ??not sure tvChildren[i].html = ' = '+i2b2.h.Escape(values.ValueFlag) + "</div></div>";
									title = ' = '+i2b2.h.Escape(values.ValueFlag);
									break;
								case "VALUE":
                                    if ((values.GeneralValueType== "LARGESTRING") || (values.GeneralValueType=="TEXT")) {
                                        title = ' = ("' + values.ValueString +'")';
                                    } else if (values.GeneralValueType=="ENUM") { 
										try {
											var sEnum = [];
											for (var i2=0;i2<values.ValueEnum.length;i2++) {
												sEnum.push(i2b2.h.Escape(values.ValueEnum[i2]));
											}
											sEnum = sEnum.join("\", \"");
											sEnum = ' =  ("'+sEnum+'")';
											//tvChildren[i].html =  sEnum + "</div></div>"
											title = sEnum;
										} catch (e) {
											
										}
									} else {
										if (values.NumericOp == 'BETWEEN') {
											title =  ' '+i2b2.h.Escape(values.ValueLow)+' - '+i2b2.h.Escape(values.ValueHigh);
										} else {
											switch(values.NumericOp) {
											case "LT":
												var numericOp = " < ";
												break;
											case "LE":
												var numericOp = " <= ";
												break;
											case "EQ":
												var numericOp = " = ";
												break;
											case "GT":
												var numericOp = " > ";
												break;
											case "GE":
												var numericOp = " >= ";
												break;
												
											case "":
												break;	
											}
											title =   numericOp +i2b2.h.Escape(values.Value);
											if (!Object.isUndefined(values.UnitsCtrl))
											{
												title += " " + values.UnitsCtrl;				
											}
										}
									}
									break;
								case "":
									break;
							}
				}
			
			if (sdxConcept.origData.isModifier) {

				i2b2.CRC.ctrlr.QT.hasModifier = true;
				//Get the blob for this now.
				if (isDragged) {
					var cdetails = i2b2.ONT.ajax.GetModifierInfo("CRC:QueryTool", {modifier_applied_path:sdxConcept.origData.applied_path, modifier_key_value:sdxConcept.origData.key, ont_synonym_records: true, ont_hidden_records: true} );
					// this is what comes out of the old AJAX call
					try { new ActiveXObject ("MSXML2.DOMDocument.6.0"); isActiveXSupported =  true; } catch (e) { isActiveXSupported =  false; }
	
					if (isActiveXSupported) {
						//Internet Explorer
						xmlDocRet = new ActiveXObject("Microsoft.XMLDOM");
						xmlDocRet.async = "false";
						xmlDocRet.loadXML(cdetails.msgResponse);
						xmlDocRet.setProperty("SelectionLanguage", "XPath");
						var c = i2b2.h.XPath(xmlDocRet, 'descendant::modifier');						
					} else {					
						var c = i2b2.h.XPath(cdetails.refXML, 'descendant::modifier');
					}					
					if (c.length > 0) {
							sdxConcept.origData.xmlOrig = c[0];
							
					}
				}
				
				//Get parent who is not a modifier
				var modParent = sdxConcept.origData.parent;
				while  (modParent != null)
				{
					if (modParent.isModifier)
					{
						modParent = modParent.parent;
					} else {
						sdxConcept.origData.newName = modParent.name + " [" + sdxConcept.origData.name + title + "]";
						break;
					}
				}
				
				
				
				
				var hasContainer = false;
				var data = sdxConcept.origData;
				while (hasContainer || !Object.isUndefined(data.parent))
				{
					if ((data.hasChildren == "OAE") || (data.hasChildren == "OA"))
					{
						hasContainer = true;	
						
						var realdata = sdxConcept.origData;
						while ((realdata.hasChildren != "FA") &&
						(realdata.hasChildren != "FAE"))

						{
							realdata  = realdata.parent;	
						}
						sdxConcept.origData.level = realdata.level;
						sdxConcept.origData.parent.key = realdata.key;
						//sdxConcept.origData.parent.name = sdxConcept.origData.name;
						//sdxConcept.origData.name = realdata.name;
						if (undefined == sdxConcept.origData.newName)
							sdxConcept.origData.newName = realdata.name + " [" + sdxConcept.origData.name + title + "]";
		//mm				sdxConcept.origData.tooltip = realdata.tooltip;	
						sdxConcept.origData.hasChildren = realdata.hasChildren;	
					}
					if (!Object.isUndefined(data.parent)) {
						data = data.parent;	
						
					} else {
						break;
					}
				}

				
				var renderOptions = {
					title: sdxConcept.origData.newName, //name + " [" + sdxConcept.origData.name + title + "]",
					dblclick: "i2b2.CRC.ctrlr.QT.ToggleNode(this,'"+tvTree.id+"')",
					icon: {
						root: "sdx_ONT_CONCPT_root.gif",
						rootExp: "sdx_ONT_CONCPT_root-exp.gif",
						branch: "sdx_ONT_CONCPT_branch.gif",
						branchExp: "sdx_ONT_CONCPT_branch-exp.gif",
						leaf: "sdx_ONT_CONCPT_branch.gif"
					}
				};
				
			} else {
				if (isDragged) {
					var cdetails = i2b2.ONT.ajax.GetTermInfo("CRC:QueryTool", {concept_key_value:sdxConcept.origData.key, ont_synonym_records: true, ont_hidden_records: true} );
					try { new ActiveXObject ("MSXML2.DOMDocument.6.0"); isActiveXSupported =  true; } catch (e) { isActiveXSupported =  false; }
	
					if (isActiveXSupported) {
						//Internet Explorer
						xmlDocRet = new ActiveXObject("Microsoft.XMLDOM");
						xmlDocRet.async = "false";
						xmlDocRet.loadXML(cdetails.msgResponse);
						xmlDocRet.setProperty("SelectionLanguage", "XPath");
						var c = i2b2.h.XPath(xmlDocRet, 'descendant::concept');						
					} else {					
						var c = i2b2.h.XPath(cdetails.refXML, 'descendant::concept');
					}
					if (c.length > 0) {
							sdxConcept.origData.xmlOrig = c[0];					
					 }
				}
					
				sdxConcept.origData.newName = sdxConcept.origData.name + title;
				var renderOptions = {
					title: sdxConcept.origData.name + title,
					dblclick: "i2b2.CRC.ctrlr.QT.ToggleNode(this,'"+tvTree.id+"')",
					icon: {
						root: "sdx_ONT_CONCPT_root.gif",
						rootExp: "sdx_ONT_CONCPT_root-exp.gif",
						branch: "sdx_ONT_CONCPT_branch.gif",
						branchExp: "sdx_ONT_CONCPT_branch-exp.gif",
						leaf: "sdx_ONT_CONCPT_leaf.gif"
					}
				};
			}
		} else if (sdxConcept.sdxInfo.sdxType == "ENS") {
			var sdxDataNode = i2b2.sdx.Master.EncapsulateData('ENS',sdxConcept.origData);
			var title = sdxConcept.origData.titleCRC;
			if (title == undefined)
			{
				title = sdxConcept.origData.title;
			}
			var renderOptions = {
				title: title,
				icon: "sdx_CRC_PRS.jpg"
				};
		} else if (sdxConcept.sdxInfo.sdxType == "PRS") {
			var sdxDataNode = i2b2.sdx.Master.EncapsulateData('PRS',sdxConcept.origData);
			var title = sdxConcept.origData.titleCRC;
			if (title == undefined)
			{
				title = sdxConcept.origData.title;
			}
			var renderOptions = {
				title: title,
				icon: "sdx_CRC_PRS.jpg"
			};		
		} else if (sdxConcept.sdxInfo.sdxType == "QM") {
			var sdxDataNode = i2b2.sdx.Master.EncapsulateData('QM',sdxConcept.origData);
			var title = sdxConcept.origData.titleCRC;
			if (title == undefined)
			{
				title = sdxConcept.origData.title;
			}
			var renderOptions = {
				title: title,
				icon: "sdx_CRC_QM.gif"
			};
		
		}
		
		if (!sdxDataNode) { return false; }

		var sdxRenderData = i2b2.sdx.Master.RenderHTML(tvTree.id, sdxDataNode, renderOptions);
	
		if (sdxConcept.itemNumber)
			sdxRenderData.itemNumber = sdxConcept.itemNumber;
		else 
			sdxRenderData.itemNumber = this.itemNumber++;
	
		if (!sdxConcept.origData.isModifier) {
			//check if lab has value if so than auto popup
			var lvMetaDatas2 = i2b2.h.XPath(sdxConcept.origData.xmlOrig, 'metadataxml/ValueMetadata[string-length(Version)>0]');
			if (lvMetaDatas2.length > 0) {
				//bring up popup
				this.showLabValues(sdxConcept.origData.key, sdxRenderData);
			}
		} else {
			//check if the mod has a value, if so than auto popup
			var lvMetaDatas1 = i2b2.h.XPath(sdxConcept.origData.xmlOrig, 'metadataxml/ValueMetadata[string-length(Version)>0]');
			if (lvMetaDatas1.length > 0) {
				//bring up popup
				this.showModValues(sdxConcept.origData.key, sdxRenderData);			
			}
		}

		
		i2b2.sdx.Master.AppendTreeNode(tvTree, tvParent, sdxRenderData);
		return sdxRenderData;
	}	

// ================================================================================================== //
	this._deleteConcept = function(htmlID) {
		var pd = i2b2.CRC.model.queryCurrent.panels[i2b2.CRC.ctrlr.QT.temporalGroup][this.panelCurrentIndex];
		$('infoQueryStatusText').innerHTML = "";
		$('infoQueryStatusChart').innerHTML = "";

		if (undefined===htmlID) { return; } 
		// remove the node in the treeview
		var tvChildren = pd.tvRootNode.children
		for (var i=0; i< tvChildren.length; i++) {
			 if (tvChildren[i].data.nodeid===htmlID) { 
				this.yuiTree.removeNode(tvChildren[i],false);
				this._redrawTree.call(this, pd);
				break;
			}
		}
		// remove the concept from the data model
		pd.items.splice(i,1);
		// remove this panel if it's empty
		if (pd.items.length == 0) { this.doDelete(); }
		// clear the query name if it was set
		this.QTController.doSetQueryName.call(this,'');
	}

// ================================================================================================== //
	this._renameConcept = function(key, isModifier, pd) {
		$('infoQueryStatusText').innerHTML = "";
		$('infoQueryStatusChart').innerHTML = "";

		//var pd = i2b2.CRC.model.queryCurrent.panels[this.panelCurrentIndex];
		// remove the concept from panel
		for (var i=0; i< pd.items.length; i++) {
			if ((pd.items[i].origData.key == key)
						  || (pd.items[i].itemNumber == key)) {
				// found the concept to remove
				var rto = pd.items[i];
				break;
			}
		}
		if (undefined===rto) { return; }
		// rename the node in the treeview
		var tvChildren = pd.tvRootNode.children
		for (var i=0; i< tvChildren.length; i++) {
			//if ((tvChildren[i].data.i2b2_SDX.sdxInfo.sdxKeyValue===rto.origData.key)
				//			|| (tvChildren[i].data.i2b2_SDX.sdxInfo.sdxKeyValue===rto.origData.id)) {
				
			if (tvChildren[i].data.i2b2_SDX.itemNumber==key) {	
				var tt = tvChildren[i].getContentHtml();
				var tt2 = tt.substring(0, tt.lastIndexOf("\"/>")+3);
				var tt3 = "";
				if (isModifier) {
					var values = rto.ModValues;


				var modParent = rto.origData.parent;
				while  (modParent != null)
				{
					if (modParent.isModifier)
					{
						modParent = modParent.parent;
					} else {
						break;
					}
				}
				

					tt2 +=  modParent.name + " ["+ rto.origData.name;
					tt3 =  "]";
					rto.origData.newName =  modParent.name + " ["+ rto.origData.name;
				} else  {
					var values = rto.LabValues;				
					tt2 +=  rto.origData.name;
					rto.origData.newName =rto.origData.name;
				}
				tvChildren[i].html = tt2  + tt3 + "</div></div>"
				rto.origData.newName +=  tt3 ;
				
				if (undefined  != values) {
				    switch(values.MatchBy) {
								case "FLAG":
								tvChildren[i].html = tt2 + ' = '+i2b2.h.Escape(values.ValueFlag) + "</div></div>";
								rto.origData.newName += ' = '+i2b2.h.Escape(values.ValueFlag);

									break;
								case "VALUE":
									if (values.GeneralValueType=="ENUM") {
										var sEnum = [];
										for (var i2=0;i2<values.ValueEnum.length;i2++) {
											sEnum.push(i2b2.h.Escape(values.NameEnum[i2].text));
										}
										sEnum = sEnum.join("\", \"");
										sEnum = ' =  ("'+sEnum+'")';
										tvChildren[i].html = tt2 + sEnum + tt3 + "</div></div>";
										rto.origData.newName += sEnum + tt3 ;
									} else if (values.GeneralValueType=="LARGESTRING") {
										tvChildren[i].html = tt2 + ' [contains "' + i2b2.h.Escape(values.ValueString) + '"]' + tt3 + "</div></div>";
										rto.origData.newName += ' [contains "' + i2b2.h.Escape(values.ValueString) + '"]' +  tt3;							
									} else if (values.GeneralValueType=="STRING")  {
										if (values.StringOp == undefined )
										{
											var stringOp = "";
										} else {
										switch(values.StringOp) {
											case "LIKE[exact]":
												var  stringOp = "Exact: ";
												break;
											case "LIKE[begin]":
												var  stringOp = "Starts With: ";
												break;
											case "LIKE[end]":
												var  stringOp = "Ends With: ";
												break;
											case "LIKE[contains]":
												var  stringOp = "Contains: ";
												break;
											default:
												var stringOp = "";
												break;
										}
										}
										tvChildren[i].html = tt2 + ' ['+stringOp + i2b2.h.Escape(values.ValueString) + "]" + tt3 + "</div></div>";
										rto.origData.newName += ' ['+stringOp + i2b2.h.Escape(values.ValueString) + "]" +  tt3;
									} else {
										if (!Object.isUndefined(values.UnitsCtrl))
										{
											tt3 = " " + values.UnitsCtrl + tt3;				
										}
										
										if (values.NumericOp == 'BETWEEN') {
											tvChildren[i].html = tt2 + ' '+i2b2.h.Escape(values.ValueLow)+' - '+i2b2.h.Escape(values.ValueHigh) + tt3 + "</div></div>";
											rto.origData.newName += ' '+i2b2.h.Escape(values.ValueLow)+' - '+i2b2.h.Escape(values.ValueHigh) + tt3;
										} else {
											switch(values.NumericOp) {
											case "LT":
												var numericOp = " < ";
												break;
											case "LE":
												var numericOp = " <= ";
												break;
											case "EQ":
												var numericOp = " = ";
												break;
											case "GT":
												var numericOp = " > ";
												break;
											case "GE":
												var numericOp = " >= ";
												break;
												
											case "":
												break;	
											}
											tvChildren[i].html = tt2  + numericOp +i2b2.h.Escape(values.Value) + tt3 + "</div></div>"
											rto.origData.newName += numericOp +i2b2.h.Escape(values.Value) + tt3 ;
										}
									}
									break;
								case "":
									break;
							}
				}
				//tvChildren[i].setNodesProperty("label", "test", true);
				//tvChildren[i].getLabelEl().innerHTML = label;
				//this.yuiTree.removeNode(tvChildren[i],false);
				
				
				//this._redrawTree.call(this, pd);
				//this._redrawTree(pd);
				pd.tvRootNode.tree.draw();
				break;
			}
		}
		// remove the concept from the data model
		//pd.items.splice(i,1);
		// remove this panel if it's empty
		if (pd.items.length == 0) { this.doDelete(); }
		// clear the query name if it was set
		this.QTController.doSetQueryName.call(this,'');
	}



// ================================================================================================== //
	this.setPanelRecord = function (index, actual) { 
		this.panelCurrentIndex = index; 
		this.actualPanelIndex = actual;
		this.doRedraw();
	}

// ================================================================================================== //
	this.doDelete = function() { 
		$('infoQueryStatusText').innerHTML = "";
		$('infoQueryStatusChart').innerHTML = "";
		// function fired when the [X] icon for the GUI panel is clicked
		i2b2.CRC.ctrlr.QT.panelDelete(this.panelCurrentIndex);
		// redraw the panels 
		var idx = this.panelCurrentIndex - this.ctrlIndex;
		if (idx < 0) { idx = 0; }
		i2b2.CRC.ctrlr.QT.doShowFrom(idx);
	}	
}




console.timeEnd('execute time');
console.groupEnd();
