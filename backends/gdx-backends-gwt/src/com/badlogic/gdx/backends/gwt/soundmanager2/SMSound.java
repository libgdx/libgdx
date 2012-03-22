package com.badlogic.gdx.backends.gwt.soundmanager2;

import com.google.gwt.core.client.JavaScriptObject;

public class SMSound extends JavaScriptObject {
	protected SMSound () {
	}
	
	public native final void destruct() /*-{
		this.destruct();
	}-*/;
	
	public native final int getPosition() /*-{
		return this.position;
	}-*/;
	
	public native final void pause() /*-{
		this.pause();
	}-*/;
	
	public native final void play() /*-{
		this.play();
	}-*/;
	
	public native final void resume() /*-{
		this.resume();
	}-*/;
	
	public native final void stop() /*-{
		this.stop();
	}-*/;
	
	public native final void setVolume(int volume) /*-{
		this.setVolume(volume);
	}-*/;
	
	public native final int getVolume() /*-{
		return this.volume;
	}-*/;
	
	public native final void loops(int loops) /*-{
		this.loops = loops;
	}-*/;
}
