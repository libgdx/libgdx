package com.btdstudio.glbase;

import java.util.ArrayList;
import java.util.List;

public class IObject {
	// @off
	/*JNI
	 	#include "macros.h"
	    #include "Iobject.h"
	    #include "IrenderQueue.h"
	    #include "drawCall.h"
	    #include <string.h>
	    
	    // Temp
	    ArrayList tempList2(128);
	    int tempDcNum2;
	    DrawCall* tempDcList2;
    */	
	
	long handle;
	
	// Temp
	private List<DrawCall> templist = new ArrayList<DrawCall>();
	
	IObject(long handle){
		this.handle = handle;
	}
	
	public String getId(){
		char[] chars = new char[getIdLength(handle)];
		for( int i=0; i<chars.length; i++ ){
			chars[i] = getId(handle, i);
		}
		
		return new String(chars);
	}
	
	public String getName(){
		char[] chars = new char[getNameLength(handle)];
		for( int i=0; i<chars.length; i++ ){
			chars[i] = getName(handle, i);
		}
		
		return new String(chars);
	}
	
	public String getVersion(){
		char[] chars = new char[getVersionLength(handle)];
		for( int i=0; i<chars.length; i++ ){
			chars[i] = getVersion(handle, i);
		}
		
		return new String(chars);
	}
	
	public String getMetainfo(){
		char[] chars = new char[getMetainfoLength(handle)];
		for( int i=0; i<chars.length; i++ ){
			chars[i] = getMetainfo(handle, i);
		}
		
		return new String(chars);
	}
	
