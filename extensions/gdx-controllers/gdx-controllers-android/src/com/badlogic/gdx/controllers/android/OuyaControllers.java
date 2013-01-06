
package com.badlogic.gdx.controllers.android;

import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnGenericMotionListener;
import android.view.View.OnKeyListener;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidInput;
import com.badlogic.gdx.backends.android.AndroidInputThreePlus;
import com.badlogic.gdx.controllers.ControlType;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.PovDirection;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Pool;

import tv.ouya.console.api.OuyaController;

/** @author Nathan Sweet */
public class OuyaControllers {
	private final AndroidControllerManager manager;
	final LibgdxOuyaController[] controllers = new LibgdxOuyaController[OuyaController.MAX_CONTROLLERS];
	final Array<Event> eventQueue = new Array();
	final Pool<Event> eventPool = new Pool<Event>(16, 1000) {
		protected Event newObject () {
			return new Event();
		}
	};

	public OuyaControllers (final AndroidControllerManager manager) {
		this.manager = manager;
		AndroidInputThreePlus androidInput = (AndroidInputThreePlus)Gdx.input;
		androidInput.addKeyListener(new OnKeyListener() {
			public boolean onKey (View v, int keyCode, KeyEvent event) {
				boolean handled = false;
				switch (event.getAction()) {
				case android.view.KeyEvent.ACTION_DOWN:
					handled = OuyaController.onKeyDown(keyCode, event);
					break;
				case android.view.KeyEvent.ACTION_UP:
					handled = OuyaController.onKeyUp(keyCode, event);
					break;
				}
				if (handled) updateControllers();
				return handled;
			}
		});
		androidInput.addGenericMotionListener(new OnGenericMotionListener() {
			public boolean onGenericMotion (View view, MotionEvent event) {
				if (OuyaController.onGenericMotionEvent(event)) {
					updateControllers();
					return true;
				}
				return false;
			}
		});

		new Runnable() {
			public void run () {
				Array<ControllerListener> allListeners = manager.listeners;
				for (int i = 0, n = eventQueue.size; i < n; i++) {
					Event event = eventQueue.get(i);
					switch (event.type) {
					case Event.BUTTON_DOWN:
						for (int ii = 0, nn = allListeners.size; ii < nn; ii++)
							allListeners.get(ii).buttonDown(event.controller, i);
						for (int ii = 0, nn = event.controller.listeners.size; ii < nn; ii++)
							event.controller.listeners.get(ii).buttonDown(event.controller, i);
						break;
					case Event.BUTTON_UP:
						for (int ii = 0, nn = allListeners.size; ii < nn; ii++)
							allListeners.get(ii).buttonUp(event.controller, i);
						for (int ii = 0, nn = event.controller.listeners.size; ii < nn; ii++)
							event.controller.listeners.get(ii).buttonUp(event.controller, i);
						break;
					case Event.AXIS:
						for (int ii = 0, nn = allListeners.size; ii < nn; ii++)
							allListeners.get(ii).axisMoved(event.controller, i, event.axisValue);
						for (int ii = 0, nn = event.controller.listeners.size; ii < nn; ii++)
							event.controller.listeners.get(ii).axisMoved(event.controller, i, event.axisValue);
						break;
					}
					eventPool.free(event);
				}
				Gdx.app.postRunnable(this);
			}
		}.run();
	}

	void updateControllers () {
		for (int i = 0; i < OuyaController.MAX_CONTROLLERS; i++) {
			OuyaController controller = OuyaController.getControllerByPlayer(i);
			if (controller != null) {
				LibgdxOuyaController libgdxController = controllers[i];
				if (libgdxController == null) {
					controllers[i] = libgdxController = new LibgdxOuyaController(controller, i);
					manager.controllers.add(libgdxController);
				}
				libgdxController.update();
			}
		}
	}

	private class LibgdxOuyaController implements Controller {
		private final OuyaController controller;
		private final int playerIndex;
		private final String name;
		private final boolean[] buttons = new boolean[15];
		private final float[] axes = new float[6];
		final Array<ControllerListener> listeners = new Array();

		public LibgdxOuyaController (OuyaController controller, int playerIndex) {
			this.controller = controller;
			this.playerIndex = playerIndex;
			name = "Player " + playerIndex;
		}

