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
#include "OISLIRCFactoryCreator.h"
#include "OISException.h"
#include <assert.h>
#include <stdlib.h>

#ifdef OIS_WIN32_PLATFORM
#  pragma warning (disable : 4996)
#  pragma warning (disable : 4267)
#  pragma warning (disable : 4554)
#  pragma warning (disable : 4996)
#  define _WIN32_WINNT 0x0500
#endif
#include <boost/asio.hpp>
#include <boost/bind.hpp>
#include <boost/thread.hpp>

#include <istream>
#include <sstream>

using namespace OIS;

//---------------------------------------------------------------------------------//
class LIRCFactoryCreator::BoostWrapper
{
public:
	LIRCFactoryCreator::BoostWrapper() : mSocket(mIOService), mThreadHandler(0)
	{
	}

	//TCP stuff
	boost::asio::io_service mIOService;
	boost::asio::ip::tcp::socket mSocket;

	//Thread Stuff
	//! Boost thread execution object (only alive when at least 1 lirc is alive)
	boost::thread *mThreadHandler;
		
	//! Gaurds access to the active lirc list
	boost::mutex mLircListMutex;

};

//---------------------------------------------------------------------------------//
LIRCFactoryCreator::LIRCFactoryCreator() :
	mConnected(false),
	mThreadRunning(false),
	mCount(0),
	mWrapped(0)
{
	mWrapped = new BoostWrapper();

	mIP   = (getenv("OIS_LIRC_IP") != 0) ? getenv("OIS_LIRC_IP") : "127.0.0.1";
	mPort = (getenv("OIS_LIRC_PORT") != 0) ? getenv("OIS_LIRC_PORT") : "8765";

	try
	{
		enableConnection(true);
		discoverRemotes();
	}
	catch(...)
	{
		mCount = 0;
	}

	//Regardless of if there is remotes or not, we will close the conenction now.
	enableConnection(false);
}

//---------------------------------------------------------------------------------//
LIRCFactoryCreator::~LIRCFactoryCreator()
{
	enableConnectionThread(false);
	enableConnection(false);

	delete mWrapped;
}

//---------------------------------------------------------------------------------//
void LIRCFactoryCreator::discoverRemotes()
{
	//http://www.lirc.org/html/technical.html#applications
	mCount = 0;

	mWrapped->mSocket.write_some(boost::asio::buffer("LIST\n"));

	boost::asio::streambuf buffer;

	//Read all remotes
	bool start = false;
	bool data = false;
	for(;;)
	{
		boost::asio::read_until(mWrapped->mSocket, buffer, '\n');

		std::istream str(&buffer);
		std::string res;
		str >> res;

		if( res == "" )				//If nothing left, we are done
			break;
		else if( res == "ERROR" )	//If any errors, we leave immediately
			return;
		else if( res == "END" )		//We have reached the end block
			start = false;
		else if( res == "DATA" )	//After Data will be a list of remote names
		{
			start = true;
			data = true;
			continue;
		}

		//Have we  gotten the DATA word yet?
		if( start == false )
			continue;

		if( data ) //How many?
			mCount = atoi(res.c_str());
		else //What follows should now be a list of remote names
			mUnusedRemotes.push_back(res);

		data = false;
	}

	//Read information about each remote
	boost::asio::streambuf buffer2;
	for( int i = 0; i < mCount; ++i )
	{
		std::ostringstream istr;
		istr << "LIST " << mUnusedRemotes[i] << "\n";
		
		mWrapped->mSocket.write_some(boost::asio::buffer(istr.str()));
		RemoteInfo information;
		int buttonCount = 0;

		start = data = false;
		
		for(;;)
		{
			boost::asio::read_until(mWrapped->mSocket, buffer, '\n');

			std::istream str(&buffer);
			std::string res;
			str >> res;

			if( res == "" )				//If nothing left, we are done
				break;
			else if( res == "ERROR" )	//If error, bail out
				return;
			else if( res == "END" )		//We have reached the end block
				start = false;
			else if( res == "DATA" )	//After Data will be button count
			{
				start = true;
				data = true;
				continue;
			}

			//Have we  gotten the DATA word yet?
			if( start == false )
				continue;

			if( data ) //After button count, there will be a list of button names
				information.buttons = atoi(res.c_str());
			else
				information.buttonMap[res] = buttonCount++;

			data = false;
		}

		mJoyStickInformation[mUnusedRemotes[i]] = information;
	}
}

//---------------------------------------------------------------------------------//
void LIRCFactoryCreator::enableConnection(bool enable, bool blocking)
{
	if( enable == true && mConnected == false )
	{
		boost::asio::ip::tcp::resolver resolver(mWrapped->mIOService);
		boost::asio::ip::tcp::resolver::query query(mIP, mPort);
		boost::asio::ip::tcp::resolver::iterator endpoint_iterator = resolver.resolve(query);
		boost::asio::ip::tcp::resolver::iterator end;

		//Connect (trying all found connections - ip4/ip6)
		boost::asio::error result = boost::asio::error::host_not_found;
		while (result && endpoint_iterator != end)
		{
			mWrapped->mSocket.close();
			mWrapped->mSocket.connect(*endpoint_iterator++, boost::asio::assign_error(result));
		}

		if (result != boost::asio::error::success)
			throw (result);

		if( blocking == false )
		{
			mWrapped->mSocket.io_control(boost::asio::socket_base::non_blocking_io(true));
		}

		mConnected = true;
	}
	else if( enable == false )
	{
		mWrapped->mSocket.close();
		mConnected = false;
	}
}

