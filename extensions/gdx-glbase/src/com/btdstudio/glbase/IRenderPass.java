package com.btdstudio.glbase;

public class IRenderPass {
	// @off
	/*JNI
	    #include "IrenderPass.h"
	    #include <string.h>
    */	
	
	long handle;
	
	IRenderPass(long handle) {
		this.handle = handle;
	}
	
	public String getMuM(){
		char[] chars = new char[getMumLength(handle)];
		for( int i=0; i<chars.length; i++ ){
			chars[i] = getMum(handle, i);
		}
		
		return new String(chars);
	}
	
	public String getId(){
		char[] chars = new char[getIdLength(handle)];
		for( int i=0; i<chars.length; i++ ){
			chars[i] = getId(handle, i);
		}
		
		return new String(chars);
	}
	
	public int getUniformsNum(){
		return getUniformsNum(handle);
	}
	
	public String getUniformName(int uniformIndex){
		char[] chars = new char[getUniformNameLength(handle, uniformIndex)];
		for( int i=0; i<chars.length; i++ ){
			chars[i] = getUniformName(handle, uniformIndex, i);
		}
		
		return new String(chars);
	}

	public int getUniformSize(int uniformIndex){
		return getUniformSize(handle, uniformIndex);
	}
	
	public float[] getUniformValues(int uniformIndex){
		float[] values = new float[getUniformSize(uniformIndex)];
		for( int i=0; i<values.length; i++ ){
			values[i] = getUniformValue(handle, uniformIndex, i);
		}
		
		return values;
	}
	
	// --- PRIVATE ----
	private static native int getMumLength(long handle); /*
		return strlen(((IRenderPass*)handle)->getMuM());
	*/
	
	private static native char getMum(long handle, int pos); /*
		return ((IRenderPass*)handle)->getMuM()[pos];
	*/
	
	private static native int getIdLength(long handle); /*
		return strlen(((IRenderPass*)handle)->getId());
	*/
	
	private static native char getId(long handle, int pos); /*
		return ((IRenderPass*)handle)->getId()[pos];
	*/

	private static native int getUniformsNum(long handle); /*
		return ((IRenderPass*)handle)->getUniformsNum();
	*/
	
	private static native int getUniformNameLength(long handle, int uniformIndex); /*
		return strlen(((IRenderPass*)handle)->getUniformName(uniformIndex));
	*/
	
	private static native char getUniformName(long handle, int uniformIndex, int pos); /*
		return ((IRenderPass*)handle)->getUniformName(uniformIndex)[pos];
	*/
	
	private static native int getUniformSize(long handle, int uniformIndex); /*
		return ((IRenderPass*)handle)->getUniformSize(uniformIndex);
	*/
	
	private static native float getUniformValue(long handle, int uniformIndex, int pos); /*
		return ((IRenderPass*)handle)->getUniformValues(uniformIndex)[pos];
	*/
}
