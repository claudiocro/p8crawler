<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.google.appengine.api.users.User" %>
<%@ page import="com.google.appengine.api.users.UserService" %>
<%@ page import="com.google.appengine.api.users.UserServiceFactory" %>
<%@ page import="com.google.appengine.api.*" %>
<%@ page import="com.google.appengine.api.blobstore.*" %>
<%@ page import="com.google.appengine.api.datastore.*" %>
<%@ page import="com.google.appengine.api.images.*" %>

<%@ page import="ch.plus8.hikr.gappserver.repository.*" %>
<%@ page import="ch.plus8.hikr.gappserver.admin.*" %>

<%@ page import="com.google.appengine.api.datastore.Query.FilterOperator" %>
<%@ page import="com.google.appengine.api.datastore.Query.SortDirection" %>

<jsp:include page="defaultHeader.jsp" />
<div style="position:relative;height:100%;">
  <div class="cnt"><h1>Galleries menus</h1></div>
  <div id="searchResult" class="cnt p8-scroll" style="top:50px;">
<%  
  		
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();  
    Query query = new Query("cnt:simple");
    query.setAncestor(UserUtils.getCurrentKeyFor());
    
	query.addSort("sort", SortDirection.ASCENDING);
	
	if(request.getParameter("group") != null)
  		query.addFilter("group", FilterOperator.EQUAL, request.getParameter("group"));
	
    PreparedQuery pq = datastore.prepare(query);  
    int pageSize = 30;  
  
    FetchOptions fetchOptions = FetchOptions.Builder.withLimit(pageSize);  
    String startCursor = request.getParameter("cursor");  
  
    // If this servlet is passed a cursor parameter, let's use it  
    if (startCursor != null) {  
    	fetchOptions.startCursor(Cursor.fromWebSafeString(startCursor));  
	}  
  	
    QueryResultList<Entity> results = pq.asQueryResultList(fetchOptions);
    
%>

  <table>
  <thead>
    <tr>
      <th>image</th>
      <th>key</th>
      <th>group</th>
      <th>sort</th>
      <th>menu1_idx</th>
      <th>title</th>
    </tr>
  </thead>
  <tbody>
<%  
    for (Entity entity : results) {
     
%>  
    <tr>
      <td><img height=40 width=40 src="<%= entity.getProperty("image")%>" </img></td>
      <td><a href="p8/menu.jsp?id=<%= entity.getKey().getName()%>"><%= entity.getKey().getName()%></a></td>
      <td><a href="p8/menuList.jsp?group=<%= entity.getProperty("group")%>"><%= entity.getProperty("group")%></a></td>
      <td><%= entity.getProperty("sort")%></td>
      <td><%= entity.getProperty("menu1_idx")%></td>
      <td><%= entity.getProperty("title")%></td>
    </tr>
<%
	}
%>
  </tbody>
  </table>

  </div>
</div>
<jsp:include page="defaultFooter.jsp" />