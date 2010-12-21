package com.badlogic.gdx.awesomium;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;

import com.badlogic.gdx.awesomium.WebViewListenerC.WebViewListenerCStub;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.WString;
import com.sun.jna.ptr.PointerByReference;

public class WebView {
	Pointer webView;
	CharBuffer text;
	CharBuffer unmodifiedText;
	PointerByReference identifierPointer;
	Memory identifier;
	ByteBuffer identifierBuffer;
		
	WebView(Pointer ptr) {
		this.webView = ptr;
		ByteBuffer textBuffer = ByteBuffer.allocateDirect(20);
		ByteBuffer unmodifiedTextBuffer = ByteBuffer.allocateDirect(20);
		identifier = new Memory(20);		
		identifierPointer = new PointerByReference(identifier);
		identifierBuffer = identifier.getByteBuffer(0, 20);
		
		textBuffer.order(ByteOrder.nativeOrder());
		unmodifiedTextBuffer.order(ByteOrder.nativeOrder());		
		
		text = textBuffer.asCharBuffer();
		unmodifiedText = unmodifiedTextBuffer.asCharBuffer();
	}
	
	public void destroy() {
		Awesomium.INSTANCE.awe_WebView_destroy(webView);
	}
	
	public void setListener(WebViewListener listener) {
		WebViewListenerC listenerC = new WebViewListenerC(new WebViewListenerCStub(listener));
		Awesomium.INSTANCE.awe_WebView_setListener(webView, listenerC);
	}
	
	public void loadURL(String url, String frameName, String username, String password) {
		Awesomium.INSTANCE.awe_WebView_loadURLW(webView, new WString(url), new WString(frameName), username, password);
	}
	
	public void loadHTML(String html, String frameName) {
		Awesomium.INSTANCE.awe_WebView_loadHTMLW(webView, new WString(html), new WString(frameName));
	}
	
	public void loadFile(String file, String frameName) {
		Awesomium.INSTANCE.awe_WebView_loadFile(webView, file, new WString(frameName));
	}
	
	public void goToHistoryOffset(int offset) {
		Awesomium.INSTANCE.awe_WebView_goToHistoryOffset(webView, offset);
	}
	
	public void stop() {
		Awesomium.INSTANCE.awe_WebView_stop(webView);
	}
	
	public void reload() {
		Awesomium.INSTANCE.awe_WebView_reload(webView);
	}
	
	public void executeJavascript(String javascript, String frameName) {
		Awesomium.INSTANCE.awe_WebView_executeJavascriptW(webView, new WString(javascript), new WString(frameName));
	}
	
	public JSValue executeJavascriptWithResult(String javascript, String frameName) {
		// FIXME
		throw new UnsupportedOperationException("not implemented yet");
	}
	
	public JSValue executeJavascriptWithResult(String javascript, String frameName, int timeoutMS) {
		// FIXME
		throw new UnsupportedOperationException("not implemented yet");		
	}
	
	public void callJavascriptFunction(String object, String function, JSArguments args, String frameName) {
		// FIXME
		throw new UnsupportedOperationException("not implemented yet");		
	}
	
	public void createObject(String objectName) {
		Awesomium.INSTANCE.awe_WebView_createObject(webView, new WString(objectName));
	}
	
	public void destroyObject(String objectName) {
		Awesomium.INSTANCE.awe_WebView_destroyObject(webView, new WString(objectName));
	}
	
	public void setObjectProperty(String objectName, String propName, JSValue value) {
		// FIXME
		throw new UnsupportedOperationException("not implemented yet");		
	}
	
	public void setObjectCallback(String objectName, String callbackName) {
		Awesomium.INSTANCE.awe_WebView_setObjectCallback(webView, new WString(objectName), new WString(callbackName));
	}
	
	public boolean isLoadingPage() {
		return Awesomium.INSTANCE.awe_WebView_isLoadingPage(webView)!=0?true:false;
	}
	
	public boolean isDirty() {
		return Awesomium.INSTANCE.awe_WebView_isDirty(webView)!=0?true:false;
	}
	
	public Rect getDirtyBounds() {
		// FIXME
		throw new UnsupportedOperationException("not implemented yet");		
	}
	
	public RenderBuffer render() {
		Pointer renderBuffer = Awesomium.INSTANCE.awe_WebView_render(webView);
		if(Pointer.nativeValue(renderBuffer) == 0)
			return null;
		
		return new RenderBuffer(renderBuffer);
	}
	
	public void pauseRendering() {
		Awesomium.INSTANCE.awe_WebView_pauseRendering(webView);
	}
	
	public void resumeRendering() {
		Awesomium.INSTANCE.awe_WebView_resumeRendering(webView);
	}
	
