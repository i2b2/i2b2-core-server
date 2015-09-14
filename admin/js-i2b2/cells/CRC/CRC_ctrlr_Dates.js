/**
 * @projectDescription	The Date Constraint controller (GUI-only controller).
 * @inherits 	i2b2.CRC.ctrlr
 * @namespace	i2b2.CRC.ctrlr.dateConstraint
 * @author		Nick Benik, Griffin Weber MD PhD
 * @version 	1.3
 * ----------------------------------------------------------------------------------------
 * updated 9-15-08: RC4 launch [Nick Benik] 
 */
console.group('Load & Execute component file: CRC > ctrlr > Dates');
console.time('execute time');

// ================================================================================================== //
i2b2.CRC.ctrlr.dateConstraint = {
	defaultStartDate: '12/01/1979',
	defaultEndDate: '12/31/2006',
	currentPanelIndex: false,

// ================================================================================================== //
	showDates: function(panelControllerIndex) {
		var dm = i2b2.CRC.model.queryCurrent;
		var panelIndex = i2b2.CRC.ctrlr.QT.panelControllers[panelControllerIndex].panelCurrentIndex;
		if (undefined==dm.panels[i2b2.CRC.ctrlr.QT.temporalGroup][panelIndex]) return;
		// grab our current values
		var qn = dm.panels[i2b2.CRC.ctrlr.QT.temporalGroup][panelIndex];
		this.currentPanelIndex = panelIndex;

		// only build the prompt box 1 time
		if (!i2b2.CRC.view.modalDates) {

			var handleSubmit = function(){
				var closure_pi = i2b2.CRC.ctrlr.dateConstraint.currentPanelIndex;
				// save the dates
				if (i2b2.CRC.ctrlr.dateConstraint.doProcessDates(closure_pi)) {
					// saved and validated, close modal form
					this.submit();
				}
			};
			var handleCancel = function(){
				this.cancel();
			}
			i2b2.CRC.view.modalDates = new YAHOO.widget.SimpleDialog("constraintDates", {
				width: "400px",
				fixedcenter: true,
				constraintoviewport: true,
				modal: true,
				zindex: 700,
				buttons: [{
					text: "OK",
					isDefault: true,					
					handler: handleSubmit
				}, {
					text: "Cancel",
					handler: handleCancel
				}]
			});
			$('constraintDates').show();
			i2b2.CRC.view.modalDates.render(document.body);
		} 
		i2b2.CRC.view.modalDates.show();
		// load our panel data				
		var DateRecord = new Object;
		if (qn.dateFrom) { 
			DateRecord.Start = padNumber(qn.dateFrom.Month,2)+'/'+padNumber(qn.dateFrom.Day,2)+'/'+qn.dateFrom.Year;
		} else { 
			DateRecord.Start = this.defaultStartDate; 
		}
		$('constraintDateStart').value = DateRecord.Start;
		if (qn.dateTo) {
			DateRecord.End = padNumber(qn.dateTo.Month,2)+'/'+padNumber(qn.dateTo.Day,2)+'/'+qn.dateTo.Year;
		} else {
//			DateRecord.End = this.defaultEndDate;
			var curdate = new Date(); 
			DateRecord.End = padNumber(curdate.getMonth()+1,2)+'/'+padNumber(curdate.getDate(),2)+'/'+curdate.getFullYear();
		}
		$('constraintDateEnd').value = DateRecord.End;
		if (qn.dateFrom) {
			$('checkboxDateStart').checked = true;
			$('constraintDateStart').disabled = false;
		} else {
			$('checkboxDateStart').checked = false;
			$('constraintDateStart').disabled = true;
		}
		if (qn.dateTo) {
			$('checkboxDateEnd').checked = true;
			$('constraintDateEnd').disabled = false;
		} else {
			$('checkboxDateEnd').checked = false;
			$('constraintDateEnd').disabled = true;
		}
	},

// ================================================================================================== //
	toggleDate: function() {
		if ($('checkboxDateStart').checked) {
			$('constraintDateStart').disabled = false;
			setTimeout("$('constraintDateStart').select()",150);
		} else {
			$('constraintDateStart').disabled = true;
		}
		if ($('checkboxDateEnd').checked) {
			$('constraintDateEnd').disabled = false;
			setTimeout("$('constraintDateEnd').select()", 150);
		} else {
			$('constraintDateEnd').disabled = true;
		}
	},

// ================================================================================================== //
	doShowCalendar: function(whichDate) {
		// create calendar if not already initialized
		if (!this.DateConstrainCal) {
			this.DateConstrainCal = new YAHOO.widget.Calendar("DateContstrainCal","calendarDiv");
			this.DateConstrainCal.selectEvent.subscribe(this.dateSelected, this.DateConstrainCal,true);
		}
		this.DateConstrainCal.clear();
		// process click
		if (whichDate=='S') {
			if ($('checkboxDateStart').checked==false) { return; }
			var apos = Position.cumulativeOffset($('dropDateStart'));
			var cx = apos[0] - $("calendarDiv").getWidth() + $('dropDateStart').width + 3;
			var cy = apos[1] + $('dropDateStart').height + 3;
			$("calendarDiv").style.top = cy+'px';
			$("calendarDiv").style.left = cx+'px';
			$("constraintDateStart").select();
			var sDateValue = $('constraintDateStart').value;
		} else {
			if ($('checkboxDateEnd').checked==false) { return; }
			var apos = Position.cumulativeOffset($('dropDateEnd'));
			var cx = apos[0] - $("calendarDiv").getWidth() + $('dropDateEnd').width + 3;
			var cy = apos[1] + $('dropDateEnd').height + 3;
			$("calendarDiv").style.top = cy+'px';
			$("calendarDiv").style.left = cx+'px';
			$("constraintDateEnd").select();
			var sDateValue = $('constraintDateEnd').value;
		}
		var rxDate = /^\d{1,2}(\-|\/|\.)\d{1,2}\1\d{4}$/
		if (rxDate.test(sDateValue)) {
			var aDate = sDateValue.split(/\//);
			this.DateConstrainCal.setMonth(aDate[0]-1);
			this.DateConstrainCal.setYear(aDate[2]);
		} else {
			alert("Invalid Date Format, please use mm/dd/yyyy or select a date using the calendar.");
		}
		// save our date type on the calendar object for later use
		this.whichDate = whichDate;
		// display everything
		$("calendarDiv").show();
		
	 	var w =  window.innerWidth || (window.document.documentElement.clientWidth || window.document.body.clientWidth);
	    var h =  window.innerHeight || (window.document.documentElement.clientHeight || window.document.body.clientHeight);

		$("calendarDivMask").style.top = "0px";
		$("calendarDivMask").style.left = "0px";
		$("calendarDivMask").style.width = (w - 10) + "px";
		$("calendarDivMask").style.height = (h - 10) + "px";
		$("calendarDivMask").show();
		this.DateConstrainCal.render(document.body);
	},

// ================================================================================================== //
	dateSelected: function(eventName, selectedDate) {
		// function is event callback fired by YUI Calendar control 
		// (this function looses it's class scope)
		var cScope = i2b2.CRC.ctrlr.dateConstraint;
		if (cScope.whichDate=='S') {
			var tn = $('constraintDateStart');
		} else {
			var tn = $('constraintDateEnd');
		}
		var selectDate = selectedDate[0][0];
		tn.value = selectDate[1]+'/'+selectDate[2]+'/'+selectDate[0];
		cScope.hideCalendar.call(cScope);
	},

// ================================================================================================== //
	hideCalendar: function() {
		$("calendarDiv").hide();
		$("calendarDivMask").hide();
	},

// ================================================================================================== //
	doProcessDates: function(panelIndex) {
		// push the dates into the data model
		var sDate = new String;
		var sDateError = false;
		var rxDate = /^\d{1,2}(\-|\/|\.)\d{1,2}\1\d{4}$/
		var DateRecord = {};
		var dm = i2b2.CRC.model.queryCurrent.panels[i2b2.CRC.ctrlr.QT.temporalGroup][panelIndex];
		this.currentPanelIndex = panelIndex;
		// start date
		if ($('checkboxDateStart').checked) {
			DateRecord.Start = {};
			sDate = $('constraintDateStart').value;
			if (rxDate.test(sDate)) {
				var aDate = sDate.split(/\//);
				DateRecord.Start.Month = padNumber(aDate[0],2);
				DateRecord.Start.Day = padNumber(aDate[1],2);
				DateRecord.Start.Year = aDate[2];
			} else {
				sDateError = "Invalid Start Date\n";
			}
		}
		// end date
		if ($('checkboxDateEnd').checked) {
			DateRecord.End = {};
			sDate = $('constraintDateEnd').value;
			if (rxDate.test(sDate)) {
				var aDate = sDate.split(/\//);
				DateRecord.End.Month = padNumber(aDate[0]);
				DateRecord.End.Day = padNumber(aDate[1]);
				DateRecord.End.Year = aDate[2];
			} else {
				sDateError = "Invalid End Date\n";
			}
		}
		// check for processing errors
		if (sDateError) {
			sDateError += "\nPlease use the following format: mm/dd/yyyy";
			alert(sDateError);
			return false;
		} else {
			// attach the data to our panel data
			if (DateRecord.Start) {
				dm.dateFrom = DateRecord.Start;
			} else {
				delete dm.dateFrom;
			}
			if (DateRecord.End) {
				dm.dateTo = DateRecord.End;
			} else {
				delete dm.dateTo;
			}
			// clear the query name and set the query as having dirty data
			var QT = i2b2.CRC.ctrlr.QT;
			QT.doSetQueryName.call(QT,'');
		}
		// redraw buttons if needed
		var panelctrlFound = false;
		var pd = i2b2.CRC.ctrlr.QT;
		for (var i=0; i<pd.panelControllers.length; i++) {
			if (pd.panelControllers[i].panelCurrentIndex == panelIndex) {
				// found the controller for the panel requested
				panelctrlFound = pd.panelControllers[i];
				break;
			}
		}
		if (panelctrlFound!==false) { panelctrlFound._redrawButtons(); }
		return true;
	}
};


console.timeEnd('execute time');
console.groupEnd();