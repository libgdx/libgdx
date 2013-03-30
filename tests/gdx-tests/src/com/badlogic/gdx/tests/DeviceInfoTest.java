package com.badlogic.gdx.tests;

//import com.badlogic.gdx.DeviceInfo;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.IntMap;

public class DeviceInfoTest extends GdxTest {

	@Override
	public boolean needsGL20 () {
		return false;
	}

	@Override
	public void create () {
		/*DeviceInfo info = Gdx.app.getDeviceInfo();
		int[] keys = info.keys();
		int cnt = 0;
		for (int i = 0; i < keys.length; i++) {
			int key = keys[i];
			Gdx.app.log("DeviceInfo", "key = "+key+"; value = "+info.value(key));
			cnt++;
		}
		Gdx.app.log("DeviceInfo", "Total of "+cnt+" values");*/
	}

	@Override
	public void render () {
	}
}