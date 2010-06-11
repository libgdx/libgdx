package com.badlogic.gdx.helloworld;

import com.badlogic.gdx.backends.desktop.JoglApplication;

public class HelloWorldDesktop 
{
	public static void main( String[] argv )
	{
		JoglApplication app = new JoglApplication( "Hello World", 480, 320, false );
		app.getGraphics().setRenderListener( new HelloWorld() );
	}
}
