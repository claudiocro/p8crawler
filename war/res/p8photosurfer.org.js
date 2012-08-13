/*
 *  p8 photosurfer  0.9.6
 * 
 * Depends on:
 * 
 * - jquery
 * - jquery-ui
 * - jquery-busy
 * 
 * - p8core
 */

(function($) {

	var defaultContentFunction = function(feed) {
		return feed;
	};

	$.widget("ui.p8FeedItem", {
		options : {
			activeCnt : null,
			inactiveCnt : null,
			contentFunc : defaultContentFunction,
			showBusyOnlyIfHidden : true,
			showBusy : false,
			busyImage : null,
			_timeout : null,
			_imageLoadTicket : 0,
			_loading: false
		},
		
		_create : function() {
			var self = this;
			var elem = this.element;
			elem.addClass("p8FeedItem");
			// elem.hide(false);

			var p8FeetCont = $('<div></div>').addClass('p8FeetCont').addClass('active');
			p8FeetCont.css({
				position : 'absolute',
				height : '100%',
				width : '100%',
				top : 0,
				left : 0,
				opacity : 0,
				'z-index' : 10
			});
			elem.append(p8FeetCont);

			var p8FeetInactiveCont = $('<div></div>').addClass('p8FeetCont').addClass('inactive');
			p8FeetInactiveCont.css({
				position : 'absolute',
				height : '100%',
				width : '100%',
				top : 0,
				left : 0,
				opacity : 0,
				'z-index' : 9
			});
			elem.append(p8FeetInactiveCont);

			var article = $('<div class="article"></div>').addClass('feedItem');
			if (self.options.activeCnt != null)
				article.append(self.options.activeCnt);
			p8FeetCont.append(article);

			var article = $('<div class="article"></div>').addClass('feedItem');
			if (self.options.inactiveCnt != null)
				article.append(self.options.inactiveCnt);
			p8FeetInactiveCont.append(article);
		},
		
		_ajustByClass : function() {
			var elem = this.element;
			$(".active", elem).css('z-index', 10);
			$(".inactive", elem).css('z-index', 9);
			$(".active", elem).css('opacity', 1);
			$(".inactive", elem).css('opacity', 0);
		},
		
		isVisible : function() {
			return $(".active", this.element).css('opacity') != 0 || $(".inactive", this.element).css('opacity') != 0;
		},
		
		isHidden : function() {
			return $(".active", this.element).css('opacity') == 0 && $(".inactive", this.element).css('opacity') == 0;
		},
		
		isMoving : function() {
			return $(".active", this.element).css('opacity') != 0 && $(".inactive", this.element).css('opacity') != 0;
		},
		
		load : function(image, feed) {
			var self = this;
			var elem = this.element;

			if (self.options._timeout != null)
				clearTimeout(self.options._timeout);

			// wait until animation has stopped
			if (self.isMoving()) {
				if (self.options._timeout != null)
					clearTimeout(self.options._timeout);

				self.options._timeout = setTimeout(function() {
					self.options._timeout = null;
					self.load(image, feed);
				}, 80);

				return;
			}

			this.options._imageLoadTicket = this.options._imageLoadTicket + 1;
			var nowTicket = this.options._imageLoadTicket;

			var article = null;
			if (self.isHidden())
				article = $('.active .article', elem);
			else
				article = $('.inactive .article', elem);

			if (image != null) {
				article.data('feedImage', image);
				article.css({
					'background-image' : 'url(' + image + ')'
				});
				var imageToWaitFor = new Image();
				if ($.browser.msie && parseInt($.browser.version, 10) <= 8) {
					setTimeout(function() {
						if (nowTicket == self.options._imageLoadTicket)
							self.switchFeeds();
					}, 350);
				} else {
					self._setLoading(true);
					imageToWaitFor.onerror = function() {
						if (nowTicket == self.options._imageLoadTicket) {
							self._setLoading(false);
							self.switchFeeds();
						}
					};
					imageToWaitFor.onload = function() {
						if (nowTicket == self.options._imageLoadTicket)
							self._setLoading(false);
						
						setTimeout(function() {
							if (nowTicket == self.options._imageLoadTicket)
								self.switchFeeds();
						}, 250);
					};
					imageToWaitFor.src = image;
					
				}
				if (feed != null) {
					article.empty().append(self.options.contentFunc(feed));
					article.data("feedItem", feed);
				} else {
					article.data("feedItem", null);
				}
			} else {
				article.data('feedImage', null);
				article.css({
					'background-image' : 'none'
				});
				if (feed != null) {
					article.empty().append(self.options.contentFunc(feed));
					article.data("feedItem", feed);
				} else {
					article.data("feedItem", null);
				}

				setTimeout(function() {
					self.switchFeeds();
				}, 250);
			}

//TODO: remove jTruncate dependency
$(".feedTitle", article).jTruncate({
	length : 40,
	minTrail : 0
});

		},
		
		switchFeeds : function() {
			var self = this;
			var elem = this.element;
			var moveAway = null;
			var moveIn = null;
			
			self._setLoading(false);
			
			if (self.isHidden()) {
				moveAway = $(".inactive", elem);
				moveIn = $(".active", elem);
			} else {
				moveAway = $(".active", elem);
				moveIn = $(".inactive", elem);
			}

			moveAway.css('z-index', 9);
			moveIn.css('z-index', 10);

			moveAway.animate({
				opacity : .001
			}, {
				duration : 1000,
				queue : true
			});

			moveIn.animate({
				opacity : 1
			}, {
				duration : 1500,
				queue : true,
				complete : function() {

					moveAway.removeClass("active");
					moveIn.removeClass("inactive");
					moveAway.addClass("inactive");
					moveIn.addClass("active");

					self._ajustByClass();
				}
			});
		},
		
		activeFeedContent : function() {
			return $('.active .article', this.element).html();
		},
		
		activeFeedItem : function() {
			if ($('.inactive', this.element).css('z-index') == 10)
				return $('.inactive article', this.element).data('feedItem');
			else
				return $('.active .article', this.element).data('feedItem');
		},
		
		clean : function() {
			var self = this;
			var elem = this.element;
			
			this.options._imageLoadTicket = this.options._imageLoadTicket + 1;
			
			self._setLoading(false);
			
			$('.p8FeetCont', elem).animate({
				opacity : 0
			}, 200);
			$('.article', elem).data('feedItem', null);
			$('.article', elem).data('feedImage', null);
			// $('article', elem).css({'background-image': ''}).empty();
		},
		_setLoading : function(loading) {
			this._loading = loading;
			if(this.options.showBusy == true) {
				if(loading == true && ((this.options.showBusyOnlyIfHidden == true && this.isHidden() == true) || this.options.showBusyOnlyIfHidden == false)) {
					var busyP = {hide : false};
					if(this.options.busyImage != null)
						$extend(busyP, {img:this.options.busyImage});
					
					$(this.element).busy(busyP);
				}
				else {
					$(this.element).busy('hide');
				}
			}
			
			this._trigger('loading', 0, loading);
		}
	});

}(jQuery));

