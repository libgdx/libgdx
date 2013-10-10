package com.badlogic.gdx.controllers.gwt;

import com.badlogic.gdx.controllers.PovDirection;

public class GwtControllerEvent {
	
	public static final int BUTTON_DOWN = 0;
	public static final int BUTTON_UP = 1;
	public static final int AXIS = 2;
	public static final int POV = 3;
	public static final int CONNECTED = 4;
	public static final int DISCONNECTED = 5;

	/** the controller the event belongs to **/
	public GwtController controller;
	/** the event type, see constants above **/
	public int type;
	/** the code for the even source, e.g. button keycode, axis index **/
	public int code;
	/** the value if this is an #AXIS, a #BUTTON_DOWN, or a #BUTTON_UP event **/
	public float amount;
	/** the POV direction if this is a #POV event **/ 
	public PovDirection povDirection;
	
}
