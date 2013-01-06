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

#ifndef __LP64__

#include "mac/MacKeyboard.h"
#include "mac/MacInputManager.h"
#include "mac/MacHelpers.h"
#include "OISException.h"
#include "OISEvents.h"

#include <Carbon/Carbon.h>

#include <list>
#include <string>

const EventTypeSpec DownSpec[] = {{kEventClassKeyboard, kEventRawKeyDown},	//non - repeats
							{kEventClassKeyboard, kEventRawKeyRepeat}}; //repeats
const EventTypeSpec UpSpec = {kEventClassKeyboard, kEventRawKeyUp},
			  ModSpec = {kEventClassKeyboard, kEventRawKeyModifiersChanged};

const EventTypeSpec AllSpecs[] = {{kEventClassKeyboard, kEventRawKeyDown},
						  {kEventClassKeyboard, kEventRawKeyRepeat},
						  {kEventClassKeyboard, kEventRawKeyUp},
						  {kEventClassKeyboard, kEventRawKeyModifiersChanged}};

using namespace OIS;

//-------------------------------------------------------------------//
MacKeyboard::MacKeyboard( InputManager* creator, bool buffered, bool repeat )
	: Keyboard(creator->inputSystemName(), buffered, 0, creator)
{
	keyDownEventRef = NULL;
	keyUpEventRef = NULL;
	keyModEventRef = NULL;
	
	useRepeat = repeat;

	// Get a so-called "Univeral procedure pointer" for our callback
	keyDownUPP = NewEventHandlerUPP( KeyDownWrapper );
	keyUpUPP   = NewEventHandlerUPP( KeyUpWrapper );
	keyModUPP  = NewEventHandlerUPP( KeyModWrapper );
	
	// populate the conversion map
	populateKeyConversion();

	static_cast<MacInputManager*>(mCreator)->_setKeyboardUsed(true);
}

//-------------------------------------------------------------------//
MacKeyboard::~MacKeyboard()
{
	// Remove our handlers so that this instance doesn't get called
	// after it is deleted
	if (keyDownEventRef != NULL)
		RemoveEventHandler(keyDownEventRef);
		
	if (keyUpEventRef != NULL)
		RemoveEventHandler(keyUpEventRef);
	
	if (keyModEventRef != NULL)
		RemoveEventHandler(keyModEventRef);
	
	// dispose of our UPPs
	DisposeEventHandlerUPP(keyDownUPP);
	DisposeEventHandlerUPP(keyUpUPP);
	DisposeEventHandlerUPP(keyModUPP);

	//Free the input managers keyboard
	static_cast<MacInputManager*>(mCreator)->_setKeyboardUsed(false);
}

//-------------------------------------------------------------------//
void MacKeyboard::_initialize()
{
	EventTargetRef event = ((MacInputManager*)mCreator)->_getEventTarget();

	memset( &KeyBuffer, 0, 256 );
	mModifiers = 0;
	prevModMask = 0;
	
	// just in case this gets called after the first time.. better safe
	if (keyDownEventRef != NULL)
		RemoveEventHandler(keyDownEventRef);
		
	if (keyUpEventRef != NULL)
		RemoveEventHandler(keyUpEventRef);
		
	if (keyModEventRef != NULL)
		RemoveEventHandler(keyModEventRef);
   
	keyDownEventRef = NULL;
	keyUpEventRef = NULL;
	keyModEventRef = NULL;

	OSStatus status;
	// send both elements of downspec array... second index is for repeat events
	if ( useRepeat )
		status = InstallEventHandler( event, keyDownUPP, 2, DownSpec, this, &keyDownEventRef );
	else
		status = InstallEventHandler( event, keyDownUPP, 1, DownSpec, this, &keyDownEventRef );
		
	if (status != noErr)
		OIS_EXCEPT( E_General, "MacKeyboard::_initialize >> Error loading KeyDown event handler" );

	if (InstallEventHandler( event, keyUpUPP, 1, &UpSpec, this, &keyUpEventRef ) != noErr)
		OIS_EXCEPT( E_General, "MacKeyboard::_initialize >> Error loading KeyUp event handler" );

	if (InstallEventHandler( event, keyModUPP, 1, &ModSpec, this, &keyModEventRef ) != noErr )
		OIS_EXCEPT( E_General, "MacKeyboard::_initialize >> Error loading Keymods event handler" );
}

