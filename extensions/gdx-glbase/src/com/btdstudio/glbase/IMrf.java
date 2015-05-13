package com.btdstudio.glbase;

import java.util.ArrayList;
import java.util.List;

public class IMrf {
	// @off
	/*JNI
	    #include "Imrf.h"
	    #include "IrenderQueue.h"
	    #include "Iobject.h"
	    #include "drawCall.h"
	    #include <string.h>
	    
	    // Temp drawcall list
	    int tempDcNum;
	    DrawCall* tempDcList;
    */	
	
	long handle;
	
	// Temp
	private List<DrawCall> templist = new ArrayList<DrawCall>();
	private IRenderPass temppass = new IRenderPass(-1);
	
	IMrf(long handle){
		this.handle = handle;
	}
	
	public static native void initialize(int screenWidth, int screenHeight); /*
		IMrf::initialize(screenWidth, screenHeight);
	*/
	
	public static native int getFramebuffer(int renderTarget); /*
		return IMrf::getFramebuffer(renderTarget);
	*/
	
	public void registerDrawCalls(IRenderQueue queue, BasicRenderParameters params, IObject object){
		registerDrawCalls(handle, queue.handle, params.handle, object.handle);
	}
	
	public List<DrawCall> prepareDrawCalls(BasicRenderParameters params, IObject object){
		templist.clear();
		
		// Prepare the calls
		prepareDrawCalls(handle, params.handle, object.handle);
		
		// Map the calls back to java
		int calls = getDrawCallsNum();
		for( int i=0; i<calls; i++ ){
			DrawCall dc = Pools.acquireDrawCall();
			dc.handle = getDrawCall(i);
			templist.add(dc);
		}
		
		return templist;
	}
	
	public int getPassesNum(){
		return getPassesNum(handle);
	}
	
	public IRenderPass getPass(int passIndex){
		temppass.handle = getPass(handle, passIndex);
		return temppass;
	}
	
	public String getId(){
		char[] chars = new char[getIdLength(handle)];
		for( int i=0; i<chars.length; i++ ){
			chars[i] = getId(handle, i);
		}
		
		return new String(chars);
	}
	
	public void setUniforms(){
		setUniforms(handle);
	}
	
	public void dispose(){
		dispose(handle);
	}
	
	// --- PRIVATE ---
	private static native int getDrawCallsNum(); /*
		return tempDcNum;
	*/
	
	private static native long getDrawCall(int pos); /*
		return (long long)&tempDcList[pos];
	*/
	
	private static native void registerDrawCalls(long handle, long queueHandle, long paramsHandle, long objectHandle); /*
		IRenderQueue* queue = (IRenderQueue*)queueHandle;
		BasicRenderParameters* params = (BasicRenderParameters*)paramsHandle;
		IObject* object = (IObject*)objectHandle;
		
		((IMrf*)handle)->registerDrawCalls(queue, params, object);
	*/
	
	private static native void prepareDrawCalls(long handle, long paramsHandle, long objectHandle); /*
		BasicRenderParameters* params = (BasicRenderParameters*)paramsHandle;
		IObject* object = (IObject*)objectHandle;
		
		tempDcNum = ((IMrf*)handle)->prepareDrawCalls(tempDcList, params, object);
	*/
	
	private static native int getPassesNum(long handle); /*
		return ((IMrf*)handle)->getPassesNum();
	*/
	
	private static native long getPass(long handle, int pos); /*
		return (long long)((IMrf*)handle)->getPass(pos);
	*/
	
	private static native int getIdLength(long handle); /*
		return strlen(((IMrf*)handle)->getId());
	*/
	
	private static native char getId(long handle, int pos); /*
		return ((IMrf*)handle)->getId()[pos];
	*/
	
	private static native void setUniforms(long handle); /*
		((IMrf*)handle)->setUniforms();
	*/
	
	private static native void dispose(long handle); /*
		delete (IMrf*)handle;
	*/
}