		void update () {
			for (int i = 0, n = buttons.length; i < n; i++) {
				boolean newValue = controller.getButton(indexToOuyaButton[i]);
				if (newValue != buttons[i]) {
					buttons[i] = newValue;

					Event event = eventPool.obtain();
					event.controller = this;
					event.type = newValue ? Event.BUTTON_DOWN : Event.BUTTON_UP;
					eventQueue.add(event);
				}
			}
			for (int i = 0, n = axes.length; i < n; i++) {
				float newValue = controller.getAxisValue(indexToOuyaAxis[i]);
				if (newValue != axes[i]) {
					axes[i] = newValue;

					Event event = eventPool.obtain();
					event.controller = this;
					event.type = Event.AXIS;
					event.axisValue = newValue;
					eventQueue.add(event);
				}
			}
		}

		public boolean getButton (int buttonIndex) {
			return buttons[buttonIndex];
		}

		public float getAxis (int axisIndex) {
			return axes[axisIndex];
		}

		public PovDirection getPov (int povIndex) {
			throw new GdxRuntimeException("Invalid slider index: " + povIndex);
		}

		public boolean getSliderX (int sliderIndex) {
			throw new GdxRuntimeException("Invalid slider index: " + sliderIndex);
		}

		public boolean getSliderY (int sliderIndex) {
			throw new GdxRuntimeException("Invalid slider index: " + sliderIndex);
		}

		public Vector3 getAccelerometer (int accelerometerIndex) {
			throw new GdxRuntimeException("Invalid accelerometer index: " + accelerometerIndex);
		}

		public void setAccelerometerSensitivity (float sensitivity) {
		}

		public int getControlCount (ControlType type) {
			switch (type) {
			case button:
				return buttons.length;
			case axis:
				return axes.length;
			}
			return 0;
		}

		public void addListener (ControllerListener listener) {
			listeners.add(listener);
		}

		public void removeListener (ControllerListener listener) {
			listeners.removeValue(listener, true);
		}

		public String getName () {
			return name;
		}

		public String toString () {
			return name;
		}
	}

	static private int ouyaButtonToIndex (int ouyaButton) {
		switch (ouyaButton) {
		case OuyaController.BUTTON_O:
			return 0;
		case OuyaController.BUTTON_U:
			return 1;
		case OuyaController.BUTTON_Y:
			return 2;
		case OuyaController.BUTTON_A:
			return 3;
		case OuyaController.BUTTON_DPAD_UP:
			return 4;
		case OuyaController.BUTTON_DPAD_DOWN:
			return 5;
		case OuyaController.BUTTON_DPAD_LEFT:
			return 6;
		case OuyaController.BUTTON_DPAD_RIGHT:
			return 7;
		case OuyaController.BUTTON_L1:
			return 8;
		case OuyaController.BUTTON_L2:
			return 9;
		case OuyaController.BUTTON_L3:
			return 10;
		case OuyaController.BUTTON_R1:
			return 11;
		case OuyaController.BUTTON_R2:
			return 12;
		case OuyaController.BUTTON_R3:
			return 13;
		case OuyaController.BUTTON_SYSTEM:
			return 14;
		}
		throw new GdxRuntimeException("Unknown Ouya button: " + ouyaButton);
	}

	static int[] indexToOuyaButton = {OuyaController.BUTTON_O, OuyaController.BUTTON_U, OuyaController.BUTTON_Y,
		OuyaController.BUTTON_A, OuyaController.BUTTON_DPAD_UP, OuyaController.BUTTON_DPAD_DOWN, OuyaController.BUTTON_DPAD_LEFT,
		OuyaController.BUTTON_DPAD_RIGHT, OuyaController.BUTTON_L1, OuyaController.BUTTON_L2, OuyaController.BUTTON_L3,
		OuyaController.BUTTON_R1, OuyaController.BUTTON_R2, OuyaController.BUTTON_R3, OuyaController.BUTTON_SYSTEM};
	static int[] indexToOuyaAxis = {OuyaController.AXIS_L2, OuyaController.AXIS_LS_X, OuyaController.AXIS_LS_Y,
		OuyaController.AXIS_R2, OuyaController.AXIS_RS_X, OuyaController.AXIS_RS_Y};

	static private int ouyaAxisToIndex (int ouyaButton) {
		switch (ouyaButton) {
		case OuyaController.AXIS_L2:
			return 0;
		case OuyaController.AXIS_LS_X:
			return 1;
		case OuyaController.AXIS_LS_Y:
			return 2;
		case OuyaController.AXIS_R2:
			return 3;
		case OuyaController.AXIS_RS_X:
			return 4;
		case OuyaController.AXIS_RS_Y:
			return 5;
		}
		throw new GdxRuntimeException("Unknown Ouya axis: " + ouyaButton);
	}

	static class Event {
		static final int BUTTON_DOWN = 0;
		static final int BUTTON_UP = 1;
		static final int AXIS = 2;

		int type;
		LibgdxOuyaController controller;
		float axisValue;
	}
}
