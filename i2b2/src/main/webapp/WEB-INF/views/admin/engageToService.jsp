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
<%@ page contentType="text/html; charset=iso-8859-1" language="java" import="org.apache.axis2.Constants,
                                                                             org.apache.axis2.description.AxisModule,
                                                                             org.apache.axis2.description.AxisService,
                                                                             java.util.Collection,
                                                                             java.util.HashMap,
                                                                             java.util.Iterator"
         %>
<%@ page import="org.apache.axis2.util.Utils" %>
<jsp:include page="/WEB-INF/include/adminheader.jsp"/>
<h1>Engage Module for a Service</h1>

<p>To engage a module for a axis service,</p>

    <ol>
        <li>select the module you want to engage</li>
        <li>select the axis service you like the module to be engaged.</li>
        <li>click "Engage".</li>
    </ol>

<form method="post" name="selectModuleForm" action="<c:url value="axis2-admin/doEngageToService"/>">
    <table summary="main content table" border="0" width="100%" cellspacing="1" cellpadding="1">
        <tr>
            <td>
                &nbsp;
                &nbsp;
            </td>
        </tr>
        <tr>
            <td>Select a Module :</td>
        </tr>
        <tr>
            <td>
                <select name="module">
                    <%

                        HashMap moduels = (HashMap)request.getSession().getAttribute(Constants.MODULE_MAP);
                        request.getSession().setAttribute(Constants.MODULE_MAP,null);
                        Collection moduleCol =  moduels.values();
                        for (Iterator iterator = moduleCol.iterator(); iterator.hasNext();) {
                            AxisModule axisOperation = (AxisModule) iterator.next();
                            String modulename = axisOperation.getName();

                    %>
                    <option value="<%=modulename%>"><%=modulename%>
                    </option>
                    <%

                       }

                    %>
                 </select>
            </td>
        </tr>
        <tr>
            <td>
                &nbsp;
                &nbsp;
            </td>
        </tr>
        <tr>
            <td>Select a Service :</td>
        </tr>
        <tr>
            <td>
                <select name="axisService">
                    <c:forEach var="service" items="${requestScope.configContext.axisConfiguration.services.values()}">
                        <option value="${service.name}"><c:out value="${service.name}"/></option>
                    </c:forEach>
                 </select>
            </td>
        </tr>
        <tr>
            <td>
                &nbsp;
                &nbsp;
            </td>
        </tr>
        <tr>
            <td>
                <input name="submit" type="submit" value=" Engage ">
            </td>
        </tr>
        <tr>
            <td>
                &nbsp;
                &nbsp;
            </td>
        </tr>
        <tr>
            <td>
                &nbsp;
                &nbsp;
            </td>
        </tr>
        <tr>
            <td>
                <t:status/>
            </td>
        </tr>
    </table>
</form>
<jsp:include page="/WEB-INF/include/adminfooter.jsp"/>
