package com.badlogic.gdx.graphics.g3d.decals;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap.Values;

/**
 * <p>
 * This class provides hooks which are invoked by {@link DecalBatch} to evaluate the group a sprite falls into, as
 * well as to adjust settings before and after rendering a group.
 * </p>
 * <p>
 * A group is identified by an integer. The {@link #beforeGroup(int, com.badlogic.gdx.utils.ObjectMap.Values) beforeGroup()}
 * method provides the strategy with a list of all the decals, which are contained in the group itself, and will be rendered
 * before the associated call to {@link #afterGroup(int)}.<br/>
 * A call to {@code beforeGroup()} is always fallowed by a call to {@code afterGroup()}.<br/>
 * <b>Groups are always invoked based on their ascending int values</b>. Group -10 will be rendered before group -5,
 * group -5 before group 0, group 0 before group 6 and so on.<br/>
 * The call order for a single flush is always {@code beforeGroups(), beforeGroup1(), afterGroup1(), ... beforeGroupN(),
 * afterGroupN(), afterGroups()}.
 * </p>
 * <p>
 * The contents of the {@code beforeGroup()} call are split into multiple {@link Array Array's} based. Each array
 * contains an entry of the group which will be batched and rendered using the entry's material.</br>
 * The contents can be modified at will to realize view frustum culling, depth sorting, ... all based on
 * the requirements of the current group. The batch itself does not change OpenGL settings except for whichever
 * changes are entailed {@link spark.graphics.decal.DecalMaterial#set()}. If the group requires a special shader,
 * blending, etc. {@code beforeGroup()} is the place to apply those, and if needed {@code afterGroup()} the place
 * to clean up.
 * </p>
 */
public interface GroupStrategy {
	/**
	 * Assigns a group to a decal
	 *
	 * @param decal Decal to assign group to
	 * @return group assigned
	 */
	public int decideGroup(Decal decal);

	/**
	 * Invoked directly before rendering the contents of a group
	 *
	 * @param group    Group that will be rendered
	 * @param contents List of entries of arrays containing all the decals in the group
	 */
	public void beforeGroup(int group, Values<Array<Decal>> contents);

	/**
	 * Invoked directly after rendering of a group has completed
	 *
	 * @param group Group which completed rendering
	 */
	public void afterGroup(int group);

	/**
	 * Invoked before rendering any group
	 */
	public void beforeGroups();

	/**
	 * Invoked after having rendered all groups
	 */
	public void afterGroups();
}
