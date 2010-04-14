package com.badlogic.gdx;

import com.badlogic.gdx.backends.desktop.JoglApplication;

public class SpriteBatchTest 
{
	public static void main( String[] argv )
	{
		JoglApplication app = new JoglApplication( "SpriteBatch Test", 480, 320, false );
		app.getGraphics().setRenderListener( new com.badlogic.gdx.tests.SpriteBatchTest() );
	}
}
