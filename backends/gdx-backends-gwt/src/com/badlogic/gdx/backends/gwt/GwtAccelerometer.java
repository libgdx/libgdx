package com.badlogic.gdx.backends.gwt;

public class GwtAccelerometer extends GwtSensor {

	protected GwtAccelerometer() {
	}

	static native GwtAccelerometer getInstance() /*-{
		return new $wnd.Accelerometer();
	}-*/;

	static native boolean isSupported() /*-{
		return "Accelerometer" in $wnd;
	}-*/;

	final native double x() /*-{
		return this.x;
	}-*/;

	final native double y() /*-{
		return this.y;
	}-*/;

	final native double z() /*-{
		return this.z;
	}-*/;
}
