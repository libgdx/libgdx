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

package com.badlogic.gdx.net;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.badlogic.gdx.Net;
import com.badlogic.gdx.Net.HttpMethods;
import com.badlogic.gdx.Net.HttpRequest;
import com.badlogic.gdx.Net.HttpResponse;
import com.badlogic.gdx.Net.HttpResponseListener;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.StreamUtils;
import com.badlogic.gdx.utils.StringBuilder;

/** Implements part of the {@link Net} API using {@link HttpURLConnection}, to be easily reused between the Android and Desktop
 * backends.
 * @author acoppes */
public class NetJavaImpl {

	static class HttpClientResponse implements HttpResponse {
		private HttpURLConnection connection;
		private HttpStatus status;
		private InputStream inputStream;

		public HttpClientResponse (HttpURLConnection connection) throws IOException {
			this.connection = connection;
			try {
				this.inputStream = connection.getInputStream();
			} catch (IOException e) {
				this.inputStream = connection.getErrorStream();
			}

			try {
				this.status = new HttpStatus(connection.getResponseCode());
			} catch (IOException e) {
				this.status = new HttpStatus(-1);
			}
		}

		@Override
		public byte[] getResult () {
			try {
				int contentLength = connection.getContentLength();
				ByteArrayOutputStream buffer;
				if (contentLength > 0)
					buffer = new OptimizedByteArrayOutputStream(contentLength);
				else
					buffer = new OptimizedByteArrayOutputStream();
				StreamUtils.copyStream(inputStream, buffer);
				return buffer.toByteArray();
			} catch (IOException e) {
				return StreamUtils.EMPTY_BYTES;
			}
		}

		@Override
		public String getResultAsString () {
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
			try {
				int approxStringLength = connection.getContentLength();
				StringBuilder b;
				if (approxStringLength > 0)
					b = new StringBuilder(approxStringLength);
				else
					b = new StringBuilder();
				String line;
				while ((line = reader.readLine()) != null)
					b.append(line);
				return b.toString();
			} catch (IOException e) {
				return "";
			} finally {
				StreamUtils.closeQuietly(reader);
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

		@Override
		public String getHeader (String name) {
			return connection.getHeaderField(name);
		}

		@Override
		public Map<String, List<String>> getHeaders () {
			return connection.getHeaderFields();
		}
	}

	private final ExecutorService executorService;

	public NetJavaImpl () {
		executorService = Executors.newCachedThreadPool();
	}

	public void sendHttpRequest (final HttpRequest httpRequest, final HttpResponseListener httpResponseListener) {
		if (httpRequest.getUrl() == null) {
			httpResponseListener.failed(new GdxRuntimeException("can't process a HTTP request without URL set"));
			return;
		}

		try {
			final String method = httpRequest.getMethod();

			URL url;

			if (method.equalsIgnoreCase(HttpMethods.GET)) {
				String queryString = "";
				String value = httpRequest.getContent();
				if (value != null && !"".equals(value)) queryString = "?" + value;
				url = new URL(httpRequest.getUrl() + queryString);
			} else {
				url = new URL(httpRequest.getUrl());
			}

			final HttpURLConnection connection = (HttpURLConnection)url.openConnection();
			// should be enabled to upload data.
			final boolean doingOutPut = method.equalsIgnoreCase(HttpMethods.POST) || method.equalsIgnoreCase(HttpMethods.PUT);
			connection.setDoOutput(doingOutPut);
			connection.setDoInput(true);
			connection.setRequestMethod(method);

			// Headers get set regardless of the method
			for (Map.Entry<String, String> header : httpRequest.getHeaders().entrySet())
				connection.addRequestProperty(header.getKey(), header.getValue());

			// Set Timeouts
			connection.setConnectTimeout(httpRequest.getTimeOut());
			connection.setReadTimeout(httpRequest.getTimeOut());

			executorService.submit(new Runnable() {
				@Override
				public void run () {
					try {

						// Set the content for POST and PUT (GET has the information embedded in the URL)
						if (doingOutPut) {
							// we probably need to use the content as stream here instead of using it as a string.
							String contentAsString = httpRequest.getContent();
							InputStream contentAsStream = httpRequest.getContentStream();

							OutputStream outputStream = connection.getOutputStream();
							if (contentAsString != null) {
								OutputStreamWriter writer = new OutputStreamWriter(outputStream);
								writer.write(contentAsString);
								writer.flush();
								writer.close();
							} else if (contentAsStream != null) {
								StreamUtils.copyStream(contentAsStream, outputStream);
								outputStream.flush();
								outputStream.close();
							}
						}

						connection.connect();

						final HttpClientResponse clientResponse = new HttpClientResponse(connection);
						try {
							httpResponseListener.handleHttpResponse(clientResponse);
						} finally {
							connection.disconnect();
						}
					} catch (final Exception e) {
						connection.disconnect();
						httpResponseListener.failed(e);
					}
				}
			});

		} catch (Exception e) {
			httpResponseListener.failed(e);
			return;
		}
	}

	/** A ByteArrayOutputStream which avoids copying of the byte array if not necessary. */
	static class OptimizedByteArrayOutputStream extends ByteArrayOutputStream {
		OptimizedByteArrayOutputStream () {
		}

		OptimizedByteArrayOutputStream (int initialSize) {
			super(initialSize);
		}

		@Override
		public synchronized byte[] toByteArray () {
			if (count == buf.length) return buf;
			return super.toByteArray();
		}
	}
}