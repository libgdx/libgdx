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
import com.badlogic.gdx.graphics.g3d.RenderableProvider;
import com.badlogic.gdx.graphics.g3d.environment.BaseLight;

/** Nearfar Analyzer computes near and far plane of camera. It has to call camera.update() after setting values. Updated camera's
 * frustum should encompass all casting shadow objects.
 * @author realitix */
public interface NearFarAnalyzer {
	/** Update near and far plane of camera.
	 * @param light Current light
	 * @param camera Light's camera
	 * @param renderableProviders Renderable providers */
	public <T extends RenderableProvider> void analyze (BaseLight light, Camera camera, Iterable<T> renderableProviders);
}
