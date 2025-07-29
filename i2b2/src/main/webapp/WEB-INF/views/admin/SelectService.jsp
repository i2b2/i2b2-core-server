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
<%@ page import="org.apache.axis2.Constants,
                 org.apache.axis2.description.AxisService,
                 java.util.Collection,
                 java.util.HashMap,
                 java.util.Iterator"%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<jsp:include page="/WEB-INF/include/adminheader.jsp"/>

  <%
      String buttonName="" ;
      String status = (String)request.getSession().getAttribute(Constants.SELECT_SERVICE_TYPE);
      String heading = "";
      String disc = "";
      if(status != null && status.equals("MODULE")) {
          buttonName = " View Operations";
          heading = "Select a service to view operation specific chains";
          disc = "Select an Axis service from the combo and click on the 'View Operations' button to view operation specific Chains.";
      } else if(status != null && status.equals("VIEW")){
          buttonName = " View ";
          heading = "Select a service to view service handlers";
          disc = "Select an Axis service from the combo and click on the 'View' button to view service handlers.";
      } else if (status != null && status.equals("SERVICE_PARAMETER")){
          buttonName = " Edit Parameters ";
          heading = "Select a Service to Edit Parameters";
          disc = "Select an Axis service from the combo and click on the 'Edit Parameters' button to edit parameters.";
      }
  %>
<h1><%=heading%></h1>
<p><%=disc%></p>
<form method="get" name="selectServiceForm" action="<c:url value="axis2-admin/${requestScope.action}"/>">
<table summary="main content table" border="0" style="width: 50%" cellspacing="1" cellpadding="1">
         <tr>
        <td style="width: 35%">Select a Service :</td><td style="width:65%">
            <select name="axisService">
                <c:forEach var="service" items="${requestScope.configContext.axisConfiguration.services.values()}">
                    <option value="${service.name}"><c:out value="${service.name}"/></option>
                </c:forEach>
             </select>
           </td>
        </tr>
	<tr><td colspan="2">&nbsp;</td></tr>
        <tr><td>&nbsp;</td>
             <td colspan="2" align="left">
                <input name="submit" type="submit" value="<%=buttonName%>" >
             </td>
         </tr>
      </table>
   </form>
<jsp:include page="/WEB-INF/include/adminfooter.jsp"/>
