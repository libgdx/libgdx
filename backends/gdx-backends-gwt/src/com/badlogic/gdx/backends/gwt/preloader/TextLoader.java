
package com.badlogic.gdx.backends.gwt.preloader;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;

public class TextLoader {
	private final LoaderCallback<String> callback;

	public TextLoader (String url, LoaderCallback<String> callback) {
		this.callback = callback;
		RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, url);
		try {
			builder.sendRequest(null, new RequestCallback() {
				@Override
				public void onResponseReceived (Request request, Response response) {
					TextLoader.this.callback.success(response.getText());
				}

				@Override
				public void onError (Request request, Throwable exception) {
					TextLoader.this.callback.error();
				}
			});
		} catch (RequestException e) {
			callback.error();
		}
	}
}
