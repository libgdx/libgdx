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
package com.badlogic.gdx.backends.jogl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineEvent.Type;

import com.badlogic.gdx.audio.Sound;

/**
 * An implementation of {@link Sound} based on java sound.
 * @author badlogicgames@gmail.com
 *
 */
final class JoglSound implements Sound
{
	byte[] bytes;
	
	JoglSound( InputStream in ) throws IOException
	{
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		int b = -1;
		while( (b = in.read()) != -1 )
			bytes.write( b );
		this.bytes = bytes.toByteArray();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void play() {
		try
		{
			final Clip clip = AudioSystem.getClip();
			clip.open( AudioSystem.getAudioInputStream( new ByteArrayInputStream( bytes ) ) );
			clip.addLineListener( new LineListener() {
				
				@Override
				public void update(LineEvent arg0) {
					if( arg0.getType() == Type.STOP )
						clip.close();
				}
			});
			
			clip.start();
		}
		catch( Exception ex )
		{
			
		}	
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}
	
}

