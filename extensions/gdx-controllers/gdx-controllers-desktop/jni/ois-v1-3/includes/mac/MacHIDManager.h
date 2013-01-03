/*
 The zlib/libpng License
 
 Copyright (c) 2007 Phillip
 
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
#ifndef OIS_MacHIDManager_Header
#define OIS_MacHIDManager_Header

#include "OISPrereqs.h"
#include "mac/MacPrereqs.h"
#include "OISFactoryCreator.h"

#import <CoreFoundation/CFString.h>
#import <IOKit/IOKitLib.h>
#import <IOKit/IOCFPlugIn.h>
#import <IOKit/hid/IOHIDLib.h>
#import <IOKit/hid/IOHIDKeys.h>
#import <Kernel/IOKit/hidsystem/IOHIDUsageTables.h>

namespace OIS
{
	//Information needed to create Mac HID Devices
	class HidInfo
	{
	public:
		HidInfo() : type(OISUnknown), numButtons(0), numHats(0), numAxes(0), inUse(false), interface(0)
		{
		}

		//Useful tracking information
		Type type;
		std::string vendor;
		std::string productKey;
		std::string combinedKey;

		//Retain some count information for recreating devices without having to reparse
		int numButtons;
		int numHats;
		int numAxes;
		bool inUse;

		//Used for opening a read/write/tracking interface to device
		IOHIDDeviceInterface **interface;
	};

	typedef std::vector<HidInfo*> HidInfoList;
		
	class MacHIDManager : public FactoryCreator
	{
	public:
		MacHIDManager();
		~MacHIDManager();

		void initialize();
		
		void iterateAndOpenDevices(io_iterator_t iterator);
		io_iterator_t lookUpDevices(int usage, int page);

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

	private:
		HidInfo* enumerateDeviceProperties(CFMutableDictionaryRef propertyMap);
		void parseDeviceProperties(CFDictionaryRef properties);
		void parseDevicePropertiesGroup(CFDictionaryRef properties);

		HidInfoList mDeviceList;		
	};
}
#endif
