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

package com.badlogic.gdx.backends.android;

import com.badlogic.gdx.Net;
import com.badlogic.gdx.Net.HttpResult;
import com.badlogic.gdx.Net.Protocol;
import com.badlogic.gdx.net.ServerSocket;
import com.badlogic.gdx.net.ServerSocketHints;
import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.net.SocketHints;

public class AndroidNet implements Net {
	
	// IMPORTANT: The Gdx.net classes are a currently duplicated for LWJGL + Android!
	//            If you make changes here, make changes in the other backend as well.

	@Override
	public HttpResult httpGet (String url, String... parameters) {
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public HttpResult httpPost (String url, String contentType, byte[] content) {
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public ServerSocket newServerSocket (Protocol protocol, int port, ServerSocketHints hints) {
		return new AndroidServerSocket(protocol, port, hints);
	}

	@Override
	public Socket newClientSocket (Protocol protocol, String host, int port, SocketHints hints) {
		return new AndroidSocket(protocol, host, port, hints);
	}
}
