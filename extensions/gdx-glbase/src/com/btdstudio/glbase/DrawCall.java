package com.btdstudio.glbase;

import com.btdstudio.glbase.RenderEnums.BlendMode;
import com.btdstudio.glbase.RenderEnums.ClearMode;
import com.btdstudio.glbase.RenderEnums.CullingMode;
import com.btdstudio.glbase.RenderEnums.DepthFunc;

public class DrawCall {
	// @off
	/*JNI
	 	#include "drawCall.h"
	    #include "IpolygonMap.h"
	    #include "IanimationPlayer.h"
	    #include "matrix.h"
	    #include "types.h"
	    #include "renderEnums.h"
    */
	
	long handle;
	private Matrix transformRef = new Matrix(-1);
	private IPolygonMap pmRef = new IPolygonMap(-1);
	private IAnimationPlayer playerRef = new IAnimationPlayer(-1);
	private float[] box = new float[6];
	private float[] clearCol = new float[4];
	private boolean[] colorMask = new boolean[4];
	
	private MyUniformValue tempMyUniform = new MyUniformValue(-1);
	
	DrawCall(long handle){
		this.handle = handle;
	}
	
	public void set( IPolygonMap polygonMap ){
		set(handle, polygonMap.handle);
	}
	
	public void set( ClearMode mode, float[] color ){
		set(handle, mode.ordinal(), color);
	}
	
	public void set( float[] bbox ){
		set(handle, bbox);
	}
	
	public void set( int numParticles ){
		set(handle, numParticles);
	}
	
	public void setFullScreen(){
		setFullScreen(handle);
	}
	
	public void set(DrawCall drawCall){
		setCopy(handle, drawCall.handle);
	}
	
	public IPolygonMap getRenderTargetPolygonMap(){
		pmRef.handle = getRenderTargetPolygonMap(handle);
		return pmRef;
	}
	
	public float[] getRenderTargetBox(){
		for( int i=0; i<6; i++ ){
			box[i] = getRenderTargetBox(handle, i);
		}
		return box;
	}
	
	public int getRenderTargetNumParticles(){
		return getRenderTargetNumParticles(handle);
	}
	
	public ClearMode getRenderTargetClearMode(){
		return ClearMode.values()[getRenderTargetClearMode(handle)];
	}
	
	public float[] getRenderTargetClearColor(){
		for( int i=0; i<4; i++ ){
			clearCol[i] = getRenderTargetClearColor(handle, i);
		}
		
		return clearCol;
	}
	
	public int getShader(){
		return getShader(handle);
	}
	
	public void setShader(int shader){
		setShader(handle, shader);
	}
	
	public int getTexture(){
		return getTexture(handle);
	}
	
	public void setTexture(int texture){
		setTexture(handle, texture);
	}
	
	public int getFramebuffer(){
		return getFramebuffer(handle);
	}
	
	public void setFramebuffer(int framebuffer){
		setFramebuffer(handle, framebuffer);
	}
	
	public IAnimationPlayer getAnimationPlayer(){
		long playerHandle = getAnimationPlayer(handle);
		if( playerHandle == 0L ) return null;
		
		playerRef.handle = playerHandle;
		return playerRef;
	}
	
	public void setAnimationPlayer(IAnimationPlayer player){
		setAnimationPlayer(handle, player.handle);
	}
	
	public int getMyUniformsSize(){
		return getMyUniformsSize(handle);
	}
	
	public MyUniformValue getMyUniform(int id){
		long uniformHandle = getMyUniform(handle, id);
		if( uniformHandle == 0L ) return null;
		
		tempMyUniform.handle = uniformHandle;
		return tempMyUniform;
	}
	
	public void addMyUniform(int id, MyUniformValue uniform){
		addMyUniform(handle, id, uniform.handle);
	}
	
	public void removeMyUniform(int id){
		removeMyUniform(handle, id);
	}
	
	public boolean getBlendSrcAlpha(){
		return getBlendSrcAlpha(handle);
	}
	
	public void setBlendSrcAlpha(boolean value){
		setBlendSrcAlpha(handle, value);
	}
	
	public BlendMode getBlendMode(){
		return BlendMode.values()[getBlendMode(handle)];
	}
	
	public void setBlendMode(BlendMode mode){
		setBlendMode(handle, mode.ordinal());
	}
	
	public CullingMode getCullingMode(){
		return CullingMode.values()[getCullingMode(handle)];
	}
	
	public void setCullingMode(CullingMode mode){
		setCullingMode(handle, mode.ordinal());
	}
	
	public boolean getUseDepthTest(){
		return getUseDepthTest(handle);
	}
	
	public void setUseDepthTest(boolean value){
		setUseDepthTest(handle, value);
	}
	
	public DepthFunc getDepthFunc(){
		return DepthFunc.values()[getDepthFunc(handle)];
	}
	
	public void setDepthFunc(DepthFunc func){
		setDepthFunc(handle, func.ordinal());
	}
	
	public boolean getDepthMask(){
		return getDepthMask(handle);
	}
	
	public void setDepthMask(boolean value){
		setDepthMask(handle, value);
	}
	
	public boolean[] getColorMask(){
		for( int i=0; i<4; i++ ){
			colorMask[i] = getColorMask(handle, i);
		}
		return colorMask;
	}
	
	public void setColorMask(boolean[] values){
		for( int i=0; i<4; i++ ){
			setColorMask(handle, i, values[i]);
		}
	}
	
	public Matrix getTransform(){
		transformRef.handle = getTransformRef(handle);
		return transformRef;
	}
	
