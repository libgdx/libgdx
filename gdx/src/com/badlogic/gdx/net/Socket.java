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

import java.io.InputStream;
import java.io.OutputStream;

import com.badlogic.gdx.Net;
import com.badlogic.gdx.Net.Protocol;
import com.badlogic.gdx.utils.Disposable;

/** A client socket that talks to a server socket via some {@link Protocol}. See
 * {@link Net#newClientSocket(Protocol, String, int, SocketHints)} and
 * {@link Net#newServerSocket(Protocol, int, ServerSocketHints)}.</p>
 * 
 * A socket has an {@link InputStream} used to send data to the other end of the connection, and an {@link OutputStream} to
 * receive data from the other end of the connection.</p>
 * 
 * A socket needs to be disposed if it is no longer used. Disposing also closes the connection.
 * 
 * @author mzechner */
public interface Socket extends Disposable {
	/** @return whether the socket is connected */
	public boolean isConnected ();

	/** @return the {@link InputStream} used to read data from the other end of the connection. */
	public InputStream getInputStream ();

	/** @return the {@link OutputStream} used to write data to the other end of the connection. */
	public OutputStream getOutputStream ();

	/** @return the RemoteAddress of the Socket as String */
	public String getRemoteAddress ();
}
