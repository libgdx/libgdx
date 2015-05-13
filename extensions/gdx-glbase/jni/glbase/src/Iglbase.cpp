/**
 * @file Iglbase.cpp
 * @brief Implementation for IGLBase: wrap library over GL ES 2.0
 **/

#include "glbase.h"

IGLBase* IGLBase::get()
{
  return GLBase::get();
}
