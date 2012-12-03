package com.badlogic.gdx.backends.gwt;

import com.badlogic.gdx.Net;
import com.badlogic.gdx.net.ServerSocket;
import com.badlogic.gdx.net.ServerSocketHints;
import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.net.SocketHints;
import com.google.gwt.user.client.Window;

public class GwtNet implements Net {
	
	@Override
	public void sendHttpRequest (HttpRequest httpRequest, HttpResponseListener httpResultListener) {
		throw new UnsupportedOperationException("Not implemented");
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
	public void openURI(String URI) {
		Window.open(URI, "_blank", null);
	}
}
