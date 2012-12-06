/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *	 http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.backends.lwjgl;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.Net.HttpMethods;
import com.badlogic.gdx.Net.HttpResponse;
import com.badlogic.gdx.net.ServerSocket;
import com.badlogic.gdx.net.ServerSocketHints;
import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.net.SocketHints;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.JsonWriter;

public class LwjglNet implements Net {

	private final class HttpClientResponse implements HttpResponse {
		
		private HttpURLConnection connection;
		private HttpStatus status;
		private InputStream inputStream;
		
		public HttpClientResponse(HttpURLConnection connection) throws IOException {
			this.connection = connection;
			this.inputStream = connection.getInputStream();
			
			try {
				this.status = new HttpStatus(connection.getResponseCode());
			} catch (IOException e) {
				this.status = new HttpStatus(-1);
			}
		}
		
		@Override
		public byte[] getResult () {
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();

			int nRead;
			byte[] data = new byte[16384];

			try {
				while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
				  buffer.write(data, 0, nRead);
				}
				buffer.flush();
			} catch (IOException e) {
				return new byte[0];
			}
			return buffer.toByteArray();
		}

		@Override
		public String getResultAsString () {
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
			String tmp, line = "";
			try {
				while((tmp=reader.readLine()) != null)
					line += tmp;
				reader.close();
				return line;
			} catch (IOException e) {
				return "";
			}
		}

		@Override
		public InputStream getResultAsStream () {
			return inputStream;
		}

		@Override
		public HttpStatus getStatus () {
			return status;
		}
		
	}
	
	
	// IMPORTANT: The Gdx.net classes are a currently duplicated for LWJGL + Android!
	// If you make changes here, make changes in the other backend as well.

	private final ExecutorService executorService;
	
	public LwjglNet() {
		executorService = Executors.newCachedThreadPool();
	}
	
	@Override
	public void sendHttpRequest (HttpRequest httpRequest, final HttpResponseListener httpResultListener) {
		if (httpRequest.getUrl() == null) {
			httpResultListener.failed(new GdxRuntimeException("can't process a HTTP request without URL set"));
			return;
		}
		
		try {
			String value = httpRequest.convertHttpRequest();
			String method = httpRequest.getMethod();
			
			URL url;
			if(method.equalsIgnoreCase(HttpMethods.GET))
				url = new URL(httpRequest.getUrl()+"?"+value);
			else
				url = new URL(httpRequest.getUrl());
			
			final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setRequestMethod((method.equalsIgnoreCase(HttpMethods.JSON))? HttpMethods.POST : method);
			
			// Headers get set regardless of the method
			Map<String,String> content = httpRequest.getHeaders();
			Set<String> keySet = content.keySet();
			for (String name : keySet) {
				connection.addRequestProperty(name, content.get(name));
			}
			
			// Set Timeouts
			connection.setConnectTimeout(httpRequest.getTimeout());
			connection.setReadTimeout(httpRequest.getTimeout());
			
			// Set the content for JSON or POST (GET has the information embedded in the URL)
			if(!method.equalsIgnoreCase(HttpMethods.GET)) {
				OutputStreamWriter wr = new OutputStreamWriter(connection.getOutputStream ());
				wr.write(value);
				wr.flush();
				wr.close();
			}
			
			executorService.submit(new Runnable() {
				@Override
				public void run () {
					try {
						connection.connect();
						// post a runnable to sync the handler with the main thread
						Gdx.app.postRunnable(new Runnable() {
							@Override
							public void run () {
								try {
									httpResultListener.handleHttpResponse(new HttpClientResponse(connection));
								} catch (IOException e) {
									httpResultListener.failed(e);
									connection.disconnect();
								}
							}
						});
					} catch (final Exception e) {
						// post a runnable to sync the handler with the main thread
						Gdx.app.postRunnable(new Runnable() {
							@Override
							public void run () {
								httpResultListener.failed(e);
								connection.disconnect();
							}
						});
					}
				}
			});
			
		} catch (Exception e) {
			httpResultListener.failed(e);
			return;
		}
	}
	
	@Override
	public ServerSocket newServerSocket (Protocol protocol, int port, ServerSocketHints hints) {
		return new LwjglServerSocket(protocol, port, hints);
	}

	@Override
	public Socket newClientSocket (Protocol protocol, String host, int port, SocketHints hints) {
		return new LwjglSocket(protocol, host, port, hints);
	}

	@Override
	public void openURI (String URI) {
		if (!Desktop.isDesktopSupported()) return;

		Desktop desktop = Desktop.getDesktop();

		if (!desktop.isSupported(Desktop.Action.BROWSE)) return;

		try {
			desktop.browse(new java.net.URI(URI));
		} catch (Exception e) {
			throw new GdxRuntimeException(e);
		}
	}
}
