/**
 * @file textureFI.cpp
 * @brief Free Image texture implementation
 **/

#include "texture.h"
#include "textureLoaderFI.h"
#include "fileUtils.h"
#include "glbase.h"
#include "macros.h"
#include "FreeImage.h"
#include "Utilities.h"
#include "types.h"
#include GL2_H
#include <string.h>

Texture* TextureLoaderFI::load(byte* data, int length, bool repeat, bool mipmap)
{
  // メモリーストリームを開く
  FIMEMORY *hmem = FreeImage_OpenMemory(data, length);
  if( !hmem ) return NULL;

  // ファイルタイプを取得
  FREE_IMAGE_FORMAT fif = FreeImage_GetFileTypeFromMemory(hmem, 0);

  // 画像をロード！
  FIBITMAP *bmp = FreeImage_LoadFromMemory(fif, hmem, 0);
  if( !bmp ) return NULL;

  // 画像形式をチェックする
  bpp = FreeImage_GetBPP(bmp);

  // 幅と高さはずっと変わらない
  width = FreeImage_GetWidth(bmp);
  height = FreeImage_GetHeight(bmp);

  switch( bpp ){
  case 1:
  case 2:
  case 4:
  case 8:
    // 16ビットの適切なものへ変換！
    bmp = convertTo16Bit(bmp);
    if( !bmp ) return NULL;
    
    // NO BREAK
    
  case 16:
    {
      unsigned redMask = FreeImage_GetRedMask(bmp);
      unsigned greenMask = FreeImage_GetGreenMask(bmp);
      unsigned blueMask = FreeImage_GetBlueMask(bmp);

      // RGB 565/4444/5551 形式：Native対応！
      if( is565(redMask, greenMask, blueMask) ){
	setFrom565(bmp);
      }
      else if( is4444(redMask, greenMask, blueMask) ){
	setFrom4444(bmp);
      }
      else if( is5551(redMask, greenMask, blueMask) ) {
	setFrom5551(bmp);
      }
      else{
	bmp = convertTo16Bit(bmp);
	if( !bmp ) return NULL;
	setFrom565(bmp);
      }
    }
    break;

  case 24:
    setFrom24(bmp);
    break;
  case 32:
    setFrom32(bmp);
    break;

  default:
    bmp = convertTo24or32Bit(bmp);

    if( !bmp ) {
      etrace("Unsupported bit per pixel size :%d. Supported only 24/32 bits bmps", bpp);
      return NULL;
    }

    if( FreeImage_GetBPP(bmp) == 24 ){
      setFrom24(bmp);
    }
    else{
      setFrom32(bmp);
    }

    break;
  }

  // メモリーストリーム開放
  FreeImage_CloseMemory(hmem);
  FreeImage_Unload(bmp);

  return new Texture(width, height, format, pixelFormat, true, 
		     repeat, imageData, mipmap, bpp);
}

void TextureLoaderFI::setFrom565(FIBITMAP* bmp)
{
  trace("Final image: %dbits, %dx%d %s", FreeImage_GetBPP(bmp), width, height, FreeImage_IsTransparent(bmp) ? "transparent" : "");
  
  int pitch = FreeImage_GetPitch(bmp);
  bpp = FreeImage_GetBPP(bmp);
  
  // ピクセルデータに変換！
  imageData = new byte[height*pitch];
  FreeImage_ConvertToRawBits(imageData, bmp, pitch, bpp, FI_RGBA_RED_MASK, FI_RGBA_GREEN_MASK, FI_RGBA_BLUE_MASK, false);

  format = GL_RGB;
  pixelFormat = GL_UNSIGNED_SHORT_5_6_5;
  trace("Using native RGB565!");
}

void TextureLoaderFI::setFrom4444(FIBITMAP* bmp)
{
  trace("Final image: %dbits, %dx%d %s", FreeImage_GetBPP(bmp), width, height, FreeImage_IsTransparent(bmp) ? "transparent" : "");

  int pitch = FreeImage_GetPitch(bmp);
  bpp = FreeImage_GetBPP(bmp);

  // ピクセルデータに変換！
  imageData = new byte[height*pitch];
  convertToRaw4444Bits(imageData, bmp);
  
  format = GL_RGBA;
  pixelFormat = GL_UNSIGNED_SHORT_4_4_4_4;
  trace("Using native RGB4444!");
}

void TextureLoaderFI::setFrom5551(FIBITMAP* bmp)
{
  trace("Final image: %dbits, %dx%d %s", FreeImage_GetBPP(bmp), width, height, FreeImage_IsTransparent(bmp) ? "transparent" : "");
  
  int pitch = FreeImage_GetPitch(bmp);
  bpp = FreeImage_GetBPP(bmp);

  // ピクセルデータに変換！
  imageData = new byte[height*pitch];
  convertToRaw5551Bits(imageData, bmp);
  
  format = GL_RGBA;
  pixelFormat = GL_UNSIGNED_SHORT_5_5_5_1;
  trace("Using native RGB5551!");
}

void TextureLoaderFI::setFrom24(FIBITMAP* bmp)
{
  SwapRedBlue32(bmp);
  trace("Final image: %dbits, %dx%d %s", FreeImage_GetBPP(bmp), width, height, FreeImage_IsTransparent(bmp) ? "transparent" : "");

  int pitch = FreeImage_GetPitch(bmp);
  bpp = FreeImage_GetBPP(bmp);
  
  // ピクセルデータに変換！
  imageData = new byte[height*pitch];
  FreeImage_ConvertToRawBits(imageData, bmp, pitch, bpp, FI_RGBA_RED_MASK, FI_RGBA_GREEN_MASK, FI_RGBA_BLUE_MASK, false);
  
  format = GL_RGB;
  pixelFormat = GL_UNSIGNED_BYTE;
  trace("Using native RGB888!");
}

