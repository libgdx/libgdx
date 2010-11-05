package com.badlogic.gdx.backends.jogl;

import java.awt.image.BufferedImage;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCanvas;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GL11;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GLCommon;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.math.WindowedMean;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.sun.opengl.util.Animator;

public abstract class JoglGraphicsBase implements Graphics, GLEventListener {
	GLCanvas canvas;
	Animator animator;	
	boolean useGL2;
	long frameStart = System.nanoTime();
	long lastFrameTime = System.nanoTime();
	float deltaTime = 0;
	WindowedMean mean = new WindowedMean(10);
	int fps;
	int frames;
	boolean paused = true;
	
	GLCommon gl;
	GL10 gl10;
	GL11 gl11;
	GL20 gl20;
	
	void initialize(String title, int width, int height, boolean useGL2) {
		GLCapabilities caps = new GLCapabilities();
		caps.setRedBits(8);
		caps.setGreenBits(8);
		caps.setBlueBits(8);
		caps.setAlphaBits(8);
		caps.setDepthBits(16);
		caps.setStencilBits(8);
		caps.setNumSamples(0);
		caps.setSampleBuffers(false);
		caps.setDoubleBuffered(true);
		
		canvas = new GLCanvas(caps);

		canvas.addGLEventListener(this);
		this.useGL2 = useGL2;
	}
	
	GLCanvas getCanvas() {
		return canvas;
	}

	void create() {		
		frameStart = System.nanoTime();
		lastFrameTime = frameStart;
		deltaTime = 0;
		mean.clear();	
		animator = new Animator(canvas);
//		animator.setRunAsFastAsPossible(true);
		animator.start();
	}

	void pause() {
		synchronized (this) {
			paused = true;
		}
		animator.stop();
	}
	
	void resume() {		
		paused = false;		
		frameStart = System.nanoTime();
		lastFrameTime = frameStart;
		deltaTime = 0;
		mean.clear();			
		animator = new Animator(canvas);
		animator.setRunAsFastAsPossible(true);
		animator.start();
	}	
	
	void initializeGLInstances (GLAutoDrawable drawable) {
		String version = drawable.getGL().glGetString(GL.GL_VERSION);
		int major = Integer.parseInt("" + version.charAt(0));
		int minor = Integer.parseInt("" + version.charAt(2));

		if (useGL2 && major >= 2) {
			gl20 = new JoglGL20(drawable.getGL());
			gl = gl20;
		} else {
			if (major == 1 && minor < 5) {
				gl10 = new JoglGL10(drawable.getGL());
			} else {
				gl11 = new JoglGL11(drawable.getGL());
				gl10 = gl11;
			}
			gl = gl10;
		}
		
		Gdx.gl = gl;
		Gdx.gl10 = gl10;
		Gdx.gl11 = gl11;
		Gdx.gl20 = gl20;
	}
	
	void updateTimes () {
		deltaTime = (System.nanoTime() - lastFrameTime) / 1000000000.0f;
		lastFrameTime = System.nanoTime();
		mean.addValue(deltaTime);
		
		if (System.nanoTime() - frameStart > 1000000000) {
			fps = frames;
			frames = 0;
			frameStart = System.nanoTime();
		}
		frames++;
	}
	
	@Override
	public float getDeltaTime() {
		return mean.getMean() == 0 ? deltaTime : mean.getMean();
	}

	@Override
	public int getFramesPerSecond() {
		return fps;
	}
	
	@Override
	public int getHeight() {
		return canvas.getHeight();
	}

	@Override
	public int getWidth() {
		return canvas.getWidth();
	}
	
	@Override
	public GL10 getGL10() {	
		return gl10;
	}

	@Override
	public GL11 getGL11() {		
		return gl11;
	}

	@Override
	public GL20 getGL20() {		
		return gl20;
	}

	@Override
	public GLCommon getGLCommon() {
		return gl;
	}
	
	@Override
	public boolean isGL11Available() {
		return gl11 != null;
	}

	@Override
	public boolean isGL20Available() {
		return gl20 != null;
	}
	
	@Override
	public GraphicsType getType() {
		return GraphicsType.JoglGL;
	}

	@Override
	public Pixmap newPixmap(int width, int height, Format format) {
		return new JoglPixmap(width, height, format);
	}

	@Override
	public Pixmap newPixmap(InputStream in) {
		try {
			BufferedImage img = (BufferedImage) ImageIO.read(in);
			return new JoglPixmap(img);
		} catch (Exception ex) {
			throw new GdxRuntimeException(
					"Couldn't load Pixmap from InputStream", ex);
		}
	}

	@Override
	public Pixmap newPixmap(FileHandle file) {
		return newPixmap(file.readFile());
	}

	@Override
	public Pixmap newPixmap(Object nativePixmap) {
		return new JoglPixmap((BufferedImage) nativePixmap);
	}

	private static boolean isPowerOfTwo(int value) {
		return ((value != 0) && (value & (value - 1)) == 0);
	}

	@Override
	public Texture newUnmanagedTexture(int width, int height,
			Pixmap.Format format, TextureFilter minFilter,
			TextureFilter magFilter, TextureWrap uWrap, TextureWrap vWrap) {
		if (!isPowerOfTwo(width) || !isPowerOfTwo(height))
			throw new GdxRuntimeException(
					"Texture dimensions must be a power of two");

		if (format == Format.Alpha)
			return new JoglTexture(width, height, BufferedImage.TYPE_BYTE_GRAY,
					minFilter, magFilter, uWrap, vWrap, false);
		else
			return new JoglTexture(width, height,
					BufferedImage.TYPE_4BYTE_ABGR, minFilter, magFilter, uWrap,
					vWrap, false);
	}

	@Override
	public Texture newUnmanagedTexture(Pixmap pixmap, TextureFilter minFilter,
			TextureFilter magFilter, TextureWrap uWrap, TextureWrap vWrap) {
		if (!isPowerOfTwo(pixmap.getHeight())
				|| !isPowerOfTwo(pixmap.getWidth()))
			throw new GdxRuntimeException(
					"Texture dimensions must be a power of two");

		return new JoglTexture((BufferedImage) pixmap.getNativePixmap(),
				minFilter, magFilter, uWrap, vWrap, false);
	}

	@Override
	public Texture newTexture(FileHandle file, TextureFilter minFilter,
			TextureFilter magFilter, TextureWrap uWrap, TextureWrap vWrap) {
		Pixmap pixmap = newPixmap(file);
		if (!isPowerOfTwo(pixmap.getHeight())
				|| !isPowerOfTwo(pixmap.getWidth()))
			throw new GdxRuntimeException(
					"Texture dimensions must be a power of two");

		return new JoglTexture((BufferedImage) pixmap.getNativePixmap(),
				minFilter, magFilter, uWrap, vWrap, false);
	}
}
