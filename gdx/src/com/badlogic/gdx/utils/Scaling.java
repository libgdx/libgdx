
package com.badlogic.gdx.utils;

import com.badlogic.gdx.math.Vector2;

/** @author Nathan Sweet */
public enum Scaling {
	/** Scales the source to fit the target while keeping the same aspect ratio. This may cause the source to be smaller than the
	 * target in one dimension. */
	fit,
	/** Scales the source to completely fill the target while keeping the same aspect ratio. This may cause the source to be larger
	 * than the target in one dimension. */
	fill,
	/** Scales the source to completely fill the target. This may cause the source to not keep the same aspect ratio. */
	stretch,
	/** Scales the source to completely fill the target in the x direction, without changing the y direction. This may cause the
	 * source to not keep the same aspect ratio. */
	stretchX,
	/** Scales the source to completely fill the target in the y direction, without changing the x direction. This may cause the
	 * source to not keep the same aspect ratio. */
	stretchY,
	/** The source is not scaled. */
	none;

	static private Vector2 temp = new Vector2();

	/** Returns the size of the source scaled to the target. Note the same Vector2 instance is always returned and should never be
	 * cached. */
	public Vector2 apply (float sourceWidth, float sourceHeight, float targetWidth, float targetHeight) {
		switch (this) {
		case fit: {
			float targetRatio = targetHeight / targetWidth;
			float sourceRatio = sourceHeight / sourceWidth;
			float scale = targetRatio > sourceRatio ? targetWidth / sourceWidth : targetHeight / sourceHeight;
			temp.x = sourceWidth * scale;
			temp.y = sourceHeight * scale;
			break;
		}
		case fill: {
			float targetRatio = targetHeight / targetWidth;
			float sourceRatio = sourceHeight / sourceWidth;
			float scale = targetRatio < sourceRatio ? targetWidth / sourceWidth : targetHeight / sourceHeight;
			temp.x = sourceWidth * scale;
			temp.y = sourceHeight * scale;
			break;
		}
		case stretch:
			temp.x = targetWidth;
			temp.y = targetHeight;
			break;
		case stretchX:
			temp.x = targetWidth;
			temp.y = sourceHeight;
			break;
		case stretchY:
			temp.x = sourceWidth;
			temp.y = targetHeight;
			break;
		case none:
			temp.x = sourceWidth;
			temp.y = sourceHeight;
			break;
		}
		return temp;
	}
}
