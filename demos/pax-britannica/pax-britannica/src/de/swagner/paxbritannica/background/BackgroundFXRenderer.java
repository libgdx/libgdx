package de.swagner.paxbritannica.background;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

import de.swagner.paxbritannica.Resources;

public class BackgroundFXRenderer {

	SpriteBatch backgroundFXBatch;
	Array<Debris> debrises = new Array<Debris>();
	
	Array<Fish> fishes = new Array<Fish>();
	
	SpriteBatch backgroundBatch;
	Sprite background;	

	public BackgroundFXRenderer() {
		createDebris();
		createFishes();
		backgroundFXBatch = new SpriteBatch();
		backgroundFXBatch.getProjectionMatrix().setToOrtho2D(0, 0, 800, 480);
		
		background = Resources.getInstance().background;
		backgroundBatch = new SpriteBatch();
		backgroundBatch.getProjectionMatrix().setToOrtho2D(0, 0, 128, 128);
	}

	private void createDebris() {
		for(int i = 0; i<30;++i) {
			debrises.add(new Debris(new Vector2(MathUtils.random(-100, 800),MathUtils.random(-200, 400))));
		}
	}
	
	private void createFishes() {
		for(int i = 0; i<15;++i) {
			fishes.add(new Fish(new Vector2(MathUtils.random(-100, 800),MathUtils.random(-200, 400))));
		}
	}

	float stateTime = 0;
	Vector3 lerpTarget = new Vector3();

	public void render() {		
		backgroundBatch.begin();
		background.draw(backgroundBatch);
		backgroundBatch.end();
		
		backgroundFXBatch.begin();
		for (Debris debris : debrises) {
			if (debris.alive) {
				debris.draw(backgroundFXBatch);
			} else {
				debris.reset();
			}
		}
		for (Fish fish : fishes) {
			if (fish.alive) {
				fish.draw(backgroundFXBatch);
			} else {
				fish.reset();
			}
		}
		backgroundFXBatch.end();		
	}
	
	public void resize(int width, int height) {
		backgroundFXBatch.getProjectionMatrix().setToOrtho2D(0, 0, width, height);
	}
	
	public void dispose() {
		fishes.clear();
		debrises.clear();
		backgroundFXBatch.dispose();
		backgroundBatch.dispose();
	}
	
}
