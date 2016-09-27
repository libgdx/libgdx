/*******************************************************************************
 * Copyright (c) 2013, Daniel Murphy
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
package org.jbox2d.common;

/**
 * This is the viewport transform used from drawing. Use yFlip if you are drawing from the top-left
 * corner.
 * 
 * @author Daniel
 */
public interface IViewportTransform {

  /**
   * @return if the transform flips the y axis
   */
  boolean isYFlip();

  /**
   * @param yFlip if we flip the y axis when transforming
   */
  void setYFlip(boolean yFlip);

  /**
   * This is the half-width and half-height. This should be the actual half-width and half-height,
   * not anything transformed or scaled. Not a copy.
   */
  Vec2 getExtents();

  /**
   * This sets the half-width and half-height. This should be the actual half-width and half-height,
   * not anything transformed or scaled.
   */
  void setExtents(Vec2 extents);

  /**
   * This sets the half-width and half-height of the viewport. This should be the actual half-width
   * and half-height, not anything transformed or scaled.
   */
  void setExtents(float halfWidth, float halfHeight);

  /**
   * center of the viewport. Not a copy.
   */
  Vec2 getCenter();

  /**
   * sets the center of the viewport.
   */
  void setCenter(Vec2 pos);

  /**
   * sets the center of the viewport.
   */
  void setCenter(float x, float y);

  /**
   * Sets the transform's center to the given x and y coordinates, and using the given scale.
   */
  void setCamera(float x, float y, float scale);

  /**
   * Transforms the given directional vector by the viewport transform (not positional)
   */
  void getWorldVectorToScreen(Vec2 world, Vec2 screen);


  /**
   * Transforms the given directional screen vector back to the world direction.
   */
  void getScreenVectorToWorld(Vec2 screen, Vec2 world);
  
  Mat22 getMat22Representation();


  /**
   * takes the world coordinate (world) puts the corresponding screen coordinate in screen. It
   * should be safe to give the same object as both parameters.
   */
  void getWorldToScreen(Vec2 world, Vec2 screen);


  /**
   * takes the screen coordinates (screen) and puts the corresponding world coordinates in world. It
   * should be safe to give the same object as both parameters.
   */
  void getScreenToWorld(Vec2 screen, Vec2 world);

  /**
   * Multiplies the viewport transform by the given Mat22
   */
  void mulByTransform(Mat22 transform);
}
