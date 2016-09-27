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
#include "iphone/iPhoneMultiTouch.h"
#include "iphone/iPhoneInputManager.h"

using namespace OIS;

//-------------------------------------------------------------------//
iPhoneMultiTouch::iPhoneMultiTouch( InputManager* creator, bool buffered )
	: MultiTouch(creator->inputSystemName(), buffered, 0, creator)
{
	iPhoneInputManager *man = static_cast<iPhoneInputManager*>(mCreator);

    man->_setMultiTouchUsed(true);
    [man->_getDelegate() setTouchObject:this];
}

iPhoneMultiTouch::~iPhoneMultiTouch()
{
	iPhoneInputManager *man = static_cast<iPhoneInputManager*>(mCreator);
    
    man->_setMultiTouchUsed(false);
    [man->_getDelegate() setTouchObject:nil];
}

void iPhoneMultiTouch::_initialize()
{
//    mTempState.clear();
}

void iPhoneMultiTouch::setBuffered( bool buffered )
{
	mBuffered = buffered;
}

void iPhoneMultiTouch::capture()
{
#if 0
    for( std::multiset<MultiTouchState *>::iterator i = mStates.begin(), e = mStates.end(); i != e; ++i )
    {
        // Clear the state first
        dynamic_cast<MultiTouchState *>(*i)->clear();

        if(mTempState.X.rel || mTempState.Y.rel || mTempState.Z.rel)
        {
            MultiTouchState *iState = dynamic_cast<MultiTouchState *>(*i);
    //		NSLog(@"%i %i %i", mTempState.X.rel, mTempState.Y.rel, mTempState.Z.rel);

            // Set new relative motion values
            iState->X.rel = mTempState.X.rel;
            iState->Y.rel = mTempState.Y.rel;
            iState->Z.rel = mTempState.Z.rel;
            
            // Update absolute position
            iState->X.abs += mTempState.X.rel;
            iState->Y.abs += mTempState.Y.rel;
            
            if(iState->X.abs > iState->width)
                iState->X.abs = iState->width;
            else if(iState->X.abs < 0)
                iState->X.abs = 0;

            if(iState->Y.abs > iState->height)
                iState->Y.abs = iState->height;
            else if(iState->Y.abs < 0)
                iState->Y.abs = 0;
                
            iState->Z.abs += mTempState.Z.rel;
            
            //Fire off event
            if(mListener && mBuffered)
                mListener->touchMoved(MultiTouchEvent(this, *iState));
        }
    }

	mTempState.clear();
#endif
}

void iPhoneMultiTouch::_touchBegan(UITouch *touch)
{
    CGPoint location = [touch locationInView:static_cast<iPhoneInputManager*>(mCreator)->_getDelegate()];

    MultiTouchState newState;
    newState.X.abs = location.x;
    newState.Y.abs = location.y;
    newState.touchType |= 1 << MT_Pressed;

    if( mListener && mBuffered )
    {
        mListener->touchPressed(MultiTouchEvent(this, newState));
    }
    else
    {
        mStates.push_back(newState);
    }
}

void iPhoneMultiTouch::_touchEnded(UITouch *touch)
{
    CGPoint location = [touch locationInView:static_cast<iPhoneInputManager*>(mCreator)->_getDelegate()];

    MultiTouchState newState;
    newState.X.abs = location.x;
    newState.Y.abs = location.y;
    newState.touchType |= 1 << MT_Released;

    if( mListener && mBuffered )
    {
        mListener->touchReleased(MultiTouchEvent(this, newState));
    }
    else
    {
        mStates.push_back(newState);
    }
}

void iPhoneMultiTouch::_touchMoved(UITouch *touch)
{
    CGPoint location = [touch locationInView:static_cast<iPhoneInputManager*>(mCreator)->_getDelegate()];
    CGPoint previousLocation = [touch previousLocationInView:static_cast<iPhoneInputManager*>(mCreator)->_getDelegate()];

    MultiTouchState newState;
    newState.X.rel = (location.x - previousLocation.x);
    newState.Y.rel = (location.y - previousLocation.y);
    newState.X.abs = location.x;
    newState.Y.abs = location.y;
    newState.touchType |= 1 << MT_Moved;

    if( mListener && mBuffered )
    {
        mListener->touchMoved(MultiTouchEvent(this, newState));
    }
    else
    {
        mStates.push_back(newState);
    }
}

void iPhoneMultiTouch::_touchCancelled(UITouch *touch)
{
    CGPoint location = [touch locationInView:static_cast<iPhoneInputManager*>(mCreator)->_getDelegate()];

    MultiTouchState newState;
    newState.X.abs = location.x;
    newState.Y.abs = location.y;
    newState.touchType |= 1 << MT_Cancelled;

    if( mListener && mBuffered )
    {
        mListener->touchCancelled(MultiTouchEvent(this, newState));
    }
    else
    {
        mStates.push_back(newState);
    }
}