(function($) {

	$.widget("ui.p8ImageCont", {
		options : {
			defaultZIndex : 1,
			activeZIndex : 20,
			maxWidth : 1245,
			maxHeight : 615,
			hideOnLoad : true
		},
		_create : function() {
			var self = this;
			var elem = this.element;

			this.ticket = 0;
			this.isBigContent = false;

			elem.addClass("p8ImageCont");
			elem.append($('<div></div>').addClass('bigimage-content').css({
				opacity : 0,
				"z-index" : self.options.defaultZIndex
			}).append($('<img />')).append($('<div><div class="bigimage-inner-desc"></div></div>').addClass('bigimage-desc').css({
				position : 'absolute',
				bottom : 0,
				right : 0,
				height : '15px',
				opacity : 0
			})));

//TODO: belongs not here?			
elem
// .css({height:"10px"})
.hover(function() {
	$('.bigimage-desc', this).stop().animate({
		opacity : .5,
		height : "30px"
	}, {
		duration : 250,
		easing : "easeInSine"
	});
}, function() {
	$('.bigimage-desc', this).stop().animate({
		opacity : 0,
		height : "15px"
	}, {
		duration : 200
	});
});

		},
		load : function(image, description) {
			var self = this;
			var elem = this.element;
			var bigImageCnt = $('.bigimage-content', elem);
			
			bigImageCnt.busy({
				img : 'res/images/busy.gif',
				hide : false
			});
			
			if (self.isBigContent && self.options.hideOnLoad == true) {
				bigImageCnt.stop().animate({
					opacity : 0
				}, 200);
			}
			

			var imageToWaitFor = new Image();
			imageToWaitFor.onerror = function() {
				if(curTicket == self.ticket) {
					imageToWaitFor.onerror = "";
					bigImageCnt.busy("hide");
					self.unload();
				}
			};
			
			var curTicket = ++this.ticket;
			imageToWaitFor.onload = function() {
				if(curTicket == self.ticket) {
					var imgElem = $('img', elem);
					
					bigImageCnt.busy("hide");
					
					
					setTimeout(function() {
						if(curTicket == self.ticket) {
							if (self.isBigContent && self.options.hideOnLoad == false) {
								bigImageCnt.stop().animate({
									opacity : 0
								}, 200, function() {
									
									imgElem.remove();
									bigImageCnt.append($('<img />').attr('src', image));
									self._load(imageToWaitFor, $('img', elem), image, description);		
								});
							} else {
								imgElem.remove();
								bigImageCnt.append($('<img />').attr('src', image));
								self._load(imageToWaitFor, $('img', elem), image, description);
							}
						}
					}, 350);
				}
			};
			imageToWaitFor.src = image;
		},
		
		unload : function() {
			var self = this;
			var elem = this.element;
			
			self.ticket++;
			self.isBigContent = false;
			$('.bigimage-content', elem).stop().animate({
				opacity : 0
			}, 600).css({
				"z-index" : self.options.defaultZIndex
			}).busy('hide');
		},
		
		_load : function(imageToWaitFor, imgElem, image, description) {
			var self = this;
			var elem = this.element;
			var bigImageCnt = $('.bigimage-content', elem);
			var bigimageDesc = $('.bigimage-desc', elem);
			var bigimageInnerDesc = $('.bigimage-inner-desc', elem);
			
			bigimageInnerDesc.empty();
			//imgElem.attr('src', image);
			
			var width = imageToWaitFor.naturalWidth;
			var height = imageToWaitFor.naturalHeight;

			if (width == null || height == null) {
				width = imageToWaitFor.width;
				height = imageToWaitFor.height;
			}

			var scaled = $.scaleSize(self.options.maxWidth, self.options.maxHeight, width, height);
			imgElem.attr("width", parseInt(scaled[0])).attr("height", parseInt(scaled[1])).css({
				"margin-top" : parseInt((self.options.maxHeight - scaled[1]) / 2)
			});

			bigimageDesc.css({
				bottom : parseInt((self.options.maxHeight - scaled[1]) / 2) + 'px',
				right : parseInt((self.options.maxWidth - scaled[0]) / 2) + 'px',
				width : scaled[0]
			});
			bigimageInnerDesc.append(description);

			bigImageCnt.stop().css("opacity",0).animate({
				opacity : 1
			}, 1000).css({
				"z-index" : self.options.activeZIndex
			});

			
			self.isBigContent = true;
		}
	});

}(jQuery));

