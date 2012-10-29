package ch.plus8.hikr.gappserver.dropbox;

import com.google.api.client.http.HttpResponseException;

public class DropboxException extends Exception {

	private static final long serialVersionUID = 1L;

	
	private HttpResponseException response;

	protected DropboxException() {
	}

	public DropboxException(String paramString) {
		super(paramString);
	}

	public DropboxException(HttpResponseException response) {
		super(response);
		this.response = response;
	}	
	
	public int getStatusCode() {
		return response.getStatusCode();
	}

}
