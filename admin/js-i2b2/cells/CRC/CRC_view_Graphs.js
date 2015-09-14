/** -----------------------------------------------------------------------------------------------------------------------
 * @projectDescription	View controller for the query graph window (which is a GUI-only component of the CRC module).
 * @inherits 	i2b2.CRC.view
 * @namespace	i2b2.CRC.view.graphs
 * @author 		Shawn Murphy MD PhD, Hannah Murphy
 * @version 	1.7
 * @description This set of functions uses D3 and its derivative C3 to graph the text that results in the query-status-window
 *              of the i2b2 web client.  
 *              The main function is "createGraphs".
 *              Because it makes extensive use of Vector Graphic in the D3 library it will only work in Microsoft Internet 
 *              Explorer 9 and above.  It assumes the STATUS window is a specific height which is 146px.  In theory the 
 *              Width can vary, but right not it is set to 535px in many places.
 *              It draws the graphs in a div (which should be the dimensions above), using a string which is essentially 
 *              screen-scraped from the text what is placed in the query_status box of the web client.  To distinguish the
 *              normal i2b2 vs. SHRINE text, a boolean flag is used.  A regular i2b2 result (bIsMultiSite = false)
 *              or a SHRINE result (bIsMultiSite = true).
 *                   Internally, everything works off an array the is produced from the test that is a six element array of 
 *              ****  0 "query name", 1 "title", 2 "site", 3 "element name", 4 quantity, 5 "sentence"  ****
 *              for example, one element would be:
 *              **** ["Circulatory sys@20:21:19", "Age patient breakdown", "MGH" "0-9 years old", 0, "0-9 years old: 0"] ****
 *              It also uses some jQuery, but only for the scroll bar function with *** jQuery Stuff ***
 *              in the comments.
 *              There are four web client javascript files in the CRC folder that have references to functions in this 
 *              javascript file, and the default.htm folder in the main web client folder, they are:
 *              CRC_view_Status, CRC_ctlr_QryStatus, CRC_ctlr_QryTools, and cell_config_data
 ** -----------------------------------------------------------------------------------------------------------------------*/
console.group('Load & Execute component file: CRC > view > Graphs');
console.time('execute time');

//i2b2.PM.model.isObfuscated =  true; // for testing
	
// Constants
var msSpecialBreakdownSite = "";  // this constant designates a site from which the breakdown arrays will always be obtained
var msStringDefineingNumberOfPatients = "number of patients";  // this constant is what appears in the breakdown text 
//                            which is the number of patients and is lower cased and trimmed on both sides for comparison

// create and save the screen objects and more constants

i2b2.CRC.view.graphs = new i2b2Base_cellViewController(i2b2.CRC, 'graphs');
i2b2.CRC.view.graphs.visible = false;
i2b2.CRC.view.graphs.iObfuscatedFloorNumber = 3;  // this is the amount reported that the numbers are obfuscated by
i2b2.CRC.view.graphs.sObfuscatedText = "<3";  // this is the text that is replaced for a small number in obfuscated mode
//                            so that it can be cleaned up before the next display
i2b2.CRC.view.graphs.sObfuscatedEnding = "&plusmn;3";  //this is the text that is added to all numbers in obfuscated mode
i2b2.CRC.view.graphs.bIsSHRINE = false;  // this changes the way the graphs are made if the file is being run in SHRINE mode
//                            NOTE THAT THIS IS DEMO ONLY IN THIS VERSION - IT DOES NOT REALLY WORK
i2b2.CRC.view.graphs.asTitleOfShrineGroup = ["patient age count breakdown", "patient gender count breakdown", "patient race count breakdown", 
                                             "patient vital status count breakdown"];

// These functions manage the graph divs, but DECISIONS are made in the CRC_view_Status code

i2b2.CRC.view.graphs.show = function() {
	i2b2.CRC.view.graphs.visible = true;
	$('crcGraphsBox').show();
}
i2b2.CRC.view.graphs.hide = function() {
	i2b2.CRC.view.graphs.visible = false;
	$('crcGraphsBox').hide();
}

i2b2.CRC.view.graphs.showDisplay = function() {
	var targs = $('infoQueryStatusChart').parentNode.parentNode.select('DIV.tabBox.active');
	// remove all active tabs
	targs.each(function(el) { el.removeClassName('active'); });
	// set us as active
	$('infoQueryStatusChart').parentNode.parentNode.select('DIV.tabBox.tabQueryGraphs')[0].addClassName('active');
	$('infoQueryStatusChart').show();
}

i2b2.CRC.view.graphs.hideDisplay = function() {
	$('infoQueryStatusChart').hide();
}

// ================================================================================================== //


/*********************************************************************************
   FUNCTION createGraphs
   Takes a Div, the text from the query status view, and a multisite flag and populates the Div
**********************************************************************************/
i2b2.CRC.view.graphs.createGraphs = function(sDivName, sInputString, bIsMultiSite) {
try {
	if (sDivName === undefined || sDivName === null || sDivName === "") throw ("ERROR 201 - sDivName in function createGraphs is null");
	//i2b2.CRC.view.graphs.sNameOfPreviousDiv = sDivName;
	i2b2.CRC.view.graphs.clearGraphs(sDivName);
	if (!i2b2.CRC.view.graphs.bisGTIE8) {
		i2b2.CRC.view.graphs.aDivForIE8(sDivName);
		return;
	}
	if (bIsMultiSite) sInputString = i2b2.CRC.view.graphs.returnTestString(true);
	if (sInputString === undefined || sInputString === null || sInputString === "") throw ("ERROR 202 - sInputString in function createGraphs is null");
	var asBreakdownArray = [[]];
	var iBreakdown = 0;

	// make the input array  
	var asInputArray = parseInputIntoArray(sInputString, bIsMultiSite);
	// Pull out unique breakdown types
	var asBreakdownTypes = [];
	var iBreakdown = 0;
	for (var i = 0; i < asInputArray.length; i++) {
			asBreakdownTypes[iBreakdown] = asInputArray[i][1];
			iBreakdown++;
	}
    var asUniqueBreakdownTypes = [];
    for (var i=0; i < asBreakdownTypes.length; i++) {
        if (asUniqueBreakdownTypes.indexOf(asBreakdownTypes[i]) === -1 && asBreakdownTypes[i] !== '')
            asUniqueBreakdownTypes.push(asBreakdownTypes[i]);
	}
	if (asUniqueBreakdownTypes.length === 0) throw ("ERROR 203 in createGraphs, there are no breakdown types in *unique* array");
	//console.log(asUniqueBreakdownTypes);
	// rearrange unique array so that patient number is on the top
	for (var i = 0; i < asUniqueBreakdownTypes.length; i++) {
		if (asUniqueBreakdownTypes[i].toLowerCase().trim() == msStringDefineingNumberOfPatients.toLowerCase().trim()) {
			var sTempVariable = asUniqueBreakdownTypes[0];
			asUniqueBreakdownTypes[0] = asUniqueBreakdownTypes[i];
			asUniqueBreakdownTypes[i] = sTempVariable;
			break;
		}
	}

	//Make Divs in the original div for the charts
	oParentDiv = document.getElementById(sDivName);
	for (var i=0; i<asUniqueBreakdownTypes.length; i++){
		sChartDivName = "chart"+i;
		var child = document.createElement("div");
		child.setAttribute("id",sChartDivName);
		//child.setAttribute("class","StatusBoxInnerGraph");
		child.setAttribute("width","auto");
		oParentDiv.appendChild(child);
	}
	// populate each Div with a graph
	// populate the Div that has the number of patients first to make it the top one
	if (!bIsMultiSite) {
		var iIncrement = 0;
		if (asUniqueBreakdownTypes[0].toLowerCase().trim() == msStringDefineingNumberOfPatients.toLowerCase().trim()) {
			graph_singlesite_patient_number("chart0", asUniqueBreakdownTypes[0], asInputArray);
			iIncrement = 1;
		}
		//graph_singlesite_patient_number("chart0", asUniqueBreakdownTypes[0], asInputArray);
		for (var i=0+iIncrement; i<asUniqueBreakdownTypes.length; i++){
			graph_singlesite_patient_breakdown("chart"+i, asUniqueBreakdownTypes[i], asInputArray);
		}
	}
	else {
		graph_multiplesite_patient_number("chart0", asUniqueBreakdownTypes[0], asInputArray);
		for (var i=1; i<asUniqueBreakdownTypes.length; i++){
			graph_multiplesite_patient_breakdown("chart"+i, asUniqueBreakdownTypes[i], asInputArray);
		}		
     }
	
	//document.getElementById('chart1').style.height = '500px';
	//document.getElementById('chart1').style.maxHeight = '150px';
	
	/*
	// create the scroll bar behaviour = *** jQuery Stuff ***
	// store the position of the element in position
	var position = jQuery('#' + sDivName).offset(); //=position()
	// on scrolling of the document do something
	jQuery('#' + sDivName).scroll(function () {
		//the current height
		var y = jQuery(this).scrollTop();
	//console.log(y);
		//If the current Y is bigger than the element. (you scrolled beyond the element)
		if(y < 100) {
			//jQuery(this).offset().top = 0;
			jQuery(this).scrollTop(0);
		}else if (y>100 && y<200) {
			//do something else 
			jQuery(this).scrollTop(148);
		} else {
			jQuery(this).scrollTop(296);
		}
	}); */
}
catch(err) {
	console.error(err);
}
} // END of function createGraphs

