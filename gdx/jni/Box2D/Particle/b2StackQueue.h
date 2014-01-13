/*
* Copyright (c) 2013 Google, Inc.
*
* This software is provided 'as-is', without any express or implied
* warranty.  In no event will the authors be held liable for any damages
* arising from the use of this software.
* Permission is granted to anyone to use this software for any purpose,
* including commercial applications, and to alter it and redistribute it
* freely, subject to the following restrictions:
* 1. The origin of this software must not be misrepresented; you must not
* claim that you wrote the original software. If you use this software
* in a product, an acknowledgment in the product documentation would be
* appreciated but is not required.
* 2. Altered source versions must be plainly marked as such, and must not be
* misrepresented as being the original software.
* 3. This notice may not be removed or altered from any source distribution.
*/
#ifndef B2_STACK_QUEUE
#define B2_STACK_QUEUE

#include <Box2D/Common/b2StackAllocator.h>

template <typename T>
class b2StackQueue
{

public:

	b2StackQueue(b2StackAllocator *allocator, int32 capacity)
	{
		m_allocator = allocator;
		m_buffer = (T*) m_allocator->Allocate(sizeof(T) * capacity);
		m_front = m_buffer;
		m_back = m_buffer;
		m_end = m_buffer + capacity;
	}

	~b2StackQueue()
	{
		m_allocator->Free(m_buffer);
		m_buffer = NULL;
		m_front = NULL;
		m_back = NULL;
		m_end = NULL;
	}

	void Push(const T &item)
	{
		if (m_back >= m_end)
		{
			ptrdiff_t diff = m_front - m_buffer;
			for (T *it = m_front; it < m_back; ++it)
			{
				*(it - diff) = *it;
			}
			m_front -= diff;
			m_back -= diff;
			if (m_back >= m_end)
			{
				return;
			}
		}
		*m_back++ = item;
	}

	void Pop()
	{
		b2Assert(m_front < m_back);
		m_front++;
	}

	bool Empty() const
	{
		return m_front >= m_back;
	}

	const T &Front() const
	{
		return *m_front;
	}

private:

	b2StackAllocator *m_allocator;
	T* m_buffer;
	T* m_front;
	T* m_back;
	T* m_end;

};

#endif
