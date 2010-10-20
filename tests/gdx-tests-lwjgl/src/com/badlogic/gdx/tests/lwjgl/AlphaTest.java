package com.badlogic.gdx.tests.lwjgl;

import com.badlogic.gdx.backends.desktop.LwjglApplication;

public class AlphaTest 
{
	public static void main( String[] argv )
	{
		LwjglApplication app = new LwjglApplication( "Alpha Test", 480, 320, false );
		app.getGraphics().setRenderListener( new com.badlogic.gdx.tests.AlphaTest() );
	}
}
