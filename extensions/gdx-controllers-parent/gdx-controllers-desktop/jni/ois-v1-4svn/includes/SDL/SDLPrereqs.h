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
#ifndef OIS_SDLPrereqs_H
#define OIS_SDLPrereqs_H

#include "OISPrereqs.h"

#ifdef OIS_APPLE_PLATFORM
#  include <SDL/SDL.h>
#else
#  include <SDL.h>
#endif

#define OIS_SDL_KEY_BUFF   16
#define OIS_SDL_MOUSE_BUFF 50
#define OIS_SDL_JOY_BUFF   80

#endif
