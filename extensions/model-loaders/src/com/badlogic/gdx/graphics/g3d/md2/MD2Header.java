
package com.badlogic.gdx.graphics.g3d.md2;


public class MD2Header {
	public int ident; 
	public int version;

	public int skinWidth;
	public int skinHeight;

	public int frameSize; 

	public int numSkins; 
	public int numVertices; 
	public int numSt;
	public int numTris;
	public int numGlcmds;
	public int numFrames;

	public int offsetSkins;
	public int offsetSt;
	public int offsetTris;
	public int offsetFrames;

	public int offsetGlcmds;
	public int offsetEnd;	
	
	@Override public String toString () {
		return "MD2Header [ident=" + ident + ", version=" + version + ", skinWidth=" + skinWidth + ", skinHeight=" + skinHeight
			+ ", frameSize=" + frameSize + ", numSkins=" + numSkins + ", numVertices=" + numVertices + ", numSt=" + numSt
			+ ", numTris=" + numTris + ", numGlcmds=" + numGlcmds + ", numFrames=" + numFrames + ", offsetSkins=" + offsetSkins
			+ ", offsetSt=" + offsetSt + ", offsetTris=" + offsetTris + ", offsetFrames=" + offsetFrames + ", offsetGlcmds="
			+ offsetGlcmds + ", offsetEnd=" + offsetEnd + "]";
	}
}
