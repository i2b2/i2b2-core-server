/**
 * @projectDescription	Example of a "Hello World" plugin.
 * @inherits	i2b2
 * @namespace	i2b2.ExampHello
 * @author	Nick Benik, Griffin Weber MD PhD
 * @version 	1.3
 * ----------------------------------------------------------------------------------------
 * updated 10-30-08: Initial Launch [Nick Benik] 
 */

i2b2.ExampHello.Init = function(loadedDiv) {
	// this function is called after the HTML is loaded into the viewer DIV
	// find all the drop targets by style for PRS type
	i2b2.ExampHello.view.containerDiv = loadedDiv;
	
	alert("Hello World! This message is from the initialization routine.");
	
};

i2b2.ExampHello.Unload = function() {
	// this function is called before the plugin is unloaded by the framework
	// alert("Hello World! This message is from the unload routine.");
	return true;
	
	// If you want to cancel the unload return false.  The next line is boiler plate code that should work 
	// in many instances.  This routine can also be used to save the state of the plugin so that work can 
	// seamlessly resume the plugin is loaded.
	// 	return confirm("Are you sure you want to unload the Hello World plugin?");
}

i2b2.ExampHello.Resize = function(resizeInfo) {
	// Optional function that is automatically called by the plugin framework when ever a resize occurs.
	// Data format for resizeInfo variable:
	//	resizeInfo.top = top location in pixels (absolute)
	//	resizeInfo.left = left location in pixels (absolute)
	//	resizeInfo.width = new width in pixels
	//	resizeInfo.height = new height in pixels
}

