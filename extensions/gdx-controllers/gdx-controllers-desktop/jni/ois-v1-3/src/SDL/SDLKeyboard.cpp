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
#include "SDL/SDLKeyboard.h"
#include "SDL/SDLInputManager.h"
#include "OISException.h"
#include "OISEvents.h"
#include <sstream>

using namespace OIS;

//-------------------------------------------------------------------//
SDLKeyboard::SDLKeyboard( bool buffered )
{
	mBuffered = buffered;
	mType = OISKeyboard;
	listener = 0;

	//Clear our keyboard state buffer
	memset( &KeyBuffer, 0, 256 );
}

//-------------------------------------------------------------------//
void SDLKeyboard::_initialize()
{
	mModifiers = 0;
	mSDLBuff = 0;

	mKeyMap.insert( KeyMap::value_type(SDLK_ESCAPE,KC_ESCAPE) );
	mKeyMap.insert( KeyMap::value_type(SDLK_1, KC_1) );
	mKeyMap.insert( KeyMap::value_type(SDLK_2, KC_2) );
	mKeyMap.insert( KeyMap::value_type(SDLK_3, KC_3) );
	mKeyMap.insert( KeyMap::value_type(SDLK_4, KC_4) );
	mKeyMap.insert( KeyMap::value_type(SDLK_5, KC_5) );
	mKeyMap.insert( KeyMap::value_type(SDLK_6, KC_6) );
	mKeyMap.insert( KeyMap::value_type(SDLK_7, KC_7) );
	mKeyMap.insert( KeyMap::value_type(SDLK_8, KC_8) );
	mKeyMap.insert( KeyMap::value_type(SDLK_9, KC_9) );
	mKeyMap.insert( KeyMap::value_type(SDLK_0, KC_0) );
	mKeyMap.insert( KeyMap::value_type(SDLK_MINUS, KC_MINUS) );
	mKeyMap.insert( KeyMap::value_type(SDLK_EQUALS, KC_EQUALS) );
	mKeyMap.insert( KeyMap::value_type(SDLK_BACKSPACE, KC_BACK) );
	mKeyMap.insert( KeyMap::value_type(SDLK_TAB, KC_TAB) );
	mKeyMap.insert( KeyMap::value_type(SDLK_q, KC_Q) );
	mKeyMap.insert( KeyMap::value_type(SDLK_w, KC_W) );
	mKeyMap.insert( KeyMap::value_type(SDLK_e, KC_E) );
	mKeyMap.insert( KeyMap::value_type(SDLK_r, KC_R) );
	mKeyMap.insert( KeyMap::value_type(SDLK_t, KC_T) );
	mKeyMap.insert( KeyMap::value_type(SDLK_y, KC_Y) );
	mKeyMap.insert( KeyMap::value_type(SDLK_u, KC_U) );
	mKeyMap.insert( KeyMap::value_type(SDLK_i, KC_I) );
	mKeyMap.insert( KeyMap::value_type(SDLK_o, KC_O) );
	mKeyMap.insert( KeyMap::value_type(SDLK_p, KC_P) );
	mKeyMap.insert( KeyMap::value_type(SDLK_RETURN, KC_RETURN) );
	mKeyMap.insert( KeyMap::value_type(SDLK_LCTRL, KC_LCONTROL));
	mKeyMap.insert( KeyMap::value_type(SDLK_a, KC_A) );
	mKeyMap.insert( KeyMap::value_type(SDLK_s, KC_S) );
	mKeyMap.insert( KeyMap::value_type(SDLK_d, KC_D) );
	mKeyMap.insert( KeyMap::value_type(SDLK_f, KC_F) );
	mKeyMap.insert( KeyMap::value_type(SDLK_g, KC_G) );
	mKeyMap.insert( KeyMap::value_type(SDLK_h, KC_H) );
	mKeyMap.insert( KeyMap::value_type(SDLK_j, KC_J) );
	mKeyMap.insert( KeyMap::value_type(SDLK_k, KC_K) );
	mKeyMap.insert( KeyMap::value_type(SDLK_l, KC_L) );
	mKeyMap.insert( KeyMap::value_type(SDLK_SEMICOLON, KC_SEMICOLON) );
	mKeyMap.insert( KeyMap::value_type(SDLK_COLON, KC_COLON) );
	mKeyMap.insert( KeyMap::value_type(SDLK_QUOTE, KC_APOSTROPHE) );
	mKeyMap.insert( KeyMap::value_type(SDLK_BACKQUOTE, KC_GRAVE)  );
	mKeyMap.insert( KeyMap::value_type(SDLK_LSHIFT, KC_LSHIFT) );
	mKeyMap.insert( KeyMap::value_type(SDLK_BACKSLASH, KC_BACKSLASH) );
	mKeyMap.insert( KeyMap::value_type(SDLK_SLASH, KC_SLASH) );
	mKeyMap.insert( KeyMap::value_type(SDLK_z, KC_Z) );
	mKeyMap.insert( KeyMap::value_type(SDLK_x, KC_X) );
	mKeyMap.insert( KeyMap::value_type(SDLK_c, KC_C) );
	mKeyMap.insert( KeyMap::value_type(SDLK_v, KC_V) );
	mKeyMap.insert( KeyMap::value_type(SDLK_b, KC_B) );
	mKeyMap.insert( KeyMap::value_type(SDLK_n, KC_N) );
	mKeyMap.insert( KeyMap::value_type(SDLK_m, KC_M) );
	mKeyMap.insert( KeyMap::value_type(SDLK_COMMA, KC_COMMA)  );
	mKeyMap.insert( KeyMap::value_type(SDLK_PERIOD, KC_PERIOD));
	mKeyMap.insert( KeyMap::value_type(SDLK_RSHIFT, KC_RSHIFT));
	mKeyMap.insert( KeyMap::value_type(SDLK_KP_MULTIPLY, KC_MULTIPLY) );
	mKeyMap.insert( KeyMap::value_type(SDLK_LALT, KC_LMENU) );
	mKeyMap.insert( KeyMap::value_type(SDLK_SPACE, KC_SPACE));
	mKeyMap.insert( KeyMap::value_type(SDLK_CAPSLOCK, KC_CAPITAL) );
	mKeyMap.insert( KeyMap::value_type(SDLK_F1, KC_F1) );
	mKeyMap.insert( KeyMap::value_type(SDLK_F2, KC_F2) );
	mKeyMap.insert( KeyMap::value_type(SDLK_F3, KC_F3) );
	mKeyMap.insert( KeyMap::value_type(SDLK_F4, KC_F4) );
	mKeyMap.insert( KeyMap::value_type(SDLK_F5, KC_F5) );
	mKeyMap.insert( KeyMap::value_type(SDLK_F6, KC_F6) );
	mKeyMap.insert( KeyMap::value_type(SDLK_F7, KC_F7) );
	mKeyMap.insert( KeyMap::value_type(SDLK_F8, KC_F8) );
	mKeyMap.insert( KeyMap::value_type(SDLK_F9, KC_F9) );
	mKeyMap.insert( KeyMap::value_type(SDLK_F10, KC_F10) );
	mKeyMap.insert( KeyMap::value_type(SDLK_NUMLOCK, KC_NUMLOCK) );
	mKeyMap.insert( KeyMap::value_type(SDLK_SCROLLOCK, KC_SCROLL));
	mKeyMap.insert( KeyMap::value_type(SDLK_KP7, KC_NUMPAD7) );
	mKeyMap.insert( KeyMap::value_type(SDLK_KP8, KC_NUMPAD8) );
	mKeyMap.insert( KeyMap::value_type(SDLK_KP9, KC_NUMPAD9) );
	mKeyMap.insert( KeyMap::value_type(SDLK_KP_MINUS, KC_SUBTRACT) );
	mKeyMap.insert( KeyMap::value_type(SDLK_KP4, KC_NUMPAD4) );
	mKeyMap.insert( KeyMap::value_type(SDLK_KP5, KC_NUMPAD5) );
	mKeyMap.insert( KeyMap::value_type(SDLK_KP6, KC_NUMPAD6) );
	mKeyMap.insert( KeyMap::value_type(SDLK_KP_PLUS, KC_ADD) );
	mKeyMap.insert( KeyMap::value_type(SDLK_KP1, KC_NUMPAD1) );
	mKeyMap.insert( KeyMap::value_type(SDLK_KP2, KC_NUMPAD2) );
	mKeyMap.insert( KeyMap::value_type(SDLK_KP3, KC_NUMPAD3) );
	mKeyMap.insert( KeyMap::value_type(SDLK_KP0, KC_NUMPAD0) );
	mKeyMap.insert( KeyMap::value_type(SDLK_KP_PERIOD, KC_DECIMAL) );
	mKeyMap.insert( KeyMap::value_type(SDLK_F11, KC_F11) );
	mKeyMap.insert( KeyMap::value_type(SDLK_F12, KC_F12) );
	mKeyMap.insert( KeyMap::value_type(SDLK_F13, KC_F13) );
	mKeyMap.insert( KeyMap::value_type(SDLK_F14, KC_F14) );
	mKeyMap.insert( KeyMap::value_type(SDLK_F15, KC_F15) );
	mKeyMap.insert( KeyMap::value_type(SDLK_KP_EQUALS, KC_NUMPADEQUALS) );
	mKeyMap.insert( KeyMap::value_type(SDLK_KP_DIVIDE, KC_DIVIDE) );
	mKeyMap.insert( KeyMap::value_type(SDLK_SYSREQ, KC_SYSRQ) );
	mKeyMap.insert( KeyMap::value_type(SDLK_RALT, KC_RMENU) );
	mKeyMap.insert( KeyMap::value_type(SDLK_HOME, KC_HOME) );
	mKeyMap.insert( KeyMap::value_type(SDLK_UP, KC_UP) );
	mKeyMap.insert( KeyMap::value_type(SDLK_PAGEUP, KC_PGUP) );
	mKeyMap.insert( KeyMap::value_type(SDLK_LEFT, KC_LEFT) );
	mKeyMap.insert( KeyMap::value_type(SDLK_RIGHT, KC_RIGHT) );
	mKeyMap.insert( KeyMap::value_type(SDLK_END, KC_END) );
	mKeyMap.insert( KeyMap::value_type(SDLK_DOWN, KC_DOWN) );
	mKeyMap.insert( KeyMap::value_type(SDLK_PAGEDOWN, KC_PGDOWN) );
	mKeyMap.insert( KeyMap::value_type(SDLK_INSERT, KC_INSERT) );
	mKeyMap.insert( KeyMap::value_type(SDLK_DELETE, KC_DELETE) );
	mKeyMap.insert( KeyMap::value_type(SDLK_LSUPER, KC_LWIN) );
	mKeyMap.insert( KeyMap::value_type(SDLK_RSUPER, KC_RWIN) );

	SDL_EnableUNICODE(1);
}

