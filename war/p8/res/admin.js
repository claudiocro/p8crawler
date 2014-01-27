/**
 * Version 0.5.1
 */

window.App = Ember.Application.create();



App.DatastoreDto = Ember.Object.extend(Ember.Copyable,{
	key: null,
	kind: null,
	uid: null,
	title: null,
	copy: function(deep) {
		return App.DatastoreDto.create().setProperties(this.getProperties([
			'key','kind','uid','title'
		]));
	}
});




App.datastoresController = Ember.ArrayProxy.create({
	content: [],
	
	current: null,
	editCopy: null,
	
	load: function() {
		var self = this;
		var jp = {t:"list"};
		self.clear();
		jQuery.getJSON("/p8admin/client/datastore", jp, function(data){
			for ( var i=0; i<data.response.length; ++i ){
				var d = App.DatastoreDto.create(data.response[i]);
				self.pushObject(d);
			}
		});
	},
	edit: function(datastore) {
		this.set("current", datastore);
		this.set("editCopy", Ember.copy(datastore));
	},
	cancelEdit: function(postFunc) {
		this.set("current", null);
		this.set("editCopy", null);
		postFunc();
	},
	updateCurrent: function(postFunc) {
		var self = this;
		$.post("/p8admin/client/datastore", {store:1,datastore:$.toJSON(App.datastoresController.get("editCopy"))}, function(data) {
			self.get('current').setProperties(data.response);
			self.set('editCopy', null);
			postFunc();
		}, "json");		
		
	},
	dropboxDatastores: function() {
		return this.get("content").filterProperty("kind",'dropbox:user');
	}.property('content.@each'),
	
	googleDatastores: function() {
		return this.get("content").filterProperty("kind",'google:user');
	}.property('content.@each')
});

App.DatastoreSingleView = Ember.View.extend({
	createNewDropbox: function() {
		window.open("/dropbox/dropboxSyncher?createToken=1", "createNewDropbox");
		return false;
	},
	createNewGoogle: function() {
		window.open("/gdrive/createDatastore", "createNewGoogle");
		return false;
	},
	edit: function() {
		var self = this;

		console.log("edit");
		App.datastoresController.edit(this.get('content'));

		$("#datastoreEdit").dialog({
			buttons: 
				{ "Ok": function() {
					var dialogSelf = this;
					if($("form").valid()) {
						App.datastoresController.updateCurrent(function() {
							$(dialogSelf).dialog("close");
							$(dialogSelf).dialog("destroy");
						});
					}
				}, "Cancel": function() {
					var dialogSelf = this;
					App.datastoresController.cancelEdit(function() {
						$(dialogSelf).dialog("close");
						$(dialogSelf).dialog("destroy");
					});
				}},
			title: "Edit datastore",
			position: ["center",100],
			modal: true,
			width:430
		});
		
		return false;
	}
});



App.GalleryDto = Ember.Object.extend(Ember.Copyable,{
	key: null,
	kind: null,
	ref: null,
	title: null,
	desc: null,
	copy: function(deep) {
		return App.GalleryDto.create().setProperties(this.getProperties([
			'key','kind','ref','title','desc'
		]));
	}
});

App.NewDropboxGalleryDto = Ember.Object.extend(Ember.Copyable,{
	datastore: null,
	path: null,
	title: null,
	desc: null,
	copy: function(deep) {
		return App.NewDropboxGalleryDto.create().setProperties(this.getProperties([
			'datastore','path','title','desc'
		]));
	}
});

App.NewGDriveGalleryDto = Ember.Object.extend(Ember.Copyable,{
	datastore: null,
	path: null,
	title: null,
	desc: null,
	copy: function(deep) {
		return App.NewGDriveGalleryDto.create().setProperties(this.getProperties([
			'datastore','path','title','desc'
		]));
	}
});

