package com.btdstudio.glbase;

import com.badlogic.gdx.utils.SharedLibraryLoader;
import com.btdstudio.glbase.RenderEnums.ClearMode;

/**
 * Created by lake on 15/04/23.
 */
public class IGLBase {
	static{
		new SharedLibraryLoader().load("gdx-glbase");
	}
	
    // @off
	/*JNI
	    #include "Iglbase.h"
	    #include "Iobject.h"
	    #include "IpolygonMap.h"
	    #include "IanimationPlayer.h"
	    #include "renderEnums.h"
	    
	    #ifdef __ANDROID__
	    #include <android/log.h>
	    
	    int logi(char const* fmt, va_list arglist)
		{
		  return __android_log_vprint(ANDROID_LOG_INFO, "glbase", fmt, arglist);
		}
		
		int loge(char const* fmt, va_list arglist)
		{
		  return __android_log_vprint(ANDROID_LOG_ERROR, "glbase", fmt, arglist);
		}
	    #else
	    int log(char const* fmt, va_list arglist)
	    {
	    	int res = vprintf(fmt, arglist);
	    	printf("\n");
	    	fflush(stdout);
	    	return res;
	    }
	    #endif
    */
	
	static final int DEFAULT_SHADER = 0;

	
    public static native void intialize(int screenWidth, int screenHeight); /*    
        // Set log function
        #ifdef __ANDROID__
        IGLBase::get()->setTraceFunction(&logi);
  		IGLBase::get()->setEtraceFunction(&loge);
        #else
        IGLBase::get()->setTraceFunction(&log);
  		IGLBase::get()->setEtraceFunction(&log);
        #endif
        
        IGLBase::get()->initialize(screenWidth, screenHeight);
    */
    
    public static void flush() {
    	flushNTV();
    	
    	// Free all pooled stuff
    	Pools.freeAll();
    }
    
    public static IObject loadBo3(byte[] data){
    	return loadBo3(data, false);
    }
    
    public static IObject loadBo3(byte[] data, boolean gpuOnly){
    	return new IObject(loadBo3NTV(data, data.length, gpuOnly));
    }
    
    public static IObject subObject(IObject object, int vertexOffset, int triangleOffset, 
    		int vertexLength, int triangleLength, int layer, int polygonMap){
    	return new IObject(subObjectNTV(object.handle, vertexOffset, triangleOffset, 
    			vertexLength, triangleLength, layer, polygonMap));
    }
    
    public static IMergeGroup createMergeGroup(IObject base, int maxVertices){
    	return createMergeGroup(base, maxVertices, 1);
    }
    
    public static IMergeGroup createMergeGroup(IObject base, int maxVertices, int polygonMaps){
    	return new IMergeGroup(createMergeGroupNTV(base.handle, maxVertices, polygonMaps));
    }
    
    public static IMergeGroup createMergeGroup(int maxVertices){
    	return new IMergeGroup(createMergeGroupNTV(maxVertices));
    }
    
    public static IRenderQueue createRenderQueue(){
    	return new IRenderQueue(createRenderQueueNTV());
    }
    
    public static native int loadShader(String vertexCode, String fragmentCode, 
    		String vShaderName, String fShaderName); /*
    	return IGLBase::get()->loadShader(vertexCode, fragmentCode, vShaderName, fShaderName);
    */
    
    public static native int loadShader(String vertexCode, String fragmentCode, String programName); /*
    	return IGLBase::get()->loadShader(vertexCode, fragmentCode, programName);
    */
    
    public static native void addShaderUniform(int shaderId, int uniformPos, String uniformName); /*
    	IGLBase::get()->addShaderUniform(shaderId, uniformPos, uniformName);
    */
    
    public static native int getShaderProgramID(String shaderName); /*
    	return IGLBase::get()->getShaderProgramID(shaderName);
    */
   
    public static native boolean hasMyUniform( int shaderID, int uniformPos ); /*
    	return IGLBase::get()->hasMyUniform(shaderID, uniformPos);
    */
    
    public static int loadTexture(byte[] data, boolean repeat, boolean mipmap){
    	return loadTexture(data, repeat, mipmap, true);
    }
    
