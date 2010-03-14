package com.badlogic.gdx2.backends.desktop;

import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.media.opengl.GL;

import com.badlogic.gdx2.graphics.GL11;


class JoglGL11 extends JoglGL10 implements GL11 
{
	double tmpDouble[] = new double[1000];
	float tmpFloat[] = new float[1000];
	
	public JoglGL11(GL gl) 
	{
		super(gl);
	}

	@Override
	public void glBindBuffer(int target, int buffer) 
	{	
		gl.glBindBuffer( target, buffer );
	}

	@Override
	public void glBufferData(int target, int size, Buffer data, int usage) 
	{	
		gl.glBufferData( target, size, data, usage );
	}

	@Override
	public void glBufferSubData(int target, int offset, int size, Buffer data) 
	{	
		gl.glBufferSubData( target, offset, size, data );
	}

	@Override
	public void glClipPlanef(int plane, float[] equation, int offset) 
	{			
		throw new UnsupportedOperationException( "not implemented" );
	}

	@Override
	public void glClipPlanef(int plane, FloatBuffer equation) 
	{	
		throw new UnsupportedOperationException( "not implemented" );
	}

	@Override
	public void glClipPlanex(int plane, int[] equation, int offset) 
	{
		throw new UnsupportedOperationException( "not implemented" );		
	}

	@Override
	public void glClipPlanex(int plane, IntBuffer equation) 
	{
		throw new UnsupportedOperationException( "not implemented" );		
	}

	@Override
	public void glColor4ub(byte red, byte green, byte blue, byte alpha) 
	{
		throw new UnsupportedOperationException( "not implemented" );		
	}

	@Override
	public void glDeleteBuffers(int n, int[] buffers, int offset) 
	{
		gl.glDeleteBuffers( n, buffers, offset );		
	}

	@Override
	public void glDeleteBuffers(int n, IntBuffer buffers) 
	{	
		gl.glDeleteBuffers( n, buffers );
	}

	@Override
	public void glGenBuffers(int n, int[] buffers, int offset) 
	{	
		gl.glGenBuffers( n, buffers, offset );
	}

	@Override
	public void glGenBuffers(int n, IntBuffer buffers) 
	{	
		gl.glGenBuffers( n, buffers );
	}

	@Override
	public void glGetBooleanv(int pname, boolean[] params, int offset) 
	{		
		throw new UnsupportedOperationException( "not implemented" );		
	}

	@Override
	public void glGetBooleanv(int pname, IntBuffer params) 
	{
		throw new UnsupportedOperationException( "not implemented" );		
	}

	@Override
	public void glGetBufferParameteriv(int target, int pname, int[] params,	int offset) 
	{
		gl.glGetBufferParameteriv( target, pname, params, offset );
	}

	@Override
	public void glGetBufferParameteriv(int target, int pname, IntBuffer params) 
	{
		gl.glGetBufferParameteriv(target, pname, params );
	}

	@Override
	public void glGetClipPlanef(int pname, float[] eqn, int offset) 
	{
		throw new UnsupportedOperationException( "not implemented" );		
	}

	@Override
	public void glGetClipPlanef(int pname, FloatBuffer eqn) 
	{
		throw new UnsupportedOperationException( "not implemented" );		
	}

	@Override
	public void glGetClipPlanex(int pname, int[] eqn, int offset) 
	{
		throw new UnsupportedOperationException( "not implemented" );		
	}

	@Override
	public void glGetClipPlanex(int pname, IntBuffer eqn) 
	{
		throw new UnsupportedOperationException( "not implemented" );		
	}

	@Override
	public void glGetFixedv(int pname, int[] params, int offset) 
	{
		throw new UnsupportedOperationException( "not implemented" );		
	}

	@Override
	public void glGetFixedv(int pname, IntBuffer params) 
	{
		throw new UnsupportedOperationException( "not implemented" );		
	}

	@Override
	public void glGetFloatv(int pname, float[] params, int offset) 
	{
		gl.glGetFloatv( pname, params, offset );		
	}

	@Override
	public void glGetFloatv(int pname, FloatBuffer params) 
	{
		gl.glGetFloatv( pname, params );		
	}

	@Override
	public void glGetLightfv(int light, int pname, float[] params, int offset) 
	{
		gl.glGetLightfv( light, pname, params, offset );
	}

	@Override
	public void glGetLightfv(int light, int pname, FloatBuffer params) 
	{
		gl.glGetLightfv( light, pname, params );		
	}

	@Override
	public void glGetLightxv(int light, int pname, int[] params, int offset) 
	{
		throw new UnsupportedOperationException( "not implemented" );		
	}

	@Override
	public void glGetLightxv(int light, int pname, IntBuffer params) 
	{
		throw new UnsupportedOperationException( "not implemented" );		
	}

	@Override
	public void glGetMaterialfv(int face, int pname, float[] params, int offset) 
	{
		gl.glGetMaterialfv( face, pname, params, offset );		
	}

	@Override
	public void glGetMaterialfv(int face, int pname, FloatBuffer params) 
	{
		gl.glGetMaterialfv( face, pname, params );	
	}

	@Override
	public void glGetMaterialxv(int face, int pname, int[] params, int offset) 
	{
		throw new UnsupportedOperationException( "not implemented" );		
	}

	@Override
	public void glGetMaterialxv(int face, int pname, IntBuffer params) 
	{
		throw new UnsupportedOperationException( "not implemented" );
	}

	@Override
	public void glGetPointerv(int pname, Buffer[] params) 
	{
		throw new UnsupportedOperationException( "not implemented" );
	}

