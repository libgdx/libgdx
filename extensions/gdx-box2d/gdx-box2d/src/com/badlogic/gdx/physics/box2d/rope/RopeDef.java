
package com.badlogic.gdx.physics.box2d.rope;

import com.badlogic.gdx.math.Vector2;

public class RopeDef {
	/** Must not be null when used */
	public float[] masses;
	public RopeTuning tuning;

	/** Must not be null when used */
	public Vector2[] vertices;
	public Vector2 position;
	public Vector2 gravity;

	public RopeDef () {
		position = new Vector2();
		vertices = null;
		masses = null;
		gravity = new Vector2();
		tuning = new RopeTuning();
	}
}
