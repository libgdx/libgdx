
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
			Instancer instancer = new Instancer() {

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
			};
			list.add(instancer);
		}
		return list.toArray(new Instancer[0]);
	}
}