//-------------------------------------------------------------------//
bool MacKeyboard::isKeyDown( KeyCode key ) const
{
	return (bool)KeyBuffer[key];
}


//-------------------------------------------------------------------//
void MacKeyboard::capture()
{
	// if not buffered just return, we update the unbuffered automatically
	if ( !mBuffered || !mListener )
		return;

	// run through our event stack
	eventStack::iterator cur_it;
	
	for (cur_it = pendingEvents.begin(); cur_it != pendingEvents.end(); cur_it++)
	{
		
		if ( (*cur_it).Type == MAC_KEYDOWN || (*cur_it).Type == MAC_KEYREPEAT)
			mListener->keyPressed( (*cur_it).Event );
		else if ( (*cur_it).Type == MAC_KEYUP )
			mListener->keyReleased( (*cur_it).Event );
	}
	
	pendingEvents.clear();
}


//-------------------------------------------------------------------//
std::string& MacKeyboard::getAsString( KeyCode key )
{
    CGKeyCode deviceKeycode;
    
    // Convert OIS KeyCode back into device keycode
    for(VirtualtoOIS_KeyMap::iterator it = keyConversion.begin(); it != keyConversion.end(); ++it)
    {
        if(it->second == key)
            deviceKeycode = it->first;
    }
    
    UniChar unicodeString[1];
    UniCharCount actualStringLength = 0;
    
    CGEventSourceRef sref = CGEventSourceCreate(kCGEventSourceStateHIDSystemState);
    CGEventRef ref = CGEventCreateKeyboardEvent(sref, deviceKeycode, true);
    CGEventKeyboardGetUnicodeString(ref, sizeof(unicodeString) / sizeof(*unicodeString), &actualStringLength, unicodeString);
//    NSLog([NSString stringWithFormat:@"%C\n", unicodeString[0]]);
    getString = unicodeString[0];

	return getString;
}

//-------------------------------------------------------------------//
void MacKeyboard::setBuffered( bool buffered )
{
	mBuffered = buffered;
}

#include <iostream>
//-------------------------------------------------------------------//
void MacKeyboard::_keyDownCallback( EventRef theEvent )
{
	
	UInt32 virtualKey;
	OSStatus status;
	
	unsigned int time = (unsigned int)GetEventTime(theEvent);
	
	status = GetEventParameter(theEvent,
					'kcod',			// get it in virtual keycode
					typeUInt32, NULL,	// desired return type
					sizeof(UInt32), NULL, 	// bufsize
					&virtualKey );
	
	KeyCode kc = keyConversion[virtualKey];

	// record what kind of text we should pass the KeyEvent
	UniChar text[10];
	char macChar;
	
	// TODO clean this up
	if (mTextMode == Unicode)
	{
		//get string size
		UInt32 stringsize;
		//status = GetEventParameter( theEvent, 'kuni', typeUnicodeText, NULL, 0, &stringsize, NULL);
		//status = GetEventParameter( theEvent, 'kuni', typeUnicodeText, NULL, sizeof(UniChar)*10, NULL, &text );
		status = GetEventParameter( theEvent, 'kuni', typeUnicodeText, NULL, sizeof(UniChar) * 10, &stringsize, &text );
//		std::cout << "String length: " << stringsize << std::endl;
		
		//wstring unitext;
		//for (int i=0;i<10;i++) unitext += (wchar_t)text[i];
		//wcout << "Unicode out: " << unitext << endl;
		
		if(stringsize > 0)
		{
			// for each unicode char, send an event
			stringsize--; // no termination char
			for ( int i = 0; i < stringsize; i++ )
			{
				injectEvent( kc, time, MAC_KEYDOWN, (unsigned int)text[i] );
			}
		}
	} 
	else if (mTextMode == Ascii)
	{
		 
		status = GetEventParameter( theEvent, 'kchr', typeChar, NULL, sizeof(char), NULL, &macChar );
		injectEvent( kc, time, MAC_KEYDOWN, (unsigned int)macChar );
	}
	else
	{
		injectEvent( kc, time, MAC_KEYDOWN );
	}
}

