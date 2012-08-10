package com.badlogic.gdx.tests;

import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.tests.utils.GdxTest;

public class MatrixPerformanceTest extends GdxTest {
	public static final int MSEC_FOR_TEST = 2000;
	
	boolean done = false;
	BitmapFont font;
	SpriteBatch batch;
	String result;

	@Override
	public void create() {
		font = new BitmapFont();
		batch = new SpriteBatch();
	}
	
	@Override
	public void render() {
		Gdx.gl10.glClear(GL10.GL_COLOR_BUFFER_BIT);
		if(!done) {
			doBenchmark();
			done = true;
		} else {
			batch.begin();
			font.drawMultiLine(batch, result, 0, 300);
			batch.end();
		}
	}
	
	public void doBenchmark() {
		Matrix4 m1 = new Matrix4();
		Matrix4 m2 = new Matrix4();
		final int MULTIPLICATIONS = 15000000;

		// warm up
		long startTime = System.nanoTime();
		long ops = 0;
		while (System.nanoTime() - startTime < 2000000000l) {
			m1.mul(m2);
			Matrix4.mul(m1.val, m2.val);
			ops++;
		}

		startTime = System.nanoTime();
		ops = 0;
		while (System.nanoTime() - startTime < 2000000000l) {
			m1.mul(m2);
			ops++;
		}
		
		// test Java version
		startTime = System.nanoTime();
		ops = 0;
		while (System.nanoTime() - startTime < 5000000000l) {
			m1.mul(m2);
			ops++;
		}					
		result = "Matrix4 (Java): " + ops / 5+ " ops/s\n";
		
		// test native version
		startTime = System.nanoTime();
		ops = 0;
		while (System.nanoTime() - startTime < 5000000000l) {
			m1.mul(m2);
			ops++;
		}					
		result += "Matrix4 (native): " + ops / 5 + " ops/s\n";
		Gdx.app.log("MatrixPerformanceTest", result);
	}
	

	public void doBenchmark2() {

		// initialize the pile of matricies
		final Matrix4[] matrix4 = new Matrix4[1024];
		final Random random = new Random(matrix4.length);
		for (int i = 0; i < matrix4.length; i++) {
			matrix4[i] = new Matrix4();
			for (int j = 0; j < matrix4[i].val.length; j++) {
				matrix4[i].val[j] = (float) (random.nextInt(256) + 1) / 64f;
			}
		}

		final Matrix4 nativeResult = new Matrix4();
		final Matrix4 pojoResult = new Matrix4();
		Matrix4 slowResult = new Matrix4();

		for (int i = 0; i < matrix4.length; i++) {
			Matrix4.mul(nativeResult.val, matrix4[i].val);
			pojoResult.mul(matrix4[i]);
			slowResult = slowResult.cpy().mul(matrix4[i]);
			System.currentTimeMillis();
		}

		int pojoTotal = 0;
		int slowTotal = 0;
		int nativeTotal = 0;
		int count = 0;

		for (final long stop = MSEC_FOR_TEST + System.currentTimeMillis(); stop >= System.currentTimeMillis(); count++) {

			nativeResult.idt();
			final long nativeStart = System.currentTimeMillis();
			for (int i = 0; i < matrix4.length; i++) {
				Matrix4.mul(nativeResult.val, matrix4[i].val);
			}
			final long nativeStop = System.currentTimeMillis();
			final long nativeTime = nativeStop - nativeStart;
			nativeTotal += (int) nativeTime;



			slowResult.idt();
			final long slowStart = System.currentTimeMillis();
			for (int i = 0; i < matrix4.length; i++) {
				slowResult = slowResult.cpy().mul(matrix4[i]);
			}
			final long slowStop = System.currentTimeMillis();
			final long slowTime = slowStop - slowStart;
			slowTotal += (int) slowTime;



			pojoResult.idt();
			final long pojoStart = System.currentTimeMillis();
			for (int i = 0; i < matrix4.length; i++) {
				Matrix4.mul(pojoResult.val, matrix4[i].val);
			}
			final long pojoStop = System.currentTimeMillis();
			final long pojoTime = pojoStop - pojoStart;
			pojoTotal += (int) pojoTime;


		}

		final StringBuilder builder = new StringBuilder();
		Gdx.app.log("App", "==> Ran " + count + " times over " + MSEC_FOR_TEST + " msec");
		//	builder.append(count).append('/');
		Gdx.app.log("App", "pojoTotal = " + pojoTotal);
		builder.append(pojoTotal).append('/');
		Gdx.app.log("App", "nativeTotal = " + nativeTotal);
		builder.append(nativeTotal).append('/');
		Gdx.app.log("App", "slowTotal = " + slowTotal);
		result = builder.toString();
	}
}
