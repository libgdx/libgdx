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
#include "win32/Win32JoyStick.h"
#include "win32/Win32InputManager.h"
#include "win32/Win32ForceFeedback.h"
#include "OISEvents.h"
#include "OISException.h"

#include <cassert>

// Only if xinput support is enabled
#ifdef OIS_WIN32_XINPUT_SUPPORT
#include <wbemidl.h>
#include <oleauto.h>
//#include <wmsstd.h>
#ifndef SAFE_RELEASE
#define SAFE_RELEASE(x) \
   if(x != NULL)        \
   {                    \
      x->Release();     \
      x = NULL;         \
   }
#endif

#pragma comment(lib, "xinput.lib")
#endif

//DX Only defines macros for the JOYSTICK not JOYSTICK2, so fix it
#undef DIJOFS_BUTTON
#undef DIJOFS_POV

#define DIJOFS_BUTTON(n)  (FIELD_OFFSET(DIJOYSTATE2, rgbButtons) + (n))
#define DIJOFS_POV(n)     (FIELD_OFFSET(DIJOYSTATE2, rgdwPOV)+(n)*sizeof(DWORD))
#define DIJOFS_SLIDER0(n) (FIELD_OFFSET(DIJOYSTATE2, rglSlider)+(n) * sizeof(LONG))
#define DIJOFS_SLIDER1(n) (FIELD_OFFSET(DIJOYSTATE2, rglVSlider)+(n) * sizeof(LONG))
#define DIJOFS_SLIDER2(n) (FIELD_OFFSET(DIJOYSTATE2, rglASlider)+(n) * sizeof(LONG))
#define DIJOFS_SLIDER3(n) (FIELD_OFFSET(DIJOYSTATE2, rglFSlider)+(n) * sizeof(LONG))

#define XINPUT_TRANSLATED_BUTTON_COUNT 12
#define XINPUT_TRANSLATED_AXIS_COUNT 6

using namespace OIS;

//--------------------------------------------------------------------------------------------------//
Win32JoyStick::Win32JoyStick( InputManager* creator, IDirectInput8* pDI, bool buffered, DWORD coopSettings, const JoyStickInfo &info ) :
	JoyStick(info.vendor, buffered, info.devId, creator),
	mDirectInput(pDI),
	coopSetting(coopSettings),
	mJoyStick(0),
	mJoyInfo(info),
	mFfDevice(0)
{
}

//--------------------------------------------------------------------------------------------------//
Win32JoyStick::~Win32JoyStick()
{
	delete mFfDevice;

	if(mJoyStick)
	{
		mJoyStick->Unacquire();
		mJoyStick->Release();
		mJoyStick = 0;
	}

	//Return joystick to pool
	static_cast<Win32InputManager*>(mCreator)->_returnJoyStick(mJoyInfo);
}

//--------------------------------------------------------------------------------------------------//
void Win32JoyStick::_initialize()
{
    if (mJoyInfo.isXInput)
    {
        _enumerate();
    }
    else
    {
	    //Clear old state
	    mState.mAxes.clear();

	    delete mFfDevice;
	    mFfDevice = 0;

	    DIPROPDWORD dipdw;

	    dipdw.diph.dwSize       = sizeof(DIPROPDWORD);
	    dipdw.diph.dwHeaderSize = sizeof(DIPROPHEADER);
	    dipdw.diph.dwObj        = 0;
	    dipdw.diph.dwHow        = DIPH_DEVICE;
	    dipdw.dwData            = JOYSTICK_DX_BUFFERSIZE;

	    if(FAILED(mDirectInput->CreateDevice(mJoyInfo.deviceID, &mJoyStick, NULL)))
		    OIS_EXCEPT( E_General, "Win32JoyStick::_initialize() >> Could not initialize joy device!");

	    if(FAILED(mJoyStick->SetDataFormat(&c_dfDIJoystick2)))
		    OIS_EXCEPT( E_General, "Win32JoyStick::_initialize() >> data format error!");

	    HWND hwin = ((Win32InputManager*)mCreator)->getWindowHandle();

	    if(FAILED(mJoyStick->SetCooperativeLevel( hwin, coopSetting)))
		    OIS_EXCEPT( E_General, "Win32JoyStick::_initialize() >> failed to set cooperation level!");

	    if( FAILED(mJoyStick->SetProperty(DIPROP_BUFFERSIZE, &dipdw.diph)) )
		    OIS_EXCEPT( E_General, "Win32Mouse::Win32Mouse >> Failed to set buffer size property" );

	    //Enumerate all axes/buttons/sliders/etc before aquiring
	    _enumerate();

	    mState.clear();

	    capture();
    }
}

