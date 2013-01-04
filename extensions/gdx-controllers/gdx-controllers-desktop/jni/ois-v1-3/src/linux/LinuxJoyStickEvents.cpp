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
#include "OISConfig.h"

#include "linux/LinuxJoyStickEvents.h"
#include "linux/LinuxInputManager.h"
#include "linux/LinuxForceFeedback.h"
#include "linux/EventHelpers.h"

#include "OISEvents.h"
#include "OISException.h"

#include <fcntl.h>        //Needed to Open a file descriptor
#include <cassert>	
#include <linux/input.h>


#include <sstream>
# include <iostream>
using namespace std;

using namespace OIS;

//#define OIS_LINUX_JOY_DEBUG

//-------------------------------------------------------------------//
LinuxJoyStick::LinuxJoyStick(InputManager* creator, bool buffered, const JoyStickInfo& js)
	: JoyStick(js.vendor, buffered, js.devId, creator)
{
	mJoyStick = js.joyFileD;

	mState.mAxes.clear();
	mState.mAxes.resize(js.axes);
	mState.mButtons.clear();
	mState.mButtons.resize(js.buttons);

	mPOVs = js.hats;

	mButtonMap = js.button_map;
	mAxisMap = js.axis_map;
	mRanges = js.axis_range;

	ff_effect = 0;
}

//-------------------------------------------------------------------//
LinuxJoyStick::~LinuxJoyStick()
{
	EventUtils::removeForceFeedback( &ff_effect );
}

//-------------------------------------------------------------------//
void LinuxJoyStick::_initialize()
{
	//Clear old joy state
	mState.mAxes.resize(mAxisMap.size());
	mState.clear();

	//This will create and new us a force feedback structure if it exists
	EventUtils::enumerateForceFeedback( mJoyStick, &ff_effect );

	if( mJoyStick == -1 )
		OIS_EXCEPT(E_InputDeviceNonExistant, "LinuxJoyStick::_initialize() >> JoyStick Not Found!");
}

//-------------------------------------------------------------------//
void LinuxJoyStick::capture()
{
	static const short POV_MASK[8] = {0,0,1,1,2,2,3,3};

	//Used to determine if an axis has been changed and needs an event
	bool axisMoved[32] = {false, false, false, false, false, false, false, false, false, false, false, false, false,
						  false, false, false, false, false, false, false, false, false, false, false, false, false,
						  false, false, false, false, false, false};

	//We are in non blocking mode - we just read once, and try to fill up buffer
	input_event js[JOY_BUFFERSIZE];
	while(true)
	{
		int ret = read(mJoyStick, &js, sizeof(struct input_event) * JOY_BUFFERSIZE);
        if( ret < 0 )
			break;

		//Determine how many whole events re read up
		ret /= sizeof(struct input_event);
		for(int i = 0; i < ret; ++i)
		{
			switch(js[i].type)
			{
			case EV_KEY:  //Button
			{
				int button = mButtonMap[js[i].code];

				#ifdef OIS_LINUX_JOY_DEBUG
				  cout << "\nButton Code: " << js[i].code << ", OIS Value: " << button << endl;
				#endif

				//Check to see whether push or released event...
				if(js[i].value)
				{
					mState.mButtons[button] = true;
					if( mBuffered && mListener )
						if(!mListener->buttonPressed(JoyStickEvent(this,mState), button)) return;
				}
				else
				{
					mState.mButtons[button] = false;
					if( mBuffered && mListener )
						if(!mListener->buttonReleased(JoyStickEvent(this,mState), button)) return;
				}
				break;
			}

			case EV_ABS:  //Absolute Axis
			{
				//A Stick (BrakeDefine is the highest possible Axis)
				if( js[i].code <= ABS_BRAKE )
				{
					int axis = mAxisMap[js[i].code];
					assert( axis < 32 && "Too many axes (Max supported is 32). Report this to OIS forums!" );

					axisMoved[axis] = true;

					//check for rescaling:
					if( mRanges[axis].min == JoyStick::MIN_AXIS && mRanges[axis].max != JoyStick::MAX_AXIS )
					{	//Scale is perfect
						mState.mAxes[axis].abs = js[i].value;
					}
					else
					{	//Rescale
						float proportion = (float)(js[i].value-mRanges[axis].max)/(float)(mRanges[axis].min-mRanges[axis].max);
						mState.mAxes[axis].abs = (int)(32767.0f - (65535.0f * proportion));
					}
				}
				else if( js[i].code <= ABS_HAT3Y ) //A POV - Max four POVs allowed
				{
					//Normalise the POV to between 0-7
					//Even is X Axis, Odd is Y Axis
					unsigned char LinuxPovNumber = js[i].code - 16;
					short OIS_POVIndex = POV_MASK[LinuxPovNumber];

					//Handle X Axis first (Even) (left right)
					if((LinuxPovNumber & 0x0001) == 0)
					{
						//Why do this? Because, we use a bit field, and when this axis is east,
						//it can't possibly be west too. So clear out the two X axes, then refil
						//it in with the new direction bit.
						//Clear the East/West Bit Flags first
						mState.mPOV[OIS_POVIndex].direction &= 0x11110011;
						if( js[i].value == -1 )	//Left
							mState.mPOV[OIS_POVIndex].direction |= Pov::West;
						else if( js[i].value == 1 ) //Right
							mState.mPOV[OIS_POVIndex].direction |= Pov::East;
					}
					//Handle Y Axis (Odd) (up down)
					else
					{
						//Clear the North/South Bit Flags first
						mState.mPOV[OIS_POVIndex].direction &= 0x11111100;
						if( js[i].value == -1 )	//Up
							mState.mPOV[OIS_POVIndex].direction |= Pov::North;
						else if( js[i].value == 1 ) //Down
							mState.mPOV[OIS_POVIndex].direction |= Pov::South;
					}

					if( mBuffered && mListener )
						if( mListener->povMoved( JoyStickEvent(this,mState), OIS_POVIndex) == false )
							return;
				}
				break;
			}

			
			case EV_REL: //Relative Axes (Do any joystick actually have a relative axis?)
	#ifdef OIS_LINUX_JOY_DEBUG
				cout << "\nWarning: Relatives axes not supported yet" << endl;
	#endif
				break;
			default: break;
			}
		}
	}

	//All axes and POVs are combined into one movement per pair per captured frame
	if( mBuffered && mListener )
	{
		for( int i = 0; i < 32; ++i )
			if( axisMoved[i] )
				if( mListener->axisMoved( JoyStickEvent(this,mState), i) == false )
					return;
	}
}

