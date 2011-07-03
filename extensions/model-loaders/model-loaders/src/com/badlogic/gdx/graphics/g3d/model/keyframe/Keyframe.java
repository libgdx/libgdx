package com.badlogic.gdx.graphics.g3d.model.keyframe;

public class Keyframe {
	public final float timeStamp;	
	public final float[] vertices;
	
	public Keyframe(float timeStamp, float[] vertices) {
		this.timeStamp = timeStamp;		
		this.vertices = vertices;
	}
}
