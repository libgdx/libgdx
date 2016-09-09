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

#include "mac/CocoaInputManager.h"
#include "mac/CocoaKeyboard.h"
#include "mac/CocoaMouse.h"
#include "mac/MacHIDManager.h"
#include "OISException.h"

using namespace std;
using namespace OIS;

//--------------------------------------------------------------------------------//
CocoaInputManager::CocoaInputManager() : InputManager("Mac OS X Cocoa Input Manager")
{
    mHideMouse = true;
    mUseRepeat = false;
	mWindow = nil;

	keyboardUsed = mouseUsed = false;

	//Setup our internal factories
	mFactories.push_back(this);

	mHIDManager = new MacHIDManager();
	mFactories.push_back(mHIDManager);
}

//--------------------------------------------------------------------------------//
CocoaInputManager::~CocoaInputManager()
{
	delete mHIDManager;
}

//--------------------------------------------------------------------------------//
void CocoaInputManager::_initialize( ParamList &paramList )
{
	_parseConfigSettings( paramList );
    
	//Enumerate all devices attached
	_enumerateDevices();
	
	mHIDManager->initialize();
}

//--------------------------------------------------------------------------------//
void CocoaInputManager::_parseConfigSettings( ParamList &paramList )
{
    // Some carbon apps are running in a window, however full screen apps
	// do not have a window, so we need to account for that too.
	ParamList::iterator i = paramList.find("WINDOW");
	if(i != paramList.end())
	{
		mWindow = (NSWindow *)strtoul(i->second.c_str(), 0, 10);
		if(mWindow == 0)
		{
			mWindow = nil;
		}
    }
	else
	{
		// else get the main active window.. user might not have access to it through some
		// graphics libraries, if that fails then try at the application level.
		mWindow = [[NSApplication sharedApplication] keyWindow];
	}
	
	if(mWindow == nil)
		OIS_EXCEPT( E_General, "CocoaInputManager::_parseConfigSettings >> Unable to find a window or event target" );
    
    // Keyboard
    if(paramList.find("MacAutoRepeatOn") != paramList.end())
	{
        if(paramList.find("MacAutoRepeatOn")->second == "true")
		{
            mUseRepeat = true;
        }
    }
}

//--------------------------------------------------------------------------------//
void CocoaInputManager::_enumerateDevices()
{
}

//--------------------------------------------------------------------------------//
DeviceList CocoaInputManager::freeDeviceList()
{
	DeviceList ret;

	if( keyboardUsed == false )
		ret.insert(std::make_pair(OISKeyboard, mInputSystemName));

	if( mouseUsed == false )
		ret.insert(std::make_pair(OISMouse, mInputSystemName));

	return ret;
}

//--------------------------------------------------------------------------------//
int CocoaInputManager::totalDevices(Type iType)
{
	switch(iType)
	{
	case OISKeyboard: return 1;
	case OISMouse: return 1;
	default: return 0;
	}
}

//--------------------------------------------------------------------------------//
int CocoaInputManager::freeDevices(Type iType)
{
	switch(iType)
	{
	case OISKeyboard: return keyboardUsed ? 0 : 1;
	case OISMouse: return mouseUsed ? 0 : 1;
	default: return 0;
	}
}

//--------------------------------------------------------------------------------//
bool CocoaInputManager::vendorExist(Type iType, const std::string & vendor)
{
	if( (iType == OISKeyboard || iType == OISMouse) && vendor == mInputSystemName )
		return true;

	return false;
}

//--------------------------------------------------------------------------------//
Object* CocoaInputManager::createObject(InputManager* creator, Type iType, bool bufferMode, 
									  const std::string & vendor)
{
	Object *obj = 0;

	switch(iType)
	{
        case OISKeyboard: 
        {
            if( keyboardUsed == false )
                obj = new CocoaKeyboard(this, bufferMode, mUseRepeat);
            break;
        }
        case OISMouse:
        {
            if( mouseUsed == false )
                obj = new CocoaMouse(this, bufferMode);
            break;
        }
        default:
        {
            obj = mHIDManager->createObject(creator, iType, bufferMode, vendor);
            break;
        }
	}

	if( obj == 0 )
		OIS_EXCEPT(E_InputDeviceNonExistant, "No devices match requested type.");

	return obj;
}

//--------------------------------------------------------------------------------//
void CocoaInputManager::destroyObject(Object* obj)
{
	delete obj;
}
