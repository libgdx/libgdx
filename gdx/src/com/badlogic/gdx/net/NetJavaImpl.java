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
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.Net.HttpMethods;
import com.badlogic.gdx.Net.HttpRequest;
import com.badlogic.gdx.Net.HttpResponse;
import com.badlogic.gdx.Net.HttpResponseListener;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.StreamUtils;

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
				while ((tmp = reader.readLine()) != null)
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
			Map<String, String> headers = httpRequest.getHeaders();
			Set<String> keySet = headers.keySet();
			for (String name : keySet)
				connection.addRequestProperty(name, headers.get(name));

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
// finally {
// connection.disconnect();
// }
				}
			});

		} catch (Exception e) {
			httpResponseListener.failed(e);
			return;
		}
	}

}