//-------------------------------------------------------------------//
void MacKeyboard::_keyUpCallback( EventRef theEvent )
{
	UInt32 virtualKey;
	
	OSStatus status;
	status = GetEventParameter( theEvent, kEventParamKeyCode, typeUInt32,
								NULL, sizeof(UInt32), NULL, &virtualKey );
	
	KeyCode kc = keyConversion[virtualKey];
	injectEvent( kc, (int)GetEventTime(theEvent), MAC_KEYUP );
	
}

//-------------------------------------------------------------------//
void MacKeyboard::_modChangeCallback( EventRef theEvent )
{
	UInt32 mods;
	
	OSStatus status;
	status = GetEventParameter( theEvent, kEventParamKeyModifiers,
								typeUInt32, NULL, sizeof(UInt32), NULL, &mods );
	
	// find the changed bit
	UInt32 change = prevModMask ^ mods;
	MacEventType newstate = ((change & prevModMask) > 0) ? MAC_KEYUP : MAC_KEYDOWN;
	unsigned int time = (int)GetEventTime( theEvent );
	
	//cout << "preMask: " << hex << prevModMask << endl;
	//cout << "ModMask: " << hex << mods << endl;
	//cout << "Change:  " << hex << (change & prevModMask) << endl << endl;
	
	// TODO test modifiers on a full keyboard to check if different mask for left/right
	switch (change)
	{
		case (shiftKey): // shift
			mModifiers &= (newstate == MAC_KEYDOWN) ? Shift : ~Shift;
			injectEvent( KC_LSHIFT, time, newstate );
			//injectEvent( KC_RSHIFT, time, newstate );
			break;
			
		case (optionKey): // option (alt)
			mModifiers &= (newstate == MAC_KEYDOWN) ? Alt : -Alt;
			//injectEvent( KC_RMENU, time, newstate );
			injectEvent( KC_LMENU, time, newstate );
			break;
			
		case (controlKey): // Ctrl
			mModifiers += (newstate == MAC_KEYDOWN) ? Ctrl : -Ctrl;
			//injectEvent( KC_RCONTROL, time, newstate );
			injectEvent( KC_LCONTROL, time, newstate );
			break;
	
		case (cmdKey): // apple
			//injectEvent( KC_RWIN, time, newstate );
			injectEvent( KC_LWIN, time, newstate );
			break;
	
		case (kEventKeyModifierFnMask): // fn key
			injectEvent( KC_APPS, time, newstate );
			break;
			
		case (kEventKeyModifierNumLockMask): // numlock
			injectEvent( KC_NUMLOCK, time, newstate );
			break;
			
		case (alphaLock): // caps lock
			injectEvent( KC_CAPITAL, time, newstate );
			break;
	}
	
	prevModMask = mods;
}

//-------------------------------------------------------------------//
void MacKeyboard::injectEvent( KeyCode kc, unsigned int time, MacEventType type, unsigned int txt )
{
	// set to 1 if this is either a keydown or repeat
	KeyBuffer[kc] = ( type == MAC_KEYUP ) ? 0 : 1;
	
	if ( mBuffered && mListener )
		pendingEvents.push_back( MacKeyStackEvent( KeyEvent(this, kc, txt), type) );
}


//-------------------------------------------------------------------//
void MacKeyboard::copyKeyStates( char keys[256] ) const
{
	memcpy( keys, KeyBuffer, 256 );
}



