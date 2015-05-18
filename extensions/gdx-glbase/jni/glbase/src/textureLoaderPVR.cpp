/**
 * @file texturePVR.h
 * @brief PVR compressed texture
 **/

#include "texture.h"
#include "textureLoaderPVR.h"
#include "textureCompressed.h"
#include "fileUtils.h"
#include "glbase.h"
#include "macros.h"
#include GL2EXT_H

#include <string.h>

// PVR圧縮種類
enum PVRTYPE {
  PVR_RGB_2BPP = 0x00,
  PVR_RGBA_2BPP = 0x01,
  PVR_RGB_4BPP = 0x02,
  PVR_RGBA_4BPP = 0x03
};


Texture* TextureLoaderPVR::load(byte* data, int length, bool repeat, bool mipmap)
{
  // PVRタイプをセット
  PVRTYPE pvrType = (PVRTYPE)data[8];

  int width = data[ 27 ]*0x1000000 + data[ 26 ]*0x10000 + data[ 25 ]*0x100 + data[ 24 ];
  int height = data[ 31 ]*0x1000000 + data[ 30 ]*0x10000 + data[ 29 ]*0x100 + data[ 28 ];
	
  int extraHeaderLength = data[ 51 ]*0x1000000 + data[ 50 ]*0x10000 + data[ 49 ]*0x100 + data[ 48 ];
  int dataLength = length-(52+extraHeaderLength);
	
  trace("PVRImage(%x) : %d bytes, %dx%d", pvrType, dataLength, width, height);

    // 適切なピクセルフォーマットを指定する
  int bpp;
  GLuint format;
  switch( pvrType ){

  case PVR_RGB_2BPP:          
    format = GL_COMPRESSED_RGB_PVRTC_2BPPV1_IMG;
    bpp=2;
    break;

  case PVR_RGB_4BPP:     
    format = GL_COMPRESSED_RGB_PVRTC_4BPPV1_IMG;
    bpp=4;
    break;
    
  case PVR_RGBA_2BPP: 
    format = GL_COMPRESSED_RGBA_PVRTC_2BPPV1_IMG;
    bpp=2;
    break;

  case PVR_RGBA_4BPP:
    format = GL_COMPRESSED_RGBA_PVRTC_4BPPV1_IMG;
    bpp=4;
    break;
    
  default:
    etrace("Unsupported pvr subtype: %x", (int)pvrType);
  }

  int offset = 52 + extraHeaderLength;

  byte* imageData = new byte[dataLength];
  memcpy(imageData, data+offset, dataLength);

  return new TextureCompressed(width, height, format, repeat, 
			       true, imageData, mipmap, bpp);
}
