// MIT License

// Copyright (c) 2019 Erin Catto

// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:

// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.

// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.

#ifndef B2_SETTINGS_H
#define B2_SETTINGS_H

#include "b2_types.h"
#include "b2_api.h"

/// @file
/// Settings that can be overriden for your application
///

/// Define this macro in your build if you want to override settings
#ifdef B2_USER_SETTINGS

/// This is a user file that includes custom definitions of the macros, structs, and functions
/// defined below.
#include "b2_user_settings.h"

#else

#include <stdarg.h>
#include <stdint.h>

// Tunable Constants

/// You can use this to change the length scale used by your game.
/// For example for inches you could use 39.4.
#define b2_lengthUnitsPerMeter 1.0f

/// The maximum number of vertices on a convex polygon. You cannot increase
/// this too much because b2BlockAllocator has a maximum object size.
#define b2_maxPolygonVertices	8

// User data

///// You can define this to inject whatever data you want in b2Body
//struct B2_API b2BodyUserData
//{
//	b2BodyUserData()
//	{
//		pointer = 0;
//	}
//
//	/// For legacy compatibility
//	uintptr_t pointer;
//};
//
///// You can define this to inject whatever data you want in b2Fixture
//struct B2_API b2FixtureUserData
//{
//	b2FixtureUserData()
//	{
//		pointer = 0;
//	}
//
//	/// For legacy compatibility
//	uintptr_t pointer;
//};
//
///// You can define this to inject whatever data you want in b2Joint
//struct B2_API b2JointUserData
//{
//	b2JointUserData()
//	{
//		pointer = 0;
//	}
//
//	/// For legacy compatibility
//	uintptr_t pointer;
//};

// Memory Allocation

/// Default allocation functions
B2_API void* b2Alloc_Default(int32 size);
B2_API void b2Free_Default(void* mem);

/// Implement this function to use your own memory allocator.
inline void* b2Alloc(int32 size)
{
	return b2Alloc_Default(size);
}

/// If you implement b2Alloc, you should also implement this function.
inline void b2Free(void* mem)
{
	b2Free_Default(mem);
}

/// Default logging function
B2_API void b2Log_Default(const char* string, va_list args);

/// Implement this to use your own logging.
inline void b2Log(const char* string, ...)
{
	va_list args;
	va_start(args, string);
	b2Log_Default(string, args);
	va_end(args);
}

#endif // B2_USER_SETTINGS

#include "b2_common.h"

#endif
