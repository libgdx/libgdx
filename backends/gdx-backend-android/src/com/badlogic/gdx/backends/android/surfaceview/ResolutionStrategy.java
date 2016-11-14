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

/** Will manipulate the GLSurfaceView. Gravity is always center. The width and height of the View will be determined by the
 * classes implementing {@link ResolutionStrategy}.
 * 
 * @author christoph widulle */
public interface ResolutionStrategy {

	public MeasuredDimension calcMeasures (final int widthMeasureSpec, final int heightMeasureSpec);

	public static class MeasuredDimension {
		public final int width;
		public final int height;

		public MeasuredDimension (int width, int height) {
			this.width = width;
			this.height = height;
		}

	}

}
