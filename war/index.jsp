<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.google.appengine.api.*" %>
<%@ page import="com.google.appengine.api.blobstore.*" %>
<%@ page import="com.google.appengine.api.datastore.*" %>
<%@ page import="ch.plus8.hikr.gappserver.admin.*" %>
<%@ page import="ch.plus8.hikr.gappserver.repository.*" %>
<%@ page import="com.google.appengine.api.datastore.Query.FilterOperator" %>
<%@ page import="com.google.appengine.api.datastore.Query.SortDirection" %>
<!DOCTYPE html>
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />

  <title>p8 photography stream</title>

  <link href='http://fonts.googleapis.com/css?family=Droid+Sans:regular,bold&subset=latin' rel='stylesheet' type='text/css'	>
  <link href="res/jquery-ui.css" rel="stylesheet" type="text/css"/>
  <link href="res/style.css" rel="stylesheet" type="text/css"/>
  <link href="res/content1.css" rel="stylesheet" type="text/css"/>
  <link href="res/colorbox.css" rel="stylesheet" type="text/css"/>
  <!-- link href="res/jScrollPane.css" rel="stylesheet" type="text/css"/ -->
  <link href="res/jquery.jscrollpane.css" rel="stylesheet" type="text/css"/>
	
  <script type="text/javascript" language="javascript" src="res/jquery-1.7.1.js"></script>
  <script type="text/javascript" language="javascript" src="res/jquery-ui.js"></script>
  <script type="text/javascript" language="javascript" src="res/jquery.easing.1.3.js"></script>
  <script type="text/javascript" language="javascript" src="res/jquery.busy.js"></script>
  <script type="text/javascript" language="javascript" src="res/jquery.jtruncate.js"></script>
  <script type="text/javascript" language="javascript" src="res/jquery.colorbox-min.js"></script>
  <script type="text/javascript" language="javascript" src="res/jquery.mousewheel.js"></script>
  <script type="text/javascript" language="javascript" src="res/jquery.jscrollpane.js"></script>
  
  <script type="text/javascript" language="javascript" src="res/p8js.js"></script>
  <script type="text/javascript" language="javascript" src="res/p8photosurfer.js"></script>
  
 
  <script type="text/javascript" language="javascript">
  	$.ajaxSetup({ scriptCharset: "utf-8" , contentType: "application/json; charset=utf-8"});
  	
  	var currentContent = "null";
  	
