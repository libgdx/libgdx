package com.badlogic.gdx.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Timer.Task;

public class TimerTest extends GdxTest {
	@Override
	public void create () {
		new Timer().scheduleTask(new Task() {
			@Override
			public void run () {
				Gdx.app.log("TimerTest", "ping");
			}
		}, 0, 1);
	}
}
