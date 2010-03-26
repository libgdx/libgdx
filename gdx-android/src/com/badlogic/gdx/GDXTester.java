/*
 *  This file is part of Libgdx by Mario Zechner (badlogicgames@gmail.com)
 *
 *  Libgdx is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Libgdx is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with libgdx.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.badlogic.gdx;

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
								   "Fixed Point Test", "Float Test", "Lag Test", "Pong", "Collision Test" };
	
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
			
		startActivity( intent );
	}

}