//---------------------------------------------------------------------------------//
void LIRCFactoryCreator::enableConnectionThread(bool enable)
{
	if( enable == true && mThreadRunning == false )
	{
		mThreadRunning = true;
		mWrapped->mThreadHandler = new boost::thread(boost::bind(&LIRCFactoryCreator::threadUpdate, this));
	}
	else if( enable == false && mThreadRunning == true )
	{
		mThreadRunning = false;
		mWrapped->mThreadHandler->join();
		delete mWrapped->mThreadHandler;
		mWrapped->mThreadHandler = 0;
	}
}

//---------------------------------------------------------------------------------//
void LIRCFactoryCreator::threadUpdate()
{
	boost::xtime timer;
	boost::asio::streambuf buffer;
	std::istream stream(&buffer);
	std::string code, repeat, button, remote;


	while( mThreadRunning )
	{
		try
		{
			while(  mWrapped->mSocket.in_avail() > 0 )
			{
				boost::asio::read_until(mWrapped->mSocket, buffer, '\n');
				
				stream >> code;   //64 bit value, ignorable
				stream >> repeat; //Repeat rate starting at zero (we ignore, for now)
				stream >> button; //Button name
				stream >> remote; //Remote name

				{	//Lock object, find out which remote sent event
					boost::mutex::scoped_lock arrayLock(mWrapped->mLircListMutex);
					std::map<std::string, LIRCControl*>::iterator i = mUpdateRemotes.find(remote);
					if( i != mUpdateRemotes.end() )
					{
						i->second->queueButtonPressed(button);
					}
				}
			}
		}
		catch(...)
		{	//Hmm, what should we do if we get a socket error here.. Ignore it I suppose,
		}	//and wait till the used remote objects get shutdown. We could try to 
			//reconnect, but how do we know if we will even get the same remotes.

		boost::xtime_get(&timer, boost::TIME_UTC);
		timer.nsec += 300000000; // 100 000 000 ~= .3 sec
		boost::thread::sleep(timer);
	}
}

//---------------------------------------------------------------------------------//
DeviceList LIRCFactoryCreator::freeDeviceList()
{
	DeviceList list;
	for( std::vector<std::string>::iterator i = mUnusedRemotes.begin(); i != mUnusedRemotes.end(); ++i )
		list.insert(std::make_pair(OISJoyStick, *i));

	return list;
}

//---------------------------------------------------------------------------------//
int LIRCFactoryCreator::totalDevices(Type iType)
{
	if( iType == OISJoyStick )
		return mCount;
	else
		return 0;
}

//---------------------------------------------------------------------------------//
int LIRCFactoryCreator::freeDevices(Type iType)
{
	if( iType == OISJoyStick )
		return (int)mUnusedRemotes.size();
	else
		return 0;
}

//---------------------------------------------------------------------------------//
bool LIRCFactoryCreator::vendorExist(Type iType, const std::string & vendor)
{
	if( iType == OISJoyStick && std::find(mUnusedRemotes.begin(), mUnusedRemotes.end(), vendor) != mUnusedRemotes.end() )
		return true;
	else
		return false;
}

//---------------------------------------------------------------------------------//
Object* LIRCFactoryCreator::createObject(InputManager* creator, Type iType, bool bufferMode, const std::string & vendor)
{
	if( mUnusedRemotes.size() > 0 )
	{
		std::vector<std::string>::iterator remote = mUnusedRemotes.end();
		if( vendor == "" )
			remote = mUnusedRemotes.begin();
		else
			remote = std::find(mUnusedRemotes.begin(), mUnusedRemotes.end(), vendor);

		if( remote != mUnusedRemotes.end() )
		{
			//Make sure connection is established
			enableConnection(true, false);

			//Make sure connection thread is alive
			enableConnectionThread(true);

			//Create device
			LIRCControl *obj = new LIRCControl(creator, 0, bufferMode, this, mJoyStickInformation[*remote]);

			//Add to used list, and then remove from unused list
			{
				boost::mutex::scoped_lock arrayLock(mWrapped->mLircListMutex);
				mUpdateRemotes[*remote] = obj;
			}
			mUnusedRemotes.erase(remote);

			return obj;
		}
	}
	
	OIS_EXCEPT(E_InputDeviceNonExistant, "No Device found which matches description!");
}

//---------------------------------------------------------------------------------//
void LIRCFactoryCreator::destroyObject(Object* obj)
{
	if( obj == 0 )
		return;

	int remotes_alive = 0;

	{	//Scope lock
		boost::mutex::scoped_lock arrayLock(mWrapped->mLircListMutex);

		//Find object
		std::map<std::string, LIRCControl*>::iterator i = mUpdateRemotes.begin(), e = mUpdateRemotes.end();
		bool found = false;
		for(; i != e; ++i)
		{
			if( i->second == obj )
			{
				found = true;
				break;
			}
		}

		if( found == false )
			OIS_EXCEPT(E_General, "Device not found in LIRC remote collection!");

		//Move from used to unused list
		mUnusedRemotes.push_back(i->first);
		mUpdateRemotes.erase(i);
		
		delete obj;

		remotes_alive = (int)mUpdateRemotes.size();
	}

	//Destroy thread if no longer in use (we do this after unlocking mutex!)
	if( remotes_alive == 0 )
		enableConnectionThread(false);
}
#endif
