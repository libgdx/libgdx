package com.badlogic.gdx.graphics;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A simple parser class that takes a gl header file and converts it into
 * a java interface file. This is a complete hack and was only tested
 * with the headers from the khronos site. Manual post-processing is required.
 * 
 * @author badlogicgames@gmail.com
 *
 */
public class GLGenerator 
{
	class Define
	{
		String name;
		String value;
		
		public Define( String name, String value )
		{
			this.name = name;
			this.value = value;
		}
		
		public String toString( )
		{
			StringBuffer buffer = new StringBuffer( );
			buffer.append( "public static final int " );
			buffer.append( name );
			buffer.append( " = " );
			buffer.append( value );
			buffer.append( ";" );
			
			return buffer.toString();
		}
	}
	
	class TypeDef
	{
		String name;
		String type;
		
		public TypeDef( String name, String type )
		{
			this.name = name;
			this.type = type;
		}
	}
	
	class Parameter
	{
		String type;
		String name;
		
		public Parameter( String type, String name )
		{
			this.type = type;
			this.name = name;
		}
	}
	
	class Function
	{
		String returnType;
		String name;
		List<Parameter> parameters = new ArrayList<Parameter>( );
		
		public Function( String returnType, String name )
		{
			this.returnType = returnType;
			this.name = name;
		}
		
		public String toString( )
		{
			StringBuffer buffer = new StringBuffer( );
			buffer.append( "public " );
			buffer.append( returnType );
			buffer.append( " " );
			buffer.append( name );
			buffer.append( " ( " );
			
			for( int i = 0; i < parameters.size(); i++ )
			{
				Parameter parameter = parameters.get(i);
				buffer.append( parameter.type );
				buffer.append( " " );
				buffer.append( parameter.name );
				if( i < parameters.size() - 1 )
					buffer.append( ", " );
			}
			buffer.append( " );" );
			return buffer.toString();
		}
	}
		
	private BufferedReader inputFile;	
	private List<Define> defines = new ArrayList<Define>( );
	private Map<String, String> khronosDefines = new HashMap<String, String>( );
	private List<TypeDef> typedefs = new ArrayList<TypeDef>( );
	private List<Function> functions = new ArrayList<Function>( );
	private Set<String> functionsSet = new HashSet<String>( );
	private Map<String, TypeDef> typedefsMap = new HashMap<String, TypeDef>();
	private Map<String, Define> definesMap = new HashMap<String, Define>( );
	
	public GLGenerator( InputStream input, String className, String outputFile ) throws IOException
	{
		khronosDefines.put( "khronos_int8_t", "char" );
		khronosDefines.put( "khronos_uint8_t", "char" );
		khronosDefines.put( "khronos_int16_t", "short" );
		khronosDefines.put( "khronos_int32_t", "int" );
		khronosDefines.put( "khronos_float_t", "float" );
		
		inputFile = new BufferedReader( new InputStreamReader( input ) );
		
		String line = inputFile.readLine();
		while( line != null )
		{
			String[] tokens = line.split( " " );
			
			if( tokens[0].equals( "#define" ) && tokens.length >= 3 )
				addDefine( tokens );
			if( tokens[0].equals( "typedef" ) )
				addTypeDef( tokens );
			if( tokens[0].equals( "GLAPI" ) || tokens[0].equals( "GL_API" ) )
				addFunction( tokens );
			
			
			line = inputFile.readLine();
		}
		
		
		for( Define define: defines )
			System.out.println( define );
		
		for( Function function: functions )
			System.out.println( function.toString() );
		
		BufferedWriter out = new BufferedWriter( new OutputStreamWriter( new FileOutputStream( outputFile ) ) );
		out.write( "package com.badlogic.gdx.graphics;\nimport java.nio.Buffer;\n\n" );
		out.write( "public interface " + className + "\n" );
		out.write( "{\n" );
		for( Define define: defines )
			out.write( "\t" + define + "\n" );
		out.write( "\n" );
		for( Function func: functions )
			out.write( "\t" + func + "\n\n" );
		out.write( "}" );
		out.close();
	}	
	
