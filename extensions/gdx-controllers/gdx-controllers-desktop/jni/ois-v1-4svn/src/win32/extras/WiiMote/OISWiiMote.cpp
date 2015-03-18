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
#include "OISWiiMote.h"
#include "OISWiiMoteFactoryCreator.h"
#include "OISException.h"
#include "OISWiiMoteForceFeedback.h"
#define _USE_MATH_DEFINES
#include <math.h>
#include <limits.h>

using namespace OIS;

//-----------------------------------------------------------------------------------//
WiiMote::WiiMote(InputManager* creator, int id, bool buffered, WiiMoteFactoryCreator* local_creator) :
	JoyStick("cWiiMote", buffered, id, creator),
	mWiiCreator(local_creator),
	mtInitialized(false),
	mRingBuffer(OIS_WII_EVENT_BUFFER),
	mtLastButtonStates(0),
	mtLastPOVState(0),
	mtLastX(0.0f),
	mtLastY(1.0f),
	mtLastZ(0.0f),
	mtLastNunChuckX(0.0f),
	mtLastNunChuckY(1.0f),
	mtLastNunChuckZ(0.0f),
	mLastNunChuckXAxis(0),
	mLastNunChuckYAxis(0),
	_mWiiMoteMotionDelay(5),
	mRumble(0)
{
	mRumble = new WiiMoteForceFeedback(mWiiMote);
}

//-----------------------------------------------------------------------------------//
WiiMote::~WiiMote()
{
	delete mRumble;

	if( mWiiMote.IsConnected() )
	{
		mWiiMote.StopDataStream();
		mWiiMote.Disconnect();
	}
	mWiiCreator->_returnWiiMote(mDevID);
}

//-----------------------------------------------------------------------------------//
void WiiMote::_initialize()
{
	if( mWiiMote.ConnectToDevice(mDevID) == false )
		OIS_EXCEPT(E_InputDisconnected, "Error connecting to WiiMote!");

	if( mWiiMote.StartDataStream() == false )
		OIS_EXCEPT(E_InputDisconnected, "Error starting WiiMote data stream!");

	//Fill in joystick information
	mState.mVectors.clear();
	mState.mButtons.clear();
	mState.mAxes.clear();

	if( mWiiMote.IsNunChuckAttached() )
	{	//Setup for WiiMote + nunChuck
		mState.mVectors.resize(2);
		mState.mButtons.resize(9);
		mState.mAxes.resize(2);
		mState.mAxes[0].absOnly = true;
		mState.mAxes[1].absOnly = true;
	}
	else
	{	//Setup for WiiMote
		mState.mVectors.resize(1);
		mState.mButtons.resize(7);
	}

	mPOVs = 1;
	mState.clear();
	mtInitialized = true;
}