	// --- PRIVATE ----
	private static native long getTransformRef(long handle); /*
		DrawCall* dc = (DrawCall*)handle;
		return (long long)&dc->modelTransform;
	*/
	
	private static native void set(long handle, long pmHandle); /*
		((DrawCall*)handle)->set(((IPolygonMap*)pmHandle));
	*/
	
	private static native void set(long handle, int mode, float[] color); /*
		((DrawCall*)handle)->set(((RenderEnums::ClearMode)mode), color);
	*/
	
	private static native void set(long handle, float[] bbox); /*
		((DrawCall*)handle)->set(bbox);
	*/
	
	private static native void set(long handle, int numParticles); /*
		((DrawCall*)handle)->set(numParticles);
	*/
	
	private static native void setFullScreen(long handle); /*
		((DrawCall*)handle)->setFullScreen();
	*/
	
	private static native void setCopy(long handle, long copy); /*
		((DrawCall*)handle)->set(((DrawCall*)copy));
	*/
	
	private static native long getRenderTargetPolygonMap(long handle); /*
		DrawCall* dc = (DrawCall*)handle;
		return (long long)dc->renderTarget.polygonMap;
	*/
	
	private static native float getRenderTargetBox(long handle, int pos); /*
		DrawCall* dc = (DrawCall*)handle;
		return dc->renderTarget.box[pos];
	*/
	
	private static native int getRenderTargetNumParticles(long handle); /*
		DrawCall* dc = (DrawCall*)handle;
		return dc->renderTarget.numParticles;
	*/
	
	private static native int getRenderTargetClearMode(long handle); /*
		DrawCall* dc = (DrawCall*)handle;
		return (int)dc->renderTarget.clear.mode;
	*/
	
	private static native float getRenderTargetClearColor(long handle, int pos); /*
		DrawCall* dc = (DrawCall*)handle;
		return dc->renderTarget.clear.color[pos];
	*/
	
	private static native int getShader(long handle); /*
		return ((DrawCall*)handle)->shader;
	*/
	
	private static native void setShader(long handle, int shader); /*
		((DrawCall*)handle)->shader = shader;
	*/
	
	private static native int getTexture(long handle); /*
		return ((DrawCall*)handle)->texture;
	*/
	
	private static native void setTexture(long handle, int texture); /*
		((DrawCall*)handle)->texture = texture;
	*/
	
	private static native int getFramebuffer(long handle); /*
		return ((DrawCall*)handle)->framebuffer;
	*/
	
	private static native void setFramebuffer(long handle, int framebuffer); /*
		((DrawCall*)handle)->framebuffer = framebuffer;
	*/
	
	private static native long getAnimationPlayer(long handle); /*
		return (long long)((DrawCall*)handle)->animationPlayer;
	*/
	
	private static native void setAnimationPlayer(long handle, long playerHandle); /*
		((DrawCall*)handle)->animationPlayer = (IAnimationPlayer*)playerHandle;
	*/
	
	private static native int getMyUniformsSize(long handle); /*
		return ((DrawCall*)handle)->myUniforms.size();
	*/
	
	private static native long getMyUniform(long handle, int id); /*
		if( ((DrawCall*)handle)->myUniforms.count(id) == 0 ) return 0L;
	 	return (long long)((DrawCall*)handle)->myUniforms[id];
	*/
	
	private static native void addMyUniform(long handle, int id, long uniformHandle); /*
		((DrawCall*)handle)->myUniforms[id] = (MyUniformValue*)uniformHandle;
	*/
	
	private static native void removeMyUniform(long handle, int id); /*
		((DrawCall*)handle)->myUniforms.erase(id);
	*/
	
	private static native boolean getBlendSrcAlpha(long handle); /*
		return ((DrawCall*)handle)->blendSrcAlpha;
	*/
	
	private static native void setBlendSrcAlpha(long handle, boolean value); /*
		((DrawCall*)handle)->blendSrcAlpha = value;;
	*/
	
	private static native int getBlendMode(long handle); /*
		return (int)((DrawCall*)handle)->blendMode;
	*/
	
	private static native void setBlendMode(long handle, int mode); /*
		((DrawCall*)handle)->blendMode = (RenderEnums::BlendMode)mode;
	*/
	
	private static native int getCullingMode(long handle); /*
		return (int)((DrawCall*)handle)->cullingMode;
	*/
	
	private static native void setCullingMode(long handle, int mode); /*
		((DrawCall*)handle)->cullingMode = (RenderEnums::CullingMode)mode;
	*/
	
	private static native boolean getUseDepthTest(long handle); /*
		return ((DrawCall*)handle)->useDepthTest;
	*/
	
	private static native void setUseDepthTest(long handle, boolean value); /*
		((DrawCall*)handle)->useDepthTest = value;
	*/
	
	private static native int getDepthFunc(long handle); /*
		return (int)((DrawCall*)handle)->depthFunc;
	*/
	
	private static native void setDepthFunc(long handle, int mode); /*
		((DrawCall*)handle)->depthFunc = (RenderEnums::DepthFunc)mode;
	*/
	
	private static native boolean getDepthMask(long handle); /*
		return ((DrawCall*)handle)->depthMask;
	*/
	
	private static native void setDepthMask(long handle, boolean value); /*
		((DrawCall*)handle)->depthMask = value;
	*/
	
	private static native boolean getColorMask(long handle, int pos); /*
		return ((DrawCall*)handle)->colorMask[pos];
	*/
	
	public static native void setColorMask(long handle, int pos, boolean value); /*
		((DrawCall*)handle)->colorMask[pos] = value;
	*/
}
