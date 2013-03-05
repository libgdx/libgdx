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

package com.badlogic.gdx.backends.jglfw;

import com.badlogic.gdx.Net;
import com.badlogic.gdx.net.NetJavaImpl;
import com.badlogic.gdx.net.ServerSocket;
import com.badlogic.gdx.net.ServerSocketHints;
import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.net.SocketHints;
import com.badlogic.gdx.utils.GdxRuntimeException;

import java.awt.Desktop;
import java.net.URI;

/** Desktop implementation of the {@link Net} API.
 * @author acoppes */
public class JglfwNet implements Net {
	NetJavaImpl netJavaImpl = new NetJavaImpl();

	public void sendHttpRequest (HttpRequest httpRequest, HttpResponseListener httpResponseListener) {
		netJavaImpl.sendHttpRequest(httpRequest, httpResponseListener);
	}

	public ServerSocket newServerSocket (Protocol protocol, int port, ServerSocketHints hints) {
		return new JglfwServerSocket(protocol, port, hints);
	}

	public Socket newClientSocket (Protocol protocol, String host, int port, SocketHints hints) {
		return new JglfwSocket(protocol, host, port, hints);
	}

	public void openURI (String uri) {
		if (!Desktop.isDesktopSupported()) return;

		Desktop desktop = Desktop.getDesktop();
		if (!desktop.isSupported(Desktop.Action.BROWSE)) return;

		try {
			desktop.browse(new URI(uri));
		} catch (Exception ex) {
			throw new GdxRuntimeException(ex);
		}
	}
}
