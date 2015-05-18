package com.btdstudio.glbase;

public class Matrix {
	// @off
	/*JNI
	    #include "matrix.h"
	    
	    // Buffers for vector transforms (absolutely not thread safe)
	    float tmpMem[16];
    */	
	
	long handle;
	
	public Matrix(){
		this(createMatrix());
	}
	
	Matrix(long handle){
		this.handle = handle;
	}
	
	public void dispose(){
		dispose(handle);
	}

	public void setIdentity(){ 
		setIdentity(handle); 
	}
	
	public void setZero(){ 
		setZero(handle); 
	}
	
	public void setTranslation(float tx, float ty, float tz){
		setTranslation(handle, tx, ty, tz);
	}
	
	public void setScale(float sx, float sy, float sz){
		setScale(handle, sx, sy, sz);
	}
	
	public void setRotationX(float angle){
		setRotationX(handle, angle);
	}
	
	public void setRotationY(float angle){
		setRotationY(handle, angle);
	}
	
	public void setRotationZ(float angle){
		setRotationZ(handle, angle);
	}
	
	public void setFrustum(float l, float r, float b, float t, float n , float f){
		setFrustum(handle, l, r, b, t, n, f);
	}
	
	public void setOrtho(float l, float r, float b, float t, float n , float f){
		setOrtho(handle, l, r, b, t, n, f);
	}
	
	public void setPerspective(float fovy, float aspectRatio, float near, float far){
		setPerspective(handle, fovy, aspectRatio, near, far);
	}
	
	public void translate( float tx, float ty, float tz ){
		translate(handle, tx, ty, tz);
	}

	public void scale( float sx, float sy, float sz ){
		scale(handle, sx, sy, sz);
	}
	
	public void rotateX( float angle ){
		rotateX(handle, angle);
	}
	
	public void rotateY( float angle ){
		rotateY(handle, angle);
	}
	
	public void rotateZ( float angle ){
		rotateZ(handle, angle);
	}
	
	public void lookAt(float[] v3eye, float[] v3center, float[] v3up){
		lookAt(handle, v3eye, v3center, v3up); 
	}
	
	public void transform3(float[] vector){
		transform3(handle, vector);
		
		// Take results back
		for( int i=0; i<3; i++ ){
			vector[i] = getTmpMem(i);
		}
	}
	
	public void transform4(float[] vector4){
		transform4(handle, vector4);
		
		// Take results back
		for( int i=0; i<4; i++ ){
			vector4[i] = getTmpMem(i); 
		}
	}
	
	public void invert(){
		invert(handle);
	}
	
	public void multiply(Matrix operand){
		multiply(handle, operand.handle);
	}
	
	public void premultiply(Matrix operand){
		premultiply(handle, operand.handle);
	}
	
	public void copyFrom(Matrix src){
		copyFrom(handle, src.handle);
	}
	
	public void to3x4(float[] out){
		to3x4(handle);
		
		// Copy back
		for(int i=0; i<12; i++){
			out[i] = getTmpMem(i);
		}
	}
	
	public void to4x4(float[] out){
		to4x4(handle);
		
		// Copy back
		for(int i=0; i<16; i++){
			out[i] = getTmpMem(i);
		}
	}
	
	@Override
	public String toString(){
		to3x4(handle);
		
		StringBuilder builder = new StringBuilder();
		for( int y=0; y<4; y++ ){
			for( int x=0; x<4; x++ ){
				builder.append(String.format("%.2f  ", getTmpMem(y*4+x)));
			}
			builder.append('\n');
		}
		
		return builder.toString();
	}
	
	// --- PRIVATE ---
	
	private static native long createMatrix(); /*
		Matrix* matrix = new Matrix();
		matrix->setIdentity();
		return (long long)matrix;
	*/
	
	private native void dispose(long handle); /*
		delete (Matrix*)handle;
	*/
	
	private native float getTmpMem(int comp); /*
		return tmpMem[comp];
	*/

	private native void setIdentity(long handle); /*
		((Matrix*)handle)->setIdentity();
	*/
	
	private native void setZero(long handle); /*
		((Matrix*)handle)->setZero();
	*/
	
	private native void setTranslation(long handle, float tx, float ty, float tz); /*
		((Matrix*)handle)->setTranslation(tx, ty, tz);
	*/
	
	private native void setScale(long handle, float sx, float sy, float sz); /*
		((Matrix*)handle)->setScale(sx, sy, sz);
	*/
	
	private native void setRotationX(long handle, float angle); /*
		((Matrix*)handle)->setRotationX(angle);
	*/
	
	private native void setRotationY(long handle, float angle); /*
		((Matrix*)handle)->setRotationY(angle);
	*/
	
	private native void setRotationZ(long handle, float angle); /*
		((Matrix*)handle)->setRotationZ(angle);
	*/
	
	private native void setFrustum(long handle, float l, float r, float b, float t, float n , float f); /*
		((Matrix*)handle)->setFrustum(l, r, b, t, n, f);
	*/
	
	private native void setOrtho(long handle, float l, float r, float b, float t, float n , float f); /*
		((Matrix*)handle)->setOrtho(l, r, b, t, n, f);
	*/
	
	private native void setPerspective(long handle, float fovy, float aspectRatio, float near, float far); /*
		((Matrix*)handle)->setPerspective(fovy, aspectRatio, near, far);
	*/
	
	private native void translate(long handle, float tx, float ty, float tz); /*
		((Matrix*)handle)->translate(tx, ty, tz);
	*/
	
	private native void scale(long handle, float sx, float sy, float sz); /*
		((Matrix*)handle)->scale(sx, sy, sz);
	*/
	
	private native void rotateX(long handle, float angle); /*
		((Matrix*)handle)->rotateX(angle);
	*/
	
	private native void rotateY(long handle, float angle); /*
		((Matrix*)handle)->rotateY(angle);
	*/
	
	private native void rotateZ(long handle, float angle); /*
		((Matrix*)handle)->rotateZ(angle);
	*/
	
	private native void invert(long handle); /*
		((Matrix*)handle)->invert();
	*/
	
	private native void transform3(long handle, float[] vector); /*
		tmpMem[0] = vector[0];
		tmpMem[1] = vector[1];
		tmpMem[2] = vector[2];
		((Matrix*)handle)->transform3(tmpMem);
	*/
	
	private native void transform4(long handle, float[] vector); /*
		tmpMem[0] = vector[0];
		tmpMem[1] = vector[1];
		tmpMem[2] = vector[2];
		tmpMem[3] = vector[3];
		((Matrix*)handle)->transform4(tmpMem);
	*/
	
	private native void lookAt(long handle, float[] eye,float[] center, float[] up); /*
		((Matrix*)handle)->lookAt(eye, center, up);
	*/
	
	private native void multiply(long handle, long operandHandle); /*
		((Matrix*)handle)->multiply((Matrix*)operandHandle);
	*/
	
	private native void premultiply(long handle, long operandHandle); /*
		((Matrix*)handle)->premultiply((Matrix*)operandHandle);
	*/
	
	private native void copyFrom(long handle, long srcHandle); /*
		((Matrix*)handle)->copyFrom((Matrix const*)srcHandle);
	*/
	
	private native void to3x4(long handle); /*
		((Matrix*)handle)->to3x4(tmpMem);
	*/
	
	private native void to4x4(long handle); /*
		for( int i=0; i<16; i++ ){
			tmpMem[i] = ((Matrix*)handle)->getMatrixPointer()[i];
		}
	*/
}
