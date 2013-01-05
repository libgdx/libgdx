#include <com.badlogic.gdx.controllers.desktop.ois.Ois.h>

//@line:57

	#include <OISJoyStick.h>
	#include <OISInputManager.h>
	#include <sstream>

	#ifdef _WIN32
	#include <windows.h>
	#endif
	JNIEXPORT jlong JNICALL Java_com_badlogic_gdx_controllers_desktop_ois_Ois_getWindowHandleWindowsHack(JNIEnv* env, jobject object) {


//@line:72

	#ifdef _WIN32
		HWND joyHwnd = CreateWindow(
			"Static",         // class name (static so we don't have to register a class)
			"JoystickWindow", // window name
			WS_BORDER,        // window style
			0, 0, 0, 0,       // x, y, width, height
			0,                // parent handle
			0,                // menu handle
			0,                // instance handle
			0);               // additional params
		return (jlong)joyHwnd;
	#else
		return 0;
	#endif
	

}

JNIEXPORT jlong JNICALL Java_com_badlogic_gdx_controllers_desktop_ois_Ois_createInputManager(JNIEnv* env, jobject object, jlong hwnd) {


//@line:89

		std::ostringstream hwndStr;
		hwndStr << hwnd;
		OIS::ParamList params;
		params.insert(std::make_pair("WINDOW", hwndStr.str()));
		//params.insert(std::make_pair("w32_joystick", "DISCL_BACKGROUND"));
		//params.insert(std::make_pair("w32_joystick", "DISCL_NONEXCLUSIVE"));
		OIS::InputManager *inputManager = OIS::InputManager::createInputSystem(params);
		return (jlong)inputManager;
	

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_controllers_desktop_ois_Ois_getJoystickCount(JNIEnv* env, jobject object, jlong inputManagerPtr) {


//@line:100

		OIS::InputManager* inputManager = (OIS::InputManager*)inputManagerPtr;
	 	return inputManager->getNumberOfDevices(OIS::OISJoyStick);
	

}

JNIEXPORT jlong JNICALL Java_com_badlogic_gdx_controllers_desktop_ois_Ois_createJoystick(JNIEnv* env, jobject object, jlong inputManagerPtr) {


//@line:105

		OIS::InputManager* inputManager = (OIS::InputManager*)inputManagerPtr;
		try {
			return (jlong)static_cast<OIS::JoyStick*>(inputManager->createInputObject(OIS::OISJoyStick, true));
		} catch (std::exception &ex) {
			printf("Error creating joystick: %s\n", ex.what());
			fflush(stdout);
			return 0;
		}
	

}

