package com.badlogic.gdx.graphics.g3d.model.data;

import com.badlogic.gdx.math.Vector2;

public class ModelTexture {
	public final static int USAGE_UNKNOWN = 0;
	public final static int USAGE_NONE = 1;
	public final static int USAGE_DIFFUSE = 2;
	public final static int USAGE_EMISSIVE = 3;
	public final static int USAGE_AMBIENT = 4;
	public final static int USAGE_SPECULAR = 5;
	public final static int USAGE_SHININESS = 6;
	public final static int USAGE_NORMAL = 7;
	public final static int USAGE_BUMP = 8;
	public final static int USAGE_TRANSPARENCY = 9;
	public final static int USAGE_REFLECTION = 10;
	
	public String id;
	public String fileName;
	public Vector2 uvTranslation;
	public Vector2 uvScaling;
	public int usage;
}
