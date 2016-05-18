package com.badlogic.gdx.controllers.lwjgl3;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import org.lwjgl.glfw.GLFW;

import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.PovDirection;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

public class Lwjgl3Controller implements Controller {
	final Lwjgl3ControllerManager manager;
	final Array<ControllerListener> listeners = new Array<ControllerListener>();
	final int index;
	final float[] axisState;	
	final boolean[] buttonState;
	final Vector3 zero = new Vector3(0, 0, 0);
	final String name;
	
	public Lwjgl3Controller(Lwjgl3ControllerManager manager, int index) {
		this.manager = manager;
		this.index = index;
		this.axisState = new float[GLFW.glfwGetJoystickAxes(index).limit()];	
		this.buttonState = new boolean[GLFW.glfwGetJoystickButtons(index).limit()];
		this.name = GLFW.glfwGetJoystickName(index);
	}
	
	void pollState() {
		if(GLFW.glfwJoystickPresent(index) == GLFW.GLFW_FALSE) {
			manager.disconnected(this);
			return;
		}
		
		FloatBuffer axes = GLFW.glfwGetJoystickAxes(index);
		if(axes == null) {
			manager.disconnected(this);
			return;
		}
		ByteBuffer buttons = GLFW.glfwGetJoystickButtons(index);
		if(buttons == null) {
			manager.disconnected(this);
			return;
		}
		
		for(int i = 0; i < axes.limit(); i++) {
			if(axisState[i] != axes.get(i)) {
				for(ControllerListener listener: listeners) {
					listener.axisMoved(this, i, axes.get(i));
				}
				manager.axisChanged(this, i, axes.get(i));
			}
			axisState[i] = axes.get(i);
		}

		for(int i = 0; i < buttons.limit(); i++) {
			if(buttonState[i] != (buttons.get(i) == GLFW.GLFW_PRESS)) {
				for(ControllerListener listener: listeners) {
					if(buttons.get(i) == GLFW.GLFW_PRESS) {
						listener.buttonDown(this, i);
					} else {
						listener.buttonUp(this, i);
					}
				}
				manager.buttonChanged(this, i, buttons.get(i) == GLFW.GLFW_PRESS);
			}
			buttonState[i] = buttons.get(i) == GLFW.GLFW_PRESS;
		}
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
	public boolean getButton (int buttonCode) {
		if(buttonCode < 0 || buttonCode >= buttonState.length) {
			return false;
		}
		return buttonState[buttonCode];
	}

	@Override
	public float getAxis (int axisCode) {
		if(axisCode < 0 || axisCode >= axisState.length) {
			return 0;
		}
		return axisState[axisCode];
	}

	@Override
	public PovDirection getPov (int povCode) {
		return PovDirection.center;
	}

	@Override
	public boolean getSliderX (int sliderCode) {
		return false;
	}

	@Override
	public boolean getSliderY (int sliderCode) {
		return false;
	}

	@Override
	public Vector3 getAccelerometer (int accelerometerCode) {
		return zero;
	}

	@Override
	public void setAccelerometerSensitivity (float sensitivity) {
	}

	@Override
	public String getName () {
		return name;
	}	
}
