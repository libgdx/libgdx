/**
 * @file textureLoaderPKMBMP.h
 * @brief Loader for PKM compressed texture
 **/
#pragma once

#include "textureLoader.h"

// Forward
class Texture;

/**
 * PKMテクスチャー(BMPに解凍)
 **/
class TextureLoaderPKMBMP : public TextureLoader
{
public:
  Texture* load(byte* data, int length, bool repeat, bool mipmap);
};
