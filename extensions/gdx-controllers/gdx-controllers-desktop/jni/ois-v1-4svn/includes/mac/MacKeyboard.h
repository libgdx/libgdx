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
#ifndef OIS_MacKeyboard_H
#define OIS_MacKeyboard_H

#include "OISKeyboard.h"
#include "mac/MacHelpers.h"
#include "mac/MacPrereqs.h"

#include <Carbon/Carbon.h>

namespace OIS
{
    
    class MacKeyboard : public Keyboard
    {
    public:
        MacKeyboard( InputManager* creator, bool buffered, bool repeat );
        virtual ~MacKeyboard();
        
        // Sets buffered mode
        virtual void setBuffered( bool buffered );
        
        // unbuffered keydown check
        virtual bool isKeyDown( KeyCode key ) const;
        
        // This will send listener events if buffered is on.
        // Note that in the mac implementation, unbuffered input is
        // automatically updated without calling this.
        virtual void capture();
        
        // Copies the current key buffer
        virtual void copyKeyStates( char keys[256] ) const;
        
        // Returns a description of the given key
        virtual std::string& getAsString( KeyCode key );
        
        virtual Interface* queryInterface( Interface::IType type ) { return 0; }
        
        
        // Public but reserved for internal use:
        virtual void _initialize();
        void _keyDownCallback( EventRef theEvent );
        void _keyUpCallback( EventRef theEvent );
        void _modChangeCallback( EventRef theEvent );
        

    protected:
        // just to get this out of the way
        void populateKeyConversion();
        
        // updates the keybuffer and optionally the eventStack
        void injectEvent(KeyCode kc, unsigned int time, MacEventType type, unsigned int txt = 0 );
                
        typedef std::map<UInt32, KeyCode> VirtualtoOIS_KeyMap;
        VirtualtoOIS_KeyMap keyConversion;
        
        std::string getString;
        
        char KeyBuffer[256];
        UInt32 prevModMask;
        
        
        // "universal procedure pointers" - required reference for callbacks
        EventHandlerUPP keyDownUPP;
        EventHandlerUPP keyUpUPP;
        EventHandlerUPP keyModUPP;
        
        // so we can delete the handlers on destruction
        EventHandlerRef keyDownEventRef;
        EventHandlerRef keyUpEventRef;
        EventHandlerRef keyModEventRef;
        
        // buffered events, fifo stack
        typedef std::list<MacKeyStackEvent> eventStack;
        eventStack pendingEvents;
        
        bool useRepeat;
        
    };
}
#endif