	public void injectMouseMove(int x, int y) {
		Awesomium.INSTANCE.awe_WebView_injectMouseMove(webView, x, y);
	}
	
	public void injectMouseDown(MouseButton button) {
		int buttonValue = Awesomium.AWE_LEFT_BUTTON;
		if(button == MouseButton.Right)
			buttonValue = Awesomium.AWE_RIGHT_BUTTON;
		if(button == MouseButton.Middle)
			buttonValue = Awesomium.AWE_MIDDLE_BUTTON;
		
		Awesomium.INSTANCE.awe_WebView_injectMouseDown(webView, buttonValue);
	}
	
	public void injectMouseUp(MouseButton button) {
		int buttonValue = Awesomium.AWE_LEFT_BUTTON;
		if(button == MouseButton.Right)
			buttonValue = Awesomium.AWE_RIGHT_BUTTON;
		if(button == MouseButton.Middle)
			buttonValue = Awesomium.AWE_MIDDLE_BUTTON;
		
		Awesomium.INSTANCE.awe_WebView_injectMouseUp(webView, buttonValue);
	}
	
	public void injectMouseWheel(int scrollAmount) {
		Awesomium.INSTANCE.awe_WebView_injectMouseWheel(webView, scrollAmount);
	}
	
	public void injectKeyDown(int virtualKeyCode, int modifiers, boolean isSystemKey) {
		Awesomium.INSTANCE.awe_getKeyIdentifierFromVirtualKeyCode(virtualKeyCode, identifierPointer);
		Awesomium.INSTANCE.awe_WebView_injectKeyboardEventArgs(webView, Awesomium.AWE_TYPE_KEY_DOWN, modifiers, virtualKeyCode, virtualKeyCode, identifierBuffer, text, unmodifiedText, isSystemKey?-1:0);
	}
	
	public void injectKeyUp(int virtualKeyCode, int modifiers, boolean isSystemKey) {
		Awesomium.INSTANCE.awe_getKeyIdentifierFromVirtualKeyCode(virtualKeyCode, identifierPointer);
		Awesomium.INSTANCE.awe_WebView_injectKeyboardEventArgs(webView, Awesomium.AWE_TYPE_KEY_UP, modifiers, virtualKeyCode, virtualKeyCode, identifierBuffer, text, unmodifiedText, isSystemKey?-1:0);
	}
	
	public void injectKeyTyped(char character) {
		Awesomium.INSTANCE.awe_WebView_injectKeyboardEventCharacter(webView, character);
	}
	
	public void cut() {
		Awesomium.INSTANCE.awe_WebView_cut(webView);
	}
	
	public void copy() {
		Awesomium.INSTANCE.awe_WebView_copy(webView);
	}
	
	public void paste() {
		Awesomium.INSTANCE.awe_WebView_paste(webView);
	}
	
	public void selectAll() {
		Awesomium.INSTANCE.awe_WebView_selectAll(webView);
	}
	
	public void setZoom(int zoomPercent) {
		Awesomium.INSTANCE.awe_WebView_setZoom(webView, zoomPercent);
	}
	
	public void resetZoom() {
		Awesomium.INSTANCE.awe_WebView_resetZoom(webView);
	}
	
	public void resize(int width, int height, boolean waitForRepaint, int repaintTimeoutMS) {
		Awesomium.INSTANCE.awe_WebView_resize(webView, width, height, waitForRepaint?-1:0, repaintTimeoutMS);
	}
	
	public boolean isResizing() {
		return Awesomium.INSTANCE.awe_WebView_isResizing(webView)!=0?true:false;
	}
	
	public void unfocus() {
		Awesomium.INSTANCE.awe_WebView_unfocus(webView);
	}
	
	public void focus() {
		Awesomium.INSTANCE.awe_WebView_focus(webView);
	}
	
	public void setTransparent(boolean isTransparent) {
		Awesomium.INSTANCE.awe_WebView_setTransparent(webView, isTransparent?-1:0);
	}
	
	public void setURLFilteringMode(URLFilteringMode mode) {
		int modeValue = 0;
		if(mode == URLFilteringMode.Blacklist)
			modeValue = 1;
		if(mode == URLFilteringMode.Whitelist)
			modeValue = 2;
		Awesomium.INSTANCE.awe_WebView_setURLFilteringMode(webView, modeValue);
	}
	
	public void addURLFilter(String filter) {
		Awesomium.INSTANCE.awe_WebView_addURLFilter(webView, new WString(filter));
	}
	
	public void clearAllURLFilters() {
		Awesomium.INSTANCE.awe_WebView_clearAllURLFilters(webView);
	}	
}
