package com.badlogic.gdx.tests.desktop;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import com.badlogic.gdx.backends.desktop.JoglApplication;
import com.badlogic.gdx.graphics.loaders.md5.MD5Loader;
import com.badlogic.gdx.graphics.loaders.md5.MD5Mesh;
import com.badlogic.gdx.graphics.loaders.md5.MD5Model;

public class MD5Test 
{
	public static void main( String[] argv ) throws FileNotFoundException
	{
		JoglApplication app = new JoglApplication( "MD5 Test", 480, 320, false );
		app.getGraphics().setRenderListener( new com.badlogic.gdx.tests.MD5Test() );
	}
}
