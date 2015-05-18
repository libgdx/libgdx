/**
 * @file textureCompressed.h
 * @brief Compressed texture module
 **/
#pragma once

#include "texture.h"
#include "types.h"
#include GL2_H

class Framebuffer;

/**
 * GLES2.0テクスチャーを象徴する
 **/
class TextureCompressed : public Texture
{
  friend class Framebuffer;
  friend class RenderQueue;
  
 public:
  
  /**
   * 初期化
   **/
  TextureCompressed(int width, int height, int format, bool filterLinear,
		    bool repeat, byte* data, bool mipmap, int bpp );

  /**
   * Destroy
   **/
  ~TextureCompressed();

  /**
   * レンダリング準備を行う
   **/
  void loadDataAndMipMaps( GLenum textureMode );
  
  /**
   * 指定テクスチャの指定位置にサブ画像をコピー
   * @return true
   */
  bool copySubImage(int offsetX, int offsetY, int width, int height, 
		    int format, int pixelFormat, byte *subData);
};
