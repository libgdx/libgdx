/**
 * @file textureLoaderPVR.h
 * @brief Loader for PVRTC compressed texture
 **/
#pragma once

#include "textureLoader.h"

// Forward
class Texture;

/**
 * Loader for PVRTCテクスチャー
 **/
class TextureLoaderPVR : public TextureLoader
{
 public:
  Texture* load(byte* data, int length, bool repeat, bool mipmap);  
};
