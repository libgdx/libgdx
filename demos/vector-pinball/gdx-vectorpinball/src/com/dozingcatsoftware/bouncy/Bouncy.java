
package com.dozingcatsoftware.bouncy;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GLCommon;
import com.badlogic.gdx.graphics.g2d.OrthographicCamera;
import com.badlogic.gdx.math.WindowedMean;
import com.dozingcatsoftware.bouncy.elements.FieldElement;

public class Bouncy extends InputAdapter implements ApplicationListener {
	OrthographicCamera cam;
	GLFieldRenderer renderer;
	Field field;
	int level = 1;
	WindowedMean physicsMean = new WindowedMean(10);
	WindowedMean renderMean = new WindowedMean(10);
	long startTime = System.nanoTime();

	@Override public void create () {
		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
		cam = new OrthographicCamera();
		renderer = new GLFieldRenderer();
		field = new Field();
		field.resetForLevel(level);
		Gdx.input.setInputProcessor(this);
	}

	@Override public void resume () {

	}

	@Override public void render () {
		GLCommon gl = Gdx.gl;

		long startPhysics = System.nanoTime();
		field.tick((long)(Gdx.graphics.getDeltaTime() * 3000), 4);
		physicsMean.addValue((System.nanoTime() - startPhysics) / 1000000000.0f);

		gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		cam.setViewport(field.getWidth(), field.getHeight());
		cam.getPosition().set(field.getWidth() / 2, field.getHeight() / 2, 0);
		cam.setMatrices();

		long startRender = System.nanoTime();
		renderer.begin();
		int len = field.getFieldElements().size();
		for (int i = 0; i < len; i++) {
			FieldElement element = field.getFieldElements().get(i);
			element.draw(renderer);
		}
		renderer.end();

		renderer.begin();
		field.drawBalls(renderer);
		renderer.end();
		renderMean.addValue((System.nanoTime() - startRender) / 1000000000.0f);

		if (System.nanoTime() - startTime > 1000000000) {
			Gdx.app.log("Bouncy", "fps: " + Gdx.graphics.getFramesPerSecond() + ", physics: " + physicsMean.getMean() * 1000
				+ ", rendering: " + renderMean.getMean() * 1000);
			startTime = System.nanoTime();
		}
	}

	@Override public void resize (int width, int height) {

	}

	@Override public void pause () {

	}

	@Override public void dispose () {

	}

	@Override public boolean touchDown (int x, int y, int pointer) {
		field.removeDeadBalls();
		if (field.getBalls().size() != 0) field.setAllFlippersEngaged(true);
		return false;
	}

	@Override public boolean touchUp (int x, int y, int pointer) {
		field.removeDeadBalls();
		if (field.getBalls().size() == 0) field.launchBall();
		field.setAllFlippersEngaged(false);
		return false;
	}

	@Override public boolean touchDragged (int x, int y, int pointer) {
		if (field.getBalls().size() != 0) field.setAllFlippersEngaged(true);
		return false;
	}
}
