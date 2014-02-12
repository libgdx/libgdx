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

package com.badlogic.gdx.backends.headless;

import java.awt.Desktop;
import java.awt.GraphicsEnvironment;
import java.awt.Desktop.Action;

import com.badlogic.gdx.Net;
import com.badlogic.gdx.net.NetJavaImpl;
import com.badlogic.gdx.net.ServerSocket;
import com.badlogic.gdx.net.ServerSocketHints;
import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.net.SocketHints;
import com.badlogic.gdx.net.NetJavaSocketImpl;
import com.badlogic.gdx.net.NetJavaServerSocketImpl;
import com.badlogic.gdx.utils.GdxRuntimeException;

/** Headless implementation of the {@link com.badlogic.gdx.Net} API, based on LWJGL implementation
 * @author acoppes
 * @author Jon Renner */
public class HeadlessNet implements Net {

	NetJavaImpl netJavaImpl = new NetJavaImpl();

	@Override
	public void sendHttpRequest (HttpRequest httpRequest, HttpResponseListener httpResponseListener) {
		netJavaImpl.sendHttpRequest(httpRequest, httpResponseListener);
	}

	@Override
	public ServerSocket newServerSocket (Protocol protocol, int port, ServerSocketHints hints) {
		return new NetJavaServerSocketImpl(protocol, port, hints);
	}

	@Override
	public Socket newClientSocket (Protocol protocol, String host, int port, SocketHints hints) {
		return new NetJavaSocketImpl(protocol, host, port, hints);
	}

	@Override
	public void openURI (String URI) {
		try {
			if (!GraphicsEnvironment.isHeadless() && Desktop.isDesktopSupported()) {
				if (Desktop.getDesktop().isSupported(Action.BROWSE)) {
					Desktop.getDesktop().browse(java.net.URI.create(URI));
					return;
				}
			}
		} catch (Throwable t) {
			t.printStackTrace();
			return;
		}
		// don't throw the exception, don't want to kill the app, just let the headless app know it can't open URIs
		Exception e = new GdxRuntimeException("ERROR: cannot open URI on a headless application");
		e.printStackTrace();
	}
}
