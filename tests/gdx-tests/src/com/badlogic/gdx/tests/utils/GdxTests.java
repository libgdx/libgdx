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
import java.util.List;

import com.badlogic.gdx.tests.*;
import com.badlogic.gdx.tests.gles2.HelloTriangle;
import com.badlogic.gdx.tests.gles2.SimpleVertexShader;

/**
 * List of GdxTest classes. To be used by the test launchers.
 * If you write your own test, add it in here!
 * 
 * @author badlogicgames@gmail.com
 *
 */
public class GdxTests 
{
	public static final Class[] tests = {							
		AnimationTest.class,
		AccelerometerTest.class,
		ActionTest.class,
		ActionSequenceTest.class,
		AlphaTest.class,	
		AtlasIssueTest.class,		
		FilterPerformanceTest.class,
		AudioDeviceTest.class,
		AudioRecorderTest.class,
		BitmapFontAlignmentTest.class,
		BitmapFontFlipTest.class,
		BitmapFontTest.class,
		BobTest.class,
		Box2DInitialOverlapTest.class,
		Box2DTest.class,
		Box2DTestCollection.class,
		BufferUtilsTest.class,
		CompassTest.class,
		ComplexActionTest.class,
		CullTest.class,
		DeltaTimeTest.class,
		EdgeDetectionTest.class,
		ExitTest.class,
		FilesTest.class,
		//FillrateTest.class,
		FloatTest.class,
		FrameBufferTest.class,
		FramebufferToTextureTest.class,
		FullscreenTest.class,
		FastTextReadingTest.class,
		Gdx2DTest.class,
		GroupFadeTest.class,
		ImmediateModeRendererTest.class,
		ImmediateModeRendererAlphaTest.class,
		IndexBufferObjectClassTest.class,
		IndexBufferObjectShaderTest.class,
		InputTest.class,
		IntegerBitmapFontTest.class,
		IsoCamTest.class,
		IsometricTileTest.class,
		KinematicBodyTest.class,
		LifeCycleTest.class,
		LineDrawingTest.class,
		ManagedTest.class,
		ManualBindTest.class,
		MatrixJNITest.class,
		MD5Test.class,
		MeshMultitextureTest.class,
		MeshShaderTest.class,
		MeshTest.class,		
		//Mpg123Test.class,
		MultitouchTest.class,		
		MusicTest.class,
		MultiTouchActorTest.class,
		MyFirstTriangle.class,
		ObjTest.class,
		OldUITest.class,
		OnscreenKeyboardTest.class,
		OrthoCamBorderTest.class,
		ParticleEmitterTest.class,
		PickingTest.class,
		PixelsPerInchTest.class,
		PixmapBlendingTest.class,
		PixmapTest.class,
		PreferencesTest.class,
		ProjectiveTextureTest.class,
		Pong.class,
		ProjectTest.class,
		RemoteTest.class,
		RotationTest.class,
		ShaderMultitextureTest.class,
		ShadowMappingTest.class,
		SplineTest.class,		
		SimpleTest.class,
		SimpleAnimationTest.class,
		SimpleStageCullingTest.class,
		SoundTest.class,
		SpriteCacheTest.class,
		SpriteCacheOffsetTest.class,
		SpriteBatchRotationTest.class,
		SpriteBatchShaderTest.class,
		SpriteBatchTest.class,		
		SpritePerformanceTest.class,
		SpritePerformanteTest2.class,
		StagePerformanceTest.class,
		StageTest.class,
		TerrainTest.class,		
		TextureDataTest.class,
		TextureDownloadTest.class,
		TextureFormatTest.class,
		TextureAtlasTest.class,
		TextInputDialogTest.class,
		TextureRenderTest.class,
		TiledMapTest.class,
		TileTest.class,		
		UITest.class,
		VBOVATest.class,
		VertexArrayTest.class,		
		VertexBufferObjectTest.class,
		VertexArrayClassTest.class,
		VertexBufferObjectClassTest.class,
		VertexBufferObjectShaderTest.class,
		VibratorTest.class,
		//VorbisTest.class,
		WaterRipples.class,
		HelloTriangle.class,		
		SimpleVertexShader.class
	};
	
	public static String[] getNames () {
		List<String> names = new ArrayList<String>( );
		for( Class clazz: tests )
			names.add(clazz.getSimpleName());
		return names.toArray(new String[names.size()]);
	}

	public static GdxTest newTest (String testName)	{
		try {
			Class clazz = Class.forName("com.badlogic.gdx.tests." + testName);
			return (GdxTest)clazz.newInstance();
		}
		catch( Exception ex ) {
			try {
				Class clazz = Class.forName("com.badlogic.gdx.tests.gles2." + testName);
				return (GdxTest)clazz.newInstance();
			} catch(Exception e) {
				return null;
			}
		}		
	}
}
