package ch.plus8.hikr.gappserver.gplus;

import ch.plus8.hikr.gappserver.Util;

import com.google.api.client.http.json.JsonHttpRequest;
import com.google.api.client.http.json.JsonHttpRequestInitializer;
import com.google.api.services.plus.PlusRequest;

public class PlusRequestInitializer implements JsonHttpRequestInitializer {
    public void initialize(JsonHttpRequest request) {
      PlusRequest plusRequest = (PlusRequest)request;
      plusRequest.setPrettyPrint(true);
      plusRequest.setKey(Util.GOOGLE_API_KEY);
  }
}