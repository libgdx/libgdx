
package com.badlogicgames.gdx.tests;

import com.badlogic.gdx.tests.AbstractTestWrapper;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.tests.utils.GdxTests;

import java.lang.reflect.InvocationTargetException;

public class IOSTestWapper extends AbstractTestWrapper {
	@Override
	protected Instancer[] getTestList () {
		return GdxTests.tests.stream().map(aClass -> new Instancer() {

			@Override
			public GdxTest instance () {
				try {
					return aClass.getConstructor().newInstance();
				} catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
					e.printStackTrace();
				}
				return null;
			}

			@Override
			public String getSimpleName () {
				return aClass.getSimpleName();
			}
		}).toArray(Instancer[]::new);
	}
}
