<%@ page import="com.google.appengine.api.datastore.*" %>
<%@ page import="com.google.appengine.api.users.*" %>
<%@ page import="ch.plus8.hikr.gappserver.admin.*" %>
<%@ page import="ch.plus8.hikr.gappserver.dropbox.*" %>
<%@ page import="ch.plus8.hikr.gappserver.repository.*" %>

<jsp:include page="defaultHeader.jsp" />

<%
	DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
	UserService userService = UserServiceFactory.getUserService();
%>

<script type="text/x-handlebars" data-template-name="datastores">
<h2>Storages</h2>
{{#view App.DatastoreSingleView}}
  <div><a href="#" {{action "createNewDropbox"}}>Create new dropbox</a></div>
  <div><a href="#" {{action "createNewGoogle"}}>Create new google datastore</a></div>
{{/view}}

<table>
  <tbody>
     {{#each view.content}}
       {{#view App.DatastoreSingleView tagName="tr" contentBinding="this" }}         
           <td>b{{kind}}</td><td>{{uid}}</td><td>{{title}}</td><td>[<a href="#" {{action "edit"}}>edit</a>]</td>
       {{/view}}
     {{/each}}
  </tbody>	
</table>
</script>



<script type="text/x-handlebars" data-template-name="galleries">
<h2>Galleries</h2>
<div class="cnt" style="position:relative;height:250px;">
<div class="cnt p8-scroll" >
{{#view App.GallerySingleView }}
<div><a href="#" {{action "newDropboxGallery" }}>Create new dropbox gallery</a></div>
<div><a href="#" {{action "newGDriveGallery" }}>Create new gdrive gallery</a></div>
{{/view}}
<table>
  <tbody>
  {{#each view.content }}
    {{#view App.GallerySingleView contentBinding="this"}}
      <tr>
        <td>{{title}}</td>
        <td>[<a href="#" {{action "edit"}}>edit</a>]</a></td>
        <td><a target="_blank" href="?{{userparam "u"}}&{{urlparam "cat" key}}">[show]</a></td>
        <td>[<a href="#" {{action "showFeeds"}}>showFeeds</a>]</a></td>
      </tr>
    {{/view}}
  {{/each}}
  </tbody>
</table>
</div>
</div>
</script>


<script type="text/x-handlebars" data-template-name="contentGroups">
<h2>Contentgroups</h2>
{{#view App.ContentGroupSingleView}}
<div><a href="#" {{action "createNewContentGroup"}}>Create new contentgroup</a></div>
{{/view}}
<table>
  <tbody>
     {{#each view.content}}
       {{#view App.ContentGroupSingleView contentBinding="this"}}
         <tr>
           <td>{{title}}</td><td>{{groupId}}</td><td>[<a href="#" {{action "edit"}}>edit</a>]</td>
         </tr>
       {{/view}}
     {{/each}}
  </tbody>	
</table>
</script>


<script type="text/x-handlebars" data-template-name="feedItems">
<h1>Galleries menus</h1>
{{#view contentBinding="App.feedItemsController" elementId="searchNavigation"}}
<ul>
<li><a href="#" {{action "showWelcomePage" target="App.navigationHandler"}}>{{nextOffset}} Home</a>&nbsp;&nbsp;</li>
<li>|&nbsp;&nbsp;<a href="#" {{action "goPrevious" target="App.feedItemsController"}}>{{previousOffset}} Prev</a></li>
<li>|&nbsp;&nbsp;<a href="#" {{action "goNext" target="App.feedItemsController"}}>{{nextOffset}} Next</a></li>
</ul>
{{/view}}

<div id="searchResult" class="cnt p8-scroll" style="top:80px;width:100%;height:530px;width:1250px;">
<table>
  <thead>
    <tr>
      <th>img2</th>
      <th>actions</th>
      <th>link</th>
      <th>imageLink</th>
      <th>publishedDate</th>
      <th>source</th>
      <th>status</th>
      <th>categories</th>
    </tr>
  </thead>
  <tbody>
    {{#each view.content}}
      {{#view App.FeedItemSingleView contentBinding="this" tagName="tr"}}
        <td><img height=40 width=40 {{urlparam "src" this.img2Link}} />&nbsp;</td>
        <td>{{this.link}}</td>
        <td>[<a href="#" {{action "reparse"}}>reparse</a>]</td>
        <td>{{this.imageLink}}</td>
        <td>{{this.publishedDate}}</td>
        <td>{{this.source}}</td>
        <td>{{this.status}}</td>
        <td>{{this.categories}}</td>
      {{/view}}
    {{/each}}
  </tbody>
</table>
</div>
</script>


<script type="text/x-handlebars" data-template-name="feedListPage">
<div style="float:left;" id="feedListPage">
ha
</div>
</script>


<script type="text/javascript">
	$(document).ready(function() {
	
		App.datastoresController.load();
		App.galleriesController.load();
		App.contentGroupsController.load();
	});
</script>

<!-- h1>photography stream admin</h1 -->

<%
	if (userService.isUserLoggedIn()) {
%>

<div id="mainNavigationContent">

</div>
<% } else { %>
<div><a href="<%= userService.createLoginURL(request.getRequestURI()) %>">login</a></div>
<% } %>


<jsp:include page="defaultFooter.jsp" />