/*****************************************************************************************************
   @Function parseInputIntoArray(sInputString, isMultiSite)
   @Input (String *text from query status*, Boolean false = single site, true = multiple site)
   @Output Create a two dimensional array out of these strings called asInputFragments
   Each array element is a six element array of 0 "query name", 1 "title", 2 "site", 3 "element name", 4 quantity, 5 "sentence"
   for example, one element would be ["Circulatory sys@20:21:19", "Age patient breakdown", "MGH" "0-9 years old", 0, "0-9 years old: 0"]
   
*****************************************************************************************************/
parseInputIntoArray = function(sInputString, isMultiSite) {
var sCheckForNothing = "something";  // this gets checked to be a zero length string
try {
	var old_demo = false;
	if (sInputString === undefined || sInputString === null || sInputString === "") throw ("ERROR - sInputString in function parseInputIntoArray is empty");
	var asInputFragments = [[]];
	if (!isMultiSite) {
		var asTempArray = [];
		var sLatestTitle, sLatestQueryName, sLatestElementName, iLatestQuantity, sLatestSite;
		// process input one line at a time to look for the start of a block.
		// you know it because it has two \"'s
		// begin your parsing by separating into an array of sentences that were delimited with \n
		var asInputSentences = sInputString.split("\n");
		var iFragmentArrayCounter = 0;
		for(var i = 0; i < asInputSentences.length; i++) {
			if (asInputSentences[i].indexOf("for") > 0) { 
				asTempArray = asInputSentences[i].split("for");
				sLatestTitle = asTempArray[0];
				sLatestQueryName = asTempArray[1];
				sLatestSite = ".";
				//document.write("<br /> Element " + i + " = " + asInputSentences[i]); 
			} else if (asInputSentences[i].indexOf(":") > 0) {
				asTemp2Array = asInputSentences[i].split(":");
				sLatestElementName = asTemp2Array[0];
				iLatestQuantity = asTemp2Array[1];
				//document.write("<br /> Element " + i + " = " + asInputSentences[i]); 
				asInputFragments[iFragmentArrayCounter] = new Array (6);
				asInputFragments[iFragmentArrayCounter][0] = sLatestQueryName;
				asInputFragments[iFragmentArrayCounter][1] = sLatestTitle;
				asInputFragments[iFragmentArrayCounter][2] = sLatestSite;
				asInputFragments[iFragmentArrayCounter][3] = sLatestElementName;
				asInputFragments[iFragmentArrayCounter][4] = i2b2.CRC.view.graphs.sValueOfi2b2Text(iLatestQuantity);
				asInputFragments[iFragmentArrayCounter][5] = asInputSentences[i];
				for(var j = 0; j < 6; j++) {
					//console.log("Element " + i + "@" + j + " ===== " + asInputFragments[iFragmentArrayCounter][j]); 
				}
				iFragmentArrayCounter++;
			} else {
				//document.write("<br /> ERROR? " + i + " = " + asInputSentences[i]); 
			}
			//document.write("<br /> Element " + i + " = " + sLatestTitle + " " + sLatestQueryName + " " + sLatestElementName + " " + iLatestQuantity);
		}
	}
	else if (old_demo == true) {  // parsing for old_demo SHRINE strings
		var asTempArray = [];
		var asTemp2Array = [];
		var sLatestTitle, sLatestQueryName, sLatestElementName, iLatestQuantity, sLatestSite;
		// process input one line at a time to look for the start of a block.
		// you know it because it has two \"'s
		// begin your parsing by separating into an array of sentences that were delimited with \n
		var asInputSentences = sInputString.split("\n");
		var iFragmentArrayCounter = 0;
		for (var i = 0; i < asInputSentences.length; i++) {
			if (asInputSentences[i].indexOf("for") > 0) { 
				asTempArray = asInputSentences[i].split("for");
				sLatestTitle = asTempArray[0];
				if (asTempArray[1].indexOf("=") > 0) {
					asTemp2Array = asTempArray[1].split("=");
					sLatestQueryName = asTemp2Array[0];
					sLatestSite = asTemp2Array[1];
				}
				else {
					sLatestQueryName = asTempArray[1];
					sLatestSite = "xxx";
				}
				//document.write("<br /> Element " + i + " = " + asInputSentences[i]); 
			} else if (asInputSentences[i].indexOf(":") > 0) {
				asTemp2Array = asInputSentences[i].split(":");
				sLatestElementName = asTemp2Array[0];
				iLatestQuantity = asTemp2Array[1];
				//document.write("<br /> Element " + i + " = " + asInputSentences[i]); 
				asInputFragments[iFragmentArrayCounter] = new Array (6);
				asInputFragments[iFragmentArrayCounter][0] = sLatestQueryName;
				asInputFragments[iFragmentArrayCounter][1] = sLatestTitle;
				asInputFragments[iFragmentArrayCounter][2] = sLatestSite;
				asInputFragments[iFragmentArrayCounter][3] = sLatestElementName;
				asInputFragments[iFragmentArrayCounter][4] = iLatestQuantity;
				asInputFragments[iFragmentArrayCounter][5] = asInputSentences[i];
				for(var j = 0; j < 6; j++) {
					//console.log("Element " + i + "@" + j + " ===== " + asInputFragments[iFragmentArrayCounter][j]); 
				}
				iFragmentArrayCounter++;
			} else {
				//document.write("<br /> ERROR? " + i + " = " + asInputSentences[i]); 
			}
			//document.write("<br /> Element " + i + " = " + sLatestTitle + " " + sLatestQueryName + " " + sLatestElementName + " " + iLatestQuantity);
		}
	}
	else {  // parsing for SHRINE strings
		var asTempArray = [];
		var asTemp2Array = [];
		var sLatestTitle, sLatestQueryName, sLatestElementName, iLatestQuantity, sLatestSite;
		// process input one line at a time to look for the start of a block.
		// + 'Hospital 1 \"Diseases of the@19:12:10\"\n' +   \\ Pattern is in these two lines
		// + 'Patient Count: - 67 +-3 patients\n' +          \\ to define new hospital
		// + '\n' +
		// + 'Patient Age Count Breakdown:\n' +
		// + '0-9 years old: - 10 patients or fewer\n' +
		// + '10-17 years old: - 10 patients or fewer\n' +
		// you know it because it has two \"'s
		// begin your parsing by separating into an array of sentences that were delimited with \n
		var asInputSentences = sInputString.split("\n");
		var iFragmentArrayCounter = 0;
		for(var i = 0; i < asInputSentences.length; i++) {
			if (asInputSentences[i].indexOf('\"') > 1) {          // There are two or more quotations means it is
				asTempArray = asInputSentences[i].split('\"');    // the hospital and the name of the query
				sLatestSite = asTempArray[0];
				if (asTempArray[1].indexOf('\"') > 0) {
					asTemp2Array = asTempArray[1].split('\"');
					sLatestQueryName = asTemp2Array[0];
				}
				else {
					sLatestQueryName = asTempArray[1];
				}
				//document.write("<br /> Element " + i + " = " + asInputSentences[i]);
				document.write("<br>Element " + i + " Site = " + sLatestSite + " Name = " + sLatestQueryName); 
			} else if (asInputSentences[i].indexOf("Patient Count:") > -1) {    //  Finding the "patient count" line is
				asTemp2Array = asInputSentences[i].split(":");                  //  unique in the SHRINE
				sLatestTitle = 'Patient Count';
				sLatestElementName = 'Patient Count';
				iLatestQuantity = asTemp2Array[1];
				//document.write("<br /> Element " + i + " = " + asInputSentences[i]); 
				asInputFragments[iFragmentArrayCounter] = new Array (6);
				asInputFragments[iFragmentArrayCounter][0] = sLatestQueryName;
				asInputFragments[iFragmentArrayCounter][1] = sLatestTitle;
				asInputFragments[iFragmentArrayCounter][2] = sLatestSite;
				asInputFragments[iFragmentArrayCounter][3] = sLatestElementName;
				asInputFragments[iFragmentArrayCounter][4] = iLatestQuantity;
				asInputFragments[iFragmentArrayCounter][5] = asInputSentences[i];
				for(var j = 0; j < 6; j++) {
					document.write("<br>Element " + i + "@" + j + " ===== " + asInputFragments[iFragmentArrayCounter][j] + "====="); 
				}
				iFragmentArrayCounter++;  // don't want to advance array counter
			/*} else if (i2b2.CRC.view.graphs.asTitleOfShrineGroup.indexOf(asInputSentences[i].toLowerCase().trim()) > -1) {
				asTemp2Array = asInputSentences[i].split(":");                    // Finding a type of breakdown
				sLatestTitle = asTemp2Array[0].trim();
				sCheckForNothing = asTemp2Array[1];
				if (sCheckForNothing.length() != 0) {
					//console.log("Supposed to be nothing, but is ->" + sCheckForNothing + "<-")
				} else {
					document.write("<br>Got this for title of breakdown -=" + sLatestTitle + "=-")
				}
				iFragmentArrayCounter++;*/
			} else if (asInputSentences[i].indexOf(":") > -1) {                   // parsing the values
				asTemp2Array = asInputSentences[i].split(":");
				sLatestElementName = asTemp2Array[0];
				iLatestQuantity = asTemp2Array[1];
				if (i2b2.CRC.view.graphs.asTitleOfShrineGroup.indexOf(sLatestElementName.toLowerCase().trim()) > -1) {
					sLatestTitle = sLatestElementName.trim();
					sCheckForNothing = iLatestQuantity;
					/*if (sCheckForNothing.length() != 0) {
						alert("a " + sLatestElementName.toLowerCase().trim());
						//console.log("Supposed to be nothing, but is ->" + sCheckForNothing + "<-")
					} else {
						document.write("<br>Got this for title of breakdown -=" + sLatestTitle + "=-");
						alert("b " + sLatestElementName.toLowerCase().trim());
					}*/
				}
				//document.write("<br /> Element " + i + " = " + asInputSentences[i]); 
				asInputFragments[iFragmentArrayCounter] = new Array (6);
				asInputFragments[iFragmentArrayCounter][0] = sLatestQueryName;
				asInputFragments[iFragmentArrayCounter][1] = sLatestTitle;
				asInputFragments[iFragmentArrayCounter][2] = sLatestSite;
				asInputFragments[iFragmentArrayCounter][3] = sLatestElementName;
				asInputFragments[iFragmentArrayCounter][4] = iLatestQuantity;
				asInputFragments[iFragmentArrayCounter][5] = asInputSentences[i];
				for(var j = 0; j < 6; j++) {
					document.write("<br>Element " + i + "@" + j + " ===== " + asInputFragments[iFragmentArrayCounter][j] + "====="); 
				}
				iFragmentArrayCounter++;
			} else {
				document.write("<br> ERROR? " + i + " = " + asInputSentences[i]); 
			}
			//document.write("<br> Element " + i + " = " + sLatestTitle + " " + sLatestQueryName + " " + sLatestElementName + " " + iLatestQuantity);
		}
	}
	// Just for debugging ...
	for(var i = 0; i < iFragmentArrayCounter; i++) {
		for(var j = 0; j < 6; j++) {
			//console.log("Element " + i + "@" + j + " ===== " + (!asInputFragments[i][j] ? "NotDef" : asInputFragments[i][j]) ); 
		}
	}
	return asInputFragments
}
catch(err) {
	console.error(err);
}
} // END of function parseInputIntoArray


