/* Copyright (C) 2006-2009 Charlie C & Erwin Coumans http://gamekit.googlecode.com
*
* This software is provided 'as-is', without any express or implied
* warranty.  In no event will the authors be held liable for any damages
* arising from the use of this software.
*
* Permission is granted to anyone to use this software for any purpose,
* including commercial applications, and to alter it and redistribute it
* freely, subject to the following restrictions:
*
* 1. The origin of this software must not be misrepresented; you must not
*    claim that you wrote the original software. If you use this software
*    in a product, an acknowledgment in the product documentation would be
*    appreciated but is not required.
* 2. Altered source versions must be plainly marked as such, and must not be
*    misrepresented as being the original software.
* 3. This notice may not be removed or altered from any source distribution.
*/
#ifndef __B_DEFINES_H__
#define __B_DEFINES_H__


// MISC defines, see BKE_global.h, BKE_utildefines.h
#define SIZEOFBLENDERHEADER 12


// ------------------------------------------------------------
#if defined(__sgi) || defined (__sparc) || defined (__sparc__) || defined (__PPC__) || defined (__ppc__) || defined (__BIG_ENDIAN__)
#	define MAKE_ID(a,b,c,d) ( (int)(a)<<24 | (int)(b)<<16 | (c)<<8 | (d) )
#else
#	define MAKE_ID(a,b,c,d) ( (int)(d)<<24 | (int)(c)<<16 | (b)<<8 | (a) )
#endif


// ------------------------------------------------------------
#if defined(__sgi) || defined(__sparc) || defined(__sparc__) || defined (__PPC__) || defined (__ppc__) || defined (__BIG_ENDIAN__)
#	define MAKE_ID2(c, d) ( (c)<<8 | (d) )
#	define MOST_SIG_BYTE   0
#	define BBIG_ENDIAN
#else
#	define MAKE_ID2(c, d) ( (d)<<8 | (c) )
#	define MOST_SIG_BYTE  1
#	define BLITTLE_ENDIAN
#endif

// ------------------------------------------------------------
#define ID_SCE		MAKE_ID2('S', 'C')
#define ID_LI		MAKE_ID2('L', 'I')
#define ID_OB		MAKE_ID2('O', 'B')
#define ID_ME		MAKE_ID2('M', 'E')
#define ID_CU		MAKE_ID2('C', 'U')
#define ID_MB		MAKE_ID2('M', 'B')
#define ID_MA		MAKE_ID2('M', 'A')
#define ID_TE		MAKE_ID2('T', 'E')
#define ID_IM		MAKE_ID2('I', 'M')
#define ID_IK		MAKE_ID2('I', 'K')
#define ID_WV		MAKE_ID2('W', 'V')
#define ID_LT		MAKE_ID2('L', 'T')
#define ID_SE		MAKE_ID2('S', 'E')
#define ID_LF		MAKE_ID2('L', 'F')
#define ID_LA		MAKE_ID2('L', 'A')
#define ID_CA		MAKE_ID2('C', 'A')
#define ID_IP		MAKE_ID2('I', 'P')
#define ID_KE		MAKE_ID2('K', 'E')
#define ID_WO		MAKE_ID2('W', 'O')
#define ID_SCR		MAKE_ID2('S', 'R')
#define ID_VF		MAKE_ID2('V', 'F')
#define ID_TXT		MAKE_ID2('T', 'X')
#define ID_SO		MAKE_ID2('S', 'O')
#define ID_SAMPLE	MAKE_ID2('S', 'A')
#define ID_GR		MAKE_ID2('G', 'R')
#define ID_ID		MAKE_ID2('I', 'D')
#define ID_AR		MAKE_ID2('A', 'R')
#define ID_AC		MAKE_ID2('A', 'C')
#define ID_SCRIPT	MAKE_ID2('P', 'Y')
#define ID_FLUIDSIM	MAKE_ID2('F', 'S')
#define ID_NT		MAKE_ID2('N', 'T')
#define ID_BR		MAKE_ID2('B', 'R')


#define ID_SEQ		MAKE_ID2('S', 'Q')
#define ID_CO		MAKE_ID2('C', 'O')
#define ID_PO		MAKE_ID2('A', 'C')
#define ID_NLA		MAKE_ID2('N', 'L')

#define ID_VS		MAKE_ID2('V', 'S')
#define ID_VN		MAKE_ID2('V', 'N')


// ------------------------------------------------------------
#define FORM MAKE_ID('F','O','R','M')
#define DDG1 MAKE_ID('3','D','G','1')
#define DDG2 MAKE_ID('3','D','G','2')
#define DDG3 MAKE_ID('3','D','G','3')
#define DDG4 MAKE_ID('3','D','G','4')
#define GOUR MAKE_ID('G','O','U','R')
#define BLEN MAKE_ID('B','L','E','N')
#define DER_ MAKE_ID('D','E','R','_')
#define V100 MAKE_ID('V','1','0','0')
#define DATA MAKE_ID('D','A','T','A')
#define GLOB MAKE_ID('G','L','O','B')
#define IMAG MAKE_ID('I','M','A','G')
#define TEST MAKE_ID('T','E','S','T')
#define USER MAKE_ID('U','S','E','R')


// ------------------------------------------------------------
#define DNA1 MAKE_ID('D','N','A','1')
#define REND MAKE_ID('R','E','N','D')
#define ENDB MAKE_ID('E','N','D','B')
#define NAME MAKE_ID('N','A','M','E')
#define SDNA MAKE_ID('S','D','N','A')
#define TYPE MAKE_ID('T','Y','P','E')
#define TLEN MAKE_ID('T','L','E','N')
#define STRC MAKE_ID('S','T','R','C')


// ------------------------------------------------------------
#define SWITCH_INT(a) { \
    char s_i, *p_i; \
    p_i= (char *)&(a); \
    s_i=p_i[0]; p_i[0]=p_i[3]; p_i[3]=s_i; \
    s_i=p_i[1]; p_i[1]=p_i[2]; p_i[2]=s_i; }

// ------------------------------------------------------------
#define SWITCH_SHORT(a)	{ \
    char s_i, *p_i; \
	p_i= (char *)&(a); \
	s_i=p_i[0]; p_i[0]=p_i[1]; p_i[1]=s_i; }

// ------------------------------------------------------------
#define SWITCH_LONGINT(a) { \
    char s_i, *p_i; \
    p_i= (char *)&(a);  \
    s_i=p_i[0]; p_i[0]=p_i[7]; p_i[7]=s_i; \
    s_i=p_i[1]; p_i[1]=p_i[6]; p_i[6]=s_i; \
    s_i=p_i[2]; p_i[2]=p_i[5]; p_i[5]=s_i; \
    s_i=p_i[3]; p_i[3]=p_i[4]; p_i[4]=s_i; }

#endif//__B_DEFINES_H__
