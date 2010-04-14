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
package com.badlogic.gdx.tests;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.RenderListener;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Font;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.SpriteBatch;
import com.badlogic.gdx.graphics.Font.FontStyle;

public class TextTest implements RenderListener
{
	SpriteBatch spriteBatch;
	Font font;				


	@Override
	public void surfaceCreated(Application app) 
	{
		if( font == null )
		{		
			spriteBatch = new SpriteBatch(app.getGraphics());
			font = app.getGraphics().newFont( "Arial", 32, FontStyle.Italic, true );
		}
	}
	
	@Override
	public void render(Application app) 
	{
		app.getGraphics().getGL10().glClearColor( 0.7f, 0.7f, 0.7f, 1 );
		app.getGraphics().getGL10().glClear(GL10.GL_COLOR_BUFFER_BIT );						
		
		spriteBatch.begin();
		spriteBatch.drawText( font, "this is a test", 100, 100, Color.RED );
		spriteBatch.end();
	}

	@Override
	public void dispose(Application app) 
	{	
		
	}
	
	@Override
	public void surfaceChanged(Application app, int width, int height) 
	{	
		
	}
}
