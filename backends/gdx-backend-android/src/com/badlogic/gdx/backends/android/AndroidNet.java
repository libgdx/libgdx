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

import android.content.Intent;
import android.net.Uri;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.Net.HttpRequest;
import com.badlogic.gdx.net.NetJavaImpl;
import com.badlogic.gdx.net.ServerSocket;
import com.badlogic.gdx.net.ServerSocketHints;
import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.net.SocketHints;
import com.badlogic.gdx.net.NetJavaSocketImpl;
import com.badlogic.gdx.net.NetJavaServerSocketImpl;

/** Android implementation of the {@link Net} API.
 * @author acoppes */
public class AndroidNet implements Net {

	// IMPORTANT: The Gdx.net classes are a currently duplicated for JGLFW/LWJGL + Android!
	// If you make changes here, make changes in the other backend as well.
	final AndroidApplicationBase app;
	NetJavaImpl netJavaImpl;

	public AndroidNet (AndroidApplicationBase activity) {
		app = activity;
		netJavaImpl = new NetJavaImpl();
	}

	@Override
	public void sendHttpRequest (HttpRequest httpRequest, final HttpResponseListener httpResponseListener) {
		netJavaImpl.sendHttpRequest(httpRequest, httpResponseListener);
	}

	@Override
	public void cancelHttpRequest (HttpRequest httpRequest) {
		netJavaImpl.cancelHttpRequest(httpRequest);
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
		if (app == null) {
			Gdx.app.log("AndroidNet", "Can't open browser activity from livewallpaper");
			return;
		}
		final Uri uri = Uri.parse(URI);
		app.runOnUiThread(new Runnable() {
			@Override
			public void run () {
				app.startActivity(new Intent(Intent.ACTION_VIEW, uri));
			}
		});
	}

}
