package com.badlogic.gdx.graphics.loaders.md5;

import com.badlogic.gdx.Version;

public class MD5Jni 
{
	static
	{
		System.loadLibrary( "gdx-" + Version.VERSION );
	}
	
	public static native void calculateVertices( float[] skeleton, float[] weights, float[] verticesIn, float[] verticesOut, int numVertices );	
}
