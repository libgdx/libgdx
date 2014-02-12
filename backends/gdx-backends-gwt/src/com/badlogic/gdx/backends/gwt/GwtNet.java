/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.backends.gwt;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.Net.HttpRequest;
import com.badlogic.gdx.net.HttpStatus;
import com.badlogic.gdx.net.ServerSocket;
import com.badlogic.gdx.net.ServerSocketHints;
import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.net.SocketHints;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.http.client.Header;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.RequestTimeoutException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Window;

public class GwtNet implements Net {

	private ObjectMap<HttpRequest, Request> requests;
	private ObjectMap<HttpRequest, HttpResponseListener> listeners;

	private final class HttpClientResponse implements HttpResponse {

		private Response response;
		private HttpStatus status;

		public HttpClientResponse (Response response) {
			this.response = response;
			this.status = new HttpStatus(response.getStatusCode());
		}

		@Override
		public byte[] getResult () {
			return null;
		}

		@Override
		public String getResultAsString () {
			return response.getText();
		}

		@Override
		public InputStream getResultAsStream () {
			return null;
		}

		@Override
		public HttpStatus getStatus () {
			return status;
		}

		@Override
		public Map<String, List<String>> getHeaders () {
			Map<String, List<String>> headers = new HashMap<String, List<String>>();
			Header[] responseHeaders = response.getHeaders();
			for (int i = 0; i < responseHeaders.length; i++) {
				String headerName = responseHeaders[i].getName();
				List<String> headerValues = headers.get(headerName);
				if (headerValues == null) {
					headerValues = new ArrayList<String>();
					headers.put(headerName, headerValues);
				}
				headerValues.add(responseHeaders[i].getValue());
			}
			return headers;
		}

		@Override
		public String getHeader (String name) {
			return response.getHeader(name);
		}
	}

	public GwtNet () {
		requests = new ObjectMap<HttpRequest, Request>();
		listeners = new ObjectMap<HttpRequest, HttpResponseListener>();
	}

	@Override
	public void sendHttpRequest (HttpRequest httpRequest, final HttpResponseListener httpResultListener) {
		if (httpRequest.getUrl() == null) {
			httpResultListener.failed(new GdxRuntimeException("can't process a HTTP request without URL set"));
			return;
		}

		final boolean is_get = (httpRequest.getMethod() == HttpMethods.GET);
		final String value = httpRequest.getContent();

		final RequestBuilder builder = is_get ? new RequestBuilder(RequestBuilder.GET, httpRequest.getUrl() + "?" + value)
			: new RequestBuilder(RequestBuilder.POST, httpRequest.getUrl());

		Map<String, String> content = httpRequest.getHeaders();
		Set<String> keySet = content.keySet();
		for (String name : keySet) {
			builder.setHeader(name, content.get(name));
		}

		builder.setTimeoutMillis(httpRequest.getTimeOut());

		try {
			Request request = builder.sendRequest(is_get ? null : value, new RequestCallback() {

				@Override
				public void onResponseReceived (Request request, Response response) {
					HttpRequest httpRequest = requests.findKey(requests, true);

					if (httpRequest != null) {
						httpResultListener.handleHttpResponse(new HttpClientResponse(response));
						requests.remove(httpRequest);
						listeners.remove(httpRequest);
					}
				}

				@Override
				public void onError (Request request, Throwable exception) {
					HttpRequest httpRequest = requests.findKey(requests, true);

					if (httpRequest != null) {
						httpResultListener.failed(exception);
						requests.remove(httpRequest);
						listeners.remove(httpRequest);
					}
				}
			});

			requests.put(httpRequest, request);
			listeners.put(httpRequest, httpResultListener);

		} catch (RequestException e) {
			httpResultListener.failed(e);
		}

	}

	@Override
	public void cancelHttpRequest (HttpRequest httpRequest) {
		HttpResponseListener httpResponseListener = listeners.get(httpRequest);
		Request request = requests.get(httpRequest);

		if (httpResponseListener != null && request != null) {
			request.cancel();
			httpResponseListener.cancelled();
			requests.remove(httpRequest);
			listeners.remove(httpRequest);
		}
	}

	@Override
	public ServerSocket newServerSocket (Protocol protocol, int port, ServerSocketHints hints) {
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public Socket newClientSocket (Protocol protocol, String host, int port, SocketHints hints) {
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public void openURI (String URI) {
		Window.open(URI, "_blank", null);
	}
}
