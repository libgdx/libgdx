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
#ifndef _WIN32_INPUTSYSTEM_PREREQS_H
#define _WIN32_INPUTSYSTEM_PREREQS_H

#include <cstddef>
#define WIN32_LEAN_AND_MEAN
#define DIRECTINPUT_VERSION 0x0800
#include <windows.h>
#include <dinput.h>

#ifdef OIS_WIN32_XINPUT_SUPPORT
#	include <XInput.h>
#endif

//Max number of elements to collect from buffered DirectInput
#define KEYBOARD_DX_BUFFERSIZE 17
#define MOUSE_DX_BUFFERSIZE 128
#define JOYSTICK_DX_BUFFERSIZE 129

//MinGW defines
#if defined(OIS_MINGW_COMPILER)
#	undef FIELD_OFFSET
#	define FIELD_OFFSET offsetof
#endif

namespace OIS
{
	//Local Forward declarations
	class Win32InputManager;
	class Win32Keyboard;
	class Win32JoyStick;
	class Win32Mouse;
	class Win32ForceFeedback;

	//Information needed to create DirectInput joysticks
	class JoyStickInfo
	{
	public:
		int devId;
		GUID deviceID;
		GUID productGuid;
		std::string vendor;
        bool isXInput;
		int xInputDev;
	};

	typedef std::vector<JoyStickInfo> JoyStickInfoList;
}

#endif //_WIN32_INPUTSYSTEM_PREREQS_H