//-------------------------------------------------------------------//
SDLKeyboard::~SDLKeyboard()
{
}

//-------------------------------------------------------------------//
void SDLKeyboard::capture()
{
	SDL_Event events[OIS_SDL_KEY_BUFF];
	int count = SDL_PeepEvents(events, OIS_SDL_KEY_BUFF, SDL_GETEVENT, 
		SDL_EVENTMASK(SDL_KEYDOWN) | SDL_EVENTMASK(SDL_KEYUP));

	for( int i = 0; i < count; ++i )
	{
		KeyCode kc = mKeyMap[events[i].key.keysym.sym];
		KeyBuffer[kc] = events[i].key.state;

		if( mBuffered && listener )
		{
			if( events[i].key.state == SDL_PRESSED )
			{
				if( listener->keyPressed(KeyEvent(this, 0, kc, events[i].key.keysym.unicode)) == false )
					break;
			}
			else
			{
				if( listener->keyReleased(KeyEvent(this, 0, kc, events[i].key.keysym.unicode)) == false )
					break;
			}
		}
	}

	//Release Grab mode on Alt-Tab combinations (for non-window systems)
	if( KeyBuffer[KC_RMENU] || KeyBuffer[KC_LMENU])
	{
		if( KeyBuffer[KC_TAB] )
			static_cast<SDLInputManager*>(InputManager::getSingletonPtr())->_setGrabMode(false);
	}
}

