
package com.badlogic.gdx.assets.loaders.resolvers;

import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;

/** {@link FileHandleResolver} that adds a prefix to the filename before passing it to the base resolver. Can be used e.g. to use a
 * given subfolder from the base resolver. The prefix is added as is, you have to include any trailing '/' character if needed.
 * @author Xoppa */
public class PrefixFileHandleResolver implements FileHandleResolver {
	private String prefix;
	private FileHandleResolver baseResolver;
	
	public PrefixFileHandleResolver(FileHandleResolver baseResolver, String prefix) {
		this.baseResolver = baseResolver;
		this.prefix = prefix;
	}
	
	public void setBaseResolver(FileHandleResolver baseResolver) {
		this.baseResolver = baseResolver;
	}
	
	public FileHandleResolver getBaseResolver() {
		return baseResolver;
	}

	public void setPrefix (String prefix) {
		this.prefix = prefix;
	}

	public String getPrefix () {
		return prefix;
	}

	@Override
	public FileHandle resolve (String fileName) {
		return baseResolver.resolve(prefix + fileName);
	}
}
