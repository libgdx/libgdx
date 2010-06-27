package com.badlogic.gdx.tests.desktop;

import com.badlogic.gdx.backends.desktop.JoglApplication;

public class VorbisTest 
{
	public static void main( String[] argv )
	{
		JoglApplication app = new JoglApplication( "Vorbis test", 480, 320, false );
		app.getGraphics().setRenderListener( new com.badlogic.gdx.tests.VorbisTest() );
	}
}