	public GLGenerator( InputStream input, InputStream prevInput, String className, String outputFile ) throws IOException
	{
		this( prevInput, className, outputFile );
		
		inputFile = new BufferedReader( new InputStreamReader( input ) );
		
		String line = inputFile.readLine();
		while( line != null )
		{
			String[] tokens = line.split( " " );
			
			if( tokens[0].equals( "#define" ) && tokens.length >= 3 )
				addDefine( tokens );
			if( tokens[0].equals( "typedef" ) )
				addTypeDef( tokens );
			if( tokens[0].equals( "GLAPI" ) || tokens[0].equals( "GL_API" ) )
				addFunction( tokens );
			
			
			line = inputFile.readLine();
		}
		
		
		for( Define define: defines )
			System.out.println( define );
		
		for( Function function: functions )
			System.out.println( function.toString() );
		
		BufferedWriter out = new BufferedWriter( new OutputStreamWriter( new FileOutputStream( outputFile ) ) );
		out.write( "package com.badlogic.gdx.graphics;\nimport java.nio.Buffer;\n\n" );
		out.write( "public interface " + className + "\n" );
		out.write( "{\n" );
		for( Define define: defines )
			out.write( "\t" + define + "\n" );
		out.write( "\n" );
		for( Function func: functions )
			out.write( "\t" + func + "\n\n" );
		out.write( "}" );
		out.close();
	}
	
	private void addDefine( String[] tokens )
	{		
		if( definesMap.containsKey( tokens[1] ) )
			System.out.println( "warning: " + tokens[1] + " is already in map" );
		
		if( !tokens[1].startsWith( "GL_") )
			return;
		
		if( definesMap.containsKey( tokens[1] ) )
		{
			System.out.println( "warning: '" + tokens[1] + "' already defined in previous file" );
			Iterator<Define> defineIter = defines.iterator();
			while( defineIter.hasNext() )
				if( defineIter.next().name.equals( tokens[1] ) )
					defineIter.remove();
			return;
		}
		
		Define define = new Define( tokens[1], tokens[tokens.length-1] );
		defines.add( define );
		definesMap.put( tokens[1], define);
	}
	
	private void addTypeDef( String[] tokens )
	{
		String name = tokens[tokens.length-1].replace( ";", "" );
		String type = tokens[tokens.length-2];
		if( type.equals( "" ) )
		{
			type = tokens[1];
			if( type.equals( "unsigned" ) )
				type = tokens[2];
		}
		if( khronosDefines.containsKey( type ) )
			type = khronosDefines.get( type );
		if( typedefsMap.containsKey( name) )
		{
			System.out.println("warning: typedef '" + name + "' is already in map" );
		}
		
		TypeDef typedef = new TypeDef( name, type );
		typedefs.add( typedef );
		typedefsMap.put( name, typedef );
	}
	
	private void addFunction( String[] tokens )
	{
		String returnType = tokens[1];
		if( returnType.equals( "const" ) )
			returnType = tokens[2];
		
		if( returnType.equals( "*char" ) )
			returnType = "String";
		
		if( typedefsMap.containsKey( returnType ) )
			returnType = typedefsMap.get( returnType ).type;
		
		int index = 0;
		for( int i = 0; i < tokens.length; i++, index++ )
			if( tokens[i].startsWith( "gl" ) )
				break;
		
		String name = tokens[index++];		
		
		if( functionsSet.contains( name ) )
		{
			System.out.println( "omitting '" + name + "', already defined in previous file" );
			Iterator<Function> funcIter = functions.iterator();
			while( funcIter.hasNext() )
				if( funcIter.next().name.equals(name) )
					funcIter.remove();
			
			return;
		}
		
		for( int i = index; i < tokens.length; i++ )		
			tokens[i] = tokens[i].replace( "(", "" ).replace( ",", "" ).replace( ");", "" );
		
		Function func = new Function( returnType, name );
		functions.add( func );
		functionsSet.add( func.name );
		
		for( int i = index; i < tokens.length;)
		{
			if( tokens[i].equals( "const" ) )
			{ 
				i++;
				continue;
			}
			
			String type = tokens[i++];
			if( i == tokens.length )
				break;
			String paramName = tokens[i++];
			if( paramName.startsWith( "*" ) )
			{
				String pointerType = "Buffer";								
				
				if( type.equals( "GLvoid") )
					pointerType = "Buffer";
				if( type.equals( "GLuint" ) || type.equals( "GLint"))
					pointerType = "IntBuffer";
				if( type.equals( "GLfloat" ) )
					pointerType = "FloatBuffer";
				
				Parameter param = new Parameter( pointerType, paramName.replace( "*", "" ) );
				func.parameters.add( param );
			}
			else
			if( type.startsWith( "GLboolean" ))
			{
				Parameter param = new Parameter( "boolean", paramName );
				func.parameters.add( param );
			}
			else
			{
				if( typedefsMap.containsKey( type ) )
					type = typedefsMap.get( type ).type;
				Parameter param = new Parameter( type, paramName);
				func.parameters.add( param );
			}
		}
	}
	
	public static void main( String[] argv ) throws FileNotFoundException, IOException
	{
		new GLGenerator( new FileInputStream( "gl11.h" ), new FileInputStream( "gl10.h" ), "GL11", "GL11.java" );
	}
}
