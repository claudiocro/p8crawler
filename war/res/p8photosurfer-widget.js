/*
 *  p8 photosurfer-widget  0.2.0
 * 
 */


if(p8WidgetType == "demo-1") {
	$('.p8GalleryWidget').append(
		''+
		'<img class="p81998582214447" src="images/cat-3.jpg"/>'+
		'<img class="p81998582214447" src="images/cat-4.jpg"/>'+
		'<img class="p81998582214447" src="images/cat-1.jpg"/>'+
	    '<img class="p81998582214447" src="images/cat-2.jpg"/>'+
	    '');
	
	$('.p81998582214447').hide();
	$('#mainGallery').p8GalleryCreator({
		datas: $('.p81998582214447'),
		reload: true,
		nextSelector: $('.prevNextCont .next'),
		previousSelector: $('.prevNextCont .previous')
	});
}

else if(p8WidgetType == "default") {
	
	
	
	$("#"+p8ElementId).append(''+
		'<div style="margin-bottom: 10px;">'+
			'<div class="prevNextCont" style="position: relative;"><a href="#" class="previous">Previous</a>&nbsp;|&nbsp;<a href="#" class="next">Next</a><span class="page-count"></span></div>'+
		'</div>'+
		'<div class="p8GalleryCont"> </div>'+
		'<div style="clear:both;"></div>'+
		'<div class="p8GalleryLoadedItems"> </div>');
	
	
	var galleryKey = "#"+p8ElementId+' .p8GalleryCont';
	var gallerySel = $(galleryKey);
	var singleIndex = -1;
	var galleryIndexUpdater = function() {
		$(galleryKey).p8JsonGallery('moveToSingleIndex', singleIndex++);
	};
	
	
	
	var singleOpen = false;
	var coloboxOpened = function() {singleOpen = true;}
	var coloboxClosed = function() {singleOpen = false;}
	
	
	var updateNavigation = function() {
		$('.page-count').text("seite: "+(gallerySel.p8JsonGallery('getCurrentCount')));
	}

	var galleryJsonRequest = function() {
		var self = this;
		var reqParams = self.element.data("galleryRequestParam");
		
		var jp = {cat:reqParams.cat, 'page':reqParams.callCount++,nocache:"1"};
		if(reqParams.cursor != null)
			jp = {cat:reqParams.cat, 'cursor':reqParams.cursor, 'page': reqParams.callCount++, nocache:"1"};
						
		
		$.ajax({
			  dataType: 'jsonp',
			  data: jp,
			  jsonp: 'callback',
			  url: 'http://photo.plus8.ch/feed',
			  success: function (data) {
				  self._preProcessResponse();
					
					if(data != null || data.error == null) {
						reqParams.cursor = data.cursor;
										
						var heavyImage = new Image(); 
						for ( var i=0; i <data.response.length; i++ ){
							//heavyImage.src = data.response[i].img1Link;
							heavyImage.src = data.response[i].img2Link;
							$("#"+p8ElementId+' .p8GalleryLoadedItems').append(
								$('<a></a>').attr('href', data.response[i].imageLink).attr('rel', 'p8Gallery')
							);
						}
						
						$("#"+p8ElementId+' .p8GalleryLoadedItems a').colorbox({rel:'p8Gallery',open:false,maxWidth:'100%',maxHeight:'100%',onComplete:galleryIndexUpdater,onOpen:coloboxOpened,onClosed:coloboxClosed,photo:true});
						
						if(singleOpen && $.colorbox.element().length > 0) {
								$.colorbox.element().colorbox({rel:'p8Gallery',open:singleOpen,maxWidth:'100%',maxHeight:'100%',onComplete:galleryIndexUpdater,onOpen:coloboxOpened,onClosed:coloboxClosed,photo:true});
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

			  },
			});
	};


	
		//create feeditems
	var createFeedContent = function(feed) {
		return $('<div class="summary"></div>')
			.css({opacity:0})
			.html('<div class="summaryCont">'+
				  '<span class="feedTitle">'+feed.title+'</span> &copy;'+
				  '<a href="'+feed.link+'" target="_blank">'+feed.authorName+'@'+feed.source+'</a>'
				+'</div>');
	}
	for ( var i=0; i<12; ++i ){
		gallerySel.append($('<div></div>').p8FeedItem({contentFunc:createFeedContent}));
	}

	$('.coloboxItems a').live().colorbox({rel:'p8Gallery'});
	
	
	gallerySel
		.data("galleryRequestParam", {callCount:0,cursor:null,cat:null})
		.p8GalleryCreator({
			singleNextSelector: $('.singleNav .next'),
			singlePreviousSelector: $('.singleNav .previous'),
			singleClickSelector: '.article',
			singleCompareFunction: function(clickEl, el){ 	
				var description = $(clickEl).closest('.p8FeedItem').p8FeedItem('activeFeedItem');
				return description.img2Link == el.img2Link;
			},
			singleClickSelectorFunction: function(feed,index) { 
				if(feed.imageLink != null) {
					var newImg = $('<a></a>').attr('href', feed.imageLink).attr('rel', 'p8Gallery');
					$("#"+p8ElementId+' .p8GalleryLoadedItems a[href="'+feed.imageLink+'"]').colorbox({rel:'p8Gallery',open:true,maxWidth:'100%',maxHeight:'100%',onComplete:galleryIndexUpdater,onOpen:coloboxOpened,onClosed:coloboxClosed,photo:true});
				}
				singleIndex = index;
			},
			nextSelector: $('.prevNextCont .next'),
			previousSelector: $('.prevNextCont .previous'),
			feedItemsChangedFunction:function(){$("#"+p8ElementId).busy("hide");},
			feedLoaderFunction: function(p8Item,feed) {if(feed!=null){p8Item.p8FeedItem("load",feed.img2Link,feed);}else{p8Item.p8FeedItem("clean");}},
			singleNavigationShowHideFunction: function(e, s, t) {if(s){$(e).animate({opacity: 1},200);} else {$(e).animate({opacity: .2},200);}},
			navigationShowHideFunction: function(e, s) {if(s){$(e).animate({opacity: 1},200);} else {$(e).animate({opacity: .2},200);}},
			moveForwards:		updateNavigation,
			moveBackwards:		updateNavigation,
			//loadingFunction:	function(evt, p){$('.prefNavBusy').fadeTo(200, (p.loading) ? 1 : 0 );},
			//requestFunction: requestFunctiona
			requestFunction: galleryJsonRequest
		}).p8SimpleGrid({total:12,totalInRow:3});
	
	
	
	$(galleryKey+' .p8FeedItem .article')
	.hover(function() {
			$('.summary', this).stop().animate({opacity: .6, height:"30px"},{duration:250, easing:"easeInSine"});
		},function() {
			$('.summary', this).stop().animate({opacity: 0,height:"10px"},{duration:200});
		}
	);

	$("#"+p8ElementId).busy({hide:false});

	var reqParams = gallerySel.data('galleryRequestParam');
	reqParams.cat = p8Cat;
	
	gallerySel.data('galleryRequestParam',reqParams);
	gallerySel.p8JsonGallery('reload');
	
}