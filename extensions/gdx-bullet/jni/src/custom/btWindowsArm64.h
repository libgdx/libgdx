#pragma once

#if defined(_MSC_VER) && defined(_M_ARM64)

#include <math.h>
#include <stdlib.h>
#include <float.h>
#include <stdio.h>

// Bullet 2.87 only recognizes _M_ARM when disabling Windows SSE intrinsics.
// Scope the compatibility macro to btScalar.h so it cannot affect the Windows SDK.
#pragma push_macro("_M_ARM")
#undef _M_ARM
#define _M_ARM 7
#include "LinearMath/btScalar.h"
#pragma pop_macro("_M_ARM")
#endif
