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
#include "OISJoyStick.h"

using namespace OIS;

//----------------------------------------------------------------------------//
JoyStick::JoyStick(const std::string &vendor, bool buffered, int devID, InputManager* creator) :
	Object(vendor, OISJoyStick, buffered, devID, creator),
	mSliders(0),
	mPOVs(0),
	mListener(0),
	mVector3Sensitivity(OIS_JOYSTICK_VECTOR3_DEFAULT)
{
}

//----------------------------------------------------------------------------//
int JoyStick::getNumberOfComponents(ComponentType cType) const
{
	switch( cType )
	{
	case OIS_Button:	return (int)mState.mButtons.size();
	case OIS_Axis:		return (int)mState.mAxes.size();
	case OIS_Slider:	return mSliders;
	case OIS_POV:		return mPOVs;
	case OIS_Vector3:	return (int)mState.mVectors.size();
	default:			return 0;
	}
}

//----------------------------------------------------------------------------//
void JoyStick::setVector3Sensitivity(float degrees)
{
	mVector3Sensitivity = degrees;
}

//----------------------------------------------------------------------------//
float JoyStick::getVector3Sensitivity() const
{
	return mVector3Sensitivity;
}

//----------------------------------------------------------------------------//
void JoyStick::setEventCallback( JoyStickListener *joyListener )
{
	mListener = joyListener;
}

//----------------------------------------------------------------------------//
JoyStickListener* JoyStick::getEventCallback() const
{
	return mListener;
}
