
package com.badlogic.gdx.tests.gwt.client;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.gwt.GwtApplication;
import com.badlogic.gdx.backends.gwt.GwtApplicationConfiguration;
import com.badlogic.gdx.tests.AccelerometerTest;
import com.badlogic.gdx.tests.ActionSequenceTest;
import com.badlogic.gdx.tests.ActionTest;
import com.badlogic.gdx.tests.AlphaTest;
import com.badlogic.gdx.tests.AnimationTest;
import com.badlogic.gdx.tests.AssetManagerTest;
import com.badlogic.gdx.tests.AtlasIssueTest;
import com.badlogic.gdx.tests.BitmapFontAlignmentTest;
import com.badlogic.gdx.tests.BitmapFontFlipTest;
import com.badlogic.gdx.tests.BitmapFontTest;
import com.badlogic.gdx.tests.BlitTest;
import com.badlogic.gdx.tests.Box2DCharacterControllerTest;
import com.badlogic.gdx.tests.Box2DTest;
import com.badlogic.gdx.tests.Box2DTestCollection;
import com.badlogic.gdx.tests.ComplexActionTest;
import com.badlogic.gdx.tests.CustomShaderSpriteBatchTest;
import com.badlogic.gdx.tests.DecalTest;
import com.badlogic.gdx.tests.EdgeDetectionTest;
import com.badlogic.gdx.tests.FilterPerformanceTest;
import com.badlogic.gdx.tests.FlickScrollPaneTest;
import com.badlogic.gdx.tests.FrameBufferTest;
import com.badlogic.gdx.tests.GestureDetectorTest;
import com.badlogic.gdx.tests.GroupCullingTest;
import com.badlogic.gdx.tests.GroupFadeTest;
import com.badlogic.gdx.tests.ImageScaleTest;
import com.badlogic.gdx.tests.ImageTest;
import com.badlogic.gdx.tests.IndexBufferObjectShaderTest;
import com.badlogic.gdx.tests.IntegerBitmapFontTest;
import com.badlogic.gdx.tests.InverseKinematicsTest;
import com.badlogic.gdx.tests.IsoCamTest;
import com.badlogic.gdx.tests.IsometricTileTest;
import com.badlogic.gdx.tests.KinematicBodyTest;
import com.badlogic.gdx.tests.LabelTest;
import com.badlogic.gdx.tests.LifeCycleTest;
import com.badlogic.gdx.tests.MatrixJNITest;
import com.badlogic.gdx.tests.MeshShaderTest;
import com.badlogic.gdx.tests.MipMapTest;
import com.badlogic.gdx.tests.MultitouchTest;
import com.badlogic.gdx.tests.MusicTest;
import com.badlogic.gdx.tests.ParallaxTest;
import com.badlogic.gdx.tests.ParticleEmitterTest;
import com.badlogic.gdx.tests.PixelsPerInchTest;
import com.badlogic.gdx.tests.ProjectiveTextureTest;
import com.badlogic.gdx.tests.RotationTest;
import com.badlogic.gdx.tests.ScrollPaneTest;
import com.badlogic.gdx.tests.ShaderMultitextureTest;
import com.badlogic.gdx.tests.ShadowMappingTest;
import com.badlogic.gdx.tests.ShapeRendererTest;
import com.badlogic.gdx.tests.SimpleAnimationTest;
import com.badlogic.gdx.tests.SimpleDecalTest;
import com.badlogic.gdx.tests.SimpleStageCullingTest;
import com.badlogic.gdx.tests.SortedSpriteTest;
import com.badlogic.gdx.tests.SoundTest;
import com.badlogic.gdx.tests.SpriteBatchShaderTest;
import com.badlogic.gdx.tests.SpriteCacheOffsetTest;
import com.badlogic.gdx.tests.SpriteCacheTest;
import com.badlogic.gdx.tests.StagePerformanceTest;
import com.badlogic.gdx.tests.StageTest;
import com.badlogic.gdx.tests.TableTest;
import com.badlogic.gdx.tests.TextButtonTest;
import com.badlogic.gdx.tests.TextButtonTestGL2;
import com.badlogic.gdx.tests.TextureAtlasTest;
import com.badlogic.gdx.tests.UITest;
import com.badlogic.gdx.tests.VertexBufferObjectShaderTest;
import com.badlogic.gdx.tests.YDownTest;
import com.badlogic.gdx.tests.utils.GdxTest;

public class GwtTestStarter extends GwtApplication {
	GdxTest[] tests = {
		new AccelerometerTest(),
		new ActionTest(),
		new ActionSequenceTest(),
		new AlphaTest(),
		new AnimationTest(),
		new AssetManagerTest(),
		new AtlasIssueTest(),
		new BitmapFontAlignmentTest(),
		new BitmapFontFlipTest(),
		new BitmapFontTest(),
		new BlitTest(),
		new Box2DCharacterControllerTest(),
		new Box2DTest(),
		new Box2DTestCollection(),
		new ComplexActionTest(),
		new CustomShaderSpriteBatchTest(),
		new DecalTest(),
		new EdgeDetectionTest(),
		new FilterPerformanceTest(),
		new FlickScrollPaneTest(),
		new FrameBufferTest(),
		new GestureDetectorTest(),
		new GroupCullingTest(),
		new GroupFadeTest(),
		new ImageScaleTest(),
		new ImageTest(),
		new IndexBufferObjectShaderTest(),
		new IntegerBitmapFontTest(),
		new InverseKinematicsTest(),
		new IsoCamTest(),
		new IsometricTileTest(),
		new KinematicBodyTest(),
		new LifeCycleTest(),
		new LabelTest(),
		new MatrixJNITest(),
		new MeshShaderTest(),
		new MipMapTest(),
		new MultitouchTest(),
		new MusicTest(),
		new ParallaxTest(),
		new ParticleEmitterTest(),
		new PixelsPerInchTest(),
//		new PixmapBlendingTest(), // FIXME no idea why this doesn't work
		new ProjectiveTextureTest(),
		new RotationTest(),
		new ScrollPaneTest(),
		new ShaderMultitextureTest(),
		new ShadowMappingTest(),
		new ShapeRendererTest(),
		new SimpleAnimationTest(),
		new SimpleDecalTest(),
		new SimpleStageCullingTest(),
		new SortedSpriteTest(),
		new SpriteBatchShaderTest(),
		new SpriteCacheOffsetTest(),
		new SpriteCacheTest(),
		new SoundTest(),
		new StageTest(),
		new StagePerformanceTest(),
		new TableTest(),
		new TextButtonTest(),
		new TextButtonTestGL2(),
		new TextureAtlasTest(),
		new UITest(),
		new VertexBufferObjectShaderTest(),
		new YDownTest()
	};
	
	@Override
	public GwtApplicationConfiguration getConfig () {
		return new GwtApplicationConfiguration(640, 640);
	}

	@Override
	public ApplicationListener getApplicationListener () {
		return new Box2DTestCollection();
	}
}
