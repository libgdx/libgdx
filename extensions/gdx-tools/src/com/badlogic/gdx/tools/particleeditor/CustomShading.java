/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
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

package com.badlogic.gdx.tools.particleeditor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Array;

public class CustomShading {
	private ShaderProgram shader;
	Array<String> extraTexturePaths = new Array<String>();
	Array<Texture> extraTextures = new Array<Texture>();
	String defaultVertexShaderCode;
	String defaultFragmentShaderCode;
	String vertexShaderCode;
	String fragmentShaderCode;
	FileHandle lastVertexShaderFile;
	FileHandle lastFragmentShaderFile;
	boolean hasShaderErrors;
	String shaderErrorMessage;
	boolean hasMissingSamplers;
	String missingSamplerMessage;

	public CustomShading () {
		shader = SpriteBatch.createDefaultShader();
		vertexShaderCode = defaultVertexShaderCode = shader.getVertexShaderSource();
		fragmentShaderCode = defaultFragmentShaderCode = shader.getFragmentShaderSource();
	}

	public void begin (SpriteBatch spriteBatch) {
		spriteBatch.setShader(shader);
		for (int i = 0; i < extraTextures.size; i++) {
			extraTextures.get(i).bind(i + 1);
		}
		Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
	}

	public void end (SpriteBatch spriteBatch) {
		spriteBatch.setShader(null);
		for (int i = 0; i < extraTextures.size; i++) {
			Gdx.gl.glActiveTexture(GL20.GL_TEXTURE1 + i);
			Gdx.gl.glBindTexture(extraTextures.get(i).glTarget, 0);
		}
		Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
	}

	public void setVertexShaderFile (String absolutePath) {
		if (absolutePath == null) {
			lastVertexShaderFile = null;
			vertexShaderCode = defaultVertexShaderCode;
		} else {
			lastVertexShaderFile = Gdx.files.absolute(absolutePath);
			vertexShaderCode = lastVertexShaderFile.readString();
		}
		updateShader();
	}

	public void setFragmentShaderFile (String absolutePath) {
		if (absolutePath == null) {
			lastFragmentShaderFile = null;
			fragmentShaderCode = defaultFragmentShaderCode;
		} else {
			lastFragmentShaderFile = Gdx.files.absolute(absolutePath);
			fragmentShaderCode = lastFragmentShaderFile.readString();
		}
		updateShader();
	}

	public void reloadVertexShader () {
		if (lastVertexShaderFile != null) {
			vertexShaderCode = lastVertexShaderFile.readString();
		}
		updateShader();
	}

	public void reloadFragmentShader () {
		if (lastFragmentShaderFile != null) {
			fragmentShaderCode = lastFragmentShaderFile.readString();
		}
		updateShader();
	}

	private void updateShader () {
		ShaderProgram shader = new ShaderProgram(vertexShaderCode, fragmentShaderCode);
		if (shader.isCompiled()) {
			hasShaderErrors = false;
			shaderErrorMessage = null;
			if (this.shader != null) {
				this.shader.dispose();
			}
			this.shader = shader;
			updateSamplers();
		} else {
			hasShaderErrors = true;
			shaderErrorMessage = shader.getLog();
			shader.dispose();
		}
	}

	public void addTexture (String absolutePath) {
		extraTexturePaths.add(absolutePath);
		extraTextures.add(new Texture(Gdx.files.absolute(absolutePath)));
		updateSamplers();
	}

	public void swapTexture (int indexA, int indexB) {
		extraTexturePaths.swap(indexA, indexB);
		extraTextures.swap(indexA, indexB);
		updateSamplers();
	}

	public void removeTexture (int index) {
		extraTexturePaths.removeIndex(index);
		extraTextures.removeIndex(index).dispose();
		updateSamplers();
	}

	public void reloadTexture (int index) {
		Texture previousTexture = extraTextures.get(index);
		String path = extraTexturePaths.get(index);
		Texture texture = new Texture(Gdx.files.absolute(path));
		previousTexture.dispose();
		extraTextures.set(index, texture);
	}

	private void updateSamplers () {
		hasMissingSamplers = false;
		missingSamplerMessage = "";

		shader.bind();
		for (int i = 0; i < extraTextures.size; i++) {
			int unit = i + 1;
			int location = shader.fetchUniformLocation("u_texture" + unit, false);
			if (location >= 0) {
				shader.setUniformi(location, unit);
			} else {
				hasMissingSamplers = true;
				missingSamplerMessage += "uniform sampler2D u_texture" + unit + " missing in shader program.\n";
			}
		}
	}
}
