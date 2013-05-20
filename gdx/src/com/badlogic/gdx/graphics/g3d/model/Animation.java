package com.badlogic.gdx.graphics.g3d.model;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.utils.Array;

/**
 * An Animation has an id and a list of {@link NodeAnimation} instances. Each
 * NodeAnimation animates a single {@link Node} in the {@link Model}. Every
 * {@link NodeAnimation} is assumed to have the same amount of keyframes,
 * at the same timestamps, as all other node animations for faster keyframe
 * searches.
 * 
 * @author badlogic
 */
public class Animation {
	/** the unique id of the animation **/
	public String id;
	/** the duration in seconds **/
	public float duration;
	/** the animation curves for individual nodes **/
	public Array<NodeAnimation> nodeAnimations = new Array<NodeAnimation>();
}