(function($) {

	$.widget("ui.p8JsonGallery", {
		options : {
			maxCount : 150,
			requestFunction : null,
			feedLoaderFunction : null
		},
		_init : function() {
			this.currentCount = 0;
			this.ajaxTickedId = 0;
			this.allFeeds = new Array();
			this.feedStreamEnd = false;
			this.isRetrivingFeed = false;
			this.forceMoveForeward = false;
			this.total = this.element.children().size();
		},

		_create : function() {
			this.element.addClass("p8JsonGallery");
			this.element.children().addClass("p8JsonGallery-item");
		},
		reload : function() {
			this.currentCount = 0;
			this.allFeeds = new Array();
			this.feedStreamEnd = false;

			this._trigger('reload');
			
			this.moveForwards(true);
		},

		getCurrentCount : function() {
			return this.currentCount;
		},

		getAllFeeds : function() {
			return this.allFeeds;
		},

		canMoveForwards : function() {
			return (this.currentCount <= this.options.maxCount && this.allFeeds.length>0 && (this.total * (this.currentCount)) < this.allFeeds.length);
		},

		moveForwards : function(force) {
			if (this.canMoveForwards() == false && force != true)
				return false;

			var self = this;

			// handle navigation busy indicator
			/*if (self.feedStreamEnd != true && self.currentCount < self.options.maxCount && self.allFeeds.length < self.total * (self.currentCount + 4))
				self._trigger('loading', 0, true);
			else if (self.currentCount >= self.options.maxCount)
				self._trigger('loading', 0, false);
			*/

			// preload feed if necessary
			if (self.feedStreamEnd != true && self.allFeeds.length - (self.total * 4) < self.currentCount * self.total && !self.isRetrivingFeed) {
				self.isRetrivingFeed = true;

				self.ajaxTickedId++;

				self.options.requestFunction.call(this);
				self.forceMoveForeward = true;
			}

			// load next available feedItems
			if (self.canMoveForwards()) {
				self.forceMoveForeward = false;
				self._loadNextItems();
				self.currentCount++;
				
				self._trigger('moveForwards');
				return true;
			} else {
				return false;
			}
		},

		canMoveBackwards : function() {
			return (this.currentCount >= 2);
		},

		moveBackwards : function() {
			if (this.canMoveBackwards() == false)
				return false;

			var self = this;
			self.forceMoveForeward = false;
			
			self.currentCount--;
			self.currentCount--;
			self._loadNextItems();
			self.currentCount++;

			self._trigger('moveBackwards');
			return true;
		},
		
		moveToSingleIndex : function(imageIndex) {
			if(imageIndex>=0 && imageIndex < this.allFeeds.length) {
				return this.moveToPage(Math.floor((imageIndex/this.total)+1));
			}
			return false;
		},
		
		moveToPage : function(page) {
			if(this.currentCount != page) {
				var oldCurrentCount = this.currentCount;
				this.currentCount = page-1;
				if(this.moveForwards()) {
					return true;
				} else {
					this.currentCount = oldCurrentCount;
					return false;
				}
			}
			else {
				return true;
			}
		},

		_loadNextItems: function() {
			var self = this;
			var p8Items = $('.p8JsonGallery-item', this.element);
			var cnt = 0; 
			for ( var i = this.total * this.currentCount, len = this.allFeeds.length; i < this.total * (this.currentCount+1) && i < len && i > -1; ++i) {
				self.options.feedLoaderFunction.call(self, $(p8Items.get(i - this.total * this.currentCount)), this.allFeeds[i]);
				cnt++;
			}
			for(;cnt < this.total; cnt++){
				self.options.feedLoaderFunction.call(self, $(p8Items.get(cnt)), null);
			}
			
			
		},
		
		_preProcessResponse : function() {
			var self = this;
			
			self.isRetrivingFeed = true;
			self._trigger('loading', null, {loading:true});
		},

		_postProcessResponse : function() {
			var self = this;
			
			self.isRetrivingFeed = false;
			self._trigger('loading', null, {loading:false});
			if (self.forceMoveForeward == true) {
				self.moveForwards();
			}
			
			self._trigger('feedItemsChanged');
		}
	});

}(jQuery));

