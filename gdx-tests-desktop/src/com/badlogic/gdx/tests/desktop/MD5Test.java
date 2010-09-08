package com.badlogic.gdx.tests.desktop;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import com.badlogic.gdx.graphics.loaders.md5.MD5Loader;

public class MD5Test 
{
	public static void main( String[] argv ) throws FileNotFoundException
	{
		MD5Loader.loadModel( new FileInputStream( "data/zfat.md5mesh" ) );
	}
}
