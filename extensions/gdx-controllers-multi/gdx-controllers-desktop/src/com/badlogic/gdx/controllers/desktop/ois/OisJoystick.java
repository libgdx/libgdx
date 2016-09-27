/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.controllers.desktop.ois;

/** @author mzechner
 * @author Nathan Sweet */
public class OisJoystick {
	static private final int MIN_AXIS = -32768, MAX_AXIS = 32767;

	static public enum OisPov {
		Centered, North, South, East, West, NorthEast, SouthEast, NorthWest, SouthWest
	}

	private final String name;
	private final long joystickPtr;
	private final boolean[] buttons;
	private final float[] axes;
	private final int[] povs;
	private final boolean[] slidersX, slidersY;
	private OisListener listener;

	public OisJoystick (long joystickPtr, String name) {
		this.joystickPtr = joystickPtr;
		this.name = name;
		initialize(this);
		buttons = new boolean[getButtonCount()];
		axes = new float[getAxisCount()];
		povs = new int[getPovCount()];
		slidersX = new boolean[getSliderCount()];
		slidersY = new boolean[getSliderCount()];
	}

	public void setListener (OisListener listener) {
		this.listener = listener;
	}

	private void buttonPressed (int buttonIndex) {
		buttons[buttonIndex] = true;
		if (listener != null) listener.buttonPressed(this, buttonIndex);
	}

	private void buttonReleased (int buttonIndex) {
		buttons[buttonIndex] = false;
		if (listener != null) listener.buttonReleased(this, buttonIndex);
	}

	private void axisMoved (int axisIndex, int value) {
		axes[axisIndex] = ((value - MIN_AXIS) << 1) / (float)(MAX_AXIS - MIN_AXIS) - 1; // -1 to 1
		if (listener != null) listener.axisMoved(this, axisIndex, axes[axisIndex]);
	}

	private void povMoved (int povIndex, int value) {
		povs[povIndex] = value;
		if (listener != null) listener.povMoved(this, povIndex, getPov(povIndex));
	}

	private void sliderMoved (int sliderIndex, int x, int y) {
		boolean xChanged = slidersX[sliderIndex] != (x == 1);
		boolean yChanged = slidersY[sliderIndex] != (y == 1);
		slidersX[sliderIndex] = x == 1;
		slidersY[sliderIndex] = y == 1;
		if (listener != null) {
			if (xChanged) listener.xSliderMoved(this, sliderIndex, x == 1);
			if (yChanged) listener.ySliderMoved(this, sliderIndex, y == 1);
		}
	}

	public void update () {
		update(joystickPtr, this);
	}

	public int getAxisCount () {
		return getAxesCount(joystickPtr);
	}

	public int getButtonCount () {
		return getButtonCount(joystickPtr);
	}

	public int getPovCount () {
		return getPovCount(joystickPtr);
	}

	public int getSliderCount () {
		return getSliderCount(joystickPtr);
	}

	public float getAxis (int axisIndex) {
		if (axisIndex < 0 || axisIndex >= axes.length) return 0;
		return axes[axisIndex];
	}

	public OisPov getPov (int povIndex) {
		if (povIndex < 0 || povIndex >= povs.length) return OisPov.Centered;
		switch (povs[povIndex]) {
		case 0x00000000:
			return OisPov.Centered;
		case 0x00000001:
			return OisPov.North;
		case 0x00000010:
			return OisPov.South;
		case 0x00000100:
			return OisPov.East;
		case 0x00001000:
			return OisPov.West;
		case 0x00000101:
			return OisPov.NorthEast;
		case 0x00000110:
			return OisPov.SouthEast;
		case 0x00001001:
			return OisPov.NorthWest;
		case 0x00001010:
			return OisPov.SouthWest;
		default:
			throw new RuntimeException("Unexpected POV value reported by OIS: " + povs[povIndex]);
		}
	}

	public boolean isButtonPressed (int buttonIndex) {
		if (buttonIndex < 0 || buttonIndex >= buttons.length) return false;
		return buttons[buttonIndex];
	}

	public boolean getSliderX (int sliderIndex) {
		if (sliderIndex < 0 || sliderIndex >= slidersX.length) return false;

		return slidersX[sliderIndex];
	}

	public boolean getSliderY (int sliderIndex) {
		if (sliderIndex < 0 || sliderIndex >= slidersY.length) return false;
		return slidersY[sliderIndex];
	}

