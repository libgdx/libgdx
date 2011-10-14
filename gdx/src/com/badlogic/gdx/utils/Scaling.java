
package com.badlogic.gdx.utils;

import com.badlogic.gdx.math.Vector2;

public enum Scaling {
	fill, fit, stretch, stretchX, stretchY, none;

	static private Vector2 temp = new Vector2();

	public Vector2 apply (float sourceWidth, float sourceHeight, float targetWidth, float targetHeight) {
		switch (this) {
		case fill: {
			float targetRatio = targetHeight / targetWidth;
			float sourceRatio = sourceHeight / sourceWidth;
			float scale = targetRatio > sourceRatio ? targetWidth / sourceWidth : targetHeight / sourceHeight;
			temp.x = sourceWidth * scale;
			temp.y = sourceHeight * scale;
			break;
		}
		case fit: {
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