//--------------------------------------------------------------------------------------------------//
void Win32JoyStick::_enumerate()
{
    if (mJoyInfo.isXInput)
    {
        mPOVs = 1;

        mState.mButtons.resize(XINPUT_TRANSLATED_BUTTON_COUNT);
	    mState.mAxes.resize(XINPUT_TRANSLATED_AXIS_COUNT);
    }
    else
    {
		// Get joystick capabilities.
		mDIJoyCaps.dwSize = sizeof(DIDEVCAPS);
		if( FAILED(mJoyStick->GetCapabilities(&mDIJoyCaps)) )
			OIS_EXCEPT( E_General, "Win32JoyStick::_enumerate >> Failed to get capabilities" );

	    mPOVs = (short)mDIJoyCaps.dwPOVs;

	    mState.mButtons.resize(mDIJoyCaps.dwButtons);
	    mState.mAxes.resize(mDIJoyCaps.dwAxes);

	    //Reset the axis mapping enumeration value
	    _AxisNumber = 0;

	    //Enumerate Force Feedback (if any)
	    mJoyStick->EnumEffects(DIEnumEffectsCallback, this, DIEFT_ALL);

	    //Enumerate and set axis constraints (and check FF Axes)
	    mJoyStick->EnumObjects(DIEnumDeviceObjectsCallback, this, DIDFT_AXIS);
    }
}

//--------------------------------------------------------------------------------------------------//
BOOL CALLBACK Win32JoyStick::DIEnumDeviceObjectsCallback(LPCDIDEVICEOBJECTINSTANCE lpddoi, LPVOID pvRef)
{
	Win32JoyStick* _this = (Win32JoyStick*)pvRef;

	//Setup mappings
	DIPROPPOINTER diptr;
	diptr.diph.dwSize       = sizeof(DIPROPPOINTER);
	diptr.diph.dwHeaderSize = sizeof(DIPROPHEADER);
	diptr.diph.dwHow        = DIPH_BYID;
	diptr.diph.dwObj        = lpddoi->dwType;
	//Add a magic number to recognise we set seomthing
	diptr.uData             = 0x13130000 | _this->_AxisNumber;

	//Check if axis is slider, if so, do not treat as regular axis
	if(GUID_Slider == lpddoi->guidType)
	{
		++_this->mSliders;

		//Decrease Axes, since this slider shows up in a different place
		_this->mState.mAxes.pop_back();
	}
	else if (FAILED(_this->mJoyStick->SetProperty(DIPROP_APPDATA, &diptr.diph)))
	{	//If for some reason we could not set needed user data, just ignore this axis
		return DIENUM_CONTINUE;
	}

	//Increase for next time through
	if(GUID_Slider != lpddoi->guidType)
		_this->_AxisNumber += 1;

	//Set range
	DIPROPRANGE diprg;
	diprg.diph.dwSize       = sizeof(DIPROPRANGE);
	diprg.diph.dwHeaderSize = sizeof(DIPROPHEADER);
	diprg.diph.dwHow        = DIPH_BYID;
	diprg.diph.dwObj        = lpddoi->dwType;
	diprg.lMin              = MIN_AXIS;
	diprg.lMax              = MAX_AXIS;

	if (FAILED(_this->mJoyStick->SetProperty(DIPROP_RANGE, &diprg.diph)))
		OIS_EXCEPT( E_General, "Win32JoyStick::_DIEnumDeviceObjectsCallback >> Failed to set min/max range property" );

	//Check if FF Axes, and if so, increment counter
	if((lpddoi->dwFlags & DIDOI_FFACTUATOR) != 0 )
	{
		if( _this->mFfDevice )
		{
			_this->mFfDevice->_addFFAxis();
		}
	}

	//Force the flags for gain and auto-center support to true,
	//as DInput has no API to query the device for these capabilities
	//(the only way to know is to try them ...)
	if( _this->mFfDevice )
	{
	    _this->mFfDevice->_setGainSupport(true);
	    _this->mFfDevice->_setAutoCenterSupport(true);
	}

	return DIENUM_CONTINUE;
}

