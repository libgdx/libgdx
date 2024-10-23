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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.badlogic.gdx.tests.*;
import com.badlogic.gdx.tests.bench.TiledMapBench;
import com.badlogic.gdx.tests.conformance.AudioSoundAndMusicIsolationTest;
import com.badlogic.gdx.tests.conformance.DisplayModeTest;
import com.badlogic.gdx.tests.examples.MoveSpriteExample;
import com.badlogic.gdx.tests.extensions.FreeTypeAtlasTest;
import com.badlogic.gdx.tests.extensions.FreeTypeDisposeTest;
import com.badlogic.gdx.tests.extensions.FreeTypeFontLoaderTest;
import com.badlogic.gdx.tests.extensions.FreeTypeIncrementalTest;
import com.badlogic.gdx.tests.extensions.FreeTypeMetricsTest;
import com.badlogic.gdx.tests.extensions.FreeTypePackTest;
import com.badlogic.gdx.tests.extensions.FreeTypeTest;
import com.badlogic.gdx.tests.extensions.InternationalFontsTest;
import com.badlogic.gdx.tests.gles3.NonPowerOfTwoTest;
import com.badlogic.gdx.tests.gles3.UniformBufferObjectsTest;
import com.badlogic.gdx.tests.math.CollisionPlaygroundTest;
import com.badlogic.gdx.tests.math.OctreeTest;
import com.badlogic.gdx.tests.g3d.Animation3DTest;
import com.badlogic.gdx.tests.g3d.AnisotropyTest;
import com.badlogic.gdx.tests.g3d.Basic3DSceneTest;
import com.badlogic.gdx.tests.g3d.Basic3DTest;
import com.badlogic.gdx.tests.g3d.Benchmark3DTest;
import com.badlogic.gdx.tests.g3d.FogTest;
import com.badlogic.gdx.tests.g3d.FrameBufferCubemapTest;
import com.badlogic.gdx.tests.g3d.HeightMapTest;
import com.badlogic.gdx.tests.g3d.LightsTest;
import com.badlogic.gdx.tests.g3d.MaterialEmissiveTest;
import com.badlogic.gdx.tests.g3d.MaterialTest;
import com.badlogic.gdx.tests.g3d.MeshBuilderTest;
import com.badlogic.gdx.tests.g3d.ModelCacheTest;
import com.badlogic.gdx.tests.g3d.ModelTest;
import com.badlogic.gdx.tests.g3d.MultipleRenderTargetTest;
import com.badlogic.gdx.tests.g3d.ParticleControllerInfluencerSingleTest;
import com.badlogic.gdx.tests.g3d.ParticleControllerTest;
import com.badlogic.gdx.tests.g3d.PolarAccelerationTest;
import com.badlogic.gdx.tests.g3d.ShaderCollectionTest;
import com.badlogic.gdx.tests.g3d.ShaderTest;
import com.badlogic.gdx.tests.g3d.ShadowMappingTest;
import com.badlogic.gdx.tests.g3d.SkeletonTest;
import com.badlogic.gdx.tests.g3d.TangentialAccelerationTest;
import com.badlogic.gdx.tests.g3d.TextureArrayTest;
import com.badlogic.gdx.tests.g3d.TextureRegion3DTest;
import com.badlogic.gdx.tests.g3d.utils.DefaultTextureBinderTest;
import com.badlogic.gdx.tests.gles2.GlTexImage2D;
import com.badlogic.gdx.tests.gles2.HelloTriangle;
import com.badlogic.gdx.tests.gles2.SimpleVertexShader;
import com.badlogic.gdx.tests.gles2.VertexArrayTest;
import com.badlogic.gdx.tests.gles3.GL30Texture3DTest;
import com.badlogic.gdx.tests.gles3.InstancedRenderingTest;
import com.badlogic.gdx.tests.gles3.ModelInstancedRenderingTest;
import com.badlogic.gdx.tests.gles3.PixelBufferObjectTest;
import com.badlogic.gdx.tests.gles31.GL31FrameBufferMultisampleMRTTest;
import com.badlogic.gdx.tests.gles31.GL31FrameBufferMultisampleTest;
import com.badlogic.gdx.tests.gles31.GL31IndirectDrawingIndexedTest;
import com.badlogic.gdx.tests.gles31.GL31IndirectDrawingNonIndexedTest;
import com.badlogic.gdx.tests.gles31.GL31ProgramIntrospectionTest;
import com.badlogic.gdx.tests.gles32.GL32AdvancedBlendingTest;
import com.badlogic.gdx.tests.gles32.GL32DebugControlTest;
import com.badlogic.gdx.tests.gles32.GL32MultipleRenderTargetsBlendingTest;
import com.badlogic.gdx.tests.gles32.GL32OffsetElementsTest;
import com.badlogic.gdx.tests.math.collision.OrientedBoundingBoxTest;
import com.badlogic.gdx.tests.net.NetAPITest;
import com.badlogic.gdx.tests.superkoalio.SuperKoalio;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.StreamUtils;