/*********************************************************************************
   FUNCTION graph_singlesite_patient_number
   Fills in the Div for a single patient number display
**********************************************************************************/
function graph_singlesite_patient_number(sDivName, sBreakdownType, asInputFragments) {
try {
	if (sBreakdownType === undefined || sBreakdownType === null) throw ("ERROR - sBreakdownType in function graph_patient_breakdown is null");
	var asBreakdownArray = [[]];
	var iBreakdown = 0;

	// for loop to only pull out data from the breakdown type specified in sBreakdownType variable 
	for (var i = 0; i < asInputFragments.length; i++) {
		if (asInputFragments[i][1].toLowerCase().trim() === sBreakdownType.toLowerCase().trim()) {
			//document.write("<br /> OK? " + i + " = " + asInputFragments[i][0]);
			asBreakdownArray[iBreakdown] = new Array (2);
			asBreakdownArray[iBreakdown][0] = asInputFragments[i][3];
			asBreakdownArray[iBreakdown][1] = asInputFragments[i][4];
			iBreakdown++;
		} else {
			//document.write("<br /> ERROR? " + i + " = " + asInputFragments[i][0]);
		}
	}
	// establish style and draw out containing Div
	var sDivStyle = "font-family: Verdana, Geneva, sans-serif;" 
	        + "font-size: 12px;"
            + "text-align: center;"
            + "vertical-align: middle;"
            + "background-color: white;"
            + "height: 100%;"
            + "width: 100%;";
	document.getElementById(sDivName).setAttribute("style",sDivStyle);
	// establish table in Div and set up its style.
	var sDisplayNumber = i2b2.CRC.view.graphs.sTexti2b2Value(asBreakdownArray[0][1]);
	var sTableHtml = '<table style="width: 400px; margin-left: auto; margin-right: auto;">' +
                        '<tr style="background-color: white">' +
					        '<td style="color: red; text-align: center; vertical-align: middle;">&nbsp</td>' +
						'</tr>' +
                        '<tr style="background-color: #B0C4DE">' +
					        '<td style="color: black; text-align: center; vertical-align: middle;">'+sBreakdownType+'</td>' +
						'</tr>' +
                        '<tr style="background-color: #B0C4DE">' +
					        '<td style="color: darkblue; text-align: center; vertical-align: middle; font-size: 45px">'+sDisplayNumber+'</td>' +
						'</tr>' +
                        '<tr style="background-color: #B0C4DE">' +
					        '<td style="color: black; text-align: center; vertical-align: middle;">For Query '+asInputFragments[0][0]+'</td>' +
						'</tr>' +
                        '<tr style="background-color: white">' +
					        '<td style="color: red; text-align: center; vertical-align: middle;">&nbsp</td>' +
						'</tr>' +
						'</table>';
	document.getElementById(sDivName).innerHTML = sTableHtml;
}
catch(err) {
	console.error(err);
}
} // END of function graph_single_patient_number