    public static int loadTexture(byte[] data, boolean repeat, boolean mipmap, boolean gpuOnly){
    	return loadTextureNTV(data, data.length, repeat, mipmap, gpuOnly);
    }
    
    public static int loadTextureCube(byte[] data1, byte[] data2, byte[] data3, byte[] data4, byte[] data5,
    		byte[] data6, boolean mipmap){
    	int[] lengths = new int[6];
    	lengths[0] = data1.length;
    	lengths[1] = data2.length;
    	lengths[2] = data3.length;
    	lengths[3] = data4.length;
    	lengths[4] = data5.length;
    	lengths[5] = data6.length;
    	
    	return loadTextureCubeNTV(data1, data2, data3, data4, data5, data6, lengths, mipmap);
    }
    
    public static native void deleteTexture(int texture); /*
    	return IGLBase::get()->deleteTexture(texture);
    */
    
    public static TextureInfo getTextureInfo(int texture){
    	TextureInfo info = new TextureInfo();
    	info.width = getTextureInfoWidth(texture);
    	info.height = getTextureInfoHeight(texture);
    	info.format = getTextureInfoFormat(texture);
    	info.pixelFormat = getTextureInfoPixelFormat(texture);
    	
    	return info;
    }

	public static int createTexture( int width, int height, int format, int pixelFormat, 
		     boolean repeat ){
    	return createTexture(width, height, format, pixelFormat, repeat, null, true);
    }
    
    public static int createTexture( int width, int height, int format, int pixelFormat, 
		     boolean repeat, byte[] data ){
    	return createTexture(width, height, format, pixelFormat, repeat, data, true);
    }
    
    public static int createTexture( int width, int height, int format, int pixelFormat, 
		     boolean repeat, boolean filterLinear ){
	   	return createTexture(width, height, format, pixelFormat, repeat, null, filterLinear);
	}
    
    public static native int createTexture( int width, int height, int format, int pixelFormat, 
		     boolean repeat, byte[] data, boolean filterLinear ); /*
	    return IGLBase::get()->createTexture(width, height, format, pixelFormat, repeat, (byte*)data, filterLinear);
	*/
        
    public static native int createTextureCube( int width, int height, int format, int pixelFormat, 
			 boolean repeat, byte[] data1, byte[] data2, byte[] data3, byte[] data4, byte[] data5,
	    	 byte[] data6, boolean filterLinear ); /*
		byte* data[6] = { (byte*)data1, (byte*)data2, (byte*)data3, (byte*)data4, (byte*)data5, (byte*)data6 };
		return IGLBase::get()->createTextureCube(width, height, format, pixelFormat, repeat, data, filterLinear);
	*/
    
    public static native boolean copySubImage(int texture, int offsetX, int offsetY, int width, int height, 
		    int format, int pixelFormat, byte[] subData); /*
		return IGLBase::get()->copySubImage(texture, offsetX, offsetY, width, height, format, pixelFormat, (byte*)subData);
	*/
    
    public static native int createFBO(); /*
    	return IGLBase::get()->createFBO();
    */
    
    public static native void setFBOTexture(int fbo, int texture, boolean createDepth); /*
    	IGLBase::get()->setFBOTexture(fbo, texture, createDepth);
    */
    
    public static int loadAnimation(byte[] bsFile, byte[] binFile){
    	return loadAnimation(bsFile, bsFile.length, binFile, binFile.length);
    }
    
    public static int loadAnimation(byte[] bbmFile){
    	return loadAnimationNTV(bbmFile, bbmFile.length);
    }
    
    public static native void setAnimationParent(int child, int parent); /*
    	IGLBase::get()->setAnimationParent(child, parent);
    */
    
    public static native boolean deleteAnimation(int animationID); /*
    	return IGLBase::get()->deleteAnimation(animationID);
    */
    
    public static IMrf loadMrf(String mrfFile){
    	return loadMrf(mrfFile, false);
    }
    
    public static IMrf loadMrf(String mrfFile, boolean skipSetUniforms){
    	return new IMrf(loadMrfNTV(mrfFile, skipSetUniforms));
    }
    
    public static IAnimationPlayer loadAnimationPlayer( int animation, IObject object ){
    	return new IAnimationPlayer(loadAnimationPlayerNTV(animation, object.handle));
    }
    
