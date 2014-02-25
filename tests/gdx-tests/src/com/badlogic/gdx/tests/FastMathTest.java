package com.badlogic.gdx.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.linearmath.int4;
import com.badlogic.gdx.tests.utils.GdxTest;

public class FastMathTest extends GdxTest {
	private final int PROCESSING_SIZE = 10000000;
	@Override
	public boolean needsGL20 () {
		return false;
	}

	@Override
	public void create () {
		vector3Test();
		vector2Test();
	}

	private void vector2Test () {
		Gdx.app.log("log", "Vector 2 Test");
		Vector2 vector = new Vector2();
		
		Long normalizeTime = System.nanoTime();
		for(int i = 0; i < PROCESSING_SIZE; ++i){
			vector.set(MathUtils.random(100f), MathUtils.random(100f)).nor();
		}
		normalizeTime = System.nanoTime() - normalizeTime;
		
		Long fastNormalizeTime = System.nanoTime();
		for(int i = 0; i < PROCESSING_SIZE; ++i){
			vector.set(MathUtils.random(100f), MathUtils.random(100f)).fastNor();
		}
		fastNormalizeTime = System.nanoTime() - fastNormalizeTime;

		Gdx.app.log("log", "Normalize time "+normalizeTime);
		Gdx.app.log("log", "Fast Normalize time "+fastNormalizeTime);
		Gdx.app.log("log", "Time difference "+(normalizeTime-fastNormalizeTime));
	}

	private void vector3Test () {
		Gdx.app.log("log", "Vector 3 Test");
		Vector3 vector = new Vector3();
		
		Long normalizeTime = System.nanoTime();
		for(int i = 0; i < PROCESSING_SIZE; ++i){
			vector.set(MathUtils.random(100f), MathUtils.random(100f), MathUtils.random(100f)).nor();
		}
		normalizeTime = System.nanoTime() - normalizeTime;
		
		Long fastNormalizeTime = System.nanoTime();
		for(int i = 0; i < PROCESSING_SIZE; ++i){
			vector.set(MathUtils.random(100f), MathUtils.random(100f), MathUtils.random(100f)).fastNor();
		}
		fastNormalizeTime = System.nanoTime() - fastNormalizeTime;

		Gdx.app.log("log", "Normalize time "+normalizeTime);
		Gdx.app.log("log", "Fast Normalize time "+fastNormalizeTime);
		Gdx.app.log("log", "Time difference "+(normalizeTime-fastNormalizeTime));
	}

}
