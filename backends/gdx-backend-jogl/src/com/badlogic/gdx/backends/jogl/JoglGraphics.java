package com.badlogic.gdx.backends.jogl;

import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLContext;
import javax.media.opengl.GLEventListener;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.utils.GdxRuntimeException;

/**
 * Implements the {@link Graphics} interface with Jogl.
 * 
 * @author mzechner
 * 
 */
public class JoglGraphics extends JoglGraphicsBase implements GLEventListener {		
	ApplicationListener listener = null;	
	boolean useGL2;	
	boolean created = false;

	public JoglGraphics(ApplicationListener listener, String title, int width, int height, boolean useGL2) {
		initialize(title, width, height, useGL2);
		if(listener==null) throw new GdxRuntimeException("RenderListener must not be null");
		this.listener = listener;
	}	
	
	public void create () {
		super.create();
	}
	
	public void pause () {
		super.pause();
		canvas.getContext().makeCurrent();		
		listener.pause();
	}
	
	public void resume () {		
		canvas.getContext().makeCurrent();
		listener.resume();
		super.resume();
	}

	@Override
	public void init(GLAutoDrawable drawable) {
		initializeGLInstances(drawable);		
		
		if( !created ) {				
			listener.create();
			synchronized (this) {
				paused = false;
			}	
			created = true;
		}
	}

	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width,
			int height) {	
	}
	
	@Override
	public void display(GLAutoDrawable arg0) {				
		synchronized (this) {
			if (!paused) {
				updateTimes();
				listener.render();
			}
		}		
		
		Gdx.input.processEvents(null);
	}
	
	@Override
	public void displayChanged(GLAutoDrawable arg0, boolean arg1, boolean arg2) {

	}

	public void destroy() {		
		canvas.getContext().makeCurrent();
		listener.destroy();
	}
}
