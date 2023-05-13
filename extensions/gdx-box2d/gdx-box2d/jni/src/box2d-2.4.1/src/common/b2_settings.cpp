// MIT License

// Copyright (c) 2019 Erin Catto

// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:

// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.

// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.

#define _CRT_SECURE_NO_WARNINGS

#include "box2d/b2_settings.h"
#include <stdio.h>
#include <stdarg.h>
#include <stdlib.h>

b2Version b2_version = {2, 4, 1};

// Memory allocators. Modify these to use your own allocator.
void* b2Alloc_Default(int32 size)
{
	return malloc(size);
}

void b2Free_Default(void* mem)
{
	free(mem);
}

// You can modify this to use your logging facility.
void b2Log_Default(const char* string, va_list args)
{
	vprintf(string, args);
}

FILE* b2_dumpFile = nullptr;

void b2OpenDump(const char* fileName)
{
	b2Assert(b2_dumpFile == nullptr);
	b2_dumpFile = fopen(fileName, "w");
}

void b2Dump(const char* string, ...)
{
	if (b2_dumpFile == nullptr)
	{
		return;
	}

	va_list args;
	va_start(args, string);
	vfprintf(b2_dumpFile, string, args);
	va_end(args);
}

void b2CloseDump()
{
	fclose(b2_dumpFile);
	b2_dumpFile = nullptr;
}
