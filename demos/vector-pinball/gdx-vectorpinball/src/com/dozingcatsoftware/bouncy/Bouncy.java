
package com.dozingcatsoftware.bouncy;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GLCommon;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.WindowedMean;
import com.badlogic.gdx.utils.TimeUtils;
import com.dozingcatsoftware.bouncy.elements.FieldElement;

public class Bouncy extends InputAdapter implements ApplicationListener {
	OrthographicCamera cam;
	GLFieldRenderer renderer;
	Field field;
	int level = 1;
	WindowedMean physicsMean = new WindowedMean(10);
	WindowedMean renderMean = new WindowedMean(10);
	long startTime = TimeUtils.nanoTime();

	@Override
	public void create () {
		cam = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		renderer = new GLFieldRenderer();
		field = new Field();
		field.resetForLevel(level);
		Gdx.input.setInputProcessor(this);
	}

	@Override
	public void resume () {

	}

	@Override
	public void render () {
		GLCommon gl = Gdx.gl;

		long startPhysics = TimeUtils.nanoTime();
		field.tick((long)(Gdx.graphics.getDeltaTime() * 3000), 4);
		physicsMean.addValue((TimeUtils.nanoTime() - startPhysics) / 1000000000.0f);

		gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		cam.viewportWidth = field.getWidth();
		cam.viewportHeight = field.getHeight();
		cam.position.set(field.getWidth() / 2, field.getHeight() / 2, 0);
		cam.update();
		renderer.setProjectionMatrix(cam.combined);

		long startRender = TimeUtils.nanoTime();
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
		renderMean.addValue((TimeUtils.nanoTime() - startRender) / 1000000000.0f);

		if (TimeUtils.nanoTime() - startTime > 1000000000) {
			Gdx.app.log("Bouncy", "fps: " + Gdx.graphics.getFramesPerSecond() + ", physics: " + physicsMean.getMean() * 1000
				+ ", rendering: " + renderMean.getMean() * 1000);
			startTime = TimeUtils.nanoTime();
		}
	}

	@Override
	public void resize (int width, int height) {

	}

	@Override
	public void pause () {

	}

	@Override
	public void dispose () {

	}

	@Override
	public boolean touchDown (int x, int y, int pointer, int button) {
		field.removeDeadBalls();
		if (field.getBalls().size() != 0) field.setAllFlippersEngaged(true);
		return false;
	}

	@Override
	public boolean touchUp (int x, int y, int pointer, int button) {
		field.removeDeadBalls();
		if (field.getBalls().size() == 0) field.launchBall();
		field.setAllFlippersEngaged(false);
		return false;
	}

	@Override
	public boolean touchDragged (int x, int y, int pointer) {
		if (field.getBalls().size() != 0) field.setAllFlippersEngaged(true);
		return false;
	}
}
