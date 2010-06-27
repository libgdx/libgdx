package com.badlogic.gdx.tests.desktop;

import com.badlogic.gdx.backends.desktop.JoglApplication;

public class PerformanceTest
{	
	public static void main( String[] argv )
	{
		JoglApplication app = new JoglApplication( "Performance Test", 480, 320, false );
		app.getGraphics().setRenderListener( new com.badlogic.gdx.tests.PerformanceTest() );
	}
}
