<!DOCTYPE html>
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />

  <title>p8 photostream for g+</title>

  <!-- link href='http://fonts.googleapis.com/css?family=Droid+Sans:regular,bold&subset=latin' rel='stylesheet' type='text/css' -->
  <link href="res/jquery-ui.css" rel="stylesheet" type="text/css"/>
  <link href="res/style.css" rel="stylesheet" type="text/css"/>
  <link href="res/content.css" rel="stylesheet" type="text/css"/>
	
  <script type="text/javascript" language="javascript" src="res/jquery.js"></script>
  <script type="text/javascript" language="javascript" src="res/jquery-ui.js"></script>
  <script type="text/javascript" language="javascript" src="res/jquery.easing.1.3.js"></script>
  <script type="text/javascript" language="javascript" src="res/jquery.busy.js"></script>
  <script type="text/javascript" language="javascript" src="res/jquery.jtruncate.js"></script>
  <script type="text/javascript" language="javascript" src="res/p8photosurfer.js"></script>
  
  
  
  <script type="text/javascript" language="javascript">
  	var feedChunks = 40;
  	var maxCount = 100;
    var callCount = 0;
  	var cursor = null;
  	var allFeeds = new Array();
  	var currentCount = 0;
  	var total = 8;
  	var isRetrivingFeed = false;
  	var isBigContent = false;
  	var feedStreamEnd = false;
  	
  	var pSource = $.getUrlVar('source');
  	if(pSource != null)
  		pSource = pSource.replace("#","");
  	
  	var pCat = $.getUrlVar('cat');
  	if(pCat != null)
  		pCat = pCat.replace("#","");
  	
  	$.ajaxSetup({ scriptCharset: "utf-8" , contentType: "application/json; charset=utf-8"});
  	
  	
  	
  	
  	var loadNextFeedResults = function() {
  		moveForwards();  		
  	}
  	
	var moveForwards = function() {
		
		//handle navigation busy indicator
		if(feedStreamEnd != true && currentCount<maxCount && allFeeds.length < total*(currentCount+4))
			$('.prefNavBusy').busy({img:'res/images/busy.gif', hide:false, width:'20px',height:'20px'});
  		else if (currentCount>=maxCount)
  			$('.prefNavBusy').busy('hide');
  		
		//preload feed if necessary
  		if(feedStreamEnd != true && allFeeds.length-(total*5) < currentCount*total && !isRetrivingFeed) {
			isRetrivingFeed = true;
			
			jQuery.getJSON("feed", {source:pSource,cat:pCat,'cursor': cursor,'page': callCount++}, function(data){
				isRetrivingFeed = false;
				$('.prefNavBusy').busy('hide');
				processResponse(data);
				updateNavigation();
			});
		}
		
		//load next available feedItems
		var p8Items = $('.p8FeedItem');
		if(currentCount >= maxCount || (total*(currentCount+1)) >= allFeeds.length) {
		}else{
			for ( var i=total*currentCount, len=allFeeds.length; i<total*(currentCount+1) && i<len; ++i ){
				$(p8Items.get(i-total*currentCount)).p8FeedItem("load",allFeeds[i]);
			}
			currentCount++;
		}
		
		updateNavigation();
	}
	
	var moveBackwards = function() {
		var p8Items = $('.p8FeedItem');
		if(currentCount >=1) {
			currentCount--;
			currentCount--;
			for ( var i=total*currentCount, len=allFeeds.length; i<total*(currentCount+1) && i<len && i>-1; ++i ){
				$(p8Items.get(i-total*currentCount)).p8FeedItem("load",allFeeds[i]);
			}
			currentCount++;
		}
		
		updateNavigation();
	}
  	
  	
  	
	$(document).ready(function() {
		
		$('#navigationCont .next').button({'disabled':true});
		$('#navigationCont .previous').button({'disabled':true});
		
		
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
		
		
		$('#galleryContent').busy({img:'res/images/busy.gif', hide:false});
		
		
		//create feeditems
		var createFeedContent = function(feed) {
			return $('<summary style="opacity:0;"><summary>').html('<div class="summaryCont"><span class="feedTitle">'+feed.title+'</span> &copy;<a href="'+feed.link+'" target="_blank">'+feed.authorName+'@'+feed.source+'</a></div>');
		}
		for ( var i=0; i<total; ++i ){
			$("#workerCont").append($('<div></div>').p8FeedItem({contentFunc:createFeedContent}));
		}
		
		$('.p8FeedItem article')
			.hover(function() {
					$('summary', this)
						.stop()
						.animate({opacity: .6, height:"30px"},{duration:250, easing:"easeInSine"});
				},function() {
					$('summary', this)
						.stop()
						.animate({opacity: 0,height:"10px"},{duration:200});
				}
			)
			.click(function(event) {
				var feed = $(this).closest('.p8FeedItem').p8FeedItem('option','feed');
				if( feed != null)
					showBigContent(feed);
			});
		
		$('#galleryContent')
			.empty()
			.append($('<div></div>').p8SimpleGrid({contentSelector:$('.p8FeedItem')}))
			.append(
				$('<div></div>')
					.p8ImageCont()
					.click(showGridded)
				);
		
		
		// Log all jQuery AJAX requests to Google Analytics
	    $(document).ajaxSend(function(event, xhr, settings){ 
	      if (typeof _gaq !== "undefined" && _gaq !== null) {
	        _gaq.push(['_trackPageview', settings.url]);
	      }
	    });
		
	  //Request first feed
		jQuery.getJSON("feed", {source: pSource,cat:pCat,'page': callCount++}, function FeedRequestCallback(data) {
			processResponse(data);
			
			$('#galleryContent').busy("hide");
			moveForwards();
		});
	  
		
	  	//keyboard navigation
	    $('body').keyup(function(event) {
	        if (event.keyCode == 39) {
	        	if(feedStreamEnd == true || currentCount >= maxCount || (total*(currentCount+1)) >= allFeeds.length) {
	        	} else
	        		loadNextFeedResults();
	        } else if (event.keyCode == 37) {
	        	if(currentCount > 1)
	        		moveBackwards();
	        } else if(event.keyCode == 27 && isBigContent == true) {
	        	showGridded();
	        }
	    });
		
	    $('body').focus();
	});
	
	
	function processResponse(data) {
		
		if(data != null || data.error == null) {
			cursor = data.cursor;
			
			var heavyImage = new Image(); 
			for ( var i=0; i <data.response.length; i++ ){
				//heavyImage.src = data.response[i].img1Link;
				heavyImage.src = data.response[i].img2Link;
			}
			
			if(data.response.length > 0)
				allFeeds = allFeeds.concat(data.response);
			else
				feedStreamEnd = true;
			
			
		} else {
			cursor = null;
			isRetrivingFeed = null;
		}			
	}
	
	
	
	
	var updateNavigation = function() {
		$('.page-count').text("page: "+(currentCount));
		
		//the bind/unbind is a jquery bug workaround for not doing it when disabling/enabling
		if(currentCount >= maxCount || (total*(currentCount+1)) >= allFeeds.length) {
			$('#navigationCont .next')
				.button('disable')
				.unbind('click',loadNextFeedResults);
		}
		else {
			$('#navigationCont .next')
				.unbind('click',loadNextFeedResults)
				.button('enable')
				.bind('click', loadNextFeedResults);
		}
		
		if(currentCount <= 1) {
			$('#navigationCont .previous').button('disable').unbind('click',moveBackwards);
		} else {
			$('#navigationCont .previous')
				.unbind('click',moveBackwards)
				.button('enable')
				.bind('click', moveBackwards);
		}
	}

	
	
	var showGridded = function() {
		isBigContent = false;
		$('#galleryContent').busy("hide");
		$('.gridded-content').animate({opacity:1},900);
		$('.p8ImageCont').p8ImageCont('unload');
		updateNavigation();
		
	}
	
	var showBigContent = function(feed) {
		isBigContent = true;
		
		$('#navigationCont .next').button('disable').unbind('click');
		$('#navigationCont .previous').button('disable').unbind('click');
		
		$('.gridded-content').animate({opacity:.0},400);
		var imgDesc = '<span class="feedTitle">'+feed.title+'</span> &copy;<a href="'+feed.link+'" target="_blank">'+feed.authorName+'@'+feed.source+'</a>';
		$('.p8ImageCont').p8ImageCont('load',{imageLink:feed.imageLink,description:imgDesc});
	}
	
	
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
 
 
 <style type='text/css'>@import url('http://getbarometer.s3.amazonaws.com/assets/barometer/css/barometer.css');</style>
