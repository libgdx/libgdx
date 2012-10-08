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

import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Future;

import com.badlogic.gdx.net.ServerSocketHints;
import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.net.ServerSocket;
import com.badlogic.gdx.net.SocketHints;
import com.badlogic.gdx.utils.GdxRuntimeException;

/**
 * Provides methods to perform networking operations, such as simple HTTP get and post
 * requests, and TCP server/client socket communication.</p> 
 * 
 * To perform an HTTP Get or Post request, invoke the methods {@link #httpGet(String, String...)}
 * and {@link #httpPost(String, String, byte[])}. Both methods return a {@link HttpResult} which
 * provides methods to query the progress and data returned by the operations. The {@link HttpResult}
 * works like a {@link Future} in that the operation is executed asynchronously, while the API client
 * can use the {@link HttpResult} to poll for the status and result of the operation.</p>
 * 
 * To create a TCP client socket to communicate with a remote TCP server, invoke the {@link #newClientSocket(Protocol, String, int, SocketHints)}
 * method. The returned {@link Socket} offers an {@link InputStream} and {@link OutputStream} to
 * communicate with the end point.</p>
 * 
 * To create a TCP server socket that waits for incoming connections, invoke the {@link #newServerSocket(Protocol, int, ServerSocketHints)}
 * method. The returned {@link ServerSocket} offers an {@link ServerSocket#accept(SocketHints options)} method
 * that waits for an incoming connection.
 * 
 * @author mzechner
 * @author noblemaster
 */
public interface Net {
	
	/**
	 * {@link Future} like interface used with the HTTP get
	 * and post methods. Allows to cancel the operation, 
	 * get it's current progress and return the result
	 * as a byte array or string. Implementations must be thread-safe.
	 * 
	 * @author mzechner
	 */
	public interface HttpResult {
		/**
		 * @return true in case the operation was completed normally or abnormally (cancelled, exception). 
		 */
		public boolean isDone();
		
		/**
		 * @return true in case the operation was cancelled or terminated abnormally, e.g. due to an exception.
		 */
		public boolean isAborted();
		
		/**
		 * @return an estimate of the progress as a number between 0.0 and 1.0. This estimate might be unreliable.
		 */
		public float getProgress();
		
		/**
		 * Cancels the operation. If the operation was already
		 * canceled or completed, this method has no effect. The
		 * operation will not block. This method may or may not 
		 * work depending on the implementation of the operation.
		 */
		public void cancel();
		
		/**
		 * @param timeOut the number of milliseconds to wait before giving up, 0 to block until the operation is done
		 * @return the result as a byte array or null in case of a timeout or if the operation was canceled/terminated abnormally.
		 */
		public byte[] getResult(int timeOut);
		
		/**
		 * @param timeOut the number of milliseconds to wait before giving up, 0 to block until the operation is done
		 * @return the result as a string or null in case of a timeout or if the operation was canceled/terminated abnormally.
		 */
		public String getResultAsString(int timeOut);
	}
	
	/**
	 * Performs an HTTP Get request using the given URL and parameters. The
	 * parameters are passed in as an array where two subsequent entries are
	 * a key/value pair. The keys and values are URL encoded automatically.
	 * 
	 * @param url the URL to perform the Get request on
	 * @param parameters the parameters
	 * @return the {@link HttpResult}
	 */
	public HttpResult httpGet(String url, String ... parameters);
	
	/**
	 * Performs an HTTP Put request using the given URL and content. A
	 * <a href="http://en.wikipedia.org/wiki/MIME_type">MIME-type</a> has
	 * to be given for the content.
	 * 
	 * @param url the URL to perform the Post request on
	 * @param contentType the MIME type of the content send to the server
	 * @param content the content
	 * @return the {@link HttpResult}
	 */
	public HttpResult httpPost(String url, String contentType, byte[] content);
	
	/**
	 * Protocol used by {@link Net#newServerSocket(Protocol, int, ServerSocketHints)} and
	 * {@link Net#newClientSocket(Protocol, String, int, SocketHints)}.
	 * @author mzechner
	 *
	 */
	public enum Protocol {
		TCP
	}
	
	/**
	 * Creates a new server socket on the given port, using the given {@link Protocol}, 
	 * waiting for incoming connections.
	 * 
	 * @param port the port to listen on
	 * @param hints additional {@link ServerSocketHints} used to create the socket. Input null to
	 *        use the default setting provided by the system.
	 * @return the {@link ServerSocket}
	 * @throws GdxRuntimeException in case the socket couldn't be opened
	 */
	public ServerSocket newServerSocket(Protocol protocol, int port, ServerSocketHints hints);
	
	/**
	 * Creates a new TCP client socket that connects to the given host and port. 
	 * 
	 * @param host the host address
	 * @param port the port
	 * @param hints additional {@link SocketHints} used to create the socket. Input null to
	 *        use the default setting provided by the system.
	 * @return GdxRuntimeException in case the socket couldn't be opened
	 */
	public Socket newClientSocket(Protocol protocol, String host, int port, SocketHints hints);
}
