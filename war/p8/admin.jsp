<%@ page import="com.google.appengine.api.datastore.*" %>
<%@ page import="com.google.appengine.api.users.*" %>
<%@ page import="ch.plus8.hikr.gappserver.admin.*" %>
<%@ page import="ch.plus8.hikr.gappserver.dropbox.*" %>
<%@ page import="ch.plus8.hikr.gappserver.repository.*" %>

<jsp:include page="defaultHeader.jsp" />

<h1>photography stream admin</h1>

<%
	DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
	UserService userService = UserServiceFactory.getUserService();
	if (userService.isUserLoggedIn()) {
%>

<div style="float:left;">

<ul>
<li><a href="p8/menuList.jsp">edit galleries menu</a></li>
<li><a href="<%= userService.createLogoutURL(request.getRequestURI()) %>">logout</a></li>
</ul>


<h2>Storages</h2>
<table>
<tbody>
<%
	Query query = new Query(DropboxSyncher.DROPBOXUSER_KIND);
	query.setAncestor(UserUtils.getCurrentKeyFor());
	
	FetchOptions fetchOptions = FetchOptions.Builder.withLimit(20);
	fetchOptions.prefetchSize(20);
	PreparedQuery prepare = datastore.prepare(query);
	
	QueryResultList<Entity> resultList = prepare.asQueryResultList(fetchOptions);
	for(Entity entity : resultList) {
%>
<tr>
  <td>Dropbox</td><td><%=entity.getProperty("dropbboxUid")%></td><td>[add gallery]</td>
</tr>
<%
	}
%>
</tbody>
</table>

</div>
<div style="float:right;width:650px;">
<h2>galleries</h2>
<div class="cnt" style="position:relative;height:250px;">
<div class="cnt p8-scroll" >
<table>
<tbody>
<%
	Query query1 = new Query(GAEFeedRepository.USER_GALLERY_KIND);
	query1.setAncestor(UserUtils.getCurrentKeyFor());
	
	FetchOptions fetchOptions1 = FetchOptions.Builder.withLimit(100);
	fetchOptions1.prefetchSize(100);
	PreparedQuery prepare1 = datastore.prepare(query1);
	
	QueryResultList<Entity> resultList1 = prepare1.asQueryResultList(fetchOptions1);
	for(Entity entity : resultList1) {
%>

<tr>
  <td><%=entity.getProperty("kind")%></td>
  <td><%=entity.getProperty("title")%></td>
  <td><%=entity.getProperty("ref")%></td>
  <td><a href="p8/category.jsp?id=<%=entity.getKey().getName()%>">[edit]</a></td>
  <td><a target="_blank" href="?u=<%=UserUtils.getUserIdByKey(entity.getKey().getParent())%>&cat=<%=entity.getKey().getName()%>">[show]</a></td>
  <td><a href="p8/feedList.jsp?cat=<%=entity.getKey().getName()%>">[admin show]</a></td>
  <td>[reparse]</td>
</tr>
<%
	}
%>
<tbody>
</table>
</div>


</div>
</div>

<% } else { %>
<div><a href="<%= userService.createLoginURL(request.getRequestURI()) %>">login</a></div>
<% } %>

<jsp:include page="defaultFooter.jsp" />