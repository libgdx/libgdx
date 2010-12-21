package com.badlogic.gdx.awesomium;

public class SimpleTest {
	public static void main(String[] argv) {
		WebCore webCore = new WebCore();
		WebView webView = webCore.createWebView(512, 512);
		
		webView.loadURL("http://www.google.at", "", "", "");
		
		while(webView.isLoadingPage()) {
			webCore.update();
		}
		
		RenderBuffer renderBuffer = webView.render();
		if(renderBuffer != null) {
			renderBuffer.saveToPNG("result.png", true);
		}
		
		webView.destroy();
		webCore.dispose();
	}
}
