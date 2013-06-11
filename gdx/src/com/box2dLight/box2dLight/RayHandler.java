package com.box2dLight.box2dLight;

/**
 * @author kalle_h
 *
 */

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.box2dLight.shaders.LightShader;

public class RayHandler implements Disposable {

	boolean isGL20 = false;
	boolean culling = true;
	boolean shadows = true;
	boolean blur = true;

	int blurNum = 1;
	Color ambientLight = new Color();

	World world;
	ShaderProgram lightShader;

	/** gles1.0 shadows mesh */
	private Mesh box;

	/**
	 * combined: matrix that include projection and translation matrices
	 */
	final private Matrix4 combined = new Matrix4();

	/** camera matrix corners */
	float x1, x2, y1, y2;

	private LightMap lightMap;

	/**
	 * This Array contain all the lights.
	 * 
	 * NOTE: DO NOT MODIFY THIS LIST
	 */
	final public Array<Light> lightList = new Array<Light>(false, 16);
	/**
	 * This Array contain all the disabled lights.
	 * 
	 * NOTE: DO NOT MODIFY THIS LIST
	 */
	final public Array<Light> disabledLights = new Array<Light>(false, 16);

	/** how many lights passed culling and rendered to scene */
	public int lightRenderedLastFrame = 0;

	/**
	 * Construct handler that manages everything related to updating and
	 * rendering the lights MINIMUM parameters needed are world where collision
	 * geometry is taken.
	 * 
	 * Default setting: culling = true, shadows = true, blur =
	 * true(GL2.0),blurNum = 1, ambientLight = 0.0f;
	 * 
	 * NOTE1: rays number per lights are capped to 1023. For different size use
	 * other constructor
	 * 
	 * NOTE2: On GL 2.0 FBO size is 1/4 * screen size and used by default. For
	 * different sizes use other constructor
	 * 
	 * @param world
	 */
	public RayHandler(World world) {
		this(world, Gdx.graphics.getWidth() / 4, Gdx.graphics
				.getHeight() / 4);
	}

	/**
	 * Construct handler that manages everything related to updating and
	 * rendering the lights MINIMUM parameters needed are world where collision
	 * geometry is taken.
	 * 
	 * Default setting: culling = true, shadows = true, blur =
	 * true(GL2.0),blurNum = 1, ambientLight = 0.0f;
	 * 
	 * 
	 * @param world
	 * @param fboWidth
	 * @param fboHeigth
	 */
	public RayHandler(World world, int fboWidth, int fboHeigth) {
		this.world = world;

		isGL20 = Gdx.graphics.isGL20Available();
		if (isGL20) {

			lightMap = new LightMap(this, fboWidth, fboHeigth);
			lightShader = LightShader.createLightShader();

		} else {
			setGammaCorrection(false);
			if (Gdx.graphics.getBufferFormat().a == 0) {
				setShadows(false);
			} else {
				box = new Mesh(true, 12, 0, new VertexAttribute(Usage.Position,
						2, "vertex_positions"), new VertexAttribute(
						Usage.ColorPacked, 4, "quad_colors"));
				setShadowBox();
			}

		}
	}

	/**
	 * Set combined camera matrix. Matrix will be copied and used for rendering
	 * lights, culling. Matrix must be set to work in box2d coordinates. Matrix
	 * has to be updated every frame(if camera is changed)
	 * 
	 * 
	 * NOTE: Matrix4 is assumed to be orthogonal for culling and directional
	 * lights.
	 * 
	 * If any problems detected Use: [public void setCombinedMatrix(Matrix4
	 * combined, float x, float y, float viewPortWidth, float viewPortHeight)]
	 * Instead
	 * 
	 * 
	 * @param combined
	 *            matrix that include projection and translation matrices
	 */
	public void setCombinedMatrix(Matrix4 combined) {
		System.arraycopy(combined.val, 0, this.combined.val, 0, 16);

		// updateCameraCorners
		float invWidth = combined.val[Matrix4.M00];

		final float halfViewPortWidth = 1f / invWidth;
		final float x = -halfViewPortWidth * combined.val[Matrix4.M03];
		x1 = x - halfViewPortWidth;
		x2 = x + halfViewPortWidth;

		float invHeight = combined.val[Matrix4.M11];

		final float halfViewPortHeight = 1f / invHeight;
		final float y = -halfViewPortHeight * combined.val[Matrix4.M13];
		y1 = y - halfViewPortHeight;
		y2 = y + halfViewPortHeight;

	}

