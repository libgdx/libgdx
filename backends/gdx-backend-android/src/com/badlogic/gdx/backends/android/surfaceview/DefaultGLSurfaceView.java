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

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

public class DefaultGLSurfaceView extends GLSurfaceView {

	final ResolutionStrategy resolutionStrategy;

	public DefaultGLSurfaceView (Context context, ResolutionStrategy resolutionStrategy) {
		super(context);
		this.resolutionStrategy = resolutionStrategy;
	}

	public DefaultGLSurfaceView (Context context, AttributeSet attrs, ResolutionStrategy resolutionStrategy) {
		super(context, attrs);
		this.resolutionStrategy = resolutionStrategy;
	}

	@Override
	protected void onMeasure (int widthMeasureSpec, int heightMeasureSpec) {
		ResolutionStrategy.MeasuredDimension measures = resolutionStrategy.calcMeasures(widthMeasureSpec, heightMeasureSpec);
		setMeasuredDimension(measures.width, measures.height);
	}
}
