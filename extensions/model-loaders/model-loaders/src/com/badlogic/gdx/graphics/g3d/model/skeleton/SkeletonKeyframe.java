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
package com.badlogic.gdx.graphics.g3d.model.skeleton;

import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

public class SkeletonKeyframe {
	public float timeStamp = 0;
	public int parentIndex = -1;
	public final Vector3 position = new Vector3();
	public final Vector3 scale = new Vector3(1, 1, 1);
	public final Quaternion rotation = new Quaternion(0, 0, 0, 1);

	public String toString () {
		return "time: " + timeStamp + ", " + "parent: " + parentIndex + ", " + "position: " + position + ", " + "scale: " + scale
			+ ", " + "rotation: " + rotation;
	}
}