/**
 * @file textureLoaderCTES.h
 * @brief Loader for ATITC compressed texture / CTES header
 **/
#pragma once

#include "textureLoader.h"

class Texture;

/**
 * ATITC/CTESテクスチャー
 **/
class TextureLoaderCTES : public TextureLoader
{
public: 
  Texture* load(byte* data, int length, bool repeat, bool mipmap);
};
