
package com.badlogic.gdx.tests.gwt.client;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.gwt.GwtApplication;
import com.badlogic.gdx.backends.gwt.GwtApplicationConfiguration;
import com.badlogic.gdx.tests.AlphaTest;
import com.badlogic.gdx.tests.AnimationTest;
import com.badlogic.gdx.tests.AssetManagerTest;
import com.badlogic.gdx.tests.AtlasIssueTest;
import com.badlogic.gdx.tests.BitmapFontAlignmentTest;
import com.badlogic.gdx.tests.BitmapFontFlipTest;
import com.badlogic.gdx.tests.BitmapFontTest;
import com.badlogic.gdx.tests.BlitTest;
import com.badlogic.gdx.tests.CustomShaderSpriteBatchTest;
import com.badlogic.gdx.tests.DecalTest;
import com.badlogic.gdx.tests.EdgeDetectionTest;
import com.badlogic.gdx.tests.FilterPerformanceTest;
import com.badlogic.gdx.tests.FrameBufferTest;
import com.badlogic.gdx.tests.GestureDetectorTest;
import com.badlogic.gdx.tests.IndexBufferObjectShaderTest;
import com.badlogic.gdx.tests.IntegerBitmapFontTest;
import com.badlogic.gdx.tests.InverseKinematicsTest;
import com.badlogic.gdx.tests.IsoCamTest;
import com.badlogic.gdx.tests.IsometricTileTest;
import com.badlogic.gdx.tests.MatrixJNITest;
import com.badlogic.gdx.tests.MeshShaderTest;
import com.badlogic.gdx.tests.MultitouchTest;
import com.badlogic.gdx.tests.MusicTest;
import com.badlogic.gdx.tests.ParallaxTest;
import com.badlogic.gdx.tests.ParticleEmitterTest;
import com.badlogic.gdx.tests.PixelsPerInchTest;
import com.badlogic.gdx.tests.RotationTest;
import com.badlogic.gdx.tests.ShaderMultitextureTest;
import com.badlogic.gdx.tests.ShapeRendererTest;
import com.badlogic.gdx.tests.SimpleAnimationTest;
import com.badlogic.gdx.tests.SimpleDecalTest;
import com.badlogic.gdx.tests.SortedSpriteTest;
import com.badlogic.gdx.tests.SpriteBatchShaderTest;
import com.badlogic.gdx.tests.SpriteCacheOffsetTest;
import com.badlogic.gdx.tests.SpriteCacheTest;
import com.badlogic.gdx.tests.TextureAtlasTest;
import com.badlogic.gdx.tests.VertexBufferObjectShaderTest;
import com.badlogic.gdx.tests.YDownTest;
import com.badlogic.gdx.tests.utils.GdxTest;

public class GwtTestStarter extends GwtApplication {
	GdxTest[] tests = {
		new AlphaTest(),
		new AnimationTest(),
		new AssetManagerTest(),
		new AtlasIssueTest(),
		new BitmapFontAlignmentTest(),
		new BitmapFontFlipTest(),
		new BitmapFontTest(),
		new BlitTest(),
		new CustomShaderSpriteBatchTest(),
		new DecalTest(),
		new EdgeDetectionTest(),
		new FilterPerformanceTest(),
		new FrameBufferTest(),
		new GestureDetectorTest(),
		new IndexBufferObjectShaderTest(),
		new IntegerBitmapFontTest(),
		new InverseKinematicsTest(),
		new IsoCamTest(),
		new IsometricTileTest(),
		new MatrixJNITest(),
		new MeshShaderTest(),
		new MultitouchTest(),
		new MusicTest(),
		new ParallaxTest(),
		new ParticleEmitterTest(),
		new PixelsPerInchTest(),
//		new PixmapBlendingTest(), // no idea why this doesn't work
		new RotationTest(),
		new ShaderMultitextureTest(),
		new ShapeRendererTest(),
		new SimpleAnimationTest(),
		new SimpleDecalTest(),
		new SortedSpriteTest(),
		new SpriteBatchShaderTest(),
		new SpriteCacheOffsetTest(),
		new SpriteCacheTest(),
		new TextureAtlasTest(),
		new VertexBufferObjectShaderTest(),
		new YDownTest()
	};
	
	@Override
	public GwtApplicationConfiguration getConfig () {
		return new GwtApplicationConfiguration(640, 640);
	}

	@Override
	public ApplicationListener getApplicationListener () {
		return new AssetManagerTest();
	}
}
