////////////////////////////////////////////////////////////////////////////////
///
/// A header file for detecting the Intel MMX instructions set extension.
///
/// Please see 'mmx_win.cpp', 'mmx_cpp.cpp' and 'mmx_non_x86.cpp' for the 
/// routine implementations for x86 Windows, x86 gnu version and non-x86 
/// platforms, respectively.
///
/// Author        : Copyright (c) Olli Parviainen
/// Author e-mail : oparviai 'at' iki.fi
/// SoundTouch WWW: http://www.surina.net/soundtouch
///
////////////////////////////////////////////////////////////////////////////////
//
// Last changed  : $Date: 2008-02-10 16:26:55 +0000 (So, 10 Feb 2008) $
// File revision : $Revision: 4 $
//
// $Id: cpu_detect.h 11 2008-02-10 16:26:55Z oparviai $
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

#ifndef _CPU_DETECT_H_
#define _CPU_DETECT_H_

#include "STTypes.h"

#define SUPPORT_MMX         0x0001
#define SUPPORT_3DNOW       0x0002
#define SUPPORT_ALTIVEC     0x0004
#define SUPPORT_SSE         0x0008
#define SUPPORT_SSE2        0x0010

/// Checks which instruction set extensions are supported by the CPU.
///
/// \return A bitmask of supported extensions, see SUPPORT_... defines.
uint detectCPUextensions(void);

/// Disables given set of instruction extensions. See SUPPORT_... defines.
void disableExtensions(uint wDisableMask);

#endif  // _CPU_DETECT_H_
