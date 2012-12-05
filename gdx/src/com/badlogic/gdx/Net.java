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

package com.badlogic.gdx;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

import com.badlogic.gdx.Net.HttpRequest;
import com.badlogic.gdx.Net.HttpResponseListener;
import com.badlogic.gdx.net.ServerSocketHints;
import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.net.ServerSocket;
import com.badlogic.gdx.net.SocketHints;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.JsonWriter;

/** Provides methods to perform networking operations, such as simple HTTP get and post requests, and TCP server/client socket
 * communication.</p>
 * 
 * To perform an HTTP request create a {@link HttpRequest} with the HTTP method (see {@link HttpMethods} for common methods) and
 * invoke {@link #sendHttpRequest(HttpRequest, HttpResponseListener)} with it and a {@link HttpResponseListener}. After the HTTP
 * request was processed, the {@link HttpResponseListener} is called with a {@link HttpResponse} with the HTTP response values and
 * an status code to determine if the request was successful or not.
 * 
 * To create a TCP client socket to communicate with a remote TCP server, invoke the
 * {@link #newClientSocket(Protocol, String, int, SocketHints)} method. The returned {@link Socket} offers an {@link InputStream}
 * and {@link OutputStream} to communicate with the end point.</p>
 * 
 * To create a TCP server socket that waits for incoming connections, invoke the
 * {@link #newServerSocket(Protocol, int, ServerSocketHints)} method. The returned {@link ServerSocket} offers an
 * {@link ServerSocket#accept(SocketHints options)} method that waits for an incoming connection.
 * 
 * @author mzechner
 * @author noblemaster
 * @author arielsan */
public interface Net {

	/** Contains information about the HTTP status line returned with the {@link HttpResponse} after a {@link HttpRequest} was
	 * performed. */
	public static class HttpStatus {

		int statusCode;

		/** Returns the status code of the HTTP response, normally 2xx status codes indicate success while 4xx and 5xx indicate
		 * client and server errors, respectively (see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html">HTTP/1.1:
		 * Status Code Definitions</a> for more information about HTTP status codes). */
		public int getStatusCode () {
			return statusCode;
		}

		public HttpStatus (int statusCode) {
			this.statusCode = statusCode;
		}

	}

	/** HTTP response interface with methods to get the response data as a byte[], a {@link String} or an {@link InputStream}. */
	public static interface HttpResponse {
		/** Returns the data of the HTTP response as a byte[].
		 * @return the result as a byte[] or null in case of a timeout or if the operation was canceled/terminated abnormally. The
		 *         timeout is specified when creating the HTTP request, with {@link HttpRequest#setTimeOut(long)} */
		byte[] getResult ();

		/** Returns the data of the HTTP response as a {@link String}.
		 * @return the result as a string or null in case of a timeout or if the operation was canceled/terminated abnormally. The
		 *         timeout is specified when creating the HTTP request, with {@link HttpRequest#setTimeOut(long)} */
		String getResultAsString ();

		/** Returns the data of the HTTP response as an {@link InputStream}.
		 * @return An {@link InputStream} with the {@link HttpResponse} data. */
		InputStream getResultAsStream ();

		/** Returns the {@link HttpStatus} containing the statusCode of the HTTP response. */
		HttpStatus getStatus ();
	}

	/** Provides common HTTP methods to use when creating a {@link HttpRequest}. 
	 * <ul><li>GET</li><li>POST</li></ul>*/
	public static interface HttpMethods {

		public static final String GET = "GET";

		public static final String POST = "POST";
		
		public static final String JSON = "JSON";

	}

	/** 
	 * Contains getters and setters for the following parameters: 
	 * <ul><li><strong>httpMethod:</strong> GET or POST are most common, can use {@link Net.HttpMethods HttpMethods} for static references</li>
	 * <li><strong>url:</strong> the url</li>
	 * <li><strong>headers:</strong> a map of the headers, setter can be called multiple times</li>
	 * <li><strong>timeout:</strong> time spent trying to connect before giving up</li>
	 * <li><strong>content:</strong> Map used for both POST, GET, or JSON. </li></ul>
	 * 
	 * Abstracts the concept of a HTTP Request:
	 * 
	 * <pre>
	 * HttpRequest httpGet = new HttpRequest(HttpMethods.Get);
	 * httpGet.setUrl("http://somewhere.net");
	 * httpGet.setContent("user", "MyUsername");
	 * httpGet.setContent("password", "P4ssw0rd!1234")
	 * ...
	 * Gdx.net.sendHttpRequest (httpGet, new HttpResponseListener() {
	 * 	public void handleHttpResponse(HttpResponse httpResponse) {
	 * 		status = httpResponse.getResultAsString();
	 * 		//do stuff here based on response
	 * 	}
	 * 
	 * 	public void failed(Throwable t) {
	 * 		status = "failed";
	 * 		//do stuff here based on the failed attempt
	 * 	}
	 * });
	 * </pre> 
	 * 
	 * PHP for POST should store values in $_POST,<br>
	 * PHP for GET should store values in $_GET,<br>
	 * PHP to retrieve JSON is as follows:
	 * <pre>
	 * $requestBody = file_get_contents('php://input');
	 * $requestBody = json_decode($requestBody);
	 * // and to access variable "user"
	 * $requestBody->user
	 * </pre>*/
	public static class HttpRequest {