//--------------------------------------------------------------------------------------------------//
BOOL CALLBACK Win32JoyStick::DIEnumEffectsCallback(LPCDIEFFECTINFO pdei, LPVOID pvRef)
{
	Win32JoyStick* _this = (Win32JoyStick*)pvRef;

	//Create the FF instance only after we know there is at least one effect type
	if( _this->mFfDevice == 0 )
		_this->mFfDevice = new Win32ForceFeedback(_this->mJoyStick, &_this->mDIJoyCaps);

	_this->mFfDevice->_addEffectSupport(pdei);

	return DIENUM_CONTINUE;
}

//--------------------------------------------------------------------------------------------------//
void Win32JoyStick::capture()
{
#ifdef OIS_WIN32_XINPUT_SUPPORT
	//handle xbox controller differently
    if (mJoyInfo.isXInput)
	{
		captureXInput();
		return;
	}
#endif

	//handle directinput based devices
	DIDEVICEOBJECTDATA diBuff[JOYSTICK_DX_BUFFERSIZE];
	DWORD entries = JOYSTICK_DX_BUFFERSIZE;

	// Poll the device to read the current state
	HRESULT hr = mJoyStick->Poll();
	if( hr == DI_OK )
		hr = mJoyStick->GetDeviceData( sizeof(DIDEVICEOBJECTDATA), diBuff, &entries, 0 );

	if( hr != DI_OK )
	{
		hr = mJoyStick->Acquire();
		while( hr == DIERR_INPUTLOST )
			hr = mJoyStick->Acquire();

		// Poll the device to read the current state
		mJoyStick->Poll();
		hr = mJoyStick->GetDeviceData( sizeof(DIDEVICEOBJECTDATA), diBuff, &entries, 0 );
		//Perhaps the user just tabbed away
		if( FAILED(hr) )
			return;
	}

	bool axisMoved[24] = {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,
						  false,false,false,false,false,false,false,false};
	bool sliderMoved[4] = {false,false,false,false};

	//Loop through all the events
	for(unsigned int i = 0; i < entries; ++i)
	{
		//This may seem outof order, but is in order of the way these variables
		//are declared in the JoyStick State 2 structure.
		switch(diBuff[i].dwOfs)
		{
		//------ slider -//
		case DIJOFS_SLIDER0(0):
			sliderMoved[0] = true;
			mState.mSliders[0].abX = diBuff[i].dwData;
			break;
		case DIJOFS_SLIDER0(1):
			sliderMoved[0] = true;
			mState.mSliders[0].abY = diBuff[i].dwData;
			break;
		//----- Max 4 POVs Next ---------------//
		case DIJOFS_POV(0):
			if(!_changePOV(0,diBuff[i]))
				return;
			break;
		case DIJOFS_POV(1):
			if(!_changePOV(1,diBuff[i]))
				return;
			break;
		case DIJOFS_POV(2):
			if(!_changePOV(2,diBuff[i]))
				return;
			break;
		case DIJOFS_POV(3):
			if(!_changePOV(3,diBuff[i]))
				return;
			break;
		case DIJOFS_SLIDER1(0):
			sliderMoved[1] = true;
			mState.mSliders[1].abX = diBuff[i].dwData;
			break;
		case DIJOFS_SLIDER1(1):
			sliderMoved[1] = true;
			mState.mSliders[1].abY = diBuff[i].dwData;
			break;
		case DIJOFS_SLIDER2(0):
			sliderMoved[2] = true;
			mState.mSliders[2].abX = diBuff[i].dwData;
			break;
		case DIJOFS_SLIDER2(1):
			sliderMoved[2] = true;
			mState.mSliders[2].abY = diBuff[i].dwData;
			break;
		case DIJOFS_SLIDER3(0):
			sliderMoved[3] = true;
			mState.mSliders[3].abX = diBuff[i].dwData;
			break;
		case DIJOFS_SLIDER3(1):
			sliderMoved[3] = true;
			mState.mSliders[3].abY = diBuff[i].dwData;
			break;
		//-----------------------------------------//
		default:
			//Handle Button Events Easily using the DX Offset Macros
			if( diBuff[i].dwOfs >= DIJOFS_BUTTON(0) && diBuff[i].dwOfs < DIJOFS_BUTTON(128) )
			{
				if(!_doButtonClick((diBuff[i].dwOfs - DIJOFS_BUTTON(0)), diBuff[i]))
					return;
			}
			else if((short)(diBuff[i].uAppData >> 16) == 0x1313)
			{	//If it was nothing else, might be axis enumerated earlier (determined by magic number)
				int axis = (int)(0x0000FFFF & diBuff[i].uAppData); //Mask out the high bit
				assert( axis >= 0 && axis < (int)mState.mAxes.size() && "Axis out of range!");

				if(axis >= 0 && axis < (int)mState.mAxes.size())
				{
					mState.mAxes[axis].abs = diBuff[i].dwData;
					axisMoved[axis] = true;
				}
			}

			break;
		} //end case
	} //end for

	//Check to see if any of the axes values have changed.. if so send events
	if( mBuffered && mListener && entries > 0 )
	{
		JoyStickEvent temp(this, mState);

		//Update axes
		for( int i = 0; i < 24; ++i )
			if( axisMoved[i] )
				if( mListener->axisMoved( temp, i ) == false )
					return;

		//Now update sliders
		for( int i = 0; i < 4; ++i )
			if( sliderMoved[i] )
				if( mListener->sliderMoved( temp, i ) == false )
					return;
	}
}

