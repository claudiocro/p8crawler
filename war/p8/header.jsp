<%@ page import="ch.plus8.hikr.gappserver.admin.*" %>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />

  <title>p8 photography stream</title>

<%   if("localhost".equals(request.getServerName())) {%>
  <base href="http://localhost:8888/" />
<% } else { %>
  <base href="http://photo.plus8.ch/" />
<% } %>

  <link href='http://fonts.googleapis.com/css?family=Droid+Sans:regular,bold&subset=latin' rel='stylesheet' type='text/css'	>
  <link href="res/jquery-ui.css" rel="stylesheet" type="text/css"/>
  <link href="res/style.css" rel="stylesheet" type="text/css"/>
  <link href="res/content1.css" rel="stylesheet" type="text/css"/>
  <link href="/p8/res/admin.css" rel="stylesheet" type="text/css"/>
  <link href="res/colorbox.css" rel="stylesheet" type="text/css"/>
  <link href="res/jquery.jscrollpane.css" rel="stylesheet" type="text/css"/>
	
  <script type="text/javascript" language="javascript" src="res/jquery-1.7.1.js"></script>
  <script type="text/javascript" language="javascript" src="res/jquery-ui.js"></script>
  <script type="text/javascript" language="javascript" src="res/jquery.easing.1.3.js"></script>
  <script type="text/javascript" language="javascript" src="res/jquery.busy.js"></script>
  <script type="text/javascript" language="javascript" src="res/jquery.jtruncate.js"></script>
  <script type="text/javascript" language="javascript" src="res/jquery.colorbox-min.js"></script>
  <script type="text/javascript" language="javascript" src="res/jquery.mousewheel.js"></script>
  <script type="text/javascript" language="javascript" src="res/jquery.jscrollpane.js"></script>
  <script type="text/javascript" language="javascript" src="res/jquery.il18n.js"></script>
  <script type="text/javascript" language="javascript" src="res/jquery.validate.js"></script>
  <script type="text/javascript" language="javascript" src="res/jquery.infoTip.js"></script>
  
  <script type="text/javascript" language="javascript" src="p8/res/handlebars-1.0.0.beta.6.js"></script>
  <script type="text/javascript" language="javascript" src="p8/res/ember-1.0.pre.js"></script>
  
  
  <script type="text/javascript" language="javascript" src="res/p8js.js"></script>
  <script type="text/javascript" language="javascript" src="res/p8photosurfer.js"></script>
  <script type="text/javascript" language="javascript" src="res/p8ui.js"></script>
  <script type="text/javascript" language="javascript" src="/p8/res/p8ember-ui.js"></script>
  <script type="text/javascript" language="javascript" src="/p8/res/admin.js"></script>
  
  <script type="text/javascript" language="javascript">
 	/*App.UserDto = Ember.Object.extend({
		email: null,
		id: null
	});
	*/
	App.User = App.DatastoreDto.create({ email:null, id: <% if(UserUtils.isUserLoggedIn()) { %>"<%=UserUtils.getUserIdByCurrent()%>"<% } else {%>null<% } %> });
	</script>
</head>