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

package com.badlogic.gdx.backends.android;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import android.content.Intent;
import android.net.Uri;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.net.ServerSocket;
import com.badlogic.gdx.net.ServerSocketHints;
import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.net.SocketHints;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.JsonWriter;

public class AndroidNet implements Net {

	private final class HttpClientResponse implements HttpResponse {

		private org.apache.http.HttpResponse httpResponse;
		private HttpStatus httpStatus;

		public HttpClientResponse (org.apache.http.HttpResponse httpResponse) {
			this.httpResponse = httpResponse;
			this.httpStatus = new HttpStatus(httpResponse.getStatusLine().getStatusCode());
		}

		@Override
		public String getResultAsString () {
			try {
				return EntityUtils.toString(httpResponse.getEntity());
			} catch (Exception e) {
				throw new GdxRuntimeException("Failed to get HTTP response as string", e);
			}
		}

		@Override
		public InputStream getResultAsStream () {
			try {
				return httpResponse.getEntity().getContent();
			} catch (Exception e) {
				throw new GdxRuntimeException("Failed to get HTTP response as stream", e);
			}
		}

		@Override
		public byte[] getResult () {
			try {
				return EntityUtils.toByteArray(httpResponse.getEntity());
			} catch (Exception e) {
				throw new GdxRuntimeException("Failed to retrieve byte array from response", e);
			}
		}

		@Override
		public HttpStatus getStatus () {
			return httpStatus;
		}
		
	}

	// IMPORTANT: The Gdx.net classes are a currently duplicated for LWJGL + Android!
	// If you make changes here, make changes in the other backend as well.
	final AndroidApplication app;

	private final ExecutorService executorService;

	DefaultHttpClient httpClient; 

	public AndroidNet (AndroidApplication activity) {
		app = activity;
		executorService = Executors.newCachedThreadPool();
		httpClient = new DefaultHttpClient();
	}

	@Override
	public void sendHttpRequest (HttpRequest httpRequest, final HttpResponseListener httpResultListener) {
		if (httpRequest.getUrl() == null) { 
			httpResultListener.failed(new GdxRuntimeException("can't process a HTTP request without URL set"));
			return;
		}

		final HttpUriRequest httpClientRequest = getHttpClientRequest(httpRequest);

		final HttpParams httpParams = new BasicHttpParams();
		
		// Sets the timeout for time until TCP connection is established and timeout until first byte received to request timeout value 
		HttpConnectionParams.setConnectionTimeout(httpParams, httpRequest.getTimeout());
		HttpConnectionParams.setSoTimeout(httpParams, httpRequest.getTimeout());
		httpClient.setParams(httpParams);
		
		executorService.submit(new Runnable() {
			@Override
			public void run () {
				try {
					final org.apache.http.HttpResponse httpResponse = httpClient.execute(httpClientRequest);
					// post a runnable to sync the handler with the main thread
					Gdx.app.postRunnable(new Runnable() {
						@Override
						public void run () {
							httpResultListener.handleHttpResponse(new HttpClientResponse(httpResponse));
						}
					});
				} catch (final Exception e) {
					// post a runnable to sync the handler with the main thread
					Gdx.app.postRunnable(new Runnable() {
						@Override
						public void run () {
							httpResultListener.failed(e);
						}
					});
				}
			}
		});
	}

	private HttpUriRequest getHttpClientRequest (HttpRequest httpRequest) {
		if (httpRequest.getMethod() == null) throw new GdxRuntimeException("HTTP method can't be null");

		HttpUriRequest httpClientRequest = convertHttpClientRequest(httpRequest);

		Map<String, String> headers = httpRequest.getHeaders();
		Set<String> keySet = headers.keySet();
		for (String name : keySet) {
			httpClientRequest.setHeader(name, headers.get(name));
		}

		return httpClientRequest;
	}

	private HttpUriRequest convertHttpClientRequest (HttpRequest httpRequest) {
		String content = httpRequest.convertHttpRequest();
		
		if (httpRequest.getMethod().equalsIgnoreCase(HttpMethods.GET)) {
			return new HttpGet(httpRequest.getUrl().concat(content));
		} 
		else if (httpRequest.getMethod().equalsIgnoreCase(HttpMethods.POST) || 
			httpRequest.getMethod().equalsIgnoreCase(HttpMethods.JSON)) {
			// process specific HTTP POST logic
			HttpPost httpPost = new HttpPost(httpRequest.getUrl());
			
			try {
				httpPost.setEntity(new StringEntity(content));
				return httpPost;
			} catch (UnsupportedEncodingException e) {
				return httpPost;
			}
		} else {
			throw new GdxRuntimeException("Android implementation of Net API can't support other HTTP methods yet.");
		}
	}

	@Override
	public ServerSocket newServerSocket (Protocol protocol, int port, ServerSocketHints hints) {
		return new AndroidServerSocket(protocol, port, hints);
	}

	@Override
	public Socket newClientSocket (Protocol protocol, String host, int port, SocketHints hints) {
		return new AndroidSocket(protocol, host, port, hints);
	}

	@Override
	public void openURI (String URI) {
		final Uri uri = Uri.parse(URI);
		app.runOnUiThread(new Runnable() {
			@Override
			public void run () {
				app.startActivity(new Intent(Intent.ACTION_VIEW, uri));
			}
		});
	}

}