//-------------------------------------------------------------------//
void MacKeyboard::populateKeyConversion()
{
	// TODO finish the key mapping
	
	// Virtual Key Map to KeyCode
	keyConversion.insert(VirtualtoOIS_KeyMap::value_type(0x12, KC_1));
	keyConversion.insert(VirtualtoOIS_KeyMap::value_type(0x13, KC_2));
	keyConversion.insert(VirtualtoOIS_KeyMap::value_type(0x14, KC_3));
	keyConversion.insert(VirtualtoOIS_KeyMap::value_type(0x15, KC_4));
	keyConversion.insert(VirtualtoOIS_KeyMap::value_type(0x17, KC_5));
	keyConversion.insert(VirtualtoOIS_KeyMap::value_type(0x16, KC_6));
	keyConversion.insert(VirtualtoOIS_KeyMap::value_type(0x1A, KC_7));
	keyConversion.insert(VirtualtoOIS_KeyMap::value_type(0x1C, KC_8));
	keyConversion.insert(VirtualtoOIS_KeyMap::value_type(0x19, KC_9));
	keyConversion.insert(VirtualtoOIS_KeyMap::value_type(0x1D, KC_0));
	
	keyConversion.insert(VirtualtoOIS_KeyMap::value_type(0x33, KC_BACK));  // might be wrong
	
	keyConversion.insert(VirtualtoOIS_KeyMap::value_type(0x1B, KC_MINUS));
	keyConversion.insert(VirtualtoOIS_KeyMap::value_type(0x18, KC_EQUALS));
	keyConversion.insert(VirtualtoOIS_KeyMap::value_type(0x31, KC_SPACE));
	keyConversion.insert(VirtualtoOIS_KeyMap::value_type(0x2B, KC_COMMA));
	keyConversion.insert(VirtualtoOIS_KeyMap::value_type(0x2F, KC_PERIOD));
	
	keyConversion.insert(VirtualtoOIS_KeyMap::value_type(0x2A, KC_BACKSLASH));
	keyConversion.insert(VirtualtoOIS_KeyMap::value_type(0x2C, KC_SLASH));
	keyConversion.insert(VirtualtoOIS_KeyMap::value_type(0x21, KC_LBRACKET));
	keyConversion.insert(VirtualtoOIS_KeyMap::value_type(0x1E, KC_RBRACKET));
	
	keyConversion.insert(VirtualtoOIS_KeyMap::value_type(0x35, KC_ESCAPE));
	keyConversion.insert(VirtualtoOIS_KeyMap::value_type(0x39, KC_CAPITAL));
	
	keyConversion.insert(VirtualtoOIS_KeyMap::value_type(0x30, KC_TAB));
	keyConversion.insert(VirtualtoOIS_KeyMap::value_type(0x24, KC_RETURN));  // double check return/enter
	
	//keyConversion.insert(VirtualtoOIS_KeyMap::value_type(XK_colon, KC_COLON));	 // no colon?
	keyConversion.insert(VirtualtoOIS_KeyMap::value_type(0x29, KC_SEMICOLON));
	keyConversion.insert(VirtualtoOIS_KeyMap::value_type(0x27, KC_APOSTROPHE));
	keyConversion.insert(VirtualtoOIS_KeyMap::value_type(0x32, KC_GRAVE));
	
	keyConversion.insert(VirtualtoOIS_KeyMap::value_type(0x0B, KC_B));
	keyConversion.insert(VirtualtoOIS_KeyMap::value_type(0x00, KC_A));
	keyConversion.insert(VirtualtoOIS_KeyMap::value_type(0x08, KC_C));
	keyConversion.insert(VirtualtoOIS_KeyMap::value_type(0x02, KC_D));
	keyConversion.insert(VirtualtoOIS_KeyMap::value_type(0x0E, KC_E));
	keyConversion.insert(VirtualtoOIS_KeyMap::value_type(0x03, KC_F));
	keyConversion.insert(VirtualtoOIS_KeyMap::value_type(0x05, KC_G));
	keyConversion.insert(VirtualtoOIS_KeyMap::value_type(0x04, KC_H));
	keyConversion.insert(VirtualtoOIS_KeyMap::value_type(0x22, KC_I));
	keyConversion.insert(VirtualtoOIS_KeyMap::value_type(0x26, KC_J));
	keyConversion.insert(VirtualtoOIS_KeyMap::value_type(0x28, KC_K));
	keyConversion.insert(VirtualtoOIS_KeyMap::value_type(0x25, KC_L));
	keyConversion.insert(VirtualtoOIS_KeyMap::value_type(0x2E, KC_M));
	keyConversion.insert(VirtualtoOIS_KeyMap::value_type(0x2D, KC_N));
	keyConversion.insert(VirtualtoOIS_KeyMap::value_type(0x1F, KC_O));
	keyConversion.insert(VirtualtoOIS_KeyMap::value_type(0x23, KC_P));
	keyConversion.insert(VirtualtoOIS_KeyMap::value_type(0x0C, KC_Q));
	keyConversion.insert(VirtualtoOIS_KeyMap::value_type(0x0F, KC_R));
	keyConversion.insert(VirtualtoOIS_KeyMap::value_type(0x01, KC_S));
	keyConversion.insert(VirtualtoOIS_KeyMap::value_type(0x11, KC_T));
	keyConversion.insert(VirtualtoOIS_KeyMap::value_type(0x20, KC_U));
	keyConversion.insert(VirtualtoOIS_KeyMap::value_type(0x09, KC_V));
	keyConversion.insert(VirtualtoOIS_KeyMap::value_type(0x0D, KC_W));
	keyConversion.insert(VirtualtoOIS_KeyMap::value_type(0x07, KC_X));
	keyConversion.insert(VirtualtoOIS_KeyMap::value_type(0x10, KC_Y));
	keyConversion.insert(VirtualtoOIS_KeyMap::value_type(0x06, KC_Z));
	
	keyConversion.insert(VirtualtoOIS_KeyMap::value_type(0x7A, KC_F1));
	keyConversion.insert(VirtualtoOIS_KeyMap::value_type(0x78, KC_F2));
	keyConversion.insert(VirtualtoOIS_KeyMap::value_type(0x63, KC_F3));
	keyConversion.insert(VirtualtoOIS_KeyMap::value_type(0x76, KC_F4));
	keyConversion.insert(VirtualtoOIS_KeyMap::value_type(0x60, KC_F5));
	keyConversion.insert(VirtualtoOIS_KeyMap::value_type(0x61, KC_F6));
	keyConversion.insert(VirtualtoOIS_KeyMap::value_type(0x62, KC_F7));
	keyConversion.insert(VirtualtoOIS_KeyMap::value_type(0x64, KC_F8));
	keyConversion.insert(VirtualtoOIS_KeyMap::value_type(0x65, KC_F9));
	keyConversion.insert(VirtualtoOIS_KeyMap::value_type(0x6D, KC_F10));
	keyConversion.insert(VirtualtoOIS_KeyMap::value_type(0x67, KC_F11));
	keyConversion.insert(VirtualtoOIS_KeyMap::value_type(0x6F, KC_F12));
	keyConversion.insert(VirtualtoOIS_KeyMap::value_type(0x69, KC_F13));
	keyConversion.insert(VirtualtoOIS_KeyMap::value_type(0x6B, KC_F14));
	keyConversion.insert(VirtualtoOIS_KeyMap::value_type(0x71, KC_F15));
	
	//Keypad
	keyConversion.insert(VirtualtoOIS_KeyMap::value_type(0x52, KC_NUMPAD0));
	keyConversion.insert(VirtualtoOIS_KeyMap::value_type(0x53, KC_NUMPAD1));
	keyConversion.insert(VirtualtoOIS_KeyMap::value_type(0x54, KC_NUMPAD2));
	keyConversion.insert(VirtualtoOIS_KeyMap::value_type(0x55, KC_NUMPAD3));
	keyConversion.insert(VirtualtoOIS_KeyMap::value_type(0x56, KC_NUMPAD4));
	keyConversion.insert(VirtualtoOIS_KeyMap::value_type(0x57, KC_NUMPAD5));
	keyConversion.insert(VirtualtoOIS_KeyMap::value_type(0x58, KC_NUMPAD6));
	keyConversion.insert(VirtualtoOIS_KeyMap::value_type(0x59, KC_NUMPAD7));
	keyConversion.insert(VirtualtoOIS_KeyMap::value_type(0x5B, KC_NUMPAD8));
	keyConversion.insert(VirtualtoOIS_KeyMap::value_type(0x5C, KC_NUMPAD9));
	keyConversion.insert(VirtualtoOIS_KeyMap::value_type(0x45, KC_ADD));
	keyConversion.insert(VirtualtoOIS_KeyMap::value_type(0x4E, KC_SUBTRACT));
	keyConversion.insert(VirtualtoOIS_KeyMap::value_type(0x41, KC_DECIMAL));
	keyConversion.insert(VirtualtoOIS_KeyMap::value_type(0x51, KC_NUMPADEQUALS));
	keyConversion.insert(VirtualtoOIS_KeyMap::value_type(0x4B, KC_DIVIDE));
	keyConversion.insert(VirtualtoOIS_KeyMap::value_type(0x43, KC_MULTIPLY));
	keyConversion.insert(VirtualtoOIS_KeyMap::value_type(0x4C, KC_NUMPADENTER));
	
	//Keypad with numlock off
	//keyConversion.insert(VirtualtoOIS_KeyMap::value_type(0x73, KC_NUMPAD7));  // not sure of these
	//keyConversion.insert(VirtualtoOIS_KeyMap::value_type(XK_KP_Up, KC_NUMPAD8)); // check on a non-laptop
	//keyConversion.insert(VirtualtoOIS_KeyMap::value_type(XK_KP_Page_Up, KC_NUMPAD9));
	//keyConversion.insert(VirtualtoOIS_KeyMap::value_type(XK_KP_Left, KC_NUMPAD4));
	//keyConversion.insert(VirtualtoOIS_KeyMap::value_type(XK_KP_Begin, KC_NUMPAD5));
	//keyConversion.insert(VirtualtoOIS_KeyMap::value_type(XK_KP_Right, KC_NUMPAD6));
	//keyConversion.insert(VirtualtoOIS_KeyMap::value_type(XK_KP_End, KC_NUMPAD1));
	//keyConversion.insert(VirtualtoOIS_KeyMap::value_type(XK_KP_Down, KC_NUMPAD2));
	//keyConversion.insert(VirtualtoOIS_KeyMap::value_type(XK_KP_Page_Down, KC_NUMPAD3));
	//keyConversion.insert(VirtualtoOIS_KeyMap::value_type(XK_KP_Insert, KC_NUMPAD0));
	//keyConversion.insert(VirtualtoOIS_KeyMap::value_type(XK_KP_Delete, KC_DECIMAL));
	
	keyConversion.insert(VirtualtoOIS_KeyMap::value_type(0x7E, KC_UP));
	keyConversion.insert(VirtualtoOIS_KeyMap::value_type(0x7D, KC_DOWN));
	keyConversion.insert(VirtualtoOIS_KeyMap::value_type(0x7B, KC_LEFT));
	keyConversion.insert(VirtualtoOIS_KeyMap::value_type(0x7C, KC_RIGHT));
	
	keyConversion.insert(VirtualtoOIS_KeyMap::value_type(0x74, KC_PGUP));
	keyConversion.insert(VirtualtoOIS_KeyMap::value_type(0x79, KC_PGDOWN));
	keyConversion.insert(VirtualtoOIS_KeyMap::value_type(0x73, KC_HOME));
	keyConversion.insert(VirtualtoOIS_KeyMap::value_type(0x77, KC_END));
	
	//keyConversion.insert(VirtualtoOIS_KeyMap::value_type(XK_Print, KC_SYSRQ));		// ??
	//keyConversion.insert(VirtualtoOIS_KeyMap::value_type(XK_Scroll_Lock, KC_SCROLL)); // ??
	//keyConversion.insert(VirtualtoOIS_KeyMap::value_type(XK_Pause, KC_PAUSE));		// ??
	
	
	//keyConversion.insert(VirtualtoOIS_KeyMap::value_type(XK_Insert, KC_INSERT));	  // ??
	keyConversion.insert(VirtualtoOIS_KeyMap::value_type(0x75, KC_DELETE)); // del under help key?
}

#endif
