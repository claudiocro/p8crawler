package ch.plus8.hikr.gappserver.googledrive;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import ch.plus8.hikr.gappserver.Util;
import ch.plus8.hikr.gappserver.google.P8CredentialStore;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.appengine.http.UrlFetchTransport;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.Drive.Children;
import com.google.api.services.drive.model.ChildList;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.ParentReference;
import com.google.gdata.client.DocumentQuery;
import com.google.gdata.client.docs.DocsService;
import com.google.gdata.data.PlainTextConstruct;
import com.google.gdata.data.docs.DocumentListFeed;
import com.google.gdata.data.docs.FolderEntry;
import com.google.gdata.util.ServiceException;

public class GDriveApi {

	private Drive drive;
	private DocsService docs;

	public void loadById(String googleId) throws IOException {
		Credential credential = new GoogleAuthorizationCodeFlow.Builder(new NetHttpTransport(), new GsonFactory(), Util.GOOGLE_OAUTH2_CLIENT_ID, Util.GOOGLE_OAUTH2_CLIENT_SECRET, GDriveGalleryServlet.SCOPES).setCredentialStore(new P8CredentialStore()).build().loadCredential(googleId);

		drive = new Drive.Builder(new UrlFetchTransport(), new GsonFactory(),credential)
			.setApplicationName("photography-stream/1.1.1")
			.build();

		docs = new DocsService("photography-stream/1.1.1");
		docs.setOAuth2Credentials(credential);
	}

	public void loadByCredentials(Credential credential) throws IOException {
		drive = new Drive.Builder(new UrlFetchTransport(), new GsonFactory(), credential)
			.setApplicationName("photography-stream/1.1.1")
			.build();

		docs = new DocsService("photography-stream/1.1.1");
		docs.setOAuth2Credentials(credential);
	}

	public ChildList getContentsFromEntry(String id, String pageToken) throws IOException, ServiceException {
		/*String feedLink = "https://docs.google.com/feeds/default/private/full/folder%3A" + id + "/contents";
		DocumentQuery query = new DocumentQuery(new URL(feedLink));
		return docs.query(query, DocumentListFeed.class);
		*/
		Children.List list = drive.children().list(id);
		if(pageToken != null) {
			list.setPageToken(pageToken);
		}
		return list.execute();
	}

	public DocumentListFeed getContentFromEntryByTitle(String id, String title) throws IOException, ServiceException {
		String feedLink = "https://docs.google.com/feeds/default/private/full/folder%3A" + id + "/contents";
		DocumentQuery query = new DocumentQuery(new URL(feedLink));
		query.setTitleQuery(title);
		query.setTitleExact(true);
		return docs.query(query, DocumentListFeed.class);
	}

	public DocumentListFeed getFolderFromParentByTitle(String parentId, String title) throws IOException, ServiceException {
		String feedLink = "https://docs.google.com/feeds/default/private/full/folder%3A" + parentId + "/contents/-/folder";

		DocumentQuery query = new DocumentQuery(new URL(feedLink));
		query.setTitleQuery(title);
		query.setTitleExact(true);
		return docs.query(query, DocumentListFeed.class);
	}

	public FolderEntry createFolder(String parentId, String title) throws IOException, ServiceException {
		String newUrl = "https://docs.google.com/feeds/default/private/full/folder%3A" + parentId + "/contents";
		FolderEntry tFolder = new FolderEntry();
		tFolder.setTitle(new PlainTextConstruct(title));
		tFolder = docs.insert(new URL(newUrl), tFolder);
		return tFolder;
	}

	public void deleteFileById(String id) throws IOException, ServiceException {
		URL docURL = new URL("https://docs.google.com/feeds/default/private/full/" + id + "?delete=true");
		docs.delete(docURL, "*");
	}

	public File getGFileById(String id) throws IOException {
		return drive.files().get(id).execute();
	}

	public File addFile(String parentId, String mimeType, String title, byte[] imageData) throws IOException, ServiceException {
		/*
		String newUrl = "https://docs.google.com/feeds/default/private/full/folder%3A"+parentId+"/contents";
		FileEntry tFolder = new FileEntry();
		tFolder.setTitle(new PlainTextConstruct(title));
		tFolder.setMediaSource(new MediaByteArraySource(imageData, mimeType));
		tFolder = docs.insert(new URL(newUrl), tFolder);
		return tFolder;
		*/
		File file = new File();
		file.setMimeType(mimeType);
		file.setTitle(title);

		ParentReference pc = new ParentReference();
		pc.setId(parentId);
		List<ParentReference> parentsCol = new ArrayList<ParentReference>();
		parentsCol.add(pc);
		file.setParents(parentsCol);
		file = drive.files().insert(file, new ByteArrayContent(mimeType, imageData)).execute();
		return file;

	}
}
