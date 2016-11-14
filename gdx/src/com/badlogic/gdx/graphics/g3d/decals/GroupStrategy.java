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

package com.badlogic.gdx.graphics.g3d.decals;

import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Array;

/** <p>
 * This class provides hooks which are invoked by {@link DecalBatch} to evaluate the group a sprite falls into, as well as to
 * adjust settings before and after rendering a group.
 * </p>
 * <p>
 * A group is identified by an integer. The {@link #beforeGroup(int, Array) beforeGroup()} method provides the strategy with a list
 * of all the decals, which are contained in the group itself, and will be rendered before the associated call to
 * {@link #afterGroup(int)}.<br/>
 * A call to {@code beforeGroup()} is always followed by a call to {@code afterGroup()}.<br/>
 * <b>Groups are always invoked based on their ascending int values</b>. Group -10 will be rendered before group -5, group -5
 * before group 0, group 0 before group 6 and so on.<br/>
 * The call order for a single flush is always {@code beforeGroups(), beforeGroup1(), afterGroup1(), ... beforeGroupN(),
 * afterGroupN(), afterGroups()}.
 * </p>
 * <p>
 * The contents of the {@code beforeGroup()} call can be modified at will to realize view frustum culling, material & depth
 * sorting, ... all based on the requirements of the current group. The batch itself does not change OpenGL settings except for
 * whichever changes are entailed {@link DecalMaterial#set()}. If the group requires a special shader, blending,
 * {@link #getGroupShader(int)} should return it so that DecalBatch can apply it while rendering the group.
 * </p> */
public interface GroupStrategy {
	/** Returns the shader to be used for the group. Can be null in which case the GroupStrategy doesn't support GLES 2.0
	 * @param group the group
	 * @return the {@link ShaderProgram} */
	public ShaderProgram getGroupShader (int group);

	/** Assigns a group to a decal
	 * 
	 * @param decal Decal to assign group to
	 * @return group assigned */
	public int decideGroup (Decal decal);

	/** Invoked directly before rendering the contents of a group
	 * 
	 * @param group Group that will be rendered
	 * @param contents Array of entries of arrays containing all the decals in the group */
	public void beforeGroup (int group, Array<Decal> contents);

	/** Invoked directly after rendering of a group has completed
	 * 
	 * @param group Group which completed rendering */
	public void afterGroup (int group);

	/** Invoked before rendering any group */
	public void beforeGroups ();

	/** Invoked after having rendered all groups */
	public void afterGroups ();
}
