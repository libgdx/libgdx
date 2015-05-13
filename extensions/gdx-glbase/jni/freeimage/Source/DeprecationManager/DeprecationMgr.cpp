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

#ifdef _MSC_VER 
#pragma warning (disable : 4786) // identifier was truncated to 'number' characters
#endif 

#ifdef _WIN32
#include <windows.h>
#endif // _WIN32
#include "FreeImage.h"
#include "Utilities.h"
#include "DeprecationMgr.h"

// ==========================================================

DeprecationMgr::DeprecationMgr() {
}

DeprecationMgr::~DeprecationMgr() {
#ifdef _WIN32
	if (!m_functions.empty()) {
		OutputDebugString( "*************************************************************************************\n" );
		OutputDebugString( "This is a warning, because you use one or more deprecated functions.\nContinuing to use these functions might eventually render your program uncompilable.\nThe following functions are deprecated:\n\n" );

		for (std::map<const char *, DeprecatedFunction>::iterator i = m_functions.begin(); i != m_functions.end(); ++i) {
			DeprecatedFunction *function = &((*i).second);

			char txt[255];

			sprintf(txt, " * %s called from %i different places. Instead use %s.\n", function->old_function_name,  function->called_from.size(), function->new_function_name);

			OutputDebugString(txt);
		}

		OutputDebugString( "*************************************************************************************\n" );

		m_functions.clear();
	}
#endif // _WIN32
}

// ==========================================================

DeprecationMgr *
DeprecationMgr::GetInstance() {
	static DeprecationMgr Instance;
	return &Instance;
}

// ==========================================================

void
DeprecationMgr::AddDeprecatedFunction(const char *old_function_name, const char *new_function_name, const void *frame_ptr) {
#ifdef _WIN32
	int *preturn = (int *)frame_ptr + 1; // usual return address @ [ebp+4]
	int called_from = IsBadReadPtr(preturn, 4) ? 0 : *preturn;

	// check if this function was already listed as deprecated
	// if it wasn't, make a new entry for it
	// if it was, keep track of where it's called from.

	std::map<const char *, DeprecatedFunction>::iterator existing_function = m_functions.find(old_function_name);

	if (existing_function == m_functions.end()) {
		DeprecatedFunction function;

		function.old_function_name = old_function_name;
		function.new_function_name = new_function_name;
		function.called_from.insert(called_from);

		m_functions[old_function_name] = function;
	} else {
		// since we're keeping track of the addresses this function
		// was called from in a set, we don't need to check whether we've
		// already added the address.

		DeprecatedFunction *function = &((*existing_function).second);

		function->called_from.insert(called_from);
	}
#endif // _WIN32
}


