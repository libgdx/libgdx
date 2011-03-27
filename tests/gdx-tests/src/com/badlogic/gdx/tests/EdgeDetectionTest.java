package com.badlogic.gdx.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.loaders.obj.ObjLoader;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.tests.utils.GdxTest;

public class EdgeDetectionTest extends GdxTest {
	@Override public boolean needsGL20 () {
		return true;
	}
	
	FPSLogger logger;
	ShaderProgram shader;
	Mesh mesh;
	FrameBuffer fbo;
	PerspectiveCamera cam;	
	Matrix4 matrix = new Matrix4();
	float angle = 0;
	TextureRegion fboRegion;
	SpriteBatch batch;
	ShaderProgram batchShader;
		
	public void create() {
		ShaderProgram.pedantic = false;
		shader = new ShaderProgram(Gdx.files.internal("data/default.vert").readString(),
											Gdx.files.internal("data/default.frag").readString());
		if(!shader.isCompiled()) {
			Gdx.app.log("EdgeDetectionTest", shader.getLog());
		}
		batchShader = new ShaderProgram(Gdx.files.internal("data/batch.vert").readString(),
												  Gdx.files.internal("data/batch.frag").readString());
		if(!batchShader.isCompiled()) {
			Gdx.app.log("EdgeDetectionTest", batchShader.getLog());
		}
		
		mesh = ObjLoader.loadObj(Gdx.files.internal("data/cube.obj").read());
		fbo = new FrameBuffer(Format.RGB565, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
		cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cam.position.set(0, 0, 5);
		batch = new SpriteBatch();
		batch.setShader(batchShader);
		fboRegion = new TextureRegion(fbo.getColorBufferTexture());
		fboRegion.flip(false, true);
		logger = new FPSLogger();
	}
	
	public void render() {
		angle += 45 * Gdx.graphics.getDeltaTime();
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
		
		cam.update();
		matrix.setToRotation(0, 1, 0, angle);
		cam.combined.mul(matrix);
		
		fbo.begin();
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
		shader.begin();
		shader.setUniformMatrix("u_projView", cam.combined);
		mesh.render(shader, GL10.GL_TRIANGLES);
		shader.end();
		fbo.end();
		
		batch.begin();
		batch.draw(fboRegion, 0, 0);
		batch.end();
		logger.log();
	}
}
