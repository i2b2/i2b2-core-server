<%--
  ~ Licensed to the Apache Software Foundation (ASF) under one
  ~ or more contributor license agreements. See the NOTICE file
  ~ distributed with this work for additional information
  ~ regarding copyright ownership. The ASF licenses this file
  ~ to you under the Apache License, Version 2.0 (the
  ~ "License"); you may not use this file except in compliance
  ~ with the License. You may obtain a copy of the License at
  ~
  ~ http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing,
  ~ software distributed under the License is distributed on an
  ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  ~ KIND, either express or implied. See the License for the
  ~ specific language governing permissions and limitations
  ~ under the License.
  --%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page session="false" %>
<%@ page import="org.apache.axis2.Constants,
                 org.apache.axis2.description.AxisOperation" %>
<%@ page import="org.apache.axis2.description.AxisService" %>
<%@ page import="org.apache.axis2.util.Utils" %>
<%@ page import="java.util.Collection" %>
<%@ page import="java.util.Enumeration" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Hashtable" %>
<%@ page import="java.util.Iterator" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>
<head>
<jsp:include page="/WEB-INF/include/httpbase.jsp"/>
<title>List Services</title>
    <link href="axis2-web/css/axis-style.css" rel="stylesheet" type="text/css">
</head>

<body>
<jsp:include page="/WEB-INF/include/header.inc"/>
<jsp:include page="/WEB-INF/include/link-footer.jsp"/>
<h1>Available services</h1>
<% String prefix = request.getAttribute("frontendHostUrl") + (String)request.getAttribute(Constants.SERVICE_PATH) + "/";
%>
<%
    Hashtable errornessservice = (Hashtable) request.getAttribute(Constants.ERROR_SERVICE_MAP);
    boolean status = false;
%>
<c:forEach var="service" items="${requestScope.configContext.axisConfiguration.services.values()}">
<%
            AxisService axisService = (AxisService) pageContext.getAttribute("service");
            if (!Utils.isHiddenService(axisService)) {
            Iterator opItr = axisService.getOperations();
            String serviceName = axisService.getName();
%><h2><a style="color:blue" href="<%=prefix + axisService.getName()%>?wsdl"><%=serviceName%></a></h2>
<%
    String serviceDescription = axisService.getDocumentation();
    if (serviceDescription == null || "".equals(serviceDescription)) {
        serviceDescription = "No description available for this service";
    }
%>
<h5>Service Description : <span style="color:black;"><%=serviceDescription%></span></h5>
<h5>Service EPR : <%=prefix + axisService.getName()%></h5>
<h5>Service Status : <%=axisService.isActive() ? "Active" : "InActive"%></h5><br>
<%
    if (opItr.hasNext()) {
%><i>Available Operations</i><%
} else {
%><i> There are no Operations specified</i><%
    }
    opItr = axisService.getOperations();
%><ul><%
    while (opItr.hasNext()) {
        AxisOperation axisOperation = (AxisOperation) opItr.next();
%><li><%=axisOperation.getName().getLocalPart()%></li>
    <%--                <br>Operation EPR : <%=prifix + axisService.getName().getLocalPart() + "/"+ axisOperation.getName().getLocalPart()%>--%>
    <%
        }
    %></ul>
<%
            status = true;
            }
%>
</c:forEach>
<%
    if (errornessservice != null) {
        if (errornessservice.size() > 0) {
            request.setAttribute(Constants.IS_FAULTY, Constants.IS_FAULTY);
%>
<hr>

<h3 style="color:blue">Faulty Services</h3>
<%
    Enumeration faultyservices = errornessservice.keys();
    while (faultyservices.hasMoreElements()) {
        String faultyserviceName = (String) faultyservices.nextElement();
%>
<h3><a style="color:blue" href="services/ListFaultyServices?serviceName=<%=faultyserviceName%>">
    <%=faultyserviceName%></a></h3>
<%
            }
        }
        status = true;
    }
    if (!status) {
%> No services listed! Try hitting refresh. <%
    }
%>
<jsp:include page="/WEB-INF/include/footer.inc"/>
</body>
</html>
