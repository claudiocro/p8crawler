/*
 *  p8 photosurfer-widget  0.3.4.2
 * 
 */

var galleryKey = "#" + p8ElementId + ' .p8GalleryCont';
var gallerySel = null;
var singleIndex = -1;
var galleryIndexUpdater = function() {
	jQuery(galleryKey).p8JsonGallery('moveToSingleIndex', singleIndex++);
};

var singleOpen = false;
var isSingleOpen = function() {
	return singleOpen;
};
var coloboxOpened = function() {
	singleOpen = true;
};
var coloboxClosed = function() {
	singleOpen = false;
};

p8Options = jQuery.extend(
		{
			maxImageWidth : -1,
			maxImageHeight : -1,
			totalInRow : 4,
			maxImagesInPage : 12,
			horizontalGrid : false,
			createFeedContent : function(feed) {
				return jQuery('<div class="summary"></div>').css({opacity : 0}).html(
							'<div class="summaryCont">'+
							'<span class="feedTitle">' + feed.title + 
							'</span> &copy;' + '<a href="' + feed.link + 
							'" target="_blank">' + feed.authorName + 
							'</a></div>');
			},
			singleClickSelectorFunction : function(feed, index) {
				if (feed.imageLink !== null) {
					jQuery("#" + p8ElementId +' .p8GalleryLoadedItems a[href="' +feed.imageLink + '"]').colorbox({
						rel : 'p8Gallery',
						open : true,
						maxWidth : '100%',
						maxHeight : '100%',
						onComplete : galleryIndexUpdater,
						onOpen : coloboxOpened,
						onClosed : coloboxClosed,
						photo : true
					});
				}
				singleIndex = index;
			},
			appendForSingle : function(imageLink) {
				if (imageLink !== undefined) {
					return jQuery('<a></a>').attr('href', imageLink).attr(
							'rel', 'p8Gallery');
				}
			},
			openSingle : function(p8ElementId, isSingleOpen, singleOpened,
					singleClosed) {
				jQuery("#" + p8ElementId + ' .p8GalleryLoadedItems a')
						.colorbox({
							rel : 'p8Gallery',
							open : false,
							maxWidth : '100%',
							maxHeight : '100%',
							onComplete : galleryIndexUpdater,
							onOpen : singleOpened,
							onClosed : singleClosed,
							photo : true
						});

				if (isSingleOpen() && jQuery.colorbox.element().length > 0) { 
					jQuery.colorbox.element().colorbox({
						rel : 'p8Gallery',
						open : singleOpen,
						maxWidth : '100%',
						maxHeight : '100%',
						onComplete : galleryIndexUpdater,
						onOpen : singleOpened,
						onClosed : singleClosed,
						photo : true
					});
				}
			},
			postCreateGallery : function() {
				jQuery(galleryKey + ' .p8FeedItem .article').hover(function() {
					jQuery('.summary', this).stop().animate({
						opacity : 0.6,
						height : "30px"
					}, {
						duration : 250,
						easing : "easeInSine"
					});
				}, function() {
					jQuery('.summary', this).stop().animate({
						opacity : 0,
						height : "10px"
					}, {
						duration : 200
					});
				});
			}
		}, p8Options);





