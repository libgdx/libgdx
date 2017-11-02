/*
Copyright (c) 2003-2014 Erwin Coumans  http://bullet.googlecode.com

This software is provided 'as-is', without any express or implied warranty.
In no event will the authors be held liable for any damages arising from the use of this software.
Permission is granted to anyone to use this software for any purpose, 
including commercial applications, and to alter it and redistribute it freely, 
subject to the following restrictions:

1. The origin of this software must not be misrepresented; you must not claim that you wrote the original software. If you use this software in a product, an acknowledgment in the product documentation would be appreciated but is not required.
2. Altered source versions must be plainly marked as such, and must not be misrepresented as being the original software.
3. This notice may not be removed or altered from any source distribution.
*/



#ifndef BT_THREADS_H
#define BT_THREADS_H

#include "btScalar.h" // has definitions like SIMD_FORCE_INLINE

///
/// btSpinMutex -- lightweight spin-mutex implemented with atomic ops, never puts
///               a thread to sleep because it is designed to be used with a task scheduler
///               which has one thread per core and the threads don't sleep until they
///               run out of tasks. Not good for general purpose use.
///
class btSpinMutex
{
    int mLock;

public:
    btSpinMutex()
    {
        mLock = 0;
    }
    void lock();
    void unlock();
    bool tryLock();
};

#if BT_THREADSAFE

// for internal Bullet use only
SIMD_FORCE_INLINE void btMutexLock( btSpinMutex* mutex )
{
    mutex->lock();
}

SIMD_FORCE_INLINE void btMutexUnlock( btSpinMutex* mutex )
{
    mutex->unlock();
}

SIMD_FORCE_INLINE bool btMutexTryLock( btSpinMutex* mutex )
{
    return mutex->tryLock();
}

// for internal use only
bool btIsMainThread();
unsigned int btGetCurrentThreadIndex();
const unsigned int BT_MAX_THREAD_COUNT = 64;

#else

// for internal Bullet use only
// if BT_THREADSAFE is undefined or 0, should optimize away to nothing
SIMD_FORCE_INLINE void btMutexLock( btSpinMutex* ) {}
SIMD_FORCE_INLINE void btMutexUnlock( btSpinMutex* ) {}
SIMD_FORCE_INLINE bool btMutexTryLock( btSpinMutex* ) {return true;}
#endif


#endif //BT_THREADS_H
