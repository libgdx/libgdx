package com.badlogic.gdxinvaders;

import java.applet.Applet;

import com.badlogic.gdx.backends.applet.AppletApplication;

public class GdxInvadersApplet extends Applet
{
	public void init()
	{
		AppletApplication app = new AppletApplication( this, false, false );		
		app.getGraphics().setRenderListener( new GdxInvaders() );
	}
}
