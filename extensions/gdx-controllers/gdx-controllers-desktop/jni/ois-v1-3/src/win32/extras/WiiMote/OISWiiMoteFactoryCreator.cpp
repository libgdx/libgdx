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
#include "OISWiiMoteFactoryCreator.h"
#include "OISException.h"
#include "OISWiiMote.h"
#include <assert.h>
#include <boost/thread.hpp>   //include here, keep compilation times down
#include <boost/function.hpp>
#include <boost/bind.hpp>


using namespace OIS;

//---------------------------------------------------------------------------------//
WiiMoteFactoryCreator::WiiMoteFactoryCreator() :
	mVendorName("cWiiMote"),
	mCount(0),
	mtThreadHandler(0),
	mtWiiMoteListMutex(0),
	mtThreadRunning(0)
{
	//Discover how many Wii's there are
	for( ; mCount < OIS_cWiiMote_MAX_WIIS; ++mCount )
	{
		cWiiMote wii;
		if( wii.ConnectToDevice(mCount) == false )
			break;
	}

	//Store how many WiiMotes there were in the form of integer handles
	for(int i = 0; i < mCount; ++i)
		mFreeWiis.push_back(i);

	//The mutex lasts the whole life of this class. The thread does not.
	mtWiiMoteListMutex = new boost::mutex();
}

//---------------------------------------------------------------------------------//
WiiMoteFactoryCreator::~WiiMoteFactoryCreator()
{
	//Thread (once all objects destroyed) should be killed off already
	assert( (mtThreadRunning == false && mtThreadHandler == 0) && 
		"~WiiMoteFactoryCreator(): invalid state.. Some objects left dangling!");

	delete mtWiiMoteListMutex;
}

//---------------------------------------------------------------------------------//
DeviceList WiiMoteFactoryCreator::freeDeviceList()
{
	DeviceList list;
	for( std::deque<int>::iterator i = mFreeWiis.begin(); i != mFreeWiis.end(); ++i )
	{
		list.insert(std::make_pair(OISJoyStick, mVendorName));
	}
	return list;
}

//---------------------------------------------------------------------------------//
int WiiMoteFactoryCreator::totalDevices(Type iType)
{
	if( iType == OISJoyStick )
		return mCount;
	else
		return 0;
}

//---------------------------------------------------------------------------------//
int WiiMoteFactoryCreator::freeDevices(Type iType)
{
	if( iType == OISJoyStick )
		return (int)mFreeWiis.size();
	else
		return 0;
}

//---------------------------------------------------------------------------------//
bool WiiMoteFactoryCreator::vendorExist(Type iType, const std::string & vendor)
{
	if( iType == OISJoyStick && mVendorName == vendor )
		return true;
	else
		return false;
}

//---------------------------------------------------------------------------------//
Object* WiiMoteFactoryCreator::createObject(InputManager* creator, Type iType, bool bufferMode, const std::string & vendor)
{
	if( mFreeWiis.size() > 0 && (vendor == "" || vendor == mVendorName ) )
	{
		int id = mFreeWiis.front();
		mFreeWiis.pop_front();
		WiiMote *wii = new WiiMote(creator, id, bufferMode, this);

		if( mtThreadRunning == false )
		{	//Create common thread manager (this is the first wiimote created)
			mtThreadRunning = true;
			mtThreadHandler = new boost::thread(boost::bind(&WiiMoteFactoryCreator::_updateWiiMotesThread, this));
		}
		
		//Now, add new WiiMote to thread manager for polling
		{	//Get an auto lock on the list of active wiimotes
			boost::mutex::scoped_lock arrayLock(*mtWiiMoteListMutex);
			mtInUseWiiMotes.push_back(wii);
		}

		return wii;
	}
	else
		OIS_EXCEPT(E_InputDeviceNonExistant, "No Device found which matches description!");
}

//---------------------------------------------------------------------------------//
void WiiMoteFactoryCreator::destroyObject(Object* obj)
{
	if( obj == 0 )
		return;

	int wiis_alive = 0;

	{	//Get an auto lock on the list of active wiimotes
		boost::mutex::scoped_lock arrayLock(*mtWiiMoteListMutex);

		//Find object
		std::vector<WiiMote*>::iterator i = std::find(mtInUseWiiMotes.begin(), mtInUseWiiMotes.end(), obj);
		if( i == mtInUseWiiMotes.end() )
			OIS_EXCEPT(E_General, "Device not found in wimote collection!");

		//Erase opject
		mtInUseWiiMotes.erase(i);

		//Delete object
		delete obj;

		wiis_alive = (int)mtInUseWiiMotes.size();
	}

	//Destroy thread if no longer in use (we do this after unlocking mutex!)
	if( wiis_alive == 0 && mtThreadRunning )
	{
		mtThreadRunning = false;
		mtThreadHandler->join();
		delete mtThreadHandler;
		mtThreadHandler = 0;
	}

}

//---------------------------------------------------------------------------------//
void WiiMoteFactoryCreator::_returnWiiMote(int id)
{	//Restore ID to controller pool
	mFreeWiis.push_front(id);
}

//---------------------------------------------------------------------------------//
bool WiiMoteFactoryCreator::_updateWiiMotesThread()
{
	boost::xtime timer;

	while(mtThreadRunning)
	{
		int numMotes = 0;
		{	//Get an auto lock on the list of active wiimotes
			boost::mutex::scoped_lock arrayLock(*mtWiiMoteListMutex);
			numMotes = (int)mtInUseWiiMotes.size();
			for( std::vector<WiiMote*>::iterator i = mtInUseWiiMotes.begin(), e = mtInUseWiiMotes.end(); i != e; ++i )
			{	//Update it
				(*i)->_threadUpdate();
			}
		}

		//ok, we have updated all wiimotes, let us rest a bit
		//sleep time = 30 / 1000 
		//boost::thread::sleep(xtime) todo xxx wip use sleep instead??
		//boost::thread::yield();
		boost::xtime_get(&timer, boost::TIME_UTC);
		timer.nsec += 20000000; //20 000 000 ~= 1/50 sec
		boost::thread::sleep(timer);
	}

	return true;
}

#endif
