/**
 * @file textureLoaderATI.h
 * @brief Loader for ATI compressed texture
 **/

#include "texture.h"
#include "textureLoaderATI.h"
#include "textureCompressed.h"
#include "fileUtils.h"
#include "glbase.h"
#include "macros.h"
#include GL2EXT_H

#include <string.h>

// ATC圧縮種類
enum ATCTYPE {
  ATC_RGB = 0x20,
  ATC_EXPLICIT_ALPHA = 0x41,
  ATC_INTERPOLATED_ALPHA = 0x49
};

Texture* TextureLoaderATI::load(byte* data, int length, bool repeat, bool mipmap)
{
  // ATIタイプをセット
  ATCTYPE atcType = (ATCTYPE)data[87];

  int dataLength = length-128;

  int width = data[ 15 ]*0x1000000 + data[ 14 ]*0x10000 + data[ 13 ]*0x100 + data[ 12 ];
  int height = data[ 19 ]*0x1000000 + data[ 18 ]*0x10000 + data[ 17 ]*0x100 + data[ 16 ];

  trace("ATIImage(%x) : %d bytes, %dx%d", atcType, length-128, width, height);

  int bpp;
  GLuint format;
  switch( atcType ){
    
  case ATC_RGB:                
    format = GL_ATC_RGB_AMD;
    bpp=4; 
    break;

  case ATC_EXPLICIT_ALPHA:     
    format = GL_ATC_RGBA_EXPLICIT_ALPHA_AMD;
    bpp=8;
    break;
    
  case ATC_INTERPOLATED_ALPHA: 
    format = GL_ATC_RGBA_INTERPOLATED_ALPHA_AMD;
    bpp=8;
    break;
    
  default:
    etrace("Unkown atitc subtype: %x", (int)atcType);
  }

  byte* imageData = new byte[dataLength];
  memcpy(imageData, data+128, dataLength);
  
  return new TextureCompressed(width, height, format, repeat, 
			       true, imageData, mipmap, bpp);
}
