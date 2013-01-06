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

#ifndef OIS_CocoaHelpers_H
#define OIS_CocoaHelpers_H

#include "OISEvents.h"
#include "OISKeyboard.h"
#include "OISMouse.h"

// This is needed for keeping an event stack for keyboard and mouse
namespace OIS
{
    
    // used in the eventStack to store the type
    enum Mac_EventType { MAC_KEYUP = 0,
                         MAC_KEYDOWN = 1,
                         MAC_KEYREPEAT,
                         MAC_MOUSEDOWN,
                         MAC_MOUSEUP,
                         MAC_MOUSEMOVED,
                         MAC_MOUSESCROLL};
    typedef enum Mac_EventType MacEventType;
}

#endif
