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

package com.badlogic.gdx.backends.ios;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import cli.MonoTouch.Foundation.NSUrl;
import cli.MonoTouch.UIKit.UIApplication;
import cli.System.IO.Stream;
import cli.System.IO.StreamReader;
import cli.System.Net.HttpWebRequest;
import cli.System.Net.HttpWebResponse;
import cli.System.Net.WebHeaderCollection;
import cli.System.Net.WebRequest;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.net.HttpStatus;
import com.badlogic.gdx.net.ServerSocket;
import com.badlogic.gdx.net.ServerSocketHints;
import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.net.SocketHints;
import com.badlogic.gdx.utils.StreamUtils;

public class IOSNet implements Net {

	public static class InputStreamNetStreamImpl extends InputStream {
		private final Stream stream;

		public InputStreamNetStreamImpl (Stream stream) {
			this.stream = stream;
		}

		@Override
		public int read () throws IOException {
			return stream.ReadByte();
		}
	}

	public static class OutputStreamNetStreamImpl extends OutputStream {

		private Stream stream;

		public OutputStreamNetStreamImpl (Stream stream) {
			this.stream = stream;
		}

		@Override
		public void write (int b) throws IOException {
			// should be the first 8bits of the 32bits int.
			stream.WriteByte((byte)b);
		}

	}

	static class IosHttpResponse implements HttpResponse {

		private HttpWebResponse webResponse;

		public IosHttpResponse (HttpWebResponse webResponse) {
			this.webResponse = webResponse;
		}

		@Override
		public HttpStatus getStatus () {
			return new HttpStatus(webResponse.get_StatusCode().Value);
		}

		@Override
		public String getResultAsString () {
			StreamReader reader = new StreamReader(webResponse.GetResponseStream());
			return reader.ReadToEnd();
		}

		@Override
		public InputStream getResultAsStream () {
			return new InputStreamNetStreamImpl(webResponse.GetResponseStream());
		}

		@Override
		public byte[] getResult () {
			int length = (int)webResponse.get_ContentLength();
			byte[] result = new byte[length];
			webResponse.GetResponseStream().Read(result, 0, length);
			return result;
		}

		@Override
		public String getHeader (String name) {
			return webResponse.get_Headers().Get(name);
		}

		@Override
		public Map<String, List<String>> getHeaders () {
			WebHeaderCollection responseHeaders = webResponse.get_Headers();
			Map<String, List<String>> headers = new HashMap<String, List<String>>();
			for (int i = 0, j = responseHeaders.get_Count(); i < j; i++) {
				String headerName = responseHeaders.GetKey(i);
				List<String> headerValues = headers.get(headerName);
				if (headerValues == null) {
					headerValues = new ArrayList<String>();
					headers.put(headerName, headerValues);
				}
				String[] responseHeaderValues = responseHeaders.GetValues(i);
				for (int k = 0; k < responseHeaderValues.length; k++) {
					headerValues.add(responseHeaderValues[k]);
				}				
			}
			return headers;
		}

	}

	final UIApplication uiApp;
	final ExecutorService executorService;

	public IOSNet (IOSApplication app) {
		uiApp = app.uiApp;
		executorService = Executors.newCachedThreadPool();
	}

	@Override
	public void sendHttpRequest (final HttpRequest httpRequest, final HttpResponseListener httpResultListener) {

		Future<?> processHttpRequestFuture = executorService.submit(new Runnable() {
			@Override
			public void run () {

				try {

					String url = httpRequest.getUrl();
					String method = httpRequest.getMethod();

					if (method.equalsIgnoreCase(HttpMethods.GET)) {
						String value = httpRequest.getContent();
						if (value != null && !"".equals(value)) url += "?" + value;
					}

					HttpWebRequest httpWebRequest = (HttpWebRequest)WebRequest.Create(url);

					int timeOut = httpRequest.getTimeOut();
					if (timeOut > 0)
						httpWebRequest.set_Timeout(timeOut);
					else
						httpWebRequest.set_Timeout(-1); // the value of the Infinite constant (see
// http://msdn.microsoft.com/en-us/library/system.threading.timeout.infinite.aspx)
					
					httpWebRequest.set_Method(method);

					Map<String, String> headers = httpRequest.getHeaders();
					WebHeaderCollection webHeaderCollection = new WebHeaderCollection();
					for (String key : headers.keySet())
						webHeaderCollection.Add(key, headers.get(key));
					httpWebRequest.set_Headers(webHeaderCollection);

					if (method.equalsIgnoreCase(HttpMethods.POST) || method.equalsIgnoreCase(HttpMethods.PUT)) {
						InputStream contentAsStream = httpRequest.getContentStream();
						String contentAsString = httpRequest.getContent();

						if (contentAsStream != null) {
							httpWebRequest.set_ContentLength(contentAsStream.available());
							
							Stream stream = httpWebRequest.GetRequestStream();
							StreamUtils.copyStream(contentAsStream, new OutputStreamNetStreamImpl(stream));
							stream.Close();
						} else if (contentAsString != null) {
							byte[] data = contentAsString.getBytes();
							httpWebRequest.set_ContentLength(data.length);
							
							Stream stream = httpWebRequest.GetRequestStream();
							stream.Write(data, 0, data.length);
							stream.Close();
						}
						
					}

					final HttpWebResponse httpWebResponse = (HttpWebResponse)httpWebRequest.GetResponse();
							httpResultListener.handleHttpResponse(new IosHttpResponse(httpWebResponse));
				} catch (final Exception e) {
							httpResultListener.failed(e);
				}
			}
		});
	}

	@Override
	public ServerSocket newServerSocket (Protocol protocol, int port, ServerSocketHints hints) {
		return new IOSServerSocket(protocol, port, hints);
	}

	@Override
	public Socket newClientSocket (Protocol protocol, String host, int port, SocketHints hints) {
		return new IOSSocket(protocol, host, port, hints);
	}

	@Override
	public void openURI (String URI) {
		uiApp.OpenUrl(new NSUrl(URI));
	}
}