//--------------------------------------------------------------------------------------------------//
void Win32JoyStick::captureXInput()
{
#ifdef OIS_WIN32_XINPUT_SUPPORT
    XINPUT_STATE inputState;
	if (XInputGetState((DWORD)mJoyInfo.xInputDev, &inputState) != ERROR_SUCCESS)
        memset(&inputState, 0, sizeof(inputState));

    //Sticks and triggers
	int value;
    bool axisMoved[XINPUT_TRANSLATED_AXIS_COUNT] = {false,false,false,false,false,false};

	//LeftY
	value = -(int)inputState.Gamepad.sThumbLY;
	mState.mAxes[0].rel = value - mState.mAxes[0].abs;
	mState.mAxes[0].abs = value;
	if(mState.mAxes[0].rel != 0)
        axisMoved[0] = true;

	//LeftX
    mState.mAxes[1].rel = inputState.Gamepad.sThumbLX - mState.mAxes[1].abs;
    mState.mAxes[1].abs = inputState.Gamepad.sThumbLX;

	if(mState.mAxes[1].rel != 0)
        axisMoved[1] = true;

	//RightY
	value = -(int)inputState.Gamepad.sThumbRY;           
    mState.mAxes[2].rel = value - mState.mAxes[2].abs;
    mState.mAxes[2].abs = value;
	if(mState.mAxes[2].rel != 0)
        axisMoved[2] = true;

	//RightX
    mState.mAxes[3].rel = inputState.Gamepad.sThumbRX - mState.mAxes[3].abs;
    mState.mAxes[3].abs = inputState.Gamepad.sThumbRX;
	if(mState.mAxes[3].rel != 0)
		axisMoved[3] = true;

	//Left trigger
    value = inputState.Gamepad.bLeftTrigger * 129;
	if(value > JoyStick::MAX_AXIS)
		value = JoyStick::MAX_AXIS;

    mState.mAxes[4].rel = value - mState.mAxes[4].abs;
    mState.mAxes[4].abs = value;
	if(mState.mAxes[4].rel != 0)
		axisMoved[4] = true;

	//Right trigger
    value = (int)inputState.Gamepad.bRightTrigger * 129;
	if(value > JoyStick::MAX_AXIS)
		value = JoyStick::MAX_AXIS;

	mState.mAxes[5].rel = value - mState.mAxes[5].abs;
    mState.mAxes[5].abs = value;
	if(mState.mAxes[5].rel != 0)
		axisMoved[5] = true;
    
    //POV
    int previousPov = mState.mPOV[0].direction;        
    int& pov = mState.mPOV[0].direction;
    pov = Pov::Centered;        
    if (inputState.Gamepad.wButtons & XINPUT_GAMEPAD_DPAD_UP)
        pov |= Pov::North;
    else if (inputState.Gamepad.wButtons & XINPUT_GAMEPAD_DPAD_DOWN)
        pov |= Pov::South;
    if (inputState.Gamepad.wButtons & XINPUT_GAMEPAD_DPAD_LEFT)
        pov |= Pov::West;
    else if (inputState.Gamepad.wButtons & XINPUT_GAMEPAD_DPAD_RIGHT)
        pov |= Pov::East;
    
    //Buttons - The first 4 buttons don't need to be checked since they represent the dpad
    bool previousButtons[XINPUT_TRANSLATED_BUTTON_COUNT];
    std::copy(mState.mButtons.begin(), mState.mButtons.end(), previousButtons);
    for (size_t i = 0; i < XINPUT_TRANSLATED_BUTTON_COUNT; i++)
        mState.mButtons[i] = (inputState.Gamepad.wButtons & (1 << (i + 4))) != 0;

    //Send events
    if (mBuffered && mListener)
    {
	    JoyStickEvent joystickEvent(this, mState);

	    //Axes
	    for (int i = 0; i < XINPUT_TRANSLATED_AXIS_COUNT; i++)
        {
		    if (axisMoved[i] && !mListener->axisMoved(joystickEvent, i))
			    return;
        }

        //POV
        if (previousPov != pov && !mListener->povMoved(joystickEvent, 0))
            return;

        //Buttons
        for (int i = 0; i < XINPUT_TRANSLATED_BUTTON_COUNT; i++)
        {
            if (!previousButtons[i] && mState.mButtons[i])
            {
                if (!mListener->buttonPressed(joystickEvent, i))
                    return;
            }
            else if (previousButtons[i] && !mState.mButtons[i])
            {
                if (!mListener->buttonReleased(joystickEvent, i))
                    return;
            }
        }
    }
#endif
}

