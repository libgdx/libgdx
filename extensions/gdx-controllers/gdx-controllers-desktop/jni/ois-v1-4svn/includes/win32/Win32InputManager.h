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
#ifndef OIS_Win32InputManager_H
#define OIS_Win32InputManager_H

#include "OISInputManager.h"
#include "OISFactoryCreator.h"
#include "win32/Win32Prereqs.h"

namespace OIS
{
	/**	Win32InputManager specialization - Using DirectInput8 */
	class Win32InputManager : public InputManager, public FactoryCreator
	{
	public:
		Win32InputManager();
		virtual ~Win32InputManager();

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
		Object* createObject(InputManager* creator, Type iType, bool bufferMode, const std::string & vendor = "");

		/** @copydoc FactoryCreator::destroyObject */
		void destroyObject(Object* obj);

		//Internal Items
		//! Internal method, used for flaggin keyboard as available/unavailable for creation
		void _setKeyboardUsed(bool used) {keyboardUsed = used; }

		//! Internal method, used for flaggin mouse as available/unavailable for creation
		void _setMouseUsed(bool used) { mouseUsed = used; }
		
		//! Internal method, return unused joystick to queue
		void _returnJoyStick(const JoyStickInfo& joystick);

		//! Returns HWND needed by DirectInput Device Object
		HWND getWindowHandle() { return hWnd; }

	protected:
		//! internal class method for dealing with param list
		void _parseConfigSettings( ParamList &paramList );
		
		//! internal class method for finding attached devices
		void _enumerateDevices();

		//! Used during device enumeration
		static BOOL CALLBACK _DIEnumDevCallback(LPCDIDEVICEINSTANCE lpddi, LPVOID pvRef);

		//! Keep a list of all joysticks enumerated, but not in use
		JoyStickInfoList unusedJoyStickList;

		//! The window handle we are using
		HWND hWnd;

		//! Direct Input Interface
		IDirectInput8* mDirectInput;

		//! Used for keyboard device settings
		DWORD kbSettings;

		//! Used for mouse device settings
		DWORD mouseSettings;

		//! Used for joystick device settings
		DWORD joySettings;

		//! Number of total joysticks (inuse or not)
		char joySticks;

		//! Used to know if we used up keyboard
		bool keyboardUsed;

		//! Used to know if we used up mouse
		bool mouseUsed;
	};
}
#endif
