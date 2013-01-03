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
#include "linux/LinuxInputManager.h"
#include "linux/LinuxKeyboard.h"
#include "linux/LinuxJoyStickEvents.h"
#include "linux/LinuxMouse.h"
#include "OISException.h"
#include <cstdlib>

using namespace OIS;

//--------------------------------------------------------------------------------//
LinuxInputManager::LinuxInputManager() : InputManager("X11InputManager")
{
	window = 0;

	//Default settings
	grabMouse = true;
	grabKeyboard = true;
	hideMouse = true;
	mGrabs = true;
	keyboardUsed = mouseUsed = false;

	//Setup our internal factories
	mFactories.push_back(this);
}

//--------------------------------------------------------------------------------//
LinuxInputManager::~LinuxInputManager()
{
	//Close all joysticks
	LinuxJoyStick::_clearJoys(unusedJoyStickList);
}

//--------------------------------------------------------------------------------//
void LinuxInputManager::_initialize( ParamList &paramList )
{
	_parseConfigSettings( paramList );

	//Enumerate all devices attached
	_enumerateDevices();
}

//--------------------------------------------------------------------------------//
void LinuxInputManager::_parseConfigSettings( ParamList &paramList )
{
	ParamList::iterator i = paramList.find("WINDOW");
	if( i == paramList.end() ) 
		OIS_EXCEPT( E_InvalidParam, "LinuxInputManager >> No WINDOW!" );

	//TODO 64 bit proof this little conversion xxx wip
	window  = strtoul(i->second.c_str(), 0, 10);

	//--------- Keyboard Settings ------------//
	i = paramList.find("x11_keyboard_grab");
	if( i != paramList.end() )
		if( i->second == "false" )
			grabKeyboard = false;

	//--------- Mouse Settings ------------//
	i = paramList.find("x11_mouse_grab");
	if( i != paramList.end() )
		if( i->second == "false" )
			grabMouse = false;

	i = paramList.find("x11_mouse_hide");
	if( i != paramList.end() )
		if( i->second == "false" )
			hideMouse = false;
}

//--------------------------------------------------------------------------------//
void LinuxInputManager::_enumerateDevices()
{
	//Enumerate all attached devices
	unusedJoyStickList = LinuxJoyStick::_scanJoys();
	joySticks = unusedJoyStickList.size();
}

//----------------------------------------------------------------------------//
DeviceList LinuxInputManager::freeDeviceList()
{
	DeviceList ret;

	if( keyboardUsed == false )
		ret.insert(std::make_pair(OISKeyboard, mInputSystemName));

	if( mouseUsed == false )
		ret.insert(std::make_pair(OISMouse, mInputSystemName));

	for(JoyStickInfoList::iterator i = unusedJoyStickList.begin(); i != unusedJoyStickList.end(); ++i)
		ret.insert(std::make_pair(OISJoyStick, i->vendor));

	return ret;
}

//----------------------------------------------------------------------------//
int LinuxInputManager::totalDevices(Type iType)
{
	switch(iType)
	{
	case OISKeyboard: return 1;
	case OISMouse: return 1;
	case OISJoyStick: return joySticks;
	default: return 0;
	}
}

//----------------------------------------------------------------------------//
int LinuxInputManager::freeDevices(Type iType)
{
	switch(iType)
	{
	case OISKeyboard: return keyboardUsed ? 0 : 1;
	case OISMouse: return mouseUsed ? 0 : 1;
	case OISJoyStick: return (int)unusedJoyStickList.size();
	default: return 0;
	}
}

//----------------------------------------------------------------------------//
bool LinuxInputManager::vendorExist(Type iType, const std::string & vendor)
{
	if( (iType == OISKeyboard || iType == OISMouse) && vendor == mInputSystemName )
	{
		return true;
	}
	else if( iType == OISJoyStick )
	{
		for(JoyStickInfoList::iterator i = unusedJoyStickList.begin(); i != unusedJoyStickList.end(); ++i)
			if(i->vendor == vendor)
				return true;
	}

	return false;
}

//----------------------------------------------------------------------------//
Object* LinuxInputManager::createObject(InputManager *creator, Type iType, bool bufferMode, const std::string & vendor)
{
	Object *obj = 0;

	switch(iType)
	{
	case OISKeyboard:
	{
		if( keyboardUsed == false )
			obj = new LinuxKeyboard(this, bufferMode, grabKeyboard);
		break;
	}
	case OISMouse:
	{
		if( mouseUsed == false )
			obj = new LinuxMouse(this, bufferMode, grabMouse, hideMouse);
		break;
	}
	case OISJoyStick:
	{
		for(JoyStickInfoList::iterator i = unusedJoyStickList.begin(); i != unusedJoyStickList.end(); ++i)
		{
			if(vendor == "" || i->vendor == vendor)
			{
				obj = new LinuxJoyStick(this, bufferMode, *i);
				unusedJoyStickList.erase(i);
				break;
			}
		}
		break;
	}
	default:
		break;
	}

	if( obj == 0 )
		OIS_EXCEPT(E_InputDeviceNonExistant, "No devices match requested type.");

	return obj;
}

//----------------------------------------------------------------------------//
void LinuxInputManager::destroyObject( Object* obj )
{
	if( obj )
	{
		if( obj->type() == OISJoyStick )
		{
			unusedJoyStickList.push_back( ((LinuxJoyStick*)obj)->_getJoyInfo() );
		}

		delete obj;
	}
}
