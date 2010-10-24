
package com.badlogic.gdx.graphics.loaders.md5;

public class MD5Jni {
	public static native void calculateVertices (float[] skeleton, float[] weights, float[] verticesIn, float[] verticesOut,
		int numVertices);
}
