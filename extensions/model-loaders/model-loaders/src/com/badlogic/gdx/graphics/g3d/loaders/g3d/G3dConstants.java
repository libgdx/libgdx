
package com.badlogic.gdx.graphics.g3d.loaders.g3d;

public class G3dConstants {
	// Version info for file format
	public static final byte MAJOR_VERSION = 0;
	public static final byte MINOR_VERSION = 1;

	// Unique IDs for chunk declarations
	public static final int G3D_ROOT = 0x4733441A;
	public static final int VERSION_INFO = 0x0001;
	public static final int STILL_MODEL = 0x1000;
	public static final int STILL_SUBMESH = 0x1100;
	public static final int KEYFRAMED_MODEL = 0x2000;
	public static final int KEYFRAMED_SUBMESH = 0x2200;
	public static final int VERTEX_LIST = 0x1110;
	public static final int INDEX_LIST = 0x1111;
	public static final int VERTEX_ATTRIBUTES = 0x1120;
	public static final int VERTEX_ATTRIBUTE = 0x1121;	
}
