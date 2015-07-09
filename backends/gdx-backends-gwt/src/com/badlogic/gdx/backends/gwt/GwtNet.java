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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.badlogic.gdx.Net;
import com.badlogic.gdx.backends.gwt.preloader.Preloader;
import com.badlogic.gdx.Net.Protocol;
import com.badlogic.gdx.net.HttpStatus;
import com.badlogic.gdx.net.NetJavaServerSocketImpl;
import com.badlogic.gdx.net.ServerSocket;
import com.badlogic.gdx.net.ServerSocketHints;
import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.net.SocketHints;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap;
import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.event.dom.client.ErrorEvent;
import com.google.gwt.event.dom.client.ErrorHandler;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.http.client.Header;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.RootPanel;

public class GwtNet implements Net {

	ObjectMap<HttpRequest, Request> requests;
	ObjectMap<HttpRequest, HttpResponseListener> listeners;
	Preloader preloader;

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
				Header header = responseHeaders[i];
				if (header != null) {
					String headerName = responseHeaders[i].getName();
					List<String> headerValues = headers.get(headerName);
					if (headerValues == null) {
						headerValues = new ArrayList<String>();
						headers.put(headerName, headerValues);
					}
					headerValues.add(responseHeaders[i].getValue());
				}
			}
			return headers;
		}

		@Override
		public String getHeader (String name) {
			return response.getHeader(name);
		}
	}

	public GwtNet (Preloader preloader) {
		this.preloader = preloader;
		requests = new ObjectMap<HttpRequest, Request>();
		listeners = new ObjectMap<HttpRequest, HttpResponseListener>();
	}

	@Override
	public void sendHttpRequest (final HttpRequest httpRequest, final HttpResponseListener httpResultListener) {
		if (httpRequest.getUrl() == null) {
			httpResultListener.failed(new GdxRuntimeException("can't process a HTTP request without URL set"));
			return;
		}
		final String acceptHeader = httpRequest.getHeaders().get("Accept");
		if ("image/png".equals(acceptHeader) || "image/jpeg".equals(acceptHeader) || "image/jpg".equals(acceptHeader)
			|| "image/gif".equals(acceptHeader)) {
			final String url = httpRequest.getUrl();
			final Image img = new Image();
			img.addErrorHandler(new ErrorHandler() {
				@Override
				public void onError (ErrorEvent event) {
					httpResultListener.failed(new GdxRuntimeException(event.toString()));
				}
			});
			img.setVisible(false);
			RootPanel.get().add(img);
			ImageElement.as(img.getElement()).setAttribute("crossOrigin", "Anonymous");
			img.addLoadHandler(new LoadHandler() {
				@Override
				public void onLoad (LoadEvent event) {
					Canvas canvas = Canvas.createIfSupported();
					canvas.getCanvasElement().setWidth(img.getWidth());
					canvas.getCanvasElement().setHeight(img.getHeight());
					Context2d context = canvas.getContext2d();
					ImageElement imageElement = ImageElement.as(img.getElement());
					context.drawImage(imageElement, 0, 0);
					final String dataUrl = canvas.toDataUrl(acceptHeader);
					img.removeFromParent();
					preloader.images.put(dataUrl, imageElement);
					httpResultListener.handleHttpResponse(new HttpResponse() {
						@Override
						public HttpStatus getStatus () {
							return new HttpStatus(200);
						}

						@Override
						public String getResultAsString () {
							return dataUrl;
						}

						@Override
						public InputStream getResultAsStream () {
							return null;
						}

						@Override
						public byte[] getResult () {
							return dataUrl.getBytes();
						}

						@Override
						public Map<String, List<String>> getHeaders () {
							return Collections.emptyMap();
						}

						@Override
						public String getHeader (String name) {
							return null;
						}
					});
				}
			});
			img.setUrl(url);
		} else {

			final String method = httpRequest.getMethod();
			final String value = httpRequest.getContent();
			final boolean valueInBody = method.equalsIgnoreCase(HttpMethods.POST) || method.equals(HttpMethods.PUT);

			RequestBuilder builder;

			String url = httpRequest.getUrl();
			if (method.equalsIgnoreCase(HttpMethods.GET)) {
				if (value != null) {
					url += "?" + value;
				}
				builder = new RequestBuilder(RequestBuilder.GET, url);
			} else if (method.equalsIgnoreCase(HttpMethods.POST)) {
				builder = new RequestBuilder(RequestBuilder.POST, url);
			} else if (method.equalsIgnoreCase(HttpMethods.DELETE)) {
				if (value != null) {
					url += "?" + value;
				}
				builder = new RequestBuilder(RequestBuilder.DELETE, url);
			} else if (method.equalsIgnoreCase(HttpMethods.PUT)) {
				builder = new RequestBuilder(RequestBuilder.PUT, url);
			} else {
				throw new GdxRuntimeException("Unsupported HTTP Method");
			}

			Map<String, String> content = httpRequest.getHeaders();
			Set<String> keySet = content.keySet();
			for (String name : keySet) {
				builder.setHeader(name, content.get(name));
			}

			builder.setTimeoutMillis(httpRequest.getTimeOut());

			try {
				Request request = builder.sendRequest(valueInBody ? value : null, new RequestCallback() {

					@Override
					public void onResponseReceived (Request request, Response response) {
						httpResultListener.handleHttpResponse(new HttpClientResponse(response));
						requests.remove(httpRequest);
						listeners.remove(httpRequest);
					}

					@Override
					public void onError (Request request, Throwable exception) {
						httpResultListener.failed(exception);
						requests.remove(httpRequest);
						listeners.remove(httpRequest);
					}
				});
				requests.put(httpRequest, request);
				listeners.put(httpRequest, httpResultListener);
			} catch (RequestException e) {
				httpResultListener.failed(e);
			}
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
	public ServerSocket newServerSocket (Protocol protocol, String hostname, int port, ServerSocketHints hints) {
		throw new UnsupportedOperationException("Not implemented");
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
	public boolean openURI (String URI) {
		Window.open(URI, "_blank", null);
		return true;
	}
}
