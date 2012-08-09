////////////////////////////////////////////////////////////////////////////////
///
/// Common type definitions for SoundTouch audio processing library.
///
/// Author        : Copyright (c) Olli Parviainen
/// Author e-mail : oparviai 'at' iki.fi
/// SoundTouch WWW: http://www.surina.net/soundtouch
///
////////////////////////////////////////////////////////////////////////////////
//
// Last changed  : $Date: 2012-04-01 18:01:42 +0100 (So, 01 Apr 2012) $
// File revision : $Revision: 3 $
//
// $Id: STTypes.h 136 2012-04-01 17:01:42Z oparviai $
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

#ifndef STTypes_H
#define STTypes_H

typedef unsigned int    uint;
typedef unsigned long   ulong;

#ifdef __GNUC__
    // In GCC, include soundtouch_config.h made by config scritps
    #include "soundtouch_config.h"
#endif

#ifndef _WINDEF_
    // if these aren't defined already by Windows headers, define now

    typedef int BOOL;

    #define FALSE   0
    #define TRUE    1

#endif  // _WINDEF_


namespace soundtouch
{
    /// Activate these undef's to overrule the possible sampletype 
    /// setting inherited from some other header file:
    //#undef SOUNDTOUCH_INTEGER_SAMPLES
    //#undef SOUNDTOUCH_FLOAT_SAMPLES

    #if !(SOUNDTOUCH_INTEGER_SAMPLES || SOUNDTOUCH_FLOAT_SAMPLES)
       
        /// Choose either 32bit floating point or 16bit integer sampletype
        /// by choosing one of the following defines, unless this selection 
        /// has already been done in some other file.
        ////
        /// Notes:
        /// - In Windows environment, choose the sample format with the
        ///   following defines.
        /// - In GNU environment, the floating point samples are used by 
        ///   default, but integer samples can be chosen by giving the 
        ///   following switch to the configure script:
        ///       ./configure --enable-integer-samples
        ///   However, if you still prefer to select the sample format here 
        ///   also in GNU environment, then please #undef the INTEGER_SAMPLE
        ///   and FLOAT_SAMPLE defines first as in comments above.
        //#define SOUNDTOUCH_INTEGER_SAMPLES     1    //< 16bit integer samples
        #define SOUNDTOUCH_FLOAT_SAMPLES       1    //< 32bit float samples
     
    #endif

    #if (_M_IX86 || __i386__ || __x86_64__ || _M_X64)
        /// Define this to allow X86-specific assembler/intrinsic optimizations. 
        /// Notice that library contains also usual C++ versions of each of these
        /// these routines, so if you're having difficulties getting the optimized 
        /// routines compiled for whatever reason, you may disable these optimizations 
        /// to make the library compile.

        #define SOUNDTOUCH_ALLOW_X86_OPTIMIZATIONS     1

        /// In GNU environment, allow the user to override this setting by
        /// giving the following switch to the configure script:
        /// ./configure --disable-x86-optimizations
        /// ./configure --enable-x86-optimizations=no
        #ifdef SOUNDTOUCH_DISABLE_X86_OPTIMIZATIONS
            #undef SOUNDTOUCH_ALLOW_X86_OPTIMIZATIONS
        #endif
    #else
        /// Always disable optimizations when not using a x86 systems.
        #undef SOUNDTOUCH_ALLOW_X86_OPTIMIZATIONS

    #endif

    // If defined, allows the SIMD-optimized routines to take minor shortcuts 
    // for improved performance. Undefine to require faithfully similar SIMD 
    // calculations as in normal C implementation.
    #define SOUNDTOUCH_ALLOW_NONEXACT_SIMD_OPTIMIZATION    1


    #ifdef SOUNDTOUCH_INTEGER_SAMPLES
        // 16bit integer sample type
        typedef short SAMPLETYPE;
        // data type for sample accumulation: Use 32bit integer to prevent overflows
        typedef long  LONG_SAMPLETYPE;

        #ifdef SOUNDTOUCH_FLOAT_SAMPLES
            // check that only one sample type is defined
            #error "conflicting sample types defined"
        #endif // SOUNDTOUCH_FLOAT_SAMPLES

        #ifdef SOUNDTOUCH_ALLOW_X86_OPTIMIZATIONS
            // Allow MMX optimizations
            #define SOUNDTOUCH_ALLOW_MMX   1
        #endif

    #else

        // floating point samples
        typedef float  SAMPLETYPE;
        // data type for sample accumulation: Use double to utilize full precision.
        typedef double LONG_SAMPLETYPE;

        #ifdef SOUNDTOUCH_ALLOW_X86_OPTIMIZATIONS
            // Allow SSE optimizations
            #define SOUNDTOUCH_ALLOW_SSE       1
        #endif

    #endif  // SOUNDTOUCH_INTEGER_SAMPLES

};

// define ST_NO_EXCEPTION_HANDLING switch to disable throwing std exceptions:
// #define ST_NO_EXCEPTION_HANDLING    1
#ifdef ST_NO_EXCEPTION_HANDLING
    // Exceptions disabled. Throw asserts instead if enabled.
    #include <assert.h>
    #define ST_THROW_RT_ERROR(x)    {assert((const char *)x);}
#else
    // use c++ standard exceptions
    #include <stdexcept>
    #define ST_THROW_RT_ERROR(x)    {throw std::runtime_error(x);}
#endif

// When this #define is active, eliminates a clicking sound when the "rate" or "pitch" 
// parameter setting crosses from value <1 to >=1 or vice versa during processing. 
// Default is off as such crossover is untypical case and involves a slight sound 
// quality compromise.
//#define SOUNDTOUCH_PREVENT_CLICK_AT_RATE_CROSSOVER   1

#endif