//-------------------------------------------------------------------//
void LinuxJoyStick::setBuffered(bool buffered)
{
	if( buffered != mBuffered )
	{
		mBuffered = buffered;
		_initialize();
	}
}

//-------------------------------------------------------------------//
JoyStickInfo LinuxJoyStick::_getJoyInfo()
{
	JoyStickInfo js;

	js.devId = mDevID;
	js.joyFileD = mJoyStick;
	js.vendor = mVendor;
	js.axes = (int)mState.mAxes.size();
	js.buttons = (int)mState.mButtons.size();
	js.hats = mPOVs;
	js.button_map = mButtonMap;
	js.axis_map = mAxisMap;
	js.axis_range = mRanges;

	return js;
}

//-------------------------------------------------------------------//
JoyStickInfoList LinuxJoyStick::_scanJoys()
{
	JoyStickInfoList joys;

	//Search through all of the event devices.. and identify which ones are joysticks
	//xxx move this to InputManager, as it can also scan all other events
	for(int i = 0; i < 64; ++i )
	{
		stringstream s;
		s << "/dev/input/event" << i;
		int fd = open( s.str().c_str(), O_RDWR |O_NONBLOCK );
		if(fd == -1)
			continue;

        #ifdef OIS_LINUX_JOY_DEBUG
		  cout << "Opening " << s.str() << "..." << endl;
        #endif
		try
		{
			JoyStickInfo js;
			if( EventUtils::isJoyStick(fd, js) )
			{
				joys.push_back(js);
                #ifdef OIS_LINUX_JOY_DEBUG
                  cout << "=> Joystick added to list." << endl;
                #endif
			}
			else
			{
                #ifdef OIS_LINUX_JOY_DEBUG
                  cout << "=> Not a joystick." << endl;
                #endif
				close(fd);
			}
		}
		catch(...)
		{
            #ifdef OIS_LINUX_JOY_DEBUG
              cout << "Exception caught!!" << endl;
            #endif
			close(fd);
		}
	}

	return joys;
}

//-------------------------------------------------------------------//
void LinuxJoyStick::_clearJoys(JoyStickInfoList &joys)
{
	for(JoyStickInfoList::iterator i = joys.begin(); i != joys.end(); ++i)
		close(i->joyFileD);
	joys.clear();
}

//-------------------------------------------------------------------//
Interface* LinuxJoyStick::queryInterface(Interface::IType type)
{
	if( ff_effect && type == Interface::ForceFeedback )
		return ff_effect;

	return 0;
}
