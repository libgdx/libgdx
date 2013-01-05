
package com.badlogic.gdx.controllers.desktop.ois;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;

import org.lwjgl.opengl.Display;

/** JNI wrapper for the object-oriented input system
 * @author mzechner
 * @author Nathan Sweet */
public class Ois {
	private final long inputManagerPtr;
	private final ArrayList<OisJoystick> joysticks = new ArrayList();

	public Ois () {
		// hack doesn't work :/ FIXME - Try using hwnd from AWT for LwjglFrame.
// if(System.getProperty("os.name").toLowerCase().contains("windows")) {
// inputManager = createInputManager(getWindowHandleWindowsHack());
// } else {
		inputManagerPtr = createInputManager(getWindowHandle());
// }

		String[] names = getJoystickNames(inputManagerPtr);
		for (int i = 0, n = names.length; i < n; i++) {
			System.out.println(names[i]);
			joysticks.add(new OisJoystick(createJoystick(inputManagerPtr)));
		}
	}

	public ArrayList<OisJoystick> getJoysticks () {
		return joysticks;
	}

	public void update () {
		for (int i = 0, n = joysticks.size(); i < n; i++)
			joysticks.get(i).update();
	}

	/** Returns the window handle from LWJGL needed by OIS. */
	private long getWindowHandle () {
		try {
			Method getImplementation = Display.class.getDeclaredMethod("getImplementation", new Class[0]);
			getImplementation.setAccessible(true);
			Object display = getImplementation.invoke(null, (Object[])null);
			String fieldName = System.getProperty("os.name").toLowerCase().contains("windows") ? "hwnd" : "parent_window";
			Field field = display.getClass().getDeclaredField(fieldName);
			field.setAccessible(true);
			return (Long)field.get(display);
		} catch (Exception ex) {
			throw new RuntimeException("Unable to get window handle.", ex);
		}
	}

	public int getVersionNumber () {
		return getVersionNumber(inputManagerPtr);
	}

	public String getVersionName () {
		return getVersionName(inputManagerPtr);
	}

	public String getInputSystemName () {
		return getInputSystemName(inputManagerPtr);
	}

	// @off
	/*JNI
	#include <OISJoyStick.h>
	#include <OISInputManager.h>
	#include <sstream>

	#ifdef _WIN32
	#include <windows.h>
	#endif
	*/

	/**
	 * Used on Windows32 with LwjglFrame to work around the cooperation level problem. Returns 0 on other platforms.
	 * FIXME - Doesn't cause errors, but we don't get any input events.
	 * @return the HWND for the invisible window, to be passed to {@link #createInputManager(long)}
	 */
	private native long getWindowHandleWindowsHack(); /*
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
	*/

	private native long createInputManager (long hwnd); /*
		std::ostringstream hwndStr;
		hwndStr << hwnd;
		OIS::ParamList params;
		params.insert(std::make_pair("WINDOW", hwndStr.str()));
		//params.insert(std::make_pair("w32_joystick", "DISCL_BACKGROUND"));
		//params.insert(std::make_pair("w32_joystick", "DISCL_NONEXCLUSIVE"));
		OIS::InputManager *inputManager = OIS::InputManager::createInputSystem(params);
		return (jlong)inputManager;
	*/

	private native String[] getJoystickNames (long inputManagerPtr); /*
		OIS::InputManager* inputManager = (OIS::InputManager*)inputManagerPtr;
		OIS::DeviceList map = inputManager->listFreeDevices();
		int joystickCount = inputManager->getNumberOfDevices(OIS::OISJoyStick);
		jobjectArray names = (jobjectArray)env->NewObjectArray(joystickCount, env->FindClass("java/lang/String"), env->NewStringUTF(""));
		int index = 0;
		for (OIS::DeviceList::iterator i = map.begin(); i != map.end(); ++i) {
			if (i->first != OIS::OISJoyStick) continue;
			env->SetObjectArrayElement(names, index++, env->NewStringUTF(i->second.c_str()));
		}
		return names;
	*/

	private native int getVersionNumber (long inputManagerPtr); /*
		OIS::InputManager* inputManager = (OIS::InputManager*)inputManagerPtr;
	 	return inputManager->getVersionNumber();
	*/
	
	private native String getVersionName (long inputManagerPtr); /*
		OIS::InputManager* inputManager = (OIS::InputManager*)inputManagerPtr;
	 	return env->NewStringUTF(inputManager->getVersionName().c_str());
	 */
	
	private native String getInputSystemName (long inputManagerPtr); /*
		OIS::InputManager* inputManager = (OIS::InputManager*)inputManagerPtr;
	 	return env->NewStringUTF(inputManager->inputSystemName().c_str());
	 */

	private native long createJoystick (long inputManagerPtr); /*
		OIS::InputManager* inputManager = (OIS::InputManager*)inputManagerPtr;
		try {
			return (jlong)static_cast<OIS::JoyStick*>(inputManager->createInputObject(OIS::OISJoyStick, true));
		} catch (std::exception &ex) {
			printf("Error creating joystick: %s\n", ex.what());
			fflush(stdout);
			return 0;
		}
	*/
}
