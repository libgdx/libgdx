package com.badlogic.gdx.tests.desktop.box2d;

import com.badlogic.gdx.backends.desktop.JoglApplication;

public class TestCollection 
{
	public static void main( String[] argv )
	{
		JoglApplication app = new JoglApplication( "Simple Test", 640, 480, false );
		app.getGraphics().setRenderListener( new com.badlogic.gdx.tests.box2d.TestCollection( ) );
	}
}
