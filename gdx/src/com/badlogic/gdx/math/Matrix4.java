/*******************************************************************************
 * Copyright 2010 Mario Zechner (contact@badlogicgames.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.badlogic.gdx.math;


/**
 * Encapsulates a column major 4 by 4 matrix. You can access
 * the linear array for use with OpenGL via the public {@link Matrix4.val}
 * member. Like the {@link Vector3} class it allows to chain methods by
 * returning a reference to itself.
 * 
 * @author badlogicgames@gmail.com
 *
 */
public final class Matrix4
{    
	private static final long serialVersionUID = -2717655254359579617L;
	public static final int M00=0;//0;
	public static final int M01=4;//1;
	public static final int M02=8;//2;
	public static final int M03=12;//3;
	public static final int M10=1;//4;
	public static final int M11=5;//5;
	public static final int M12=9;//6;
	public static final int M13=13;//7;
	public static final int M20=2;//8;
	public static final int M21=6;//9;
	public static final int M22=10;//10;
	public static final int M23=14;//11;
	public static final int M30=3;//12;
	public static final int M31=7;//13;
	public static final int M32=11;//14;
	public static final int M33=15;//15;

    public final float tmp[] = new float[16];
    public final float val[] = new float[16];
   
    /**
     * Constructs an identity matrix
     */
    public Matrix4()
    {
        val[M00]=1f; val[M11]=1f; val[M22]=1f; val[M33]=1f;
    }

    /**
     * Constructs a matrix from the given matrix
     * 
     * @param matrix The matrix
     */
    public Matrix4(Matrix4 matrix)
    {
        this.set(matrix);
    }

    /**
     * Constructs a matrix from the given float array. The
     * array must have at least 16 elements
     * @param values The float array
     */
    public Matrix4(float[] values)
    {
        this.set(values);
    }

    /**
     * Constructs a rotation matrix from the given {@link Quaternion}
     * @param quaternion The quaternion
     */
    public Matrix4(Quaternion quaternion)
    {
        this.set(quaternion);
    }

    /**
     * Sets the matrix to the given matrix.
     * 
     * @param matrix The matrix
     * @return This matrix for chaining
     */
    public  Matrix4 set(Matrix4 matrix)
    {
        return this.set(matrix.val);
    }

    /**
     * Sets the matrix to the given matrix as a float array.
     * The float array must have at least 16 elements.
     * 
     * @param values The matrix 
     * @return This matrix for chaining
     */
    public  Matrix4 set(float[] values)
    {
        val[M00]=values[M00]; val[M10]=values[M10]; val[M20]=values[M20]; val[M30]=values[M30];
        val[M01]=values[M01]; val[M11]=values[M11]; val[M21]=values[M21]; val[M31]=values[M31];
        val[M02]=values[M02]; val[M12]=values[M12]; val[M22]=values[M22]; val[M32]=values[M32];
        val[M03]=values[M03]; val[M13]=values[M13]; val[M23]=values[M23]; val[M33]=values[M33];
        return this;
    }

    /**
     * Sets the matrix to a rotation matrix representing the
     * quaternion.
     * 
     * @param quaternion The quaternion
     * @return This matrix for chaining
     */
    public  Matrix4 set(Quaternion quaternion)
    {
        // Compute quaternion factors
        float l_xx = quaternion.x*quaternion.x;
        float l_xy = quaternion.x*quaternion.y;
        float l_xz = quaternion.x*quaternion.z;
        float l_xw = quaternion.x*quaternion.w;
        float l_yy = quaternion.y*quaternion.y;
        float l_yz = quaternion.y*quaternion.z;
        float l_yw = quaternion.y*quaternion.w;
        float l_zz = quaternion.z*quaternion.z;
        float l_zw = quaternion.z*quaternion.w;
        // Set matrix from quaternion
        val[M00]=1-2*(l_yy +l_zz);
        val[M01]=2*(l_xy -l_zw);
        val[M02]=2*(l_xz +l_yw);
        val[M10]=2*(l_xy +l_zw);
        val[M11]=1-2*(l_xx +l_zz);
        val[M12]=2*(l_yz -l_xw);
        val[M20]=2*(l_xz -l_yw);
        val[M21]=2*(l_yz +l_xw);
        val[M22]=1-2*(l_xx +l_yy);
        val[M33]=1;
        return this;
    }

