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
import com.badlogic.gdx.graphics.g3d.environment.BaseLight;

/** Select only casting shadow lights. Allows to optimize shadow system.
 * @author realitix */
public interface LightFilter {
	/** Return true if light should be used for shadow computation.
	 * @param light Current light
	 * @param camera Light's camera
	 * @param mainCamera Main scene camera
	 * @return boolean */
	public boolean filter (BaseLight light, Camera camera, Camera mainCamera);
}
