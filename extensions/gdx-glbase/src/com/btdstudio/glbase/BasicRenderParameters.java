package com.btdstudio.glbase;

public class BasicRenderParameters {
	// @off
	/*JNI
	    #include "Imrf.h"
	    #include "IanimationPlayer.h"
    */	
	
	long handle;
	Matrix matrixRef = new Matrix(-1);
	IAnimationPlayer playerRef = new IAnimationPlayer(-1);
	
	public BasicRenderParameters(){
		this(createBasicRenderParameters());
	}
	
	BasicRenderParameters(long handle){
		this.handle = handle;
	}
	
	public void dispose(){
		disposeBasicRenderParameters(handle);
	}
	
	public void setTexture(int texture){
		setTexture(handle, texture);
	}
	
	public int getTexture(){
		return getTexture(handle);
	}
	
	public void setFramebuffer(int framebuffer){
		setFramebuffer(handle, framebuffer);
	}
	
	public int getFramebuffer(){
		return getFramebuffer(handle);
	}
	
	public void setAnimationPlayer(IAnimationPlayer player){
		if( player == null ){
			setAnimationPlayer(handle, 0L);
		} else {
			setAnimationPlayer(handle, player.handle);
		}
	}
	
	public IAnimationPlayer getAnimationPlayer(){
		long playerHandle = getAnimationPlayer(handle);
		if( playerHandle == 0L ) return null;
		
		playerRef.handle = playerHandle;
		return playerRef;
	}
	
	public Matrix getModelTransform(){
		matrixRef.handle = getMatrixRef(handle);
		return matrixRef;
	}
	
	// --- PRIVATE ---
	
	private static native long createBasicRenderParameters(); /*
		BasicRenderParameters* params = new BasicRenderParameters();
		return (long long)params;
	*/
	
	private native long getMatrixRef(long handle); /*
		BasicRenderParameters* params = static_cast<BasicRenderParameters*>((void*)handle);
		return (long long)&params->modelTransform;
	*/
	
	private native void disposeBasicRenderParameters(long handle); /*
		BasicRenderParameters* params = static_cast<BasicRenderParameters*>((void*)handle);
		delete params;
	*/
	
	private native void setTexture(long handle, int texture); /*
		BasicRenderParameters* params = static_cast<BasicRenderParameters*>((void*)handle);
		params->texture = texture;
	*/
	
	private native int getTexture(long handle); /*
		BasicRenderParameters* params = static_cast<BasicRenderParameters*>((void*)handle);
		return params->texture;
	*/
	
	private native void setFramebuffer(long handle, int framebuffer); /*
		BasicRenderParameters* params = static_cast<BasicRenderParameters*>((void*)handle);
		params->framebuffer = framebuffer;
	*/
	
	private native int getFramebuffer(long handle); /*
		BasicRenderParameters* params = static_cast<BasicRenderParameters*>((void*)handle);
		return params->framebuffer;
	*/
	
	private native void setAnimationPlayer(long handle, long playerHandle); /*
		BasicRenderParameters* params = static_cast<BasicRenderParameters*>((void*)handle);
		IAnimationPlayer* player = (IAnimationPlayer*)playerHandle;
		
		params->animationPlayer = player;
	*/
	
	private native long getAnimationPlayer(long handle); /*
		BasicRenderParameters* params = static_cast<BasicRenderParameters*>((void*)handle);
		return (long long)&params->animationPlayer;
	*/
}
