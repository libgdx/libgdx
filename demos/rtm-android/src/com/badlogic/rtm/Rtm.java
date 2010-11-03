package com.badlogic.rtm;

import android.content.pm.ActivityInfo;
import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;

public class Rtm extends AndroidApplication {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        initialize(new LevelRenderer(), false);
    }
}