App.galleriesController = Ember.ArrayProxy.create({
	content: [],
	
	current: null,
	editCopy: null,
	newModel: null,
	
	load: function() {
		var self = this;
		var jp = {t:"list"};
		self.clear();			
		jQuery.getJSON("/p8admin/client/gallery", jp, function(data){
			for ( var i=0; i<data.response.length; ++i ){
				var g=data.response[i]; 
				g = App.GalleryDto.create({ key:g.key, kind:g.kind, ref:g.ref, title:g.title, desc:g.desc });
				self.pushObject(g);
			}
		});
	},
	createNew: function() {
		this.set("newModel", null);
		this.set("current", null);
		this.set("editCopy", App.ContentGroupDto.create({ }));
	},
	newDropbox: function() {
		this.set("newModel", App.NewDropboxGalleryDto.create()); //was {dropboxUid: 5031239}
		this.set("editCopy", null);
		this.set("current", null);
	},
	newGDrive: function() {
		this.set("newModel", App.NewGDriveGalleryDto.create()); //was {googleId: 'be393ccf-f8ef-47ce-8f25-78590cc1e12b'}
		this.set("editCopy", null);
		this.set("current", null);
	},
	edit: function(gallery) {
		this.set("newModel", null);
		this.set("current", gallery);
		this.set("editCopy", Ember.copy(gallery));
	},
	cancelEdit: function(postFunc) {
		this.set("newModel", null);
		this.set("current", null);
		this.set("editCopy", null);
		postFunc();
	},
	updateCurrent: function(postFunc) {
		var self = this;
		$.post("/p8admin/client/gallery", {store:1,gallery:$.toJSON(self.get("editCopy"))}, function(data) {
			self.get('current').setProperties(data.response);
			self.set('editCopy', null);
			postFunc();
		}, "json");		
		
	},
	updateNewModel: function(postFunc) {
		var self = this;
		if(self.get("newModel.datastore.kind") === 'dropbox:user') {
			$.post("/dropbox/dropboxSyncher", {
					createAlbum:1,
					dropboxUid:self.get("newModel.datastore.key"),
					path:self.get("newModel.path"),
					title:self.get("newModel.title"),
					desc:self.get("newModel.desc")
				}, 
				function(data) {
					self.set('newModel', null);
					self.load();
					postFunc();
				},
				"json");		
		}
		else if(self.get("newModel.datastore.kind") === 'google:user') {
			$.post("/gdrive/createGallery",{
					createAlbum:1,
					googleUid:self.get("newModel.datastore.key"),
					path:self.get("newModel.path"),
					title:self.get("newModel.title"),
					desc:self.get("newModel.desc")
				}, 
				function(data) {
					self.set('newModel', null);
					self.load();
					postFunc();
				},
				"json");		
		}
	}
});


App.GallerySingleView = Ember.View.extend({
	showFeeds: function() {
		App.feedItemsController.load(this.get('content').key);
		App.pageState.goToState("showingFeedListPage");
		return false;
	},
	edit: function() {
		var self = this;

		App.galleriesController.edit(this.get('content'));

		$("#galleryEdit").dialog({
			buttons: 
				{ "Ok": function() {
					var dialogSelf = this;
					if($("form").valid()) {
						App.galleriesController.updateCurrent(function() {
							$(dialogSelf).dialog("close");
							$(dialogSelf).dialog("destroy");
						});
					}
				}, "Cancel": function() {
					var dialogSelf = this;
					App.galleriesController.cancelEdit(function() {
						$(dialogSelf).dialog("close");
						$(dialogSelf).dialog("destroy");
					});
				}},
			title: "Edit gallery",
			position: ["center",100],
			modal: true,
			width:430
		});
		
		return false;
	},
	newDropboxGallery: function() {
		var self = this;

		App.galleriesController.newDropbox();

		$("#galleryNewDropbox").dialog({
			buttons: 
				{ "Ok": function() {
					var dialogSelf = this;
					if($("form").valid()) {
						App.galleriesController.updateNewModel(function() {
							$(dialogSelf).dialog("close");
							$(dialogSelf).dialog("destroy");
						});
					}
				}, "Cancel": function() {
					var dialogSelf = this;
					App.galleriesController.cancelEdit(function() {
						$(dialogSelf).dialog("close");
						$(dialogSelf).dialog("destroy");
					});
				}},
			title: "Edit gallery",
			position: ["center",100],
			modal: true,
			width:430
		});
		
		return false;
	},
	
	newGDriveGallery : function() {
		var self = this;

		App.galleriesController.newGDrive();

		$("#galleryNewGDrive").dialog({
			buttons: 
				{ "Ok": function() {
					var dialogSelf = this;
					if($("form").valid()) {
						App.galleriesController.updateNewModel(function() {
							$(dialogSelf).dialog("close");
							$(dialogSelf).dialog("destroy");
						});
					}
				}, "Cancel": function() {
					var dialogSelf = this;
					App.galleriesController.cancelEdit(function() {
						$(dialogSelf).dialog("close");
						$(dialogSelf).dialog("destroy");
					});
				}},
			title: "Edit gallery",
			position: ["center",100],
			modal: true,
			width:430
		});
		
		return false;
	}
});













