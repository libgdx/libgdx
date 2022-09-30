
package com.badlogicgames.gdx.tests;

import com.badlogic.gdx.tests.AbstractTestWrapper;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.tests.utils.GdxTests;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class IOSTestWapper extends AbstractTestWrapper {
	@Override
	protected Instancer[] getTestList () {
		List<Instancer> list = new ArrayList<>();
		for (Class<? extends GdxTest> aClass : GdxTests.tests) {
			final Class<? extends GdxTest> testClass = aClass;
			Instancer instancer = new Instancer() {
				@Override
				public GdxTest instance () {
					try {
						return testClass.getConstructor().newInstance();
					} catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
						e.printStackTrace();
					}
					return null;
				}

				@Override
				public String getSimpleName () {
					return testClass.getSimpleName();
				}
			};
			list.add(instancer);
		}
		return list.toArray(new Instancer[0]);
	}
}