//-------------------------------------------------------------------//
bool SDLKeyboard::isKeyDown( KeyCode key )
{
	return KeyBuffer[key] == 1 ? true : false;
}

//-------------------------------------------------------------------//
const std::string& SDLKeyboard::getAsString( KeyCode kc )
{
    switch(kc)
    {
    case KC_ESCAPE: mGetString = SDL_GetKeyName(SDLK_ESCAPE); break;
    case KC_1: mGetString = SDL_GetKeyName(SDLK_1); break;
    case KC_2: mGetString = SDL_GetKeyName(SDLK_2); break;
    case KC_3: mGetString = SDL_GetKeyName(SDLK_3); break;
    case KC_4: mGetString = SDL_GetKeyName(SDLK_4); break;
    case KC_5: mGetString = SDL_GetKeyName(SDLK_5); break;
    case KC_6: mGetString = SDL_GetKeyName(SDLK_6); break;
    case KC_7: mGetString = SDL_GetKeyName(SDLK_7); break;
    case KC_8: mGetString = SDL_GetKeyName(SDLK_8); break;
    case KC_9: mGetString = SDL_GetKeyName(SDLK_9); break;
    case KC_0: mGetString = SDL_GetKeyName(SDLK_0); break;
    case KC_MINUS: mGetString = SDL_GetKeyName(SDLK_MINUS); break;
    case KC_EQUALS: mGetString = SDL_GetKeyName(SDLK_EQUALS); break;
    case KC_BACK: mGetString = SDL_GetKeyName(SDLK_BACKSPACE); break;
    case KC_TAB: mGetString = SDL_GetKeyName(SDLK_TAB); break;
    case KC_Q: mGetString = SDL_GetKeyName(SDLK_q); break;
    case KC_W: mGetString = SDL_GetKeyName(SDLK_w); break;
    case KC_E: mGetString = SDL_GetKeyName(SDLK_e); break;
    case KC_R: mGetString = SDL_GetKeyName(SDLK_r); break;
    case KC_T: mGetString = SDL_GetKeyName(SDLK_t); break;
    case KC_Y: mGetString = SDL_GetKeyName(SDLK_y); break;
    case KC_U: mGetString = SDL_GetKeyName(SDLK_u); break;
    case KC_I: mGetString = SDL_GetKeyName(SDLK_i); break;
    case KC_O: mGetString = SDL_GetKeyName(SDLK_o); break;
    case KC_P: mGetString = SDL_GetKeyName(SDLK_p); break;
    case KC_LBRACKET: mGetString = "["; break;
    case KC_RBRACKET: mGetString = "]"; break;
    case KC_RETURN: mGetString = SDL_GetKeyName(SDLK_RETURN); break;
    case KC_LCONTROL: mGetString = SDL_GetKeyName(SDLK_LCTRL); break;
    case KC_A: mGetString = SDL_GetKeyName(SDLK_a); break;
    case KC_S: mGetString = SDL_GetKeyName(SDLK_s); break;
    case KC_D: mGetString = SDL_GetKeyName(SDLK_d); break;
    case KC_F: mGetString = SDL_GetKeyName(SDLK_f); break;
    case KC_G: mGetString = SDL_GetKeyName(SDLK_g); break;
    case KC_H: mGetString = SDL_GetKeyName(SDLK_h); break;
    case KC_J: mGetString = SDL_GetKeyName(SDLK_j); break;
    case KC_K: mGetString = SDL_GetKeyName(SDLK_k); break;
    case KC_L: mGetString = SDL_GetKeyName(SDLK_l); break;
    case KC_SEMICOLON: mGetString = SDL_GetKeyName(SDLK_SEMICOLON); break;
    case KC_APOSTROPHE: mGetString = SDL_GetKeyName(SDLK_QUOTE); break;
    case KC_GRAVE: mGetString = SDL_GetKeyName(SDLK_BACKQUOTE); break;
    case KC_LSHIFT: mGetString = SDL_GetKeyName(SDLK_LSHIFT); break;
    case KC_BACKSLASH: mGetString = SDL_GetKeyName(SDLK_BACKSLASH); break;
    case KC_Z: mGetString = SDL_GetKeyName(SDLK_z); break;
    case KC_X: mGetString = SDL_GetKeyName(SDLK_x); break;
    case KC_C: mGetString = SDL_GetKeyName(SDLK_c); break;
    case KC_V: mGetString = SDL_GetKeyName(SDLK_v); break;
    case KC_B: mGetString = SDL_GetKeyName(SDLK_b); break;
    case KC_N: mGetString = SDL_GetKeyName(SDLK_n); break;
    case KC_M: mGetString = SDL_GetKeyName(SDLK_m); break;
    case KC_COMMA: mGetString = SDL_GetKeyName(SDLK_COMMA); break;
    case KC_PERIOD: mGetString = SDL_GetKeyName(SDLK_PERIOD); break;
    case KC_SLASH: mGetString = SDL_GetKeyName(SDLK_SLASH); break;
    case KC_RSHIFT: mGetString = SDL_GetKeyName(SDLK_RSHIFT); break;
    case KC_MULTIPLY: mGetString = SDL_GetKeyName(SDLK_KP_MULTIPLY); break;
    case KC_LMENU: mGetString = SDL_GetKeyName(SDLK_LALT); break;
    case KC_SPACE: mGetString = SDL_GetKeyName(SDLK_SPACE); break;
    case KC_CAPITAL: mGetString = SDL_GetKeyName(SDLK_CAPSLOCK); break;
    case KC_F1: mGetString = SDL_GetKeyName(SDLK_F1); break;
    case KC_F2: mGetString = SDL_GetKeyName(SDLK_F2); break;
    case KC_F3: mGetString = SDL_GetKeyName(SDLK_F3); break;
    case KC_F4: mGetString = SDL_GetKeyName(SDLK_F4); break;
    case KC_F5: mGetString = SDL_GetKeyName(SDLK_F5); break;
    case KC_F6: mGetString = SDL_GetKeyName(SDLK_F6); break;
    case KC_F7: mGetString = SDL_GetKeyName(SDLK_F7); break;
    case KC_F8: mGetString = SDL_GetKeyName(SDLK_F8); break;
    case KC_F9: mGetString = SDL_GetKeyName(SDLK_F9); break;
    case KC_F10: mGetString = SDL_GetKeyName(SDLK_F10); break;
    case KC_NUMLOCK: mGetString = SDL_GetKeyName(SDLK_NUMLOCK); break;
    case KC_SCROLL: mGetString = SDL_GetKeyName(SDLK_SCROLLOCK); break;
    case KC_NUMPAD7: mGetString = SDL_GetKeyName(SDLK_KP7); break;
    case KC_NUMPAD8: mGetString = SDL_GetKeyName(SDLK_KP8); break;
    case KC_NUMPAD9: mGetString = SDL_GetKeyName(SDLK_KP9); break;
    case KC_SUBTRACT: mGetString = SDL_GetKeyName(SDLK_KP_MINUS); break;
    case KC_NUMPAD4: mGetString = SDL_GetKeyName(SDLK_KP4); break;
    case KC_NUMPAD5: mGetString = SDL_GetKeyName(SDLK_KP5); break;
    case KC_NUMPAD6: mGetString = SDL_GetKeyName(SDLK_KP6); break;
    case KC_ADD: mGetString = SDL_GetKeyName(SDLK_KP_PLUS); break;
    case KC_NUMPAD1: mGetString = SDL_GetKeyName(SDLK_KP1); break;
    case KC_NUMPAD2: mGetString = SDL_GetKeyName(SDLK_KP2); break;
    case KC_NUMPAD3: mGetString = SDL_GetKeyName(SDLK_KP3); break;
    case KC_NUMPAD0: mGetString = SDL_GetKeyName(SDLK_KP0); break;
    case KC_DECIMAL: mGetString = SDL_GetKeyName(SDLK_KP_PERIOD); break;
    case KC_OEM_102: mGetString = "OEM_102"; break;
    case KC_F11: mGetString = SDL_GetKeyName(SDLK_F11); break;
    case KC_F12: mGetString = SDL_GetKeyName(SDLK_F12); break;
    case KC_F13: mGetString = SDL_GetKeyName(SDLK_F13); break;
    case KC_F14: mGetString = SDL_GetKeyName(SDLK_F14); break;
    case KC_F15: mGetString = SDL_GetKeyName(SDLK_F15); break;
    case KC_KANA: mGetString = "Kana"; break;
    case KC_ABNT_C1: mGetString = "ABNT_C1"; break;
    case KC_CONVERT: mGetString = "CONVERT"; break;
    case KC_NOCONVERT: mGetString = "NOCONVERT"; break;
    case KC_YEN: mGetString = "YEN"; break;
    case KC_ABNT_C2: mGetString = "ABNT_C2"; break;
    case KC_NUMPADEQUALS: mGetString = SDL_GetKeyName(SDLK_KP_EQUALS); break;
    case KC_PREVTRACK: mGetString = "KC_PREVTRACK"; break;
    case KC_AT: mGetString = "KC_AT"; break;
    case KC_COLON: mGetString = SDL_GetKeyName(SDLK_COLON); break;
    case KC_UNDERLINE: mGetString = "KC_UNDERLINE"; break;
    case KC_KANJI: mGetString = "KC_KANJI"; break;
    case KC_STOP: mGetString = "KC_STOP"; break;
    case KC_AX: mGetString = "KC_AX"; break;
    case KC_UNLABELED: mGetString = "KC_UNLABELED"; break;
    case KC_NEXTTRACK: mGetString = "KC_NEXTTRACK"; break;
    case KC_NUMPADENTER: mGetString = "KC_NUMPADENTER"; break;
    case KC_RCONTROL: mGetString = "KC_RCONTROL"; break;
    case KC_MUTE: mGetString = "KC_MUTE"; break;
    case KC_CALCULATOR: mGetString = "KC_CALCULATOR"; break;
    case KC_PLAYPAUSE: mGetString = "KC_PLAYPAUSE"; break;
    case KC_MEDIASTOP: mGetString = "KC_MEDIASTOP"; break;
    case KC_VOLUMEDOWN: mGetString = "KC_VOLUMEDOWN"; break;
    case KC_VOLUMEUP: mGetString = "KC_VOLUMEUP"; break;
    case KC_WEBHOME: mGetString = "KC_WEBHOME"; break;
    case KC_NUMPADCOMMA: mGetString = "KC_NUMPADCOMMA"; break;
    case KC_DIVIDE: mGetString = SDL_GetKeyName(SDLK_KP_DIVIDE); break;
    case KC_SYSRQ: mGetString = SDL_GetKeyName(SDLK_SYSREQ); break;
    case KC_RMENU: mGetString = SDL_GetKeyName(SDLK_RALT); break;
    case KC_PAUSE: mGetString = "Pause"; break;
    case KC_HOME: mGetString = SDL_GetKeyName(SDLK_HOME); break;
    case KC_UP: mGetString = SDL_GetKeyName(SDLK_UP); break;
    case KC_PGUP: mGetString = SDL_GetKeyName(SDLK_PAGEUP); break;
    case KC_LEFT: mGetString = SDL_GetKeyName(SDLK_LEFT); break;
    case KC_RIGHT: mGetString = SDL_GetKeyName(SDLK_RIGHT); break;
    case KC_END:  mGetString = SDL_GetKeyName(SDLK_END); break;
    case KC_DOWN: mGetString = SDL_GetKeyName(SDLK_DOWN); break;
    case KC_PGDOWN: mGetString = SDL_GetKeyName(SDLK_PAGEDOWN); break;
    case KC_INSERT: mGetString = SDL_GetKeyName(SDLK_INSERT); break;
    case KC_DELETE: mGetString = SDL_GetKeyName(SDLK_DELETE); break;
    case KC_LWIN: mGetString = SDL_GetKeyName(SDLK_LSUPER); break;
    case KC_RWIN: mGetString = SDL_GetKeyName(SDLK_RSUPER); break;
    case KC_APPS: mGetString = "KC_APPS"; break;
    case KC_POWER: mGetString = "KC_POWER"; break;
    case KC_SLEEP: mGetString = "KC_SLEEP"; break;
    case KC_WAKE: mGetString = "KC_WAKE"; break;
    case KC_WEBSEARCH: mGetString = "KC_WEBSEARCH"; break;
    case KC_WEBFAVORITES: mGetString = "KC_WEBFAVORITES"; break;
    case KC_WEBREFRESH: mGetString = "KC_WEBREFRESH"; break;
    case KC_WEBSTOP: mGetString = "KC_WEBSTOP"; break;
    case KC_WEBFORWARD: mGetString = "KC_WEBFORWARD"; break;
    case KC_WEBBACK: mGetString = "KC_WEBBACK"; break;
    case KC_MYCOMPUTER: mGetString = "KC_MYCOMPUTER"; break;
    case KC_MAIL: mGetString = "KC_MAIL"; break;
    case KC_MEDIASELECT: mGetString = "KC_MEDIASELECT"; break;
    default: mGetString = "Unknown"; break;
    };

	return mGetString;
}

//-------------------------------------------------------------------//
void SDLKeyboard::copyKeyStates( char keys[256] )
{
	for(int i = 0; i < 256; ++i)
		keys[i] = KeyBuffer[i];
}

//-------------------------------------------------------------------//
void SDLKeyboard::setBuffered(bool buffered)
{
	mBuffered = buffered;
}

//-------------------------------------------------------------------//
void SDLKeyboard::setTextTranslation( TextTranslationMode mode )
{
	mTextMode = mode;
	if( mode == Off || mode == Ascii )
		SDL_EnableUNICODE(0);
	else if( mode == Unicode )
		SDL_EnableUNICODE(1);
}
