#include "OISConfig.h"
#ifdef OIS_LIRC_SUPPORT
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
#ifndef OIS_LIRCFactoryCreator_H
#define OIS_LIRCFactoryCreator_H

#include "OISPrereqs.h"
#include "OISFactoryCreator.h"
#include "OISLIRC.h"

namespace OIS
{
	//Forward declare local classes
	class LIRCControl;

	/** LIRC Factory Creator Class */
	class _OISExport LIRCFactoryCreator : public FactoryCreator
	{
	public:
		LIRCFactoryCreator();
		~LIRCFactoryCreator();

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

	protected:
		//! Gets a list of all remotes available
		void discoverRemotes();

		//! Connects to LIRC server
		void enableConnection(bool enable, bool blocking = true);

		//! Creates/destroys threaded read
		void enableConnectionThread(bool enable);

		void threadUpdate();

		std::string mIP;
		std::string mPort;
		bool mConnected;
		volatile bool mThreadRunning;
		std::map<std::string, LIRCControl*> mUpdateRemotes;

		//! List of vendor named remotes that are not used yet
		std::vector<std::string> mUnusedRemotes;
		
		//! Information about enumerated remotes
		std::map<std::string, RemoteInfo> mJoyStickInformation;

		//! Number of total found remotes
		int mCount;

		//! Get the slow boost header includes from this header by using a proxy wrapper
		class BoostWrapper;

		//! Wrapped objects
		BoostWrapper *mWrapped;
	};
}
#endif //OIS_LIRCFactoryCreator_H
#endif
