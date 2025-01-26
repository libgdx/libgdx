
package com.badlogic.gdx.physics.box2d;

import com.badlogic.gdx.math.Vector2;

public class JniUtil {
	public static float[] arrayOfVec2IntoFloat (Vector2[] vertices) {
		float[] verts = new float[vertices.length * 2];
		for (int i = 0, j = 0; i < vertices.length * 2; i += 2, j++) {
			verts[i] = vertices[j].x;
			verts[i + 1] = vertices[j].y;
		}
		return verts;
	}
}
