<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.google.appengine.api.*" %>
<%@ page import="com.google.appengine.api.blobstore.*" %>
<%@ page import="com.google.appengine.api.datastore.*" %>
<%@ page import="java.util.UUID" %>

<%@ page import="ch.plus8.hikr.gappserver.admin.*" %>
<%@ page import="ch.plus8.hikr.gappserver.repository.*" %>
<%

boolean store = "1".equals(request.getParameter("store"));
Entity entity = null;

DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

 
Key key = null;
if(!"".equals(request.getParameter("id")) && request.getParameter("id") != null) { 
	key= KeyFactory.createKey(UserUtils.getCurrentKeyFor(), GAEFeedRepository.USER_GALLERY_KIND, request.getParameter("id"));
}

try {
	entity = datastore.get(key);
} catch (EntityNotFoundException e) {
//	if(store)
//		entity = new Entity(key);
}

if(store) {
	entity.setUnindexedProperty("title", request.getParameter("title"));
	entity.setUnindexedProperty("desc", new Text(request.getParameter("desc")));
	datastore.put(entity);
}


%>
<jsp:include page="defaultHeader.jsp" />

<h1>Category: <%=entity.getProperty("title")%></h1>

<div>
  <form action="p8/category.jsp" method="post">
    id: <% if(entity != null){ %> <%=entity.getKey().getName() %><% } %>
    <br />
    ref: <% if(entity != null){ %> <%=entity.getProperty("ref") %><% } %>
    <br />
    kind: <% if(entity != null){ %> <%=entity.getProperty("kind") %><% } %>
    <br />
    key: <% if(entity != null && entity.getProperty("key") != null){ %> <%=entity.getProperty("key") %><% } %>
    <br />
    title: <input type="text" name="title" <% if(entity != null && entity.getProperty("title") != null){ %> value="<%=entity.getProperty("title") %>"<% } %> ></input>
    <br />
    desc: <textarea name="desc"><% if(entity != null && entity.getProperty("desc") != null){ %><%=((Text)entity.getProperty("desc")).getValue()%><% } %></textarea>
    <br />
    <input type="hidden" name="id" <% if(entity != null){ %> value="<%=entity.getKey().getName() %>"<% } %>></input>
    <input type="hidden" name="store" value="1"></input>
    <input type="submit"></input>
  </form>
</div>  
<jsp:include page="defaultFooter.jsp" />