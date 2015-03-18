#include "OISConfig.h"
#ifdef OIS_WIN32_WIIMOTE_SUPPORT
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
#ifndef OIS_WiiMoteFactoryCreator_H
#define OIS_WiiMoteFactoryCreator_H

#include "OISPrereqs.h"
#include "OISFactoryCreator.h"
#include <deque>

//Forward declare boost classes used
namespace boost
{
	class thread;
	class mutex;
}

namespace OIS
{
	//Forward declare local classes
	class WiiMote;

	//! Max amount of Wiis we will attempt to find
	#define OIS_cWiiMote_MAX_WIIS 4

	/** WiiMote Factory Creator Class */
	class _OISExport WiiMoteFactoryCreator : public FactoryCreator
	{
	public:
		WiiMoteFactoryCreator();
		~WiiMoteFactoryCreator();

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

		//! Local method used to return controller to pool
		void _returnWiiMote(int id);

	protected:
		//! Internal - threaded method
		bool _updateWiiMotesThread();

		//! String name of this vendor
		std::string mVendorName;

		//! queue of open wiimotes (int represents index into hid device)
		std::deque<int> mFreeWiis;

		//! Number of total wiimotes
		int mCount;

		//! Boost thread execution object (only alive when at least 1 wiimote is alive)
		boost::thread *mtThreadHandler;
		
		//! Gaurds access to the Active WiiMote List
		boost::mutex *mtWiiMoteListMutex;

		//! List of created (active) WiiMotes
		std::vector<WiiMote*> mtInUseWiiMotes;

		//! Used to signal thread running or not
		volatile bool mtThreadRunning;
	};
}
#endif //OIS_WiiMoteFactoryCreator_H
#endif