	/**
	 * EXPERT USE Set combined camera matrix. Matrix will be copied and used for
	 * rendering lights, culling. Matrix must be set to work in box2d
	 * coordinates. Matrix has to be updated every frame(if camera is changed)
	 * 
	 * NOTE: this work with rotated cameras.
	 * 
	 * @param combined
	 *            matrix that include projection and translation matrices
	 * 
	 * @param x
	 *            combined matrix position
	 * @param y
	 *            combined matrix position
	 * @param viewPortWidth
	 *            NOTE!! use actual size, remember to multiple with zoom value
	 *            if pulled from OrthoCamera
	 * @param viewPortHeight
	 *            NOTE!! use actual size, remember to multiple with zoom value
	 *            if pulled from OrthoCamera
	 */
	public void setCombinedMatrix(Matrix4 combined, float x, float y,
			float viewPortWidth, float viewPortHeight) {
		System.arraycopy(combined.val, 0, this.combined.val, 0, 16);
		// updateCameraCorners
		final float halfViewPortWidth = viewPortWidth * 0.5f;
		x1 = x - halfViewPortWidth;
		x2 = x + halfViewPortWidth;

		final float halfViewPortHeight = viewPortHeight * 0.5f;
		y1 = y - halfViewPortHeight;
		y2 = y + halfViewPortHeight;

	}

	boolean intersect(float x, float y, float side) {
		return (x1 < (x + side) && x2 > (x - side) && y1 < (y + side) && y2 > (y - side));
	}

	/**
	 * Remember setCombinedMatrix(Matrix4 combined) before drawing.
	 * 
	 * Don't call this inside of any begin/end statements. Call this method
	 * after you have rendered background but before UI. Box2d bodies can be
	 * rendered before or after depending how you want x-ray light interact with
	 * bodies
	 */
	public final void updateAndRender() {
		update();
		render();
	}

	/**
	 * Manual update method for all lights. Use this if you have less physic
	 * steps than rendering steps.
	 */
	public final void update() {
		final int size = lightList.size;
		for (int j = 0; j < size; j++) {
			lightList.get(j).update();
		}

	}

	/**
	 * Manual rendering method for all lights.
	 * 
	 * NOTE! Remember to call updateRays if you use this method. * Remember
	 * setCombinedMatrix(Matrix4 combined) before drawing.
	 * 
	 * 
	 * Don't call this inside of any begin/end statements. Call this method
	 * after you have rendered background but before UI. Box2d bodies can be
	 * rendered before or after depending how you want x-ray light interact with
	 * bodies
	 */
	public void render() {

		lightRenderedLastFrame = 0;

		Gdx.gl.glDepthMask(false);
		Gdx.gl.glEnable(GL10.GL_BLEND);
		Gdx.gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE);

