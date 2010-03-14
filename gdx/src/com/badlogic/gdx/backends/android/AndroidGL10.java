package com.badlogic.gdx.backends.android;

import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import com.badlogic.gdx.graphics.GL10;

public class AndroidGL10 implements GL10
{
	private final javax.microedition.khronos.opengles.GL10 gl;
	
	public AndroidGL10( javax.microedition.khronos.opengles.GL10 gl )
	{
		this.gl = gl;		
	}

	@Override
	public final void glActiveTexture(int texture) 
	{
		gl.glActiveTexture( texture );		
	}

	@Override
	public final void glAlphaFunc(int func, float ref) 
	{
		gl.glAlphaFunc( func, ref );
	}

	@Override
	public final void glAlphaFuncx(int func, int ref) {
		gl.glAlphaFuncx( func, ref );		
	}

	@Override
	public final void glBindTexture(int target, int texture) 
	{	
		gl.glBindTexture( target, texture );
	}

	@Override
	public final void glBlendFunc(int sfactor, int dfactor) 
	{
		gl.glBlendFunc( sfactor, dfactor );		
	}

	@Override
	public final void glClear(int mask) 
	{	
		gl.glClear( mask );
	}

	@Override
	public final void glClearColor(float red, float green, float blue, float alpha) 
	{	
		gl.glClearColor( red, green, blue, alpha );
	}

	@Override
	public final void glClearColorx(int red, int green, int blue, int alpha) 
	{
		gl.glClearColorx( red, green, blue, alpha );
	}

	@Override
	public final void glClearDepthf(float depth) 
	{
		gl.glClearDepthf( depth );		
	}

	@Override
	public final void glClearDepthx(int depth) 
	{
		gl.glClearDepthx( depth );
	}

	@Override
	public final void glClearStencil(int s) 
	{	
		gl.glClearStencil( s );
	}

	@Override
	public final void glClientActiveTexture(int texture) 
	{	
		gl.glClientActiveTexture( texture );
	}

	@Override
	public final void glColor4f(float red, float green, float blue, float alpha) 
	{
		gl.glColor4f( red, green, blue, alpha );	
	}

	@Override
	public final void glColor4x(int red, int green, int blue, int alpha) 
	{	
		gl.glColor4x( red, green, blue, alpha );
	}

	@Override
	public final void glColorMask(boolean red, boolean green, boolean blue, boolean alpha) 
	{
		gl.glColorMask( red, green, blue, alpha );		
	}

	@Override
	public final void glColorPointer(int size, int type, int stride, Buffer pointer) 
	{	
		gl.glColorPointer( size, type, stride, pointer );
	}

	@Override
	public final void glCompressedTexImage2D(int target, int level,
			int internalformat, int width, int height, int border,
			int imageSize, Buffer data) 
	{	
		gl.glCompressedTexImage2D( target, level, internalformat, width, height, border, imageSize, data );
	}

	@Override
	public final void glCompressedTexSubImage2D(int target, int level, int xoffset,
			int yoffset, int width, int height, int format, int imageSize,
			Buffer data) 
	{	
		gl.glCompressedTexSubImage2D( target, level, xoffset, yoffset, width, height, format, imageSize, data );
	}

	@Override
	public final void glCopyTexImage2D(int target, int level, int internalformat,
			int x, int y, int width, int height, int border) 
	{
		gl.glCopyTexImage2D( target, level, internalformat, x, y, width, height, border );		
	}

	@Override
	public final void glCopyTexSubImage2D(int target, int level, int xoffset,
			int yoffset, int x, int y, int width, int height) 
	{	
		gl.glCopyTexSubImage2D( target, level, xoffset, yoffset, x, y, width, height );
	}

	@Override
	public final void glCullFace(int mode) 
	{	
		gl.glCullFace( mode );
	}

	@Override
	public final void glDeleteTextures(int n, IntBuffer textures) 
	{	
		gl.glDeleteTextures( n, textures );
	}

	@Override
	public final void glDepthFunc(int func) 
	{	
		gl.glDepthFunc( func );
	}

	@Override
	public final void glDepthMask(boolean flag) 
	{	
		gl.glDepthMask( flag );
	}

	@Override
	public final void glDepthRangef(float zNear, float zFar) 
	{	
		gl.glDepthRangef( zNear, zFar );
	}

	@Override
	public final void glDepthRangex(int zNear, int zFar) 
	{
		gl.glDepthRangex( zNear, zFar );
	}

	@Override
	public final void glDisable(int cap) 
	{	
		gl.glDisable( cap );
	}

