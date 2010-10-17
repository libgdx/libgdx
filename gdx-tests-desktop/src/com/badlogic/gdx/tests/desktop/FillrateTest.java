package com.badlogic.gdx.tests.desktop;

import com.badlogic.gdx.backends.desktop.JoglApplication;

public class FillrateTest 
{
	public static void main( String[] argv )
	{
		JoglApplication app = new JoglApplication( "Fillrate Test", 480, 420, false );
		app.getGraphics().setRenderListener( new com.badlogic.gdx.tests.FillrateTest() );
	}
}
