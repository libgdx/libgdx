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
#ifndef OIS_InputManager_H
#define OIS_InputManager_H

#include "OISPrereqs.h"

namespace OIS
{
	//Forward declare a couple of classes we might use later
	class LIRCFactoryCreator;
	class WiiMoteFactoryCreator;

	/**
		Base Manager class. No longer a Singleton; so feel free to create as many InputManager's as you have
		windows.
	*/
	class _OISExport InputManager
	{
	public:
		/**
		@remarks
			Returns version number (useful in DLL/SO libs)
		@returns
			Bits: 1-8 Patch number, 9-16 Minor version, 17-32 Major version
		*/
		static unsigned int getVersionNumber();

		/**
		@remarks
			Returns version string (useful in DLL/SO libs)
		@returns
			Version name
		*/
		const std::string &getVersionName();

		/**
		@remarks
			Creates appropriate input system dependent on platform. 
		@param winHandle
			Contains OS specific window handle (such as HWND or X11 Window)
		@returns
			A pointer to the created manager, or raises Exception
		*/
		static InputManager* createInputSystem( std::size_t winHandle );

		/**
		@remarks
			Creates appropriate input system dependent on platform. 
		@param paramList
			ParamList contains OS specific info (such as HWND and HINSTANCE for window apps),
			and access mode.
		@returns
			A pointer to the created manager, or raises Exception
		*/
		static InputManager* createInputSystem( ParamList &paramList );

		/**
		@remarks
			Destroys the InputManager
		@param manager
			Manager to destroy
		*/
		static void destroyInputSystem(InputManager* manager);

		/**
		@remarks Gets the name of the current platform input system
		*/
		const std::string& inputSystemName();

		/**
		@remarks
			Returns the number of the specified OIS::Type devices discovered by OIS
		@param iType
			Type that you are interested in
		*/
		int getNumberOfDevices( Type iType );

		/**
		@remarks
			Lists all unused devices
		@returns
			DeviceList which contains Type and vendor of device
		*/
		DeviceList listFreeDevices();

		/**
		@remarks
			Tries to create an object with the specified vendor. If you have no
			preference of vendor, leave vender as default (""). Raises exception on failure
		*/
		Object* createInputObject( Type iType, bool bufferMode, const std::string &vendor = "");

		/**
		@remarks Destroys Input Object
		*/
		void destroyInputObject( Object* obj );

		/**
		@remarks
			Add a custom object factory to allow for user controls.
		@param factory
			Factory instance to add
		@notes
			Make sure you do not delete the factory before devices created from
			the factory are destroyed (either by calling RemoveFactoryCreator, or shutting down
			the input system). Order should be something like the following:
				* Create Input System
				* Create Factory Instance
				* AddFactoryCreator(factory)
				* Create a device from the InputManager (device created by factory)
				* One of the follwoing:
					* removeFactoryCreator(factory)
					* inputManager->destroyInputObject(obj)
				* destroyInputSystem(inputManager)
				* destroy Factory Instance
			You can safely delete the factory instance once you have removed it or shut down the
			input manager.
		*/
		void addFactoryCreator( FactoryCreator* factory );

		/**
		@remarks
			Remove a previously added object factory
		@param factory
			Factory object to remove.
		@notes
			Removing a factory will automatically destroy any Objects created from the factory
		*/
		void removeFactoryCreator( FactoryCreator* factory );

		//! All generic devices OIS supports internally (if they are compiled in)
		enum AddOnFactories
		{
			AddOn_All = 0,		//All Devices
			AddOn_LIRC = 1,		//PC Linux Infrared Remote Control
			AddOn_WiiMote = 2	//PC WiiMote Support
		};

		/**
		@remarks
			Enable an addon FactoryCreator extension. By default, none are activated.
			If the desired support was not compiled in, this has no effect. Calling
			multiple times has no effect. Once activated, there is no way to deactivate -
			simply destroy and recreate input manager.
		*/
		void enableAddOnFactory(AddOnFactories factory);

	protected:
		/**
		@remarks
			Called from createInputSystem, gives derived input class a chance to setup after it is created
		*/
		virtual void _initialize(ParamList &paramList) = 0;

		/**
		@remarks
			Derived classes must provide input system name
		*/
		InputManager(const std::string& name);

		/**
		@remarks
			Virtual Destructor - this base class will clean up all devices still opened in mFactoryObjects list
		*/
		virtual ~InputManager();

		//! OIS Version name
		const std::string m_VersionName;

		//! FactoryCreator list	
		FactoryList mFactories;

		//! Factory created objects - useful so we can find creator to send destruction request to
		FactoryCreatedObject mFactoryObjects;

		//! Name of the input system
		const std::string mInputSystemName;

		//! Extra factory (not enabled by default)
		LIRCFactoryCreator *m_lircSupport;
		WiiMoteFactoryCreator *m_wiiMoteSupport;
	};
}
#endif
