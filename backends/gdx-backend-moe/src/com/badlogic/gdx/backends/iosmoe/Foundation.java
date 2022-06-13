package com.badlogic.gdx.backends.iosmoe;

import apple.uikit.UIDevice;

public class Foundation {

    public static int getMajorSystemVersion() {
        return Integer.parseInt(UIDevice.currentDevice().systemVersion().split("\\.")[0]);
    }
}