/*********************************************************************************
   FUNCTION graph_singlesite_patient_breakdown
   function where the dataset is displayed
**********************************************************************************/
function graph_singlesite_patient_breakdown(sDivName,sBreakdownType,asInputFragments) {

try {
	if (sBreakdownType === undefined || sBreakdownType === null) throw ("ERROR 101 in graph_patient_breakdown, sBreakdownType is null");
	var asBreakdownArray = [[]];
	var iBreakdown = 0;
	// for loop to only pull out data from the breakdown type specified in sBreakdownType variable
	// for multiple sites make a data array for each site
	for (var i = 0; i < asInputFragments.length; i++) {
		if (asInputFragments[i][1].toLowerCase().trim() === sBreakdownType.toLowerCase().trim()) {
			//console.log("IN> " + i + " = " + asInputFragments[i][2]);
			asBreakdownArray[iBreakdown] = new Array (3);
			asBreakdownArray[iBreakdown][0] = asInputFragments[i][2]; // site
			asBreakdownArray[iBreakdown][1] = asInputFragments[i][3]; // text
			asBreakdownArray[iBreakdown][2] = asInputFragments[i][4]; // number
			iBreakdown++;
		} else {
			//console.log("OUT> " + i + " = " + asInputFragments[i][0]);  // items that were left out
		}
	}
	// the text 'patient breakdown' is removed and remainder trimmed
	var sBreakdownText = "";
	var iPbLocation = sBreakdownType.toLowerCase().indexOf(" patient breakdown");
	if (iPbLocation != -1) {
		sBreakdownText = sBreakdownType.substring(0,iPbLocation);
		//console.log(sBreakdownText + "|");
	} else {
		sBreakdownText = sBreakdownType;
		//console.log(sBreakdownText + "it is");
	}
	// function where the dataset arrays are created:
	var iBreakdown = 1;
	var c3xaxis = new Array();
	var c3values = new Array();
	c3xaxis[0] = 'x';
	c3values[0] = sBreakdownText;
	for (var i = 0; i < asInputFragments.length; i++) {
		if (asInputFragments[i][1].toLowerCase().trim() === sBreakdownType.toLowerCase().trim()) {
			//document.write("<br /> OK? " + i + " = " + asInputFragments[i][0]);
			c3xaxis[iBreakdown] = asInputFragments[i][3];
			c3values[iBreakdown] = asInputFragments[i][4];
			iBreakdown++;
		} else {
			//document.write("<br /> ERROR? " + i + " = " + asInputFragments[i][0]);
		}
	}
		
	// Trying out some C3
	var graph_color = 'darkblue';
	//var graph_color = 'hsl(' + Math.floor( 360 * Math.random() ) + ', 85%, 55%)'; // random color
	var chart = c3.generate({
		bindto: '#' + sDivName,
		size: { 
			//width: 535,
			height: 146
		},
		data: {
			x: 'x',
			columns: [
				c3xaxis,
				c3values
			],
			type: 'bar',
			color: function (color, d) {return graph_color;},
			labels: true
		},
		legend: {
			//position: 'inset'
			position: 'right',
		},
		axis: {
			x: {
				type: 'category',
				tick: {
					rotate: 25
				},
				height: 45
			},
			y: {
				label: {
					text: 'Number of Patients',
					//position: 'outer-middle',
					position: 'outer-bottom'
				}
			}
		},
		bar: {
			width: {
				ratio: 0.75 // this makes bar width 75% of length between ticks
			}
		}
	});
}
catch(err) {
	console.error(err);
}
}; // end of function graph_singlesite_patient_breakdown


/*********************************************************************************
   FUNCTION graph_multiplesite_patient_number
   Fills in the Div for multiple patient number display
**********************************************************************************/
function graph_multiplesite_patient_number(sDivName,sBreakdownType,asInputFragments) {
try {
	if (sBreakdownType === undefined || sBreakdownType === null) throw ("ERROR - sBreakdownType in function graph_patient_breakdown is null");
	var asBreakdownArray = [[]];
	var iBreakdown = 0;
	// for loop to only pull out data from the breakdown type specified in sBreakdownType variable
	// for multiple sites make a data array for each site
	for (var i = 0; i < asInputFragments.length; i++) {
		if (asInputFragments[i][1].toLowerCase().trim() === sBreakdownType.toLowerCase().trim()) {
			//console.log("IN> " + i + " = " + asInputFragments[i][2]);
			asBreakdownArray[iBreakdown] = new Array (3);
			asBreakdownArray[iBreakdown][0] = asInputFragments[i][2]; // site
			asBreakdownArray[iBreakdown][1] = asInputFragments[i][3]; // text
			asBreakdownArray[iBreakdown][2] = asInputFragments[i][4]; // number
			iBreakdown++;
		} else {
			//console.log("OUT> " + i + " = " + asInputFragments[i][0]);  // items that were left out
		}
	}
	// function where the dataset arrays are created:
	var c3values = new Array();
	for (var i = 0; i < asBreakdownArray.length; i++) {
		//console.log("Element " + i + " = " + asBreakdownArray[i][0] + " " + asBreakdownArray[i][2]);
		c3values[i] = new Array(2);
		c3values[i][0] = asBreakdownArray[i][0].trim() + " " + asBreakdownArray[i][1].trim();
		c3values[i][1] = Number(asBreakdownArray[i][2]);
	}
    // C3 that makes pie chart
	var chart = c3.generate({
		bindto: '#' + sDivName,
		size: { 
			width: 535,
			height: 146
		},
		data: {
			columns: c3values,
			type: 'pie',
			//labels: false
		},
		pie: {
		    label: {
            format: d3.format('^g,') 
			},
		},
		legend: {
			//position: 'inset'
			position: 'right',
		},
		axis: {
			x: {
				type: 'category',
				tick: {
					rotate: 25
				},
				height: 45
			},
			y: {
				label: {
					text: 'Number of Patients',
					//position: 'outer-middle',
					position: 'outer-bottom'
				}
			}
		},
		bar: {
			width: {
				ratio: 0.75 // this makes bar width 75% of length between ticks
			}
		}
	});
}
catch(err) {
	console.error(err);
}
}; // end of function graph_multiplesite_patient_number


