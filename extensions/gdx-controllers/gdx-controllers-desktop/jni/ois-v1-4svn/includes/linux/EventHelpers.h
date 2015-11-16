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
#ifndef _LINUX_OISEVENT_HEADER_
#define _LINUX_OISEVENT_HEADER_

#include "linux/LinuxPrereqs.h"

#define OIS_MAX_DEVICES 32
#define OIS_DEVICE_NAME 128

namespace OIS
{
	class EventUtils
	{
	public:
		static bool isJoyStick( int deviceID, JoyStickInfo &js );
		static bool isMouse( int ) {return false;}
		static bool isKeyboard( int ) {return false;}

		//Double pointer is so that we can set the value of the sent pointer
		static void enumerateForceFeedback( int deviceID, LinuxForceFeedback** ff );
		static void removeForceFeedback( LinuxForceFeedback** ff );

		static std::string getName( int deviceID );
		static std::string getUniqueId( int deviceID );
		static std::string getPhysicalLocation( int deviceID );
	};
}
#endif
