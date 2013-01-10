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
#ifndef OIS_iPhoneHelpers_H
#define OIS_iPhoneHelpers_H

#include "iphone/iPhonePrereqs.h"
#include "OISMultiTouch.h"

// This is needed for keeping an event stack for keyboard and mouse
namespace OIS
{
    // used in the eventStack to store the type
    enum iPhone_EventType { iPhone_KEYUP = 0,
                         iPhone_KEYDOWN = 1,
                         iPhone_KEYREPEAT,
                         iPhone_MOUSEDOWN,
                         iPhone_MOUSEUP,
                         iPhone_MOUSEMOVED,
                         iPhone_MOUSESCROLL};
    typedef enum iPhone_EventType iPhoneEventType;

    // only used by iPhoneMultiTouch
    typedef class iPhoneMultiTouchStackEvent
    {
        friend class iPhoneMultiTouch;
        
    private:
        iPhoneMultiTouchStackEvent( MultiTouchEvent event, iPhoneEventType type) : Event(event), Type(type) {}
        
        iPhoneEventType Type;
        MultiTouchEvent Event;
        
    } iPhoneMultiTouchStackEvent;
}

#endif
