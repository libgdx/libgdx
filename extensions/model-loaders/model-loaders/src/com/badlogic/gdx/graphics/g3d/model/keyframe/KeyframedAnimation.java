package com.badlogic.gdx.graphics.g3d.model.keyframe;

import com.badlogic.gdx.graphics.g3d.model.Animation;

public class KeyframedAnimation extends Animation {		
	public final float frameDuration;
	public final Keyframe[] keyframes;
	
	public KeyframedAnimation(String name, float frameDuration, Keyframe[] keyframes) {
		super(name, frameDuration * keyframes.length);
		this.frameDuration = frameDuration;
		this.keyframes = keyframes;
	}
}
