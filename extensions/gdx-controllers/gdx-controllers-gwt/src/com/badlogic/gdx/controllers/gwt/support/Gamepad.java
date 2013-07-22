package com.badlogic.gdx.controllers.gwt.support;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayNumber;

public final class Gamepad extends JavaScriptObject {
	
	protected Gamepad() {
		// Required by GWT
	}
	
	public native String getId() /*-{
		return this.id; 
	}-*/;
	
	public native int getIndex() /*-{
		return this.index;
	}-*/;
	
	public native double getTimestamp() /*-{
		return this.timestamp; 
	}-*/;
	
	public native JsArrayNumber getAxes() /*-{
		return this.axes;
	}-*/;
	
	public native JsArrayNumber getButtons() /*-{
		return this.buttons;
	}-*/;

	public native double getPreviousTimestamp() /*-{
		return this.previousTimestamp;
	}-*/;
	
	public native void setPreviousTimestamp(double previousTimestamp) /*-{
		this.previousTimestamp = previousTimestamp;
	}-*/;
	
	public static Gamepad getGamepad(int index) {
		return GamepadSupport.getGamepad(index);		
	}

}
