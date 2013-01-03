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
#ifndef OIS_FactoryCreator_H
#define OIS_FactoryCreator_H

#include "OISPrereqs.h"

namespace OIS
{
	/**
		Interface for creating devices - all devices ultimately get enumerated/created via a factory.
		A factory can create multiple types of objects.
	*/
	class _OISExport FactoryCreator
	{
	public:
		/**
			@remarks Virtual Destructor
		*/
		virtual ~FactoryCreator() {};

		/**
			@remarks Return a list of all unused devices the factory maintains
		*/
		virtual DeviceList freeDeviceList() = 0;

		/**
			@remarks Number of total devices of requested type
			@param iType Type of devices to check
		*/
		virtual int totalDevices(Type iType) = 0;

		/**
			@remarks Number of free devices of requested type
			@param iType Type of devices to check
		*/
		virtual int freeDevices(Type iType) = 0;

		/**
			@remarks Does a Type exist with the given vendor name
			@param iType Type to check
			@param vendor Vendor name to test
		*/
		virtual bool vendorExist(Type iType, const std::string & vendor) = 0;

		/**
			@remarks Creates the object
			@param iType Type to create
			@param bufferMode True to setup for buffered events
			@param vendor Create a device with the vendor name, "" means vendor name is unimportant
		*/
		virtual Object* createObject(InputManager* creator, Type iType, bool bufferMode, const std::string & vendor = "") = 0;

		/**
			@remarks Destroys object
			@param obj Object to destroy
		*/
		virtual void destroyObject(Object* obj) = 0;
	};
}
#endif //OIS_FactoryCreator_H
