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

import com.badlogic.gdx.graphics.g3d.materials.Material;

public class AnimatedModelNode extends StillModelNode implements AnimatedModelInstance {
	public String animation;
	public float time;
	public boolean looping;
	
	public AnimatedModelNode() {
		super();
	}
	
	public AnimatedModelNode(Material[] materials) {
		super(materials);
	}

	@Override
	public String getAnimation () {
		return animation;
	}

	@Override
	public float getAnimationTime () {
		return time;
	}

	@Override
	public boolean isLooping () {
		return looping;
	}

	public AnimatedModelNode copy () {
		final AnimatedModelNode copy = new AnimatedModelNode();
		if (materials != null) {
			final int len = materials.length;
			Material[] mats = new Material[len];
			for (int i = 0; i < len; i++) {
				mats[i] = materials[i].copy();
			}
			copy.materials = mats;
		}
		copy.matrix.set(matrix.val);
		copy.origin.set(origin);
		copy.radius = radius;
		copy.transformedPosition.set(transformedPosition);
		copy.animation = animation;
		copy.time = time;
		copy.looping = looping;
		return copy;
	}

}
