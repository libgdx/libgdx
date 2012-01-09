/*************************************************************************
 *                                                                       *
 * Tokamak Physics Engine, Copyright (C) 2002-2007 David Lam.            *
 * All rights reserved.  Email: david@tokamakphysics.com                 *
 *                       Web: www.tokamakphysics.com                     *
 *                                                                       *
 * This library is distributed in the hope that it will be useful,       *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the files    *
 * LICENSE.TXT for more details.                                         *
 *                                                                       *
 *************************************************************************/

#ifndef NE_DEBUG_H
#define NE_DEBUG_H

#ifdef WIN32
#include <windows.h>
#include <stdio.h>
#endif

#include <assert.h>

#ifdef VERIFY
#undef VERIFY
#endif

#ifdef VERIFYS
#undef VERIFYS
#endif

#ifdef ASSERT
#undef ASSERT
#endif

#ifdef ASSERTS
#undef ASSERTS
#endif

#ifdef BREAK
#undef BREAK
#endif


#if _DEBUG
    #define ASSERT(E)    assert(E)
#else
    #define ASSERT(E)    
#endif

#if _DEBUG

	#ifdef WIN32
		
		#define TOKAMAK_OUTPUT(str) {OutputDebugString(str);}

		#define TOKAMAK_OUTPUT_1(frmt,d) \
		{	char tmpBuffer[256];\
			sprintf(tmpBuffer, frmt, d);\
			OutputDebugString(tmpBuffer);\
		}
		#define TOKAMAK_OUTPUT_2(frmt,d1,d2) \
		{	char tmpBuffer[256];\
			sprintf(tmpBuffer, frmt, d1, d2);\
			OutputDebugString(tmpBuffer);\
		}
		#define TOKAMAK_OUTPUT_3(frmt,d1,d2,d3) \
		{	char tmpBuffer[256];\
			sprintf(tmpBuffer, frmt, d1, d2,d3);\
			OutputDebugString(tmpBuffer);\
		}
		#define TOKAMAK_OUTPUT_4(frmt,d1,d2,d3,d4) \
		{	char tmpBuffer[256];\
			sprintf(tmpBuffer, frmt, d1,d2,d3,d4);\
			OutputDebugString(tmpBuffer);\
		}

	#else

		#define TOKAMAK_OUTPUT(str)
		#define TOKAMAK_OUTPUT_1(frmt,d1)
		#define TOKAMAK_OUTPUT_2(frmt,d1,d2)
		#define TOKAMAK_OUTPUT_3(frmt,d1,d2,d3)
		#define TOKAMAK_OUTPUT_4(frmt,d1,d2,d3,d4)

	#endif

#else

		#define TOKAMAK_OUTPUT(str)
		#define TOKAMAK_OUTPUT_1(frmt,d1)
		#define TOKAMAK_OUTPUT_2(frmt,d1,d2)
		#define TOKAMAK_OUTPUT_3(frmt,d1,d2,d3)
		#define TOKAMAK_OUTPUT_4(frmt,d1,d2,d3,d4)

#endif

#endif//NE_DEBUG_H
