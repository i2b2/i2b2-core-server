<?php

/****************************************************************

  PHP-BASED I2B2 PROXY "CELL" 

(does not use SimpleXML library)

	Author: Nick Benik
	Last Revised: 10-28-08

*****************************************************************

This file acts as a simple i2b2 proxy cell.  If no variables have been sent it is assumed that the request is from a 
user's Web browser requesting the default page for the current directory.  In this case, this file will read the 
contents of the default.htm file and return its contents to the browser via the current HTTP connection.

*/







$pmURL = "http://127.0.0.1:8080/i2b2/rest/PMService/getServices";
$pmCheckAllRequests = false;


$WHITELIST = array(
	"http://",
	"http://127.0.0.1:9090/axis2/rest/",
	"http://localhost:9090/axis2/rest/",
	"http://127.0.0.1:7070/i2b2/rest/",
	"http://localhost:7070/i2b2/rest/",
	"http://webservices.i2b2.org",
	"https://webservices.i2b2.org"
);



$BLACKLIST = array(
	"http://127.0.0.1:9090/test",
	"http://localhost:9090/test",
	"http://127.0.0.1:7070/test",
	"http://localhost:7070/test"
);









$PostBody = file_get_contents("php://input");
if ($PostBody=="") {
	// no POST variables sent, assume this is user navigation
	// load the inital page "default.htm"
	
	$IndexFile = dirname($_SERVER["SCRIPT_FILENAME"]).'/default.htm';

	if (!file_exists($IndexFile) || !is_file($IndexFile)) {
		die("The initial HTML file does not exist!");
	} else {
		// read and passthru the file contents to the browser
		readfile($IndexFile);
	}
} else {
	// Process the POST for proxy redirection

	// Validate that POST data is XML and extract <proxy> tag
	$startPos = strpos($PostBody,"<redirect_url>") + 14;
	$endPos = strpos($PostBody,"</redirect_url>", $startPos);
	$proxyURL = substr($PostBody, $startPos, ($endPos - $startPos));
	$newXML = $PostBody;

	if ($pmCheckAllRequests) 
	{	
	error_log("Searhing for Security in " . $PostBody);
	//Validate that user is valid against known PM

        preg_match("/<security(.*)?>(.*)?<\/security>/", $PostBody, $proxySecurity);

	error_log("My Security is " .  $proxySecurity[1]);
        preg_match("/<domain(.*)?>(.*)?<\/domain>/", $proxySecurity[0], $proxyDomain);
        preg_match("/<username(.*)?>(.*)?<\/username>/", $proxySecurity[0], $proxyUsername);
	preg_match("/<password(.*)?>(.*)?<\/password>/", $proxySecurity[0], $proxyPassword);

	$checkPMXML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><i2b2:request xmlns:i2b2=\"http://www.i2b2.org/xsd/hive/msg/1.1/\" xmlns:pm=\"http://www.i2b2.org/xsd/cell/pm/1.1/\"> <message_header> <i2b2_version_compatible>1.1</i2b2_version_compatible> <hl7_version_compatible>2.4</hl7_version_compatible> <sending_application> <application_name>i2b2 Project Management</application_name> <application_version>1.1</application_version> </sending_application> <sending_facility> <facility_name>i2b2 Hive</facility_name> </sending_facility> <receiving_application> <application_name>Project Management Cell</application_name> <application_version>1.1</application_version> </receiving_application> <receiving_facility> <facility_name>i2b2 Hive</facility_name> </receiving_facility> <datetime_of_message>2007-04-09T15:19:18.906-04:00</datetime_of_message> <security> " . $proxyDomain[0] .  $proxyUsername[0] .  $proxyPassword[0] . " </security> <message_control_id> <message_num>0qazI4rX6SDlQlk46wqQ3</message_num> <instance_num>0</instance_num> </message_control_id> <processing_id> <processing_id>P</processing_id> <processing_mode>I</processing_mode> </processing_id> <accept_acknowledgement_type>AL</accept_acknowledgement_type> <application_acknowledgement_type>AL</application_acknowledgement_type> <country_code>US</country_code> <project_id>undefined</project_id> </message_header> <request_header> <result_waittime_ms>180000</result_waittime_ms> </request_header> <message_body> <pm:get_user_configuration> <project>undefined</project> </pm:get_user_configuration> </message_body></i2b2:request>";
 	// Process the POST for proxy redirection
 


	error_log($checkPMXML,0 );
	error_log("My proxy: " . $proxyURL, 0);

	}

	// ---------------------------------------------------
	//   white-list processing on the URL
	// ---------------------------------------------------
	$isAllowed = false;
	$requestedURL = strtoupper($proxyURL);
	foreach ($WHITELIST as $entryValue) {
		$checkValue = strtoupper(substr($requestedURL, 0, strlen($entryValue)));
		if ($checkValue == strtoupper($entryValue)) { 
			$isAllowed = true;
			break;
		}
	}
	if (!$isAllowed) {
		// security as failed - exit here and don't allow one more line of execution the opportunity to reverse this
		die("The proxy has refused to relay your request.");
	}
	// ---------------------------------------------------
	//   black-list processing on the URL
	// ---------------------------------------------------
	foreach ($BLACKLIST as $entryValue) {
		$checkValue = strtoupper(substr($requestedURL, 0, strlen($entryValue)));
		if ($checkValue == strtoupper($entryValue)) { 
			// security as failed - exit here and don't allow one more line of execution the opportunity to reverse this
			die("The proxy has refused to relay your request.");
		}
	}



	if ($pmCheckAllRequests) {
        // open the URL and forward the new XML in the POST body
        $proxyRequest = curl_init($pmURL);

        // these options are set for hyper-vigilance purposes
        curl_setopt($proxyRequest, CURLOPT_COOKIESESSION, 0);
        curl_setopt($proxyRequest, CURLOPT_FORBID_REUSE, 1);
        curl_setopt($proxyRequest, CURLOPT_FRESH_CONNECT, 0);
        // Specify NIC to use for outgoing connection, fixes firewall+DMZ headaches
        // curl_setopt($proxyRequest, CURLOPT_INTERFACE, "XXX.XXX.XXX.XXX");
        // other options
        curl_setopt($proxyRequest, CURLOPT_RETURNTRANSFER, 1);
        curl_setopt($proxyRequest, CURLOPT_CONNECTTIMEOUT, 900);        // wait 15 minutes
        // data to proxy thru
        curl_setopt($proxyRequest, CURLOPT_POST, 1);
        curl_setopt($proxyRequest, CURLOPT_POSTFIELDS, $checkPMXML);
        // SEND REQUEST!!!
        curl_setopt($proxyRequest, CURLOPT_HTTPHEADER, array('Expect:', 'Content-Type: text/xml'));
        $proxyResult = curl_exec($proxyRequest);
        // cleanup cURL connection
        curl_close($proxyRequest);
	error_log("My PM Result " . $proxyResult);


        $pattern = "/<status type=\"ERROR\">/i";
        //Check if request is valid
        if (preg_match($pattern, $proxyResult)) {
	error_log("Local PM denied request");
                die("Local PM server could not validate the request.");
        }

	}


	// open the URL and forward the new XML in the POST body
	$proxyRequest = curl_init($proxyURL);
	
	// these options are set for hyper-vigilance purposes
	curl_setopt($proxyRequest, CURLOPT_COOKIESESSION, 0);
	curl_setopt($proxyRequest, CURLOPT_FORBID_REUSE, 1);
	curl_setopt($proxyRequest, CURLOPT_FRESH_CONNECT, 0);
	// Specify NIC to use for outgoing connection, fixes firewall+DMZ headaches
	// curl_setopt($proxyRequest, CURLOPT_INTERFACE, "XXX.XXX.XXX.XXX");  
	// other options
	curl_setopt($proxyRequest, CURLOPT_RETURNTRANSFER, 1);
	curl_setopt($proxyRequest, CURLOPT_CONNECTTIMEOUT, 900); 	// wait 15 minutes
	// data to proxy thru
	curl_setopt($proxyRequest, CURLOPT_POST, 1);
	curl_setopt($proxyRequest, CURLOPT_POSTFIELDS, $newXML);
	// SEND REQUEST!!!
	curl_setopt($proxyRequest, CURLOPT_HTTPHEADER, array('Expect:', 'Content-Type: text/xml'));
	$proxyResult = curl_exec($proxyRequest);
	// cleanup cURL connection
	curl_close($proxyRequest);


	// perform any analysis or processing on the returned result here
	header("Content-Type: text/xml", true);
	print($proxyResult);

}


?>