/** List of GdxTest classes. To be used by the test launchers. If you write your own test, add it in here!
 * 
 * @author badlogicgames@gmail.com */
public class GdxTests {
	public static final List<Class<? extends GdxTest>> tests = new ArrayList<Class<? extends GdxTest>>(Arrays.asList(
	// @off
		IssueTest.class,
		AccelerometerTest.class,
		ActionSequenceTest.class,
		ActionTest.class,
		Affine2Test.class,
		AlphaTest.class,
		Animation3DTest.class,
		AnimationTest.class,
		AnisotropyTest.class,
		AnnotationTest.class,
		AssetManagerTest.class,
		AtlasIssueTest.class,
		AudioChangeDeviceTest.class,
		AudioDeviceTest.class,
		AudioRecorderTest.class,
		AudioSoundAndMusicIsolationTest.class,
		Basic3DSceneTest.class,
		Basic3DTest.class,
		Benchmark3DTest.class,
		BigMeshTest.class,
		BitmapFontAlignmentTest.class,
		BitmapFontDistanceFieldTest.class,
		BitmapFontFlipTest.class,
		BitmapFontMetricsTest.class,
		BitmapFontTest.class,
		BitmapFontAtlasRegionTest.class,
		BlitTest.class,
		Box2DTest.class,
		Box2DTestCollection.class,
		Bresenham2Test.class,
		BufferUtilsTest.class,
		BulletTestCollection.class,
		ClipboardTest.class,
		CollectionsTest.class,
		CollisionPlaygroundTest.class,
		ColorTest.class,
		ContainerTest.class,
		CoordinatesTest.class,
		CpuSpriteBatchTest.class,
		CullTest.class,
		CursorTest.class,
		DecalTest.class,
		DefaultTextureBinderTest.class,
		DelaunayTriangulatorTest.class,
		DeltaTimeTest.class,
		DirtyRenderingTest.class,
		DisplayModeTest.class,
		DownloadTest.class,
		DragAndDropTest.class,
		ETC1Test.class,
//		EarClippingTriangulatorTest.class,
		EdgeDetectionTest.class,
		ExitTest.class,
		ExternalMusicTest.class,
		FilesTest.class,
		FilterPerformanceTest.class,
		FloatTextureTest.class,
		FogTest.class,
		FrameBufferCubemapTest.class,
		FrameBufferTest.class,
		FramebufferToTextureTest.class,
		FullscreenTest.class,
		Gdx2DTest.class,
		GestureDetectorTest.class,
		GL30Texture3DTest.class,
		GLES30Test.class,
		GL31IndirectDrawingIndexedTest.class,
		GL31IndirectDrawingNonIndexedTest.class,
		GL31FrameBufferMultisampleMRTTest.class,
		GL31FrameBufferMultisampleTest.class,
		GL31ProgramIntrospectionTest.class,
		GL32AdvancedBlendingTest.class,
		GL32DebugControlTest.class,
		GL32MultipleRenderTargetsBlendingTest.class,
		GL32OffsetElementsTest.class,
		GlTexImage2D.class,
		GLProfilerErrorTest.class,
		GroupCullingTest.class,
		GroupFadeTest.class,
		GroupTest.class,
		HeightMapTest.class,
		HelloTriangle.class,
		HexagonalTiledMapTest.class,
		I18NMessageTest.class,
		I18NSimpleMessageTest.class,
		ImageScaleTest.class,
		ImageTest.class,
		ImmediateModeRendererTest.class,
		IndexBufferObjectShaderTest.class,
		InputTest.class,
		InstancedRenderingTest.class,
		IntegerBitmapFontTest.class,
		InterpolationTest.class,
		IntersectorOverlapConvexPolygonsTest.class,
		InverseKinematicsTest.class,
		IsometricTileTest.class,
		KinematicBodyTest.class,
		KTXTest.class,
		LabelScaleTest.class,
		LabelTest.class,
		LifeCycleTest.class,
		LightsTest.class,
		MaterialTest.class,
		MaterialEmissiveTest.class,
		MatrixJNITest.class,
		MeshBuilderTest.class,
		MeshShaderTest.class,
		MeshWithCustomAttributesTest.class,
		MipMapTest.class,
		ModelTest.class,
		ModelCacheTest.class,
		ModelInstancedRenderingTest.class,
		MoveSpriteExample.class,
		MultipleRenderTargetTest.class,
		MultitouchTest.class,
		MusicTest.class,
		NetAPITest.class,
		NinePatchTest.class,
		NoncontinuousRenderingTest.class,
		NonPowerOfTwoTest.class,
		OctreeTest.class,
		OnscreenKeyboardTest.class,
		NativeInputTest.class,
		OrientedBoundingBoxTest.class,
		PathTest.class,
		ParallaxTest.class,
		ParticleControllerInfluencerSingleTest.class,
		ParticleControllerTest.class,
		ParticleEmitterTest.class,
		ParticleEmittersTest.class,
		ParticleEmitterChangeSpriteTest.class,
		PixelBufferObjectTest.class,
		PixelsPerInchTest.class,
		PixmapBlendingTest.class,
		PixmapPackerTest.class,
		PixmapPackerIOTest.class,
		PixmapTest.class,
		PolarAccelerationTest.class,
		PolygonRegionTest.class,
		PolygonSpriteTest.class,
		PreferencesTest.class,
		ProjectTest.class,
		ProjectiveTextureTest.class,
		ReflectionTest.class,
		ReflectionCorrectnessTest.class,
		RotationTest.class,
		RunnablePostTest.class,
		Scene2dTest.class,
		ScrollPane2Test.class,
		ScrollPaneScrollBarsTest.class,
		ScrollPaneTest.class,
		ScrollPaneTextAreaTest.class,
		ScrollPaneWithDynamicScrolling.class,
		SelectTest.class,
		SensorTest.class,
		ShaderCollectionTest.class,
		ShaderMultitextureTest.class,
		ShaderTest.class,
		ShadowMappingTest.class,
		ShapeRendererTest.class,
		ShapeRendererAlphaTest.class,
		SimpleAnimationTest.class,
		SimpleDecalTest.class,
		SimpleStageCullingTest.class,
		SimpleVertexShader.class,
		SkeletonTest.class,
		SoftKeyboardTest.class,
		SortedSpriteTest.class,
		SoundTest.class,
		SpriteBatchRotationTest.class,
		SpriteBatchPerformanceTest.class,
		SpriteBatchShaderTest.class,
		SpriteBatchTest.class,
		SpriteCacheOffsetTest.class,
		SpriteCacheTest.class,
		StageDebugTest.class,
		StagePerformanceTest.class,
		StageTest.class,
		SuperKoalio.class,
		SystemCursorTest.class,
		TableLayoutTest.class,
		TableTest.class,
		TangentialAccelerationTest.class,
		TextAreaTest.class,
		TextAreaTest2.class,		
		TextAreaTest3.class,
		TextButtonTest.class,
		TextInputDialogTest.class,
		TextureAtlasTest.class,
		TextureArrayTest.class,
		TextureDataTest.class,
		TextureDownloadTest.class,
		TextureFormatTest.class,
		TextureRegion3DTest.class,
		TideMapAssetManagerTest.class,
		TideMapDirectLoaderTest.class,
		TiledDrawableTest.class,
		TileTest.class,
		TiledMapAnimationLoadingTest.class,
		TiledMapAssetManagerTest.class,
		TiledMapGroupLayerTest.class,
		TiledMapGroupLayerWithImagelayerTest.class,
		TiledMapAtlasAssetManagerTest.class,
		TiledMapDirectLoaderTest.class,
		TiledMapImageLayerRepeatTest.class,
		TiledMapModifiedExternalTilesetTest.class,
		TiledMapObjectLoadingTest.class,
		TiledMapObjectPropertyTest.class,
		TiledMapBench.class,
		TiledMapLayerOffsetTest.class,
		TimerTest.class,
		TimeUtilsTest.class,
		TouchpadTest.class,
		TreeTest.class,
		UISimpleTest.class,
		UITest.class,
		UniformBufferObjectsTest.class,
		UtfFontTest.class,
		VBOWithVAOPerformanceTest.class,
		Vector2dTest.class,
		VertexArrayTest.class,
		VertexBufferObjectShaderTest.class,
		VibratorTest.class,
		ViewportTest1.class,
		ViewportTest2.class,
		ViewportTest3.class,
		YDownTest.class,
		FreeTypeFontLoaderTest.class,
		FreeTypeDisposeTest.class,
		FreeTypeMetricsTest.class,
		FreeTypeIncrementalTest.class,
		FreeTypePackTest.class,
		FreeTypeAtlasTest.class,
		FreeTypeTest.class,
		InternationalFontsTest.class,
		PngTest.class,
		JsonTest.class,
		QuadTreeFloatTest.class,
		QuadTreeFloatNearestTest.class
		// @on

	// SoundTouchTest.class, Mpg123Test.class, WavTest.class, FreeTypeTest.class,
	// VorbisTest.class
	));

