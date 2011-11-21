package com.badlogic.gdx.assets.loaders.resolvers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;

public class ExternalInternalFileHandleResolver implements FileHandleResolver {
	@Override
	public FileHandle resolve (String fileName) {
		FileHandle external = Gdx.files.external(fileName);
		if(external.exists()) return external;
		else return Gdx.files.internal(fileName);
	}
}
