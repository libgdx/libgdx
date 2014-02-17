/*******************************************************************************
 * Copyright (c) 2013, Daniel Murphy
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * 	* Redistributions of source code must retain the above copyright notice,
 * 	  this list of conditions and the following disclaimer.
 * 	* Redistributions in binary form must reproduce the above copyright notice,
 * 	  this list of conditions and the following disclaimer in the documentation
 * 	  and/or other materials provided with the distribution.
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
/**
 * 
 * 1:13:11 AM, Jul 17, 2009
 */

package org.jbox2d.common;

// updated to rev 100
/** This is the viewport transform used from drawing. Use yFlip if you are drawing from the top-left corner.
 * @author daniel */
public interface IViewportTransform {

	/** @return if the transform flips the y axis */
	public boolean isYFlip ();

	/** @param yFlip if we flip the y axis when transforming */
	public void setYFlip (boolean yFlip);

	/** This is the half-width and half-height. This should be the actual half-width and half-height, not anything transformed or
	 * scaled. Not a copy.
	 * @return */
	public Vec2 getExtents ();

	/** This sets the half-width and half-height. This should be the actual half-width and half-height, not anything transformed or
	 * scaled.
	 * @param argExtents */
	public void setExtents (Vec2 argExtents);

	/** This sets the half-width and half-height of the viewport. This should be the actual half-width and half-height, not anything
	 * transformed or scaled.
	 * @param argHalfWidth
	 * @param argHalfHeight */
	public void setExtents (float argHalfWidth, float argHalfHeight);

	/** center of the viewport. Not a copy.
	 * @return */
	public Vec2 getCenter ();

	/** sets the center of the viewport.
	 * @param argPos */
	public void setCenter (Vec2 argPos);

	/** sets the center of the viewport.
	 * @param x
	 * @param y */
	public void setCenter (float x, float y);

	/** Sets the transform's center to the given x and y coordinates, and using the given scale.
	 * @param x
	 * @param y
	 * @param scale */
	public void setCamera (float x, float y, float scale);

	/** Transforms the given directional vector by the viewport transform (not positional)
	 * @param argVec
	 * @param argOut */
	public void getWorldVectorToScreen (Vec2 argWorld, Vec2 argScreen);

	/** Transforms the given directional screen vector back to the world direction.
	 * @param argVec
	 * @param argOut */
	public void getScreenVectorToWorld (Vec2 argScreen, Vec2 argWorld);

	/** takes the world coordinate (argWorld) puts the corresponding screen coordinate in argScreen. It should be safe to give the
	 * same object as both parameters.
	 * @param argWorld
	 * @param argScreen */
	public void getWorldToScreen (Vec2 argWorld, Vec2 argScreen);

	/** takes the screen coordinates (argScreen) and puts the corresponding world coordinates in argWorld. It should be safe to give
	 * the same object as both parameters.
	 * @param argScreen
	 * @param argWorld */
	public void getScreenToWorld (Vec2 argScreen, Vec2 argWorld);
}