		if (isGL20) {
			renderWithShaders();
		} else {
			Gdx.gl10.glMatrixMode(GL10.GL_PROJECTION);
			Gdx.gl10.glLoadMatrixf(combined.val, 0);

			if (shadows) {
				alphaChannelClear();
			}

			for (int i = 0, size = lightList.size; i < size; i++) {
				lightList.get(i).render();
			}

			if (shadows) {
				if (box != null) {
					Gdx.gl.glBlendFunc(GL10.GL_ONE, GL10.GL_DST_ALPHA);
					box.render(GL10.GL_TRIANGLE_FAN, 0, 4);
				}
			}

			Gdx.gl.glDisable(GL10.GL_BLEND);
		}

	}

	void renderWithShaders() {

		if (shadows || blur) {
			lightMap.frameBuffer.begin();
			Gdx.gl20.glClearColor(0f, 0f, 0f, 0f);
			Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
		}

		lightShader.begin();
		{
			lightShader.setUniformMatrix("u_projTrans", combined);
			for (int i = 0, size = lightList.size; i < size; i++) {
				lightList.get(i).render();
			}
		}
		lightShader.end();

		if (shadows || blur) {
			lightMap.frameBuffer.end();
			lightMap.render();
		}

	}

	/**
	 * Checks whether the given point is inside of any light volume.
	 * 
	 * @param x
	 * @param y
	 * @return true if point intersect any light volume
	 */
	public boolean pointAtLight(float x, float y) {
		for (int i = 0, size = lightList.size; i < size; i++) {
			if (lightList.get(i).contains(x, y))
				return true;
		}
		return false;
	}

	/**
	 * Checks whether the given point outside of all light volumes.
	 * 
	 * @param x
	 * @param y
	 * @return true if point intersect any light volume
	 */
	public boolean pointAtShadow(float x, float y) {
		for (int i = 0, size = lightList.size; i < size; i++) {
			if (lightList.get(i).contains(x, y))
				return false;
		}
		return true;
	}

	private void alphaChannelClear() {
		Gdx.gl10.glClearColor(0f, 0f, 0f, ambientLight.a);
		Gdx.gl10.glColorMask(false, false, false, true);
		Gdx.gl10.glClear(GL10.GL_COLOR_BUFFER_BIT);
		Gdx.gl10.glColorMask(true, true, true, true);
		Gdx.gl10.glClearColor(0f, 0f, 0f, 0f);

	}

	public void dispose() {
	  while (lightList.size>0)
	    lightList.pop().remove();

	  while (disabledLights.size>0)
	    disabledLights.pop().remove();


		if (lightMap != null)
			lightMap.dispose();
		if (lightShader != null)
			lightShader.dispose();
	}

	public void removeAll() {

		while (lightList.size > 0)
			lightList.pop().remove();

		while (disabledLights.size > 0)
			disabledLights.pop().remove();

	}

	private void setShadowBox() {
		int i = 0;
		// This need some work, maybe camera matrix would needed
		float c = Color.toFloatBits(0, 0, 0, 1);
		float m_segments[] = new float[12];
		m_segments[i++] = -1000000f;
		m_segments[i++] = -1000000f;
		m_segments[i++] = c;
		m_segments[i++] = -1000000f;
		m_segments[i++] = 1000000f;
		m_segments[i++] = c;
		m_segments[i++] = 1000000f;
		m_segments[i++] = 1000000f;
		m_segments[i++] = c;
		m_segments[i++] = 1000000f;
		m_segments[i++] = -1000000;
		m_segments[i++] = c;
		box.setVertices(m_segments, 0, i);
	}

	/**
	 * Disables/enables culling. This save cpu and gpu time when world is bigger
	 * than screen.
	 * 
	 * Default = true
	 * 
	 * @param culling
	 *            the culling to set
	 */
	public final void setCulling(boolean culling) {
		this.culling = culling;
	}

	/**
	 * Disables/enables gaussian blur. This make lights much more softer and
	 * realistic look but also cost some precious shader time. With default fbo
	 * size on android cost around 1ms
	 * 
	 * default = true;
	 * 
	 * @param blur
	 *            the blur to set
	 */
	public final void setBlur(boolean blur) {
		this.blur = blur;
	}

	/**
	 * Set number of gaussian blur passes. Blurring can be pretty heavy weight
	 * operation, 1-3 should be safe. Setting this to 0 is same as
	 * setBlur(false)
	 * 
	 * default = 1
	 * 
	 * @param blurNum
	 *            the blurNum to set
	 */
	public final void setBlurNum(int blurNum) {
		this.blurNum = blurNum;
	}

	/**
	 * Disables/enables shadows. NOTE: If gl1.1 android you need to change
	 * render target to contain alpha channel* default = true
	 * 
	 * @param shadows
	 *            the shadows to set
	 */
	public final void setShadows(boolean shadows) {
		this.shadows = shadows;
	}

	/**
	 * Ambient light is how dark are the shadows. clamped to 0-1
	 * 
	 * default = 0;
	 * 
	 * @param ambientLight
	 *            the ambientLight to set
	 */
	public final void setAmbientLight(float ambientLight) {
		if (ambientLight < 0)
			ambientLight = 0;
		if (ambientLight > 1)
			ambientLight = 1;
		this.ambientLight.a = ambientLight;
	}

	/**
	 * Ambient light color is how dark and what colored the shadows are. clamped
	 * to 0-1 NOTE: color is changed only in gles2.0 default = 0;
	 * 
	 */
	public final void setAmbientLight(float r, float g, float b, float a) {
		this.ambientLight.r = r;
    this.ambientLight.g = g;
    this.ambientLight.b = b;
    this.ambientLight.a = a;   
	}

	/**
	 * Ambient light color is how dark and what colored the shadows are. clamped
	 * to 0-1 NOTE: color is changed only in gles2.0 default = 0,0,0,0;
	 * 
	 * @param ambientLightColor
	 *            the ambientLight to set
	 */
	public final void setAmbientLight(Color ambientLightColor) {
		this.ambientLight.set(ambientLightColor);
	}

	/**
	 * @param world
	 *            the world to set
	 */
	public final void setWorld(World world) {
		this.world = world;
	}

	static boolean gammaCorrection = false;
	static float gammaCorrectionParameter = 1f;
	static public boolean isDiffuse = false;
	final static float GAMMA_COR = 0.625f;

	/**
	 * return is gamma correction enabled
	 */
	public static boolean getGammaCorrection() {
		return gammaCorrection;
	}

	/**
	 * set gammaCorrection. This need to be done before creating instance of
	 * rayHandler. NOTE: this do nothing on gles1.0. NOTE2: for match the
	 * visuals with gamma uncorrected lights light distance parameters is
	 * modified internal.
	 * 
	 * @param gammeCorrectionWanted
	 */
	public static void setGammaCorrection(boolean gammeCorrectionWanted) {
		gammaCorrection = gammeCorrectionWanted;
		if (gammaCorrection)
			gammaCorrectionParameter = GAMMA_COR;
		else
			gammaCorrectionParameter = 1f;
	}

	/**
	 * If this is set to true and shadow are on lights are blended with diffuse
	 * algoritm. this preserve colors but might look bit darker. This is more
	 * realistic model than normally used This might improve perfromance
	 * slightly
	 * 
	 * @param useDiffuse
	 */
	public static void useDiffuseLight(boolean useDiffuse) {
		isDiffuse = useDiffuse;
	}

	/**
	 * enable/disable lightMap automatic rendering. Default is true If set to
	 * false user need use getLightMapTexture() and render that or use it as a
	 * light map when rendering. Example shader for spriteBatch is given. This
	 * is faster way to do if there is not that much overdrawing or if just
	 * couple object need light/shadows.
	 * 
	 * @param isAutomatic
	 */
	public void setLightMapRendering(boolean isAutomatic) {
		lightMap.lightMapDrawingDisabled = !isAutomatic;
	}

	/**
	 * Expert functionality
	 * 
	 * @return Texture that contain lightmap texture that can be used as light
	 *         texture in your shaders
	 */
	public Texture getLightMapTexture() {
		return lightMap.frameBuffer.getColorBufferTexture();
	}

	/**
	 * Expert functionality, no support given
	 * 
	 * @return FrameBuffer that contains lightMap
	 */
	public FrameBuffer getLightMapBuffer() {
		return lightMap.frameBuffer;
	}

}
