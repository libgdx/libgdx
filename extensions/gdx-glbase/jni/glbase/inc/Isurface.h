/**
 * @file Isurface.h
 * @brief Public interface of a surface of an object
 **/
#pragma once

class ISurface
{
 public:
  /**
   * @return Texture id
   **/
  virtual int getTexture() = 0;
};