	@Override
	public final void glDisableClientState(int array) 
	{	
		gl.glDisableClientState( array );
	}

	@Override
	public final void glDrawArrays(int mode, int first, int count) 
	{	
		gl.glDrawArrays( mode, first, count );
	}

	@Override
	public final void glDrawElements(int mode, int count, int type, Buffer indices) 
	{	
		gl.glDrawElements( mode, count, type, indices );
	}

	@Override
	public final void glEnable(int cap) 
	{	
		gl.glEnable( cap );
	}

	@Override
	public final void glEnableClientState(int array) 
	{	
		gl.glEnableClientState( array );
	}

	@Override
	public final void glFinish() 
	{	
		gl.glFinish();
	}

	@Override
	public final void glFlush() 
	{	
		gl.glFlush();
	}

	@Override
	public final void glFogf(int pname, float param) 
	{
		gl.glFogf( pname, param );
	}

	@Override
	public final void glFogfv(int pname, FloatBuffer params) 
	{			
		gl.glFogfv( pname, params );
	}

	@Override
	public final void glFogx(int pname, int param) 
	{	
		gl.glFogx( pname, param );
	}

	@Override
	public final void glFogxv(int pname, IntBuffer params) 
	{	
		gl.glFogxv( pname, params );
	}

	@Override
	public final void glFrontFace(int mode) 
	{	
		gl.glFrontFace( mode );
	}

	@Override
	public final void glFrustumf(float left, float right, float bottom, float top,
			float zNear, float zFar) 
	{	
		gl.glFrustumf( left, right, bottom, top, zNear, zFar );
	}

	@Override
	public final void glFrustumx(int left, int right, int bottom, int top, int zNear,
			int zFar) 
	{	
		gl.glFrustumx( left, right, bottom, top, zNear, zFar );
	}

	@Override
	public final void glGenTextures(int n, IntBuffer textures) 
	{	
		gl.glGenTextures( n, textures );
	}

	@Override
	public final int glGetError() 
	{
		return gl.glGetError();
	}

	@Override
	public final void glGetIntegerv(int pname, IntBuffer params) 
	{	
		gl.glGetIntegerv( pname, params );
	}

	@Override
	public final String glGetString(int name) 
	{	
		return gl.glGetString( name );
	}

	@Override
	public final void glHint(int target, int mode) 
	{	
		gl.glHint( target, mode );
	}

	@Override
	public final void glLightModelf(int pname, float param) 
	{	
		gl.glLightModelf( pname, param );
	}

	@Override
	public final void glLightModelfv(int pname, FloatBuffer params) 
	{
		gl.glLightModelfv( pname, params );
	}

	@Override
	public final void glLightModelx(int pname, int param) 
	{	
		gl.glLightModelx( pname, param );
	}

	@Override
	public final void glLightModelxv(int pname, IntBuffer params) 
	{
		gl.glLightModelxv( pname, params );		
	}

	@Override
	public final void glLightf(int light, int pname, float param) 
	{	
		gl.glLightf( light, pname, param );
	}

	@Override
	public final void glLightfv(int light, int pname, FloatBuffer params) 
	{	
		gl.glLightfv( light, pname, params );
	}

	@Override
	public final void glLightx(int light, int pname, int param) 
	{	
		gl.glLightx( light, pname, param );
	}

	@Override
	public final void glLightxv(int light, int pname, IntBuffer params) 
	{	
		gl.glLightxv( light, pname, params );
	}

	@Override
	public final void glLineWidth(float width) 
	{	
		gl.glLineWidth( width );
	}

	@Override
	public final void glLineWidthx(int width) 
	{	
		gl.glLineWidthx( width );
	}

	@Override
	public final void glLoadIdentity() 
	{	
		gl.glLoadIdentity();
	}

	@Override
	public final void glLoadMatrixf(FloatBuffer m) 
	{	
		gl.glLoadMatrixf( m );
	}

	@Override
	public final void glLoadMatrixx(IntBuffer m) 
	{	
		gl.glLoadMatrixx( m );
	}

	@Override
	public final void glLogicOp(int opcode) 
	{	
		gl.glLogicOp( opcode );
	}

	@Override
	public final void glMaterialf(int face, int pname, float param) 
	{	
		gl.glMaterialf( face, pname, param );
	}

	@Override
	public final void glMaterialfv(int face, int pname, FloatBuffer params) 
	{	
		gl.glMaterialfv( face, pname, params );
	}

