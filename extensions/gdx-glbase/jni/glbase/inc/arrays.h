/**
 * @file arrays.h
 * @brief Uses arrayX utilities to define arrays of basic types
 **/
#pragma once
#include "types.h"


// int
#define TYPE int
#define SUFFIX I

#include "arrayX.h"

#undef TYPE
#undef SUFFIX

// float
#define TYPE float
#define SUFFIX F

#include "arrayX.h"

#undef TYPE
#undef SUFFIX

// unsigned short
#define TYPE ushort
#define SUFFIX US

#include "arrayX.h"

#undef TYPE
#undef SUFFIX

// unsigned bytes
#define TYPE byte
#define SUFFIX UB

#include "arrayX.h"

#undef TYPE
#undef SUFFIX
