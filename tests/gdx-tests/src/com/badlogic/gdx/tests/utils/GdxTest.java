package com.badlogic.gdx.tests.utils;

import com.badlogic.gdx.ApplicationListener;

public abstract class GdxTest implements ApplicationListener
{
	public abstract boolean needsGL20( );		
	public void create( ) { };
	public void resume( ) { };
	public void render( ) { };
	public void resize(int width, int height) { };
	public void pause( ) { };
	public void dispose( ) { };
}
