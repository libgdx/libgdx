package com.badlogic.gdx.tests.lwjgl;

import com.badlogic.gdx.backends.desktop.LwjglApplication;

public class MD5Test
{
	public static void main( String[] argv )
	{
		LwjglApplication app = new LwjglApplication( "MD5 Test", 480, 320, false );
		app.getGraphics().setRenderListener( new com.badlogic.gdx.tests.MD5Test() );
	}
}
