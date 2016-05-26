package com.badlogic.gdx.controllers.lwjgl3;

import org.lwjgl.glfw.GLFW;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.ControllerManager;
import com.badlogic.gdx.utils.Array;

public class Lwjgl3ControllerManager implements ControllerManager {
	final Array<Controller> controllers = new Array<Controller>();
	final Array<Controller> polledControllers = new Array<Controller>();
	final Array<ControllerListener> listeners = new Array<ControllerListener>();
	
	public Lwjgl3ControllerManager() {
		for(int i = GLFW.GLFW_JOYSTICK_1; i < GLFW.GLFW_JOYSTICK_LAST; i++) {
			if(GLFW.glfwJoystickPresent(i) == GLFW.GLFW_TRUE) {
				controllers.add(new Lwjgl3Controller(this, i));
			}
		}
		
		Gdx.app.postRunnable(new Runnable() {
			@Override
			public void run () {
				pollState();
				Gdx.app.postRunnable(this);
			}
		});
	}
	
	void pollState() {
		for(int i = GLFW.GLFW_JOYSTICK_1; i < GLFW.GLFW_JOYSTICK_LAST; i++) {
			if(GLFW.glfwJoystickPresent(i) == GLFW.GLFW_TRUE) {
				boolean alreadyUsed = false;
				for(int j = 0; j < controllers.size; j++) {
					if(((Lwjgl3Controller)controllers.get(j)).index == i) {
						alreadyUsed = true;
						break;
					}
				}
				if(!alreadyUsed) {
					Lwjgl3Controller controller = new Lwjgl3Controller(this, i);
					connected(controller);
				}
			}
		}
		
		polledControllers.addAll(controllers);
		for(Controller controller: polledControllers) {
			((Lwjgl3Controller)controller).pollState();
		}
		polledControllers.clear();
	}
	
	@Override
	public Array<Controller> getControllers () {
		return controllers;
	}

	@Override
	public void addListener (ControllerListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeListener (ControllerListener listener) {
		listeners.removeValue(listener, true);
	}

	@Override
	public void clearListeners () {
		listeners.clear();
	}
	
	void connected (Lwjgl3Controller controller) {
		controllers.add(controller);
		for(ControllerListener listener: listeners) {
			listener.connected(controller);
		}
	}

	void disconnected (Lwjgl3Controller controller) {	
		controllers.removeValue(controller, true);
		for(ControllerListener listener: listeners) {
			listener.disconnected(controller);
		}
	}
	
	void axisChanged (Lwjgl3Controller controller, int axisCode, float value) {
		for(ControllerListener listener: listeners) {
			listener.axisMoved(controller, axisCode, value);
		}
	}
	
	void buttonChanged (Lwjgl3Controller controller, int buttonCode, boolean value) {
		for(ControllerListener listener: listeners) {
			if(value) {
				listener.buttonDown(controller, buttonCode);
			} else {
				listener.buttonUp(controller, buttonCode);
			}
		}
	}
}
