/*
The zlib/libpng License

Copyright (c) 2005-2007 Phillip Castaneda (pjcast -- www.wreckedgames.com)

This software is provided 'as-is', without any express or implied warranty. In no event will
the authors be held liable for any damages arising from the use of this software.

Permission is granted to anyone to use this software for any purpose, including commercial 
applications, and to alter it and redistribute it freely, subject to the following
restrictions:

    1. The origin of this software must not be misrepresented; you must not claim that 
		you wrote the original software. If you use this software in a product, 
		an acknowledgment in the product documentation would be appreciated but is 
		not required.

    2. Altered source versions must be plainly marked as such, and must not be 
		misrepresented as being the original software.

    3. This notice may not be removed or altered from any source distribution.
*/
#ifndef OIS_LinuxInputManager_H
#define OIS_LinuxInputManager_H

#include "linux/LinuxPrereqs.h"
#include "OISFactoryCreator.h"
#include "OISInputManager.h"
#include <X11/Xlib.h>

namespace OIS
{
	/**
		Linux X11 InputManager specialization - Using lowlevel joys
	*/
	class LinuxInputManager : public InputManager, public FactoryCreator
	{
	public:
		LinuxInputManager();
		virtual ~LinuxInputManager();

		//InputManager Overrides
		/** @copydoc InputManager::_initialize */
		void _initialize( ParamList &paramList );

		//FactoryCreator Overrides
		/** @copydoc FactoryCreator::deviceList */
		DeviceList freeDeviceList();

		/** @copydoc FactoryCreator::totalDevices */
		int totalDevices(Type iType);

		/** @copydoc FactoryCreator::freeDevices */
		int freeDevices(Type iType);

		/** @copydoc FactoryCreator::vendorExist */
		bool vendorExist(Type iType, const std::string & vendor);

		/** @copydoc FactoryCreator::createObject */
		Object* createObject(InputManager *creator, Type iType, bool bufferMode, const std::string & vendor = "");

		/** @copydoc FactoryCreator::destroyObject */
		void destroyObject(Object* obj);

		//Internal Items
		//! Method for retrieving the XWindow Handle
		Window _getWindow() {return window;}

		//! Internal method for checking if regrabbing is needed
		void _setGrabState(bool grab) {mGrabs = grab;}
		bool _getGrabState() {return mGrabs;}

		//! Internal method, used for flaggin keyboard as available/unavailable for creation
		void _setKeyboardUsed(bool used) {keyboardUsed = used; }

		//! Internal method, used for flaggin mouse as available/unavailable for creation
		void _setMouseUsed(bool used) { mouseUsed = used; }
		
	protected:
		//! internal class method for dealing with param list
		void _parseConfigSettings( ParamList &paramList );
		//! internal class method for finding attached devices
		void _enumerateDevices();

		//! List of unused joysticks ready to be used
		JoyStickInfoList unusedJoyStickList;
		//! Number of joysticks found
		char joySticks;

		//! Used to know if we used up keyboard
		bool keyboardUsed;

		//! Used to know if we used up mouse
		bool mouseUsed;

		//! X11 Stuff
		Window window;
		
		/// Keyboard, Mouse Settings
		bool grabMouse, grabKeyboard;
		bool mGrabs;
		bool hideMouse;
	};
}
#endif
