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

#ifndef B2_BLOCK_ALLOCATOR_H
#define B2_BLOCK_ALLOCATOR_H

#include "b2_api.h"
#include "b2_settings.h"

const int32 b2_blockSizeCount = 14;

struct b2Block;
struct b2Chunk;

/// This is a small object allocator used for allocating small
/// objects that persist for more than one time step.
/// See: http://www.codeproject.com/useritems/Small_Block_Allocator.asp
class B2_API b2BlockAllocator
{
public:
	b2BlockAllocator();
	~b2BlockAllocator();

	/// Allocate memory. This will use b2Alloc if the size is larger than b2_maxBlockSize.
	void* Allocate(int32 size);

	/// Free memory. This will use b2Free if the size is larger than b2_maxBlockSize.
	void Free(void* p, int32 size);

	void Clear();

private:

	b2Chunk* m_chunks;
	int32 m_chunkCount;
	int32 m_chunkSpace;

	b2Block* m_freeLists[b2_blockSizeCount];
};

#endif
