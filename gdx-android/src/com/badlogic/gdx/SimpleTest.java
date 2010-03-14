package com.badlogic.gdx;

import android.content.pm.ActivityInfo;
import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;

public class SimpleTest extends AndroidApplication 
{
	public void onCreate( Bundle bundle )
	{
		super.onCreate( bundle );
		setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE );
		initialize(false);
		getGraphics().setRenderListener( new com.badlogic.gdx.tests.SimpleTest() );
	}
}
