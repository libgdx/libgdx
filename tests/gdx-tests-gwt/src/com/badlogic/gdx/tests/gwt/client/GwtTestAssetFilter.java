
package com.badlogic.gdx.tests.gwt.client;

import com.badlogic.gdx.backends.gwt.preloader.DefaultAssetFilter;

public class GwtTestAssetFilter extends DefaultAssetFilter {
	@Override
	public boolean preload (String file) {
		return !file.contains("gwt_lazy");
	}
}
