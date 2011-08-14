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
import com.badlogic.gdx.utils.Array;

public class SkeletonJoint {
	public String name;

	public int index;
	public int parentIndex;
	public SkeletonJoint parent;
	public final Array<SkeletonJoint> children = new Array<SkeletonJoint>(1);

	public final Vector3 position = new Vector3();
	public final Quaternion rotation = new Quaternion(new Vector3(0, 1, 0), 0);
	public final Vector3 scale = new Vector3(1, 1, 1);
}