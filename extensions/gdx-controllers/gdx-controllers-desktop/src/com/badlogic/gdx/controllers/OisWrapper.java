
package com.badlogic.gdx.controllers;

import com.badlogic.gdx.utils.SharedLibraryLoader;

/** JNI wrapper for the object-oriented input system
 * @author mzechner
 * @author Nathan Sweet */
public class OisWrapper {
	static {
		new SharedLibraryLoader().load("gdx-controllers-desktop");
	}

	// @off
	/*JNI
	#include <OISJoyStick.h>
	#include <OISInputManager.h>
	#include <sstream>
	
	static jclass callbackClass = 0;
	static jmethodID povMovedId = 0;
	static jmethodID axisMovedId = 0;
	static jmethodID sliderMovedId = 0;
	static jmethodID buttonPressedId = 0;
	static jmethodID buttonReleasedId = 0;
	
	static void initializeClasses(JNIEnv* env) {
		// we leak one global ref
		if(!callbackClass) {
			callbackClass = (jclass)env->NewGlobalRef(env->FindClass("com/badlogic/gdx/controllers/OisCallback"));
			povMovedId = env->GetMethodID(callbackClass, "povMoved", "()V");
			axisMovedId = env->GetMethodID(callbackClass, "axisMoved", "()V");
			sliderMovedId = env->GetMethodID(callbackClass, "sliderMoved", "()V");
			buttonPressedId = env->GetMethodID(callbackClass, "buttonPressed", "()V");
			buttonReleasedId = env->GetMethodID(callbackClass, "buttonReleased", "()V");
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
		printf("buttonPressed: %i\n", buttonId);
		fflush(stdout);
		return true;
	}

	bool Listener::buttonReleased (const OIS::JoyStickEvent &event, int buttonId) {
		printf("buttonReleased: %i\n", buttonId);
		fflush(stdout);
		return true;
	}

	bool Listener::axisMoved (const OIS::JoyStickEvent &event, int axisId) {
		printf("axisMoved: %i\n", axisId);
		fflush(stdout);
		return true;
	}

	bool Listener::povMoved (const OIS::JoyStickEvent &event, int povId) {
		printf("povMoved: %i\n", povId);
		fflush(stdout);
		return true;
	}

	bool Listener::sliderMoved (const OIS::JoyStickEvent &event, int sliderId) {
		printf("sliderMoved: %i\n", sliderId);
		fflush(stdout);
		return true;
	}
	
	#ifdef _WIN32
	#include <windows.h>
	#endif
	*/
	
	/**
	 * Used on Windows32 with LwjglFrame to work around the cooperation
	 * level problem. Returns 0 on other platforms.
	 * @return the HWND for the invisible window, to be passed to {@link #initialize(long)}
	 */
	static public native long invisibleWindowHack(); /*
	#ifdef _WIN32
		HWND joyHwnd = CreateWindow(
			"Static",         // Class Name (using static so I don't have to register a class)
			"JoystickWindow", // Window Name
			WS_BORDER,        // Window Style
			0, 0, 0, 0,       // x, y, width, height
			0,                // parent handle
			0,                // Menu handle
			0,                // Instance handle
			0);               // Additional Params
		printf("hwnd: %d\n", joyHwnd);
		fflush(stdout);
		return (jlong)joyHwnd;
	#else
		return 0;
	#endif
	*/

	static public native long initialize (long hwnd); /*
		initializeClasses(env);
	
		std::ostringstream hwndStr;
		hwndStr << hwnd;

		OIS::ParamList params;
		params.insert(std::make_pair("WINDOW", hwndStr.str()));
	//	params.insert(std::make_pair("w32_joystick", "DISCL_BACKGROUND"));
	//	params.insert(std::make_pair("w32_joystick", "DISCL_NONEXCLUSIVE"));

		OIS::InputManager *inputManager = OIS::InputManager::createInputSystem(params);
		printf("OIS version: %i\n", inputManager->getVersionNumber());
		fflush(stdout);

		Listener *listener = new Listener(0, 0);
		int count = inputManager->getNumberOfDevices(OIS::OISJoyStick);
		printf("joystick count: %i\n", count);
		fflush(stdout);
		for (int i = 0; i < count; i++) {
			try {
				OIS::JoyStick* joystick = static_cast<OIS::JoyStick*>(inputManager->createInputObject(OIS::OISJoyStick, true));
				joystick->setEventCallback(listener);
			} catch (std::exception &ex) {
				printf("couldn't create input object!\n%s\n", ex.what());
				fflush(stdout);
			}
		}
		return (jlong)inputManager;
	*/
}