/*********************************************************************************
   FUNCTION graph_multiplesite_patient_breakdown
   function where the dataset is displayed
**********************************************************************************/
function graph_multiplesite_patient_breakdown(sDivName,sBreakdownType,asInputFragments) {
try {
	if (sBreakdownType === undefined || sBreakdownType === null) throw ("ERROR 101 in graph_patient_breakdown, sBreakdownType is null");
	var asBreakdownArray = [[]];
	var iBreakdown = 0;
	// for loop to only pull out data from the breakdown type specified in sBreakdownType variable
	// for multiple sites make a data array for each site
	for (var i = 0; i < asInputFragments.length; i++) {
		if (asInputFragments[i][1].toLowerCase().trim() === sBreakdownType.toLowerCase().trim()) {
			//console.log("IN> " + i + " = " + asInputFragments[i][2]);
			asBreakdownArray[iBreakdown] = new Array (3);
			asBreakdownArray[iBreakdown][0] = asInputFragments[i][2]; // site
			asBreakdownArray[iBreakdown][1] = asInputFragments[i][3]; // text
			asBreakdownArray[iBreakdown][2] = asInputFragments[i][4]; // number
			iBreakdown++;
		} else {
			//console.log("OUT> " + i + " = " + asInputFragments[i][0]);  // items that were left out
		}
	}
	// the text 'patient breakdown' is removed and remainder trimmed
	var sBreakdownText = "";
	var iPbLocation = sBreakdownType.toLowerCase().indexOf(" patient breakdown");
	if (iPbLocation != -1) {
		sBreakdownText = sBreakdownType.substring(0,iPbLocation);
		//console.log(sBreakdownText + "|");
	} else {
		sBreakdownText = sBreakdownType;
		//console.log(sBreakdownText + "it is");
	}
	// Pull out unique sites from the breakdown array
	var asBreakdownSites = [];
	var iBreakdown = 0;
	for (var i = 0; i < asBreakdownArray.length; i++) {
			asBreakdownSites[iBreakdown] = asBreakdownArray[i][0].trim();
			iBreakdown++;
	}
    var asUniqueBreakdownSites = [];
    for (var i=0; i < asBreakdownSites.length; i++) {
        if (asUniqueBreakdownSites.indexOf(asBreakdownSites[i]) === -1 && asBreakdownSites[i] !== '')
            asUniqueBreakdownSites.push(asBreakdownSites[i]);
    }
	if (asUniqueBreakdownSites.length === 0) throw("ERROR 102 in graph_patient_breakdown, asUniqueBreakdownSites array is null");
	console.log(asUniqueBreakdownSites);
	// set up an array from one site which can be determined or just the first
	var sSiteToGetBreakdownsFrom = "";
	if (msSpecialBreakdownSite.trim().length != 0) {
		sSiteToGetBreakdownsFrom = msSpecialBreakdownSite.trim();
	}
	else {
		sSiteToGetBreakdownsFrom = asUniqueBreakdownSites[0].trim();
	}
	if (sSiteToGetBreakdownsFrom.length === 0) throw("ERROR 103 in graph_patient_breakdown, sSiteToGetBreakdownsFrom is empty");
	// set up a data vector from the sSiteToGetBreakdownsFrom
	var iBreakdown = 1;
	var c3xaxis = new Array();
	var c3values = new Array();
	c3xaxis[0] = 'x';
	c3values[0] = sSiteToGetBreakdownsFrom;
	for (var i = 0; i < asBreakdownArray.length; i++) {
		if (asBreakdownArray[i][0].toLowerCase().trim() === sSiteToGetBreakdownsFrom.toLowerCase().trim()) {
			//document.write("<br /> OK? " + i + " = " + asBreakdownArray[i][0]);
			c3xaxis[iBreakdown] = asBreakdownArray[i][1];
			c3values[iBreakdown] = asBreakdownArray[i][2];
			iBreakdown++;
		} else {
			//document.write("<br /> ERROR? " + i + " = " + asBreakdownArray[i][0]);
		}
	}
	var c3dataarray = Array();
	c3dataarray[0] = c3xaxis;
	c3dataarray[1] = c3values;
	if (asUniqueBreakdownSites.length > 1) {
		for (var j = 1; j < asUniqueBreakdownSites.length; j++) {
			var iBreakdown = 1;
			var c3values = new Array();
			c3values[0] = asUniqueBreakdownSites[j];
			for (var i = 0; i < asBreakdownArray.length; i++) {
				if (asUniqueBreakdownSites[j].toLowerCase().trim() === asBreakdownArray[i][0].toLowerCase().trim()) {
					//document.write("<br /> OK? " + i + " = " + asBreakdownArray[i][0]);
					//c3xaxis[iBreakdown] = asBreakdownArray[i][1];
					c3values[iBreakdown] = asBreakdownArray[i][2];
					iBreakdown++;
				} else {
					//document.write("<br /> ERROR? " + i + " = " + asBreakdownArray[i][0]);
				}
			}
			c3dataarray[j+1] = c3values;
		}
	/*
	var iBreakdown = 1;
	var c3xaxis = new Array();
	var c3values = new Array();
	c3xaxis[0] = 'x';
	c3values[0] = sBreakdownText;
	for (var i = 0; i < asInputFragments.length; i++) {
		if (asInputFragments[i][1].toLowerCase().trim() === sBreakdownType.toLowerCase().trim()) {
			//document.write("<br /> OK? " + i + " = " + asInputFragments[i][0]);
			c3xaxis[iBreakdown] = asInputFragments[i][3];
			c3values[iBreakdown] = asInputFragments[i][4];
			iBreakdown++;
		} else {
			//document.write("<br /> ERROR? " + i + " = " + asInputFragments[i][0]);
		}
	}
	*/
	}
	// Trying out some C3
	//var colors = ['darkblue', 'darkblue', 'darkblue', 'darkblue', 'darkblue', 'darkblue', 'darkblue', 'darkblue', 'darkblue'];
	var chart = c3.generate({
		bindto: '#' + sDivName,
		size: { 
			width: 535,
			height: 146
		},
		data: {
			x: 'x',
			columns: c3dataarray,
			type: 'bar',
			groups: [asUniqueBreakdownSites],
			//groups: asUniqueBreakdownSites
			//color: function (color, d) {return colors[d.index];}
			//labels: false
		},
		legend: {
			//position: 'inset'
			position: 'right',
		},
		axis: {
			x: {
				type: 'category',
				tick: {
					rotate: 25
				},
				height: 45
			},
			y: {
				label: {
					text: 'Number of Patients',
					//position: 'outer-middle',
					position: 'outer-bottom'
				}
			}
		},
		bar: {
			width: {
				ratio: 0.75 // this makes bar width 75% of length between ticks
			}
		}
	});
}
catch(err) {
	console.error(err);
}
}; // end of function graph_multiplesite_patient_breakdown

