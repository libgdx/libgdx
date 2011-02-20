
package com.badlogicgames.superjumper;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GLCommon;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.tmp.OrthographicCamera;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;

public class HighscoresScreen extends Screen {
	OrthographicCamera guiCam;
	SpriteBatch batcher;
	Rectangle backBounds;
	Vector3 touchPoint;
	String[] highScores;
	float xOffset = 0;

	public HighscoresScreen (Game game) {
		super(game);

		guiCam = new OrthographicCamera(320, 480);		
		guiCam.position.set(320 / 2, 480 / 2, 0);
		backBounds = new Rectangle(0, 0, 64, 64);
		touchPoint = new Vector3();
		batcher = new SpriteBatch();
		highScores = new String[5];
		for (int i = 0; i < 5; i++) {
			highScores[i] = (i + 1) + ". " + Settings.highscores[i];
			xOffset = Math.max(Assets.font.getBounds(highScores[i]).width, xOffset);
		}
		xOffset = 160 - xOffset / 2 + Assets.font.getSpaceWidth() / 2;
	}

	@Override public void update (float deltaTime) {
		if (Gdx.input.justTouched()) {
			guiCam.unproject(touchPoint.set(Gdx.input.getX(), Gdx.input.getY(), 0));
			
			if (OverlapTester.pointInRectangle(backBounds, touchPoint.x, touchPoint.y)) {
				Assets.playSound(Assets.clickSound);
				game.setScreen(new MainMenuScreen(game));
				return;
			}
		}
	}

	@Override public void present (float deltaTime) {
		GLCommon gl = Gdx.gl;
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		guiCam.update();
		guiCam.apply(Gdx.gl10);

		gl.glEnable(GL10.GL_TEXTURE_2D);

		batcher.disableBlending();
		batcher.begin();
		batcher.draw(Assets.backgroundRegion, 0, 0, 320, 480);
		batcher.end();
	
		batcher.enableBlending();
		batcher.begin();
		batcher.draw(Assets.highScoresRegion, 10, 360 - 16, 300, 33);

		float y = 230;
		for (int i = 4; i >= 0; i--) {
			Assets.font.draw(batcher, highScores[i], xOffset, y);
			y += Assets.font.getLineHeight();
		}

		batcher.draw(Assets.arrow, 0, 0, 64, 64);
		batcher.end();		
	}

	@Override public void resume () {
	}

	@Override public void pause () {
	}

	@Override public void dispose () {
	}
}
