package ${package}.client;

import ${package}.${main};
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.gwt.GwtApplication;
import com.badlogic.gdx.backends.gwt.GwtApplicationConfiguration;

public class ${main}Html extends GwtApplication {
	@Override
	public ApplicationListener getApplicationListener() {
		return new ${main}();
	}

	@Override
	public GwtApplicationConfiguration getConfig() {
		return new GwtApplicationConfiguration(480, 320);
	}
}
