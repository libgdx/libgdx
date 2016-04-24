package com.badlogic.gdx.net;

import com.badlogic.gdx.Net;
import com.badlogic.gdx.Net.HttpResponse;
import com.badlogic.gdx.Net.HttpResponseListener;

/** Adapter class for {@link com.badlogic.gdx.Net.HttpResponseListener}} */
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
