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
#ifndef _OIS_EXCEPTION_HEADER_
#define _OIS_EXCEPTION_HEADER_
#include "OISPrereqs.h"
#include <exception>

namespace OIS
{
	//! Simple enum's for dealing with exceptions
    enum OIS_ERROR
	{
		E_InputDisconnected,
		E_InputDeviceNonExistant,
		E_InputDeviceNotSupported,
		E_DeviceFull,
		E_NotSupported,
		E_NotImplemented,
		E_Duplicate,
		E_InvalidParam,
		E_General
	};

	/**
	@remarks
		Class for handling OIS exceptions. Much cleaner than checking every method for reurn value.
		Inherits from std::exception so you can simply log those messages if you want to be generic.
		Also note that this has a source file now since OSX was not finding the OIS::Exception symbol
		which would cause program abortion with now correponding exception type.
	*/
	class _OISExport Exception : public std::exception
	{
		//! Hidden default
		Exception() : eType(E_General), eLine(0), eFile(0) {}
	public:
		//! Creates exception object
		Exception( OIS_ERROR err, const char* str, int line, const char *file )
			: eType(err), eLine(line), eFile(file), eText(str) {}

		~Exception() throw() {}

		virtual const char* what() const throw();

		//! The type of exception raised
		const OIS_ERROR eType;
		//! The line number it occurred on
		const int eLine;
		//! The source file
		const char* eFile;
		//! A message passed along when the exception was raised
		const char* eText;
	};
}

//! Use this macro to handle exceptions easily
#define OIS_EXCEPT( err, str ) throw( OIS::Exception(err, str, __LINE__, __FILE__) )

#endif //_OIS_EXCEPTION_HEADER_
