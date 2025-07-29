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
<h1>Deactivate Service</h1>
<form method="post" name="serviceInActivate" action="<c:url value="axis2-admin/doDeactivateService"/>">
  <table summary="main content table" style="width: 100%"  border="0">
<tr>
  <td colspan="2" >
     <p>Only the services that are active are listed below. Note that although you can activate a service from this page, once system is restarted the service will be active again</p>
  </td>
  </tr>
  <tr>
<%
String html = "";
int count = 0;
%>
<c:forEach var="service" items="${requestScope.configContext.axisConfiguration.services.values()}">
<%
	AxisService axisServices = (AxisService) pageContext.getAttribute("service");
	if(axisServices.isActive()){
		count++;
		html += "<option value='" + axisServices.getName() + "'>";
		html += axisServices.getName() + "</option>";
	}
%>
</c:forEach>
<%
if (count > 0) {
%>

    <td style="width: 20%"> Select Service : </td>
    <td style="width: 80%">
       <select name="axisService" class="selectBoxes">
<%=html%>
		</select>
	</td>
  </tr>
  <tr>
    <td style="width: 20%">Deactivate Service </td>
    <td style="width: 80%"><input type="checkbox" name="turnoff">
    </td>
  </tr>
  <tr>
  <td>&nbsp;</td>
  <td>
    <input name="submit" type="submit" value=" Deactivate " >
   <input name="reset" type="reset" value=" Clear " >
  </td>
<%
} else {
	%>
	<td colspan="2">No active services present.</td>
	<%
}
%>
  </tr>

</table>
</form>
<jsp:include page="/WEB-INF/include/adminfooter.jsp"/>
