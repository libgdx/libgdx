/*
 * Copyright 2010 Dave Clayton (contact@redskyforge.com)
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
package com.badlogic.gdx.graphics;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.Utils;
import com.badlogic.gdx.graphics.texturedict.TextureRef;

/**
 * Holds material data. The data contains texture/shader information and material properties for lighting. Currently the material
 * also supports partial serialization of its data.<br>
 * 
 * @author Dave Clayton <contact@redskyforge.com>
 * 
 */
public class Material {
	public String Name;
	public ShaderProgram Shader;
	public TextureRef Texture = null;
	public String TexturePath = "";
	public Color Ambient = null;
	public Color Diffuse = null;
	public Color Specular = null;
	public Color Emissive = null;
	
	/**
	 * Constructs a new material.
	 * @param name
	 *          The material's name.
	 */
	public Material(String name)
	{
		Name = name;
	}
	
	/**
	 * Sends the material properties to the OpenGL state.
	 * @param face
	 *          Which faces this applies to (e.g. GL10.GL_FRONT).
	 */
	public void set(int face)
	{
		//TODO: should probably load shaderprogram here if we're using them
		GL10 gl = Gdx.graphics.getGL10();
		//TODO: caching of last material set using statics to see if we need to set material states again
		if(Ambient != null)
		{
			Utils.setLparam(Ambient.r, Ambient.g, Ambient.b, Ambient.a);
			gl.glMaterialfv(face, GL10.GL_AMBIENT, Utils.lparam, 0);
		}
		if(Diffuse != null)
		{
			Utils.setLparam(Diffuse.r, Diffuse.g, Diffuse.b, Diffuse.a);
			gl.glMaterialfv(face, GL10.GL_DIFFUSE, Utils.lparam, 0);
		}
	}
	
	/**
	 * Serialization. Experimental.
	 * @param i The DataInputStream to serialize from.
	 * @return whether serialization succeeded.
	 * @throws IOException
	 */
	public boolean read(DataInputStream i) throws IOException
	{
		Name = i.readUTF();
		TexturePath = i.readUTF();
		boolean hasAmbient = i.readBoolean();
		if(hasAmbient)
		{
			float r = i.readFloat();
			float g = i.readFloat();
			float b = i.readFloat();
			float a = i.readFloat();
			Ambient = new Color(r, g, b, a);
		}
		boolean hasDiffuse = i.readBoolean();
		if(hasDiffuse)
		{
			float r = i.readFloat();
			float g = i.readFloat();
			float b = i.readFloat();
			float a = i.readFloat();
			Diffuse = new Color(r, g, b, a);
		}
		return true;
	}
	
	/**
	 * Serialization. Experimental.
	 * @param o The DataOutputStream to serialize to.
	 * @return Whether serialization succeeded.
	 * @throws IOException
	 */
	public boolean write(DataOutputStream o) throws IOException
	{
		//TODO: serialize out shader
		o.writeUTF(Name);
		// process path
		String filename = Texture.Name.substring(Texture.Name.lastIndexOf("\\")+1);
		o.writeUTF(filename);
		o.writeBoolean(Ambient != null);
		if(Ambient != null)
		{
			o.writeFloat(Ambient.r);
			o.writeFloat(Ambient.g);
			o.writeFloat(Ambient.b);
			o.writeFloat(Ambient.a);
		}
		o.writeBoolean(Diffuse != null);
		if(Diffuse != null)
		{
			o.writeFloat(Diffuse.r);
			o.writeFloat(Diffuse.g);
			o.writeFloat(Diffuse.b);
			o.writeFloat(Diffuse.a);
		}
		return true;
	}
}
