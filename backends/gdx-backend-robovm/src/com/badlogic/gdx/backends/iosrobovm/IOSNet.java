package com.badlogic.gdx.backends.iosrobovm;

import org.robovm.cocoatouch.foundation.NSURL;
import org.robovm.cocoatouch.uikit.UIApplication;

import com.badlogic.gdx.Net;
import com.badlogic.gdx.net.NetJavaImpl;
import com.badlogic.gdx.net.ServerSocket;
import com.badlogic.gdx.net.ServerSocketHints;
import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.net.SocketHints;

public class IOSNet implements Net {

	NetJavaImpl netJavaImpl = new NetJavaImpl();
	final UIApplication uiApp;

	public IOSNet (IOSApplication app) {
		uiApp = app.uiApp;
	}

	@Override
	public void sendHttpRequest (HttpRequest httpRequest, HttpResponseListener httpResponseListener) {
		netJavaImpl.sendHttpRequest(httpRequest, httpResponseListener);
	}

	@Override
	public ServerSocket newServerSocket (Protocol protocol, int port, ServerSocketHints hints) {
		return new IOSServerSocket(protocol, port, hints);
	}

	@Override
	public Socket newClientSocket (Protocol protocol, String host, int port, SocketHints hints) {
		return new IOSSocket(protocol, host, port, hints);
	}

	@Override
	public void openURI (String URI) {
		uiApp.openURL(new NSURL(URI));
	}
}