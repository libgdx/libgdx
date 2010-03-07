package com.badlogic.gdx.math;

import java.io.Serializable;
import java.nio.FloatBuffer;


public final class Matrix implements Serializable
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
    final FloatBuffer buffer = FloatBuffer.wrap(val);
    
    public Matrix()
    {
        val[M00]=1f; val[M11]=1f; val[M22]=1f; val[M33]=1f;
    }

    public Matrix(Matrix a_matrix)
    {
        this.set(a_matrix);
    }

    public Matrix(float[] a_values)
    {
        this.set(a_values);
    }

    public Matrix(Quaternion a_qut)
    {
        this.set(a_qut);
    }

    public  Matrix set(Matrix a_matrix)
    {
        return this.set(a_matrix.val);
    }

    public  Matrix set(float[] a_val)
    {
        val[M00]=a_val[M00]; val[M10]=a_val[M10]; val[M20]=a_val[M20]; val[M30]=a_val[M30];
        val[M01]=a_val[M01]; val[M11]=a_val[M11]; val[M21]=a_val[M21]; val[M31]=a_val[M31];
        val[M02]=a_val[M02]; val[M12]=a_val[M12]; val[M22]=a_val[M22]; val[M32]=a_val[M32];
        val[M03]=a_val[M03]; val[M13]=a_val[M13]; val[M23]=a_val[M23]; val[M33]=a_val[M33];
        return this;
    }

    public  Matrix set(Quaternion a_qut)
    {
        // Compute quaternion factors
        float l_xx = a_qut.val[0]*a_qut.val[0];
        float l_xy = a_qut.val[0]*a_qut.val[1];
        float l_xz = a_qut.val[0]*a_qut.val[2];
        float l_xw = a_qut.val[0]*a_qut.val[3];
        float l_yy = a_qut.val[1]*a_qut.val[1];
        float l_yz = a_qut.val[1]*a_qut.val[2];
        float l_yw = a_qut.val[1]*a_qut.val[3];
        float l_zz = a_qut.val[2]*a_qut.val[2];
        float l_zw = a_qut.val[2]*a_qut.val[3];
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

    public void set( Vector xAxis, Vector yAxis, Vector zAxis, Vector pos )
    {
    	val[M00] = xAxis.getX();
    	val[M01] = xAxis.getY();
    	val[M02] = xAxis.getZ();
    	val[M10] = yAxis.getX();
    	val[M11] = yAxis.getY();
    	val[M12] = yAxis.getZ();
    	val[M20] = -zAxis.getX();
    	val[M21] = -zAxis.getY();
    	val[M22] = -zAxis.getZ();
    	val[M03] = pos.getX();
    	val[M13] = pos.getY();
    	val[M23] = pos.getZ();
    	val[M30] = 0;
    	val[M31] = 0;
    	val[M32] = 0;
    	val[M33] = 1;
    }

    public  Matrix cpy()
    {
        return new Matrix(this);
    }

    public  Matrix setToIdentity()
    {
        return this.idt();
    }

    public  Matrix trn(Vector a_vector)
    {
        val[M03]+=a_vector.getX();
        val[M13]+=a_vector.getY();
        val[M23]+=a_vector.getZ();
        return this;
    }
    
    public Matrix trn(float x, float y, float z)
    {
        val[M03]+=x;
        val[M13]+=y;
        val[M23]+=z;
        return this;
    }

    public  float[] getValues()
    {
        return val;
    }

    public  Matrix mul(Matrix a_mat)
    {
        tmp[M00]=val[M00]*a_mat.val[M00] + val[M01]*a_mat.val[M10] + val[M02]*a_mat.val[M20] + val[M03]*a_mat.val[M30];
        tmp[M01]=val[M00]*a_mat.val[M01] + val[M01]*a_mat.val[M11] + val[M02]*a_mat.val[M21] + val[M03]*a_mat.val[M31];
        tmp[M02]=val[M00]*a_mat.val[M02] + val[M01]*a_mat.val[M12] + val[M02]*a_mat.val[M22] + val[M03]*a_mat.val[M32];
        tmp[M03]=val[M00]*a_mat.val[M03] + val[M01]*a_mat.val[M13] + val[M02]*a_mat.val[M23] + val[M03]*a_mat.val[M33];
        tmp[M10]=val[M10]*a_mat.val[M00] + val[M11]*a_mat.val[M10] + val[M12]*a_mat.val[M20] + val[M13]*a_mat.val[M30];
        tmp[M11]=val[M10]*a_mat.val[M01] + val[M11]*a_mat.val[M11] + val[M12]*a_mat.val[M21] + val[M13]*a_mat.val[M31];
        tmp[M12]=val[M10]*a_mat.val[M02] + val[M11]*a_mat.val[M12] + val[M12]*a_mat.val[M22] + val[M13]*a_mat.val[M32];
        tmp[M13]=val[M10]*a_mat.val[M03] + val[M11]*a_mat.val[M13] + val[M12]*a_mat.val[M23] + val[M13]*a_mat.val[M33];
        tmp[M20]=val[M20]*a_mat.val[M00] + val[M21]*a_mat.val[M10] + val[M22]*a_mat.val[M20] + val[M23]*a_mat.val[M30];
        tmp[M21]=val[M20]*a_mat.val[M01] + val[M21]*a_mat.val[M11] + val[M22]*a_mat.val[M21] + val[M23]*a_mat.val[M31];
        tmp[M22]=val[M20]*a_mat.val[M02] + val[M21]*a_mat.val[M12] + val[M22]*a_mat.val[M22] + val[M23]*a_mat.val[M32];
        tmp[M23]=val[M20]*a_mat.val[M03] + val[M21]*a_mat.val[M13] + val[M22]*a_mat.val[M23] + val[M23]*a_mat.val[M33];
        tmp[M30]=val[M30]*a_mat.val[M00] + val[M31]*a_mat.val[M10] + val[M32]*a_mat.val[M20] + val[M33]*a_mat.val[M30];
        tmp[M31]=val[M30]*a_mat.val[M01] + val[M31]*a_mat.val[M11] + val[M32]*a_mat.val[M21] + val[M33]*a_mat.val[M31];
        tmp[M32]=val[M30]*a_mat.val[M02] + val[M31]*a_mat.val[M12] + val[M32]*a_mat.val[M22] + val[M33]*a_mat.val[M32];
        tmp[M33]=val[M30]*a_mat.val[M03] + val[M31]*a_mat.val[M13] + val[M32]*a_mat.val[M23] + val[M33]*a_mat.val[M33];
        return this.set(tmp);
    }

    public  Matrix tra()
    {
        tmp[M00]=val[M00]; tmp[M01]=val[M10]; tmp[M02]=val[M20]; tmp[M03]=val[M30];
        tmp[M10]=val[M01]; tmp[M11]=val[M11]; tmp[M12]=val[M21]; tmp[M13]=val[M31];
        tmp[M20]=val[M02]; tmp[M21]=val[M12]; tmp[M22]=val[M22]; tmp[M23]=val[M32];
        tmp[M30]=val[M03]; tmp[M31]=val[M13]; tmp[M32]=val[M23]; tmp[M33]=val[M33];
        return this.set(tmp);
    }

    public  Matrix idt()
    {
        val[M00]=1;  val[M01]=0;  val[M02]=0;  val[M03]=0;
        val[M10]=0;  val[M11]=1;  val[M12]=0;  val[M13]=0;
        val[M20]=0;  val[M21]=0;  val[M22]=1;  val[M23]=0;
        val[M30]=0;  val[M31]=0;  val[M32]=0;  val[M33]=1;
        return this;
    }

    public  Matrix inv()
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

    public  Matrix setToProjection(float a_near, float a_far, float a_fov, float a_asp)
    {
        this.setToIdentity();
        float l_fd=(float)(1.0/Math.tan((a_fov*(Math.PI/180))/2.0));
        float l_a1=-(a_far+a_near)/(a_far-a_near);
        float l_a2=-(2*a_far*a_near)/(a_far-a_near);
        val[M00]=l_fd/a_asp;  val[M10]=0;       val[M20]=0;     val[M30]=0;
        val[M01]=0;           val[M11]=l_fd;    val[M21]=0;     val[M31]=0;
        val[M02]=0;           val[M12]=0;       val[M22]=l_a1;  val[M32]=-1;
        val[M03]=0;           val[M13]=0;       val[M23]=l_a2;  val[M33]=0;
        return this;
    }

    public  Matrix setToOrtho2D( float x, float y, float width, float height )
    {
    	setToOrtho( 0, width, 0, height, 0, 1 );
    	return this;
    }
    
    public  Matrix setToOrtho2D( float x, float y, float width, float height, float near, float far )
    {
    	setToOrtho( 0, width, 0, height, near, far );
    	return this;
    }
    
    public  Matrix setToOrtho( float left, float right, float bottom, float top, float near, float far )
    {
    
    	this.setToIdentity();
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
    
    public  Matrix setToTranslation(Vector a_vector)
    {
        this.setToIdentity();
        val[M03]=a_vector.getX();
        val[M13]=a_vector.getY();
        val[M23]=a_vector.getZ();
        return this;
    }
    
    public  Matrix setToTranslation(float x, float y, float z)
    {
        this.setToIdentity();
        val[M03]=x;
        val[M13]=y;
        val[M23]=z;
        return this;
    }

    public  Matrix setToTranslationAndScaling(Vector a_trn,Vector a_scl)
    {
        this.setToIdentity();
        val[M03]=a_trn.getX();
        val[M13]=a_trn.getY();
        val[M23]=a_trn.getZ();
        val[M00]=a_scl.getX();
        val[M11]=a_scl.getY();
        val[M22]=a_scl.getZ();
        return this;
    }
    
    public  Matrix setToTranslationAndScaling(float tX, float tY, float tZ, float sX, float sY, float sZ)
    {
        this.setToIdentity();
        val[M03]=tX;
        val[M13]=tY;
        val[M23]=tZ;
        val[M00]=sX;
        val[M11]=sY;
        val[M22]=sZ;
        return this;
    }

    static Quaternion quat = new Quaternion();
    public  Matrix setToRotation(Vector a_axs, float a_ang)
    {
        this.setToIdentity();
        if(a_ang==0) return this;
        return this.set(quat.set(a_axs,a_ang));
    }    

    public  Matrix setToScaling(Vector a_vector)
    {
        this.setToIdentity();
        val[M00]=a_vector.getX();
        val[M11]=a_vector.getY();
        val[M22]=a_vector.getZ();
        return this;
    }
    
    public Matrix setToScaling(float x, float y, float z)
    {
        this.setToIdentity();
        val[M00]=x;
        val[M11]=y;
        val[M22]=z;
        return this;
    }
    
    public Matrix scale( Vector a_vector )
    {
    	val[M00]*=a_vector.getX();
    	val[M11]*=a_vector.getY();
    	val[M22]*=a_vector.getZ();
    	return this;
    }
    
    public Matrix scale( float x, float y, float z )
    {
    	val[M00]*=x;
    	val[M11]*=y;
    	val[M22]*=z;
    	return this;
    }

    static Vector l_vez = new Vector( );
    static Vector l_vex = new Vector( );
    static Vector l_vey = new Vector( );
    
    public  Matrix setToLookat(Vector a_dir, Vector a_up)
    {
		l_vez.set(a_dir).nor();
		l_vex.set(a_dir).nor();
        l_vex.crs(a_up).nor();
		l_vey.set(l_vex).crs(l_vez).nor();
        this.setToIdentity();
		val[M00]=l_vex.val[0];
		val[M01]=l_vex.val[1];
		val[M02]=l_vex.val[2];
		val[M10]=l_vey.val[0];
		val[M11]=l_vey.val[1];
		val[M12]=l_vey.val[2];
		val[M20]=-l_vez.val[0];
		val[M21]=-l_vez.val[1];
		val[M22]=-l_vez.val[2];    	        	
    	
        return this;
    }         

    public  Matrix setToRotateTo(Vector a_from, Vector a_to)
    {

        idt();
        
        float e, h;

        Vector v = a_from.cpy().crs(a_to);
        e = a_from.dot(a_to);
        
        h = 1.0f/(1.0f + e);
        float hvx = h * v.get(0);
        float hvz = h * v.get(2);
        float hvxy = hvx * v.get(1);
        float hvxz = hvx * v.get(2);
        float hvyz = hvz * v.get(1);
        val[M00] = e + hvx * v.get(0);
        val[M01] = hvxy - v.get(2);
        val[M02] = hvxz + v.get(1);
    
        val[M10] = hvxy + v.get(2);
        val[M11] = e + h * v.get(1) * v.get(1);
        val[M12] = hvyz - v.get(0);
    
        val[M20] = hvxz - v.get(1);
        val[M21] = hvyz + v.get(0);
        val[M22] = e + hvz * v.get(2);

        return this;
    }

    public  String toString()
    {
        return 
               "["+val[M00]+"|"+val[M01]+"|"+val[M02]+"|"+val[M03]+"]\n"+
               "["+val[M10]+"|"+val[M11]+"|"+val[M12]+"|"+val[M13]+"]\n"+
               "["+val[M20]+"|"+val[M21]+"|"+val[M22]+"|"+val[M23]+"]\n"+
               "["+val[M30]+"|"+val[M31]+"|"+val[M32]+"|"+val[M33]+"]\n";              
    }

    public  void setToRotateTo(Vector vector3) {
    }
    
    public FloatBuffer toFloatBuffer( )
    {    
    	return buffer;
    }
}

