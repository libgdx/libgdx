/**
 * @file textureCompressed.cpp
 * @brief Compressed texture module implementation
 **/
#include "textureCompressed.h"
#include "glbase.h"
#include "macros.h"

TextureCompressed::TextureCompressed(int width, int height, int format, bool filterLinear,
				     bool repeat, byte* data, bool mipmap, int bpp )
  : Texture( width, height, format, -1, filterLinear, repeat, data, mipmap, bpp )
{
}

TextureCompressed::~TextureCompressed()
{
}

void TextureCompressed::loadDataAndMipMaps( GLenum textureMode ) {  
  // 第一レベル
  GLOP( glCompressedTexImage2D( textureMode, 0, format, width, height, 
				0, (width*width*bpp)>>3, imageData ) );
  trace( "loaded compressed texture with glid %d, width %d, height %d, format %X, repeat %d", glid, width, height, format, repeat );

  // ミップマップあり
  if( mipmap ) {
    // PKMの場合は、データバッファーに全てのミップマップが格納されている仮定
    int size = width;
    int level = 0;
    byte* readPos = imageData + ((width*width*bpp) >> 3);
    int levelSize = MAX2( 8, (size*size*bpp) >> 3 );
    

    while( size > 1 ){
      level++;
      size /= 2;
      readPos += levelSize;
      levelSize = MAX2( 8, (size*size*bpp) >> 3 );

      GLOP( glCompressedTexImage2D( textureMode, level, format, width, height, 
				    0, levelSize, readPos ) );
    }
  }
}


  
bool TextureCompressed::copySubImage(int offsetX, int offsetY, int width, int height, 
				     int format, int pixelFormat, byte *subData)
{
  // TODO
}
