package com.badlogic.gdx;

import com.badlogic.gdx.backends.desktop.JoglApplication;

public class SpriteBatchShaderTest
{
	public static void main( String[] argv )
	{
		JoglApplication app = new JoglApplication( "SpriteBatch Shader Test", 480, 320, true );
		app.getGraphics().setRenderListener( new com.badlogic.gdx.tests.SpriteBatchShaderTest() );
	}
}
