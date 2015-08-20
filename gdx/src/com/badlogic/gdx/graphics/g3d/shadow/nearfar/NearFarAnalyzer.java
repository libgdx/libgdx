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

package com.badlogic.gdx.graphics.g3d.shadow.nearfar;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.environment.BaseLight;
import com.badlogic.gdx.math.Vector2;

/**
 * Nearfar Analyzer compute the near and far plane
 * of a camera
 * @author realitix
 */
public interface NearFarAnalyzer {
	/**
	 * Analyze near and far plane for the camera
	 * @param light Current light
	 * @param camera Camera associated with light
	 * @return Vector2
	 */
	public Vector2 analyze(BaseLight light, Camera camera);
}
