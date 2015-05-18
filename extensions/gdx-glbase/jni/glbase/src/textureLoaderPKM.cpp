/**
 * @file textureLoaderPKM.h
 * @brief Loader for PKM compressed texture
 **/

#include "texture.h"
#include "textureLoaderPKM.h"
#include "textureCompressed.h"
#include "fileUtils.h"
#include "glbase.h"
#include "macros.h"
#include GL2EXT_H

#include <string.h>

Texture* TextureLoaderPKM::load(byte* data, int length, bool repeat, bool mipmap)
{
  // PKMタイプをセット
  int dataLength = length-16;

  int width = data[ 8 ] * 256 + data[ 9 ];
  int height = data[ 10 ] * 256 + data[ 11 ];
  GLuint format = GL_ETC1_RGB8_OES;

  trace("Image : %d bytes, %dx%d", dataLength, width, height);

  byte* imageData = new byte[dataLength];
  memcpy(imageData, data+16, dataLength);

  return new TextureCompressed(width, height, format, repeat, 
			       true, imageData, mipmap, 4);
}
