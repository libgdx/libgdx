package com.badlogic.gdx.tests.utils;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.tests.AlphaTest;
import com.badlogic.gdx.tests.AudioDeviceTest;
import com.badlogic.gdx.tests.AudioRecorderTest;
import com.badlogic.gdx.tests.BitmapFontFlipTest;
import com.badlogic.gdx.tests.BitmapFontTest;
import com.badlogic.gdx.tests.Box2DTest;
import com.badlogic.gdx.tests.Box2DTestCollection;
import com.badlogic.gdx.tests.FillrateTest;
import com.badlogic.gdx.tests.FloatTest;
import com.badlogic.gdx.tests.FrameBufferTest;
import com.badlogic.gdx.tests.ImmediateModeRendererTest;
import com.badlogic.gdx.tests.IndexBufferObjectClassTest;
import com.badlogic.gdx.tests.IndexBufferObjectShaderTest;
import com.badlogic.gdx.tests.InputTest;
import com.badlogic.gdx.tests.LifeCycleTest;
import com.badlogic.gdx.tests.MD5Test;
import com.badlogic.gdx.tests.ManagedTest;
import com.badlogic.gdx.tests.ManualBindTest;
import com.badlogic.gdx.tests.MeshMultitextureTest;
import com.badlogic.gdx.tests.MeshShaderTest;
import com.badlogic.gdx.tests.MeshTest;
import com.badlogic.gdx.tests.Mpg123Test;
import com.badlogic.gdx.tests.MultitouchTest;
import com.badlogic.gdx.tests.MyFirstTriangle;
import com.badlogic.gdx.tests.ObjTest;
import com.badlogic.gdx.tests.ParticleEmitterTest;
import com.badlogic.gdx.tests.PixelsPerInchTest;
import com.badlogic.gdx.tests.Pong;
import com.badlogic.gdx.tests.SimpleTest;
import com.badlogic.gdx.tests.SoundTest;
import com.badlogic.gdx.tests.SpriteBatchRotationTest;
import com.badlogic.gdx.tests.SpriteBatchShaderTest;
import com.badlogic.gdx.tests.SpriteBatchTest;
import com.badlogic.gdx.tests.SpriteCacheTest;
import com.badlogic.gdx.tests.StageTest;
import com.badlogic.gdx.tests.TerrainTest;
import com.badlogic.gdx.tests.TextureRenderTest;
import com.badlogic.gdx.tests.UITest;
import com.badlogic.gdx.tests.VBOVATest;
import com.badlogic.gdx.tests.VertexArrayClassTest;
import com.badlogic.gdx.tests.VertexArrayTest;
import com.badlogic.gdx.tests.VertexBufferObjectClassTest;
import com.badlogic.gdx.tests.VertexBufferObjectShaderTest;
import com.badlogic.gdx.tests.VertexBufferObjectTest;
import com.badlogic.gdx.tests.VorbisTest;
import com.badlogic.gdx.tests.WaterRipples;

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
		AlphaTest.class,
		AudioDeviceTest.class,
		AudioRecorderTest.class,
		BitmapFontFlipTest.class,
		BitmapFontTest.class,
		Box2DTest.class,
		Box2DTestCollection.class,
		FillrateTest.class,
		FloatTest.class,
		FrameBufferTest.class,
		ImmediateModeRendererTest.class,
		IndexBufferObjectClassTest.class,
		IndexBufferObjectShaderTest.class,
		InputTest.class,
		LifeCycleTest.class,
		ManagedTest.class,
		ManualBindTest.class,
		MD5Test.class,
		MeshMultitextureTest.class,
		MeshShaderTest.class,
		MeshTest.class,
		Mpg123Test.class,
		MultitouchTest.class,
		MyFirstTriangle.class,
		ObjTest.class,
		ParticleEmitterTest.class,
		PixelsPerInchTest.class,
		Pong.class,
		SimpleTest.class,
		SoundTest.class,
		SpriteCacheTest.class,
		SpriteBatchRotationTest.class,
		SpriteBatchShaderTest.class,
		SpriteBatchTest.class,
		StageTest.class,
		TerrainTest.class,		
		TextureRenderTest.class,
		UITest.class,
		VBOVATest.class,
		VertexArrayTest.class,		
		VertexBufferObjectTest.class,
		VertexArrayClassTest.class,
		VertexBufferObjectClassTest.class,
		VertexBufferObjectShaderTest.class,
		VorbisTest.class,
		WaterRipples.class
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
			return null;
		}		
	}
}
