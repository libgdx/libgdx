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
import com.badlogic.gdx.tests.g3d.MaterialTest;
import com.badlogic.gdx.tests.g3d.ModelTest;
import com.badlogic.gdx.tests.g3d.ShaderCollectionTest;
import com.badlogic.gdx.tests.g3d.ShaderTest;
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
		AnimationTest.class, AccelerometerTest.class, ActionTest.class, ActionSequenceTest.class, LetterBoxTest3.class,
		GroupTest.class, AlphaTest.class, AtlasIssueTest.class, AssetManagerTest.class, FilterPerformanceTest.class,
		AudioDeviceTest.class, AudioRecorderTest.class, BitmapFontAlignmentTest.class, BitmapFontDistanceFieldTest.class, BitmapFontFlipTest.class,
		GroupCullingTest.class, GestureDetectorTest.class, LabelTest.class, BitmapFontMetricsTest.class, BlitTest.class, TableTest.class,
		BobTest.class, ImageScaleTest.class, TableLayoutTest.class, Box2DTest.class, BulletTestCollection.class, InterpolationTest.class, TouchpadTest.class,
		Box2DTestCollection.class, BufferUtilsTest.class, ImageTest.class, CompassTest.class, ComplexActionTest.class,
		CullTest.class, DeltaTimeTest.class, EarClippingTriangulatorTest.class, EdgeDetectionTest.class, ETC1Test.class, ExitTest.class, FilesTest.class,
		ScrollPaneTest.class, FloatTest.class, FloatTextureTest.class, FrameBufferTest.class, FramebufferToTextureTest.class, FrustumTest.class,
		FullscreenTest.class, Gdx2DTest.class, GroupFadeTest.class, ImmediateModeRendererTest.class, Scene2dTest.class,
		ImmediateModeRendererAlphaTest.class, IndexBufferObjectClassTest.class, TreeTest.class, IndexBufferObjectShaderTest.class,
		InputTest.class, IntegerBitmapFontTest.class, InverseKinematicsTest.class, IsoCamTest.class, IsometricTileTest.class,
		KinematicBodyTest.class, LifeCycleTest.class, LineDrawingTest.class, ScrollPane2Test.class, ManagedTest.class,
		ManualBindTest.class, MaterialTest.class, MatrixJNITest.class, MeshMultitextureTest.class, MeshShaderTest.class, MeshTest.class,
		MipMapTest.class, MultitouchTest.class, MusicTest.class, MyFirstTriangle.class, ObjTest.class, OnscreenKeyboardTest.class,
		OrthoCamBorderTest.class, ParallaxTest.class, ParticleEmitterTest.class, PickingTest.class, PixelsPerInchTest.class,
		PixmapBlendingTest.class, PixmapTest.class, PixmapPackerTest.class, PolygonRegionTest.class, PolygonSpriteTest.class, PreferencesTest.class,
		ProjectiveTextureTest.class, Pong.class, ProjectTest.class, RemoteTest.class, RotationTest.class, DragAndDropTest.class,
		ShaderMultitextureTest.class, ShadowMappingTest.class, PathTest.class, SimpleAnimationTest.class, SimpleDecalTest.class,
		SimpleStageCullingTest.class, SoundTest.class, SpriteCacheTest.class, SpriteCacheOffsetTest.class, LetterBoxTest1.class,
		SpriteBatchRotationTest.class, SpriteBatchShaderTest.class, SpriteBatchTest.class, SpritePerformanceTest.class,
		SpritePerformanceTest2.class, StagePerformanceTest.class, StageTest.class, TerrainTest.class, TextureDataTest.class,
		TextureDownloadTest.class, TextureFormatTest.class, TextureAtlasTest.class, TextInputDialogTest.class,
		TextureRenderTest.class, TileTest.class, UITest.class, VBOVATest.class, VertexArrayTest.class,
		VertexBufferObjectTest.class, VertexArrayClassTest.class, VertexBufferObjectClassTest.class, LetterBoxTest2.class,
		VertexBufferObjectShaderTest.class, VibratorTest.class, WaterRipples.class, HelloTriangle.class,
		SimpleVertexShader.class, ShapeRendererTest.class, MoveSpriteExample.class, UISimpleTest.class,
		// SoundTouchTest.class, Mpg123Test.class, WavTest.class, FreeTypeTest.class,
		// InternationalFontsTest.class, VorbisTest.class
		TextButtonTest.class, TextButtonTestGL2.class, TextureBindTest.class, SortedSpriteTest.class, DelaunayTriangulatorTest.class,
		ExternalMusicTest.class, SoftKeyboardTest.class, DirtyRenderingTest.class, YDownTest.class, ShaderCollectionTest.class,
		ScreenCaptureTest.class, BitmapFontTest.class, LabelScaleTest.class, GamepadTest.class, NetAPITest.class, TideMapAssetManagerTest.class, TideMapDirectLoaderTest.class, TiledMapAssetManagerTest.class, TiledMapBench.class,
		RunnablePostTest.class, Vector2dTest.class, SuperKoalio.class, NinePatchTest.class, Basic3DSceneTest.class, Animation3DTest.class,
		ModelTest.class, Basic3DTest.class, ShaderTest.class, SkeletonTest.class, HexagonalTiledMapTest.class, FogTest.class, TimerTest.class, Bresenham2Test.class));
	
	public static List<String> getNames () {
		List<String> names = new ArrayList<String>(tests.size());
		for (Class clazz : tests)
			names.add(clazz.getSimpleName());
		Collections.sort(names);
		return names;
	}

	private static Class<? extends GdxTest> forName (String name)
	{
		for (Class clazz : tests)
			if (clazz.getSimpleName().equals(name))
				return clazz;
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
