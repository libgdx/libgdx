package com.badlogic.gdx.tests.desktop;

import com.badlogic.gdx.backends.desktop.JoglApplication;

public class JoglDebugStarter {
	
	public static void main( String[] argv ) {
		JoglApplication app = new JoglApplication( "Debug Test", 480, 320, true );
		app.getGraphics().setRenderListener( new com.badlogic.gdx.tests.VertexBufferObjectShaderTest() );
	}
}
