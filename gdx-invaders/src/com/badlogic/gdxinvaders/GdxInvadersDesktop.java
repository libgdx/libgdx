package com.badlogic.gdxinvaders;

import com.badlogic.gdx.backends.desktop.JoglApplication;

/**
 * Entry point for desktop version of Gdx Invaders. Constructs a JoglApplication
 * and registers the renderer.
 * @author mzechner
 *
 */
public class GdxInvadersDesktop 
{
	public static void main( String[] argv )
	{
		JoglApplication app = new JoglApplication( "Gdx Invaders", 480, 320, false );
		app.getGraphics().setRenderListener( new GdxInvaders() );
	}
}
