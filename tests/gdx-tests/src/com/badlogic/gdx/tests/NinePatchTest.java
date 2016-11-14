/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.Array;

public class NinePatchTest extends GdxTest {
	/** A string name for the type of test, and the NinePatch being tested. */
	private static class TestPatch {
		public final String name;
		public final NinePatch ninePatch;

		TestPatch (String n) {
			this.name = n;
			this.ninePatch = NinePatchTest.newNinePatch();
		}

		TestPatch (String n, NinePatch np) {
			this.name = n;
			this.ninePatch = np;
		}
	}

	private OrthographicCamera camera;
	private SpriteBatch b;
	private Array<TestPatch> ninePatches = new Array<TestPatch>(10);

	private final long start = System.currentTimeMillis();

	@Override
	public void create () {
		TestPatch tp;

		// Create all the NinePatches to test
		ninePatches.add(new TestPatch("default"));

		tp = new TestPatch("20px width");
		int bWidth = 20;
		tp.ninePatch.setLeftWidth(bWidth);
		tp.ninePatch.setRightWidth(bWidth);
		tp.ninePatch.setTopHeight(bWidth);
		tp.ninePatch.setBottomHeight(bWidth);
		ninePatches.add(tp);

		tp = new TestPatch("fat left");
		tp.ninePatch.setLeftWidth(3 * tp.ninePatch.getRightWidth());
		ninePatches.add(tp);

		tp = new TestPatch("fat top");
		tp.ninePatch.setTopHeight(3 * tp.ninePatch.getBottomHeight());
		ninePatches.add(tp);

		tp = new TestPatch("degenerate", newDegenerateNinePatch());
		ninePatches.add(tp);

		tp = new TestPatch("upper-left quad", newULQuadPatch());
		ninePatches.add(tp);

		tp = new TestPatch("no middle row", newMidlessPatch());
		ninePatches.add(tp);

		b = new SpriteBatch();
	}

	// Make a new 'pixmapSize' square texture region with 'patchSize' patches in it. Each patch is a different color.
	static TextureRegion newPatchPix (int patchSize, int pixmapSize) {
		final int pixmapDim = MathUtils.nextPowerOfTwo(pixmapSize);

		Pixmap p = new Pixmap(pixmapDim, pixmapDim, Pixmap.Format.RGBA8888);
		p.setColor(1, 1, 1, 0);
		p.fill();

		for (int x = 0; x < pixmapSize; x += patchSize) {
			for (int y = 0; y < pixmapSize; y += patchSize) {
				p.setColor(x / (float)pixmapSize, y / (float)pixmapSize, 1.0f, 1.0f);
				p.fillRectangle(x, y, patchSize, patchSize);
			}
		}

		return new TextureRegion(new Texture(p), pixmapSize, pixmapSize);
	}

	// Make a degenerate NinePatch
	static NinePatch newDegenerateNinePatch () {
		final int patchSize = 8;
		final int pixmapSize = patchSize * 3;
		TextureRegion tr = newPatchPix(patchSize, pixmapSize);
		return new NinePatch(tr);
	}

	// Make a basic NinePatch with different colors in each of the nine patches
	static NinePatch newNinePatch () {
		final int patchSize = 8;
		final int pixmapSize = patchSize * 3;
		TextureRegion tr = newPatchPix(patchSize, pixmapSize);

		return new NinePatch(tr, patchSize, patchSize, patchSize, patchSize);
	}

	// Make a upper-left "quad" patch (only 4 patches defined in the top-left corner of the ninepatch)
	static NinePatch newULQuadPatch () {
		final int patchSize = 8;
		final int pixmapSize = patchSize * 2;
		TextureRegion tr = newPatchPix(patchSize, pixmapSize);

		return new NinePatch(tr, patchSize, 0, patchSize, 0);
	}

	// Make a ninepatch with no middle band, just top three and bottom three.
	static NinePatch newMidlessPatch () {
		final int patchSize = 8;
		final int fullPatchHeight = patchSize * 2;
		final int fullPatchWidth = patchSize * 3;
		final int pixmapDim = MathUtils.nextPowerOfTwo(Math.max(fullPatchWidth, fullPatchHeight));

		Pixmap testPatch = new Pixmap(pixmapDim, pixmapDim, Pixmap.Format.RGBA8888);
		testPatch.setColor(1, 1, 1, 0);
		testPatch.fill();

		for (int x = 0; x < fullPatchWidth; x += patchSize) {
			for (int y = 0; y < fullPatchHeight; y += patchSize) {
				testPatch.setColor(x / (float)fullPatchWidth, y / (float)fullPatchHeight, 1.0f, 1.0f);
				testPatch.fillRectangle(x, y, patchSize, patchSize);
			}
		}

		return new NinePatch(new TextureRegion(new Texture(testPatch), fullPatchWidth, fullPatchHeight), patchSize, patchSize,
			patchSize, patchSize);
	}

	private float timePassed = 0;
	private final Color filterColor = new Color();
	private final Color oldColor = new Color();

	@Override
	public void render () {
		final int screenWidth = Gdx.graphics.getWidth();
		final int screenHeight = Gdx.graphics.getHeight();

		Gdx.gl.glClearColor(0, 0, 0, 0);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		timePassed += Gdx.graphics.getDeltaTime();

		b.begin();
		final int sz = ninePatches.size;
		final int XGAP = 10;
		final int pheight = (int)((screenHeight * 0.5f) / ((sz + 1) / 2));
		int x = XGAP;
		int y = 10;

		// Test that batch color is applied to NinePatch
		if (timePassed < 2) {
			b.setColor(1, 1, 1, Interpolation.sine.apply(timePassed / 2f));
		}

		// Test that the various nine patches render
		for (int i = 0; i < sz; i += 2) {
			int pwidth = (int)(0.44f * screenWidth);

			final NinePatch np1 = ninePatches.get(i).ninePatch;
			np1.draw(b, x, y, pwidth, pheight);

			if (i + 1 < sz) {
				final NinePatch np2 = ninePatches.get(i + 1).ninePatch;
				final int x2 = x + pwidth + XGAP;
				final int pwidth2 = screenWidth - XGAP - x2;

				np2.draw(b, x2, y, pwidth2, pheight);
			}

			y += pheight + 2;
		}

		// Dim a np by setting its color. Also test sending same np to batch twice
		NinePatch np = ninePatches.get(0).ninePatch;
		oldColor.set(np.getColor());
		filterColor.set(0.3f, 0.3f, 0.3f, 1.0f);
		np.setColor(filterColor);
		np.draw(b, x, y, 100, 30);
		np.setColor(oldColor);

		b.end();
	}

	@Override
	public void resize (int width, int height) {
		float ratio = ((float)Gdx.graphics.getWidth() / (float)Gdx.graphics.getHeight());
		int h = 10;
		int w = (int)(h * ratio);
		camera = new OrthographicCamera(w, h);
	}
}
