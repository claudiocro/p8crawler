package ch.plus8.hikr.gappserver.signpost;

import oauth.signpost.AbstractOAuthConsumer;
import oauth.signpost.http.HttpRequest;

public class AppEngineOAuthConsumer extends AbstractOAuthConsumer {

	private static final long serialVersionUID = 1L;

	
	public AppEngineOAuthConsumer(String consumerKey, String consumerSecret) {
		super(consumerKey, consumerSecret);
	}

	protected HttpRequest wrap(Object request) {
		if (!(request instanceof com.google.api.client.http.HttpRequest)) {
			throw new IllegalArgumentException("The default consumer expects requests of type com.google.api.client.http.HttpRequest");
		}

		return new AppEngineHttpRequestAdapter((com.google.api.client.http.HttpRequest) request);
	}
}