//--------------------------------------------------------------------------------------------------//
bool Win32JoyStick::_doButtonClick( int button, DIDEVICEOBJECTDATA& di )
{
	if( di.dwData & 0x80 )
	{
		mState.mButtons[button] = true;
		if( mBuffered && mListener )
			return mListener->buttonPressed( JoyStickEvent( this, mState ), button );
	}
	else
	{
		mState.mButtons[button] = false;
		if( mBuffered && mListener )
			return mListener->buttonReleased( JoyStickEvent( this, mState ), button );
	}

	return true;
}

//--------------------------------------------------------------------------------------------------//
bool Win32JoyStick::_changePOV( int pov, DIDEVICEOBJECTDATA& di )
{
	//Some drivers report a value of 65,535, instead of —1,
	//for the center position
	if(LOWORD(di.dwData) == 0xFFFF)
	{
		mState.mPOV[pov].direction = Pov::Centered;
	}
	else
	{
		switch(di.dwData)
		{
			case 0: mState.mPOV[pov].direction = Pov::North; break;
			case 4500: mState.mPOV[pov].direction = Pov::NorthEast; break;
			case 9000: mState.mPOV[pov].direction = Pov::East; break;
			case 13500: mState.mPOV[pov].direction = Pov::SouthEast; break;
			case 18000: mState.mPOV[pov].direction = Pov::South; break;
			case 22500: mState.mPOV[pov].direction = Pov::SouthWest; break;
			case 27000: mState.mPOV[pov].direction = Pov::West; break;
			case 31500: mState.mPOV[pov].direction = Pov::NorthWest; break;
		}
	}

	if( mBuffered && mListener )
		return mListener->povMoved( JoyStickEvent( this, mState ), pov );

	return true;
}

//--------------------------------------------------------------------------------------------------//
void Win32JoyStick::setBuffered(bool buffered)
{
	mBuffered = buffered;
}

//--------------------------------------------------------------------------------------------------//
Interface* Win32JoyStick::queryInterface(Interface::IType type)
{
	if( mFfDevice && type == Interface::ForceFeedback )
		return mFfDevice;
	else
		return 0;
}

