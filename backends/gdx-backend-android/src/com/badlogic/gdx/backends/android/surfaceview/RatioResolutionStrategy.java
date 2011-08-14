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

package com.badlogic.gdx.backends.android.surfaceview;

import android.view.View;

/** This {@link ResolutionStrategy} will maintain a given aspect ratio and stretch the GLSurfaceView to the maximum available
 * screen size.
 * 
 * @author christoph widulle */
public class RatioResolutionStrategy implements ResolutionStrategy {

	private final float ratio;

	public RatioResolutionStrategy (float ratio) {
		this.ratio = ratio;
	}

	public RatioResolutionStrategy (final float width, final float height) {
		this.ratio = width / height;
	}

	@Override
	public MeasuredDimension calcMeasures (int widthMeasureSpec, int heightMeasureSpec) {

		final int specWidth = View.MeasureSpec.getSize(widthMeasureSpec);
		final int specHeight = View.MeasureSpec.getSize(heightMeasureSpec);

		final float desiredRatio = ratio;
		final float realRatio = (float)specWidth / specHeight;

		int width;
		int height;
		if (realRatio < desiredRatio) {
			width = specWidth;
			height = Math.round(width / desiredRatio);
		} else {
			height = specHeight;
			width = Math.round(height * desiredRatio);
		}

		return new MeasuredDimension(width, height);
	}
}
