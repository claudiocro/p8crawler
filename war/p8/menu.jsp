<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.google.appengine.api.*" %>
<%@ page import="com.google.appengine.api.blobstore.*" %>
<%@ page import="com.google.appengine.api.datastore.*" %>
<%@ page import="java.util.UUID" %>

<%@ page import="ch.plus8.hikr.gappserver.admin.*" %>
<%

boolean store = "1".equals(request.getParameter("store"));
Entity entity = null;

DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

 
Key key = null;
if(!"".equals(request.getParameter("id")) && request.getParameter("id") != null) { 
	key= KeyFactory.createKey(UserUtils.getCurrentKeyFor(), "cnt:simple", request.getParameter("id"));
} else{
	key= KeyFactory.createKey(UserUtils.getCurrentKeyFor(), "cnt:simple", UUID.randomUUID().toString());
}


try {
	entity = datastore.get(key);
} catch (EntityNotFoundException e) {
	if(store)
		entity = new Entity(key);
}

if(store) {
	entity.setProperty("group", request.getParameter("group"));
	entity.setProperty("sort", Integer.valueOf(request.getParameter("sort")));
	entity.setUnindexedProperty("image", request.getParameter("image"));
	entity.setUnindexedProperty("menu1_idx", Integer.valueOf(request.getParameter("menu1_idx")));
	entity.setUnindexedProperty("title", request.getParameter("title"));
	entity.setUnindexedProperty("content", new Text(request.getParameter("content")));
	datastore.put(entity);
}

if(entity != null && entity.getProperty("content") instanceof String) {
	entity.setProperty("content", new Text((String)entity.getProperty("content")));
}

%>
<jsp:include page="defaultHeader.jsp" />

<% if(entity != null){ %><h1>Gallery menu: <%=entity.getProperty("title")%></h1><% } %>
<% if(entity == null){ %><h1>New gallery menu</h1><% } %>

<div>
  <form action="menu.jsp" method="post">
    title: <input type="text" name="title" <% if(entity != null){ %> value="<%=entity.getProperty("title") %>"<% } %> ></input>
    <br />
    group: <input type="text" name="group" <% if(entity != null){ %> value="<%=entity.getProperty("group") %>"<% } %> ></input>
    <br />
    image: <input type="text" name="image" <% if(entity != null){ %> value="<%=entity.getProperty("image") %>"<% } %> ></input>
    <br />
    sort: <input type="text" name="sort" <% if(entity != null){ %> value="<%=entity.getProperty("sort") %>"<% } %>></input>
    <br />
    menu1_idx: <input type="text" name="menu1_idx" <% if(entity != null){ %> value="<%=entity.getProperty("menu1_idx") %>"<% } %>></input>
    <br />
    content: <textarea name="content"><% if(entity != null){ %><%=((Text)entity.getProperty("content")).getValue()%><% } %></textarea>
    <br />
    <input type="hidden" name="id" <% if(entity != null){ %> value="<%=entity.getKey().getName() %>"<% } %>></input>
    <input type="hidden" name="store" value="1"></input>
    <input type="submit"></input>
  </form>
</div>  
<jsp:include page="defaultFooter.jsp" />