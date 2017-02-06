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

package com.badlogic.gdx.assets.loaders;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;

/** {@link AssetLoader} for {@link ShaderProgram} instances loaded from text files. If the file suffix is ".vert", it is assumed
 * to be a vertex shader, and a fragment shader is found using the same file name with a ".frag" suffix. And vice versa if the
 * file suffix is ".frag". These default suffixes can be changed in the ShaderProgramLoader constructor.
 * <p>
 * For all other file suffixes, the same file is used for both (and therefore should internally distinguish between the programs
 * using preprocessor directives and {@link ShaderProgram#prependVertexCode} and {@link ShaderProgram#prependFragmentCode}).
 * <p>
 * The above default behavior for finding the files can be overridden by explicitly setting the file names in a
 * {@link ShaderProgramParameter}. The parameter can also be used to prepend code to the programs.
 * @author cypherdare */
public class ShaderProgramLoader extends AsynchronousAssetLoader<ShaderProgram, ShaderProgramLoader.ShaderProgramParameter> {

	private String vertexFileSuffix = ".vert";
	private String fragmentFileSuffix = ".frag";

	public ShaderProgramLoader (FileHandleResolver resolver) {
		super(resolver);
	}

	public ShaderProgramLoader (FileHandleResolver resolver, String vertexFileSuffix, String fragmentFileSuffix) {
		super(resolver);
		this.vertexFileSuffix = vertexFileSuffix;
		this.fragmentFileSuffix = fragmentFileSuffix;
	}

	@Override
	public Array<AssetDescriptor> getDependencies (String fileName, FileHandle file, ShaderProgramParameter parameter) {
		return null;
	}

	@Override
	public void loadAsync (AssetManager manager, String fileName, FileHandle file, ShaderProgramParameter parameter) {
	}

	@Override
	public ShaderProgram loadSync (AssetManager manager, String fileName, FileHandle file, ShaderProgramParameter parameter) {
		String vertFileName = null, fragFileName = null;
		if (parameter != null) {
			if (parameter.vertexFile != null) vertFileName = parameter.vertexFile;
			if (parameter.fragmentFile != null) fragFileName = parameter.fragmentFile;
		}
		if (vertFileName == null && fileName.endsWith(fragmentFileSuffix)) {
			vertFileName = fileName.substring(0, fileName.length() - fragmentFileSuffix.length()) + vertexFileSuffix;
		}
		if (fragFileName == null && fileName.endsWith(vertexFileSuffix)) {
			fragFileName = fileName.substring(0, fileName.length() - vertexFileSuffix.length()) + fragmentFileSuffix;
		}
		FileHandle vertexFile = vertFileName == null ? file : resolve(vertFileName);
		FileHandle fragmentFile = fragFileName == null ? file : resolve(fragFileName);
		String vertexCode = vertexFile.readString();
		String fragmentCode = vertexFile.equals(fragmentFile) ? vertexCode : fragmentFile.readString();
		if (parameter != null) {
			if (parameter.prependVertexCode != null) vertexCode = parameter.prependVertexCode + vertexCode;
			if (parameter.prependFragmentCode != null) fragmentCode = parameter.prependFragmentCode + fragmentCode;
		}

		ShaderProgram shaderProgram = new ShaderProgram(vertexCode, fragmentCode);
		if ((parameter == null || parameter.logOnCompileFailure) && !shaderProgram.isCompiled()) {
			manager.getLogger().error("ShaderProgram " + fileName + " failed to compile:\n" + shaderProgram.getLog());
		}

		return shaderProgram;
	}

	static public class ShaderProgramParameter extends AssetLoaderParameters<ShaderProgram> {
		/** File name to be used for the vertex program instead of the default determined by the file name used to submit this asset
		 * to AssetManager. */
		public String vertexFile;
		/** File name to be used for the fragment program instead of the default determined by the file name used to submit this
		 * asset to AssetManager. */
		public String fragmentFile;
		/** Whether to log (at the error level) the shader's log if it fails to compile. Default true. */
		public boolean logOnCompileFailure = true;
		/** Code that is always added to the vertex shader code. This is added as-is, and you should include a newline (`\n`) if
		 * needed. {@linkplain ShaderProgram#prependVertexCode} is placed before this code. */
		public String prependVertexCode;
		/** Code that is always added to the fragment shader code. This is added as-is, and you should include a newline (`\n`) if
		 * needed. {@linkplain ShaderProgram#prependFragmentCode} is placed before this code. */
		public String prependFragmentCode;
	}
}
