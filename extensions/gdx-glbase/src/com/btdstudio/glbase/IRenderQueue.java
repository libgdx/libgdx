package com.btdstudio.glbase;

public class IRenderQueue {
	
	// @off
	/*JNI
	 	#include "IrenderQueue.h"
	    #include "drawCall.h"
	    #include "matrix.h"
    */
	
	long handle;
	
	IRenderQueue(long handle){
		this.handle = handle;
	}
	
	public void setProjection(Matrix projection){
		setProjection(handle, projection.handle);
	}
	
	public void setView(Matrix view){
		setView(handle, view.handle);
	}
	
	public void getProjection(Matrix out){
		out.handle = getProjection(handle);
	}
	
	public void getView(Matrix out){
		out.handle = getView(handle);
	}
	
	public void setFog(float[] fogColor, float fogNear, float fogFar){
		setFog(handle, fogColor, fogNear, fogFar);
	}
	
	public void registerDrawCall(DrawCall drawCall){
		registerDrawCallNTV(handle, drawCall.handle);
	}
	
	public void execRender() {
		execRender(handle);
	}
	
	public void dispose(){
		dispose(handle);
	}
	
	private static native void registerDrawCallNTV(long renderQueue, long drawCall); /*
		DrawCall* dc = (DrawCall*)drawCall;
		((IRenderQueue*)renderQueue)->registerDrawCall(dc);
	*/
	
	private static native void execRender(long renderQueue); /*
		((IRenderQueue*)renderQueue)->execRender();
	*/
	
	private static native void setProjection(long renderQueue, long matrixHandle); /*
		((IRenderQueue*)renderQueue)->setProjection((Matrix*)matrixHandle);
	*/
	
	private static native void setView(long renderQueue, long matrixHandle); /*
		((IRenderQueue*)renderQueue)->setView((Matrix*)matrixHandle);
	*/
	
	private static native long getProjection(long renderQueue); /*
		return (long long)((IRenderQueue*)renderQueue)->getProjection();
	*/
	
	private static native long getView(long renderQueue); /*
		return (long long)((IRenderQueue*)renderQueue)->getView();
	*/
	
	private static native void setFog(long renderQueue, float[] fogColor, float fogNear, float fogFar); /*
		return ((IRenderQueue*)renderQueue)->setFog(fogColor, fogNear, fogFar);
	*/
	
	private static native void dispose(long handle); /*
		delete (IRenderQueue*)handle;
	*/
}