App.ContentGroupDto = Ember.Object.extend(Ember.Copyable,{
	key: null,
	kind: null,
	groupId: null,
	title: null,
	copy: function(deep) {
		return App.ContentGroupDto.create().setProperties(this.getProperties([
			'key','kind','groupId','title'
		]));
	}
});


App.contentGroupsController = Ember.ArrayProxy.create({
	content: [],
	
	current: null,
	editCopy: null,
	
	load: function() {
		debugger;
		var self = this;
		var jp = {t:"list"};
		self.clear();			
		jQuery.getJSON("/p8admin/client/contentgroup", jp, function(data){
			for ( var i=0; i<data.response.length; ++i ){
				var g=data.response[i]; 
				g = App.ContentGroupDto.create({ key:g.key, kind:g.kind, groupId:g.groupId, title:g.title});
				self.pushObject(g);
			}
		});
	},
	createNew: function() {
		this.set("current", null);
		this.set("editCopy", App.SimpleContentDto.create({ }));
	},
	edit: function(contentGroup) {
		debugger;
		this.set("current", contentGroup);
		this.set("editCopy", Ember.copy(contentGroup));
	},
	cancelEdit: function(postFunc) {
		this.set("current", null);
		this.set("editCopy", null);
		postFunc();
	},
	updateCurrent: function(postFunc) {
		var self = this;
		$.post("/p8admin/client/contentgroup", {store:1,pageGroup:$.toJSON(self.get("editCopy"))}, function(data) {
			if(self.get('current') != null) {
				self.get('current').setProperties(data.response);
			} else {
				var g = App.ContentGroupDto.create({ key:data.response.key, kind:data.response.kind, groupId:data.response.groupId, title:data.response.title});
				self.pushObject(g);
			}
			self.set('editCopy', null);
			postFunc();
		}, "json");		
		
	}
});




App.ContentGroupSingleView = Ember.View.extend({
	createNewContentGroup: function() {
		var self = this;

		App.contentGroupsController.createNew();
		
		$("#contentGroupNew").dialog({
			buttons: 
				{ "Ok": function() {
					var dialogSelf = this;
					if($("form").valid()) {
						App.contentGroupsController.updateCurrent(function() {
							$(dialogSelf).dialog("close");
							$(dialogSelf).dialog("destroy");
						});
					}
				}, "Cancel": function() {
					var dialogSelf = this;
					App.contentGroupsController.cancelEdit(function() {
						$(dialogSelf).dialog("close");
						$(dialogSelf).dialog("destroy");
					});
				}},
			title: "New contentgroup",
			position: ["center",100],
			modal: true,
			width:250
		});
		return false;
		return false;
	},
	edit: function() {
		var self = this;

		App.contentGroupsController.edit(this.get('content'));
		
		$("#contentGroupEdit").dialog({
			buttons: 
				{ "Ok": function() {
					var dialogSelf = this;
					if($("form").valid()) {
						App.contentGroupsController.updateCurrent(function() {
							$(dialogSelf).dialog("close");
							$(dialogSelf).dialog("destroy");
						});
					}
				}, "Cancel": function() {
					var dialogSelf = this;
					App.contentGroupsController.cancelEdit(function() {
						$(dialogSelf).dialog("close");
						$(dialogSelf).dialog("destroy");
					});
				}},
			title: "Edit contentgroup",
			position: ["center",100],
			modal: true,
			width:800
		});
		return false;
	},
	editPage: function() {
		App.simpleContentEditorController.edit(this.get('content'));
	}
});



App.SimpleContentDto = Ember.Object.extend(Ember.Copyable,{
	key: null,
	kind: null,
	group: null,
	title: null,
	image: null,
	sort: null,
	menu1_idx: null,
	content: null,
	copy: function(deep) {
		return App.SimpleContentDto.create().setProperties(this.getProperties([
			'key','kind','group','title','image','sort','menu1_idx','content'
		]));
	}
});



