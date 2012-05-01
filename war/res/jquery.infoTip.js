
(function($){  
	
	var infoTipOverlay = $('<div id="p8-infoTip-overlay"/>');
	
	$.fn.p8InfoTip = function (psettings) {
		
		
		function InfoTip(elem, settings) {
			
			var infoTip = this;
			var isShowing = false;
			var fadingOut = false;
			var tinyTip;
			var tText;
			
			
			_initialize();
			
			function _initialize() {
				var self = this;
				
				tinyTip  = $('<div class="'+settings.tipClass+'"><div class="top" style="text-align:'+settings.topAlign+';"><img src="'+settings.peackImage+'"></div><div class="content" style="width:'+settings.width+'"></div></div>');
				tinyTip.hide();
				$('body').append(tinyTip);		
				
				if(settings.removeOnFocusLost) {
					elem.blur(function() {
						setTimeout(function() {
							if(settings.removeAfterIfFocus || !elem.is(':focus'))
								hide();
							
						},settings.removeAfter);
					});
				}
				
				if(settings.removeOnClickOutside) {
					if($('#p8-infoTip-overlay').size() == 0)
						var overlay = $(infoTipOverlay).css({'opacity':0, 'z-index':settings.zindex-1}).hide()
						$('body').append(overlay);
					
					$('#p8-infoTip-overlay').click(function(){
						hide();
						return false;
					});
					
					
				}
				
				if(settings.content !== undefined)
					$('.content', tinyTip).append(settings.content);
				
			}
			
			function _show() {
				
				if(isShowing == true)
					return;
				
				isShowing = true;
				//var xOffset = (tinyTip.width() / 2) - (elem.width() / 2);
				var xOffset = 0;
				if(settings.topAlign == 'center')
					xOffset = (tinyTip.width() / 2) - (elem.width() / 2);
				
				var pos = elem.offset();
				var nPos = pos;
				
				// Add the offsets to the tooltip position
				nPos.top = pos.top + elem.height()-10;
				nPos.left = pos.left - xOffset;
				
				// Make sure that the tooltip has absolute positioning and a high z-index, 
				// then place it at the correct spot and fade it in.
				
				if(settings.removeOnClickOutside)
					$('#p8-infoTip-overlay').css({'width':$(document).width(),'height':$(document).height()}).show();  
				
				
				tinyTip
					.css('position', 'absolute')
					.css('z-index', settings.zindex)
					.css('display','block')
					.css('opacity',.5);
				tinyTip.css(nPos);
				tinyTip.animate(
					{opacity:.8, top: nPos.top+15 },
					{queue: false, duration:settings.speed});
			}
			
			function show(supCont) {
				
				if(supCont !== undefined) {
					settings.content = supCont;
					$('.content', tinyTip).empty().append(supCont);
				}
				
				if(settings.content === undefined)
					return;
				
				_show();
				
				
				
				
				if(settings.removeAfter > 0) {
					setTimeout(function() {
						if(settings.removeAfterIfFocus || !elem.is(':focus'))
							hide();
					},settings.removeAfter);
				}
				
				if(settings.showTrigger !== undefined)
					settings.showTrigger.call();
			}
			
			function hide() {
				if(isShowing) {
					isShowing = false;
					
					if(settings.removeOnClickOutside)
						$('#p8-infoTip-overlay').hide();  
					
					tinyTip.animate(
							{opacity:0, top: '+=10'},
							settings.hideSpeed,
							function() {
								tinyTip.css('display', 'none');
							});
					
					if(settings.hideTrigger !== undefined)
						settings.hideTrigger.call();
				}
			}
			
			
			function destroy() {
				
				if(settings.removeOnClickOutside)
					$('#p8-infoTip-overlay').hide();  
				
				tinyTip.fadeOut(settings.speed, function() {
					$(this).remove();
				});
				elem.removeData('p8InfoTip', null);
			}
			
			
			$.extend(infoTip,{
				show: function(text){
					show(text);
				},
				hide: function(){
					hide();
				},
				isShowing: function(){
					return isShowing;
				},
				destroy: function(){
					destroy();
				}
			});
		}
		
			
		psettings = $.extend({}, $.fn.p8InfoTip.defaults, psettings);
		
		this.each( function() {
			var elem = $(this);
			var p8infoTip = elem.data('p8InfoTip');
			if (!p8infoTip) {
				p8infoTip = new InfoTip(elem, psettings);
				elem.data('p8InfoTip', p8infoTip);
			}
		});
		
		
		return this;
	}
	
	
	$.fn.p8InfoTip.defaults = {
		'tipClass'				: 'error-tip',
		'speed'					: 375,
		'hideSpeed'				: 125,
		'width'					: '150px',
		'removeAfter'			: 1300,  //removes the infoTip after a period of time
		'removeAfterIfFocus'	: false, //remove it even it is focused
		'removeOnFocusLost'		: true,  //remove if the source element loses the focus
		'removeOnClickOutside'	: true,  //removes if the someone clicks outside the infoTip
		'zindex'				: 2005,
		'content'				: undefined,
		'peackImage'			: 'res/images/error-tip_top.png',
		'topAlign'				: 'center', //left,center,right
		'hideTrigger'			: undefined,
		'showTrigger'			: undefined
	};

})(jQuery);