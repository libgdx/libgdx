/**
 * @file textureATI.h
 * @brief ATI compressed texture
 **/

#include "texture.h"
#include "textureLoaderCTES.h"
#include "textureCompressed.h"
#include "fileUtils.h"
#include "glbase.h"
#include "macros.h"
#include GL2EXT_H

#include <string.h>

// CTES圧縮種類
enum CTESTYPE {
  CTES_RGB = 0x1,
  CTES_EXPLICIT_ALPHA = 0x2,
  CTES_INTERPOLATED_ALPHA = 0x12
};

Texture* TextureLoaderCTES::load(byte* data, int length, bool repeat, bool mipmap)
{
  // CTESタイプをセット
  CTESTYPE atcType = (CTESTYPE)data[12];

  int dataLength = length-32;

  int width = data[ 7 ]*0x1000000 + data[ 6 ]*0x10000 + data[ 5 ]*0x100 + data[ 4 ];
  int height = data[ 11 ]*0x1000000 + data[ 10 ]*0x10000 + data[ 9 ]*0x100 + data[ 8 ];

  trace("CTES Image(%x) : %d bytes, %dx%d", atcType, length-32, width, height);

  // 適切なピクセルフォーマットを指定する
  int bpp;
  int format;

  switch( atcType ){

  case CTES_RGB:                
    format = GL_ATC_RGB_AMD;
    bpp=4; 
    break;

  case CTES_EXPLICIT_ALPHA:     
    format = GL_ATC_RGBA_EXPLICIT_ALPHA_AMD;
    bpp=8;
    break;
    
  case CTES_INTERPOLATED_ALPHA: 
    format = GL_ATC_RGBA_INTERPOLATED_ALPHA_AMD;
    bpp=8;
    break;
    
  default:
    etrace("Unkown atitc subtype: %x", (int)atcType);
  }

  byte* imageData = new byte[dataLength];
  memcpy(imageData, data+32, dataLength);

  return new TextureCompressed(width, height, format, repeat, 
			       true, imageData, mipmap, bpp);
}