<% if(request.getParameter("cat") != null) { %>
  	var pCat = "<%=request.getParameter("cat")%>";
  	pCat = decodeURI(pCat.replace("#",""));
<% } else { %>
	var pCat = null;

<% } %>  	
	$(document).ready(function() {
		
		var oldBusy = $.fn.busy;
		$(window).busy('defaults',{img: 'res/images/busy.gif'});
		
		
		$('#navigationCont .home').button().click(showMainNavigation);
		$('#navigationCont .show-gridded').button().click(showGridded);
		$('#navigationCont .next').button();
		$('#navigationCont .previous').button();
		
		$('.prefNavBusy').css('opacity',0);
		
		$('.likes');
		$('.likes')
			.css({opacity: .1})
			.hover(
				function() {
					$('.likes').animate({opacity: .8});
				},
				function() {
					$('.likes').animate({opacity: .05});
				}
			);
		
		
		//$('#galleryContent').busy({hide:false});
		
		$('#mainNavigationContent div').p8CreateAutoLoadFeedItem({
			showBusy: true
			});
		$('#mainNavigationContent').p8SimpleGrid();
		
		var datasa = [
		 			{img2Link:'res/content-img/cat-1.jpg'},{img2Link:'res/content-img/cat-2.jpg'},{img2Link:'res/content-img/cat-3.jpg'},{img2Link:'res/content-img/cat-4.jpg'},
		 			{img2Link:'res/content-img/cat-5.jpg'},{img2Link:'res/content-img/cat-6.jpg'},{img2Link:'res/content-img/cat-7.jpg'},{img2Link:'res/content-img/cat-8.jpg'},
		 			{img2Link:'res/content-img/cat-5.jpg'},{img2Link:'res/content-img/cat-6.jpg'},{img2Link:'res/content-img/cat-7.jpg'},{img2Link:'res/content-img/cat-8.jpg'},
		 			{img2Link:'res/content-img/cat-1.jpg'},{img2Link:'res/content-img/cat-2.jpg'},{img2Link:'res/content-img/cat-3.jpg'},{img2Link:'res/content-img/cat-4.jpg'}
		 		];
		var requestFunctiona = function() {
			var self = this;
			self._preProcessResponse();
			
			setTimeout(function() {
				self.allFeeds = self.allFeeds.concat(datasa);
				
				self.feedStreamEnd = true;		
				self._postProcessResponse();
			
			}, 3000);
		}
		
		
		//create feeditems
		var createFeedContent = function(feed) {
			return $('<div class="summary"></div>')
				.css({opacity:0})
				.html('<div class="summaryCont">'+
					  '<span class="feedTitle">'+feed.title+'</span> &copy;'+
					  '<a href="'+feed.link+'" target="_blank">'+feed.authorName+'@'+feed.source+'</a>'
					+'</div>');
		}
		for ( var i=0; i<8; ++i ){
			$("#mainGallery").append($('<div></div>').p8FeedItem({contentFunc:createFeedContent}));
		}
		
		$("#mainGallery")
			.data("galleryRequestParam", {callCount:0,cursor:null,cat:null})
			.p8GalleryCreator({
				singleNextSelector: $('.singleNav .next'),
				singlePreviousSelector: $('.singleNav .previous'),
				singleClickSelector: '.article',
				singleCompareFunction: function(clickEl, el){ 	
					var description = $(clickEl).closest('.p8FeedItem').p8FeedItem('activeFeedItem');
					return description.img2Link == el.img2Link;
				},
				singleClickSelectorFunction: function(feed) { 
					if( feed.imageLink != null)
						showBigContent(feed.imageLink, feed);
					
				},
					
				nextSelector:		$('.galleryNav .next'),
				previousSelector:	$('.galleryNav .previous'),
				feedItemsChangedFunction:function(){$('#galleryContent').busy("hide");},
				feedLoaderFunction: function(p8Item,feed) {if(feed!=null){p8Item.p8FeedItem("load",feed.img2Link,feed);}else{p8Item.p8FeedItem("clean");}},
				singleNavigationShowHideFunction: function(e, s, t) {if(s){$(e).animate({opacity: 1},200);} else {$(e).animate({opacity: .2},200);}},
				navigationShowHideFunction: function(e, s) {if(s){$(e).animate({opacity: 1},200);} else {$(e).animate({opacity: .2},200);}},
				moveForwards:		updateNavigation,
				moveBackwards:		updateNavigation,
				loadingFunction:	function(evt, p){$('.prefNavBusy').fadeTo(200, (p.loading) ? 1 : 0 );},
				//requestFunction: requestFunctiona
				requestFunction: galleryJsonRequest
			}).p8SimpleGrid();
		
		$('#galleryContent')
			.append($('#mainNavigationContent'))
			.append(
				$('<div id="mainBigImage"></div>')
					.p8ImageCont()
					.click(showGridded)
				);
		
		
		$('#mainGallery .p8FeedItem .article')
		.hover(function() {
				$('.summary', this).stop().animate({opacity: .6, height:"30px"},{duration:250, easing:"easeInSine"});
			},function() {
				$('.summary', this).stop().animate({opacity: 0,height:"10px"},{duration:200});
			}
		);
	
		
		$('#mainNavigationContent .p8FeedItem .nav-title')
			.css({opacity: .5})
			.click(function() {
				var item = $(this).closest('.p8FeedItem');
				$('.nav-title', item).stop().animate({opacity: 0});
				$('.nav-cnt', item).stop().animate({opacity: .8});
			});
			
		$('#mainNavigationContent .p8FeedItem .nav-cnt').css({opacity: 0});
		$('#mainNavigationContent .p8FeedItem')
		.hover(function() {
				var item = $(this).closest('.p8FeedItem');
				$('.nav-title', item).stop().animate({opacity: 0},{duration:50});
				$('.nav-cnt', item).stop().animate({opacity: .8},{duration:450});
			}, function() {
				var item = $(this).closest('.p8FeedItem');
				$('.nav-title', item).stop().animate({opacity: .5});
				$('.nav-cnt', item).stop().animate({opacity: 0});
			}
		);
		

		// Log all jQuery AJAX requests to Google Analytics
	    $(document).ajaxSend(function(event, xhr, settings){ 
	      if (typeof _gaq !== "undefined" && _gaq !== null) {
	        _gaq.push(['_trackPageview', settings.url]);
	      }
	    });
		
	  	//Request first feed
	  	if(pCat != null) {
	  		showGridded();
	  		
	  		var mainGallery = $('#mainGallery');
	  		var reqParams = mainGallery.data('galleryRequestParam');
	  		reqParams.cat = pCat;
	  		
	  		mainGallery.data('galleryRequestParam',reqParams);
	  		mainGallery.p8JsonGallery('reload');
		} else {
			showMainNavigation();
		}
	  
		
	  	//keyboard navigation

	    /*$('body').keyup(function(event) {
	        if (event.keyCode == 39) {
	        	if(!$('#navigationCont .next').button( "option", "disabled" ))
	        		moveForwards();
	        } else if (event.keyCode == 37) {
	        	if(!$('#navigationCont .previous').button( "option", "disabled" ))
	        		moveBackwards();
	        	
	        } else if(event.keyCode == 27 && currentContent == "bigContent") {
	        	showGridded();
	        }
	    });*/


	  	
	    $('body').focus();
	    $(".scroll-cnt").jScrollPane();
	    
	    //$('.remove-user').colorbox();
	});
	
	
	var updateNavigation = function() {
		if(currentContent != 'gallery') {
			$('.page-count').text('');
			if(currentContent == 'bigContent')
				$('#navigationCont .singleNav').show();
			else{
				$('#navigationCont .singleNav').hide();
			}
			
			$('#navigationCont .galleryNav').hide();
			
		} 
		else {
			$('.page-count').text("page: "+($('#mainGallery').p8JsonGallery('getCurrentCount')));
			$('#navigationCont .singleNav').hide();
			$('#navigationCont .galleryNav').show();
		}
	}

	
	var loadGallery = function(type, album, sort) {
		if(type == 'cat') {
			pCat = album;
			
			var sortDir = "storeDate";
			if(sort != undefined)
				sortDir = "publishedDate";

		  	$('#mainNavigationContent').css({'z-index':1}).animate({opacity:0},900);
		  	$('#galleryContent').busy({hide:false});
		  	
			var gSel = $('#mainGallery');
	  		var reqParams = gSel.data('galleryRequestParam');
	  		reqParams.cat = pCat;
	  		reqParams.callCount= 0;
	  		reqParams.cursor=null;
	  		reqParams.sort = sortDir;
	  		
	  		gSel.data('galleryRequestParam',reqParams);
			gSel.p8JsonGallery('reload');
			
			showGridded();
		}
		
		
	}
	
	var showGridded = function() {
		currentContent = "gallery";
		//$('#galleryContent').busy("hide");
		$('#mainNavigationContent').css({'z-index':1}).animate({opacity:0},900);
		$('#mainGallery').animate({opacity:1},900);
		$('.p8ImageCont').p8ImageCont('unload');
		updateNavigation();
		
	}
	
	var showMainNavigation = function() {
		
		$('#galleryContent').busy("hide");
		if(currentContent == "gallery")
			$('#mainGallery').animate({opacity:0},900);
		else if(currentContent == "bigContent")
			$('.p8ImageCont').p8ImageCont('unload');
		
		
		$('#mainGallery .p8FeedItem').p8FeedItem('clean');
		$('#mainNavigationContent').css({'z-index':100}).animate({opacity:1},900);
		currentContent = "navigation";

		updateNavigation();
	}
	
	var showBigContent = function(image, feed) {
		currentContent = "bigContent";
		
		updateNavigation();
		
		$('#mainGallery').animate({opacity:.0},400);
		$('#mainNavigationContent').css({'z-index':1}).animate({opacity:0},900);
		
		var imgDesc = '<span class="feedTitle">'+feed.title+'</span> &copy;<a href="'+feed.link+'" target="_blank">'+feed.authorName+'@'+feed.source+'</a>';
		$('.p8ImageCont').p8ImageCont('load',image, imgDesc);
	}
	
	
	
	var galleryJsonRequest = function() {
		var self = this;
		var reqParams = self.element.data("galleryRequestParam");
		
		var jp = {cat:reqParams.cat, 'page':reqParams.callCount++, sort:reqParams.sort};
		//if(reqParams.cursor != null)
		//	jp = {cat:reqParams.cat, 'cursor':reqParams.cursor, 'page': jp.page, sort:reqParams.sort};
						
		jQuery.getJSON("feed", jp, function(data){
			self._preProcessResponse();
			
			if(data != null || data.error == null) {
				reqParams.cursor = data.cursor;
								
				for ( var i=0; i <data.response.length; i++ ){
					var heavyImage = new Image();
					//heavyImage.src = data.response[i].img1Link;
					heavyImage.src = data.response[i].img2Link;
				}
								
				if(data.response.length > 0)
					self.allFeeds = self.allFeeds.concat(data.response);
				else
					self.feedStreamEnd = true;
					
								
			} else {
				reqParams.cursor = null;
				self.isRetrivingFeed = false;
			}
			
			self.element.data("galleryRequestParam", reqParams);
			
			self._postProcessResponse();
		});
	};
	
	
	var login = function() {
		$.colorbox({href:"login.jsp",iframe:true,width:800,height:600});
	}
	
  </script>
  
  <script type="text/javascript">
  //document.addEventListener('touchmove', function (e) { e.preventDefault(); }, false);
  </script>
  
  
  <script type="text/javascript">
  var _gaq = _gaq || [];
  _gaq.push(['_setAccount', 'UA-26764399-1']);
  _gaq.push(['_trackPageview']);

  (function() {
    var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;
    ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';
    var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga, s);
  })();
 </script>
 
 <!-- Dieses Tag in den Head-Bereich oder direkt vor dem schließenden Body-Tag einfügen -->
 <!-- script type="text/javascript" src="https://apis.google.com/js/plusone.js"></script-->
 
 