//-----------------------------------------------------------------------------------//
void WiiMote::_threadUpdate()
{
	//Leave early if nothing is setup yet
	if( mtInitialized == false )
		return;

	//Oops, no room left in ring buffer.. have to wait for client app to call Capture()
	if( mRingBuffer.GetWriteAvailable() == 0 )
		return;

	WiiMoteEvent newEvent;
	newEvent.clear();

	//Update read
	mWiiMote.HeartBeat();

	//Get & check current button states
	const cWiiMote::tButtonStatus &bState = mWiiMote.GetLastButtonStatus();
	_doButtonCheck(bState.m1, 0, newEvent.pushedButtons, newEvent.releasedButtons);	//1
	_doButtonCheck(bState.m2, 1, newEvent.pushedButtons, newEvent.releasedButtons);	//2
	_doButtonCheck(bState.mA, 2, newEvent.pushedButtons, newEvent.releasedButtons);	//A
	_doButtonCheck(bState.mB, 3, newEvent.pushedButtons, newEvent.releasedButtons);	//B
	_doButtonCheck(bState.mPlus, 4, newEvent.pushedButtons, newEvent.releasedButtons);//+
	_doButtonCheck(bState.mMinus, 5, newEvent.pushedButtons, newEvent.releasedButtons);//-
	_doButtonCheck(bState.mHome, 6, newEvent.pushedButtons, newEvent.releasedButtons);//Home

	//Check POV
	newEvent.povChanged = _doPOVCheck(bState, newEvent.povDirection);

	//Do motion check on main orientation - accounting for sensitivity factor
	mWiiMote.GetCalibratedAcceleration(newEvent.x, newEvent.y, newEvent.z);
	//Normalize new vector (old vector is already normalized)
	float len = sqrt((newEvent.x*newEvent.x) + (newEvent.y*newEvent.y) + (newEvent.z*newEvent.z));
	newEvent.x /= len;
	newEvent.y /= len;
	newEvent.z /= len;
	
	//Get new angle
	float angle = acos((newEvent.x * mtLastX) + (newEvent.y * mtLastY) + (newEvent.z * mtLastZ));
	if( angle > (mVector3Sensitivity * (M_PI / 180.0)) )
	{	//Store for next check
		mtLastX = newEvent.x;
		mtLastY = newEvent.y;
		mtLastZ = newEvent.z;

		if( _mWiiMoteMotionDelay <= 0 )
			newEvent.movement = true; //Set flag as moved
		else
			--_mWiiMoteMotionDelay;
	}

	//Act on NunChuck Data
	if( mWiiMote.IsNunChuckAttached() )
	{
		const cWiiMote::tChuckReport &bState = mWiiMote.GetLastChuckReport();
		_doButtonCheck(bState.mButtonC, 7, newEvent.pushedButtons, newEvent.releasedButtons); //C
		_doButtonCheck(bState.mButtonZ, 8, newEvent.pushedButtons, newEvent.releasedButtons); //Z

		mWiiMote.GetCalibratedChuckAcceleration(newEvent.nunChuckx, newEvent.nunChucky, newEvent.nunChuckz);
		//Normalize new vector (old vector is already normalized)
		float len = sqrt((newEvent.nunChuckx*newEvent.nunChuckx) + 
			             (newEvent.nunChucky*newEvent.nunChucky) +
						 (newEvent.nunChuckz*newEvent.nunChuckz));

		newEvent.nunChuckx /= len;
		newEvent.nunChucky /= len;
		newEvent.nunChuckz /= len;

		float angle = acos((newEvent.nunChuckx * mtLastNunChuckX) + 
			               (newEvent.nunChucky * mtLastNunChuckY) +
						   (newEvent.nunChuckz * mtLastNunChuckZ));

		if( angle > (mVector3Sensitivity * (M_PI / 180.0)) )
		{	//Store for next check
			mtLastNunChuckX = newEvent.nunChuckx;
			mtLastNunChuckY = newEvent.nunChucky;
			mtLastNunChuckZ = newEvent.nunChuckz;

			if( _mWiiMoteMotionDelay <= 0 )
				newEvent.movementChuck = true;
		}

		//Ok, Now check both NunChuck Joystick axes for movement
		float tempX = 0.0f, tempY = 0.0f;
		mWiiMote.GetCalibratedChuckStick(tempX, tempY);
		
		//Convert to int and clip
		newEvent.nunChuckXAxis = (int)(tempX * JoyStick::MAX_AXIS);
		if( newEvent.nunChuckXAxis > JoyStick::MAX_AXIS )
			newEvent.nunChuckXAxis = JoyStick::MAX_AXIS;
		else if( newEvent.nunChuckXAxis < JoyStick::MIN_AXIS )
			newEvent.nunChuckXAxis = JoyStick::MIN_AXIS;

		newEvent.nunChuckYAxis = (int)(tempY * JoyStick::MAX_AXIS);
		if( newEvent.nunChuckYAxis > JoyStick::MAX_AXIS )
			newEvent.nunChuckYAxis = JoyStick::MAX_AXIS;
		else if( newEvent.nunChuckYAxis < JoyStick::MIN_AXIS )
			newEvent.nunChuckYAxis = JoyStick::MIN_AXIS;

		//Apply a little dead-zone dampner
		int xDiff = newEvent.nunChuckXAxis - mLastNunChuckXAxis;
		if( xDiff > 1500 || xDiff < -1500 )
		{
			mLastNunChuckXAxis = newEvent.nunChuckXAxis;
			newEvent.nunChuckXAxisMoved = true;
		}

		int yDiff = newEvent.nunChuckYAxis - mLastNunChuckYAxis;
		if( yDiff > 1500 || yDiff < -1500 )
		{
			mLastNunChuckYAxis = newEvent.nunChuckYAxis;
			newEvent.nunChuckYAxisMoved = true;
		}
	}

	//Ok, put entry in ringbuffer if something changed
	if(newEvent.pushedButtons || newEvent.releasedButtons || newEvent.povChanged || newEvent.movement ||
	   newEvent.movementChuck || newEvent.nunChuckXAxisMoved || newEvent.nunChuckYAxisMoved)
	{
		mRingBuffer.Write(&newEvent, 1);
	}

	//mWiiMote.PrintStatus();
}