/*********************************************************************************
   FUNCTION ie
   function to test for the version of internet explorer
   usage if ie < 9 then ...
**********************************************************************************/
var ie = (function(){

    var undef,
        v = 3,
        div = document.createElement('div'),
        all = div.getElementsByTagName('i');

    while (
        div.innerHTML = '<!--[if gt IE ' + (++v) + ']><i></i><![endif]-->',
        all[0]
    );

    return v > 4 ? v : undef;

}());

/*********************************************************************************
   FUNCTION bisGTIE8 
   function to specifically test for internet explorer gt 8 or any other browser 
	which returns "true"
   usage if bisGTIE8 then ...
**********************************************************************************/
i2b2.CRC.view.graphs.bisGTIE8 = (function(){
	try {
		if ( document.addEventListener ) {
			//alert("you got IE9 or greater (or a modern browser)");
			return true;
		}
		else {
			return false;
		}
	}
	catch (e) {
		return false;
	}
}());

/*********************************************************************************
   FUNCTION sValueOfi2b2Text
   Return a INTEGER number that was obfuscated = MAKES IT ZERO
**********************************************************************************/

i2b2.CRC.view.graphs.sValueOfi2b2Text = function(sValue) {
try {
	if (sValue === undefined || sValue === null || sValue === "") {
		iValue = "undefined in";
		return iValue;
	}
	if (i2b2.PM.model.isObfuscated) {
		var asTempArray = [];
		if (sValue.toLowerCase().trim() == i2b2.CRC.view.graphs.sObfuscatedText.toLowerCase().trim()) {
			iValue = "0";
		}
		else {
			if (sValue.indexOf(i2b2.CRC.view.graphs.sObfuscatedEnding) > 0) {
				var asTempArray = sValue.split(i2b2.CRC.view.graphs.sObfuscatedEnding);
				iValue = asTempArray[0];
			}
			else {
				iValue = sValue;
			}
		}
	}
	else {
		iValue = sValue;
	}
	function isNumber(obj) {return ! isNaN(obj-0) && obj; };
	if (!isNumber(iValue)) {
		iValue = "undefined #";
		return iValue;
	}
	return iValue;
}
catch(err) {
	console.error(err);
}
} // END of function sValueOfi2b2Text

/*********************************************************************************
   FUNCTION sTexti2b2Value
   Return a TEXT number that has obfuscated blurring - FOR DISPLAY ONLY
**********************************************************************************/

i2b2.CRC.view.graphs.sTexti2b2Value = function(iValue) {
try {
	if (iValue === undefined || iValue === null || iValue === "") {
		sValue = "undefined";
		return sValue;
	}
	function isNumber(obj) {return ! isNaN(obj-0) && obj; };
	if (!isNumber(iValue)) {
		sValue = "undefined";
		return sValue;
	}		
	if (iValue >= i2b2.CRC.view.graphs.iObfuscatedFloorNumber) {
		if (i2b2.PM.model.isObfuscated) {
			sValue = iValue+i2b2.CRC.view.graphs.sObfuscatedEnding;
		} else {
			sValue = iValue;
		}
	} else {
		if (i2b2.PM.model.isObfuscated) {
			sValue = i2b2.CRC.view.graphs.sObfuscatedText;
		} else {
			sValue = iValue;
		}
	}
	return sValue;
}
catch(err) {
	console.error(err);
}
} // END of function sTexti2b2Value
			
			
/*********************************************************************************
   FUNCTION clearGraphs
   Clear the previously used Div for the graphs
**********************************************************************************/
i2b2.CRC.view.graphs.clearGraphs = function(sDivNameToClear) {
try {
	if (sDivNameToClear === undefined || sDivNameToClear === null || sDivNameToClear === "") 
		throw ("ERROR 291 - i2b2.CRC.view.graphs.sNameOfPreviousDiv in function clearGraphs is null");
	//Clear Divs in the original div for the charts
	var oClearDiv = document.getElementById(sDivNameToClear);
	oClearDiv.innerHTML = "";
}
catch(err) {
	console.error(err);
}
} // END of function clearGraphs

/*********************************************************************************
   FUNCTION clearDivForIE8
   Clear Div and put message in the middle that "The Graphs Cannot Display in IE8"
**********************************************************************************/
i2b2.CRC.view.graphs.aDivForIE8 = function(sDivNameToClear) {
try {
	if (sDivNameToClear === undefined || sDivNameToClear === null || sDivNameToClear === "") 
		throw ("ERROR 291 - i2b2.CRC.view.graphs.sNameOfPreviousDiv in function clearGraphs is null");
	// Clear Divs in the original div for the charts
	var oClearDiv = document.getElementById(sDivNameToClear);		
	var child = document.createElement("div");
	child.setAttribute("id","IE_Div");
	child.setAttribute("width","auto");
	oClearDiv.appendChild(child);

    // establish style and draw out containing Div
	var sDivStyle = "font-family: Verdana, Geneva, sans-serif;" 
	        + "font-size: 12px;"
            + "text-align: center;"
            + "vertical-align: middle;"
            + "background-color: white;"
            + "height: 100%;"
            + "width: 100%;";
	child.setAttribute("style",sDivStyle);
	// establish table in Div and set up its style.
	var sTableHtml = '<table style="width: 400px; margin-left: auto; margin-right: auto;">' +
                        '<tr style="background-color: white">' +
					        '<td style="color: red; text-align: center; vertical-align: middle;">&nbsp</td>' +
						'</tr>' +
                        '<tr style="background-color: white">' +
					        '<td style="color: darkblue; text-align: center; vertical-align: middle; font-size: 20px">Graph Results is not supported for this version</td>' +
						'</tr>' +
                        '<tr style="background-color: white">' +
					        '<td style="color: darkblue; text-align: center; vertical-align: middle; font-size: 20px">of Internet Explorer. In order to display the graphs in </td>' +
						'</tr>' +                        
                        '<tr style="background-color: white">' +
					        '<td style="color: darkblue; text-align: center; vertical-align: middle; font-size: 20px">Internet Explorer you will need to use version 11 or higher.</td>' +
						'</tr>' + 
						'<tr style="background-color: white">' +
					        '<td style="color: red; text-align: center; vertical-align: middle;">&nbsp</td>' +
						'</tr>' +
						'</table>';
	child.innerHTML = sTableHtml;
}
catch(err) {
	console.error(err);
}
} // END of function clearDivForIE8

