/**
 * @file myUniforms.h
 * @brief A wrapper class for custom uniforms buffer
 **/
#pragma once

#include "types.h"

#define MAX_MYUNIFORMS 32000

/**
 * Uniform value type
 **/
typedef enum
  {
    UTYPE_FLOAT,
    UTYPE_TEXTURE
  } MyUniformType;

/**
 * Single uniform settings
 **/
typedef struct MyUniformValue
{
  union{
    struct{
      float data[4];
      byte length;
    } vector;
    struct{
      int id;
      int glactive;  // GL_TEXTUREX
    } texture;
  } value;

  MyUniformType type;      // Type
} MyUniformValue;

/**
 * Buffer of uniform values
 **/
class MyUniformsBuffer
{
 public:
  MyUniformsBuffer();

  /**
   * Add a vector uniform (size 1-4)
   **/
  MyUniformValue* acquire(const float* vector, int numComponents);

  /**
   * Add a texture uniform
   **/
  MyUniformValue* acquire(int texture, int glactive);

  /**
   * Reset all data to start
   **/
  void reset();

 private:
  // Current length
  int numUniforms;
  // Data buffer
  MyUniformValue buffer[MAX_MYUNIFORMS];
};
