package com.badlogic.gdx.assets.loaders;

import com.badlogic.gdx.files.FileHandle;

public interface FileHandleResolver {
	public FileHandle resolve(String fileName);
}
