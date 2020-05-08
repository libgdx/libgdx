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
import com.badlogic.gdx.graphics.glutils.ShaderPart;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShaderStage;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;

/** {@link AssetLoader} for {@link ShaderProgram} instances loaded from text files. 
 * Since asset loader only support one file reference, any of files can be used. 
 * The file base name is used to load all shader files following official convention see {@link ShaderStage}
 * <p>
 * The above default behavior for finding the files can be overridden by explicitly setting the file names in a
 * {@link ShaderProgramParameter}. The parameter can also be used to prepend code to individual parts.
 * @author cypherdare */
public class ShaderProgramLoader extends AsynchronousAssetLoader<ShaderProgram, ShaderProgramLoader.ShaderProgramParameter> {

	private Array<ShaderFile> shaderFiles;

	public ShaderProgramLoader (FileHandleResolver resolver) {
		super(resolver);
	}

	@Override
	public Array<AssetDescriptor> getDependencies (String fileName, FileHandle file, ShaderProgramParameter parameter) {
		return null;
	}

	@Override
	public void loadAsync (AssetManager manager, String fileName, FileHandle file, ShaderProgramParameter parameter) {
		shaderFiles = new Array<ShaderFile>();
		
		// find shader stage for original filename based on its suffix.
		ShaderStage assetStage = null;
		for(ShaderStage stage : ShaderStage.stages) {
			if(fileName.endsWith(stage.suffix)){
				assetStage = stage;
			}
		}
		if(assetStage == null) {
			throw new GdxRuntimeException("Cannot determine shader stage for asset " + fileName);
		}
		shaderFiles.add(new ShaderFile(assetStage, fileName));
		
		// build shader list.
		if (parameter != null && parameter.shaderFiles != null) {
			shaderFiles.addAll(parameter.shaderFiles);
		} else {
			final String basename = file.nameWithoutExtension();
			final String basepath = file.parent() != null ? file.parent().child(basename).path() : basename;
			for(ShaderStage stage : ShaderStage.stages) {
				if(stage != assetStage) {
					appendShaderFile(stage, basepath + stage.suffix);
				}
			}
		}
		
		// prepend shader code.
		for(ShaderFile shaderFile : shaderFiles) {
			String code = resolve(shaderFile.filename).readString();
			if(shaderFile.prependCode != null){
				code = shaderFile.prependCode + code;
			}
			shaderFile.code = code;
		}
	}

	private void appendShaderFile (ShaderStage stage, String filename) {
		FileHandle file = resolve(filename);
		if(file.exists()){
			ShaderFile shaderFile = new ShaderFile(stage, file.path());
			shaderFiles.add(shaderFile);
		}
	}

	@Override
	public ShaderProgram loadSync (AssetManager manager, String fileName, FileHandle file, ShaderProgramParameter parameter) {
		
		final Array<ShaderPart> shaders = new Array<ShaderPart>();
		for(ShaderFile shaderFile : shaderFiles) {
			shaders.add(new ShaderPart(shaderFile.stage, shaderFile.code));
		}
		shaderFiles = null;
		ShaderProgram shaderProgram = new ShaderProgram(shaders);
		if ((parameter == null || parameter.logOnCompileFailure) && !shaderProgram.isCompiled()) {
			manager.getLogger().error("ShaderProgram " + fileName + " failed to compile:\n" + shaderProgram.getLog());
		}

		return shaderProgram;
	}

	static public class ShaderFile {
		public String filename;
		public String prependCode;
		public ShaderStage stage;
		String code;
		public ShaderFile (ShaderStage stage, String filename, String prependCode) {
			super();
			this.stage = stage;
			this.filename = filename;
			this.prependCode = prependCode;
		}
		public ShaderFile (ShaderStage stage, String filename) {
			this(stage, filename, null);
		}
	}
	static public class ShaderProgramParameter extends AssetLoaderParameters<ShaderProgram> {
		/** Additional shader parts. Set a null value to enable default loader behavior (automatic
		 * files loading based on filename suffix).
		 * Set additional shader parts to override default loader behavior. 
		 * Default is null. */
		public ShaderFile [] shaderFiles;
		/** Whether to log (at the error level) the shader's log if it fails to compile. Default true. */
		public boolean logOnCompileFailure = true;
	}
}
