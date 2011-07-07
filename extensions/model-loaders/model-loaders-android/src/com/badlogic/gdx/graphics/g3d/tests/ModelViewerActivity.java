package com.badlogic.gdx.graphics.g3d.tests;

import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.graphics.g3d.test.KeyframedModelViewer;
import com.badlogic.gdx.graphics.g3d.test.QbobViewer;
import com.badlogic.gdx.graphics.g3d.test.SkeletonModelViewer;

public class ModelViewerActivity extends AndroidApplication {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        config.useAccelerometer = false;
        config.useCompass = false;
        config.useWakelock = true;
        
//        initialize(new QbobViewer(), config);
        initialize(new KeyframedModelViewer("data/knight.g3d", "data/knight.jpg"), config);
//        initialize(new SkeletonModelViewer("data/ninja.mesh.xml", "data/ninja.jpg"), config);
    }
}