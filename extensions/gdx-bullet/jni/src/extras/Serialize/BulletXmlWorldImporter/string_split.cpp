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

#include <assert.h>
//#include <stdbool.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "string_split.h"

///todo: remove stl dependency

namespace bullet_utils
{
    void split( btAlignedObjectArray<std::string>&pieces, const std::string& vector_str, const std::string& separator)
	{
		char** strArray = str_split(vector_str.c_str(),separator.c_str());
		int numSubStr = str_array_len(strArray);
		for (int i=0;i<numSubStr;i++)
			pieces.push_back(std::string(strArray[i]));
		str_array_free(strArray);
	}

};



/* Append an item to a dynamically allocated array of strings. On failure,
 return NULL, in which case the original array is intact. The item
 string is dynamically copied. If the array is NULL, allocate a new
 array. Otherwise, extend the array. Make sure the array is always
 NULL-terminated. Input string might not be '\0'-terminated. */
char **str_array_append(char **array, size_t nitems, const char *item,
                        size_t itemlen)
{
    /* Make a dynamic copy of the item. */
    char *copy;
    if (item == NULL)
        copy = NULL;
    else {
        copy = (char*)malloc(itemlen + 1);
        if (copy == NULL)
            return NULL;
        memcpy(copy, item, itemlen);
        copy[itemlen] = '\0';
    }
	
    /* Extend array with one element. Except extend it by two elements,
	 in case it did not yet exist. This might mean it is a teeny bit
	 too big, but we don't care. */
    array = (char**)realloc(array, (nitems + 2) * sizeof(array[0]));
    if (array == NULL) {
        free(copy);
        return NULL;
    }
	
    /* Add copy of item to array, and return it. */
    array[nitems] = copy;
    array[nitems+1] = NULL;
    return array;
}


/* Free a dynamic array of dynamic strings. */
void str_array_free(char **array)
{
    if (array == NULL)
        return;
    for (size_t i = 0; array[i] != NULL; ++i)
        free(array[i]);
    free(array);
}


/* Split a string into substrings. Return dynamic array of dynamically
 allocated substrings, or NULL if there was an error. Caller is
 expected to free the memory, for example with str_array_free. */
char **str_split(const char *input, const char *sep)
{
    size_t nitems = 0;
    char **array = NULL;
    const char *start = input;
    const char *next = strstr(start, sep);
    size_t seplen = strlen(sep);
    const char *item;
    size_t itemlen;
	
    for (;;) {
        next = strstr(start, sep);
        if (next == NULL) {
            /* Add the remaining string (or empty string, if input ends with
			 separator. */
            char **newstr = str_array_append(array, nitems, start, strlen(start));
            if (newstr == NULL) {
                str_array_free(array);
                return NULL;
            }
            array = newstr;
            ++nitems;
            break;
        } else if (next == input) {
            /* Input starts with separator. */
            item = "";
            itemlen = 0;
        } else {
            item = start;
            itemlen = next - item;
        }
        char **newstr = str_array_append(array, nitems, item, itemlen);
        if (newstr == NULL) {
            str_array_free(array);
            return NULL;
        }
        array = newstr;
        ++nitems;
        start = next + seplen;
    }
	
    if (nitems == 0) {
        /* Input does not contain separator at all. */
        assert(array == NULL);
        array = str_array_append(array, nitems, input, strlen(input));
    }
	
    return array;
}


/* Return length of a NULL-delimited array of strings. */
size_t str_array_len(char **array)
{
    size_t len;
	
    for (len = 0; array[len] != NULL; ++len)
        continue;
    return len;
}

#ifdef UNIT_TEST_STRING

#define MAX_OUTPUT 20


int main(void)
{
    struct {
        const char *input;
        const char *sep;
        char *output[MAX_OUTPUT];
    } tab[] = {
        /* Input is empty string. Output should be a list with an empty
		 string. */
        {
            "",
            "and",
            {
                "",
                NULL,
            },
        },
        /* Input is exactly the separator. Output should be two empty
		 strings. */
        {
            "and",
            "and",
            {
                "",
                "",
                NULL,
            },
        },
        /* Input is non-empty, but does not have separator. Output should
		 be the same string. */
        {
            "foo",
            "and",
            {
                "foo",
                NULL,
            },
        },
        /* Input is non-empty, and does have separator. */
        {
            "foo bar 1 and foo bar 2",
            " and ",
            {
                "foo bar 1",
                "foo bar 2",
                NULL,
            },
        },
    };
    const int tab_len = sizeof(tab) / sizeof(tab[0]);
    bool errors;
	
    errors = false;
	
    for (int i = 0; i < tab_len; ++i) {
        printf("test %d\n", i);
		
        char **output = str_split(tab[i].input, tab[i].sep);
        if (output == NULL) {
            fprintf(stderr, "output is NULL\n");
            errors = true;
            break;
        }
        size_t num_output = str_array_len(output);
        printf("num_output %lu\n", (unsigned long) num_output);
		
        size_t num_correct = str_array_len(tab[i].output);
        if (num_output != num_correct) {
            fprintf(stderr, "wrong number of outputs (%lu, not %lu)\n",
                    (unsigned long) num_output, (unsigned long) num_correct);
            errors = true;
        } else {
            for (size_t j = 0; j < num_output; ++j) {
                if (strcmp(tab[i].output[j], output[j]) != 0) {
                    fprintf(stderr, "output[%lu] is '%s' not '%s'\n",
                            (unsigned long) j, output[j], tab[i].output[j]);
                    errors = true;
                    break;
                }
            }
        }
		
        str_array_free(output);
        printf("\n");
    }
	
    if (errors)
        return EXIT_FAILURE;
    return 0;
}

#endif//


