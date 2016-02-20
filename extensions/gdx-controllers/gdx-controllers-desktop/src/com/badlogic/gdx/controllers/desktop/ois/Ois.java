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

import java.util.ArrayList;

/** JNI wrapper for OIS (Object-oriented Input System).
 * @author mzechner
 * @author Nathan Sweet */
public class Ois {
	private final long inputManagerPtr;
	private final ArrayList<OisJoystick> joysticks = new ArrayList();

	public Ois (long hwnd) {
		inputManagerPtr = createInputManager(hwnd);

		String[] names = getJoystickNames(inputManagerPtr);
		for (int i = 0, n = names.length; i < n; i++)
			joysticks.add(new OisJoystick(createJoystick(inputManagerPtr), names[i]));
	}

	public ArrayList<OisJoystick> getJoysticks () {
		return joysticks;
	}

	public void update () {
		for (int i = 0, n = joysticks.size(); i < n; i++)
			joysticks.get(i).update();
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
	*/

	private native long createInputManager (long hwnd); /*
		OIS::ParamList params;
		#ifndef __APPLE__
			std::ostringstream hwndStr;
			hwndStr << hwnd;
			params.insert(std::make_pair("WINDOW", hwndStr.str()));
		#endif		
		
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