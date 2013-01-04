#include <com.badlogic.gdx.controllers.OisWrapper.h>

//@line:10

	#include <OISJoyStick.h>
	#include <OISInputManager.h>
	 JNIEXPORT jint JNICALL Java_com_badlogic_gdx_controllers_OisWrapper_initialize(JNIEnv* env, jclass clazz, jint hwnd) {


//@line:15

        size_t windowHnd = 0;
        OIS::InputManager::createInputSystem( windowHnd );
        return OIS::InputManager::getVersionNumber();
	

}

