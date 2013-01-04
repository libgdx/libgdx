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
#include "OISInputManager.h"
#include "OISException.h"
#include "OISFactoryCreator.h"
#include "OISObject.h"
#include <sstream>
#include <algorithm>

//Bring in correct Header / InputManager for current build platform
#if defined OIS_SDL_PLATFORM
#  include "SDL/SDLInputManager.h"
#elif defined OIS_WIN32_PLATFORM
#  include "win32/Win32InputManager.h"
#elif defined OIS_LINUX_PLATFORM
#  include "linux/LinuxInputManager.h"
#elif defined OIS_APPLE_PLATFORM
#  include "mac/MacInputManager.h"
#elif defined OIS_IPHONE_PLATFORM
#  include "iphone/iPhoneInputManager.h"
#elif defined OIS_XBOX_PLATFORM
#  include "xbox/XBoxInputManager.h"
#endif

//Bring in extra controls
#if defined OIS_LIRC_SUPPORT
#  include "extras/LIRC/OISLIRCFactoryCreator.h"
#endif
#if defined OIS_WIN32_WIIMOTE_SUPPORT
#  include "win32/extras/WiiMote/OISWiiMoteFactoryCreator.h"
#endif


using namespace OIS;

//----------------------------------------------------------------------------//
InputManager::InputManager(const std::string& name) :
	m_VersionName(OIS_VERSION_NAME),
	mInputSystemName(name),
	m_lircSupport(0),
	m_wiiMoteSupport(0)
{
    mFactories.clear();
    mFactoryObjects.clear();
}

//----------------------------------------------------------------------------//
InputManager::~InputManager()
{
#if defined OIS_LIRC_SUPPORT
	delete m_lircSupport;
#endif

#if defined OIS_WIN32_WIIMOTE_SUPPORT
	delete m_wiiMoteSupport;
#endif
}

//----------------------------------------------------------------------------//
unsigned int InputManager::getVersionNumber()
{
	return OIS_VERSION;
}

//----------------------------------------------------------------------------//
const std::string &InputManager::getVersionName()
{
	return m_VersionName;
}

//----------------------------------------------------------------------------//
InputManager* InputManager::createInputSystem( std::size_t windowhandle )
{
	ParamList pl;
	std::ostringstream wnd;
	wnd << windowhandle;
	pl.insert(std::make_pair( std::string("WINDOW"), wnd.str() ));

	return createInputSystem( pl );
}

//----------------------------------------------------------------------------//
InputManager* InputManager::createInputSystem( ParamList &paramList )
{
	InputManager* im = 0;

#if defined OIS_SDL_PLATFORM
	im = new SDLInputManager();
#elif defined OIS_WIN32_PLATFORM
	im = new Win32InputManager();
#elif defined OIS_XBOX_PLATFORM
	im = new XBoxInputManager();
#elif defined OIS_LINUX_PLATFORM
	im = new LinuxInputManager();
#elif defined OIS_APPLE_PLATFORM
	im = new MacInputManager();
#elif defined OIS_IPHONE_PLATFORM
	im = new iPhoneInputManager();
#else
	OIS_EXCEPT(E_General, "No platform library.. check build platform defines!");
#endif 

	try
	{
		im->_initialize(paramList);
	}
	catch(...)
	{
		delete im;
		throw; //rethrow
	}

	return im;
}

//----------------------------------------------------------------------------//
void InputManager::destroyInputSystem(InputManager* manager)
{
	if( manager == 0 )
		return;

	//Cleanup before deleting...
	for( FactoryCreatedObject::iterator i = manager->mFactoryObjects.begin(); 
		i != manager->mFactoryObjects.end(); ++i )
	{
		i->second->destroyObject( i->first );
	}

	manager->mFactoryObjects.clear();
	delete manager;
}

//--------------------------------------------------------------------------------//
const std::string& InputManager::inputSystemName()
{
	return mInputSystemName;
}

//--------------------------------------------------------------------------------//
int InputManager::getNumberOfDevices( Type iType )
{
	//Count up all the factories devices
	int factoyObjects = 0;
	FactoryList::iterator i = mFactories.begin(), e = mFactories.end();
	for( ; i != e; ++i )
		factoyObjects += (*i)->totalDevices(iType);

	return factoyObjects;
}

//----------------------------------------------------------------------------//
DeviceList InputManager::listFreeDevices()
{
	DeviceList list;
	FactoryList::iterator i = mFactories.begin(), e = mFactories.end();
	for( ; i != e; ++i )
	{
		DeviceList temp = (*i)->freeDeviceList();
		list.insert(temp.begin(), temp.end());
	}

	return list;
}

//----------------------------------------------------------------------------//
Object* InputManager::createInputObject( Type iType, bool bufferMode, const std::string &vendor )
{
	Object* obj = 0;
	FactoryList::iterator i = mFactories.begin(), e = mFactories.end();
	for( ; i != e; ++i)
	{
		if( (*i)->freeDevices(iType) > 0 )
		{
			if( vendor == "" || (*i)->vendorExist(iType, vendor) )
			{
				obj = (*i)->createObject(this, iType, bufferMode, vendor);
				mFactoryObjects[obj] = (*i);
				break;
			}
		}
	}

	if(!obj)
		OIS_EXCEPT(E_InputDeviceNonExistant, "No devices match requested type.");

	try
	{	//Intialize device
		obj->_initialize();
	}
	catch(...)
	{	//Somekind of error, cleanup and rethrow
		destroyInputObject(obj);
		throw;
	}

	return obj;
}

//----------------------------------------------------------------------------//
void InputManager::destroyInputObject( Object* obj )
{
	if( obj == 0 )
		return;

	FactoryCreatedObject::iterator i = mFactoryObjects.find(obj);
	if( i != mFactoryObjects.end() )
	{
		i->second->destroyObject(obj);
		mFactoryObjects.erase(i);
	}
	else
	{
		OIS_EXCEPT(E_General, "Object creator not known.");
	}
}

//----------------------------------------------------------------------------//
void InputManager::addFactoryCreator( FactoryCreator* factory )
{
	if(factory != 0)
		mFactories.push_back(factory);
}

//----------------------------------------------------------------------------//
void InputManager::removeFactoryCreator( FactoryCreator* factory )
{
	if(factory != 0)
	{
		//First, destroy all devices created with the factory
		for( FactoryCreatedObject::iterator i = mFactoryObjects.begin(); i != mFactoryObjects.end(); ++i )
		{
			if( i->second == factory )
			{
				i->second->destroyObject(i->first);
				mFactoryObjects.erase(i++);
			}
		}

		//Now, remove the factory itself
		FactoryList::iterator fact = std::find(mFactories.begin(), mFactories.end(), factory);
		if( fact != mFactories.end() )
			mFactories.erase(fact);
	}
}

//----------------------------------------------------------------------------//
void InputManager::enableAddOnFactory(AddOnFactories factory)
{
#if defined OIS_LIRC_SUPPORT
	if( factory == AddOn_LIRC || factory == AddOn_All )
	{
		if( m_lircSupport == 0 )
		{
			m_lircSupport = new LIRCFactoryCreator();
			addFactoryCreator(m_lircSupport);
		}
	}
#endif

#if defined OIS_WIN32_WIIMOTE_SUPPORT
	if( factory == AddOn_WiiMote || factory == AddOn_All )
	{
		if( m_wiiMoteSupport == 0 )
		{
			m_wiiMoteSupport = new WiiMoteFactoryCreator();
			addFactoryCreator(m_wiiMoteSupport);
		}
	}
#endif
}