(function($) {

	/*
	 * Simple grid sollte in eine funktion umgewandelt werden. 
	 *  
	 */
	
	$.widget("ui.p8SimpleGrid", {
		options : {
			contentSelector : null,
			total : 8,
			totalInRow : 2
		},

		_create : function() {
			if (this.options.contentSelector == null)
				this.options.contentSelector = this.element.children();

			var self = this;
			var elem = this.element;

			elem.addClass('p8SimpleGrid').addClass('gridded-content').css({
				'z-index' : 2
			});

			var currentCol = 0;
			var currentRow = 0;
			var currentColE = null;
			self.options.contentSelector.each(function() {
				if (currentRow == 0) {
					currentColE = $('<div class="grid col col-' + currentCol + '">');
				}
				currentColE.append($(this));

				elem.append(currentColE);
				if (currentRow == self.options.totalInRow - 1) {
					currentCol++;
					currentRow = 0;
				} else {
					currentRow++;
				}
			});
		}
	});

}(jQuery));


(function($) {
	
	var extractFunction = function(){
		var image = $.extractUrl($(this).css("background-image"));
		var r = ({image: image, content: $(this).html()}); 
		$(this).css('background-image', 'none').empty();
		return r;
	};
	
	$.fn.p8CreateAutoLoadFeedItem = function(poptions) {
		var options = jQuery.extend ({
				extractFunction:extractFunction,
				showBusyOnlyIfHidden : true,
				showBusy : false,
			},poptions);
		
		return this.each (function () {
			var self = this;
			var feed = options.extractFunction.call(self);
			return $(self).p8FeedItem({
				showBusyOnlyIfHidden : options.showBusyOnlyIfHidden,
				showBusy : options.showBusy,
//TODO: busyImage
			}).p8FeedItem('load', feed.image,feed.content);
		});
	};
	
}(jQuery));


