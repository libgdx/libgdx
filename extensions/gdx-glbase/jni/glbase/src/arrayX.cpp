/**
 * @file arrayX.cpp
 * @brief Defines some useful arrays type
 *
 **/
#include "arrays.h"

/************* 1. int Array **********************************************/
#define TYPE int
#define SUFFIX I
#define SUFFIX_S "I"

//#include "ArrayX_.cpp"
#include "arrayX_.h"

#undef TYPE
#undef SUFFIX
#undef SUFFIX_S



/************* 2. ushort Array *******************************************/
#define TYPE ushort
#define SUFFIX US
#define SUFFIX_S "US"

//#include "ArrayX_.cpp"
#include "arrayX_.h"

#undef TYPE
#undef SUFFIX
#undef SUFFIX_S



/************* 3. GLubyte Array *******************************************/
#define TYPE byte
#define SUFFIX UB
#define SUFFIX_S "UB"

//#include "ArrayX_.cpp"
#include "arrayX_.h"

#undef TYPE
#undef SUFFIX
#undef SUFFIX_S



/************* 4. GLfloat Array *******************************************/
#define TYPE float
#define SUFFIX F
#define SUFFIX_S "F"
#define IS_FLOATING_POINT

//#include "ArrayX_.cpp"
#include "arrayX_.h"

#undef TYPE
#undef SUFFIX
#undef SUFFIX_S
