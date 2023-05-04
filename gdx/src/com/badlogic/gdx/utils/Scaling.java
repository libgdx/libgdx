/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.utils;

import com.badlogic.gdx.math.Vector2;

/** Various scaling types for fitting one rectangle into another.
 * @author Nathan Sweet */
public interface Scaling {
	 Vector2 temp = new Vector2();

	/** Returns the size of the source scaled to the target. Note the same Vector2 instance is always returned and should never be
	 * cached. */
	Vector2 apply (float sourceWidth, float sourceHeight, float targetWidth, float targetHeight);

	/** Scales the source to fit the target while keeping the same aspect ratio. This may cause the source to be smaller than the
	 * target in one direction. */
	Scaling fit = (sourceWidth, sourceHeight, targetWidth, targetHeight) -> {
		float targetRatio = targetHeight / targetWidth;
		float sourceRatio = sourceHeight / sourceWidth;
		float scale = targetRatio > sourceRatio ? targetWidth / sourceWidth : targetHeight / sourceHeight;
		temp.x = sourceWidth * scale;
		temp.y = sourceHeight * scale;
		return temp;
	};

	/** Scales the source to fit the target while keeping the same aspect ratio, but the source is not scaled at all if smaller in
	 * both directions. This may cause the source to be smaller than the target in one or both directions. */
	Scaling contain = (sourceWidth, sourceHeight, targetWidth, targetHeight) -> {
		float targetRatio = targetHeight / targetWidth;
		float sourceRatio = sourceHeight / sourceWidth;
		float scale = targetRatio > sourceRatio ? targetWidth / sourceWidth : targetHeight / sourceHeight;
		if (scale > 1) scale = 1;
		temp.x = sourceWidth * scale;
		temp.y = sourceHeight * scale;
		return temp;
	};

	/** Scales the source to fill the target while keeping the same aspect ratio. This may cause the source to be larger than the
	 * target in one direction. */
	Scaling fill = (sourceWidth, sourceHeight, targetWidth, targetHeight) -> {
		float targetRatio = targetHeight / targetWidth;
		float sourceRatio = sourceHeight / sourceWidth;
		float scale = targetRatio < sourceRatio ? targetWidth / sourceWidth : targetHeight / sourceHeight;
		temp.x = sourceWidth * scale;
		temp.y = sourceHeight * scale;
		return temp;
	};

	/** Scales the source to fill the target in the x direction while keeping the same aspect ratio. This may cause the source to
	 * be smaller or larger than the target in the y direction. */
	Scaling fillX = (sourceWidth, sourceHeight, targetWidth, targetHeight) -> {
		float scale = targetWidth / sourceWidth;
		temp.x = sourceWidth * scale;
		temp.y = sourceHeight * scale;
		return temp;
	};

	/** Scales the source to fill the target in the y direction while keeping the same aspect ratio. This may cause the source to
	 * be smaller or larger than the target in the x direction. */
	Scaling fillY = (sourceWidth, sourceHeight, targetWidth, targetHeight) -> {
		float scale = targetHeight / sourceHeight;
		temp.x = sourceWidth * scale;
		temp.y = sourceHeight * scale;
		return temp;
	};

	/** Scales the source to fill the target. This may cause the source to not keep the same aspect ratio. */
	Scaling stretch = (sourceWidth, sourceHeight, targetWidth, targetHeight) -> {
		temp.x = targetWidth;
		temp.y = targetHeight;
		return temp;
	};

	/** Scales the source to fill the target in the x direction, without changing the y direction. This may cause the source to not
	 * keep the same aspect ratio. */
	Scaling stretchX = (sourceWidth, sourceHeight, targetWidth, targetHeight) -> {
		temp.x = targetWidth;
		temp.y = sourceHeight;
		return temp;
	};

	/** Scales the source to fill the target in the y direction, without changing the x direction. This may cause the source to not
	 * keep the same aspect ratio. */
	Scaling stretchY = (sourceWidth, sourceHeight, targetWidth, targetHeight) -> {
		temp.x = sourceWidth;
		temp.y = targetHeight;
		return temp;
	};

	/** The source is not scaled. */
	Scaling none = (sourceWidth, sourceHeight, targetWidth, targetHeight) -> {
		temp.x = sourceWidth;
		temp.y = sourceHeight;
		return temp;
	};
}
