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
#ifndef OIS_Prereqs_H
#define OIS_Prereqs_H
//----------------------------------------------------------------------------//
// This Header File contains: forward declared classes
//  * Forward Declarations of all public API classes
//  * Several typedef's used around the library
//  * Base class component types
//  * Preprocessor definitons
//----------------------------------------------------------------------------//

//-------------- Common STL Containers ---------------------------------------//
#include <vector>
#include <string>
#include <map>
#include "OISConfig.h"

// Default is blank for most OS's
#define _OISExport

//-------------- Determine Compiler ---------------------------------
#if defined( _MSC_VER )
#	define OIS_MSVC_COMPILER
#elif defined( __GNUC__ )
#	if defined( __WIN32__ ) || defined( _WIN32 )
#		define OIS_MINGW_COMPILER
#	else
#		define OIS_GCC_COMPILER
#	endif
#elif defined( __BORLANDC__ )
#	define OIS_BORLAND_COMPILER
#else
#	error No Recognized Compiler!
#endif

// --------------- Determine Operating System Platform ---------------
#if defined( __WIN32__ ) || defined( _WIN32 ) // Windows 2000, XP, ETC
#	if defined ( _XBOX )
#		define OIS_XBOX_PLATFORM
#	else
#		define OIS_WIN32_PLATFORM
#		if defined( OIS_DYNAMIC_LIB )
#			undef _OISExport
			//Ignorable Dll interface warning...
#           if !defined(OIS_MINGW_COMPILER)
#			    pragma warning (disable : 4251)
#           endif
#			if defined( OIS_NONCLIENT_BUILD )
#				define _OISExport __declspec( dllexport )
#			else
#               if defined(OIS_MINGW_COMPILER)
#                   define _OISExport
#               else
#				    define _OISExport __declspec( dllimport )
#               endif
#			endif
#		endif
#	endif
#elif defined( __APPLE_CC__ ) // Apple OS X
    // Device                                       Simulator
#   if __IPHONE_OS_VERSION_MIN_REQUIRED >= 20201 || __IPHONE_OS_VERSION_MIN_REQUIRED >= 20000
//#   if __IPHONE_OS_VERSION_MIN_REQUIRED >= 30000 || __IPHONE_OS_VERSION_MIN_REQUIRED >= 30000
#       define OIS_IPHONE_PLATFORM
#   else
#       define OIS_APPLE_PLATFORM
#   endif
#   undef _OISExport
#   define _OISExport __attribute__((visibility("default")))
#else //Probably Linux
#	define OIS_LINUX_PLATFORM
#	include <unistd.h>
#endif

//Is Processor 32 or 64 bits...
#if defined(__x86_64__)
#	define OIS_ARCH_64
#else
#	define OIS_ARCH_32
#endif

//-------------- Common Classes, Enums, and Typdef's -------------------------//
#define OIS_VERSION_MAJOR 1
#define OIS_VERSION_MINOR 4
#define OIS_VERSION_PATCH 0
#define OIS_VERSION_NAME "1.4.0"

#define OIS_VERSION ((OIS_VERSION_MAJOR << 16) | (OIS_VERSION_MINOR << 8) | OIS_VERSION_PATCH)

namespace OIS
{
	//Forward Declarations
	class InputManager;
	class FactoryCreator;
	class Object;
	class Keyboard;
	class Mouse;
	class JoyStick;
	class MultiTouch;
	class KeyListener;
	class MouseListener;
	class MultiTouchListener;
	class JoyStickListener;
	class Interface;
	class ForceFeedback;
	class Effect;
	class Exception;

	//! Way to send OS nuetral parameters.. ie OS Window handles, modes, flags
	typedef std::multimap<std::string, std::string> ParamList;

	//! List of FactoryCreator's
	typedef std::vector<FactoryCreator*> FactoryList;

	//! Map of FactoryCreator created Objects
	typedef std::map<Object*, FactoryCreator*> FactoryCreatedObject;

	//! Each Input class has a General Type variable, a form of RTTI
	enum Type
	{
		OISUnknown       = 0,
		OISKeyboard      = 1,
		OISMouse         = 2,
		OISJoyStick      = 3,
		OISTablet        = 4,
		OISMultiTouch    = 5
	};

	//! Map of device objects connected and their respective vendors
	typedef std::multimap<Type, std::string> DeviceList;

	//--------     Shared common components    ------------------------//

	//! Base type for all device components (button, axis, etc)
	enum ComponentType
	{
		OIS_Unknown = 0,
		OIS_Button  = 1, //ie. Key, mouse button, joy button, etc
		OIS_Axis    = 2, //ie. A joystick or mouse axis
		OIS_Slider  = 3, //
		OIS_POV     = 4, //ie. Arrow direction keys
		OIS_Vector3 = 5  //ie. WiiMote orientation
	};

	//! Base of all device components (button, axis, etc)
	class _OISExport Component
	{
	public:
		Component() : cType(OIS_Unknown) {};
		Component(ComponentType type) : cType(type) {};
		//! Indicates what type of coponent this is
		ComponentType cType;
	};

	//! Button can be a keyboard key, mouse button, etc
	class _OISExport Button : public Component
	{
	public:
		Button() : Component(OIS_Button), pushed(false) {}
		Button(bool bPushed) : Component(OIS_Button), pushed(bPushed) {}
		//! true if pushed, false otherwise
		bool pushed;
	};

	//! Axis component
	class _OISExport Axis : public Component
	{
	public:
		Axis() : Component(OIS_Axis), abs(0), rel(0), absOnly(false) {};

		//! Absoulte and Relative value components
		int abs, rel;

		//! Indicates if this Axis only supports Absoulte (ie JoyStick)
		bool absOnly;

		//! Used internally by OIS
		void clear()
		{
			abs = rel = 0;
		}
	};

	//! A 3D Vector component (perhaps an orientation, as in the WiiMote)
	class _OISExport Vector3 : public Component
	{
	public:
		Vector3() {}
		Vector3(float _x, float _y, float _z) : Component(OIS_Vector3), x(_x), y(_y), z(_z) {};
		
		//! X component of vector
		float x;
		
		//! Y component of vector
		float y;

		//! Z component of vector
		float z;

		void clear()
		{
			x = y = z = 0.0f;
		}
	};
}

#endif //end if prereq header defined