<script src='http://getbarometer.s3.amazonaws.com/assets/barometer/javascripts/barometer.js' type='text/javascript'></script>
<script type="text/javascript" charset="utf-8">
  BAROMETER.load('Q5sbeK38yrhHD2bwzkG6c');
</script>
 
</head>

<body>
<div id="fb-root"></div>
<script>(function(d, s, id) {
  var js, fjs = d.getElementsByTagName(s)[0];
  if (d.getElementById(id)) {return;}
  js = d.createElement(s); js.id = id;
  js.src = "//connect.facebook.net/de_DE/all.js#xfbml=1";
  fjs.parentNode.insertBefore(js, fjs);
}(document, 'script', 'facebook-jssdk'));</script>

<div id="wrapper">
  <h1 style="display:none">Photostream gallery for g+</h1>
  <div id="navigationCont">
    <div class="prevNextCont" style="position: relative;">
      <a href="#" class="previous" style="z-index:10">Previous</a>
  	  <a href="#" class="next" style="z-index:10">Next</a>
  	  <span class="prefNavBusy" style="">&nbsp</span>
  	  <span class="page-count"></span>
    </div>
  </div>
  <div id="galleryContent">
  </div>
  
  <div id="footer" style="">
    <div class="footerCont" style="position:relative; top:5px;height: 20px">
      <span style="">powered by <a href="https://plus.google.com/u/0/113880730306243229744/posts" target="_blank">social pictures</a></span>
      <span class="likes" style="width: 100%;  text-align: right; position:absolute; right:1px; bottom:0;">
      	<a href="https://twitter.com/share" class="twitter-share-button" data-count="none" data-via="plus8gmbh">Tweet</a><script type="text/javascript" src="//platform.twitter.com/widgets.js"></script>
        <div class="fb-like" style="vertical-align: top;width:75px;" data-href="photo.plus8.ch" data-send="false" data-layout="button_count" data-width="75" data-show-faces="false"></div>
        <!-- g:plusone size="medium" count="false" href="photo.plus8.ch"></g:plusone -->
      </span>
    </div>
  </div>
  
  
 </div>
 <!-- Dieses Tag dort einfügen, wo die +1-Schaltfläche dargestellt werden soll -->
  
 <div id="workerCont"  style="display:none;">
 </div>
</body>
</html>