<!-- style type='text/css'>@import url('http://getbarometer.s3.amazonaws.com/assets/barometer/css/barometer.css');</style>
<script src='http://getbarometer.s3.amazonaws.com/assets/barometer/javascripts/barometer.js' type='text/javascript'></script>
<script type="text/javascript" charset="utf-8">
  BAROMETER.load('Q5sbeK38yrhHD2bwzkG6c');
</script -->
 
</head>

<body>
<!-- div id="fb-root"></div>
<script>(function(d, s, id) {
  var js, fjs = d.getElementsByTagName(s)[0];
  if (d.getElementById(id)) {return;}
  js = d.createElement(s); js.id = id;
  js.src = "//connect.facebook.net/de_DE/all.js#xfbml=1";
  fjs.parentNode.insertBefore(js, fjs);
}(document, 'script', 'facebook-jssdk'));</script -->

<div id="wrapper">
  <h1 style="display:none">Photostream gallery for g+</h1>
  <div id="navigationCont">
    <div class="prevNextCont" style="position:relative;">
      <a href="#" class="home" >Home</a>&nbsp;&nbsp;&nbsp;
      <span class="galleryNav">
        <a href="#" class="previous">Previous</a>&nbsp;|&nbsp;
  	    <a href="#" class="next">Next</a>
  	  </span>
  	  <span class="singleNav">
  	    <a href="#" class="show-gridded">Gallery</a>&nbsp;|&nbsp;
  	    <a href="#" class="previous">Previous</a>&nbsp;|&nbsp;
  	    <a href="#" class="next">Next</a>
  	  </span>
  	  <span class="prefNavBusy"><img src="res/images/busy.gif" valign="top" width=22 height=22 /></span>
  	  <span class="page-count"></span>
    </div>
  </div>
  
  
  
  <div id="galleryContent">
  	<div id="mainGallery"></div>
    <div id="mainNavigationContent" style="opacity: 0;">