/*********************************************************************************
   FUNCTION returnTestString
   function to create some sample text input for a demo
**********************************************************************************/
i2b2.CRC.view.graphs.returnTestString = function (isSHRINE) {
    var sInput = "";
	if (!isSHRINE) {
		sInput =  "Patient Set for \"Circulatory sys@20:21:19\"\n"
		+ "Age patient breakdown for \"Circulatory sys@20:21:19\"\n"
		+ "0-9 years old: 0\n"
		+ "10-17 years old: 1\n"
		+ "18-34 years old: 20\n"
		+ "35-44 years old: 12\n"
		+ "45-54 years old: 12\n"
		+ "55-64 years old: 5\n"
		+ "65-74 years old: 9\n"
		+ "75-84 years old: 4\n"
		+ ">= 85 years old: 3\n"
		+ "Not recorded: 0\n"
		+ "Number of patients for \"Circulatory sys@20:21:19\"\n"
		+ "patient_count: 66\n"
		+ "Vital Status patient breakdown for \"Circulatory sys@20:21:19\"\n"
		+ "Deceased: 1\n"
		+ "Deferred: 0\n"
		+ "Living: 65\n"
		+ "Not recorded: 0";
	} 
	else {
		sInput = 'Finished Query: \"Diseases of the@19:12:10\"\n'
		 + '[1.7 secs]\n'
		 + '\n'
		 + 'Compute Time: 1.7 secs\n'
		 + '\n'
		 + 'Hospital 1 \"Diseases of the@19:12:10\"\n'   // Pattern is in these two lines
		 + 'Patient Count: - 67 +-3 patients\n'          // to define new hospital
		 + '\n'
		 + 'Patient Age Count Breakdown:\n'
		 + '0-9 years old: - 10 patients or fewer\n'
		 + '10-17 years old: - 10 patients or fewer\n'
		 + '18-34 years old: - 15 +-3 patients\n'
		 + '35-44 years old: - 13 +-3 patients\n'
		 + '45-54 years old: - 15 +-3 patients\n'
		 + '55-64 years old: - 10 patients or fewer\n'
		 + '65-74 years old: - 10 patients or fewer\n'
		 + '75-84 years old: - 10 patients or fewer\n'
		 + '>= 65 years old: - 18 +-3 patients\n'
		 + '>= 85 years old: - 10 patients or fewer\n'
		 + 'Not recorded: - 10 patients or fewer\n'
		 + '\n'
		 + 'Patient Gender Count Breakdown:\n'
		 + 'Female: - 26 +-3 patients\n'
		 + 'Male: - 40 +-3 patients\n'
		 + 'Unknown: - 10 patients or fewer\n'
		 + '\n'
		 + 'Patient Race Count Breakdown:\n'
		 + 'Aleutian: - 10 patients or fewer\n'
		 + 'American Indian: - 10 patients or fewer\n'
		 + 'Asian: - 10 +-3 patients\n'
		 + 'Asian Pacific Islander: - 10 patients or fewer\n'
		 + 'Black: - 25 +-3 patients\n'
		 + 'Eskimo: - 10 patients or fewer\n'
		 + 'Hispanic: - 20 +-3 patients\n'
		 + 'Indian: - 10 patients or fewer\n'
		 + 'Middle Eastern: - 10 patients or fewer\n'
		 + 'Multiracial: - 10 patients or fewer\n'
		 + 'Native American: - 10 patients or fewer\n'
		 + 'Navajo: - 10 patients or fewer\n'
		 + 'Not recorded: - 10 patients or fewer\n'
		 + 'Oriental: - 10 patients or fewer\n'
		 + 'Other: - 10 patients or fewer\n'
		 + 'White: - 10 patients or fewer\n'
		 + '\n'
		 + 'Patient Vital Status Count Breakdown:\n'
		 + 'Deceased: - 10 patients or fewer\n'
		 + 'Deferred: - 10 patients or fewer\n'
		 + 'Living: - 64 +-3 patients\n'
		 + 'Not recorded: - 10 patients or fewer\n'
		 + '\n'
		 + 'Hospital 3 \"Diseases of the@19:12:10\"\n'
		 + 'Patient Count: - 66 +-3 patients\n'
		 + '\n'
		 + 'Patient Age Count Breakdown:\n'
		 + '0-9 years old: - 10 patients or fewer\n'
		 + '10-17 years old: - 10 patients or fewer\n'
		 + '18-34 years old: - 14 +-3 patients\n'
		 + '35-44 years old: - 13 +-3 patients\n'
		 + '45-54 years old: - 17 +-3 patients\n'
		 + '55-64 years old: - 10 patients or fewer\n'
		 + '65-74 years old: - 10 patients or fewer\n'
		 + '75-84 years old: - 10 patients or fewer\n'
		 + '>= 65 years old: - 15 +-3 patients\n'
		 + '>= 85 years old: - 10 patients or fewer\n'
		 + 'Not recorded: - 10 patients or fewer\n'
		 + '\n'
		 + 'Patient Gender Count Breakdown:\n'
		 + 'Female: - 26 +-3 patients\n'
		 + 'Male: - 38 +-3 patients\n'
		 + 'Unknown: - 10 patients or fewer\n'
		 + '\n'
		 + 'Patient Race Count Breakdown:\n'
		 + 'Aleutian: - 10 patients or fewer\n'
		 + 'American Indian: - 10 patients or fewer\n'
		 + 'Asian: - 12 +-3 patients\n'
		 + 'Asian Pacific Islander: - 10 patients or fewer\n'
		 + 'Black: - 25 +-3 patients\n'
		 + 'Eskimo: - 10 patients or fewer\n'
		 + 'Hispanic: - 15 +-3 patients\n'
		 + 'Indian: - 10 patients or fewer\n'
		 + 'Middle Eastern: - 10 patients or fewer\n'
		 + 'Multiracial: - 10 patients or fewer\n'
		 + 'Native American: - 10 patients or fewer\n'
		 + 'Navajo: - 10 patients or fewer\n'
		 + 'Not recorded: - 10 patients or fewer\n'
		 + 'Oriental: - 10 patients or fewer\n'
		 + 'Other: - 10 patients or fewer\n'
		 + 'White: - 10 patients or fewer\n'
		 + '\n'
		 + 'Patient Vital Status Count Breakdown:\n'
		 + 'Deceased: - 10 patients or fewer\n'
		 + 'Deferred: - 10 patients or fewer\n'
		 + 'Living: - 66 +-3 patients\n'
		 + 'Not recorded: - 10 patients or fewer\n'
		 + '\n'
		 + 'Hospital 4 \"Diseases of the@19:12:10\"\n'
		 + 'Patient Count: - 67 +-3 patients\n'
		 + '\n'
		 + 'Patient Age Count Breakdown:\n'
		 + '0-9 years old: - 10 patients or fewer\n'
		 + '10-17 years old: - 10 patients or fewer\n'
		 + '18-34 years old: - 17 +-3 patients\n'
		 + '35-44 years old: - 15 +-3 patients\n'
		 + '45-54 years old: - 14 +-3 patients\n'
		 + '55-64 years old: - 10 patients or fewer\n'
		 + '65-74 years old: - 10 patients or fewer\n'
		 + '75-84 years old: - 10 patients or fewer\n'
		 + '>= 65 years old: - 17 +-3 patients\n'
		 + '>= 85 years old: - 10 patients or fewer\n'
		 + 'Not recorded: - 10 patients or fewer\n'
		 + '\n'
		 + 'Patient Gender Count Breakdown:\n'
		 + 'Female: - 23 +-3 patients\n'
		 + 'Male: - 40 +-3 patients\n'
		 + 'Unknown: - 10 patients or fewer\n'
		 + '\n'
		 + 'Patient Race Count Breakdown:\n'
		 + 'Aleutian: - 10 patients or fewer\n'
		 + 'American Indian: - 10 patients or fewer\n'
		 + 'Asian: - 13 +-3 patients\n'
		 + 'Asian Pacific Islander: - 10 patients or fewer\n'
		 + 'Black: - 24 +-3 patients\n'
		 + 'Eskimo: - 10 patients or fewer\n'
		 + 'Hispanic: - 18 +-3 patients\n'
		 + 'Indian: - 10 patients or fewer\n'
		 + 'Middle Eastern: - 10 patients or fewer\n'
		 + 'Multiracial: - 10 patients or fewer\n'
		 + 'Native American: - 10 patients or fewer\n'
		 + 'Navajo: - 10 patients or fewer\n'
		 + 'Not recorded: - 10 patients or fewer\n'
		 + 'Oriental: - 10 patients or fewer\n'
		 + 'Other: - 10 patients or fewer\n'
		 + 'White: - 10 patients or fewer\n'
		 + '\n'
		 + 'Patient Vital Status Count Breakdown:\n'
		 + 'Deceased: - 10 patients or fewer\n'
		 + 'Deferred: - 10 patients or fewer\n'
		 + 'Living: - 65 +-3 patients\n'
		 + 'Not recorded: - 10 patients or fewer\n'
		 + '\n'
		 + 'Hospital 2 \"Diseases of the@19:12:10\"\n'
		 + 'Patient Count: - 68 +-3 patients\n'
		 + '\n'
		 + 'Patient Age Count Breakdown:\n'
		 + '0-9 years old: - 10 patients or fewer\n'
		 + '10-17 years old: - 10 patients or fewer\n'
		 + '18-34 years old: - 16 +-3 patients\n'
		 + '35-44 years old: - 16 +-3 patients\n'
		 + '45-54 years old: - 13 +-3 patients\n'
		 + '55-64 years old: - 10 patients or fewer\n'
		 + '65-74 years old: - 10 patients or fewer\n'
		 + '75-84 years old: - 10 patients or fewer\n'
		 + '>= 65 years old: - 16 +-3 patients\n'
		 + '>= 85 years old: - 10 patients or fewer\n'
		 + 'Not recorded: - 10 patients or fewer\n'
		 + '\n'
		 + 'Patient Gender Count Breakdown:\n'
		 + 'Female: - 24 +-3 patients\n'
		 + 'Male: - 40 +-3 patients\n'
		 + 'Unknown: - 10 patients or fewer\n'
		 + '\n'
		 + 'Patient Race Count Breakdown:\n'
		 + 'Aleutian: - 10 patients or fewer\n'
		 + 'American Indian: - 10 patients or fewer\n'
		 + 'Asian: - 14 +-3 patients\n'
		 + 'Asian Pacific Islander: - 10 patients or fewer\n'
		 + 'Black: - 25 +-3 patients\n'
		 + 'Eskimo: - 10 patients or fewer\n'
		 + 'Hispanic: - 18 +-3 patients\n'
		 + 'Indian: - 10 patients or fewer\n'
		 + 'Middle Eastern: - 10 patients or fewer\n'
		 + 'Multiracial: - 10 patients or fewer\n'
		 + 'Native American: - 10 patients or fewer\n'
		 + 'Navajo: - 10 patients or fewer\n'
		 + 'Not recorded: - 10 patients or fewer\n'
		 + 'Oriental: - 10 patients or fewer\n'
		 + 'Other: - 10 patients or fewer\n'
		 + 'White: - 10 patients or fewer\n'
		 + '\n'
		 + 'Patient Vital Status Count Breakdown:\n'
		 + 'Deceased: - 10 patients or fewer\n'
		 + 'Deferred: - 10 patients or fewer\n'
		 + 'Living: - 65 +-3 patients\n'
		 + 'Not recorded: - 10 patients or fewer\n';
	}
	/*else { // this was the original guess of SHRINE output
		sInput =  "Patient Set for \"Circulatory sys@20:21:19\" = Partners\n"
		+ "Age patient breakdown for \"Circulatory sys@20:21:19\" = Partners\n"
		+ "0-9 years old: 422\n"
		+ "10-17 years old: 1013\n"
		+ "18-34 years old: 5009\n"
		+ "35-44 years old: 12788\n"
		+ "45-54 years old: 12768\n"
		+ "55-64 years old: 5786\n"
		+ "65-74 years old: 977\n"
		+ "75-84 years old: 454\n"
		+ ">= 85 years old: 356\n"
		+ "Not recorded: 0\n"
		+ "Number of patients for \"Circulatory sys@20:21:19\" = Partners\n"
		+ "patient_count: 203866\n"
		+ "Vital Status patient breakdown for \"Circulatory sys@20:21:19\" = Partners\n"
		+ "Deceased: 1\n"
		+ "Deferred: 0\n"
		+ "Living: 65\n"
		+ "Not recorded: 0\n"
		+ "Patient Set for \"Circulatory sys@20:21:19\" = CHB\n"
		+ "Age patient breakdown for \"Circulatory sys@20:21:19\" = CHB\n"
		+ "0-9 years old: 7483\n"
		+ "10-17 years old: 1544\n"
		+ "18-34 years old: 2055\n"
		+ "35-44 years old: 124\n"
		+ "45-54 years old: 125\n"
		+ "55-64 years old: 51\n"
		+ "65-74 years old: 9\n"
		+ "75-84 years old: 0\n"
		+ ">= 85 years old: 0\n"
		+ "Not recorded: 0\n"
		+ "Number of patients for \"Circulatory sys@20:21:19\" = CHB\n"
		+ "patient_count: 66876\n"
		+ "Vital Status patient breakdown for \"Circulatory sys@20:21:19\" = CHB\n"
		+ "Deceased: 1\n"
		+ "Deferred: 0\n"
		+ "Living: 65\n"
		+ "Not recorded: 0\n"
		+ "Patient Set for \"Circulatory sys@20:21:19\" = BIDMC\n"
		+ "Age patient breakdown for \"Circulatory sys@20:21:19\" = BIDMC\n"
		+ "0-9 years old: 0\n"
		+ "10-17 years old: 156\n"
		+ "18-34 years old: 2066\n"
		+ "35-44 years old: 1266\n"
		+ "45-54 years old: 12456\n"
		+ "55-64 years old: 5655\n"
		+ "65-74 years old: 9546\n"
		+ "75-84 years old: 4555\n"
		+ ">= 65 years old: 1653\n"
		+ ">= 85 years old: 334\n"
		+ "Not recorded: 45\n"
		+ "Number of patients for \"Circulatory sys@20:21:19\" = BIDMC\n"
		+ "patient_count: 676806\n"
		+ "Vital Status patient breakdown for \"Circulatory sys@20:21:19\" = BIDMC\n"
		+ "Deceased: 1\n"
		+ "Deferred: 0\n"
		+ "Living: 65\n"
		+ "Not recorded: 0\n"
		+ "Patient Set for \"Circulatory sys@20:21:19\" = DFCI\n"
		+ "Age patient breakdown for \"Circulatory sys@20:21:19\" = DFCI\n"
		+ "0-9 years old: 0\n"
		+ "10-17 years old: 1\n"
		+ "18-34 years old: 20\n"
		+ "35-44 years old: 12\n"
		+ "45-54 years old: 12\n"
		+ "55-64 years old: 5\n"
		+ "65-74 years old: 9\n"
		+ "75-84 years old: 4\n"
		+ ">= 65 years old: 16\n"
		+ ">= 85 years old: 3\n"
		+ "Not recorded: 0\n"
		+ "Number of patients for \"Circulatory sys@20:21:19\" = DFCI\n"
		+ "patient_count: 85647\n"
		+ "Vital Status patient breakdown for \"Circulatory sys@20:21:19\" = DFCI\n"
		+ "Deceased: 1\n"
		+ "Deferred: 0\n"
		+ "Living: 65\n"
		+ "Not recorded: 0\n";
	}*/
	return sInput;
}

//console.timeEnd('execute time');
//console.groupEnd();