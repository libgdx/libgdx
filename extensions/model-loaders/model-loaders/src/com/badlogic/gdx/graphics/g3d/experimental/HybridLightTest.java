package com.badlogic.gdx.graphics.g3d.experimental;

import java.io.IOException;
import java.io.InputStream;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.jogl.JoglApplication;
import com.badlogic.gdx.backends.jogl.JoglApplicationConfiguration;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.loaders.obj.ObjLoader;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;

public class HybridLightTest implements ApplicationListener {

	static final int LIGHTS_NUM = 8;
	static final float LIGHT_INTESITY = 5;
	
	PerspectiveCamController camController;
	PerspectiveCamera cam;

	Mesh mesh;
	Mesh mesh2;
	private Texture texture;
	private Texture texture2;

	FPSLogger logger = new FPSLogger();
	ShaderProgram lightShader;

	float[] lightsPos = new float[LIGHTS_NUM * 3];
	float[] lightsCol = new float[LIGHTS_NUM * 3];
	float[] lightsInt = new float[LIGHTS_NUM];

	public void render() {

		logger.log();

		final float delta = Gdx.graphics.getDeltaTime();
		camController.update(delta);

		Gdx.gl.glEnable(GL10.GL_CULL_FACE);
		Gdx.gl.glCullFace(GL10.GL_BACK);
		Gdx.gl.glEnable(GL10.GL_DEPTH_TEST);
		Gdx.gl.glDepthMask(true);

		Gdx.gl.glClearColor(0, 0, 0, 0);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

		texture.bind(0);

		lightShader.begin();
		lightShader.setUniformf("camPos", cam.position.x, cam.position.y,
				cam.position.z);

		
		lightShader.setUniform3fv("lightsPos", lightsPos, 0, LIGHTS_NUM * 3);
		lightShader.setUniform3fv("lightsCol", lightsCol, 0, LIGHTS_NUM * 3);
		lightShader.setUniform1fv("lightsInt", lightsInt, 0, LIGHTS_NUM);

		lightShader.setUniformMatrix("u_projectionViewMatrix", cam.combined);
		lightShader.setUniformi("u_texture", 0);

		mesh.render(lightShader, GL10.GL_TRIANGLES);

		texture2.bind(0);
		mesh2.render(lightShader, GL10.GL_TRIANGLES);

		lightShader.end();

	}

	public void create() {
		Gdx.graphics.setVSync(false);

		// rng light pos and colors
		for (int i = 0; i < LIGHTS_NUM; i++) {
			lightsPos[3 * i + 0] = 2-MathUtils.random() * 5;
			lightsPos[3 * i + 1] = MathUtils.random() * 5;
			;
			lightsPos[3 * i + 2] = 1 - MathUtils.random() * 8;

			lightsCol[3 * i + 0] = MathUtils.random();
			lightsCol[3 * i + 1] = MathUtils.random();
			lightsCol[3 * i + 2] = MathUtils.random();

			lightsInt[i] = LIGHT_INTESITY;
		}

		lightShader = ShaderLoader.createShader("light", "light");

		cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(),
				Gdx.graphics.getHeight());
		cam.near = 0.1f;
		cam.far = 64f;
		cam.position.set(0, 0.5f, 1.5f);
		cam.update();

		camController = new PerspectiveCamController(cam);
		Gdx.input.setInputProcessor(camController);

		 texture = new Texture(Gdx.files.internal("data/multipleuvs_1.png"),
		 null,
		 true);
		 texture.setFilter(TextureFilter.MipMapLinearLinear,
		 TextureFilter.Linear);
		
		 texture2 = new Texture(Gdx.files.internal("data/wall.png"), null,
		 true);
		 texture2.setFilter(TextureFilter.MipMapLinearLinear,
		 TextureFilter.Linear);
		 texture2.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);

		try {
			InputStream in = Gdx.files.internal("data/smoothsphere.obj").read();
			mesh = ObjLoader.loadObj(in);
			in.close();
			in = Gdx.files.internal("data/basicscene.obj").read();
			mesh2 = ObjLoader.loadObj(in);
			in.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mesh2.scale(1.25f, 1.25f, 1.25f);

		mesh.getVertexAttribute(Usage.Position).alias = "a_position";
		mesh.getVertexAttribute(Usage.Normal).alias = "a_normal";
		// mesh.getVertexAttribute(Usage.TextureCoordinates).alias =
		// "a_texCoord0";

		mesh2.getVertexAttribute(Usage.Position).alias = "a_position";
		mesh2.getVertexAttribute(Usage.Normal).alias = "a_normal";
		// mesh2.getVertexAttribute(Usage.TextureCoordinates).alias =
		// "a_texCoord0";

	}

	public void resize(int width, int height) {
	}

	public void pause() {
	}

	public void dispose() {
		mesh.dispose();
		mesh2.dispose();
		 texture.dispose();
		 texture2.dispose();

	}

	public void resume() {
	}

	public static void main(String[] argv) {
		JoglApplicationConfiguration config = new JoglApplicationConfiguration();
		config.title = "Hybrid Light";
		config.width = 800;
		config.height = 480;
		config.samples = 8;
		config.vSyncEnabled = true;
		config.useGL20 = true;
		new JoglApplication(new HybridLightTest(), config);
	}
}
