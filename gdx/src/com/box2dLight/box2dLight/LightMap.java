package com.box2dLight.box2dLight;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.box2dLight.shaders.DiffuseShader;
import com.box2dLight.shaders.Gaussian;
import com.box2dLight.shaders.ShadowShader;
import com.box2dLight.shaders.WithoutShadowShader;

class LightMap {
	private ShaderProgram shadowShader;
	FrameBuffer frameBuffer;
	private Mesh lightMapMesh;

	private FrameBuffer pingPongBuffer;

	private RayHandler rayHandler;
	private ShaderProgram withoutShadowShader;
	private ShaderProgram blurShader;
	private ShaderProgram diffuseShader;

	boolean lightMapDrawingDisabled;

	public void render() {

		boolean needed = rayHandler.lightRenderedLastFrame > 0;
		// this way lot less binding
		if (needed && rayHandler.blur)
			gaussianBlur();

		if (lightMapDrawingDisabled)
			return;
		frameBuffer.getColorBufferTexture().bind(0);

		// at last lights are rendered over scene
		if (rayHandler.shadows) {

			final Color c = rayHandler.ambientLight;
			ShaderProgram shader = shadowShader;
			if (RayHandler.isDiffuse) {
				shader = diffuseShader;
				shader.begin();
				Gdx.gl20.glBlendFunc(GL20.GL_DST_COLOR, GL20.GL_SRC_COLOR);
				shader.setUniformf("ambient", c.r, c.g, c.b, c.a);
			} else {
				shader.begin();
				Gdx.gl20.glBlendFunc(GL20.GL_ONE, GL20.GL_ONE_MINUS_SRC_ALPHA);
				shader.setUniformf("ambient", c.r * c.a, c.g * c.a,
						c.b * c.a, 1f - c.a);
			}
		//	shader.setUniformi("u_texture", 0);
			lightMapMesh.render(shader, GL20.GL_TRIANGLE_FAN);
			shader.end();
		} else if (needed) {

			Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
			withoutShadowShader.begin();
		//	withoutShadowShader.setUniformi("u_texture", 0);
			lightMapMesh.render(withoutShadowShader, GL20.GL_TRIANGLE_FAN);
			withoutShadowShader.end();
		}

		Gdx.gl20.glDisable(GL20.GL_BLEND);
	}

	public void gaussianBlur() {

		Gdx.gl20.glDisable(GL20.GL_BLEND);
		for (int i = 0; i < rayHandler.blurNum; i++) {
			frameBuffer.getColorBufferTexture().bind(0);
			// horizontal
			pingPongBuffer.begin();
			{
				blurShader.begin();
		//		blurShader.setUniformi("u_texture", 0);
				blurShader.setUniformf("dir", 1f, 0f);
				lightMapMesh.render(blurShader, GL20.GL_TRIANGLE_FAN, 0, 4);
				blurShader.end();
			}
			pingPongBuffer.end();

			pingPongBuffer.getColorBufferTexture().bind(0);
			// vertical
			frameBuffer.begin();
			{
				blurShader.begin();
			//	blurShader.setUniformi("u_texture", 0);
				blurShader.setUniformf("dir", 0f, 1f);
				lightMapMesh.render(blurShader, GL20.GL_TRIANGLE_FAN, 0, 4);
				blurShader.end();

			}
			frameBuffer.end();
		}

		Gdx.gl20.glEnable(GL20.GL_BLEND);

	}

	public LightMap(RayHandler rayHandler, int fboWidth, int fboHeight) {
		this.rayHandler = rayHandler;

		if (fboWidth <= 0)
			fboWidth = 1;
		if (fboHeight <= 0)
			fboHeight = 1;
		frameBuffer = new FrameBuffer(Format.RGBA8888, fboWidth,
				fboHeight, false);
		pingPongBuffer = new FrameBuffer(Format.RGBA8888, fboWidth,
				fboHeight, false);

		lightMapMesh = createLightMapMesh();

		shadowShader = ShadowShader.createShadowShader();
		diffuseShader = DiffuseShader.createShadowShader();

		withoutShadowShader = WithoutShadowShader.createShadowShader();

		blurShader = Gaussian.createBlurShader(fboWidth, fboHeight);

	}

	void dispose() {
		shadowShader.dispose();
		blurShader.dispose();
		lightMapMesh.dispose();
		frameBuffer.dispose();
		pingPongBuffer.dispose();

	}

	private Mesh createLightMapMesh() {
		float[] verts = new float[VERT_SIZE];
		// vertex coord
		verts[X1] = -1;
		verts[Y1] = -1;

		verts[X2] = 1;
		verts[Y2] = -1;

		verts[X3] = 1;
		verts[Y3] = 1;

		verts[X4] = -1;
		verts[Y4] = 1;

		// tex coords
		verts[U1] = 0f;
		verts[V1] = 0f;

		verts[U2] = 1f;
		verts[V2] = 0f;

		verts[U3] = 1f;
		verts[V3] = 1f;

		verts[U4] = 0f;
		verts[V4] = 1f;

		Mesh tmpMesh = new Mesh(true, 4, 0, new VertexAttribute(
				Usage.Position, 2, "a_position"), new VertexAttribute(
				Usage.TextureCoordinates, 2, "a_texCoord"));

		tmpMesh.setVertices(verts);
		return tmpMesh;

	}

	static public final int VERT_SIZE = 16;
	static public final int X1 = 0;
	static public final int Y1 = 1;
	static public final int U1 = 2;
	static public final int V1 = 3;
	static public final int X2 = 4;
	static public final int Y2 = 5;
	static public final int U2 = 6;
	static public final int V2 = 7;
	static public final int X3 = 8;
	static public final int Y3 = 9;
	static public final int U3 = 10;
	static public final int V3 = 11;
	static public final int X4 = 12;
	static public final int Y4 = 13;
	static public final int U4 = 14;
	static public final int V4 = 15;

}
