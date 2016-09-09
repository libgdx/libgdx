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

package com.badlogic.gdx.graphics.g3d;

import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

/** Returns a list of {@link Renderable} instances to be rendered by a {@link ModelBatch}.
 * @author badlogic */
public interface RenderableProvider {
	/** Returns {@link Renderable} instances. Renderables are obtained from the provided {@link Pool} and added to the provided
	 * array. The Renderables obtained using {@link Pool#obtain()} will later be put back into the pool, do not store them
	 * internally. The resulting array can be rendered via a {@link ModelBatch}.
	 * @param renderables the output array
	 * @param pool the pool to obtain Renderables from */
	public void getRenderables (Array<Renderable> renderables, Pool<Renderable> pool);
}