	public String getFilename(){
		char[] chars = new char[getFilenameLength(handle)];
		for( int i=0; i<chars.length; i++ ){
			chars[i] = getFilename(handle, i);
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
	
	public int getLeftBitShift(){
		return getLeftBitShift(handle);
	}
	
	public String getShadeModel(){
		char[] chars = new char[getShadeModelLength(handle)];
		for( int i=0; i<chars.length; i++ ){
			chars[i] = getShadeModel(handle, i);
		}
		
		return new String(chars);
	}
	
	public String getShadeValue(){
		char[] chars = new char[getShadeValueLength(handle)];
		for( int i=0; i<chars.length; i++ ){
			chars[i] = getShadeValue(handle, i);
		}
		
		return new String(chars);
	}
	
	public int getLayersNum(){
		return getLayersNum(handle);
	}
	
	public ILayer getLayer(int pos){
		return new ILayer(getLayer(handle, pos));
	}
	
	public void setTexture(int layerIdx, int polygonMapIdx, int texture){
		setTexture(handle, layerIdx, polygonMapIdx, texture);
	}
	
	public void setTexture(int polygonMapIdx, int texture){
		setTexture(handle, polygonMapIdx, texture);
	}
	
	public void addDrawCalls(IRenderQueue queue, RenderParameters params){
		addDrawCalls(handle, queue.handle, params.handle);
	}
	
	public List<DrawCall> prepareDrawCalls(RenderParameters params){
		templist.clear();
		
		// Prepare the calls
		prepareDrawCalls(handle, params.handle);
		
		// Map the calls back to java
		int calls = getDrawCallsNum();
		for( int i=0; i<calls; i++ ){
			DrawCall dc = Pools.acquireDrawCall();
			dc.handle = getDrawCall(i);
			templist.add(dc);
		}
		
		return templist;
	}
	
	public void setMatrixIndicesNames(String[] names){
		for( int i=0; i<names.length; i++ ){
			addMatrixIndicesName(names[i]);
		}
		
		setMatrixIndicesNames(handle);
	}
	
	public boolean initRenderEnv(){
		return initRenderEnv(handle);
	}
	
	public void releaseOriginalData(){
		releaseOriginalData(handle);
	}
	
	public void dispose(){
		dispose(handle);
	}
	
	// --- PRIVATE ---
	private static native void addMatrixIndicesName(String name); /*
		tempList2.add(strdup2(name));
	*/
	
	private static native void setMatrixIndicesNames(long handle); /*
		((IObject*)handle)->setMatrixIndicesNames(&tempList2);
		
		// Clear array
		for( int i=0; i<tempList2.getSize(); i++ ){
			delete (char*)tempList2.get(i);
		}
		
		tempList2.clear();
	*/

	private static native int getIdLength(long handle); /*
		return strlen(((IObject*)handle)->getId());
	*/
	
	private static native char getId(long handle, int pos); /*
		return ((IObject*)handle)->getId()[pos];
	*/
	
	private static native int getNameLength(long handle); /*
		return strlen(((IObject*)handle)->getName());
	*/
	
	private static native char getName(long handle, int pos); /*
		return ((IObject*)handle)->getName()[pos];
	 */
	
	private static native int getVersionLength(long handle); /*
		return strlen(((IObject*)handle)->getVersion());
	*/
	
	private static native char getVersion(long handle, int pos); /*
		return ((IObject*)handle)->getVersion()[pos];
	 */
	
	private static native int getMetainfoLength(long handle); /*
		return strlen(((IObject*)handle)->getMetainfo());
	*/
	
	private static native char getMetainfo(long handle, int pos); /*
		return ((IObject*)handle)->getMetainfo()[pos];
	 */
	
	private static native int getFilenameLength(long handle); /*
		return strlen(((IObject*)handle)->getFilename());
	*/
	
	private static native char getFilename(long handle, int pos); /*
		return ((IObject*)handle)->getFilename()[pos];
	 */
	
	private static native float getBoundingBox(long handle, int pos); /*
		return ((IObject*)handle)->getBoundingBox()[pos];
	*/
	
	private static native int getLeftBitShift(long handle); /*
		return ((IObject*)handle)->getLeftBitShift();
	*/
	
	private static native int getShadeModelLength(long handle); /*
		return strlen(((IObject*)handle)->getShadeModel());
	*/
	
	private static native char getShadeModel(long handle, int pos); /*
		return ((IObject*)handle)->getShadeModel()[pos];
	 */
	
	private static native int getShadeValueLength(long handle); /*
		return strlen(((IObject*)handle)->getShadeValue());
	*/
	
	private static native char getShadeValue(long handle, int pos); /*
		return ((IObject*)handle)->getShadeValue()[pos];
	 */
	
	private static native int getLayersNum(long handle); /*
		return ((IObject*)handle)->getLayers()->getSize();
	*/
	
	private static native long getLayer(long handle, int pos); /*
		return (long long)((IObject*)handle)->getLayers()->get(pos);
	*/
	
	private static native void setTexture(long handle, int layerIdx, int polygonMapIdx, int texture); /*
		((IObject*)handle)->setTexture(layerIdx, polygonMapIdx, texture);
	*/
	
	private static native void setTexture(long handle, int polygonMapIdx, int texture); /*
		((IObject*)handle)->setTexture(polygonMapIdx, texture);
	*/
	
	private static native void addDrawCalls(long handle, long renderQueueHandle, long renderParamsHandle); /*
		IRenderQueue* queue = (IRenderQueue*)renderQueueHandle;
		RenderParameters* params = (RenderParameters*)renderParamsHandle;
		
		((IObject*)handle)->addDrawCalls(queue, params);
	*/
	
	private static native void prepareDrawCalls(long handle, long paramsHandle); /*
		RenderParameters* params = (RenderParameters*)paramsHandle;		
		tempDcNum2 = ((IObject*)handle)->prepareDrawCalls(tempDcList2, params);
	*/
	
	private static native int getDrawCallsNum(); /*
		return tempDcNum2;
	*/
	
	private static native long getDrawCall(int pos); /*
		return (long long)&tempDcList2[pos];
	 */
	
	private static native boolean initRenderEnv(long handle); /*
		return ((IObject*)handle)->initRenderEnv();
	*/
	
	private static native void releaseOriginalData(long handle); /*
		((IObject*)handle)->releaseOriginalData();
	*/
	
	private static native void dispose(long handle); /*
		delete (IObject*)handle;
	*/
}