void TextureLoaderFI::setFrom32(FIBITMAP* bmp)
{
  SwapRedBlue32(bmp);
  trace("Final image: %dbits, %dx%d %s", FreeImage_GetBPP(bmp), width, height, FreeImage_IsTransparent(bmp) ? "transparent" : "");
  
  int pitch = FreeImage_GetPitch(bmp);
  bpp = FreeImage_GetBPP(bmp);
  
  // ピクセルデータに変換！
  imageData = new byte[height*pitch];
  convertToRaw32Bits(imageData, bmp);
  
  format = GL_RGBA;
  pixelFormat = GL_UNSIGNED_BYTE;
  trace("Using native RGBA8888!");
}

FIBITMAP* TextureLoaderFI::convertTo16Bit(FIBITMAP* bmp)
{
  trace("Converting %d-bit image to RGB565!", FreeImage_GetBPP(bmp));

  // 変換
  FIBITMAP* bmp16 = FreeImage_ConvertTo16Bits565(bmp);
  // 古いBMPを開放
  FreeImage_Unload(bmp);  

  return bmp16;
}

FIBITMAP* TextureLoaderFI::convertTo24or32Bit(FIBITMAP* bmp)
{
  FIBITMAP* bmpNew;
  if( FreeImage_IsTransparent(bmp) ) {
    trace("Converting %d-bit image to RGBA8888!", FreeImage_GetBPP(bmp));
    bmpNew = FreeImage_ConvertTo32Bits(bmp);
  }
  else {
    trace("Converting %d-bit image to RGB888!", FreeImage_GetBPP(bmp));
    bmpNew = FreeImage_ConvertTo24Bits(bmp);
  }
  
  // 古いbmpを開放
  FreeImage_Unload(bmp);
  
  if( bmpNew != NULL ){
    // BGRA->RGBA
    SwapRedBlue32(bmpNew);
  }

  return bmpNew;
}

void TextureLoaderFI::convertToRaw4444Bits(byte* outData, FIBITMAP* bmp)
{
  int pitch = FreeImage_GetPitch(bmp);

  for (unsigned i = 0; i < FreeImage_GetHeight(bmp); ++i) {
    unsigned short *scanline = (unsigned short*)FreeImage_GetScanLine(bmp, i);
    
    for( int x=0; x<width; x++ ){
      // ARGB -> RGBA
      byte a = (scanline[x] & 0xf000) >> 12;
      byte r = (scanline[x] & 0xf00) >> 8;
      byte g = (scanline[x] & 0xf0) >> 4;
      byte b = (scanline[x] & 0xf);

      unsigned short* out = (unsigned short*)&outData[x*2];
      *out = (((unsigned short)r)<<12) | (((unsigned short)g)<<8) | (((unsigned short)b)<<4) | a;
    }
    
    outData += pitch;
  }  
}

void TextureLoaderFI::convertToRaw5551Bits(byte* outData, FIBITMAP* bmp)
{
  int pitch = FreeImage_GetPitch(bmp);

  for (unsigned i = 0; i < FreeImage_GetHeight(bmp); ++i) {
    unsigned short *scanline = (unsigned short*)FreeImage_GetScanLine(bmp, i);

    for( int x=0; x<width; x++ ){
      // ARGB -> RGBA
      byte a = (scanline[x] & 0x8000) >> 15;
      byte r = (scanline[x] & 0x7c00) >> 10;
      byte g = (scanline[x] & 0x3e0) >> 5;
      byte b = (scanline[x] & 0x1f);

      unsigned short* out = (unsigned short*)&outData[x*2];
      *out = (((unsigned short)r)<<11) | (((unsigned short)g)<<6) | (((unsigned short)b)<<1) | a;
    }

    outData += pitch;
  }  
}

void TextureLoaderFI::convertToRaw32Bits(byte* outData, FIBITMAP* bmp)
{
  int pitch = FreeImage_GetPitch(bmp);

  for (unsigned i = 0; i < FreeImage_GetHeight(bmp); ++i) {
    int *scanline = (int*)FreeImage_GetScanLine(bmp, i);

    for( int x=0; x<width; x++ ){
      // ARGB -> RGBA
      byte a = scanline[x] >> 24;
      byte b = scanline[x] >> 16;
      byte g = scanline[x] >> 8;
      byte r = scanline[x];

      outData[x*4] = r;
      outData[x*4 + 1] = g;
      outData[x*4 + 2] = b;
      outData[x*4 + 3] = a;
    }

    outData += pitch;
  }  
}

bool TextureLoaderFI::is565(unsigned redMask, unsigned greenMask, unsigned blueMask)
{
  return redMask == 0xf800 && greenMask == 0x7e0 && blueMask == 0x1f;
}

bool TextureLoaderFI::is4444(unsigned redMask, unsigned greenMask, unsigned blueMask)
{
  return redMask == 0xf00 && greenMask == 0xf0 && blueMask == 0xf;
}

bool TextureLoaderFI::is5551(unsigned redMask, unsigned greenMask, unsigned blueMask)
{
  return redMask == 0x7C00 && greenMask == 0x3e0 && blueMask == 0x1f;
}
