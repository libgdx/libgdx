package com.badlogic.gdx.tests.jogl;

import com.badlogic.gdx.backends.jogl.JoglApplication;

public class JoglDebugStarter {
	
	public static void main( String[] argv ) {
		new JoglApplication( new com.badlogic.gdx.tests.MeshTest(), "Debug Test", 480, 320, false );		
	}
}