    /**
     * Sets the four columns of the matrix which correspond to the
     * x-, y- and z-axis of the vector space this matrix creates as
     * well as the 4th column representing the translation of any
     * point that is multiplied by this matrix.
     * 
     * @param xAxis The x-axis
     * @param yAxis The y-axis
     * @param zAxis The z-axis
     * @param pos The translation vector
     */
    public void set( Vector3 xAxis, Vector3 yAxis, Vector3 zAxis, Vector3 pos )
    {
    	val[M00] = xAxis.x;
    	val[M01] = xAxis.y;
    	val[M02] = xAxis.z;
    	val[M10] = yAxis.x;
    	val[M11] = yAxis.y;
    	val[M12] = yAxis.z;
    	val[M20] = -zAxis.x;
    	val[M21] = -zAxis.y;
    	val[M22] = -zAxis.z;
    	val[M03] = pos.x;
    	val[M13] = pos.y;
    	val[M23] = pos.z;
    	val[M30] = 0;
    	val[M31] = 0;
    	val[M32] = 0;
    	val[M33] = 1;
    }

    /**
     * @return a copy of this matrix
     */
    public  Matrix4 cpy()
    {
        return new Matrix4(this);
    }

    /**
     * Adds a translational component to the matrix in the 4th column. 
     * The other columns are untouched.
     * 
     * @param vector The translation vector
     * @return This matrix for chaining
     */
    public  Matrix4 trn(Vector3 vector)
    {
        val[M03]+=vector.x;
        val[M13]+=vector.y;
        val[M23]+=vector.z;
        return this;
    }
    
    /**
     * Adds a translational component to the matrix in the 4th column.
     * The other columns are untouched.
     * 
     * @param x The x-component of the translation vector
     * @param y The y-component of the translation vector
     * @param z The z-component of the translation vector
     * @return This matrix for chaining
     */
    public Matrix4 trn(float x, float y, float z)
    {
        val[M03]+=x;
        val[M13]+=y;
        val[M23]+=z;
        return this;
    }

    /**
     * @return the backing float array
     */
    public  float[] getValues()
    {
        return val;
    }

    /**
     * Multiplies this matrix with the given matrix, storing
     * the result in this matrix.
     * 
     * @param matrix The other matrix
     * @return This matrix for chaining.
     */
    public  Matrix4 mul(Matrix4 matrix)
    {
        tmp[M00]=val[M00]*matrix.val[M00] + val[M01]*matrix.val[M10] + val[M02]*matrix.val[M20] + val[M03]*matrix.val[M30];
        tmp[M01]=val[M00]*matrix.val[M01] + val[M01]*matrix.val[M11] + val[M02]*matrix.val[M21] + val[M03]*matrix.val[M31];
        tmp[M02]=val[M00]*matrix.val[M02] + val[M01]*matrix.val[M12] + val[M02]*matrix.val[M22] + val[M03]*matrix.val[M32];
        tmp[M03]=val[M00]*matrix.val[M03] + val[M01]*matrix.val[M13] + val[M02]*matrix.val[M23] + val[M03]*matrix.val[M33];
        tmp[M10]=val[M10]*matrix.val[M00] + val[M11]*matrix.val[M10] + val[M12]*matrix.val[M20] + val[M13]*matrix.val[M30];
        tmp[M11]=val[M10]*matrix.val[M01] + val[M11]*matrix.val[M11] + val[M12]*matrix.val[M21] + val[M13]*matrix.val[M31];
        tmp[M12]=val[M10]*matrix.val[M02] + val[M11]*matrix.val[M12] + val[M12]*matrix.val[M22] + val[M13]*matrix.val[M32];
        tmp[M13]=val[M10]*matrix.val[M03] + val[M11]*matrix.val[M13] + val[M12]*matrix.val[M23] + val[M13]*matrix.val[M33];
        tmp[M20]=val[M20]*matrix.val[M00] + val[M21]*matrix.val[M10] + val[M22]*matrix.val[M20] + val[M23]*matrix.val[M30];
        tmp[M21]=val[M20]*matrix.val[M01] + val[M21]*matrix.val[M11] + val[M22]*matrix.val[M21] + val[M23]*matrix.val[M31];
        tmp[M22]=val[M20]*matrix.val[M02] + val[M21]*matrix.val[M12] + val[M22]*matrix.val[M22] + val[M23]*matrix.val[M32];
        tmp[M23]=val[M20]*matrix.val[M03] + val[M21]*matrix.val[M13] + val[M22]*matrix.val[M23] + val[M23]*matrix.val[M33];
        tmp[M30]=val[M30]*matrix.val[M00] + val[M31]*matrix.val[M10] + val[M32]*matrix.val[M20] + val[M33]*matrix.val[M30];
        tmp[M31]=val[M30]*matrix.val[M01] + val[M31]*matrix.val[M11] + val[M32]*matrix.val[M21] + val[M33]*matrix.val[M31];
        tmp[M32]=val[M30]*matrix.val[M02] + val[M31]*matrix.val[M12] + val[M32]*matrix.val[M22] + val[M33]*matrix.val[M32];
        tmp[M33]=val[M30]*matrix.val[M03] + val[M31]*matrix.val[M13] + val[M32]*matrix.val[M23] + val[M33]*matrix.val[M33];
        return this.set(tmp);
    }

