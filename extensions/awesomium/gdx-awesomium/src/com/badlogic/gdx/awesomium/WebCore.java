package com.badlogic.gdx.awesomium;

import com.sun.jna.Pointer;
import com.sun.jna.WString;

public class WebCore {
	final Pointer webCore;
	
	public WebCore() {
		webCore = Awesomium.INSTANCE.awe_WebCore_new();
	}
	
	public WebCore(String pluginPath) {
		webCore = Awesomium.INSTANCE.awe_WebCore_newWithPlugins(new WString(pluginPath));
	}	
	
	public void dispose() {
		Awesomium.INSTANCE.awe_WebCore_delete(webCore);
	}
	
	public void setBaseDirectory(String baseDirectory) {
		Awesomium.INSTANCE.awe_WebCore_setBaseDirectory(webCore, baseDirectory);
	}
	
	public WebView createWebView(int width, int height) {
		Pointer ptr = Awesomium.INSTANCE.awe_WebCore_createWebView(webCore, width, height);
		return new WebView(ptr);
	}
	
	public void setCustomResponsePage(int statusCode, String filePath) {
		Awesomium.INSTANCE.awe_WebCore_setCustomResponsePage(webCore, statusCode, new WString(filePath));
	}
	
	public void update() {
		Awesomium.INSTANCE.awe_WebCore_update(webCore);
	}
	
	public String getBaseDirectory() {
		return new String(Awesomium.INSTANCE.awe_WebCore_getBaseDirectory(webCore).toString());
	}
	
	public boolean arePluginsEnabled() {
		return Awesomium.INSTANCE.awe_WebCore_arePluginsEnabled(webCore) != 0;
	}
	
	public void clearCache() {
		Awesomium.INSTANCE.awe_WebCore_clearCache(webCore);
	}
	
	public void clearCookies() {
		Awesomium.INSTANCE.awe_WebCore_clearCookies(webCore);
	}
	
	public void setCookie(String url, String cookieString, boolean isHTTPOnly, boolean forceSessionCookie) {
		Awesomium.INSTANCE.awe_WebCore_setCookie(webCore, url, cookieString, isHTTPOnly?-1:0, forceSessionCookie?-1:0);
	}
}
