/**
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
 *  along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.badlogic.gdx.samples;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.RenderListener;
import com.badlogic.gdx.Mesh;
import com.badlogic.gdx.Mesh.PrimitiveType;
import com.badlogic.gdx.backends.jogl.JoglApplication;

public class TriangleTest implements RenderListener
{
	Mesh mesh;	

	@Override
	public void setup(Application application) 
	{
		mesh = application.newMesh( 3, false, false, false, false, 0, true );
		mesh.vertex( -0.5f, -0.5f, 0 );
		mesh.vertex( 0.5f, -0.5f, 0 );
		mesh.vertex( 0, 0.5f, 0 );		
	}
	
	@Override
	public void render(Application application) 
	{
		mesh.render( PrimitiveType.Triangles );
	}
	
	@Override
	public void dispose(Application application) 
	{	
		
	}

	public static void main( String[] argv )
	{
		JoglApplication app = new JoglApplication( "Triangle", 480, 320 );
		app.addRenderListener( new TriangleTest() );
	}
}
