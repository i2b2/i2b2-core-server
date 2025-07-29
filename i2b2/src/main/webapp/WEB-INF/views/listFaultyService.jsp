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

<%@ page session="false" %>
<%@ page import="org.apache.axis2.Constants,
                 org.apache.axis2.description.AxisOperation"%>
<%@ page import="org.apache.axis2.description.AxisService"%>
<%@ page import="java.util.Hashtable"%>
<%@ page import="java.util.Iterator"%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
  <head>
    <jsp:include page="/WEB-INF/include/httpbase.jsp"/>
    <title>List Single service</title>
    <link href="axis2-web/css/axis-style.css" rel="stylesheet" type="text/css">
  </head>
  <body>
  <jsp:include page="/WEB-INF/include/header.inc"/>
    <jsp:include page="/WEB-INF/include/link-footer.jsp"/>
  <%
        String prifix = request.getAttribute("frontendHostUrl") + (String)request.getAttribute(Constants.SERVICE_PATH) +"services/";
    %>
        <%
            String isFault = (String)request.getAttribute(Constants.IS_FAULTY);
            String servicName = request.getParameter("serviceName");
            if(Constants.IS_FAULTY.equals(isFault)){
                Hashtable errornessservices =(Hashtable)request.getAttribute(Constants.ERROR_SERVICE_MAP);
                %>
                    <h3>This Web axisService has deployment faults</h3><%
                     %><p style="color:red"><%=(String)errornessservices.get(servicName) %></p>
                <%

                    }else {

                    AxisService axisService =
                            (AxisService) request.getAttribute(Constants.SINGLE_SERVICE);
                    if(axisService!=null){
           Iterator opItr = axisService.getOperations();
            //operationsList = operations.values();
          String  serviceName = axisService.getName();
            %><h2><a style="color:blue" href="<%=prifix + axisService.getName()%>?wsdl"><%=serviceName%></a></h2>
            <p>
            <span style="color:blue">Service EPR :</span>
            <span style="color:black"><%=prifix + axisService.getName()%></span>
            </p>
           
           <h4>Service Description : <span style="color:blue"><%=axisService.getDocumentation()%></span></h4>
           <p>
           <span style="color:blue; font-style:italic">Service Status : <%=axisService.isActive()?"Active":"InActive"%></span>
           </p>
           <%
            if (opItr.hasNext()) {
                %><span style="font-style:italic">Available operations</span><%
            } else {
                %><span style="font-style:italic"> There are no Operations specified</span><%
            }
               opItr = axisService.getOperations();
           %><ul><%
            while (opItr.hasNext()) {
                AxisOperation axisOperation = (AxisOperation) opItr.next();
                %><li><%=axisOperation.getName().getLocalPart()%></li>
                <%
            }
           %></ul>
           <%
                    } else{
                           %>
                <h3 style="color:red">No services found in this location.</h3>
 <%
                    }

            }
        %>
<jsp:include page="/WEB-INF/include/footer.inc"/>
        </body>
</html>
