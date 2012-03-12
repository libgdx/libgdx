
package com.badlogic.gdx.backends.gwt;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.utils.Pool;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.CanvasElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;

public class GwtInput implements Input {
	boolean touched = false;
	int mouseX, mouseY;
	int deltaX, deltaY;
	boolean justTouched = false;
	Set<Integer> pressedButtons = new HashSet<Integer>();
	Set<Integer> pressedKeys = new HashSet<Integer>();
	InputProcessor processor;
	char lastKeyCharPressed;
	float keyRepeatTimer;
	long currentEventTimeStamp;
	final CanvasElement canvas;

	public GwtInput (CanvasElement canvas) {
		this.canvas = canvas;
		hookEvents();
	}

	@Override
	public float getAccelerometerX () {
		return 0;
	}

	@Override
	public float getAccelerometerY () {
		return 0;
	}

	@Override
	public float getAccelerometerZ () {
		return 0;
	}

	@Override
	public int getX () {
		return mouseX;
	}

	@Override
	public int getX (int pointer) {
		if (pointer != 0) return 0;
		return mouseX;
	}

	@Override
	public int getDeltaX () {
		return deltaX;
	}

	@Override
	public int getDeltaX (int pointer) {
		if (pointer != 0) return 0;
		return deltaX;
	}

	@Override
	public int getY () {
		return mouseY;
	}

	@Override
	public int getY (int pointer) {
		if (pointer != 0) return 0;
		return mouseY;
	}

	@Override
	public int getDeltaY () {
		return deltaY;
	}

	@Override
	public int getDeltaY (int pointer) {
		if (pointer != 0) return 0;
		return deltaY;
	}

	@Override
	public boolean isTouched () {
		return touched;
	}

	@Override
	public boolean justTouched () {
		return justTouched;
	}

	@Override
	public boolean isTouched (int pointer) {
		if (pointer != 0) return false;
		return touched;
	}

	@Override
	public boolean isButtonPressed (int button) {
		// FIXME
		return button == Buttons.LEFT && touched;
	}

	@Override
	public boolean isKeyPressed (int key) {
		// FIXME
		return false;
	}

	@Override
	public void getTextInput (TextInputListener listener, String title, String text) {
		// FIXME
	}

	@Override
	public void getPlaceholderTextInput (TextInputListener listener, String title, String placeholder) {
		// FIXME
	}

	@Override
	public void setOnscreenKeyboardVisible (boolean visible) {
	}

	@Override
	public void vibrate (int milliseconds) {
	}

	@Override
	public void vibrate (long[] pattern, int repeat) {
	}

	@Override
	public void cancelVibrate () {
	}

	@Override
	public float getAzimuth () {
		return 0;
	}

	@Override
	public float getPitch () {
		return 0;
	}

	@Override
	public float getRoll () {
		return 0;
	}

	@Override
	public void getRotationMatrix (float[] matrix) {
	}

	@Override
	public long getCurrentEventTime () {
		return currentEventTimeStamp;
	}

	@Override
	public void setCatchBackKey (boolean catchBack) {
	}

	@Override
	public void setCatchMenuKey (boolean catchMenu) {
	}

	@Override
	public void setInputProcessor (InputProcessor processor) {
		this.processor = processor;
	}

	@Override
	public InputProcessor getInputProcessor () {
		return processor;
	}

	@Override
	public boolean isPeripheralAvailable (Peripheral peripheral) {
		if (peripheral == Peripheral.Accelerometer) return false;
		if (peripheral == Peripheral.Compass) return false;
		if (peripheral == Peripheral.HardwareKeyboard) return true;
		if (peripheral == Peripheral.MultitouchScreen) return false;
		if (peripheral == Peripheral.OnscreenKeyboard) return false;
		if (peripheral == Peripheral.Vibrator) return false;
		return false;
	}

	@Override
	public int getRotation () {
		return 0;
	}

	@Override
	public Orientation getNativeOrientation () {
		return Orientation.Landscape;
	}

	@Override
	public void setCursorCatched (boolean catched) {
		// FIXME??
	}

	@Override
	public boolean isCursorCatched () {
		return false;
	}

	@Override
	public void setCursorPosition (int x, int y) {
		// FIXME??
	}

	// kindly borrowed from our dear playn friends...
	static native void addEventListener (JavaScriptObject target, String name, GwtInput handler, boolean capture) /*-{
		target
				.addEventListener(
						name,
						function(e) {
							handler.@com.badlogic.gdx.backends.gwt.GwtInput::handleEvent(Lcom/google/gwt/dom/client/NativeEvent;)(e);
						}, capture);
	}-*/;