    /**
     * Transposes the matrix
     * 
     * @return This matrix for chaining
     */
    public  Matrix4 tra()
    {
        tmp[M00]=val[M00]; tmp[M01]=val[M10]; tmp[M02]=val[M20]; tmp[M03]=val[M30];
        tmp[M10]=val[M01]; tmp[M11]=val[M11]; tmp[M12]=val[M21]; tmp[M13]=val[M31];
        tmp[M20]=val[M02]; tmp[M21]=val[M12]; tmp[M22]=val[M22]; tmp[M23]=val[M32];
        tmp[M30]=val[M03]; tmp[M31]=val[M13]; tmp[M32]=val[M23]; tmp[M33]=val[M33];
        return this.set(tmp);
    }

    /**
     * Sets the matrix to an identity matrix
     * 
     * @return This matrix for chaining
     */
    public  Matrix4 idt()
    {
        val[M00]=1;  val[M01]=0;  val[M02]=0;  val[M03]=0;
        val[M10]=0;  val[M11]=1;  val[M12]=0;  val[M13]=0;
        val[M20]=0;  val[M21]=0;  val[M22]=1;  val[M23]=0;
        val[M30]=0;  val[M31]=0;  val[M32]=0;  val[M33]=1;
        return this;
    }

    /**
     * Inverts the matrix. Throws a RuntimeException in case the 
     * matrix is not invertible. Stores the result in this matrix
     *  
     * @return This matrix for chaining
     */
    public  Matrix4 inv()
    {
        float l_det=this.det();
        if(l_det==0f) throw new RuntimeException("non-invertible matrix");
        tmp[M00]=val[M12]*val[M23]*val[M31] - val[M13]*val[M22]*val[M31] + val[M13]*val[M21]*val[M32] - val[M11]*val[M23]*val[M32] - val[M12]*val[M21]*val[M33] + val[M11]*val[M22]*val[M33];
        tmp[M01]=val[M03]*val[M22]*val[M31] - val[M02]*val[M23]*val[M31] - val[M03]*val[M21]*val[M32] + val[M01]*val[M23]*val[M32] + val[M02]*val[M21]*val[M33] - val[M01]*val[M22]*val[M33];
        tmp[M02]=val[M02]*val[M13]*val[M31] - val[M03]*val[M12]*val[M31] + val[M03]*val[M11]*val[M32] - val[M01]*val[M13]*val[M32] - val[M02]*val[M11]*val[M33] + val[M01]*val[M12]*val[M33];
        tmp[M03]=val[M03]*val[M12]*val[M21] - val[M02]*val[M13]*val[M21] - val[M03]*val[M11]*val[M22] + val[M01]*val[M13]*val[M22] + val[M02]*val[M11]*val[M23] - val[M01]*val[M12]*val[M23];
        tmp[M10]=val[M13]*val[M22]*val[M30] - val[M12]*val[M23]*val[M30] - val[M13]*val[M20]*val[M32] + val[M10]*val[M23]*val[M32] + val[M12]*val[M20]*val[M33] - val[M10]*val[M22]*val[M33];
        tmp[M11]=val[M02]*val[M23]*val[M30] - val[M03]*val[M22]*val[M30] + val[M03]*val[M20]*val[M32] - val[M00]*val[M23]*val[M32] - val[M02]*val[M20]*val[M33] + val[M00]*val[M22]*val[M33];
        tmp[M12]=val[M03]*val[M12]*val[M30] - val[M02]*val[M13]*val[M30] - val[M03]*val[M10]*val[M32] + val[M00]*val[M13]*val[M32] + val[M02]*val[M10]*val[M33] - val[M00]*val[M12]*val[M33];
        tmp[M13]=val[M02]*val[M13]*val[M20] - val[M03]*val[M12]*val[M20] + val[M03]*val[M10]*val[M22] - val[M00]*val[M13]*val[M22] - val[M02]*val[M10]*val[M23] + val[M00]*val[M12]*val[M23];
        tmp[M20]=val[M11]*val[M23]*val[M30] - val[M13]*val[M21]*val[M30] + val[M13]*val[M20]*val[M31] - val[M10]*val[M23]*val[M31] - val[M11]*val[M20]*val[M33] + val[M10]*val[M21]*val[M33];
        tmp[M21]=val[M03]*val[M21]*val[M30] - val[M01]*val[M23]*val[M30] - val[M03]*val[M20]*val[M31] + val[M00]*val[M23]*val[M31] + val[M01]*val[M20]*val[M33] - val[M00]*val[M21]*val[M33];
        tmp[M22]=val[M01]*val[M13]*val[M30] - val[M03]*val[M11]*val[M30] + val[M03]*val[M10]*val[M31] - val[M00]*val[M13]*val[M31] - val[M01]*val[M10]*val[M33] + val[M00]*val[M11]*val[M33];
        tmp[M23]=val[M03]*val[M11]*val[M20] - val[M01]*val[M13]*val[M20] - val[M03]*val[M10]*val[M21] + val[M00]*val[M13]*val[M21] + val[M01]*val[M10]*val[M23] - val[M00]*val[M11]*val[M23];
        tmp[M30]=val[M12]*val[M21]*val[M30] - val[M11]*val[M22]*val[M30] - val[M12]*val[M20]*val[M31] + val[M10]*val[M22]*val[M31] + val[M11]*val[M20]*val[M32] - val[M10]*val[M21]*val[M32];
        tmp[M31]=val[M01]*val[M22]*val[M30] - val[M02]*val[M21]*val[M30] + val[M02]*val[M20]*val[M31] - val[M00]*val[M22]*val[M31] - val[M01]*val[M20]*val[M32] + val[M00]*val[M21]*val[M32];
        tmp[M32]=val[M02]*val[M11]*val[M30] - val[M01]*val[M12]*val[M30] - val[M02]*val[M10]*val[M31] + val[M00]*val[M12]*val[M31] + val[M01]*val[M10]*val[M32] - val[M00]*val[M11]*val[M32];
        tmp[M33]=val[M01]*val[M12]*val[M20] - val[M02]*val[M11]*val[M20] + val[M02]*val[M10]*val[M21] - val[M00]*val[M12]*val[M21] - val[M01]*val[M10]*val[M22] + val[M00]*val[M11]*val[M22];
        this.set(tmp);
        val[M00]/=l_det; val[M01]/=l_det; val[M02]/=l_det; val[M03]/=l_det;
        val[M10]/=l_det; val[M11]/=l_det; val[M12]/=l_det; val[M13]/=l_det;
        val[M20]/=l_det; val[M21]/=l_det; val[M22]/=l_det; val[M23]/=l_det;
        val[M30]/=l_det; val[M31]/=l_det; val[M32]/=l_det; val[M33]/=l_det;
        return this;
    }

