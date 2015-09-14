/**
 * @projectDescription	Message Sniffer object - sniffs communication for objects which expose a _DebugMessaging event.  Example: i2b2.CRC.ajax._DebugMessaging is a custom YUI event object.
 * @author		Nick Benik, Griffin Weber MD PhD
 * @version 	1.3
 * updated 9-15-08: RC4 launch [Nick Benik] 
 */

i2b2.hive.MsgSniffer = {
	yuiModalMsg: false,
	crtlDown: false,
	windowRef: null,
	sniffSources: [],
	signalOrigins: [],
	signalMessageDB: [],
	asyncStackFilter: false,
	initialize: function(doActivate) {},
	_showSingleMsgModal: function() {
		if (!i2b2.hive.MsgSniffer.yuiModalMsg) {
			// show non-modal dialog with help documentation		
			var panel = new YAHOO.widget.Panel("commViewerSingleMsg-panel", { 
				draggable: true,
				zindex:10000,
				width: "650px", 
				height: "450px", 
				autofillheight: "body", 
				constraintoviewport: true,
				context: ["tl", "bl"],
				visible: false
			}); 
			$("commViewerSingleMsg-panel").show();
			panel.render(document.body);
			i2b2.hive.MsgSniffer.yuiModalMsg = panel;
			
			// resizer object and event handlers
			i2b2.hive.MsgSniffer.yuiSingleMsgResizer = new YAHOO.util.Resize("commViewerSingleMsg-panel", { 
				handles: ['br'], 
				autoRatio: false, 
				minWidth: 350, 
				minHeight: 250, 
				status: false 
			}); 
			
			i2b2.hive.MsgSniffer.yuiSingleMsgResizer.on('resize', function(args) { 
				var panelHeight = args.height; 
				this.cfg.setProperty("height", panelHeight + "px"); 
			}, i2b2.hive.MsgSniffer.yuiModalMsg, true); 
			
			i2b2.hive.MsgSniffer.yuiSingleMsgResizer.on('startResize', function(args) { 	 
				if (this.cfg.getProperty("constraintoviewport")) { 
					var D = YAHOO.util.Dom; 
					var clientRegion = D.getClientRegion(); 
					var elRegion = D.getRegion(this.element); 
					resize.set("maxWidth", clientRegion.right - elRegion.left - YAHOO.widget.Overlay.VIEWPORT_OFFSET); 
					resize.set("maxHeight", clientRegion.bottom - elRegion.top - YAHOO.widget.Overlay.VIEWPORT_OFFSET); 
				} else { 
					resize.set("maxWidth", null); 
					resize.set("maxHeight", null); 
				} 
			}, i2b2.hive.MsgSniffer.yuiModalMsg, true); 			
		}
	},
	showSingleMsgRequest: function(strXML) {
		this._showSingleMsgModal();
		try {
			var t = $("commViewerSingleMsg-panel").select(".hd")[0];
			t.innerHTML = "Last Request Message";
			var t = $("commViewerSingleMsg-body").select(".xmlMsg")[0];
//			t.style.background = "#AFFFAF";
			t.innerHTML = '<PRE class="msgXML msgSent">'+strXML+'</PRE>';
		} catch (e) { console.error("Could not display information!"); }
		i2b2.hive.MsgSniffer.yuiModalMsg.show();

	},
	showSingleMsgResponse: function(strXML) {
		this._showSingleMsgModal();
		try {
			var t = $("commViewerSingleMsg-panel").select(".hd")[0];
			t.innerHTML = "Last Response Message";
			var t = $("commViewerSingleMsg-body").select(".xmlMsg")[0];
//			t.style.background = "#FFAFAF";
			t.innerHTML = '<PRE class="msgXML msgReceive">'+strXML+'</PRE>';
		} catch (e) { console.error("Could not display information!"); }
		i2b2.hive.MsgSniffer.yuiModalMsg.show();
	},
	show: function() {
		this.asyncStackFilter = false;
		this._spawnWin.call(this);
	},
	_spawnWin: function() {
		try {
			// spawn new
			i2b2.hive.MsgSniffer.windowRef = window.open('assets/msg-viewer.html', 'i2b2_msgsniffer', 'toolbar=no,resizable=yes,scrollbars=yes,height=480,width=740', false);
			i2b2.hive.MsgSniffer.windowRef.focus();
		} catch(e) {
			// bring to front, resize, make visible, etc
			i2b2.hive.MsgSniffer.windowRef.focus();
		}
		if (!i2b2.hive.MsgSniffer.windowRef) {
			alert('Could not display the Communications Channel Viewer.\n Please disable any popup blockers and try again.');
			return;
		}
	},
	showStack: function(viewtitle, origins, cells, actions) {
		// put info into asyncStackFilter that msgViewer code will check upon it's initialization
		this.asyncStackFilter = {
			title: viewtitle+" Msg Stack",
			origins: origins,
			cells: cells,
			actions: actions
		};
		i2b2.hive.MsgSniffer._spawnWin();
	},
	// Register event for message sources
	RegisterMessageSource: function(regMsg) {
		// expected data format: {
		//    channelName: "CELLNAME",
		//    channelActions: ["the names", "of the", "Cell's server calls"],
		//    channelSniffEvent: {yui custom event}
		// }
		if (!regMsg.channelName || !regMsg.channelActions || !regMsg.channelSniffEvent || !regMsg.channelSniffEvent.subscribe) {
			console.error('MsgSniffer: bad registration info / '+Object.inspect(regMsg));
			return false;
		}
		var t = regMsg.channelName;
		regMsg.channelCode = t;
		if (i2b2[t]) {
			if (i2b2[t].cfg.config.name) {
				regMsg.channelName = i2b2[t].cfg.config.name;
				
			}
		}	
		var was_found = false;
		for (var i = 0; i < this.sniffSources.length; i++) {
			if (this.sniffSources[i].channelSniffEvent === regMsg.channelSniffEvent) {
				was_found = true;
				var t = this.sniffSources[i].channelActions.concat(regMsg.channelActions);
				t = t.uniq();
				this.sniffSources[i].channelActions = t;
			}
		}
		if (!was_found) {
			this.sniffSources.push(regMsg);
			regMsg.channelSniffEvent.subscribe(i2b2.hive.MsgSniffer.MsgHandler);
		}
		return true;
	},
	MsgHandler: function(msgType, sniffMsg) {
		// Function where the messages enter the sniffer subsystem 
		// (SCOPE IS NOT IN NAMESPACE WHEN FUNCTION IS CALLED)
		var thisobj = i2b2.hive.MsgSniffer;
		if (!i2b2.PM.model.login_debugging) { return true; }
		// save the data if the framework is in debug mode
		sniffMsg = sniffMsg[0];
		sniffMsg.SignalType = msgType;
		var d = new Date();
		sniffMsg.SignalTimestamp = d;
		thisobj.signalMessageDB.push(sniffMsg);
		
		if (thisobj.signalOrigins.indexOf(sniffMsg.SignalOrigin) == -1) {
			thisobj.signalOrigins.push(sniffMsg.SignalOrigin);
		}
		// refresh the message viewer window if it is open
		if (!i2b2.hive.MsgSniffer.windowRef || !i2b2.hive.MsgSniffer.windowRef.focus) { return true; }
		i2b2.hive.MsgSniffer.windowRef.SnifferDisplay.renderFilteredList();
		return true;
	}
};
