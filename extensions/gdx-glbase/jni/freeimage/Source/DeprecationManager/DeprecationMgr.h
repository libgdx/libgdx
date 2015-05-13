// ==========================================================
// Deprecation Manager
//
// Design and implementation by
// - Noel Llopis (Game Programming Gems II)
//
// This file is part of FreeImage 3
//
// COVERED CODE IS PROVIDED UNDER THIS LICENSE ON AN "AS IS" BASIS, WITHOUT WARRANTY
// OF ANY KIND, EITHER EXPRESSED OR IMPLIED, INCLUDING, WITHOUT LIMITATION, WARRANTIES
// THAT THE COVERED CODE IS FREE OF DEFECTS, MERCHANTABLE, FIT FOR A PARTICULAR PURPOSE
// OR NON-INFRINGING. THE ENTIRE RISK AS TO THE QUALITY AND PERFORMANCE OF THE COVERED
// CODE IS WITH YOU. SHOULD ANY COVERED CODE PROVE DEFECTIVE IN ANY RESPECT, YOU (NOT
// THE INITIAL DEVELOPER OR ANY OTHER CONTRIBUTOR) ASSUME THE COST OF ANY NECESSARY
// SERVICING, REPAIR OR CORRECTION. THIS DISCLAIMER OF WARRANTY CONSTITUTES AN ESSENTIAL
// PART OF THIS LICENSE. NO USE OF ANY COVERED CODE IS AUTHORIZED HEREUNDER EXCEPT UNDER
// THIS DISCLAIMER.
//
// Use at your own risk!
// ==========================================================

#ifndef DEPRECATIONMGR_H
#define DEPRECATIONMGR_H

#ifdef _MSC_VER 
#pragma warning(disable : 4786 )  // identifier was truncated to 'number' characters
#endif 

#include "Utilities.h"

// ==========================================================

#if !defined(_M_X64) && defined(_MSC_VER)
	#define DEPRECATE(a,b) \
	{ \
		void *fptr;	\
		_asm { mov fptr, ebp }	\
		DeprecationMgr::GetInstance()->AddDeprecatedFunction(a, b, fptr); \
	}

#elif defined(__i386__) && defined(__GNUC__)
	#define DEPRECATE(a,b) \
	{ \
		void *fptr;	\
		__asm__("movl %%ebp, %0" : "=m" (fptr));	\
		DeprecationMgr::GetInstance()->AddDeprecatedFunction(a, b, fptr); \
	}

#else
	// default fallback case, which does not use the ebp register's content
	#define DEPRECATE(a,b) \
	{ \
		void *fptr = NULL;	\
		DeprecationMgr::GetInstance()->AddDeprecatedFunction(a, b, fptr); \
	}
#endif

// ==========================================================

class DeprecationMgr {
#if (_MSC_VER == 1100) // VC 5.0 need to look into the docs for the compiler for the value of each version
public:
#else
private:
#endif

	struct DeprecatedFunction {
		const char *old_function_name;
		const char *new_function_name;
		std::set<int> called_from;
	};

	std::map<const char *, DeprecatedFunction> m_functions;

public:
	DeprecationMgr();
	~DeprecationMgr();

	static DeprecationMgr * GetInstance ( void );
	void AddDeprecatedFunction(const char *old_function_name, const char *new_function_name, const void *frame_ptr);
};

#endif //DEPRECATIONMGR_H