    /**
     * @return The determinant of this matrix
     */
    public  float det()
    {
        return
        val[M30] * val[M21] * val[M12] * val[M03]-val[M20] * val[M31] * val[M12] * val[M03]-val[M30] * val[M11] * val[M22] * val[M03]+val[M10] * val[M31] * val[M22] * val[M03]+
        val[M20] * val[M11] * val[M32] * val[M03]-val[M10] * val[M21] * val[M32] * val[M03]-val[M30] * val[M21] * val[M02] * val[M13]+val[M20] * val[M31] * val[M02] * val[M13]+
        val[M30] * val[M01] * val[M22] * val[M13]-val[M00] * val[M31] * val[M22] * val[M13]-val[M20] * val[M01] * val[M32] * val[M13]+val[M00] * val[M21] * val[M32] * val[M13]+
        val[M30] * val[M11] * val[M02] * val[M23]-val[M10] * val[M31] * val[M02] * val[M23]-val[M30] * val[M01] * val[M12] * val[M23]+val[M00] * val[M31] * val[M12] * val[M23]+
        val[M10] * val[M01] * val[M32] * val[M23]-val[M00] * val[M11] * val[M32] * val[M23]-val[M20] * val[M11] * val[M02] * val[M33]+val[M10] * val[M21] * val[M02] * val[M33]+
        val[M20] * val[M01] * val[M12] * val[M33]-val[M00] * val[M21] * val[M12] * val[M33]-val[M10] * val[M01] * val[M22] * val[M33]+val[M00] * val[M11] * val[M22] * val[M33];
    }

