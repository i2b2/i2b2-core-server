/**
 * @projectDescription	Module used to manage GUI view modes and window "zooming".
 * @author		Nick Benik, Griffin Weber MD PhD
 * @version 	1.3
 * updated 10-29-08: RC4 launch [Nick Benik] 
 */
console.group('Load & Execute component file: hive > master view controller');
console.time('execute time');


// Master View Mode controller
// ================================================================================================== //
i2b2.hive.MasterView = {
	_validViews: ['Patients', 'Admin', 'Analysis', 'AnalysisZoomed'],
	_currentView: false,
	_ZoomWindows: [],
	eventChangeMode: {},
	eventZoomWindows: {},
	// ================================================================================================== //
	initViewMode: function()
	{
		var newMode = 'Patients';
		var tn = $("viewMode-"+newMode);
		if (tn) 
		{
			// remove highlighting from old screen mode links
			var old = $$('.selectedView');
			old.each(function(el)
			{
				el.removeClassName('selectedView');
			});			
			// highlight the new screen mode's label
			tn.addClassName('selectedView');
		}

		// update data
		this._currentView = newMode;
		this.eventInitView.fire( newMode );
		return true;
	},

// ================================================================================================== //
	setViewMode: function(requestedMode) {
		if (this._currentView == requestedMode) { return true; }
		var newMode = false;
		for (var i=0; i<this._validViews.length; i++) {
			if (this._validViews[i] == requestedMode) {
				newMode = this._validViews[i];
				break;
			}
		}
		if (newMode) {
			if (newMode == "Analysis" && !i2b2.h.allowAnalysis() ) {
				alert('Analysis tools are disabled - This access attempt has been logged to the server.');
				return false;
			}
// TODO: move this into a view controller?
			// change new link to selected state
			var tn = $("viewMode-"+newMode);
			if (tn) {
				// remove highlighting from old screen mode links
				var old = $$('.selectedView');
				old.each(function(el){
					el.removeClassName('selectedView');
				});
			
				// highlight the new screen mode's label
				tn.addClassName('selectedView');
				
//				// change old link to non-selected state (this is only done if the new mode has a tab)
//				var tn = $("viewMode-"+this._currentView);
//				if (tn) {
//					tn.removeClassName('selectedView');
//				}
			}

			// update data
			this._currentView = newMode;
			this.eventChangeMode.fire(newMode);
			return true;
		} else {
			return false;
		}
	},
// ================================================================================================== //
	getViewMode: function() {
		var readMode = false;
		for (var i=0; i<this._validViews.length; i++) {
			if (this._validViews[i] == this._currentView) {
				readMode = this._currentView;
				break;
			}
		}
		return readMode;
	},
// ================================================================================================== //
	getZoomWindows: function() {
		return this._ZoomWindows.clone();
	},
// ================================================================================================== //
	addZoomWindow: function(s) {
		this._ZoomWindows.push(s);
		this.eventZoomWindows.fire({action: "ADD", window:s});
	},
// ================================================================================================== //
	removeZoomWindow: function(s) {
		this._ZoomWindows = this._ZoomWindows.without(s);
		this.eventZoomWindows.fire({action: "REMOVE", window:s});
	},
// ================================================================================================== //
	toggleZoomWindow: function(s) {
		if (this._ZoomWindows.indexOf(s) == -1) {
			this.addZoomWindow.call(this, s);
		} else {
			this.removeZoomWindow.call(this, s);
		}
	}
}

// create custom events
i2b2.hive.MasterView.eventInitView	= new YAHOO.util.CustomEvent('ViewModeInit');
i2b2.hive.MasterView.eventChangeMode = new YAHOO.util.CustomEvent('ViewModeChange');
i2b2.hive.MasterView.eventZoomWindows = new YAHOO.util.CustomEvent('ZoomListChange');
// expose only the subscriber function in the i2b2 framework event collection
i2b2.events.initView		= {};
i2b2.events.initView.subscribe = (function(a1,a2,a3,a4,a5,a6) {i2b2.hive.MasterView.eventInitView.subscribe.call(i2b2.hive.MasterView.eventInitView, a1,a2,a3,a4,a5,a6); });
i2b2.events.changedViewMode = {};
i2b2.events.changedViewMode.subscribe = (function(a1,a2,a3,a4,a5,a6) {i2b2.hive.MasterView.eventChangeMode.subscribe.call(i2b2.hive.MasterView.eventChangeMode, a1,a2,a3,a4,a5,a6); });
i2b2.events.changedZoomWindows = {};
i2b2.events.changedZoomWindows.subscribe = (function(a1,a2,a3,a4,a5,a6) {i2b2.hive.MasterView.eventChangeMode.subscribe.call(i2b2.hive.MasterView.eventZoomWindows, a1,a2,a3,a4,a5,a6); });


console.timeEnd('execute time');
console.groupEnd();
