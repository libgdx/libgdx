////////////////////////////////////////////////////////////////////////////////
///
/// Generic version of the x86 CPU extension detection routine.
///
/// This file is for GNU & other non-Windows compilers, see 'cpu_detect_x86_win.cpp' 
/// for the Microsoft compiler version.
///
/// Author        : Copyright (c) Olli Parviainen
/// Author e-mail : oparviai 'at' iki.fi
/// SoundTouch WWW: http://www.surina.net/soundtouch
///
////////////////////////////////////////////////////////////////////////////////
//
// Last changed  : $Date: 2012-04-01 21:00:09 +0100 (So, 01 Apr 2012) $
// File revision : $Revision: 4 $
//
// $Id: cpu_detect_x86.cpp 138 2012-04-01 20:00:09Z oparviai $
//
////////////////////////////////////////////////////////////////////////////////
//
// License :
//
//  SoundTouch audio processing library
//  Copyright (c) Olli Parviainen
//
//  This library is free software; you can redistribute it and/or
//  modify it under the terms of the GNU Lesser General Public
//  License as published by the Free Software Foundation; either
//  version 2.1 of the License, or (at your option) any later version.
//
//  This library is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//  Lesser General Public License for more details.
//
//  You should have received a copy of the GNU Lesser General Public
//  License along with this library; if not, write to the Free Software
//  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//
////////////////////////////////////////////////////////////////////////////////

#include "cpu_detect.h"
#include "STTypes.h"

#if defined(SOUNDTOUCH_ALLOW_X86_OPTIMIZATIONS)

    #if defined(__GNUC__) && defined(__i386__)
        // gcc
        #include "cpuid.h"
    #endif

    #if defined(_M_IX86)
        // windows
        #include <intrin.h>
        #define bit_MMX		(1 << 23)
        #define bit_SSE		(1 << 25)
        #define bit_SSE2	(1 << 26)
    #endif

#endif


//////////////////////////////////////////////////////////////////////////////
//
// processor instructions extension detection routines
//
//////////////////////////////////////////////////////////////////////////////

// Flag variable indicating whick ISA extensions are disabled (for debugging)
static uint _dwDisabledISA = 0x00;      // 0xffffffff; //<- use this to disable all extensions

// Disables given set of instruction extensions. See SUPPORT_... defines.
void disableExtensions(uint dwDisableMask)
{
    _dwDisabledISA = dwDisableMask;
}



/// Checks which instruction set extensions are supported by the CPU.
uint detectCPUextensions(void)
{
/// If building for a 64bit system (no Itanium) and the user wants optimizations.
/// Return the OR of SUPPORT_{MMX,SSE,SSE2}. 11001 or 0x19.
/// Keep the _dwDisabledISA test (2 more operations, could be eliminated).
#if ((defined(__GNUC__) && defined(__x86_64__)) \
    || defined(_M_X64))  \
    && defined(SOUNDTOUCH_ALLOW_X86_OPTIMIZATIONS)
    return 0x19 & ~_dwDisabledISA;

/// If building for a 32bit system and the user wants optimizations.
/// Keep the _dwDisabledISA test (2 more operations, could be eliminated).
#elif ((defined(__GNUC__) && defined(__i386__)) \
    || defined(_M_IX86))  \
    && defined(SOUNDTOUCH_ALLOW_X86_OPTIMIZATIONS)

    if (_dwDisabledISA == 0xffffffff) return 0;
 
    uint res = 0;
 
#if defined(__GNUC__)
    // GCC version of cpuid. Requires GCC 4.3.0 or later for __cpuid intrinsic support.
    uint eax, ebx, ecx, edx;  // unsigned int is the standard type. uint is defined by the compiler and not guaranteed to be portable.

    // Check if no cpuid support.
    if (!__get_cpuid (1, &eax, &ebx, &ecx, &edx)) return 0; // always disable extensions.

    if (edx & bit_MMX)  res = res | SUPPORT_MMX;
    if (edx & bit_SSE)  res = res | SUPPORT_SSE;
    if (edx & bit_SSE2) res = res | SUPPORT_SSE2;

#else
    // Window / VS version of cpuid. Notice that Visual Studio 2005 or later required 
    // for __cpuid intrinsic support.
    int reg[4] = {-1};

    // Check if no cpuid support.
    __cpuid(reg,0);
    if ((unsigned int)reg[0] == 0) return 0; // always disable extensions.

    __cpuid(reg,1);
    if ((unsigned int)reg[3] & bit_MMX)  res = res | SUPPORT_MMX;
    if ((unsigned int)reg[3] & bit_SSE)  res = res | SUPPORT_SSE;
    if ((unsigned int)reg[3] & bit_SSE2) res = res | SUPPORT_SSE2;

#endif

    return res & ~_dwDisabledISA;

#else

/// One of these is true:
/// 1) We don't want optimizations.
/// 2) Using an unsupported compiler.
/// 3) Running on a non-x86 platform.
    return 0;

#endif
}
