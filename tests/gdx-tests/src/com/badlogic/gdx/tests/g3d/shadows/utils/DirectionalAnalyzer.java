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

package com.badlogic.gdx.tests.g3d.shadows.utils;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;

/** Directional Analyzer computes the camera's properties needed by directional light. Implementation should use main camera
 * frustum and scene objects to encompass all casting shadow objects.
 * @author realitix */
public interface DirectionalAnalyzer {
	/** Compute the camera dimension based on directional light. Camera should be an orthographic camera.
	 * @param light Current directional light
	 * @param out Updated camera
	 * @param mainCamera Main Scene camera
	 * @return Camera Camera out for chaining */
	public Camera analyze (DirectionalLight light, Camera out, Camera mainCamera);
}
