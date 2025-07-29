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
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ page import="org.apache.axis2.Constants,
                 org.apache.axis2.description.AxisModule" %>
<%@ page import="org.apache.axis2.description.AxisOperation" %>
<%@ page import="org.apache.axis2.description.AxisService" %>
<%@ page import="java.util.Collection" %>
<%@ page import="java.util.Enumeration" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Hashtable" %>
<%@ page import="java.util.Iterator" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<jsp:include page="/WEB-INF/include/adminheader.jsp"/>

<h1>Available Services</h1>
<t:status/>
<% String prefix = request.getAttribute("frontendHostUrl") + (String)request.getAttribute(Constants.SERVICE_PATH) + "/";
%>
<%
    Hashtable errornessservice = (Hashtable) request.getSession().getAttribute(Constants.ERROR_SERVICE_MAP);
    boolean status = false;
%>
<c:forEach var="service" items="${requestScope.configContext.axisConfiguration.services.values()}">
<%
            AxisService axisService = (AxisService) pageContext.getAttribute("service");
            Iterator operations = axisService.getOperations();
            String serviceName = axisService.getName();
%><h2><a style="color:blue" href="<%=prefix + axisService.getName()%>?wsdl"><%=serviceName%></a></h2>
<%
    String serviceDescription = axisService.getDocumentation();
    if (serviceDescription == null || "".equals(serviceDescription)) {
        serviceDescription = "No description available for this service";
    }
%>
<p>Service Description : <%=serviceDescription%><br>
Service EPR : <%=prefix + axisService.getName()%><br>
Service Status : <%=axisService.isActive() ? "Active" : "InActive"%>
<form name="<%=serviceName%>" method="post" action="<c:url value="axis2-admin/deleteService"/>"><input type="hidden" name="serviceName" value="<%=serviceName%>"><input type="submit" value="Remove Service"></form></p>
<%
    Collection engagedModules = axisService.getEngagedModules();
    String moduleName;
    boolean modules_present = false;
    if (engagedModules.size() > 0) {
%>
<span style="font-style: italic">Engaged modules for the service</span>
<%
    for (Iterator iteratorm = engagedModules.iterator(); iteratorm.hasNext();) {
        AxisModule axisOperation = (AxisModule) iteratorm.next();
        moduleName = axisOperation.getName();
        if (!modules_present) {
            modules_present = true;
%>
<ul>
    <% }
    %><li><form method="post" action="<c:url value="axis2-admin/disengageModule"/>"><%=moduleName%> :: <input type="hidden" name="type" value="service"><input type="hidden" name="serviceName" value="<%=serviceName%>"><input type="hidden" name="module" value="<%=moduleName%>"><input type="submit" value="Disengage"></form></li>

    <%
        }
        if (modules_present) {%>
</ul>
<%
        }
    }
    if (operations.hasNext()) {
%>
<br>
<span style="font-style: italic">Available operations</span>
<%
} else {
%><span style="font-style: italic"> There are no Operations specified</span>
<%
    }
%>
<ul><%
    operations = axisService.getOperations();
    while (operations.hasNext()) {
        AxisOperation axisOperation = (AxisOperation) operations.next();
%><li><%=axisOperation.getName().getLocalPart()%></li>
    <%--                 <br>Operation EPR : <%=prifix + axisService.getName().getLocalPart() + "/"+ axisOperation.getName().getLocalPart()%>--%>
    <%
        engagedModules = axisOperation.getEngagedModules();
        if (engagedModules.size() > 0) {
    %>
    <li>
    
    <span style="font-style: italic">Engaged Modules for the Operation</span>
    <ul>
    <%
        for (Iterator iterator2 = engagedModules.iterator(); iterator2.hasNext();) {
            AxisModule moduleDecription = (AxisModule) iterator2.next();
            moduleName = moduleDecription.getName();
    %>
    <li>
    <form method="post" action="<c:url value="axis2-admin/disengageModule"/>"><%=moduleName%> :: <input type="hidden" name="type" value="operation"><input type="hidden" name="serviceName" value="<%=serviceName%>"><input type="hidden" name="operation" value="<%=axisOperation.getName().getLocalPart()%>"><input type="hidden" name="module" value="<%=moduleName%>"><input type="submit" value="Disengage"></form>
    </li>
    <%
    }
	%>
</ul></li><%
        }

    }
%>
</ul>
<%
            status = true;
%>
</c:forEach>
<%
    if (errornessservice != null) {
        if (errornessservice.size() > 0) {
            request.getSession().setAttribute(Constants.IS_FAULTY, Constants.IS_FAULTY);
%>
<h3 style="color:red">Faulty Services</h3>
<%
    Enumeration faultyservices = errornessservice.keys();
    while (faultyservices.hasMoreElements()) {
        String faultyserviceName = (String) faultyservices.nextElement();
%><h3><a styel="color:blue" href="services/ListFaultyServices?serviceName=<%=faultyserviceName%>">
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
<jsp:include page="/WEB-INF/include/adminfooter.jsp"/>
