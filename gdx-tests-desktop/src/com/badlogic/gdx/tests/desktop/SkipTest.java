package com.badlogic.gdx.tests.desktop;

import java.nio.ShortBuffer;

import com.badlogic.gdx.audio.analysis.AudioTools;
import com.badlogic.gdx.audio.io.Mpg123Decoder;

public class SkipTest 
{
	public static void main( String[] argv )
	{		
		Mpg123Decoder decoder = new Mpg123Decoder( "data/threeofaperfectpair.mp3" );
		ShortBuffer samples = AudioTools.allocateShortBuffer( 512, decoder.getNumChannels() );
		int skipSamples = decoder.getRate() / 25;
		int i = 0;			
		
		while( decoder.readSamples( samples ) > 0 )
		{
			i++;
		}		
		decoder.dispose();
		System.out.println( i + " fetches");
		
		i = 0;		
		decoder = new Mpg123Decoder( "data/threeofaperfectpair.mp3" );
		while( decoder.readSamples( samples ) > 0 )
		{
			decoder.skipSamples( (skipSamples - 512) * decoder.getNumChannels() );
			
			i++;
		}
		decoder.dispose();						
	}
}
