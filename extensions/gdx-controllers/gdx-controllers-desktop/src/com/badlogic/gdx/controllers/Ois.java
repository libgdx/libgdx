
package com.badlogic.gdx.controllers;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.lwjgl.opengl.Display;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.SharedLibraryLoader;

/** JNI wrapper for the object-oriented input system
 * @author mzechner
 * @author Nathan Sweet */
public class Ois {
	private long inputManager;
	private Array<OisJoystick> joysticks = new Array<OisJoystick>();
	
	public Ois() {
		// hack doesn't work :/
//		if(System.getProperty("os.name").toLowerCase().contains("windows")) {
//			inputManager = createInputManager(getWindowHandleWindowsHack());
//		} else {
			inputManager = createInputManager(getWindowHandle());
//		}
		
		int numJoysticks = getNumJoysticks(inputManager);
		for(int i = 0; i < numJoysticks; i++) {
			joysticks.add(new OisJoystick(createJoystick(inputManager)));
		}
	}

	public void update() {
		for(OisJoystick joystick: joysticks) {
			joystick.update();
		}
	}
	
	/**
	 * Retrieves the window handle needed by Ois
	 * @return the window handle
	 */
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
			throw new GdxRuntimeException("Unable to get window handle.", ex);
		}
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
	 * Used on Windows32 with LwjglFrame to work around the cooperation
	 * level problem. Returns 0 on other platforms.
	 * @return the HWND for the invisible window, to be passed to {@link #createInputManager(long)}
	 */
	private native long getWindowHandleWindowsHack(); /*
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

	private native long createInputManager (long hwnd); /*
		std::ostringstream hwndStr;
		hwndStr << hwnd;
		OIS::ParamList params;
		params.insert(std::make_pair("WINDOW", hwndStr.str()));
	//	params.insert(std::make_pair("w32_joystick", "DISCL_BACKGROUND"));
	//	params.insert(std::make_pair("w32_joystick", "DISCL_NONEXCLUSIVE"));
		OIS::InputManager *inputManager = OIS::InputManager::createInputSystem(params);
		return (jlong)inputManager;
	*/
	
	private native int getNumJoysticks(long inputManagerPtr); /*
		OIS::InputManager* inputManager = (OIS::InputManager*)inputManagerPtr;
	 	return inputManager->getNumberOfDevices(OIS::OISJoyStick);
	*/
	
	private native long createJoystick(long inputManagerPtr); /*
		OIS::InputManager* inputManager = (OIS::InputManager*)inputManagerPtr;
		try {
			return (jlong)static_cast<OIS::JoyStick*>(inputManager->createInputObject(OIS::OISJoyStick, true));
		} catch (std::exception &ex) {
				printf("couldn't create joystick object!\n%s\n", ex.what());
				fflush(stdout);
				return 0;
		}
	*/
}
