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
      {{#view App.FormView contentBinding="App.datastoresController" className="datastore-editor-form" tagName="form"}}
      <table>
        <tbody>  
          <tr><td>Title: </td><td>{{view App.TextField name="title" title="Title" valueBinding="content.editCopy.title"}}</td></tr>
          <tr><td>Key: </td><td>{{content.editCopy.key}}</td></tr>
        </tbody>
      </table>
      {{/view}}
    </div>
  </script>
  
  <script type="text/x-handlebars">
    <div id="galleryEdit">
      {{#view App.FormView contentBinding="App.galleriesController" className="gallery-editor-form" tagName="form"}}
      <table width="100%">
        <tbody>  
          <tr><td><label>Key: </label></td><td>{{content.editCopy.key}}</td></tr>
          <tr><td><label>Kind: </label></td><td>{{content.editCopy.kind}}</td></tr>
          <tr><td><label>Reference: </label></td><td>{{content.editCopy.ref}}</td></tr>
          <tr><td><label>Title: </label></td><td>{{view App.TextField style="width:400px" name="title" title="Title" valueBinding="content.editCopy.title"}}</td></tr>
          <tr><td colspan="2">&nbsp;</td></tr>
          <tr><td colspan="2"><label>Description: </label></td></tr>
          <tr><td colspan="2">{{view Ember.TextArea style="width:400px" valueBinding="content.editCopy.desc"}}</td></tr>
        </tbody>
      </table>
      {{/view}}
    </div>
  </script>
  
  
  <script type="text/x-handlebars">
    <div id="galleryNewDropbox">
      {{#view App.FormView contentBinding="App.galleriesController" className="gallery-dropbox-new-form" tagName="form"}}
      <table width="100%">
        <tbody>  
          <tr><td><label>DropboxId: </label></td><td>{{content.newModel.dropboxUid}}</td></tr>
          <tr><td><label>Path: </label></td><td>{{view App.TextField style="width:400px" name="path" title="Path" valueBinding="content.newModel.path"}}</td></tr>
          <tr><td><label>Title: </label></td><td>{{view App.TextField style="width:400px" name="title" title="Title" valueBinding="content.newModel.title"}}</td></tr>
          <tr><td colspan="2">&nbsp;</td></tr>
          <tr><td colspan="2"><label>Description: </label></td></tr>
          <tr><td colspan="2">{{view Ember.TextArea style="width:400px" valueBinding="content.newModel.desc"}}</td></tr>
        </tbody>
      </table>
      {{/view}}
    </div>
  </script>
  
  
  <script type="text/x-handlebars">
    <div id="galleryNewGDrive">
      {{#view App.FormView contentBinding="App.galleriesController" className="gallery-gdrive-new-form" tagName="form"}}
      <table width="100%">
        <tbody>  
          <tr><td><label>Google: </label></td><td>{{content.newModel.googleId}}</td></tr>
          <tr><td><label>Path: </label></td><td>{{view App.TextField style="width:400px" name="path" title="Path" valueBinding="content.newModel.path"}}</td></tr>
          <tr><td><label>Title: </label></td><td>{{view App.TextField style="width:400px" name="title" title="Title" valueBinding="content.newModel.title"}}</td></tr>
          <tr><td colspan="2">&nbsp;</td></tr>
          <tr><td colspan="2"><label>Description: </label></td></tr>
          <tr><td colspan="2">{{view Ember.TextArea style="width:400px" valueBinding="content.newModel.desc"}}</td></tr>
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
              <tr><td>Id: </td><td>{{content.editCopy.groupId}}</td></tr>
              <tr><td>Title: </td><td>{{view App.TextField name="title" title="Title" valueBinding="content.editCopy.title"}}</td></tr>
            </tbody>
          </table>
        {{/view}}
      </div>
      
      <div class="contentGroup-editor-list">
      <table>
        <tbody>
          {{#each App.simpleContentEditorController}}
            {{#view App.simpleContentEditorView contentBinding="this" tagName="tr"}}
              <td>{{content.sort}}&nbsp;<td>
              <td>{{content.title}}</td><td>{{content.groupId}}</td>
              <td>[<a href="#" {{action "edit"}}>edit</a>]</td>
            {{/view}}
          {{/each}}
        </tbody>
      </table>
      </div>
      
      {{#view App.simpleContentEditorView contentBinding="App.simpleContentEditorController" classNames="simplecontent-editor"}}
      {{#view App.FormView contentBinding="content" className="simplecontent-editor-form" tagName="form"}}
      <table width="100%">
        <tbody>
          <tr><td>Title: </td><td>{{view App.TextField name="title" title="Title" valueBinding="content.editCopy.title"}}</td></tr>
          <tr><td>Sort: </td><td>{{content.editCopy.sort}}</td></tr>
          <tr><td colspan="2">&nbsp;</td></tr>
          <tr><td colspan="2"><label>Content: </label></td></tr>
          <tr><td colspan="2">{{view App.TextArea classNames="contentField" valueBinding="content.editCopy.content"}}</td></tr>
          <tr><td colspan="2">&nbsp;</td></tr>
          <tr><td colspan="2">{{#view App.SaveModelButton modelBinding="content" target="parentView.parentView" action="updateCurrent" elementStyle="{'background-color':'black'}"}}Store content{{/view}}</td></tr>
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
              <tr><td>Id: </td><td>{{view App.TextField name="groupId" title="Id" valueBinding="content.editCopy.groupId"}}</td></tr>
              <tr><td>Title: </td><td>{{view App.TextField name="title" title="Title" valueBinding="content.editCopy.title"}}</td></tr>
            </tbody>
          </table>
        {{/view}}
      </div> 
    </div>
  </script>

</div>
</body>
</html>
