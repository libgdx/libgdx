package com.badlogic.gdx.graphics.g3d.decals;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;

/**
 * Handles a single group's pre and post render arrangements. Can be plugged into {@link PluggableGroupStrategy} to build
 * modular {@link GroupStrategy GroupStrategies}.
 */
public interface GroupPlug {
	public void beforeGroup(Array<Decal> contents);
	public void afterGroup();
}
