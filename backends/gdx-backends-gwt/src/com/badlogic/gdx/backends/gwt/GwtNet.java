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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.badlogic.gdx.Net;
import com.badlogic.gdx.net.HttpStatus;
import com.badlogic.gdx.net.ServerSocket;
import com.badlogic.gdx.net.ServerSocketHints;
import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.net.SocketHints;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap;
import com.google.gwt.http.client.Header;
import com.google.gwt.typedarrays.shared.Int8Array;
import com.google.gwt.typedarrays.shared.TypedArrays;
import com.google.gwt.user.client.Window;
import com.google.gwt.xhr.client.ReadyStateChangeHandler;
import com.google.gwt.xhr.client.XMLHttpRequest;

public class GwtNet implements Net {

	ObjectMap<HttpRequest, XMLHttpRequest> requests;
	ObjectMap<HttpRequest, HttpResponseListener> listeners;

	private final class HttpClientResponse implements HttpResponse {

		private Int8Array response;
		private HttpStatus status;
        private XMLHttpRequest request;

		public HttpClientResponse(int status, Int8Array response, XMLHttpRequest request) {
			this.response = response;
			this.status = new HttpStatus(status);
            this.request = request;
		}

		@Override
		public byte[] getResult () {
            byte[] array = new byte[response.length()];
            for (int i = 0; i < array.length; i++) {
                array[i] = response.get(i);
            }
			return array;
		}

        @Override
        public String getHeader(String header) {
            return request.getResponseHeader(header);
        }

        public Header[] getHeadersArray() {
            String allHeaders = request.getAllResponseHeaders();
            String[] unparsedHeaders = allHeaders.split("\n");
            Header[] parsedHeaders = new Header[unparsedHeaders.length];
            for (int i = 0, n = unparsedHeaders.length; i < n; ++i) {
                String unparsedHeader = unparsedHeaders[i];
                if (unparsedHeader.length() == 0) {
                    continue;
                }
                int endOfNameIdx = unparsedHeader.indexOf(':');
                if (endOfNameIdx < 0) {
                    continue;
                }
                final String name = unparsedHeader.substring(0, endOfNameIdx).trim();
                final String value = unparsedHeader.substring(endOfNameIdx + 1).trim();
                Header header = new Header() {
                    @Override
                    public String getName() {
                        return name;
                    }
                    @Override
                    public String getValue() {
                        return value;
                    }
                    @Override
                    public String toString() {
                        return name + " : " + value;
                    }
                };
                parsedHeaders[i] = header;
            }
            return parsedHeaders;
        }

		@Override
		public String getResultAsString () {
			return response.toString();
		}

		@Override
		public InputStream getResultAsStream () {
			return new ByteArrayInputStream(getResult());
		}

		@Override
		public HttpStatus getStatus () {
			return status;
		}

		@Override
        public Map<String, List<String>> getHeaders () {
            Map<String, List<String>> headers = new HashMap<String, List<String>>();
            Header[] responseHeaders = getHeadersArray();
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
	}

	public GwtNet () {
		requests = new ObjectMap<HttpRequest, XMLHttpRequest>();
		listeners = new ObjectMap<HttpRequest, HttpResponseListener>();
	}

	@Override
	public void sendHttpRequest (final HttpRequest httpRequest, final HttpResponseListener httpResultListener) {
		if (httpRequest.getUrl() == null) {
			httpResultListener.failed(new GdxRuntimeException("can't process a HTTP request without URL set"));
			return;
		}

		final String method = httpRequest.getMethod();		
		final String value = httpRequest.getContent();
		final boolean valueInBody = method.equalsIgnoreCase(HttpMethods.POST) || method.equals(HttpMethods.PUT);

        String url = httpRequest.getUrl();
        if (method.equalsIgnoreCase(HttpMethods.GET)) {
            if (value != null) {
                url += "?" + value;
            }
        } else if (method.equalsIgnoreCase(HttpMethods.POST)) {
        } else if (method.equalsIgnoreCase(HttpMethods.DELETE)) {
            if (value != null) {
                url += "?" + value;
            }
        } else if (method.equalsIgnoreCase(HttpMethods.PUT)) {
        } else {
            throw new GdxRuntimeException("Unsupported HTTP Method");
        }

        XMLHttpRequest request = XMLHttpRequest.create();

        request.setOnReadyStateChange(new ReadyStateChangeHandler() {
            @Override
            public void onReadyStateChange(XMLHttpRequest xhr) {
                if (xhr.getReadyState() == XMLHttpRequest.DONE) {
                    if (xhr.getStatus() != 200) {
                        httpResultListener.failed(new Throwable(xhr.getStatusText()));
                        requests.remove(httpRequest);
                        listeners.remove(httpRequest);
                    } else {
                        Int8Array data = TypedArrays.createInt8Array(xhr.getResponseArrayBuffer());
                        httpResultListener.handleHttpResponse(new HttpClientResponse(xhr.getStatus(), data, xhr));
                        requests.remove(httpRequest);
                        listeners.remove(httpRequest);
                    }
                }
            }
        });
        request.open(method, url);

        Map<String, String> content = httpRequest.getHeaders();
        Set<String> keySet = content.keySet();
        for (String name : keySet) {
            request.setRequestHeader(name, content.get(name));
        }
        request.setResponseType(XMLHttpRequest.ResponseType.ArrayBuffer);

        if (valueInBody)
            request.send(value);
        else
            request.send();


        requests.put(httpRequest, request);
        listeners.put(httpRequest, httpResultListener);
	}

	@Override
	public void cancelHttpRequest (HttpRequest httpRequest) {
		HttpResponseListener httpResponseListener = listeners.get(httpRequest);
		XMLHttpRequest request = requests.get(httpRequest);

		if (httpResponseListener != null && request != null) {
			request.abort();
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
