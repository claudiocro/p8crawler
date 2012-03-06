<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.google.appengine.api.users.User" %>
<%@ page import="com.google.appengine.api.users.UserService" %>
<%@ page import="com.google.appengine.api.users.UserServiceFactory" %>
<%@ page import="com.google.appengine.api.*" %>
<%@ page import="com.google.appengine.api.blobstore.*" %>
<%@ page import="com.google.appengine.api.datastore.*" %>
<%@ page import="com.google.appengine.api.images.*" %>

<%@ page import="ch.plus8.hikr.gappserver.repository.*" %>
<%@ page import="com.google.appengine.api.datastore.Query.FilterOperator" %>
<%@ page import="com.google.appengine.api.datastore.Query.SortDirection" %>


<html>
 <head>
<%
//   UserService userService = UserServiceFactory.getUserService();
//   if (userService.isUserLoggedIn()) {
%>
  </head>
  <body>
  <div id="searchResult">
<%  
  	String source = request.getParameter("source");
    ImagesService imagesService = ImagesServiceFactory.getImagesService();
    	
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();  
    Query query = new Query(GAEFeedRepository.FEED_ITEM_KIND);
    query.addFilter("source", FilterOperator.EQUAL, "gplus");
	query.addFilter("img1A", FilterOperator.EQUAL, 1);
	query.addFilter("img2A", FilterOperator.EQUAL, 1);
		
	query.addSort("publishedDate", SortDirection.DESCENDING);
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
      <th>img2</th>
      <th>link</th>
      <th>imageLink</th>
      <th>publishedDate</th>
      <th>source</th>
      <th>categories</th>
    </tr>
  </thead>
  <tbody>
<%  
    for (Entity entity : results) {  
    String img2 = imagesService.getServingUrl((BlobKey)entity.getProperty("img2"));
%>  
    <tr>
      <td><img height=40 width=40 src="<%= img2%>" </img></td>
      <td><%= entity.getProperty("link")%></td>
      <td><%= entity.getProperty("imageLink")%></td>
      <td><%= entity.getProperty("publishedDate")%></td>
      <td><%= entity.getProperty("source")%></td>
      <td><%= entity.getProperty("categories")%></td>
    </tr>
<%
	}
%>
  </tbody>
  </table>

  </div>
</html>