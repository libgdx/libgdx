/*
 The zlib/libpng License
 
 Copyright (c) 2006 Chris Snyder 
 
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

#include "iphone/iPhoneInputManager.h"
#include "iphone/iPhoneHelpers.h"
#include "iphone/iPhoneAccelerometer.h"
#include "iphone/iPhoneMultiTouch.h"
#include "OISException.h"

using namespace std;
using namespace OIS;

@implementation InputDelegate

@synthesize touchObject;
@synthesize accelerometerObject;

- (id)init {
    if((self = [super init])) {
        touchObject = nil;
        accelerometerObject = nil;
    }
    return self;
}

- (void)dealloc {
    delete touchObject; touchObject = NULL;
    delete accelerometerObject; accelerometerObject = NULL;

    [super dealloc];
}

- (BOOL)canBecomeFirstResponder
{
    return YES;
}

#pragma mark Accelerator Event Handling
- (void)accelerometer:(UIAccelerometer *)accelerometer didAccelerate:(UIAcceleration *)acceleration {
    accelerometerObject->didAccelerate(acceleration);
}

#pragma mark Touch Event Handling
- (void)touchesEnded:(NSSet *)touches withEvent:(UIEvent *)event {
    for(UITouch *touch in touches) {
        touchObject->_touchEnded(touch);
    }
}

- (void)touchesMoved:(NSSet *)touches withEvent:(UIEvent *)event {
    for(UITouch *touch in touches) {
        touchObject->_touchMoved(touch);
    }
}

- (void)touchesCancelled:(NSSet *)touches withEvent:(UIEvent *)event {
    for(UITouch *touch in touches) {
        touchObject->_touchCancelled(touch);
    }
}

- (void)touchesBegan:(NSSet *)touches withEvent:(UIEvent *)event {
    for(UITouch *touch in touches) {
        touchObject->_touchBegan(touch);
    }
}

@end

//--------------------------------------------------------------------------------//
iPhoneInputManager::iPhoneInputManager() : InputManager("iPhone Input Manager")
{
    mHideMouse = true;
	bAccelerometerUsed = bMultiTouchUsed = false;

	// Setup our internal factories
	mFactories.push_back(this);
}

//--------------------------------------------------------------------------------//
iPhoneInputManager::~iPhoneInputManager()
{
    [mDelegate release]; mDelegate = nil;
    [mWindow release]; mWindow = nil;
}

//--------------------------------------------------------------------------------//
void iPhoneInputManager::_initialize( ParamList &paramList )
{
	_parseConfigSettings( paramList );
    
    mDelegate = [[InputDelegate alloc] initWithFrame:[[UIScreen mainScreen] bounds]];
    
    // Set flags that we want to accept multiple finger touches and be the only one to receive touch events
    [mDelegate setMultipleTouchEnabled:YES];
    [mDelegate setExclusiveTouch:YES];
    [mDelegate becomeFirstResponder];

    [mWindow addSubview:mDelegate];
}

//--------------------------------------------------------------------------------//
void iPhoneInputManager::_parseConfigSettings( ParamList &paramList )
{
    // Some carbon apps are running in a window, however full screen apps
	// do not have a window, so we need to account for that too.
	ParamList::iterator i = paramList.find("WINDOW");
	if(i != paramList.end())
	{
		mWindow = (UIWindow *)strtoul(i->second.c_str(), 0, 10);
		if(mWindow == 0)
			mWindow = nil;
    }
	else
	{
		// else get the main active window.. user might not have access to it through some
		// graphics libraries, if that fails then try at the application level.
        mWindow = [[UIApplication sharedApplication] keyWindow];
	}
	
	if(mWindow == nil)
		OIS_EXCEPT( E_General, "iPhoneInputManager::_parseConfigSettings >> Unable to find a window or event target" );
}

//--------------------------------------------------------------------------------//
DeviceList iPhoneInputManager::freeDeviceList()
{
	DeviceList ret;

	if( bAccelerometerUsed == false )
		ret.insert(std::make_pair(OISJoyStick, mInputSystemName));

	if( bMultiTouchUsed == false )
		ret.insert(std::make_pair(OISMultiTouch, mInputSystemName));

	return ret;
}

//--------------------------------------------------------------------------------//
int iPhoneInputManager::totalDevices(Type iType)
{
	switch(iType)
	{
        case OISJoyStick: return 1;
        case OISMultiTouch: return 1;
        default: return 0;
	}
}

//--------------------------------------------------------------------------------//
int iPhoneInputManager::freeDevices(Type iType)
{
	switch(iType)
	{
        case OISJoyStick: return bAccelerometerUsed ? 0 : 1;
        case OISMultiTouch: return bMultiTouchUsed ? 0 : 1;
        default: return 0;
	}
}

//--------------------------------------------------------------------------------//
bool iPhoneInputManager::vendorExist(Type iType, const std::string & vendor)
{
	if( ( iType == OISMultiTouch || iType == OISJoyStick ) && vendor == mInputSystemName )
		return true;

	return false;
}

//--------------------------------------------------------------------------------//
Object* iPhoneInputManager::createObject(InputManager* creator, Type iType, bool bufferMode, 
									  const std::string & vendor)
{
	Object *obj = 0;

	switch(iType)
	{
        case OISJoyStick:
        {
            if( bAccelerometerUsed == false )
                obj = new iPhoneAccelerometer(this, bufferMode);
            break;
        }
        case OISMultiTouch:
        {
            if( bMultiTouchUsed == false )
                obj = new iPhoneMultiTouch(this, bufferMode);
            break;
        }
        default:
            break;
	}

	if( obj == 0 )
		OIS_EXCEPT(E_InputDeviceNonExistant, "No devices match requested type.");

	return obj;
}

//--------------------------------------------------------------------------------//
void iPhoneInputManager::destroyObject(Object* obj)
{
	delete obj;
}