	public String getName () {
		return name;
	}

	public String toString () {
		return name;
	}

	// @off
	/*JNI
	#include <OISJoyStick.h>
	#include <OISInputManager.h>
	 
	static jclass callbackClass = 0;
	static jmethodID povMovedId = 0;
	static jmethodID axisMovedId = 0;
	static jmethodID sliderMovedId = 0;
	static jmethodID buttonPressedId = 0;
	static jmethodID buttonReleasedId = 0;
	
	static void initializeClasses(JNIEnv* env, jobject clazz) {
		// we leak one global ref
		if(callbackClass == 0) {
			callbackClass = (jclass)env->NewGlobalRef(env->GetObjectClass(clazz));
			povMovedId = env->GetMethodID(callbackClass, "povMoved", "(II)V");
			axisMovedId = env->GetMethodID(callbackClass, "axisMoved", "(II)V");
			sliderMovedId = env->GetMethodID(callbackClass, "sliderMoved", "(III)V");
			buttonPressedId = env->GetMethodID(callbackClass, "buttonPressed", "(I)V");
			buttonReleasedId = env->GetMethodID(callbackClass, "buttonReleased", "(I)V");
		}
	}

	class Listener : public OIS::JoyStickListener {
	public:
		Listener(JNIEnv* env, jobject obj) {
			this->env = env;
			this->obj = obj;
		}

		JNIEnv* env;
		jobject obj;

		bool povMoved (const OIS::JoyStickEvent &event, int pov);
		bool axisMoved (const OIS::JoyStickEvent &event, int axis);
		bool sliderMoved (const OIS::JoyStickEvent &event, int sliderID);
		bool buttonPressed (const OIS::JoyStickEvent &event, int button);
		bool buttonReleased (const OIS::JoyStickEvent &event, int button);
	};

	bool Listener::buttonPressed (const OIS::JoyStickEvent &event, int buttonId) {
		env->CallVoidMethod(obj, buttonPressedId, (jint)buttonId);
		return true;
	}

	bool Listener::buttonReleased (const OIS::JoyStickEvent &event, int buttonId) {
		env->CallVoidMethod(obj, buttonReleasedId, (jint)buttonId);
		return true;
	}

	bool Listener::axisMoved (const OIS::JoyStickEvent &event, int axisId) {
		env->CallVoidMethod(obj, axisMovedId, (jint)axisId, (jint)event.state.mAxes[axisId].abs);
		return true;
	}

	bool Listener::povMoved (const OIS::JoyStickEvent &event, int povId) {
		env->CallVoidMethod(obj, povMovedId, (jint)povId, (jint)event.state.mPOV[povId].direction);
		return true;
	}

	bool Listener::sliderMoved (const OIS::JoyStickEvent &event, int sliderId) {
		env->CallVoidMethod(obj, sliderMovedId, (jint)sliderId,
			(jint)event.state.mSliders[sliderId].abX, (jint)event.state.mSliders[sliderId].abY);
		return true;
	}
	 */
	
	private native void initialize(OisJoystick joystick); /*
		initializeClasses(env, joystick);
	*/
	
	private native void update(long joystickPtr, OisJoystick callback); /*
		OIS::JoyStick* joystick = (OIS::JoyStick*)joystickPtr;
		Listener listener(env, callback);
		joystick->setEventCallback(&listener);
		joystick->capture();
	*/
	
	private native int getAxesCount (long joystickPtr); /*
		OIS::JoyStick* joystick = (OIS::JoyStick*)joystickPtr;
		return joystick->getNumberOfComponents(OIS::OIS_Axis);
	*/
	
	private native int getButtonCount (long joystickPtr); /*
		OIS::JoyStick* joystick = (OIS::JoyStick*)joystickPtr;
		return joystick->getNumberOfComponents(OIS::OIS_Button);
	*/
	
	private native int getPovCount (long joystickPtr); /*
		OIS::JoyStick* joystick = (OIS::JoyStick*)joystickPtr;
		return joystick->getNumberOfComponents(OIS::OIS_POV);
	*/
	
	private native int getSliderCount (long joystickPtr); /*
		OIS::JoyStick* joystick = (OIS::JoyStick*)joystickPtr;
		return joystick->getNumberOfComponents(OIS::OIS_Slider);
	 */
}