package com.badlogic.gdx;

import com.badlogic.gdx.backends.android.AndroidApplication;

import android.os.Bundle;

public class LifeCycleTest extends AndroidApplication
{
	public void onCreate( Bundle bundle )
	{
		super.onCreate( bundle );
		this.initialize(false);
		com.badlogic.gdx.tests.LifeCycleTest test = new com.badlogic.gdx.tests.LifeCycleTest();
		this.setApplicationListener( test );
		this.getGraphics().setRenderListener( test );
	}
}
