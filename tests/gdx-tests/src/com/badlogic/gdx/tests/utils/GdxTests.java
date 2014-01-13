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
/*
 * Copyright 2010 Mario Zechner (contact@badlogicgames.com), Nathan Sweet (admin@esotericsoftware.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package com.badlogic.gdx.tests.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.badlogic.gdx.tests.*;
import com.badlogic.gdx.tests.bench.TiledMapBench;
import com.badlogic.gdx.tests.examples.MoveSpriteExample;
import com.badlogic.gdx.tests.g3d.Animation3DTest;
import com.badlogic.gdx.tests.g3d.Basic3DSceneTest;
import com.badlogic.gdx.tests.g3d.Basic3DTest;
import com.badlogic.gdx.tests.g3d.FogTest;
import com.badlogic.gdx.tests.g3d.LightsTest;
import com.badlogic.gdx.tests.g3d.MaterialTest;
import com.badlogic.gdx.tests.g3d.ModelTest;
import com.badlogic.gdx.tests.g3d.ShaderCollectionTest;
import com.badlogic.gdx.tests.g3d.ShaderTest;
import com.badlogic.gdx.tests.g3d.ShadowMappingTest;
import com.badlogic.gdx.tests.g3d.SkeletonTest;
import com.badlogic.gdx.tests.gles2.HelloTriangle;
import com.badlogic.gdx.tests.gles2.SimpleVertexShader;
import com.badlogic.gdx.tests.net.NetAPITest;
import com.badlogic.gdx.tests.superkoalio.SuperKoalio;

/** List of GdxTest classes. To be used by the test launchers. If you write your own test, add it in here!
 * 
 * @author badlogicgames@gmail.com */
