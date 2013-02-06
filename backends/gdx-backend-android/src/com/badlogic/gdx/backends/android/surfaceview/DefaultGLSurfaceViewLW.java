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

import android.service.wallpaper.WallpaperService.Engine;
import android.util.AttributeSet;


public class DefaultGLSurfaceViewLW extends GLBaseSurfaceViewLW {

	final ResolutionStrategy resolutionStrategy;

	public DefaultGLSurfaceViewLW(Engine engine,
			ResolutionStrategy resolutionStrategy) {
		super(engine);
		this.resolutionStrategy = resolutionStrategy;
	}

	public DefaultGLSurfaceViewLW(Engine engine, AttributeSet attrs,
			ResolutionStrategy resolutionStrategy) {
		super(engine, attrs);
		this.resolutionStrategy = resolutionStrategy;
	}

	// @Override
	// protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
	// ResolutionStrategy.MeasuredDimension measures =
	// resolutionStrategy.calcMeasures(widthMeasureSpec, heightMeasureSpec);
	// setMeasuredDimension(measures.width, measures.height);
	// }

}