	@Override
	public final void glMaterialx(int face, int pname, int param) 
	{	
		gl.glMaterialx( face, pname, param );
	}

	@Override
	public final void glMaterialxv(int face, int pname, IntBuffer params) 
	{	
		gl.glMaterialxv( face, pname, params );
	}

	@Override
	public final void glMatrixMode(int mode) 
	{
		gl.glMatrixMode( mode );
	}

	@Override
	public final void glMultMatrixf(FloatBuffer m) 
	{	
		gl.glMultMatrixf( m );
	}

	@Override
	public final void glMultMatrixx(IntBuffer m) 
	{	
		gl.glMultMatrixx( m );
	}

	@Override
	public final void glMultiTexCoord4f(int target, float s, float t, float r, float q) 
	{	
		gl.glMultiTexCoord4f( target, s, t, r, q );
	}

	@Override
	public final void glMultiTexCoord4x(int target, int s, int t, int r, int q) 
	{	
		gl.glMultiTexCoord4x( target, s, t, r, q );
	}

	@Override
	public final void glNormal3f(float nx, float ny, float nz) 
	{	
		gl.glNormal3f( nx, ny, nz );
	}

	@Override
	public final void glNormal3x(int nx, int ny, int nz) 
	{	
		gl.glNormal3x( nx, ny, nz );
	}

	@Override
	public final void glNormalPointer(int type, int stride, Buffer pointer) 
	{	
		gl.glNormalPointer( type, stride, pointer );
	}

	@Override
	public final void glOrthof(float left, float right, float bottom, float top,
			float zNear, float zFar) 
	{	
		gl.glOrthof( left, right, bottom, top, zNear, zFar );
	}

	@Override
	public final void glOrthox(int left, int right, int bottom, int top, int zNear,
			int zFar) 
	{	
		gl.glOrthox( left, right, bottom, top, zNear, zFar );
	}

	@Override
	public final void glPixelStorei(int pname, int param) 
	{	
		gl.glPixelStorei( pname, param );
	}

	@Override
	public final void glPointSize(float size) 
	{	
		gl.glPointSize( size );
	}

	@Override
	public final void glPointSizex(int size) 
	{	
		gl.glPointSizex( size );
	}

	@Override
	public final void glPolygonOffset(float factor, float units) 
	{	
		gl.glPolygonOffset( factor, units );
	}

	@Override
	public final void glPolygonOffsetx(int factor, int units) 
	{	
		gl.glPolygonOffsetx( factor, units );
	}

	@Override
	public final void glPopMatrix() 
	{
		gl.glPopMatrix();
	}

	@Override
	public final void glPushMatrix() 
	{	
		gl.glPushMatrix();
	}

	@Override
	public final void glReadPixels(int x, int y, int width, int height, int format,
			int type, Buffer pixels) 
	{	
		gl.glReadPixels( x, y, width, height, format, type, pixels );
	}

	@Override
	public final void glRotatef(float angle, float x, float y, float z) 
	{
		gl.glRotatef( angle, x, y, z );
	}

	@Override
	public final void glRotatex(int angle, int x, int y, int z) 
	{	
		gl.glRotatex( angle, x, y, z );
	}

	@Override
	public final void glSampleCoverage(float value, boolean invert) 
	{	
		gl.glSampleCoverage( value, invert );
	}

	@Override
	public final void glSampleCoveragex(int value, boolean invert) 
	{	
		gl.glSampleCoveragex( value, invert );
	}

	@Override
	public final void glScalef(float x, float y, float z) 
	{	
		gl.glScalef( x, y, z );
	}

	@Override
	public final void glScalex(int x, int y, int z) 
	{	
		gl.glScalex( x, y, z );
	}

	@Override
	public final void glScissor(int x, int y, int width, int height) 
	{	
		gl.glScissor( x, y, width, height );
	}

	@Override
	public final void glShadeModel(int mode) 
	{	
		gl.glShadeModel(mode);
	}

	@Override
	public final void glStencilFunc(int func, int ref, int mask) 
	{	
		gl.glStencilFunc( func, ref, mask );
	}

	@Override
	public final void glStencilMask(int mask) 
	{	
		gl.glStencilMask( mask );
	}

	@Override
	public final void glStencilOp(int fail, int zfail, int zpass) 
	{	
		gl.glStencilOp( fail, zfail, zpass );
	}

	@Override
	public final void glTexCoordPointer(int size, int type, int stride, Buffer pointer) 
	{	
		gl.glTexCoordPointer( size, type, stride, pointer );
	}

