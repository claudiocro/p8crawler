package ch.plus8.hikr.gappserver.dropbox;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.logging.Logger;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import ch.plus8.hikr.gappserver.dropbox.Metadata.DropboxAccount;
import ch.plus8.hikr.gappserver.dropbox.Metadata.DropboxEntity;
import ch.plus8.hikr.gappserver.dropbox.Metadata.DropboxLink;

import com.google.api.client.extensions.appengine.http.urlfetch.UrlFetchTransport;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpMethod;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.json.JsonHttpParser;
import com.google.api.client.json.gson.GsonFactory;
import com.google.appengine.api.images.Image;

public class DropboxAPI {
	
	private static final Logger logger = Logger.getLogger(DropboxAPI.class.getName());

	public static final int STATUS_404 = 404;
	
	private JsonHttpParser parser;
	private final OAuthConsumer consumer;
	private UrlFetchTransport transport;
	
	private DropboxAccount accountInfo;

	public DropboxAPI(OAuthConsumer consumer) {
		this.consumer = consumer;
		this.parser = JsonHttpParser.builder(new GsonFactory()).setContentType("text/javascript").build();
		this.transport = new UrlFetchTransport();
	}
	
	public DropboxAccount accountInfo() throws IOException, OAuthMessageSignerException, OAuthExpectationFailedException, OAuthCommunicationException {
		if(accountInfo != null)
			return accountInfo;
		
		String url = "https://api.dropbox.com/1/account/info/";
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept("*/*");

		HttpRequest request = transport.createRequestFactory().buildGetRequest(new GenericUrl(url));
		request.addParser(parser);
		request.setHeaders(headers);
		
		consumer.sign(request);
		DropboxAccount metadata = request.execute().parseAs(DropboxAccount.class);
		accountInfo = metadata;
		return metadata;
	}
	
	public DropboxEntity metadata(String path) throws IOException, OAuthMessageSignerException, OAuthExpectationFailedException, OAuthCommunicationException {
		if(path.startsWith("/"))
			path = path.substring(1);
		String url = "https://api.dropbox.com/1/metadata/dropbox/"+encodePath(path);
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept("*/*");

		HttpRequest request = transport.createRequestFactory().buildGetRequest(new GenericUrl(url));
		request.addParser(parser);
		request.setHeaders(headers);

		try {
			consumer.sign(request);
			DropboxEntity metadata = request.execute().parseAs(DropboxEntity.class);
			return metadata;
		} catch(HttpResponseException e) {
			if(e.getResponse().getStatusCode() == 404) //not found;
				return null;
			else 
				throw e;
		}

	}
	
	public DropboxLink share(String path) throws IOException, OAuthMessageSignerException, OAuthExpectationFailedException, OAuthCommunicationException {
		if(path.startsWith("/"))
			path = path.substring(1);
		
		String url = "https://api.dropbox.com/1/shares/dropbox/"+encodePath(path);
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept("*/*");

		HttpRequest request = transport.createRequestFactory().buildGetRequest(new GenericUrl(url));
		request.addParser(parser);
		request.setHeaders(headers);
		
		consumer.sign(request);
		DropboxLink metadata = request.execute().parseAs(DropboxLink.class);
		return metadata;
	}
	
	public DropboxLink media(String path) throws IOException, OAuthMessageSignerException, OAuthExpectationFailedException, OAuthCommunicationException {
		if(path.startsWith("/"))
			path = path.substring(1);
		
		
		if(path.toLowerCase().startsWith("public/")) {
			DropboxLink dropboxLink = new DropboxLink();
			dropboxLink.expires = null;
			dropboxLink.url = "https://dl.dropbox.com/u/"+accountInfo().uid+"/"+encodePath(path.substring(7));
			return dropboxLink;
		}
		
		String url = "https://api.dropbox.com/1/media/dropbox/"+encodePath(path);
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept("*/*");

		HttpRequest request = transport.createRequestFactory().buildGetRequest(new GenericUrl(url));
		request.addParser(parser);
		request.setHeaders(headers);
		
		consumer.sign(request);
		DropboxLink metadata = request.execute().parseAs(DropboxLink.class);
		return metadata;
	}
	
	public DropboxEntity uploadImage(String path, String thumbName, Image thumb) throws IOException, OAuthMessageSignerException, OAuthExpectationFailedException, OAuthCommunicationException {
		if(path.startsWith("/"))
			path = path.substring(1);
		
		String url = "https://api-content.dropbox.com/1/files_put/dropbox/"+encodePath(path+thumbName);
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept("*/*");

		ByteArrayContent imageContent = new ByteArrayContent("application/octet-stream", thumb.getImageData());
		HttpRequest request = transport.createRequestFactory().buildPutRequest(new GenericUrl(url), imageContent);
		request.setMethod(HttpMethod.PUT);
		request.addParser(parser);
		request.setHeaders(headers);
		
		consumer.sign(request);
		DropboxEntity savedMetadata = request.execute().parseAs(DropboxEntity.class);
		
		return savedMetadata;
	}
	
	public DropboxEntity delete(String path) throws DropboxException, IOException, OAuthMessageSignerException, OAuthExpectationFailedException, OAuthCommunicationException {
		String url = "https://api.dropbox.com/1/fileops/delete";
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept("*/*");

		GenericUrl genericUrl = new GenericUrl(url);
		genericUrl.set("root", "dropbox");
		genericUrl.set("path", path);
		
		HttpRequest request = transport.createRequestFactory().buildPostRequest(genericUrl, null);
		request.addParser(parser);
		request.setHeaders(headers);
		
		consumer.sign(request);
		try {
			DropboxEntity metadata = request.execute().parseAs(DropboxEntity.class);
			return metadata;
		} catch(HttpResponseException e) {
			throw new DropboxException(e);
		}
	
	}
	
	
	private String encodePath(String path) throws UnsupportedEncodingException {
	 path = URLEncoder.encode(path, "UTF-8");
     path = path.replace("%2F", "/");
     path = path.replace("+", "%20").replace("*", "%2A");
     
     return path;
	}
}