	static final ObjectMap<String, String> obfuscatedToOriginal = new ObjectMap();
	static final ObjectMap<String, String> originalToObfuscated = new ObjectMap();
	static {
		InputStream mappingInput = GdxTests.class.getResourceAsStream("/mapping.txt");
		if (mappingInput != null) {
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new InputStreamReader(mappingInput), 512);
				while (true) {
					String line = reader.readLine();
					if (line == null) break;
					if (line.startsWith("    ")) continue;
					String[] split = line.replace(":", "").split(" -> ");
					String original = split[0];
					if (original.indexOf('.') != -1) original = original.substring(original.lastIndexOf('.') + 1);
					originalToObfuscated.put(original, split[1]);
					obfuscatedToOriginal.put(split[1], original);
				}
				reader.close();
			} catch (Exception ex) {
				System.out.println("GdxTests: Error reading mapping file: mapping.txt");
				ex.printStackTrace();
			} finally {
				StreamUtils.closeQuietly(reader);
			}
		}
	}

	public static List<String> getNames () {
		List<String> names = new ArrayList<String>(tests.size());
		for (Class clazz : tests)
			names.add(obfuscatedToOriginal.get(clazz.getSimpleName(), clazz.getSimpleName()));
		Collections.sort(names);
		return names;
	}

	public static Class<? extends GdxTest> forName (String name) {
		name = originalToObfuscated.get(name, name);
		for (Class clazz : tests)
			if (clazz.getSimpleName().equals(name)) return clazz;
		return null;
	}

	public static GdxTest newTest (String testName) {
		testName = originalToObfuscated.get(testName, testName);
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
