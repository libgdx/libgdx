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
#ifndef _LINUX_INPUTSYSTEM_PREREQS_H
#define _LINUX_INPUTSYSTEM_PREREQS_H

//Bring in any auto generated config files
#ifdef HAVE_CONFIG_H
# include "config.h"
#endif

#include "OISPrereqs.h"

//! Max number of elements to collect from buffered input
#define JOY_BUFFERSIZE 64

namespace OIS
{
	class LinuxInputManager;
	class LinuxKeyboard;
	class LinuxJoyStick;
	class LinuxMouse;
	
	class LinuxForceFeedback;

	class Range
	{
	public:
		Range() {};
		Range(int _min, int _max) : min(_min), max(_max) {};
		int min, max;
	};

	class JoyStickInfo
	{
	public:
		JoyStickInfo(): devId(-1),joyFileD(-1),version(0),axes(0),buttons(0),hats(0) {}
		//! Device number (/dev/input/j#) or /dev/input/event#
		int devId;
		//! File descriptor
		int joyFileD;
		//! Driver version
		int version;
		//! Joy vendor
		std::string vendor;
		//! Number of axes
		unsigned char axes;
		//! Number of buttons
		unsigned char buttons;
		//! Number of hats
		unsigned char hats;
		//! Maps Linux button values to OIS buttons values
		std::map<int, int> button_map;
		//! Maps Linux axis values to OIS axis
		std::map<int, int> axis_map;
		//! Maps OIS axis values to it's range
		std::map<int, Range> axis_range;
	};

	typedef std::vector< JoyStickInfo > JoyStickInfoList;
}

#endif //_LINUX_INPUTSYSTEM_PREREQS_H
