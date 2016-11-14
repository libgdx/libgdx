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

#ifndef OIS_CocoaInputManager_H
#define OIS_CocoaInputManager_H

#include "OISInputManager.h"
#include "OISFactoryCreator.h"
#include <Cocoa/Cocoa.h>

namespace OIS
{
    class MacHIDManager;
    
    class CocoaInputManager : public InputManager, public FactoryCreator
    {
    public:
        CocoaInputManager();
        virtual ~CocoaInputManager();
        
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

        //! method for getting window
        NSWindow * _getWindow() {return mWindow;}
        
    protected:        
        void _parseConfigSettings( ParamList& paramList );
        
        void _enumerateDevices();
        
        static const std::string iName;
        
        // Mac stuff
		NSWindow *mWindow;
        
        // settings
        bool mHideMouse;
        bool mUseRepeat;

		//! Used to know if we used up keyboard
		bool keyboardUsed;

		//! Used to know if we used up mouse
		bool mouseUsed;
		
		//! HID Manager class handling devices other than keyboard/mouse
		MacHIDManager *mHIDManager;
    };
}
#endif