    public static IAnimationPlayer loadAnimationPlayer( int animation, IPolygonMap polygonMap){
    	return new IAnimationPlayer(loadAnimationPlayerPMNTV(animation, polygonMap.handle));
    }
    
    public static void adaptTo( IAnimationPlayer animationPlayer, IObject object ){
    	adaptTo(animationPlayer.handle, object.handle);
    }
    
    public static void adaptTo( IAnimationPlayer animationPlayer, IPolygonMap polygonMap ){
    	adaptToPM(animationPlayer.handle, polygonMap.handle);
    }
    
	public static native void setViewport(int framebuffer, int left, int top, int right, int bottom); /*
		IGLBase::get()->setViewport(framebuffer, left, top, right, bottom);
	*/
	
	public static MyUniformValue acquireMyUniform(float[] vector, int numComponents){
		MyUniformValue mu = Pools.acquiteMyUniformValue();
		mu.handle = acquireMyUniformNTV(vector, numComponents);
		return mu;
	}
	
	public static MyUniformValue acquireMyUniform(int texture, int glactive){
		MyUniformValue mu = Pools.acquiteMyUniformValue();
		mu.handle = acquireMyUniformNTV(texture, glactive);
		return mu;
	}
	
	public static DrawCall acquireDrawCall(){
		DrawCall dc = Pools.acquireDrawCall();
		dc.handle = acquireDrawCallNTV();
		return dc;
	}
	
	public static DrawCall acquireDrawCall(IPolygonMap polygonMap){
		DrawCall dc = Pools.acquireDrawCall();
		dc.handle = acquireDrawCallNTV(polygonMap.handle);
		return dc;
	}
	
	public static DrawCall acquireDrawCall(float[] bbox){
		DrawCall dc = Pools.acquireDrawCall();
		dc.handle = acquireDrawCallNTV(bbox);
		return dc;
	}
	
	public static DrawCall acquireDrawCall(int numParticles){
		DrawCall dc = Pools.acquireDrawCall();
		dc.handle = acquireDrawCallNTV(numParticles);
		return dc;
	}
	
	public static DrawCall acquireDrawCall(ClearMode clearMode, float[] clearColor){
		DrawCall dc = Pools.acquireDrawCall();
		dc.handle = acquireDrawCallNTV(clearMode.ordinal(), clearColor);
    	return dc;
    }
	
	public static native boolean getFboUsed(int index); /*
		return IGLBase::get()->getFboUsed()[index];
	*/
	
	public static native double getTime(); /*
		return IGLBase::get()->getTime();
	*/
    
	public static native void onResume(); /*
		IGLBase::get()->onResume();
	*/
	
	public static native void updateTimer(int millis); /*
		timeval t;
		t.tv_sec = 0;
		t.tv_usec = millis*1000;
		
		IGLBase::get()->updateTimer(t);
	*/ 
	
	public static native void resetGLState(); /*
		IGLBase::get()->resetGLState();
	*/
    
	// -- PRIVATE -- // 

	private static native long createRenderQueueNTV(); /*
    	return (long long)IGLBase::get()->createRenderQueue();
    */
    
    private static native long loadBo3NTV(byte[] data, int length, boolean gpuOnly); /*
    	return (long long)IGLBase::get()->loadBo3((byte*)data, length, gpuOnly);
    */
    
    private static native long subObjectNTV(long object, int vertexOffset, int triangleOffset, 
    		int vertexLength, int triangleLength, int layer, int polygonMap); /*
    	IObject* parent = (IObject*)object;
    	return (long long)IGLBase::get()->subObject(parent, vertexOffset, triangleOffset, 
    			vertexLength, triangleLength, layer, polygonMap);
    */
    
    private static native long createMergeGroupNTV(long object, int maxVertices, int polygonMaps); /*
    	IObject* parent = (IObject*)object;
    	return (long long)IGLBase::get()->createMergeGroup(parent, maxVertices, polygonMaps);
    */
    
    private static native long createMergeGroupNTV(int maxVertices); /*
		return (long long)IGLBase::get()->createMergeGroup(maxVertices);
	*/
    
