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

package com.badlogic.gdx.backends.iosmoe;

import com.badlogic.gdx.Net;
import com.badlogic.gdx.net.NetJavaImpl;
import com.badlogic.gdx.net.NetJavaServerSocketImpl;
import com.badlogic.gdx.net.NetJavaSocketImpl;
import com.badlogic.gdx.net.ServerSocket;
import com.badlogic.gdx.net.ServerSocketHints;
import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.net.SocketHints;
import apple.foundation.NSURL;
import apple.uikit.UIApplication;

public class IOSNet implements Net {

	NetJavaImpl netJavaImpl;
	final UIApplication uiApp;

	public IOSNet (IOSApplication app, IOSApplicationConfiguration configuration) {
		uiApp = app.uiApp;
		netJavaImpl = new NetJavaImpl(configuration.maxNetThreads);
	}

	@Override
	public void sendHttpRequest (HttpRequest httpRequest, HttpResponseListener httpResponseListener) {
		netJavaImpl.sendHttpRequest(httpRequest, httpResponseListener);
	}

	@Override
	public void cancelHttpRequest (HttpRequest httpRequest) {
		netJavaImpl.cancelHttpRequest(httpRequest);
	}
	
	@Override
	public ServerSocket newServerSocket (Protocol protocol, String hostname, int port, ServerSocketHints hints) {
		return new NetJavaServerSocketImpl(protocol, hostname, port, hints);
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
	public boolean openURI (String URI) {
		if (uiApp.canOpenURL(NSURL.URLWithString(URI))) {
			uiApp.openURL(NSURL.URLWithString(URI));
			return true;
		}
		return false;
	}
}
