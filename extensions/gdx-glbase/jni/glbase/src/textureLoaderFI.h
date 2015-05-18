/**
 * @file textureLoaderFI.h
 * @brief Loader for FreeImage texture: supports all types supported by FreeImage(BMP, PNG, JPG..)
 **/
#pragma once

#include "textureLoader.h"
#include "FreeImage.h"

// Forward
class Texture;

/**
 * FreeImageテクスチャー
 **/
class TextureLoaderFI : public TextureLoader
{
 public:
  Texture* load(byte* data, int length, bool repeat, bool mipmap);  
  
 private:
  static const int MAX_MIPMAPS = 16;

  int width;
  int height;
  int bpp;
  GLuint format;
  GLuint pixelFormat;
  byte* imageData;
  
  // メソッド
  FIBITMAP* convertTo16Bit(FIBITMAP* bmp);
  FIBITMAP* convertTo24or32Bit(FIBITMAP* bmp);

  bool is565(unsigned redMask, unsigned greenMask, unsigned blueMask);
  bool is4444(unsigned redMask, unsigned greenMask, unsigned blueMask);
  bool is5551(unsigned redMask, unsigned greenMask, unsigned blueMask);

  void convertToRaw4444Bits(byte* outData, FIBITMAP* bmp);
  void convertToRaw5551Bits(byte* outData, FIBITMAP* bmp);
  void convertToRaw32Bits(byte* outData, FIBITMAP* bmp);

  void setFrom565(FIBITMAP* bmp);
  void setFrom4444(FIBITMAP* bmp);
  void setFrom5551(FIBITMAP* bmp);
  void setFrom24(FIBITMAP* bmp);
  void setFrom32(FIBITMAP* bmp);  
};
