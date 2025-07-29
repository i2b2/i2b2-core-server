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

<%@ page import="org.apache.axis2.Constants,
                 org.apache.axis2.description.AxisOperation" %>
<%@ page import="org.apache.axis2.description.AxisService" %>
<%@ page import="java.util.Hashtable" %>
<%@ page import="java.util.Iterator" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<jsp:include page="/WEB-INF/include/adminheader.jsp"/>
<h1>List Single Service</h1>
<%
    String prefix = request.getAttribute("frontendHostUrl") + (String)request.getAttribute(Constants.SERVICE_PATH) + "/";
%>
<%
    String isFault = (String) request.getSession().getAttribute(Constants.IS_FAULTY);
    String servicName = request.getParameter("serviceName");
    if (Constants.IS_FAULTY.equals(isFault)) {
        Hashtable errornessservices = (Hashtable) request.getSession().getAttribute(Constants.ERROR_SERVICE_MAP);
%>
<h3>This Axis service has deployment faults.</h3>
<p style="color:red"><%=(String) errornessservices.get(servicName) %></p>
<%

} else {

    AxisService axisService =
            (AxisService) request.getSession().getAttribute(Constants.SINGLE_SERVICE);
    if (axisService != null) {
        Iterator opItr = axisService.getOperations();
        //operationsList = operations.values();
        String serviceName = axisService.getName();
%>
<h2><a style="color:blue" href="<%=prefix + axisService.getName()%>?wsdl"><%=serviceName%></a></h2>
<p>
<span style="color:blue">Service EPR : </span>
<span style="color:black"><%=prefix + axisService.getName()%></span>
</p>
<%
    String serviceDescription = axisService.getDocumentation();
    if (serviceDescription == null || "".equals(serviceDescription)) {
        serviceDescription = "No description available for this service";
    }
%>
<h4>Service Description : <span style="color:black"><%=serviceDescription%></span></h4>
<p style="color:blue; font-style:italic">
Service Status : <%=axisService.isActive() ? "Active" : "InActive"%>
</p>
<%
    if (opItr.hasNext()) {
%><i>Available operations</i><%
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
} else {
%>
<h3 style="color:red">No services found in this location</h3>
<%
        }

    }
%>
<jsp:include page="/WEB-INF/include/adminfooter.jsp"/>
