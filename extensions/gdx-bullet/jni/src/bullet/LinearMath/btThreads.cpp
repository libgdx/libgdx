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


#include "btThreads.h"

//
// Lightweight spin-mutex based on atomics
// Using ordinary system-provided mutexes like Windows critical sections was noticeably slower
// presumably because when it fails to lock at first it would sleep the thread and trigger costly
// context switching.
// 

#if BT_THREADSAFE

#if __cplusplus >= 201103L

// for anything claiming full C++11 compliance, use C++11 atomics
// on GCC or Clang you need to compile with -std=c++11
#define USE_CPP11_ATOMICS 1

#elif defined( _MSC_VER )

// on MSVC, use intrinsics instead
#define USE_MSVC_INTRINSICS 1

#elif defined( __GNUC__ ) && (__GNUC__ > 4 || (__GNUC__ == 4 && __GNUC_MINOR__ >= 7))

// available since GCC 4.7 and some versions of clang
// todo: check for clang
#define USE_GCC_BUILTIN_ATOMICS 1

#elif defined( __GNUC__ ) && (__GNUC__ == 4 && __GNUC_MINOR__ >= 1)

// available since GCC 4.1
#define USE_GCC_BUILTIN_ATOMICS_OLD 1

#endif


#if USE_CPP11_ATOMICS

#include <atomic>
#include <thread>

#define THREAD_LOCAL_STATIC thread_local static

bool btSpinMutex::tryLock()
{
    std::atomic<int>* aDest = reinterpret_cast<std::atomic<int>*>(&mLock);
    int expected = 0;
    return std::atomic_compare_exchange_weak_explicit( aDest, &expected, int(1), std::memory_order_acq_rel, std::memory_order_acquire );
}

void btSpinMutex::lock()
{
    // note: this lock does not sleep the thread.
    while (! tryLock())
    {
        // spin
    }
}

void btSpinMutex::unlock()
{
    std::atomic<int>* aDest = reinterpret_cast<std::atomic<int>*>(&mLock);
    std::atomic_store_explicit( aDest, int(0), std::memory_order_release );
}


#elif USE_MSVC_INTRINSICS

#define WIN32_LEAN_AND_MEAN

#include <windows.h>
#include <intrin.h>

#define THREAD_LOCAL_STATIC __declspec( thread ) static


bool btSpinMutex::tryLock()
{
    volatile long* aDest = reinterpret_cast<long*>(&mLock);
    return ( 0 == _InterlockedCompareExchange( aDest, 1, 0) );
}

void btSpinMutex::lock()
{
    // note: this lock does not sleep the thread
    while (! tryLock())
    {
        // spin
    }
}

void btSpinMutex::unlock()
{
    volatile long* aDest = reinterpret_cast<long*>( &mLock );
    _InterlockedExchange( aDest, 0 );
}

#elif USE_GCC_BUILTIN_ATOMICS

#define THREAD_LOCAL_STATIC static __thread


bool btSpinMutex::tryLock()
{
    int expected = 0;
    bool weak = false;
    const int memOrderSuccess = __ATOMIC_ACQ_REL;
    const int memOrderFail = __ATOMIC_ACQUIRE;
    return __atomic_compare_exchange_n(&mLock, &expected, int(1), weak, memOrderSuccess, memOrderFail);
}

void btSpinMutex::lock()
{
    // note: this lock does not sleep the thread
    while (! tryLock())
    {
        // spin
    }
}

void btSpinMutex::unlock()
{
    __atomic_store_n(&mLock, int(0), __ATOMIC_RELEASE);
}

#elif USE_GCC_BUILTIN_ATOMICS_OLD


#define THREAD_LOCAL_STATIC static __thread

bool btSpinMutex::tryLock()
{
    return __sync_bool_compare_and_swap(&mLock, int(0), int(1));
}

void btSpinMutex::lock()
{
    // note: this lock does not sleep the thread
    while (! tryLock())
    {
        // spin
    }
}

void btSpinMutex::unlock()
{
    // write 0
    __sync_fetch_and_and(&mLock, int(0));
}

#else //#elif USE_MSVC_INTRINSICS

#error "no threading primitives defined -- unknown platform"

#endif  //#else //#elif USE_MSVC_INTRINSICS


struct ThreadsafeCounter
{
    unsigned int mCounter;
    btSpinMutex mMutex;

    ThreadsafeCounter() {mCounter=0;}

    unsigned int getNext()
    {
        // no need to optimize this with atomics, it is only called ONCE per thread!
        mMutex.lock();
        unsigned int val = mCounter++;
        mMutex.unlock();
        return val;
    }
};

static ThreadsafeCounter gThreadCounter;


// return a unique index per thread, starting with 0 and counting up
unsigned int btGetCurrentThreadIndex()
{
    const unsigned int kNullIndex = ~0U;
    THREAD_LOCAL_STATIC unsigned int sThreadIndex = kNullIndex;
    if ( sThreadIndex == kNullIndex )
    {
        sThreadIndex = gThreadCounter.getNext();
    }
    return sThreadIndex;
}

bool btIsMainThread()
{
    return btGetCurrentThreadIndex() == 0;
}

#else // #if BT_THREADSAFE

// These should not be called ever
void btSpinMutex::lock()
{
    btAssert(!"unimplemented btSpinMutex::lock() called");
}

void btSpinMutex::unlock()
{
    btAssert(!"unimplemented btSpinMutex::unlock() called");
}

bool btSpinMutex::tryLock()
{
    btAssert(!"unimplemented btSpinMutex::tryLock() called");
    return true;
}


#endif // #if BT_THREADSAFE