		private final String httpMethod;
		private String url;
		private Map<String, String> headers;
		private int timeOut = -1;

		private Map<String, Object> content;
		
		/**
		 * When this is created, if POST or GET are selected as the httpMethod, this will set up the following default parameters:
		 * <br><br><strong>POST:</strong>
		 * <br><i>headers</i> - Accept=application/x-www-form-urlencoded
		 * <br><i>headers</i> - Content-Type=application/x-www-form-urlencoded
		 * <br><i>timeout</i> - 30000 milliseconds (30 seconds)
		 * <br><br><strong>JSON:</strong>
		 * <br><i>headers</i> - Accept=application/json
		 * <br><i>headers</i> - Content-Type=application/json
		 * <br><i>timeout</i> - 30000 milliseconds (30 seconds)
		 * <br><br><strong>GET:</strong>
		 * <br><i>headers</i> - Content-Type=text/html; charset=UTF-8
		 * <br><i>timeout</i> - 30000 milliseconds (30 seconds)
		 * <br><br>
		 * @param httpMethod	This is the method for the request, usually HttpMethods.POST or HttpMethods.GET */
		public HttpRequest (String httpMethod) {
			this.httpMethod = httpMethod;
			this.headers = new HashMap<String, String>();
			this.content = new HashMap<String, Object>();
			
			// Setting the default parameters for POST, JSON, and GET
			if(this.httpMethod == HttpMethods.POST) {
				setHeader("Accept", "application/x-www-form-urlencoded");
            setHeader("Content-type", "application/x-www-form-urlencoded");
            setTimeOut(30000);
			} 
			else if(this.httpMethod == HttpMethods.JSON) {
				setHeader("Accept", "application/json");
            setHeader("Content-type", "application/json");
            setTimeOut(30000);
			}
			else if(this.httpMethod == HttpMethods.GET){
				setHeader("Content-type", "text/html; charset=UTF-8");
            setTimeOut(30000);
			}
		}

		/** Sets the URL of the HTTP request.
		 * @param url The URL to set. */
		public void setUrl (String url) {
			this.url = url;
		}
		
		/** Sets a header to this HTTP request. Headers definition could be found at <a
		 * href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html">HTTP/1.1: Header Field Definitions</a> document.
		 * @param name the name of the header.
		 * @param value the value of the header. */
		public void setHeader (String name, String value) {
			headers.put(name, value);
		}

		/** Adds name->value to the content. Can be called multiple times. 
		 * @param name the name of the content, Example: "password"
		 * @param value the value of the content, Example: "P4ssw0rd!1234"
		 */
		public void setContent (String name, Object value) {
			content.put(name, value);
		}
		
		/** Sets the time to wait for the HTTP request to be processed, use 0 block until it is done. The timeout defaults
		 * to 30000 milliseconds, and is used for both the timeout when establishing TCP connection, and the timeout until
		 * the first byte of data is received. 
		 * @param timeOut the number of milliseconds to wait before giving up, 0 to block until the operation is done */
		public void setTimeOut (int timeOut) {
			this.timeOut = timeOut;
		}

		/** Returns the HTTP method of the HttpRequest. */
		public String getMethod () {
			return httpMethod;
		}

		/** Returns the URL of the HTTP request. */
		public String getUrl () {
			return url;
		}

		/** Returns the content as a HashMap<String,Object> */
		public Map<String,Object> getContent () {
			return content;
		}

		/** Returns a Map<String, String> with the headers of the HTTP request. */
		public Map<String, String> getHeaders () {
			return headers;
		}
		
		/** Returns the timeOut set for this httpRequest. If not set, defaults to 30000 milliseconds */
		public int getTimeout () {
			return (timeOut<=0)? 30000 : timeOut;
		}

