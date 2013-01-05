package com.badlogic.gdx.controllers;

public class OisJoystick {
	private long joystickPtr;

	public OisJoystick(long joystickPtr) {
		this.joystickPtr = joystickPtr;
		initialize(this);
	}
	
	public void povMoved (int pov, int value) {
		System.out.println("pov moved " + pov + ", " + value);
	}

	public void axisMoved (int axis, int value) {
		System.out.println("axis moved " + axis + ", " + value);
	}

	public void sliderMoved (int slider, int x, int y) {
		System.out.println("slider moved " + slider + ", " + x + ", " + y);
	}

	public void buttonPressed (int button) {
		System.out.println("button pressed " + button);
	}

	public void buttonReleased (int button) {
		System.out.println("button released " + button);
	}
	
	public void update() {
		updateJni(joystickPtr, this);
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
		env->CallVoidMethod(obj, sliderMovedId, (jint)sliderId, (jint)event.state.mSliders[sliderId].abX, (jint)event.state.mSliders[sliderId].abY);
		return true;
	}
	 */
	
	private native void initialize(OisJoystick joystick); /*
		initializeClasses(env, joystick);
	*/
	
	private native void updateJni(long joystickPtr, OisJoystick callback); /*
		OIS::JoyStick* joystick = (OIS::JoyStick*)joystickPtr;
		Listener listener(env, callback);
		joystick->setEventCallback(&listener);
		joystick->capture();
	*/
}