    private static native int loadAnimation(byte[] bsFile, int bsFileLength, byte[] binFile, int binFileLength); /*
    	return IGLBase::get()->loadAnimation(bsFile, bsFileLength, (byte*)binFile, binFileLength);
    */
    
    private static native int loadAnimationNTV(byte[] bbmFile, int bbmFileLength); /*
    	return IGLBase::get()->loadAnimation((byte*)bbmFile, bbmFileLength);
    */
    
	private static native long loadMrfNTV(String mrfFile, boolean skipSetUniforms); /*
		return (long long)IGLBase::get()->loadMrf(mrfFile, skipSetUniforms);
	*/
	
	private static native long loadAnimationPlayerNTV(int animation, long object); /*
		IObject* target = (IObject*)object;
		return (long long)IGLBase::get()->loadAnimationPlayer(animation, target);
	*/
	
	private static native long loadAnimationPlayerPMNTV(int animation, long pm); /*
		IPolygonMap* target = (IPolygonMap*)pm;
		return (long long)IGLBase::get()->loadAnimationPlayer(animation, target);
	*/
	
	private static native void adaptTo(long animationPlayerHandle, long objectHandle); /*
		IAnimationPlayer* player = (IAnimationPlayer*)animationPlayerHandle;
		IObject* object= (IObject*)objectHandle;
		IGLBase::get()->adaptTo(player, object);
	*/
	
	private static native void adaptToPM(long animationPlayerHandle, long pmHandle); /*
		IAnimationPlayer* player = (IAnimationPlayer*)animationPlayerHandle;
		IPolygonMap* pm = (IPolygonMap*)pmHandle;
		IGLBase::get()->adaptTo(player, pm);
	*/
	
	private static native int loadTextureNTV(byte[] data, int length, boolean repeat, boolean mipmap, boolean gpuOnly); /*
		return IGLBase::get()->loadTexture((byte*)data, length, repeat, mipmap, gpuOnly);
	*/

	private static native int loadTextureCubeNTV(byte[] data1, byte[] data2, byte[] data3, byte[] data4, byte[] data5,
    		byte[] data6, int[] length, boolean mipmap); /*
    	byte* data[6] = { (byte*)data1, (byte*)data2, (byte*)data3, (byte*)data4, (byte*)data5, (byte*)data6 };
    	return IGLBase::get()->loadTextureCube(data, length, mipmap);
    */

    private static native long acquireMyUniformNTV(float[] vector, int numComponents); /*
    	return (long long)IGLBase::get()->acquireMyUniform(vector, numComponents);
    */
    
    private static native long acquireMyUniformNTV(int texture, int glactive); /*
		return (long long)IGLBase::get()->acquireMyUniform(texture, glactive);
     */
    
    private static native long acquireDrawCallNTV(long pmHandle); /*
    	IPolygonMap* pm = (IPolygonMap*)pmHandle;
		return (long long)IGLBase::get()->acquireDrawCall(pm);
	 */
    
    private static native long acquireDrawCallNTV(float[] bbox); /*
		return (long long)IGLBase::get()->acquireDrawCall(bbox);
	 */
    
    private static native long acquireDrawCallNTV(int numParticles); /*
		return (long long)IGLBase::get()->acquireDrawCall(numParticles);
	 */
    
    private static native long acquireDrawCallNTV(); /*
    	return (long long)IGLBase::get()->acquireDrawCall();
    */
    
	private static native long acquireDrawCallNTV(int mode, float[] color); /*
		return (long long)IGLBase::get()->acquireDrawCall((RenderEnums::ClearMode)mode, color);
	*/
	    
	private static native int getTextureInfoWidth(int texture); /*
		return IGLBase::get()->getTextureInfo(texture).width;
	*/
	
	private static native int getTextureInfoHeight(int texture); /*
		return IGLBase::get()->getTextureInfo(texture).height;
	*/
	
	private static native int getTextureInfoFormat(int texture); /*
		return IGLBase::get()->getTextureInfo(texture).format;
	*/
	
	private static native int getTextureInfoPixelFormat(int texture); /*
		return IGLBase::get()->getTextureInfo(texture).pixelFormat;
	*/
	
    private static native void flushNTV(); /*
		IGLBase::get()->flush();
	*/
}