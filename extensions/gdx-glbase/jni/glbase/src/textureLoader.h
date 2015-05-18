/**
 * @file texture.h
 * @brief Texture module
 **/
#pragma once

#include "types.h"
#include GL2_H

// Forward
class Texture;

/**
 * GLES2.0テクスチャーを読み込む
 **/
class TextureLoader
{
 public:
  /**
   * Specified method to load a texture from headered byte data
   **/
  virtual Texture* load( byte* data, int length, bool repeat, bool mipmap ) = 0;

  /**
   * Create appropriate loader for specified byte data
   **/
  static TextureLoader* createLoaderFor( byte* data );

 private:
  /**
   * @return 指定のバイトデータがpkmテクスチャーのデータであるか否か
   **/
  static bool isPkm(byte* data);

  /**
   * @return 指定のバイトデータがatiテクスチャーのデータであるか否か
   **/
  static bool isATI(byte* data);

  /**
   * @return 指定のバイトデータがctesテクスチャーのデータであるか否か
   **/
  static bool isCTES(byte* data);

  /**
   * @return 指定のバイトデータがpvrテクスチャーのデータであるか否か
   **/
  static bool isPVR(byte* data);

};
