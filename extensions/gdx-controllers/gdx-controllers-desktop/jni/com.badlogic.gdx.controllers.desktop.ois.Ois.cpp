#include <com.badlogic.gdx.controllers.desktop.ois.Ois.h>

//@line:63

	#include <OISJoyStick.h>
	#include <OISInputManager.h>
	#include <sstream>
	
	#ifdef _WIN32
	#include <windows.h>
	#endif
	JNIEXPORT jlong JNICALL Java_com_badlogic_gdx_controllers_desktop_ois_Ois_getWindowHandleWindowsHack(JNIEnv* env, jobject object) {


//@line:78

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
	

}

JNIEXPORT jlong JNICALL Java_com_badlogic_gdx_controllers_desktop_ois_Ois_createInputManager(JNIEnv* env, jobject object, jlong hwnd) {


//@line:97

		std::ostringstream hwndStr;
		hwndStr << hwnd;
		OIS::ParamList params;
		params.insert(std::make_pair("WINDOW", hwndStr.str()));
	//	params.insert(std::make_pair("w32_joystick", "DISCL_BACKGROUND"));
	//	params.insert(std::make_pair("w32_joystick", "DISCL_NONEXCLUSIVE"));
		OIS::InputManager *inputManager = OIS::InputManager::createInputSystem(params);
		return (jlong)inputManager;
	

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_controllers_desktop_ois_Ois_getNumJoysticks(JNIEnv* env, jobject object, jlong inputManagerPtr) {


//@line:108

		OIS::InputManager* inputManager = (OIS::InputManager*)inputManagerPtr;
	 	return inputManager->getNumberOfDevices(OIS::OISJoyStick);
	

}

JNIEXPORT jlong JNICALL Java_com_badlogic_gdx_controllers_desktop_ois_Ois_createJoystick(JNIEnv* env, jobject object, jlong inputManagerPtr) {


//@line:113

		OIS::InputManager* inputManager = (OIS::InputManager*)inputManagerPtr;
		try {
			return (jlong)static_cast<OIS::JoyStick*>(inputManager->createInputObject(OIS::OISJoyStick, true));
		} catch (std::exception &ex) {
				printf("couldn't create joystick object!\n%s\n", ex.what());
				fflush(stdout);
				return 0;
		}
	

}

