/*
Bullet Continuous Collision Detection and Physics Library
Copyright (c) 2003-2012 Erwin Coumans  http://bulletphysics.org

This software is provided 'as-is', without any express or implied warranty.
In no event will the authors be held liable for any damages arising from the use of this software.
Permission is granted to anyone to use this software for any purpose, 
including commercial applications, and to alter it and redistribute it freely, 
subject to the following restrictions:

1. The origin of this software must not be misrepresented; you must not claim that you wrote the original software. If you use this software in a product, an acknowledgment in the product documentation would be appreciated but is not required.
2. Altered source versions must be plainly marked as such, and must not be misrepresented as being the original software.
3. This notice may not be removed or altered from any source distribution.
*/

///The string split C code is by Lars Wirzenius
///See http://stackoverflow.com/questions/2531605/how-to-split-a-string-with-a-delimiter-larger-than-one-single-char


#ifndef STRING_SPLIT_H
#define STRING_SPLIT_H

#include <cstring>
#include "LinearMath/btAlignedObjectArray.h"

#include <string>

namespace bullet_utils
{
	void split( btAlignedObjectArray<std::string>&pieces, const std::string& vector_str, const std::string& separator);
};

///The string split C code is by Lars Wirzenius
///See http://stackoverflow.com/questions/2531605/how-to-split-a-string-with-a-delimiter-larger-than-one-single-char


/* Split a string into substrings. Return dynamic array of dynamically
 allocated substrings, or NULL if there was an error. Caller is
 expected to free the memory, for example with str_array_free. */
char**	str_split(const char* input, const char* sep);

/* Free a dynamic array of dynamic strings. */
void str_array_free(char** array);

/* Return length of a NULL-delimited array of strings. */
size_t str_array_len(char** array);

#endif //STRING_SPLIT_H