	private static native float getMouseWheelVelocity (NativeEvent evt) /*-{
		var delta = 0.0;
		var agentInfo = @com.badlogic.gdx.backends.gwt.GwtApplication::agentInfo()();

		if (agentInfo.isFirefox) {
			if (agentInfo.isMacOS) {
				delta = 1.0 * evt.detail;
			} else {
				delta = 1.0 * evt.detail / 3;
			}
		} else if (agentInfo.isOpera) {
			if (agentInfo.isLinux) {
				delta = -1.0 * evt.wheelDelta / 80;
			} else {
				// on mac
				delta = -1.0 * evt.wheelDelta / 40;
			}
		} else if (agentInfo.isChrome || agentInfo.isSafari) {
			delta = -1.0 * evt.wheelDelta / 120;
			// handle touchpad for chrome
			if (Math.abs(delta) < 1) {
				if (agentInfo.isWindows) {
					delta = -1.0 * evt.wheelDelta;
				} else if (agentInfo.isMacOS) {
					delta = -1.0 * evt.wheelDelta / 3;
				}
			}
		}
		return delta;
	}-*/;

	/** Kindly borrowed from PlayN. **/
	protected static native String getMouseWheelEvent() /*-{
    	if (navigator.userAgent.toLowerCase().indexOf('firefox') != -1) {
      	return "DOMMouseScroll";
   	} else {
      	return "mousewheel";
   	}
  	}-*/;
	
	/** Kindly borrowed from PlayN. **/
	protected static float getRelativeX (NativeEvent e, Element target) {
		return e.getClientX() - target.getAbsoluteLeft() + target.getScrollLeft() + target.getOwnerDocument().getScrollLeft();
	}

	/** Kindly borrowed from PlayN. **/
	protected static float getRelativeY (NativeEvent e, Element target) {
		return e.getClientY() - target.getAbsoluteTop() + target.getScrollTop() + target.getOwnerDocument().getScrollTop();
	}

	private void hookEvents () {
		addEventListener(canvas, "mousedown", this, true);
		addEventListener(Document.get(), "mousedown", this, true);
		addEventListener(canvas, "mouseup", this, true);
		addEventListener(Document.get(), "mouseup", this, true);
		addEventListener(canvas, "mousemove", this, true);
		addEventListener(Document.get(), "mousemove", this, true);
		addEventListener(canvas, getMouseWheelEvent(), this, true);
	}
	
	private int getButton(int button) {
		if(button == NativeEvent.BUTTON_LEFT) return Buttons.LEFT;
		if(button == NativeEvent.BUTTON_RIGHT) return Buttons.RIGHT;
		if(button == NativeEvent.BUTTON_MIDDLE) return Buttons.MIDDLE;
		return Buttons.LEFT;
	}

	private void handleEvent (NativeEvent e) {
		if(e.getType().equals("mousedown")) {
			if(!e.getEventTarget().equals(canvas)) return;
			this.justTouched = true;
			this.touched = true;
			this.pressedButtons.add(getButton(e.getButton()));
			this.deltaX = 0;
			this.deltaY = 0;
			this.mouseX = (int)getRelativeX(e, canvas);
			this.mouseY = (int)getRelativeY(e, canvas);
			this.currentEventTimeStamp = System.currentTimeMillis() * 1000;
			if(processor != null) processor.touchDown(mouseX, mouseY, 0, getButton(e.getButton()));
		}
		
		if(e.getType().equals("mousemove")) {
			if(!e.getEventTarget().equals(canvas) && !touched) return;
			this.deltaX = (int)getRelativeX(e, canvas) - mouseX;
			this.deltaY = (int)getRelativeY(e, canvas) - mouseY;
			this.mouseX = (int)getRelativeX(e, canvas);
			this.mouseY = (int)getRelativeY(e, canvas);
			this.currentEventTimeStamp = System.currentTimeMillis() * 1000;
			if(processor != null) {
				if(touched) processor.touchDragged(mouseX, mouseY, 0);
				else processor.touchMoved(mouseX, mouseY);
			}
		}
		
		if(e.getType().equals("mouseup")) {
			if(!e.getEventTarget().equals(canvas) && !touched) return;
			this.pressedButtons.remove(getButton(e.getButton()));
			this.touched = pressedButtons.size() > 0;
			this.deltaX = (int)getRelativeX(e, canvas) - mouseX;
			this.deltaY = (int)getRelativeY(e, canvas) - mouseY;
			this.mouseX = (int)getRelativeX(e, canvas);
			this.mouseY = (int)getRelativeY(e, canvas);
			this.currentEventTimeStamp = System.currentTimeMillis() * 1000;
			if(processor != null) processor.touchUp(mouseX, mouseY, 0, getButton(e.getButton()));
		}
	}
}