App.simpleContentEditorController = Ember.ArrayProxy.create({
	content: [],
	
	current: null,
	editCopy: null,
	
	load: function(contentGroup) {
		var self = this;
		self.clear();			
		if(contentGroup != null) {
			var jp = {t:"list",group:contentGroup.get("groupId")};
			jQuery.getJSON("/p8admin/client/simplecontent", jp, function(data){
				for ( var i=0; i<data.response.length; ++i ){
					var g=data.response[i]; 
					g = App.SimpleContentDto.create({ key:g.key, kind:g.kind, group:g.group, title:g.title, sort:g.sort, title:g.title, menu1_idx:g.menu1_idx, content:g.content});
					self.pushObject(g);
				}
			});
		}
	},
	
	edit: function(contentGroup) {
		this.set("current", contentGroup);
		this.set("editCopy", Ember.copy(contentGroup));
	},
	cancelEdit: function(postFunc) {
		this.set("current", null);
		this.set("editCopy", null);
		postFunc();
	},
	updateCurrent: function(postFunc) {
		var self = this;
		$.post("/p8admin/client/simplecontent", {store:1,pageGroup:$.toJSON(self.get("editCopy"))}, function(data) {
			if(self.get('current') != null) {
				self.get('current').setProperties(data.response);
			} else {
				var g = App.SimpleContentDto.create({ key:data.response.key, kind:data.response.kind, groupId:data.response.groupId, title:data.response.title});
				self.pushObject(g);
			}
			self.set('editCopy', null);
			postFunc();
		}, "json");		
		
	}
});



App.simpleContentEditorView = Ember.View.extend({
	edit: function() {
		var self = this;
		App.simpleContentEditorController.edit(this.get('content'));
		return false;
	},
	updateCurrent: function() {
		var self = this;
		console.log("before");
		App.simpleContentEditorController.updateCurrent(function() {
			console.log("done");
		});
		return false;
	}
});

App.contentGroupsController.addObserver('current', function(a,b,c,d,e) {
	App.simpleContentEditorController.load(this.get('current'));
});



App.FeedItemDto = Ember.Object.extend(Ember.Copyable,{
	key: null,
	kind: null,
	link: null,
	publishedDate: null,
	source: null,
	author: null,
	title: null,

	imageLink: null,
	img1A: null,
	authorName: null,
	authorLink: null,
	img1Link: null,
	img2Link: null,
	storeDate: null,
	copy: function(deep) {
		return App.SimpleContentDto.create().setProperties(this.getProperties([
			'key','kind','link','publishedDate','source','author','title','imageLink',
			'img1A','authorName','authorLink','img1Link','img2Link','storeDate'
		]));
	}
});



App.feedItemsController = Ember.ArrayProxy.create({
	content: [],
	
	current: null,
	previousOffset: function() {
		return this.get("offset")-30;
	}.property("offset"),
	nextOffset: function() {
		return this.get("offset")+30;
	}.property("offset"),
	offset: 0,
	cat: null,
	
	goNext: function() {
		this.set("offset", this.get("nextOffset"));
		this.load(this.get("cat"));
		return false;
	},
	goPrevious: function() {
		this.set("offset", this.get("previousOffset"));
		this.load(this.get("cat"));
		return false;
	},
	reparseItem: function(item) {
		var self = this;
		self.set("current", item);
		$.post("/p8admin/client/feedprocessor", {reparse:1,key:item.key}, function(data) {
			self.get('current').setProperties(data);
		}, "json");		
		
	},
	
	load: function(pcat) {
		var self = this;
		self.set("cat", pcat);
		var jp = {cat:pcat,offset:self.get("offset")};
		self.clear();
		jQuery.getJSON("/p8admin/client/feeditem", jp, function(data){
			for ( var i=0; i<data.response.length; ++i ){
				var d = App.FeedItemDto.create(data.response[i]);
				self.pushObject(d);
			}
		});
	}
});


App.FeedItemSingleView = Ember.View.extend({
	reparse: function() {
		var self = this;
		App.feedItemsController.reparseItem(this.get('content'));
		return false;
	}
});




Handlebars.registerHelper('urlparam', function(type,value,fn) {
	var context = (fn.contexts && fn.contexts[1]) || this;
	value = Ember.get(context, value, fn);
	return type+'='+value;
});

Handlebars.registerHelper('userparam', function(type) {
	return type+'='+App.User.id;
});





App.FormView = Ember.View.extend({
	didInsertElement: function() {
    	this.$().validate({showErrors:errorHandler,ignoreTitle:true });
    }
});

