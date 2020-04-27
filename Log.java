package com.badlogic.gdx.utils;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;

public class Log {
    private final static String TAG = "LibGDX:  ";
    
    private static boolean isLevelEnabled(int logLevel) {
    	return logLevel <= Gdx.app.getLogLevel();
    }

    public static void debug(String format, Object...params) {
    	if (isLevelEnabled(Application.LOG_DEBUG))
    		Gdx.app.debug(TAG, String.format(format, params));
    }

    public static void debug(Object value) {
    	if (isLevelEnabled(Application.LOG_DEBUG))
    		debug("%s", value);
    }

    public static void info(String format, Object...params) {
    	if (isLevelEnabled(Application.LOG_INFO))
    		Gdx.app.log(TAG, String.format(format, params));
    }

    public static void info(Object value) {
    	if (isLevelEnabled(Application.LOG_INFO))
    		info("%s", value);
    }

    public static void error(String format, Object...params) {
    	if (isLevelEnabled(Application.LOG_ERROR))
    		Gdx.app.error(TAG, String.format(format, params));
    }

    public static void error(Throwable exception, String format, Object...params) {
    	if (isLevelEnabled(Application.LOG_ERROR))
    		Gdx.app.error(TAG, String.format(format, params), exception);
    }
}