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
#ifndef _OIS_SDLMOUSE_H
#define _OIS_SDLMOUSE_H

#include "OISMouse.h"
#include "SDL/SDLPrereqs.h"

namespace OIS
{
	class SDLMouse : public Mouse
	{
	public:
		SDLMouse( bool buffered );
		virtual ~SDLMouse();
		
		/** @copydoc Object::setBuffered */
		virtual void setBuffered(bool buffered);

		/** @copydoc Object::capture */
		virtual void capture();

		/** @copydoc Object::queryInterface */
		virtual Interface* queryInterface(Interface::IType type) {return 0;}

		/** @copydoc Object::_initialize */
		virtual void _initialize();

		void _setGrab(bool grabbed);
		void _setVisible(bool visible);

	protected:
		SDLMouse() {}

		bool mGrabbed;
		bool mRegainFocus;
	};
}
#endif
