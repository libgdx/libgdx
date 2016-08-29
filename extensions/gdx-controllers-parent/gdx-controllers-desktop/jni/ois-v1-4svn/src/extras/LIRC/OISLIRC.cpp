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
#include "OISLIRC.h"
#include "OISLIRCFactoryCreator.h"
#include "OISException.h"

using namespace OIS;

//-----------------------------------------------------------------------------------//
LIRCControl::LIRCControl(InputManager* creator, int id, bool buffered, LIRCFactoryCreator* local_creator, RemoteInfo &info) :
	JoyStick("Generic LIRC", buffered, id, creator),
	mLIRCCreator(local_creator),
	mRingBuffer(OIS_LIRC_EVENT_BUFFER),
	mInfo(info)
{
	//Fill in joystick information
	mState.mButtons.resize(mInfo.buttons);
}

//-----------------------------------------------------------------------------------//
LIRCControl::~LIRCControl()
{
}

//-----------------------------------------------------------------------------------//
void LIRCControl::_initialize()
{
	mState.clear();
}

//-----------------------------------------------------------------------------------//
void LIRCControl::setBuffered(bool buffered)
{
	mBuffered = buffered;
}

//-----------------------------------------------------------------------------------//
void LIRCControl::capture()
{
	//Anything to read?
	int entries = mRingBuffer.GetReadAvailable();
	if( entries <= 0 )
		return;

	LIRCEvent events[OIS_LIRC_EVENT_BUFFER];
	if( entries > OIS_LIRC_EVENT_BUFFER )
		entries = OIS_LIRC_EVENT_BUFFER;
	
	mRingBuffer.Read(events, entries);

	//Loop through each event
	for( int i = 0; i < entries; ++i )
	{
		if( mBuffered && mListener )
		{
			//Quickly send off button events (there is no real stored state)
			//As, even a held down button will kep generating button presses
			mState.mButtons[events[i].button] = true;
			if( !mListener->buttonPressed(JoyStickEvent(this, mState), events[i].button) )
				return;

			mState.mButtons[events[i].button] = false;
			if( !mListener->buttonReleased(JoyStickEvent(this, mState), events[i].button) )
				return;
		}
	}
}

//-----------------------------------------------------------------------------------//
void LIRCControl::queueButtonPressed(const std::string &id)
{
	if( mRingBuffer.GetWriteAvailable() > 0 )
	{
		LIRCEvent evt;
		evt.button = mInfo.buttonMap[id];
		mRingBuffer.Write(&evt, 1);
	}
}

//-----------------------------------------------------------------------------------//
Interface* LIRCControl::queryInterface(Interface::IType type)
{
	return 0;
}
#endif
