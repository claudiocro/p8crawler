/*
 *  p8 ui  0.2.1
 * 
 * */


/**
 * Line break to html BR
 * 
 */
function nl2br(text){
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

(function($) {

/*
 * HELPER METHOD
 * CREATE MultiContactInfoEditor widget
 * 
 */
	

/**
 * Maps single value field with a type from the options in p8uiCreateTypeValueMultiEditor 
 * 
 */
function mapSingleTypedValuesFields(options) {
	var valueFields = new Array();
	
	if(options.typeField != null) {
		var valueField = {};
		valueField.value = options.typeField;
		valueField.customValueField = options.customValueField;
		valueField.types = options.types;
		valueField.title = $.i18n(options.i180nPackage, options.group+'-'+prop);
		valueField.i180nPackage = options.i180nPackage;
		valueField.type = "select";
		valueFields.push(valueField);
	}
	
	for(prop in options.mapping) {
		var valueField = {};
		valueField.value = options.mapping[prop];
		valueField.title = $.i18n(options.i180nPackage, options.group+'-'+prop);
		valueField.type = "text";
		valueField.rules = options.rules[prop];
		valueFields.push(valueField);
	}
	options.valueFields = valueFields;
	
//	$.extend (options, contactInfoEditorParams);
	
}


/**
 * Create a type with value editor based on the provided options
 * 
 */
$.fn.p8uiCreateTypeValueMultiEditor = function (poptions) {

	var options = jQuery.extend ({
		valueField: "value",
		i180nPackage: '',
		mapping: null,
		typeField: null,
		customValueField: null,
		types: null,
		newCount: 0,
		hiddenNewData: {},
		rules: {}
	}, poptions);
	
	if(options.valueField != null && options.mapping == null)
		options.mapping = {VALUE_FIELD: options.valueField};
	
	mapSingleTypedValuesFields(options);
	
	return this.each (function () {
		$(this).p8uiMultiEditor(options);
		return $(this);
	});
};
	


}(jQuery));









(function($) {

	
/**
 * Returns the id attribute from the provided parameter
 * 
 */
var uniqueIdFunc = function(editor) {
	return editor.attr('id');
};
	
/* 
 * WIDGET
 * MULTI CONTACT INFO EDITOR
 * 
 */


/**
 * 
 * 
 */
var p8uiMultiEditor = {
	options: {
		group: '',
		title: '',
		innerSel: '.form-row-inner',
		newCount: 0,
		newFunc: null,
		allowNew: true,
		initFunc: null,
		hiddenNewData: {}, //should be used by the newFunc
		uniqueIdFunc: uniqueIdFunc
	},
	
	_create: function() {
		var self = this;
		var selfElem = self.element;
		
		
		if(this.options.newFunc != null) {
			$('<a />').button({ text:false, label:'Add', icons: {primary:'ui-icon-plus'}}).addClass('add').hide().button().click(function() {
				self.addNew(true);
			}).hide().appendTo(selfElem);
		}
		
		this.options.title = this.options.group; 
		
//		selfElem.prepend($('<br/>'));
		
		self.focused = false;
	},
	_decorateEditor: function(editor) {
		var self = this;
		
		editor.addClass("form-row");
		
		var editorWrap = editor.find(this.options.innerSel+"-wrap");
		editorWrap.prepend($('<div><br /></div>').addClass('action-panel'));
		if($.isFunction(this.options.newFunc)) {
		
			if(editor.hasClass("init-new"))
				return;
			
			var delButton = $('<a />').button({ text:false, label:'Delete', icons: {primary:'ui-icon-minus'}})
			.addClass('delete')
			.click(function() {
				var remove = false;
				if(editor.hasClass('new'))
					remove = true;
				else
					editor.addClass('deleted');
				
				
				var scrollApi = self.element.parents(".p8-scroll").data('jsp');
				if(self.element.find('.form-row').not(':hidden').size() > 1) {
					var editorHeight = editor.height()+2; //TODO: +2 is the padding of the parent div ...
					var containerHeight = self.element.parents(".jspContainer:first").height();
					var jspPane = self.element.parents(".jspPane:first");
					var paneHeight = jspPane.height()+jspPane.position().top;
					
					if((paneHeight-editorHeight) < containerHeight) {
						scrollApi.scrollByY(editorHeight*-1, true);
					}
				}
				
				editor.animate(
					{	height:"toggle", opacity:"toggle"},
	  				{	duration: 600, easing: 'easeInBack',
						complete: function() {
	  						
							if(remove) {
								self.element.find('.add').hide().appendTo(self.element);
								$(this).remove();
							}
							
							if(self.element.find('.form-row').not(':hidden').size() == 0)
								self.addNew(true);
							else {
								self.updateAddButton();
								scrollApi.reinitialise();								
							}
						}
	  				}
				);
				
				return true;
			});
	
			editor.find(this.options.innerSel+'-wrap:last').find('.action-panel').prepend(delButton);
			editor.find('input').focus(function(){
				clearTimeout(self.blurTimer);
				if(!self.focused) {
					self.focused = true;
					self._trigger('focusgained', null, {item: this});
				}
			});
			
			editor.find('input').blur(function(){
				clearTimeout(self.blurTimer);
				self.focused = false;
				self.blurTimer = setTimeout(function() {
					if(self.focused != true) {
						self._trigger('focuslost');
					}
				},50);
			});
			
			this.element.trigger('focus');
			
//			$('.ui-autocomplete').jScrollPane();
		}
		
		for(prop in this.options.hiddenNewData) {
			editorWrap.append(
				$("<input/>").attr('type', 'hidden').
				attr('name',  this.options.uniqueIdFunc.call(this,editor)+'-'+prop).
				attr('value', this.options.hiddenNewData[prop])
			);
		}
		
	},
	load: function(contactInfos) {
		var newFunc = this.options.newFunc;
		if ($.isFunction(newFunc)) {
			for ( var i=0, len=contactInfos.length; i<len; i++ ) {			
				var editor = newFunc.call(this,contactInfos[i]);
				this._decorateEditor(editor);
				this.element.append(editor);
				
				$(".p8-typeSelect", editor).p8TypeSelect("initRules");
				$(".p8-valueField", editor).p8ValueField("initRules");
			}
			
		}
		
		if(contactInfos.length == 0) 
			this.addNew();
	
		this.updateAddButton();
	},
	addNew: function(animate) {
		if(this.options.allowNew == false)
			return;
		
		var that = this;
		
		var initFunc = this.options.initFunc;
		var newFunc = this.options.newFunc;
		if ($.isFunction(initFunc) && this.element.find(".init-new").length == 0) {
			this.addInit(animate);
		}
		else if ($.isFunction(newFunc)) {
			this.element.find(".init-new").hide(200, function() {
				 var addButton = that.element.find('.add');
				 addButton.hide();
				 addButton.appendTo(that.element);
				$(this).remove();
			});
			
			var editor = newFunc.call(this);
			
			this.options.newCount = this.options.newCount+1;
			editor.addClass('new');
			this._decorateEditor(editor);
			editor.css('opacity', 1);
			this.element.append(editor);
			this.updateAddButton();
			
			$(".p8-typeSelect", editor).p8TypeSelect("initRules");
			//$(".p8-valueField", editor).p8ValueField("initRules");
			
			if(animate) {
				editor.css('opacity', 0);
				var scrollPaneElem = this.element.parents(".p8-scroll").filter(':first');
				if(scrollPaneElem.length >0) {
					var scrollApi = scrollPaneElem.data('jsp');
					var elemTop = editor.position().top;
					var elemHeight = (editor.height()+2); 
					scrollApi.reinitialise();
					if(elemTop+elemHeight > scrollPaneElem.height())
						scrollApi.scrollByY(elemHeight, true);
				}
			}
			
			editor.hide();
			editor.css('opacity', 1);
			if (animate===undefined) {
				editor.show();
			} else {
				editor.animate(
					{opacity:'toggle', height:'toggle'},
	  				{duration:600, easing:'easeOutBack', complete:function(){that.updateAddButton();}}
	  			);
  			}
			

		}
			
	},
	addInit: function(animate) {
		var initFunc = this.options.initFunc;
		if ($.isFunction(initFunc)) {
			var editor = initFunc.call(this);
			
			this.options.newCount = this.options.newCount+1;
			editor.addClass('init-new');
			this._decorateEditor(editor);
			editor.css('opacity', 1);
			this.element.append(editor);
			this.updateAddButton();
			
			if(animate) {
				var scrollPaneElem = this.element.parents(".p8-scroll").filter(':first');
				if(scrollPaneElem.length >0) {
					var scrollApi = scrollPaneElem.data('jsp');
					var elemTop = editor.position().top;
					var elemHeight = (editor.height()+2); 
					scrollApi.reinitialise();
					if(elemTop+elemHeight > scrollPaneElem.height())
						scrollApi.scrollByY(elemHeight, true);
				}
			}
			
			
			editor.hide();
			editor.css('opacity', 1);
			if (animate===undefined) {
				editor.show();
				
			} else {
				editor.animate(
					{opacity:'toggle', height:'toggle'},
	  				{duration:600, easing:'easeOutBack'}
	  			);
  			}

		}
			
	},
	
	updateAddButton: function() {
		this.element.find('.form-row').removeClass('last').removeClass('first');
		
		if(this.element.find('.form-row').not(":hidden").length ==1) {
			this.element.find('.form-row').not(":hidden").addClass('first').addClass('last');
		} else {
			this.element.find('.form-row').not(":hidden").first().addClass('first');
			this.element.find('.form-row').not(":hidden").last().addClass('last');
		}
		
		this.element.find(this.options.innerSel+"-wrap").removeClass('last').removeClass('first');
		if(this.element.find(this.options.innerSel+"-wrap").not(":hidden").length ==1) {
			this.element.find(this.options.innerSel+"-wrap").not(":hidden").addClass('first').addClass('last');
		} else {
			this.element.find(this.options.innerSel+"-wrap").not(":hidden").first().addClass('first');
			this.element.find(this.options.innerSel+"-wrap").not(":hidden").last().addClass('last');
		}
		
		
		if(this.options.newFunc != null) {
			//appends to the last
			//var addButton = this.element.find('.add').css('display','inline-block');
			//this.element.filter(':not(.form-row:hidden)').find('.form-row-inner:last').find('.action-panel').prepend(addButton);
			
			//appends to .init-new
			if(this.element.find('.init-new').length >0) {
				var addButton = this.element.find('.add');
				addButton.css('display','inline-block');
				this.element.find('.init-new').find('.action-panel').prepend(addButton);
			}
			
		}
	}
};

$.widget("ui.p8uiMultiEditor", p8uiMultiEditor);
	
}(jQuery));















(function($) {


/*
 * JQUERY FUNCTION
 * CREATE a two column layout for a MultiContactInfoEditor
 * 
 */
	
$.fn.p8uiTwoColPanel = function (poptions) {

	var options = jQuery.extend ({
		data: null,
		valueFields: [],
		prefix: '',
		innerClass: 'form-row-inner'
	}, poptions);

	return this.each (function () {
		
		var selfElem = $(this);
		
		var innerRow = null;
		var length = options.valueFields.length;
		var colId = 0;
		for ( var i=0; i<length; i++ ){
		
			var valueField=options.valueFields[i];
			if(i %2 == 0) {
				colId = 0;
				innerRow = $('<div class="'+options.innerClass+' edit p8TwoCol-row-'+i/2+' '+length+'" />');
				if(i == 0) innerRow.addClass("p8TwoCol-row-first");
				
				selfElem.append($('<div class="'+options.innerClass+'-wrap" />').append(innerRow));
			}
			
			if(i == length-1 && innerRow != null) innerRow.addClass("p8TwoCol-row-last");
			
			if(valueField.type == "text") {
				innerRow.append($('<span />').addClass('col-'+colId).p8ValueField({
					data: options.data,
					valueField: valueField.value,
					prefix: options.prefix,
					title: valueField.title,
					rules: valueField.rules
				}));
			}
			else if(valueField.type == "select") {
				innerRow.append($("<span />").addClass('col-'+colId).p8TypeSelect({
					types: valueField.types,
					data: options.data,
					types: valueField.types,
					typeField: valueField.value,
					customValueField: valueField.customValueField,
					prefix: options.prefix,
					title: valueField.title,
					i180nPackage: valueField.i180nPackage,
					rules: {
						minlength: 2,
						required: true,
						messages: {
							minlength:"Please, at least {0} characters are necessary"}}
				}));
			}
			colId++;
		}
		
		return $(this);
	});
};
	
	

}(jQuery));



(function($) {


/*
 * JQUERY FUNCTION
 * CREATE panel with a singlefiled and ok / cancel buttons
 * 
 */
	
$.fn.p8uiSimpleSearchPanel = function (poptions) {

	var options = jQuery.extend ({
		okFunc: null,
		cancelFunc: null,
		cancelText: 'Cancel',
		contentTop: null,
		contentRight: null,
		fieldName: 'search',
		fieldTitle: 'Search'
	}, poptions);

	return this.each (function () {
		var selfElem = $(this);
		
		var panel = $('<div class="p8-simpleSearchPanel" />');
		
		if(options.contentTop != null) {
			var top = $('<div class="p8-simpleSearchPanel-top" />').append(options.contentTop);
			selfElem.append(top);
		}
		
		var searchField = $('<input />').
			attr('type', 'text').
			attr('name', options.fieldName).
			attr('title', options.fieldTitle);
		
		searchField.keypress(function(e) {
			c = e.which ? e.which : e.keyCode;
	        if(c == 13) {
	        	if($.isFunction(options.okFunc))
	        		options.okFunc.call(this);
	        }
	    });
	
		var input = $('<div class="p8-simpleSearchPanel-field" />');
		if(options.contentRight != null)
			input.append(options.contentRight);
		input.append(searchField);
		selfElem.append(input);
		
		if(options.buttons != null) {
			var buttons = $('<div class="p8-simpleSearchPanel-button" />').
				append($('<button />').html(options.cancelText).button().click(function() {
					
				}));
			selfElem.append(buttons);
		}
		
		
		
	});
 };
 
}(jQuery));

(function($) {


	/*
	 * JQUERY FUNCTION
	 * CREATE hint text based on the title for an input field
	 * 
	 */

$.fn.p8AddHint = function() {
	// applies a text hint to the text input field by using the input's title attribute
	return this.each(function(){
		var selfElem = jQuery(this);
		var title = this.title;
		
		if (selfElem.val() == '') {
			selfElem.addClass('hint_text');
			selfElem.val(title);
		}
		
		selfElem.focus(function(){
			if (selfElem.val() == title) {
				selfElem.removeClass('hint_text');
				selfElem.val('');
			}
		});
		
		selfElem.blur(function(){
			if (selfElem.val() == '') {
				selfElem.addClass('hint_text');
				selfElem.val(title);
			}
		});
	});
};

}(jQuery));













(function($) {

/*
 * WIDGETS
 * FORM FIELDS
 * 
 * - Text
 * - Select
 * 
 */
	
	
function addDirty() {
	$(this).addClass("dirty");
}
	
var p8ValueField = {
	options: {
		data: null,
		valueField: 'value',
		prefix: '',
		title:''
	},
	_create: function() {
		this.element.addClass("p8-inputElem");
		this.element.addClass("p8-valueField");
		this.element.change(addDirty);
		
		var value = ''; 
		if( this.options.data[this.options.valueField] !== undefined )
			value = this.options.data[this.options.valueField];
		
		var input = $('<input />').
			attr('type', 'text').
			attr('name', this.options.prefix+'-'+this.options.valueField).
			attr('title', this.options.title).
			attr('value', value).
			p8InfoTip();
		this.element.append(input);
		
		input.p8AddHint();
		input.data('p8-inputElem-value', value);
		input.data('p8-inputElem-orgValue', value);
		input.blur(function() {
			$(this).data('p8-inputElem-value', $(this).attr('value'));
		});
		input.focus(function() {
			$(this).valid();
		});
	},
	initRules: function() {
		if(this.options.rules !== undefined)
			this.element.find("input").rules("add", this.options.rules);
	}
};

$.widget("ui.p8ValueField", p8ValueField);
	

var p8ValueFieldT = {
		options: {
			data: null,
			valueField: 'value',
			prefix: '',
			title:'',
			showHint: false
		},
		_create: function() {
			var cnt = $("<span>");
			
			cnt.addClass("p8-inputElem");
			cnt.addClass("p8-valueField");
			cnt.change(addDirty);
			var parent = this.element.parent();
			cnt.appendTo(parent);
			
			this.element.appendTo(cnt);
			
/*			var value = ''; 
			if( this.options.data[this.options.valueField] !== undefined )
				value = this.options.data[this.options.valueField];
*/			
			this.element.
				attr('name', this.options.valueField).
				attr('title', this.options.title).
				//attr('value', value).
				p8InfoTip();

			if(this.options.showHint)
				this.element.p8AddHint();
			
			this.element.data('p8-inputElem-value', $(this).attr('value'));
			this.element.data('p8-inputElem-orgValue', $(this).attr('value'));
			this.element.blur(function() {
				$(this).data('p8-inputElem-value', $(this).attr('value'));
			});
			
			this.element.focus(function() {
				$(this).valid();
			});

			this.initRules();
		},
		initRules: function() {
			if(this.options.rules !== undefined)
				this.element.rules("add", this.options.rules);
		}
	};

	$.widget("ui.p8ValueFieldT", p8ValueFieldT);



var p8TypeSelect = {
	options: {
		data: {},
		typeField: 'type',
		customValueField: 'value2',
		types: null,
		prefix: '',
		title:'',
		cclass: '',
		i180nPackage: 'contactTypes'
	},
	_create: function() {
		this.element.addClass("p8-inputElem");
		this.element.addClass("p8-typeSelect");
		
		var value = this.options.data[this.options.typeField];
		var rvalue = this.options.data[this.options.typeField];
		if(value == 'CUSTOM') {
			value = this.options.data[this.options.customValueField];
			rvalue = this.options.data[this.options.customValueField];
		}
		else if(value === undefined) {
			value = $.i18n(this.options.i180nPackage, this.options.types[0]);
		}
		else {
			rvalue = value;
			value = $.i18n(this.options.i180nPackage, value);
		}
		
		if(value === undefined)
			value = "";
		
		
		var types = this.options.types;
		var source = new Array();
		var that = this;
		$.each(types, function(index, value) { 
			source.push($.i18n(that.options.i180nPackage, value));
		});
			
		var input = $('<input />').
			attr('value', value).
			attr('name', this.options.prefix+'-'+this.options.typeField).
			addClass(this.options.cclass).
			autocomplete({
				source: source,
				zIndex: 99,
				minLength: 0
			}).
			p8InfoTip();
		
		
		var button = $('<a/>').button({
			label:'Open', 
			text:false, 
			icons: {primary:'ui-icon-circle-triangle-s'}});
		button.click(function() {
			input.autocomplete("search", '');
			input.focus();
		});
		this.element.append(input).append(button);
		
		
		
		this.element
		.change(addDirty)
		.bind('autocompletechange', addDirty)
		.bind('autocompleteselect', addDirty)
		.bind('autocompleteselect', function(event, selected) {
			input.data('p8-inputElem-value',  $.i18n(that.options.i180nPackage, selected.item.value));
		});
		
		if(rvalue) {
			input.data('p8-inputElem-orgValue', rvalue);
			input.data('p8-inputElem-value', rvalue);
		}
		
		input.focus(function() {
			$(this).valid();
		})
		.change(function() {
			$(this).data('p8-inputElem-value', $.i18n(that.options.i180nPackage, $(this).attr('value')));
		});
		
		
	},
	initRules: function() {
		if(this.options.rules !== undefined) {
			var input = this.element.find("input");
			input.rules("add", this.options.rules);
		}
		
	}
};

$.widget("ui.p8TypeSelect", p8TypeSelect);

}(jQuery));