(function($) {
	$.picasa = {
		albums : function(user, album, callback) {
			var url = "http://picasaweb.google.com/data/feed/api/user/:user_id?alt=json&kind=album&hl=en_US&access=visible&fields=entry(id,media:group(media:content,media:description,media:keywords,media:title))";
			url = url.replace(/:user_id/, user);

			$.ajax({
				dataType : 'jsonp',
				jsonp : 'callback',
				url : url,
				success : function(data) {
					var image = null;
					var albums = [];
					$.each(data.feed.entry, function(i, element) {
						image = element["media$group"]["media$content"][0];
						image.id = element.id["$t"].split("?")[0].split("albumid/")[1];
						image.title = element["media$group"]["media$title"]["$t"];
						image.img2Link = element["media$group"]["media$content"][0]["url"];
						image.source = "picasa";
						albums.push(image);
					});
					callback(albums);
				}
			});
		},
		images : function(user, album, callback) {
			var url = "http://picasaweb.google.com/data/feed/base/user/:user_id/albumid/:album_id?alt=json&kind=photo&imgmax=1600&hl=en_US&fields=entry(title,link,gphoto:numphotos,media:group(media:credit,media:content,media:thumbnail))";
			url = url.replace(/:user_id/, user).replace(/:album_id/, album);
			var image = null;
			var images = [];

			$.ajax({
				dataType : 'jsonp',
				jsonp : 'callback',
				url : url,
				success : function(data) {
					$.each(data.feed.entry, function(i, element) {
						image = element["media$group"]["media$content"][0];
						image.title = element.title["$t"];
						image.link = element.link[1].href;
						image.authorName = element["media$group"]["media$credit"][0]["$t"];
						image.img2Link = element["media$group"]["media$thumbnail"][2].url;
						image.imageLink = image.url;
						image.source = "picasa";
						images.push(image);

					});
					callback(images);
				}
			});
		}
	};

	$.fn.picasaAlbums = function(user, callback) {
		$.picasa.albums(user, function(images) {
			if (callback) {
				callback(images);
			}
		});
	};

	$.fn.picasaImages = function(user, album, callback) {
		$.picasa.images(user, album, function(images) {
			if (callback) {
				callback(images);
			}
		});
	};
}(jQuery));

