
package com.badlogic.gdx.assets;

public interface ReferenceCountedAsset {
	public void incRefCount ();

	public int getRefCount ();
}
