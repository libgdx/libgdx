package com.badlogic.gdx.setup;

/** @author Kotcrab */
public class ExternalExtensionDependency {
	public final String text;
	/** Indicates that this dependency is not internal part of extension. If set to true this dependency text must not
	 * be modified to append extension version. See {@link ExternalExtension#getPlatformDependencies(String)}*/
	public final boolean external;

	public ExternalExtensionDependency (String text, boolean external) {
		this.text = text;
		this.external = external;
	}
}
