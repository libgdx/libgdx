
package com.badlogic.gdx.controllers.desktop;

import com.badlogic.gdx.controllers.ControlType;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.desktop.ois.Ois;
import com.badlogic.gdx.controllers.desktop.ois.OisJoystick;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

import java.util.ArrayList;

public class OisControllers {
	private final Ois ois = new Ois();
	final OisController[] controllers;
	final Array<Event> eventQueue = new Array();
	final Pool<Event> eventPool = new Pool<Event>(16, 1000) {
		protected Event newObject () {
			return new Event();
		}
	};

	public OisControllers (DesktopControllerManager manager) {
		ArrayList<OisJoystick> joysticks = ois.getJoysticks();
		controllers = new OisController[joysticks.size()];
		for (int i = 0, n = joysticks.size(); i < n; i++) {
			OisJoystick joystick = joysticks.get(i);
			controllers = new OisController(joystick);
		}
	}

	static public class OisController implements Controller {
		private OisJoystick joystick;

		public OisController (OisJoystick joystick) {
			this.joystick = joystick;
		}

		public void setAccelerometerSensitivity (float sensitivity) {
		}

		public int getControlCount (ControlType type) {
			switch (type) {
			case button:
				return joystick.getButtonCount();
			case axis:
				return joystick.getAxisCount();
			case slider:
				return joystick.getSliderCount();
			case pov:
				return joystick.getPovCount();
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

	static class Event {
		static final int BUTTON_DOWN = 0;
		static final int BUTTON_UP = 1;
		static final int AXIS = 2;

		int type;
		OisController controller;
		float axisValue;
	}
}
