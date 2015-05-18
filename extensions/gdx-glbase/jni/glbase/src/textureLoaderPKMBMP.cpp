/**
 * @file textureLoaderPKM.h
 * @brief Loader for PKM compressed texture
 **/

#include "texture.h"
#include "textureLoaderPKMBMP.h"
#include "fileUtils.h"
#include "glbase.h"
#include "macros.h"
#include "etc1_utils.h"

#include <string.h>

Texture* TextureLoaderPKMBMP::load(byte* data, int length, bool repeat, bool mipmap)
{
  // PKMタイプをセット
  int dataLength = length-16;

  int width = data[ 8 ] * 256 + data[ 9 ];
  int height = data[ 10 ] * 256 + data[ 11 ];

  trace("Image : %d bytes, %dx%d", dataLength, width, height);

  // RGB888 output data
  byte* imageData = new byte[width*height*3];

  // Convert!
  int res = etc1_decode_image(data + 16, imageData, width, height, 3, width * 3);
  if( res != 0 ){
	  etrace("Error converting PKM to BMP: %d", res);
  }

  GLuint format = GL_RGB;
  GLuint pixelFormat = GL_UNSIGNED_BYTE;
  int bpp = 24;

  return new Texture(width, height, format, pixelFormat, true,
  		     repeat, imageData, mipmap, bpp);
}
