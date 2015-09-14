/**
 * @projectDescription	Help file viewer
 * @inherits 	i2b2
 * @namespace	i2b2
 * @author		Nick Benik, Griffin Weber MD PhD
 * @version 	1.3
 * ----------------------------------------------------------------------------------------
 * updated 9-15-08: RC4 launch [Nick Benik] 
 */
console.group('Load & Execute component file: hive > helpviewer');
console.time('execute time');


i2b2.hive.HelpViewer = {
	show: function() {
		if (!i2b2.hive.HelpViewer.yuiPanel) {
					// load the help page
		new Ajax.Updater('help-viewer-body', 'help/default.htm', {method: 'get', parameters: { cell: 'CORE', page:'ROOT' }});

			// show non-modal dialog with help documentation		
			var panel = new YAHOO.widget.Panel("help-viewer-panel", { 
				draggable: true,
				zindex:10000,
				width: "900px", 
				height: "550px", 
				autofillheight: "body", 
				constraintoviewport: true, 
				context: ["showbtn", "tl", "bl"]
			}); 
			$("help-viewer-panel").show();
			panel.render(document.body); 
			panel.show(); 
			i2b2.hive.HelpViewer.yuiPanel = panel;
			
			// resizer object and event handlers
			i2b2.hive.HelpViewer.resizer = new YAHOO.util.Resize("help-viewer-panel", { 
				handles: ['br'], 
				autoRatio: false, 
				minWidth: 300, 
				minHeight: 200, 
				status: false 
			}); 
			
			i2b2.hive.HelpViewer.resizer.on('resize', function(args) { 
				var panelHeight = args.height; 
				this.cfg.setProperty("height", panelHeight + "px"); 
			}, i2b2.hive.HelpViewer.yuiPanel, true); 
			
			i2b2.hive.HelpViewer.resizer.on('startResize', function(args) { 	 
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
			}, i2b2.hive.HelpViewer.yuiPanel, true); 			
		} else {
			i2b2.hive.HelpViewer.yuiPanel.show();
		}
		// load the help page
		new Ajax.Updater('help-viewer-body', 'help/default.htm', {method: 'get', parameters: { cell: 'CORE', page:'ROOT' }});
	},
	hide: function() {
		try {
			i2b2.hive.HelpViewer.yuiPanel.hide();
		} catch (e) {}
	}
};



console.timeEnd('execute time');
console.groupEnd();
