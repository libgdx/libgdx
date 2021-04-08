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

package com.badlogic.gdx.tools.flame;

import com.badlogic.gdx.graphics.g3d.particles.ParticleSorter;

enum SortMode {
	None("None", new ParticleSorter.None()), Distance("Distance", new ParticleSorter.Distance());

	public static SortMode find (ParticleSorter sorter) {
		Class type = sorter.getClass();
		for (SortMode wrapper : values()) {
			if (wrapper.sorter.getClass() == type) return wrapper;
		}
		return null;
	}

	public String desc;
	public ParticleSorter sorter;

	SortMode (String desc, ParticleSorter sorter) {
		this.sorter = sorter;
		this.desc = desc;
	}

	@Override
	public String toString () {
		return desc;
	}
}
