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
#include "iphone/iPhoneAccelerometer.h"
#include "iphone/iPhoneInputManager.h"

using namespace OIS;

//-------------------------------------------------------------------//
iPhoneAccelerometer::iPhoneAccelerometer( InputManager* creator, bool buffered )
	: JoyStick(creator->inputSystemName(), buffered, 0, creator)
{
    iPhoneInputManager *man = static_cast<iPhoneInputManager*>(mCreator);
    
    man->_setAccelerometerUsed(true);
    [man->_getDelegate() setAccelerometerObject:this];
    [[UIAccelerometer sharedAccelerometer] setDelegate:man->_getDelegate()];
    mUpdateInterval = 60.0f;
}

iPhoneAccelerometer::~iPhoneAccelerometer()
{
    iPhoneInputManager *man = static_cast<iPhoneInputManager*>(mCreator);
    
    man->_setAccelerometerUsed(false);
    [man->_getDelegate() setAccelerometerObject:nil];
}

void iPhoneAccelerometer::_initialize()
{
	// Clear old joy state
    mState.mVectors.resize(1);
	mState.clear();
	mTempState.clear();

    // Set the update interval
    [[UIAccelerometer sharedAccelerometer] setUpdateInterval:(1.0 / mUpdateInterval)];
}

void iPhoneAccelerometer::setBuffered( bool buffered )
{
	mBuffered = buffered;
}

void iPhoneAccelerometer::didAccelerate(UIAcceleration *acceleration)
{
    mTempState.clear();
    
    mTempState.x = acceleration.x;
    mTempState.y = acceleration.y;
    mTempState.z = acceleration.z;
}

void iPhoneAccelerometer::capture()
{
    mState.clear();
    mState.mVectors[0] = mTempState;

    if(mListener && mBuffered)
        mListener->axisMoved(JoyStickEvent(this, mState), 0);
}
