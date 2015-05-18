package com.btdstudio.glbase;

import com.btdstudio.glbase.RenderEnums.BlendMode;
import com.btdstudio.glbase.RenderEnums.CullingMode;
import com.btdstudio.glbase.RenderEnums.DepthFunc;

public class RenderParameters extends BasicRenderParameters {
	/*JNI
	    #include "Iobject.h"
	*/	
	
	public RenderParameters(){
		super(createRenderParameters());
	}
	
	public void dispose(){
		disposeRenderParameters(handle);
	}
	
	public void setShader(int shader){
		setShader(handle, shader);
	}
	
	public int getShader(){
		return getShader(handle);
	}
	
	public void setBlendSrcAlpha(boolean value){
		setBlendSrcAlpha(handle, value);
	}
	
	public boolean getBlendSrcAlpha(){
		return getBlendSrcAlpha(handle);
	}
	
	public void setBlendMode(BlendMode blendMode){
		setBlendMode(handle, blendMode.ordinal());
	}
	
	public BlendMode getBlendMode(){
		return BlendMode.values()[getBlendMode(handle)];
	}
	
	public void setCullingMode(CullingMode cullingMode){
		setCullingMode(handle, cullingMode.ordinal());
	}

	public CullingMode getCullingMode(){
		return CullingMode.values()[getCullingMode(handle)];
	}

	public void setUseDepthTest(boolean value){
		setUseDepthTest(handle, value);
	}
	
	public boolean getUseDepthTest(){
		return getUseDepthTest(handle);
	}
	
	public void setDepthFuncMode(DepthFunc depthFunc){
		setDepthFunc(handle, depthFunc.ordinal());
	}
	
	public DepthFunc getDepthFuncMode(){
		return DepthFunc.values()[getDepthFunc(handle)];
	}

	public void setDepthMask(boolean value){
		setDepthMask(handle, value);
	}
	
	public boolean getDepthMask(){
		return getDepthMask(handle);
	}
	
	public void setColorMask(int pos, boolean value){
		setColorMask(handle, pos, value);
	}
	
	public boolean getColorMask(int pos){
		return getColorMask(handle, pos);
	}
	
	
	// --- PRIVATE ---
	
	private static native long createRenderParameters(); /*
		RenderParameters* params = new RenderParameters();
		return (long long)params;
	*/
	
	private native void disposeRenderParameters(long handle); /*
		RenderParameters* params = static_cast<RenderParameters*>((void*)handle);
		delete params;
	*/
	
	private native void setShader(long handle, int shader); /*
		RenderParameters* params = static_cast<RenderParameters*>((void*)handle);
		params->shader = shader;
	*/
	
	private native int getShader(long handle); /*
		RenderParameters* params = static_cast<RenderParameters*>((void*)handle);
		return params->shader;
	*/
	
	private native void setBlendSrcAlpha(long handle, boolean value); /*
		RenderParameters* params = static_cast<RenderParameters*>((void*)handle);
		params->blendSrcAlpha = value;
	*/
	
	private native boolean getBlendSrcAlpha(long handle); /*
		RenderParameters* params = static_cast<RenderParameters*>((void*)handle);
		return params->blendSrcAlpha;
	*/
	
	private native void setBlendMode(long handle, int blendMode); /*
		RenderParameters* params = static_cast<RenderParameters*>((void*)handle);
		params->blendMode = (RenderEnums::BlendMode)blendMode;
	*/
	
	private native int getBlendMode(long handle); /*
		RenderParameters* params = static_cast<RenderParameters*>((void*)handle);
		return (int)params->blendMode;
	*/
	
	private native void setCullingMode(long handle, int cullingMode); /*
		RenderParameters* params = static_cast<RenderParameters*>((void*)handle);
		params->cullingMode = (RenderEnums::CullingMode)cullingMode;
	*/
	
	private native int getCullingMode(long handle); /*
		RenderParameters* params = static_cast<RenderParameters*>((void*)handle);
		return (int)params->cullingMode;
	*/
	
	private native void setUseDepthTest(long handle, boolean value); /*
		RenderParameters* params = static_cast<RenderParameters*>((void*)handle);
		params->useDepthTest = value;
	*/
	
	private native boolean getUseDepthTest(long handle); /*
		RenderParameters* params = static_cast<RenderParameters*>((void*)handle);
		return params->useDepthTest;
	*/
	
	private native void setDepthFunc(long handle, int depthFunc); /*
		RenderParameters* params = static_cast<RenderParameters*>((void*)handle);
		params->depthFunc = (RenderEnums::DepthFunc)depthFunc;
	*/
	
	private native int getDepthFunc(long handle); /*
		RenderParameters* params = static_cast<RenderParameters*>((void*)handle);
		return (int)params->depthFunc;
	*/
	
	private native void setDepthMask(long handle, boolean value); /*
		RenderParameters* params = static_cast<RenderParameters*>((void*)handle);
		params->depthMask = value;
	*/
	
	private native boolean getDepthMask(long handle); /*
		RenderParameters* params = static_cast<RenderParameters*>((void*)handle);
		return params->depthMask;
	*/
	
	private native void setColorMask(long handle, int pos, boolean value); /*
		RenderParameters* params = static_cast<RenderParameters*>((void*)handle);
		params->colorMask[pos] = value;
	*/
	
	private native boolean getColorMask(long handle, int pos); /*
		RenderParameters* params = static_cast<RenderParameters*>((void*)handle);
		return params->colorMask[pos];
	*/
}
