package com.badlogic.gdx.backends.gwt.preloader;


public class DefaultAssetFilter implements AssetFilter {
	private String extension (String file) {
		String name = file;
		int dotIndex = name.lastIndexOf('.');
		if (dotIndex == -1) return "";
		return name.substring(dotIndex + 1);
	}
	
	@Override
	public boolean accept (String file, boolean isDirectory) {
		if(isDirectory && file.endsWith(".svn")) return false;
		return true;
	}

	@Override
	public AssetType getType (String file) {
		String extension = extension(file).toLowerCase();
		if(isImage(extension)) return AssetType.Image;
		if(isAudio(extension)) return AssetType.Audio;
		if(isText(extension)) return AssetType.Text;
		return AssetType.Binary;
	}

	private boolean isImage (String extension) {
		return extension.equals("jpg") || 
				 extension.equals("png") ||
				 extension.equals("bmp") ||
				 extension.equals("gif");
	}


	private boolean isText (String extension) {
		return extension.equals("json") ||
				 extension.equals("xml") ||
				 extension.equals("txt") ||
				 extension.equals("glsl") ||
				 extension.equals("fnt") ||
				 extension.equals("pack") ||
				 extension.equals("obj");
	}

	private boolean isAudio (String extension) {
		return extension.equals("mp3") ||
				 extension.equals("ogg") ||
				 extension.equals("wav");
	}
}