App.TextField = Ember.TextField.extend({
	name: "",
	title: "",
	didInsertElement: function() {
		this.$().p8ValueFieldT({
			title: this.title,
			valueField: this.name,
			rules: { 
				minlength: 2,
				required: true,
			}
		})
    }
});


App.TextArea = Ember.TextArea.extend({
	attributeBindings: ['rows', 'cols','wrap'],
	wrap: "off"
});

App.Button = Ember.View.extend({
	didInsertElement: function() {
		this.$().button();
		
		this.addObserver('disabled', function(a,b,c,d,e) {
			this.$().button( "option" , "disabled" , c);
		});
	}
});

App.SaveModelButton = App.Button.extend({
    //disabledBinding: Ember.Binding.isNull('model.editCopy')
	updateCurrent: function() {
		App.simpleContentEditorController.updateCurrent();
	}
});


var errorHandler = function (errorMap, errorList) {
	 for ( var i = 0; errorList[i]; i++ ) {
		 var error = errorList[i];
		 if(i == 0) {
			 var infoTip = $(error.element).data('p8InfoTip')
			 if(infoTip != null) {
				 infoTip.show(error.message, 3000);
			 }
			 if(errorList.length > 1)
				 error.element.focus();
		 }
		 
		 if(this.settings.highlight) {
			 this.settings.highlight.call( 
				 this, 
				 error.element, 
				 this.settings.errorClass, 
				 this.settings.validClass );
		 }
	 }
	 
	 
	 if (this.settings.unhighlight) {
		 for ( var i = 0, elements = this.validElements(); elements[i]; i++ ) {
			 this.settings.unhighlight.call( this, elements[i], this.settings.errorClass, this.settings.validClass );
			 var infoTip = $(elements[i]).data('p8InfoTip');
			 if(infoTip != null)
				 infoTip.hide();
		 }
	 }
};



App.navigationHandler = Ember.Object.create({
	showWelcomePage: function() {
		App.pageState.goToState("showingWelcomePage");
		return false;
	}
});




App.pageState = Ember.StateManager.create({
	rootElement: '#mainNavigationContent',
  
	showingWelcomePage: Ember.ViewState.create({
		view: Ember.ContainerView.create({
			didInsertElement: function() {
				this.$().find('div').p8CreateAutoLoadFeedItem({
					showBusy: true
				});
				this.$().p8SimpleGrid({totalInRow:2});
			},
			elementId: 'welcomePage',
		    childViews: ['galleryView', Ember.View.create(), 'datastoreView', Ember.View.create(), 'contentGroupView', Ember.View.create(), Ember.View.create(), Ember.View.create()],
		    galleryView: Ember.View.extend({
		    	elementId: 'galleriesCnt',
		    	templateName: 'galleries',
	  			contentBinding: 'App.galleriesController.content'
		    }),
		    datastoreView: Ember.View.create({
		    	elementId: 'datastoresCnt',
		    	templateName: 'datastores',
		    	contentBinding: 'App.datastoresController.content'
		    }),
		    contentGroupView: Ember.View.extend({
		    	elementId: 'contentGroupsCnt',
		    	templateName: 'contentGroups',
	  			contentBinding: 'App.contentGroupsController.content'
		    })
		})
	}),
	
	showingFeedListPage: Ember.ViewState.create({
		view: Ember.View.extend({
	    	elementId: 'feedListPage',
	    	templateName: 'feedItems',
  			contentBinding: 'App.feedItemsController.content'
	    })
	})
});

setTimeout(function() {
	App.pageState.goToState("showingWelcomePage");
}, 50);















/*
 * jQuery JSON Plugin
 * version: 2.1 (2009-08-14)
 *
 * This document is licensed as free software under the terms of the
 * MIT License: http://www.opensource.org/licenses/mit-license.php
 *
 * Brantley Harris wrote this plugin. It is based somewhat on the JSON.org 
 * website's http://www.json.org/json2.js, which proclaims:
 * "NO WARRANTY EXPRESSED OR IMPLIED. USE AT YOUR OWN RISK.", a sentiment that
 * I uphold.
 *
 * It is also influenced heavily by MochiKit's serializeJSON, which is 
 * copyrighted 2005 by Bob Ippolito.
 */
 
