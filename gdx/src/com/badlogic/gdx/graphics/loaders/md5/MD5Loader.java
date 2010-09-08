package com.badlogic.gdx.graphics.loaders.md5;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MD5Loader 
{
	public static MD5Model loadModel( InputStream in )
	{
		BufferedReader reader = new BufferedReader( new InputStreamReader( in ) );
		MD5Model model = new MD5Model( );
		
		try
		{			
			String line;
			while( (line = reader.readLine() ) != null )
			{
				//
				// check version string
				//
				if( line.startsWith( "MD5Version" ) )
				{
					int version = Integer.parseInt(line.split( " " )[1]);
					if( version != 10 )
						throw new IllegalArgumentException( "Not a valid MD5 file, go version " + version + ", need 10" );
				}							
			}
			
			return null;
		}
		catch( Exception ex )
		{
			return null;
		}			
	}
}
