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

package com.badlogic.gdx.maps;

/** Map layer containing a set of MapLayers, objects and properties */
public class MapGroupLayer extends MapLayer {

	private MapLayers layers = new MapLayers();

	/**
	 *
	 * @return the {@link MapLayers} owned by this group
	 */
	public MapLayers getLayers () {
		return layers;
	}

	@Override
	public void invalidateRenderOffset () {
		super.invalidateRenderOffset();
		for (int i = 0; i < layers.size(); i++) {
			MapLayer child = layers.get(i);
			child.invalidateRenderOffset();
		}
	}
}