//--------------------------------------------------------------------------------------------------//
#ifdef OIS_WIN32_XINPUT_SUPPORT
void Win32JoyStick::CheckXInputDevices(JoyStickInfoList &joys)
{
    IWbemLocator*           pIWbemLocator  = NULL;
    IEnumWbemClassObject*   pEnumDevices   = NULL;
    IWbemClassObject*       pDevices[20]   = {0};
    IWbemServices*          pIWbemServices = NULL;
    BSTR                    bstrNamespace  = NULL;
    BSTR                    bstrDeviceID   = NULL;
    BSTR                    bstrClassName  = NULL;
    DWORD                   uReturned      = 0;
    bool                    bIsXinputDevice= false;
	DWORD                   iDevice        = 0;
	int                     xDevice        = 0;
    VARIANT                 var;
    HRESULT                 hr;

	if(joys.size() == 0)
		return;

    // CoInit if needed
    hr = CoInitialize(NULL);
    bool bCleanupCOM = SUCCEEDED(hr);

    // Create WMI
    hr = CoCreateInstance(__uuidof(WbemLocator), NULL, CLSCTX_INPROC_SERVER, __uuidof(IWbemLocator), (LPVOID*)&pIWbemLocator);
    if( FAILED(hr) || pIWbemLocator == NULL )
        goto LCleanup;

    bstrNamespace = SysAllocString( L"\\\\.\\root\\cimv2" );
	if( bstrNamespace == NULL )
		goto LCleanup;

    bstrClassName = SysAllocString( L"Win32_PNPEntity" );
	if( bstrClassName == NULL )
		goto LCleanup;

    bstrDeviceID  = SysAllocString( L"DeviceID" );
	if( bstrDeviceID == NULL )
		goto LCleanup;
    
    // Connect to WMI 
    hr = pIWbemLocator->ConnectServer( bstrNamespace, NULL, NULL, 0L, 0L, NULL, NULL, &pIWbemServices );
    if( FAILED(hr) || pIWbemServices == NULL )
        goto LCleanup;

    // Switch security level to IMPERSONATE. 
    CoSetProxyBlanket(pIWbemServices, RPC_C_AUTHN_WINNT, RPC_C_AUTHZ_NONE, NULL, RPC_C_AUTHN_LEVEL_CALL, RPC_C_IMP_LEVEL_IMPERSONATE, NULL, EOAC_NONE );                    

    hr = pIWbemServices->CreateInstanceEnum( bstrClassName, 0, NULL, &pEnumDevices ); 
    if( FAILED(hr) || pEnumDevices == NULL )
        goto LCleanup;

    // Loop over all devices
    for( ;; )
    {
        // Get 20 at a time
        hr = pEnumDevices->Next(5000, 20, pDevices, &uReturned);
        if( FAILED(hr) )
            goto LCleanup;

        if( uReturned == 0 )
            break;

        for(iDevice = 0; iDevice < uReturned; iDevice++)
        {
            // For each device, get its device ID
            hr = pDevices[iDevice]->Get(bstrDeviceID, 0L, &var, NULL, NULL);
            if(SUCCEEDED(hr) && var.vt == VT_BSTR && var.bstrVal != NULL)
            {
                // Check if the device ID contains "IG_".  If it does, then it's an XInput device - This information can not be found from DirectInput 
                if(wcsstr(var.bstrVal, L"IG_"))
                {
                    // If it does, then get the VID/PID from var.bstrVal
                    DWORD dwPid = 0, dwVid = 0;
                    WCHAR* strVid = wcsstr( var.bstrVal, L"VID_" );
                    if(strVid && swscanf_s( strVid, L"VID_%4X", &dwVid ) != 1)
						dwVid = 0;

                    WCHAR* strPid = wcsstr( var.bstrVal, L"PID_" );
                    if(strPid && swscanf_s( strPid, L"PID_%4X", &dwPid ) != 1)
                        dwPid = 0;

                    // Compare the VID/PID to the DInput device
                    DWORD dwVidPid = MAKELONG(dwVid, dwPid);
					for(JoyStickInfoList::iterator i = joys.begin(); i != joys.end(); ++i)
					{
						if(!i->isXInput && dwVidPid == i->productGuid.Data1)
						{
							i->isXInput = true;
							i->xInputDev = xDevice;
							++xDevice;
						}
					}

					if(joys.size() == 0)
						goto LCleanup;
                }
            }

            SAFE_RELEASE(pDevices[iDevice]);
        }
    }

LCleanup:
    if(bstrNamespace)
        SysFreeString(bstrNamespace);

    if(bstrDeviceID)
        SysFreeString(bstrDeviceID);

    if(bstrClassName)
        SysFreeString(bstrClassName);

    for(iDevice=0; iDevice < 20; iDevice++)
        SAFE_RELEASE(pDevices[iDevice]);

    SAFE_RELEASE(pEnumDevices);
    SAFE_RELEASE(pIWbemLocator);
    SAFE_RELEASE(pIWbemServices);

    if(bCleanupCOM)
        CoUninitialize();
}
#endif
