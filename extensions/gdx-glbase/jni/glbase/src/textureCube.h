/**
 * @file textureCube.h
 * @brief Cube texture module
 **/
#pragma once

#include "texture.h"
#include "types.h"
#include GL2_H

class Framebuffer;

/**
 * GLES2.0 Cubeテクスチャーを象徴する
 **/
class TextureCube : public Texture
{
 public:
  
  /**
   * 初期化
   **/
  TextureCube(Texture* faces[6]);

  /**
   * Destroy
   **/
  ~TextureCube();

  /**
   * レンダリング準備を行う
   **/
  void initRenderEnv();

  /**
   * Bind
   **/
  bool bind();
  
  /**
   * 指定テクスチャの指定位置にサブ画像をコピー
   * @return true
   */
  bool copySubImage(int offsetX, int offsetY, int width, int height, 
		    int format, int pixelFormat, byte *subData);

 private:
  Texture* faces[6];
};
