package com.badlogic.gdx.graphics.g3d.model.keyframe;

public class Keyframe {
	public final float timeStamp;
	public final int animatedComponents;
	public final float[] vertices;
	
	public Keyframe(float timeStamp, int animatedComponents, float[] vertices) {
		this.timeStamp = timeStamp;
		this.animatedComponents = animatedComponents;
		this.vertices = vertices;
	}
}
