package com.btdstudio.glbase;


public class IMergeGroup extends IObject {
	// @off
	/*JNI
	    #include "ImergeGroup.h"
	    #include "IpolygonMap.h"
	    #include "matrix.h"
	    #include "types.h"
    */
	
	IMergeGroup(long handle) {
		super(handle);
	}
	
	public void startGroup(){
		startGroup(handle);
	}
	
	public void endGroup(){
		endGroup(handle);
	}
	
	public void addPolygonMap(IPolygonMap pm, int targetPmId, float x, float y, float z){
		addPolygonMap(handle, pm.handle, targetPmId, x, y, z);
	}
	
	public void addObject(IObject obj, int targetPmId, float x, float y, float z){
		addObject(handle, obj.handle, targetPmId, x, y, z);
	}
	
	public void addObject(IObject obj, int targetPmId, Matrix matrix){
		addObject(handle, obj.handle, targetPmId, matrix.handle);
	}
	
	public void addTriangles(int vertices, int triangles, float[] points, 
		    short[] indices, float[] uvs){
		addTriangles(handle, vertices, triangles, points, indices, uvs);
	}
	
	public void addSprite(float x, float y, float z, int texture){
		addSprite(handle, x, y, z, texture);
	}
	
	public void addSprite(float x, float y, float z, float w, float h, int texture,
			 float sx, float sy, float sw, float sh){
		addSprite(x, y, z, w, h, texture, sx, sy, sw, sh, false);
	}
	
	public void addSprite(float x, float y, float z, float w, float h, int texture,
			 float sx, float sy, float sw, float sh, boolean flipV){
		addSprite(handle, x, y, z, w, h, texture, sx, sy, sw, sh, flipV);
	}
	
	public void addSprite(int texture, float sx, float sy, 
			 float sw, float sh, Matrix transform){
		addSprite(texture, sx, sy, sw, sh, transform, false);
	}

	public void addSprite(int texture, float sx, float sy, 
			 float sw, float sh, Matrix transform, boolean flipV){
		addSprite(handle, texture, sx, sy, sw, sh, transform.handle, flipV);
	}
	
	public void addSpriteUV(float x, float y, float z, float w, float h, 
			   float u, float v, float u2, float v2){
		addSpriteUV(handle, x, y, z, w, h, u, v, u2, v2);
	}
	
	public void addQuad(SimpleVertex v1, SimpleVertex v2, SimpleVertex v3, SimpleVertex v4, float z){
		addQuad(handle,
				v1.x, v1.y, v1.u, v1.v, 
				v2.x, v2.y, v2.u, v2.v,
				v3.x, v3.y, v3.u, v3.v,
				v4.x, v4.y, v4.u, v4.v, 
				z
				);
	}
	
	// --- PRIVATE --- 
	
	private static native void startGroup(long handle); /*
		((IMergeGroup*)handle)->startGroup();
	*/
	
	private static native void endGroup(long handle); /*
		((IMergeGroup*)handle)->endGroup();
	*/
	
	private static native void addPolygonMap(long handle, long pmHandle, int targetPmId, float x, float y, float z); /*
		IPolygonMap* pm = (IPolygonMap*)pmHandle;
		((IMergeGroup*)handle)->addPolygonMap(pm, targetPmId, x, y, z);
	*/
	
	private static native void addObject(long handle, long objHandle, int targetPmId, float x, float y, float z); /*
		IObject* obj = (IObject*)objHandle;
		((IMergeGroup*)handle)->addObject(obj, targetPmId, x, y, z);
	*/
	
	private static native void addObject(long handle, long objHandle, int targetPmId, long matrixHandle); /*
		Matrix* matrix = (Matrix*)matrixHandle;
		
		IObject* obj = (IObject*)objHandle;
		((IMergeGroup*)handle)->addObject(obj, targetPmId, matrix);
	*/
	
	private static native void addTriangles(long handle, int vertices, int triangles, float[] points, 
		    short[] indices, float[] uvs); /*
		((IMergeGroup*)handle)->addTriangles(vertices, triangles, points, (const unsigned short*)indices, uvs);
	*/
	
	private static native void addSprite(long handle, float x, float y, float z, int texture); /*
		((IMergeGroup*)handle)->addSprite(x, y, z, texture);
	*/
	
	private static native void addSprite(long handle, float x, float y, float z, float w, float h, int texture,
			 float sx, float sy, float sw, float sh, boolean flipV); /*
		((IMergeGroup*)handle)->addSprite(x, y, z, w, h, texture, sx, sy, sw, sh, flipV);
	*/
	
	private static native void addSprite(long handle, int texture, float sx, float sy, 
			 float sw, float sh, long matrixHandle, boolean flipV); /*
		Matrix* transform = (Matrix*)matrixHandle;
		((IMergeGroup*)handle)->addSprite(texture, sx, sy, sw, sh, transform, flipV);
	*/
	
	private static native void addSpriteUV(long handle, float x, float y, float z, float w, float h, 
			   float u, float v, float u2, float v2); /*
		((IMergeGroup*)handle)->addSpriteUV(x, y, z, w, h, u, v, u2, v2);
	*/
	
	private static native void addQuad(long handle,
			float v1x, float v1y, float v1u, float v1v,
			float v2x, float v2y, float v2u, float v2v,
			float v3x, float v3y, float v3u, float v3v,
			float v4x, float v4y, float v4u, float v4v,
			float z
			); /*
		SimpleVertex v1 = { v1x, v1y, v1u, v1v };
		SimpleVertex v2 = { v2x, v2y, v2u, v2v };
		SimpleVertex v3 = { v3x, v3y, v3u, v3v };
		SimpleVertex v4 = { v4x, v4y, v4u, v4v };
		
		((IMergeGroup*)handle)->addQuad(&v1, &v2, &v3, &v4, z);
	*/
}
