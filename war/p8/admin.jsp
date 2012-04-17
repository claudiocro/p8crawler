<%@ page import="com.google.appengine.api.users.*" %>
<%@ page import="ch.plus8.hikr.gappserver.admin.*" %>

<jsp:include page="defaultHeader.jsp" />

<h1>photography stream admin</h1>

<%
   UserService userService = UserServiceFactory.getUserService();
   if (userService.isUserLoggedIn()) {
%>

<ul>
<li><a href="p8/menuList.jsp">edit galleries menu</a></li>
<li><a href="<%= userService.createLogoutURL(request.getRequestURI()) %>">logout</a></li>
</ul>

<% } else { %>
<div><a href="<%= userService.createLoginURL(request.getRequestURI()) %>">login</a></div>
<% } %>

<jsp:include page="defaultFooter.jsp" />