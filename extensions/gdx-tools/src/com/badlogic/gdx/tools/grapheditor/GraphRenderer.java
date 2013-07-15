package com.badlogic.gdx.tools.grapheditor;

import java.awt.Font;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.shaders.graph.ShaderGraph;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class GraphRenderer {
	private final OrthographicCamera camera;
	private final ShapeRenderer shapeRenderer;
	private final SpriteBatch batch;
	private final BitmapFont font;
	
	public GraphRenderer() {
		camera = new OrthographicCamera();
		camera.setToOrtho(false);
		shapeRenderer = new ShapeRenderer();
		batch = new SpriteBatch();
		font = new BitmapFont();
	}
	
	public void resize(int width, int height) {
		camera.viewportWidth = width;
		camera.viewportHeight = height;
		camera.update();
	}
	
	public void render(ShaderGraph graph) {
		camera.update();
	}
}
