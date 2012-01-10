package com.badlogic.gdx.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.Array;

/**
 * Shows the impact on frame rate if you switch textures to often. This is
 * how you should NOT do it :)
 * @author mzechner
 */
public class TextureBindTest extends GdxTest {
	Texture tex1;
	Texture tex2;
	Mesh mesh;
	ShaderProgram shader;
	Array<Vector2> positions = new Array<Vector2>();
	Camera cam;
	
	public void create() {
		Pixmap pixmap = new Pixmap(32, 32, Format.RGB565);
		pixmap.setColor(1, 0, 0, 1);
		pixmap.fill();
		tex1 = new Texture(pixmap);
		pixmap.setColor(0, 0, 1, 1);
		pixmap.fill();
		tex2 = new Texture(pixmap);
		pixmap.dispose();
		
		for(int i = 0; i < 5000; i++) {
			positions.add(new Vector2((float)Math.random() * Gdx.graphics.getWidth(), (float)Math.random() * Gdx.graphics.getHeight()));
		}
		
		if(Gdx.graphics.isGL20Available()) shader = SpriteBatch.createDefaultShader();
		cam = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cam.update();
		mesh = new Mesh(false, 4, 6, new VertexAttribute(Usage.Position, 2,
				ShaderProgram.POSITION_ATTRIBUTE), new VertexAttribute(Usage.ColorPacked, 4, ShaderProgram.COLOR_ATTRIBUTE),
				new VertexAttribute(Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE + "0"));
		mesh.setIndices(new short[] { 0, 1, 2, 2, 3, 0 });
		mesh.setVertices(new float[] { 0, 0, Color.WHITE.toIntBits(), 0, 1,
									   32, 0, Color.WHITE.toIntBits(), 1, 1,
									   32, 32, Color.WHITE.toIntBits(), 1, 0,
									   0, 32, Color.WHITE.toIntBits(), 0, 0 });
		mesh.setAutoBind(false);
	}

	public void render() {
		Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		
		if(Gdx.graphics.isGL20Available()) {
			shader.begin();
			shader.setUniformi("u_texture", 0);
			shader.setUniformMatrix("u_projectionViewMatrix", cam.combined);
			mesh.bind(shader);
		} else {
			cam.apply(Gdx.gl10);
			Gdx.gl.glEnable(GL10.GL_TEXTURE_2D);
			mesh.bind();
		}

		for(int i = 0; i < positions.size; i++) {
			float x = positions.get(i).x;
			float y = positions.get(i).y;
			if(i % 2 == 0) tex1.bind();
			else tex2.bind();
			
			if(Gdx.graphics.isGL20Available()) mesh.render(shader, GL10.GL_TRIANGLES);
			else mesh.render(GL10.GL_TRIANGLES);
		}
		if(Gdx.graphics.isGL20Available()) {
			mesh.unbind(shader);
			shader.end();
		} else {
			mesh.unbind();
		}
		Gdx.app.log("TextureBindTest", "fps: " + Gdx.graphics.getFramesPerSecond());
	}

	@Override
	public boolean needsGL20() {
		return false;
	}
}
