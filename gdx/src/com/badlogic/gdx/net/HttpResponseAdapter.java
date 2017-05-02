package com.badlogic.gdx.net;

import com.badlogic.gdx.Net.HttpResponse;
import com.badlogic.gdx.Net.HttpResponseListener;

/** Convenience implementation of {@link HttpResponseListener}. Derive from this and only override what you need.
 * @author nrallakis */
public class HttpResponseAdapter implements HttpResponseListener {
	@Override
	public void handleHttpResponse (HttpResponse httpResponse) {
	}

	@Override
	public void failed (Throwable t) {
	}

	@Override
	public void cancelled () {
	}
	
}
