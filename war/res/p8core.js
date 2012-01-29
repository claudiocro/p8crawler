/*
 *  p8 core  0.1.0
 * 
 */

$.extend({
	nl2br : function(text){
		text = escape(text);
		if(text.indexOf('%0D%0A') > -1){
			re_nlchar = /%0D%0A/g ;
		}else if(text.indexOf('%0A') > -1){
			re_nlchar = /%0A/g ;
		}else if(text.indexOf('%0D') > -1){
			re_nlchar = /%0D/g ;
		}
		return unescape( text.replace(re_nlchar,'<br />') );
	}
});


$.extend({
	getUrlVars : function() {
		var vars = [], hash;
		var hashes = window.location.href.slice(window.location.href.indexOf('?') + 1).split('&');
		for ( var i = 0; i < hashes.length; i++) {
			hash = hashes[i].split('=');
			vars.push(hash[0]);
			vars[hash[0]] = hash[1];
		}
		return vars;
	},
	getUrlVar : function(name) {
		return $.getUrlVars()[name];
	}
});

$.extend({
	scaleSize : function(maxW, maxH, currW, currH) {
		var ratio = currH / currW;
		var maxRatio = maxH / maxW;

		if (currW >= maxW && ratio <= maxRatio) {
			currW = maxW;
			currH = currW * ratio;
		} else if (currH >= maxH) {
			currH = maxH;
			currW = currH / ratio;
		}

		return [ currW, currH ];
	}
});


$.extend({
	// remove quotes and wrapping url()
	extractUrl : function extractUrl(input) {
		return input.replace(/"/g,"").replace(/url\(|\)$/ig, "");
	}
});


/*
 * JQuery selector to get the activeElement from a document
 * 
 * Use: $(this).is(':focus')
 */
jQuery.extend(jQuery.expr[':'], {
    focus: function(element) { 
        return element == document.activeElement; 
    }
});

