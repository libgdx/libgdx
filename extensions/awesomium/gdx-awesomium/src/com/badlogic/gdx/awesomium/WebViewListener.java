package com.badlogic.gdx.awesomium;

public interface WebViewListener {
	public void onBeginNavigation(String url, String frameName);
	public void onBeginLoading(String url, String frameName, int statusCode, String mimeType);
	public void onFinishLoading();
	public void onCallback(String objectName, String callbackName, JSArguments args);
	public void onReceiveTitle(String title, String frameName);
	public void onChangeTooltip(String tooltip);
	public void onChangeCursor(int cursor);
	public void onChangeKeyboardFocus(boolean isFocused);
	public void onChangeTargetURL(String url);
	public void onOpenExternalLink(String url, String source);
	public void onRequestDownload(String url);
	public void onWebViewCrashed();
	public void onPluginCrashed(String pluginName);
	public void onRequestMove(int x, int y);
	public void onGetPageContents(String url, String contents);
	public void onDOMReady();
}