    /**
     * Sets the matrix to a projection matrix with a near- and
     * far plane, a field of view in degrees and an aspect ratio.
     * 
     * @param near The near plane
     * @param far The far plane
     * @param fov The field of view in degrees
     * @param aspectRatio The aspect ratio
     * @return This matrix for chaining
     */
    public  Matrix4 setToProjection(float near, float far, float fov, float aspectRatio)
    {
        this.idt();
        float l_fd=(float)(1.0/Math.tan((fov*(Math.PI/180))/2.0));
        float l_a1=-(far+near)/(far-near);
        float l_a2=-(2*far*near)/(far-near);
        val[M00]=l_fd/aspectRatio;  val[M10]=0;       val[M20]=0;     val[M30]=0;
        val[M01]=0;           val[M11]=l_fd;    val[M21]=0;     val[M31]=0;
        val[M02]=0;           val[M12]=0;       val[M22]=l_a1;  val[M32]=-1;
        val[M03]=0;           val[M13]=0;       val[M23]=l_a2;  val[M33]=0;
        return this;
    }

    /**
     * Sets this matrix to an orthographic projection matrix with the
     * origin at (x,y) extending by width and height. The near plane
     * is set to 0, the far plane is set to 1.
     * 
     * @param x The x-coordinate of the origin
     * @param y The y-coordinate of the origin
     * @param width The width
     * @param height The height
     * @return This matrix for chaining
     */
    public  Matrix4 setToOrtho2D( float x, float y, float width, float height )
    {
    	setToOrtho( 0, width, 0, height, 0, 1 );
    	return this;
    }
    
    /**
     * Sets this matrix to an orthographic projection matrix with the
     * origin at (x,y) extending by width and height, having a near
     * and far plane.
     * 
     * @param x The x-coordinate of the origin
     * @param y The y-coordinate of the origin
     * @param width The width
     * @param height The height
     * @param near The near plane
     * @param far The far plane
     * @return This matrix for chaining
     */
    public  Matrix4 setToOrtho2D( float x, float y, float width, float height, float near, float far )
    {
    	setToOrtho( 0, width, 0, height, near, far );
    	return this;
    }

    /**
     * Sets the matrix to an orthographic projection like glOrtho (http://www.opengl.org/sdk/docs/man/xhtml/glOrtho.xml)
     * following the OpenGL equivalent
     * 
     * @param left The left clipping plane
     * @param right The right clipping plane
     * @param bottom The bottom clipping plane
     * @param top The top clipping plane
     * @param near The near clipping plane
     * @param far The far clipping plane
     * @return This matrix for chaining
     */
    public  Matrix4 setToOrtho( float left, float right, float bottom, float top, float near, float far )
    {
    
    	this.idt();
    	float x_orth = 2 / ( right - left );
    	float y_orth = 2 / ( top - bottom );
    	float z_orth = -2 / ( far - near );
    	
    	float tx = -( right + left ) / ( right - left );
    	float ty = -( top + bottom ) / ( top - bottom );
    	float tz = ( far + near ) / ( far - near );
    	
        val[M00]=x_orth;	  val[M10]=0;       val[M20]=0;     val[M30]=0;
        val[M01]=0;           val[M11]=y_orth; 	val[M21]=0;     val[M31]=0;
        val[M02]=0;           val[M12]=0;       val[M22]=z_orth;val[M32]=0;
        val[M03]=tx;           val[M13]=ty;       val[M23]=tz;  	val[M33]=1;
    	
    	return this;    	
    }
    
