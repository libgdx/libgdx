package com.btdstudio.glbase;

public class ILayer {
	// @off
	/*JNI
	 	#include "types.h"
	    #include "Ilayer.h"
	    #include "arrayList.h"
	    #include "macros.h"
	    #include <string.h>
	    
	    // Temp
	    ArrayList tempList(128);
    */	
	
	long handle;
	
	ILayer(long handle){
		this.handle = handle;
	}
	
	public int getPolygonMapsNum(){
		return getPolygonMapsNum(handle);
	}
	
	public IPolygonMap getPolygonMap(int pos){
		return new IPolygonMap(getPolygonMap(handle, pos));
	}
	
	public int getPointsNum(){
		return getPointsNum(handle);
	}
	
	public float[] getPointsBuffer(){
		float[] res = new float[getPointsNum()*3];
		for( int i=0; i<res.length; i++ ){
			res[i] = getPointsBuffer(handle, i);
		}
		return res;
	}
	
	public float[] getVcolorsBuffer(){
		float[] res = new float[getPointsNum()*4];
		for( int i=0; i<res.length; i++ ){
			res[i] = getVcolorsBuffer(handle, i);
		}
		return res;
	}
	
	public float[] getNormalsBuffer(){
		float[] res = new float[getPointsNum()*3];
		for( int i=0; i<res.length; i++ ){
			res[i] = getNormalsBuffer(handle, i);
		}
		return res;
	}
	
	public byte[] getMatrixIndicesBuffer(){
		byte[] res = new byte[getPointsNum()*4];
		for( int i=0; i<res.length; i++ ){
			res[i] = getMatrixIndicesBuffer(handle, i);
		}
		return res;
	}
	
	public float[] getMatrixWeightsBuffer(){
		float[] res = new float[getPointsNum()*4];
		for( int i=0; i<res.length; i++ ){
			res[i] = getMatrixWeightsBuffer(handle, i);
		}
		return res;
	}
	
	public String getName(){
		char[] chars = new char[getNameLength(handle)];
		for( int i=0; i<chars.length; i++ ){
			chars[i] = getName(handle, i);
		}
		
		return new String(chars);
	}
	
	public float[] getBoundingBox(){
		float[] res = new float[6];
		
		for( int i=0; i<res.length; i++ ){
			res[i] = getBoundingBox(handle, i);
		}
		
		return res;
	}
	
	public void setMatrixIndicesNames(String[] names){
		for( int i=0; i<names.length; i++ ){
			addMatrixIndicesName(names[i]);
		}
		
		setMatrixIndicesNames(handle);
	}
	
	public void commitPointsChanges(float[] points){
		commitPointsChanges(handle, points);
	}
	
	public void commitVcolorsChanges(float[] vcolors){
		commitVcolorChanges(handle, vcolors);
	}
	
	public void commitNormalsChanges(float[] normals){
		commitNormalsChanges(handle, normals);
	}
	
	public void commitMatrixIndicesChanges(byte[] indices){
		commitMatrixIndicesChanges(handle, indices);
	}
	
	public void commitWeightsChanges(float[] weights){
		commitMatrixWeightsChanges(handle, weights);
	}
	
	// --- PRIVATE ---
	
	private static native void addMatrixIndicesName(String name); /*
		tempList.add(strdup2(name));
	*/
	
	private static native void setMatrixIndicesNames(long handle); /*
		((ILayer*)handle)->setMatrixIndicesNames(&tempList);
		
		// Clear array
		for( int i=0; i<tempList.getSize(); i++ ){
			delete (char*)tempList.get(i);
		}
		
		tempList.clear();
	*/
	
	private static native int getNameLength(long handle); /*
		return strlen(((ILayer*)handle)->getName());
	*/
	
	private static native char getName(long handle, int pos); /*
		return ((ILayer*)handle)->getName()[pos];
	*/
	
	private static native float getBoundingBox(long handle, int pos); /*
		return ((ILayer*)handle)->getBoundingBox()[pos];
	*/
	
	private static native int getPointsNum(long handle); /*
		return ((ILayer*)handle)->getPointsNum();
	*/
	
	private static native int getPolygonMapsNum(long handle); /*
		return ((ILayer*)handle)->getPolygonMaps()->getSize();
	*/
	
	private static native long getPolygonMap(long handle, int pos); /*
		return (long long)((ILayer*)handle)->getPolygonMaps()->get(pos);
	*/
	
	private static native float getPointsBuffer(long handle, int pos); /*
		return ((ILayer*)handle)->getPointsBuffer()[pos];
	*/
	
	private static native float getVcolorsBuffer(long handle, int pos); /*
		return ((ILayer*)handle)->getVcolorsBuffer()[pos];
	*/
	
	private static native float getNormalsBuffer(long handle, int pos); /*
		return ((ILayer*)handle)->getNormalsBuffer()[pos];
	*/
	
	private static native byte getMatrixIndicesBuffer(long handle, int pos); /*
		return ((ILayer*)handle)->getMatrixIndicesBuffer()[pos];
	*/
	
	private static native float getMatrixWeightsBuffer(long handle, int pos); /*
		return ((ILayer*)handle)->getMatrixWeightsBuffer()[pos];
	*/
	
	private static native void commitPointsChanges(long handle, float[] points); /*
		float* oldPoints = ((ILayer*)handle)->getPointsBuffer();
		int numPoints = ((ILayer*)handle)->getPointsNum();
		memcpy(oldPoints, points,  numPoints*3*sizeof(float));
		
		((ILayer*)handle)->commitPointsChanges();
	*/
	
	private static native void commitVcolorChanges(long handle, float[] values); /*
		float* old = ((ILayer*)handle)->getVcolorsBuffer();
		int numPoints = ((ILayer*)handle)->getPointsNum();
		memcpy(old, values,  numPoints*4*sizeof(float));
		
		((ILayer*)handle)->commitVcolorsChanges();
	*/
	
	private static native void commitNormalsChanges(long handle, float[] values); /*
		float* old = ((ILayer*)handle)->getNormalsBuffer();
		int numPoints = ((ILayer*)handle)->getPointsNum();
		memcpy(old, values,  numPoints*3*sizeof(float));
		
		((ILayer*)handle)->commitNormalsChanges();
	*/
	
	private static native void commitMatrixIndicesChanges(long handle, byte[] values); /*
		byte* old = ((ILayer*)handle)->getMatrixIndicesBuffer();
		int numPoints = ((ILayer*)handle)->getPointsNum();
		memcpy(old, values,  numPoints*4*sizeof(byte));
		
		((ILayer*)handle)->commitMatrixIndicesChanges();
	*/
	
	private static native void commitMatrixWeightsChanges(long handle, float[] values); /*
		float* old = ((ILayer*)handle)->getMatrixWeightsBuffer();
		int numPoints = ((ILayer*)handle)->getPointsNum();
		memcpy(old, values,  numPoints*4*sizeof(float));
		
		((ILayer*)handle)->commitMatrixWeightsChanges();
	*/
}
