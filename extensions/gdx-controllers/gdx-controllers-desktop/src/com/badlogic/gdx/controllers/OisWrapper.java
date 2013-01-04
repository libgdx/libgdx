package com.badlogic.gdx.controllers;

/**
 * JNI wrapper for the object-oriented input system
 * @author mzechner
 *
 */
public class OisWrapper {
	// @off
	/*JNI
	#include <OISJoyStick.h>
	#include <OISInputManager.h>
	 */
	
	private static native int initialize(int hwnd); /*
        size_t windowHnd = 0;
        OIS::InputManager::createInputSystem( windowHnd );
        return OIS::InputManager::getVersionNumber();
	*/
}
