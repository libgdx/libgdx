package com.badlogic.gdx.backends.android.red;

import com.jfixby.cmns.api.assets.AssetID;
import com.jfixby.cmns.api.log.L;
import com.jfixby.cmns.api.sys.Sys;
import com.jfixby.r3.api.ui.unit.Unit;
import com.jfixby.r3.fokker.api.UnitSpawnerComponent;

public class AndroidUnitsSpawner implements UnitSpawnerComponent {

	@Override
	public Unit spawnUnit(AssetID unit_id) {
		String string_name = unit_id.toString();
		Class<?> clazz;
		try {
			clazz = Class.forName(string_name);
			Unit unit_instance = (Unit) clazz.newInstance();
			return unit_instance;
		} catch (Throwable e) {
			L.e("Unknown unit class: " + unit_id);
			e.printStackTrace();
			Sys.exit();
		}
		return null;
	}

}