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
#include "win32/Win32InputManager.h"
#include "win32/Win32KeyBoard.h"
#include "OISException.h"
#include "OISEvents.h"
#include <sstream>

using namespace OIS;

//--------------------------------------------------------------------------------------------------//
Win32Keyboard::Win32Keyboard( InputManager* creator, IDirectInput8* pDI, bool buffered, DWORD coopSettings )
	: Keyboard(creator->inputSystemName(), buffered, 0, creator)
{
	mKeyboard = 0;
	mDirectInput = pDI;
	coopSetting = coopSettings;

	//Clear our keyboard state buffer
	memset( &KeyBuffer, 0, 256 );
	deadKey = '\0';
	static_cast<Win32InputManager*>(mCreator)->_setKeyboardUsed(true);
}

//--------------------------------------------------------------------------------------------------//
void Win32Keyboard::_initialize()
{
	mModifiers = 0;
	deadKey = '\0';

	if(FAILED(mDirectInput->CreateDevice(GUID_SysKeyboard, &mKeyboard, NULL)))
		OIS_EXCEPT( E_General, "Win32Keyboard::Win32Keyboard >> Could not init device!");

	if(FAILED(mKeyboard->SetDataFormat(&c_dfDIKeyboard)))
		OIS_EXCEPT( E_General, "Win32Keyboard::Win32Keyboard >> format error!");

	HWND hwin = ((Win32InputManager*)mCreator)->getWindowHandle();

	if(FAILED(mKeyboard->SetCooperativeLevel( hwin, coopSetting)))
		OIS_EXCEPT( E_General, "Win32Keyboard::Win32Keyboard >> coop error!");

	if( mBuffered )
	{
		DIPROPDWORD dipdw;
		dipdw.diph.dwSize       = sizeof(DIPROPDWORD);
		dipdw.diph.dwHeaderSize = sizeof(DIPROPHEADER);
		dipdw.diph.dwObj        = 0;
		dipdw.diph.dwHow        = DIPH_DEVICE;
		dipdw.dwData            = KEYBOARD_DX_BUFFERSIZE;

		if (FAILED(mKeyboard->SetProperty( DIPROP_BUFFERSIZE, &dipdw.diph )))
			OIS_EXCEPT( E_General, "Win32Keyboard::Win32Keyboard >> buffer error!");
	}

	HRESULT hr = mKeyboard->Acquire();
	if(FAILED(hr) && hr != DIERR_OTHERAPPHASPRIO)
		OIS_EXCEPT( E_General, "Win32Keyboard::Win32Keyboard >> aquire error!");
}

//--------------------------------------------------------------------------------------------------//
Win32Keyboard::~Win32Keyboard()
{
	if(mKeyboard)
	{
		mKeyboard->Unacquire();
		mKeyboard->Release();
		mKeyboard = 0;
	}
	static_cast<Win32InputManager*>(mCreator)->_setKeyboardUsed(false);
}

//--------------------------------------------------------------------------------------------------//
void Win32Keyboard::capture()
{
	if( mBuffered )
		_readBuffered();
	else
		_read();
}

//--------------------------------------------------------------------------------------------------//
void Win32Keyboard::_readBuffered()
{
	DIDEVICEOBJECTDATA diBuff[KEYBOARD_DX_BUFFERSIZE];
	DWORD entries = KEYBOARD_DX_BUFFERSIZE;
	HRESULT hr;
	//Only one keyboard allowed per app, so static is ok
	static bool verifyAfterAltTab = false;

	hr = mKeyboard->GetDeviceData( sizeof(DIDEVICEOBJECTDATA), diBuff, &entries, 0 );
	if( hr != DI_OK )
	{
		hr = mKeyboard->Acquire();
		if (hr == E_ACCESSDENIED)
			verifyAfterAltTab = true;

		while( hr == DIERR_INPUTLOST )
			hr = mKeyboard->Acquire();

		return;
	}

	if( FAILED(hr) )
		OIS_EXCEPT( E_General, "Win32Keyboard::_readBuffered() >> Problem with Device!" );

	//Update keyboard and modifier states.. And, if mListener, fire events
	for(unsigned int i = 0; i < entries; ++i )
	{
		//If the listener returns false, that means that we are probably deleted...
		//send no more events and just leave as the this pointer is invalid now...
		bool ret = true;
		KeyCode kc = (KeyCode)diBuff[ i ].dwOfs;
			
		//Store result in our keyBuffer too
		KeyBuffer[kc] = static_cast<unsigned char>(diBuff[ i ].dwData);
			
		if( diBuff[ i ].dwData & 0x80 )
		{
			//Turn on modifier
			if( kc == KC_LCONTROL || kc == KC_RCONTROL )
				mModifiers |= Ctrl;
			else if( kc == KC_LSHIFT || kc == KC_RSHIFT )
				mModifiers |= Shift;
			else if( kc == KC_LMENU || kc == KC_RMENU )
				mModifiers |= Alt;

			if( mListener )
				ret = mListener->keyPressed( KeyEvent( this, kc, _translateText(kc) ) );
		}
		else
		{
			//Turn off modifier
			if( kc == KC_LCONTROL || kc == KC_RCONTROL )
				mModifiers &= ~Ctrl;
			else if( kc == KC_LSHIFT || kc == KC_RSHIFT )
				mModifiers &= ~Shift;
			else if( kc == KC_LMENU || kc == KC_RMENU )
				mModifiers &= ~Alt;

			//Fire off event
			if( mListener )
				ret = mListener->keyReleased( KeyEvent( this, kc, 0 ) );
		}

		if(ret == false)
			break;
	}

	// If a lost device/access denied was detected, recover gracefully with new events
	if(verifyAfterAltTab)
	{
		bool ret = true;
		
		//Copy old buffer to temp location to compare against
		unsigned char keyBufferCopy[256];
		memcpy(keyBufferCopy, KeyBuffer, 256);

		//Update new state
		_read();

		for (unsigned i = 0; i < 256; i++)
		{
			if (keyBufferCopy[i] != KeyBuffer[i])
			{
				if (mListener)
				{
					if (KeyBuffer[i])
						ret = mListener->keyPressed( KeyEvent( this, (KeyCode)i, _translateText((KeyCode)i) ) );
					else
						ret = mListener->keyReleased( KeyEvent( this, (KeyCode)i, 0 ) );
				}
			}

			//If user returned false from callback, return immediately
			if(ret == false)
				return;
		}

		verifyAfterAltTab = false;
	}
}