public class GdxTests {
	public static final List<Class<? extends GdxTest>> tests = new ArrayList<Class<? extends GdxTest>>(Arrays.asList(
		// @off
		AccelerometerTest.class,
		ActionSequenceTest.class,
		ActionTest.class,
		AlphaTest.class,
		Animation3DTest.class,
		AnimationTest.class,
		AssetManagerTest.class,
		AtlasIssueTest.class,
		AudioDeviceTest.class,
		AudioRecorderTest.class,
		Basic3DSceneTest.class,
		Basic3DTest.class,
		BitmapFontAlignmentTest.class,
		BitmapFontDistanceFieldTest.class,
		BitmapFontFlipTest.class,
		BitmapFontMetricsTest.class,
		BitmapFontTest.class,
		BlitTest.class,
		BobTest.class,
		Box2DTest.class,
		Box2DTestCollection.class,
		Bresenham2Test.class,
		BufferUtilsTest.class,
		BulletTestCollection.class,
		CompassTest.class,
		ComplexActionTest.class,
		CullTest.class,
		DelaunayTriangulatorTest.class,
		DeltaTimeTest.class,
		DirtyRenderingTest.class,
		DragAndDropTest.class,
		ETC1Test.class,
		EarClippingTriangulatorTest.class,
		EdgeDetectionTest.class,
		ExitTest.class,
		ExternalMusicTest.class,
		FilesTest.class,
		FilterPerformanceTest.class,
		FloatTest.class,
		FloatTextureTest.class,
		FogTest.class,
		FrameBufferTest.class,
		FramebufferToTextureTest.class,
		FrustumTest.class,
		FullscreenTest.class,
		GamepadTest.class,
		Gdx2DTest.class,
		GestureDetectorTest.class,
		GroupCullingTest.class,
		GroupFadeTest.class,
		GroupTest.class,
		HelloTriangle.class,
		HexagonalTiledMapTest.class,
		ImageScaleTest.class,
		ImageTest.class,
		ImmediateModeRendererAlphaTest.class,
		ImmediateModeRendererTest.class,
		IndexBufferObjectClassTest.class,
		IndexBufferObjectShaderTest.class,
		InputTest.class,
		IntegerBitmapFontTest.class,
		InterpolationTest.class,
		InverseKinematicsTest.class,
		IsometricTileTest.class,
		KinematicBodyTest.class,
		LabelScaleTest.class,
		LabelTest.class,
		LetterBoxTest1.class,
		LetterBoxTest2.class,
		LetterBoxTest3.class,
		LifeCycleTest.class,
		LightsTest.class,
		LineDrawingTest.class,
		LiquidFunTest.class,
		ManagedTest.class,
		ManualBindTest.class,
		MaterialTest.class,
		MatrixJNITest.class,
		MeshMultitextureTest.class,
		MeshShaderTest.class,
		MeshTest.class,
		MipMapTest.class,
		ModelTest.class,
		MoveSpriteExample.class,
		MultitouchTest.class,
		MusicTest.class,
		MyFirstTriangle.class,
		NetAPITest.class,
		NinePatchTest.class,
		ObjTest.class,
		OnscreenKeyboardTest.class,
		OrthoCamBorderTest.class,
		ParallaxTest.class,
		ParticleEmitterTest.class,
		PathTest.class,
		PickingTest.class,
		PixelsPerInchTest.class,
		PixmapBlendingTest.class,
		PixmapPackerTest.class,
		PixmapTest.class,
		PolygonRegionTest.class,
		PolygonSpriteTest.class,
		Pong.class,
		PreferencesTest.class,
		ProjectTest.class,
		ProjectiveTextureTest.class,
		ReflectionTest.class,
		RemoteTest.class,
		RotationTest.class,
		RunnablePostTest.class,
		Scene2dTest.class,
		ScreenCaptureTest.class,
		ScrollPane2Test.class,
		ScrollPaneScrollBarsTest.class,
		ScrollPaneTest.class,
		SelectTest.class,
		ShaderCollectionTest.class,
		ShaderMultitextureTest.class,
		ShaderTest.class,
		ShadowMappingTest.class,
		ShapeRendererTest.class,
		SimpleAnimationTest.class,
		SimpleDecalTest.class,
		SimpleStageCullingTest.class,
		SimpleVertexShader.class,
		SkeletonTest.class,
		SoftKeyboardTest.class,
		SortedSpriteTest.class,
		SoundTest.class,
		SpriteBatchRotationTest.class,
		SpriteBatchShaderTest.class,
		SpriteBatchTest.class,
		SpriteCacheOffsetTest.class,
		SpriteCacheTest.class,
		SpritePerformanceTest.class,
		SpritePerformanceTest2.class,
		StagePerformanceTest.class,
		StageTest.class,
		SuperKoalio.class,
		TableLayoutTest.class,
		TableTest.class,
		TerrainTest.class,
		TextButtonTest.class,
		TextButtonTestGL2.class,
		TextInputDialogTest.class,
		TextureAtlasTest.class,
		TextureBindTest.class,
		TextureDataTest.class,
		TextureDownloadTest.class,
		TextureFormatTest.class,
		TextureRenderTest.class,
		TideMapAssetManagerTest.class,
		TideMapDirectLoaderTest.class,
		TileTest.class,
		TiledMapAssetManagerTest.class,
		TiledMapAtlasAssetManagerTest.class,
		TiledMapBench.class,
		TimerTest.class,
		TouchpadTest.class,
		TreeTest.class,
		UISimpleTest.class,
		UITest.class,
		VBOVATest.class,
		Vector2dTest.class,
		VertexArrayClassTest.class,
		VertexArrayTest.class,
		VertexBufferObjectClassTest.class,
		VertexBufferObjectShaderTest.class,
		VertexBufferObjectTest.class,
		VibratorTest.class,
		WaterRipples.class,
		YDownTest.class
		// @on

		// SoundTouchTest.class, Mpg123Test.class, WavTest.class, FreeTypeTest.class,
		// InternationalFontsTest.class, VorbisTest.class
		));

	public static List<String> getNames () {
		List<String> names = new ArrayList<String>(tests.size());
		for (Class clazz : tests)
			names.add(clazz.getSimpleName());
		Collections.sort(names);
		return names;
	}

	private static Class<? extends GdxTest> forName (String name) {
		for (Class clazz : tests)
			if (clazz.getSimpleName().equals(name)) return clazz;
		return null;
	}

	public static GdxTest newTest (String testName) {
		try {
			return forName(testName).newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}
}