(function($) {
	$.p8RequestFunctions = {
		picasaAlbums : function(p8ElementId, isSingleOpen, singleOpened,singleClosed) {
			return function() {
				var self = this;
				self._preProcessResponse();

				var reqParams = self.element.data("galleryRequestParam");

				// var user = "110416871235589164413";
				// var album = "5633935823357108977";
				var user = reqParams.user;
				var album = reqParams.cat;
				var galleryLoadedItemsSel = $("#" + p8ElementId + ' .p8GalleryLoadedItems');
				var gallerySel = $("#" + p8ElementId + ' .p8GalleryCont');
				$.picasa.albums(user,album,function(images) {
					$.each(images,function(i, image) {
						galleryLoadedItemsSel.append(p8Options.appendForSingle(image.imageLink));

						if (p8Options.maxImagesInPage < 1) {
							gallerySel.append(jQuery('<div></div>').p8FeedItem({
								contentFunc : p8Options.createFeedContent,
								maxWidth : p8Options.maxImageWidth,
								maxHeight : p8Options.maxImageHeight
							}));
						}

					});

					self.allFeeds = self.allFeeds.concat(images);
					self.feedStreamEnd = true;

					if (p8Options.maxImagesInPage < 1) {
						gallerySel.p8JsonGallery("reloadChildren");
						gallerySel.p8SimpleGrid({
							totalInRow : p8Options.totalInRow,
							horizontal : p8Options.horizontalGrid
						});
					}

					p8Options.openSingle(p8ElementId,isSingleOpen, singleOpened,singleClosed);
					self._postProcessResponse();
				});
			};
		},

		picasaImages : function(p8ElementId, isSingleOpen, singleOpened,singleClosed) {
			return function() {
				var self = this;
				self._preProcessResponse();

				var reqParams = self.element.data("galleryRequestParam");

				// var user = "110416871235589164413";
				// var album = "5633935823357108977";
				var user = reqParams.user;
				var album = reqParams.cat;

				var galleryLoadedItemsSel = $("#" + p8ElementId+ ' .p8GalleryLoadedItems');
				var gallerySel = $("#" + p8ElementId + ' .p8GalleryCont');

				$.picasa.images(user,album,function(images) {
					$.each(images,function(i, image) {
						galleryLoadedItemsSel.append(p8Options.appendForSingle(image.imageLink));
						if (p8Options.maxImagesInPage < 1) {
							gallerySel.append(jQuery('<div></div>').p8FeedItem({
								contentFunc : p8Options.createFeedContent,
								maxWidth : p8Options.maxImageWidth,
								maxHeight : p8Options.maxImageHeight
							}));
						}
					});

					self.allFeeds = self.allFeeds.concat(images);
					self.feedStreamEnd = true;

					if (p8Options.maxImagesInPage < 1) {
						gallerySel.p8JsonGallery("reloadChildren");
						gallerySel.p8SimpleGrid({
							totalInRow : p8Options.totalInRow,
							horizontal : p8Options.horizontalGrid
						});
					}

					p8Options.openSingle(p8ElementId,isSingleOpen, singleOpened,singleClosed);
					self._postProcessResponse();
					
				});
			};
		},

		p8Gallery : function(p8ElementId, isSingleOpen, singleOpened,singleClosed) {
			return function() {
				var self = this;
				var reqParams = self.element.data("galleryRequestParam");

				var jp = {
					cat : reqParams.cat,
					'page' : reqParams.callCount++,
					nocache : "1"
				};
				if (reqParams.cursor != null) {
					jp = {
						cat : reqParams.cat,
						'cursor' : reqParams.cursor,
						'page' : reqParams.callCount++,
						nocache : "1"
					};
				}

				var galleryLoadedItemsSel = $("#" + p8ElementId + ' .p8GalleryLoadedItems');
				var gallerySel = $("#" + p8ElementId + ' .p8GalleryCont');
				$.ajax({
					dataType : 'jsonp',
					data : jp,
					jsonp : 'callback',
					url : 'http://photo.plus8.ch/feed',
					success : function(data) {
						self._preProcessResponse();

						if (data != null || data.error == null) {
							reqParams.cursor = data.cursor;
							for ( var i = 0; i < data.response.length; i++) {
								galleryLoadedItemsSel.append(p8Options.appendForSingle(data.response[i].imageLink));

								if (p8Options.maxImagesInPage < 1 && self.allFeeds.length === 0 ) {
									gallerySel.append(jQuery('<div></div>').p8FeedItem(																	{
										contentFunc : p8Options.createFeedContent,
										maxWidth : p8Options.maxImageWidth,
										maxHeight : p8Options.maxImageHeight
									}));
								}
							}

							
							
							if (p8Options.maxImagesInPage < 1 && self.allFeeds.length === 0 ) {
								gallerySel.p8JsonGallery("reloadChildren");
								gallerySel.p8SimpleGrid({totalInRow : p8Options.totalInRow,horizontal : p8Options.horizontalGrid});
							}

							self.allFeeds = self.allFeeds.concat(data.response);
							if(data.response.length === 0) {
								self.feedStreamEnd = true;
							}
							
							
							p8Options.openSingle(p8ElementId,isSingleOpen, singleOpened,singleClosed);

						} else {
							reqParams.cursor = null;
							self.isRetrivingFeed = false;
						}

						self.element.data("galleryRequestParam", reqParams);
						self._postProcessResponse();

					}
				});
			};
		}
	};
}(jQuery));

if (p8WidgetType === "demo-1") {
	jQuery('.p8GalleryWidget').append(
			'' + '<img class="p81998582214447" src="images/cat-3.jpg"/>' +
			'<img class="p81998582214447" src="images/cat-4.jpg"/>' +
			'<img class="p81998582214447" src="images/cat-1.jpg"/>' +
			'<img class="p81998582214447" src="images/cat-2.jpg"/>' +
			'');

	jQuery('.p81998582214447').hide();
	jQuery('#mainGallery').p8GalleryCreator({
		datas : jQuery('.p81998582214447'),
		reload : true,
		nextSelector : jQuery('.prevNextCont .next'),
		previousSelector : jQuery('.prevNextCont .previous')
	});
}

