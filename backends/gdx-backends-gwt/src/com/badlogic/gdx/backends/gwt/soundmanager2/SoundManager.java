package com.badlogic.gdx.backends.gwt.soundmanager2;


public class SoundManager {
	public interface SoundManagerCallback {
		public void loaded();
		public void error();
	}
	
	public static final native SoundManager getInstance() /*-{
		return $wnd.soundManager;
	}-*/;
	
	public static native String getVersion() /*-{
		return $wnd.soundManager.version;
	}-*/;
	
	public static native String getUrl() /*-{
		return $wnd.soundManager.url;
	}-*/;
	
	public static native void setUrl(String url) /*-{
		$wnd.soundManager.url = url;
	}-*/;
	
	public static native void setDebugMode(boolean debug) /*-{
		$wnd.soundManager.debugMode = debug;
	}-*/;
	
	public static native boolean getDebugMode() /*-{
		return $wnd.soundManager.debugMode;
	}-*/;
	
	public static native void setFlashVersion(int version) /*-{
		$wnd.soundManager.flashVersion = version;
	}-*/;
	
	public static native int getFlashVersion() /*-{
		return $wnd.soundManager.flashVersion;
	}-*/;
	
	public static native SMSound createSound(String id, String url) /*-{
		return $wnd.soundManager.createSound(id, url);
	}-*/;
	
	public static native void reboot() /*-{
		$wnd.soundManager.reboot();
	}-*/;
	
	public static native boolean swfLoaded() /*-{
		return $wnd.soundManager.swfLoaded;
	}-*/;

	public static native void init (String moduleBaseURL, int flashVersion) /*-{
		$wnd.SM2_DEFER = true;
		$wnd.soundManager = new $wnd.SoundManager();
		$wnd.soundManager.url = moduleBaseURL;
		$wnd.soundManager.flashVersion = flashVersion;
		$wnd.soundManager.beginDelayedInit()
	}-*/;
}
