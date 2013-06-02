package com.badlogic.gdx.graphics.g3d.shaders.subshaders;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.materials.Material.Attribute;
import com.badlogic.gdx.graphics.g3d.materials.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.materials.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

public class DiffuseColorTextureShader extends BaseSubShader {
	private boolean useVertexColor;
	private boolean useDiffuseColor;
	private boolean useDiffuseTexture;
	
	@Override
	public void init (Renderable renderable) {
		String value = "";
		
		// check if we have vertex color
		if(renderable.mesh.getVertexAttribute(Usage.Color) != null||
			renderable.mesh.getVertexAttribute(Usage.ColorPacked) != null) {
			vertexVars.addAll(new String[] {
				"attribute vec4 " + ShaderProgram.COLOR_ATTRIBUTE + ";",
				"varying vec4 v_diffuseColor;"
			});
			vertexCode.add("v_diffuseColor = " + ShaderProgram.COLOR_ATTRIBUTE + ";");
			fragmentVars.add("varying LOWP vec4 v_diffuseColor;");
			useVertexColor = true;
			value = "v_diffuseColor";
		}
		
		// check if we have a diffuse color
		if(renderable.material.has(ColorAttribute.Diffuse)) {
			fragmentVars.addAll(new String[] {
				"uniform LOWP vec4 u_diffuseColor;"
			});
			useDiffuseColor = true;
			value += useVertexColor? " * ": "";
			value += "u_diffuseColor";
		}
		
		// check if we have a diffuse texture
		if(renderable.material.has(TextureAttribute.Diffuse)) {
			vertexVars.addAll(new String[] {
				"attribute vec2 a_texCoord0;",
				"varying vec2 v_texCoords0;"
			});
			vertexCode.addAll(new String[] {
				"v_texCoords0 = a_texCoord0;"
			});
			fragmentVars.addAll(new String[] {
				"uniform sampler2D u_diffuseTexture;",
				"varying MED vec2 v_texCoords0;"
			});
			useDiffuseTexture= true;
			value += (useVertexColor || useDiffuseColor)? " * ": "";
			value += "texture2D(u_diffuseTexture, v_texCoords0)";
		}
		
		fragmentCode.add("color = color * " + value + ";");
	}

	@Override
	public void apply (ShaderProgram program, RenderContext context, Camera camera, Renderable renderable) {
		if(useDiffuseColor) {
			ColorAttribute attribute = (ColorAttribute)renderable.material.get(ColorAttribute.Diffuse);
			program.setUniformf("u_diffuseColor", attribute.color);
		}
		if(useDiffuseTexture) {
			TextureAttribute attribute = (TextureAttribute)renderable.material.get(TextureAttribute.Diffuse);
			int unit = context.textureBinder.bind(attribute.textureDescription);
			program.setUniformi("u_diffuseTexture", unit);
		}
	}
}