<%

	String u = request.getParameter("u");
	if(u == null)
		 u = "FD88335391E8948A2CC50CE79D7E0CFE";
		 

	String ctx = request.getParameter("ctx");
	if(ctx == null)
		 ctx = "photography-stream";

	DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();  
    Query query = new Query("cnt:simple");
    query.setAncestor(UserUtils.getUserKeyById(u));
    query.addFilter("group", FilterOperator.EQUAL, ctx);
    query.addSort("sort", SortDirection.ASCENDING);
    
    PreparedQuery pq = datastore.prepare(query);  
    int pageSize = 30;  
  
    FetchOptions fetchOptions = FetchOptions.Builder.withLimit(pageSize);  
    String startCursor = request.getParameter("cursor");  
  
    // If this servlet is passed a cursor parameter, let's use it  
    if (startCursor != null) {  
    	fetchOptions.startCursor(Cursor.fromWebSafeString(startCursor));  
	}  
  
    QueryResultList<Entity> results = pq.asQueryResultList(fetchOptions);
    
    for (Entity entity : results) {  
   
%>
<div class="cat cat<%=entity.getProperty("menu1_idx")%>" id="nav-<%=entity.getKey().getName()%>"style="background-image:url(<%=entity.getProperty("image")%>)"><div>
<%
if(!"".equals(entity.getProperty("title")) && entity.getProperty("title") != null)
	out.println("<h2 class=\"nav-title\">"+entity.getProperty("title")+"</h2>");
if(!"".equals(entity.getProperty("content")) && entity.getProperty("content") != null && !"".equals(((Text)entity.getProperty("content")).getValue()))
	out.println("<div class=\"nav-cnt\"><div class=\"scroll-cnt\">"+((Text)entity.getProperty("content")).getValue()+"</div></div>");
%>
</div></div>
<%
	}
%>
  	
    </div>
  
  </div>
  
  <div id="footer" style="">
    <div class="footerCont" style="position:relative; top:5px;height: 20px">
      <span style="">powered by <a href="https://plus.google.com/113880730306243229744/" target="_blank">Photography Stream</a></span>
      <!-- span class="likes" style="width: 100%;  text-align: right; position:absolute; right:1px; bottom:0;">
      	<a href="https://twitter.com/share" class="twitter-share-button" data-count="none" data-via="plus8gmbh">Tweet</a><script type="text/javascript" src="//platform.twitter.com/widgets.js"></script>
        <div class="fb-like" style="vertical-align: top;width:75px;" data-href="photo.plus8.ch" data-send="false" data-layout="button_count" data-width="75" data-show-faces="false"></div>
        <!-- g:plusone size="medium" count="false" href="photo.plus8.ch"></g:plusone -->
      </span-->
    </div>
  </div>
  
  
 </div>
 <div id="workerCont"  style="display:none;">
 </div>
</body>
</html>
