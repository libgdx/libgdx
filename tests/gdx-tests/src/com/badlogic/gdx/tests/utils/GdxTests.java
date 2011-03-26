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

import com.badlogic.gdx.tests.ActionTest;
import com.badlogic.gdx.tests.AlphaTest;
import com.badlogic.gdx.tests.AnimationTest;
import com.badlogic.gdx.tests.AtlasIssueTest;
import com.badlogic.gdx.tests.AudioDeviceTest;
import com.badlogic.gdx.tests.AudioRecorderTest;
import com.badlogic.gdx.tests.BitmapFontAlignmentTest;
import com.badlogic.gdx.tests.BitmapFontFlipTest;
import com.badlogic.gdx.tests.BitmapFontTest;
import com.badlogic.gdx.tests.BobTest;
import com.badlogic.gdx.tests.Box2DTest;
import com.badlogic.gdx.tests.Box2DTestCollection;
import com.badlogic.gdx.tests.CompassTest;
import com.badlogic.gdx.tests.ComplexActionTest;
import com.badlogic.gdx.tests.CullTest;
import com.badlogic.gdx.tests.DeltaTimeTest;
import com.badlogic.gdx.tests.FilesTest;
import com.badlogic.gdx.tests.FilterPerformanceTest;
import com.badlogic.gdx.tests.FloatTest;
import com.badlogic.gdx.tests.FrameBufferTest;
import com.badlogic.gdx.tests.FramebufferToTextureTest;
import com.badlogic.gdx.tests.Gdx2DTest;
import com.badlogic.gdx.tests.GroupFadeTest;
import com.badlogic.gdx.tests.ImmediateModeRendererAlphaTest;
import com.badlogic.gdx.tests.ImmediateModeRendererTest;
import com.badlogic.gdx.tests.IndexBufferObjectClassTest;
import com.badlogic.gdx.tests.IndexBufferObjectShaderTest;
import com.badlogic.gdx.tests.InputTest;
import com.badlogic.gdx.tests.IsoCamTest;
import com.badlogic.gdx.tests.IsometricTileTest;
import com.badlogic.gdx.tests.LifeCycleTest;
import com.badlogic.gdx.tests.MD5Test;
import com.badlogic.gdx.tests.ManagedTest;
import com.badlogic.gdx.tests.ManualBindTest;
import com.badlogic.gdx.tests.MeshMultitextureTest;
import com.badlogic.gdx.tests.MeshShaderTest;
import com.badlogic.gdx.tests.MeshTest;
import com.badlogic.gdx.tests.MultitouchTest;
import com.badlogic.gdx.tests.MusicTest;
import com.badlogic.gdx.tests.MyFirstTriangle;
import com.badlogic.gdx.tests.ObjTest;
import com.badlogic.gdx.tests.OrthoCamBorderTest;
import com.badlogic.gdx.tests.ParticleEmitterTest;
import com.badlogic.gdx.tests.PickingTest;
import com.badlogic.gdx.tests.PixelsPerInchTest;
import com.badlogic.gdx.tests.PixmapBlendingTest;
import com.badlogic.gdx.tests.PixmapTest;
import com.badlogic.gdx.tests.Pong;
import com.badlogic.gdx.tests.PreferencesTest;
import com.badlogic.gdx.tests.ProjectTest;
import com.badlogic.gdx.tests.RemoteTest;
import com.badlogic.gdx.tests.RotationTest;
import com.badlogic.gdx.tests.ShaderMultitextureTest;
import com.badlogic.gdx.tests.SimpleAnimationTest;
import com.badlogic.gdx.tests.SimpleTest;
import com.badlogic.gdx.tests.SoundTest;
import com.badlogic.gdx.tests.SplineTest;
import com.badlogic.gdx.tests.SpriteBatchRotationTest;
import com.badlogic.gdx.tests.SpriteBatchShaderTest;
import com.badlogic.gdx.tests.SpriteBatchTest;
import com.badlogic.gdx.tests.SpriteCacheOffsetTest;
import com.badlogic.gdx.tests.SpriteCacheTest;
import com.badlogic.gdx.tests.SpritePerformanceTest;
import com.badlogic.gdx.tests.SpritePerformanteTest2;
import com.badlogic.gdx.tests.StagePerformanceTest;
import com.badlogic.gdx.tests.StageTest;
import com.badlogic.gdx.tests.TerrainTest;
import com.badlogic.gdx.tests.TextInputDialogTest;
import com.badlogic.gdx.tests.TextureAtlasTest;
import com.badlogic.gdx.tests.TextureDataTest;
import com.badlogic.gdx.tests.TextureRenderTest;
import com.badlogic.gdx.tests.TileTest;
import com.badlogic.gdx.tests.TiledMapTest;
import com.badlogic.gdx.tests.UITest;
import com.badlogic.gdx.tests.VBOVATest;
import com.badlogic.gdx.tests.VertexArrayClassTest;
import com.badlogic.gdx.tests.VertexArrayTest;
import com.badlogic.gdx.tests.VertexBufferObjectClassTest;
import com.badlogic.gdx.tests.VertexBufferObjectShaderTest;
import com.badlogic.gdx.tests.VertexBufferObjectTest;
import com.badlogic.gdx.tests.VibratorTest;
import com.badlogic.gdx.tests.WaterRipples;
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
		PixmapTest.class,
		RotationTest.class,
		Gdx2DTest.class,
		AnimationTest.class,
		ActionTest.class,
		AlphaTest.class,	
		AtlasIssueTest.class,		
		FilterPerformanceTest.class,
		AudioDeviceTest.class,
		AudioRecorderTest.class,
		BitmapFontAlignmentTest.class,
		BitmapFontFlipTest.class,
		BitmapFontTest.class,
		BobTest.class,
		Box2DTest.class,
		Box2DTestCollection.class,
		CompassTest.class,
		ComplexActionTest.class,
		CullTest.class,
		DeltaTimeTest.class,
		FilesTest.class,
		//FillrateTest.class,
		FloatTest.class,
		FrameBufferTest.class,
		FramebufferToTextureTest.class,
		GroupFadeTest.class,
		ImmediateModeRendererTest.class,
		ImmediateModeRendererAlphaTest.class,
		IndexBufferObjectClassTest.class,
		IndexBufferObjectShaderTest.class,
		InputTest.class,
		IsoCamTest.class,
		IsometricTileTest.class,
		LifeCycleTest.class,
		ManagedTest.class,
		ManualBindTest.class,
		MD5Test.class,
		MeshMultitextureTest.class,
		MeshShaderTest.class,
		MeshTest.class,		
		//Mpg123Test.class,
		MultitouchTest.class,
		MusicTest.class,
		MyFirstTriangle.class,
		ObjTest.class,
		OrthoCamBorderTest.class,
		ParticleEmitterTest.class,
		PickingTest.class,
		PixelsPerInchTest.class,
		PixmapBlendingTest.class,
		PreferencesTest.class,
		Pong.class,
		ProjectTest.class,
		RemoteTest.class,
		ShaderMultitextureTest.class,
		SplineTest.class,		
		SimpleTest.class,
		SimpleAnimationTest.class,
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
