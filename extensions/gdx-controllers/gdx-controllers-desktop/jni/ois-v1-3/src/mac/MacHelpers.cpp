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

#include "mac/MacHelpers.h"
#include "mac/MacKeyboard.h"
#include "mac/MacMouse.h"
#include "OISException.h"

#include <Carbon/Carbon.h>

using namespace OIS;
    
//-------------------------------------------------------------------//
OSStatus KeyDownWrapper( EventHandlerCallRef nextHandler,
                        EventRef               theEvent,
                        void*                  callClass )
{
    // TODO find a better way. This cast isn't very safe
    if (callClass != NULL) {
        ((MacKeyboard*)callClass)->_keyDownCallback( theEvent );
        
        // propagate the event down the chain
        return CallNextEventHandler( nextHandler, theEvent );
    }
    else {
        OIS_EXCEPT(E_General, "KeyDownWrapper >> Being called by something other than our event handler!");
        return noErr;
    }
}


//-------------------------------------------------------------------//
OSStatus KeyUpWrapper( EventHandlerCallRef nextHandler,
                       EventRef               theEvent,
                       void*                  callClass )
{
    if (callClass != NULL) {
        ((MacKeyboard*)callClass)->_keyUpCallback( theEvent );
        
        // propagate the event down the chain
        return CallNextEventHandler( nextHandler, theEvent );
    }
    else {
        OIS_EXCEPT(E_General, "KeyUpWrapper >> Being called by something other than our event handler!");
        return noErr;
    }
}


//-------------------------------------------------------------------//
OSStatus KeyModWrapper( EventHandlerCallRef nextHandler,
                        EventRef               theEvent,
                        void*                  callClass )
{
    if (callClass != NULL) {
        ((MacKeyboard*)callClass)->_modChangeCallback( theEvent );
        
        // propagate the event down the chain
        return CallNextEventHandler( nextHandler, theEvent );
        
    }
    else {
        OIS_EXCEPT(E_General, "KeyModWrapper >> Being called by something other than our event handler!");
        return noErr;
    }
}

/*
//-------------------------------------------------------------------//
OSStatus MouseMoveWrapper( EventHandlerCallRef nextHandler,
                           EventRef            theEvent,
                           void*               callClass )
{
    if (callClass != NULL) {
        ((MacMouse*)callClass)->_mouseMoveCallback( theEvent );
        
        // propagate the event down the chain
        return CallNextEventHandler( nextHandler, theEvent );
        
    }
    else {
        OIS_EXCEPT(E_General, "MouseMoveWrapper >> Being called by something other than our event handler!");
        return noErr;
    }
}


//-------------------------------------------------------------------//
OSStatus MouseScrollWrapper( EventHandlerCallRef nextHandler,
                             EventRef            theEvent,
                             void*               callClass )
{
    if (callClass != NULL) {
        ((MacMouse*)callClass)->_mouseScrollCallback( theEvent );
        
        // propagate the event down the chain
        return CallNextEventHandler( nextHandler, theEvent );
        
    }
    else {
        OIS_EXCEPT(E_General, "MouseScrollWrapper >> Being called by something other than our event handler!");
        return noErr;
    }
}


//-------------------------------------------------------------------//
OSStatus MouseButtonWrapper( EventHandlerCallRef nextHandler,
                             EventRef            theEvent,
                             void*               callClass )
{
    if (callClass != NULL) {
        ((MacMouse*)callClass)->_mouseButtonCallback( theEvent );
        
        // propagate the event down the chain
        return CallNextEventHandler( nextHandler, theEvent );
        
    }
    else {
        OIS_EXCEPT(E_General, "MouseButtonWrapper >> Being called by something other than our event handler!");
        return noErr;
    }
}
*/

//-------------------------------------------------------------------//
OSStatus MouseWrapper( EventHandlerCallRef nextHandler, EventRef theEvent, void* callClass )
{
    if (callClass != NULL)
	{
        ((MacMouse*)callClass)->_mouseCallback( theEvent );

        // propagate the event down the chain
        return CallNextEventHandler( nextHandler, theEvent );        
    }
    else
        OIS_EXCEPT(E_General, "MouseWrapper >> Being called by something other than our event handler!");
}
