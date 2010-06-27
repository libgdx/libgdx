package com.badlogic.gdx.tests.desktop;

import com.badlogic.gdx.backends.desktop.JoglApplication;

public class Box2DTest 
{
	public static void main( String[] argv )
	{
		JoglApplication app = new JoglApplication( "Box2D Test", 480, 320, false );		
		app.getGraphics().setRenderListener( new com.badlogic.gdx.tests.Box2DTest() );
	}
}
