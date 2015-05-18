/**
 * @file framebuffer.h
 * @brief Framebuffer module
 **/
#pragma once

#include "types.h"
#include GL2_H

// Fwd
class Texture;

/**
 * GLES2.0 FBOを象徴する
 **/
class Framebuffer
{
 public:
  Framebuffer();
  ~Framebuffer();

  // Load vbo
  bool initRenderEnv();
  // Unload vbo
  void uninitRenderEnv();

  // Set texture
  void setTexture(Texture* texture, bool createDepth);
  // Bind
  void bind();

  // Set viewport
  void setViewport(int l, int t, int r, int b);
  int* getViewport();
  
 private:
  GLuint glid;
  GLuint depthRenderBuffer;
  int viewport[4];
};