		/** This function takes the set content and converts it into a string based on the request method. 
		 * @param httpRequest An HttpRequest ready to be executed
		 * @return String formatted in the style based on httpRequest.getMethod()
		 * @throws IOException
		 */
		public String convertHttpRequest() {
			if (this.getMethod().equalsIgnoreCase(HttpMethods.GET) || this.getMethod().equalsIgnoreCase(HttpMethods.POST)) {
				Map<String,Object> content = this.getContent();
				Set<String> keySet = content.keySet();
				String appendUrl = "";
				for (String name : keySet) {
					appendUrl += name+"="+content.get(name)+"&";
				}
				return appendUrl;
			} 
			else if (this.getMethod().equalsIgnoreCase(HttpMethods.JSON)){
				StringWriter jsonText = new StringWriter();
				JsonWriter writer = new JsonWriter(jsonText);
				
				try {
					this.createJson(this.getContent(), "", writer);
				} catch (IOException e) {
					return "";
				}
				return jsonText.toString();
			}
			return null;
		}
		
		/** Run this to fill writer with JSON based on the content */
		private void createJson (Object content, String name, JsonWriter writer) throws IOException {
			if(content instanceof Map){
				if(name == "")
					writer.object();
				else 
					writer.object(name);
				Set<String> keySet = ((Map<String,?>) content).keySet();
				for(String key : keySet){
					createJson(((Map)content).get(key), key, writer);
				}
				writer.pop();
			} else if (content instanceof Object[]){
				if(name == "")
					writer.array();
				else 
					writer.array(name);
				for(Object key : (Object[])content) {
					createJson(key, "", writer);
				}
				writer.pop();
			} else {
				if(name == "")
					writer.value(content);
				else
					writer.set(name, content);
			}
		}
	}

	/** Listener to be able to do custom logic once the {@link HttpResponse} is ready to be processed, register it with
	 * {@link Net#sendHttpRequest(HttpRequest, HttpResponseListener)}. */
	public static interface HttpResponseListener {

		/** Called when the {@link HttpRequest} has been processed and there is a {@link HttpResponse} ready. {@link HttpResponse}
		 * contains the {@link HttpStatus} and should be used to determine if the request was successful or not (see more info at
		 * {@link HttpStatus#getStatusCode()}). For example:
		 * 
		 * <pre>
		 *  HttpResponseListener listener = new HttpResponseListener() {
		 *  	public void handleHttpResponse (HttpResponse httpResponse) {
		 *  		HttpStatus status = httpResponse.getStatus();
		 *  		if (status.getStatusCode() >= 200 && status.getStatusCode() < 300) {
		 *  			// it was successful
		 *  		} else {
		 *  			// do something else
		 *  		}
		 *  	}
		 *  }
		 * </pre>
		 * 
		 * @param httpResponse The {@link HttpResponse} with the HTTP response values. */
		void handleHttpResponse (HttpResponse httpResponse);

		/** Called if the {@link HttpRequest} failed because an exception when processing the HTTP request, could be a timeout any
		 * other reason (not an HTTP error).
		 * @param t If the HTTP request failed because an Exception, t encapsulates it to give more information. */
		void failed (Throwable t);

	}

	/** Process the specified {@link HttpRequest} and reports the {@link HttpResponse} to the specified {@link HttpResponseListener}
	 * .
	 * @param httpRequest The {@link HttpRequest} to be performed.
	 * @param httpResponseListener The {@link HttpResponseListener} to call once the HTTP response is ready to be processed. Could
	 *           be null, in that case no listener is called. */
	public void sendHttpRequest (HttpRequest httpRequest, HttpResponseListener httpResponseListener);

	/** Protocol used by {@link Net#newServerSocket(Protocol, int, ServerSocketHints)} and
	 * {@link Net#newClientSocket(Protocol, String, int, SocketHints)}.
	 * @author mzechner */
	public enum Protocol {
		TCP
	}

	/** Creates a new server socket on the given port, using the given {@link Protocol}, waiting for incoming connections.
	 * 
	 * @param port the port to listen on
	 * @param hints additional {@link ServerSocketHints} used to create the socket. Input null to use the default setting provided
	 *           by the system.
	 * @return the {@link ServerSocket}
	 * @throws GdxRuntimeException in case the socket couldn't be opened */
	public ServerSocket newServerSocket (Protocol protocol, int port, ServerSocketHints hints);

	/** Creates a new TCP client socket that connects to the given host and port.
	 * 
	 * @param host the host address
	 * @param port the port
	 * @param hints additional {@link SocketHints} used to create the socket. Input null to use the default setting provided by the
	 *           system.
	 * @return GdxRuntimeException in case the socket couldn't be opened */
	public Socket newClientSocket (Protocol protocol, String host, int port, SocketHints hints);

	/** Launches the default browser to display a URI. If the default browser is not able to handle the specified URI, the
	 * application registered for handling URIs of the specified type is invoked. The application is determined from the protocol
	 * and path of the URI.
	 * 
	 * @param URI the URI to be opened. */
	public void openURI (String URI);
}