else if (p8WidgetType === "default" || p8WidgetType === "picasa") {

	var content = '<div style="margin-bottom: 10px;" class="galleryNavCont">';
	if(p8Options.maxImagesInPage > 1) {
		content += '<div class="prevNextCont" style="position: relative;"><a href="#" class="previous">Previous</a>&nbsp;|&nbsp;<a href="#" class="next">Next</a><span class="page-count"></span></div>';
	}
	content += '</div>'+ 
		'<div class="p8GalleryCont"> </div>'+
		'<div style="clear:both;"></div>'+
		'<div class="p8GalleryLoadedItems"> </div>';
	
	jQuery("#" + p8ElementId)
			.append(content);

	gallerySel = jQuery(galleryKey);		
			
	var updateNavigation = function() {
		jQuery('.page-count').text(
				"seite: " + (gallerySel.p8JsonGallery('getCurrentCount')));
	};

	// create feeditems

	for ( var i = 0; i < p8Options.maxImagesInPage; ++i) {
		gallerySel.append(jQuery('<div></div>').p8FeedItem({
			contentFunc : p8Options.createFeedContent,
			maxWidth : p8Options.maxImageWidth,
			maxHeight : p8Options.maxImageHeight
		}));
	}

	jQuery('.coloboxItems a').live().colorbox({
		rel : 'p8Gallery'
	});

	var galleryRequestFunction = jQuery.p8RequestFunctions.p8Gallery(
			p8ElementId, isSingleOpen, coloboxOpened, coloboxClosed);

	if (p8WidgetType === "picasa") {
		if (p8Options.picasaType === "gallery") {
			galleryRequestFunction = jQuery.p8RequestFunctions.picasaAlbums(
					p8ElementId, isSingleOpen, coloboxOpened, coloboxClosed);
		} else {
			galleryRequestFunction = jQuery.p8RequestFunctions.picasaImages(
					p8ElementId, isSingleOpen, coloboxOpened, coloboxClosed);
		}
	}

	gallerySel
		.data("galleryRequestParam", {callCount : 0, cursor : null, cat : null})
		.p8GalleryCreator({
			singleNextSelector : jQuery('.singleNav .next'), // TODO: not needed with colorbox
			singlePreviousSelector : jQuery('.singleNav .previous'), // TODO: not needed with colorbox
			singleClickSelector : '.article',
			singleCompareFunction : function(clickEl, el) {
				var description = jQuery(clickEl).closest('.p8FeedItem').p8FeedItem('activeFeedItem');
				return description.img2Link === el.img2Link;
			},
			singleClickSelectorFunction : p8Options.singleClickSelectorFunction,
			nextSelector : jQuery('.prevNextCont .next'),
			previousSelector : jQuery('.prevNextCont .previous'),
			feedItemsChangedFunction : function() {jQuery("#" + p8ElementId).busy("hide");},
			feedLoaderFunction : function(p8Item, feed) {
				if (feed !== null) {
					p8Item.p8FeedItem("load", feed.img2Link, feed);
				} else {
					p8Item.p8FeedItem("clean");
				}
			},
			singleNavigationShowHideFunction : function(e, s, t) {if (s) {jQuery(e).animate({opacity : 1}, 200);} else {jQuery(e).animate({opacity : 0.2}, 200);}},
			navigationShowHideFunction : function(e, s) {if (s) {jQuery(e).animate({opacity : 1}, 200);} else {jQuery(e).animate({opacity : 0.2}, 200);}},
			moveForwards : updateNavigation,
			moveBackwards : updateNavigation,
			// loadingFunction: function(evt,
			// p){jQuery('.prefNavBusy').fadeTo(200, (p.loading) ? 1
			// : 0 );},
			// requestFunction: requestFunctiona
			imageExtractorFunction : function(data) {return data.img2Link;},
			requestFunction : galleryRequestFunction,
			useMoveDelay : true
	});

	if (p8Options.maxImagesInPage > 0) {
		gallerySel.p8SimpleGrid({
			totalInRow : p8Options.totalInRow,
			horizontal : p8Options.horizontalGrid
		});
	} else {
		gallerySel.total = -1;
	}
	
	if (jQuery.isFunction(p8Options.postCreateGallery)) {
		p8Options.postCreateGallery();
	}

	jQuery("#" + p8ElementId).busy({
		hide : false
	});

	var reqParams = gallerySel.data('galleryRequestParam');
	reqParams.cat = p8Cat;
	reqParams.user = p8User;

	gallerySel.data('galleryRequestParam', reqParams);
	gallerySel.p8JsonGallery('reload');

}