	@Override
	public void glGetTexEnviv(int env, int pname, int[] params, int offset) 
	{
		gl.glGetTexEnviv( env, pname, params, offset );
	}

	@Override
	public void glGetTexEnviv(int env, int pname, IntBuffer params) 
	{
		gl.glGetTexEnviv( env, pname, params );
	}

	@Override
	public void glGetTexEnvxv(int env, int pname, int[] params, int offset) 
	{
		throw new UnsupportedOperationException( "not implemented" );			
	}

	@Override
	public void glGetTexEnvxv(int env, int pname, IntBuffer params) 
	{
		throw new UnsupportedOperationException( "not implemented" );		
	}

	@Override
	public void glGetTexParameterfv(int target, int pname, float[] params, int offset) 
	{
		gl.glGetTexParameterfv( target, pname, params, offset );		
	}

	@Override
	public void glGetTexParameterfv(int target, int pname, FloatBuffer params) 
	{
		gl.glGetTexParameterfv( target, pname, params );		
	}

	@Override
	public void glGetTexParameteriv(int target, int pname, int[] params, int offset) 
	{
		gl.glGetTexParameteriv( target, pname, params, offset );		
	}

	@Override
	public void glGetTexParameteriv(int target, int pname, IntBuffer params) 
	{
		gl.glGetTexParameteriv( target, pname, params );		
	}

	@Override
	public void glGetTexParameterxv(int target, int pname, int[] params, int offset) 
	{
		throw new UnsupportedOperationException( "not implemented" );		
	}

	@Override
	public void glGetTexParameterxv(int target, int pname, IntBuffer params) 
	{
		throw new UnsupportedOperationException( "not implemented" );		
	}

	@Override
	public boolean glIsBuffer(int buffer) 
	{
		return gl.glIsBuffer( buffer );
	}

	@Override
	public boolean glIsEnabled(int cap) 
	{	
		return gl.glIsEnabled( cap );
	}

	@Override
	public boolean glIsTexture(int texture) 
	{	
		return gl.glIsTexture( texture );
	}

	@Override
	public void glPointParameterf(int pname, float param) 
	{	
		gl.glPointParameterf( pname, param );
	}

	@Override
	public void glPointParameterfv(int pname, float[] params, int offset) 
	{	
		gl.glPointParameterfv( pname, params, offset );
	}

	@Override
	public void glPointParameterfv(int pname, FloatBuffer params) 
	{	
		gl.glPointParameterfv( pname, params );
	}

	@Override
	public void glPointParameterx(int pname, int param) 
	{	
		gl.glPointParameterf(pname, FIXED_TO_FLOAT * param );
	}

	@Override
	public void glPointParameterxv(int pname, int[] params, int offset) 
	{
		if( tmpFloat.length < params.length )
			tmpFloat = new float[params.length];
		for( int i = 0; i < params.length; i++ )
			tmpFloat[i] = FIXED_TO_FLOAT * params[i];
		gl.glPointParameterfv( pname, tmpFloat, offset );	
	}

	@Override
	public void glPointParameterxv(int pname, IntBuffer params) 
	{	
		if( tmpFloat.length < params.capacity() )
			tmpFloat = new float[params.capacity()];
		int i = 0;
		while( params.hasRemaining() )
			tmpFloat[i] = FIXED_TO_FLOAT * params.get();
		gl.glPointParameterfv( pname, tmpFloat, 0 );
	}

	@Override
	public void glPointSizePointerOES(int type, int stride, Buffer pointer) 
	{	
		throw new UnsupportedOperationException( "not implemented" );
	}

	@Override
	public void glTexEnvi(int target, int pname, int param) 
	{	
		gl.glTexEnvi( target, pname, param );
	}

	@Override
	public void glTexEnviv(int target, int pname, int[] params, int offset) 
	{	
		gl.glTexEnviv( target, pname, params, offset );
	}

	@Override
	public void glTexEnviv(int target, int pname, IntBuffer params) 
	{	
		gl.glTexEnviv( target, pname, params );
	}

	@Override
	public void glTexParameterfv(int target, int pname, float[] params,	int offset) 
	{	
		gl.glTexParameterfv( target, pname, params, offset );
	}

	@Override
	public void glTexParameterfv(int target, int pname, FloatBuffer params) 
	{	
		gl.glTexParameterfv( target, pname, params );
	}

	@Override
	public void glTexParameteri(int target, int pname, int param) 
	{	
		gl.glTexParameteri( target, pname, param );
	}

	@Override
	public void glTexParameteriv(int target, int pname, int[] params, int offset) 
	{	
		gl.glTexParameteriv( target, pname, params, offset );
	}

	@Override
	public void glTexParameteriv(int target, int pname, IntBuffer params) 
	{	
		gl.glTexParameteriv( target, pname, params );
	}

	@Override
	public void glTexParameterxv(int target, int pname, int[] params, int offset) 
	{	
		if( tmpFloat.length < params.length )
			tmpFloat = new float[params.length];
		for( int i = 0; i < params.length; i++ )
			tmpFloat[i] = params[i];
		gl.glTexParameterfv( target, pname, tmpFloat, offset );
	}

	@Override
	public void glTexParameterxv(int target, int pname, IntBuffer params) 
	{
		if( tmpFloat.length < params.capacity() )
			tmpFloat = new float[params.capacity()];
		int i = 0;
		while( params.hasRemaining() )
			tmpFloat[i] = FIXED_TO_FLOAT * params.get();
		gl.glTexParameterfv( target, pname, tmpFloat, 0 );
	}
	
}
