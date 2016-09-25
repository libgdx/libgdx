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
#ifndef _WIN32_MOUSE_H_EADER_
#define _WIN32_MOUSE_H_EADER_

#include "OISMouse.h"
#include "win32/Win32Prereqs.h"

namespace OIS
{
	class Win32Mouse : public Mouse
	{
	public:
		Win32Mouse( InputManager* creator, IDirectInput8* pDI, bool buffered, DWORD coopSettings );
		virtual ~Win32Mouse();
		
		/** @copydoc Object::setBuffered */
		virtual void setBuffered(bool buffered);

		/** @copydoc Object::capture */
		virtual void capture();

		/** @copydoc Object::queryInterface */
		virtual Interface* queryInterface(Interface::IType type) {return 0;}

		/** @copydoc Object::_initialize */
		virtual void _initialize();

	protected:
		bool _doMouseClick( int mouseButton, DIDEVICEOBJECTDATA& di );

		IDirectInput8* mDirectInput;
		IDirectInputDevice8* mMouse;
		DWORD coopSetting;
		HWND mHwnd;
	};
}

#endif //_WIN32_MOUSE_H_EADER_
