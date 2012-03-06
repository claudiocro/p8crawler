<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.google.appengine.api.users.User" %>
<%@ page import="com.google.appengine.api.users.UserService" %>
<%@ page import="com.google.appengine.api.users.UserServiceFactory" %>

<html>
 <head>
<%
   UserService userService = UserServiceFactory.getUserService();
   if (userService.isUserLoggedIn()) {
%>
    <script type="text/javascript" src="sample/sample.nocache.js"></script>
    <script type="text/javascript">
      var info = { "email" : "<%= userService.getCurrentUser().getEmail() %>" };
      //parent.$.fn.colorbox.close()
    </script>
  </head>
  <body>
  <div><a href="<%= userService.createLogoutURL(request.getRequestURI()) %>">Log out</a></div>
<%
   } else {
%>
  </head>
  <body>
    <div style="width:300px;height:300px;"><a href="<%= userService.createLoginURL(request.getRequestURI()) %>">Log in</a></div>
<%
   }
%>
 </body>
</html>