package com.btdstudio.glbase;

public class ISurface {
	// @off
	/*JNI
	    #include "Isurface.h"
    */	
	
	long handle;
	
	ISurface(long handle){
		this.handle = handle;
	}
	
	public int getTexture(){
		return getTexture(handle);
	}
	
	// --- PRIVATE ---
	private static native int getTexture(long handle); /*
		return ((ISurface*)handle)->getTexture();
	*/
}
