package ch.plus8.hikr.gappserver.signpost;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import oauth.signpost.http.HttpRequest;

import com.google.api.client.http.HttpHeaders;

public class AppEngineHttpRequestAdapter implements HttpRequest {

	private com.google.api.client.http.HttpRequest request;

	public AppEngineHttpRequestAdapter(com.google.api.client.http.HttpRequest request) {
		this.request = request;
	}

	@Override
	public String getMethod() {
		return this.request.getMethod().name();
	}

	@Override
	public String getRequestUrl() {
		return this.request.getUrl().toString();
	}

	@Override
	public void setRequestUrl(String paramString) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setHeader(String paramString1, String paramString2) {
		this.request.getHeaders().put(paramString1, paramString2);

	}

	@Override
	public String getHeader(String name) {
		return (String)this.request.getHeaders().get("name");
	}

	@Override
	public Map<String, String> getAllHeaders() {
		 HttpHeaders headers = this.request.getHeaders();
		 Map<String,String> allHeaders = new HashMap<String,String>();
		 for(Entry<String, Object> e : headers.entrySet()) {
			 allHeaders.put(e.getKey(), (String)e.getValue());
		 }
		 
		 return allHeaders;
	}

	@Override
	public InputStream getMessagePayload() throws IOException {
		return null;
	}

	@Override
	public String getContentType() {
		return this.request.getHeaders().getContentType();
	}

	@Override
	public Object unwrap() {
		return this.request;
	}

}