//--------------------------------------------------------------------------------------------------//
void Win32Keyboard::_read()
{
    HRESULT  hr = mKeyboard->GetDeviceState( sizeof(KeyBuffer), &KeyBuffer );

	if( hr == DIERR_INPUTLOST || hr == DIERR_NOTACQUIRED )
	{
		hr = mKeyboard->Acquire();
		if (hr != DIERR_OTHERAPPHASPRIO)
			mKeyboard->GetDeviceState(sizeof(KeyBuffer), &KeyBuffer);
	}

	//Set Shift, Ctrl, Alt
	mModifiers = 0;
	if( isKeyDown(KC_LCONTROL) || isKeyDown(KC_RCONTROL) )
		mModifiers |= Ctrl;
	if( isKeyDown(KC_LSHIFT) || isKeyDown(KC_RSHIFT) )
		mModifiers |= Shift;
	if( isKeyDown(KC_LMENU) || isKeyDown(KC_RMENU) )
		mModifiers |= Alt;
}

//--------------------------------------------------------------------------------------------------//
int Win32Keyboard::_translateText( KeyCode kc )
{
	if( mTextMode == Off )
		return 0;

	BYTE keyState[256];
	HKL  layout = GetKeyboardLayout(0);
	if( GetKeyboardState(keyState) == 0 )
		return 0;

	unsigned int vk = MapVirtualKeyEx(kc, 3, layout);
	if( vk == 0 )
		return 0;

	WCHAR buff[3] = {0};
	int ascii = ToUnicodeEx(vk, kc, keyState, buff, 3, 0, layout);

	if(ascii == 1 && deadKey != '\0' )
	{
		// A dead key is stored and we have just converted a character key
		// Combine the two into a single character
		WCHAR wcBuff[3] = {buff[0], deadKey, '\0'};
		WCHAR out[3];
		
		deadKey = '\0';
		if(FoldStringW(MAP_PRECOMPOSED, (LPWSTR)wcBuff, 3, (LPWSTR)out, 3))
			return out[0];
	}
	else if (ascii == 1)
	{	// We have a single character
		deadKey = '\0';
		return buff[0];
	}
	else if(ascii == 2)
	{	// Convert a non-combining diacritical mark into a combining diacritical mark
		// Combining versions range from 0x300 to 0x36F; only 5 (for French) have been mapped below
		// http://www.fileformat.info/info/unicode/block/combining_diacritical_marks/images.htm
		switch(buff[0])	{
		case 0x5E: // Circumflex accent: â
			deadKey = 0x302; break;
		case 0x60: // Grave accent: à
			deadKey = 0x300; break;
		case 0xA8: // Diaeresis: ü
			deadKey = 0x308; break;
		case 0xB4: // Acute accent: é
			deadKey = 0x301; break;
		case 0xB8: // Cedilla: ç
			deadKey = 0x327; break;
		default:
			deadKey = buff[0]; break;
		}
	}

	return 0;
}

//--------------------------------------------------------------------------------------------------//
bool Win32Keyboard::isKeyDown( KeyCode key ) const
{
	return (KeyBuffer[key] & 0x80) != 0;
}

//--------------------------------------------------------------------------------------------------//
const std::string& Win32Keyboard::getAsString(KeyCode kc)
{
	char temp[256];

	DIPROPSTRING prop;
	prop.diph.dwSize = sizeof(DIPROPSTRING);
	prop.diph.dwHeaderSize = sizeof(DIPROPHEADER);
	prop.diph.dwObj = static_cast<DWORD>(kc);
	prop.diph.dwHow = DIPH_BYOFFSET;

	if (SUCCEEDED(mKeyboard->GetProperty(DIPROP_KEYNAME, &prop.diph)))
	{
		// convert the WCHAR in "wsz" to multibyte
		if (WideCharToMultiByte(CP_ACP, 0, prop.wsz, -1, temp, sizeof(temp), NULL, NULL))
			return mGetString.assign(temp);
	}

	std::stringstream ss;
	ss << "Key_" << (int)kc;
	return mGetString.assign(ss.str());
}

//--------------------------------------------------------------------------------------------------//
void Win32Keyboard::copyKeyStates( char keys[256] ) const
{
	for(int i = 0; i < 256; ++i)
		keys[i] = KeyBuffer[i] > 0; //Normalise the DX values (0x80)
}

//--------------------------------------------------------------------------------------------------//
void Win32Keyboard::setBuffered(bool buffered)
{
	if( buffered != mBuffered )
	{
		if(mKeyboard)
		{
			mKeyboard->Unacquire();
			mKeyboard->Release();
			mKeyboard = 0;
		}
		
		mBuffered = buffered;
		_initialize();
	}
}
