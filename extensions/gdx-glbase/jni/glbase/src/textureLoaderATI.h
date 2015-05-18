/**
 * @file textureLoaderATI.h
 * @brief Loader for ATITC compressed texture
 **/
#pragma once

#include "textureLoader.h"

class Texture;

/**
 * ATITCテクスチャー
 **/
class TextureLoaderATI : public TextureLoader
{
public:
  Texture* load(byte* data, int length, bool repeat, bool mipmap);
};

