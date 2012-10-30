package com.badlogic.gdx.backends.gwt;

import com.badlogic.gdx.Net;
import com.badlogic.gdx.Net.HttpMethod;
import com.badlogic.gdx.Net.HttpRequest;
import com.badlogic.gdx.Net.HttpResponseListener;
import com.badlogic.gdx.Net.Protocol;
import com.badlogic.gdx.net.ServerSocket;
import com.badlogic.gdx.net.ServerSocketHints;
import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.net.SocketHints;
import com.google.gwt.user.client.Window;

public class GwtNet implements Net {
	
	@Override
	public HttpRequest createHttpRequest (HttpMethod httpMethod) {
		return null;
	}
	
	@Override
	public void processHttpRequest (HttpRequest httpRequest, HttpResponseListener httpResultListener) {
		throw new UnsupportedOperationException("Not implemented");
	}
	
	@Override
	public void processHttpRequest (HttpRequest httpRequest) {
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
