
package com.badlogic.cubocy.screens;

import com.badlogic.cubocy.Map;
import com.badlogic.cubocy.MapRenderer;
import com.badlogic.cubocy.OnscreenControlRenderer;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.GL10;

public class GameScreen extends CubocScreen {
	Map map;
	MapRenderer renderer;
	OnscreenControlRenderer controlRenderer;

	public GameScreen (Game game) {
		super(game);
	}

	@Override
	public void show () {
		map = new Map();
		renderer = new MapRenderer(map);
		controlRenderer = new OnscreenControlRenderer(map);
	}

	@Override
	public void render (float delta) {
		delta = Math.min(0.06f, Gdx.graphics.getDeltaTime());
		map.update(delta);
		Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		renderer.render(delta);
		controlRenderer.render();

		if (map.bob.bounds.overlaps(map.endDoor.bounds)) {
			game.setScreen(new GameOverScreen(game));
		}

		if (Gdx.input.isKeyPressed(Keys.ESCAPE)) {
			game.setScreen(new MainMenu(game));
		}
	}

	@Override
	public void hide () {
		System.out.println("dispose game screen");
		renderer.dispose();
		controlRenderer.dispose();
	}
}
