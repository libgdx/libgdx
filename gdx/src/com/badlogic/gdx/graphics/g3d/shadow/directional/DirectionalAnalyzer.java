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

package com.badlogic.gdx.graphics.g3d.shadow.directional;

import com.badlogic.gdx.graphics.g3d.environment.BaseLight;
import com.badlogic.gdx.math.Frustum;
import com.badlogic.gdx.math.Vector3;

/** Directional Analyzer compute the properties of the camera needed by a directional light
 * @author realitix */
public interface DirectionalAnalyzer {
	/** Compute the good orthographicCamera dimension based on the frustum. Be careful, direction must be normalized.
	 * @param light Current light
	 * @param frustum Frustum of the main camera
	 * @param direction Direction of the directional light */
	public DirectionalResult analyze (BaseLight light, Frustum frustum, Vector3 direction);
}
