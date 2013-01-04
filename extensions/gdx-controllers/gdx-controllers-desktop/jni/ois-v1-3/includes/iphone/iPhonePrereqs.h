/*
 The zlib/libpng License
 
 Copyright (c) 2006 Chris Snyder 
 
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
#ifndef OIS_iPhonePrereqs_H
#define OIS_iPhonePrereqs_H

#include <string>
#include <list>
#include <CoreFoundation/CoreFoundation.h>

namespace OIS
{
    class iPhoneInputManager;
    class iPhoneAccelerometer;
	class iPhoneMouse;

	/** 
		Simple wrapper class for CFString which will create a valid CFString and retain ownership until class instance is outof scope
		To Access the CFStringRef instance, simply cast to void*, pass into a function expecting a void* CFStringRef object, or access via cf_str() method
	*/
	class OIS_CFString
	{
	public:
		OIS_CFString() { m_StringRef = CFStringCreateWithCString(NULL, "", kCFStringEncodingUTF8); }
		OIS_CFString(const char* c_str) { m_StringRef = CFStringCreateWithCString(NULL, c_str, kCFStringEncodingUTF8); }
		OIS_CFString(const std::string &s_str) { m_StringRef = CFStringCreateWithCString(NULL, s_str.c_str(), kCFStringEncodingUTF8); }
		~OIS_CFString() { CFRelease(m_StringRef); }

		//Allow this class to be autoconverted to base class of StringRef (void*)
		operator void*() { return (void*)m_StringRef; }
		CFStringRef cf_str() { return m_StringRef; }
	
	private:
		CFStringRef m_StringRef;
	};
}
#endif
