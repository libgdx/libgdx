/*
	mangle: support defines for preprocessed assembler

	copyright 1995-2007 by the mpg123 project - free software under the terms of the LGPL 2.1
	see COPYING and AUTHORS files in distribution or http://mpg123.org

	This once started out as mangle.h from MPlayer, but you can't really call it derived work... the small part that in principle stems from MPlayer also being not very special (once you decided to use such a header at all, it's quite obvious material).
*/

#ifndef __MANGLE_H
#define __MANGLE_H

#include "config.h"
#include "intsym.h"

#ifdef CCALIGN
#define MOVUAPS movaps
#else
#define MOVUAPS movups
#endif

/*
	ALIGNX: align to X bytes
	This differs per compiler/platform in taking the byte count or an exponent for base 2.
	A way out is balign, if the assembler supports it (gas extension).
*/

#ifdef ASMALIGN_BALIGN

#define ALIGN4  .balign 4
#define ALIGN8  .balign 8
#define ALIGN16 .balign 16
#define ALIGN32 .balign 32

#else

#ifdef ASMALIGN_EXP
#define ALIGN4  .align 2
#define ALIGN8  .align 3
#define ALIGN16 .align 4
#define ALIGN32 .align 5
#else
#ifdef ASMALIGN_BYTE
#define ALIGN4  .align 4
#define ALIGN8  .align 8
#define ALIGN16 .align 16
#define ALIGN32 .align 32
#else
#error "Dunno how assembler alignment works. Please specify."
#endif
#endif

#endif

#define MANGLE_MACROCAT_REALLY(a, b) a ## b
#define MANGLE_MACROCAT(a, b) MANGLE_MACROCAT_REALLY(a, b)
/* Feel free to add more to the list, eg. a.out IMO */
#if defined(__USER_LABEL_PREFIX__)
#define ASM_NAME(a) MANGLE_MACROCAT(__USER_LABEL_PREFIX__,a)
#define ASM_VALUE(a) MANGLE_MACROCAT($,ASM_NAME(a))
#elif defined(__CYGWIN__) || defined(_WIN32) && !defined (_WIN64) || defined(__OS2__) || \
   (defined(__OpenBSD__) && !defined(__ELF__)) || defined(__APPLE__)
#define ASM_NAME(a) MANGLE_MACROCAT(_,a)
#define ASM_VALUE(a) MANGLE_MACROCAT($_,a)
#else
#define ASM_NAME(a) a
#define ASM_VALUE(a) MANGLE_MACROCAT($,a)
#endif

#if defined(__CYGWIN__) || defined(__MINGW32__) || defined(__APPLE__)
#define COMM(a,b,c) .comm a,b
#else
#define COMM(a,b,c) .comm a,b,c
#endif
/* more hacks for macosx; no .bss ... */
#ifdef __APPLE__
#define BSS .data
#else
#define BSS .bss
#endif

/* Mark non-executable stack.
   It's mainly for GNU on Linux... who else does (not) like this? */
#if !defined(__SUNPRO_C) && defined(__linux__) && defined(__ELF__)
#define NONEXEC_STACK .section .note.GNU-stack,"",%progbits
#else
#define NONEXEC_STACK
#endif

#endif /* !__MANGLE_H */

