/**
 * @file myUniforms.cpp
 * @brief A wrapper class for custom uniforms buffer
 **/

#include <stdio.h>
#include <string.h>

#include "myUniforms.h"
#include "glbase.h"
#include "macros.h"

MyUniformsBuffer::MyUniformsBuffer()
{
  reset();
}

void MyUniformsBuffer::reset()
{
  numUniforms = 0;
}

MyUniformValue* MyUniformsBuffer::acquire(const float* vector, int numComponents)
{  
  MyUniformValue* cell = &buffer[numUniforms++];
  cell->type = UTYPE_FLOAT;
  cell->value.vector.length = numComponents;
  memcpy(cell->value.vector.data, vector, sizeof(float) * numComponents);

  return cell;
}

MyUniformValue* MyUniformsBuffer::acquire(int texture, int glactive)
{
  MyUniformValue* cell = &buffer[numUniforms++];
  cell->type = UTYPE_TEXTURE;
  cell->value.texture.id = texture;
  cell->value.texture.glactive = glactive;

  return cell;
}
