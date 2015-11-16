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
#ifndef _LINUX_MOUSE_H_EADER_
#define _LINUX_MOUSE_H_EADER_

#include "linux/LinuxPrereqs.h"
#include "OISMouse.h"
#include <X11/Xlib.h>

namespace OIS
{
	class LinuxMouse : public Mouse
	{
	public:
		LinuxMouse(InputManager* creator, bool buffered, bool grab, bool hide);
		virtual ~LinuxMouse();

		/** @copydoc Object::setBuffered */
		virtual void setBuffered(bool buffered);

		/**
		@remarks
			Note: Calling this will also update the keyboard (X11 updates in a single
			event queue). Updates State and/or raises event for buffered mode..
		*/
		virtual void capture();

		/** @copydoc Object::queryInterface */
		virtual Interface* queryInterface(Interface::IType) {return 0;}

		/** @copydoc Object::_initialize */
		virtual void _initialize();

		void grab(bool grab);
		void hide(bool hide);

	protected:
		void _processXEvents();

		bool mMoved, mWarped;

		//Since X11 provides us with absolute values, we need to keep track of relative values
		long oldXMouseX, oldXMouseY, oldXMouseZ;

		Window window;		//The X Window
		Display *display;	//The X display
		Cursor cursor;		//A blank cursor

		bool grabMouse;		//Are we grabbing the mouse to the window?
		bool hideMouse;		//Are we hiding OS mouse?
		bool mouseFocusLost;//Has the mouse just lost focus?
	};
}

#endif //_LINUX_MOUSE_H_EADER_