(function($) {
    /** jQuery.toJSON( json-serializble )
        Converts the given argument into a JSON respresentation.

        If an object has a "toJSON" function, that will be used to get the representation.
        Non-integer/string keys are skipped in the object, as are keys that point to a function.

        json-serializble:
            The *thing* to be converted.
     **/
    $.toJSON = function(o)
    {
        if (typeof(JSON) == 'object' && JSON.stringify)
            return JSON.stringify(o);
        
        var type = typeof(o);
    
        if (o === null)
            return "null";
    
        if (type == "undefined")
            return undefined;
        
        if (type == "number" || type == "boolean")
            return o + "";
    
        if (type == "string")
            return $.quoteString(o);
    
        if (type == 'object')
        {
            if (typeof o.toJSON == "function") 
                return $.toJSON( o.toJSON() );
            
            if (o.constructor === Date)
            {
                var month = o.getUTCMonth() + 1;
                if (month < 10) month = '0' + month;

                var day = o.getUTCDate();
                if (day < 10) day = '0' + day;

                var year = o.getUTCFullYear();
                
                var hours = o.getUTCHours();
                if (hours < 10) hours = '0' + hours;
                
                var minutes = o.getUTCMinutes();
                if (minutes < 10) minutes = '0' + minutes;
                
                var seconds = o.getUTCSeconds();
                if (seconds < 10) seconds = '0' + seconds;
                
                var milli = o.getUTCMilliseconds();
                if (milli < 100) milli = '0' + milli;
                if (milli < 10) milli = '0' + milli;

                return '"' + year + '-' + month + '-' + day + 'T' +
                             hours + ':' + minutes + ':' + seconds + 
                             '.' + milli + 'Z"'; 
            }

            if (o.constructor === Array) 
            {
                var ret = [];
                for (var i = 0; i < o.length; i++)
                    ret.push( $.toJSON(o[i]) || "null" );

                return "[" + ret.join(",") + "]";
            }
        
            var pairs = [];
            for (var k in o) {
                var name;
                var type = typeof k;

                if (type == "number")
                    name = '"' + k + '"';
                else if (type == "string")
                    name = $.quoteString(k);
                else
                    continue;  //skip non-string or number keys
            
                if (typeof o[k] == "function") 
                    continue;  //skip pairs where the value is a function.
            
                var val = $.toJSON(o[k]);
            
                pairs.push(name + ":" + val);
            }

            return "{" + pairs.join(", ") + "}";
        }
    };

    /** jQuery.evalJSON(src)
        Evaluates a given piece of json source.
     **/
    $.evalJSON = function(src)
    {
        if (typeof(JSON) == 'object' && JSON.parse)
            return JSON.parse(src);
        return eval("(" + src + ")");
    };
    
    /** jQuery.secureEvalJSON(src)
        Evals JSON in a way that is *more* secure.
    **/
    $.secureEvalJSON = function(src)
    {
        if (typeof(JSON) == 'object' && JSON.parse)
            return JSON.parse(src);
        
        var filtered = src;
        filtered = filtered.replace(/\\["\\\/bfnrtu]/g, '@');
        filtered = filtered.replace(/"[^"\\\n\r]*"|true|false|null|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?/g, ']');
        filtered = filtered.replace(/(?:^|:|,)(?:\s*\[)+/g, '');
        
        if (/^[\],:{}\s]*$/.test(filtered))
            return eval("(" + src + ")");
        else
            throw new SyntaxError("Error parsing JSON, source is not valid.");
    };

    /** jQuery.quoteString(string)
        Returns a string-repr of a string, escaping quotes intelligently.  
        Mostly a support function for toJSON.
    
        Examples:
            >>> jQuery.quoteString("apple")
            "apple"
        
            >>> jQuery.quoteString('"Where are we going?", she asked.')
            "\"Where are we going?\", she asked."
     **/
    $.quoteString = function(string)
    {
        if (string.match(_escapeable))
        {
            return '"' + string.replace(_escapeable, function (a) 
            {
                var c = _meta[a];
                if (typeof c === 'string') return c;
                c = a.charCodeAt();
                return '\\u00' + Math.floor(c / 16).toString(16) + (c % 16).toString(16);
            }) + '"';
        }
        return '"' + string + '"';
    };
    
    var _escapeable = /["\\\x00-\x1f\x7f-\x9f]/g;
    
    var _meta = {
        '\b': '\\b',
        '\t': '\\t',
        '\n': '\\n',
        '\f': '\\f',
        '\r': '\\r',
        '"' : '\\"',
        '\\': '\\\\'
    };
})(jQuery);
