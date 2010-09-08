package com.badlogic.gdx.graphics.loaders.md5;

import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

public class MD5Joint 
{
	public String name;
	public int parent;
	
	public final Vector3 pos = new Vector3( );
	public final Quaternion orient = new Quaternion( 0, 0, 0, 0 );
}
