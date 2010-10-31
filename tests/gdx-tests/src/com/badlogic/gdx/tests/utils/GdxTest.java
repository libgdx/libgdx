package com.badlogic.gdx.tests.utils;

import com.badlogic.gdx.RenderListener;
import com.badlogic.gdx.utils.GdxRuntimeException;

public interface GdxTest extends RenderListener
{
	public boolean needsGL20( );	
}
