</div>
  
  <div id="footer" style="">
    <div class="footerCont" style="position:relative; top:5px;height: 20px">
      <span style="">powered by <a href="https://plus.google.com/113880730306243229744/" target="_blank">Photography Stream</a></span>
    </div>
  </div>
  
  
 </div>

  <div id="workerCont"  style="display:none;">
  <script type="text/x-handlebars">
    <div id="datastoreEdit">
      {{#view App.FormView className="datastore-editor-form" tagName="form"}}
      <table>
        <tbody>  
          <tr><td>Title: </td><td>{{view App.TextField name="title" title="Title" valueBinding="App.datastoresController.editCopy.title"}}</td></tr>
          <tr><td>Key: </td><td>{{App.datastoresController.editCopy.key}}</td></tr>
        </tbody>
      </table>
      {{/view}}
    </div>
  </script>
  
  <script type="text/x-handlebars">
    <div id="galleryEdit">
      {{#view App.FormView className="gallery-editor-form" tagName="form"}}
      <table width="100%">
        <tbody>  
          <tr><td><label>Key: </label></td><td>{{App.galleriesController.editCopy.key}}</td></tr>
          <tr><td><label>Kind: </label></td><td>{{App.galleriesController.editCopy.kind}}</td></tr>
          <tr><td><label>Reference: </label></td><td>{{App.galleriesController.editCopy.ref}}</td></tr>
          <tr><td><label>Title: </label></td><td>{{view App.TextField style="width:400px" name="title" title="Title" valueBinding="App.galleriesController.editCopy.title"}}</td></tr>
          <tr><td colspan="2">&nbsp;</td></tr>
          <tr><td colspan="2"><label>Description: </label></td></tr>
          <tr><td colspan="2">{{view Ember.TextArea style="width:400px" valueBinding="App.galleriesController.editCopy.desc"}}</td></tr>
        </tbody>
      </table>
      {{/view}}
    </div>
  </script>
  
  
  <script type="text/x-handlebars">
    <div id="galleryNewDropbox">
      {{#view App.FormView className="gallery-dropbox-new-form" tagName="form"}}
      <table width="100%">
        <tbody>  
          <tr><td><label>DropboxId: </label></td><td>{{App.galleriesController.newModel.dropboxUid}}</td></tr>
          <tr><td><label>Path: </label></td><td>{{view App.TextField style="width:400px" name="path" title="Path" valueBinding="App.galleriesController.newModel.path"}}</td></tr>
          <tr><td><label>Title: </label></td><td>{{view App.TextField style="width:400px" name="title" title="Title" valueBinding="App.galleriesController.newModel.title"}}</td></tr>
          <tr><td colspan="2">&nbsp;</td></tr>
          <tr><td colspan="2"><label>Description: </label></td></tr>
          <tr><td colspan="2">{{view Ember.TextArea style="width:400px" valueBinding="App.galleriesController.newModel.desc"}}</td></tr>
        </tbody>
      </table>
      {{/view}}
    </div>
  </script>
  
  
  <script type="text/x-handlebars">
    <div id="galleryNewGDrive">
      {{#view App.FormView className="gallery-gdrive-new-form" tagName="form"}}
      <table width="100%">
        <tbody>  
          <tr><td><label>Google: </label></td><td>{{App.galleriesController.newModel.googleId}}</td></tr>
          <tr><td><label>Path: </label></td><td>{{view App.TextField style="width:400px" name="path" title="Path" valueBinding="App.galleriesController.newModel.path"}}</td></tr>
          <tr><td><label>Title: </label></td><td>{{view App.TextField style="width:400px" name="title" title="Title" valueBinding="App.galleriesController.newModel.title"}}</td></tr>
          <tr><td colspan="2">&nbsp;</td></tr>
          <tr><td colspan="2"><label>Description: </label></td></tr>
          <tr><td colspan="2">{{view Ember.TextArea style="width:400px" valueBinding="App.galleriesController.newModel.desc"}}</td></tr>
        </tbody>
      </table>
      {{/view}}
    </div>
  </script>

  <script type="text/x-handlebars">
    <div id="contentGroupEdit">
      <div class="contentGroup-editor">
        {{#view App.FormView contentBinding="App.contentGroupsController" className="contentGroup-editor-form" tagName="form"}}
          <table width="100%">
            <tbody>  
              <tr><td>Id: </td><td>{{view.content.editCopy.groupId}}</td></tr>
              <tr><td>Title: </td><td>{{view App.TextField name="title" title="Title" valueBinding="view.content.editCopy.title"}}</td></tr>
            </tbody>
          </table>
        {{/view}}
      </div>
      
      {{#view contentBinding="App.simpleContentEditorController.content" classNames="contentGroup-editor-list"}}
        <table>
          <tbody>
            {{#each view.content}}
              {{#view App.simpleContentEditorView contentBinding="this" tagName="tr"}}
                <td>{{this.sort}}&nbsp;<td>
                <td>{{this.title}}</td><td>{{this.groupId}}</td>
                <td>[<a href="#" {{action "edit"}}>edit</a>]</td>
              {{/view}}
            {{/each}}
          </tbody>
        </table>
      {{/view}}
      
      {{#view App.simpleContentEditorView classNames="simplecontent-editor"}}
      {{#view App.FormView contentBinding="App.simpleContentEditorController" className="simplecontent-editor-form" tagName="form"}}
      <table width="100%">
        <tbody>
          <tr><td>Title: </td><td>{{view App.TextField name="title" title="Title" valueBinding="view.content.editCopy.title"}}</td></tr>
          <tr><td>Sort: </td><td>{{view.content.editCopy.sort}}</td></tr>
          <tr><td colspan="2">&nbsp;</td></tr>
          <tr><td colspan="2"><label>Content: </label></td></tr>
          <tr><td colspan="2">{{view App.TextArea classNames="contentField" valueBinding="view.content.editCopy.content"}}</td></tr>
          <tr><td colspan="2">&nbsp;</td></tr>
          <tr><td colspan="2">{{#view App.SaveModelButton modelBinding="App.simpleContentEditorController" target="view.content" action="updateCurrent" elementStyle="{'background-color':'black'}"}}Store content{{/view}}</td></tr>
        </tbody>
      </table>
      {{/view}}
      {{/view}}
      
    </div>
  </script>
  
  
  
  <script type="text/x-handlebars">
    <div id="contentGroupNew">
      <div class="contentGroup-new">
        {{#view App.FormView contentBinding="App.contentGroupsController" className="contentGroup-editor-form" tagName="form"}}
          <table width="100%">
            <tbody>  
              <tr><td>Id: </td><td>{{view App.TextField name="groupId" title="Id" valueBinding="view.content.editCopy.groupId"}}</td></tr>
              <tr><td>Title: </td><td>{{view App.TextField name="title" title="Title" valueBinding="view.content.editCopy.title"}}</td></tr>
            </tbody>
          </table>
        {{/view}}
      </div> 
    </div>
  </script>

</div>
</body>
</html>
