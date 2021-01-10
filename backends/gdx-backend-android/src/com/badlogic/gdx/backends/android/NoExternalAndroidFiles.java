package com.badlogic.gdx.backends.android;

import android.content.ContextWrapper;
import android.content.res.AssetManager;

/**
 * Use this AndroidFiles implementation to avoid access to getExternalFilesDir - see issue #6345
 */
public class NoExternalAndroidFiles extends DefaultAndroidFiles {

    public NoExternalAndroidFiles(AssetManager assets, ContextWrapper contextWrapper) {
        super(assets, contextWrapper);
    }

    @Override
    protected String initExternalFilesPath(ContextWrapper contextWrapper) {
        return null;
    }
}