(function($) {
	
	var feedLoaderFunction = function(element,data) {
		if(data!= null)
			element.empty().append($(data).clone().show());
		else
			element.empty();
	};
	
	var navigationShowHideFunction = function(element, show, type) {
		if(show == true) {
			$(element).show(200);
		} else {
			$(element).hide(200);
		}
	};
	
	
	$.fn.p8GalleryCreator = function(poptions) {
		var options = jQuery.extend ({
				datas:								null,
				reload:								false,
				nextSelector:						null,
				previousSelector:					null,
				requestFunction:					null,
				singleNextSelector:					null,
				singlePreviousSelector:				null,
				singleCompareFunction:				null,
				singleClickSelector:				null,
				singleClickSelectorFunction:		null,
				singleNavigationShowHideFunction:	navigationShowHideFunction,
				feedLoaderFunction:					feedLoaderFunction,
				navigationShowHideFunction:			navigationShowHideFunction,
				loadingFunction:					null,
				feedItemsChangedFunction:			null,
				reloadFunction:						null,
				moveForwards:						null,
				moveBackwards:						null
			},poptions);
		
		var index = -1;
		var updateSingleNavigation = function() {
				var length = $(this).p8JsonGallery('getAllFeeds').length;
				if(options.singleNextSelector != null) {
					if(index != -1 && index+1 < length) {
						options.singleNavigationShowHideFunction(options.singleNextSelector, true, 'fwd');
					} else {
						options.singleNavigationShowHideFunction(options.singleNextSelector, false, 'fwd');
					}
				}
				
				if(options.singlePreviousSelector != null) {
					if(length == 0 || index <=0) {
						options.singleNavigationShowHideFunction(options.singlePreviousSelector, false, 'back');
					} else {
						options.singleNavigationShowHideFunction(options.singlePreviousSelector, true, 'back');
					}
				}
		};
		
		
		
		var updateNavigation = function() {
			if(options.nextSelector != null)
				options.navigationShowHideFunction(options.nextSelector, $(this).p8JsonGallery('canMoveForwards'), 'fwd');
			
			if(options.previousSelector != null)
				options.navigationShowHideFunction(options.previousSelector, $(this).p8JsonGallery('canMoveBackwards'), 'back');
		};
		
		if(options.datas instanceof jQuery) {
			options.datas = options.datas.toArray();
		}
		
		if(options.requestFunction == null) {
			options.requestFunction = function() {
				this._preProcessResponse();
				
				this.allFeeds = this.allFeeds.concat(options.datas);
				this.feedStreamEnd = true;
				this._postProcessResponse();
			};
		}
		
		
		return this.each (function () {
			var self = this;
			
			if(options.nextSelector != null) {
				options.navigationShowHideFunction(options.nextSelector, false);
				options.nextSelector.bind('click', function() {
					if($(self).p8JsonGallery('canMoveForwards'))
						$(self).p8JsonGallery('moveForwards');
					
					return false;
				});
			}
			
			if(options.previousSelector != null) {
				options.navigationShowHideFunction(options.previousSelector, false);
				options.previousSelector.bind('click', function() {
					if($(self).p8JsonGallery('canMoveBackwards'))
						$(self).p8JsonGallery('moveBackwards');
					
					return false;
				});
			}
			
			if(options.singleClickSelectorFunction != null) {
				if(options.singleNextSelector != null) {
					options.singleNextSelector.click(function() {
						index++;
						var canMove = $(self).p8JsonGallery('moveToSingleIndex', index);
						if(canMove) {
							options.singleClickSelectorFunction.call(self, $(self).p8JsonGallery('getAllFeeds')[index]);
						} else {
							index--;
						} 
						updateSingleNavigation.call(self);
						return false;
					});
				}
				
				if(options.singlePreviousSelector != null) {
					options.singlePreviousSelector.click(function() {
						if(index!=0){
							index--;
							var canMove = $(self).p8JsonGallery('moveToSingleIndex', index);
							if(index >=0 && canMove) {
								options.singleClickSelectorFunction.call(self, $(self).p8JsonGallery('getAllFeeds')[index]);
							}
							else {
								index--;
							}
							updateSingleNavigation.call(self);
						}
						return false;
					});
				}
				
				
			}
			
			if(options.singleClickSelector != null) {
				$(options.singleClickSelector, self).click(function() {
					if(options.singleClickSelectorFunction != null && options.singleCompareFunction != null) {
						
						var allFeeds = $(self).p8JsonGallery('getAllFeeds');
						
						for(var i=0;i<allFeeds.length; i++) {
							if(options.singleCompareFunction(this, allFeeds[i])) {
							//if(src == $(allFeeds[i]).attr("src")) {
								index = i;
								options.singleClickSelectorFunction.call(self, allFeeds[index],index);
								break;
							}
								
						}
					}
					updateSingleNavigation.call(self);
					return false;
				});
			}
			
			
			$(this).p8JsonGallery({
				feedLoaderFunction: options.feedLoaderFunction,
				requestFunction: options.requestFunction,
				loading: options.loadingFunction,
				moveForwards:function(){
					if(options.moveForwards != null){options.moveForwards.call(this);} 
					updateNavigation.call(this);},
				moveBackwards:function(){
					if(options.moveForwards != null){options.moveBackwards.call(this);}
					updateNavigation.call(this);},
				reload: function(){
					if(options.reloadFunction != null){options.reloadFunction.call(this);} 
					updateNavigation.call(this);
					updateSingleNavigation.call(this);},
				feedItemsChanged:function(){
					updateNavigation.call(this);
					updateSingleNavigation.call(this);
					if(options.feedItemsChangedFunction != null){options.feedItemsChangedFunction.call(this);}}
			});
		
			if(options.reload)
				$(this).p8JsonGallery('reload');
			else {
				updateNavigation.call(this);
				updateSingleNavigation.call(this);
			}
			
			return $(this);
		});
	};
	
})(jQuery);