    /**
     * Sets this matrix to a translation matrix, overwriting it first
     * by an identity matrix and then setting the 4th column to the
     * translation vector.
     * 
     * @param vector The translation vector
     * @return This matrix for chaining
     */
    public  Matrix4 setToTranslation(Vector3 vector)
    {
        this.idt();
        val[M03]=vector.x;
        val[M13]=vector.y;
        val[M23]=vector.z;
        return this;
    }
    
    /**
     * Sets this matrix to a translation matrix, overwriting it first
     * by an identity matrix and then setting the 4th column to the
     * translation vector.
     * 
     * @param x The x-component of the translation vector
     * @param y The y-component of the translation vector
     * @param z The z-component of the translation vector
     * @return This matrix for chaining
     */
    public  Matrix4 setToTranslation(float x, float y, float z)
    {
    	idt();
        val[M03]=x;
        val[M13]=y;
        val[M23]=z;
        return this;
    }

    /**
     * Sets this matrix to a translation and scaling matrix by first
     * overwritting it with an identity and then setting the translation
     * vector in the 4th column and the scaling vector in the diagonal.
     * 
     * @param translation The translation vector
     * @param scaling The scaling vector
     * @return This matrix for chaining
     */
    public  Matrix4 setToTranslationAndScaling(Vector3 translation,Vector3 scaling)
    {
        idt();
        val[M03]=translation.x;
        val[M13]=translation.y;
        val[M23]=translation.z;
        val[M00]=scaling.x;
        val[M11]=scaling.y;
        val[M22]=scaling.z;
        return this;
    }
    
    /**
     * Sets this matrix to a translation and scaling matrix by first
     * overwritting it with an identity and then setting the translation
     * vector in the 4th column and the scaling vector in the diagonal.
     * 
     * @param translationX The x-component of the translation vector
     * @param translationY The y-component of the translation vector
     * @param translationZ The z-component of the translation vector
     * @param scalingX The x-component of the scaling vector
     * @param scalingY The x-component of the scaling vector
     * @param scalingZ The x-component of the scaling vector
     * @return This matrix for chaining
     */
    public  Matrix4 setToTranslationAndScaling(float translationX, float translationY, float translationZ, float scalingX, float scalingY, float scalingZ)
    {
        this.idt();
        val[M03]=translationX;
        val[M13]=translationY;
        val[M23]=translationZ;
        val[M00]=scalingX;
        val[M11]=scalingY;
        val[M22]=scalingZ;
        return this;
    }

    static Quaternion quat = new Quaternion();
    
    /**
     * Sets the matrix to a rotation matrix around the given
     * axis.
     * 
     * @param axis The axis
     * @param angle The angle in degrees
     * @return This matrix for chaining
     */
    public  Matrix4 setToRotation(Vector3 axis, float angle)
    {
        idt();
        if(angle==0) return this;
        return this.set(quat.set(axis,angle));
    }    
    
    /**
     * Sets this matrix to a rotation matrix from the given euler
     * angles. 
     * @param yaw the yaw in degrees
     * @param pitch the pitch in degress
     * @param roll the roll in degrees
     * @return this matrix
     */
    public Matrix4 setFromEulerAngles( float yaw, float pitch, float roll )
    {
    	idt();
    	quat.setEulerAngles(yaw, pitch, roll);
    	return this.set(quat);
    }

    /**
     * Sets this matrix to a scaling matrix
     * 
     * @param vector The scaling vector
     * @return This matrix for chaining.
     */
    public  Matrix4 setToScaling(Vector3 vector)
    {
        idt();
        val[M00]=vector.x;
        val[M11]=vector.y;
        val[M22]=vector.z;
        return this;
    }
    
