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
