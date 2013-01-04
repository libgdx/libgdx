
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

	class Listener : public OIS::JoyStickListener {
	private:
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
	*/

	static public native long initialize (long hwnd); /*
		std::ostringstream hwndStr;
		hwndStr << hwnd;

		OIS::ParamList params;
		params.insert(std::make_pair("WINDOW", hwndStr.str()));
		params.insert(std::make_pair("w32_joystick", "DISCL_BACKGROUND"));
		params.insert(std::make_pair("w32_joystick", "DISCL_NONEXCLUSIVE"));

		OIS::InputManager *inputManager = OIS::InputManager::createInputSystem(params);
		printf("OIS version: %i\n", inputManager->getVersionNumber());
		fflush(stdout);

		//Listener *listener = new Listener();
		int count = inputManager->getNumberOfDevices(OIS::OISJoyStick);
		printf("joystick count: %i\n", count);
		fflush(stdout);
		for (int i = 0; i < count; i++) {
			try {
				inputManager->createInputObject(OIS::OISJoyStick, true);
			} catch (std::exception &ex) {
				printf("exception!\n%s\n", ex.what());
				fflush(stdout);
			}
			//OIS::JoyStick* joystick = static_cast<OIS::JoyStick*>(inputManager->createInputObject(OIS::OISJoyStick, true));
			//joystick->setEventCallback(listener);
		}
		return (jlong)inputManager;
	*/
}