//-----------------------------------------------------------------------------------//
void WiiMote::_doButtonCheck(bool new_state, int ois_button, unsigned int &pushed, unsigned int &released)
{
	const bool old_state = ((mtLastButtonStates & ( 1L << ois_button )) == 0) ? false : true;
	
	//Check to see if new state and old state are the same, and hence, need no change
	if( new_state == old_state )
		return;

	//Ok, so it changed... but how?
	if( new_state )
	{	//Ok, new state is pushed, old state was not pushed.. so send button press
		mtLastButtonStates |= 1 << ois_button; //turn the bit flag on
		pushed |= 1 << ois_button;
	}
	else
	{	//Ok, so new state is not pushed, and old state was pushed.. So, send release
		mtLastButtonStates &= ~(1 << ois_button); //turn the bit flag off
		released |= 1 << ois_button;
	}
}

//-----------------------------------------------------------------------------------//
bool WiiMote::_doPOVCheck(const cWiiMote::tButtonStatus &bState, unsigned int &newPosition)
{
	newPosition = Pov::Centered;

	if( bState.mUp )
		newPosition |= Pov::North;
	else if( bState.mDown )
		newPosition |= Pov::South;

	if( bState.mLeft )
		newPosition |= Pov::West;
	else if( bState.mRight )
		newPosition |= Pov::East;

	//Was there a change?
	if( mtLastPOVState != newPosition )
	{
		mtLastPOVState = newPosition;
		return true;
	}

	return false;
}

//-----------------------------------------------------------------------------------//
void WiiMote::setBuffered(bool buffered)
{
	mBuffered = buffered;
}

//-----------------------------------------------------------------------------------//
void WiiMote::capture()
{
	//Anything to read?
	int entries = mRingBuffer.GetReadAvailable();
	if( entries <= 0 )
		return;

	WiiMoteEvent events[OIS_WII_EVENT_BUFFER];
	if( entries > OIS_WII_EVENT_BUFFER )
		entries = OIS_WII_EVENT_BUFFER;
	
	mRingBuffer.Read(events, entries);

	//Loop through each event
	for( int i = 0; i < entries; ++i )
	{
		//Any movement changes in the main accellerometers?
		if( events[i].movement )
		{
			mState.mVectors[0].x = events[i].x;
			mState.mVectors[0].y = events[i].y;
			mState.mVectors[0].z = events[i].z;
			if( mBuffered && mListener )
				if( !mListener->vector3Moved( JoyStickEvent( this, mState ), 0 ) ) return;
		}

		//Check NunChuck movements
		if( events[i].movementChuck )
		{
			mState.mVectors[1].x = events[i].nunChuckx;
			mState.mVectors[1].y = events[i].nunChucky;
			mState.mVectors[1].z = events[i].nunChuckz;
			if( mBuffered && mListener )
				if( !mListener->vector3Moved( JoyStickEvent( this, mState ), 1 ) ) return;
		}

		if( events[i].nunChuckXAxisMoved )
		{
			mState.mAxes[0].abs = events[i].nunChuckXAxis;

			if( mBuffered && mListener )
				if( !mListener->axisMoved( JoyStickEvent( this, mState ), 0 ) ) return;
		}

		if( events[i].nunChuckYAxisMoved )
		{
			mState.mAxes[1].abs = events[i].nunChuckYAxis;

			if( mBuffered && mListener )
				if( !mListener->axisMoved( JoyStickEvent( this, mState ), 1 ) ) return;
		}

		//Has the hat swtich changed?
		if( events[i].povChanged )
		{
			mState.mPOV[0].direction = events[i].povDirection;
			if( mBuffered && mListener )
				if( !mListener->povMoved( JoyStickEvent( this, mState ), 0 ) ) return;
		}

		//Check for any pushed/released events for each button bit
		int buttons = (int)mState.mButtons.size();
		for( int b = 0; b < buttons; ++b )
		{
			unsigned bit_flag = 1 << b;
			if( (events[i].pushedButtons & bit_flag) != 0 )
			{	//send event
				mState.mButtons[b] = true;
				if( mBuffered && mListener )
					if( !mListener->buttonPressed( JoyStickEvent( this, mState ), b ) ) return;
			}

			if( (events[i].releasedButtons & bit_flag) != 0 )
			{	//send event
				mState.mButtons[b] = false;
				if( mBuffered && mListener )
					if( !mListener->buttonReleased( JoyStickEvent( this, mState ), b ) ) return;
			}
		}
	}
}

//-----------------------------------------------------------------------------------//
Interface* WiiMote::queryInterface(Interface::IType type)
{
	if( type == Interface::ForceFeedback && mtInitialized )
		return mRumble;

	return 0;
}
#endif
