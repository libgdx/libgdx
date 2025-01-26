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

#include "box2d/b2_block_allocator.h"
#include <limits.h>
#include <string.h>
#include <stddef.h>

static const int32 b2_chunkSize = 16 * 1024;
static const int32 b2_maxBlockSize = 640;
static const int32 b2_chunkArrayIncrement = 128;

// These are the supported object sizes. Actual allocations are rounded up the next size.
static const int32 b2_blockSizes[b2_blockSizeCount] =
{
	16,		// 0
	32,		// 1
	64,		// 2
	96,		// 3
	128,	// 4
	160,	// 5
	192,	// 6
	224,	// 7
	256,	// 8
	320,	// 9
	384,	// 10
	448,	// 11
	512,	// 12
	640,	// 13
};

// This maps an arbitrary allocation size to a suitable slot in b2_blockSizes.
struct b2SizeMap
{
	b2SizeMap()
	{
		int32 j = 0;
		values[0] = 0;
		for (int32 i = 1; i <= b2_maxBlockSize; ++i)
		{
			b2Assert(j < b2_blockSizeCount);
			if (i <= b2_blockSizes[j])
			{
				values[i] = (uint8)j;
			}
			else
			{
				++j;
				values[i] = (uint8)j;
			}
		}
	}

	uint8 values[b2_maxBlockSize + 1];
};

static const b2SizeMap b2_sizeMap;

struct b2Chunk
{
	int32 blockSize;
	b2Block* blocks;
};

struct b2Block
{
	b2Block* next;
};

b2BlockAllocator::b2BlockAllocator()
{
	b2Assert(b2_blockSizeCount < UCHAR_MAX);

	m_chunkSpace = b2_chunkArrayIncrement;
	m_chunkCount = 0;
	m_chunks = (b2Chunk*)b2Alloc(m_chunkSpace * sizeof(b2Chunk));
	
	memset(m_chunks, 0, m_chunkSpace * sizeof(b2Chunk));
	memset(m_freeLists, 0, sizeof(m_freeLists));
}

b2BlockAllocator::~b2BlockAllocator()
{
	for (int32 i = 0; i < m_chunkCount; ++i)
	{
		b2Free(m_chunks[i].blocks);
	}

	b2Free(m_chunks);
}

void* b2BlockAllocator::Allocate(int32 size)
{
	if (size == 0)
	{
		return nullptr;
	}

	b2Assert(0 < size);

	if (size > b2_maxBlockSize)
	{
		return b2Alloc(size);
	}

	int32 index = b2_sizeMap.values[size];
	b2Assert(0 <= index && index < b2_blockSizeCount);

	if (m_freeLists[index])
	{
		b2Block* block = m_freeLists[index];
		m_freeLists[index] = block->next;
		return block;
	}
	else
	{
		if (m_chunkCount == m_chunkSpace)
		{
			b2Chunk* oldChunks = m_chunks;
			m_chunkSpace += b2_chunkArrayIncrement;
			m_chunks = (b2Chunk*)b2Alloc(m_chunkSpace * sizeof(b2Chunk));
			memcpy(m_chunks, oldChunks, m_chunkCount * sizeof(b2Chunk));
			memset(m_chunks + m_chunkCount, 0, b2_chunkArrayIncrement * sizeof(b2Chunk));
			b2Free(oldChunks);
		}

		b2Chunk* chunk = m_chunks + m_chunkCount;
		chunk->blocks = (b2Block*)b2Alloc(b2_chunkSize);
#if defined(_DEBUG)
		memset(chunk->blocks, 0xcd, b2_chunkSize);
#endif
		int32 blockSize = b2_blockSizes[index];
		chunk->blockSize = blockSize;
		int32 blockCount = b2_chunkSize / blockSize;
		b2Assert(blockCount * blockSize <= b2_chunkSize);
		for (int32 i = 0; i < blockCount - 1; ++i)
		{
			b2Block* block = (b2Block*)((int8*)chunk->blocks + blockSize * i);
			b2Block* next = (b2Block*)((int8*)chunk->blocks + blockSize * (i + 1));
			block->next = next;
		}
		b2Block* last = (b2Block*)((int8*)chunk->blocks + blockSize * (blockCount - 1));
		last->next = nullptr;

		m_freeLists[index] = chunk->blocks->next;
		++m_chunkCount;

		return chunk->blocks;
	}
}

void b2BlockAllocator::Free(void* p, int32 size)
{
	if (size == 0)
	{
		return;
	}

	b2Assert(0 < size);

	if (size > b2_maxBlockSize)
	{
		b2Free(p);
		return;
	}

	int32 index = b2_sizeMap.values[size];
	b2Assert(0 <= index && index < b2_blockSizeCount);

#if defined(_DEBUG)
	// Verify the memory address and size is valid.
	int32 blockSize = b2_blockSizes[index];
	bool found = false;
	for (int32 i = 0; i < m_chunkCount; ++i)
	{
		b2Chunk* chunk = m_chunks + i;
		if (chunk->blockSize != blockSize)
		{
			b2Assert(	(int8*)p + blockSize <= (int8*)chunk->blocks ||
						(int8*)chunk->blocks + b2_chunkSize <= (int8*)p);
		}
		else
		{
			if ((int8*)chunk->blocks <= (int8*)p && (int8*)p + blockSize <= (int8*)chunk->blocks + b2_chunkSize)
			{
				found = true;
			}
		}
	}

	b2Assert(found);

	memset(p, 0xfd, blockSize);
#endif

	b2Block* block = (b2Block*)p;
	block->next = m_freeLists[index];
	m_freeLists[index] = block;
}

void b2BlockAllocator::Clear()
{
	for (int32 i = 0; i < m_chunkCount; ++i)
	{
		b2Free(m_chunks[i].blocks);
	}

	m_chunkCount = 0;
	memset(m_chunks, 0, m_chunkSpace * sizeof(b2Chunk));
	memset(m_freeLists, 0, sizeof(m_freeLists));
}