	@Override
	public final void glTexEnvf(int target, int pname, float param) 
	{	
		gl.glTexEnvf( target, pname, param );
	}

	@Override
	public final void glTexEnvfv(int target, int pname, FloatBuffer params) 
	{	
		gl.glTexEnvfv( target, pname, params );
	}

	@Override
	public final void glTexEnvx(int target, int pname, int param) 
	{	
		gl.glTexEnvx( target, pname, param );
	}

	@Override
	public final void glTexEnvxv(int target, int pname, IntBuffer params) 
	{	
		gl.glTexEnvxv( target, pname, params );
	}

	@Override
	public final void glTexImage2D(int target, int level, int internalformat,
			int width, int height, int border, int format, int type,
			Buffer pixels) 
	{	
		gl.glTexImage2D( target, level, internalformat, width, height, border, format, type, pixels );
	}

	@Override
	public final void glTexParameterf(int target, int pname, float param) 
	{	
		gl.glTexParameterf( target, pname, param );
	}

	@Override
	public final void glTexParameterx(int target, int pname, int param) 
	{	
		gl.glTexParameterx( target, pname, param );
	}

	@Override
	public final void glTexSubImage2D(int target, int level, int xoffset,
			int yoffset, int width, int height, int format, int type,
			Buffer pixels) 
	{
		gl.glTexSubImage2D( target, level, xoffset, yoffset, width, height, format, type, pixels );
	}

	@Override
	public final void glTranslatef(float x, float y, float z) 
	{	
		gl.glTranslatef( x, y, z );
	}

	@Override
	public final void glTranslatex(int x, int y, int z) 
	{	
		gl.glTranslatex( x, y, z );
	}

	@Override
	public final void glVertexPointer(int size, int type, int stride, Buffer pointer) 
	{	
		gl.glVertexPointer( size, type, stride, pointer );
	}

	@Override
	public final void glViewport(int x, int y, int width, int height) 
	{	
		gl.glViewport( x, y, width, height );
	}

	@Override
	public final void glDeleteTextures(int n, int[] textures, int offset) 
	{	
		gl.glDeleteTextures( n, textures, offset );
	}

	@Override
	public final void glFogfv(int pname, float[] params, int offset) 
	{	
		gl.glFogfv( pname, params, offset );
	}

	@Override
	public final void glFogxv(int pname, int[] params, int offset) 
	{	
		gl.glFogxv( pname, params, offset );
	}

	@Override
	public final void glGenTextures(int n, int[] textures, int offset) 
	{	
		gl.glGenTextures( n, textures, offset );
	}

	@Override
	public final void glGetIntegerv(int pname, int[] params, int offset) 
	{	
		gl.glGetIntegerv( pname, params, offset );
	}

	@Override
	public final void glLightModelfv(int pname, float[] params, int offset) 
	{	
		gl.glLightModelfv( pname, params, offset );
	}

	@Override
	public final void glLightModelxv(int pname, int[] params, int offset) 
	{
		gl.glLightModelxv( pname, params, offset );
	}

	@Override
	public final void glLightfv(int light, int pname, float[] params, int offset) 
	{	
		gl.glLightfv( light, pname, params, offset );
	}

	@Override
	public final void glLightxv(int light, int pname, int[] params, int offset) 
	{
		gl.glLightxv( light, pname, params, offset );
	}

	@Override
	public final void glLoadMatrixf(float[] m, int offset) 
	{	
		gl.glLoadMatrixf( m, offset );
	}

	@Override
	public final void glLoadMatrixx(int[] m, int offset) 
	{	
		gl.glLoadMatrixx( m, offset );
	}

	@Override
	public final void glMaterialfv(int face, int pname, float[] params, int offset) 
	{	
		gl.glMaterialfv( face, pname, params, offset );
	}

	@Override
	public final void glMaterialxv(int face, int pname, int[] params, int offset) 
	{	
		gl.glMaterialxv( face, pname, params, offset );
	}

	@Override
	public final void glMultMatrixf(float[] m, int offset) 
	{	
		gl.glMultMatrixf( m, offset );
	}

	@Override
	public final void glMultMatrixx(int[] m, int offset) 
	{
		gl.glMultMatrixx( m, offset );
	}

	@Override
	public final void glTexEnvfv(int target, int pname, float[] params, int offset) 
	{	
		gl.glTexEnvfv( target, pname, params, offset );
	}

	@Override
	public final void glTexEnvxv(int target, int pname, int[] params, int offset) 
	{	
		gl.glTexEnvxv( target, pname, params, offset );
	}		
}
