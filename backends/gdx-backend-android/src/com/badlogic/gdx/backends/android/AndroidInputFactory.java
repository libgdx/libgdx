package com.badlogic.gdx.backends.android;

import java.lang.reflect.Constructor;

import android.content.Context;

import com.badlogic.gdx.Application;

/**
 * Class that instantiates AndroidInput or AndroidInputThreePlus
 * depending on the SDK level, via reflection.
 * @author mzechner
 *
 */
public class AndroidInputFactory {
	public static AndroidInput newAndroidInput(Application activity, Context context, Object view, AndroidApplicationConfiguration config) {
		try {
			Class<?> clazz = null;
			AndroidInput input = null;
			
			int sdkVersion = Integer.parseInt(android.os.Build.VERSION.SDK);
			if (sdkVersion >= 12) {
				clazz = Class.forName("com.badlogic.gdx.backends.android.AndroidInputThreePlus");
			} else {
				clazz = Class.forName("com.badlogic.gdx.backends.android.AndroidInput");
			}
			
			Constructor<?> constructor = clazz.getConstructor(Application.class, Context.class, Object.class, AndroidApplicationConfiguration.class);
			input = (AndroidInput)constructor.newInstance(activity, context, view, config);
			return input;
		} catch(Exception e) {
			throw new RuntimeException("Couldn't construct AndroidInput, this should never happen", e);
		}
	}
}
