package com.btdstudio.glbase;

public class IPolygonMap {
	// @off
	/*JNI
	 	#include "IpolygonMap.h"
	    #include <string.h>
    */	
	
	long handle;
	
	// Refs
	private ISurface surfaceRef = new ISurface(-1);
	
	IPolygonMap(long handle) {
		this.handle = handle;
	}

	public float[] getUvsBuffer(){
		float[] res = new float[getUvsNum(handle)*2];
		for( int i=0; i<res.length; i++ ){
			res[i] = getUvsBuffer(handle, i);
		}
		return res;
	}
	
	public short[] getPolygonsBuffer(){
		short[] res = new short[getPolygonsNum(handle)];
		for( int i=0; i<res.length; i++ ){
			res[i] = getPolygonsBuffer(handle, i);
		}
		return res;
	}
	
	public void commitUVsChanges(float[] uvs){
		commitUVsChanges(handle, uvs);
	}
	
	public void commitPolygonsChanges(short[] polygons){
		commitPolygonsChanges(handle, polygons);
	}
	
	public ISurface getSurface(){
		surfaceRef.handle = getSurface(handle);
		return surfaceRef;
	}
	
	public String getSurfaceName(){
		char[] chars = new char[getSurfaceNameLength(handle)];
		for( int i=0; i<chars.length; i++ ){
			chars[i] = getSurfaceName(handle, i);
		}
		
		return new String(chars);
	}
	
	
	// --- PRIVATE ---
	
	private static native long getSurface(long handle); /*
		return (long long)((IPolygonMap*)handle)->getSurface();
	*/
	
	private static native int getUvsNum(long handle); /*
		return ((IPolygonMap*)handle)->getUvsBufferLength();
	*/
	
	private static native float getUvsBuffer(long handle, int pos); /*
		return ((IPolygonMap*)handle)->getUvsBuffer()[pos];
	*/
	
	private static native int getPolygonsNum(long handle); /*
		return ((IPolygonMap*)handle)->getPolygonsBufferLength();
	*/
	
	private static native short getPolygonsBuffer(long handle, int pos); /*
		return ((IPolygonMap*)handle)->getPolygonsBuffer()[pos];
	*/
	
	private static native void commitUVsChanges(long handle, float[] values); /*
		float* old = ((IPolygonMap*)handle)->getUvsBuffer();
		int numPoints = ((IPolygonMap*)handle)->getUvsBufferLength();
		memcpy(old, values,  numPoints*2*sizeof(float));
		
		((IPolygonMap*)handle)->commitUvsChanges();
	*/
	
	private static native void commitPolygonsChanges(long handle, short[] values); /*
		unsigned short* old = ((IPolygonMap*)handle)->getPolygonsBuffer();
		int numPoints = ((IPolygonMap*)handle)->getPolygonsBufferLength();
		memcpy(old, values,  numPoints*3*sizeof(unsigned short));
		
		((IPolygonMap*)handle)->commitPolygonsChanges();
	*/
	
	private static native int getSurfaceNameLength(long handle); /*
		return strlen(((IPolygonMap*)handle)->getSurfaceName());
	*/
	
	private static native char getSurfaceName(long handle, int pos); /*
		return ((IPolygonMap*)handle)->getSurfaceName()[pos];
	*/
}
