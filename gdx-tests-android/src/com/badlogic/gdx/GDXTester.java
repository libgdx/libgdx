/*******************************************************************************
 * Copyright 2010 mzechner
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
package com.badlogic.gdx;

import com.badlogic.gdx.box2d.TestCollection;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class GDXTester extends ListActivity 
{
	String[] items = new String[]{ "Life Cycle Test", "Simple Test", "Vertex Array Test", "Vertex Buffer Object Test", "MeshRenderer Test", 
								   "Fixed Point MeshRenderer Test", "Managed Test", "Text Test", "Sound Test", "Input Test", "Obj Test",
								   "Fixed Point Test", "Float Test", "Lag Test", "Pong", "Collision Test", "Audio Device Test", 
								   "Mpg123 Test", "Vorbis Test", "Performance Test", "Mesh Shader Test", "SpriteBatch Test",
								   "SpriteBatch Shader Test", "FrameBuffer Test", "SpriteBatch Rotation Test", "Box2D Test",
								   "Audio Recorder Test", "Box2D Test Collection", "Resources Test", "MD5 Test", "Micro Benchmarks"};
	
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState); 
		setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items));        
	}

	protected void onListItemClick(ListView l, View v, int position, long id)
	{
		super.onListItemClick(l, v, position, id);
	
		Object o = this.getListAdapter().getItem(position);
		String keyword = o.toString();

		Intent intent = null;
		if( keyword.equals( items[0] ) )
			intent = new Intent( this, LifeCycleTest.class );
		if( keyword.equals( items[1] ) )
			intent = new Intent( this, SimpleTest.class );
		if( keyword.equals( items[2] ) )
			intent = new Intent( this, VertexArrayTest.class );
		if( keyword.equals( items[3] ) )
			intent = new Intent( this, VertexBufferObjectTest.class );
		if( keyword.equals( items[4] ) )
			intent = new Intent( this, MeshRendererTest.class );
		if( keyword.equals( items[5] ) )
			intent = new Intent( this, FixedPointMeshRendererTest.class );
		if( keyword.equals( items[6] ) )
			intent = new Intent( this, ManagedTest.class );
		if( keyword.equals( items[7] ) )
			intent = new Intent( this, TextTest.class );
		if( keyword.equals( items[8] ) )
			intent = new Intent( this, SoundTest.class );		
		if( keyword.equals( items[9] ) )
			intent = new Intent( this, InputTest.class );
		if( keyword.equals( items[10] ) )
			intent = new Intent( this, ObjTest.class );
		if( keyword.equals( items[11] ) )
			intent = new Intent( this, FixedPointTest.class );
		if( keyword.equals( items[12] ) )
			intent = new Intent( this, FloatTest.class );
		if( keyword.equals( items[13] ) )
			intent = new Intent( this, LagTest.class );
		if( keyword.equals( items[14] ) )
			intent = new Intent( this, Pong.class );
		if( keyword.equals( items[15] ) )
			intent = new Intent( this, CollisionTest.class );
		if( keyword.equals( items[16] ) )
			intent = new Intent( this, AudioDeviceTest.class );
		if( keyword.equals( items[17] ) )
			intent = new Intent( this, Mpg123Test.class );
		if( keyword.equals( items[18] ) )
			intent = new Intent( this, VorbisTest.class );
		if( keyword.equals( items[19] ) )
			intent = new Intent( this, PerformanceTest.class );
		if( keyword.equals( items[20] ) )
			intent = new Intent( this, MeshShaderTest.class );
		if( keyword.equals( items[21] ) )
			intent = new Intent( this, SpriteBatchTest.class );
		if( keyword.equals( items[22] ) )
			intent = new Intent( this, SpriteBatchShaderTest.class );
		if( keyword.equals( items[23] ) )
			intent = new Intent( this, FrameBufferTest.class );
		if( keyword.equals( items[24] ) )
			intent = new Intent( this, SpriteBatchRotationTest.class );
		if( keyword.equals( items[25] ) )
			intent = new Intent( this, Box2DTest.class );
		if( keyword.equals( items[26] ) )
			intent = new Intent( this, AudioRecorderTest.class );
		if( keyword.equals( items[27] ) )
			intent = new Intent( this, TestCollection.class );
		if( keyword.equals( items[28] ) )
			intent = new Intent( this, ResourcesTest.class );
		if( keyword.equals( items[29] ) )
			intent = new Intent( this, MD5Test.class );
		if( keyword.equals( items[30] ) )
			intent = new Intent( this, MicroBenchmarks.class );
			
		startActivity( intent );
	}

}