    /**
     * Sets this matrix to a scaling matrix
     * 
     * @param x The x-component of the scaling vector
     * @param y The y-component of the scaling vector
     * @param z The z-component of the scaling vector
     * @return This matrix for chaining.
     */
    public Matrix4 setToScaling(float x, float y, float z)
    {
    	idt();
        val[M00]=x;
        val[M11]=y;
        val[M22]=z;
        return this;
    }      

    static Vector3 l_vez = new Vector3( );
    static Vector3 l_vex = new Vector3( );
    static Vector3 l_vey = new Vector3( );
    
    /**
     * Sets the matrix to a look at matrix with a direction
     * and an up vector. Multiply with a translation matrix
     * to get a camera model view matrix.
     * 
     * @param direction The direction vector
     * @param up The up vector
     * @return This matrix for chaining
     */
    public  Matrix4 setToLookAt(Vector3 direction, Vector3 up)
    {
		l_vez.set(direction).nor();
		l_vex.set(direction).nor();
        l_vex.crs(up).nor();
		l_vey.set(l_vex).crs(l_vez).nor();
		idt();
		val[M00]=l_vex.x;
		val[M01]=l_vex.y;
		val[M02]=l_vex.z;
		val[M10]=l_vey.x;
		val[M11]=l_vey.y;
		val[M12]=l_vey.z;
		val[M20]=-l_vez.x;
		val[M21]=-l_vez.y;
		val[M22]=-l_vez.z;    	        	
    	
        return this;
    }          
    
    /**
     * Sets this matrix to a look at matrix with the given
     * position, target and up vector.
     * 
     * @param position the position
     * @param target the target
     * @param up the up vector
     * @return this matrix
     */
    static final Vector3 tmpVec = new Vector3();   
    static final Matrix4 tmpMat = new Matrix4();
    public Matrix4 setToLookAt( Vector3 position, Vector3 target, Vector3 up )
    {
    	tmpVec.set(target).sub(position);
    	setToLookAt( tmpVec, up);    	   
    	this.mul( tmpMat.setToTranslation( position.tmp().mul(-1) ) );
    	
    	return this;
    }    

    static Vector3 right = new Vector3();
    static Vector3 tmpForward = new Vector3();
    static Vector3 tmpUp = new Vector3();    
    public Matrix4 setToWorld( Vector3 position, Vector3 forward, Vector3 up )
    { 
    	tmpForward.set(forward).nor();
    	right.set(tmpForward).crs(up).nor();
        tmpUp.set(right).crs(tmpForward).nor();
        
        val[M00] = right.x;
        val[M01] = right.y;
        val[M02] = right.z;
        
        val[M10] = tmpUp.x;
        val[M11] = tmpUp.y;
        val[M12] = tmpUp.z;
        
        val[M20] = -tmpForward.x;
        val[M21] = -tmpForward.y;
        val[M22] = -tmpForward.z;
    	
    	val[M30] = position.x;
    	val[M31] = position.y;
    	val[M32] = position.z;
    	
    	val[M03] = val[M13] = val[M23] = 0; val[M33] = 1.0f;
    	return this;
    }
    
    /**
     * {@inheritDoc}
     */
    public  String toString()
    {
        return 
               "["+val[M00]+"|"+val[M01]+"|"+val[M02]+"|"+val[M03]+"]\n"+
               "["+val[M10]+"|"+val[M11]+"|"+val[M12]+"|"+val[M13]+"]\n"+
               "["+val[M20]+"|"+val[M21]+"|"+val[M22]+"|"+val[M23]+"]\n"+
               "["+val[M30]+"|"+val[M31]+"|"+val[M32]+"|"+val[M33]+"]\n";              
    }

    /**
     * Linearly interpolates between this matrix and the given matrix mixing by alpha
     * @param matrix the matrix
     * @param alpha the alpha value in the range [0,1]
     */
	public void lerp(Matrix4 matrix, float alpha) 
	{	
		for( int i = 0; i < 16; i++ )
			this.val[i] = this.val[i] * (1-alpha) + matrix.val[i] * alpha;
	}
}

