<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.google.appengine.api.users.User" %>
<%@ page import="com.google.appengine.api.users.UserService" %>
<%@ page import="com.google.appengine.api.users.UserServiceFactory" %>
<%@ page import="com.google.appengine.api.*" %>
<%@ page import="com.google.appengine.api.blobstore.*" %>
<%@ page import="com.google.appengine.api.datastore.*" %>
<%@ page import="com.google.appengine.api.images.*" %>

<%@ page import="ch.plus8.hikr.gappserver.*" %>
<%@ page import="ch.plus8.hikr.gappserver.repository.*" %>
<%@ page import="com.google.appengine.api.datastore.Query.FilterOperator" %>
<%@ page import="com.google.appengine.api.datastore.Query.SortDirection" %>

<jsp:include page="defaultHeader.jsp" />

<%  
  	String source = request.getParameter("source");
    ImagesService imagesService = ImagesServiceFactory.getImagesService();
    	
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();  
    Query query = new Query(GAEFeedRepository.FEED_ITEM_KIND);
    
    String additionalParams = "";
    
    String pcat = request.getParameter("cat");
    String psource = request.getParameter("source");
    if(!Util.isBlank(pcat)) {
    	query.addFilter("categories", FilterOperator.EQUAL, pcat);
    	additionalParams += "&cat="+pcat;
    } else if(!Util.isBlank(psource)) {
    	query.addFilter("source", FilterOperator.EQUAL, psource);
    	additionalParams += "&source="+psource;
    }
    
	
		
	query.addSort("publishedDate", SortDirection.DESCENDING);
    PreparedQuery pq = datastore.prepare(query);  
    int pageSize = 30;  
  
    FetchOptions fetchOptions = FetchOptions.Builder.withLimit(pageSize);  
    String soffset = request.getParameter("offset");
    int offset = 0;  
    if (soffset != null) {
    	offset = Integer.valueOf(soffset);
    	fetchOptions.offset(offset);    	
    }
      
  
    QueryResultList<Entity> results = pq.asQueryResultList(fetchOptions);
%>



<div style="position:relative;height:100%;">
<h1>Galleries menus</h1>
<div id="searchNavigation" style="">
<ul>
<li><a href="p8/feedList.jsp?offset=<%=offset-pageSize%><%=additionalParams%>">Prev</a>&nbsp;&nbsp;</li>
<li>|&nbsp;&nbsp;<a href="p8/feedList.jsp?offset=<%=offset+pageSize%><%=additionalParams%>">Next</a></li>
</ul>
</div>
<div id="searchResult" class="cnt p8-scroll" style="top:80px;">

  <table>
  <thead>
    <tr>
      <th>img2</th>
      <th>link</th>
      <th>imageLink</th>
      <th>publishedDate</th>
      <th>source</th>
      <th>status</th>
      <th>categories</th>
    </tr>
  </thead>
  <tbody>
<%  
    for (Entity entity : results) {  
    
%>  
    <tr>
      <td><img height=40 width=40 src="<%= entity.getProperty("img2Link")%>" </img></td>
      <td><%= entity.getProperty("link")%></td>
      <td><%= entity.getProperty("imageLink")%></td>
      <td><%= entity.getProperty("publishedDate")%></td>
      <td><%= entity.getProperty("source")%></td>
      <td><%= entity.getProperty("status")%></td>
      <td><%= entity.getProperty("categories")%></td>
      
    </tr>
<%
	}
%>
  </tbody>
  </table>

  </div>
  </div>
  </div>
  
 <jsp:include page="defaultFooter.jsp" />