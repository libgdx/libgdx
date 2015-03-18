/*
 The zlib/libpng License
 
 Copyright (c) 2006 Chris Snyder 
 
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
#ifndef OIS_iPhoneInputManager_H
#define OIS_iPhoneInputManager_H

#include "OISInputManager.h"
#include "OISFactoryCreator.h"
#include "iphone/iPhonePrereqs.h"

#import <UIKit/UIKit.h>
namespace OIS {
    class iPhoneAccelerometer;
    class iPhoneMultiTouch;
}

@interface InputDelegate : UIView <UIAccelerometerDelegate> {
    OIS::iPhoneAccelerometer    *accelerometerObject;
    OIS::iPhoneMultiTouch       *touchObject;
}

@property (assign) OIS::iPhoneAccelerometer     *accelerometerObject;
@property (assign) OIS::iPhoneMultiTouch        *touchObject;

@end

namespace OIS
{

    class iPhoneInputManager : public InputManager, public FactoryCreator
    {
    public:
        iPhoneInputManager();
        virtual ~iPhoneInputManager();
        
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

		//! Internal method, used for flagging multi-touch as available/unavailable for creation
		void _setMultiTouchUsed(bool used) { bMultiTouchUsed = used; }

        //! Internal method, used for flagging accelerometer as available/unavailable for creation
		void _setAccelerometerUsed(bool used) { bAccelerometerUsed = used; }

        //! methodfor getting the delegate
        InputDelegate * _getDelegate() { return mDelegate; }

        //! method for getting window
        UIWindow * _getWindow() { return mWindow; }

    protected:        
        void _parseConfigSettings( ParamList& paramList );

        // iPhone stuff
		UIWindow *mWindow;
        InputDelegate *mDelegate;

        // settings
        bool mHideMouse;

		//! Used to know if we used up multi-touch device
		bool bMultiTouchUsed;

        //! Used to know if we used up accelerometer
		bool bAccelerometerUsed;
    };
}

#endif
